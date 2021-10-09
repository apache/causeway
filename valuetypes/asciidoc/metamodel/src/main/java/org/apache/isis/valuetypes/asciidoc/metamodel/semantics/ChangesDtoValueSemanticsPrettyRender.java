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
package org.apache.isis.valuetypes.asciidoc.metamodel.semantics;

import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.schema.chg.v2.ChangesDto;

import lombok.NonNull;

@Component
@Named("isis.val.ChangesDtoValueSemanticsPrettyRender")
@Order(PriorityPrecedence.EARLY)
public class ChangesDtoValueSemanticsPrettyRender
extends XmlValueSemanticsAbstract<ChangesDto> {

    @Override
    public Class<ChangesDto> getCorrespondingClass() {
        return ChangesDto.class;
    }

    @Override
    protected String asXml(final Context context, @NonNull final ChangesDto value) {
        return ChangesDtoUtils.toXml(value);
    }

}
