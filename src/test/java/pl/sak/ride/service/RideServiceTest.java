package pl.sak.ride.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pl.sak.ride.exception.RideNotEditableException;
import pl.sak.ride.exception.RideNotFoundException;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.command.EditRideCommand;
import pl.sak.ride.model.command.EditRidePartiallyCommand;
import pl.sak.ride.model.dto.CustomerDto;
import pl.sak.ride.model.dto.DriverDto;
import pl.sak.ride.repository.RideRepository;
import pl.sak.ride.service.producer.RideProducerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static pl.sak.ride.enums.Status.COMPLETED;
import static pl.sak.ride.enums.Status.INPROGRESS;

class RideServiceTest {

    private Ride ride;

    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideProducerService rideProducerService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private DriverFeignClient driverFeignClient;
    @Mock
    private CustomerFeignClient customerFeignClient;
    @InjectMocks
    private RideService rideService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        rideService = new RideService(rideRepository, modelMapper, driverFeignClient, customerFeignClient);
        String rideUuid = "550e8403-e29b-41d4-a716-446655440000";
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";
        ride = new Ride(1L,UUID.fromString(rideUuid), false, "test", LocalDateTime.now(), null,
                "test", "test", INPROGRESS, false, UUID.fromString(customerUuid), UUID.fromString(driverUuid));
    }

    @Test
    void shouldCreateRide() {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440001";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440001";
        CreateRideCommand command = CreateRideCommand.builder()
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        CustomerDto customer = CustomerDto.builder()
                .customerUuid(UUID.fromString(customerUuid))
                .build();
        DriverDto driver = DriverDto.builder()
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        when(customerFeignClient.findByUuid(UUID.fromString(customerUuid)))
                .thenReturn(customer);
        when(driverFeignClient.findByUuid(UUID.fromString(driverUuid)))
                .thenReturn(driver);
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        Ride result = rideService.create(command);

        //Then
        verify(customerFeignClient, times(1)).findByUuid(UUID.fromString(customerUuid));
        verify(driverFeignClient, times(1)).findByUuid(UUID.fromString(driverUuid));
        verify(rideRepository, times(1)).save(any(Ride.class));
        assertEquals(result, ride);
    }

    @Test
    void shouldFindAllRides() {
        //Given
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        List<Ride> expectedRides = new ArrayList<>();
        expectedRides.add(ride);
        expectedRides.add(ride2);

        when(rideRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(expectedRides));

        //When
        Page<Ride> result = rideService.findAll(pageable);

        //Then
        verify(rideRepository, times(1)).findAll(pageable);
        assertEquals(expectedRides.size(), result.getContent().size());
    }

    @Test
    void shouldFindAllDeletedRides() {
        //Given
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        ride2.setIsDeleted(true);
        ride.setIsDeleted(true);
        List<Ride> deletedRides = new ArrayList<>();
        deletedRides.add(ride);
        deletedRides.add(ride2);

        when(rideRepository.findAllDeleted(pageable))
                .thenReturn(new PageImpl<>(deletedRides));

        //When
        Page<Ride> result = rideService.findAllDeleted(pageable);

        //Then
        verify(rideRepository, times(1)).findAllDeleted(pageable);
        assertEquals(ride, result.getContent().get(0));
        assertEquals(ride2, result.getContent().get(1));
    }

    @Test
    void shouldFindAllRidesByCustomerIdWhenIsCompletedTrue() {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String customerUuid2 = "550e8400-e29b-41d4-a716-446655440001";
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        ride2.setIsCompleted(true);
        ride2.setCustomerUuid(UUID.fromString(customerUuid));
        ride.setIsCompleted(true);
        ride.setCustomerUuid(UUID.fromString(customerUuid2));

        List<Ride> completedRides = new ArrayList<>();
        completedRides.add(ride);
        completedRides.add(ride2);

        when(rideRepository.findAllCompletedByCustomerUuidWithCustomer(UUID.fromString(customerUuid), true, pageable))
                .thenReturn(new PageImpl<>(completedRides));

        //When
        Page<Ride> result = rideService.findAllByCustomerUuid(UUID.fromString(customerUuid), true, pageable);

        //Then
        verify(rideRepository, times(1)).findAllCompletedByCustomerUuidWithCustomer(UUID.fromString(customerUuid), true, pageable);
        assertEquals(ride, result.getContent().get(0));
        assertEquals(ride2, result.getContent().get(1));
    }

    @Test
    void shouldFindAllRidesByCustomerIdWhenIsCompletedFalse() {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String customerUuid2 = "550e8400-e29b-41d4-a716-446655440001";
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        ride2.setIsCompleted(true);
        ride2.setCustomerUuid(UUID.fromString(customerUuid));
        ride.setIsCompleted(true);
        ride.setCustomerUuid(UUID.fromString(customerUuid2));

        List<Ride> completedRides = new ArrayList<>();
        completedRides.add(ride);
        completedRides.add(ride2);

        when(rideRepository.findAllCompletedByCustomerUuidWithCustomer(UUID.fromString(customerUuid), false, pageable))
                .thenReturn(new PageImpl<>(completedRides));

        //When
        Page<Ride> result = rideService.findAllByCustomerUuid(UUID.fromString(customerUuid), false, pageable);

        //Then
        verify(rideRepository, times(1)).findAllCompletedByCustomerUuidWithCustomer(UUID.fromString(customerUuid), false, pageable);
        assertEquals(ride, result.getContent().get(0));
        assertEquals(ride2, result.getContent().get(1));
    }

    @Test
    void findAllRidesByDriverIdWhenIsCompletedTrue() {
        //Given
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";
        String driverUuid2 = "550e8401-e29b-41d4-a716-446655440001";
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        ride2.setIsCompleted(true);
        ride2.setDriverUuid(UUID.fromString(driverUuid));
        ride.setIsCompleted(true);
        ride.setDriverUuid(UUID.fromString(driverUuid2));

        List<Ride> completedRides = new ArrayList<>();
        completedRides.add(ride);
        completedRides.add(ride2);

        when(rideRepository.findAllCompletedByDriverUuidWithDriver(UUID.fromString(driverUuid), true, pageable))
                .thenReturn(new PageImpl<>(completedRides));

        //When
        Page<Ride> result = rideService.findAllByDriverUuid(UUID.fromString(driverUuid), true, pageable);

        //Then
        verify(rideRepository, times(1)).findAllCompletedByDriverUuidWithDriver(UUID.fromString(driverUuid), true, pageable);
        assertEquals(ride, result.getContent().get(0));
        assertEquals(ride2, result.getContent().get(1));
    }

    @Test
    void findAllRidesByDriverIdWhenIsCompletedFalse() {
        //Given
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";
        String driverUuid2 = "550e8401-e29b-41d4-a716-446655440001";
        Pageable pageable = Pageable.ofSize(5).withPage(0);

        Ride ride2 = new Ride();
        ride2.setIsCompleted(false);
        ride2.setDriverUuid(UUID.fromString(driverUuid));
        ride.setIsCompleted(false);
        ride.setDriverUuid(UUID.fromString(driverUuid2));

        List<Ride> completedRides = new ArrayList<>();
        completedRides.add(ride);
        completedRides.add(ride2);

        when(rideRepository.findAllCompletedByDriverUuidWithDriver(UUID.fromString(driverUuid), false, pageable))
                .thenReturn(new PageImpl<>(completedRides));

        //When
        Page<Ride> result = rideService.findAllByDriverUuid(UUID.fromString(driverUuid), false, pageable);

        //Then
        verify(rideRepository, times(1)).findAllCompletedByDriverUuidWithDriver(UUID.fromString(driverUuid), false, pageable);
        assertEquals(ride, result.getContent().get(0));
        assertEquals(ride2, result.getContent().get(1));
    }

    @Test
    void shouldFindByRideId() {
        //Given
        long rideId = 1L;

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride));

        //When
        Ride result = rideService.findById(rideId);

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        assertEquals(ride, result);
    }

    @Test
    void shouldEditRide() {
        //Given
        long rideId = 1L;

        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        Ride result = rideService.editRide(rideId, command);

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, times(1)).save(ride);
        assertEquals(command.getDriverNode(), result.getDriverNode());
        assertEquals(command.getStartTime(), result.getStartTime());
        assertEquals(command.getStartLocation(), result.getStartLocation());
        assertEquals(command.getEndLocation(), result.getEndLocation());
    }

    @Test
    void shouldEditRidePartially() {
        //Given
        long rideId = 1L;

        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        Ride result = rideService.editRidePartially(rideId, command);

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, times(1)).save(ride);
        assertEquals(command.getDriverNode(), result.getDriverNode());
        assertEquals(command.getStartTime(), result.getStartTime());
        assertEquals(command.getStartLocation(), result.getStartLocation());
        assertEquals(command.getEndLocation(), result.getEndLocation());
    }

    @Test
    void shouldDeleteRide() {
        //Given
        long rideId = 1L;

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride))
                .thenThrow(new RideNotFoundException(rideId));

        //When
        rideService.delete(rideId);

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, times(1)).delete(ride);
        assertThrows(RideNotFoundException.class, () -> rideService.delete(rideId));
    }

    @Test
    void shouldThrowRideNotFoundExceptionWhenRideIdDoesNotExistsForFindByIdMethod() {
        //Given
        long rideId = 1L;

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.empty());

        //When
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.findById(rideId)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        assertEquals("Ride with id: 1 not found!", exception.getMessage());
    }

    @Test
    void shouldThrowRideNotEditableExceptionWhenRideStatusIsCompletedForEditRideMethod() {
        //Given
        long rideId = 1L;
        ride.setStatus(COMPLETED);

        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        RideNotEditableException exception = assertThrows(
                RideNotEditableException.class,
                () -> rideService.editRide(rideId, command)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any(Ride.class));
        assertEquals("Cannot be edited ride with id: 1 when the ride status is: COMPLETED.", exception.getMessage());
    }

    @Test
    void shouldThrowRideNotFoundExceptionWhenRideIsDoesNotExistsForEditRideMethod() {
        //Given
        long rideId = 1L;

        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.empty());
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.editRide(rideId, command)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any(Ride.class));
        assertEquals("Ride with id: 1 not found!", exception.getMessage());
    }

    @Test
    void shouldThrowRideNotEditableExceptionWhenRideStatusIsCompletedForEditRidePartiallyMethod() {
        //Given
        long rideId = 1L;
        ride.setStatus(COMPLETED);

        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        RideNotEditableException exception = assertThrows(
                RideNotEditableException.class,
                () -> rideService.editRidePartially(rideId, command)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any(Ride.class));
        assertEquals("Cannot be edited ride with id: 1 when the ride status is: COMPLETED.", exception.getMessage());
    }

    @Test
    void shouldThrowRideNotFoundExceptionWhenRideIdDoesNotExistsForEditRidePartiallyMethod() {
        //Given
        long rideId = 1L;

        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        Ride editedRide = Ride.builder()
                .id(rideId)
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .build();

        when(modelMapper.map(any(), eq(Ride.class)))
                .thenReturn(editedRide);
        when(rideRepository.findById(rideId))
                .thenReturn(Optional.empty());
        when(rideRepository.save(any(Ride.class)))
                .thenReturn(ride);

        //When
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.editRidePartially(rideId, command)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any(Ride.class));
        assertEquals("Ride with id: 1 not found!", exception.getMessage());
    }

    @Test
    void shouldThrowRideNotFoundExceptionWhenRideIdDoesNotExistsForDeleteMethod() {
        //Given
        long rideId = 1L;

        when(rideRepository.findById(rideId))
                .thenReturn(Optional.empty());

        //When
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.delete(rideId)
        );

        //Then
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).delete(any(Ride.class));
        assertEquals("Ride with id: 1 not found!", exception.getMessage());
    }
}