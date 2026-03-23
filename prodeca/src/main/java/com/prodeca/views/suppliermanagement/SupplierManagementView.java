package com.prodeca.views.suppliermanagement;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("SupplierManagement")
@Route("suppliers/:samplePersonID?/:action?(edit)")
@Menu(order = 2, icon = LineAwesomeIconUrl.DROPBOX)
@AnonymousAllowed
@Uses(Icon.class)
public class SupplierManagementView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) { 

    }
}
