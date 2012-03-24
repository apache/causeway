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

package org.apache.isis.viewer.html.task;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanValueFacet;
import org.apache.isis.viewer.html.action.Action;
import org.apache.isis.viewer.html.action.ActionException;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.Form;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.component.ViewPane;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.Request;

public final class TaskStep implements Action {
    private void addSelector(final Context context, final Form form, final String currentEntry, final String fieldId, final String fieldLabel, final String fieldDescription, final boolean required, final String errorMessage, final Task task, final ObjectAdapter[] objects) {
        task.checkInstances(context, objects);

        int size = 0;
        for (final ObjectAdapter object : objects) {
            if (object != null) {
                size++;
            }
        }

        final String[] instances = new String[size];
        final String[] ids = new String[size];
        int selectedIndex = -1;

        for (int i = 0, j = 0; i < objects.length; i++) {
            final ObjectAdapter element = objects[i];
            if (element != null) {
                instances[j] = element.titleString();
                ids[j] = context.mapObject(element);
                if (ids[j].equals(currentEntry)) {
                    selectedIndex = i;
                }
                j++;
            }
        }
        form.addLookup(fieldLabel, fieldDescription, fieldId, selectedIndex, instances, ids, required, errorMessage);
    }

    private void addSelectorForKnownReferences(final Context context, final Form form, final ObjectSpecification type, final String currentEntry, final String fieldId, final String fieldLabel, final String fieldDescription, final boolean required, final String errorMessage, final Task task) {

        final ObjectAdapter[] objects = context.getKnownInstances(type);
        addSelector(context, form, currentEntry, fieldId, fieldLabel, fieldDescription, required, errorMessage, task, objects);
    }

    private void addSelectorForObjectOptions(final Context context, final Form form, final String currentEntry, final String fieldId, final String fieldLabel, final String fieldDescription, final ObjectAdapter[] options, final boolean required, final String errorMessage, final Task task) {
        final ObjectAdapter[] objects = new ObjectAdapter[options.length];
        for (int i = 0; i < options.length; i++) {
            objects[i] = options[i];
        }
        addSelector(context, form, currentEntry, fieldId, fieldLabel, fieldDescription, required, errorMessage, task, objects);
    }

