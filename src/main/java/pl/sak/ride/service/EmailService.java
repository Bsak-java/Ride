package pl.sak.ride.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.sak.ride.feign.CustomerFeignClient;
import pl.sak.ride.model.Ride;
import pl.sak.ride.model.dto.CustomerDto;

@Component
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final CustomerFeignClient customerFeignClient;

    public void sendMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("b-test@example.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendConfirmedRideToCustomer(Ride ride) {
        CustomerDto customer = customerFeignClient.findByUuid(ride.getCustomerUuid());
        String to = customer.getEmail();
        String subject = "Ride confirmed.";
        String text = "Your ride request from: " + ride.getStartLocation() + " to: " + ride.getEndLocation() + " has been confirmed." +
                "\n\nEnjoy your ride!";
        sendMessage(to, subject, text);
    }

    public void sendRejectedRideToCustomer(Ride ride) {
        CustomerDto customer = customerFeignClient.findByUuid(ride.getCustomerUuid());
        String to = customer.getEmail();
        String subject = "Ride rejected.";
        String text = "Your ride request from: " + ride.getStartLocation() + " to: " + ride.getEndLocation() + " has been rejected." +
                "\n\nTry again!";
        sendMessage(to, subject, text);
    }

    public void sendCompletedRideToCustomer(Ride ride) {
        CustomerDto customer = customerFeignClient.findByUuid(ride.getCustomerUuid());
        String to = customer.getEmail();
        String subject = "Ride completed.";
        String text = "Your ride from: " + ride.getStartLocation() + " to: " + ride.getEndLocation() + "has been completed successfully." +
                "\nWe invite you to use our services again!";
        sendMessage(to, subject, text);
    }
}
