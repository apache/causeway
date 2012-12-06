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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;

/**
 * Specification for a tree node that will display an open collection as a root
 * node or within an object.
 * 
 * @see org.apache.isis.viewer.dnd.tree.ClosedCollectionNodeSpecification for
 *      displaying a closed collection within an object.
 */
public class OpenCollectionNodeSpecification extends CompositeNodeSpecification {
    /**
     * A collection tree can only be displayed for a collection that has
     * elements.
     */
    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        final ObjectAdapter collection = requirement.getAdapter();
        if (collection == null) {
            return false;
        } else {
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            return facet != null && facet.size(collection) > 0;
        }
    }

    public OpenCollectionNodeSpecification() {
        builder = new CollectionElementBuilder(this);
    }

    @Override
    public Layout createLayout(final Content content, final Axes axes) {
        return new StackLayout();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isSubView() {
        return false;
    }

    @Override
    public int canOpen(final Content content) {
        return CAN_OPEN;
    }

    @Override
    public String getName() {
        return "Collection tree node - open";
    }
}
