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
package org.apache.isis.core.metamodel.valuesemantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.CommandDtoValueSemantics")
public class CommandDtoValueSemantics
extends ValueSemanticsAbstract<CommandDto>
implements
    EncoderDecoder<CommandDto>,
    Renderer<CommandDto> {

    @Override
    public Class<CommandDto> getCorrespondingClass() {
        return CommandDto.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final CommandDto commandDto) {
        return CommandDtoUtils.toXml(commandDto);
    }

    @Override
    public CommandDto fromEncodedString(final String xml) {
        return CommandDtoUtils.fromXml(xml);
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final CommandDto value) {
        return render(value, CommandDtoUtils::toXml);
    }

}
