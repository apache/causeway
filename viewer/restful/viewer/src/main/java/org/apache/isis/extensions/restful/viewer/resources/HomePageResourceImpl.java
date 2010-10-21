package org.apache.isis.extensions.restful.viewer.resources;

import javax.ws.rs.Path;

import org.apache.isis.extensions.restful.applib.resources.HomePageResource;
import org.apache.isis.extensions.restful.viewer.html.XhtmlTemplate;


/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than
 * the interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/")
public class HomePageResourceImpl extends ResourceAbstract implements HomePageResource {

    public String resources() {
        init();
        final XhtmlTemplate xhtml = new XhtmlTemplate("Home Page", getServletRequest());
        
        xhtml.appendToBody(asDivNofSession());
        xhtml.appendToBody(resourcesDiv());
        
		return xhtml.toXML();
    }

}

// Copyright (c) Naked Objects Group Ltd.
