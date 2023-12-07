package pl.sak.ride.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DriverDto {

    private Long id;
    private UUID driverUuid;
    private String name;
    private String surname;
    private String email;
    private String pesel;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Boolean isAvailable;
}
