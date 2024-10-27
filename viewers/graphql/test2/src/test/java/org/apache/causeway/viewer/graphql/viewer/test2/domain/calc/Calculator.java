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
package org.apache.causeway.viewer.graphql.viewer.test2.domain.calc;

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
    public byte addBytes(byte x, byte y) {
        return (byte)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addByteWrappers(Byte x, @Parameter(optionality = Optionality.OPTIONAL) Byte y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public short addShorts(short x, short y) {
        return (short)(x+y);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Short addShortWrappers(Short x, @Parameter(optionality = Optionality.OPTIONAL) Short y) {
        return y != null ? (short)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegers(int x, int y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int addIntegerWrappers(Integer x, @Parameter(optionality = Optionality.OPTIONAL) Integer y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public double addDoubles(double x, double y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Double addDoubleWrappers(Double x, @Parameter(optionality = Optionality.OPTIONAL) Double y) {
        return y != null ? x+y : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public float addFloats(float x, float y) {
        return x+y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Float addFloatWrappers(Float x, @Parameter(optionality = Optionality.OPTIONAL) Float y) {
        return y != null ? (float)(x+y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigInteger addBigIntegers(BigInteger x, @Parameter(optionality = Optionality.OPTIONAL) BigInteger y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public BigDecimal addBigDecimals(BigDecimal x, @Parameter(optionality = Optionality.OPTIONAL) BigDecimal y) {
        return y != null ? x.add(y) : x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDate jdk8LocalPlusDays(LocalDate date, int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public OffsetDateTime jdk8OffsetPlusDaysAndHoursAndMinutes(OffsetDateTime dateTime, int numDays, int numHours, int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public ZonedDateTime jdk8ZonedPlusDaysAndHoursAndMinutes(ZonedDateTime dateTime, int numDays, int numHours, int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public OffsetTime jdk8OffsetPlusHoursAndMinutes(OffsetTime time, int numHours, int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalTime jdk8LocalPlusHoursAndMinutes(LocalTime time, int numHours, int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.LocalDate jodaLocalPlusDays(org.joda.time.LocalDate date, int numDays) {
        return date.plusDays(numDays);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.DateTime jodaPlusDaysAndHoursAndMinutes(org.joda.time.DateTime dateTime, int numDays, int numHours, int numMinutes) {
        return dateTime.plusDays(numDays).plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public org.joda.time.LocalTime jodaLocalPlusHoursAndMinutes(org.joda.time.LocalTime time, int numHours, int numMinutes) {
        return time.plusHours(numHours).plusMinutes(numMinutes);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean and(boolean x, boolean y) {
        return x & y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean or(boolean x, boolean y) {
        return x | y;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public boolean not(boolean x) {
        return !x;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Month nextMonth(Month month) {
        return month.nextMonth();
    }

    @Action(semantics = SemanticsOf.SAFE)
    public String concat(String prefix, @Parameter(optionality = Optionality.OPTIONAL) String suffix) {
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
