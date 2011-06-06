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
package org.apache.isis.viewer.restful.viewer.xom;


import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restful.viewer.tree.Element;
import org.apache.isis.viewer.restful.viewer.util.OidUtils;

public abstract class TableColumnAbstract<T> implements TableColumn<T> {
    private final String headerText;

    protected final ResourceContext resourceContext;
    protected final XhtmlRenderer xhtmlRenderer;

    protected TableColumnAbstract(final String headerText, final ResourceContext resourceContext) {
        this.headerText = headerText;
        this.resourceContext = resourceContext;
        this.xhtmlRenderer = new XhtmlRenderer();
    }

    protected ElementBuilder builder() {
        return new ElementBuilder();
    }

    protected String getContextPath() {
        return resourceContext.getHttpServletRequest().getContextPath();
    }

    @Override
    public String getHeaderText() {
        return headerText;
    }

    @Override
    public Element th() {
        final Element th = new Element("th");
        th.appendChild(headerText);
        return th;
    }

    @Override
    public Element td(final T t) {
        final Element td = new Element("td");
        final Element doTd = doTd(t);
        if (doTd != null) {
            td.appendChild(doTd);
        }
        return td;
    }

    protected abstract Element doTd(T t);

    protected String getOidStr(final ObjectAdapter adapter) {
        return OidUtils.getOidStr(adapter, getOidStringifier());
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    // //////////////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    private static OidStringifier getOidStringifier() {
        return getOidGenerator().getOidStringifier();
    }

}
