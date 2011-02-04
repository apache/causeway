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


package org.apache.isis.viewer.scimpi.dispatcher.view.edit;

import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.EditAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HiddenInputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HtmlFormBuilder;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;


public class EditObject extends AbstractElementProcessor {

    @Override
    public void process(Request request) {
        RequestContext context = request.getContext();

        String objectId = request.getOptionalProperty(OBJECT);
        String forwardEditedTo = request.getOptionalProperty(VIEW);
        String forwardErrorTo = request.getOptionalProperty(ERRORS);
        boolean hideNonEditableFields = request.isRequested(HIDE_UNEDITABLE, false);
        boolean showIcon = request.isRequested(SHOW_ICON, true);
        String buttonTitle = request.getOptionalProperty(BUTTON_TITLE);
        String formTitle = request.getOptionalProperty(FORM_TITLE);
        String variable = request.getOptionalProperty(RESULT_NAME);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        String scope = request.getOptionalProperty(SCOPE);
        String className = request.getOptionalProperty(CLASS, "edit full");
        String id = request.getOptionalProperty(ID);
        String completionMessage = request.getOptionalProperty(MESSAGE);


        final ObjectAdapter object = context.getMappedObjectOrResult(objectId);
        String actualObjectId = context.mapObject(object, Scope.INTERACTION);
        String version = context.mapVersion(object);

        final FormState entryState = (FormState) context.getVariable(ENTRY_FIELDS);

        final ObjectSpecification specification = object.getSpecification();
        FormFieldBlock containedBlock = new FormFieldBlock() {
            @Override
            public boolean isVisible(String name) {
                ObjectAssociation fld = specification.getAssociation(name);
                boolean isVisible = fld.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed();
                boolean isUseable = fld.isUsable(IsisContext.getAuthenticationSession(), object).isAllowed();
                return isVisible && isUseable;
            }
            
            public ObjectAdapter getCurrent(String name) {
                ObjectAdapter value = null;
                if (entryState != null) {
                    FieldEditState field2 = entryState.getField(name);
                    value = field2.getValue();
                }
                if (value == null) {
                    ObjectAssociation fld = specification.getAssociation(name);
                    value = fld.get(object);
                }
                return value;
            }
            
            public boolean isNullable(String name) {
                ObjectAssociation fld = specification.getAssociation(name);
                return !fld.isMandatory();
            }
        };
        
        request.setBlockContent(containedBlock);
        request.processUtilCloseTag();

        AuthenticationSession session = IsisContext.getAuthenticationSession();
        List<ObjectAssociation> fields = specification.getAssociations(ObjectAssociationFilters.dynamicallyVisible(session, object));
        fields = containedBlock.includedFields(fields);
        InputField[] formFields = createFields(fields);
        
        initializeFields(context, object, formFields, entryState, !hideNonEditableFields);
        setDefaults(context, object, formFields, entryState, showIcon);
        
        copyFieldContent(context, object, formFields, showIcon);
        overrideWithHtml(context, containedBlock, formFields);
        String errors = null;
        if (entryState != null && entryState.isForForm(actualObjectId)) {
            copyEntryState(context, object, formFields, entryState);
            errors = entryState.getError(); 
        }

        String errorView = context.fullFilePath(forwardErrorTo == null ? context.getResourceFile() : forwardErrorTo);
        HiddenInputField[] hiddenFields = new HiddenInputField[] { 
                new HiddenInputField(OBJECT, actualObjectId),
                new HiddenInputField(VERSION, version),
                completionMessage == null ? null : new HiddenInputField(MESSAGE, completionMessage),
                forwardEditedTo == null ? null : new HiddenInputField(VIEW, context.fullFilePath(forwardEditedTo)),
                new HiddenInputField(ERRORS, errorView), variable == null ? null : new HiddenInputField(RESULT_NAME, variable),
                resultOverride == null ? null : new HiddenInputField(RESULT_OVERRIDE, resultOverride),
                scope == null ? null : new HiddenInputField(SCOPE, scope) };


        if (formTitle == null) {
            formTitle = specification.getSingularName();
        }

        if (buttonTitle == null) {
            buttonTitle = "Save " + specification.getSingularName();
        } else if( buttonTitle.equals("")) {
            buttonTitle = "Save";
        }
        
        HtmlFormBuilder.createForm(request, EditAction.ACTION + ".app", hiddenFields, formFields, className, id, formTitle, null, null, buttonTitle, errors);
        request.popBlockContent();
    }

    private InputField[] createFields(List<ObjectAssociation> fields) {
        InputField[] formFields = new InputField[fields.size()];
        int length = 0;
        for (int i = 0; i < fields.size(); i++) {
            if (!fields.get(i).isOneToManyAssociation()) {
                formFields[i] = new InputField(fields.get(i).getId());
                length++;
            }
        }
        InputField[] array = new InputField[length];
        for (int i = 0, j = 0; i < formFields.length; i++) {
            if (formFields[i] != null) {
                array[j++] = formFields[i];
            }
        }
        return array;
    }

