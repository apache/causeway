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

import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaximumFractionDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlTimestamp;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.BigDecimalConverterWithScale;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaDateTime;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaLocalDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.DateConverterForJodaLocalDateTime;

import lombok.val;

/**
 * A locator for IConverters for ObjectAdapters
 * @deprecated instead use {@link ValueSemanticsProvider}
 */
@Deprecated(forRemoval = true, since = "2.0.0-M7")
public class IsisConverterLocator {

    /**
     * Locates the best IConverter implementation for a given {@link ManagedObject}
     *
     * @param objectAdapter The object adapter to locate converter for
     * @param wicketViewerSettings The date related settings
     * @return The best converter for the object adapter's type
     */
    public static IConverter<Object> findConverter(
            final ManagedObject objectAdapter,
            final WicketViewerSettings wicketViewerSettings) {

        val objectSpecification = objectAdapter.getSpecification();

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

        if (java.util.Date.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJavaUtilDate(wicketViewerSettings, adjustBy));
        }
        if (java.sql.Date.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJavaSqlDate(wicketViewerSettings, adjustBy));
        }
        if (org.joda.time.LocalDate.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJodaLocalDate(wicketViewerSettings, adjustBy));
        }
        if (org.joda.time.LocalDateTime.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJodaLocalDateTime(wicketViewerSettings, adjustBy));
        }
        if (org.joda.time.DateTime.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJodaDateTime(wicketViewerSettings, adjustBy));
        }
        if (java.sql.Timestamp.class == correspondingClass) {
            return _Casts.uncheckedCast(new DateConverterForJavaSqlTimestamp(wicketViewerSettings, adjustBy));
        }
        {
            // date converter plugins (if any)
            val serviceRegistry = objectSpecification.getMetaModelContext().getServiceRegistry();
            DateConverter<?> converter = serviceRegistry.select(DateConverterPlugin.class).stream()
                    .map(plugin->plugin.converterForClassIfAny(correspondingClass, wicketViewerSettings, adjustBy))
                    .filter(_NullSafe::isPresent)
                    .findAny()
                    .orElse(null);

            if(converter!=null) {
                return _Casts.uncheckedCast(converter);
            }

        }
        if (java.math.BigInteger.class == correspondingClass) {
            return _Casts.uncheckedCast(new BigIntegerConverter());
        }
        if (java.math.BigDecimal.class == correspondingClass) {
            final int scale = objectSpecification
                .lookupFacet(MaximumFractionDigitsFacet.class)
                .map(MaximumFractionDigitsFacet::getMaximumFractionDigits)
                .orElse(-1);
            return _Casts.uncheckedCast(new BigDecimalConverterWithScale(scale).forViewMode());
        }

        if(Application.exists()) {
            final IConverterLocator converterLocator = Application.get().getConverterLocator();
            return _Casts.uncheckedCast(converterLocator.getConverter(correspondingClass));
        }

        return null;
    }
}
