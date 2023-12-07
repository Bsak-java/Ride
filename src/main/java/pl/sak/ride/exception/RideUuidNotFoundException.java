package pl.sak.ride.exception;

import java.util.UUID;

public class RideUuidNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Ride with uuid: %s not found!";

    public RideUuidNotFoundException(UUID rideUuid) {
        super(String.format(ERROR_MESSAGE, rideUuid));
    }
}
