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

package org.apache.isis.security.authorization.standard;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.components.ApplicationScopedComponent;

public interface Authorizor extends ApplicationScopedComponent {

    void init();
    void shutdown();

    boolean isVisibleInAnyRole(final Identifier identifier);
    boolean isUsableInAnyRole(final Identifier identifier);

    /**
     * Checked for each of the user's roles.
     */
    boolean isVisibleInRole(final String role, final Identifier identifier);

    /**
     * Checked for each of the user's roles.
     */
    boolean isUsableInRole(final String role, final Identifier identifier);

    // -- NOP IMPLEMENTATIOn 

    final static Authorizor NOP = new Authorizor() {

        @Override
        public void init() {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public boolean isVisibleInRole(final String user, final Identifier identifier) {
            return true;
        }

        @Override
        public boolean isUsableInRole(final String role, final Identifier identifier) {
            return true;
        }

        @Override
        public boolean isVisibleInAnyRole(Identifier identifier) {
            return true;
        }

        @Override
        public boolean isUsableInAnyRole(Identifier identifier) {
            return true;
        }
    };

    public static Authorizor nop() {
        return NOP;
    }

}
