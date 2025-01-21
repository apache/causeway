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
package org.apache.causeway.viewer.wicket.ui.panels;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.HasPageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;

public abstract class FormAbstract<T> extends Form<T>
implements
    HasCommonContext,
    HasComponentFactoryRegistry,
    HasPageClassRegistry {

    private static final long serialVersionUID = 1L;

    protected FormAbstract(final String id) {
        super(id);
    }

    protected FormAbstract(final String id, final IModel<T> model) {
        super(id, model);
    }

    // -- WICKET 10.3.0 BUG
    
    // see https://github.com/apache/wicket/pull/1076/files#top
    @Deprecated // remove once Wicket 10.4.0 is available
    @Override
    protected void addDefaultSubmitButtonHandler(IHeaderResponse headerResponse) {
        final Component component = (Component) super.getDefaultButton();
        String submitId = component.getMarkupId();

        AppendingStringBuffer script = new AppendingStringBuffer();
        script.append("if (event.target.tagName.toLowerCase() !== 'input' || event.which != 13) return;");
        script.append("var b = document.getElementById('" + submitId + "');");
        script.append("if (window.getComputedStyle(b).visibility === 'hidden') return;");
        script.append("event.stopPropagation();");
        script.append("event.preventDefault();");
        script.append("if (b != null && b.onclick != null && typeof (b.onclick) != 'undefined') {");
        script.append("var r = Wicket.bind(b.onclick, b)();");
        script.append("if (r != false) b.click();");
        script.append("} else {");
        script.append("b.click();");
        script.append("}");
        script.append("return false;");

        headerResponse.render(OnEventHeaderItem.forMarkupId(getMarkupId(), "keypress", script.toString()));
    }
    
    // -- DEPENDENCIES

    private transient ComponentFactoryRegistry componentFactoryRegistry;
    @Override
    public final ComponentFactoryRegistry getComponentFactoryRegistry() {
        if(componentFactoryRegistry==null) {
            componentFactoryRegistry = ((HasComponentFactoryRegistry) getApplication()).getComponentFactoryRegistry();
        }
        return componentFactoryRegistry;
    }

    private transient PageClassRegistry pageClassRegistry;
    @Override
    public final PageClassRegistry getPageClassRegistry() {
        if(pageClassRegistry==null) {
            pageClassRegistry = ((HasPageClassRegistry) getApplication()).getPageClassRegistry();
        }
        return pageClassRegistry;
    }

}
