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
package org.apache.isis.applib.spec;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.applib.util.ReasonBuffer;

/**
 * Adapter to make it easy to perform boolean algebra on {@link Specification}s.
 *
 * <p>
 * Subclasses represent the conjunction of multiple {@link Specification}s. An
 * implementation should instantiate the {@link Specification}s to be satisfied
 * in its constructor.
 *
 * <p>
 * For example:
 *
 * <pre>
 * public class TeaOrCoffeeSpec extends SpecificationOr {
 *     public TeaOrCoffeeSpec() {
 *         super(new MustBeTeaSpec(), new MustBeCoffeeSpec());
 *     }
 * }
 * </pre>
 *
 * @see SpecificationAnd
 * @see SpecificationNot
 * @since 1.x {@index}
 */
public abstract class SpecificationOr implements Specification {

    private final Specification[] specifications;

    public SpecificationOr(final Specification... specifications) {
        this.specifications = specifications;
    }

    @Programmatic
    @Override
    public String satisfies(final Object obj) {
        final ReasonBuffer buf = new ReasonBuffer();
        for (final Specification specification : specifications) {
            final String reasonNotSatisfiedIfAny = specification.satisfies(obj);
            if (reasonNotSatisfiedIfAny == null) {
                // at least one is ok, so all is ok.
                return null;
            }
            buf.append(reasonNotSatisfiedIfAny);
        }
        return buf.getReason(); // will be non-null if none were satisfied.
    }

}
