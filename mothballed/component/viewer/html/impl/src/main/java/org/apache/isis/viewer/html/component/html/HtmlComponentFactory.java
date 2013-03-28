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

package org.apache.isis.viewer.html.component.html;

import static org.apache.isis.viewer.html.HtmlViewerConstants.FOOTER;
import static org.apache.isis.viewer.html.HtmlViewerConstants.FOOTER_FILE;
import static org.apache.isis.viewer.html.HtmlViewerConstants.HEADER;
import static org.apache.isis.viewer.html.HtmlViewerConstants.HEADER_FILE;
import static org.apache.isis.viewer.html.HtmlViewerConstants.STYLE_SHEET;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanValueFacet;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentFactory;
import org.apache.isis.viewer.html.component.DebugPane;
import org.apache.isis.viewer.html.component.Form;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.component.Table;

public class HtmlComponentFactory implements ComponentFactory {

    private static final long serialVersionUID = 1L;
    
    protected final String footer;
    protected final String header;
    protected final String styleSheet;
    
    private final PathBuilder pathBuilder;

    public HtmlComponentFactory(final PathBuilder pathBuilder) {
        this(pathBuilder, getConfiguration());
    }

    public HtmlComponentFactory(final PathBuilder pathBuilder, final IsisConfiguration configuration) {
        this.pathBuilder = pathBuilder;
        this.styleSheet = configuration.getString(STYLE_SHEET);
        this.header = loadFileElseDefault(configuration, HEADER_FILE, HEADER);
        this.footer = loadFileElseDefault(configuration, FOOTER_FILE, FOOTER);
    }

    
    // /////////////////////////////////////////////////////////////
    // Pages
    // /////////////////////////////////////////////////////////////

    @Override
    public Page createPage() {
        return new DynamicHtmlPage(this, styleSheet, header, footer);
    }

    public LogonFormPage createLogonPage(final String user, final String password, final boolean registerLink, final String error) {
        return new LogonFormPage(this, styleSheet, header, footer, user, password, registerLink, error);
    }

    public RegisterFormPage createRegisterPage(final String user, final String password, final String error) {
        return new RegisterFormPage(this, styleSheet, header, footer, user, password, error);
    }

    // /////////////////////////////////////////////////////////////
    // Menus
    // /////////////////////////////////////////////////////////////

    @Override
    public Component createMenuItem(final String actionId, final String name, final String description, final String reasonDisabled, final ActionType type, final boolean hasParameters, final String targetObjectId) {
        return new MenuItem(this, actionId, name, description, reasonDisabled, type, hasParameters, targetObjectId);
    }

    @Override
    public Component createSubmenu(final String menuName, final Component[] items) {
        return new Submenu(menuName, items);
    }


    // /////////////////////////////////////////////////////////////
    // Icons
    // /////////////////////////////////////////////////////////////

    @Override
    public Component createCollectionIcon(final ObjectAssociation field, final ObjectAdapter collection, final String id) {
        return new CollectionLink(this, field, collection, field.getDescription(), id);
    }

    @Override
    public Component createCollectionIcon(final ObjectAdapter collection, final String collectionId) {
        return new CollectionIcon(this, collection, collection.getSpecification().getDescription(), collectionId);
    }

    @Override
    public Component createObjectIcon(final ObjectAdapter object, final String objectId, final String style) {
        return new ObjectIcon(this, object, object.getSpecification().getDescription(), objectId, style);
    }

    @Override
    public Component createObjectIcon(final ObjectAssociation field, final ObjectAdapter object, final String objectId, final String style) {
        return new ObjectIcon(this, object, field.getDescription(), objectId, style);
    }


    // /////////////////////////////////////////////////////////////
    // Options
    // /////////////////////////////////////////////////////////////

    @Override
    public Component createEditOption(final String id) {
        return new ActionComponent(this, "edit", "Edit Object", "Edit the current object", id, null, null);
    }

    @Override
    public Component createRemoveOption(final String id, final String elementId, final String fieldName) {
        return new ActionComponent(this, "remove", "Remove", "Remove item from collection", id, elementId, fieldName);
    }

    @Override
    public Component createAddOption(final String id, final String fieldName) {
        return new ActionComponent(this, "add", "Add Item", "Add item to collection", id, null, fieldName);
    }


    // /////////////////////////////////////////////////////////////
    // Messages
    // /////////////////////////////////////////////////////////////

    @Override
    public Component createErrorMessage(final Exception e, final boolean isDebug) {
        return new ErrorMessage(e, isDebug);
    }


    // /////////////////////////////////////////////////////////////
    // Form & Form Widgets
    // /////////////////////////////////////////////////////////////

    @Override
    public Form createForm(final String id, final String actionName, final int step, final int noOfPages, final boolean isEditing) {
        return new HtmlForm(this, id, actionName, step, noOfPages, isEditing);
    }

    @Override
    public Component createCheckboxBlock(final boolean isEditable, final boolean isSet) {
        return new Checkbox(isSet, isEditable);
    }

    @Override
    public Component createLink(final String link, final String name, final String description) {
        return new Link(this, link, name, description);
    }


    @Override
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

    @Override
    public Table createTable(final int noColumns, final boolean withSelectorColumn) {
        return new HtmlTable(this, noColumns, withSelectorColumn);
    }


    // /////////////////////////////////////////////////////////////
    // Furniture
    // /////////////////////////////////////////////////////////////

    @Override
    public Block createBlock(final String style, final String description) {
        return new Div(this, style, description);
    }

    @Override
    public Component createBreadCrumbs(final String[] names, final boolean[] isLinked) {
        return new BreadCrumbs(this, names, isLinked);
    }

    @Override
    public Component createInlineBlock(final String style, final String text, final String description) {
        return new Span(style, text, description);
    }

    @Override
    public Component createHeading(final String name) {
        return new Heading(this, name, 4);
    }

    @Override
    public Component createService(final String objectId, final String title, final String iconName) {
        return new ServiceComponent(this, objectId, title, iconName);
    }

    @Override
    public Component createUserSwap(final String name) {
        return new UserSwapLink(this, name);

    }

    // /////////////////////////////////////////////////////////////
    // Debug
    // /////////////////////////////////////////////////////////////

    @Override
    public DebugPane createDebugPane() {
        return new HtmlDebug();
    }


    // /////////////////////////////////////////////////////////////
    // PathBuilder impl
    // /////////////////////////////////////////////////////////////

    @Override
    public String getSuffix() {
        return pathBuilder.getSuffix();
    }

    @Override
    public String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

    
    // /////////////////////////////////////////////////////////////
    // helpers
    // /////////////////////////////////////////////////////////////

    private static String loadFileElseDefault(final IsisConfiguration configuration, final String fileConstant, final String literalConstant) {
        final String fileName = configuration.getString(fileConstant);
        return fileName != null ? loadFile(fileName) : configuration.getString(literalConstant);
    }


    private static String loadFile(final String file) {
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
                } catch (final IOException ignore) {
                }
            }
        }
        return content.toString();
    }

    

    // /////////////////////////////////////////////////////////////
    // injected services
    // /////////////////////////////////////////////////////////////

    private static IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

}
