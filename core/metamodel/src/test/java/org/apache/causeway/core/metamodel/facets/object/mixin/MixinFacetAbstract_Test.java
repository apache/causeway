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
package org.apache.causeway.core.metamodel.facets.object.mixin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.causeway.commons.internal.reflection._GenericResolver;

class MixinFacetAbstract_Test {

    abstract static class Collection_numberOfChildren {
        public Collection_numberOfChildren(final Object contributee) {}
        public int prop() { return 0; }
    }

    static class SimpleObject {}
    static class SimpleObject_numberOfChildren extends Collection_numberOfChildren {
        public SimpleObject_numberOfChildren(final SimpleObject contributee) { super(contributee); }
    }

    @Test
    void happy_case() throws Exception {

        // given
        var constructor = Collection_numberOfChildren.class.getConstructor(Object.class);
        var facet = new MixinFacetAbstract(
                Collection_numberOfChildren.class, "prop", constructor, null) {};

        var propMethodInSubclass = _GenericResolver.testing
                .resolveMethod(SimpleObject_numberOfChildren.class, "prop");

        // when
        var candidate = facet.isCandidateForMain(propMethodInSubclass);

        // then
        Assertions.assertThat(candidate).isTrue();
    }

}