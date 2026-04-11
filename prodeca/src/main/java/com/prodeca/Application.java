package com.prodeca;

import com.prodeca.data.Role;
import com.prodeca.services.UserService;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.sql.autoconfigure.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("themes/prodeca/styles.css")
@EnableConfigurationProperties(SqlInitializationProperties.class)
public class Application implements AppShellConfigurator {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Funktio, joka alustaa käyttäjät tietokantaan sovelluksen käynnistyessä
    @Bean
    CommandLineRunner initUsers(UserService service) {
        return args -> {
            // Admin-käyttäjä
            if (service.findByUsername("admin").isEmpty()) {
                service.registerUser(
                    "admin",
                    "Aada Admin",
                    "admin@example.prodeca",
                    "admin123", // Salataan Bcryptillä tallennuksen yhteydessä
                    Set.of(Role.ADMIN)
                );
                log.info("Admin-käyttäjä luotu: admin/admin123");
            } else {
                log.info("Käyttäjä 'admin' on jo olemessa, ohitetaan...");
            }

            // Tavallinen käyttäjä
            if (service.findByUsername("user").isEmpty()) {
                service.registerUser(
                    "user",
                    "Kalle Käyttäjä",
                    "user@example.prodeca",
                    "user123", // Salataan Bcryptillä tallennuksen yhteydessä
                    Set.of(Role.USER)
                );
                log.info("Tavallinen käyttäjä luotu: user/user123");
            } else {
                log.info("Käyttäjä 'user' on jo olemassa, ohitetaan...");
            }
        };
    }
}