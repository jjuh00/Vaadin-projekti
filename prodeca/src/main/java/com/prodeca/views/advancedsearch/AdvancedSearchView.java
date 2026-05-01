package com.prodeca.views.advancedsearch;

import com.prodeca.data.Product;
import com.prodeca.services.ProductSearchFilter;
import com.prodeca.services.ProductService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.math.BigDecimal;

@PageTitle("Tuote- ja tilaushaku")
@Route(value = "advanced-search", layout = MainLayout.class)
@Menu(order = 4, icon = LineAwesomeIconUrl.SEARCH_SOLID)
@StyleSheet("themes/prodeca/views/advanced-search-view.css")
@PermitAll
public class AdvancedSearchView extends VerticalLayout {
    
    // Hakukentät
    private final TextField nameorSkuField = new TextField("Nimi tai tuotekoodi");
    private final TextField categoryField = new TextField("Kategoria");
    private final TextField supplierNameField = new TextField("Toimittajan nimi");
    private final BigDecimalField minPriceField = new BigDecimalField("Minimihinta (€)");
    private final BigDecimalField maxPriceField = new BigDecimalField("Maksimihinta (€)");
    private final Checkbox activeOnlyBox = new Checkbox("Vain aktiiviset tuotteet");
    private final DatePicker orderDateFrom = new DatePicker("Tilauspäivä alkaen");
    private final DatePicker orderDateTo = new DatePicker("Tilauspäivä asti");

    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private final Span resultCountBadge = new Span(); // Tilamerkkijono

    private final ProductService service;

    private ProductSearchFilter activeFilter = new ProductSearchFilter();

    public AdvancedSearchView(ProductService service) {
        this.service = service;
        addClassName("advanced-search-view");

        setSizeFull();
        add(buildHeader());
        add(buildFilterSection());
        add(buildResultsSection());

        refreshGrid();
    }

    // Funktio, joka rakentaa otsikkosektion
    private Div buildHeader() {
        Div header = new Div();
        header.addClassName("search-header");

        H3 title = new H3("Tuotehaku");

        Span description = new Span(
            "Hae tuotteita nimen, kategorian tai toimittajan mukaan. " +
            "Voit myös rajata hakua hinnan tai tilauspäivän mukaan"
        );
        description.addClassName("search-description");

        header.add(title, description);
        return header;
    }

    // Funktio, joka rakentaa hakukenttäosion
    private Div buildFilterSection() {
        Div section = new Div();
        section.addClassName("filter-section");

        // Tekstikentät
        nameorSkuField.setPlaceholder("esim. iPhone 17 tai PHN012");
        nameorSkuField.setPrefixComponent(VaadinIcon.SEARCH.create());
        nameorSkuField.setClearButtonVisible(true);

        categoryField.setPlaceholder("esim. elektroniikka");
        categoryField.setClearButtonVisible(true);

        supplierNameField.setPlaceholder("esim. Toimitus Oy");
        supplierNameField.setClearButtonVisible(true);

        // Hintakentät
        minPriceField.setPlaceholder("0.00");
        maxPriceField.setPlaceholder("9999.99");

        // Päivämääräkentät
        DatePicker.DatePickerI18n fin = new DatePicker.DatePickerI18n();
        fin.setDateFormat("dd.MM.yyyy");
        orderDateFrom.setI18n(fin);
        orderDateTo.setI18n(fin);
        orderDateFrom.setPlaceholder("pp.kk.vvvv");
        orderDateTo.setPlaceholder("pp.kk.vvvv");

        // Hakunappi
        Button searchBtn = new Button("Hae", VaadinIcon.SEARCH.create());
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> search());

        // Tyhjennänappi
        Button clearBtn = new Button("Tyhjennä", VaadinIcon.CLOSE.create());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearBtn.addClickListener(e -> clearFilters());

        // Ensimmäisellä rivillä nimi/SKU, kategoria ja toimittaja
        HorizontalLayout row1 = new HorizontalLayout(nameorSkuField, categoryField, supplierNameField);
        row1.setWidthFull();
        row1.setFlexGrow(1, nameorSkuField, categoryField, supplierNameField);

        // Toisella rivillä hintaväli ja aktiiviset tuotteet
        HorizontalLayout row2 = new HorizontalLayout(minPriceField, maxPriceField, activeOnlyBox);
        row2.setAlignItems(Alignment.END);

        // Kolmannella rivillä päivämäärähaku
        HorizontalLayout row3 = new HorizontalLayout(orderDateFrom, orderDateTo);

