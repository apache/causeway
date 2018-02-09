package org.apache.isis.core.metamodel.facets.actions.action;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ActionAnnotationFacetFactoryTest_NameAndSequence_parse {

    @Test
    public void null_or_empty() throws Exception {
        assertNull(ActionAnnotationFacetFactory.NameAndSequence.parse(null));
        assertNull(ActionAnnotationFacetFactory.NameAndSequence.parse(""));
    }

    @Test
    public void too_long() throws Exception {
        assertNull(ActionAnnotationFacetFactory.NameAndSequence.parse("abc:def:ghi"));
    }

    @Test
    public void name_and_sequence() throws Exception {
        final ActionAnnotationFacetFactory.NameAndSequence ns =
                ActionAnnotationFacetFactory.NameAndSequence.parse("items:2.3");
        assertNotNull(ns);

        assertEquals("items",ns.name);
        assertEquals("2.3",ns.sequence);
    }

    @Test
    public void no_sequence() throws Exception {
        final ActionAnnotationFacetFactory.NameAndSequence ns =
                ActionAnnotationFacetFactory.NameAndSequence.parse("items");
        assertNotNull(ns);

        assertEquals("items",ns.name);
        assertEquals("1",ns.sequence);
    }


}