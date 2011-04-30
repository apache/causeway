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
package org.apache.isis.viewer.restful.viewer.resources.objects;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnNakedObjectAssociationType<T extends ObjectAssociation> extends
        TableColumnNakedObjectAssociation<T> {

    public TableColumnNakedObjectAssociationType(
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super("Type", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final T association) {
        final String assocTypeFullName = association.getSpecification().getFullIdentifier();
        final String uri = MessageFormat.format("{0}/specs/{1}", getContextPath(), assocTypeFullName);
        return xhtmlRenderer.aHref(uri, assocTypeFullName, "propertyTypeSpec", "property", HtmlClass.PROPERTY);
    }
}
