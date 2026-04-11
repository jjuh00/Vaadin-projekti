package com.prodeca.views.adminpanel;

import com.prodeca.data.Role;
import com.prodeca.data.User;
import com.prodeca.services.UserService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.data.domain.Pageable;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@PageTitle("Admin-paneeli")
@Route(value = "admin", layout = MainLayout.class)
@Menu(order = 6, icon = LineAwesomeIconUrl.USER_SHIELD_SOLID)
@RolesAllowed("ADMIN")
public class AdminPanelView extends VerticalLayout {

    private final UserService service;

    private final Grid<User> userGrid = new Grid<>(User.class, false);
    private final TextField searchField = new TextField();

    private User selectedUser;

    public AdminPanelView(UserService service) {
        this.service = service;

        addClassName(LumoUtility.Padding.LARGE);
        setWidthFull();

        H2 title = new H2("Admin-paneeli");
        title.addClassName(LumoUtility.Padding.LARGE);

        Paragraph subtitle = new Paragraph(
            "Ylläpitäjänä voit tarkastella käyttäjiä, muuttaa rooleja ja poistaa tilejä"
        );
        subtitle.addClassNames(LumoUtility.Margin.Bottom.LARGE, LumoUtility.TextColor.SECONDARY);

        add(title, subtitle);
        add(buildSummaryCards());
        add(buildUserManagementSection());
    }

