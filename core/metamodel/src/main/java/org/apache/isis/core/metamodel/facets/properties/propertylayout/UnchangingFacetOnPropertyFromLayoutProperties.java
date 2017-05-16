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

package org.apache.isis.core.metamodel.facets.properties.propertylayout;

import java.util.Properties;

import com.google.common.base.Strings;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacet;
import org.apache.isis.core.metamodel.facets.properties.renderunchanged.UnchangingFacetAbstract;

public class UnchangingFacetOnPropertyFromLayoutProperties extends UnchangingFacetAbstract {

    public static UnchangingFacet create(Properties properties, FacetHolder holder) {
        final boolean isUnchanging = unchanging(properties);
        return new UnchangingFacetOnPropertyFromLayoutProperties(isUnchanging, holder);
    }

    private UnchangingFacetOnPropertyFromLayoutProperties(final boolean isUnchanging, FacetHolder holder) {
        super(isUnchanging, holder);
    }

    private static boolean unchanging(Properties properties) {
        if(properties == null) {
            return false;
        }
        String unchanging = Strings.emptyToNull(properties.getProperty("unchanging"));
        return unchanging != null && Boolean.parseBoolean(unchanging);
    }

}
