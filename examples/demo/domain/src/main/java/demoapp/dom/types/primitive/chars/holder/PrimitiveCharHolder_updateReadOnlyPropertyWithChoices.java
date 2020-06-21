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
package demoapp.dom.types.primitive.chars.holder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;


@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        associateWith = "readOnlyProperty",
        associateWithSequence = "2"
)
@RequiredArgsConstructor
public class PrimitiveCharHolder_updateReadOnlyPropertyWithChoices {

    private final PrimitiveCharHolder primitiveCharHolder;

    public PrimitiveCharHolder act(char newValue) {
        primitiveCharHolder.setReadOnlyProperty(newValue);
        return primitiveCharHolder;
    }
    public char default0Act() {
        return primitiveCharHolder.getReadOnlyProperty();
    }
    public List<Character> choices0Act() {
        return Stream.of(charOf('a'), charOf('b'), charOf('c')).collect(Collectors.toList());
    }
    public String disableAct() {
        return "Not yet supported";
    }

    private static Character charOf(char x) {
        return x;
    }

}
