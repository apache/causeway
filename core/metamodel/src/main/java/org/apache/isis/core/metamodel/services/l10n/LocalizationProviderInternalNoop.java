package org.apache.isis.core.metamodel.services.l10n;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.profiles.Localization;

@DomainService(nature = NatureOfService.DOMAIN)
public class LocalizationProviderInternalNoop extends LocalizationProviderInternalAbstract {

    private final Localization defaultLocalization = new LocalizationDefault();

    @Override
    public Localization getLocalization() {
        return defaultLocalization;
    }
}
