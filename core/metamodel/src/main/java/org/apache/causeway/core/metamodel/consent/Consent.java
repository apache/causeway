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
package org.apache.causeway.core.metamodel.consent;

import java.io.Serializable;
import java.util.Optional;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.experimental.Accessors;

public interface Consent {

    //XXX record candidate
    @lombok.Value @Accessors(fluent=true)
    public static class VetoReason implements Serializable {
        private static final long serialVersionUID = 1L;
        /** Reason inferred by the framework or explicitly given otherwise. */
        private final boolean explicit;
        private final @NonNull String string;
        public static VetoReason inferred(final String reason) {
            _Assert.assertTrue(_Strings.isNotEmpty(reason));
            return new VetoReason(false, reason);
        }
        public static VetoReason explicit(final String reason) {
            _Assert.assertTrue(_Strings.isNotEmpty(reason));
            return new VetoReason(true, reason);
        }
        public Optional<VetoReason> toOptional() {
            return Optional.of(this);
        }
        public VetoReason concatenate(final VetoReason other) {
            return new VetoReason(this.explicit || other.explicit, this.string + "; " + other.string);
        }
    }


    /**
     * Returns true if this object is giving permission.
     */
    boolean isAllowed();

    /**
     * Returns true if this object is NOT giving permission.
     */
    boolean isVetoed();

    /**
     * Optionally the {@link VetoReason}, why consent is being vetoed, based on whether not allowed.
     * <p>
     * Will correspond to the {@link InteractionResult#getReason() reason} in
     * the contained {@link #getInteractionResult() InteractionResult} (if one
     * was specified).
     */
    Optional<VetoReason> getReason();

    /**
     * Optionally the {@link VetoReason} as String, why consent is being vetoed, based on whether not allowed.
     */
    default Optional<String> getReasonAsString() {
        return getReason().map(VetoReason::string);
    }


    /**
     * Description of the interaction that this consent represents.
     *
     * <p>
     * May be <tt>null</tt>.
     */
    String getDescription();

    /**
     * Allows the description of the interaction to which this consent relates
     * to be specified or refined.
     *
     * @param description
     * @return this consent
     */
    Consent setDescription(String description);

    /**
     * The {@link InteractionResult} that created this {@link Consent}.
     *
     * @return - may be <tt>null</tt> if created as a legacy {@link Consent}.
     *
     */
    public InteractionResult getInteractionResult();

}
