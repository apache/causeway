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


package org.apache.isis.viewer.html.viewer;

import org.apache.isis.core.commons.lang.MapUtils;
import org.apache.isis.core.runtime.Isis;
import org.apache.isis.core.runtime.viewer.IsisViewer;
import org.apache.isis.core.runtime.viewer.IsisViewerInstallerAbstract;
import org.apache.isis.core.runtime.web.EmbeddedWebViewer;
import org.apache.isis.core.runtime.web.WebAppSpecification;
import org.apache.isis.core.webapp.IsisSessionFilter;
import org.apache.isis.core.webapp.StaticContentFilter;
import org.apache.isis.core.webapp.servlets.ResourceServlet;
import org.apache.isis.viewer.html.servlet.ControllerServlet;
import org.apache.isis.viewer.html.servlet.HtmlServletConstants;
import org.apache.isis.viewer.html.servlet.LogonServlet;


/**
 * Convenience implementation of a {@link IsisViewer} providing the
 * ability to run a Jetty web server configured for the HTML viewer from the
 * {@link Isis command line}.
 * 
 * <p>
 * To run, use the <tt>--viewer html</tt> flag.
 * 
 * <p>
 * In a production deployment the configuration represented by the
 * {@link WebAppSpecification} would be specified in the <tt>web.xml<tt> file.
 */
public class HtmlViewerInstaller extends IsisViewerInstallerAbstract {
	
	private static final String LOGON_PAGE = HtmlServletConstants.LOGON_APP_PAGE;
	private static final String LOGON_PAGE_MAPPED = "/"+LOGON_PAGE;
	
	private static final String[] STATIC_CONTENT = new String[]{"*.gif", "*.png", "*.jpg", "*.css"};
	private static final String DYNAMIC_CONTENT = "*.app";


	public HtmlViewerInstaller() {
		super("html");
	}
	
	
	@Override
    public IsisViewer doCreateViewer() {
        return new EmbeddedWebViewer() {
            public WebAppSpecification getWebAppSpecification() {

                WebAppSpecification webAppSpec = new WebAppSpecification();
                
                webAppSpec.addFilterSpecification(
                		IsisSessionFilter.class, 
                		MapUtils.asMap(IsisSessionFilter.LOGON_PAGE_KEY, LOGON_PAGE_MAPPED), 
                		DYNAMIC_CONTENT);
                webAppSpec.addServletSpecification(LogonServlet.class, LOGON_PAGE_MAPPED);
                webAppSpec.addServletSpecification(ControllerServlet.class, DYNAMIC_CONTENT);
                
                webAppSpec.addFilterSpecification(StaticContentFilter.class, STATIC_CONTENT);
                webAppSpec.addServletSpecification(ResourceServlet.class, STATIC_CONTENT );
                

                final String resourceBaseDir = getConfiguration().getString(HtmlViewerConstants.VIEWER_HTML_RESOURCE_BASE_KEY);
                if (resourceBaseDir != null) {
                    webAppSpec.addResourcePath(resourceBaseDir);
                }
                webAppSpec.addResourcePath("./src/main/resources");
                webAppSpec.addResourcePath("./src/main/webapp");
                webAppSpec.addResourcePath("./web");
                webAppSpec.addResourcePath(".");
                webAppSpec.addWelcomeFile(LOGON_PAGE);
                
				webAppSpec.setLogHint("open a web browser and browse to logon.app to connect");

                return webAppSpec;
            }
        };        
    }

}


