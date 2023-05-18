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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.FileUtils;
import org.apache.causeway.commons.io.TextUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

@RequiredArgsConstructor
public class ValueTypeGenTemplate {

    @Value @Builder
    public static class Config {
        final File outputRootDir;
        final String showcaseName;

        final String showcaseValueFullyQualifiedType;
        final String showcaseValueSemantics;

        public String getShowcaseValueSimpleType() {
            String fqt = getShowcaseValueFullyQualifiedType();
            int i = fqt.lastIndexOf(".");
            String simpleType = fqt.substring(i + 1);
            return simpleType;
        }

        /**
         * If present, is used instead the first sentence.
         */
        final String preamble;

        /**
         * If present, is used after the first sentence.
         */
        final String caveat;

        public String getPreamble() {
            return preamble != null
                    ? preamble
                    : "The framework has built-in support for " +
                    (getDescriptionIfNoPreamble() != null
                        ? getDescriptionIfNoPreamble() + ", using"
                        : "") +
                    " the `" + getShowcaseValueSimpleType() + "` data type.";
        }


        /**
         * If present, is used within the first sentence (unless preamble specified, in which case that overrides
         */
        final String descriptionIfNoPreamble;
        /**
         * If present, adds a NOTE: ...  and also changes what {@link #getJdoTypeSupportNotice()}, {@link #getJpaTypeSupportNotice()} and {@link #getJaxbTypeSupportNotice()} returns.
         */
        final boolean causewaySpecific;

        final boolean frameworkSupportForJpa;
        /**
         * Implied by {@link #isCausewaySpecific()}, if present then indicates that the framework has added support for JPA persistence for certain data types.
         */
        private boolean isFrameworkSupportForJpa() {
            return causewaySpecific || frameworkSupportForJpa;
        }
        public String getJpaTypeSupportNotice() {
            return isFrameworkSupportForJpa()
                        ? "Apache Causeway provides its own implementation of the relevant JPA extension points for the `" + getShowcaseValueSimpleType() + "` value type, meaning that JPA can persist properties of this value type without further configuration."
                        : "JPA supports `" + getShowcaseValueSimpleType() + "` out-of-the-box, so no special annotations are required.\nSee link:https://www.objectdb.com/java/jpa/entity/types#simple_java_data_types[ObjectDB]";
        }

        final boolean frameworkSupportForJdo;
        /**
         * Implied by {@link #isCausewaySpecific()}, if present then indicates that the framework has added support for JDO persistence for certain data types.
         */
        private boolean isFrameworkSupportForJdo() {
            return causewaySpecific || frameworkSupportForJdo;
        }
        public String getJdoTypeSupportNotice() {
            return isFrameworkSupportForJdo()
                        ? "Apache Causeway provides its own implementation of the relevant JDO extension points for the `" + getShowcaseValueSimpleType() + "` value type, meaning that JDO can persist properties of this value type without further configuration."
                        : "JDO supports `" + getShowcaseValueSimpleType() + "` out-of-the-box, so no special annotations are required.\nSee link:https://www.datanucleus.org/products/accessplatform_6_0/jdo/mapping.html#_primitive_and_java_lang_types[DataNucleus]";
        }

        final boolean frameworkSupportForJaxb;
        /**
         * Implied by {@link #isCausewaySpecific()} or a non-null {@link #getJaxbAdapter()}, if present then indicates that the framework has added support for JAXB serialization for certain data types.
         */
        private boolean isFrameworkSupportForJaxb() {
            return causewaySpecific || frameworkSupportForJaxb || jaxbAdapter != null;
        }
        public String getJaxbTypeSupportNotice() {
            return isFrameworkSupportForJaxb()
                    ? "Apache Causeway provides its own implementation of `@XmlJavaTypeAdapter` for the `" + getShowcaseValueSimpleType() + "` value type, meaning that JAXB can serialize properties of this value type without further configuration."
                    : "JAXB supports `" + getShowcaseValueSimpleType() + "` out-of-the-box, so no special annotations are required.\nSee link:https://docs.oracle.com/cd/E12840_01/wls/docs103/webserv/data_types.html#wp223908[Oracle]";
        }

        /**
         * If specified, then used to annotate properties of JAXB view models.
         */
        final String jaxbAdapter;
        final String javaPackage;
        @Builder.Default
        final String fileNamePlaceholderForShowcaseName = "$Template";
        @Builder.Default
        final String generatedFileNotice = "This file was GENERATED by the showcase generator (tooling). Do NOT edit!";
        @Singular
        final Map<String, String> templateVariables = new HashMap<>();
        @Builder.Default
        final Can<Template> templates = Template.REGULAR_SET;
        @Builder.Default
        final TemplateVariant templateVariant = TemplateVariant.DEFAULT;

    }

