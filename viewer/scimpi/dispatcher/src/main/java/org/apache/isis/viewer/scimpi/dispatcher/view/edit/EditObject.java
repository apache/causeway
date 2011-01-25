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
import java.util.Locale;
import java.util.TimeZone;

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
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputForm;


public class EditObject extends AbstractElementProcessor {

    @Override
    public void process(Request request) {
        RequestContext context = request.getContext();

        String objectId = request.getOptionalProperty(OBJECT);
        String forwardEditedTo = request.getOptionalProperty(VIEW);
        String forwardErrorTo = request.getOptionalProperty(ERRORS);
        boolean hideNonEditableFields = request.isRequested("hide-uneditable", false);
        String buttonTitle = request.getOptionalProperty(BUTTON_TITLE, "Save");
        String formTitle = request.getOptionalProperty(FORM_TITLE);
        String variable = request.getOptionalProperty(RESULT_NAME);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        String scope = request.getOptionalProperty(SCOPE);
        String className = request.getOptionalProperty(CLASS, "edit");
        String id = request.getOptionalProperty(ID);
/*        
        String localeId = request.getOptionalProperty("locale");
        String timeZoneId = request.getOptionalProperty("timezone");
        
 */
        Locale locale = null; // new Locale(localeId);
        TimeZone timeZone = null; //TimeZone.getTimeZone(timeZoneId);

        final ObjectAdapter object = context.getMappedObjectOrResult(objectId);
        String actualObjectId = context.mapObject(object, Scope.INTERACTION);
        String version = context.mapVersion(object);

        EditFieldBlock containedBlock = new EditFieldBlock() {
            @Override
            public boolean isVisible(String name) {
                ObjectAssociation fld = object.getSpecification().getAssociation(name);
                boolean isVisible = fld.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed();
                boolean isUseable = fld.isUsable(IsisContext.getAuthenticationSession(), object).isAllowed();
                return isVisible && isUseable;
            }
        };
        request.setBlockContent(containedBlock);
        request.processUtilCloseTag();
        AuthenticationSession session = IsisContext.getAuthenticationSession();
        List<ObjectAssociation> fields = object.getSpecification().getAssociations(
                ObjectAssociationFilters.dynamicallyVisible(session, object));
        fields = containedBlock.includedFields(fields);

        InputField[] formFields = createFields(fields);
        FormState entryState = (FormState) context.getVariable(ENTRY_FIELDS);
        initializeFields(context, object, formFields, entryState, !hideNonEditableFields);
        setDefaults(context, object, formFields, entryState);
        copyFieldContent(context, object, formFields, locale, timeZone);
        overrideWithHtml(context, containedBlock, formFields);
        if (entryState != null && entryState.isForForm(actualObjectId)) {
            copyEntryState(context, object, formFields, entryState);
        }

        String errorView = context.fullFilePath(forwardErrorTo == null ? context.getResourceFile() : forwardErrorTo);
        HiddenInputField[] hiddenFields = new HiddenInputField[] { 
                new HiddenInputField(OBJECT, actualObjectId),
                new HiddenInputField(VERSION, version),
                forwardEditedTo == null ? null : new HiddenInputField(VIEW, context.fullFilePath(forwardEditedTo)),
                new HiddenInputField(ERRORS, errorView), variable == null ? null : new HiddenInputField(RESULT_NAME, variable),
                resultOverride == null ? null : new HiddenInputField(RESULT_OVERRIDE, resultOverride),
                scope == null ? null : new HiddenInputField(SCOPE, scope) };

        InputForm.createForm(request, EditAction.ACTION + ".app", buttonTitle, formFields, hiddenFields, formTitle, null, null, className, id);
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
                FieldFactory.initializeField(context, object, field, options, field.isMandatory(), includeUnusableFields, formField);
                
                Consent usable = field.isUsable(session, object);
                boolean isEditable = true;
                isEditable = isEditable && usable.isAllowed();
                if (usable.isVetoed()) {
                    formField.setDescription(usable.getReason());
                }
                formField.setEditable(isEditable);

            
            /*
                int type;

                ObjectSpecification spec = field.getSpecification();
                if (spec.getFacet(PasswordValueFacet.class) != null) {
                    type = InputField.PASSWORD;

                } else if (spec.getFacet(BooleanValueFacet.class) != null) {
                    type = InputField.CHECKBOX;

                } else if (spec.getFacet(ParseableFacet.class) != null) {
                    type = InputField.TEXT;

                    MaxLengthFacet maxLengthFacet = field.getFacet(MaxLengthFacet.class);
                    final int maxLength = maxLengthFacet.value();
                    formField.setMaxLength(maxLength);

                    TypicalLengthFacet typicalLengthFacet = field.getFacet(TypicalLengthFacet.class);
                    if (typicalLengthFacet.isDerived() && maxLength > 0) {
                        formField.setWidth(maxLength);
                    } else {
                        formField.setWidth(typicalLengthFacet.value());
                    }

                    MultiLineFacet multiLineFacet = field.getFacet(MultiLineFacet.class);
                    formField.setHeight(multiLineFacet.numberOfLines());
                    formField.setWrapped(!multiLineFacet.preventWrapping());

                } else {
                    type = InputField.REFERENCE;
                }

                formField.setType(type);
                formField.setHidden(false);
                formField.setRequired(field.isMandatory());
                formField.setDescription(field.getDescription());
                formField.setLabel(field.getName());

                Consent usable = field.isUsable(session, object);
                boolean isEditable = true;
                isEditable = isEditable && usable.isAllowed();
                if (usable.isVetoed()) {
                    formField.setDescription(usable.getReason());
                }
                formField.setEditable(isEditable);
            */

            
            
            } else {
                formFields[i].setHidden(true);
            }
        }
    }

    private void copyFieldContent(RequestContext context, ObjectAdapter object, InputField[] formFields, Locale locale, TimeZone TimeZone) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldName = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            InputField inputField = formFields[i];
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed()) {
                IsisContext.getPersistenceSession().resolveField(object, field);
                ObjectAdapter fieldValue = field.get(object);
                if (inputField.isEditable()) {
                    String value = getValue(context, fieldValue, locale, TimeZone);
                    if (!value.equals("") || inputField.getValue() == null) {
                        inputField.setValue(value);
                    }
                } else {
                    String entry = getValue(context, fieldValue, locale, TimeZone);
                    inputField.setHtml(entry);
                    inputField.setType(InputField.HTML);

                }

                if (field.getSpecification().getFacet(ParseableFacet.class) == null) {
                    if (fieldValue != null) {
                        String entry = "<img class=\"small-icon\" src=\"" + context.imagePath(field.getSpecification())
                                + "\" alt=\"" + field.getSpecification().getShortIdentifier() + "\"/>" + fieldValue.titleString();
                        inputField.setHtml(entry);
                    } else {
                        String entry = "<em>none specified</em>";
                        inputField.setHtml(entry);
                    }
                }
            }
        }
    }
