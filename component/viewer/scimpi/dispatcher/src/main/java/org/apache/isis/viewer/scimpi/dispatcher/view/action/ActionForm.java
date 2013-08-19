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

package org.apache.isis.viewer.scimpi.dispatcher.view.action;

import java.util.List;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.ActionAction;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.edit.FieldFactory;
import org.apache.isis.viewer.scimpi.dispatcher.view.edit.FormFieldBlock;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HiddenInputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HtmlFormBuilder;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;

public class ActionForm extends AbstractElementProcessor {

    // REVIEW: confirm this rendering context
    private final static Where where = Where.OBJECT_FORMS;

    @Override
    public void process(final Request request) {
        final CreateFormParameter parameters = new CreateFormParameter();
        parameters.objectId = request.getOptionalProperty(OBJECT);
        parameters.methodName = request.getRequiredProperty(METHOD);
        parameters.forwardResultTo = request.getOptionalProperty(VIEW);
        parameters.forwardVoidTo = request.getOptionalProperty(VOID);
        parameters.forwardErrorTo = request.getOptionalProperty(ERROR);
        parameters.cancelTo = request.getOptionalProperty(CANCEL_TO); 
        parameters.showIcon = request.isRequested(SHOW_ICON, showIconByDefault());
        parameters.buttonTitle = request.getOptionalProperty(BUTTON_TITLE);
        parameters.formTitle = request.getOptionalProperty(FORM_TITLE);
        parameters.labelDelimiter = request.getOptionalProperty(LABEL_DELIMITER, ":");
        parameters.formId = request.getOptionalProperty(FORM_ID, request.nextFormId());
        parameters.resultName = request.getOptionalProperty(RESULT_NAME);
        parameters.resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        parameters.scope = request.getOptionalProperty(SCOPE);
        parameters.className = request.getOptionalProperty(CLASS, "action full");
        parameters.showMessage = request.isRequested(SHOW_MESSAGE, false);
        parameters.completionMessage = request.getOptionalProperty(MESSAGE);
        parameters.id = request.getOptionalProperty(ID, parameters.methodName);
        createForm(request, parameters);
    }

    public static void createForm(final Request request, final CreateFormParameter parameterObject) {
        createForm(request, parameterObject, false);
    }

    protected static void createForm(final Request request, final CreateFormParameter parameterObject, final boolean withoutProcessing) {
        final RequestContext context = request.getContext();
        final ObjectAdapter object = MethodsUtils.findObject(context, parameterObject.objectId);
        final String version = request.getContext().mapVersion(object);
        final ObjectAction action = MethodsUtils.findAction(object, parameterObject.methodName);
        // TODO how do we distinguish between overloaded methods?

        // REVIEW Is this useful?
        if (action.getParameterCount() == 0) {
            throw new ScimpiException("Action form can only be used for actions with parameters");
        }
        if (parameterObject.showMessage && MethodsUtils.isVisible(object, action, where)) {
            final String notUsable = MethodsUtils.isUsable(object, action, where);
            if (notUsable != null) {
                if (!withoutProcessing) {
                    request.skipUntilClose();
                }
                request.appendHtml("<div class=\"" + parameterObject.className + "-message\" >");
                request.appendAsHtmlEncoded(notUsable);
                request.appendHtml("</div>");
                return;
            }
        }
        if (!MethodsUtils.isVisibleAndUsable(object, action, where)) {
            if (!withoutProcessing) {
                request.skipUntilClose();
            }
            return;
        }
        final String objectId = context.mapObject(object, Scope.INTERACTION);
        final String errorView = context.fullFilePath(parameterObject.forwardErrorTo == null ? context.getResourceFile() : parameterObject.forwardErrorTo);
        final String voidView = context.fullFilePath(parameterObject.forwardVoidTo == null ? context.getResourceFile() : parameterObject.forwardVoidTo);
        if (false /* action.isContributed() && !action.hasReturn() && parameterObject.resultOverride == null */) {
            parameterObject.resultOverride = objectId;
        }
        final HiddenInputField[] hiddenFields = new HiddenInputField[] { new HiddenInputField("_" + OBJECT, objectId), new HiddenInputField("_" + VERSION, version), new HiddenInputField("_" + FORM_ID, parameterObject.formId), new HiddenInputField("_" + METHOD, parameterObject.methodName),
                parameterObject.forwardResultTo == null ? null : new HiddenInputField("_" + VIEW, context.fullFilePath(parameterObject.forwardResultTo)), new HiddenInputField("_" + VOID, voidView), new HiddenInputField("_" + ERROR, errorView),
                parameterObject.completionMessage == null ? null : new HiddenInputField("_" + MESSAGE, parameterObject.completionMessage), parameterObject.scope == null ? null : new HiddenInputField("_" + SCOPE, parameterObject.scope),
                parameterObject.resultOverride == null ? null : new HiddenInputField("_" + RESULT_OVERRIDE, parameterObject.resultOverride), parameterObject.resultName == null ? null : new HiddenInputField("_" + RESULT_NAME, parameterObject.resultName),
                parameterObject.resultName == null ? null : new HiddenInputField(RequestContext.RESULT, (String) request.getContext().getVariable(RequestContext.RESULT)) };

        // TODO when the block contains a selector tag it doesn't disable it if
        // the field cannot be edited!!!
        final FormFieldBlock containedBlock = new FormFieldBlock() {
            @Override
            public boolean isNullable(final String name) {
                final int index = Integer.parseInt(name.substring(5)) - 1;
                final ObjectActionParameter param = action.getParameters().get(index);
                return param.isOptional();
            }
        };
        request.setBlockContent(containedBlock);
        if (!withoutProcessing) {
            request.processUtilCloseTag();
        }

        final FormState entryState = (FormState) context.getVariable(ENTRY_FIELDS);

        // TODO the list of included fields should be considered in the next
        // method (see EditObject)
        final InputField[] formFields = createFields(action, object);
        containedBlock.hideExcludedParameters(formFields);
        containedBlock.setUpValues(formFields);
        initializeFields(context, object, action, formFields);
        setDefaults(context, object, action, formFields, entryState, parameterObject.showIcon);
        String errors = null;
        if (entryState != null && entryState.isForForm(parameterObject.formId)) {
            copyEntryState(context, object, action, formFields, entryState);
            errors = entryState.getError();
        }
        overrideWithHtml(context, containedBlock, formFields);

        String formTitle;
        if (parameterObject.formTitle == null) {
            formTitle = action.getName();
        } else {
            formTitle = parameterObject.formTitle;
        }

        String buttonTitle = parameterObject.buttonTitle;
        if (buttonTitle == null) {
            buttonTitle = action.getName();
        } else if (buttonTitle.equals("")) {
            buttonTitle = "Ok";
        }

        HtmlFormBuilder.createForm(request, ActionAction.ACTION + ".app", hiddenFields, formFields, parameterObject.className,
                parameterObject.id, formTitle, parameterObject.labelDelimiter, action.getDescription(), action.getHelp(), buttonTitle, errors, parameterObject.cancelTo);

        request.popBlockContent();
    }