    @RequiredArgsConstructor
    enum Generator {
        DOC(".adoc"){
            @Override String formatAsComment(final String text) {
                return " // " + text; }
            @Override String formatAsTemplateVar(final String key) {
                return "#{" + key + "}"; } // ADOC pass through var syntax
        },
        JAVA(".java"){
            @Override String formatAsComment(final String text) {
                return "/* " + text + " */"; }
            @Override String formatAsTemplateVar(final String key) {
                return "/*${" + key + "}*/"; }
        },
        LAYOUT(".layout.xml"){
            @Override String formatAsComment(final String text) {
                return "<!-- " + text + " -->"; }
            @Override String formatAsTemplateVar(final String key) {
                return "<!--${" + key + "}-->"; }
        };
        boolean isDoc() { return this == DOC; }
        boolean isJava() { return this == JAVA; }
        boolean isLayout() { return this == LAYOUT; }
        final String fileSuffix;
        abstract String formatAsComment(String text);
        abstract String formatAsTemplateVar(String key);
    }

    @RequiredArgsConstructor
    enum TemplateVariant {
        DEFAULT(""),
        PRIMITIVE("~primitive"),
        LOB("~lob"),
        ;
        @Getter private final String suffix;
    }

    @RequiredArgsConstructor
    enum Template {
        HOLDER("holder/%sHolder", Generator.JAVA),
        HOLDER2("holder/%sHolder2", Generator.JAVA),
        HOLDER_ACTION_RETURNING("holder/%sHolder_actionReturning", Generator.JAVA),
        HOLDER_ACTION_RETURNING_ARRAY("holder/%sHolder_actionReturningArray", Generator.JAVA),
        HOLDER_ACTION_RETURNING_COLLECTION("holder/%sHolder_actionReturningCollection", Generator.JAVA),
        HOLDER_MIXIN_PROPERTY("holder/%sHolder_mixinProperty", Generator.JAVA),
        HOLDER_UPDATE_RO_PROPERTY("holder/%sHolder_updateReadOnlyProperty", Generator.JAVA),
        HOLDER_UPDATE_ROO_PROPERTY("holder/%sHolder_updateReadOnlyOptionalProperty", Generator.JAVA),
        HOLDER_UPDATE_RO_PROPERTY_WITH_CHOICES("holder/%sHolder_updateReadOnlyPropertyWithChoices", Generator.JAVA),
        HOLDER_UPDATE_ROO_PROPERTY_WITH_CHOICES("holder/%sHolder_updateReadOnlyOptionalPropertyWithChoices", Generator.JAVA),

        JDO("jdo/%sJdo", Generator.JAVA),
        JDO_ENTITIES("jdo/%sJdoEntities", Generator.JAVA),
        JDO_DESCRIPTION("jdo/%sJdo-description", Generator.DOC),

        JPA("jpa/%sJpa", Generator.JAVA),
        JPA_ENTITIES("jpa/%sJpaEntities", Generator.JAVA),
        JPA_DESCRIPTION("jpa/%sJpa-description", Generator.DOC),

        ENTITY("persistence/%sEntity", Generator.JAVA),
        ENTITY_LAYOUT("persistence/%sEntity", Generator.LAYOUT),
        SEEDING("persistence/%sSeeding", Generator.JAVA),

        SAMPLES("samples/%sSamples", Generator.JAVA),

        VIEWMODEL("vm/%sVm", Generator.JAVA),
        VIEWMODEL_DESCRIPTION("vm/%sVm-description", Generator.DOC),
        VIEWMODEL_LAYOUT("vm/%sVm", Generator.LAYOUT),

        COLLECTION("%ss", Generator.JAVA),
        COMMON_DOC("%ss-common", Generator.DOC),
        DESCRIPTION("%ss-description", Generator.DOC),
        COLLECTION_LAYOUT("%ss", Generator.LAYOUT),
        ;

        public static Can<Template> REGULAR_SET = Can.ofArray(Template.values())
            .remove(HOLDER_ACTION_RETURNING_ARRAY);

        public static Can<Template> NO_ORM_SET = REGULAR_SET
                .remove(JPA)
                .remove(JPA_DESCRIPTION)
                .remove(JPA_ENTITIES)
                .remove(JDO)
                .remove(JDO_DESCRIPTION)
                .remove(JDO_ENTITIES);

        public static Can<Template> NO_VIEWMODEL_SET = REGULAR_SET
                .remove(VIEWMODEL)
                .remove(VIEWMODEL_DESCRIPTION)
                .remove(VIEWMODEL_LAYOUT);

        public static Can<Template> REGULAR_SET_NO_SAMPLES = REGULAR_SET
                .remove(SAMPLES);

        public static Can<Template> PRIMITIVE_SET = Can.ofArray(Template.values())
                .remove(HOLDER_ACTION_RETURNING_COLLECTION)
                .remove(HOLDER_UPDATE_ROO_PROPERTY)
                .remove(HOLDER_UPDATE_ROO_PROPERTY_WITH_CHOICES)
                .remove(Template.SAMPLES);

