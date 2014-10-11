package org.apache.isis.viewer.wicket.ui.pages;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class PageAbstractTest {

    public static class AsCssStyle extends PageAbstractTest {

        @Test
        public void withSpacesAndCapitals() throws Exception {
            assertThat(PageAbstract.asCssStyle("Simple App"), is("simple-app"));
        }

        @Test
        public void withOtherCharacters() throws Exception {
            assertThat(PageAbstract.asCssStyle("Kitchen Sink (Demo) App"), is("kitchen-sink-demo-app"));
        }

    }
}