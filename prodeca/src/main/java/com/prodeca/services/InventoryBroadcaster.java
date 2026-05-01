package com.prodeca.services;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Komponentti, joka vastaa varaston muutosten broadcastauksesta.
 * ProductService käyttää tätä komponenttien jokaisen tallennuksen ja poiston jälkeen, jotta muut komponentit (esim. tilauslomake) voivat reagoida varaston muutoksiin.
 */

@Component
public class InventoryBroadcaster {
    
    // Yksittäinen säie Vaadin push:ia varten
    private static final Executor executor = Executors.newSingleThreadExecutor();

    // Kuuntelija-lista (kaikki auki olevat Vaadin-sessiot)
    private final LinkedList<Consumer<String>> listeners = new LinkedList<>();

    // Rekisteröidään käyttöliittymälle kuuntelija
    public Registration register(Consumer<String> listener) {
        synchronized (this) {
            listeners.add(listener);
        }
        return () -> {
            synchronized (InventoryBroadcaster.this) {
                listeners.remove(listener);
            }
        };
    }

    // Lähetetään viesti kaikille rekisteröidyille kuuntelijoille
    public void broadcast(String message) {
        LinkedList<Consumer<String>> listenersSnapshot;
        synchronized (this) {
            listenersSnapshot = new LinkedList<>(listeners);
        }
        for (Consumer<String> listener : listenersSnapshot) {
            executor.execute(() -> listener.accept(message));
        }
    }
}