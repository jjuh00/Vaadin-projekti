package com.prodeca.views.dashboard;

import com.prodeca.services.ProductService;
import com.prodeca.services.PurchaseOrderService;
import com.prodeca.services.SupplierService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Locale;

@PageTitle("Aloitus")
@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.HOME_SOLID)
@StyleSheet("themes/prodeca/views/dashboard-view.css")
@AnonymousAllowed
public class DashboardView extends VerticalLayout implements LocaleChangeObserver {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final PurchaseOrderService purchaseOrderService;

    // Kenttämuuttujat (LocaleChangeObserver vaatii) tekstien päivittämiseksi
    private final H2 pageTitle;
    private final Paragraph subtitle;
    private final H3 statsTitle;
    private final H3 quickNavTitle;
    private final Button languageBtn;

    private final Span productsStatLabel;
    private final Span suppliersStatLabel;
    private final Span ordersStatLabel;

    private Span productsNavTitle;
    private Span productsNavDesc;
    private Span suppliersNavTitle;
    private Span suppliersNavDesc;
    private Span ordersNavTitle;
    private Span ordersNavDesc;
    private Span searchNavTitle;
    private Span searchNavDesc;

    public DashboardView(ProductService productService, SupplierService supplierService,
        PurchaseOrderService purchaseOrderService) {

        this.productService = productService;
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;

        addClassNames("dashboard-view", LumoUtility.Padding.LARGE);
        setWidthFull();

        pageTitle = new H2(getTranslation("dashboard.title"));
        pageTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL, LumoUtility.FontSize.XXLARGE);

        subtitle = new Paragraph(getTranslation("dashboard.subtitle"));
        subtitle.addClassNames(LumoUtility.Margin.Bottom.LARGE, LumoUtility.TextColor.SECONDARY);

        languageBtn = buildLanguageSwitcher();
        HorizontalLayout headerRow = new HorizontalLayout(languageBtn);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(JustifyContentMode.END);
        add(headerRow);

        statsTitle = new H3(getTranslation("dashboard.stats.title"));
        statsTitle.addClassNames(LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.MEDIUM);

        FlexLayout statsRow = new FlexLayout();
        statsRow.addClassName(LumoUtility.Gap.MEDIUM);
        statsRow.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsRow.setWidthFull();

        productsStatLabel = new Span(getTranslation("dashboard.stat.products"));
        suppliersStatLabel = new Span(getTranslation("dashboard.stat.suppliers"));
        ordersStatLabel = new Span(getTranslation("dashboard.stat.orders"));

        statsRow.add(
            buildStatCard(String.valueOf(this.productService.count()), productsStatLabel, "stat-card-blue"),
            buildStatCard(String.valueOf(this.supplierService.count()), suppliersStatLabel, "stat-card-green"),
            buildStatCard(String.valueOf(this.purchaseOrderService.count()), ordersStatLabel, "stat-card-purple")
        );

        quickNavTitle = new H3(getTranslation("dashboard.nav.title"));
        quickNavTitle.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        FlexLayout quickLinks = buildQuickNavigation();

        add(pageTitle, subtitle, statsTitle, statsRow, quickNavTitle, quickLinks);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        // Päivitetään kaikki tekstit uudella kielellä
        pageTitle.setText(getTranslation("dashboard.title"));
        subtitle.setText(getTranslation("dashboard.subtitle"));
        statsTitle.setText(getTranslation("dashboard.stats.title"));
        quickNavTitle.setText(getTranslation("dashboard.nav.title"));
        languageBtn.setText(getTranslation("dashboard.lang.switch"));

        productsStatLabel.setText(getTranslation("dashboard.stat.products"));
        suppliersStatLabel.setText(getTranslation("dashboard.stat.suppliers"));
        ordersStatLabel.setText(getTranslation("dashboard.stat.orders"));

        productsNavTitle.setText(getTranslation("dashboard.nav.products"));
        productsNavDesc.setText(getTranslation("dashboard.nav.products.desc"));
        suppliersNavTitle.setText(getTranslation("dashboard.nav.suppliers"));
        suppliersNavDesc.setText(getTranslation("dashboard.nav.suppliers.desc"));
        ordersNavTitle.setText(getTranslation("dashboard.nav.orders"));
        ordersNavDesc.setText(getTranslation("dashboard.nav.orders.desc"));
        searchNavTitle.setText(getTranslation("dashboard.nav.search"));
        searchNavDesc.setText(getTranslation("dashboard.nav.search.desc"));

