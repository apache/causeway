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


package org.apache.isis.extensions.html.component.html;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.value.BooleanValueFacet;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.extensions.html.component.Block;
import org.apache.isis.extensions.html.component.Component;
import org.apache.isis.extensions.html.component.ComponentFactory;
import org.apache.isis.extensions.html.component.DebugPane;
import org.apache.isis.extensions.html.component.Form;
import org.apache.isis.extensions.html.component.Page;
import org.apache.isis.extensions.html.component.Table;
import static org.apache.isis.extensions.html.viewer.HtmlViewerConstants.*;


public class HtmlComponentFactory implements ComponentFactory {

    private final String footer;
    private final String header;
    private final String styleSheet;

    public HtmlComponentFactory() {
        final IsisConfiguration configuration = IsisContext.getConfiguration();
        styleSheet = configuration.getString(STYLE_SHEET);
        String file = configuration.getString(HEADER_FILE);
        header = file == null ? configuration.getString(HEADER) : loadFile(file);
        file = configuration.getString(FOOTER_FILE);
        footer = file == null ? configuration.getString(FOOTER) : loadFile(file);
    }

    public Block createBlock(final String style, final String description) {
        return new Div(style, description);
    }

    public Component createBreadCrumbs(final String[] names, final boolean[] isLinked) {
        return new BreadCrumbs(names, isLinked);
    }

    public Component createCollectionIcon(final ObjectAssociation field, final ObjectAdapter collection, final String id) {
        return new CollectionLink(field, collection, field.getDescription(), id);
    }

    public DebugPane createDebugPane() {
        return new HtmlDebug();
    }

    public Component createEditOption(final String id) {
        return new ActionComponent("edit", "Edit Object", "Edit the current object", id, null, null);
    }

    public Component createRemoveOption(final String id, final String elementId, final String fieldName) {
        return new ActionComponent("remove", "Remove", "Remove item from collection", id, elementId, fieldName);
    }

    public Component createAddOption(final String id, final String fieldName) {
        return new ActionComponent("add", "Add Item", "Add item to collection", id, null, fieldName);
    }

    public Component createErrorMessage(final Exception e, final boolean isDebug) {
        return new ErrorMessage(e, isDebug);
    }

    public Form createForm(final String id, final String actionName, final int step, final int noOfPages, final boolean isEditing) {
        return new HtmlForm(id, actionName, step, noOfPages, isEditing);
    }

    public Component createHeading(final String name) {
        return new Heading(name, 4);
    }

    public Component createInlineBlock(final String style, final String text, final String description) {
        return new Span(style, text, description);
    }

    public Component createCheckboxBlock(final boolean isEditable, final boolean isSet) {
        return new Checkbox(isSet, isEditable);
    }

    public Component createSubmenu(final String menuName, final Component[] items) {
        return new Submenu(menuName, items);
    }

    public Component createLink(final String link, final String name, final String description) {
        return new Link(link, name, description);
    }

    public Component createMenuItem(
            final String actionId,
            final String name,
            final String description,
            final String reasonDisabled,
            final ObjectActionType type,
            final boolean hasParameters,
            final String targetObjectId) {
        return new MenuItem(actionId, name, description, reasonDisabled, type, hasParameters, targetObjectId);
    }

    public Component createCollectionIcon(final ObjectAdapter collection, final String collectionId) {
        return new CollectionIcon(collection, collection.getSpecification().getDescription(), collectionId);
    }

    public Component createObjectIcon(final ObjectAdapter object, final String objectId, final String style) {
        return new ObjectIcon(object, object.getSpecification().getDescription(), objectId, style);
    }

    public Component createObjectIcon(
            final ObjectAssociation field,
            final ObjectAdapter object,
            final String objectId,
            final String style) {
        return new ObjectIcon(object, field.getDescription(), objectId, style);
    }

    public Page createPage() {
        return new DynamicHtmlPage(styleSheet, header, footer);
    }

    public LogonFormPage createLogonPage(final String user, final String password) {
        return new LogonFormPage(styleSheet, header, footer, user, password);
    }

    public Component createService(final String objectId, final String title, final String iconName) {
        return new ServiceComponent(objectId, title, iconName);
    }

    public Table createTable(final int noColumns, final boolean withSelectorColumn) {
        return new HtmlTable(noColumns, withSelectorColumn);
    }

    public Component createUserSwap(final String name) {
        return new UserSwapLink(name);

    }

    public Component createParseableField(final ObjectAssociation field, final ObjectAdapter value, final boolean isEditable) {
        final BooleanValueFacet facet = field.getSpecification().getFacet(BooleanValueFacet.class);
        if (facet != null) {
            return createCheckboxBlock(isEditable, facet.isSet(value));
        } else {
            final String titleString = value != null ? value.titleString() : "";

            final MultiLineFacet multiLineFacet = field.getSpecification().getFacet(MultiLineFacet.class);
            final boolean isWrapped = multiLineFacet != null && !multiLineFacet.preventWrapping();

            if (isWrapped) {
                return createInlineBlock("value", "<pre>" + titleString + "</pre>", null);
            } else {
                return createInlineBlock("value", titleString, null);
            }
        }
    }

    private String loadFile(final String file) {
        final StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
        } catch (final FileNotFoundException e) {
            throw new WebViewerException("Failed to find file " + file);
        } catch (final IOException e) {
            throw new WebViewerException("Failed to load file " + file, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ignore) {}
            }
        }
        return content.toString();
    }
}

