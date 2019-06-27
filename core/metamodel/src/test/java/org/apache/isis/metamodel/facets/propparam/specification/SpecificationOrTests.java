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

package org.apache.isis.metamodel.facets.propparam.specification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.applib.spec.Specification;
import org.apache.isis.applib.spec.SpecificationOr;

public class SpecificationOrTests {

    private final Specification alwaysSatisfied = new SpecificationAlwaysSatisfied();
    private final Specification neverSatisfied = new SpecificationNeverSatisfied();

    @Test
    public void satisfiedIfNone() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(nullValue()));
    }

    @Test
    public void satisfiedIfOneAndOkay() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
                super(alwaysSatisfied);
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(nullValue()));
    }

    @Test
    public void notSatisfiedIfOneAndNotOkay() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
                super(neverSatisfied);
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(not(nullValue())));
        assertThat(mySpecOr.satisfies(null), is("not satisfied"));
    }

    @Test
    public void satisfiedIfTwoAndOneIsNotOkay() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
                super(alwaysSatisfied, neverSatisfied);
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(nullValue()));
    }

    @Test
    public void satisfiedIfTwoAndBothAreOkay() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
                super(alwaysSatisfied, alwaysSatisfied);
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(nullValue()));
    }

    @Test
    public void notSatisfiedIfTwoAndBothAreNotOkayWithConcatenatedReason() {
        class MySpecOr extends SpecificationOr {
            public MySpecOr() {
                super(neverSatisfied, neverSatisfied);
            }
        }
        ;
        final Specification mySpecOr = new MySpecOr();
        assertThat(mySpecOr.satisfies(null), is(not(nullValue())));
        assertThat(mySpecOr.satisfies(null), is("not satisfied; not satisfied"));
    }

}
