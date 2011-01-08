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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.action.ActionAction;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FieldEditState;
import org.apache.isis.viewer.scimpi.dispatcher.edit.FormState;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.edit.EditFieldBlock;
import org.apache.isis.viewer.scimpi.dispatcher.view.edit.FieldFactory;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.HiddenInputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputField;
import org.apache.isis.viewer.scimpi.dispatcher.view.form.InputForm;


public class ActionForm extends AbstractElementProcessor {

    @Override
    public void process(Request request) {
        CreateFormParameter parameters = new CreateFormParameter();
        parameters.objectId = request.getOptionalProperty(OBJECT);
        parameters.methodName = request.getRequiredProperty(METHOD);
        parameters.forwardResultTo = request.getOptionalProperty(VIEW);
        parameters.forwardVoidTo = request.getOptionalProperty(VOID);
        parameters.forwardErrorTo = request.getOptionalProperty(ERRORS);
        parameters.buttonTitle = request.getOptionalProperty(BUTTON_TITLE, "OK");
        parameters.formTitle = request.getOptionalProperty(FORM_TITLE);
        parameters.resultName = request.getOptionalProperty(RESULT_NAME);
        parameters.resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        parameters.scope = request.getOptionalProperty(SCOPE);
        parameters.className = request.getOptionalProperty(CLASS, "action");
        parameters.showMessage = request.isRequested("show-message", false);
        parameters.id = request.getOptionalProperty(ID);
        createForm(request, parameters);
    }
    
    public static void createForm(Request request, CreateFormParameter parameterObject) {
        createForm(request, parameterObject, false);
    }
    
    protected static void createForm(Request request, CreateFormParameter parameterObject, boolean withoutProcessing) {
        RequestContext context = request.getContext();
        ObjectAdapter object = MethodsUtils.findObject(context, parameterObject.objectId);
        String version = request.getContext().mapVersion(object);
        ObjectAction action = MethodsUtils.findAction(object, parameterObject.methodName);
        // TODO how do we distinguish between overloaded methods?

        /*
        if (action.getParameterCount() == 0) {
            throw new ScimpiException("Action form can only be used for actions with parameters");
        }*/
        if (parameterObject.showMessage && MethodsUtils.isVisible(object, action)) {
            String notUsable = MethodsUtils.isUsable(object, action);
            if (notUsable != null) {
                if (!withoutProcessing) {
                    request.skipUntilClose();
                }  
                request.appendHtml("<div class=\"" + parameterObject.className + "-message\" >" + notUsable + "</div>");
                return;
            }
        }
        if (!MethodsUtils.isVisibleAndUsable(object, action)) {
            if (!withoutProcessing) {
                request.skipUntilClose();
            }
            return;
        }
        String objectId = context.mapObject(object, Scope.INTERACTION);
        String errorView = context.fullFilePath(parameterObject.forwardErrorTo == null ? context.getResourceFile()
                : parameterObject.forwardErrorTo);
        String voidView = context.fullFilePath(parameterObject.forwardVoidTo == null ? context.getResourceFile()
                : parameterObject.forwardVoidTo);
        HiddenInputField[] hiddenFields = new HiddenInputField[] {
                new HiddenInputField(OBJECT, objectId),
                new HiddenInputField(VERSION, version),
                new HiddenInputField(METHOD, parameterObject.methodName),
                parameterObject.forwardResultTo == null ? null : new HiddenInputField(VIEW, context
                        .fullFilePath(parameterObject.forwardResultTo)),
                new HiddenInputField(VOID, voidView),
                new HiddenInputField(ERRORS, errorView),
                parameterObject.scope == null ? null : new HiddenInputField(SCOPE, parameterObject.scope),
                parameterObject.resultOverride == null ? null : new HiddenInputField(RESULT_OVERRIDE, parameterObject.resultOverride),
                parameterObject.resultName == null ? null : new HiddenInputField(RESULT_NAME, parameterObject.resultName),
                parameterObject.resultName == null ? null : new HiddenInputField(RequestContext.RESULT, (String) request
                        .getContext().getVariable(RequestContext.RESULT)) };

        // TODO when the block contains a selector tag it doesn't disable it if the field cannot be edited!!!
        EditFieldBlock containedBlock = new EditFieldBlock();
        request.setBlockContent(containedBlock);
        if (!withoutProcessing) {
            request.processUtilCloseTag();
        }

        FormState entryState = (FormState) context.getVariable(ENTRY_FIELDS);

        // TODO the list of included fields should be considered in the next method (see EditObject)
        InputField[] formFields = createFields(action, object);
        containedBlock.hideExcludedParameters(formFields);
        containedBlock.setUpValues(formFields);
        initializeFields(context, object, action, formFields);
        setDefaults(context, object, action, formFields, entryState);
        if (entryState != null && entryState.isForForm(objectId + ":" + parameterObject.methodName)) {
            copyEntryState(context, object, action, formFields, entryState);
        }
        overrideWithHtml(context, containedBlock, formFields);

        String formTitle;
        if (parameterObject.formTitle == null) {
            formTitle = action.getName();
        } else {
            formTitle = parameterObject.formTitle;
        }

        InputForm.createForm(request, ActionAction.ACTION + ".app", parameterObject.buttonTitle, formFields, hiddenFields,
                formTitle, action.getDescription(), action.getHelp(), parameterObject.className, parameterObject.id);

        request.popBlockContent();
    }

