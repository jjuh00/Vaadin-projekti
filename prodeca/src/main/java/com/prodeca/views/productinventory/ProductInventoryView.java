package com.prodeca.views.productinventory;

import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("ProductInventory")
@Route("products")
@Menu(order = 1, icon = LineAwesomeIconUrl.FOLDER)
@AnonymousAllowed
@Uses(Icon.class)
public class ProductInventoryView extends Div {

}