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

package org.apache.isis.viewer.dnd.combined;

import org.apache.isis.viewer.dnd.form.ExpandableViewBorder;
import org.apache.isis.viewer.dnd.icon.IconElementFactory;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.composite.AbstractCollectionViewSpecification;

public class ExpandableListSpecification extends AbstractCollectionViewSpecification {

    public ExpandableListSpecification() {
        builder.addSubviewDecorator(new ExpandableViewBorder.Factory());
    }

    @Override
    protected ViewFactory createElementFactory() {
        return new IconElementFactory();
    }

    @Override
    public String getName() {
        return "Expanding List (experimental)";
    }

    // TODO this should be available if an item can be given more space
    /*
     * @Override public boolean canDisplay(final Content content,
     * ViewRequirement requirement) { return content.isCollection() &&
     * requirement.is(ViewRequirement.CLOSED) &&
     * requirement.is(ViewRequirement.SUBVIEW) &&
     * requirement.is(ViewRequirement.SUBVIEW); }
     */

}
