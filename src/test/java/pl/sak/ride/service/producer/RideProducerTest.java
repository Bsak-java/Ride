//package pl.sak.ride.service.producer;
//
//import lombok.RequiredArgsConstructor;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.listener.KafkaMessageListenerContainer;
//import org.springframework.kafka.listener.MessageListener;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.kafka.test.utils.ContainerTestUtils;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.context.ContextConfiguration;
//import pl.sak.ride.model.ride.Ride;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.TimeUnit;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.notNullValue;
//import static pl.sak.ride.enums.Status.INPROGRESS;
//
//@SpringBootTest
//@EmbeddedKafka // osadza broker Kafka do testów
//@ContextConfiguration(classes = KafkaTestConfiguration.class) // wskazuje z jakiej konfiguracji test powinien korzystac
//@RequiredArgsConstructor
//class RideProducerTest {
//
//    private String testTopic = "ride-requests";
//
//    private final RideProducer rideProducer;
//    private final EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    private KafkaMessageListenerContainer<String, DriverRequestDto> container; // odbiera wiadomosci z tematu Kafka
//    private BlockingQueue<ConsumerRecord<String, DriverRequestDto>> consumerRecords; //przechowuje otrzymane rekordy
//
//    @BeforeEach
//    void setUp() {
//        consumerRecords = new LinkedBlockingDeque<>();
//
//        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker); // grupa, "false" oznacza reczne potwierdzanie odbioru wiadomosci, korzysta z brokera
//        DefaultKafkaConsumerFactory<String, DriverRequestDto> consumer = new DefaultKafkaConsumerFactory<>(consumerProps); // tworzy fabryke konsumenta
//
//        ContainerProperties containerProperties = new ContainerProperties(testTopic); // uzywany jest do konfiguracji kontenera nasluchujacego
//        container = new KafkaMessageListenerContainer<>(consumer, containerProperties); // nasluchuje na temat Kafki
//        container.setupMessageListener((MessageListener<String, DriverRequestDto>) record -> consumerRecords.add(record)); // konfiguruje kontener nasluchujacy, ktory dodaje otrzymany rekord do kolejki
//        container.start(); // uruchamia kontener nasluchujacy
//        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()); // oczekuje na przypisanie partycji do konsumenta
//    }
//
//    @AfterEach
//    void after() {
//        container.stop();
//    }
//
//    @Test
//    void shouldSendRideConfirmationToBroker() throws InterruptedException {
//        //Given
//        long rideId = 1L;
//        long customerId = 1L;
//        long driverId = 1L;
//
//        Ride ride = Ride.builder()
//                .id(rideId)
//                .driverNode("test")
//                .startTime(LocalDateTime.now())
//                .endTime(null)
//                .startLocation("test")
//                .endLocation("test")
//                .status(INPROGRESS)
//                .isCompleted(false)
//                .customer(Customer.builder()
//                        .id(customerId)
//                        .build())
//                .driver(Driver.builder()
//                        .id(driverId)
//                        .build())
//                .build();
//
//        //When
//        rideProducer.sendRideRequest(ride);
//        ConsumerRecord<String, DriverRequestDto> consumerRecord = consumerRecords.poll(1, TimeUnit.SECONDS); // odebranie z kolejki jednego rekordu w ciagu 1 sek.
//
//        assert consumerRecord != null;
//        assertThat(consumerRecord.topic(), equalTo(testTopic));
//        assertThat(consumerRecord, is(notNullValue()));
//        assertThat(consumerRecord.value().getRideId(), is(equalTo(ride.getId())));
//        assertThat(consumerRecord.value().getDriverId(), is(equalTo(ride.getDriver().getId())));
//    }
//}