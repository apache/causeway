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


package org.apache.isis.alternatives.remoting.transport.http.server;

import java.util.List;

import org.apache.isis.runtimes.dflt.runtime.Isis;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewer;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewerInstallerAbstract;
import org.apache.isis.runtimes.dflt.runtime.web.EmbeddedWebViewer;
import org.apache.isis.runtimes.dflt.runtime.web.WebAppSpecification;

/**
 * Convenience implementation of a {@link IsisViewer} providing the
 * ability to run a Jetty web server configured for http remoting from the
 * {@link Isis command line}.
 * 
 * <p>
 * To run, use the <tt>--viewer encoding-http</tt> flag. The client-side should
 * run using <tt>--connector encoding-http</tt> flag.
 * 
 * <p>
 * In a production deployment the configuration represented by the
 * {@link WebAppSpecification} would be specified in the <tt>web.xml<tt> file.
 */
public class EncodingOverHttpRemotingViewerInstaller extends
		IsisViewerInstallerAbstract {

	private static final String REMOTING_SERVLET_MAPPED = "/remoting.svc";
	
	public EncodingOverHttpRemotingViewerInstaller() {
		super("encoding-http");
	}

	@Override
	protected void addConfigurationResources(List<String> configurationResources) {
		super.addConfigurationResources(configurationResources);
		// TODO: this (small) hack is because we don't load up the Protocol (Marshaller)
		// and Transport using the installers.
		configurationResources.add("protocol.properties");
		configurationResources.add("protocol_encoding.properties");
		configurationResources.add("transport.properties");
		configurationResources.add("transport_http.properties");
	}


	@Override
	public IsisViewer doCreateViewer() {
		return new EmbeddedWebViewer() {
			public WebAppSpecification getWebAppSpecification() {

				WebAppSpecification webAppSpec = new WebAppSpecification();
				webAppSpec.addServletSpecification(
						EncodingOverHttpRemotingServlet.class,
						REMOTING_SERVLET_MAPPED);

				return webAppSpec;
			}
		};
	}

}


