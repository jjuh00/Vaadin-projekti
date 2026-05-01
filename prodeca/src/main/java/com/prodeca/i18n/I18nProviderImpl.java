package com.prodeca.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class I18nProviderImpl implements I18NProvider {
 
    private static final Logger log = LoggerFactory.getLogger(I18nProviderImpl.class);

    private static final List<Locale> SUPPORTED = List.of(
        Locale.forLanguageTag("fi"), Locale.ENGLISH
    );

    @Override
    public List<Locale> getProvidedLocales() {
        return SUPPORTED;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        // Jos käännöstä ei löydy, käytetään englannin käännöstä varakäännöksenä
        Locale effective = SUPPORTED.contains(locale) ? locale : Locale.ENGLISH;

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                "i18n/messages", effective, I18nProviderImpl.class.getClassLoader()
            );
            String pattern = bundle.getString(key);
            return params.length == 0 ? pattern : MessageFormat.format(pattern, params);
        } catch (MissingResourceException e) {
            log.warn("Käännösavainta '{}' ei löytynyt localelle '{}'", key, effective);
            return '!' + key + '!';
        }
    }
}