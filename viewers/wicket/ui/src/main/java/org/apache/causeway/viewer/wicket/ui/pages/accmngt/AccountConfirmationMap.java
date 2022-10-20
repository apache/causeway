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
package org.apache.causeway.viewer.wicket.ui.pages.accmngt;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.util.collections.MostRecentlyUsedMap;

import lombok.val;

/**
 * A map that contains the emails to be verified. It has a constraint on the maximum entries that it
 * can contain, and a constraint on the duration of time an entry is considered valid/non-expired
 */
public class AccountConfirmationMap extends MostRecentlyUsedMap<String, Object>
{
    private static final long serialVersionUID = 1L;

    public static final MetaDataKey<AccountConfirmationMap> KEY = new MetaDataKey<AccountConfirmationMap>() {
        private static final long serialVersionUID = 1L;
    };

    /**
     * The actual object that is stored as a value of the map. It wraps the email and
     * assigns it a creation time.
     */
    private static class Value {
        /** the original email to store */
        private String email;

        /** the time when this email is stored */
        private Instant creationTime;

        public boolean isExpired(final Duration lifetime) {
            val elapsedTime = Duration.between(creationTime, Instant.now());
            val isExpired = lifetime.minus(elapsedTime).isNegative();
            return isExpired;
        }
    }

    /**
     * The duration of time before a {@link Value} is considered as expired
     */
    private final Duration lifetime;

    /**
     * Construct.
     *
     * @param maxEntries
     *            how much entries this map can contain
     * @param lifetime
     *            the duration of time to keep an entry in the map before considering it expired
     */
    public AccountConfirmationMap(final int maxEntries, final Duration lifetime){
        super(maxEntries);
        this.lifetime = lifetime;
    }

    @Override
    protected synchronized boolean removeEldestEntry(final java.util.Map.Entry<String, Object> eldest) {
        boolean removed = super.removeEldestEntry(eldest);
        if (removed == false) {
            val value = (Value)eldest.getValue();
            if (value != null) {
                if(value.isExpired(lifetime)) {
                    removedValue = value.email;
                    removed = true;
                }
            }
        }
        return removed;
    }

    @Override
    public String put(final String key, final Object email) {
        if (!(email instanceof String)) {
            throw new IllegalArgumentException(AccountConfirmationMap.class.getSimpleName() +
                    " can store only instances of " + String.class.getSimpleName() + ": " + email);
        }

        val value = new Value();
        value.creationTime = Instant.now();
        value.email = (String)email;

        Value oldValue;
        synchronized (this) {
            oldValue = (Value)super.put(key, value);
        }

        return oldValue != null ? oldValue.email : null;
    }

    @Override
    public String get(final Object key) {
        String result = null;
        Value value;
        synchronized (this) {
            value = (Value)super.get(key);
        }
        if (value != null) {
            if(value.isExpired(lifetime)) {
                // expired, remove it
                remove(key);
            } else {
                result = value.email;
            }
        }
        return result;
    }

    @Override
    public String remove(final Object key) {
        Value removedValue;
        synchronized (this) {
            removedValue = (Value)super.remove(key);
        }
        return removedValue != null ? removedValue.email : null;
    }

    @Override
    public void putAll(final Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }
}