        private final String pathTemplate;
        private final Generator generator;
        private final File outputFile(final Config config) {
            return new File(config.getOutputRootDir(),
                    String.format(pathTemplate, config.getShowcaseName())
                    + generator.fileSuffix)
                    .getAbsoluteFile();
        }
        private final File templateFile(final Config config) {
            return FileUtils.existingFile(templateFile(config, config.templateVariant)) // existence is optional
                    .orElseGet(()->{
                        // existence is mandatory
                        val defaultTemplateFile = templateFile(config, TemplateVariant.DEFAULT);
                        return FileUtils.existingFile(defaultTemplateFile)
                                .orElseThrow(()->_Exceptions.noSuchElement("template %s not found", defaultTemplateFile));
                    });
        }
        private final File templateFile(final Config config, final TemplateVariant templateVariant) {
            val templateFile = new File("src/main/resources",
                    String.format(pathTemplate, config.fileNamePlaceholderForShowcaseName)
                    + templateVariant.suffix
                    + generator.fileSuffix)
                    .getAbsoluteFile();
            return templateFile;
        }
        private final String javaPackage(final Config config) {
            return Optional.ofNullable(new File(String.format(pathTemplate, "X")).getParent())
                    .map(path->path.replace('/', '.'))
                    .map(suffix->config.javaPackage + "." + suffix)
                    .orElse(config.javaPackage);
        }
    }

    @RequiredArgsConstructor
    static class TemplateVars extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;
        private final Generator generator;
        @Override
        public String put(final String key, final String value) {
            return super.put(generator.formatAsTemplateVar(key), value);
        }
        @Override
        public void putAll(final Map<? extends String, ? extends String> other) {
            other.forEach((key, value)->put(key, value));
        }
        public void putRaw(final String key, final String value) {
            super.put(key, value);
        }
    }

    final Config config;

    @SneakyThrows
    public void generate(final Consumer<File> onSourceGenerated) {

        for(var template: config.getTemplates()) {

            val templateFile = template.templateFile(config);

            val genTarget = template.outputFile(config);

            val templateVars = new TemplateVars(template.generator);
            templateVars.putAll(config.templateVariables);
            templateVars.put("java-package", template.javaPackage(config));
            templateVars.put("showcase-name", config.showcaseName);
            templateVars.put("showcase-preamble", config.getPreamble());
            templateVars.put("showcase-caveat", (config.getCaveat() != null ? config.getCaveat() + "\n\n" : ""));
            templateVars.put("showcase-note-if-causeway-specific", (config.isCausewaySpecific() ? "NOTE: This is an Apache Causeway specific data type.\n\n": ""));
            templateVars.put("showcase-simple-type", config.getShowcaseValueSimpleType());
            templateVars.put("showcase-fully-qualified-type", config.showcaseValueFullyQualifiedType);
            templateVars.put("showcase-simple-type-boxed",
                    Optional.ofNullable(ClassUtils.resolvePrimitiveClassName(config.showcaseValueFullyQualifiedType))
                    .map(ClassUtils::resolvePrimitiveIfNecessary)
                    .map(Class::getName)
                    .orElse(config.getShowcaseValueSimpleType()));

            templateVars.put("showcase-simple-type-getter-prefix",
                    Optional.ofNullable(ClassUtils.resolvePrimitiveClassName(config.showcaseValueFullyQualifiedType))
                    .map(cls->boolean.class.equals(cls) ? "is" : "get")
                    .orElse("get"));

            templateVars.put("showcase-jaxb-adapter-type", (config.getJaxbAdapter() != null ? "@XmlJavaTypeAdapter(" + config.getJaxbAdapter() + ".class)\n    " : "" ));
            templateVars.put("showcase-java-package", config.javaPackage);
            templateVars.put("showcase-value-semantics-provider", config.showcaseValueSemantics);
            templateVars.put("generated-file-notice", template.generator.formatAsComment(config.generatedFileNotice));

            templateVars.put("jdo-type-support-notice", config.getJdoTypeSupportNotice());
            templateVars.put("jpa-type-support-notice", config.getJpaTypeSupportNotice());
            templateVars.put("jaxb-type-support-notice", config.getJaxbTypeSupportNotice());


            // allow for ADOC IDE tools, to properly resolve include statements,
            // that is referenced (template) files should exist
            if(template.generator.isDoc()) {
                templateVars.putRaw("$Template", config.showcaseName);
                // purge any TemplateVariant occurrences in ADOC templates
                Stream.of(TemplateVariant.values())
                .map(TemplateVariant::getSuffix)
                .filter(_Strings::isNotEmpty)
                .forEach(variantSuffix->templateVars.putRaw(variantSuffix, ""));
            }

            generateFromTemplate(templateVars, templateFile, genTarget);
            onSourceGenerated.accept(genTarget);
        }

    }

    private void generateFromTemplate(
            final Map<String, String> templateVars, final File template, final File genTarget) {
        val templateLines = TextUtils.readLinesFromFile(template, StandardCharsets.UTF_8);

        FileUtils.makeDir(genTarget.getParentFile());

        TextUtils.writeLinesToFile(templateLines
                .map(line->templateProcessor(templateVars, line)),
                genTarget, StandardCharsets.UTF_8);
    }

    private String templateProcessor(final Map<String, String> templateVars, final String line) {
        val lineRef = _Refs.stringRef(line);
        templateVars.forEach((key, value)->{
            lineRef.update(s->s.replace(key, value));
        });
        return lineRef.getValue();
    }

}
