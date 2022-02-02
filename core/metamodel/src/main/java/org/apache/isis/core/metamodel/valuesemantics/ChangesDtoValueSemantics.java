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
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.chg.v2.ChangesDto;

@Component
@Named("isis.val.ChangesDtoValueSemantics")
public class ChangesDtoValueSemantics
extends XmlValueSemanticsAbstract<ChangesDto> {

    @Override
    public final Class<ChangesDto> getCorrespondingClass() {
        return ChangesDto.class;
    }

    // -- ENCODER DECODER

    @Override
    public final String toXml(final ChangesDto changesDto) {
        return ChangesDtoUtils.toXml(changesDto);
    }

    @Override
    public final ChangesDto fromXml(final String xml) {
        return ChangesDtoUtils.fromXml(xml);
    }

    // -- EXAMPLES

    @Override
    public Can<ChangesDto> getExamples() {
        return Can.of(new ChangesDto(), new ChangesDto());
    }

}
