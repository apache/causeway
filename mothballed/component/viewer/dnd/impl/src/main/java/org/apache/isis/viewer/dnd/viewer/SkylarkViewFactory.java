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

package org.apache.isis.viewer.dnd.viewer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.viewer.dnd.dialog.ActionDialogSpecification;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.interaction.ContentDragImpl;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.GlobalViewFactory;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.DragViewOutline;
import org.apache.isis.viewer.dnd.view.border.DisposedObjectBorder;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.viewer.basic.FallbackView;
import org.apache.isis.viewer.dnd.viewer.basic.MinimizedView;

/**
 * This class holds all the different view types that all the different objects
 * can be viewed as.
 */
public class SkylarkViewFactory implements GlobalViewFactory {
    private static final ViewSpecification fallback = new FallbackView.Specification();
    private final ViewSpecification dialogSpec = new ActionDialogSpecification();
    public static final int INTERNAL = 2;
    private static final Logger LOG = LoggerFactory.getLogger(SkylarkViewFactory.class);
    public static final int WINDOW = 1;

    private ViewSpecification emptyFieldSpecification;
    private final Vector rootViews = new Vector();
    private final Vector subviews = new Vector();
    private ViewSpecification dragContentSpecification;

    private final List<ViewSpecification> viewSpecifications = new ArrayList<ViewSpecification>();
    private final List<ViewSpecification> designSpecifications = new ArrayList<ViewSpecification>();

    @Override
    public void addSpecification(final ViewSpecification specification) {
        viewSpecifications.add(specification);
    }

    public void addSpecification(final String specClassName) {
        ViewSpecification spec;
        spec = (ViewSpecification) InstanceUtil.createInstance(specClassName);
        LOG.info("adding view specification: " + spec);
        addSpecification(spec);
    }

    public void addDesignSpecification(final ViewSpecification specification) {
        designSpecifications.add(specification);
    }

    public void addEmptyFieldSpecification(final ViewSpecification spec) {
        emptyFieldSpecification = spec;
    }

    @Override
    public View createDialog(final Content content) {
        return createView(dialogSpec, content);
    }

    private View createView(final ViewSpecification specification, final Content content) {
        ViewSpecification spec;
        if (specification == null) {
            LOG.warn("no suitable view for " + content + " using fallback view");
            spec = new FallbackView.Specification();
        } else {
            spec = specification;
        }
        // TODO this should be passed in so that factory created views can be
        // related to the views that ask
        // for them
        final Axes axes = new Axes();
        View createView = spec.createView(content, axes, -1);

        /*
         * ObjectSpecification contentSpecification =
         * content.getSpecification(); if (contentSpecification != null) {
         * Options viewOptions = Properties.getViewConfigurationOptions(spec);
         * createView.loadOptions(viewOptions); }
         */
        if (content.isObject()) {
            final ObjectAdapter adapter = content.getAdapter();
            if (adapter != null && adapter.isDestroyed()) {
                createView = new DisposedObjectBorder(createView);
            }
        }
        createView.getSubviews();
        return createView;
    }

