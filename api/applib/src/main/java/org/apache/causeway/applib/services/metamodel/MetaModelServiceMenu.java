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
package org.apache.causeway.applib.services.metamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.BiFunction;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.resources._Xml;
import org.apache.causeway.commons.internal.resources._Xml.WriteOptions;
import org.apache.causeway.schema.metamodel.v2.MetamodelDto;

import lombok.val;

/**
 * Provides a UI to allow domain model metadata (obtained from {@link MetaModelService}) to be downloaded.
 *
 * @since 2.0 {@index}
 */
@Named(MetaModelServiceMenu.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class MetaModelServiceMenu {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".MetaModelServiceMenu";

    public static enum ExportFormat implements BiFunction<String, MetamodelDto, Clob>  {
        ASCII{
            @Override public Clob apply(final String fileName, final MetamodelDto dto) {
                val content = _AsciiExport.toAscii(dto).toString();
                return Clob.of(fileName, CommonMimeType.TXT, content);
            }
        },
        CSV{
            @Override public Clob apply(final String fileName, final MetamodelDto dto) {
                val content = _CsvExport2.toCsv(dto);
                return Clob.of(fileName, CommonMimeType.CSV, content);
            }
        },
        //XXX infinite recursion
//        JSON{
//            @Override public Clob apply(final String fileName, final MetamodelDto dto) {
//                val content = _Json.toString(dto);
//                return Clob.of(fileName, CommonMimeType.JSON, content);
//            }
//        },
        XML{
            @Override public Clob apply(final String fileName, final MetamodelDto dto) {
                val content = _Xml.writeXml(dto, WriteOptions.builder().formattedOutput(true).build())
                        .ifFailureFail()
                        .getValue()
                        .orElse("");
                return Clob.of(fileName, CommonMimeType.XML, content);
            }
        },
        //XXX empty
//        YAML{
//            @Override public Clob apply(final String fileName, final MetamodelDto dto) {
//                val content = _Yaml.toString(dto).ifFailureFail().getValue().orElse("");
//                return Clob.of(fileName, CommonMimeType.YAML, content);
//            }
//        },
    }

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> { }

    @Action(
            domainEvent = downloadMetaModel.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model",
            sequence="500.500.2")
    public class downloadMetaModel {

        public class ActionDomainEvent extends MetaModelServiceMenu.ActionDomainEvent<downloadMetaModel> { }

        @MemberSupport public Blob act(
                @ParameterLayout(named = "file name (does not require an extension)")
                final String fileName,

                @ParameterLayout(named = "Namespaces",
                        describedAs="Subset of the complete meta model, only including namespaces starting with given prefix")
                final List<String> namespaces,

                @Parameter
                final boolean includeInterfaces,

                @Parameter
                final ExportFormat exportFormat,

                @Parameter
                @ParameterLayout(
                    describedAs="Whether to zip the exported file.")
                final boolean zip
        ) {

            val config = defaultConfig(includeInterfaces, namespaces);

            final MetamodelDto metamodelDto =  metaModelService.exportMetaModel(config);

            val blob = exportFormat.apply(fileName, metamodelDto)
                    .toBlob(UTF_8);
            return zip
                    ? blob.zip()
                    : blob;
        }

        @MemberSupport public String validateAct(
                final String fileName, final List<String> namespacePrefixes, final boolean includeInterfaces,
                final ExportFormat exportFormat, final boolean zip) {
            if(namespacePrefixes == null || namespacePrefixes.isEmpty()) {
                return "At least one package must be selected";
            }
            return null;
        }

        @MemberSupport public String defaultFileName() { return "metamodel"; }
        @MemberSupport public List<String> choicesNamespaces() { return namespaceChoices(); }
        @MemberSupport public boolean defaultIncludeInterfaces() { return false; }
        @MemberSupport public ExportFormat defaultExportFormat() { return ExportFormat.XML; }
        @MemberSupport public boolean defaultZip() { return true; }

    }


    @Action(
            domainEvent = downloadMetaModelCsv.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model (CSV)",
            sequence="500.500.2")
    public class downloadMetaModelCsv {

        public class ActionDomainEvent extends MetaModelServiceMenu.ActionDomainEvent<downloadMetaModelCsv> { }

        @MemberSupport public Blob act(
                @ParameterLayout(named = ".csv file name")
                final String csvFileName) {

            final DomainModel domainModel =  metaModelService.getDomainModel();
            final StringBuilder csv = _CsvExport.toCsv(domainModel);

            return Clob.of(csvFileName, CommonMimeType.CSV, csv)
                    .toBlob(UTF_8)
                    .zip();
        }

        @MemberSupport public String default0Act() {
            return "metamodel.csv";
        }

    }

    @Action(
            domainEvent = downloadMetaModelDiff.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Generate Meta Model Diff",
            sequence="500.500.2")
    public class downloadMetaModelDiff {

        public class ActionDomainEvent extends MetaModelServiceMenu.ActionDomainEvent<downloadMetaModelDiff> { }

        @MemberSupport public Blob act(
                @ParameterLayout(named = ".txt file name")
                final String fileName,

                @ParameterLayout(named = "Namespaces",
                        describedAs="Subset of the complete meta model, only including namespaces starting with given prefix")
                final List<String> namespaces,

                @ParameterLayout()
                @Parameter(optionality=Optionality.MANDATORY)
                final boolean includeInterfaces,

                @ParameterLayout(named="Metamodel (Zipped XML)",
                        describedAs="Metamodel from a previous export, to compare the current with.")
                @Parameter(fileAccept=".zip", optionality = Optionality.MANDATORY) final
                        Blob zippedMetamodelBlob

        ) throws IOException {

            val config = defaultConfig(includeInterfaces, namespaces);

            final MetamodelDto leftMetamodelDto =  metaModelService.exportMetaModel(config);

            final String xml = zippedMetamodelBlob
                    .unZip(CommonMimeType.XML)
                    .toClob(UTF_8)
                    .getChars()
                    .toString();

            final MetamodelDto rightMetamodelDto =  jaxbService.fromXml(MetamodelDto.class, xml);

            final StringBuilder diff = _DiffExport.toDiff(leftMetamodelDto, rightMetamodelDto);

            return Clob.of(fileName, CommonMimeType.TXT, diff)
                    .toBlob(UTF_8)
                    .zip();

            // ...
        }

        @MemberSupport public String validateAct(
                final String fileName,
                final List<String> namespacePrefixes,
                final boolean includeInterfaces,
                final Blob rightMetamodelBlob) {
            if(namespacePrefixes == null || namespacePrefixes.isEmpty()) {
                return "At least one package must be selected";
            }
            return null;
        }

        @MemberSupport public String default0Act() { return "metamodel-diff.txt"; }
        @MemberSupport public List<String> choices1Act() { return namespaceChoices(); }
        @MemberSupport public boolean default2Act() { return false; }

    }


    // -- HELPER

    private Config defaultConfig(
            final boolean includeInterfaces,
            final List<String> namespaces) {
        var config = Config.builder()
                .ignoreFallbackFacets(true)
                .ignoreAbstractClasses(true)
                .ignoreBuiltInValueTypes(true)
                .ignoreInterfaces(!includeInterfaces)
                .build();
        for (final String namespace : namespaces) {
            config = config.withNamespacePrefix(namespace);
        }
        return config;
    }

    private List<String> namespaceChoices() {
        final DomainModel domainModel = metaModelService.getDomainModel();
        final List<DomainMember> export = domainModel.getDomainMembers();
        final SortedSet<String> namespaces = _Sets.newTreeSet();
        for (final DomainMember domainMember : export) {
            final String namespace = domainMember.getNamespace();
            final String[] split = namespace.split("[.]");
            final StringBuilder buf = new StringBuilder();
            for (final String part : split) {
                if(buf.length() > 0) {
                    buf.append(".");
                }
                buf.append(part);
                namespaces.add(buf.toString());
            }
        }
        return new ArrayList<>(namespaces);
    }

    // ...

    @Inject MetaModelService metaModelService;
    @Inject JaxbService jaxbService;

}
