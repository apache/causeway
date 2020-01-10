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

package org.apache.isis.security.api.authentication;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.encoding.DataInputExtended;
import org.apache.isis.commons.internal.encoding.DataOutputExtended;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

public abstract class AuthenticationSessionAbstract implements AuthenticationSession, Serializable {

    private static final long serialVersionUID = 1L;

    // -- Constructor, fields

    private final String name;
    private final Set<String> roles = _Sets.newHashSet();
    private transient Can<String> rolesImmutable;
    private final String validationCode;

    private final Map<String, Object> attributeByName = new HashMap<String, Object>();

    private final MessageBroker messageBroker;

    public AuthenticationSessionAbstract(final String name, final String code) {
        this(name, Stream.of(), code);
    }

    public AuthenticationSessionAbstract(final String name, final Stream<String> roleStream, final String validationCode) {
        this.name = name;

        stream(roleStream)
        .filter(_Strings::isNotEmpty)
        .forEach(this.roles::add);

        this.validationCode = validationCode;
        this.messageBroker = new MessageBroker();
        // nothing to do
    }

    public AuthenticationSessionAbstract(final DataInputExtended input) throws IOException {
        this.name = input.readUTF();
        this.roles.addAll(Arrays.asList(input.readUTFs()));
        
        this.validationCode = input.readUTF();
        this.messageBroker = new MessageBroker();
        // nothing to do
    }

    // -- encode

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(getUserName());
        output.writeUTFs(roles.toArray(new String[] {}));
        output.writeUTF(validationCode);
    }

    // -- User Name

    @Override
    public String getUserName() {
        return name;
    }

    @Override
    public boolean hasUserNameOf(final String userName) {
        return Objects.equals(userName, getUserName());
    }

    // -- Roles

    @Override
    public Can<String> getRoles() {
        if(rolesImmutable==null) { 
            // lazy in support of serialization, 
            // its also (practically) thread-safe without doing any synchronization here 
            rolesImmutable = Can.ofCollection(roles);
        }
        return rolesImmutable;
    }

    @Override
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    // -- Validation Code

    @Override
    public String getValidationCode() {
        return validationCode;
    }

    // -- Attributes

    @Override
    public Object getAttribute(final String attributeName) {
        return attributeByName.get(attributeName);
    }

    @Override
    public void setAttribute(final String attributeName, final Object attribute) {
        attributeByName.put(attributeName, attribute);
    }

    // -- MessageBroker

    @Override
    public MessageBroker getMessageBroker() {
        return messageBroker;
    }

    // -- createUserMemento

    @Override
    public UserMemento createUserMemento() {
        final List<RoleMemento> roles = _Lists.newArrayList();
        for (final String roleName : this.roles) {
            roles.add(new RoleMemento(roleName));
        }
        return new UserMemento(getUserName(), roles);
    }

    // -- toString

    private final static ToString<AuthenticationSessionAbstract> toString = ToString
            .toString("name", AuthenticationSessionAbstract::getUserName)
            .thenToString("code", AuthenticationSessionAbstract::getValidationCode);

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
