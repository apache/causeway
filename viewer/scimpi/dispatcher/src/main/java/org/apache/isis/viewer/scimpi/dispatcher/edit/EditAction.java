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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugView;


public class EditAction implements Action {
    public static final String ACTION = "edit";

    @Override
    public String getName() {
        return ACTION;
    }

    @Override
    public void process(RequestContext context) throws IOException {
        try {
            String objectId = context.getParameter(OBJECT);
            String version = context.getParameter(VERSION);
            String resultName = context.getParameter(RESULT_NAME);
            resultName = resultName == null ? RequestContext.RESULT : resultName;
            String override = context.getParameter(RESULT_OVERRIDE);
            String message = context.getParameter(MESSAGE);
            
            ObjectAdapter adapter = context.getMappedObject(objectId);
            AuthenticationSession session= IsisContext.getAuthenticationSession();
            List<ObjectAssociation> fields = adapter.getSpecification().getAssociations(ObjectAssociationFilters.dynamicallyVisible(session, adapter));
            FormState entryState = validateObject(context, adapter, fields);
            Version adapterVersion = adapter.getVersion();
            Version formVersion = context.getVersion(version);
            if (formVersion != null && adapterVersion.different(formVersion)) {
                
                IsisContext.getMessageBroker().addMessage("The " + adapter.getSpecification().getSingularName() + " was edited " +
                		"by another user (" + adapterVersion.getUser() +  "). Please  make your changes based on their changes.");

                String view = context.getParameter(ERRORS);
                context.setRequestPath(view, Dispatcher.EDIT);
                
                entryState.setForm(objectId);
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

                String view = context.getParameter(VIEW);

                String id = context.mapObject(adapter, Scope.REQUEST);
                context.addVariable(resultName, id, Scope.REQUEST);
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }                

                int questionMark = view == null ? -1 : view.indexOf("?");
                if (questionMark > -1) {
                    String params = view.substring(questionMark + 1);
                    int equals = params.indexOf("=");
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
                    MessageBroker messageBroker = IsisContext.getMessageBroker();
                    messageBroker.addMessage(message);
                }

            } else {
                String view = context.getParameter(ERRORS);
                context.setRequestPath(view, Dispatcher.EDIT);
                
                entryState.setForm(objectId);
                context.addVariable(ENTRY_FIELDS, entryState, Scope.REQUEST);
                context.addVariable(resultName, objectId, Scope.REQUEST);
                if (override != null) {
                    context.addVariable(resultName, override, Scope.REQUEST);
                }   
                
                MessageBroker messageBroker = IsisContext.getMessageBroker();
                messageBroker.addWarning(entryState.getError());
            }

        } catch (RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            IsisContext.getUpdateNotifier().clear();
            IsisContext.getUpdateNotifier().clear();
            throw e;
        }
    }

    private FormState validateObject(
            RequestContext context,
            ObjectAdapter object,
            List<ObjectAssociation> fields) {
        FormState formState = new FormState();
        for (int i = 0; i < fields.size(); i++) {
            ObjectAssociation field = fields.get(i);
            String fieldId = field.getId();
            String newEntry = context.getParameter(fieldId);
            if (fields.get(i).isOneToManyAssociation()) {
                continue;
            }
            if (fields.get(i).isVisible(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                continue;
            }
            if (field.isUsable(IsisContext.getAuthenticationSession(), object).isVetoed()) {
                continue;
            }
            
            if (newEntry != null && newEntry.equals("-OTHER-")) {
                newEntry = context.getParameter(fieldId + "-other");
            }

            if (newEntry == null) {
                // TODO duplicated in EditObject; line 97
                ObjectSpecification spec = field.getSpecification();
                if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class))
                                || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class))) {
                    newEntry = FALSE;
                } else {
                   continue;
                }
            }
            FieldEditState fieldState = formState.createField(fieldId, newEntry);
            
            Consent consent = null;
            if (field.isMandatory() && newEntry.equals("")) {
                consent = new Veto(field.getName() + " required");
                formState.setError("Not all fields have been set");

            } else if (field.getSpecification().containsFacet(ParseableFacet.class)) {
                try {
                    ParseableFacet facet = field.getSpecification().getFacet(ParseableFacet.class);
                    ObjectAdapter originalValue = field.get(object);
                    ObjectAdapter newValue =  facet.parseTextEntry(originalValue, newEntry);
                    consent = ((OneToOneAssociation) field).isAssociationValid(object, newValue);
                    fieldState.setValue(originalValue);
                } catch (TextEntryParseException e) {
                    consent = new Veto(e.getMessage());
                    formState.setError("Not all fields have been entered correctly");
                }

            } else {
                ObjectAdapter associate = context.getMappedObject(newEntry);
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

    private void changeObject(RequestContext context, ObjectAdapter object, FormState editState, List<ObjectAssociation> fields) {
        for (int i = 0; i < fields.size(); i++) {
            FieldEditState field = editState.getField(fields.get(i).getId());
            if (field == null) {
                continue;
            }
            String newEntry = field.getEntry();
            ObjectAdapter originalValue = fields.get(i).get(object);
            boolean isVisible = fields.get(i).isVisible(IsisContext.getAuthenticationSession(), object).isAllowed();
            boolean isUsable = fields.get(i).isUsable(IsisContext.getAuthenticationSession(), object).isAllowed();
            boolean bothEmpty = originalValue == null && newEntry.equals("");
            boolean bothSame = newEntry.equals(originalValue == null ? "" : originalValue.titleString());
            if ((!isVisible ||!isUsable) || bothEmpty || bothSame) {
                if (fields.get(i).getSpecification().getFacet(ParseableFacet.class) == null) {
                    // REVIEW restores object to loader
                    context.getMappedObject(newEntry);
                }
                continue;
            }
            
            if (fields.get(i).getSpecification().containsFacet(ParseableFacet.class)) {
                ParseableFacet facet = fields.get(i).getSpecification().getFacet(ParseableFacet.class);
                ObjectAdapter newValue =  facet.parseTextEntry(originalValue, newEntry);
                ((OneToOneAssociation) fields.get(i)).setAssociation(object, newValue);
            } else {
                ((OneToOneAssociation) fields.get(i)).setAssociation(object, field.getValue());
            }
        }
    }

    @Override
    public void init() {}

    @Override
    public void debug(DebugView view) {}
}

