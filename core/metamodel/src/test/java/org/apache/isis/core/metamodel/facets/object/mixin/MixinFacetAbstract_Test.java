package org.apache.isis.core.metamodel.facets.object.mixin;

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
                    Collection_numberOfChildren.class, "prop", constructor, null, null) {};

            val propMethodInSubclass = SimpleObject_numberOfChildren.class.getMethod("prop");

            // when
            val candidate = facet.isCandidateForMain(propMethodInSubclass);

            // then
            Assertions.assertThat(candidate).isTrue();
        }
    }
}
