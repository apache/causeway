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

package org.apache.isis.viewer.scimpi.dispatcher.logon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.UserManager;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;


public class LogonAction implements Action {

    @Override
    public void process(final RequestContext context) throws IOException {
        String username = context.getParameter("username");
        String password = context.getParameter("password");
        final String actualFormId = context.getParameter("_" + FORM_ID);
        final String expectedFormId = context.getParameter(LOGON_FORM_ID);
        boolean isDomainLogon = expectedFormId != null && expectedFormId.equals(actualFormId);
        boolean isValid;

        if (username == null || password == null) {
            context.redirectTo("/");
            return;
        }
        AuthenticationSession session = null;
        if (username.length() == 0 || password.length() == 0) {
            isValid = false;
        } else {
            if (isDomainLogon) {
                final String objectId = context.getParameter(LOGON_OBJECT);
                final String scope = context.getParameter(LOGON_SCOPE);
                final String loginMethodName = context.getParameter(LOGON_METHOD);
                final String roleFieldName = context.getParameter(PREFIX + "roles-field");
                String resultName = context.getParameter(LOGON_RESULT_NAME);
                resultName = resultName == null ? "_" + USER : resultName;

                final ObjectAdapter object = MethodsUtils.findObject(context, objectId);
                final ObjectAction loginAction = MethodsUtils.findAction(object, loginMethodName);
                final int parameterCount = loginAction.getParameterCount();
                final ObjectAdapter[] parameters = new ObjectAdapter[parameterCount];
                List<ObjectActionParameter> parameters2 = loginAction.getParameters();
                if (parameters.length != 2) {
                    throw new ScimpiException("Expected two parameters for the log-on method: " + loginMethodName);
                }

                Localization localization = IsisContext.getLocalization(); 
                ParseableFacet facet = parameters2.get(0).getSpecification().getFacet(ParseableFacet.class);
                parameters[0] = facet.parseTextEntry(null, username, localization);
                facet = parameters2.get(1).getSpecification().getFacet(ParseableFacet.class);
                parameters[1] = facet.parseTextEntry(null, password, localization);
                final ObjectAdapter result = loginAction.execute(object, parameters);
                isValid = result != null;
                if (isValid) {
                    ObjectSpecification specification = result.getSpecification();
                    ObjectAssociation association = specification.getAssociation(roleFieldName);
                    if (association == null) {
                        throw new ScimpiException("Expected a role name field called: " + roleFieldName);
                    }
                    ObjectAdapter role = association.get(result);
                    List<String> roles = new ArrayList<String>();
                    if (role != null) {
                        String[] split = role.titleString().split("\\|");
                        for (String r : split) {
                            roles.add(r);
                        }
                    }
                    //String domainRoleName = role == null ? "" : role.titleString(); 
                    
                    
                    Scope scope2 = scope == null ? Scope.SESSION : RequestContext.scope(scope);
                    final String resultId = context.mapObject(result, scope2);
                    context.addVariable(resultName, resultId, scope);
                    context.addVariable("_username", username, Scope.SESSION);
                    
                    context.clearVariable(LOGON_OBJECT, Scope.SESSION);
                    context.clearVariable(LOGON_METHOD, Scope.SESSION);
                    context.clearVariable(LOGON_RESULT_NAME, Scope.SESSION);
                    context.clearVariable(LOGON_SCOPE, Scope.SESSION);
                    context.clearVariable(PREFIX + "roles-field", Scope.SESSION);
 //                   context.clearVariable(PREFIX + "isis-user", Scope.SESSION);
                    context.clearVariable(LOGON_FORM_ID, Scope.SESSION);

                    session = new DomainSession(result.titleString(), roles);
                } else {
                    session = context.getSession();
                }
                
            } else {
                session = UserManager.authenticate(new AuthenticationRequestPassword(username, password));
                isValid = session != null;
            }
        }

        String view;
        if (!isValid) {
            final FormState formState = new FormState();
            formState.setForm(actualFormId);
            formState.setError("Failed to login. Check the username and ensure that your password was entered correctly");
            FieldEditState fieldState = formState.createField("username", username);
            if (username.length() == 0) {
                fieldState.setError("User Name required");
            }
            fieldState = formState.createField("password", password);
            if (password.length() == 0) {
                fieldState.setError("Password required");
            }
            if (username.length() == 0 || password.length() == 0) {
                formState.setError("Both the user name and password must be entered");
            }
            context.addVariable(ENTRY_FIELDS, formState, Scope.REQUEST);

            view = context.getParameter(ERROR);
            context.setRequestPath("/" + view, Dispatcher.ACTION);
        } else {
            context.setSession(session);
            context.startHttpSession();
            context.setUserAuthenticated(true);
            view = context.getParameter(VIEW);
            if (view == null) {
                // REVIEW this is duplicated in Logon.java
                view = "start." + Dispatcher.EXTENSION;
            }
            context.redirectTo(view);
        }
    }

    @Override
    public String getName() {
        return "logon";
    }

    @Override
    public void init() {}

    @Override
    public void debug(final DebugBuilder debug) {}
}
