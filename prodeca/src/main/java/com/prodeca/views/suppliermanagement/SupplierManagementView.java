package com.prodeca.views.suppliermanagement;

import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierContact;
import com.prodeca.services.SupplierContactService;
import com.prodeca.services.SupplierService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@PageTitle("Toimittajat")
@Route(value = "suppliers/:supplierID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.TRUCK_SOLID)
@StyleSheet("themes/prodeca/views/supplier-management-view.css")
@RolesAllowed({"USER", "SUPER"})
@Uses(Icon.class)
public class SupplierManagementView extends Div implements BeforeEnterObserver {
    
    private static final String SUPPLIER_ID = "supplierID";
    private static final String EDIT_ROUTE = "suppliers/%s/edit";

    private final Grid<Supplier> grid = new Grid<>(Supplier.class, false);

    // Lomakekentät
    private TextField name;
    private TextField email;
    private TextField phone;
    private TextField country;
    private TextField registrationNumber;
    private TextField website;
    private TextArea description;
    private Checkbox active;

    // Yhteyshenkilön tietojen näyttö (readonly)
    private Details contactInfoPanel;

    // Napit
    private final Button cancelBtn = new Button("Peruuta");
    private final Button saveBtn = new Button("Tallenna");
    private final Button deleteBtn = new Button("Poista");

    private final BeanValidationBinder<Supplier> binder;
    private Supplier currentSupplier;

    private final SupplierService service;
    private final SupplierContactService contactService;


    public SupplierManagementView(SupplierService service, SupplierContactService contactService) {
        this.service = service;
        this.contactService = contactService;
        addClassName("supplier-management-view");

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Gridin sarakkeet
        grid.addColumn(Supplier::getName).setHeader("Nimi").setAutoWidth(true).setSortable(true);
        grid.addColumn(Supplier::getPhone).setHeader("Puhelinnumero").setAutoWidth(true);
        grid.addColumn(Supplier::getEmail).setHeader("Sähköposti").setAutoWidth(true);
        grid.addColumn(Supplier::getCountry).setHeader("Maa").setAutoWidth(true).setSortable(true);
        grid.addColumn(Supplier::getRegistrationNumber).setHeader("Rekisteröintinumero").setAutoWidth(true);
        grid.addColumn(s -> s.isActive() ? "Kyllä" : "Ei").setHeader("Aktiivinen").setAutoWidth(true);
        grid.addColumn(s -> {
            String fullName = this.contactService.getContactFullName(s);
            return fullName.isBlank() ? "Ei yhteyshenkilöä" : fullName;
        })
        .setHeader("Yhteyshenkilö")
        .setAutoWidth(true);
        grid.setItems(query -> this.service.getWithPageable(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().navigate(String.format(EDIT_ROUTE, e.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SupplierManagementView.class);
            }
        });

        // Binderin sidonta lomakekenttiin
        binder = new BeanValidationBinder<>(Supplier.class);
        binder.bindInstanceFields(this);

        // Nappien käsittelijät
        cancelBtn.addClickListener(e -> { clearForm(); refreshGrid(); });

        saveBtn.addClickListener(e -> {
            try {
                if (currentSupplier == null) currentSupplier = new Supplier();
                binder.writeBean(currentSupplier);
                this.service.save(currentSupplier);
                clearForm();
                refreshGrid();
                Notification.show("Toimittaja tallennettu onnistuneesti");
                UI.getCurrent().navigate(SupplierManagementView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                    "Yhtäaikainen muokkausvirhe: joku muu on muokannut tätä toimittajaa. Lataa tiedot uudestaan ja yritä uudestaan"
                );
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            } catch (ValidationException ex) {
                Notification.show("Tarkista syötteet: " + ex.getMessage());
            }
        });

        deleteBtn.addClickListener(e -> {
            if (currentSupplier != null && currentSupplier.getId() != null) {
                this.service.delete(currentSupplier.getId());
                clearForm();
                refreshGrid();
                Notification.show("Toimittaja poistettu");
                UI.getCurrent().navigate(SupplierManagementView.class);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setVisible(false); // Piilotettu kunnes rivi on valittu
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> supplierId = event.getRouteParameters().get(SUPPLIER_ID).map(Long::parseLong);
        if (supplierId.isPresent()) {
            this.service.getById(supplierId.get()).ifPresentOrElse(
                supplier -> {
                    populateForm(supplier);
                    deleteBtn.setVisible(true);
                    refreshContactInfoPanel(supplier);
                },
                () -> {
                    Notification.show("Toimittajaa ei löytynyt, ID: " + supplierId.get(), 3000, Notification.Position.BOTTOM_START);
                    refreshGrid();
                    event.forwardTo(SupplierManagementView.class);
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
        name = new TextField("Nimi");
        email = new TextField("Sähköposti");
        phone = new TextField("Puhelin");
        country = new TextField("Maa");
        registrationNumber = new TextField("Rekisteröintinumero");
        website = new TextField("Verkkosivut");
        description = new TextArea("Kuvaus");
        active = new Checkbox("Aktiivinen");

        form.add(name, email, phone, country, registrationNumber, website, description, active);

        contactInfoPanel = buildContactInfoPanel();

        innerDiv.add(form, contactInfoPanel);
        editorDiv.add(innerDiv);
        createButtonLayout(editorDiv);
        splitLayout.addToSecondary(editorDiv);
    }

    // Funktio, joka rakentaa yhteyshenkilöpaneelin
    private Details buildContactInfoPanel() {
        VerticalLayout contactContent = new VerticalLayout();
        contactContent.setSpacing(false);
        contactContent.setPadding(false);

        contactContent.getStyle().set("padding", "var(--lumo-space-m)");
        contactContent.getStyle().set("background", "var(--lumo-contrast-5pct)");
        contactContent.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        Span infoText = new Span(
            "Jokaisella toimittajalla voi olla yksi yhteyshenkilö. " +
            "Muokkaa yhteyshenkilön tietoja alla olevalla painikkeella"
        );
        infoText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        // Placeholder-teksti kunnes toimittaja valitaan
        Span contactName = new Span("Ei yhteyshenkilöä valittuna");
        contactName.addClassNames(
            "contact-name-placeholder",
            LumoUtility.FontSize.MEDIUM,
            LumoUtility.FontWeight.MEDIUM
        );

        // Nappi, joka ohjaa SupplierContactView-näkymään
        Button editContactBtn = new Button("Muokkaa yhteyshenkilöä");
        editContactBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editContactBtn.addClickListener(e -> UI.getCurrent().navigate("supplier-contacts"));

        contactContent.add(infoText, contactName, editContactBtn);

        Details details = new Details("Yhteyshenkilö", contactContent);
        details.setOpened(false);
        details.addClassName("contact-info-details");
        return details;
    }

    // Funktio, joka päivittää yhteyshenkilöpaneelin sisällön valitun toimittajan mukaan
    private void refreshContactInfoPanel(Supplier supplier) {
        if (contactInfoPanel == null) return;

        // Haetaan toimittajan yhteyshenkilö tietokannasta
        Optional<SupplierContact> contact = this.contactService.getBySupplier(supplier);
        VerticalLayout content = (VerticalLayout) contactInfoPanel.getContent().findFirst().orElse(null);
        if (content == null) return;

        // Etsitään placeholder-teksti ja päivitetään se
        content.getChildren()
            .filter(c -> c instanceof Span && c.getElement().getClassList().contains("contact-name-placeholder"))
            .findFirst()
            .ifPresent(c -> {
                Span label = (Span) c;
                if (contact.isPresent()) {
                    label.setText(contact.get().getFullName() + " (" + contact.get().getJobTitle() + ")");
                    label.getElement().getClassList().remove("contact-name-placeholder");
                } else {
                    label.setText("Yhteyshenkilöä ei ole vielä luotu");
                }
            });

        contactInfoPanel.setOpened(true);
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
        if (contactInfoPanel != null) contactInfoPanel.setOpened(false);
    }

    private void populateForm(Supplier value) {
        currentSupplier = value;
        binder.readBean(currentSupplier);
    }
}