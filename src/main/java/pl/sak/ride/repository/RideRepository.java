package pl.sak.ride.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sak.ride.model.Ride;

import java.util.Optional;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, Long> {

    @Query(value = "SELECT * FROM Ride r WHERE r.is_deleted = true", nativeQuery = true)
    Page<Ride> findAllDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM Ride r WHERE r.customer_uuid = :customerUuid AND r.is_completed = :isCompleted", nativeQuery = true)
    Page<Ride> findAllCompletedByCustomerUuidWithCustomer(@Param("customerUuid") UUID customerUuid, @Param("isCompleted") boolean isCompleted, Pageable pageable);

    @Query(value = "SELECT * FROM Ride r WHERE r.driver_uuid = :driverUuid AND r.is_completed = :isCompleted", nativeQuery = true)
    Page<Ride> findAllCompletedByDriverUuidWithDriver(@Param("driverUuid") UUID driverUuid, @Param("isCompleted") boolean isCompleted, Pageable pageable);

    @Query(value = "SELECT r FROM Ride r WHERE r.rideUuid = :uuid")
    Optional<Ride> findByUuid(@Param("uuid") UUID rideUuid);
}
