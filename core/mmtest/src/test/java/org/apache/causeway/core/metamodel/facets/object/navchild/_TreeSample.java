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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import java.util.Map;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.commons.collections.Can;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
class _TreeSample {

    static interface SampleNode {
        String name();
    }

    record A(String name,
        @CollectionLayout(navigableSubtree = "1") Can<B> childrenB,
        @CollectionLayout(navigableSubtree = "2") Map<String, C> childrenC) implements SampleNode {
    }
    record B(String name,
        @PropertyLayout(navigableSubtree = "1") C childC,
        @CollectionLayout(navigableSubtree = "2") Can<D> childrenD) implements SampleNode {
    }
    record C(String name,
        @CollectionLayout(navigableSubtree = "1") Can<D> childrenD) implements SampleNode {
    }
    record D(String name) implements SampleNode {
    }

    A sampleA() {
        var ds = Can.of(new D("d1"), new D("d2"), new D("d3"));
        var cs = Can.of(new C("c1", ds), new C("c2", ds));
        var bs = Can.of(new B("b1", cs.getElseFail(0), ds), new B("b2", cs.getElseFail(1), ds));
        var a = new A("a", bs, cs.toMap(C::name));
        return a;
    }

    String nameOf(final Object node) {
        return node instanceof SampleNode sampleNode
            ? sampleNode.name()
            : "?";
    }

    public static class SampleNodeView implements ViewModel {

        @Programmatic
        final String memento;

        public SampleNodeView(final String memento) {
            this.memento = memento;
            this.name = "TODO";
        }

        @Override
        public String viewModelMemento() {
            return memento;
        }

        @Property @Getter
        final String name;

    }

}
