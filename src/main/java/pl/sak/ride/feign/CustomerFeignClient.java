package pl.sak.ride.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.sak.ride.model.dto.CustomerDto;

import java.util.UUID;

@FeignClient(name = "customers-service", url = "http://localhost:8081")
public interface CustomerFeignClient {

    @GetMapping("/customers/by-uuid/{uuid}")
    CustomerDto findByUuid(@PathVariable("uuid") UUID uuid);
}
