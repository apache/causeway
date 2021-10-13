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
package org.apache.isis.viewer.wicket.model.models.interaction.act;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.functions._Functions;
//FIXME[ISIS-2877] intermediate, remove when resolved
public class _2877 {

    public static Can<ParameterUiModelWkt> createChildModels(final ActionInteractionWkt actionInteractionWkt) {

        if(!actionInteractionWkt.getMetaModel().getElementType().getCorrespondingClass().getSimpleName()
                .equals("CalendarEvent")) {

            final int paramIndex = 0;
            baseTypes
            .map(_Functions.indexedZeroBase(
                    (tupleIndex, baseType)->new ParameterUiModelWkt(actionInteractionWkt, paramIndex, tupleIndex)));
        }

        final int paramCount = actionInteractionWkt.actionInteraction().getMetamodel().get().getParameterCount();
        final int tupleIndex = 0;
        return IntStream.range(0, paramCount)
                .mapToObj(paramIndex -> new ParameterUiModelWkt(actionInteractionWkt, paramIndex, tupleIndex))
                .collect(Can.toCan());
    }

    private static Can<Class<?>> baseTypes = Can.of(
            LocalDateTime.class,// dateTime,
            String.class,// calendarName,
            String.class,// title,
            String.class// notes
            );


}
