package pl.sak.ride.service.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.response.RideConfirmationResponse;
import pl.sak.ride.model.response.RideResponse;
import pl.sak.ride.model.dto.RideRequestDto;

@Component
@RequiredArgsConstructor
public class RideProducerService {

    private static final String TOPIC_TO_DRIVER = "ride_to_driver";
    private static final String TOPIC_TO_CUSTOMER = "ride_to_customer";
    private static final String TOPIC_TO_CONFIRMATION_DRIVER = "ride_to_driver_confirmation_responses";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendRideRequest(Ride ride) {
        RideRequestDto dto = RideRequestDto.builder()
                .rideUuid(ride.getRideUuid())
                .customerUuid(ride.getCustomerUuid())
                .driverUuid(ride.getDriverUuid())
                .driverNode(ride.getDriverNode())
                .startTime(ride.getStartTime())
                .startLocation(ride.getStartLocation())
                .endLocation(ride.getEndLocation())
                .build();
        try {
            String jsonRideRequestDto = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send(TOPIC_TO_DRIVER, jsonRideRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRideResponse(RideResponse response) {
        try {
            String jsonRideResponse = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(TOPIC_TO_CUSTOMER, jsonRideResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRideConfirmationResponseToDriver(RideConfirmationResponse response) {
        try {
            String jsonRideConfirmationResponse = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(TOPIC_TO_CONFIRMATION_DRIVER, jsonRideConfirmationResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
