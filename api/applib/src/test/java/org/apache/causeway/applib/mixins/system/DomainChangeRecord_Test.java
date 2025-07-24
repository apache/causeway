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
package org.apache.causeway.applib.mixins.system;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.services.bookmark.Bookmark;

class DomainChangeRecord_Test {

    @Test
    void when_populated() {
        final var dcr = new DomainChangeRecord.Empty() {
            @Override
            public Bookmark getTarget() {
                return Bookmark.forLogicalTypeNameAndIdentifier("Customer", "12345");
            }
        };

        assertThat(dcr.getTargetLogicalTypeName()).isEqualTo("Customer");
    }

    @Test
    void when_not_populated() {
        final var dcr = new DomainChangeRecord.Empty() {
            @Override
            public Bookmark getTarget() {
                return null;
            }
        };

        assertThat(dcr.getTargetLogicalTypeName()).isNull();
    }

}