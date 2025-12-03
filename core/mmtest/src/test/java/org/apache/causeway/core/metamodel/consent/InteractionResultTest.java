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

import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;

class InteractionResultTest {

    private InteractionResult.Builder builder = InteractionResult.builder(Mockito.mock(InteractionEvent.class));

    private static interface InteractionAdvisorFacet 
    extends InteractionAdvisor, Facet {
    }
    
    public static InteractionAdvisor interactionAdvisor() {
    	return new InteractionAdvisorFacet() {
    		@Override public boolean semanticEquals(final @NonNull Facet other) {
    			return this == other;
    		}
    		@Override public void visitAttributes(final BiConsumer<String, Object> visitor) {
    		}
    		@Override public Class<? extends Facet> facetType() {
    			return null;
    		}
    		@Override public org.apache.causeway.core.metamodel.facetapi.FacetHolder facetHolder() {
    			return null;
    		}
    		@Override public Precedence precedence() {
    			return Facet.Precedence.FALLBACK;
    		}
    	};
    }
    
    @Test
    void shouldHaveNullReasonWhenJustInstantiated() {
        var result = builder.build();
        assertEquals(null, extractReason(result));
    }

    @Test
    void shouldBeEmptyWhenJustInstantiated() {
        var result = builder.build();
        assertFalse(result.isVetoing());
        assertTrue(result.isAllowing());
    }

    @Test
    void shouldHaveNonNullReasonWhenAdvisedWithNonNull() {
        advise(vetoReason("foo"), interactionAdvisor());
        var result = builder.build();
        assertEquals("foo", extractReason(result));
    }

    @Test
    void shouldConcatenateAdviseWhenAdvisedWithNonNull() {
        advise(vetoReason("foo"), interactionAdvisor());
        advise(vetoReason("bar"), interactionAdvisor());
        var result = builder.build();
        assertEquals("foo; bar", extractReason(result));
    }

    @Test
    void shouldNotBeEmptyWhenAdvisedWithNonNull() {
        advise(vetoReason("foo"), interactionAdvisor());
        var result = builder.build();
        assertTrue(result.isVetoing());
        assertFalse(result.isAllowing());
    }

    @Test
    void shouldThrowWhenAdvisedWithNull() {
        assertThrowsExactly(NullPointerException.class, ()->advise(null, interactionAdvisor()));
        assertThrowsExactly(NullPointerException.class, ()->advise(vetoReason("foo"), null));
    }

    // -- HELPER

    private void advise(VetoReason vetoReason, InteractionAdvisor forTesting) {
        builder.addAdvise(vetoReason, forTesting);
    }

    static Consent.VetoReason vetoReason(final String reasonString) {
        return Consent.VetoReason.explicit(reasonString);
    }

    static String extractReason(final InteractionResult result) {
        return result.vetoReason().map(VetoReason::string).orElse(null);
    }

}
