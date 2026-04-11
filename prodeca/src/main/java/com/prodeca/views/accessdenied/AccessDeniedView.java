package com.prodeca.views.accessdenied;

import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.servlet.http.HttpServletResponse;

@PageTitle("Pääsy evätty")
@Route(value = "access-denied", layout = MainLayout.class)
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<NotFoundException> {
    
    public AccessDeniedView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Virheilmoitus
        Span icon = new Span(VaadinIcon.LOCK.create());
        icon.getStyle().set("font-size", "4rem").set("color", "var(--lumo-error-color)");

        H2 h2 = new H2("Pääsy evätty");
        h2.addClassName(LumoUtility.TextColor.ERROR);

        Paragraph message = new Paragraph(
            "Sinulla ei ole tarvittavia käyttöoikeuksia tämän sivun tarkasteluun"
        );
        message.addClassName(LumoUtility.TextColor.SECONDARY);

        Button homeBtn = new Button("Palaa etusivulle", e -> 
            getUI().ifPresent(ui -> ui.navigate(""))
        );
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(icon, h2, message, homeBtn);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        String reason = parameter.getCustomMessage();
        if (reason != null && reason.contains("Access is denied")) {
            return HttpServletResponse.SC_FORBIDDEN;
        }
        return HttpServletResponse.SC_NOT_FOUND;
    }
}