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

package org.apache.isis.runtimes.dflt.remoting.common.exchange;

import java.io.IOException;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;

public class OidForServiceRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final String serviceId;

    /**
     * provided for serialization only!
     */
    public OidForServiceRequest() {
        this(null, null);
    }

    public OidForServiceRequest(final AuthenticationSession session, final String id) {
        super(session);
        this.serviceId = id;
        initialized();
    }

    public OidForServiceRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.serviceId = input.readUTF();
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeUTF(serviceId);
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    // request data
    // ///////////////////////////////////////////////////////

    public String getServiceId() {
        return serviceId;
    }

    // ///////////////////////////////////////////////////////
    // execute, response
    // ///////////////////////////////////////////////////////

    /**
     * {@link #setResponse(Object) Sets a response} of a simple {@link Boolean}.
     */
    @Override
    public void execute(final ServerFacade serverFacade) {
        setResponse(serverFacade.oidForService(this));
    }

    /**
     * Downcasts.
     */
    @Override
    public OidForServiceResponse getResponse() {
        return (OidForServiceResponse) super.getResponse();
    }

    // ///////////////////////////////////////////////////////
    // tostring
    // ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("sequence", getId());
        str.append("id", serviceId);
        return str.toString();
    }

}
