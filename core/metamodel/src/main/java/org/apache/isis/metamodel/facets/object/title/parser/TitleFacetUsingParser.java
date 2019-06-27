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

package org.apache.isis.metamodel.facets.object.title.parser;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.metamodel.facetapi.FacetAbstract;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.metamodel.spec.ManagedObject;

public class TitleFacetUsingParser extends FacetAbstract implements TitleFacet {

    private final Parser parser;

    public TitleFacetUsingParser(final Parser parser, final FacetHolder holder) {
        super(TitleFacet.class, holder, Derivation.NOT_DERIVED);
        this.parser = parser;
    }

    @Override
    protected String toStringValues() {
        getServiceInjector().injectServicesInto(parser);
        return parser.toString();
    }

    @Override
    public String title(final ManagedObject adapter) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if (object == null) {
            return null;
        }
        getServiceInjector().injectServicesInto(parser);
        return parser.displayTitleOf(object);
    }

    @Override
    public String title(ManagedObject contextAdapter, ManagedObject targetAdapter) {
        return title(targetAdapter);
    }

    /**
     * not API
     */
    public String title(final ManagedObject adapter, final String usingMask) {
        if (adapter == null) {
            return null;
        }
        final Object object = adapter.getPojo();
        if (object == null) {
            return null;
        }
        getServiceInjector().injectServicesInto(parser);
        return parser.displayTitleOf(object, usingMask);
    }




}
