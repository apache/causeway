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

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.webapp.IsisWebAppBootstrapper;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;
import lombok.val;

@Configuration
@Import({
	HelloWorldAppConfiguration.ConfigProps.class
})
@PropertySource("classpath:/domainapp/application/isis-non-changing.properties")
public class HelloWorldAppConfiguration  {

    @ConfigurationProperties(prefix = "", ignoreUnknownFields = true)
    @Data
    public static class ConfigProps {
        private Map<String, String> isis = Collections.emptyMap();
        private Map<String, String> resteasy = Collections.emptyMap();
        private Map<String, String> datanucleus = Collections.emptyMap();
        private Map<String, String> eclipselink = Collections.emptyMap();
    }

	@Bean
	public FilterRegistrationBean<WicketFilter> wicketFilterRegistration(ConfigProps configProps) {
	    val registration = new FilterRegistrationBean<WicketFilter>();
	    registration.setFilter(wicketFilter());
	    registration.setName("wicketFilter");
	    registration.setOrder(1);
	    setupWicket(registration, configProps);
	    return registration;
	}
	
	public WicketFilter wicketFilter() {
	    return new WicketFilter();
	}
	
	private DeploymentCategory deploymentCategory() {
		return DeploymentCategory.PROTOTYPING;	
	}
	
	private AppManifest appManifest() {
		return new HelloWorldAppManifest();
	}
	
	private IsisConfigurationDefault appConfiguration(ServletContext servletContext, ConfigProps configProps) {
		val isisConfigurationBuilder = 
				IsisWebAppBootstrapper.obtainConfigBuilderFrom(servletContext);
        isisConfigurationBuilder.addDefaultConfigurationResourcesAndPrimers();
        configProps.getIsis().forEach(isisConfigurationBuilder::add);
        configProps.getResteasy().forEach(isisConfigurationBuilder::add);
        configProps.getDatanucleus().forEach(isisConfigurationBuilder::add);
        val configuration = isisConfigurationBuilder.getConfiguration();
        System.err.println("conf: " + configuration);
		return configuration;
	}
	
	private void setupWicket(FilterRegistrationBean<WicketFilter> filterReg, ConfigProps configProps) {
		
		String deploymentMode = deploymentCategory().isPrototyping() ? "development" : "deployment";
	    String wicketApp = IsisWicketApplication.class.getName();
	    String urlPattern = "/wicket/*";
	
        filterReg.addInitParameter("applicationClassName", wicketApp);
        filterReg.addInitParameter("filterMappingUrlPattern", urlPattern);
        filterReg.addInitParameter("configuration", deploymentMode);
        filterReg.addUrlPatterns(urlPattern);
        
        IsisWicketApplication.appManifestProvider = this::appManifest;
        IsisWicketApplication.appConfigurationProvider = ctx -> appConfiguration(ctx, configProps);
    }

}

