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

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.ixn.v2.InteractionDto;

@Component
@Named("isis.val.InteractionDtoValueSemantics")
public class InteractionDtoValueSemantics
extends AbstractValueSemanticsProvider<InteractionDto>
implements
    EncoderDecoder<InteractionDto>,
    Renderer<InteractionDto> {

    @Override
    public Class<InteractionDto> getCorrespondingClass() {
        return InteractionDto.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final InteractionDto interactionDto) {
        return InteractionDtoUtils.toXml(interactionDto);
    }

    @Override
    public InteractionDto fromEncodedString(final String xml) {
        return InteractionDtoUtils.fromXml(xml);
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final InteractionDto value) {
        return render(value, InteractionDtoUtils::toXml);
    }

}
