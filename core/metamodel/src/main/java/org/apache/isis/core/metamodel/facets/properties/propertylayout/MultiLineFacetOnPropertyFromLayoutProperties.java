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
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacetAbstract;

public class MultiLineFacetOnPropertyFromLayoutProperties extends MultiLineFacetAbstract {

    public static MultiLineFacet create(Properties properties, FacetHolder holder) {
        final int multiLine = multiLine(properties);
        return multiLine > 1? new MultiLineFacetOnPropertyFromLayoutProperties(multiLine, holder): null;
    }

    private MultiLineFacetOnPropertyFromLayoutProperties(int multiLine, FacetHolder holder) {
        super(multiLine, false, holder);
    }

    private static int multiLine(Properties properties) {
        if(properties == null) {
            return -1;
        }
        String multiLine = Strings.emptyToNull(properties.getProperty("multiLine"));
        if(multiLine == null) {
            return -1;
        }
        return Integer.parseInt(multiLine);
    }
}
