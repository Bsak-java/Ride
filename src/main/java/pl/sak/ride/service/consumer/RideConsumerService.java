package pl.sak.ride.service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sak.ride.exception.InvalidDriverForRideException;
import pl.sak.ride.exception.RideAlreadyCompletedException;
import pl.sak.ride.exception.RideUuidNotFoundException;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.dto.CompleteRequestDto;
import pl.sak.ride.model.dto.CustomerRideRequestDto;
import pl.sak.ride.model.dto.DriverRequestDto;
import pl.sak.ride.repository.RideRepository;
import pl.sak.ride.service.EmailService;
import pl.sak.ride.service.NotificationService;
import pl.sak.ride.service.producer.RideProducerService;

import java.time.LocalDateTime;
import java.util.UUID;

import static pl.sak.ride.enums.Status.ACCEPTED;
import static pl.sak.ride.enums.Status.COMPLETED;
import static pl.sak.ride.enums.Status.NEW;
import static pl.sak.ride.enums.Status.REJECTED;

@Service
@RequiredArgsConstructor
public class RideConsumerService {

    private final RideRepository rideRepository;
    private final RideProducerService rideProducerService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final DriverFeignClient driverFeignClient;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "customer_to_ride", groupId = "ride-group")
    public void handleRequestRideFromCustomer(String jsonCustomerRideRequestDto) {
        try {
            CustomerRideRequestDto dto = objectMapper.readValue(jsonCustomerRideRequestDto, CustomerRideRequestDto.class);
            if (driverFeignClient.findAllAvailable(Pageable.unpaged()).isEmpty()) {
                notificationService.notifyCustomerAboutNotAvailableDrivers();
                return;
            }

            Ride ride = customerRideRequestToRide(dto);

            rideProducerService.sendRideRequest(ride);
            rideRepository.save(ride);

//            CompletableFuture<DriverRequestDto> responseFuture = new CompletableFuture<>();
//
//            try {
//                DriverRequestDto response = responseFuture.get(30, TimeUnit.SECONDS);
//                handleRideResponseFromDriver(jsonCustomerRideRequestDto);
//            } catch (ExecutionException | InterruptedException | TimeoutException e) {
//                notificationService.notifyCustomerAboutTimeoutRide();
//                e.printStackTrace();
//            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "driver_to_ride", groupId = "ride-group")
    public void handleRideResponseFromDriver(String jsonDriverRequestDto) {
        try {
            DriverRequestDto response = objectMapper.readValue(jsonDriverRequestDto, DriverRequestDto.class);
            Ride ride = rideRepository.findByUuid(response.getRideUuid())
                    .orElseThrow(() -> new RideUuidNotFoundException(response.getRideUuid()));

            if (response.isAccepted()) {
                emailService.sendConfirmedRideToCustomer(ride);
                notificationService.notifyCustomerAboutAcceptedRide(response);
                ride.setDriverUuid(response.getDriverUuid());
                ride.setStatus(ACCEPTED);
                rideRepository.save(ride);
            } else {
                emailService.sendRejectedRideToCustomer(ride);
                notificationService.notifyCustomerAboutRejectedRide();
                ride.setDriverUuid(response.getDriverUuid());
                ride.setStatus(REJECTED);
                rideRepository.save(ride);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @KafkaListener(topics = "driver_to_ride_confirmation_responses", groupId = "ride-group")
    public void handleRideConfirmationResponseFromDriver(String jsonCompleteRequestDto) {
        try {
            CompleteRequestDto request = objectMapper.readValue(jsonCompleteRequestDto, CompleteRequestDto.class);
            Ride ride = rideRepository.findByUuid(request.getRideUuId())
                    .orElseThrow(() -> new RideUuidNotFoundException(request.getRideUuId()));

            if (ride.getDriverUuid().equals(request.getDriverUuId())) {
                if (ride.getStatus() != COMPLETED) {
                    ride.setStatus(COMPLETED);
                    ride.setEndTime(LocalDateTime.now());
                    ride.setIsCompleted(true);

                    emailService.sendCompletedRideToCustomer(ride);
                    notificationService.notifyDriverAboutCompletedRide(ride);
                    rideRepository.save(ride);
                } else {
                    throw new RideAlreadyCompletedException(request.getRideUuId());
                }
            } else {
                throw new InvalidDriverForRideException(request.getDriverUuId(), request.getRideUuId());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Ride customerRideRequestToRide(CustomerRideRequestDto customerRequest) {
        return Ride.builder()
                .rideUuid(UUID.randomUUID())
                .driverNode(customerRequest.getDriverNode())
                .startTime(customerRequest.getStartTime())
                .startLocation(customerRequest.getStartLocation())
                .endLocation(customerRequest.getEndLocation())
                .status(NEW)
                .isCompleted(false)
                .customerUuid(customerRequest.getCustomerUuid())
                .driverUuid(null)
                .build();
    }
}
