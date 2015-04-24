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

import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.Application;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.renderedadjusted.RenderedAdjustedFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.DateConverterForApplibDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.DateConverterForApplibDateTime;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaSqlTimestamp;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.DateConverterForJavaUtilDate;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.BigDecimalConverterWithScale;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.BigIntegerConverter;
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

        final ParseableFacet parseableFacet = objectSpecification.getFacet(ParseableFacet.class);

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
            converter = BigIntegerConverter.INSTANCE;
        } else if (java.math.BigDecimal.class == correspondingClass) {
            final BigDecimalValueFacet facet = objectSpecification.getFacet(BigDecimalValueFacet.class);
            Integer scale = null;
            if (facet != null) {
                scale = facet.getScale();
            }
            converter = new BigDecimalConverterWithScale(scale).forViewMode();
        } else if(parseableFacet != null){
            // try to parse (as a value object) using the Isis API.
            converter = new IConverter() {
                @Override public Object convertToObject(final String value, final Locale locale) throws ConversionException {
                    if(value == null) {
                        return null;
                    }
                    final ObjectAdapter parsedObjectAdapter = parseableFacet.parseTextEntry(objectAdapter, value, new Localization() {
                        @Override public Locale getLocale() {
                            return locale;
                        }

                        @Override public TimeZone getTimeZone() {
                            // TODO: need a TimeZoneProvider service, cf LocaleProvider.
                            return TimeZone.getDefault();
                        }
                    });
                    return parsedObjectAdapter != null? parsedObjectAdapter.getObject(): null;
                }

                @Override public String convertToString(final Object value, final Locale locale) {
                    if(value == null) {
                        return null;
                    }
                    final ObjectAdapter obj = getAdapterManager().adapterFor(value);
                    // TODO: the Isis API doesn't deal with locale (or timezone) so isn't symmetrical :-(
                    return parseableFacet.parseableTitle(obj);
                }
            };
        } else if (Application.exists()) {
            final IConverterLocator converterLocator = Application.get().getConverterLocator();
            converter = converterLocator.getConverter(correspondingClass);
        }
        return converter;
    }

    static AdapterManagerDefault getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }
}
