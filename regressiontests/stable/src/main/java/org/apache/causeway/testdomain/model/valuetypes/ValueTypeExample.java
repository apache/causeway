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
package org.apache.causeway.testdomain.model.valuetypes;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreeState;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.core.metamodel.valuesemantics.ApplicationFeatureIdValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.MarkupValueSemantics;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEventSemantics;
import org.apache.causeway.schema.chg.v2.ChangesDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.common.v2.OidDto;
import org.apache.causeway.schema.ixn.v2.InteractionDto;
import org.apache.causeway.valuetypes.vega.applib.value.Vega;
import org.apache.causeway.valuetypes.vega.metamodel.semantics.VegaValueSemantics;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.val;

public abstract class ValueTypeExample<T> {

    @Property
    public abstract T getValue();
    public abstract void setValue(T value);

    @Programmatic
    public abstract T getUpdateValue();

    @Action
    public final void updateValue(final T value) {
        setValue(value);
    }

    /**
     * Name of the value-type plus suffix if any, as extracted from the implementing example name.
     */
    @Programmatic
    public final String getName() {
        val nameSuffix = extractSuffix(getClass().getSimpleName())
                .map(s->"_" + s)
                .orElse("");
        val name = String.format("%s%s", getValueType().getName(), nameSuffix);
        return name;
    }

    @Autowired(required = false) List<ValueSemanticsAbstract<T>> semanticsList;
    @Programmatic
    public Can<T> getParserRoundtripExamples() {
        return Can.ofCollection(semanticsList)
        .getFirst()
        .map(semantics->semantics.getExamples())
        .orElseGet(()->Can.of(getValue(), getUpdateValue()));
    }

