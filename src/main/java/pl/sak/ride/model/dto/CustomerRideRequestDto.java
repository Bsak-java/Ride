package pl.sak.ride.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CustomerRideRequestDto {

    private UUID customerUuid;
    private String driverNode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = STRING)
    private LocalDateTime startTime;
    private String startLocation;
    private String endLocation;
}
