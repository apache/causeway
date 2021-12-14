package org.apache.isis.core.runtimeservices.locale;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.i18n.LanguageProvider;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;

import lombok.RequiredArgsConstructor;

@Service
@Named("isis.runtimeservices.LanguageProviderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LanguageProviderDefault
implements LanguageProvider {

    private final InteractionProvider interactionProvider;

    @Override
    public Optional<Locale> getPreferredLanguage() {
        return interactionProvider.currentInteractionContext()
        .map(InteractionContext::getLocale)
        .map(UserLocale::getLanguageLocale);
    }

}
