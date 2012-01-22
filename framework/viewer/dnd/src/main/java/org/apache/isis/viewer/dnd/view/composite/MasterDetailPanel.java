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

package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Selectable;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.BlankView;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.border.SelectableViewAxis;
import org.apache.isis.viewer.dnd.view.border.ViewResizeBorder;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class MasterDetailPanel extends CompositeView implements Selectable {
    private static final int MINIMUM_WIDTH = 120;
    private final ViewSpecification leftHandSideSpecification;
    private final Axes axes;

    public MasterDetailPanel(final Content content, final ViewSpecification specification, final ViewSpecification leftHandSideSpecification) {
        super(content, specification);
        this.leftHandSideSpecification = leftHandSideSpecification;
        axes = new Axes();
        axes.add(new SelectableViewAxis(this));
    }

    @Override
    protected void buildView() {
        final Content content = getContent();
        View leftHandView = leftHandSideSpecification.createView(content, axes, -1);
        leftHandView = new ViewResizeBorder(new ScrollBorder(leftHandView));
        leftHandView.setParent(getView());
        addView(leftHandView);

        final Size blankViewSize = new Size(MINIMUM_WIDTH, 0);
        final View blankView = new BlankView(new NullContent(), blankViewSize);
        blankView.setParent(getView());
        addView(blankView);

        selectFirstSuitableObject(content);
    }

    private void selectFirstSuitableObject(final Content content) {
        if (content instanceof CollectionContent) {
            final ObjectAdapter[] elements = ((CollectionContent) content).elements();
            if (elements.length > 0) {
                final ObjectAdapter firstElement = elements[0];
                final Content firstElementContent = Toolkit.getContentFactory().createRootContent(firstElement);
                setSelectedNode(firstElementContent);
            }
        } else if (content instanceof ObjectContent) {
            /*
             * TODO provide a view that shows first useful object (not
             * redisplaying parent)
             * 
             * ObjectAssociation[] associations =
             * content.getSpecification().getAssociations(); for (int i = 0; i <
             * associations.length; i++) { ObjectAssociation assoc =
             * associations[i]; if (assoc.isOneToManyAssociation()) {
             * ObjectAdapter collection = assoc.get(content.getAdapter()); final
             * Content collectionContent =
             * Toolkit.getContentFactory().createRootContent(collection);
             * setSelectedNode(collectionContent); break; } else if
             * (assoc.isOneToOneAssociation() &&
             * !((OneToOneAssociation)assoc).getSpecification().isParseable()) {
             * ObjectAdapter object = assoc.get(content.getAdapter()); if
             * (object == null) { continue; } final Content objectContent =
             * Toolkit.getContentFactory().createRootContent(object);
             * setSelectedNode(objectContent); break; } }
             */
            setSelectedNode(content);
        }
    }

    @Override
    protected void doLayout(final Size availableSpace) {
        availableSpace.contract(getView().getPadding());

        final View[] subviews = getSubviews();
        final View left = subviews[0];
        final View right = subviews[1];
        final Size leftPanelRequiredSize = left.getRequiredSize(new Size(availableSpace));
        final Size rightPanelRequiredSize = right == null ? new Size() : right.getRequiredSize(new Size(availableSpace));

        // combine the two sizes
        final Size totalSize = new Size(leftPanelRequiredSize);
        totalSize.extendWidth(rightPanelRequiredSize.getWidth());
        totalSize.ensureHeight(rightPanelRequiredSize.getHeight());

        if (totalSize.getWidth() > availableSpace.getWidth()) {
            /*
             * If the combined width is greater than the available then we need
             * to divide the space between the two sides and recalculate
             */
            if (rightPanelRequiredSize.getWidth() <= MINIMUM_WIDTH) {
                leftPanelRequiredSize.setWidth(availableSpace.getWidth() - rightPanelRequiredSize.getWidth());
            } else {
                final int availableWidth = availableSpace.getWidth();
                final int requiredWidth = totalSize.getWidth();
                leftPanelRequiredSize.setWidth(leftPanelRequiredSize.getWidth() * availableWidth / requiredWidth);
                rightPanelRequiredSize.setWidth(rightPanelRequiredSize.getWidth() * availableWidth / requiredWidth);
            }
            /*
             * final int leftWidth = Math.max(MINIMUM_WIDTH,
             * leftPanelRequiredSize.getWidth()); final int rightWidth =
             * rightPanelRequiredSize.getWidth(); final int totalWidth =
             * leftWidth + rightWidth;
             * 
             * final int bestWidth = (int) (1.0 * leftWidth / totalWidth *
             * availableWidth); final Size maximumSizeLeft = new Size(bestWidth,
             * maximumSize.getHeight()); leftPanelRequiredSize =
             * left.getRequiredSize(maximumSizeLeft);
             * 
             * final Size maximumSizeRight = new Size(availableWidth -
             * leftPanelRequiredSize.getWidth(), maximumSize.getHeight());
             * rightPanelRequiredSize = right.getRequiredSize(maximumSizeRight);
             */
        }

        // combinedSize.setHeight(Math.min(combinedSize.getHeight(),
        // maximumSize.getHeight()));
        // totalSize.limitSize(availableSpace);

        left.setSize(new Size(leftPanelRequiredSize.getWidth(), totalSize.getHeight()));
        left.layout();

        if (right != null) {
            right.setLocation(new Location(left.getSize().getWidth(), 0));

            rightPanelRequiredSize.setHeight(totalSize.getHeight());
            right.setSize(rightPanelRequiredSize);
            right.layout();
        }
    }

    @Override
    public Size requiredSize(final Size availableSpace) {
        final View[] subviews = getSubviews();
        final View left = subviews[0];
        final View right = subviews.length > 1 ? subviews[1] : null;

        Size leftPanelRequiredSize = left.getRequiredSize(new Size(availableSpace));
        Size rightPanelRequiredSize = right == null ? new Size() : right.getRequiredSize(new Size(availableSpace));

        if (leftPanelRequiredSize.getWidth() + rightPanelRequiredSize.getWidth() > availableSpace.getWidth()) {
            /*
             * If the combined width is greater than the available then we need
             * to divide the space between the two sides and recalculate
             */

            final int availableWidth = availableSpace.getWidth();
            final int leftWidth = leftPanelRequiredSize.getWidth();
            final int rightWidth = Math.max(MINIMUM_WIDTH, rightPanelRequiredSize.getWidth());
            final int totalWidth = leftWidth + rightWidth;

            final int bestWidth = (int) (1.0 * leftWidth / totalWidth * availableWidth);
            final Size maximumSizeLeft = new Size(bestWidth, availableSpace.getHeight());
            leftPanelRequiredSize = left.getRequiredSize(maximumSizeLeft);

            final Size maximumSizeRight = new Size(availableWidth - leftPanelRequiredSize.getWidth(), availableSpace.getHeight());
            rightPanelRequiredSize = right == null ? new Size() : right.getRequiredSize(maximumSizeRight);
        }

        // combine the two required sizes
        final Size combinedSize = new Size(leftPanelRequiredSize);
        combinedSize.extendWidth(rightPanelRequiredSize.getWidth());
        combinedSize.ensureHeight(rightPanelRequiredSize.getHeight());
        return combinedSize;
    }

    protected void showInRightPane(final View view) {
        replaceView(getSubviews()[1], view);
    }

    @Override
    public void setSelectedNode(final View view) {
        final Content content = view.getContent();
        setSelectedNode(content);
    }

    private void setSelectedNode(final Content content) {
        final ViewRequirement requirement = new ViewRequirement(content, ViewRequirement.OPEN | ViewRequirement.SUBVIEW | ViewRequirement.FIXED);
        /*
         * final ObjectAdapter object = content.getAdapter(); final
         * ObjectSpecification specification = object.getSpecification(); final
         * CollectionFacet facet =
         * specification.getFacet(CollectionFacet.class); if (facet != null &&
         * facet.size(object) > 0) { if
         * (mainViewTableSpec.canDisplay(requirement)) {
         * showInRightPane(mainViewTableSpec.createView(content, axes, -1)); }
         * else if (mainViewListSpec.canDisplay(requirement)) {
         * showInRightPane(mainViewListSpec.createView(content, axes, -1)); }
         * 
         * } else if (specification.isObject()) { if (object != null &&
         * mainViewFormSpec.canDisplay(requirement)) {
         * showInRightPane(mainViewFormSpec.createView(content, axes, -1)); } }
         */
        final View createView = Toolkit.getViewFactory().createView(requirement);
        showInRightPane(createView);
    }

    @Override
    public String toString() {
        return "MasterDetailPanel" + getId();
    }

}
