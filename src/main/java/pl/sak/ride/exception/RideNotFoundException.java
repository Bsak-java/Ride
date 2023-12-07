package pl.sak.ride.exception;

public class RideNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Ride with id: %d not found!";

    public RideNotFoundException(long id) {
        super(String.format(ERROR_MESSAGE, id));
    }
}