    private static InputField[] createFields(ObjectAction action, ObjectAdapter object) {
        int parameterCount = action.getParameterCount();
        InputField[] fields = new InputField[parameterCount];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new InputField(ActionAction.parameterName(i));
        }
        return fields;
    }

    private static void initializeFields(RequestContext context, ObjectAdapter object, ObjectAction action, InputField[] fields) {
        List<ObjectActionParameter> parameters = action.getParameters();
        for (int i = 0; i < fields.length; i++) {
            InputField field = fields[i];
            ObjectActionParameter param = parameters.get(i);
            
            ObjectAdapter[] optionsForParameter = action.getChoices(object)[i];
            FieldFactory.initializeField(context, object, param, optionsForParameter, !param.isOptional(), true, field);
            
            
/*            
            field.setLabel(param.getName());
            field.setDescription(param.getDescription());
            field.setRequired(!param.isOptional());

            if (param.getSpecification().getFacet(ParseableFacet.class) != null) {
                final int maxLength = param.getFacet(MaxLengthFacet.class).value();
                field.setMaxLength(maxLength);

                TypicalLengthFacet typicalLengthFacet = param.getFacet(TypicalLengthFacet.class);
                if (typicalLengthFacet.isDerived() && maxLength > 0) {
                    field.setWidth(maxLength);
                } else {
                    field.setWidth(typicalLengthFacet.value());
                }

                MultiLineFacet multiLineFacet = param.getFacet(MultiLineFacet.class);
                field.setHeight(multiLineFacet.numberOfLines());
                field.setWrapped(!multiLineFacet.preventWrapping());

                // TODO figure out a better way to determine if boolean or a password
                ObjectSpecification spec = param.getSpecification();
                if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class))
                        || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class.getName()))) {
                    field.setType(InputField.CHECKBOX);
                } else if (spec.getFullName().endsWith(".Password")) {
                    field.setType(InputField.PASSWORD);
                } else {
                    field.setType(InputField.TEXT);
                }

            } else {
                field.setType(InputField.REFERENCE);
            }

            ObjectAdapter[] optionsForParameter = action.getChoices(object)[i];
            if (optionsForParameter != null) {
                int noOptions = optionsForParameter.length;
                String[] optionValues = new String[noOptions];
                String[] optionTitles = new String[noOptions];
                for (int j = 0; j < noOptions; j++) {
                    optionValues[j] = getValue(context, optionsForParameter[j]);
                    optionTitles[j] = optionsForParameter[j].titleString();
                }
                fields[i].setOptions(optionTitles, optionValues);
            }
 */
        }
    }
/*
    private static String getValue(RequestContext context, ObjectAdapter field) {
        if (field == null) {
            return "";
        }
        if (field.getSpecification().getFacet(ParseableFacet.class) == null) {
            return context.mapObject(field, Scope.INTERACTION);
        } else {
            return field.titleString();
        }
    }
*/
    
    
    
    /**
     * Sets up the fields with their initial values
     */
    private static void setDefaults(
            RequestContext context,
            ObjectAdapter object,
            ObjectAction action,
            InputField[] fields,
            FormState entryState) {
        ObjectAdapter[] defaultValues = action.getDefaults(object);
        if (defaultValues == null) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            InputField field = fields[i];
            ObjectAdapter defaultValue = defaultValues[i];

            String title = defaultValue == null ? "" : defaultValue.titleString();
            if (field.getType() == InputField.REFERENCE) {
                ObjectSpecification objectSpecification = action.getParameters().get(i).getSpecification();
                if (defaultValue != null) {
                    String html = "<img class=\"small-icon\" src=\"" + context.imagePath(objectSpecification) + "\" alt=\""
                            + objectSpecification.getShortIdentifier() + "\"/>" + title;
                    String value = context.mapObject(defaultValue, Scope.INTERACTION);
                    field.setValue(value);
                    field.setHtml(html);
                    /*
                     * } else { html = "<em>none specified</em>"; value = null;
                     * field.setType(InputField.HTML);
                     */}

            } else {
                field.setValue(title);
            }

        }

    }

    private static void copyEntryState(
            RequestContext context,
            ObjectAdapter object,
            ObjectAction action,
            InputField[] fields,
            FormState entryState) {

        for (int i = 0; i < fields.length; i++) {
            InputField field = fields[i];
            FieldEditState fieldState = entryState.getField(field.getName());
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

    private static void overrideWithHtml(RequestContext context, EditFieldBlock containedBlock, InputField[] formFields) {
        for (int i = 0; i < formFields.length; i++) {
            String id = ActionAction.parameterName(i);
            if (containedBlock.hasContent(id)) {
                String content = containedBlock.getContent(id);
                if (content != null) {
                    formFields[i].setHtml(content);
                    //formFields[i].setValue(null);
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

