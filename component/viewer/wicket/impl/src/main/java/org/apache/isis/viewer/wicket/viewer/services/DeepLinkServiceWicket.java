package org.apache.isis.viewer.wicket.viewer.services;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;
import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * An implementation of {@link org.apache.isis.applib.services.linking.DeepLinkService}
 * for Wicket Viewer
 */
@DomainService
public class DeepLinkServiceWicket implements DeepLinkService {
    private final PageClassRegistry pageClassRegistry;

    @Inject
    public DeepLinkServiceWicket() {
        // TODO PageClassRegistry impl should be injected somehow
        this.pageClassRegistry = new PageClassRegistryDefault(new PageClassListDefault());
    }

    @Programmatic
    @Override
    public URI deepLinkFor(Object domainObject) {

        AdapterManagerDefault adapterManager = IsisContext.getPersistenceSession().getAdapterManager();
        ObjectAdapter objectAdapter = adapterManager.adapterFor(domainObject);
        PageParameters pageParameters = EntityModel.createPageParameters(objectAdapter);

        final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(PageType.ENTITY);

        RequestCycle requestCycle = RequestCycle.get();
        CharSequence urlForPojo = requestCycle.urlFor(pageClass, pageParameters);
        String fullUrl = requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlForPojo));
        try {
            return new URI(fullUrl);
        } catch (URISyntaxException x) {
            throw new RuntimeException("Cannot create a deep link to domain object: " + domainObject, x);
        }
    }
}
