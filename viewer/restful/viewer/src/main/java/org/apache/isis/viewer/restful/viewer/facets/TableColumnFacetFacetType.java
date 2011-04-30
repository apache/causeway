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
package org.apache.isis.viewer.restful.viewer.facets;

import java.text.MessageFormat;

import nu.xom.Element;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.viewer.restful.viewer.html.HtmlClass;
import org.apache.isis.viewer.restful.viewer.xom.ResourceContext;


public final class TableColumnFacetFacetType extends TableColumnFacet {
    private final String pathPrefix;

    public TableColumnFacetFacetType(final String pathPrefix, final ResourceContext resourceContext) {
        super("FacetType", resourceContext);
        this.pathPrefix = pathPrefix;
    }

    @Override
    public Element doTd(final Facet facet) {
        final String facetTypeCanonicalName = facet.facetType().getCanonicalName();
        final String uri = MessageFormat.format("{0}/facet/{1}", pathPrefix, facetTypeCanonicalName);
        return new Element(xhtmlRenderer.aHref(uri, facetTypeCanonicalName, "facet", "spec", HtmlClass.FACET));
    }
}
