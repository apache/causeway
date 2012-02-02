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

package org.apache.isis.viewer.dnd.tree;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;

/**
 * Specification for a tree browser frame with a tree displaying only
 * collections and objects containing collections.
 */
public class TreeSpecification implements ViewSpecification {
    private final OpenCollectionNodeSpecification openCollection;
    private final OpenObjectNodeSpecification openObject;

    public TreeSpecification() {
        final ClosedObjectNodeSpecification closedObject = new ClosedObjectNodeSpecification(false); // ,
                                                                                                     // new
                                                                                                     // SelectObjectBorder.Factory());
        final NodeSpecification closedCollection = new ClosedCollectionNodeSpecification();
        final EmptyNodeSpecification noNode = new EmptyNodeSpecification();

        openCollection = new OpenCollectionNodeSpecification();
        openCollection.setCollectionSubNodeSpecification(noNode);
        openCollection.setObjectSubNodeSpecification(closedObject);
        openCollection.setReplacementNodeSpecification(closedCollection);

        openObject = new OpenObjectNodeSpecification();
        openObject.setCollectionSubNodeSpecification(closedCollection);
        openObject.setObjectSubNodeSpecification(noNode);
        openObject.setReplacementNodeSpecification(closedObject);

        closedObject.setReplacementNodeSpecification(openObject);

        closedCollection.setReplacementNodeSpecification(openCollection);
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.is(ViewRequirement.OPEN) && (openCollection.canDisplay(requirement) || openObject.canDisplay(requirement)) && requirement.isExpandable();
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        View rootNode;
        final ViewRequirement requirement = new ViewRequirement(content, ViewRequirement.CLOSED);
        if (openCollection.canDisplay(requirement)) {
            rootNode = openCollection.createView(content, axes, -1);
        } else {
            rootNode = openObject.createView(content, axes, -1);
        }
        return rootNode;
    }

    @Override
    public String getName() {
        return "Tree (not working)";
    }

    @Override
    public boolean isAligned() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReplaceable() {
        return false;
    }

    @Override
    public boolean isResizeable() {
        return false;
    }

    @Override
    public boolean isSubView() {
        return false;
    }
}
