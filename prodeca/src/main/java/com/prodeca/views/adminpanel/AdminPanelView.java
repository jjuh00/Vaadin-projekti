package com.prodeca.views.adminpanel;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.RolesAllowed;

@PageTitle("AdminPanel")
@Route("admin")
@Menu(order = 5, icon = LineAwesomeIconUrl.FILE)
@RolesAllowed("ADMIN")
public class AdminPanelView extends VerticalLayout {

    public AdminPanelView() {
        
    }

}