    private void addSelectorForValueOptions(final Form form, final String currentEntry, final String fieldId, final String fieldLabel, final String fieldDescription, final ObjectAdapter[] options, final boolean required, final String errorMessage, final Task task) {
        int selectedIndex = -1;
        final String[] instances = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            instances[i] = options[i].titleString();
            if (currentEntry.equals(instances[i])) {
                selectedIndex = i;
            }
        }
        form.addLookup(fieldLabel, fieldDescription, fieldId, selectedIndex, instances, instances, required, errorMessage);
    }

    private void addTextFieldForParseable(final Form form, final ObjectSpecification type, final String currentEntryText, final String fieldId, final String fieldLabel, final String fieldDescription, final int noLines, final boolean wrap, final int maxLength, final int typicalLength,
            final boolean required, final String errorMessage) {
        form.addField(type, fieldLabel, fieldDescription, fieldId, currentEntryText, noLines, wrap, maxLength, typicalLength, required, errorMessage);
    }

    private void displayTask(final Context context, final Page page, final Task task) {
        page.setTitle(task.getName());

        final ViewPane content = page.getViewPane();
        final ObjectAdapter targetAdapter = task.getTarget(context);
        String titleString = targetAdapter.titleString();
        if (targetAdapter.isTransient()) {
            titleString += " (Unsaved)";
        }
        content.setTitle(titleString, targetAdapter.getSpecification().getDescription());
        String iconName = targetAdapter.getIconName();
        if (iconName == null) {
            iconName = targetAdapter.getSpecification().getShortIdentifier();
        }
        content.setIconName(iconName);

        final StringBuffer crumbs = new StringBuffer();
        final String[] trail = task.getTrail();
        for (final String element : trail) {
            crumbs.append(" : ");
            crumbs.append(element);
        }

        final Component[] action = new Component[1];
        action[0] = context.getComponentFactory().createInlineBlock("name", task.getName(), task.getDescription());
        content.setMenu(action);

        if (task.getError() != null) {
            content.add(context.getComponentFactory().createInlineBlock("error", task.getError(), null));
        }

        final Form form = context.getComponentFactory().createForm(task.getId(), name(), task.getStep(), task.numberOfSteps(), task.isEditing());
        final String[] parameterLabels = task.getNames();
        final String[] parameterDescriptions = task.getFieldDescriptions();
        final String[] errors = task.getErrors();
        final String[] entryText = task.getEntryText();
        final int[] noLines = task.getNoLines();
        final boolean[] canWrap = task.getWraps();
        final int[] maxLength = task.getMaxLength();
        final int[] typicalLength = task.getTypicalLength();
        final ObjectAdapter[][] options = task.getOptions(context);
        final boolean[] optional = task.getOptional();
        final boolean[] readOnly = task.getReadOnly();
        final ObjectSpecification[] types = task.getTypes();
        for (int i = 0; i < parameterLabels.length; i++) {
            final ObjectSpecification paramSpec = types[i];
            final String fieldId = "fld" + i;
            final String fieldLabel = parameterLabels[i] == null ? "" : parameterLabels[i];
            ;
            final String fieldDescription = parameterDescriptions[i] == null ? "" : parameterDescriptions[i];
            final String currentEntryTitle = entryText[i];
            final String error = errors[i];
            if (readOnly[i]) {
                addReadOnlyField(form, paramSpec, fieldLabel, fieldDescription, currentEntryTitle);
            } else if (paramSpec.isParseable() && options[i] != null && options[i].length > 0) {
                addSelectorForValueOptions(form, currentEntryTitle, fieldId, fieldLabel, fieldDescription, options[i], !optional[i], error, task);
            } else if (paramSpec.isParseable()) {
                addTextFieldForParseable(form, paramSpec, currentEntryTitle, fieldId, fieldLabel, fieldDescription, noLines[i], canWrap[i], maxLength[i], typicalLength[i], !optional[i], error);
            } else if (paramSpec.isNotCollection() && options[i] != null && options[i].length > 0) {
                addSelectorForObjectOptions(context, form, currentEntryTitle, fieldId, fieldLabel, fieldDescription, options[i], !optional[i], error, task);
            } else if (paramSpec.isNotCollection()) {
                addSelectorForKnownReferences(context, form, paramSpec, currentEntryTitle, fieldId, fieldLabel, fieldDescription, !optional[i], error, task);
            } else {
                throw new IsisException();
            }
        }
        content.add(form);
    }

    private void addReadOnlyField(final Form form, final ObjectSpecification paramSpec, final String fieldLabel, final String fieldDescription, final String currentEntryTitle) {
        if (paramSpec.containsFacet(BooleanValueFacet.class)) {
            final boolean isSet = Boolean.parseBoolean(currentEntryTitle);
            form.addReadOnlyCheckbox(fieldLabel, isSet, fieldDescription);
        } else {
            form.addReadOnlyField(fieldLabel, currentEntryTitle, fieldDescription);
        }
    }

    @Override
    public void execute(final Request request, final Context context, final Page page) {
        final String taskId = request.getTaskId();
        final Task task = context.getTask(taskId);
        final String button = request.getButtonName();
        if (task == null && !"Cancel".equals(button)) {
            throw new TaskLookupException("No task found with id " + taskId);
        }

        if (button == null) {
            // start new task
            displayTask(context, page, task);
        } else if ("Cancel".equals(button)) {
            forwardCancel(request, context, task);
        } else if ("Previous".equals(button)) {
            task.setFromFields(request, context);
            task.previousStep();
            displayTask(context, page, task);
        } else if ("Next".equals(button)) {
            task.setFromFields(request, context);
            task.nextStep();
            displayTask(context, page, task);
        } else if ("Finish".equals(button) || "Save".equals(button) || "Ok".equals(button)) {
            task.setFromFields(request, context);
            task.checkForValidity(context);

            if (hasErrors(task)) {
                displayTask(context, page, task);
            } else {
                final String targetId = context.mapObject(task.getTarget(context));
                final ObjectAdapter result = task.completeTask(context, page);
                if (result instanceof ObjectAdapter) {
                    final ObjectAdapter object = result;
                    context.updateVersion(object);
                }
                InvokeMethod.displayMethodResult(request, context, page, result, targetId);
                context.endTask(task);
            }
        } else {
            throw new ActionException("No task action: " + button);
        }
    }

    private void forwardCancel(final Request request, final Context context, final Task task) {
        final Request cancelTask = context.cancelTask(task);
        request.forward(cancelTask);
    }

    private boolean hasErrors(final Task task) {
        if (task.getError() != null) {
            return true;
        }
        final String[] errors = task.getErrors();
        for (final String error : errors) {
            if (error != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return Request.TASK_COMMAND;
    }
}
