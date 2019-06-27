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
package org.apache.isis.metamodel.specloader.validator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import org.apache.isis.commons.internal.collections._Sets;

public final class ValidationFailures implements Iterable<String> {

    private final Set<String> messages = _Sets.newLinkedHashSet();

    public void add(final String pattern, final Object... arguments) {
        final String message = String.format(pattern, arguments);
        messages.add(message);
    }

    public void addAll(final Iterable<String> messages) {
        for (final String message : messages) {
            this.messages.add(message);
        }
    }

    public void add(final ValidationFailures validationFailures) {
        addAll(validationFailures.getMessages());
    }

    public MetaModelDeficiencies getDeficienciesIfAny() {
        if (!occurred()) {
            return null;
        }
        final SortedSet<String> sortedMessages = _Sets.newTreeSet(messages);
        return MetaModelDeficiencies.of(sortedMessages);
    }

    public boolean occurred() {
        return !messages.isEmpty();
    }

    public Set<String> getMessages() {
        return Collections.unmodifiableSet(messages);
    }

    public int getNumberOfMessages() {
        return messages.size();
    }

    @Override
    public Iterator<String> iterator() {
        return getMessages().iterator();
    }


}
