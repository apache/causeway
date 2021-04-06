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
package org.apache.isis.core.metamodel.facets.objectvalue.mustsatisfyspec;

import java.util.List;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.spec.Specification2;
import org.apache.isis.applib.util.ReasonBuffer;

/**
 * Encapsulates the algorithm for evaluating {@link Specification}s that might optionally be
 * translatable (ie extend {@link Specification2}).
 */
public class SpecificationEvaluator {

    private final TranslationService translationService;
    private final TranslationContext translationContext;

    public SpecificationEvaluator(
            final TranslationService translationService,
            final TranslationContext translationContext) {
        this.translationService = translationService;
        this.translationContext = translationContext;
    }

    public Evaluation evaluation() {
        return new Evaluation();
    }

    public class Evaluation {

        final ReasonBuffer reasonBuffer = new ReasonBuffer();

        public Evaluation evaluate(final List<Specification> specifications, final Object proposedObject) {
            for (final Specification specification : specifications) {
                evaluate(specification, proposedObject);
            }
            return this;
        }

        public Evaluation evaluate(final Specification specification, final Object proposedObject) {
            if (specification instanceof Specification2) {
                final Specification2 specification2 = (Specification2) specification;
                final TranslatableString translatableReason = specification2.satisfiesTranslatable(proposedObject);
                if (translatableReason != null) {
                    final String translatedReason = translatableReason.translate(translationService, translationContext);
                    reasonBuffer.append(translatedReason);
                }
            } else {
                final String satisfies = specification.satisfies(proposedObject);
                reasonBuffer.append(satisfies);
            }
            return this;
        }

        public String getReason() {
            return reasonBuffer.getReason();
        }
    }

}
