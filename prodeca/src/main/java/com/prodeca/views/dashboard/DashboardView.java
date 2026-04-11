package com.prodeca.views.dashboard;

import com.prodeca.services.ProductService;
import com.prodeca.services.PurchaseOrderService;
import com.prodeca.services.SupplierService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Aloitus")
@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.HOME_SOLID)
@StyleSheet("themes/prodeca/views/dashboard-view.css")
@AnonymousAllowed
public class DashboardView extends VerticalLayout {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;

    public DashboardView(
        ProductService productService,
        SupplierService supplierService,
        PurchaseOrderService purchaseOrderService
    ) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;

        addClassNames("dashboard-view", LumoUtility.Padding.LARGE);
        setWidthFull();

        // Otsikko
        H2 pageTitle = new H2("Aloitus");
        pageTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.FontSize.XXLARGE);

        Paragraph subtitle = new Paragraph("Tervetuloa Prodecaan - Varastonhallintajärjestelmä");
        subtitle.addClassNames(LumoUtility.Margin.Bottom.LARGE, LumoUtility.TextColor.SECONDARY);

        H3 statsTitle = new H3("Yleiskatsaus");
        statsTitle.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.MEDIUM);

        FlexLayout statsRow = new FlexLayout();
        statsRow.addClassNames("stats-row", LumoUtility.Gap.MEDIUM);
        statsRow.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsRow.setWidthFull();

        statsRow.add(
            buildStatCard("Tuotteet", String.valueOf(this.productService.count()), "stat-card-blue"),
            buildStatCard("Toimittajat", String.valueOf(this.supplierService.count()), "stat-card-green"),
            buildStatCard("Tilaukset", String.valueOf(this.purchaseOrderService.count()), "stat-card-purple")
        );

        // Pikanavigointi
        H3 quickNavTitle = new H3("Pikanavigointi");
        quickNavTitle.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        FlexLayout quickLinks = buildQuickNavigation();

        add(pageTitle, subtitle, statsTitle, statsRow, quickNavTitle, quickLinks);
    }

    // Funktio, joka rakentaa tilastokortin palveluiden palauttamilla luvuilla
    private Div buildStatCard(String label, String value, String colorClass) {
        Div card = new Div();
        
        card.addClassNames(
            "stat-card", colorClass,
            LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE,
            LumoUtility.BoxShadow.SMALL
        );

        // Flexbox-tyylit kortille, jotta ne skaalautuvat hyvin eri näyttöleveyksille
        card.getStyle().set("flex", "1 1 180px");
        card.getStyle().set("min-width", "160px");
        card.getStyle().set("max-width", "280px");
        // Siirtymäanimaatio
        card.getStyle().set("transition", "transform 0.2s ease, box-shadow 0.2 ease");

        // Lukuarvo
        Span valueSpan = new Span(value);
        valueSpan.addClassNames(
            "stat-card-value", LumoUtility.TextColor.PRIMARY,
            LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD
        );

        // Tunnisteteksti
        Span labelSpan = new Span(label);
        labelSpan.addClassNames(
            "stat-card-label",
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.SMALL
        );

        card.add(new Div(valueSpan), new Div(labelSpan));

        return card;
    }

    // Funktio, joka rakentaa pikanavigointikortit linkeillä tärkeimpiin näkymiin
    private FlexLayout buildQuickNavigation() {
        FlexLayout links = new FlexLayout();
        links.addClassNames("quick-nav-row", LumoUtility.Gap.MEDIUM);
        links.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        links.setWidthFull();

        links.add(
            buildNavCard("Tuotevarasto", "Hallinnoi varastossa olevia tuotteita", "products", "nav-card-blue"),
            buildNavCard("Toimittajat", "Hallinnoi toimittajia", "suppliers", "nav-card-green"),
            buildNavCard("Tilaukset", "Seuraa ja hallinnoi tilauksia", "orders", "nav-card-purple"),
            buildNavCard("Haku", "Tarkka tuote- ja tilaushaku Criteria API:lla", "advanced-search", "nav-card-orange")
        );

        return links;
    }

    // Funktio, joka rakentaa yksittäisen pikanavigointikortin, joka toimii linkkinä tiettyyn näkymään
    private Div buildNavCard(String title, String description, String route, String colorClass) {
        Div card = new Div();

        card.addClassNames(
            "nav-card", colorClass,
            LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM
        );

        card.getStyle().set("flex", "1 1 180px");
        card.getStyle().set("min-width", "200px");
        card.getStyle().set("cursor", "pointer");
        card.getStyle().set("transition", "transform 0.15s ease, box-shadow 0.15s ease");

        Span cardTitle = new Span(title);
        cardTitle.addClassNames(
            "nav-card-title", LumoUtility.Display.BLOCK,
            LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD
        );

        Span desc = new Span(description);
        desc.addClassNames(
            LumoUtility.Display.BLOCK,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.XSMALL
        );

        // Navigointi reitille klikkaamalla korttia
        card.add(cardTitle, desc);
        card.addClickListener(e -> UI.getCurrent().navigate(route));

        return card;
    }
}