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
package org.apache.causeway.tooling.metaprog.demoshowcases.value;

import org.apache.causeway.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Config;
import org.apache.causeway.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Config.ConfigBuilder;
import org.apache.causeway.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.Template;
import org.apache.causeway.tooling.metaprog.demoshowcases.value.ValueTypeGenTemplate.TemplateVariant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public enum ValueShowCase {

    MARKUP(Config.builder()
            .showcaseName("CausewayMarkup")
            .javaPackage("demoapp.dom.types.causeway.markups")
            .showcaseValueType("org.apache.causeway.applib.value.Markup")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.MarkupValueSemantics")
            .templateVariant(TemplateVariant.LOB)),
    MARKDOWN(Config.builder()
            .showcaseName("CausewayMarkdown")
            .javaPackage("demoapp.dom.types.causewayval.markdowns")
            .showcaseValueType("org.apache.causeway.valuetypes.markdown.applib.value.Markdown")
            .showcaseValueSemantics("org.apache.causeway.valuetypes.markdown.metamodel.semantics.MarkdownValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own MarkdownSamples
            .templateVariant(TemplateVariant.LOB)),
    ASCIIDOC(Config.builder()
            .showcaseName("CausewayAsciiDoc")
            .javaPackage("demoapp.dom.types.causewayval.asciidocs")
            .showcaseValueType("org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc")
            .showcaseValueSemantics("org.apache.causeway.valuetypes.asciidoc.metamodel.semantics.AsciiDocValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES) // demo provides its own AsciiDocSamples
            .templateVariant(TemplateVariant.LOB)),
    VEGA(Config.builder()
            .showcaseName("CausewayVega")
            .javaPackage("demoapp.dom.types.causewayval.vegas")
            .showcaseValueType("org.apache.causeway.valuetypes.vega.applib.value.Vega")
            .showcaseValueSemantics("org.apache.causeway.valuetypes.vega.metamodel.semantics.VegaValueSemantics")
            .templateVariant(TemplateVariant.LOB)),
    UUID(Config.builder()
            .showcaseName("JavaUtilUuid")
            .javaPackage("demoapp.dom.types.javautil.uuids")
            .showcaseValueType("java.util.UUID")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.UUIDValueSemantics")),
    ENUM(Config.builder()
            .showcaseName("JavaLangEnum")
            .javaPackage("demoapp.dom.types.javalang.enums")
            .showcaseValueType("demoapp.dom._infra.samples.DemoEnum")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.EnumValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES)), // demo provides its own EnumSamples
    STRING(Config.builder()
            .showcaseName("JavaLangString")
            .javaPackage("demoapp.dom.types.javalang.strings")
            .showcaseValueType("java.lang.String")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.StringValueSemantics")
            .templates(Template.REGULAR_SET_NO_SAMPLES)), // demo provides its own NameSamples
//    VOID(Config.builder()
//            .showcaseName("JavaLangVoid")
//            .javaPackage("demoapp.dom.types.javalang.voids")
//            .showcaseValueType("java.lang.Void")
//            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.VoidValueSemantics")),

    PBOOL(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveBoolean")
            .javaPackage("demoapp.dom.types.primitive.booleans")
            .showcaseValueType("boolean")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.BooleanValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PCHAR(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveChar")
            .javaPackage("demoapp.dom.types.primitive.chars")
            .showcaseValueType("char")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.CharacterValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PLONG(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveLong")
            .javaPackage("demoapp.dom.types.primitive.longs")
            .showcaseValueType("long")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.LongValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PINT(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveInt")
            .javaPackage("demoapp.dom.types.primitive.ints")
            .showcaseValueType("int")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PSHORT(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveShort")
            .javaPackage("demoapp.dom.types.primitive.shorts")
            .showcaseValueType("short")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.ShortValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PBYTE(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveByte")
            .javaPackage("demoapp.dom.types.primitive.bytes")
            .showcaseValueType("byte")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.ByteValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PDOUBLE(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveDouble")
            .javaPackage("demoapp.dom.types.primitive.doubles")
            .showcaseValueType("double")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.DoubleValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    PFLOAT(fundamentalTypeSupportNotice()
            .showcaseName("PrimitiveFloat")
            .javaPackage("demoapp.dom.types.primitive.floats")
            .showcaseValueType("float")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.FloatValueSemantics")
            .templates(Template.PRIMITIVE_SET)
            .templateVariant(TemplateVariant.PRIMITIVE)),
    WBOOL(fundamentalTypeSupportNotice()
            .showcaseName("WrapperBoolean")
            .javaPackage("demoapp.dom.types.javalang.booleans")
            .showcaseValueType("java.lang.Boolean")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.BooleanValueSemantics")),
    WCHAR(fundamentalTypeSupportNotice()
            .showcaseName("WrapperCharacter")
            .javaPackage("demoapp.dom.types.javalang.characters")
            .showcaseValueType("java.lang.Character")
            .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.CharacterValueSemantics")),
    WLONG(fundamentalTypeSupportNotice()
                .showcaseName("WrapperLong")
                .javaPackage("demoapp.dom.types.javalang.longs")
                .showcaseValueType("java.lang.Long")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.LongValueSemantics")),
    WINT(fundamentalTypeSupportNotice()
                .showcaseName("WrapperInteger")
                .javaPackage("demoapp.dom.types.javalang.integers")
                .showcaseValueType("java.lang.Integer")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics")),
    WSHORT(fundamentalTypeSupportNotice()
                .showcaseName("WrapperShort")
                .javaPackage("demoapp.dom.types.javalang.shorts")
                .showcaseValueType("java.lang.Short")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.ShortValueSemantics")),
    WBYTE(fundamentalTypeSupportNotice()
                .showcaseName("WrapperByte")
                .javaPackage("demoapp.dom.types.javalang.bytes")
                .showcaseValueType("java.lang.Byte")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.ByteValueSemantics")),
    WDOUBLE(fundamentalTypeSupportNotice()
                .showcaseName("WrapperDouble")
                .javaPackage("demoapp.dom.types.javalang.doubles")
                .showcaseValueType("java.lang.Double")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.DoubleValueSemantics")),
    WFLOAT(fundamentalTypeSupportNotice()
                .showcaseName("WrapperFloat")
                .javaPackage("demoapp.dom.types.javalang.floats")
                .showcaseValueType("java.lang.Float")
                .showcaseValueSemantics("org.apache.causeway.core.metamodel.valuesemantics.FloatValueSemantics")),
    ;

    @Getter final ValueTypeGenTemplate.Config.ConfigBuilder configBuilder;

    private static ConfigBuilder fundamentalTypeSupportNotice() {
        val defaults = Config.builder().build();
        return Config.builder()
                .jdoTypeSupportNotice(defaults.getJdoTypeSupportNotice()
                        + " see link:https://www.datanucleus.org/products/accessplatform_6_0/jdo/mapping.html#_primitive_and_java_lang_types[DataNucleus]")
                .jpaTypeSupportNotice(defaults.getJdoTypeSupportNotice()
                        + " see link:https://www.objectdb.com/java/jpa/entity/types#simple_java_data_types[ObjectDB]")
                .jaxbTypeSupportNotice(defaults.getJaxbTypeSupportNotice()
                        + " see link:https://docs.oracle.com/cd/E12840_01/wls/docs103/webserv/data_types.html#wp223908[Oracle]");
    }

}
