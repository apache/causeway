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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.service.PerspectiveContent;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.composite.AbstractViewBuilder;

/**
 * WorkspaceBuilder builds a workspace view for an ObjectContent view by finding
 * a collection of classes from a field called 'classes' and adding an icon for
 * each element. Similarly, if there is a collection called 'objects' its
 * elements are also added to the display.
 * 
 * <p>
 * During lay-out any icons that have an UNPLACED location (-1, -1) are given a
 * location. Objects of type ObjectSpecification are added to the left-hand
 * side, while all other icons are placed on the right-hand side of the
 * workspace view. Open windows are displayed in the centre.
 */
public class ApplicationWorkspaceBuilder extends AbstractViewBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationWorkspaceBuilder.class);
    private static final int PADDING = 10;
    public static final Location UNPLACED = new Location(-1, -1);

    public static class ApplicationLayout implements Layout {
        @Override
        public Size getRequiredSize(final View view) {
            return new Size(600, 400);
        }

        public String getName() {
            return "Simple Workspace";
        }

        @Override
        public void layout(final View view1, final Size maximumSize) {
            final ApplicationWorkspace view = (ApplicationWorkspace) view1;

            final int widthUsed = layoutServiceIcons(maximumSize, view);
            layoutObjectIcons(maximumSize, view);
            layoutWindowViews(maximumSize, view, widthUsed);
        }

        private void layoutWindowViews(final Size maximumSize, final ApplicationWorkspace view, final int xOffset) {
            final Size size = view.getSize();
            size.contract(view.getPadding());

            final int maxHeight = size.getHeight();
            final int maxWidth = size.getWidth();

            final int xWindow = xOffset + PADDING;
            int yWindow = PADDING;

            int xMinimized = 1;
            int yMinimized = maxHeight - 1;

            final View windows[] = view.getWindowViews();
            for (final View v : windows) {
                final Size componentSize = v.getRequiredSize(new Size(size));
                v.setSize(componentSize);
                if (v instanceof MinimizedView) {
                    final Size s = v.getRequiredSize(Size.createMax());
                    if (xMinimized + s.getWidth() > maxWidth) {
                        xMinimized = 1;
                        yMinimized -= s.getHeight() + 1;
                    }
                    v.setLocation(new Location(xMinimized, yMinimized - s.getHeight()));
                    xMinimized += s.getWidth() + 1;

                } else if (v.getLocation().equals(UNPLACED)) {
                    final int height = componentSize.getHeight() + 6;
                    v.setLocation(new Location(xWindow, yWindow));
                    yWindow += height;

                }
                v.limitBoundsWithin(maximumSize);
            }

            for (final View window : windows) {
                window.layout();
            }
        }

        private int layoutServiceIcons(final Size maximumSize, final ApplicationWorkspace view) {
            final Size size = view.getSize();
            size.contract(view.getPadding());

            final int maxHeight = size.getHeight();

            int xService = PADDING;
            int yService = PADDING;
            int maxServiceWidth = 0;

            final View views[] = view.getServiceIconViews();
            for (final View v : views) {
                final Size componentSize = v.getRequiredSize(new Size(size));
                v.setSize(componentSize);
                final int height = componentSize.getHeight() + 6;

                final ObjectAdapter object = v.getContent().getAdapter();
                if (object.getSpecification().isService()) {
                    if (yService + height > maxHeight) {
                        yService = PADDING;
                        xService += maxServiceWidth + PADDING;
                        maxServiceWidth = 0;
                        LOG.debug("creating new column at " + xService + ", " + yService);
                    }
                    LOG.debug("service icon at " + xService + ", " + yService);
                    v.setLocation(new Location(xService, yService));
                    maxServiceWidth = Math.max(maxServiceWidth, componentSize.getWidth());
                    yService += height;
                }
                v.limitBoundsWithin(maximumSize);
            }

            return xService + maxServiceWidth;
        }

        private void layoutObjectIcons(final Size maximumSize, final ApplicationWorkspace view) {
            final Size size = view.getSize();
            size.contract(view.getPadding());

            final int maxWidth = size.getWidth();

            final int xObject = maxWidth - PADDING;
            int yObject = PADDING;

            final View views[] = view.getObjectIconViews();
            for (final View v : views) {
                final Size componentSize = v.getRequiredSize(new Size(size));
                v.setSize(componentSize);
                if (v.getLocation().equals(UNPLACED)) {
                    final int height = componentSize.getHeight() + 6;
                    v.setLocation(new Location(xObject - componentSize.getWidth(), yObject));
                    yObject += height;
                }
                v.limitBoundsWithin(maximumSize);
            }
        }
    }

    @Override
    public void build(final View view1, final Axes axes) {
        final ApplicationWorkspace workspace = (ApplicationWorkspace) view1;

        final PerspectiveContent perspectiveContent = (PerspectiveContent) view1.getContent();

        // REVIEW is this needed?
        workspace.clearServiceViews();

        final PerspectiveEntry perspective = perspectiveContent.getPerspective();
        for (final Object object : perspective.getObjects()) {
            final ObjectAdapter adapter = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(object);
            workspace.addIconFor(adapter, new Placement(ApplicationWorkspaceBuilder.UNPLACED));
        }

        for (final Object service : perspective.getServices()) {
            final ObjectAdapter adapter = IsisContext.getPersistenceSession().getAdapterManager().adapterFor(service);
            if (isHidden(adapter)) {
                continue;
            }
            workspace.addServiceIconFor(adapter);
        }
    }

    private boolean isHidden(final ObjectAdapter serviceNO) {
        final ObjectSpecification serviceNoSpec = serviceNO.getSpecification();
        return serviceNoSpec.isHidden();
    }

    public boolean canDisplay(final ObjectAdapter object) {
        return object instanceof ObjectAdapter && object != null;
    }

}
