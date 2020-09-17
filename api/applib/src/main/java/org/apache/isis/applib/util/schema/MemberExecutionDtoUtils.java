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
package org.apache.isis.applib.util.schema;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.schema.common.v2.DifferenceDto;
import org.apache.isis.schema.common.v2.PeriodDto;
import org.apache.isis.schema.ixn.v2.MemberExecutionDto;
import org.apache.isis.schema.ixn.v2.MetricsDto;
import org.apache.isis.schema.ixn.v2.ObjectCountsDto;

public final class MemberExecutionDtoUtils {

    public static <T extends MemberExecutionDto> T clone(final T dto) {
        final Class<T> aClass = _Casts.uncheckedCast(dto.getClass());
        return clone(dto, aClass);
    }

    private static <T> T clone(final T dto, final Class<T> dtoClass) {
        try {
            JAXBContext jaxbContext = jaxbContextFor(dtoClass);

            final Marshaller marshaller = jaxbContext.createMarshaller();

            final QName name = new QName("", dtoClass.getSimpleName());
            final JAXBElement<T> jaxbElement = new JAXBElement<>(name, dtoClass, null, dto);
            final StringWriter stringWriter = new StringWriter();

            marshaller.marshal(jaxbElement, stringWriter);

            final StringReader reader = new StringReader(stringWriter.toString());

            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            final JAXBElement<T> root = unmarshaller.unmarshal(new StreamSource(reader), dtoClass);

            return root.getValue();

        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> JAXBContext jaxbContextFor(final Class<T> dtoClass)  {
        return JaxbUtil.jaxbContextFor(dtoClass);
    }

    public static MetricsDto metricsFor(final MemberExecutionDto executionDto) {
        MetricsDto metrics = executionDto.getMetrics();
        if(metrics == null) {
            metrics = new MetricsDto();
            executionDto.setMetrics(metrics);
        }
        return metrics;
    }

    public static PeriodDto timingsFor(final MetricsDto metricsDto) {
        PeriodDto timings = metricsDto.getTimings();
        if(timings == null) {
            timings = new PeriodDto();
            metricsDto.setTimings(timings);
        }
        return timings;
    }

    public static ObjectCountsDto objectCountsFor(final MetricsDto metricsDto) {
        ObjectCountsDto objectCounts = metricsDto.getObjectCounts();
        if(objectCounts == null) {
            objectCounts = new ObjectCountsDto();
            metricsDto.setObjectCounts(objectCounts);
        }
        return objectCounts;
    }

    public static DifferenceDto numberObjectsLoadedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getLoaded();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setLoaded(differenceDto);
        }
        return differenceDto;
    }
    public static DifferenceDto numberObjectsDirtiedFor(final ObjectCountsDto objectCountsDto) {
        DifferenceDto differenceDto = objectCountsDto.getDirtied();
        if(differenceDto == null) {
            differenceDto = new DifferenceDto();
            objectCountsDto.setDirtied(differenceDto);
        }
        return differenceDto;
    }
}