    @Override
    public void debugData(final DebugBuilder sb) {
        sb.append("RootsViews\n");
        Enumeration fields = rootViews.elements();
        while (fields.hasMoreElements()) {
            final ViewSpecification spec = (ViewSpecification) fields.nextElement();
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");

        sb.append("Subviews\n");
        fields = subviews.elements();
        while (fields.hasMoreElements()) {
            final ViewSpecification spec = (ViewSpecification) fields.nextElement();
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");

        sb.append("Specifications\n");
        for (final ViewSpecification spec : viewSpecifications) {
            sb.append("  ");
            sb.append(spec);
            sb.append("\n");
        }
        sb.append("\n\n");
    }

    @Override
    public String debugTitle() {
        return "View factory entries";
    }

    private ViewSpecification getEmptyFieldSpecification() {
        if (emptyFieldSpecification == null) {
            LOG.error("missing empty field specification; using fallback");
            return fallback;
        }
        return emptyFieldSpecification;
    }

    public void setDragContentSpecification(final ViewSpecification dragContentSpecification) {
        this.dragContentSpecification = dragContentSpecification;
    }

    @Override
    public View createDragViewOutline(final View view) {
        return new DragViewOutline(view);
    }

    @Override
    public DragEvent createDragContentOutline(final View view, final Location location) {
        final View dragOverlay = dragContentSpecification.createView(view.getContent(), new Axes(), -1);
        return new ContentDragImpl(view, location, dragOverlay);
    }

    @Override
    public View createMinimizedView(final View view) {
        return new MinimizedView(view);
    }

    @Override
    public View createView(final ViewRequirement requirement) {
        final ViewSpecification objectFieldSpecification = getSpecificationForRequirement(requirement);
        return createView(objectFieldSpecification, requirement.getContent());
    }

    public ViewSpecification getSpecificationForRequirement(final ViewRequirement requirement) {
        final Content content = requirement.getContent();
        final ObjectSpecification specification = content.getSpecification();
        final boolean isValue = specification != null && specification.containsFacet(ValueFacet.class);
        if (content.isObject() && !isValue && content.getAdapter() == null) {
            return getEmptyFieldSpecification();
        } else {
            if (specification != null) {
                final Options viewOptions = Properties.getDefaultViewOptions(specification);
                String spec = viewOptions.getString("spec");
                if (spec == null) {
                    if (content instanceof ObjectContent && requirement.isObject() && requirement.isClosed()) {
                        spec = Properties.getDefaultIconViewOptions();
                    } else if (content instanceof CollectionContent && requirement.isCollection()) {
                        spec = Properties.getDefaultCollectionViewOptions();
                    } else if (content instanceof ObjectContent && requirement.isObject() && requirement.isOpen()) {
                        spec = Properties.getDefaultObjectViewOptions();
                    }
                }
                if (spec != null) {
                    final ViewSpecification lookSpec = lookupSpecByName(spec);
                    if (lookSpec != null && lookSpec.canDisplay(requirement)) {
                        return lookSpec;
                    }
                }
            }
            for (final ViewSpecification viewSpecification : viewSpecifications) {
                if (viewSpecification.canDisplay(requirement)) {
                    return viewSpecification;
                }

            }
            LOG.error("missing specification; using fall back");
            return fallback;
        }
    }

    public void loadUserViewSpecifications() {
        final Options options = Properties.getOptions("views.user-defined");
        final Iterator<String> names = options.names();
        while (names.hasNext()) {
            final String name = names.next();
            final Options viewOptions = options.getOptions(name);
            final String specName = viewOptions.getString("design");
            addSpecification(specName);
        }
    }

    private ViewSpecification lookupSpecByName(final String name) {
        for (final ViewSpecification viewSpecification : viewSpecifications) {
            if (viewSpecification.getName().equals(name)) {
                return viewSpecification;
            }
        }
        LOG.warn("No specification found for " + name);
        return null;
    }

    private ViewSpecification lookupSpecByClassName(final String className) {
        for (final ViewSpecification viewSpecification : viewSpecifications) {
            if (viewSpecification.getClass().getName().equals(className)) {
                return viewSpecification;
            }
        }
        LOG.warn("No specification found for " + className);
        return null;
    }

    @Override
    public Enumeration<ViewSpecification> availableViews(final ViewRequirement requirement) {
        return viewsFor(requirement, viewSpecifications);
    }

    @Override
    public Enumeration<ViewSpecification> availableDesigns(final ViewRequirement requirement) {
        return viewsFor(requirement, designSpecifications);
    }

    private Enumeration<ViewSpecification> viewsFor(final ViewRequirement requirement, final List<ViewSpecification> viewSpecifications) {
        final Vector<ViewSpecification> v = new Vector<ViewSpecification>();
        for (final ViewSpecification specification : viewSpecifications) {
            if (specification.canDisplay(requirement)) {
                v.addElement(specification);
            }
        }
        return v.elements();
    }

}
