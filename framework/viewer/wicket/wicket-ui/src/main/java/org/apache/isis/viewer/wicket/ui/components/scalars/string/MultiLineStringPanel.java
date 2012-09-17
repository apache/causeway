/*
 * Copyright 2012 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;

/**
 * Panel for rendering MultiLine scalars of type String
 */
public class MultiLineStringPanel extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";
    
    private int numberOfLines;
    
    public MultiLineStringPanel(final String id, final ScalarModel scalarModel, final int numberOfLines) {
        super(id, ID_SCALAR_VALUE, scalarModel);
        this.numberOfLines = numberOfLines;
    }

    @Override
    protected AbstractTextComponent<String> createTextField() {
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
        //textField.add(new SimpleAttributeModifier("rows", Integer.toString(numberOfLines)));
        textField.add(AttributeModifier.replace("rows", Integer.toString(numberOfLines)));
        return textField;
    }
    

}
