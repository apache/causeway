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

package org.apache.isis.viewer.dnd.view.lookup;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.action.ParameterContent;
import org.apache.isis.viewer.dnd.view.field.OneToOneField;

public class OpenObjectDropDownBorder extends OpenDropDownBorder {
    private final SelectionListSpecification listSpecification;

    public OpenObjectDropDownBorder(final View wrappedView, final ViewSpecification spec) {
        super(wrappedView);
        listSpecification = new SelectionListSpecification() {
            @Override
            protected View createElementView(final Content content) {
                return spec.createView(content, null, 0);
            }
        };
    }

    @Override
    protected View createDropDownView() {
        final Axes viewAxes = getViewAxes();
        viewAxes.add(new SelectionListAxis(this), SelectionListAxis.class);
        return listSpecification.createView(getContent(), viewAxes, -1);
    }

    @Override
    protected boolean isAvailable() {
        final Content content = getContent();
        if (content instanceof OneToOneField) {
            final OneToOneField oneToOneField = ((OneToOneField) content);
            return oneToOneField.isEditable().isAllowed();
        } else if (content instanceof ParameterContent) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    protected void setSelection(final OptionContent selectedContent) {
        final ObjectAdapter option = selectedContent.getAdapter();
        ((ObjectContent) getContent()).setObject(option);
    }
}
