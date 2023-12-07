package pl.sak.ride.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.spi.MappingContext;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.dto.CustomerDto;
import pl.sak.ride.model.dto.DriverDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CreateRideCommandToRideConverterTest {

    @Mock
    private CustomerFeignClient customerClient;
    @Mock
    private DriverFeignClient driverClient;
    @Mock
    private MappingContext<CreateRideCommand, Ride> mappingContext;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void shouldConvertFromCreateRideCommandToRide() {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CustomerDto customerDto = CustomerDto.builder()
                .customerUuid(UUID.fromString(customerUuid))
                .build();
        DriverDto driverDto = DriverDto.builder()
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.now())
                .startLocation("test")
                .endLocation("testest")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        CreateRideCommandToRideConverter converter = new CreateRideCommandToRideConverter(customerClient, driverClient);

        when(mappingContext.getSource())
                .thenReturn(command);
        when(customerClient.findByUuid(UUID.fromString(customerUuid)))
                .thenReturn(customerDto);
        when(driverClient.findByUuid(UUID.fromString(driverUuid)))
                .thenReturn(driverDto);

        //When
        Ride result = converter.convert(mappingContext);

        //Then
        assertEquals(result.getDriverNode(), command.getDriverNode());
        assertEquals(result.getStartTime(), command.getStartTime());
        assertEquals(result.getStartLocation(), command.getStartLocation());
        assertEquals(result.getEndLocation(), command.getEndLocation());
        assertEquals(result.getCustomerUuid(), command.getCustomerUuid());
        assertEquals(result.getDriverUuid(), command.getDriverUuid());
    }
}