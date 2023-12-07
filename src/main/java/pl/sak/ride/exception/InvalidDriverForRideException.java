package pl.sak.ride.exception;

import java.util.UUID;

public class InvalidDriverForRideException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Driver with uuid: %s does not belong to the ride with uuid: %s.";

    public InvalidDriverForRideException(UUID driverUuid, UUID rideUuid) {
        super(String.format(ERROR_MESSAGE, driverUuid, rideUuid));
    }
}
