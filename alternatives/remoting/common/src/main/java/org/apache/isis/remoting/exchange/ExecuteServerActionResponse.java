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


package org.apache.isis.remoting.exchange;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.encoding.Encodable;
import org.apache.isis.core.runtime.transaction.messagebroker.MessageList;
import org.apache.isis.core.runtime.transaction.messagebroker.WarningList;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;

import com.google.inject.internal.Lists;

public class ExecuteServerActionResponse implements Encodable, Serializable, MessageList, WarningList {

    private static final long serialVersionUID = 1L;

    private final Data result;
    private final List<String> messages = Lists.newArrayList();
    private final List<String> warnings = Lists.newArrayList();
    private final ObjectData[] updatesData;
    private final ReferenceData[] disposedData;
    private final ObjectData persistedTarget;
    private final ObjectData[] persistedParameters;

    public ExecuteServerActionResponse(
            final Data result,
            final ObjectData[] updatesData,
            final ReferenceData[] disposed,
            final ObjectData persistedTarget,
            final ObjectData[] persistedParameters,
            final String[] messages,
            final String[] warnings) {
        this.result = result;
        this.updatesData = updatesData;
        this.disposedData = disposed;
        this.persistedTarget = persistedTarget;
        this.persistedParameters = persistedParameters;
        this.messages.addAll(Arrays.asList(messages));
        this.warnings.addAll(Arrays.asList(warnings));
        initialized();
    }

    public ExecuteServerActionResponse(final DataInputExtended input) throws IOException {
        this.result = input.readEncodable(Data.class);
        this.updatesData = input.readEncodables(ObjectData.class);
        this.disposedData = input.readEncodables(ReferenceData.class);
        this.persistedTarget = input.readEncodable(ObjectData.class);
        this.persistedParameters = input.readEncodables(ObjectData.class);
        this.messages.addAll(Arrays.asList(input.readUTFs()));
        this.warnings.addAll(Arrays.asList(input.readUTFs()));
        initialized();
    }

    public void encode(final DataOutputExtended output) throws IOException {
        output.writeEncodable(result);
        output.writeUTFs(messages.toArray(new String[0]));
        output.writeUTFs(warnings.toArray(new String[0]));
        output.writeEncodables(updatesData);
        output.writeEncodables(disposedData);
        output.writeEncodable(persistedTarget);
        output.writeEncodables(persistedParameters);
    }

	private void initialized() {
		// nothing to do
	}

    /////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////

    /**
     * Return the Data for the result from executing the action.
     */
    public Data getReturn() {
        return result;
    }

    /**
     * Return the ObjectData for the target if it was persisited by the server.
     */
    public ObjectData getPersistedTarget() {
        return persistedTarget;
    }

    /**
     * Return the ObjectDatas for any of the parameters (in the same seqence as passed to the server) if they
     * were was persisited by the server.
     */
    public ObjectData[] getPersistedParameters() {
        return persistedParameters;
    }

    /**
     * Return the set of ObjectData for any objects that where changed by the server while executing the
     * action.
     */
    public ObjectData[] getUpdates() {
        return updatesData;
    }

    public ReferenceData[] getDisposed() {
        return disposedData;
    }

    /**
     * Return all messages created by the action.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Return all warnings created by the action.
     */
    public List<String> getWarnings() {
        return warnings;
    }

}
