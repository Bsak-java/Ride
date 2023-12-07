package pl.sak.ride.config.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import pl.sak.ride.model.dto.CustomerDto;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StreamUtils.copyToString;

public class CustomerDtoMocks {

    public static void setupMockCustomerDtoResponse(WireMockServer mockServer) throws IOException {
        mockServer.stubFor(get(urlEqualTo("/customers/by-uuid/550e8400-e29b-41d4-a716-446655440000"))
                .willReturn(
                        aResponse()
                                .withStatus(OK.value())
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBody(
                                        copyToString(
                                                CustomerDto.class.getClassLoader().getResourceAsStream("payload/get-customer-findByUuid-response.json"),
                                                defaultCharset()
                                        )
                                )
                )
        );
    }
}
