package pl.sak.ride.model.command;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class EditRideCommand {

    private String driverNode;
    @FutureOrPresent(message = "START_TIME_NOT_PAST")
    @NotNull(message = "START_TIME_NOT_NULL")
    private LocalDateTime startTime;
    @NotBlank(message = "START_LOCATION_NOT_BLANK")
    private String startLocation;
    @NotBlank(message = "END_LOCATION_NOT_BLANK")
    private String endLocation;
}
