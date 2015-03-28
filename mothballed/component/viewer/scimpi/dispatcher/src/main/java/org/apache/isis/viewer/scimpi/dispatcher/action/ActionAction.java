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

package org.apache.isis.viewer.scimpi.dispatcher.action;

import java.io.IOException;
import java.util.List;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AnonymousSession;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;

public class ActionAction implements Action {

    public static final String ACTION = "action";

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    private final Where where = Where.ANYWHERE;

    @Override
    public String getName() {
        return ACTION;
    }

    /**
     * REVIEW - this and EditAction are very similar - refactor out common code.
     */
    @Override
    public void process(final RequestContext context) throws IOException {
        final String objectId = context.getParameter("_" + OBJECT);
        final String version = context.getParameter("_" + VERSION);
        final String formId = context.getParameter("_" + FORM_ID);
        final String methodName = context.getParameter("_" + METHOD);
        final String override = context.getParameter("_" + RESULT_OVERRIDE);
        String resultName = context.getParameter("_" + RESULT_NAME);
        final String message = context.getParameter("_" + MESSAGE);
        resultName = resultName == null ? RequestContext.RESULT : resultName;

        FormState entryState = null;
        try {
            final ObjectAdapter object = MethodsUtils.findObject(context, objectId);
            // FIXME need to find method based on the set of parameters.
            // otherwise overloaded method may be incorrectly
            // selected.
            final ObjectAction action = MethodsUtils.findAction(object, methodName);
            entryState = validateParameters(context, action, object);

            AuthenticationSession session = context.getSession();
            if (session == null && action.isVisible(new AnonymousSession(), object, where).isVetoed()) {
                session = new AnonymousSession();
            }

            final Version originalVersion = context.getVersion(version);
            object.checkLock(originalVersion);
            if (entryState.isValid()) {
                final boolean hasResult = invokeMethod(context, resultName, object, action, entryState);
                String view = context.getParameter(hasResult ? "_" + VIEW : "_" + VOID);

                final int questionMark = view == null ? -1 : view.indexOf("?");
                if (questionMark > -1) {
                    final String params[] = view.substring(questionMark + 1).split("&");
                    for (final String param : params) {
                        final int equals = param.indexOf("=");
                        context.addVariable(param.substring(0, equals), param.substring(equals + 1), Scope.REQUEST);
                        view = view.substring(0, questionMark);
                    }
                }
                context.setRequestPath(view);
                if (message != null) {
                    final MessageBroker messageBroker = getMessageBroker();
                    messageBroker.addMessage(message);
                }
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }
                if (!action.hasReturn() && context.getVariable(resultName) == null) {
                    context.addVariable(resultName, objectId, Scope.REQUEST);
                }
            } else {
                entryState.setForm(formId);
                context.addVariable(ENTRY_FIELDS, entryState, Scope.REQUEST);
                context.addVariable(resultName, objectId, Scope.REQUEST);
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }
                final String error = entryState.getError();
                final String view = context.getParameter("_" + ERROR);
                context.setRequestPath(view, Dispatcher.ACTION);

                final MessageBroker messageBroker = getMessageBroker();
                messageBroker.addWarning(error);
            }
        } catch (final ConcurrencyException e) {
            final ObjectAdapter adapter = getAdapterManager().getAdapterFor(e.getOid()); 
            String user = adapter.getOid().getVersion().getUser();
            String errorMessage = "The data for '" + adapter.titleString() + "' was changed by " + user
                    + ". Please repeat the action based on those changes.";
            getMessageBroker().addMessage(errorMessage);

            entryState.setForm(formId);
            context.addVariable(ENTRY_FIELDS, entryState, Scope.REQUEST);
            context.addVariable(resultName, objectId, Scope.REQUEST);
            if (override != null) {
                context.addVariable(resultName, override, Scope.REQUEST);
            }
            final String error = entryState.getError();
            if (error != null) {
                context.addVariable(RequestContext.ERROR, error, Scope.REQUEST);
            }

            final String view = context.getParameter("_" + ERROR);
            context.setRequestPath(view, Dispatcher.ACTION);

        } catch (final RuntimeException e) {
            getMessageBroker().getMessages();
            getMessageBroker().getWarnings();
            throw e;
        }
    }

    private boolean invokeMethod(final RequestContext context, final String variable, final ObjectAdapter object, final ObjectAction action, final FormState entryState) {

        final ObjectAdapter[] parameters = getParameters(action, entryState);
        final String scopeName = context.getParameter("_" + SCOPE);
        final Scope scope = RequestContext.scope(scopeName, Scope.REQUEST);
        return MethodsUtils.runMethod(context, action, object, parameters, variable, scope);
    }

    private ObjectAdapter[] getParameters(final ObjectAction action, final FormState entryState) {
        final int parameterCount = action.getParameterCount();
        final ObjectAdapter[] parameters = new ObjectAdapter[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            parameters[i] = entryState.getField(parameterName(i)).getValue();
        }
        return parameters;
    }

    private FormState validateParameters(final RequestContext context, final ObjectAction action, final ObjectAdapter object) {
        final FormState formState = new FormState();
        final List<ObjectActionParameter> parameters2 = action.getParameters();
        final int parameterCount = action.getParameterCount();
        for (int i = 0; i < parameterCount; i++) {
            final String fieldName = parameterName(i);
            String newEntry = context.getParameter(fieldName);

            if (newEntry != null && newEntry.equals("-OTHER-")) {
                newEntry = context.getParameter(fieldName + "-other");
            }

            if (newEntry == null) {
                // TODO figure out a better way to determine if boolean or a
                // password
                final ObjectSpecification spec = parameters2.get(i).getSpecification();
                if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class)) || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class))) {
                    newEntry = FALSE;
                } else {
                    newEntry = "";
                }
            }
            final FieldEditState fieldState = formState.createField(fieldName, newEntry);
            Consent consent = null;

            if (!parameters2.get(i).isOptional() && newEntry.equals("")) {
                consent = new Veto(parameters2.get(i).getName() + " required");
                formState.setError("Not all fields have been set");

            } else if (parameters2.get(i).getSpecification().getFacet(ParseableFacet.class) != null) {
                try {
                    final ParseableFacet facet = parameters2.get(i).getSpecification().getFacet(ParseableFacet.class);
                    Localization localization = IsisContext.getLocalization(); 
                    final String message = parameters2.get(i).isValid(object, newEntry, localization); 
                    if (message != null) {
                        consent = new Veto(message);
                        formState.setError("Not all fields are valid");
                    }
                    final ObjectAdapter entry = facet.parseTextEntry(null, newEntry, localization);
                    fieldState.setValue(entry);
                } catch (final TextEntryParseException e) {
                    consent = new Veto(e.getMessage());
                    formState.setError("Not all fields are valid");
                }
            } else {
                fieldState.setValue(newEntry == null ? null : context.getMappedObject(newEntry));
            }
            if (consent != null && consent.isVetoed()) {
                fieldState.setError(consent.getReason());
            }
        }

        if (formState.isValid()) {
            final ObjectAdapter[] parameters = getParameters(action, formState);
            final Consent consent = action.isProposedArgumentSetValid(object, parameters);
            if (consent != null && consent.isVetoed()) {
                formState.setError(consent.getReason());
            }
        }

        return formState;
    }

    public static String parameterName(final int index) {
        return PARAMETER + (index + 1);
    }

    @Override
    public void init() {
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }
    

    ///////////////////////////////////////////////////////////////////////////
    // from context
    ///////////////////////////////////////////////////////////////////////////
    
    protected MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }

    protected AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }


}
