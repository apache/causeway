package org.apache.isis.extensions.restful.viewer;


import org.apache.isis.extensions.restful.applib.providers.StringApplicationXhtmlXmlProvider;
import org.apache.isis.extensions.restful.viewer.resources.HomePageResourceImpl;
import org.apache.isis.extensions.restful.viewer.resources.objects.ObjectResourceImpl;
import org.apache.isis.extensions.restful.viewer.resources.services.ServicesResourceImpl;
import org.apache.isis.extensions.restful.viewer.resources.specs.SpecsResourceImpl;
import org.apache.isis.extensions.restful.viewer.resources.user.UserResourceImpl;

public class RestfulApplication extends AbstractJaxRsApplication {

	public RestfulApplication() {
    	addSingleton(new HomePageResourceImpl());
    	addSingleton(new ObjectResourceImpl());
    	addSingleton(new ServicesResourceImpl());
    	addSingleton(new SpecsResourceImpl());
    	addSingleton(new UserResourceImpl());
    	
    	addClass(StringApplicationXhtmlXmlProvider.class);
    }

}
