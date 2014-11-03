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

package org.apache.isis.viewer.scimpi.dispatcher.edit;

import java.io.IOException;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AnonymousSession;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

/**
 * Remove an element from a collection.
 */
public class RemoveAction implements Action {
    public static final String ACTION = "remove";

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORMS;

    @Override
    public String getName() {
        return ACTION;
    }

    @Override
    public void process(final RequestContext context) throws IOException {
        AuthenticationSession session = context.getSession();
        if (session == null) {
            session = new AnonymousSession();
        }

        final String parentId = context.getParameter(OBJECT);
        final String rowId = context.getParameter(ELEMENT);

        try {
            final ObjectAdapter parent = context.getMappedObject(parentId);
            final ObjectAdapter row = context.getMappedObject(rowId);

            final String fieldName = context.getParameter(FIELD);
            final ObjectAssociation field = parent.getSpecification().getAssociation(fieldName);
            if (field == null) {
                throw new ScimpiException("No field " + fieldName + " in " + parent.getSpecification().getFullIdentifier());
            }
            if (field.isVisible(IsisContext.getAuthenticationSession(), parent, where).isVetoed()) {
                throw new ForbiddenException(field, ForbiddenException.VISIBLE);
            }

            ((OneToManyAssociation) field).removeElement(parent, row);

            // TODO duplicated in EditAction
            String view = context.getParameter(VIEW);
            final String override = context.getParameter(RESULT_OVERRIDE);

            String resultName = context.getParameter(RESULT_NAME);
            resultName = resultName == null ? RequestContext.RESULT : resultName;

            final String id = context.mapObject(parent, Scope.REQUEST);
            context.addVariable(resultName, id, Scope.REQUEST);
            if (override != null) {
                context.addVariable(resultName, override, Scope.REQUEST);
            }

            final int questionMark = view == null ? -1 : view.indexOf("?");
            if (questionMark > -1) {
                final String params = view.substring(questionMark + 1);
                final int equals = params.indexOf("=");
                context.addVariable(params.substring(0, equals), params.substring(equals + 1), Scope.REQUEST);
                view = view.substring(0, questionMark);
            }
            context.setRequestPath(view);
            // TODO end of duplication

        } catch (final RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            throw e;
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }
}
