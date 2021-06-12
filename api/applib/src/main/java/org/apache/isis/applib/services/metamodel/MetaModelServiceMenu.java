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
package org.apache.isis.applib.services.metamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;

/**
 *
 * Provides a UI to allow domain model metadata (obtained from
 * {@link MetaModelService} to be downloaded within the UI.
 *
 * @since 2.0 {@index}
 */
@Named(MetaModelServiceMenu.LOGICAL_TYPE_NAME)
@DomainService(logicalTypeName = MetaModelServiceMenu.LOGICAL_TYPE_NAME)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class MetaModelServiceMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".MetaModelServiceMenu";

    public static abstract class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<MetaModelServiceMenu> { }

    // -- CSV

    public static class DownloadMetaModelEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = DownloadMetaModelEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model (CSV)",
            sequence="500.500.2")
    public Clob downloadMetaModelCsv(
            @ParameterLayout(named = ".csv file name")
            final String csvFileName) {

        final DomainModel domainModel =  metaModelService.getDomainModel();
        final StringBuilder csv = _CsvExport.toCsv(domainModel);

        return Clob.of(csvFileName, CommonMimeType.CSV, csv);

        // ...
    }

    @MemberSupport
    public String default0DownloadMetaModelCsv() {
        return "metamodel.csv";
    }

    // -- XML

    public static class DownloadMetaModelXmlEvent extends ActionDomainEvent { }
    @Action(
            domainEvent = DownloadMetaModelXmlEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model (XML)",
            sequence="500.500.2")
    public Clob downloadMetaModelXml(
            @ParameterLayout(named = ".xml file name")
            final String fileName,

            @ParameterLayout(named = "Namespaces",
            describedAs="Subset of the complete meta model, only including namespaces starting with given prefix")
            final List<String> namespaces,

            @ParameterLayout(named = "Ignore Interfaces")
            @Parameter(optionality=Optionality.MANDATORY)
            final boolean ignoreInterfaces
            ) {

        Config config =
                new Config()
                .withIgnoreNoop()
                .withIgnoreAbstractClasses()
                .withIgnoreInterfaces()
                .withIgnoreBuiltInValueTypes();
        for (final String namespace : namespaces) {
            config = config.withNamespacePrefix(namespace);
        }
        if(ignoreInterfaces) {
            config = config.withIgnoreInterfaces();
        }

        final MetamodelDto metamodelDto =  metaModelService.exportMetaModel(config);

        final String xml = jaxbService.toXml(metamodelDto);

        return Clob.of(fileName, CommonMimeType.XML, xml);

        // ...
    }

    @MemberSupport
    public String validateDownloadMetaModelXml(
            final String fileName, final List<String> namespacePrefixes, final boolean ignoreInterfaces) {
        if(namespacePrefixes == null || namespacePrefixes.isEmpty()) {
            return "At least one package must be selected";
        }
        return null;
    }

    @MemberSupport
    public String default0DownloadMetaModelXml() {
        return "metamodel.xml";
    }

    @MemberSupport
    public List<String> choices1DownloadMetaModelXml() {
        return namespaceChoices();
    }

    @MemberSupport
    public boolean default2DownloadMetaModelXml() {
        return true;
    }

    // -- ASCII

    public static class DownloadMetaModelAsciiEvent extends ActionDomainEvent { }
    @Action(
            domainEvent = DownloadMetaModelAsciiEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model (Ascii)",
            sequence="500.500.2")
    public Clob downloadMetaModelAscii(
            @ParameterLayout(named = ".txt file name")
            final String fileName,

            @ParameterLayout(named = "Namespaces",
            describedAs="Subset of the complete meta model, only including namespaces starting with given prefix")
            final List<String> namespaces,

            @ParameterLayout(named = "Ignore Interfaces")
            @Parameter(optionality=Optionality.MANDATORY)
            final boolean ignoreInterfaces
            ) {

        Config config =
                new Config()
                .withIgnoreNoop()
                .withIgnoreAbstractClasses()
                .withIgnoreInterfaces()
                .withIgnoreBuiltInValueTypes();
        for (final String namespace : namespaces) {
            config = config.withNamespacePrefix(namespace);
        }
        if(ignoreInterfaces) {
            config = config.withIgnoreInterfaces();
        }

        final MetamodelDto metamodelDto =  metaModelService.exportMetaModel(config);

        final StringBuilder ascii = _AsciiExport.toAscii(metamodelDto);

        return Clob.of(fileName, CommonMimeType.TXT, ascii);

        // ...
    }

    @MemberSupport
    public String validateDownloadMetaModelAscii(
            final String fileName, final List<String> namespacePrefixes, final boolean ignoreInterfaces) {
        if(namespacePrefixes == null || namespacePrefixes.isEmpty()) {
            return "At least one package must be selected";
        }
        return null;
    }

    @MemberSupport
    public String default0DownloadMetaModelAscii() {
        return "metamodel.txt";
    }

    @MemberSupport
    public List<String> choices1DownloadMetaModelAscii() {
        return namespaceChoices();
    }

    @MemberSupport
    public boolean default2DownloadMetaModelAscii() {
        return true;
    }

    // -- HELPER

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