        // Päivitetään myös selaimen sivuotsikko
        UI.getCurrent().getPage().setTitle(getTranslation("dashboard.title"));
    }

    // Funktio, joka rakentaa kielenvaihtopainikkeen
    private Button buildLanguageSwitcher() {
        Button btn = new Button(getTranslation("dashboard.lang.switch"));
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        btn.addClickListener(e -> {
            // Luetaan nykyinen locale
            boolean isFinnish = "fi".equals(UI.getCurrent().getLocale().getLanguage());
            Locale nextLocale = isFinnish ? Locale.ENGLISH : Locale.forLanguageTag("fi");

            UI.getCurrent().setLocale(nextLocale);

            // Tallennetaan kielivalinta cookieen
            UI.getCurrent().getPage().executeJs(
                "document.cookie = 'prodeca_locale=' + $0 + '; path=/; max-age=25920000;'",
                nextLocale.getLanguage()
            );
        });

        return btn;
    }

    // Funktio, joka rakentaa tilastokortin palveluiden palauttamilla luvuilla
    private Div buildStatCard(String value, Span label, String colorClass) {
        Div card = new Div();
        card.addClassNames(
            "stat-card", colorClass,
            LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL
        );

        // Flexbox-tyylit korteille
        card.getStyle()
            .set("flex", "1 1 180px")
            .set("min-width", "160px")
            .set("max-width", "280px")
            .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        Span span = new Span(value);
        span.addClassNames(
            "stat-card-value", LumoUtility.TextColor.PRIMARY,
            LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD
        );

        label.addClassNames(
            "stat-card-label", 
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.SMALL
        );

        card.add(new Div(span), new Div(label));
        return card;
    }

    // Funktio, joka rakentaa pikanavigointikortit linkeillä tärkeimpiin näkymiin
    private FlexLayout buildQuickNavigation() {
        FlexLayout links = new FlexLayout();
        links.addClassName(LumoUtility.Gap.MEDIUM);
        links.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        links.setWidthFull();

        productsNavTitle = new Span(getTranslation("dashboard.nav.products"));
        productsNavDesc = new Span(getTranslation("dashboard.nav.products.desc"));
        suppliersNavTitle = new Span(getTranslation("dashboard.nav.suppliers"));
        suppliersNavDesc = new Span(getTranslation("dashboard.nav.suppliers.desc"));
        ordersNavTitle = new Span(getTranslation("dashboard.nav.orders"));
        ordersNavDesc = new Span(getTranslation("dashboard.nav.orders.desc"));
        searchNavTitle = new Span(getTranslation("dashboard.nav.search"));
        searchNavDesc = new Span(getTranslation("dashboard.nav.search.desc"));

        links.add(
            buildNavCard(productsNavTitle, productsNavDesc, "products", "nav-card-blue"),
            buildNavCard(suppliersNavTitle, suppliersNavDesc, "suppliers", "nav-card-green"),
            buildNavCard(ordersNavTitle, ordersNavDesc, "orders", "nav-card-purple"),
            buildNavCard(searchNavTitle, searchNavDesc, "advanced-search", "nav-card-orange")
        );

        return links;
    }

    // Funktio, joka rakentaa yksittäisen pikanavigointikortin, joka toimii linkkinä tiettyyn näkymään
    private Div buildNavCard(Span title, Span description, String route, String colorClass) {
        Div card = new Div();
        card.addClassNames(
            "nav-card", colorClass,
            LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM
        );

        card.getStyle()
            .set("flex", "1 1 180px")
            .set("min-width", "200px")
            .set("cursor", "pointer")
            .set("transition", "transform 0.15s ease, box-shadow 0.15s ease");

        title.addClassNames(
            LumoUtility.Display.BLOCK,
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.FontWeight.SEMIBOLD
        );

        description.addClassNames(
            LumoUtility.Display.BLOCK,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.XSMALL
        );

        // Navigointi reiteille klikkaamalla korttia
        card.add(title, description);
        card.addClickListener(e -> UI.getCurrent().navigate(route));
        return card;
    }
}