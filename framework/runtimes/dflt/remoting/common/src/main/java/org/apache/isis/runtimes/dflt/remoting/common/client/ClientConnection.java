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

package org.apache.isis.runtimes.dflt.remoting.common.client;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.Request;
import org.apache.isis.runtimes.dflt.remoting.common.exchange.ResponseEnvelope;
import org.apache.isis.runtimes.dflt.remoting.common.marshalling.ClientMarshaller;
import org.apache.isis.runtimes.dflt.remoting.server.ServerConnection;

/**
 * Mediates between a running system (which has {@link Request}s that need
 * servicing) and the {@link ClientMarshaller} that pushes the requests onto the
 * network and pulls them back.
 * 
 * @see ServerConnection
 */
public interface ClientConnection extends ApplicationScopedComponent {

    ResponseEnvelope executeRemotely(Request request);

}
