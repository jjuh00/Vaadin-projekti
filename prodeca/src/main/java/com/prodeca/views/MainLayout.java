package com.prodeca.views;

import com.prodeca.data.User;
import com.prodeca.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Top-level placeholder muille näkymille
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private H1 viewTitle;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        // Drawer toggle
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Avaa/sulje navigaatio");

        // Sovelluksen nimi headerin vasemmassa reunassa
        Span appName = new Span("Prodeca");
        appName.addClassNames(
            "app-name", LumoUtility.TextColor.PRIMARY,
            LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.BOLD
        );

        // Nykyisen sivun otsikko
        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Käyttäjäosuus headerin oikeassa reunassa
        HorizontalLayout userSection = buildUserSection();

        addToNavbar(true, toggle, appName, viewTitle, userSection);
    }

    // Funktio, joka rakentaa headerin oikeaan reunaan käyttäjäosion, 
    // jossa on avatar, nimi ja kirjaudu ulos -nappi
    private HorizontalLayout buildUserSection() {
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.addClassNames(
            "header-user-section", LumoUtility.Margin.Left.AUTO,
            LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER
        );
        userSection.setPadding(true);

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            // Avatar
            Avatar avatar = new Avatar(user.getName());
            if (user.getProfilePicture() != null && user.getProfilePicture().length > 0) {
                String base64Image = Base64.getEncoder().encodeToString(user.getProfilePicture());
                avatar.setImage("data:image/png;base64," + base64Image);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            // Käyttäjän nimi
            Span userName = new Span(user.getName());
            userName.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL,
                LumoUtility.FontWeight.MEDIUM
            );

            Button logoutBtn = new Button("Kirjaudu ulos");
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            logoutBtn.addClassName("logout-button");
            logoutBtn.addClickListener(e -> authenticatedUser.logout());

            userSection.add(avatar, userName, logoutBtn);
        } else {
            // Kirjautumattomille käyttäjille näytetään vain kirjaudutumislinkki
            Anchor loginLink = new Anchor("login", "Kirjaudu sisään");
            loginLink.addClassNames(
                LumoUtility.TextColor.PRIMARY, LumoUtility.TextColor.PRIMARY
            );
            userSection.add(loginLink);
        }

        return userSection;
    }

    private void addDrawerContent() {
        // Drawerin otsikko
        Span drawerTitle = new Span("Prodeca");
        drawerTitle.addClassNames(
            "drawer-title",
            LumoUtility.FontSize.XLARGE,
            LumoUtility.FontWeight.BOLD
        );
        Header drawerHeader = new Header(drawerTitle);
        drawerHeader.addClassName("drawer-header");

        // Navigointilinkit
        Scroller scroller = new Scroller(createNavigation());
        scroller.addClassName("drawer-scroller");

        // Kiinnitetty alatunnuste drawerin alareunassa
        Footer drawerFooter = createFooter();

        addToDrawer(drawerHeader, scroller, drawerFooter);
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("drawer-nav");

        // MenuConfiguration poimii kaikki @Menu-annotaatiolla varustetut näkymät automaattisesti
        // Vaadin suodattaa pois näkymät, joihin käyttäjällä ei ole oikeuksia
        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();
        menuEntries.forEach(entry -> {
            SideNavItem item;
            if (entry.icon() != null) {
                item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
            } else {
                item = new SideNavItem(entry.title(), entry.path());
            }
            nav.addItem(item);
        });

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.addClassName("drawer-footer");

        // Copyright-teksti
        Paragraph copyright = new Paragraph("© 2026 Prodeca");
        copyright.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.XSMALL
        );

        // Tekijä tieto
        Paragraph author = new Paragraph("Tekijä: Juho Jämsén");
        author.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.XSMALL
        );

        // Linkkipalkki
        Div linkRow = new Div();
        linkRow.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL);

        Anchor docsLink = new Anchor("https://vaadin.com/docs/latest", "Vaadin-dokumentaatio");
        docsLink.addClassNames("footer-link", LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.XSMALL);
        docsLink.setTarget("_blank");

        Anchor githubLink = new Anchor("https://github.com/jjuh00", "GitHub");
        githubLink.addClassNames("footer-link", LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.XSMALL);
        githubLink.setTarget("_blank");
        
        linkRow.add(docsLink, githubLink);

        footer.add(copyright, author, linkRow);
        return footer;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}