    // TODO duplicated in ActionForm#initializeFields
    private void initializeFields(RequestContext context, ObjectAdapter object, InputField[] formFields, FormState entryState, boolean includeUnusableFields) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldId = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            InputField formField = formFields[i];
            
            AuthenticationSession session = IsisContext.getAuthenticationSession();
            if (field.isVisible(session, object).isAllowed() && (includeUnusableFields || field.isUsable(session, object).isAllowed())) {
                ObjectAdapter[] options = field.getChoices(object);
                FieldFactory.initializeField(context, object, field, options, field.isMandatory(), formField);
                
                Consent usable = field.isUsable(session, object);
                boolean isEditable = true;
                isEditable = isEditable && usable.isAllowed();
                if (usable.isVetoed()) {
                    formField.setDescription(usable.getReason());
                }
                formField.setEditable(isEditable);
            } else {
                formFields[i].setHidden(true);
            }
        }
    }

    private void copyFieldContent(RequestContext context, ObjectAdapter object, InputField[] formFields, boolean showIcon) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldName = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            InputField inputField = formFields[i];
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed()) {
                IsisContext.getPersistenceSession().resolveField(object, field);
                ObjectAdapter fieldValue = field.get(object);
                if (inputField.isEditable()) {
                    String value = getValue(context, fieldValue);
                    if (!value.equals("") || inputField.getValue() == null) {
                        inputField.setValue(value);
                    }
                } else {
                    String entry = getValue(context, fieldValue);
                    inputField.setHtml(entry);
                    inputField.setType(InputField.HTML);

                }

                if (field.getSpecification().getFacet(ParseableFacet.class) == null) {
                    if (fieldValue != null) {
                        String iconSegment = showIcon ? "<img class=\"small-icon\" src=\"" + context.imagePath(field.getSpecification())
                                                        + "\" alt=\"" + field.getSpecification().getShortIdentifier() + "\"/>" : "";
                        String entry = iconSegment + fieldValue.titleString();
                        inputField.setHtml(entry);
                    } else {
                        String entry = "<em>none specified</em>";
                        inputField.setHtml(entry);
                    }
                }
            }
        }
    }

    private void setDefaults(RequestContext context, ObjectAdapter object, InputField[] formFields, FormState entryState, boolean showIcon) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldId = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            ObjectAdapter defaultValue = field.getDefault(object);
            if (defaultValue == null) {
                continue;
            }

            String title = defaultValue.titleString();
            if (field.getSpecification().containsFacet(ParseableFacet.class)) {
                formFields[i].setValue(title);
            } else if (field.isOneToOneAssociation()) {
                ObjectSpecification objectSpecification = field.getSpecification();
                if (defaultValue != null) {
                    String iconSegment = showIcon ? "<img class=\"small-icon\" src=\"" + context.imagePath(objectSpecification) + "\" alt=\""
                                                + objectSpecification.getShortIdentifier() + "\"/>" : "";
                    String html = iconSegment + title;
                    formFields[i].setHtml(html);
                    String value = defaultValue == null ? null : context.mapObject(defaultValue, Scope.INTERACTION);
                    formFields[i].setValue(value);
                }
            }
        }
    }

    private void overrideWithHtml(RequestContext context, FormFieldBlock containedBlock, InputField[] formFields) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldId = formFields[i].getName();
            if (containedBlock.hasContent(fieldId)) {
                String content = containedBlock.getContent(fieldId);
                if (content != null) {
                    formFields[i].setHtml(content);
                    formFields[i].setValue(null);
                    formFields[i].setType(InputField.HTML);
                }
            }
        }
    }

    private void copyEntryState(RequestContext context, ObjectAdapter object, InputField[] formFields, FormState entryState) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldId = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            InputField formField = formFields[i];
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed() && formField.isEditable()) {
                FieldEditState fieldState = entryState.getField(field.getId());
                String entry = fieldState == null ? "" : fieldState.getEntry();
                formField.setValue(entry);
                String error =  fieldState == null ? "" : fieldState.getError();
                formField.setErrorText(error);
            }
        }
    }

    private String getValue(RequestContext context, ObjectAdapter field) {
        if (field == null) {
            return "";
        }
        ParseableFacet facet = field.getSpecification().getFacet(ParseableFacet.class);
        if (facet == null) {
            return context.mapObject(field, Scope.INTERACTION);
        } else {
            return field.titleString();
        }
    }

    @Override
    public String getName() {
        return "edit";
    }

}

