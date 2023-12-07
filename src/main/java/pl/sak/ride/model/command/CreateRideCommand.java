package pl.sak.ride.model.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sak.ride.validations.uuid.ValidUuid;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CreateRideCommand {

    private String driverNode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = STRING)
    @Future(message = "START_TIME_NOT_PAST")
    @NotNull(message = "START_TIME_NOT_NULL")
    private LocalDateTime startTime;
    @NotBlank(message = "START_LOCATION_NOT_BLANK")
    private String startLocation;
    @NotBlank(message = "END_LOCATION_NOT_BLANK")
    private String endLocation;
    @ValidUuid(message = "INVALID_CUSTOMER_UUID_FORMAT")
    @NotNull(message = "CUSTOMER_UUID_NOT_NULL")
    private UUID customerUuid;
    @ValidUuid(message = "INVALID_DRIVER_UUID_FORMAT")
    @NotNull(message = "DRIVER_UUID_NOT_NULL")
    private UUID driverUuid;
}
