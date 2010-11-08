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


package org.apache.isis.webapp.util;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.services.ServiceUtil;
import org.apache.isis.runtime.session.IsisSession;
import org.apache.isis.webapp.DispatchException;
import org.apache.isis.webapp.ScimpiException;
import org.apache.isis.webapp.context.RequestContext;
import org.apache.isis.webapp.context.RequestContext.Scope;


public class MethodsUtils {
    public static final String SERVICE_PREFIX = "service:";


    public static Consent canRunMethod(
            ObjectAdapter target,
            ObjectAction action,
            ObjectAdapter[] parameters) {
        Consent consent = action.isProposedArgumentSetValid(target, parameters == null ? new ObjectAdapter[0] : parameters);
        return consent;
    }

    public static boolean runMethod(
            RequestContext context,
            ObjectAction action,
            ObjectAdapter target,
            ObjectAdapter[] parameters,
            String variable,
            Scope scope) {
        scope = scope == null ? Scope.REQUEST : scope;
        variable = variable == null ? RequestContext.RESULT : variable;

        ObjectAdapter result = action.execute(target, parameters == null ? new ObjectAdapter[0] : parameters);
        if (result == null) {
            return false;
        } else {
            String mappedId = context.mapObject(result, scope);
            context.addVariable(variable, mappedId, scope);
            // context.addVariable(variable + "_type", action.getFacet(TypeOfFacet.class), scope);
            return true;
        }
    }

    public static boolean runMethod(RequestContext context, ObjectAction action, ObjectAdapter target, ObjectAdapter[] parameters) {
        return runMethod(context, action, target, parameters, null, null);
    }

    public static ObjectAction findAction(ObjectAdapter object, String methodName) {
        if (object == null) {
            throw new ScimpiException("Object not specified when looking for " + methodName);
        }

        ObjectAction[] actions = object.getSpecification().getObjectActions(ObjectActionType.USER,
                ObjectActionType.EXPLORATION, ObjectActionType.PROTOTYPE, ObjectActionType.DEBUG);
        ObjectAction action = findAction(actions, methodName);
       /* if (action == null) {
            actions = object.getSpecification().getServiceActionsFor(ObjectActionType.USER,
                    ObjectActionType.EXPLORATION, ObjectActionType.DEBUG);
            action = findAction(actions, methodName);
        }*/
        if (action == null) {
            throw new DispatchException("Failed to find action " + methodName + " on " + object);
        }
        return action;
    }

    private static ObjectAction findAction(final ObjectAction[] actions, final String methodName) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].getActions().length > 0) {
                final ObjectAction action = findAction(actions[i].getActions(), methodName);
                if (action != null) {
                    return action;
                }

            } else {
                if (actions[i].getId().equals(methodName)) {
                    return actions[i];
                }
            }
        }
        return null;
    }

    public static ObjectAdapter findObject(RequestContext context, String objectId) {
        if (objectId == null) {
            objectId = context.getStringVariable(RequestContext.RESULT);
        }

        if (objectId != null && objectId.startsWith(SERVICE_PREFIX)) {
            String serviceId = objectId.substring(SERVICE_PREFIX.length());
            List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
            for (ObjectAdapter serviceAdapter : serviceAdapters) {
                Object service = serviceAdapter.getObject();
                if (ServiceUtil.id(service).equals(serviceId.trim())) {
                    ObjectAdapter adapter = getAdapterManager().getAdapterFor(service);
                    return adapter;
                }
            }
            throw new DispatchException("Failed to find service " + serviceId);
        } else {
            return (ObjectAdapter) context.getMappedObject(objectId);
        }
    }

    public static boolean isVisible(ObjectAdapter object, ObjectAction action) {
        return action.isVisible(getAuthenticationSession(), object).isAllowed();
    }
    
    public static String isUsable(ObjectAdapter object, ObjectAction action) {
        Consent usable = action.isUsable(getAuthenticationSession(), object);
        boolean isUsable = getSession() != null && usable.isAllowed();
        return isUsable ? null : usable.getReason();
    }
    
    public static boolean isVisibleAndUsable(ObjectAdapter object, ObjectAction action) {
        boolean isVisible = action.isVisible(getAuthenticationSession(), object).isAllowed();
        boolean isUsable = getSession() != null && action.isUsable(getAuthenticationSession(), object).isAllowed();
        boolean isVisibleAndUsable = isVisible && isUsable;
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

