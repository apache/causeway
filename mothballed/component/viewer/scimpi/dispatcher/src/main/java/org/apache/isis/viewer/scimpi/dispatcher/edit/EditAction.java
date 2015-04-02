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
import java.util.List;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AnonymousSession;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.NotLoggedInException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class EditAction implements Action {
    public static final String ACTION = "edit";

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

    @Override
    public void process(final RequestContext context) throws IOException {
        AuthenticationSession session = context.getSession();
        if (session == null) {
            session = new AnonymousSession();
        }

        try {
            final String objectId = context.getParameter("_" + OBJECT);
            final String version = context.getParameter("_" + VERSION);
            final String formId = context.getParameter("_" + FORM_ID);
            String resultName = context.getParameter("_" + RESULT_NAME);
            resultName = resultName == null ? RequestContext.RESULT : resultName;
            final String override = context.getParameter("_" + RESULT_OVERRIDE);
            String message = context.getParameter("_" + MESSAGE);

            final ObjectAdapter adapter = context.getMappedObject(objectId);

            final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(session, adapter, where));

            for (final ObjectAssociation objectAssociation : fields) {
                if (objectAssociation.isVisible(session, adapter, where).isVetoed()) {
                    throw new NotLoggedInException();
                }
            }

            final FormState entryState = validateObject(context, adapter, fields);
            final Version adapterVersion = adapter.getVersion();
            final Version formVersion = context.getVersion(version);
            if (formVersion != null && adapterVersion.different(formVersion)) {

                IsisContext.getMessageBroker().addMessage("The " + adapter.getSpecification().getSingularName() + " was edited " + "by another user (" + adapterVersion.getUser() + "). Please  make your changes based on their changes.");

                final String view = context.getParameter("_" + ERROR);
                context.setRequestPath(view, Dispatcher.EDIT);

                entryState.setForm(formId);
                context.addVariable(ENTRY_FIELDS, entryState, Scope.REQUEST);
                context.addVariable(resultName, objectId, Scope.REQUEST);
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }

            } else if (entryState.isValid()) {
                changeObject(context, adapter, entryState, fields);

                if (adapter.isTransient()) {
                    IsisContext.getPersistenceSession().makePersistent(adapter);
                    context.unmapObject(adapter, Scope.REQUEST);
                }

                String view = context.getParameter("_" + VIEW);

                final String id = context.mapObject(adapter, Scope.REQUEST);
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
                if (message == null) {
                    message = "Saved changes to " + adapter.getSpecification().getSingularName();
                } else if (message.equals("")) {
                    message = null;
                }
                if (message != null) {
                    final MessageBroker messageBroker = IsisContext.getMessageBroker();
                    messageBroker.addMessage(message);
                }

            } else {
                final String view = context.getParameter("_" + ERROR);
                context.setRequestPath(view, Dispatcher.EDIT);

                entryState.setForm(formId);
                context.addVariable(ENTRY_FIELDS, entryState, Scope.REQUEST);
                context.addVariable(resultName, objectId, Scope.REQUEST);
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }

                final MessageBroker messageBroker = IsisContext.getMessageBroker();
                messageBroker.addWarning(entryState.getError());
            }

        } catch (final RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            throw e;
        }
    }

    private FormState validateObject(final RequestContext context, final ObjectAdapter object, final List<ObjectAssociation> fields) {
        final FormState formState = new FormState();
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final String fieldId = field.getId();
            String newEntry = context.getParameter(fieldId);
            if (fields.get(i).isOneToManyAssociation()) {
                continue;
            }
            if (fields.get(i).isVisible(IsisContext.getAuthenticationSession(), object, where).isVetoed()) {
                continue;
            }
            if (field.isUsable(IsisContext.getAuthenticationSession(), object, where).isVetoed()) {
                continue;
            }

            if (newEntry != null && newEntry.equals("-OTHER-")) {
                newEntry = context.getParameter(fieldId + "-other");
            }

            if (newEntry == null) {
                // TODO duplicated in EditObject; line 97
                final ObjectSpecification spec = field.getSpecification();
                if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class)) || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class))) {
                    newEntry = FALSE;
                } else {
                    continue;
                }
            }
            final FieldEditState fieldState = formState.createField(fieldId, newEntry);

            Consent consent = null;
            if (field.isMandatory() && (newEntry.equals("") || newEntry.equals("NULL"))) {
                consent = new Veto(field.getName() + " required");
                formState.setError("Not all fields have been set");
            } else if (field.getSpecification().containsFacet(ParseableFacet.class)) {
                try {
                    final ParseableFacet facet = field.getSpecification().getFacet(ParseableFacet.class);
                    final ObjectAdapter originalValue = field.get(object);
                    Localization localization = IsisContext.getLocalization(); 
                    final ObjectAdapter newValue = facet.parseTextEntry(originalValue, newEntry, localization); 
                    consent = ((OneToOneAssociation) field).isAssociationValid(object, newValue);
                    fieldState.setValue(newValue);
                } catch (final TextEntryParseException e) {
                    consent = new Veto(e.getMessage());
                    // formState.setError("Not all fields have been entered correctly");
                }

            } else {
                final ObjectAdapter associate = newEntry.equals("null") ? null : context.getMappedObject(newEntry);
                if (associate != null) {
                    IsisContext.getPersistenceSession().resolveImmediately(associate);
                }
                consent = ((OneToOneAssociation) field).isAssociationValid(object, associate);
                fieldState.setValue(associate);

            }
            if (consent.isVetoed()) {
                fieldState.setError(consent.getReason());
                formState.setError("Not all fields have been entered correctly");
            }
        }

        // TODO check the state of the complete object.
        return formState;
    }

    private void changeObject(final RequestContext context, final ObjectAdapter object, final FormState editState, final List<ObjectAssociation> fields) {
        for (int i = 0; i < fields.size(); i++) {
            final FieldEditState field = editState.getField(fields.get(i).getId());
            if (field == null) {
                continue;
            }
            final String newEntry = field.getEntry();
            final ObjectAdapter originalValue = fields.get(i).get(object);
            final boolean isVisible = fields.get(i).isVisible(IsisContext.getAuthenticationSession(), object, where).isAllowed();
            final boolean isUsable = fields.get(i).isUsable(IsisContext.getAuthenticationSession(), object, where).isAllowed();
            final boolean bothEmpty = originalValue == null && newEntry.equals("");
            final boolean bothSame = newEntry.equals(originalValue == null ? "" : originalValue.titleString());
            if ((!isVisible || !isUsable) || bothEmpty || bothSame) {
                if (fields.get(i).getSpecification().getFacet(ParseableFacet.class) == null) {
                    // REVIEW restores object to loader
                    context.getMappedObject(newEntry);
                }
                continue;
            }

            if (fields.get(i).getSpecification().containsFacet(ParseableFacet.class)) {
                final ParseableFacet facet = fields.get(i).getSpecification().getFacet(ParseableFacet.class);
                Localization localization = IsisContext.getLocalization(); 
                final ObjectAdapter newValue = facet.parseTextEntry(originalValue, newEntry, localization);
                ((OneToOneAssociation) fields.get(i)).set(object, newValue);
            } else {
                ((OneToOneAssociation) fields.get(i)).set(object, field.getValue());
            }
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }
}
