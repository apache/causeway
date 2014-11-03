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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.facets.object.choices.enums.EnumFacet;
import org.apache.isis.core.metamodel.facets.value.booleans.BooleanValueFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ResolveFieldUtil;
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

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORMS;

    @Override
    public void process(final Request request) {
        final RequestContext context = request.getContext();

        final String objectId = request.getOptionalProperty(OBJECT);
        final String forwardEditedTo = request.getOptionalProperty(VIEW);
        final String forwardErrorTo = request.getOptionalProperty(ERROR);
        final String cancelTo = request.getOptionalProperty(CANCEL_TO); 
        final boolean hideNonEditableFields = request.isRequested(HIDE_UNEDITABLE, false);
        final boolean showIcon = request.isRequested(SHOW_ICON, showIconByDefault());
        final String labelDelimiter = request.getOptionalProperty(LABEL_DELIMITER, ":");
        String buttonTitle = request.getOptionalProperty(BUTTON_TITLE);
        String formTitle = request.getOptionalProperty(FORM_TITLE);
        final String formId = request.getOptionalProperty(FORM_ID, request.nextFormId());
        final String variable = request.getOptionalProperty(RESULT_NAME);
        final String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        final String scope = request.getOptionalProperty(SCOPE);
        final String className = request.getOptionalProperty(CLASS, "edit full");
        final String completionMessage = request.getOptionalProperty(MESSAGE);

        final ObjectAdapter object = context.getMappedObjectOrResult(objectId);
        final String actualObjectId = context.mapObject(object, Scope.INTERACTION);
        final String version = context.mapVersion(object);

        final String id = request.getOptionalProperty(ID, object.getSpecification().getShortIdentifier());

        final FormState entryState = (FormState) context.getVariable(ENTRY_FIELDS);

        final ObjectSpecification specification = object.getSpecification();
        final FormFieldBlock containedBlock = new FormFieldBlock() {
            @Override
            public boolean isVisible(final String name) {
                final ObjectAssociation fld = specification.getAssociation(name);
                final boolean isVisible = fld.isVisible(IsisContext.getAuthenticationSession(), object, where).isAllowed();
                final boolean isUseable = fld.isUsable(IsisContext.getAuthenticationSession(), object, where).isAllowed();
                return isVisible && isUseable;
            }

            @Override
            public ObjectAdapter getCurrent(final String name) {
                ObjectAdapter value = null;
                if (entryState != null) {
                    final FieldEditState field2 = entryState.getField(name);
                    value = field2.getValue();
                }
                if (value == null) {
                    final ObjectAssociation fld = specification.getAssociation(name);
                    value = fld.get(object);
                }
                return value;
            }

            @Override
            public boolean isNullable(final String name) {
                final ObjectAssociation fld = specification.getAssociation(name);
                return !fld.isMandatory();
            }
        };

        request.setBlockContent(containedBlock);
        request.processUtilCloseTag();

        final AuthenticationSession session = IsisContext.getAuthenticationSession();
        List<ObjectAssociation> viewFields = specification.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(session, object, where));
        viewFields = containedBlock.includedFields(viewFields);
        final InputField[] formFields = createFields(viewFields);

        initializeFields(context, object, formFields, entryState, !hideNonEditableFields);
        setDefaults(context, object, formFields, entryState, showIcon);

        copyFieldContent(context, object, formFields, showIcon);
        overrideWithHtml(context, containedBlock, formFields);
        String errors = null;
        if (entryState != null && entryState.isForForm(formId)) {
            copyEntryState(context, object, formFields, entryState);
            errors = entryState.getError();
        }

        final String errorView = context.fullFilePath(forwardErrorTo == null ? context.getResourceFile() : forwardErrorTo);
        final List<HiddenInputField> hiddenFields = new ArrayList<HiddenInputField>();
        hiddenFields.add(new HiddenInputField("_" + OBJECT, actualObjectId));
        hiddenFields.add(new HiddenInputField("_" + VERSION, version));
        hiddenFields.add(new HiddenInputField("_" + FORM_ID, formId));
        hiddenFields.add(completionMessage == null ? null : new HiddenInputField("_" + MESSAGE, completionMessage));
        hiddenFields.add(forwardEditedTo == null ? null : new HiddenInputField("_" + VIEW, context.fullFilePath(forwardEditedTo)));
        hiddenFields.add(new HiddenInputField("_" + ERROR, errorView));
        hiddenFields.add(variable == null ? null : new HiddenInputField("_" + RESULT_NAME, variable));
        hiddenFields.add(resultOverride == null ? null : new HiddenInputField("_" + RESULT_OVERRIDE, resultOverride));
        hiddenFields.add(scope == null ? null : new HiddenInputField("_" + SCOPE, scope));

        if (!object.isTransient()) {
            // ensure all booleans are included so the pass back TRUE if set.
            final List<ObjectAssociation> fields2 = object.getSpecification().getAssociations(Contributed.EXCLUDED);
            for (int i = 0; i < fields2.size(); i++) {
                final ObjectAssociation field = fields2.get(i);
                if (!viewFields.contains(field) && field.getSpecification().containsFacet(BooleanValueFacet.class)) {
                    final String fieldId = field.getId();
                    final String value = getValue(context, field.get(object));
                    hiddenFields.add(new HiddenInputField(fieldId, value));
                }
            }
        }

        if (formTitle == null) {
            formTitle = specification.getSingularName();
        }

        if (buttonTitle == null) {
            buttonTitle = "Save " + specification.getSingularName();
        } else if (buttonTitle.equals("")) {
            buttonTitle = "Save";
        }

        final HiddenInputField[] hiddenFieldArray = hiddenFields.toArray(new HiddenInputField[hiddenFields.size()]);
        HtmlFormBuilder.createForm(request, EditAction.ACTION + ".app", hiddenFieldArray, formFields, className, id, formTitle,
                labelDelimiter, null, null, buttonTitle, errors, cancelTo);
     request.popBlockContent();
    }

    private InputField[] createFields(final List<ObjectAssociation> fields) {
        final InputField[] formFields = new InputField[fields.size()];
        int length = 0;
        for (int i = 0; i < fields.size(); i++) {
            if (!fields.get(i).isOneToManyAssociation()) {
                formFields[i] = new InputField(fields.get(i).getId());
                length++;
            }
        }
        final InputField[] array = new InputField[length];
        for (int i = 0, j = 0; i < formFields.length; i++) {
            if (formFields[i] != null) {
                array[j++] = formFields[i];
            }
        }
        return array;
    }

    // TODO duplicated in ActionForm#initializeFields
    private void initializeFields(final RequestContext context, final ObjectAdapter object, final InputField[] formFields, final FormState entryState, final boolean includeUnusableFields) {
        for (final InputField formField : formFields) {
            final String fieldId = formField.getName();
            final ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            final AuthenticationSession session = IsisContext.getAuthenticationSession();
            final Consent usable = field.isUsable(session, object, where);
            final ObjectAdapter[] options = field.getChoices(object);
            FieldFactory.initializeField(context, object, field, options, field.isMandatory(), formField);

            final boolean isEditable = usable.isAllowed();
            if (!isEditable) {
                formField.setDescription(usable.getReason());
            }
            formField.setEditable(isEditable);
            final boolean hiddenField = field.isVisible(session, object, where).isVetoed();
            final boolean unusable = usable.isVetoed();
            final boolean hideAsUnusable = unusable && !includeUnusableFields;
            if (hiddenField || hideAsUnusable) {
                formField.setHidden(true);
            }
        }
    }

    private void copyFieldContent(final RequestContext context, final ObjectAdapter object, final InputField[] formFields, final boolean showIcon) {
        for (final InputField inputField : formFields) {
            final String fieldName = inputField.getName();
            final ObjectAssociation field = object.getSpecification().getAssociation(fieldName);
            if (field.isVisible(IsisContext.getAuthenticationSession(), object, where).isAllowed()) {
                ResolveFieldUtil.resolveField(object, field);
                final ObjectAdapter fieldValue = field.get(object);
                if (inputField.isEditable()) {
                    final String value = getValue(context, fieldValue);
                    if (!value.equals("") || inputField.getValue() == null) {
                        inputField.setValue(value);
                    }
                } else {
                    final String entry = getValue(context, fieldValue);
                    inputField.setHtml(entry);
                    inputField.setType(InputField.HTML);

                }

                if (field.getSpecification().getFacet(ParseableFacet.class) == null) {
                    if (fieldValue != null) {
                        final String iconSegment = showIcon ? "<img class=\"small-icon\" src=\"" + context.imagePath(field.getSpecification()) + "\" alt=\"" + field.getSpecification().getShortIdentifier() + "\"/>" : "";
                        final String entry = iconSegment + fieldValue.titleString();
                        inputField.setHtml(entry);
                    } else {
                        final String entry = "<em>none specified</em>";
                        inputField.setHtml(entry);
                    }
                }
            }
        }
    }

    private void setDefaults(final RequestContext context, final ObjectAdapter object, final InputField[] formFields, final FormState entryState, final boolean showIcon) {
        for (final InputField formField : formFields) {
            final String fieldId = formField.getName();
            final ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            final ObjectAdapter defaultValue = field.getDefault(object);
            if (defaultValue == null) {
                continue;
            }

            final String title = defaultValue.titleString();
            if (field.getSpecification().containsFacet(ParseableFacet.class)) {
                formField.setValue(title);
            } else if (field.isOneToOneAssociation()) {
                final ObjectSpecification objectSpecification = field.getSpecification();
                if (defaultValue != null) {
                    final String iconSegment = showIcon ? "<img class=\"small-icon\" src=\"" + context.imagePath(objectSpecification) + "\" alt=\"" + objectSpecification.getShortIdentifier() + "\"/>" : "";
                    final String html = iconSegment + title;
                    formField.setHtml(html);
                    final String value = defaultValue == null ? null : context.mapObject(defaultValue, Scope.INTERACTION);
                    formField.setValue(value);
                }
            }
        }
    }

    private void overrideWithHtml(final RequestContext context, final FormFieldBlock containedBlock, final InputField[] formFields) {
        for (final InputField formField : formFields) {
            final String fieldId = formField.getName();
            if (containedBlock.hasContent(fieldId)) {
                final String content = containedBlock.getContent(fieldId);
                if (content != null) {
                    formField.setHtml(content);
                    formField.setValue(null);
                    formField.setType(InputField.HTML);
                }
            }
        }
    }

    private void copyEntryState(final RequestContext context, final ObjectAdapter object, final InputField[] formFields, final FormState entryState) {
        for (final InputField formField : formFields) {
            final String fieldId = formField.getName();
            final ObjectAssociation field = object.getSpecification().getAssociation(fieldId);
            if (field.isVisible(IsisContext.getAuthenticationSession(), object, where).isAllowed() && formField.isEditable()) {
                final FieldEditState fieldState = entryState.getField(field.getId());
                final String entry = fieldState == null ? "" : fieldState.getEntry();
                formField.setValue(entry);
                final String error = fieldState == null ? "" : fieldState.getError();
                formField.setErrorText(error);
            }
        }
    }

    private String getValue(final RequestContext context, final ObjectAdapter field) {
        if (field == null || field.isTransient()) {
            return "";
        }
        final ObjectSpecification specification = field.getSpecification();
        if (specification.containsFacet(EnumFacet.class)) {
            return String.valueOf(field.getObject());
        } else if (specification.getFacet(ParseableFacet.class) == null) {
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
