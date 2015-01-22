package org.apache.isis.core.metamodel.facets.members.cssclassfa;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CssClassFaFacetAbstractTest {

    public static class Sanitize extends CssClassFaFacetAbstractTest {

        @Test
        public void present() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("fa foo"), is("fa fa-fw foo"));
        }

        @Test
        public void presentAtEnd() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("foo fa "), is("fa fa-fw foo"));
        }

        @Test
        public void missing() throws Exception {
            assertThat(CssClassFaFacetAbstract.sanitize("foo"), is("fa fa-fw foo"));
        }
    }
}
