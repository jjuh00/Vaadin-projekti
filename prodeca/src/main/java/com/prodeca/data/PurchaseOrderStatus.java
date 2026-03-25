package com.prodeca.data;

// Enum PurchaseOrder status elinkaarelle. PurchaseOrder.status käyttää tätä enumia
public enum PurchaseOrderStatus {
    LUONNOS,
    VASTAANOTETTU,
    VAHVISTETTU,
    KULJETUKSESSA,
    TOIMITETTU,
    PERUUTETTU   
}