package org.apache.isis.viewer.wicket.viewer.services;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.linking.DeepLinkService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;

/**
 * An implementation of {@link org.apache.isis.applib.services.linking.DeepLinkService}
 * for Wicket Viewer
 */
@DomainService
public class DeepLinkServiceWicket implements DeepLinkService {

    private final PageClassRegistry pageClassRegistry;

    /**
     * Note: we instantiate {@link org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault} and
     * {@link org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault} because there is no easy
     * way to inject these objects.  In other words we do <i>not</i> honour any custom implementations of these service
     * that are bound in {@link org.apache.isis.viewer.wicket.viewer.IsisWicketModule}.  We expect to address this
     * limitation as and when we refactor to using CDI.
     */
    public DeepLinkServiceWicket() {
        this.pageClassRegistry = new PageClassRegistryDefault(new PageClassListDefault());
    }


    @Programmatic
    @Override
    public URI deepLinkFor(final Object domainObject) {

        final AdapterManagerDefault adapterManager = getAdapterManager();
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(domainObject);
        final PageParameters pageParameters = EntityModel.createPageParameters(objectAdapter);

        final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(PageType.ENTITY);

        final RequestCycle requestCycle = RequestCycle.get();
        final CharSequence urlForPojo = requestCycle.urlFor(pageClass, pageParameters);
        final String fullUrl = requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlForPojo));
        try {
            return new URI(fullUrl);
        } catch (final URISyntaxException ex) {
            throw new RuntimeException("Cannot create a deep link to domain object: " + domainObject, ex);
        }
    }

    protected AdapterManagerDefault getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
}
