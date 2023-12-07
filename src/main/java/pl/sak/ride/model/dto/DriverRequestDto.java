package pl.sak.ride.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DriverRequestDto {

    private UUID driverUuid;
    private UUID rideUuid;
    private boolean isAccepted;
}
