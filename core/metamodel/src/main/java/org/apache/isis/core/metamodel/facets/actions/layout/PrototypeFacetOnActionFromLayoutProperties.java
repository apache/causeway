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

package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Properties;
import com.google.common.base.Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacetAbstract;

public class PrototypeFacetOnActionFromLayoutProperties extends PrototypeFacetAbstract {

    public static PrototypeFacet create(Properties properties, FacetHolder holder) {
        final boolean prototype = prototype(properties);
        return prototype ? new PrototypeFacetOnActionFromLayoutProperties(holder): null;
    }

    private PrototypeFacetOnActionFromLayoutProperties(FacetHolder holder) {
        super(holder);
    }

    private static boolean prototype(Properties properties) {
        if(properties == null) {
            return false;
        }
        String prototype = Strings.emptyToNull(properties.getProperty("prototype"));
        if(prototype == null) {
            return false;
        }
        return Boolean.parseBoolean(prototype);
    }

}
