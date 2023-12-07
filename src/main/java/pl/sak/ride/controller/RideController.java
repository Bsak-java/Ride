package pl.sak.ride.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.command.EditRideCommand;
import pl.sak.ride.model.command.EditRidePartiallyCommand;
import pl.sak.ride.model.dto.RideDto;
import pl.sak.ride.model.request.CreateRideCompletedRequest;
import pl.sak.ride.model.response.RideResponse;
import pl.sak.ride.service.RideService;

import java.util.UUID;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
@Tag(name = "Ride", description = "Ride api")
public class RideController {

    private final RideService rideService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Creates ride.",
            description = "This operation allows an administrator to creates ride using the CreateRideCommand class in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CREATED")
    })
    @PostMapping
    public ResponseEntity<RideDto> create(@RequestBody @Valid CreateRideCommand command) {
        RideDto dto = modelMapper.map(rideService.create(command), RideDto.class);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @Operation(summary = "Finds rides with pagination.",
            description = "This operation enables both users and administrators to fetches all rides with pagination.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<Page<RideDto>> findAll(@PageableDefault Pageable pageable) {
        Page<RideDto> dtos = rideService.findAll(pageable)
                .map(ride -> modelMapper.map(ride, RideDto.class));
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Finds rides removed with pagination.",
            description = "This operation allows an administrator to fetches rides removed with pagination(soft delete).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/deleted")
    public ResponseEntity<Page<RideDto>> findAllDeleted(@PageableDefault Pageable pageable) {
        Page<RideDto> dtos = rideService.findAllDeleted(pageable)
                .map(ride -> modelMapper.map(ride, RideDto.class));
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Finds rides by Customer ID with pagination.",
            description = "This operation allows both users and administrators to retrieve rides for a specific customer with optional filtering by completion status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/customers/{customerUuid}")
    public ResponseEntity<Page<RideDto>> findAllByCustomerUuid(@PathVariable("customerUuid") UUID customerUuid, @RequestBody CreateRideCompletedRequest request, @PageableDefault Pageable pageable) {
        Page<RideDto> dtos = rideService.findAllByCustomerUuid(customerUuid, request.isCompleted(), pageable)
                .map(ride -> modelMapper.map(ride, RideDto.class));
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Finds rides by Driver ID with pagination.",
            description = "This operation allows both users and administrators to retrieve rides for a specific driver with optional filtering by completion status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/drivers/{driverUuid}")
    public ResponseEntity<Page<RideDto>> findAllByDriverUuid(@PathVariable("driverUuid") UUID driverUuid, @RequestBody CreateRideCompletedRequest request, @PageableDefault Pageable pageable) {
        Page<RideDto> dtos = rideService.findAllByDriverUuid(driverUuid, request.isCompleted(), pageable)
                .map(ride -> modelMapper.map(ride, RideDto.class));
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Finds ride by id.",
            description = "This operation allows an administrator to finds ride by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RideDto> findById(@PathVariable("id") long id) {
        RideDto dto = modelMapper.map(rideService.findById(id), RideDto.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Finds ride by uuid.",
            description = "This operation allows an administrator to finds ride by uuid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/by-uuid/{uuid}")
    public ResponseEntity<RideDto> findByUuid(@PathVariable("uuid") UUID uuid) {
        RideDto dto = modelMapper.map(rideService.findByUuid(uuid), RideDto.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Edits ride details.",
            description = "This operation allows an administrator to edit details using the id ride and " +
                    "EditRideCommand class in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RideDto> editRide(@PathVariable("id") long id, @RequestBody @Valid EditRideCommand command) {
        Ride ride = rideService.editRide(id, command);
        RideDto dto = modelMapper.map(ride, RideDto.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Edits ride details partially.",
            description = "This operation allows an administrator to edit details partially using the id ride and " +
                    "EditRidePartiallyCommand class in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<RideDto> editRidePartially(@PathVariable("id") long id, @RequestBody @Valid EditRidePartiallyCommand command) {
        Ride ride = rideService.editRidePartially(id, command);
        RideDto dto = modelMapper.map(ride, RideDto.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Removes ride.",
            description = "This operation allows an administrator to removes ride using the id ride in the body(soft delete).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "NO_CONTENT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<RideResponse> delete(@PathVariable("id") long id) {
        RideResponse response = rideService.delete(id);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
