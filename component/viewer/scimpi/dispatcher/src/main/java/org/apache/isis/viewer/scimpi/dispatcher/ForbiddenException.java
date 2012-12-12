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

package org.apache.isis.viewer.scimpi.dispatcher;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * Indicates that request could not complete as it could not access (for
 * security reasons) some of the content.
 */
public class ForbiddenException extends ScimpiException {
    private static final long serialVersionUID = 1L;
    public static final boolean VISIBLE_AND_USABLE = true;
    public static final boolean VISIBLE = false;
    private final Identifier identifier;
    private final AuthenticationSession session;

    public ForbiddenException(final String message) {
        this(IsisContext.getAuthenticationSession(), message);
    }

    private ForbiddenException(final AuthenticationSession session, final String message) {
        super(message + " for " + session.getUserName() + " " + session.getRoles());
        this.session = session;
        identifier = null;
    }

    public ForbiddenException(final IdentifiedHolder target, final boolean isVisibleAndUsabable) {
        this(target.getIdentifier(), IsisContext.getAuthenticationSession(), isVisibleAndUsabable);
    }

    private ForbiddenException(final Identifier identifier, final AuthenticationSession session, final boolean isVisibleAndUsabable) {
        super((identifier.getType() == Identifier.Type.PROPERTY_OR_COLLECTION ? "Field" : "Action") + " '" + identifier.getMemberName() + "' in " + identifier.getClassName() + " is not " + (isVisibleAndUsabable ? "visible/usable " : "visible") + " for " + session.getUserName() + " "
                + session.getRoles());
        this.identifier = identifier;
        this.session = session;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<String> getRoles() {
        return session.getRoles();
    }
}
