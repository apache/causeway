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
package org.apache.isis.viewer.xhtml.viewer.resources.objects.properties;

import java.text.MessageFormat;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.xhtml.viewer.html.HtmlClass;
import org.apache.isis.viewer.xhtml.viewer.resources.objects.TableColumnNakedObjectAssociationModifyAbstract;
import org.apache.isis.viewer.xhtml.viewer.util.StringUtil;
import org.apache.isis.viewer.xhtml.viewer.xom.ResourceContext;

public final class TableColumnOneToOneAssociationClear extends
    TableColumnNakedObjectAssociationModifyAbstract<OneToOneAssociation> {

    public TableColumnOneToOneAssociationClear(final AuthenticationSession session, final ObjectAdapter nakedObject,
        final ResourceContext resourceContext) {
        super("Clear", session, nakedObject, resourceContext, false);
    }

    @Override
    protected String getFormNamePrefix() {
        return "property-";
    }

    @Override
    protected String getHtmlClassAttribute() {
        return HtmlClass.PROPERTY;
    }

    @Override
    protected String getFormButtonLabel() {
        return "Clear";
    }

    /**
     * Calls the <tt>putProperty()</tt> Javascript function that lives in <tt>isis-rest-support.js</tt>
     * 
     * @param url
     * @param associationId
     * @param inputFieldName
     *            - the name of the field in the form to read the value.
     * @return
     */
    @Override
    protected String invokeJavascript(final String url, final String associationId, final String inputFieldName) {
        return MessageFormat.format("clearProperty({0},{1});", StringUtil.quote(url), StringUtil.quote(associationId));
    }

}
