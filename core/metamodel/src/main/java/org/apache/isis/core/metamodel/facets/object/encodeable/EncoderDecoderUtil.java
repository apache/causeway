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
package org.apache.isis.core.metamodel.facets.object.encodeable;

import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public final class EncoderDecoderUtil {

    private EncoderDecoderUtil() {
    }

    public static Class<?> encoderDecoderOrNull(final Class<?> candidateClass, final String classCandidateName) {
        final Class<?> type = candidateClass != null ? ClassUtil.implementingClassOrNull(candidateClass.getName(), EncoderDecoder.class, FacetHolder.class) : null;
        return type != null ? type : ClassUtil.implementingClassOrNull(classCandidateName, EncoderDecoder.class, FacetHolder.class);
    }

}