        // Nappirivi
        HorizontalLayout buttons = new HorizontalLayout(searchBtn, clearBtn);
        buttons.setAlignItems(Alignment.END);

        section.add(row1, row2, row3, buttons);
        return section;
    }

    // Funktio, joka rakentaa hakutulososion
    private Div buildResultsSection() {
        Div section = new Div();
        section.addClassName("results-section");
        section.setSizeFull();

        // Tulospalkin otsikko ja tulos-badge
        resultCountBadge.addClassName("result-badge");
        Div resultsHeader = new Div(new Span("Hakutulokset: "), resultCountBadge);
        resultsHeader.addClassName("results-header");

        // Määritetään grid
        configureGrid();
        section.add(resultsHeader, grid);
        
        return section;
    }

    // Funktio, joka määrittelee gridin sarakkeet
    private void configureGrid() {
        grid.addColumn(Product::getName).setHeader("Tuotteen nimi").setAutoWidth(true).setSortable(true);
        grid.addColumn(Product::getSku).setHeader("Tuotekoodi").setAutoWidth(true).setSortable(true);
        grid.addColumn(Product::getCategory).setHeader("Kategoria").setAutoWidth(true).setSortable(true);
        grid.addColumn(p -> p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() + " €" : "")
            .setHeader("Yksikköhinta")
            .setAutoWidth(true)
            .setSortable(true);
        grid.addColumn(Product::getStockQuantity).setHeader("Määrä varastossa").setAutoWidth(true).setSortable(true);
        grid.addColumn(p -> p.getSupplier() != null ? p.getSupplier().getName() : "")
            .setHeader("Toimittaja")
            .setAutoWidth(true)
            .setSortable(false);
        grid.setSizeFull();
        grid.setItems(query -> {
            var pageable = VaadinSpringDataHelpers.toSpringPageRequest(query);
            var page = this.service.getWithSpec(pageable, activeFilter);
            updateResultCount((int) page.getTotalElements());
            return page.stream();
        });
    }

    // Funktio, joka suorittaa haun hakukenttien arvojen perusteella
    private void search() {
        activeFilter = new ProductSearchFilter();

        // Tekstikentät: lähetetään null jos tyhjä (Specification jättää huomioimatta)
        String nameOrSku = nameorSkuField.getValue();
        activeFilter.setNameOrSku(nameOrSku.isBlank() ? null : nameOrSku.trim());

        String category = categoryField.getValue();
        activeFilter.setCategory(category.isBlank() ? null : category.trim());

        String supplierName = supplierNameField.getValue();
        activeFilter.setSupplierName(supplierName.isBlank() ? null : supplierName.trim());

        // Numeeriset kentät
        BigDecimal min = minPriceField.getValue();
        activeFilter.setMinPrice(min);

        BigDecimal max = maxPriceField.getValue();
        activeFilter.setMaxPrice(max);

        // Yksinkertainen boolean-kenttä aktiivisille tuotteille
        activeFilter.setActiveOnly(activeOnlyBox.getValue() ? Boolean.TRUE : null);

        // Päivämääräkentät
        activeFilter.setOrderDateFrom(orderDateFrom.getValue());
        activeFilter.setOrderDateTo(orderDateTo.getValue());

        // Päivämäärävalidointi
        if (activeFilter.getOrderDateFrom() != null && 
            activeFilter.getOrderDateTo() != null &&
            activeFilter.getOrderDateFrom().isAfter(activeFilter.getOrderDateTo())) {

            Notification.show("Tilauspäivän alku ei voi olla myöhempi kuin loppupäivä", 3000, Notification.Position.BOTTOM_END);
            return;
        }

        if (!activeFilter.hasAnyFilter()) {
            Notification.show("Syötä vähintään yksi hakuehto", 3000, Notification.Position.BOTTOM_END);
            return;
        }
        
        refreshGrid();
    }

    // Funktio, joka tyhjentää kaikki hakukentät
    private void clearFilters() {
        nameorSkuField.clear();
        categoryField.clear();
        supplierNameField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        activeOnlyBox.setValue(false);
        orderDateFrom.clear();
        orderDateTo.clear();

        activeFilter = new ProductSearchFilter(); // Resetoidaan aktiivinen suodatin
        refreshGrid();
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    // Funktio, joka päivittää tulos-badgen tekstin hakutulosten määrällä
    private void updateResultCount(int count) {
        resultCountBadge.setText(count + " tulosta");
    }
}