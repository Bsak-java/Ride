package pl.sak.ride.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Service;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.EditRideCommand;

@Service
@RequiredArgsConstructor
public class EditRideCommandToRideConverter implements Converter<EditRideCommand, Ride> {

    @Override
    public Ride convert(MappingContext<EditRideCommand, Ride> mappingContext) {
        EditRideCommand command = mappingContext.getSource();

        return Ride.builder()
                .driverNode(command.getDriverNode())
                .startTime(command.getStartTime())
                .startLocation(command.getStartLocation())
                .endLocation(command.getEndLocation())
                .build();
    }
}
