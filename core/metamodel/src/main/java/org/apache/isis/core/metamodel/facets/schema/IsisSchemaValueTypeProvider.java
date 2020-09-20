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
package org.apache.isis.core.metamodel.facets.schema;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.valuetypes.ValueTypeDefinition;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeProvider;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.ixn.v2.InteractionDto;

@Component
@Named("isisMetaModel.isisSchemaValueTypeProvider")
public class IsisSchemaValueTypeProvider implements ValueTypeProvider {
    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return Arrays.asList(
                ValueTypeDefinition.of(InteractionDto.class, ValueType.STRING)
                , ValueTypeDefinition.of(ChangesDto.class, ValueType.STRING)
                , ValueTypeDefinition.of(CommandDto.class, ValueType.STRING)
        );
    }
}
