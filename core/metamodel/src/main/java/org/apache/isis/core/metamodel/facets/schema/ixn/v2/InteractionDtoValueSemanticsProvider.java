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

package org.apache.isis.core.metamodel.facets.schema.ixn.v2;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.schema.ixn.InteractionDtoValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.val;

public class InteractionDtoValueSemanticsProvider
extends ValueSemanticsProviderAndFacetAbstract<InteractionDto>
implements InteractionDtoValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return InteractionDtoValueFacet.class;
    }

    private static final InteractionDto DEFAULT_VALUE = null;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public InteractionDtoValueSemanticsProvider() {
        this(null);
    }

    public InteractionDtoValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, InteractionDto.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }


    @Override
    protected InteractionDto doParse(final Object context, final String str) {
        return doRestore(str);
    }

    @Override
    public String titleString(final Object object) {
        if (object == null) return "[null]";
        final InteractionDto interactionDto = (InteractionDto) object;
        return InteractionDtoUtils.toXml(interactionDto);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }


    @Override
    public String interactionDtoValue(final ManagedObject object) {
        if (object == null) {
            return "";
        }
        val interactionDto = (InteractionDto) object.getPojo();
        return InteractionDtoUtils.toXml(interactionDto);
    }

    @Override
    public ManagedObject createValue(final ManagedObject object, final String xml) {
        val interactionDto = InteractionDtoUtils.fromXml(xml);
        return getObjectManager().adapt(interactionDto);
    }


    @Override
    protected String doEncode(final Object object) {
        val interactionDto = (InteractionDto) object;
        return InteractionDtoUtils.toXml(interactionDto);
    }

    @Override
    protected InteractionDto doRestore(final String xml) {
        return InteractionDtoUtils.fromXml(xml);
    }


    @Override
    public String toString() {
        return "InteractionDtoValueSemanticsProvider";
    }

}
