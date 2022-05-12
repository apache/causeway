/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package domainapp.application;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.val;

@Configuration
public class HelloWorldAppConfiguration  {

	@Bean
	public FilterRegistrationBean<WicketFilter> wicketFilterRegistration() {
	    val registration = new FilterRegistrationBean<WicketFilter>();
	    registration.setFilter(wicketFilter());
	    registration.setName("wicketFilter");
	    registration.setOrder(1);
	    setupWicket(registration);
	    return registration;
	}
	
	public WicketFilter wicketFilter() {
	    return new WicketFilter();
	}
	
	private AppManifest appManifest() {
		return new HelloWorldAppManifest();
	}
	
	private DeploymentCategory deploymentCategory() {
		return DeploymentCategory.PROTOTYPING;	
	}
	
	private void setupWicket(FilterRegistrationBean<WicketFilter> filterReg) {
		
		String deploymentMode = deploymentCategory().isPrototyping() ? "development" : "deployment";
	    String wicketApp = IsisWicketApplication.class.getName();
	    String urlPattern = "/wicket/*";
	
        filterReg.addInitParameter("applicationClassName", wicketApp);
        filterReg.addInitParameter("filterMappingUrlPattern", urlPattern);
        filterReg.addInitParameter("configuration", deploymentMode);
        filterReg.addUrlPatterns(urlPattern);
        
        IsisWicketApplication.appManifestProvider = this::appManifest;
        
    }

	

}

