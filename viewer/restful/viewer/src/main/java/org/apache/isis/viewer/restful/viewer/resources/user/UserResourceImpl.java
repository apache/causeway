package org.apache.isis.viewer.restful.viewer.resources.user;

import java.util.List;

import javax.ws.rs.Path;

import nu.xom.Element;

import org.apache.isis.viewer.restful.applib.resources.UserResource;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.html.XhtmlTemplate;
import org.apache.isis.viewer.restful.viewer.resources.ResourceAbstract;


/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than
 * the interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/user")
public class UserResourceImpl extends ResourceAbstract implements UserResource {

    public String user() {
        init();

        final XhtmlTemplate xhtml = new XhtmlTemplate("User", getServletRequest());
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());
        
        final Element div = xhtmlRenderer.div_p("Roles", HtmlClass.SECTION);

        final Element ul = xhtmlRenderer.ul(HtmlClass.ROLES);
        final List<String> roles = getSession().getRoles();
        for(final String role: roles) {
            ul.appendChild(xhtmlRenderer.li_p(role, HtmlClass.ROLE));
        }
        div.appendChild(ul);
        
        xhtml.appendToBody(div);
        
        return xhtml.toXML();
    }
}

// Copyright (c) Naked Objects Group Ltd.
