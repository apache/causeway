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

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.bounded.BoundedFacetUtils;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.progmodel.facets.value.password.PasswordValueFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.Request;

/**
 * Represents a task that the user is working through. Is used for both editing
 * objects and setting up parameters for an action method.
 */
public abstract class Task {

    private static int nextID = 1;
    private int[] boundaries;
    private final String description;
    protected final String[] errors;
    protected String error;
    private final String[] entryText;
    protected final ObjectAdapter[] initialState;
    private final String name;
    protected final String[] names;
    protected final String[] descriptions;
    protected final boolean[] optional;
    protected final boolean[] readOnly;
    protected final int numberOfEntries;
    private int step;
    private final String targetId;
    protected final ObjectSpecification[] fieldSpecifications;
    protected final int[] noLines;
    protected final boolean[] wraps;
    protected final int[] maxLength;
    protected final int[] typicalLength;
    protected final int id = nextID++;

    public Task(final Context context, final String name, final String description, final ObjectAdapter target, final int noFields) {
        this.name = name;
        this.description = description;
        targetId = context.mapObject(target);

        initialState = new ObjectAdapter[noFields];
        names = new String[noFields];
        descriptions = new String[noFields];
        optional = new boolean[noFields];
        readOnly = new boolean[noFields];
        fieldSpecifications = new ObjectSpecification[noFields];

        numberOfEntries = noFields;
        entryText = new String[noFields];
        errors = new String[noFields];

        noLines = new int[noFields];
        wraps = new boolean[noFields];
        maxLength = new int[noFields];
        typicalLength = new int[noFields];
    }

    public void init(final Context context) {
        for (int i = 0; i < entryText.length; i++) {
            final ObjectAdapter obj = initialState[i];
            if (obj == null) {
                entryText[i] = "";
            } else if (obj.getSpecification().getFacet(PasswordValueFacet.class) != null) {
                final PasswordValueFacet facet = obj.getSpecification().getFacet(PasswordValueFacet.class);
                entryText[i] = facet.getEditText(obj);
            } else if (obj.getSpecification().isParseable()) {
                entryText[i] = obj.titleString();
            } else if (obj.getSpecification().isNotCollection()) {
                if (readOnly[i]) {
                    entryText[i] = (obj).titleString();
                } else {
                    entryText[i] = context.mapObject(obj);
                }
            } else if (obj.getSpecification().isParentedOrFreeCollection()) {
                entryText[i] = (obj).titleString();
            }
        }

        divyUpWork();
    }

    public abstract ObjectAdapter completeTask(Context context, Page page);

    private void copyForThisStep(final Object[] source, final Object[] destination) {
        for (int i = 0; i < noOfEntriesInThisStep(); i++) {
            destination[i] = source[firstEntryInThisStep() + i];
        }
    }

    private void copyForThisStep(final boolean[] source, final boolean[] destination) {
        for (int i = 0; i < noOfEntriesInThisStep(); i++) {
            destination[i] = source[firstEntryInThisStep() + i];
        }
    }

    private void copyForThisStep(final int[] source, final int[] destination) {
        for (int i = 0; i < noOfEntriesInThisStep(); i++) {
            destination[i] = source[firstEntryInThisStep() + i];
        }
    }

    public void checkInstances(final Context context, final ObjectAdapter[] objects) {
    }

    public void debug(final DebugBuilder debug) {
        debug.indent();
        debug.appendln("name", name);
        debug.appendln("number of steps ", numberOfSteps());
        debug.appendln("current step", step);
        debug.appendln("target", targetId);
        debug.appendln("steps (" + (boundaries.length - 1) + ")");
        debug.indent();
        for (int i = 0; i < boundaries.length - 1; i++) {
            debug.appendln("    " + (i + 1) + ". " + boundaries[i] + " - " + (boundaries[i + 1] - 1));
        }
        debug.unindent();
        debug.appendln("fields (" + names.length + ")");
        debug.indent();
        for (int i = 0; i < names.length; i++) {
            final String status = (readOnly[i] ? "R" : "-") + (optional[i] ? "O" : "M") + (errors[i] == null ? "-" : "E");
            debug.appendln("    " + i + "  " + names[i] + " (" + status + "):  " + fieldSpecifications[i].getFullIdentifier() + " -> " + entryText[i]);
        }
        debug.unindent();
        debug.unindent();
    }

    private void divyUpWork() {
        if (numberOfEntries == 0) {
            boundaries = new int[2];
        } else {
            final int[] b = new int[numberOfEntries + 2];
            int count = 0;
            b[count++] = 0;

            ObjectSpecification type = fieldSpecifications[0];
            boolean direct = simpleField(type, 0);

            for (int i = 1; i < numberOfEntries; i++) {
                type = fieldSpecifications[i];
                if (true || direct && (simpleField(type, i))) {
                    continue;
                }
                b[count++] = i;
                direct = simpleField(type, i);
            }
            b[count++] = numberOfEntries;
            boundaries = new int[count];
            System.arraycopy(b, 0, boundaries, 0, count);
        }
    }

    protected boolean simpleField(final ObjectSpecification specification, final int i) {
        return readOnly[i] || (specification.isNotCollection() && BoundedFacetUtils.isBoundedSet(specification));
    }

    private int firstEntryInThisStep() {
        return boundaries[step];
    }

    public String getDescription() {
        return description;
    }

    public String getError() {
        return error;
    }

    /**
     * Returns an array of errors, one for each element in the task.
     */
    public String[] getErrors() {
        final String[] array = new String[noOfEntriesInThisStep()];
        copyForThisStep(errors, array);
        return array;
    }