/*
    private void setupOptions(RequestContext context, ObjectAdapter object, InputField[] formFields) {
        for (int i = 0; i < formFields.length; i++) {
            String fieldId = formFields[i].getName();
            ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            InputField formField = formFields[i];
            if (field.isVisible(IsisContext.getAuthenticationSession(), object).isAllowed() && formField.isEditable()) {
                ObjectAdapter[] options = field.getChoices(object);
                if (options != null) {
                    String[] optionValues = new String[options.length];
                    String[] optionTitles = new String[options.length];
                    for (int j = 0; j < options.length; j++) {
                        optionValues[j] = getValue(context, options[j]);
                        optionTitles[j] = options[j].titleString();
                    }
                    formField.setOptions(optionTitles, optionValues);
                }
            }
        }
    }
*/
    private void setDefaults(RequestContext context, ObjectAdapter object, InputField[] formFields, FormState entryState) {
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
                    String html = "<img class=\"small-icon\" src=\"" + context.imagePath(objectSpecification) + "\" alt=\""
                            + objectSpecification.getShortIdentifier() + "\"/>" + title;
                    formFields[i].setHtml(html);
                    String value = defaultValue == null ? null : context.mapObject(defaultValue, Scope.INTERACTION);
                    formFields[i].setValue(value);
                }
            }
        }
    }

    private void overrideWithHtml(RequestContext context, EditFieldBlock containedBlock, InputField[] formFields) {
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

    private String getValue(RequestContext context, ObjectAdapter field, Locale locale, TimeZone timeZone) {
        if (field == null) {
            return "";
        }
        ParseableFacet facet = field.getSpecification().getFacet(ParseableFacet.class);
        if (facet == null) {
            return context.mapObject(field, Scope.INTERACTION);
        } else {
            /*
            if ( field.getObject() instanceof DateTime) {
                Date date = ((DateTime) field.getObject()).dateValue();
                
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                format.setTimeZone(timeZone);
                
                return format.format(date);
            } else {
            }
            */
            return field.titleString();
        }
    }

    @Override
    public String getName() {
        return "edit";
    }

}

