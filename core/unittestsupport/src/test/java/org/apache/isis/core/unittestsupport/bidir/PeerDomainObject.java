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
package org.apache.isis.core.unittestsupport.bidir;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Not instantiable; the {@link PeerDomainObjectForTesting} must be used in the bidir contract testing instead.
 */
@PersistenceCapable
public abstract class PeerDomainObject {

    // {{ Next (property)
    @Persistent(mappedBy="previous")
    private PeerDomainObject next;

    public PeerDomainObject getNext() {
        return next;
    }

    public void setNext(final PeerDomainObject next) {
        this.next = next;
    }
    public void modifyNext(final PeerDomainObject next) {
        PeerDomainObject currentNext = getNext();
        // check for no-op
        if (next == null || next.equals(currentNext)) {
            return;
        }
        // dissociate existing
        clearNext();
        // associate new
        next.setPrevious(this);
        setNext(next);
    }

    public void clearNext() {
        PeerDomainObject currentNext = getNext();
        // check for no-op
        if (currentNext == null) {
            return;
        }
        // dissociate existing
        currentNext.setPrevious(null);
        setNext(null);
    }
    // }}



    // {{ Previous (property)
    private PeerDomainObject previous;

    public PeerDomainObject getPrevious() {
        return previous;
    }

    public void setPrevious(final PeerDomainObject previous) {
        this.previous = previous;
    }

    public void modifyPrevious(final PeerDomainObject previous) {
        PeerDomainObject currentPrevious = getPrevious();
        // check for no-op
        if (previous == null || previous.equals(currentPrevious)) {
            return;
        }
        // delegate to parent(s) to (re-)associate
        if (currentPrevious != null) {
            currentPrevious.clearNext();
        }
        previous.modifyNext(this);
    }

    public void clearPrevious() {
        PeerDomainObject currentPrevious = getPrevious();
        // check for no-op
        if (currentPrevious == null) {
            return;
        }
        // delegate to parent to dissociate
        currentPrevious.clearNext();
    }
    // }}



}
