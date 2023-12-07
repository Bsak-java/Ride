package pl.sak.ride.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sak.ride.enums.Status;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RideRequestResponse {

    private String message;
    private Status status;
}
