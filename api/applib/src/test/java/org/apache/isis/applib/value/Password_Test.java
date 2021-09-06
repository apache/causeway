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
package org.apache.isis.applib.value;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class Password_Test {
    
    private static final String STARS = "********************";

    @Nested
    public class checkPassword {

        @Test
        void case_sensitive() {
            // guven
            final Password password = new Password("secret");

            // when, then
            assertFalse(password.checkPassword("SECRET"));
            assertTrue(password.checkPassword("secret"));
        }

        @Test
        void given_empty() {
            // given
            val password = new Password("");

            // when, then
            assertTrue(password.checkPassword(""));
            assertFalse(password.checkPassword("SECRET"));
        }

        @Test
        public void given_null() {

            // given
            val password = new Password(null);

            // when, then
            assertFalse(password.checkPassword(""));
            assertFalse(password.checkPassword("SECRET"));
            assertTrue(password.checkPassword(null));
            assertEquals(password, new Password(null));
        }

    }

    @Nested
    public class toString {

        @Test
        void obscures_password() {
            Password password = new Password("secret");
            assertEquals(STARS, password.toString());

            password = new Password("a very very very long password");
            assertEquals(STARS, password.toString());
        }
    }

}
