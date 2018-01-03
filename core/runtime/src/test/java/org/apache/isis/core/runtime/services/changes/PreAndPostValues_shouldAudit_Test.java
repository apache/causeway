package org.apache.isis.core.runtime.services.changes;

import org.junit.Test;

import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreAndPostValues_shouldAudit_Test {

    @Test
    public void just_created() {
        final PreAndPostValues papv = PreAndPostValues.pre(IsisTransaction.Placeholder.NEW);
        papv.setPost("Foo");

        assertTrue(papv.shouldAudit());
    }
    @Test
    public void just_deleted() {
        final PreAndPostValues papv = PreAndPostValues.pre("Foo");
        papv.setPost(IsisTransaction.Placeholder.DELETED);

        assertTrue(papv.shouldAudit());
    }
    @Test
    public void changed() {
        final PreAndPostValues papv = PreAndPostValues.pre("Foo");
        papv.setPost("Bar");

        assertTrue(papv.shouldAudit());
    }
    @Test
    public void unchanged() {
        final PreAndPostValues papv = PreAndPostValues.pre("Foo");
        papv.setPost("Foo");

        assertFalse(papv.shouldAudit());
    }
    @Test
    public void created_and_then_deleted() {
        final PreAndPostValues papv = PreAndPostValues.pre(IsisTransaction.Placeholder.NEW);
        papv.setPost(IsisTransaction.Placeholder.DELETED);

        assertFalse(papv.shouldAudit());
    }
}