    public String[] getFieldDescriptions() {
        final String[] array = new String[noOfEntriesInThisStep()];
        copyForThisStep(descriptions, array);
        return array;
    }

    public String[] getEntryText() {
        final String[] array = new String[noOfEntriesInThisStep()];
        copyForThisStep(entryText, array);
        return array;
    }

    public String getName() {
        return name;
    }

    public String[] getNames() {
        final String[] array = new String[noOfEntriesInThisStep()];
        copyForThisStep(names, array);
        return array;
    }

    public ObjectAdapter[][] getOptions(final Context context) {
        return getOptions(context, firstEntryInThisStep(), noOfEntriesInThisStep());
    }

    protected ObjectAdapter[][] getOptions(final Context context, final int from, final int len) {
        return new ObjectAdapter[len][];
    }

    protected ObjectAdapter[] getEntries(final Context context) {
        final ObjectAdapter[] entries = new ObjectAdapter[entryText.length];
        for (int i = 0; i < entries.length; i++) {
            if (entryText == null || readOnly[i]) {
                continue;
            }
            final ObjectSpecification fieldSpecification = fieldSpecifications[i];
            if (fieldSpecification.isParseable()) {
                final ParseableFacet parser = fieldSpecification.getFacet(ParseableFacet.class);
                try {
                    Localization localization = IsisContext.getLocalization(); 
                    entries[i] = parser.parseTextEntry(initialState[i], entryText[i], localization);
                } catch (final InvalidEntryException e) {
                    errors[i] = e.getMessage();
                } catch (final TextEntryParseException e) {
                    errors[i] = e.getMessage();
                }
            } else if (fieldSpecification.isNotCollection() && entryText[i] != null) {
                if (entryText[i].equals("null")) {
                    entries[i] = null;
                } else {
                    entries[i] = context.getMappedObject(entryText[i]);
                }
            }
        }
        return entries;
    }

    public String getId() {
        return "" + id;
    }

    public boolean[] getOptional() {
        final boolean[] array = new boolean[noOfEntriesInThisStep()];
        copyForThisStep(optional, array);
        return array;
    }

    public int[] getNoLines() {
        final int[] array = new int[noOfEntriesInThisStep()];
        copyForThisStep(noLines, array);
        return array;
    }

    public boolean[] getWraps() {
        final boolean[] array = new boolean[noOfEntriesInThisStep()];
        copyForThisStep(wraps, array);
        return array;
    }

    public int[] getMaxLength() {
        final int[] array = new int[noOfEntriesInThisStep()];
        copyForThisStep(maxLength, array);
        return array;
    }

    public int[] getTypicalLength() {
        final int[] array = new int[noOfEntriesInThisStep()];
        copyForThisStep(typicalLength, array);
        return array;
    }

    public boolean[] getReadOnly() {
        final boolean[] array = new boolean[noOfEntriesInThisStep()];
        copyForThisStep(readOnly, array);
        return array;
    }

    public int getStep() {
        return step;
    }

    public ObjectAdapter getTarget(final Context context) {
        return context.getMappedObject(targetId);
    }

    public String[] getTrail() {
        final String[] trail = new String[boundaries.length - 1];
        for (int i = 0; i < trail.length; i++) {
            trail[i] = "step " + i;
        }
        return trail;
    }

    public ObjectSpecification[] getTypes() {
        final ObjectSpecification[] array = new ObjectSpecification[noOfEntriesInThisStep()];
        copyForThisStep(fieldSpecifications, array);
        return array;
    }

    public boolean isEditing() {
        return false;
    }

    public void nextStep() {
        step++;
    }

    private int noOfEntriesInThisStep() {
        return boundaries[step + 1] - boundaries[step];
    }

    public int numberOfSteps() {
        return boundaries.length - 1;
    }

    public void previousStep() {
        step--;
    }

    public void setFromFields(final Request request, final Context context) {
        int fldNo = 0;
        for (int i = boundaries[step]; i < boundaries[step + 1]; i++) {
            String textEntry = request.getFieldEntry(fldNo++);
            if (readOnly[i]) {
                continue;
            }
            final ObjectSpecification spec = fieldSpecifications[i];
            // deal with check boxes specially: expect 'true' if checked and no
            // entry if not checked, hence
            // need to set as 'false'
            if (spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(boolean.class)) || spec.isOfType(IsisContext.getSpecificationLoader().loadSpecification(Boolean.class))) {
                if (textEntry == null || !textEntry.equals("true")) {
                    textEntry = "false";
                }
            }
            entryText[i] = textEntry;
            try {
                errors[i] = null;
                setFromField(context, i, spec, textEntry);
                if (!optional[i] && (textEntry == null || textEntry.equals(""))) {
                    errors[i] = "Field required";
                }
            } catch (final InvalidEntryException e) {
                errors[i] = e.getMessage();
            } catch (final TextEntryParseException e) {
                errors[i] = e.getMessage();
            }
        }
    }

    private void setFromField(final Context context, final int i, final ObjectSpecification spec, final String textEntry) {
        if (spec.isParseable()) {
            if (textEntry == null) {
                return;
            }
            // REVIEW this block uses the existing adapter as it contains the
            // regex needed. This needs to
            // be reviewed in line with Dan's proposed changes to the reflector.
            final ObjectAdapter valueAdapter = initialState[i];
            final ParseableFacet parser = spec.getFacet(ParseableFacet.class);
            Localization localization = IsisContext.getLocalization(); 
            parser.parseTextEntry(valueAdapter, textEntry, localization);
            // REVIEW what do we do when an exception is thrown - a parse fails?
        }
    }

    public abstract void checkForValidity(Context context);

}
