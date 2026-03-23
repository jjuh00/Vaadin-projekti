package com.prodeca.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.prodeca.views.login.LoginView;
import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    // Määritellään PasswordEncoder salasanojen salaamiseen
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Määritellään SecurityFilterChain, joka määrittää sovelluksen turvallisuusasetukset
    @Bean
    public SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/*.png", "/*.css").permitAll());
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/line-awesome/**").permitAll());
        http.with(vaadin(), vaadin -> {
            vaadin.loginView(LoginView.class);
        });

        return http.build();
    }
}