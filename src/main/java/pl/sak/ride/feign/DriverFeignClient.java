package pl.sak.ride.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.sak.ride.model.dto.DriverDto;

import java.util.UUID;

@FeignClient(name = "drivers-service", url = "http://localhost:8082")
public interface DriverFeignClient {

    @GetMapping("/drivers/available")
    Page<DriverDto> findAllAvailable(@PageableDefault Pageable pageable);

    @GetMapping("/drivers/by-uuid/{uuid}")
    DriverDto findByUuid(@PathVariable("uuid") UUID uuid);
}

