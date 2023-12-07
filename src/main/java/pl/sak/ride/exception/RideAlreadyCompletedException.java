package pl.sak.ride.exception;

import java.util.UUID;

public class RideAlreadyCompletedException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Ride with uuid: %s has already been completed.";

    public RideAlreadyCompletedException(UUID rideUuid) {
        super(String.format(ERROR_MESSAGE, rideUuid));
    }
}
