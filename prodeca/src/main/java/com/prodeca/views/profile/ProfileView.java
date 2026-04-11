package com.prodeca.views.profile;

import com.prodeca.data.User;
import com.prodeca.security.AuthenticatedUser;
import com.prodeca.services.UserService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.PermitAll;

@PageTitle("Profiili")
@Route(value = "profile", layout = MainLayout.class)
@Menu(order = 7, icon = LineAwesomeIconUrl.USER_CIRCLE_SOLID)
@PermitAll
public class ProfileView extends VerticalLayout {

    private final UserService service;

    private Avatar avatar;

    public ProfileView(AuthenticatedUser authenticatedUser, UserService service) {
        this.service = service;

        addClassName(LumoUtility.Padding.LARGE);
        setWidthFull();

        authenticatedUser.get().ifPresentOrElse(
            this::buildProfileView,
            () -> add(new Paragraph("Kirjaudu sisään nähdäksesi profiilisi"))
        );
    }

    // Funktio, joka rakentaa profiilinäkymän käyttäjälle
    private void buildProfileView(User user) {
        H2 title = new H2("Profiilisi");
        add(title);

        add(buildProfileInfo(user));
        add(buildUploadSection(user));
    }

    // Funktio, joka rakentaa käyttäjätietopaneelin
    private HorizontalLayout buildProfileInfo(User user) {
        // Avatar (käytetään tallennettua MIME-tyyppiä)
        avatar = new Avatar(user.getName());
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        String dataUri = user.getAvatarUri();
        if (dataUri != null) {
            avatar.setImage(dataUri);
        }

        // Käyttäjätiedot tekstinä
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);

        info.add(infoRow("Käyttäjänimi", user.getUsername()));
        info.add(infoRow("Nimi", user.getName()));
        info.add(infoRow("Sähköposti", user.getEmail()));
        info.add(infoRow("Roolit", user.getRoles() != null ? user.getRoles().toString() : "-"));

        HorizontalLayout row = new HorizontalLayout(avatar, info);
        row.setAlignItems(Alignment.CENTER);
        return row;
    }

    // Funktio, joka rakentaa tietorivin profiilitiedoille
    private HorizontalLayout infoRow(String label, String value) {
        Span labelSpan = new Span(label + ": ");
        labelSpan.addClassName(LumoUtility.FontWeight.SEMIBOLD);
        Span valueSpan = new Span(value);
        return new HorizontalLayout(labelSpan, valueSpan);
    }

    // Funktio, joka rakentaa profiilikuvan latausosion
    private VerticalLayout buildUploadSection(User user) {
        H3 uploadTitle = new H3("Profiilikuva");

        UI ui = UI.getCurrent();

        UploadHandler uploadHandler = UploadHandler.inMemory((metadata, data) -> {
            user.setProfilePicture(data);
            user.setProfilePictureType(metadata.contentType());
            this.service.save(user);

            // Käyttöliittymän päivitys ui.access() -kutsullla
            ui.access(() -> {
                String updatedUri = user.getAvatarUri();
                if (updatedUri != null) {
                    avatar.setImage(updatedUri);
                }
                Notification ok = Notification.show("Profiilikuva päivitetty onnistuneesti", 3000, Notification.Position.BOTTOM_START);
                ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                ui.getPage().reload();
            });
        });

        Upload upload = new Upload();
        upload.setUploadHandler(uploadHandler);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFileSize(4 * 1024 * 1024); // 4 Mt
        upload.setMaxFiles(1);

        Button uploadBtn = new Button("Valitse profiilikuva");
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        upload.setUploadButton(uploadBtn);

        upload.addFileRejectedListener(e -> {
            Notification error = Notification.show("Tiedosto hylätty: " + e.getErrorMessage(), 3000, Notification.Position.MIDDLE);
            error.addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        Paragraph hint = new Paragraph(
            "Sallitut tiedostotyypit: PNG, JPEG, GIF, WebP. Enimmäiskoko 4 Mt"
        );
        hint.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        VerticalLayout layout = new VerticalLayout(uploadTitle, upload, hint);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.addClassName(LumoUtility.Margin.Top.LARGE);
        return layout;
    }
}