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


package org.apache.isis.viewer.dnd.view.action;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ParseableEntryActionParameter;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.content.AbstractTextParsableContent;


public class TextParseableParameterImpl extends AbstractTextParsableContent implements TextParseableParameter {
    private ObjectAdapter object;
    private final ObjectAdapter[] options;
    private final ParseableEntryActionParameter parameter;
    private final ActionHelper invocation;
    private final int index;

    public TextParseableParameterImpl(
            final ParseableEntryActionParameter objectActionParameters,
            final ObjectAdapter adapter,
            final ObjectAdapter[] options,
            final int i,
            final ActionHelper invocation) {
        this.parameter = objectActionParameters;
        this.options = options;
        index = i;
        this.invocation = invocation;
        object = adapter;
    }

    public void debugDetails(final DebugBuilder debug) {
        debug.appendln("name", parameter.getName());
        debug.appendln("required", isRequired());
        debug.appendln("object", object);
    }

    @Override
    public void entryComplete() {}

    public String getIconName() {
        return "";
    }

    @Override
    public Image getIconPicture(final int iconHeight) {
        return ImageFactory.getInstance().loadIcon("value", 12, null);
    }

    public ObjectAdapter getAdapter() {
        return object;
    }

    public int getNoLines() {
        return parameter.getNoLines();
    }

    public ObjectAdapter[] getOptions() {
        return options;
    }

    @Override
    public boolean isEmpty() {
        return object == null;
    }

    public boolean isRequired() {
        return !parameter.isOptional();
    }

    public Consent canClear() {
        return Allow.DEFAULT;
    }

    public boolean canWrap() {
        return parameter.canWrap();
    }

    @Override
    public void clear() {
        object = null;
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public boolean isTextParseable() {
        return true;
    }

    public boolean isOptionEnabled() {
        return options != null && options.length > 0;
    }

    public String title() {
        return AdapterUtils.titleString(object);
    }

    @Override
    public String toString() {
        final ToString toString = new ToString(this);
        toString.append("object", object);
        return toString.toString();
    }

    public String getParameterName() {
        return parameter.getName();
    }

    public ObjectSpecification getSpecification() {
        return parameter.getSpecification();
    }

    public ObjectAdapter drop(final Content sourceContent) {
        return null;
    }

    public Consent canDrop(final Content sourceContent) {
        return Veto.DEFAULT;
    }

    public String titleString(final ObjectAdapter value) {
        return titleString(value, parameter, parameter.getSpecification());
    }

    /**
     * @throws InvalidEntryException -
     *             turns the parameter red if invalid.
     */
    @Override
    public void parseTextEntry(final String entryText) {
        object = parse(entryText);
        final String reason = parameter.isValid(object, AdapterUtils.unwrap(object));
        if (reason != null) {
            throw new InvalidEntryException(reason);
        } else if (!parameter.isOptional() && object == null) {
            throw new InvalidEntryException("Mandatory parameter cannot be empty");
        }
        invocation.setParameter(index, object);
    }

    private ObjectAdapter parse(final String entryText) {
        final ObjectSpecification parameterSpecification = parameter.getSpecification();
        final ParseableFacet p = parameterSpecification.getFacet(ParseableFacet.class);
        try {
        	return p.parseTextEntry(object, entryText);
        } catch(IllegalArgumentException ex) {
        	throw new InvalidEntryException(ex.getMessage(), ex);
        }
    }

    public String getDescription() {
        final String title = object == null ? "" : ": " + object.titleString();
        final String specification = getSpecification().getShortIdentifier();
        final String type = getParameterName().indexOf(specification) == -1 ? "" : " (" + specification + ")";
        return getParameterName() + type + title + " " + parameter.getDescription();
    }

    public String getHelp() {
        return null;
    }

    public String getId() {
        return null;
    }

    @Override
    public Consent isEditable() {
        return Allow.DEFAULT;
    }

    public int getMaximumLength() {
        return parameter.getMaximumLength();
    }

    public int getTypicalLineLength() {
        return parameter.getTypicalLineLength();
    }
}
