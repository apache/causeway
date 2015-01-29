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
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacetAbstract;


public class NotContributedFacetForLayoutProperties extends NotContributedFacetAbstract {

    public static NotContributedFacet create(final Properties properties, final FacetHolder holder) {
        final Contributed contributed = contributing(properties);
        if(contributed == null) {
            return null;
        }
        return contributed != null? new NotContributedFacetForLayoutProperties(NotContributed.As.from(contributed), holder): null;
    }

    private NotContributedFacetForLayoutProperties(
            final NotContributed.As as,
            final FacetHolder holder) {
        super(as, holder);
    }


    private static Contributed contributing(final Properties properties) {
        if(properties == null) {
            return null;
        }
        String contributing = Strings.emptyToNull(properties.getProperty("contributing"));
        if(contributing == null) {
            // alternate key
            contributing = Strings.emptyToNull(properties.getProperty("contributed"));
        }
        return contributing != null? Contributed.valueOf(contributing): null;
    }

}
