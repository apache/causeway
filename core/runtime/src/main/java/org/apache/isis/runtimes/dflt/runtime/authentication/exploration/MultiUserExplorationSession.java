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

package org.apache.isis.runtimes.dflt.runtime.authentication.exploration;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.core.commons.authentication.AuthenticationSessionAbstract;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;

public final class MultiUserExplorationSession extends AuthenticationSessionAbstract implements Encodable {

    private static final long serialVersionUID = 1L;

    private final Set<SimpleSession> sessions = new LinkedHashSet<SimpleSession>();;
    private SimpleSession selectedSession;

    // ////////////////////////////////////////////////////
    // Constructors
    // ////////////////////////////////////////////////////

    public MultiUserExplorationSession(final Set<SimpleSession> sessions, final String code) {
        super("unused", code);
        this.sessions.addAll(sessions);
        initialized();
    }

    public MultiUserExplorationSession(final DataInputExtended input) throws IOException {
        super(input);
        sessions.addAll(Arrays.asList(input.readEncodables(SimpleSession.class)));
        selectedSession = input.readEncodable(SimpleSession.class);
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeEncodables(sessions.toArray());
        output.writeEncodable(selectedSession);
    }

    private void initialized() {
        if (selectedSession == null && sessions.size() > 0) {
            selectedSession = sessions.iterator().next();
        }
    }

    // ////////////////////////////////////////////////////
    // Overriding API
    // ////////////////////////////////////////////////////

    @Override
    public String getUserName() {
        return selectedSession.getUserName();
    }

    @Override
    public boolean hasUserNameOf(final String userName) {
        for (final SimpleSession session : sessions) {
            if (session.hasUserNameOf(userName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getRoles() {
        return selectedSession.getRoles();
    }

    // ////////////////////////////////////////////////////
    // not API
    // ////////////////////////////////////////////////////

    public void setCurrentSession(final String name) {
        for (final SimpleSession user : this.sessions) {
            if (user.getUserName().equals(name)) {
                selectedSession = user;
                break;
            }
        }
    }

    public Set<String> getUserNames() {
        final Set<String> users = new LinkedHashSet<String>();
        for (final SimpleSession user : sessions) {
            users.add(user.getUserName());
        }
        return users;
    }

    // ////////////////////////////////////////////////////
    // toString
    // ////////////////////////////////////////////////////

    @Override
    public String toString() {
        return new ToString(this).append("name", getUserNames()).append("userCount", sessions.size()).toString();
    }

}
