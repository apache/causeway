package org.apache.isis.viewer.restful.viewer.resources.services;

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.Path;

import nu.xom.Element;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restful.applib.resources.ServicesResource;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.html.XhtmlTemplate;
import org.apache.isis.viewer.restful.viewer.resources.ResourceAbstract;


/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than
 * the interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/services")
public class ServicesResourceImpl extends ResourceAbstract implements ServicesResource {

    public String services() {
        init();
        final XhtmlTemplate xhtml = new XhtmlTemplate("Services", getServletRequest());
        
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());

        final Element div = xhtmlRenderer.div_p("Services", HtmlClass.SECTION);

        final Element ul = xhtmlRenderer.ul(HtmlClass.SERVICES);
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (final ObjectAdapter serviceAdapter : serviceAdapters) {
            final String uri = MessageFormat.format("{0}/object/{1}", getServletRequest().getContextPath(), getOidStr(serviceAdapter));
            ul.appendChild(xhtmlRenderer.li_a(uri, serviceAdapter.titleString(), "service", "services", HtmlClass.SERVICE));
        }
        div.appendChild(ul);

        xhtml.appendToBody(div);
        
		return xhtml.toXML();
    }

}
