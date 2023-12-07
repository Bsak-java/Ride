package pl.sak.ride.config.feign;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class WireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockCustomerService() {
        return new WireMockServer(8081);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockDriverService() {
        return new WireMockServer(8082);
    }
}
