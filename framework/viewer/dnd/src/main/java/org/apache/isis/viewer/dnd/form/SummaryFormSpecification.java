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

package org.apache.isis.viewer.dnd.form;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.border.BackgroundBorder;
import org.apache.isis.viewer.dnd.view.border.EmptyBorder;
import org.apache.isis.viewer.dnd.view.border.IconBorder;
import org.apache.isis.viewer.dnd.view.border.LineBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;

public class SummaryFormSpecification extends AbstractFormSpecification {

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isObject() && !requirement.isTextParseable() && requirement.hasReference() && requirement.isOpen() && requirement.isSubview() && requirement.isFixed();
    }

    @Override
    protected void init() {
        addViewDecorator(new IconBorder.Factory());
        addViewDecorator(new CompositeViewDecorator() {
            @Override
            public View decorate(final View view, final Axes axes) {
                return new EmptyBorder(3, new BackgroundBorder(new LineBorder(1, 8, new EmptyBorder(3, view))));
            }
        });
    }

    @Override
    protected ViewFactory createFieldFactory() {
        return new SummaryFields();
    }

    @Override
    public String getName() {
        return "Summary";
    }

}
