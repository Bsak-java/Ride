package pl.sak.ride.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.sak.ride.DatabaseCleaner;
import pl.sak.ride.RideApplication;
import pl.sak.ride.config.feign.WireMockConfig;
import pl.sak.ride.exception.dto.ValidationErrorDto;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.command.CreateRideCommand;
import pl.sak.ride.model.command.EditRideCommand;
import pl.sak.ride.model.command.EditRidePartiallyCommand;
import pl.sak.ride.model.request.CreateRideCompletedRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.sak.ride.config.feign.CustomerDtoMocks.setupMockCustomerDtoResponse;
import static pl.sak.ride.config.feign.DriverDtoMocks.setupMockDriverDtoResponse;

@SpringBootTest(classes = RideApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        },
        controlledShutdown = true,
        brokerPropertiesLocation = "classpath:embedded-kafka-broker-test.yml"
)
@DirtiesContext
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfig.class})
@ActiveProfiles("test")
class RideControllerIT {

    private final MockMvc postman;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    private WireMockServer mockCustomerService;
    @Autowired
    private WireMockServer mockDriverService;
    @Autowired
    private CustomerFeignClient customerClient;
    @Autowired
    private DriverFeignClient driverClient;

    @Autowired
    public RideControllerIT(MockMvc postman, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner) {
        this.postman = postman;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
    }

