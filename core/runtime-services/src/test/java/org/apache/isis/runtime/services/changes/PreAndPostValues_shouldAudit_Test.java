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
package org.apache.isis.runtime.services.changes;

import org.junit.Test;

import org.apache.isis.runtime.system.transaction.IsisTransactionPlaceholder;
import org.apache.isis.runtime.system.transaction.PreAndPostValues;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreAndPostValues_shouldAudit_Test {

    @Test
    public void just_created() {
        final PreAndPostValues papv = PreAndPostValues.pre(IsisTransactionPlaceholder.NEW);
        papv.setPost("Foo");

        assertTrue(papv.shouldAudit());
    }
    @Test
    public void just_deleted() {
        final PreAndPostValues papv = PreAndPostValues.pre("Foo");
        papv.setPost(IsisTransactionPlaceholder.DELETED);

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
        final PreAndPostValues papv = PreAndPostValues.pre(IsisTransactionPlaceholder.NEW);
        papv.setPost(IsisTransactionPlaceholder.DELETED);

        assertFalse(papv.shouldAudit());
    }
}