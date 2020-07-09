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
package org.apache.isis.valuetypes.asciidoc.ui.vaa.components;

import javax.annotation.Nullable;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;

import org.apache.isis.incubator.viewer.vaadin.ui.util.LocalResourceUtil;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

public class AsciiDocFieldVaa extends CustomField<AsciiDoc> {

    private static final long serialVersionUID = 1L;
    
    private final Div div = new Div();
    private AsciiDoc markup;

    public AsciiDocFieldVaa(String label) {
        super();
        setLabel(label);
        add(div);
        
        LocalResourceUtil.addStyleSheet(PrismResources.getCssResourceReference());
        LocalResourceUtil.executeJavaScript(PrismResources::readJsResource);
    }
    
    @Override
    protected AsciiDoc generateModelValue() {
        return markup;
    }

    @Override
    protected void setPresentationValue(@Nullable AsciiDoc markup) {
        this.markup = markup;
        
        div.removeAll();
        
        if(markup==null) {
            return;
        }

        div.add(new Html("<div style=\"line-height:normal\">" + markup.asHtml() + "</div>"));
    }

}
