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
package org.apache.causeway.valuetypes.vega.ui.vaa.components;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;

import org.springframework.lang.Nullable;

import org.apache.causeway.incubator.viewer.vaadin.ui.util.LocalResourceUtil;
import org.apache.causeway.incubator.viewer.vaadin.ui.util.PrismResourcesVaa;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;

public class VegaFieldVaa extends CustomField<Vega> {

    private static final long serialVersionUID = 1L;

    private final Div div = new Div();
    private Vega vega;

    public VegaFieldVaa(final String label) {
        super();
        setLabel(label);
        add(div);

        LocalResourceUtil.addStyleSheet(PrismResourcesVaa.getCssResourceReference());
        //LocalResourceUtil.executeJavaScript(PrismResourcesVaa::readJsResource);
        //TODO potentially needs to be executed on page loaded ...
        LocalResourceUtil.addJavaScript(PrismResourcesVaa.getJsResourceReference());
    }

    @Override
    protected Vega generateModelValue() {
        return vega;
    }

    @Override
    protected void setPresentationValue(@Nullable final Vega markup) {
        this.vega = markup;

        div.removeAll();

        if(markup==null) {
            return;
        }

        //TODO - just a stub
        div.add(new Html("<div style=\"line-height:normal\">" + markup.getSchema().name() + "</div>"));
    }

}
