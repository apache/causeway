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
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Named("university.calc.Calculator")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class Calculator {

    @Action(semantics = SemanticsOf.SAFE)
    public byte addBytes(final byte x, final byte y) {
        return (byte)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addByteWrappers(final Byte x, @Parameter(optionality = Optionality.OPTIONAL) final Byte y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public short addShorts(final short x, final short y) {
        return (short)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Short addShortWrappers(final Short x, @Parameter(optionality = Optionality.OPTIONAL) final Short y) {
        return y != null ? (short)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegers(final int x, final int y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegerWrappers(final Integer x, @Parameter(optionality = Optionality.OPTIONAL) final Integer y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public double addDoubles(final double x, final double y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Double addDoubleWrappers(final Double x, @Parameter(optionality = Optionality.OPTIONAL) final Double y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public float addFloats(final float x, final float y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Float addFloatWrappers(final Float x, @Parameter(optionality = Optionality.OPTIONAL) final Float y) {
        return y != null ? (float)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigInteger addBigIntegers(final BigInteger x, @Parameter(optionality = Optionality.OPTIONAL) final BigInteger y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigDecimal addBigDecimals(final BigDecimal x, @Parameter(optionality = Optionality.OPTIONAL) final BigDecimal y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDate jdk8LocalPlusDays(final LocalDate date, final int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public OffsetDateTime jdk8OffsetPlusDaysAndHoursAndMinutes(final OffsetDateTime dateTime, final int numDays, final int numHours, final int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public ZonedDateTime jdk8ZonedPlusDaysAndHoursAndMinutes(final ZonedDateTime dateTime, final int numDays, final int numHours, final int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public OffsetTime jdk8OffsetPlusHoursAndMinutes(final OffsetTime time, final int numHours, final int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalTime jdk8LocalPlusHoursAndMinutes(final LocalTime time, final int numHours, final int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.LocalDate jodaLocalPlusDays(final org.joda.time.LocalDate date, final int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.DateTime jodaPlusDaysAndHoursAndMinutes(final org.joda.time.DateTime dateTime, final int numDays, final int numHours, final int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.LocalTime jodaLocalPlusHoursAndMinutes(final org.joda.time.LocalTime time, final int numHours, final int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean and(final boolean x, final boolean y) {
        return x & y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean or(final boolean x, final boolean y) {
        return x | y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean not(final boolean x) {
        return !x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Month nextMonth(final Month month) {
        return month.nextMonth();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public String concat(final String prefix, @Parameter(optionality = Optionality.OPTIONAL) final String suffix) {
        return prefix + suffix;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public UUID someUuid() {
        return UUID.fromString("91be0d2d-1752-4962-ad2c-89a7ef73a656");
    }

    @SneakyThrows
    @Action(semantics = SemanticsOf.SAFE)
    public URL someUrl() {
        return new URL("https://causeway.apache.org");
    }

    @SneakyThrows
    @Action(semantics = SemanticsOf.SAFE)
    public Locale someLocale() {
        return Locale.UK;
    }

}
