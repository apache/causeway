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

package org.apache.isis.metamodel.consent;

import java.util.ArrayList;
import java.util.List;

public class InteractionResultSet {

    private final List<InteractionResult> results = new ArrayList<InteractionResult>();
    private InteractionResult firstResult = null;

    public InteractionResultSet() {
    }

    public InteractionResultSet add(final InteractionResult result) {
        if (firstResult == null) {
            firstResult = result;
        }
        this.results.add(result);
        return this;
    }

    /**
     * Empty only if all the {@link #add(InteractionResult) contained}
     * {@link InteractionResult}s are also
     * {@link InteractionResult#isNotVetoing() empty}.
     */
    public boolean isAllowed() {
        return !isVetoed();
    }

    /**
     * Vetoed if any of the {@link #add(InteractionResult) contained}
     * {@link InteractionResult}s are also {@link InteractionResult#isVetoing()
     * not empty}.
     *
     * @return
     */
    public boolean isVetoed() {
        for (final InteractionResult result : results) {
            if (result.isVetoing()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link Consent} corresponding to
     * {@link #getInteractionResult()}, or an {@link Allow} if there have been
     * no {@link InteractionResult}s {@link #add(InteractionResult) added}.
     *
     * @return
     */
    public Consent createConsent() {
        final InteractionResult interactionResult = getInteractionResult();
        if (interactionResult == null) {
            return Allow.DEFAULT;
        }
        return interactionResult.createConsent();
    }

    /**
     * Returns the &quot;best&quot; contained {@link InteractionResult}.
     *
     * <p>
     * This will be the first {@link InteractionResult} that has vetoed the
     * interaction, or the first {@link InteractionResult}
     * {@link #add(InteractionResult) added} if none have vetoed.
     *
     * @return
     */
    public InteractionResult getInteractionResult() {
        for (final InteractionResult result : results) {
            if (!result.isNotVetoing()) {
                return result;
            }
        }
        return firstResult != null ? firstResult : null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
