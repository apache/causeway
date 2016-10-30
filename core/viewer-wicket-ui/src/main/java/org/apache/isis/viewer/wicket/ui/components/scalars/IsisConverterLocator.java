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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import org.apache.wicket.Application;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigIntegerConverter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.DateConverterForApplibDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.DateConverterForApplibDateTime;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlTimestamp;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.BigDecimalConverterWithScale;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaDateTime;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaLocalDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaLocalDateTime;

/**
 * A locator for IConverters for ObjectAdapters
 */
public class IsisConverterLocator {

    /**
     * Locates the best IConverter implementation for a given {@link org.apache.isis.core.metamodel.adapter.ObjectAdapter}
     *
     * @param objectAdapter The object adapter to locate converter for
     * @param wicketViewerSettings The date related settings
     * @return The best converter for the object adapter's type
     */
    public static IConverter<Object> findConverter(final ObjectAdapter objectAdapter, final WicketViewerSettings wicketViewerSettings) {

        final ObjectSpecification objectSpecification = objectAdapter.getSpecification();

        // only use Wicket IConverter for value types, not for domain objects.
        if (!objectSpecification.isValue()) {
            return null;
        }

        // explicitly exclude enums; this will force the titleString
        // to be used from Isis' EnumValueSemanticsProvider
        final Class<?> correspondingClass = objectSpecification.getCorrespondingClass();
        if(Enum.class.isAssignableFrom(correspondingClass)) {
            return null;
        }

        final RenderedAdjustedFacet renderedAdjustedFacet = objectSpecification.getFacet(RenderedAdjustedFacet.class);
        final int adjustBy = renderedAdjustedFacet != null ? renderedAdjustedFacet.value() : 0;

        IConverter converter = null;
        if (java.util.Date.class == correspondingClass) {
            converter = new DateConverterForJavaUtilDate(wicketViewerSettings, adjustBy);
        } else if (java.sql.Date.class == correspondingClass) {
            converter = new DateConverterForJavaSqlDate(wicketViewerSettings, adjustBy);
        } else if (org.apache.isis.applib.value.Date.class == correspondingClass) {
            converter = new DateConverterForApplibDate(wicketViewerSettings, adjustBy);
        } else if (org.apache.isis.applib.value.DateTime.class == correspondingClass) {
            converter = new DateConverterForApplibDateTime(wicketViewerSettings, adjustBy);
        } else if (org.joda.time.LocalDate.class == correspondingClass) {
            converter = new DateConverterForJodaLocalDate(wicketViewerSettings, adjustBy);
        } else if (org.joda.time.LocalDateTime.class == correspondingClass) {
            converter = new DateConverterForJodaLocalDateTime(wicketViewerSettings, adjustBy);
        } else if (org.joda.time.DateTime.class == correspondingClass) {
            converter = new DateConverterForJodaDateTime(wicketViewerSettings, adjustBy);
        } else if (java.sql.Timestamp.class == correspondingClass) {
            converter = new DateConverterForJavaSqlTimestamp(wicketViewerSettings, adjustBy);
        } else if (java.math.BigInteger.class == correspondingClass) {
            converter = new BigIntegerConverter();
        } else if (java.math.BigDecimal.class == correspondingClass) {
            final BigDecimalValueFacet facet = objectSpecification.getFacet(BigDecimalValueFacet.class);
            Integer scale = null;
            if (facet != null) {
                scale = facet.getScale();
            }
            converter = new BigDecimalConverterWithScale(scale).forViewMode();
        } else if (Application.exists()) {
            final IConverterLocator converterLocator = Application.get().getConverterLocator();
            converter = converterLocator.getConverter(correspondingClass);
        }

        return converter;
    }
}
