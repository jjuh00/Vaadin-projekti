package com.prodeca.views.productinventory;

import com.prodeca.data.Product;
import com.prodeca.data.Supplier;
import com.prodeca.services.ProductService;
import com.prodeca.services.SupplierService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Optional;

@PageTitle("Tuotevarasto")
@Route("products/:productID?/:action?(edit)")
@Menu(order = 3, icon = LineAwesomeIconUrl.BOX_SOLID)
@StyleSheet("themes/prodeca/views/product-inventory-view.css")
@AnonymousAllowed
@Uses(Icon.class)
public class ProductInventoryView extends Div implements BeforeEnterObserver {

    private static final String PRODUCT_ID = "productID";
    private static final String EDIT_ROUTE = "products/%s/edit";

    private final Grid<Product> grid = new Grid<>(Product.class, false);

    // Lomakekentät
    private TextField name;
    private TextField sku;
    private BigDecimalField unitPrice;
    private IntegerField stockQuantity;
    private TextField category;
    private TextArea description;
    private BigDecimalField weight;
    private Checkbox active;

    // ComboBox linkitettyjen toimittajien valintaan
    private ComboBox<Supplier> supplierComboBox;

    // Napit
    private final Button cancelBtn = new Button("Peruuta");
    private final Button saveBtn = new Button("Tallenna");
    private final Button deleteBtn = new Button("Poista");

    private final BeanValidationBinder<Product> binder;
    private Product currentProduct;

    private final ProductService productService;
    private final SupplierService supplierService;

    public ProductInventoryView(ProductService productService, SupplierService supplierService) {
        this.productService = productService;
        this.supplierService = supplierService;
        addClassName("product-inventory-view");

        setSizeFull();

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Gridin sarakkeet
        grid.addColumn(Product::getName).setHeader("Tuotteen nimi").setAutoWidth(true).setSortable(true);
        grid.addColumn(Product::getSku).setHeader("Tuotekoodi").setAutoWidth(true);
        grid.addColumn(Product::getCategory).setHeader("Kategoria").setAutoWidth(true).setSortable(true);
        grid.addColumn(Product::getUnitPrice).setHeader("Yksikköhinta").setAutoWidth(true);
        grid.addColumn(Product::getStockQuantity).setHeader("Määrä varastossa").setAutoWidth(true);
        grid.addColumn(p -> p.getSupplier() != null ? p.getSupplier().getName() : "Ei määritettyä toimittajaa")
            .setHeader("Toimittaja")
            .setAutoWidth(true)
            .setSortable(true);
        grid.addColumn(p -> p.isActive() ? "Kyllä" : "Ei").setHeader("Aktiivinen").setAutoWidth(true);
        grid.setItems(query -> this.productService.getWithPageable(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().navigate(String.format(EDIT_ROUTE, e.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ProductInventoryView.class);
            }
        });

        // Binderin sidonta lomakekenttiin
        binder = new BeanValidationBinder<>(Product.class);
        binder.bindInstanceFields(this);
        // Sidotaan ComboBox manuaalisesti
        binder.bind(supplierComboBox, Product::getSupplier, Product::setSupplier);

        // Nappien käsittelijät
        cancelBtn.addClickListener(e -> { clearForm(); refreshGrid(); });

        saveBtn.addClickListener(e -> {
            try {
                if (currentProduct == null) currentProduct = new Product();
                binder.writeBean(currentProduct);
                this.productService.save(currentProduct);
                clearForm();
                refreshGrid();
                Notification.show("Tuote tallennettu onnistuneesti");
                UI.getCurrent().navigate(ProductInventoryView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                    "Yhtäaikainen muokkausvirhe: joku muu on muokannut tätä tuotetta. Lataa tiedot uudestaan ja yritä uudestaan"
                );
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            } catch (ValidationException ex) {
               Notification.show("Tarkista syötteet: " + ex.getMessage()); 
            }
        });

        deleteBtn.addClickListener(e -> {
            if (currentProduct != null && currentProduct.getId() != null) {
                this.productService.delete(currentProduct.getId());
                clearForm();
                refreshGrid();
                Notification.show("Tuote poistettu onnistuneesti");
                UI.getCurrent().navigate(ProductInventoryView.class);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setVisible(false); // Piilotettu kunnes rivi on valittu
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> productId = event.getRouteParameters().get(PRODUCT_ID).map(Long::parseLong);
        if (productId.isPresent()) {
            this.productService.getById(productId.get()).ifPresentOrElse(
                product -> {
                    populateForm(product);
                    deleteBtn.setVisible(true);
                },
                () -> {
                    Notification.show("Tuotetta ei löytynyt, ID: " + productId.get(), 3000, Position.BOTTOM_START);
                    refreshGrid();
                    event.forwardTo(ProductInventoryView.class);
                }
            );
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorDiv = new Div();
        editorDiv.setClassName("editor-layout");
        Div innerDiv = new Div();
        innerDiv.setClassName("editor");

        FormLayout form = new FormLayout();
        name = new TextField("Tuotteen nimi");
        sku = new TextField("Tuotekoodi");
        unitPrice = new BigDecimalField("Yksikköhinta (€)");
        stockQuantity = new IntegerField("Määrä varastossa");
        category = new TextField("Kategoria");
        description = new TextArea("Kuvaus");
        weight = new BigDecimalField("Paino (kg)");
        active = new Checkbox("Aktiivinen");

        supplierComboBox = new ComboBox<>("Toimittaja");
        supplierComboBox.setItems(this.supplierService.getAll());
        supplierComboBox.setItemLabelGenerator(Supplier::getName);

        form.add(name, sku, category, unitPrice, stockQuantity, supplierComboBox, weight, description, active);
        innerDiv.add(form);
        editorDiv.add(innerDiv);
        createButtonLayout(editorDiv);
        splitLayout.addToSecondary(editorDiv);
    }

    private void createButtonLayout(Div editorDiv) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setClassName("button-layout");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttons.add(saveBtn, cancelBtn, deleteBtn);
        editorDiv.add(buttons);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
        deleteBtn.setVisible(false);
    }

    private void populateForm(Product value) {
        currentProduct = value;
        binder.readBean(currentProduct);
    }
}