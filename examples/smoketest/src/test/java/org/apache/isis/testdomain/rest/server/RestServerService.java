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
package org.apache.isis.testdomain.rest.server;

import javax.inject.Inject;

import org.apache.isis.applib.client.RestfulClient;
import org.apache.isis.applib.client.RestfulClientConfig;
import org.apache.isis.commons.internal.resources._Resources;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class RestServerService {

	public int getPort() {
		if(port==null) {
			init();
		}
		return port;
	}

	public RestfulClient newClient() {
		
		val restRootPath = 
				"http://localhost:" + getPort() + "/" + 
				_Resources.prependContextPathIfPresent(_Resources.getRestfulPathOrThrow());

		log.info("new restful client created for {}", restRootPath);
		
		RestfulClientConfig clientConfig = new RestfulClientConfig();
		clientConfig.setRestfulBase(restRootPath);
		// setup basic-auth
		clientConfig.setUseBasicAuth(true); // default = false
		clientConfig.setRestfulAuthUser("sven");
		clientConfig.setRestfulAuthPassword("pass");
		// setup request/response debug logging
		clientConfig.setUseRequestDebugLogging(true); // default = false

		RestfulClient client = RestfulClient.ofConfig(clientConfig);

		return client;
	}	

	// -- HELPER

	private Integer port;

	private void init() {
		port = Integer.parseInt(environment.getProperty("local.server.port"));
	}

	// -- DEPENDENCIES

	@Inject Environment environment;

}
