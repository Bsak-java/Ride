package pl.sak.ride.exception;

public class RideNotEditableException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Cannot be edited ride with id: %d when the ride status is: COMPLETED.";

    public RideNotEditableException(long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
