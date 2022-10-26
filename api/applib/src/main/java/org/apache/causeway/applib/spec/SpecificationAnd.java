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
package org.apache.causeway.applib.spec;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.util.ReasonBuffer;

/**
 * Adapter to make it easy to perform boolean algebra on {@link Specification}s.
 *
 * <p>
 * Subclasses represent the intersection of multiple {@link Specification}s. An
 * implementation should instantiate the {@link Specification}s to be satisfied
 * in its constructor.
 *
 * <p>
 * For example:
 *
 * <pre>
 * public class MilkAndSugarSpec extends SpecificationAnd {
 *     public MilkAndSugarSpec() {
 *         super(new MustBeMilkySpec(), new TwoLumpsOfSugarSpec());
 *     }
 * }
 * </pre>
 *
 * @see SpecificationOr
 * @see SpecificationNot
 * @since 1.x {@index}
 */
public abstract class SpecificationAnd implements Specification {

    private final Specification[] specifications;

    public SpecificationAnd(final Specification... specifications) {
        this.specifications = specifications;
    }

    @Programmatic
    @Override
    public String satisfies(final Object obj) {
        final ReasonBuffer buf = new ReasonBuffer();
        for (final Specification specification : specifications) {
            final String reasonNotSatisfiedIfAny = specification.satisfies(obj);
            buf.append(reasonNotSatisfiedIfAny);
        }
        return buf.getReason(); // may be null if all were satisfied.
    }

}
