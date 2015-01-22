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
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetFromLayoutPropertiesAbstract;

public class HiddenFacetOnCollectionFromLayoutProperties extends HiddenFacetFromLayoutPropertiesAbstract {

    public static HiddenFacet create(Properties properties, FacetHolder holder) {
        final Where where = hidden(properties);
        return where != null && where != Where.NOT_SPECIFIED ? new HiddenFacetOnCollectionFromLayoutProperties(where, holder): null;
    }

    private HiddenFacetOnCollectionFromLayoutProperties(Where where, FacetHolder holder) {
        super(where, holder);
    }

}
