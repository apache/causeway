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


package org.apache.isis.extensions.remoting.http;

import org.apache.isis.metamodel.config.ConfigurationConstants;

public class HttpRemotingConstants {
	
	private static final String ROOT = ConfigurationConstants.ROOT + "transport.http.";
	
	/**
	 * Key used to lookup URL to which the <tt>EncodingOverHttpRemotingServlet</tt> is mapped.
	 */
	public static final String URL_KEY = ROOT + "url";
	/**
	 * Default value for {@link #URL_KEY}.
	 */
	public static final String URL_DEFAULT = "http://localhost:8080/remoting.svc";

	private HttpRemotingConstants(){}

}
