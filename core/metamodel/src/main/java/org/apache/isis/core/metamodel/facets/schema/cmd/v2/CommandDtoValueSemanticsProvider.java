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
package org.apache.isis.core.metamodel.facets.schema.cmd.v2;

import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.schema.cmd.CommandDtoValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.val;

public class CommandDtoValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<CommandDto>
implements CommandDtoValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return CommandDtoValueFacet.class;
    }

    private static final CommandDto DEFAULT_VALUE = null;

    public CommandDtoValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, CommandDto.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    @Override
    protected CommandDto doParse(final ValueSemanticsProvider.Context context, final String str) {
        return fromEncodedString(str);
    }

    @Override
    public String asTitleString(final CommandDto object) {
        if (object == null) return "[null]";
        val commandDto = object;
        return CommandDtoUtils.toXml(commandDto);
    }

    @Override
    public String commandDtoValue(final ManagedObject object) {
        if (object == null) {
            return "";
        }
        val commandDto = (CommandDto) object.getPojo();
        return CommandDtoUtils.toXml(commandDto);
    }

    @Override
    public ManagedObject createValue(final ManagedObject object, final String xml) {
        val commandDto = CommandDtoUtils.fromXml(xml);
        return getObjectManager().adapt(commandDto);
    }


    @Override
    public String toEncodedString(final CommandDto commandDto) {
        return CommandDtoUtils.toXml(commandDto);
    }

    @Override
    public CommandDto fromEncodedString(final String xml) {
        return CommandDtoUtils.fromXml(xml);
    }

}
