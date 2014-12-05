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

package org.apache.isis.core.metamodel.facets.collections.layout;

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacetAbstract;

public class PagedFacetOnCollectionFromLayoutProperties extends PagedFacetAbstract {

    public static PagedFacet create(Properties properties, FacetHolder holder) {
        final int paged = paged(properties);
        return paged != -1? new PagedFacetOnCollectionFromLayoutProperties(paged, holder): null;
    }

    private PagedFacetOnCollectionFromLayoutProperties(int paged, FacetHolder holder) {
        super(paged, holder);
    }

    private static int paged(Properties properties) {
        if(properties == null) {
            return -1;
        }
        String paged = Strings.emptyToNull(properties.getProperty("paged"));
        if(paged == null) {
            return -1;
        }
        return Integer.parseInt(paged);
    }
}