    // Funktio, joka rakentaa yhteenvetokortit.
    // Kortit näyttävät käyttäjämäärän rooleittain
    private FlexLayout buildSummaryCards() {
        var allUsers = this.service.getWithPageable(Pageable.unpaged()).getContent();

        long adminCount = allUsers.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().contains(Role.ADMIN))
            .count();
        long superCount = allUsers.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().contains(Role.SUPER))
            .count();
        long userCount = allUsers.stream()
            .filter(u -> u.getRoles() != null && u.getRoles().contains(Role.USER))
            .count();
        long totalCount = allUsers.size();

        FlexLayout cards = new FlexLayout(
            buildSummaryCard("Käyttäjiä yhteensä", String.valueOf(totalCount), VaadinIcon.USERS, "var(--lumo-primary-color)"),
            buildSummaryCard("Ylläpitäjiä (ADMIN)", String.valueOf(adminCount), VaadinIcon.USER_STAR, "hsl(35, 90%, 50%)"),
            buildSummaryCard("Superkäyttäjiä (SUPER)", String.valueOf(superCount), VaadinIcon.USER_CLOCK, "hsl(270, 60%, 55)"),
            buildSummaryCard("Tavallisia käyttäjiä (USER)", String.valueOf(userCount), VaadinIcon.USER, "hsl(145, 55%, 40%)")
        );
        cards.addClassNames(LumoUtility.Margin.Bottom.LARGE, LumoUtility.Gap.MEDIUM);
        cards.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cards.setWidthFull();
        return cards;
    }

    // Funktio, joka rakentaa  yksittäisen yhteenvetokortin
    private Div buildSummaryCard(String label, String value, VaadinIcon icon, String accentColor) {
        Div card = new Div();
        card.addClassNames(
            LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL
        );
        card.getStyle()
            .set("flex", "1 1 160px")
            .set("min-width", "150px")
            .set("background", "var(--lumo-base-color)")
            .set("border-left", "4px solid " + accentColor);

        Span iconSpan = new Span(icon.create());
        iconSpan.getStyle().set("color", accentColor).set("font-size", "1.5rem");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(
            LumoUtility.Display.BLOCK, LumoUtility.TextColor.PRIMARY,
            LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD
        );

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(
            LumoUtility.Display.BLOCK, LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.SMALL
        );

        card.add(iconSpan, valueSpan, labelSpan);
        return card;
    }

    // Funktio, joka rakentaa käyttäjähallinnan osion
    private VerticalLayout buildUserManagementSection() {
        H3 sectionTitle = new H3("Käyttäjähallinta");
        sectionTitle.addClassName(LumoUtility.Margin.Bottom.SMALL);

        // Hakukenttä
        searchField.setPlaceholder("Hae käyttäjä- tai näyttönimellä");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());

        // Roolien muokkaus
        Select<Role> roleSelect = new Select<>();
        roleSelect.setLabel("Uusi rooli valitulle käyttäjälle");
        roleSelect.setItems(Role.values());
        roleSelect.setPlaceholder("Valitse rooli");
        roleSelect.setWidth("220px");

        Button changeRoleBtn = new Button("Vaihda rooli", VaadinIcon.REFRESH.create());
        changeRoleBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        changeRoleBtn.setEnabled(false); // Aktivoidaan käyttäjän valinnan jälkeen
        changeRoleBtn.addClickListener(e -> {
            if (selectedUser == null || roleSelect.getValue() == null) return;
            selectedUser.setRoles(Set.of(roleSelect.getValue()));
            this.service.save(selectedUser);
            Notification ok = Notification.show("Käyttäjän '" + selectedUser.getUsername() + "' rooli päivitetty: " + roleSelect.getValue(), 3000, Notification.Position.BOTTOM_START);
            ok.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshGrid();
        });
 
        // Käyttäjien poisto
        Button deleteBtn = new Button("Poista käyttäjä", VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteBtn.setEnabled(false); // Aktivoidaan käyttäjän valinnan jälkeen
        deleteBtn.addClickListener(e -> {
            if (selectedUser == null) return;
            ConfirmDialog dialog = new ConfirmDialog(
                "Vahvista poisto",
                "Halautko varmasti poistaa käyttäjän '" + selectedUser.getUsername() + "'? " +
                "Tätä toimintoa ei voi peruuttaa",
                "Poista", confirmEvent -> {
                    this.service.delete(selectedUser.getId());
                    selectedUser = null;
                    changeRoleBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                    Notification.show("Käyttäjä poistettu onnistuneesti", 3000, Notification.Position.BOTTOM_START);
                    refreshGrid();
                },
                "Perruta", cancelEvent -> {}
            );
            dialog.setConfirmButtonTheme("error primary");
            dialog.open();
        });

        HorizontalLayout toolbar = new HorizontalLayout(searchField, roleSelect, changeRoleBtn, deleteBtn);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setFlexGrow(1, searchField);
        toolbar.setWidthFull();
        toolbar.addClassName(LumoUtility.Margin.Bottom.SMALL);

        // Käyttäjägrid
        configureGrid(changeRoleBtn, deleteBtn, roleSelect);

        VerticalLayout layout = new VerticalLayout(sectionTitle, toolbar, userGrid);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        return layout;
    }

    // Funktio, joka määrittää gridin sarakkeet ja rivin valinnan logiikan
    private void configureGrid(Button changeRoleBtn, Button deleteBtn, Select<Role> roleSelect) {
        userGrid.addColumn(User::getUsername).setHeader("Käyttäjänimi").setAutoWidth(true).setSortable(true);
        userGrid.addColumn(User::getName).setHeader("Näyttönimi").setAutoWidth(true).setSortable(true);
        userGrid.addColumn(User::getEmail).setHeader("Sähköposti").setAutoWidth(true);
        userGrid.addColumn(u -> {
            if (u.getRoles() == null || u.getRoles().isEmpty()) return "-";
            StringJoiner joiner = new StringJoiner(", ");
            for (Role r : u.getRoles()) {
                joiner.add(r.name());
            }
            return joiner.toString();
        }).setHeader("Roolit").setAutoWidth(true);
        
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        userGrid.setWidthFull();
        userGrid.setHeight("400px");

        // Rivin valinta aktivoi roolin vaihdon ja käyttäjän poiston
        userGrid.asSingleSelect().addValueChangeListener(e -> {
            selectedUser = e.getValue();
            boolean hasSelection = selectedUser != null;
            changeRoleBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
            if (hasSelection && selectedUser.getRoles() != null
                && !selectedUser.getRoles().isEmpty()) {
                    // Esikäytetään roolivalikko nykyisellä roolilla
                    roleSelect.setValue(selectedUser.getRoles().iterator().next());
            } else {
                roleSelect.clear();   
            }
        });

        refreshGrid();
    }

    private void refreshGrid() {
        String filter = searchField.getValue().trim().toLowerCase();
        List<User> allUsers = this.service.getWithPageable(Pageable.unpaged()).getContent();

        if (filter.isBlank()) {
            userGrid.setItems(allUsers);
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            boolean usernameMatches = u.getUsername() != null && u.getUsername().toLowerCase().contains(filter);
            boolean nameMatches = u.getName() != null && u.getName().toLowerCase().contains(filter);
            if (usernameMatches || nameMatches) {
                filtered.add(u);
            }
        }
        
        userGrid.setItems(filtered);
    }
}