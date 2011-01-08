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


package org.apache.isis.alternatives.remoting.transport.sockets.shared;

import org.apache.isis.alternatives.remoting.transport.TransportInstaller;
import org.apache.isis.core.commons.config.ConfigurationConstants;


public final class SocketTransportConstants {
    
	public static final String ROOT = 
		ConfigurationConstants.ROOT + TransportInstaller.TYPE + "." + "sockets.";

	public static final String PORT_KEY = ROOT + "port";
	public static final int PORT_DEFAULT = 9580;

	public static final String HOST_KEY = ROOT + "host";
	public static final String HOST_DEFAULT = "localhost";

	public static final String PROFILING_KEY = ROOT + "profiling";
	public static final boolean PROFILING_DEFAULT = true;

	
    private SocketTransportConstants() {}

}
