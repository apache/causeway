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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.base.AbstractFieldSpecification;
import org.apache.isis.viewer.dnd.view.border.TextFieldResizeBorder;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;
import org.apache.isis.viewer.dnd.view.lookup.OpenDropDownBorder;
import org.apache.isis.viewer.dnd.view.lookup.OptionContent;

/**
 * Creates a single line text field with the base line drawn.
 */
public class DateFieldSpecification extends AbstractFieldSpecification {
    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isTextParseable() && requirement.isForValueType(DateValueFacet.class);
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final SingleLineTextField textField = new SingleLineTextField((TextParseableContent) content, this, true);
        final View field = new TextFieldResizeBorder(textField);
        return new OpenDropDownBorder(field) {
            @Override
            protected View createDropDownView() {
                return DatePickerControl.getPicker(content);
            }

            @Override
            protected void setSelection(final OptionContent selectedContent) {
            }
        };
    }

    @Override
    public String getName() {
        return "Date Field";
    }
}
