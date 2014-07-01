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

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.SubviewDecorator;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.collection.CollectionElement;
import org.apache.isis.viewer.dnd.view.field.OneToManyField;
import org.apache.isis.viewer.dnd.view.field.OneToManyFieldElementImpl;

public class CollectionElementBuilder extends AbstractViewBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionElementBuilder.class);
    private boolean canDragView = true;
    private final ViewFactory subviewDesign;

    public CollectionElementBuilder(final ViewFactory subviewDesign) {
        this.subviewDesign = subviewDesign;
    }

    public CollectionElementBuilder(final ViewFactory subviewDesign, final SubviewDecorator decorator) {
        this.subviewDesign = subviewDesign;
        addSubviewDecorator(decorator);
    }

    @Override
    public void build(final View view, final Axes axes) {
        Assert.assertEquals(view.getView(), view);

        final Content content = view.getContent();
        final OneToManyAssociation field = content instanceof OneToManyField ? ((OneToManyField) content).getOneToManyAssociation() : null;

        LOG.debug("rebuild view " + view + " for " + content);

        final CollectionContent collectionContent = ((CollectionContent) content);
        Enumeration elements;
        elements = collectionContent.allElements();

        // addViewAxes(view);

        /*
         * remove all subviews from the view and then work through the elements
         * of the collection adding in a view for each element. Where a subview
         * for the that element already exists it should be reused.
         */
        final View[] subviews = view.getSubviews();
        final ObjectAdapter[] existingElements = new ObjectAdapter[subviews.length];
        for (int i = 0; i < subviews.length; i++) {
            view.removeView(subviews[i]);
            existingElements[i] = subviews[i].getContent().getAdapter();
        }

        int elementNumber = 0;
        while (elements.hasMoreElements()) {
            final ObjectAdapter element = (ObjectAdapter) elements.nextElement();
            View elementView = null;
            for (int i = 0; i < subviews.length; i++) {
                if (existingElements[i] == element) {
                    elementView = subviews[i];
                    existingElements[i] = null;
                    break;
                }
            }
            if (elementView == null) {
                Content elementContent;
                if (field == null) {
                    elementContent = new CollectionElement(element);
                } else {
                    final ObjectAdapter obj = ((OneToManyField) view.getContent()).getParent();
                    // ObjectAdapter obj =
                    // view.getParent().getContent().getAdapter();
                    final ObjectAdapter parent = obj;
                    elementContent = new OneToManyFieldElementImpl(parent, element, field);
                }
                elementView = subviewDesign.createView(elementContent, axes, elementNumber++);
            }
            // TODO if reusing view then probably should decorate it again!
            if (elementView != null) {
                view.addView(decorateSubview(axes, elementView));
            }
        }
    }

    @Override
    public boolean canDragView() {
        return canDragView;
    };

    public void setCanDragView(final boolean canDragView) {
        this.canDragView = canDragView;
    }
}
