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

import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.ChangesDtoValueSemantics")
public class ChangesDtoValueSemantics
extends ValueSemanticsAbstract<ChangesDto>
implements
    EncoderDecoder<ChangesDto>,
    Renderer<ChangesDto> {

    @Override
    public Class<ChangesDto> getCorrespondingClass() {
        return ChangesDto.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final ChangesDto changesDto) {
        return ChangesDtoUtils.toXml(changesDto);
    }

    @Override
    public ChangesDto fromEncodedString(final String xml) {
        return ChangesDtoUtils.fromXml(xml);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final ChangesDto value) {
        return render(value, ChangesDtoUtils::toXml);
    }

    @Override
    public Can<ChangesDto> getExamples() {
        return Can.of(new ChangesDto(), new ChangesDto());
    }

}
