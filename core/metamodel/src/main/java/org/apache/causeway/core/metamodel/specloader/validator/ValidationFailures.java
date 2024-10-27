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
package org.apache.causeway.core.metamodel.specloader.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;

public final class ValidationFailures implements Iterable<ValidationFailure> {

    private final Set<ValidationFailure> failures = _Sets.newConcurrentHashSet();

    public void add(final Identifier origin, final String pattern, final Object... arguments) {
        var message = String.format(pattern, arguments);
        failures.add(ValidationFailure.of(origin, message));
    }

    public void addAll(final Iterable<ValidationFailure> failures) {
        for (var failure : failures) {
            this.failures.add(failure);
        }
    }

    public void add(final ValidationFailure validationFailure) {
        failures.add(validationFailure);
    }

    public void add(final ValidationFailures validationFailures) {
        addAll(validationFailures.getFailures());
    }

    public Set<ValidationFailure> getFailures() {
        return Collections.unmodifiableSet(failures);
    }

    public ArrayList<String> getMessages() { // <-- ensure serializable result
        var messages = failures.stream() // already sorted
        .map(ValidationFailure::getMessage)
        .collect(Collectors.toCollection(ArrayList::new));
        return messages;
    }

    /**
     * @param messageFormat to include {@code %d} for the message-index and {@code %s} for the message-string
     */
    public ArrayList<String> getMessages(final String messageFormat) { // <-- ensure serializable result
        var messages = _Lists.<String>newArrayList();
        failures.stream() // already sorted
        .map(ValidationFailure::getMessage)
        .map(msg->String.format(messageFormat, messages.size()+1, msg))
        .forEach(messages::add);
        return messages;
    }

    public int getNumberOfFailures() {
        return failures.size();
    }

    @Override
    public Iterator<ValidationFailure> iterator() {
        return getFailures().iterator();
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    public Optional<String> getAsLineNumberedString() {
        if (!hasFailures()) {
            return Optional.empty();
        }
        return Optional.of(toLineNumberedString(getMessages()));
    }

    // -- HELPER

    private static String toLineNumberedString(final Collection<String> messages) {
        var buf = new StringBuilder();
        int i=0;
        for (var message : messages) {
            buf.append(++i).append(": ").append(message).append("\n");
        }
        return buf.toString();
    }

}
