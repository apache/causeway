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

package org.apache.isis.core.metamodel.facets.fallback;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;

/**
 * Has a name of <tt>null</tt>.
 *
 * <p>
 * TODO: should this instead be the empty string?
 */
public class NamedFacetNone extends NamedFacetAbstract {

    public static final boolean ESCAPED = true;

    public NamedFacetNone(final FacetHolder holder) {
        super(null, ESCAPED, holder);
    }

    @Override
    public boolean isNoop() {
        return true;
    }

}
