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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.val;

public final class ValidationFailures implements Iterable<ValidationFailure> {

    
    private final Set<ValidationFailure> failures = _Sets.newTreeSet();

    public void add(Identifier origin, String pattern, Object... arguments) {
        val message = String.format(pattern, arguments);
        failures.add(ValidationFailure.of(origin, message));
    }

    public void addAll(Iterable<ValidationFailure> failures) {
        for (val failure : failures) {
            this.failures.add(failure);
        }
    }

    public void add(ValidationFailures validationFailures) {
        addAll(validationFailures.getFailures());
    }

    public Set<ValidationFailure> getFailures() {
        return Collections.unmodifiableSet(failures);
    }
    
    public List<String> getMessages() {
        val messages = failures.stream() // already sorted
        .map(ValidationFailure::getMessage)
        .collect(Collectors.toList());
        
        return Collections.unmodifiableList(messages);
    }

    public int getNumberOfFailures() {
        return failures.size();
    }

    @Override
    public Iterator<ValidationFailure> iterator() {
        return getFailures().iterator();
    }
    
    public boolean occurred() {
        return !failures.isEmpty();
    }

    public MetaModelDeficiencies getDeficienciesIfAny() {
        if (!occurred()) {
            return null;
        }
        return MetaModelDeficiencies.of(getMessages());
    }


}
