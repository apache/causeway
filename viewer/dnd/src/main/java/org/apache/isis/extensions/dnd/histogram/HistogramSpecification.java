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


package org.apache.isis.extensions.dnd.histogram;

import java.util.List;

import org.apache.isis.core.commons.filters.AbstractFilter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewFactory;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.collection.CollectionContent;
import org.apache.isis.extensions.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewSpecification;


public class HistogramSpecification extends CompositeViewSpecification {

    static List<? extends ObjectAssociation> availableFields(CollectionContent content) {
        List<? extends ObjectAssociation> associationList;
        associationList = content.getElementSpecification().getAssociationList(new AbstractFilter<ObjectAssociation>() {
            public boolean accept(ObjectAssociation t) {
                return NumberAdapters.contains(t);
            }
        });
        return associationList;
    }

    public HistogramSpecification() {
        builder = new CollectionElementBuilder(new ViewFactory() {
            public View createView(Content content, Axes axes, int sequence) {
                return new HistogramBar(content, axes.getAxis(HistogramAxis.class), HistogramSpecification.this);
            }
        });
    }

    public Layout createLayout(Content content, Axes axes) {
        return new HistogramLayout();
    }

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isCollection() && requirement.isOpen() && availableFields((CollectionContent) requirement.getContent()).size() > 0;
    }
   
    @Override
    public void createAxes(Content content, Axes axes) {
        super.createAxes(content, axes);
        axes.add(new HistogramAxis(content));
    }

    public String getName() {
        return "Histogram (experimental)";
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
        return true;
    }

    @Override
    public boolean isSubView() {
        return false;
    }

}

