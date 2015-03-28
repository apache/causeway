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

package org.apache.isis.viewer.scimpi.dispatcher.util;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.viewer.scimpi.dispatcher.DispatchException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class MethodsUtils {
    public static final String SERVICE_PREFIX = "service:";

    public static Consent canRunMethod(final ObjectAdapter target, final ObjectAction action, final ObjectAdapter[] parameters) {
        final Consent consent = action.isProposedArgumentSetValid(target, parameters == null ? new ObjectAdapter[0] : parameters);
        return consent;
    }

    public static boolean runMethod(final RequestContext context, final ObjectAction action, final ObjectAdapter target, final ObjectAdapter[] parameters, String variable, Scope scope) {
        scope = scope == null ? Scope.REQUEST : scope;
        variable = variable == null ? RequestContext.RESULT : variable;

        final ObjectAdapter result = action.execute(target, parameters == null ? new ObjectAdapter[0] : parameters);
        if (result == null) {
            return false;
        } else {
            final String mappedId = context.mapObject(result, scope);
            context.addVariable(variable, mappedId, scope);
            // context.addVariable(variable + "_type",
            // action.getFacet(TypeOfFacet.class), scope);
            return true;
        }
    }

    public static boolean runMethod(final RequestContext context, final ObjectAction action, final ObjectAdapter target, final ObjectAdapter[] parameters) {
        return runMethod(context, action, target, parameters, null, null);
    }

    public static ObjectAction findAction(final ObjectAdapter object, final String methodName) {
        if (object == null) {
            throw new ScimpiException("Object not specified when looking for " + methodName);
        }

        final List<ObjectAction> actions = object.getSpecification().getObjectActions(Contributed.INCLUDED);
        final ObjectAction action = findAction(actions, methodName);
        /*
         * if (action == null) { actions =
         * object.getSpecification().getServiceActionsFor(ObjectActionType.USER,
         * ObjectActionType.EXPLORATION, ObjectActionType.DEBUG); action =
         * findAction(actions, methodName); }
         */
        if (action == null) {
            throw new DispatchException("Failed to find action " + methodName + " on " + object);
        }
        return action;
    }

    private static ObjectAction findAction(final List<ObjectAction> actions, final String methodName) {
        for (int i = 0; i < actions.size(); i++) {
            final ObjectAction objectAction = actions.get(i);
            if (objectAction.getId().equals(methodName)) {
                return objectAction;
            }
        }
        return null;
    }

    public static ObjectAdapter findObject(final RequestContext context, String objectId) {
        if (objectId == null) {
            objectId = context.getStringVariable(RequestContext.RESULT);
        }

        if (objectId != null && objectId.startsWith(SERVICE_PREFIX)) {
            final String serviceId = objectId.substring(SERVICE_PREFIX.length());
            final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
            for (final ObjectAdapter serviceAdapter : serviceAdapters) {
                final Object service = serviceAdapter.getObject();
                if (ServiceUtil.id(service).equals(serviceId.trim())) {
                    final ObjectAdapter adapter = getAdapterManager().getAdapterFor(service);
                    return adapter;
                }
            }
            throw new DispatchException("Failed to find service " + serviceId);
        } else {
            return context.getMappedObject(objectId);
        }
    }

    public static boolean isVisible(final ObjectAdapter object, final ObjectAction action, Where where) {
        return action.isVisible(getAuthenticationSession(), object, where).isAllowed();
    }

    public static String isUsable(final ObjectAdapter object, final ObjectAction action, Where where) {
        final Consent usable = action.isUsable(getAuthenticationSession(), object, where);
        final boolean isUsable = getSession() != null && usable.isAllowed();
        return isUsable ? null : usable.getReason();
    }

    public static boolean isVisibleAndUsable(final ObjectAdapter object, final ObjectAction action, Where where) {
        AuthenticationSession authenticatedSession = getAuthenticationSession();
        final boolean isVisible = action.isVisible(authenticatedSession, object, where).isAllowed();
        final boolean isUsable = getSession() != null && action.isUsable(authenticatedSession, object, where).isAllowed();
        final boolean isVisibleAndUsable = isVisible && isUsable;
        return isVisibleAndUsable;
    }

    private static AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    private static IsisSession getSession() {
        return IsisContext.getSession();
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
