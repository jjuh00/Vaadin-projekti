package com.prodeca.views.suppliercontact;

import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierContact;
import com.prodeca.services.SupplierContactService;
import com.prodeca.services.SupplierService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Optional;

@PageTitle("Yhteyshenkilöt")
@Route("supplier-contacts/:contactID?/:action?(edit)")
@Menu(order = 2, icon = LineAwesomeIconUrl.ADDRESS_CARD_SOLID)
@StyleSheet("themes/prodeca/views/supplier-contact-view.css")
@AnonymousAllowed
@Uses(Icon.class)
public class SupplierContactView extends Div implements BeforeEnterObserver {
    
    private static final String CONTACT_ID = "contactID";
    private static final String EDIT_ROUTE = "supplier-contacts/%s/edit";

    private final Grid<SupplierContact> grid = new Grid<>(SupplierContact.class, false);
 
    // Lomakekentät
    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private TextField jobTitle;
    private TextArea notes;

    // ComboBox linkitettyjen toimittajien valintaan
    private ComboBox<Supplier> supplierComboBox;

    // Napit
    private final Button cancelBtn = new Button("Peruuta");
    private final Button saveBtn = new Button("Tallenna");
    private final Button deleteBtn = new Button("Poista");

    private final BeanValidationBinder<SupplierContact> binder;
    private SupplierContact currentContact;

    private final SupplierContactService contactService;
    private final SupplierService supplierService;

    public SupplierContactView(SupplierContactService contactService, SupplierService supplierService) {
        this.contactService = contactService;
        this.supplierService = supplierService;
        addClassName("supplier-contact-view");

        Div contextBanner = buildContextBanner();
        add(contextBanner);

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        add(splitLayout);

        // Gridin sarakkeet
        grid.addColumn(c -> c.getSupplier() != null ? c.getSupplier().getName() : "Ei määritettyä toimittajaa")
            .setHeader("Toimittaja")
            .setAutoWidth(true)
            .setSortable(true);
        grid.addColumn(SupplierContact::getFullName).setHeader("Koko nimi").setAutoWidth(true);
        grid.addColumn(SupplierContact::getJobTitle).setHeader("Titteli").setAutoWidth(true);
        grid.addColumn(SupplierContact::getEmail).setHeader("Sähköposti").setAutoWidth(true);
        grid.addColumn(SupplierContact::getPhone).setHeader("Puhelinnumero").setAutoWidth(true);
        grid.setItems(query -> this.contactService.getWithPageable(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);  
        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                UI.getCurrent().navigate(String.format(EDIT_ROUTE , e.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SupplierContactView.class);
            }
        });

        // Binderin sidonta lomakekenttiin
        binder = new BeanValidationBinder<>(SupplierContact.class);
        binder.bindInstanceFields(this);
        // Sidotaan CombBox manuaalisesti
        binder.bind(supplierComboBox, SupplierContact::getSupplier, SupplierContact::setSupplier);

        // Nappien käsittelijät
        cancelBtn.addClickListener(e -> { clearForm(); refreshGrid(); });

        saveBtn.addClickListener(e -> {
            try {
                if (currentContact == null) currentContact = new SupplierContact();
                binder.writeBean(currentContact);
                this.contactService.save(currentContact);
                clearForm();
                refreshGrid();
                Notification.show("Yhteystiedot tallennettu onnistuneesti");
                UI.getCurrent().navigate(SupplierContactView.class);
            } catch (ObjectOptimisticLockingFailureException ex) {
                Notification n = Notification.show(
                    "Yhtäaikainen muokkausvirhe: joku muu on muokannut tämän toimittajan tietoja. Lataa tiedot uudestaan ja yritä uudestaan"
                );
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            } catch (ValidationException ex) {
                Notification.show("Tarkista syötteet: " + ex.getMessage());
            }
        });

        deleteBtn.addClickListener(e -> {
            if (currentContact != null && currentContact.getId() != null) {
                this.contactService.delete(currentContact.getId());
                clearForm();
                refreshGrid();
                Notification.show("Yhteyshenkilö poistettu");
                UI.getCurrent().navigate(SupplierContactView.class);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.setVisible(false); // Piilotettu kunnes rivi on valittu
    }

    // Funktio, joka rakentaa kuvaavan bannerin. Selventää tämän näkymän tarkoituksen
    // suhteessa SupplierManagementView-näkymään
    private Div buildContextBanner() {
        Div banner = new Div();
        banner.addClassName("context-banner");

        banner.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        banner.getStyle().set("padding", "var(--lumo-space-m)");
        banner.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        banner.getStyle().set("border-left", "4px solid var(--lumo-primary-color)");
        banner.getStyle().set("border-radius", "var(--lumo-border-radius-s)");

        H3 bannerTitle = new H3("Toimittajien yhteyshenkilöt");
        bannerTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.MEDIUM);

        Paragraph bannerDesc = new Paragraph(
            "Tässä näkymässä voit hallinnoida toimittajiin liittyviä yhteyshenkiöitä. " +
            "Jokaisella toimittajalla voi olla yksi yhteyshenkilö. " +
            "Toimittajan perusteidot (nimi, puhelin, maa, rekisteröintinumero jne.) " +
            "hallinnoidaan erikseen Toimittajat-näkymässä"
        );
        bannerDesc.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.SMALL
        );

        // Ristiinnavigointilinkki
        Button suppliersBtn = new Button("Siirry toimittajiin");
        suppliersBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        suppliersBtn.addClickListener(e -> UI.getCurrent().navigate("suppliers"));
        
        banner.add(bannerTitle, bannerDesc, suppliersBtn);
        return banner;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> contactId = event.getRouteParameters().get(CONTACT_ID).map(Long::parseLong);
        if (contactId.isPresent()) {
            this.contactService.getById(contactId.get()).ifPresentOrElse(
                contact -> {
                    populateForm(contact);
                    deleteBtn.setVisible(true);
                },
                () -> {
                    Notification.show("Yhteystietoja ei löytynyt, ID: " + contactId.get(), 3000, Notification.Position.BOTTOM_START);
                    refreshGrid();
                    event.forwardTo(SupplierContactView.class);
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

        supplierComboBox = new ComboBox<>("Toimittaja");
        supplierComboBox.setItems(this.supplierService.getAll());
        supplierComboBox.setItemLabelGenerator(Supplier::getName);
        supplierComboBox.setHelperText("Valitse toimittaja, johon yhteyshenkilö liitetään");
        
        firstName = new TextField("Etunimi");
        lastName = new TextField("Sukunimi");
        email = new TextField("Sähköposti");
        phone = new TextField("Puhelinnumero");
        jobTitle = new TextField("Titteli");
        notes = new TextArea("Lisätiedot");

        form.add(supplierComboBox, firstName, lastName, email, phone, jobTitle, notes);
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
        wrapper.add(grid);
        splitLayout.addToPrimary(wrapper);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
        deleteBtn.setVisible(false);
    }

    private void populateForm(SupplierContact value) {
        currentContact = value;
        binder.readBean(currentContact);
    }
}