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
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
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
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.schema.metamodel.v2.Collection;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.FacetAttr;
import org.apache.isis.schema.metamodel.v2.FacetHolder.Facets;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;
import org.apache.isis.schema.metamodel.v2.Property;

import lombok.val;

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

        final DomainModel domainMembers =  metaModelService.getDomainModel();
        final List<String> list = asList(domainMembers);
        final StringBuilder buf = asBuf(list);

        return Clob.of(csvFileName, CommonMimeType.CSV, buf.toString());

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

            @ParameterLayout(named = "Packages",
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
            final String fileName, final List<String> packagePrefixes, final boolean ignoreInterfaces) {
        if(packagePrefixes == null || packagePrefixes.isEmpty()) {
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

            @ParameterLayout(named = "Packages",
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

        final StringBuffer ascii = toAscii(metamodelDto);

        return Clob.of(fileName, CommonMimeType.TXT, ascii);

        // ...
    }

    @MemberSupport
    public String validateDownloadMetaModelAscii(
            final String fileName, final List<String> packagePrefixes, final boolean ignoreInterfaces) {
        if(packagePrefixes == null || packagePrefixes.isEmpty()) {
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

    @MemberSupport
    public boolean default2DownloadMetaModelAscii() {
        return true;
    }

    // -- HELPER

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
        return "classType,logicalTypeName,className,packageName,memberType,memberName,numParams,mixedIn?,mixin,hidden,disabled,choices,autoComplete,default,validate";
    }

    private static String asTextCsv(final DomainMember row) {
        return Stream.of(
                row.getClassType(),
                row.getLogicalTypeName(),
                row.getClassName(),
                row.getPackageName(),
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

    private StringBuffer toAscii(MetamodelDto metamodelDto) {
        val sb = new StringBuffer();

        metamodelDto
            .getDomainClassDto()
            .forEach(typeDto->toAscii(typeDto, sb));

        return sb;
    }

    private void toAscii(DomainClassDto typeDto, StringBuffer sb) {

        sb.append(typeDto.getId()).append("\n");

        toAscii("  * ", typeDto.getFacets(), sb);

        Optional.ofNullable(typeDto.getProperties())
        .map(props->props.getProp())
        .ifPresent(list->list.forEach(propDto->toAscii(propDto, sb)));

        Optional.ofNullable(typeDto.getCollections())
        .map(colls->colls.getColl())
        .ifPresent(list->list.forEach(collDto->toAscii(collDto, sb)));

        Optional.ofNullable(typeDto.getActions())
        .map(acts->acts.getAct())
        .ifPresent(list->list.forEach(actDto->toAscii(actDto, sb)));

    }

    private void toAscii(final String prefix, @Nullable Facets facets, final StringBuffer sb) {

        val attrPrefix = _Strings.of(prefix.length()-2, ' ') + "  | ";

        Optional.ofNullable(facets)
        .map(fac->fac.getFacet())
        .ifPresent(list->list.forEach(facetDto->{
            sb
                .append(prefix)
                .append(shorten(facetDto.getId()))
                .append("\n");

            Optional.ofNullable(facetDto.getAttr())
            .ifPresent(attrs->attrs.forEach(attr->{
                toAscii(attrPrefix, attr, sb);
            }));

        }));
    }

    private void toAscii(String attrPrefix, FacetAttr attr, StringBuffer sb) {
        sb.append(attrPrefix).append(attr.getName()).append('=').append(attr.getValue()).append("\n");
    }

    private void toAscii(Property propDto, StringBuffer sb) {
        sb.append("+-- prop ").append(propDto.getId()).append("\n");
        toAscii  ("      * ", propDto.getFacets(), sb);
    }

    private void toAscii(Collection collDto, StringBuffer sb) {
        sb.append("+-- coll ").append(collDto.getId()).append("\n");
        toAscii  ("      * ", collDto.getFacets(), sb);
    }

    private void toAscii(org.apache.isis.schema.metamodel.v2.Action actDto, StringBuffer sb) {
        sb.append("+-- act  ").append(actDto.getId()).append("\n");
        toAscii  ("      * ", actDto.getFacets(), sb);
    }

    private String shorten(String id) {
        return id.replace("org.apache.isis.core.metamodel.facets", "..facets");
    }

    // ...

    @Inject MetaModelService metaModelService;
    @Inject JaxbService jaxbService;

}
