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

package org.apache.isis.core.metamodel.facets.members.disabled;

import java.util.Map;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class DisabledFacetAbstractImpl extends DisabledFacetAbstract {

    private final String reason;

    public DisabledFacetAbstractImpl(final When when, final Where where, final FacetHolder holder) {
        this(when, where, null, holder);
    }

    public DisabledFacetAbstractImpl(final When when, final Where where, final FacetHolder holder, final Semantics semantics) {
        this(when, where, null, holder, semantics);
    }

    public DisabledFacetAbstractImpl(final When when, final Where where, final String reason, final FacetHolder holder) {
        super(when, where, holder);
        this.reason = reason;
    }

    public DisabledFacetAbstractImpl(final When when, final Where where, final String reason, final FacetHolder holder, final Semantics semantics) {
        super(when, where, holder, semantics);
        this.reason = reason;
    }

    @Override
    public String disabledReason(final ObjectAdapter targetAdapter) {
        if (when() == When.ALWAYS) {
            return disabledReasonElse(ALWAYS_DISABLED_REASON);
        } else if (when() == When.NEVER) {
            return null;
        }

        // remaining tests depend upon the actual target in question
        if (targetAdapter == null) {
            return null;
        }

        if (when() == When.UNTIL_PERSISTED) {
            return targetAdapter.isTransient() ? disabledReasonElse("Disabled until persisted") : null;
        } else if (when() == When.ONCE_PERSISTED) {
            return targetAdapter.representsPersistent() ? disabledReasonElse("Disabled once persisted") : null;
        }
        return null;
    }

    private String disabledReasonElse(final String defaultReason) {
        return !Strings.isNullOrEmpty(reason) ? reason : defaultReason;
    }

    /**
     * Not API... the reason as defined in subclass
     */
    public String getReason() {
        return reason;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("reason", reason);
    }
}