    @BeforeEach
    void setUp() throws IOException {
        setupMockCustomerDtoResponse(mockCustomerService);
        setupMockDriverDtoResponse(mockDriverService);
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

//    @Test
//    void shouldCreateRide() throws Exception {
//        //Given
//        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
//        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";
//
//        CreateRideCommand command = CreateRideCommand.builder()
//                .driverNode("test")
//                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
//                .startLocation("test")
//                .endLocation("testest")
//                .customerUuid(UUID.fromString(customerUuid))
//                .driverUuid(UUID.fromString(driverUuid))
//                .build();
//
//        String json = objectMapper.writeValueAsString(command);
//
//        //When
//        postman.perform(get("/rides/6"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.code").value(404))
//                .andExpect(jsonPath("$.status").value("Not Found"))
//                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
//                .andExpect(jsonPath("$.uri").value("/rides/6"))
//                .andExpect(jsonPath("$.method").value("GET"));
//
//        postman.perform(post("/rides")
//                        .contentType(APPLICATION_JSON)
//                        .content(json))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(6))
////                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440005"))
//                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
//                .andExpect(jsonPath("$.startTime").value("2023-12-12 12:30"))
//                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
//                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
//                .andExpect(jsonPath("$.customerUuid").value(command.getCustomerUuid()))
//                .andExpect(jsonPath("$.driverUuid").value(command.getDriverUuid()));
//
//        //Then
//        postman.perform(get("/rides/6"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(6))
//
//                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
//                .andExpect(jsonPath("$.startTime").value("2023-12-12 12:30"))
//                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
//                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
//                .andExpect(jsonPath("$.customerUuid").value(command.getCustomerUuid()))
//                .andExpect(jsonPath("$.driverUuid").value(command.getDriverUuid()));
//    }

    @Test
    void shouldFindAllRides() throws Exception {
        //Given
        //When
        //Then
        postman.perform(get("/rides")
                        .param("size", "3")
                        .param("page", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.content[0].startLocation").value("15.151515"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.202020"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.content[1].driverNode").value(""))
                .andExpect(jsonPath("$.content[1].startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.content[1].endTime").isEmpty())
                .andExpect(jsonPath("$.content[1].startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.content[1].endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.content[1].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[1].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[2].id").value(3))
                .andExpect(jsonPath("$.content[2].rideUuid").value("550e8402-e29b-41d4-a716-446655440002"))
                .andExpect(jsonPath("$.content[2].driverNode").value(""))
                .andExpect(jsonPath("$.content[2].startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.content[2].endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.content[2].startLocation").value("15.15151517"))
                .andExpect(jsonPath("$.content[2].endLocation").value("20.20202040"))
                .andExpect(jsonPath("$.content[2].customerUuid").value("550e8400-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.content[2].driverUuid").value("550e8401-e29b-41d4-a716-446655440001"));
    }

    @Test
    void shouldFindAllDeletedRides() throws Exception {
        //Given
        //When
        postman.perform(get("/rides/4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440003"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2022-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2022-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.15151518"))
                .andExpect(jsonPath("$.endLocation").value("20.20202050"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440002"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440002"));

        postman.perform(get("/rides/5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440004"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-08-13 17:00"))
                .andExpect(jsonPath("$.endTime").value("2023-08-13 17:05"))
                .andExpect(jsonPath("$.startLocation").value("15.15151519"))
                .andExpect(jsonPath("$.endLocation").value("20.20202060"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440003"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440003"));

        postman.perform(delete("/rides/4"))
                .andDo(print())
                .andExpect(status().isNoContent());

        postman.perform(delete("/rides/5"))
                .andDo(print())
                .andExpect(status().isNoContent());

        //Then
        postman.perform(get("/rides/deleted")
                        .param("size", "3")
                        .param("page", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(4))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440003"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2022-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2022-12-13 08:05"))
                .andExpect(jsonPath("$.content[0].startLocation").value("15.15151518"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.20202050"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440002"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440002"))
                .andExpect(jsonPath("$.content[1].id").value(5))
                .andExpect(jsonPath("$.content[1].rideUuid").value("550e8402-e29b-41d4-a716-446655440004"))
                .andExpect(jsonPath("$.content[1].driverNode").value(""))
                .andExpect(jsonPath("$.content[1].startTime").value("2023-08-13 17:00"))
                .andExpect(jsonPath("$.content[1].endTime").value("2023-08-13 17:05"))
                .andExpect(jsonPath("$.content[1].startLocation").value("15.15151519"))
                .andExpect(jsonPath("$.content[1].endLocation").value("20.20202060"))
                .andExpect(jsonPath("$.content[1].customerUuid").value("550e8400-e29b-41d4-a716-446655440003"))
                .andExpect(jsonPath("$.content[1].driverUuid").value("550e8401-e29b-41d4-a716-446655440003"));
    }

    @Test
    void shouldFindAllRidesByCustomerUuidWhenIsCompletedTrue() throws Exception {
        //Given
        //When
        CreateRideCompletedRequest request = CreateRideCompletedRequest.builder()
                .isCompleted(true)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //Then
        postman.perform(get("/rides/customers/550e8400-e29b-41d4-a716-446655440000")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.content[0].startLocation").value("15.151515"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.202020"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldFindAllRidesByCustomerUuidWhenIsCompletedFalse() throws Exception {
        //Given
        //When
        CreateRideCompletedRequest request = CreateRideCompletedRequest.builder()
                .isCompleted(false)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //Then
        postman.perform(get("/rides/customers/550e8400-e29b-41d4-a716-446655440000")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").isEmpty())
                .andExpect(jsonPath("$.content[0].startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldFindAllRidesByDriverUuidWhenIsCompletedTrue() throws Exception {
        //Given
        //When
        CreateRideCompletedRequest request = CreateRideCompletedRequest.builder()
                .isCompleted(true)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //Then
        postman.perform(get("/rides/drivers/550e8401-e29b-41d4-a716-446655440000")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.content[0].startLocation").value("15.151515"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.202020"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldFindAllRidesByDriverUuidWhenIsCompletedFalse() throws Exception {
        //Given
        //When
        CreateRideCompletedRequest request = CreateRideCompletedRequest.builder()
                .isCompleted(false)
                .build();

        String json = objectMapper.writeValueAsString(request);

        //Then
        postman.perform(get("/rides/drivers/550e8401-e29b-41d4-a716-446655440000")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .param("size", "5")
                        .param("page", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.content[0].driverNode").value(""))
                .andExpect(jsonPath("$.content[0].startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.content[0].endTime").isEmpty())
                .andExpect(jsonPath("$.content[0].startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.content[0].endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.content[0].customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.content[0].driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldFindByRideId() throws Exception {
        //Given
        //When
        //Then
        postman.perform(get("/rides/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.151515"))
                .andExpect(jsonPath("$.endLocation").value("20.202020"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldFindByRideUuid() throws Exception {
        //Given
        //When
        //Then
        postman.perform(get("/rides/by-uuid/550e8402-e29b-41d4-a716-446655440000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.151515"))
                .andExpect(jsonPath("$.endLocation").value("20.202020"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldEditRide() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2025, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        postman.perform(put("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
                .andExpect(jsonPath("$.startTime").value("2025-01-01 12:30"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
                .andExpect(jsonPath("$.startTime").value("2025-01-01 12:30"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldEditRidePartially() throws Exception {
        //Given
        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2024, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        postman.perform(patch("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
                .andExpect(jsonPath("$.startTime").value("2024-01-01 12:30"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(command.getDriverNode()))
                .andExpect(jsonPath("$.startTime").value("2024-01-01 12:30"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value(command.getStartLocation()))
                .andExpect(jsonPath("$.endLocation").value(command.getEndLocation()))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));
    }

    @Test
    void shouldDeleteRide() throws Exception {
        //Given
        //When
        postman.perform(get("/rides/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.151515"))
                .andExpect(jsonPath("$.endLocation").value("20.202020"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        postman.perform(delete("/rides/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        //Then
        postman.perform(get("/rides/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 1 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/1"))
                .andExpect(jsonPath("$.method").value("GET"));
    }

    @Test
    void shouldNotFindByRideId() throws Exception {
        //Given
        //When
        //Then
        postman.perform(get("/rides/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("GET"));
    }

    @Test
    void shouldNotEditRide() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2025, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        postman.perform(put("/rides/15")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("PUT"));
    }

    @Test
    void shouldNotEditRidePartially() throws Exception {
        //Given
        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2025, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        postman.perform(patch("/rides/15")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("PATCH"));
    }

    @Test
    void shouldNotDeleteRide() throws Exception {
        //Given
        //When
        postman.perform(get("/rides/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        postman.perform(delete("/rides/15"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 15 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/15"))
                .andExpect(jsonPath("$.method").value("DELETE"));
    }

    @Test
    void shouldNotCreateRideWhenStartTimeIsPast() throws Exception {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2000, 12, 12, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startTime' && @.code == 'START_TIME_NOT_PAST')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotCreateRideWhenStartTimeIsNull() throws Exception {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(null)
                .startLocation("test")
                .endLocation("testest")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startTime' && @.code == 'START_TIME_NOT_NULL')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotCreateRideWhenStartLocationIsBlank() throws Exception {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
                .startLocation("")
                .endLocation("testest")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startLocation' && @.code == 'START_LOCATION_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotCreateRideWhenEndLocationIsBlank() throws Exception {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
                .startLocation("test")
                .endLocation("")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'endLocation' && @.code == 'END_LOCATION_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

//    @Test
//    void shouldNotCreateRideWhenInvalidCustomerUuidFormat() throws Exception {
//        //Given
//        String customerUuid = "550e8400-e29b-41d4-a716-44665544000x";
//        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";
//
//        CreateRideCommand command = CreateRideCommand.builder()
//                .driverNode("test")
//                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
//                .startLocation("test")
//                .endLocation("testest")
//                .customerUuid(UUID.fromString(customerUuid))
//                .driverUuid(UUID.fromString(driverUuid))
//                .build();
//
//        String json = objectMapper.writeValueAsString(command);
//
//        //When
//        postman.perform(get("/rides/6"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.code").value(404))
//                .andExpect(jsonPath("$.status").value("Not Found"))
//                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
//                .andExpect(jsonPath("$.uri").value("/rides/6"))
//                .andExpect(jsonPath("$.method").value("GET"));
//
//        //Then
//        String responseJson = postman.perform(post("/rides")
//                        .contentType(APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.[?(@.field == 'customerUuid' && @.code == 'INVALID_CUSTOMER_UUID_FORMAT')]").exists())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
//        });
//        assertEquals(1, errors.size());
//    }

    @Test
    void shouldNotCreateRideWhenCustomerUuidIsNull() throws Exception {
        //Given
        String driverUuid = "550e8401-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .customerUuid(null)
                .driverUuid(UUID.fromString(driverUuid))
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'customerUuid' && @.code == 'CUSTOMER_UUID_NOT_NULL')]").exists())
                .andExpect(jsonPath("$.[?(@.field == 'customerUuid' && @.code == 'INVALID_CUSTOMER_UUID_FORMAT')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(2, errors.size());
    }

//    @Test
//    void shouldNotCreateRideWhenInvalidDriverUuidFormat() throws Exception {
//        //Given
//        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";
//        String driverUuid = "test";
//
//        CreateRideCommand command = CreateRideCommand.builder()
//                .driverNode("test")
//                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
//                .startLocation("test")
//                .endLocation("testest")
//                .customerUuid(UUID.fromString(customerUuid))
//                .driverUuid(UUID.fromString(driverUuid))
//                .build();
//
//        String json = objectMapper.writeValueAsString(command);
//
//        //When
//        postman.perform(get("/rides/6"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.code").value(404))
//                .andExpect(jsonPath("$.status").value("Not Found"))
//                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
//                .andExpect(jsonPath("$.uri").value("/rides/6"))
//                .andExpect(jsonPath("$.method").value("GET"));
//
//        //Then
//        String responseJson = postman.perform(post("/rides")
//                        .contentType(APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.[?(@.field == 'driverUuid' && @.code == 'INVALID_DRIVER_UUID_FORMAT')]").exists())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
//        });
//        assertEquals(1, errors.size());
//    }

    @Test
    void shouldNotCreateRideWhenDriverUuidIsNull() throws Exception {
        //Given
        String customerUuid = "550e8400-e29b-41d4-a716-446655440000";

        CreateRideCommand command = CreateRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2023, 12, 12, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .customerUuid(UUID.fromString(customerUuid))
                .driverUuid(null)
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/6"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.status").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ride with id: 6 not found!"))
                .andExpect(jsonPath("$.uri").value("/rides/6"))
                .andExpect(jsonPath("$.method").value("GET"));

        //Then
        String responseJson = postman.perform(post("/rides")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'driverUuid' && @.code == 'DRIVER_UUID_NOT_NULL')]").exists())
                .andExpect(jsonPath("$.[?(@.field == 'driverUuid' && @.code == 'INVALID_DRIVER_UUID_FORMAT')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(2, errors.size());
    }

    @Test
    void shouldNotEditRideWhenStatusIsCompleted() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2025, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.151515"))
                .andExpect(jsonPath("$.endLocation").value("20.202020"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        postman.perform(put("/rides/1")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot be edited ride with id: 1 when the ride status is: COMPLETED."))
                .andExpect(jsonPath("$.uri").value("/rides/1"))
                .andExpect(jsonPath("$.method").value("PUT"));
    }

    @Test
    void shouldNotEditRideWhenStartTimeIsPast() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2000, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        String responseJson = postman.perform(put("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startTime' && @.code == 'START_TIME_NOT_PAST')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotEditRideWhenStartTimeIsNull() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(null)
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        String responseJson = postman.perform(put("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startTime' && @.code == 'START_TIME_NOT_NULL')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotEditRideWhenStartLocationIsBlank() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2024, 1, 1, 12, 30))
                .startLocation("")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        String responseJson = postman.perform(put("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startLocation' && @.code == 'START_LOCATION_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotEditRideWhenEndLocationIsBlank() throws Exception {
        //Given
        EditRideCommand command = EditRideCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2024, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        String responseJson = postman.perform(put("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'endLocation' && @.code == 'END_LOCATION_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotEditRidePartiallyWhenStatusIsCompleted() throws Exception {
        //Given
        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2025, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2023-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").value("2023-12-13 08:05"))
                .andExpect(jsonPath("$.startLocation").value("15.151515"))
                .andExpect(jsonPath("$.endLocation").value("20.202020"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        postman.perform(patch("/rides/1")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Cannot be edited ride with id: 1 when the ride status is: COMPLETED."))
                .andExpect(jsonPath("$.uri").value("/rides/1"))
                .andExpect(jsonPath("$.method").value("PATCH"));
    }

    @Test
    void shouldNotEditRidePartiallyWhenStartTimeIsPast() throws Exception {
        //Given
        EditRidePartiallyCommand command = EditRidePartiallyCommand.builder()
                .driverNode("test")
                .startTime(LocalDateTime.of(2000, 1, 1, 12, 30))
                .startLocation("test")
                .endLocation("testest")
                .build();

        String json = objectMapper.writeValueAsString(command);

        //When
        postman.perform(get("/rides/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rideUuid").value("550e8402-e29b-41d4-a716-446655440001"))
                .andExpect(jsonPath("$.driverNode").value(""))
                .andExpect(jsonPath("$.startTime").value("2024-12-13 08:00"))
                .andExpect(jsonPath("$.endTime").isEmpty())
                .andExpect(jsonPath("$.startLocation").value("15.15151516"))
                .andExpect(jsonPath("$.endLocation").value("20.20202030"))
                .andExpect(jsonPath("$.customerUuid").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.driverUuid").value("550e8401-e29b-41d4-a716-446655440000"));

        //Then
        String responseJson = postman.perform(patch("/rides/2")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'startTime' && @.code == 'START_TIME_NOT_PAST')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }
}