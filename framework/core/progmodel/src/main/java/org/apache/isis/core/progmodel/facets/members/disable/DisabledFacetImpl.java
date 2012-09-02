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

package org.apache.isis.core.progmodel.facets.members.disable;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public class DisabledFacetImpl extends DisabledFacetAbstract {

    public DisabledFacetImpl(final When when, Where where, final FacetHolder holder) {
        super(when, where, holder);
    }

    @Override
    public String disabledReason(final ObjectAdapter targetAdapter) {
        if (when() == When.ALWAYS) {
            return "Always disabled";
        } else if (when() == When.NEVER) {
            return null;
        }

        // remaining tests depend upon the actual target in question
        if (targetAdapter == null) {
            return null;
        }

        if (when() == When.UNTIL_PERSISTED) {
            return targetAdapter.isTransient() ? "Disabled until persisted" : null;
        } else if (when() == When.ONCE_PERSISTED) {
            return targetAdapter.representsPersistent() ? "Disabled once persisted" : null;
        }
        return null;
    }

}
