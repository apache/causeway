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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;
import lombok.val;

class MixinFacetAbstract_Test {

    public abstract static class Collection_numberOfChildren {
        public Collection_numberOfChildren(Object contributee) {}
        public int prop() { return 0; }
    }

    public static class SimpleObject {}
    public static class SimpleObject_numberOfChildren extends Collection_numberOfChildren {
        public SimpleObject_numberOfChildren(SimpleObject contributee) { super(contributee); }
    }

    @Nested
    class isCandidateForMain {

        @SneakyThrows
        @Test
        public void happy_case() {

            // given
            val constructor = Collection_numberOfChildren.class.getConstructor(Object.class);
            val facet = new MixinFacetAbstract(
                    Collection_numberOfChildren.class, "prop", constructor, null) {};

            val propMethodInSubclass = SimpleObject_numberOfChildren.class.getMethod("prop");

            // when
            val candidate = facet.isCandidateForMain(propMethodInSubclass);

            // then
            Assertions.assertThat(candidate).isTrue();
        }
    }
}
