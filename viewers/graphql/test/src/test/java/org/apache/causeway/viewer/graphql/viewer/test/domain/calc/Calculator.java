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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.applib.value.Password;

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

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDateTime echoLocalDateTime(final LocalDateTime value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalDateTime optionalLocalDateTime(
            @Parameter(optionality = Optionality.OPTIONAL) final LocalDateTime value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public URL echoUrl(final URL value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Date echoJavaUtilDate(final Date value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public java.sql.Date echoJavaSqlDate(final java.sql.Date value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Time echoJavaSqlTime(final Time value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Timestamp echoJavaSqlTimestamp(final Timestamp value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public ComplexNumber echoComplexNumber(final ComplexNumber value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Blob echoBlob(@Parameter(fileAccept = "text/plain") final Blob value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Blob optionalBlob(
            @Parameter(optionality = Optionality.OPTIONAL, fileAccept = "text/plain") final Blob value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Clob echoClob(@Parameter(fileAccept = "text/plain") final Clob value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Blob sampleBlob() {
        return new Blob("sample.txt", "text/plain", "sample blob".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Clob sampleClob() {
        return new Clob("sample.txt", "text/plain", "sample clob");
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Password secretPassword() {
        return Password.of("NEVER_DISCLOSE_THIS_PASSWORD");
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Password echoPassword(final Password value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Bookmark echoBookmark(final Bookmark value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public ApplicationFeatureId echoApplicationFeatureId(final ApplicationFeatureId value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public LocalResourcePath echoLocalResourcePath(final LocalResourcePath value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Markup sampleMarkup() {
        return Markup.valueOf("<strong>GraphQL markup</strong>");
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Markup echoMarkup(final Markup value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Identifier sampleIdentifier() {
        return Identifier.classIdentifier(new LogicalType("university.calc.Calculator", Calculator.class));
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Identifier echoIdentifier(final Identifier value) {
        return value;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public UnmappedValue sampleUnmappedValue() {
        return new UnmappedValue("NEVER_DISCLOSE_UNMAPPED_VALUE");
    }

    @Action(semantics = SemanticsOf.SAFE)
    public UnmappedValue echoUnmappedValue(final UnmappedValue value) {
        return value;
    }

    @SneakyThrows
    @Action(semantics = SemanticsOf.SAFE)
    public Locale someLocale() {
        return Locale.UK;
    }

    @Action(semantics = SemanticsOf.SAFE)
    public Locale echoLocale(final Locale value) {
        return value;
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public EditableMementoViewModel createEditableMementoViewModel() {
        return new EditableMementoViewModel(18);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public EditableMementoViewModelContract createSharedViewModel() {
        return new EditableMementoViewModel(42);
    }

    @Action(semantics = SemanticsOf.SAFE)
    public int readSharedViewModel(final EditableMementoViewModelContract viewModel) {
        return viewModel.getAge();
    }

    public List<EditableMementoViewModelContract> choices0ReadSharedViewModel() {
        return List.of();
    }

}
