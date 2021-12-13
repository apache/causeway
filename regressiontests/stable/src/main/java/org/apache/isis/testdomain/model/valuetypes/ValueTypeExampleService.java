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
package org.apache.isis.testdomain.model.valuetypes;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Value;
import lombok.val;

@Service
public class ValueTypeExampleService {

    @Inject ValueSemanticsResolver valueSemanticsResolver;
    @Inject List<ValueTypeExample<?>> examples;

    @Value(staticConstructor = "of")
    public static class Scenario implements Comparable<Scenario> {
        static Scenario of(final ValueTypeExample<?> example) {
            val name = String.format("%s", example.getValueType().getName());
            return Scenario.of(name, Arguments.of(
                    name,
                    example.getValueType(),
                    example));
        }
        String name;
        Arguments arguments;
        @Override public int compareTo(final Scenario other) {
            return _Strings.compareNullsFirst(this.name, other.name);
        }
    }

    public Stream<ValueTypeExample<?>> streamExamples() {
        return examples.stream();
    }

    public Stream<Scenario> streamScenarios() {
        val sortedScenarios = streamExamples()
            .map(Scenario::of)
            .collect(Collectors.toCollection(TreeSet::new));
        return sortedScenarios.stream();
    }

}
