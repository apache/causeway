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

package org.apache.isis.viewer.dnd.calendar;

import java.util.List;

import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.CompositeViewSpecification;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;
import org.apache.isis.viewer.dnd.view.composite.ViewBuilder;

public class CalendarSpecification implements CompositeViewSpecification {

    public Layout createLayout(final Content content, final Axes axes) {
        return new StackLayout();
    }

    public void createAxes(final Content content, final Axes axes) {
        // axes.add(new CalendarAxis());
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        return new CalendarView(content, this);
        // return new ViewResizeBorder(new CalendarView(content, this));
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        final boolean openCollection = requirement.isCollection() && requirement.isOpen();
        if (openCollection) {
            final List<OneToOneAssociation> propertyList = ((CollectionContent) requirement.getContent()).getElementSpecification().getProperties(Contributed.EXCLUDED);
            for (final OneToOneAssociation association : propertyList) {
                if (!association.isAlwaysHidden() && association.getSpecification().containsFacet(DateValueFacet.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Calendar (experimental) ";
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReplaceable() {
        return true;
    }

    @Override
    public boolean isSubView() {
        return false;
    }

    public ViewBuilder getSubviewBuilder() {
        return null;
    }

    @Override
    public boolean isAligned() {
        return false;
    }

    @Override
    public boolean isResizeable() {
        return true;
    }

}
