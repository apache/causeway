/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
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

package org.apache.isis.core.metamodel.facets.collections.collection.defaultview;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public class DefaultViewFacetForCollectionLayoutAnnotation extends DefaultViewFacetAbstract {

    private DefaultViewFacetForCollectionLayoutAnnotation(String value, FacetHolder holder) {
        super(value, holder);
    }

    public static DefaultViewFacet create(CollectionLayout collectionLayout, FacetHolder holder) {
        if (collectionLayout == null) {
            return null;
        }

        final String defaultView = Strings.emptyToNull(collectionLayout.defaultView());
        return defaultView != null ? new DefaultViewFacetForCollectionLayoutAnnotation(defaultView, holder) : null;
    }
}
