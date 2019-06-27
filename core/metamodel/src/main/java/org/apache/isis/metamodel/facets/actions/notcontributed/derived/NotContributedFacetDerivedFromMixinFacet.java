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
package org.apache.isis.metamodel.facets.actions.notcontributed.derived;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.actions.notcontributed.NotContributedFacetAbstract;

/**
 * By default, any mixin actions are contributed as both actions and associations.  This can be overridden using
 * {@link ActionLayout#contributed()}.
 */
public class NotContributedFacetDerivedFromMixinFacet extends NotContributedFacetAbstract {

    public NotContributedFacetDerivedFromMixinFacet(
            final FacetHolder holder) {
        super(Contributed.AS_BOTH, holder, Derivation.DERIVED);
    }

}
