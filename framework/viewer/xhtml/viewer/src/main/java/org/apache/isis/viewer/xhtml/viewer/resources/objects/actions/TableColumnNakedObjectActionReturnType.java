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
package org.apache.isis.viewer.xhtml.viewer.resources.objects.actions;

import java.text.MessageFormat;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.xhtml.viewer.html.HtmlClass;
import org.apache.isis.viewer.xhtml.viewer.tree.Element;
import org.apache.isis.viewer.xhtml.viewer.xom.ResourceContext;

public final class TableColumnNakedObjectActionReturnType extends TableColumnNakedObjectAction {

    public TableColumnNakedObjectActionReturnType(final AuthenticationSession session, final ObjectAdapter nakedObject,
        final ResourceContext resourceContext) {
        super("Type", session, nakedObject, resourceContext);
    }

    @Override
    public Element doTd(final ObjectAction action) {
        final String returnTypeFullName = action.getReturnType().getFullIdentifier();
        final String uri = MessageFormat.format("{0}/specs/{1}", getContextPath(), returnTypeFullName);
        return xhtmlRenderer.aHref(uri, returnTypeFullName, "actionReturnTypeSpec", "action", HtmlClass.ACTION);
    }

}
