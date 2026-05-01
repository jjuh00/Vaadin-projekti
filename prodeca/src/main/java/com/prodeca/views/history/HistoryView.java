package com.prodeca.views.history;

import com.prodeca.data.AuditLogEntry;
import com.prodeca.services.AuditLogService;
import com.prodeca.views.MainLayout;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.format.DateTimeFormatter;

@PageTitle("Muutoshistoria")
@Route(value = "history", layout = MainLayout.class)
@Menu(order = 8, icon = LineAwesomeIconUrl.HISTORY_SOLID)
@StyleSheet("themes/prodeca/views/history-view.css")
@RolesAllowed("ADMIN")
public class HistoryView extends VerticalLayout {

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final AuditLogService logService;
    private final Grid<AuditLogEntry> grid = new Grid<>(AuditLogEntry.class, false);

    private final Select<String> entityTypeFilter = new Select<>();

    public HistoryView(AuditLogService logService) {
        this.logService = logService;

        addClassName("history-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader(), buildFilters(), buildGrid());
    }

    // Funktio, joka rakentaa näkymän headerin
    private HorizontalLayout buildHeader() {
        H2 title = new H2("Muutoshistoria");
        title.addClassName(LumoUtility.Margin.NONE);
        Paragraph description = new Paragraph(
            "Tässä näkymässä näet kaikki entiteetteihin tehdyt muutokset aikajärjestyksessä."
        );
        description.addClassNames(
            LumoUtility.Margin.NONE,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.FontSize.SMALL
        );
        VerticalLayout header = new VerticalLayout(title, description);
        header.setPadding(false);
        header.setSpacing(false);

        HorizontalLayout row = new HorizontalLayout(header);
        row.addClassName("header-section");
        row.setWidthFull();
        return row;
    }

    // Funktio, joka rakentaa suodatinvalikot
    private HorizontalLayout buildFilters() {
        entityTypeFilter.setLabel("Entiteettityyppi");
        entityTypeFilter.setItems("Kaikki", "Product", "Supplier", "SupplierContact", "PurchaseOrder");
        entityTypeFilter.setValue("Kaikki");
        entityTypeFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout filters = new HorizontalLayout(entityTypeFilter);
        filters.addClassName("filter-row");
        filters.setAlignItems(Alignment.END);
        return filters;
    }

    // Funktio, joka rakentaa datagridin
    private Grid<AuditLogEntry> buildGrid() {
        grid.addComponentColumn(entry -> {
            Span span = new Span(entry.getAction() != null ? entry.getAction().name() : "");
            span.addClassName("action-badge");
            span.getStyle()
                .set("padding", "2px 8px")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("border-radius", "12px");
            
            if (null == entry.getAction()) {
                span.getStyle()
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)");
            } else switch (entry.getAction()) {
                case CREATE -> {
                    span.getStyle()
                        .set("background", "var(--lumo-success-color-10pct)")
                        .set("color", "var(--lumo-success-text-color)");
                }
                case DELETE -> {
                    span.getStyle()
                        .set("background", "var(--lumo-error-color-10pct)")
                        .set("color", "var(--lumo-error-text-color)");
                }
                default -> {
                    span.getStyle()
                        .set("background", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-text-color)");
                }
            }
            return span;
        }).setHeader("Toiminto").setWidth("120px").setFlexGrow(0);

        grid.addColumn(AuditLogEntry::getEntityType).setHeader("Entiteetti").setWidth("130px").setFlexGrow(0);
        grid.addColumn(AuditLogEntry::getEntityLabel).setHeader("Kohde").setFlexGrow(1);
        grid.addColumn(AuditLogEntry::getChangedBy).setHeader("Muuttaja").setWidth("140px").setFlexGrow(0);
        grid.addColumn(entry -> entry.getChangedAt() != null ? entry.getChangedAt().format(DTF) : "")
            .setHeader("Aika")
            .setWidth("175px")
            .setFlexGrow(0)
            .setSortable(true);
        grid.addColumn(AuditLogEntry::getDetails).setHeader("Lisätiedot").setFlexGrow(2);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        refreshGrid();
        return grid;
    }

    private void refreshGrid() {
        String filter = entityTypeFilter.getValue();
        if ("Kaikki".equals(filter) || filter == null) {
            grid.setItems(query -> this.logService.getWithPageable(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        } else {
            grid.setItems(query -> this.logService.getByEntityType(filter, VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        }
    }
}