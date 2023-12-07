package pl.sak.ride.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.sak.ride.config.feign.WireMockConfig;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.feign.DriverFeignClient;
import pl.sak.ride.model.dto.CustomerDto;
import pl.sak.ride.model.dto.DriverDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.sak.ride.config.feign.CustomerDtoMocks.setupMockCustomerDtoResponse;
import static pl.sak.ride.config.feign.DriverDtoMocks.setupMockDriverDtoResponse;

@SpringBootTest
@EnableFeignClients
@EnableConfigurationProperties
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9094",
                "port=9094"
        },
        controlledShutdown = true,
        brokerPropertiesLocation = "classpath:embedded-kafka-broker-test.yml"
)
@DirtiesContext
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfig.class})
@ActiveProfiles("test")
public class FeignClientIT {

    @Autowired
    private WireMockServer mockCustomerService;
    @Autowired
    private WireMockServer mockDriverService;
    @Autowired
    private CustomerFeignClient customerClient;
    @Autowired
    private DriverFeignClient driverClient;

    @BeforeEach
    void setUp() throws IOException {
        setupMockCustomerDtoResponse(mockCustomerService);
        setupMockDriverDtoResponse(mockDriverService);
    }

    @Test
    void shouldReturnCustomerDtoWhenFindByIdEquals1() {
        //Given
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        CustomerDto customerDto = CustomerDto.builder()
                .id(1L)
                .customerUuid(UUID.fromString(uuid))
                .name("Maria")
                .surname("Kowalska")
                .email("maria.kowalska@example.com")
                .phoneNumber("+48123456789")
                .dateOfBirth(LocalDate.of(1980, 5, 15))
                .build();

        //When
        CustomerDto result = customerClient.findByUuid(UUID.fromString(uuid));

        //Then
        assertEquals(result.getId(), customerDto.getId());
        assertEquals(result.getCustomerUuid(), customerDto.getCustomerUuid());
        assertEquals(result.getName(), customerDto.getName());
        assertEquals(result.getSurname(), customerDto.getSurname());
        assertEquals(result.getEmail(), customerDto.getEmail());
        assertEquals(result.getPhoneNumber(), customerDto.getPhoneNumber());
        assertEquals(result.getDateOfBirth(), customerDto.getDateOfBirth());
    }

    @Test
    void shouldReturnDriverDtoWhenFindByIdEquals1() {
        //Given
        String uuid = "550e8401-e29b-41d4-a716-446655440000";
        DriverDto driverDto = DriverDto.builder()
                .id(1L)
                .driverUuid(UUID.fromString(uuid))
                .name("Basia")
                .surname("Kociol")
                .email("b.kociol@example.com")
                .pesel("00112345124")
                .phoneNumber("+48111222333")
                .dateOfBirth(LocalDate.of(2000, 12, 23))
                .build();

        //When
        DriverDto result = driverClient.findByUuid(UUID.fromString(uuid));

        //Then
        assertEquals(result.getId(), driverDto.getId());
        assertEquals(result.getDriverUuid(), driverDto.getDriverUuid());
        assertEquals(result.getName(), driverDto.getName());
        assertEquals(result.getSurname(), driverDto.getSurname());
        assertEquals(result.getEmail(), driverDto.getEmail());
        assertEquals(result.getPesel(), driverDto.getPesel());
        assertEquals(result.getPhoneNumber(), driverDto.getPhoneNumber());
        assertEquals(result.getDateOfBirth(), driverDto.getDateOfBirth());
    }
}
