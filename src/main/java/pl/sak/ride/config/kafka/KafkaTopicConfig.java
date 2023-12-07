package pl.sak.ride.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic rideToDriverTopic() {
        return TopicBuilder
                .name("ride_to_driver")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rideToCustomerTopic() {
        return TopicBuilder
                .name("ride_to_customer")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rideToDriverConfirmationResponsesTopic() {
        return TopicBuilder
                .name("ride_to_driver_confirmation_responses")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
