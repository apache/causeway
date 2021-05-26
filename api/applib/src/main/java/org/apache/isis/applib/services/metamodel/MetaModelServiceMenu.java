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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
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

    final MimeType mimeTypeTextCsv;
    final MimeType mimeTypeTextXml;

    public MetaModelServiceMenu() {
        try {
            mimeTypeTextCsv = new MimeType("text", "csv");
            mimeTypeTextXml = new MimeType("application", "xml");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

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

        final DomainModel domainMembers =  metaModelService.getDomainModel();
        final List<String> list = asList(domainMembers);
        final StringBuilder buf = asBuf(list);

        return new Clob(
                withSuffix(csvFileName, "csv"),
                mimeTypeTextCsv, buf.toString().toCharArray());

        // ...
    }


    public String default0DownloadMetaModelCsv() {
        return "metamodel.csv";
    }

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
            @ParameterLayout(named = "Packages",
            describedAs="Subset of the complete meta model, only including packages starting with given prefix.")
            final List<String> packages,
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
        for (final String pkg : packages) {
            config = config.withPackagePrefix(pkg);
        }
        if(ignoreInterfaces) {
            config = config.withIgnoreInterfaces();
        }


        final MetamodelDto metamodelDto =  metaModelService.exportMetaModel(config);

        final String xml = jaxbService.toXml(metamodelDto);
        return new Clob(_Strings.asFileNameWithExtension(fileName,  ".xml"), "text/xml", xml);

        // ...
    }

    public String validateDownloadMetaModelXml(
            final String fileName, final List<String> packagePrefixes, final boolean ignoreInterfaces) {
        if(packagePrefixes == null || packagePrefixes.isEmpty()) {
            return "At least one package must be selected";
        }
        return null;
    }

    public String default0DownloadMetaModelXml() {
        return "metamodel.xml";
    }

    public List<String> choices1DownloadMetaModelXml() {
        final DomainModel domainModel = metaModelService.getDomainModel();
        final List<DomainMember> export = domainModel.getDomainMembers();
        final SortedSet<String> packages = _Sets.newTreeSet();
        for (final DomainMember domainMember : export) {
            final String packageName = domainMember.getPackageName();
            final String[] split = packageName.split("[.]");
            final StringBuilder buf = new StringBuilder();
            for (final String part : split) {
                if(buf.length() > 0) {
                    buf.append(".");
                }
                buf.append(part);
                packages.add(buf.toString());
            }
        }
        return new ArrayList<>(packages);
    }

    public boolean default2DownloadMetaModelXml() {
        return true;
    }

    private static StringBuilder asBuf(final List<String> list) {
        final StringBuilder buf = new StringBuilder();
        for (final String row : list) {
            buf.append(row).append("\n");
        }
        return buf;
    }

    private static List<String> asList(final DomainModel model) {
        final List<String> list = _Lists.newArrayList();
        list.add(header());
        for (final DomainMember row : model.getDomainMembers()) {
            list.add(asTextCsv(row));
        }
        return list;
    }

    private static String header() {
        return "classType,packageName,className,memberType,memberName,numParams,mixedIn?,mixin,hidden,disabled,choices,autoComplete,default,validate";
    }

    private static String asTextCsv(final DomainMember row) {
        return Stream.of(
                row.getClassType(),
                row.getPackageName(),
                row.getClassName(),
                row.getType(),
                row.getMemberName(),
                row.getNumParams(),
                row.isMixedIn() ? "Y" : "",
                row.getMixin(),
                row.getHidden(),
                row.getDisabled(),
                row.getChoices(),
                row.getAutoComplete(),
                row.getDefault(),
                row.getValidate())
                .collect(Collectors.joining(","));
    }


    private static String withSuffix(String fileName, String suffix) {
        if(!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }
        if(!fileName.endsWith(suffix)) {
            fileName += suffix;
        }
        return fileName;
    }


    // ...

    @Inject
    MetaModelService metaModelService;
    @Inject
    JaxbService jaxbService;

}
