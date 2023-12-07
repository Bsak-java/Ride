package pl.sak.ride.config.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import pl.sak.ride.model.dto.CustomerDto;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

public class DriverDtoMocks {

    public static void setupMockDriverDtoResponse(WireMockServer mockServer) throws IOException {
        mockServer.stubFor(get(urlEqualTo("/drivers/by-uuid/550e8401-e29b-41d4-a716-446655440000"))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(OK.value())
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBody(
                                        copyToString(
                                                CustomerDto.class.getClassLoader().getResourceAsStream("payload/get-driver-findByUuid-response.json"),
                                                defaultCharset()
                                        )
                                )
                )
        );
    }
}
