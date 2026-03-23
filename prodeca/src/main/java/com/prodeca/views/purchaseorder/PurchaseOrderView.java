package com.prodeca.views.purchaseorder;

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

@PageTitle("PurchaseOrder")
@Route("orders/:samplePersonID?/:action?(edit)")
@Menu(order = 3, icon = LineAwesomeIconUrl.CC_VISA)
@AnonymousAllowed
@Uses(Icon.class)
public class PurchaseOrderView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

    }
}
