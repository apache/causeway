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
package org.apache.isis.tooling.metaprog.demoshowcases.value;

import org.apache.isis.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Config;
import org.apache.isis.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Template;
import org.apache.isis.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.TemplateVariant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ValueShowCase {

    BLOB(Config.builder()
            .showcaseName("IsisBlob")
            .javaPackage("demoapp.dom.types.isis.blobs")
            .showcaseValueFullyQualifiedType("org.apache.isis.applib.value.Blob")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BlobValueSemantics")
            .descriptionIfNoPreamble("binary large objects")
            .isisSpecific(true)
            .templates(Template.NO_ORM_SET.remove(Template.SAMPLES))
            .templateVariant(TemplateVariant.LOB)
    ),
    CLOB(Config.builder()
            .showcaseName("IsisClob")
            .javaPackage("demoapp.dom.types.isis.clobs")
            .showcaseValueFullyQualifiedType("org.apache.isis.applib.value.Clob")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.ClobValueSemantics")
            .descriptionIfNoPreamble("character large objects")
            .isisSpecific(true)
            .templates(Template.NO_ORM_SET.remove(Template.SAMPLES))
            .templateVariant(TemplateVariant.LOB)
    ),
    LOCALRESOURCEPATH(Config.builder()
            .showcaseName("IsisLocalResourcePath")
            .javaPackage("demoapp.dom.types.isis.localresourcepaths")
            .showcaseValueFullyQualifiedType("org.apache.isis.applib.value.LocalResourcePath")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.LocalResourcePathValueSemantics")
            .preamble(
                    "The framework has built-in support for representing servlets and other resources that exist alongside the Apache Isis runtime, using the `LocalResourcePath` data type.\n" +
                    "When an action returns an instance of this type, then this is rendered as a redirect request to the browser to that resource.\n" +
                    "In this way you could for example return a link to a PDF or image (to be rendered by the web browser itself), or provide access to dynamic content by redirecting to a servlet.\n")
            .isisSpecific(true)
            .templates(Template.REGULAR_SET_NO_SAMPLES)
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    PASSWORD(Config.builder()
            .showcaseName("IsisPassword")
            .javaPackage("demoapp.dom.types.isis.passwords")
            .showcaseValueFullyQualifiedType("org.apache.isis.applib.value.Password")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.PasswordValueSemantics")
            .descriptionIfNoPreamble("strings that are automatically masked as passwords")
            .isisSpecific(true)
            .templates(Template.NO_VIEWMODEL_SET)
            .templateVariant(TemplateVariant.DEFAULT)
    ),
//    TREENODE(Config.builder()
//            .showcaseName("IsisTreeNode")
//            .javaPackage("demoapp.dom.types.isis.treenode")
//            .showcaseValueFullyQualifiedType("org.apache.isis.applib.graph.tree.TreeNode")
//            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.TreeNodeValueSemantics")
//            .descriptionIfNoPreamble("hierarchical tree nodes")
//            .isisSpecific(true)
//            .templates(Template.REGULAR_SET_NO_SAMPLES)
//            .templateVariant(TemplateVariant.DEFAULT)),
    CALENDAREVENT(Config.builder()
            .showcaseName("IsisCalendarEvent")
            .javaPackage("demoapp.dom.types.isisext.cal")
            .showcaseValueFullyQualifiedType("org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent")
            .showcaseValueSemantics("org.apache.isis.extensions.fullcalendar.applib.value.CalendarEventSemantics")
            .preamble(
                    "The framework has built-in support for modelling events on a calendar, using the `CalendarEvent` data type.\n" +
                    "This is a editable composite type which is rendered as a HTML card.\n" +
                    "It also integrates with the link:https://isis.apache.org/vw/2.0.0-RC1/fullcalendar/about.html[FullCalendar] module; entities that implement the `CalendarEventable` interface (having a property of type `CalendarEvent`) will be rendered on a calendar.\n")
            .isisSpecific(true)
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    // removed, due to https://issues.apache.org/jira/browse/ISIS-3487 ... no support for editable properties, view model or title.
//    JAVAAWTBUFFEREDIMAGE(Config.builder()
//            .showcaseName("BufferedImage")
//            .javaPackage("demoapp.dom.types.javaawt.images")
//            .showcaseValueFullyQualifiedType("java.awt.image.BufferedImage")
//            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BufferedImageValueSemantics")
//            .frameworkSupportForJpa(true)
//            .frameworkSupportForJdo(true)
//            .frameworkSupportForJaxb(true)
//            .templates(Template.REGULAR_SET_NO_SAMPLES)
//            .templateVariant(TemplateVariant.DEFAULT)
//    ),
    JAVAMATHBIGDECIMAL(Config.builder()
            .showcaseName("BigDecimal")
            .javaPackage("demoapp.dom.types.javamath.bigdecimals")
            .showcaseValueFullyQualifiedType("java.math.BigDecimal")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVAMATHBIGINTEGER(Config.builder()
            .showcaseName("BigInteger")
            .javaPackage("demoapp.dom.types.javamath.bigintegers")
            .showcaseValueFullyQualifiedType("java.math.BigInteger")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BigIntegerValueSemantics")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVANETURL(Config.builder()
            .showcaseName("Url")
            .javaPackage("demoapp.dom.types.javanet.urls")
            .showcaseValueFullyQualifiedType("java.net.URL")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.URLValueSemantics")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVASQLDATE(Config.builder()
            .showcaseName("JavaSqlDate")
            .javaPackage("demoapp.dom.types.javasql.javasqldate")
            .showcaseValueFullyQualifiedType("java.sql.Date")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlDateValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.sql.Date`.\n")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.LocalDate`, `java.time.LocalDateTime` or `java.time.OffsetDateTime`")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVASQLTIMESTAMP(Config.builder()
            .showcaseName("JavaSqlTimestamp")
            .javaPackage("demoapp.dom.types.javasql.javasqltimestamp")
            .showcaseValueFullyQualifiedType("java.sql.Timestamp")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaSqlTimeStampValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.sql.Timestamp`.")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.OffsetDateTime`")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaSqlJaxbAdapters.TimestampToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMELOCALDATE(Config.builder()
            .showcaseName("LocalDate")
            .javaPackage("demoapp.dom.types.javatime.javatimelocaldate")
            .showcaseValueFullyQualifiedType("java.time.LocalDate")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.LocalDate`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.LocalDateToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMELOCALDATETIME(Config.builder()
            .showcaseName("LocalDateTime")
            .javaPackage("demoapp.dom.types.javatime.javatimelocaldatetime")
            .showcaseValueFullyQualifiedType("java.time.LocalDateTime")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.LocalDateTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.LocalDateTime`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.LocalDateTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMELOCALTIME(Config.builder()
            .showcaseName("LocalTime")
            .javaPackage("demoapp.dom.types.javatime.javatimelocaltime")
            .showcaseValueFullyQualifiedType("java.time.LocalTime")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.LocalTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.LocalTime`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.LocalTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMEOFFSETDATETIME(Config.builder()
            .showcaseName("OffsetDateTime")
            .javaPackage("demoapp.dom.types.javatime.javatimeoffsetdatetime")
            .showcaseValueFullyQualifiedType("java.time.OffsetDateTime")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.OffsetDateTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.OffsetDateTime`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.OffsetDateTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMEOFFSETTIME(Config.builder()
            .showcaseName("OffsetTime")
            .javaPackage("demoapp.dom.types.javatime.javatimeoffsettime")
            .showcaseValueFullyQualifiedType("java.time.OffsetTime")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.OffsetTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.OffsetTime`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.OffsetTimeAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVATIMEZONEDDATETIME(Config.builder()
            .showcaseName("ZonedDateTime")
            .javaPackage("demoapp.dom.types.javatime.javatimezoneddatetime")
            .showcaseValueFullyQualifiedType("java.time.ZonedDateTime")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.ZonedDateTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.time.ZonedDateTime`.")
            .jaxbAdapter("org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters.ZonedDateTimeAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JAVAUTILDATE(Config.builder()
            .showcaseName("JavaUtilDate")
            .javaPackage("demoapp.dom.types.javautil.javautildate")
            .showcaseValueFullyQualifiedType("java.util.Date")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.temporal.legacy.JavaUtilDateValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `java.util.Date`.")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.LocalDate`, `java.time.LocalDateTime` or `java.time.OffsetDateTime`.")
            .frameworkSupportForJaxb(true)
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JODADATETIME(Config.builder()
            .showcaseName("JodaDateTime")
            .javaPackage("demoapp.dom.types.jodatime.jodadatetime")
            .showcaseValueFullyQualifiedType("org.joda.time.DateTime")
            .showcaseValueSemantics("org.apache.isis.valuetypes.jodatime.integration.valuesemantics.JodaDateTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `org.joda.time.DateTime`.")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.OffsetDateTime`.")
            .jaxbAdapter("org.apache.isis.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.DateTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JODALOCALDATE(Config.builder()
            .showcaseName("JodaLocalDate")
            .javaPackage("demoapp.dom.types.jodatime.jodalocaldate")
            .showcaseValueFullyQualifiedType("org.joda.time.LocalDate")
            .showcaseValueSemantics("org.apache.isis.valuetypes.jodatime.integration.valuesemantics.JodaLocalDateValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `org.joda.time.LocalDate`")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.LocalDate`.")
            .jaxbAdapter("org.apache.isis.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalDateToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JODALOCALDATETIME(Config.builder()
            .showcaseName("JodaLocalDateTime")
            .javaPackage("demoapp.dom.types.jodatime.jodalocaldatetime")
            .showcaseValueFullyQualifiedType("org.joda.time.LocalDateTime")
            .showcaseValueSemantics("org.apache.isis.valuetypes.jodatime.integration.valuesemantics.JodaLocalDateTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `org.joda.time.LocalDateTime`.")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.LocalDateTime`.")
            .jaxbAdapter("org.apache.isis.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalDateTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    JODALOCALTIME(Config.builder()
            .showcaseName("JodaLocalTime")
            .javaPackage("demoapp.dom.types.jodatime.jodalocaltime")
            .showcaseValueFullyQualifiedType("org.joda.time.LocalTime")
            .showcaseValueSemantics("org.apache.isis.valuetypes.jodatime.integration.valuesemantics.JodaLocalTimeValueSemantics")
            .preamble("The framework has built-in support for a number of temporal types, including `org.joda.time.LocalTime`.")
            .caveat("Rather than use this data type, consider using a more modern class such as `java.time.LocalTime`.")
            .jaxbAdapter("org.apache.isis.valuetypes.jodatime.applib.jaxb.JodaTimeJaxbAdapters.LocalTimeToStringAdapter")
            .templateVariant(TemplateVariant.DEFAULT)
    ),
    MARKUP(Config.builder()
            .showcaseName("IsisMarkup")
            .javaPackage("demoapp.dom.types.isis.markups")
            .showcaseValueFullyQualifiedType("org.apache.isis.applib.value.Markup")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.MarkupValueSemantics")
            .descriptionIfNoPreamble("arbitrary HTML markup")
            .isisSpecific(true)
            .templateVariant(TemplateVariant.LOB)
    ),
    MARKDOWN(Config.builder()
            .showcaseName("IsisMarkdown")
            .javaPackage("demoapp.dom.types.isisval.markdowns")
            .showcaseValueFullyQualifiedType("org.apache.isis.valuetypes.markdown.applib.value.Markdown")
            .showcaseValueSemantics("org.apache.isis.valuetypes.markdown.metamodel.semantics.MarkdownValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own MarkdownSamples
            .descriptionIfNoPreamble("HTML markup, written in Markdown")
            .isisSpecific(true)
            .templateVariant(TemplateVariant.LOB)
    ),
    ASCIIDOC(Config.builder()
            .showcaseName("IsisAsciiDoc")
            .javaPackage("demoapp.dom.types.isisval.asciidocs")
            .showcaseValueFullyQualifiedType("org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc")
            .showcaseValueSemantics("org.apache.isis.valuetypes.asciidoc.metamodel.semantics.AsciiDocValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own AsciiDocSamples
            .descriptionIfNoPreamble("HTML markup, written in Asciidoc")
            .isisSpecific(true)
            .templateVariant(TemplateVariant.LOB)
    ),
    VEGA(Config.builder()
            .showcaseName("IsisVega")
            .javaPackage("demoapp.dom.types.isisval.vegas")
            .showcaseValueFullyQualifiedType("org.apache.isis.valuetypes.vega.applib.value.Vega")
            .showcaseValueSemantics("org.apache.isis.valuetypes.vega.metamodel.semantics.VegaValueSemantics")
            .descriptionIfNoPreamble("a visualization grammar (to render interactive graphs, maps and other designs)")
            .isisSpecific(true)
            .templateVariant(TemplateVariant.LOB)
    ),
    UUID(Config.builder()
            .showcaseName("JavaUtilUuid")
            .javaPackage("demoapp.dom.types.javautil.uuids")
            .showcaseValueFullyQualifiedType("java.util.UUID")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics")
            .frameworkSupportForJpa(true)
            .frameworkSupportForJdo(false)  // JDO supports out of the box
    ),
    ENUM(Config.builder()
            .showcaseName("JavaLangEnum")
            .javaPackage("demoapp.dom.types.javalang.enums")
            .showcaseValueFullyQualifiedType("demoapp.dom._infra.samples.DemoEnum")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.EnumValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own EnumSamples
    ),
    STRING(Config.builder()
            .showcaseName("JavaLangString")
            .javaPackage("demoapp.dom.types.javalang.strings")
            .showcaseValueFullyQualifiedType("java.lang.String")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.StringValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own NameSamples
    ),
//    VOID(Config.builder()
//            .showcaseName("JavaLangVoid")
//            .javaPackage("demoapp.dom.types.javalang.voids")
//            .showcaseValueType("java.lang.Void")
//            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.VoidValueSemantics")),

    PBOOL(Config.builder()
            .showcaseName("PrimitiveBoolean")
            .javaPackage("demoapp.dom.types.primitive.booleans")
            .showcaseValueFullyQualifiedType("boolean")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BooleanValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PCHAR(Config.builder()
            .showcaseName("PrimitiveChar")
            .javaPackage("demoapp.dom.types.primitive.chars")
            .showcaseValueFullyQualifiedType("char")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.CharacterValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PLONG(Config.builder()
            .showcaseName("PrimitiveLong")
            .javaPackage("demoapp.dom.types.primitive.longs")
            .showcaseValueFullyQualifiedType("long")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.LongValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PINT(Config.builder()
            .showcaseName("PrimitiveInt")
            .javaPackage("demoapp.dom.types.primitive.ints")
            .showcaseValueFullyQualifiedType("int")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.IntValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PSHORT(Config.builder()
            .showcaseName("PrimitiveShort")
            .javaPackage("demoapp.dom.types.primitive.shorts")
            .showcaseValueFullyQualifiedType("short")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.ShortValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PBYTE(Config.builder()
            .showcaseName("PrimitiveByte")
            .javaPackage("demoapp.dom.types.primitive.bytes")
            .showcaseValueFullyQualifiedType("byte")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.ByteValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PDOUBLE(Config.builder()
            .showcaseName("PrimitiveDouble")
            .javaPackage("demoapp.dom.types.primitive.doubles")
            .showcaseValueFullyQualifiedType("double")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.DoubleValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    PFLOAT(Config.builder()
            .showcaseName("PrimitiveFloat")
            .javaPackage("demoapp.dom.types.primitive.floats")
            .showcaseValueFullyQualifiedType("float")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.FloatValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)
    ),
    WBOOL(Config.builder()
            .showcaseName("WrapperBoolean")
            .javaPackage("demoapp.dom.types.javalang.booleans")
            .showcaseValueFullyQualifiedType("java.lang.Boolean")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.BooleanValueSemantics")
    ),
    WCHAR(Config.builder()
            .showcaseName("WrapperCharacter")
            .javaPackage("demoapp.dom.types.javalang.characters")
            .showcaseValueFullyQualifiedType("java.lang.Character")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.CharacterValueSemantics")
    ),
    WLONG(Config.builder()
            .showcaseName("WrapperLong")
            .javaPackage("demoapp.dom.types.javalang.longs")
            .showcaseValueFullyQualifiedType("java.lang.Long")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.LongValueSemantics")
    ),
    WINT(Config.builder()
            .showcaseName("WrapperInteger")
            .javaPackage("demoapp.dom.types.javalang.integers")
            .showcaseValueFullyQualifiedType("java.lang.Integer")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.IntValueSemantics")
    ),
    WSHORT(Config.builder()
            .showcaseName("WrapperShort")
            .javaPackage("demoapp.dom.types.javalang.shorts")
            .showcaseValueFullyQualifiedType("java.lang.Short")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.ShortValueSemantics")
    ),
    WBYTE(Config.builder()
            .showcaseName("WrapperByte")
            .javaPackage("demoapp.dom.types.javalang.bytes")
            .showcaseValueFullyQualifiedType("java.lang.Byte")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.ByteValueSemantics")
    ),
    WDOUBLE(Config.builder()
            .showcaseName("WrapperDouble")
            .javaPackage("demoapp.dom.types.javalang.doubles")
            .showcaseValueFullyQualifiedType("java.lang.Double")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.DoubleValueSemantics")
    ),
    WFLOAT(Config.builder()
            .showcaseName("WrapperFloat")
            .javaPackage("demoapp.dom.types.javalang.floats")
            .showcaseValueFullyQualifiedType("java.lang.Float")
            .showcaseValueSemantics("org.apache.isis.core.metamodel.valuesemantics.FloatValueSemantics")
    ),
    ;

    @Getter final ValueTypeGenTemplate.Config.ConfigBuilder configBuilder;

}
