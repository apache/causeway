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

package org.apache.isis.core.metamodel.facets.param.multiline.annotation;

import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.multiline.MultiLineFacetAbstract;

/**
 * @deprecated
 */
@Deprecated
public class MultiLineFacetOnParameterAnnotation extends MultiLineFacetAbstract {

    public MultiLineFacetOnParameterAnnotation(final int numberOfLines, final boolean preventWrapping, final FacetHolder holder) {
        super(numberOfLines, preventWrapping, holder);
    }

    static MultiLineFacet create(final MultiLine annotation, final Class<?> parameterType, final FacetHolder holder) {
        if (annotation != null) {
            return new MultiLineFacetOnParameterAnnotation(annotation.numberOfLines(), annotation.preventWrapping(), holder);
        }

        if (!Annotations.isString(parameterType)) {
            return null;
        }

        return null;
    }
}
