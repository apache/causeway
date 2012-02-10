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


package test.org.apache.isis.object.security;

import org.apache.isis.noa.authentication.Sessionessionession;
import org.apache.isis.nof.reflect.peer.MemberIdentifier;
import org.apache.isis.nof.reflect.security.AuthorisationManager;


public class MockAuthorisationManager implements AuthorisationManager {
    private boolean visible;
    private boolean usable;

    public boolean isUsable(final Session session, final MemberIdentifier identifier) {
        return usable;
    }

    public boolean isVisible(final Session session, final MemberIdentifier identifier) {
        return visible;
    }

    public void setupVisible(final boolean b) {
        visible = b;
    }

    public void setupUsable(final boolean usable) {
        this.usable = usable;
    }

    public void init() {}

    public void shutdown() {}

    public boolean isEditable(final Session session, final MemberIdentifier identifier) {
        return false;
    }
}
