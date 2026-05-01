package com.prodeca.i18n;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Komponentti, joka lukee prodeca_locale cookie-arvon aina uudella Vaadin UI-instanssilla ja asettaa
 * sen sessioon. Näin sovellus osaa tarjota oikean kielisiä käännöksiä käyttäjälle
*/

@Component
public class LocaleInitListener implements VaadinServiceInitListener {
    
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInitEvent -> {
            // executeJs on tapa lukea cookie palvelinpuolelta
            uiInitEvent.getUI().getPage().executeJs(
                // Palautetaan prodeca_locale cookie-arvo tai tyhjä merkkijono
                "const cookie = document.cookie.split(';')" +
                " .map(str => str.trim())" +
                " .find(str => str.startsWith('prodeca_locale='));" +
                "return cookie ? cookie.split('=')[1] : '';"
            ).then(String.class, lang -> {
                if (lang != null && !lang.isBlank()) {
                    Locale restored = Locale.forLanguageTag(lang);
                    // Asetetaan, jos cookie-arvo on tuettu kieli
                    if ("fi".equals(restored.getLanguage()) || "en".equals(restored.getLanguage())) {
                        uiInitEvent.getUI().getSession().setLocale(restored);
                    }
                }
            });
        });
    }
}