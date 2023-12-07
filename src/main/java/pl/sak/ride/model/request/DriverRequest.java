package pl.sak.ride.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sak.ride.validations.uuid.ValidUuid;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DriverRequest {

    @ValidUuid(message = "INVALID_RIDE_UUID_FORMAT")
    @NotNull(message = "RIDE_ID_NOT_NULL")
    private UUID rideUuid;
    @ValidUuid(message = "INVALID_DRIVER_UUID_FORMAT")
    @NotNull(message = "DRIVER_ID_NOT_NULL")
    private UUID driverUuid;
    private boolean isAccepted;
}
