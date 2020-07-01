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
package demoapp.dom.types.primitive.bytes.holder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;


@Action(
        semantics = SemanticsOf.IDEMPOTENT,
        associateWith = "readOnlyProperty",
        associateWithSequence = "4"
)
@ActionLayout(promptStyle = PromptStyle.AS_CONFIGURED, named = "with choices")
@RequiredArgsConstructor
public class PrimitiveByteHolder_updateReadOnlyPropertyWithChoices {

    private final PrimitiveByteHolder primitiveByteHolder;

    public PrimitiveByteHolder act(byte newValue) {
        primitiveByteHolder.setReadOnlyProperty(newValue);
        return primitiveByteHolder;
    }
    public byte default0Act() {
        return primitiveByteHolder.getReadOnlyProperty();
    }
    public List<Byte> choices0Act() {
        return Stream.of(1, 2, 3)
                .map(x -> byteOf(x))
                .collect(Collectors.toList());
    }
    public boolean hideAct() {
        return true; // TODO: choices doesn't seem to work for this datatype
    }

    private static Byte byteOf(int x) {
        return (byte)x;
    }

}
