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

package org.apache.isis.runtimes.embedded;

import java.util.List;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.authentication.AuthenticationSession;

public class EmbeddedContextNoop implements EmbeddedContext {

    @Override
    public AuthenticationSession getAuthenticationSession() {
        return null;
    }

    @Override
    public Localization getLocalization() {
        return null;
    }

    @Override
    public PersistenceState getPersistenceState(final Object object) {
        return null;
    }

    @Override
    public Object instantiate(final Class<?> type) {
        return null;
    }

    @Override
    public void makePersistent(final Object object) {
    }

    @Override
    public void remove(final Object object) {
    }

    @Override
    public void resolve(final Object parent) {
    }

    @Override
    public void resolve(final Object parent, final Object field) {
    }

    @Override
    public void objectChanged(final Object object) {
    }

    @Override
    public <T> List<T> allMatchingQuery(final Query<T> query) {
        return null;
    }

    @Override
    public <T> T firstMatchingQuery(final Query<T> query) {
        return null;
    }

    @Override
    public void commit() {
    }

    @Override
    public boolean flush() {
        return false;
    }

    @Override
    public void informUser(final String message) {
    }

    @Override
    public void warnUser(final String message) {
    }

    @Override
    public void raiseError(final String message) {
    }


}
