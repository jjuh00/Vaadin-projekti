package com.prodeca.views.purchaseorder;

import com.prodeca.data.Product;
import com.prodeca.data.PurchaseOrder;
import com.prodeca.data.PurchaseOrderItem;
import com.prodeca.data.PurchaseOrderStatus;
import com.prodeca.services.ProductService;
import com.prodeca.services.PurchaseOrderService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Tilaukset")
@Route(value = "orders/:orderID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.SHOPPING_CART_SOLID)
@StyleSheet("themes/prodeca/views/purchase-order-view.css")
@RolesAllowed({"USER", "SUPER"})
@Uses(Icon.class)
public class PurchaseOrderView extends Div implements BeforeEnterObserver {

    private static final String ORDER_ID = "orderID";
    private static final String EDIT_ROUTE = "orders/%s/edit";

    private final Grid<PurchaseOrder> grid = new Grid<>(PurchaseOrder.class, false);

    // Lomakekentät
    private TextField orderNumber;
    private DatePicker orderDate;
    private ComboBox<PurchaseOrderStatus> status;
    private DatePicker expectedDeliveryDate;
    private TextArea notes;

    // MultiSelectComboBox linkitettyjen tuotteiden valintaan
    private MultiSelectComboBox<Product> productsComboBox;

    // Napit
    private final Button cancelBtn = new Button("Peruuta");
    private final Button saveBtn = new Button("Tallenna");
    private final Button deleteBtn = new Button("Poista");

    private final BeanValidationBinder<PurchaseOrder> binder;
    private PurchaseOrder currentOrder;

    private final PurchaseOrderService orderService;
    private final ProductService productService;

    public PurchaseOrderView(PurchaseOrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
        addClassName("purchase-order-view");

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Gridin sarakkeet
        grid.addColumn(PurchaseOrder::getOrderNumber).setHeader("Tilausnumero").setAutoWidth(true).setSortable(true);
        grid.addColumn(PurchaseOrder::getOrderDate).setHeader("Tilauspäivä").setAutoWidth(true).setSortable(true);
        grid.addColumn(o -> o.getStatus() != null ? o.getStatus().name() : "Ei määritettyä tilausta").setHeader("Tila").setAutoWidth(true);
        grid.addColumn(PurchaseOrder::getTotalAmount).setHeader("Kokonaissumma (€").setAutoWidth(true);
        grid.addColumn(PurchaseOrder::getProductSummary).setHeader("Tuotteet").setAutoWidth(true);
        grid.setItems(query -> this.orderService.getWithPageable(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().navigate(String.format(EDIT_ROUTE, e.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(PurchaseOrderView.class);
            }
        });

        binder = new BeanValidationBinder<>(PurchaseOrder.class);

        binder.forField(expectedDeliveryDate)
                .withValidator((date, context) -> {
                    // Haetaan tilauksen tila lomakekentästä
                    PurchaseOrderStatus currentStatus = status.getValue();

                    // Jos tilaus on TOIMITETTU, mikä tahansa pvm kelpaa
                    if (PurchaseOrderStatus.TOIMITETTU.equals(currentStatus)) {
                        return ValidationResult.ok();
                    }

                    // Muissa tiloissa vaaditaan odotettu toimitupvm, joka on tänään tai tulevaisuudessa
                    if (date != null && date.isBefore(LocalDate.now())) {
                        return ValidationResult.error("Odotetun toimituspäivän on oltava tänää tai tulevaisuudessa");
                    }  

                    return ValidationResult.ok();
                })
                .bind(PurchaseOrder::getExpectedDeliveryDate, PurchaseOrder::setExpectedDeliveryDate);

        binder.bindInstanceFields(this);

        // Nappien käsittelijät
        cancelBtn.addClickListener(e -> { clearForm(); refreshGrid(); });

        saveBtn.addClickListener(e -> {
            try {
                if (currentOrder == null) currentOrder = new PurchaseOrder();
                binder.writeBean(currentOrder);

                // Haetaan valitut tuotteet ja tallennetaan ne tilaukseen
                List<Product> selected = new ArrayList<>(productsComboBox.getSelectedItems());
                this.orderService.saveWithProducts(currentOrder, selected);

                clearForm();
                refreshGrid();
                Notification.show("Tilaus tallennettu onnistuneesti");
                UI.getCurrent().navigate(PurchaseOrderView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                    "Yhtäaikainen muokkausvirhe: joku muu on muokannut tämän tilauksen tietoja. Lataa tiedot uudestaan ja yritä uudestaan"
                );
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            } catch (ValidationException ex) {
                Notification.show("Tarkista syötteet: " + ex.getMessage()); 
            }
        });

        deleteBtn.addClickListener(e -> {
            if (currentOrder != null && currentOrder.getId() != null) {
                this.orderService.delete(currentOrder.getId());
                clearForm();
                refreshGrid();
                Notification.show("Tilaus poistettu onnistuneesti");
                UI.getCurrent().navigate(PurchaseOrderView.class);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setVisible(false); // Piilotettu kunnes rivi on valittu
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> orderId = event.getRouteParameters().get(ORDER_ID).map(Long::parseLong);
        if (orderId.isPresent()) {
            this.orderService.getById(orderId.get()).ifPresentOrElse(
                order -> {
                    populateForm(order);
                    deleteBtn.setVisible(true);
                },
                () -> {
                    Notification.show("Tilausta ei löytynyt, ID: " + orderId.get(), 3000, Notification.Position.BOTTOM_START);
                    refreshGrid();
                    event.forwardTo(PurchaseOrderView.class);
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
        orderNumber = new TextField("Tilausnumero");
        orderDate = new DatePicker("Tilauspäivä");
        expectedDeliveryDate = new DatePicker("Arvioitu toimituspäivä");
        notes = new TextArea("Lisätiedot");
        status = new ComboBox<>("Tila");

        status.setItems(PurchaseOrderStatus.values());
        status.setValue(PurchaseOrderStatus.LUONNOS);

        productsComboBox = new MultiSelectComboBox<>("Tuotteet");
        productsComboBox.setItems(this.productService.getActive());
        productsComboBox.setItemLabelGenerator(Product::getName);

        form.add(orderNumber, orderDate, status, expectedDeliveryDate, productsComboBox, notes);
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

    private void populateForm(PurchaseOrder value) {
        currentOrder = value;
        binder.readBean(currentOrder);

        if (value != null && value.getOrderItems() != null) {
            Set<Product> linked = new HashSet<>();
            for (PurchaseOrderItem item : value.getOrderItems()) {
                linked.add(item.getProduct());
            }
            productsComboBox.setValue(linked);
        } else {
            productsComboBox.clear();
        }
    }
}