package pl.sak.ride.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sak.ride.enums.Status;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RideConfirmationResponse {

    private String message;
    private Status status;
    private UUID rideUuid;
    private UUID driverUuid;
}
