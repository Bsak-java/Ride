package pl.sak.ride.model.response;

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
public class DriverRideResponse {

    private String message;
    private UUID rideUuid;
    private UUID driverUuid;
}