    @Collection
    public List<T> getValues() {
        return List.of(getValue(), getUpdateValue());
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    public final Class<T> getValueType() {
        return (Class<T>) getValue().getClass();
    }

    // -- PARSING

    @lombok.Value @Builder
    public static class ParseExpectation<T> {
        final T value;
        @Singular
        final List<String> inputSamples;
        final String expectedOutput;
        final Class<? extends Throwable> expectedThrows;
    }

    public Can<ParseExpectation<T>> getParseExpectations() {
        System.err.printf("skipping parsing test for %s%n", getName());
        return Can.empty();
    }

    // -- RENDERING

    @lombok.Value @Builder
    public static class RenderExpectation<T> {
        final T value;
        final String title;
        final String html;
    }

    public Can<RenderExpectation<T>> getRenderExpectations() {
        System.err.printf("skipping rendering test for %s%n", getName());
        return Can.empty();
    }

    // -- HELPER

    private static Optional<String> extractSuffix(final String name) {
        if(!name.contains("_")) {
            return Optional.empty();
        }
        val ref = _Refs.stringRef(name);
        ref.cutAtIndexOfAndDrop("_");
        return Optional.of(ref.getValue());
    }

    // -- EXAMPLES - BASIC

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBoolean")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBoolean
    extends ValueTypeExample<Boolean> {
        @Property @Getter @Setter
        private Boolean value = Boolean.TRUE;
        @Getter
        private Boolean updateValue = Boolean.FALSE;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleCharacter")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCharacter
    extends ValueTypeExample<Character> {
        @Property @Getter @Setter
        private Character value = 'a';
        @Getter
        private Character updateValue = 'b';
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleString")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleString
    extends ValueTypeExample<String> {
        @Property @Getter @Setter
        private String value = "aString";
        @Getter
        private String updateValue = "anotherString";
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExamplePassword")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExamplePassword
    extends ValueTypeExample<Password> {
        @Property @Getter @Setter
        private Password value = Password.of("aPassword");
        @Getter
        private Password updateValue = Password.of("anotherPassword");
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBufferedImage")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBufferedImage
    extends ValueTypeExample<BufferedImage> {
        @Property @Getter @Setter
        private BufferedImage value = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        @Getter
        private BufferedImage updateValue = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBlob")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBlob
    extends ValueTypeExample<Blob> {
        @Property @Getter @Setter
        private Blob value = Blob.of("aBlob", CommonMimeType.BIN, new byte[] {1, 2, 3});
        @Getter
        private Blob updateValue = Blob.of("anotherBlob", CommonMimeType.BIN, new byte[] {3, 4});
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleClob")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleClob
    extends ValueTypeExample<Clob> {
        @Property @Getter @Setter
        private Clob value = Clob.of("aClob", CommonMimeType.TXT, "abc");
        @Getter
        private Clob updateValue = Clob.of("anotherClob", CommonMimeType.TXT, "ef");
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLocalResourcePath")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalResourcePath
    extends ValueTypeExample<LocalResourcePath> {
        @Property @Getter @Setter
        private LocalResourcePath value = new LocalResourcePath("img/a");
        @Getter
        private LocalResourcePath updateValue = new LocalResourcePath("img/b");
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleUrl")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleUrl
    extends ValueTypeExample<URL> {
        @Property @Getter @Setter
        private URL value = url("https://a.b.c");
        @Getter
        private URL updateValue = url("https://b.c.d");
        @SneakyThrows
        private static URL url(final String url) {
            return new URL(url);
        }
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleMarkup")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleMarkup
    extends ValueTypeExample<Markup> {
        private MarkupValueSemantics markupSemantics = new MarkupValueSemantics();
        @Property @Getter @Setter
        private Markup value = markupSemantics.getExamples().getElseFail(0);
        @Getter
        private Markup updateValue = markupSemantics.getExamples().getElseFail(1);
        @Override
        public Can<ParseExpectation<Markup>> getParseExpectations() {
            val htmlSample = "<a href=\"https://www.apache.org\" rel=\"nofollow\">link</a>";
            return Can.of(
                    ParseExpectation.<Markup>builder()
                        .value(new Markup(htmlSample))
                        .inputSample(htmlSample)
                        .expectedOutput(htmlSample)
                        .build()
                );
        }
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleVega")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleVega
    extends ValueTypeExample<Vega> {
        private VegaValueSemantics vegaSemantics = new VegaValueSemantics();
        @Property @Getter @Setter
        private Vega value = vegaSemantics.getExamples().getElseFail(0);
        @Getter
        private Vega updateValue = vegaSemantics.getExamples().getElseFail(1);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleUuid")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleUuid
    extends ValueTypeExample<UUID> {
        @Property @Getter @Setter
        private UUID value = UUID.randomUUID();
        @Getter
        private UUID updateValue = UUID.randomUUID();
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLocale")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocale
    extends ValueTypeExample<Locale> {
        @Property @Getter @Setter
        private Locale value = Locale.US;
        @Getter
        private Locale updateValue = Locale.GERMAN;
    }

    // -- EXAMPLES - NUMBERS

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleByte")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleByte
    extends ValueTypeExample<Byte> {
        @Property @Getter @Setter
        private Byte value = -63;
        @Getter
        private Byte updateValue = 0;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleShort")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleShort
    extends ValueTypeExample<Short> {
        @Property @Getter @Setter
        private Short value = -63;
        @Getter
        private Short updateValue = 0;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleInteger")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleInteger
    extends ValueTypeExample<Integer> {
        @Property @Getter @Setter
        private Integer value = -63;
        @Getter
        private Integer updateValue = 0;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLong")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLong
    extends ValueTypeExample<Long> {
        @Property @Getter @Setter
        private Long value = -63L;
        @Getter
        private Long updateValue = 0L;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleFloat")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleFloat
    extends ValueTypeExample<Float> {
        @Property @Getter @Setter
        private Float value = -63.1f;
        @Getter
        private Float updateValue = 0.f;

        //FIXME does not handle example Float.MIN_VALUE well
        @Deprecated // remove override once fixed
        @Override public Can<Float> getParserRoundtripExamples() {
            return Can.of(value, updateValue);
        }
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleDouble")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleDouble
    extends ValueTypeExample<Double> {
        @Property @Getter @Setter
        private Double value = -63.1;
        @Getter
        private Double updateValue = 0.;

        //FIXME does not handle example Double.MIN_VALUE well
        @Deprecated // remove override once fixed
        @Override public Can<Double> getParserRoundtripExamples() {
            return Can.of(value, updateValue);
        }
    }

    // -- BIG INTEGER

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBigInteger")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBigInteger
    extends ValueTypeExample<BigInteger> {
        @Property @Getter @Setter
        private BigInteger value = BigInteger.valueOf(-63L);
        @Getter
        private BigInteger updateValue = BigInteger.ZERO;
    }

    // -- BIG DECIMAL

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBigDecimal_default")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBigDecimal_default
    extends ValueTypeExample<BigDecimal> {
        @Property @Getter @Setter
        private BigDecimal value = new BigDecimal("-63.123456");
        @Getter
        private BigDecimal updateValue = BigDecimal.ZERO;
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBigDecimal_fixedFractionalDigits")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBigDecimal_fixedFractionalDigits
    extends ValueTypeExample<BigDecimal> {
        @Property @ValueSemantics(minFractionalDigits = 2, maxFractionalDigits = 2)
        @Getter @Setter
        private BigDecimal value = new BigDecimal("-63.12");
        @Getter
        private BigDecimal updateValue = BigDecimal.ZERO;

        // with this example maxFractionalDigits = 2 must not be exceeded
        @Override public Can<BigDecimal> getParserRoundtripExamples() {
            return Can.of(value, updateValue, new BigDecimal("0.1"));
        }

        @Override
        public Can<ParseExpectation<BigDecimal>> getParseExpectations() {
            return Can.of(
                    ParseExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("123"))
                        .inputSample("123")
                        .inputSample("123.0")
                        .inputSample("123.00")
                        .expectedOutput("123.00")
                        .build(),
                    ParseExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("123.45"))
                        .inputSample("123.45")
                        .expectedOutput("123.45")
                        .build(),
                    ParseExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("123.45"))
                        .inputSample("123.456")
                        //org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException:
                        // No more than 2 digits can be entered after the decimal separator, got 3 in '123.456'.
                        .expectedThrows(TextEntryParseException.class)
                        .build()
                );
        }

        @Override
        public Can<RenderExpectation<BigDecimal>> getRenderExpectations() {
            return Can.of(
                    RenderExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("123")).title("123.00").html("123.00").build(),
                    RenderExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("0")).title("0.00").html("0.00").build(),
                    RenderExpectation.<BigDecimal>builder()
                        .value(new BigDecimal("123.456")).title("123.46").html("123.46").build()
                );
        }

    }

    // -- EXAMPLES - TEMPORAL - LEGACY

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJavaUtilDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaUtilDate
    extends ValueTypeExample<java.util.Date> {
        @Property @Getter @Setter
        private java.util.Date value = new java.util.Date();
        @Getter
        private java.util.Date updateValue = new java.util.Date(0L);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJavaSqlDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaSqlDate
    extends ValueTypeExample<java.sql.Date> {
        @Property @Getter @Setter
        private java.sql.Date value = new java.sql.Date(new java.util.Date().getTime());
        @Getter
        private java.sql.Date updateValue = new java.sql.Date(0L);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJavaSqlTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaSqlTime
    extends ValueTypeExample<java.sql.Time> {
        @Property @Getter @Setter
        private java.sql.Time value = new java.sql.Time(new java.util.Date().getTime());
        @Getter
        private java.sql.Time updateValue = new java.sql.Time(0L);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleTimestamp")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleTimestamp
    extends ValueTypeExample<Timestamp> {
        @Property @Getter @Setter
        private Timestamp value = new Timestamp(new java.util.Date().getTime());
        @Getter
        private Timestamp updateValue = new Timestamp(0L);
    }

    // -- EXAMPLES - TEMPORAL - JAVA TIME

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLocalDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalDate
    extends ValueTypeExample<LocalDate> {
        @Property @Getter @Setter
        private LocalDate value = _Temporals.sampleLocalDate().getElseFail(0);
        @Getter
        private LocalDate updateValue = getValue().plusDays(2);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLocalDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalDateTime
    extends ValueTypeExample<LocalDateTime> {
        @Property @Getter @Setter
        private LocalDateTime value = _Temporals.sampleLocalDateTime().getElseFail(0);
        @Getter
        private LocalDateTime updateValue = getValue().plusDays(2).plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleLocalTime")
    @DomainObject(

            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalTime
    extends ValueTypeExample<LocalTime> {
        @Property @Getter @Setter
        private LocalTime value = _Temporals.sampleLocalTime().getElseFail(0);
        @Getter
        private LocalTime updateValue = getValue().plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleOffsetDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOffsetDateTime
    extends ValueTypeExample<OffsetDateTime> {
        @Property @Getter @Setter
        private OffsetDateTime value = _Temporals.sampleOffsetDateTime().getElseFail(0);
        @Getter
        private OffsetDateTime updateValue = getValue().plusDays(2).plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleOffsetTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOffsetTime
    extends ValueTypeExample<OffsetTime> {
        @Property @Getter @Setter
        private OffsetTime value = _Temporals.sampleOffsetTime().getElseFail(0);
        @Getter
        private OffsetTime updateValue = OffsetTime.now().plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleZonedDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleZonedDateTime
    extends ValueTypeExample<ZonedDateTime> {
        @Property @Getter @Setter
        private ZonedDateTime value = _Temporals.sampleZonedDateTime().getElseFail(0);
        @Getter
        private ZonedDateTime updateValue = getValue().plusDays(2).plusSeconds(15);
    }

    // -- EXAMPLES - TEMPORAL - JODA TIME

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJodaDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaDateTime
    extends ValueTypeExample<org.joda.time.DateTime> {
        @Property @Getter @Setter
        private org.joda.time.DateTime value = org.joda.time.DateTime.now();
        @Getter
        private org.joda.time.DateTime updateValue = org.joda.time.DateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJodaLocalDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalDateTime
    extends ValueTypeExample<org.joda.time.LocalDateTime> {
        @Property @Getter @Setter
        private org.joda.time.LocalDateTime value = org.joda.time.LocalDateTime.now();
        @Getter
        private org.joda.time.LocalDateTime updateValue = org.joda.time.LocalDateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJodaLocalDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalDate
    extends ValueTypeExample<org.joda.time.LocalDate> {
        @Property @Getter @Setter
        private org.joda.time.LocalDate value = org.joda.time.LocalDate.now();
        @Getter
        private org.joda.time.LocalDate updateValue = org.joda.time.LocalDate.now().plusDays(2);
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleJodaLocalTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalTime
    extends ValueTypeExample<org.joda.time.LocalTime> {
        @Property @Getter @Setter
        private org.joda.time.LocalTime value = org.joda.time.LocalTime.now();
        @Getter
        private org.joda.time.LocalTime updateValue = org.joda.time.LocalTime.now().plusSeconds(15);
    }

    // -- EXAMPLES - META MODEL

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleApplicationFeatureId")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleApplicationFeatureId
    extends ValueTypeExample<ApplicationFeatureId> {
        @Property @Getter @Setter
        private ApplicationFeatureId value = new ApplicationFeatureIdValueSemantics().getExamples().getElseFail(0);
        @Getter
        private ApplicationFeatureId updateValue = new ApplicationFeatureIdValueSemantics().getExamples().getElseFail(1);
    }

    // -- EXAMPLES - DATA STRUCTURE

    //TODO    TreeNode
//    @DomainObject(
//            @Named("causeway.testdomain.valuetypes.ValueTypeExampleTreeNode",
//            nature = Nature.BEAN)
    public static class ValueTypeExampleTreeNode
    extends ValueTypeExample<TreeNode<String>> {
        @Property @Getter @Setter
        private TreeNode<String> value = TreeNode.of("root", TreeAdapterString.class, TreeState.rootCollapsed());
        @Getter
        private TreeNode<String> updateValue = TreeNode.of("anotherRoot", TreeAdapterString.class, TreeState.rootCollapsed());

        private static class TreeAdapterString implements TreeAdapter<String> {
            @Override public Optional<String> parentOf(final String value) {
                return null; }
            @Override public int childCountOf(final String value) {
                return 0; }
            @Override public Stream<String> childrenOf(final String value) {
                return Stream.empty(); }
        }

    }

    // -- EXAMPLES - ENUM

    public static enum ExampleEnum {
        HALLO, WORLD
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleEnum")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleEnum
    extends ValueTypeExample<ExampleEnum> {
        @Property @Getter @Setter
        private ExampleEnum value = ExampleEnum.HALLO;
        @Getter
        private ExampleEnum updateValue = ExampleEnum.WORLD;
    }

    // -- EXAMPLES - COMPOSITES

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleCalendarEvent")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCalendarEvent
    extends ValueTypeExample<CalendarEvent> {
        @Property @Getter @Setter
        private CalendarEvent value = new CalendarEventSemantics().getExamples().getElseFail(0);
        @Getter
        private CalendarEvent updateValue = new CalendarEventSemantics().getExamples().getElseFail(1);
    }

    // -- EXAMPLES - DATA STRUCTURE

    // -- EXAMPLES - OTHER

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleBookmark")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBookmark
    extends ValueTypeExample<Bookmark> {
        @Property @Getter @Setter
        private Bookmark value = Bookmark.parseElseFail("a:b");
        @Getter
        private Bookmark updateValue = Bookmark.parseElseFail("c:d");
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleOidDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOidDto
    extends ValueTypeExample<OidDto> {
        @Property @Getter @Setter
        private OidDto value = Bookmark.parseElseFail("a:b").toOidDto();
        @Getter
        private OidDto updateValue = Bookmark.parseElseFail("c:d").toOidDto();
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleChangesDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleChangesDto
    extends ValueTypeExample<ChangesDto> {
        @Property @Getter @Setter
        private ChangesDto value = new ChangesDto();
        @Getter
        private ChangesDto updateValue = new ChangesDto();
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleCommandDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCommandDto
    extends ValueTypeExample<CommandDto> {
        @Property @Getter @Setter
        private CommandDto value = new CommandDto();
        @Getter
        private CommandDto updateValue = new CommandDto();
    }

    @Named("causeway.testdomain.valuetypes.ValueTypeExampleInteractionDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleInteractionDto
    extends ValueTypeExample<InteractionDto> {
        @Property @Getter @Setter
        private InteractionDto value = new InteractionDto();
        @Getter
        private InteractionDto updateValue = new InteractionDto();
    }

}
