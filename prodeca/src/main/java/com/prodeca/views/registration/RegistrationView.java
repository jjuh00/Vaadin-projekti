package com.prodeca.views.registration;

import com.prodeca.data.Role;
import com.prodeca.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Set;

@PageTitle("Rekisteröidy")
@Route(value = "register")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {

    private final UserService service;

    // Lomakekentät
    private final TextField usernameField = new TextField("Käyttäjänimi");
    private final TextField nameField = new TextField("Näyttönimi");
    private final EmailField emailField = new EmailField("Sähköposti");
    private final PasswordField passwordField = new PasswordField("Salasana");
    private final PasswordField confirmField = new PasswordField("Vahvista salasana");

    public RegistrationView(UserService service) {
        this.service = service;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout form = new VerticalLayout();
        form.setWidth("380px");
        form.setPadding(true);
        form.getStyle()
            .set("background", "var(--lumo-base-color)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("box-shadow", "var(--lumo-box-shadow-m)");

        H2 title = new H2("Luo uusi tili");

        // Kenttien määrittely
        usernameField.setWidthFull();
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setMinLength(3);

        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        nameField.setMinLength(2);

        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setMinLength(8);

        confirmField.setWidthFull();
        confirmField.setRequiredIndicatorVisible(true);

        Button registerBtn = new Button("Rekisteröidy", e -> handleRegistration());
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();

        // Linkki kirjautumissivulle
        Anchor loginLink = new Anchor("login", "Onko sinulla jo tili? Kirjaudu sisään");

        form.add(title, usernameField, nameField, emailField, passwordField, confirmField, registerBtn, loginLink);
        add(form);
    }

    // Funktio, joka käsittelee rekisteröitymisen logiikan
    private void handleRegistration() {
        String username = usernameField.getValue().trim();
        String name = nameField.getValue().trim();
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();
        String confirm = confirmField.getValue();

        // Perusvalidointi
        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Kaikki kentät ovat pakollisia");
            return;
        }
        if (username.length() < 3) {
            showError("Käyttäjänimen pitää olla vähintään 3 merkkiä pitkä");
            return;
        }
        if (name.length() < 2) {
            showError("Näyttönimen pitää olla vähintään 2 merkkiä pitkä");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Salasanat eivät täsmää");
            return;
        }
        if (password.length() < 8) {
            showError("Salasanan pitää olla vähintään 8 merkkiä pitkä");
            return;
        }
        
        try {
            this.service.registerUser(username, name, email, password, Set.of(Role.USER));

            Notification ok = Notification.show("Rekisteröityminen onnistui! Sinut ohjataan kirjautumissivulle...", 2000, Notification.Position.MIDDLE);
            ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Ohjataan kirjautumissivulle 2 sekunnin kuluttua
            ok.addDetachListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));
        } catch (Exception ex) {
            showError("Tarkista kentät, virhe käyttäjää luodessa: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Notification error = Notification.show(message, 4000, Notification.Position.BOTTOM_END);
        error.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}