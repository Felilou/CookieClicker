package at.spengergasse.persistanceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PersistanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersistanceServiceApplication.class, args);
    }

}
