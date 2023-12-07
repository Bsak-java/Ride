package pl.sak.ride.model.command;

import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class EditRidePartiallyCommand {

    private String driverNode;
    @FutureOrPresent(message = "START_TIME_NOT_PAST")
    private LocalDateTime startTime;
    private String startLocation;
    private String endLocation;
}
