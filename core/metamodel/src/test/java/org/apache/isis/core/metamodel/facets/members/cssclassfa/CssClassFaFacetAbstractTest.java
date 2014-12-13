package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class CssClassFaFacetAbstractTest extends Assert {

    public static class Sanitize extends CssClassFaFacetAbstractTest {

        @Test
        public void present() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("fa foo"), is("fa foo"));
        }

        @Test
        public void presentAtEnd() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("foo fa "), is("foo fa "));
        }

        @Test
        public void missing() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("foo"), is("fa foo"));
        }

    }
}