    private static InputField[] createFields(final ObjectAction action, final ObjectAdapter object) {
        final int parameterCount = action.getParameterCount();
        final InputField[] fields = new InputField[parameterCount];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new InputField(ActionAction.parameterName(i));
        }
        return fields;
    }

    private static void initializeFields(final RequestContext context, final ObjectAdapter object, final ObjectAction action, final InputField[] fields) {
        final List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < fields.length; i++) {
            final InputField field = fields[i];
            final ObjectActionParameter param = parameters.get(i);
            if (false /*action.isContributed() && i == 0*/) {
                // fields[i].setValue(context.mapObject(object,
                // Scope.INTERACTION));
                fields[i].setType(InputField.REFERENCE);
                fields[i].setHidden(true);
            } else {

                fields[i].setHelpReference("xxxhelp");
                final ObjectAdapter[] optionsForParameter = action.getChoices(object)[i];
                FieldFactory.initializeField(context, object, param, optionsForParameter, !param.isOptional(), field);
            }
        }
    }

    /**
     * Sets up the fields with their initial values
     * 
     * @param showIcon
     */
    private static void setDefaults(final RequestContext context, final ObjectAdapter object, final ObjectAction action, final InputField[] fields, final FormState entryState, final boolean showIcon) {
        final ObjectAdapter[] defaultValues = action.getDefaults(object);
        if (defaultValues == null) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            final InputField field = fields[i];
            final ObjectAdapter defaultValue = defaultValues[i];

            final String title = defaultValue == null ? "" : defaultValue.titleString();
            if (field.getType() == InputField.REFERENCE) {
                final ObjectSpecification objectSpecification = action.getParameters().get(i).getSpecification();
                if (defaultValue != null) {
                    final String imageSegment = showIcon ? "<img class=\"small-icon\" src=\"" + context.imagePath(objectSpecification) + "\" alt=\"" + objectSpecification.getShortIdentifier() + "\"/>" : "";
                    final String html = imageSegment + title;
                    final String value = context.mapObject(defaultValue, Scope.INTERACTION);
                    field.setValue(value);
                    field.setHtml(html);
                }
            } else {
                field.setValue(title);
            }
        }
    }

    private static void copyEntryState(final RequestContext context, final ObjectAdapter object, final ObjectAction action, final InputField[] fields, final FormState entryState) {

        for (final InputField field : fields) {
            final FieldEditState fieldState = entryState.getField(field.getName());
            if (fieldState != null) {
                if (field.isEditable()) {
                    String entry;
                    entry = fieldState.getEntry();
                    field.setValue(entry);
                }

                field.setErrorText(fieldState.getError());
            }
        }
    }

    private static void overrideWithHtml(final RequestContext context, final FormFieldBlock containedBlock, final InputField[] formFields) {
        for (int i = 0; i < formFields.length; i++) {
            final String id = ActionAction.parameterName(i);
            if (containedBlock.hasContent(id)) {
                final String content = containedBlock.getContent(id);
                if (content != null) {
                    formFields[i].setHtml(content);
                    formFields[i].setType(InputField.HTML);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "action-form";
    }

}
