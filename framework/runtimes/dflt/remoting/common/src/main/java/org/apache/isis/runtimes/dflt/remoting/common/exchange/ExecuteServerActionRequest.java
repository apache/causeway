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
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.runtimes.dflt.remoting.common.data.Data;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.IdentityData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ReferenceData;
import org.apache.isis.runtimes.dflt.remoting.common.facade.ServerFacade;

public class ExecuteServerActionRequest extends RequestAbstract {
    private static final long serialVersionUID = 1L;
    private final ActionType actionType;
    private final String actionIdentifier;
    private final ReferenceData target;
    private final Data[] parameters;

    public ExecuteServerActionRequest(final AuthenticationSession session, final ActionType actionType, final String actionIdentifier, final ReferenceData target, final Data[] parameters) {
        super(session);
        this.actionType = actionType;
        this.actionIdentifier = actionIdentifier;
        this.target = target;
        this.parameters = parameters;
        initialized();
    }

    public ExecuteServerActionRequest(final DataInputExtended input) throws IOException {
        super(input);
        this.actionType = ActionType.valueOf(input.readUTF());
        this.actionIdentifier = input.readUTF();
        this.target = input.readEncodable(IdentityData.class);
        this.parameters = input.readEncodables(Data.class);
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeUTF(actionType.name());
        output.writeUTF(actionIdentifier);
        output.writeEncodable(target);
        output.writeEncodables(parameters);
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    // request data
    // ///////////////////////////////////////////////////////

    public ActionType getActionType() {
        return actionType;
    }

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public ReferenceData getTarget() {
        return target;
    }

    public Data[] getParameters() {
        return parameters;
    }

    // ///////////////////////////////////////////////////////
    // execute, response
    // ///////////////////////////////////////////////////////

    /**
     * {@link #setResponse(Object) Sets a response} of a
     * {@link ExecuteServerActionResponse}.
     */
    @Override
    public void execute(final ServerFacade serverFacade) {
        final ExecuteServerActionResponse response = serverFacade.executeServerAction(this);
        setResponse(response);
    }

    /**
     * Downcasts.
     */
    @Override
    public ExecuteServerActionResponse getResponse() {
        return (ExecuteServerActionResponse) super.getResponse();
    }

    // ///////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("method", actionIdentifier);
        str.append("target", target);
        return str.toString();
    }
}
