package org.apache.isis.viewer.wicket.ui.pages.accmngt;

import javax.inject.Inject;
import java.util.UUID;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.EmailVerificationUrlService;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;

/**
 * An implementation of {@link org.apache.isis.viewer.wicket.ui.pages.EmailVerificationUrlService}
 * that uses Wicket's the current {@link org.apache.wicket.request.cycle.RequestCycle} to create the link and
 * the special {@link org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap}
 * stored in the {@link org.apache.wicket.Application application} as a temporary storage of
 * the non-encrypted data.
 */
public class EmailVerificationUrlServiceDefault implements EmailVerificationUrlService {

    private final PageClassRegistry pageClassRegistry;

    @Inject
    public EmailVerificationUrlServiceDefault(PageClassRegistry pageClassRegistry) {
        this.pageClassRegistry = pageClassRegistry;
    }

    /**
     * Creates a url to the passed <em>pageClass</em> by encrypting the given
     * <em>datum</em> as a first indexed parameter
     *
     * @param pageType The type of the page to link to
     * @param datum The data to encrypt in the url
     * @return The full url to the page with the encrypted data
     */
    @Override
    public String createVerificationUrl(final PageType pageType, final String datum) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");

        final AccountConfirmationMap accountConfirmationMap = getAccountConfirmationMap();
        accountConfirmationMap.put(uuid, datum);

        final PageParameters parameters = new PageParameters();
        parameters.set(0, uuid);

        final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);

        final String fullUrl = fullUrlFor(pageClass, parameters);
        return fullUrl;
    }

    protected String fullUrlFor(final Class<? extends Page> pageClass, final PageParameters parameters) {
        final RequestCycle requestCycle = RequestCycle.get();
        final CharSequence relativeUrl = requestCycle.urlFor(pageClass, parameters);
        final UrlRenderer urlRenderer = requestCycle.getUrlRenderer();
        final String fullUrl = urlRenderer.renderFullUrl(Url.parse(relativeUrl));
        return fullUrl;
    }

    protected AccountConfirmationMap getAccountConfirmationMap() {
        return Application.get().getMetaData(AccountConfirmationMap.KEY);
    }

}
