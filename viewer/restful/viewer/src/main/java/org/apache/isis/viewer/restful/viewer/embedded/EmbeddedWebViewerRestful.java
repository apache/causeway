package org.apache.isis.viewer.restful.viewer.embedded;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

import org.apache.isis.core.commons.lang.MapUtils;
import org.apache.isis.runtimes.dflt.runtime.web.EmbeddedWebViewer;
import org.apache.isis.runtimes.dflt.runtime.web.WebAppSpecification;
import org.apache.isis.core.webapp.IsisSessionFilter;
import org.apache.isis.core.webapp.StaticContentFilter;
import org.apache.isis.core.webapp.servlets.ResourceServlet;
import org.apache.isis.viewer.restful.viewer.RestfulApplication;
import org.apache.isis.viewer.restful.viewer.authentication.AuthenticationSessionLookupStrategyTrusted;

final class EmbeddedWebViewerRestful extends EmbeddedWebViewer {
    @Override
    public WebAppSpecification getWebAppSpecification() {
        WebAppSpecification webAppSpec = new WebAppSpecification();

        webAppSpec.addContextParams("isis.viewers", "restful");

        webAppSpec.addContextParams(RestfulViewerInstaller.JAVAX_WS_RS_APPLICATION, RestfulApplication.class.getName());

        webAppSpec.addServletContextListener(ResteasyBootstrap.class);

        webAppSpec.addFilterSpecification(IsisSessionFilter.class, MapUtils.asMap(
            IsisSessionFilter.AUTHENTICATION_SESSION_LOOKUP_STRATEGY_KEY,
            AuthenticationSessionLookupStrategyTrusted.class.getName()), RestfulViewerInstaller.EVERYTHING);
        webAppSpec.addServletSpecification(HttpServletDispatcher.class, RestfulViewerInstaller.ROOT);

        webAppSpec.addFilterSpecification(StaticContentFilter.class, RestfulViewerInstaller.STATIC_CONTENT);
        webAppSpec.addServletSpecification(ResourceServlet.class, RestfulViewerInstaller.STATIC_CONTENT);

        return webAppSpec;
    }
}