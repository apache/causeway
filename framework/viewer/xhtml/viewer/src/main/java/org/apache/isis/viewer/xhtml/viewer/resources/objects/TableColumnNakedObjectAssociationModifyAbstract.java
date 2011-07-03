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
package org.apache.isis.viewer.xhtml.viewer.resources.objects;

import java.text.MessageFormat;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.xhtml.viewer.tree.Attribute;
import org.apache.isis.viewer.xhtml.viewer.tree.Element;
import org.apache.isis.viewer.xhtml.viewer.xom.ResourceContext;

public abstract class TableColumnNakedObjectAssociationModifyAbstract<T extends ObjectAssociation> extends
    TableColumnNakedObjectAssociation<T> {

    private final boolean inputField;

    public TableColumnNakedObjectAssociationModifyAbstract(final String columnName,
        final AuthenticationSession session, final ObjectAdapter nakedObject, final ResourceContext resourceContext) {
        this(columnName, session, nakedObject, resourceContext, true);
    }

    /**
     * @param field
     *            - whether to include an input field.
     */
    public TableColumnNakedObjectAssociationModifyAbstract(final String columnName,
        final AuthenticationSession session, final ObjectAdapter nakedObject, final ResourceContext resourceContext,
        final boolean field) {
        super(columnName, session, nakedObject, resourceContext);
        this.inputField = field;
    }

    @Override
    public Element doTd(final T association) {
        if (!association.isVisible(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, getHtmlClassAttribute());
        }
        if (!association.isUsable(getSession(), getNakedObject()).isAllowed()) {
            return xhtmlRenderer.p(null, getHtmlClassAttribute());
        }

        final Element div = xhtmlRenderer.div(getHtmlClassAttribute());

        div.appendChild(form(association));
        return div;
    }

    private Element form(final T association) {
        final String associationId = association.getId();
        final String formName = getFormNamePrefix() + associationId;
        final Element form = xhtmlRenderer.form(formName, getHtmlClassAttribute());
        form.addAttribute(new Attribute("class", associationId));

        final String inputFieldName = "proposedValue";
        if (inputField) {
            final Element inputValue = new Element("input");
            inputValue.addAttribute(new Attribute("type", "value"));
            inputValue.addAttribute(new Attribute("name", inputFieldName));
            form.appendChild(inputValue);
        }

        final Element inputButton = new Element("input");
        inputButton.addAttribute(new Attribute("type", "button"));
        inputButton.addAttribute(new Attribute("value", getFormButtonLabel()));
        final String servletContextName = getContextPath();
        final String url = MessageFormat.format("{0}/object/{1}", servletContextName, getOidStr());
        inputButton.addAttribute(new Attribute("onclick", invokeJavascript(url, associationId, inputFieldName)));

        form.appendChild(inputButton);
        form.addAttribute(new Attribute("action", url));

        return form;
    }

    protected abstract String getFormButtonLabel();

    /**
     * Used to construct the <tt>&lt;form name=&quot;xxx&quot;&gt;</tt> that holds the value used to make the change.
     * 
     * @return
     */
    protected abstract String getFormNamePrefix();

    /**
     * Used HTML Class attribute used variously throughout the rendered HTML form.
     * 
     * @return
     */
    protected abstract String getHtmlClassAttribute();

    /**
     * Invoke the appropriate Javascript function from <tt>isis-rest-support.js</tt>.
     * 
     * @param url
     * @param associationId
     * @param inputFieldName
     * @return
     */
    protected abstract String invokeJavascript(String url, String associationId, String inputFieldName);

}
