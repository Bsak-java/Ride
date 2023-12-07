package pl.sak.ride.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.dto.CustomerDto;
import pl.sak.ride.model.dto.DriverDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRideCommandToRideConverter implements Converter<CreateRideCommand, Ride> {

    private final CustomerFeignClient customerFeignClient;
    private final DriverFeignClient driverFeignClient;

    @Override
    public Ride convert(MappingContext<CreateRideCommand, Ride> mappingContext) {
        CreateRideCommand command = mappingContext.getSource();
        CustomerDto customer = customerFeignClient.findByUuid(command.getCustomerUuid());
        DriverDto driver = driverFeignClient.findByUuid(command.getDriverUuid());
        return Ride.builder()
                .rideUuid(UUID.randomUUID())
                .driverNode(command.getDriverNode())
                .startTime(command.getStartTime())
                .startLocation(command.getStartLocation())
                .endLocation(command.getEndLocation())
                .customerUuid(customer.getCustomerUuid())
                .driverUuid(driver.getDriverUuid())
                .build();
    }
}
