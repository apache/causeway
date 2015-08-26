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

package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;

/**
 * Panel for rendering MultiLine scalars of type String
 */
public class MultiLineStringPanel extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;
    
    public MultiLineStringPanel(final String id, final ScalarModel scalarModel) {
        super(id, ID_SCALAR_VALUE, scalarModel);
    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular() {
        final TextArea<String> textField = new TextArea<String>(idTextField, new Model<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getModel().getObjectAsString();
            }

            @Override
            public void setObject(final String object) {
                if (object == null) {
                    getModel().setObject(null);
                } else {
                    getModel().setObjectAsString(object);
                }
            }
        });

        final MultiLineFacet multiLineFacet = getModel().getFacet(MultiLineFacet.class);
        setAttribute(textField, "rows", multiLineFacet.numberOfLines());
        
        
        final Integer maxLength = getValueOf(getModel(), MaxLengthFacet.class);
        if(maxLength != null) {
            // in conjunction with javascript in jquery.isis.wicket.viewer.js
            // see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea
            setAttribute(textField, "maxlength", maxLength);
        }

        return textField;
    }

    @Override
    protected Fragment createTextFieldFragment(String id) {
        return new Fragment(id, "textarea", MultiLineStringPanel.this);
    }

    @Override
    protected IModel<String> getScalarPanelType() {
        return Model.of("multiLineStringPanel");
    }

    private Component setAttribute(final TextArea<String> textField, final String attributeName, final int i) {
        return textField.add(AttributeModifier.replace(attributeName, ""+i));
    }

    private static Integer getValueOf(ScalarModel model, Class<? extends SingleIntValueFacet> facetType) {
        final SingleIntValueFacet facet = model.getFacet(facetType);
        return facet != null ? facet.value() : null;
    }


    public int getNumberOfLines(final ScalarModel scalarModel) {
        final MultiLineFacet multiLineFacet = scalarModel.getFacet(MultiLineFacet.class);
        int numberOfLines = 1;
        
        if (multiLineFacet != null) {
            numberOfLines = multiLineFacet.numberOfLines();
        }
        
        return numberOfLines;
    }

}
