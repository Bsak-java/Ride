package pl.sak.ride.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sak.ride.exception.RideNotEditableException;
import pl.sak.ride.exception.RideNotFoundException;
import pl.sak.ride.exception.RideUuidNotFoundException;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.command.EditRideCommand;
import pl.sak.ride.model.command.EditRidePartiallyCommand;
import pl.sak.ride.model.dto.CustomerDto;
import pl.sak.ride.model.dto.DriverDto;
import pl.sak.ride.model.response.RideResponse;
import pl.sak.ride.repository.RideRepository;

import java.util.Optional;
import java.util.UUID;

import static pl.sak.ride.enums.Status.COMPLETED;
import static pl.sak.ride.enums.Status.INPROGRESS;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final ModelMapper modelMapper;
    private final DriverFeignClient driverFeignClient;
    private final CustomerFeignClient customerFeignClient;

    @Transactional
    public Ride create(CreateRideCommand command) {
        CustomerDto customer = customerFeignClient.findByUuid(command.getCustomerUuid());

        DriverDto driver = driverFeignClient.findByUuid(command.getDriverUuid());

        Ride ride = Ride.builder()
                .driverNode(command.getDriverNode())
                .startTime(command.getStartTime())
                .startLocation(command.getStartLocation())
                .endLocation(command.getEndLocation())
                .status(INPROGRESS)
                .customerUuid(customer.getCustomerUuid())
                .driverUuid(driver.getDriverUuid())
                .build();

        return rideRepository.save(ride);
    }

    @Transactional(readOnly = true)
    public Page<Ride> findAll(Pageable pageable) {
        return rideRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ride> findAllDeleted(Pageable pageable) {
        return rideRepository.findAllDeleted(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ride> findAllByCustomerUuid(UUID customerUuid, boolean isCompleted, Pageable pageable) {
        return rideRepository.findAllCompletedByCustomerUuidWithCustomer(customerUuid, isCompleted, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ride> findAllByDriverUuid(UUID driverUuid, boolean isCompleted, Pageable pageable) {
        return rideRepository.findAllCompletedByDriverUuidWithDriver(driverUuid, isCompleted, pageable);
    }

    @Transactional(readOnly = true)
    public Ride findById(long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new RideNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Ride findByUuid(UUID uuid) {
        return rideRepository.findByUuid(uuid)
                .orElseThrow(() -> new RideUuidNotFoundException(uuid));
    }

    @Transactional
    public Ride editRide(long id, EditRideCommand command) {
        Ride ride = modelMapper.map(command, Ride.class);
        return rideRepository.findById(id)
                .map(rideToEdit -> {
                    if (!COMPLETED.equals(rideToEdit.getStatus())) {
                        rideToEdit.setDriverNode(ride.getDriverNode());
                        rideToEdit.setStartTime(ride.getStartTime());
                        rideToEdit.setStartLocation(ride.getStartLocation());
                        rideToEdit.setEndLocation(ride.getEndLocation());
                        return rideRepository.save(rideToEdit);
                    } else {
                        throw new RideNotEditableException(id);
                    }
                })
                .orElseThrow(() -> new RideNotFoundException(id));
    }

    @Transactional
    public Ride editRidePartially(long id, EditRidePartiallyCommand command) {
        Ride ride = modelMapper.map(command, Ride.class);
        return rideRepository.findById(id)
                .map(rideToEdit -> {
                    if (!COMPLETED.equals(rideToEdit.getStatus())) {
                        Optional.ofNullable(ride.getDriverNode()).ifPresent(rideToEdit::setDriverNode);
                        Optional.ofNullable(ride.getStartTime()).ifPresent(rideToEdit::setStartTime);
                        Optional.ofNullable(ride.getStartLocation()).ifPresent(rideToEdit::setStartLocation);
                        Optional.ofNullable(ride.getEndLocation()).ifPresent(rideToEdit::setEndLocation);
                        return rideRepository.save(rideToEdit);
                    } else {
                        throw new RideNotEditableException(id);
                    }
                })
                .orElseThrow(() -> new RideNotFoundException(id));
    }

    @Transactional
    public RideResponse delete(long id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RideNotFoundException(id));
        rideRepository.delete(ride);
        return RideResponse.builder()
                .message("Deleted successfully.")
                .build();
    }
}
