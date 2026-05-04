package com.prodeca.views.login;

import com.prodeca.security.AuthenticatedUser;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Kirjaudu sisään")
@Route(value = "login")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Header header = new LoginI18n.Header();
        header.setTitle("Prodeca");
        header.setDescription(
            "Kirjaudu sisään tunnuksilla:\n" +
            "admin/admin123 (ylläpitäjä)\n" +
            "user/user123 (peruskäyttäjä)"
        );
        i18n.setHeader(header);

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Kirjaudu sisään");
        form.setUsername("Käyttäjänimi");
        form.setPassword("Salasana");
        form.setSubmit("Kirjaudu sisään");
        i18n.setForm(form);

        LoginI18n.ErrorMessage error = i18n.getErrorMessage();
        error.setTitle("Väärä käyttäjänimi tai salasana");
        error.setMessage("Tarkista tunnuksesi ja yritä uudestaan");
        i18n.setErrorMessage(error);
        
        setI18n(i18n);
        setForgotPasswordButtonVisible(false);

        Paragraph registerPara = new Paragraph();
        registerPara.getStyle()
                    .set("margin", "0")
                    .set("text-align", "center")
                    .set("font-size", "var(--lumo-font-size-s)");

        Anchor registerAnchor = new Anchor("register", "Eikö sinulla ole vielä tiliä? Rekisteröidy tästä");
        registerAnchor.getStyle()
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("font-weight", "500")
                    .set("text-decoration", "underline");

        registerPara.add(registerAnchor);

        registerPara.getElement().setAttribute("slot", "custom-form-area");
        getElement().appendChild(registerPara.getElement());

        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (this.authenticatedUser.get().isPresent()) {
            // Käyttäjä on jo kirjautunut, ohjataan suoraan etusivulle
            setOpened(false);
            event.forwardTo("");
        }
        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}