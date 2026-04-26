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
package org.apache.causeway.applib.util.schema;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.JsonUtils.JacksonCustomizer;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.MemberDto;
import org.apache.causeway.schema.cmd.v2.PropertyDto;

import lombok.experimental.UtilityClass;

import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.jsontype.NamedType;

@UtilityClass
class CommandDtoJacksonSupport {

    JsonUtils.JacksonCustomizer yamlWriteCustomizer() {
    	return ((JacksonCustomizer) JsonUtils::jaxbAnnotationSupport)
                .andThen((JacksonCustomizer) CommandDtoJacksonSupport::memberDtoSupport)
                .andThen((JacksonCustomizer) JsonUtils::onlyIncludeNonNull)
                ::accept;
    }
    JsonUtils.JacksonCustomizer yamlReadCustomizer() {
    	return ((JacksonCustomizer) JsonUtils::jaxbAnnotationSupport)
                .andThen((JacksonCustomizer) CommandDtoJacksonSupport::memberDtoSupport)
                ::accept;
    }
    
    // -- HELPER

    // Mix-in to add type metadata to MemberDto
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    private abstract class AbstractDtoMixIn {}

    private void memberDtoSupport(final MapperBuilder<?, ?> mb) {
        // add mix-in so MemberDto carries @JsonTypeInfo without modifying source
        mb.addMixIn(MemberDto.class, AbstractDtoMixIn.class);
        // register concrete sub-types with logical names
        mb.registerSubtypes(new NamedType(ActionDto.class, "ACT"));
        mb.registerSubtypes(new NamedType(PropertyDto.class, "PROP"));
    }
	
}
