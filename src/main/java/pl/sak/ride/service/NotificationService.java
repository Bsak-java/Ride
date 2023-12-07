package pl.sak.ride.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.dto.DriverDto;
import pl.sak.ride.model.dto.DriverRequestDto;
import pl.sak.ride.model.response.RideConfirmationResponse;
import pl.sak.ride.model.response.RideResponse;
import pl.sak.ride.service.producer.RideProducerService;

import static pl.sak.ride.enums.Status.COMPLETED;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DriverFeignClient driverFeignClient;
    private final RideProducerService rideProducerService;

    public void notifyCustomerAboutAcceptedRide(DriverRequestDto driverResponse) {
        DriverDto driver = driverFeignClient.findByUuid(driverResponse.getDriverUuid());

        RideResponse response = RideResponse.builder()
                .message("Your ride response is accepted by driver. Have a nice ride!")
                .rideUuid(driverResponse.getRideUuid())
                .driverUuid(driverResponse.getDriverUuid())
                .driverName(driver.getName())
                .driverSurname(driver.getSurname())
                .driverPhoneNumber(driver.getPhoneNumber())
                .build();

        rideProducerService.sendRideResponse(response);
    }

    public void notifyCustomerAboutRejectedRide() {
        RideResponse response = RideResponse.builder()
                .message("Your ride request is rejected by driver. Try again!")
                .build();

        rideProducerService.sendRideResponse(response);
    }

    public void notifyCustomerAboutTimeoutRide() {
        RideResponse response = RideResponse.builder()
                .message("Sorry, we couldn't find a driver in time. Please try again.")
                .build();

        rideProducerService.sendRideResponse(response);
    }

    public void notifyCustomerAboutNotAvailableDrivers() {
        RideResponse response = RideResponse.builder()
                .message("Currently, there are no available drivers. Please try again later.")
                .build();

        rideProducerService.sendRideResponse(response);
    }

    public void notifyDriverAboutCompletedRide(Ride ride) {
        RideConfirmationResponse response = RideConfirmationResponse.builder()
                .message("Your ride has been completed successfully.")
                .status(COMPLETED)
                .rideUuid(ride.getRideUuid())
                .driverUuid(ride.getDriverUuid())
                .build();

        rideProducerService.sendRideConfirmationResponseToDriver(response);
    }
}
