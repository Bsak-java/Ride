package pl.sak.ride.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import pl.sak.ride.enums.Status;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Getter
@Setter
@ToString
@SQLDelete(sql = "UPDATE ride SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID rideUuid;
    @JsonIgnore
    @Builder.Default
    private Boolean isDeleted = false;
    private String driverNode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startLocation;
    private String endLocation;
    @Enumerated(STRING)
    private Status status;
    @JsonIgnore
    @Builder.Default
    private Boolean isCompleted = false;
    private UUID customerUuid;
    private UUID driverUuid;
}
