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

import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "isisApplib.MetaModelServicesMenu"
)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "500.500"
)
public class MetaModelServicesMenu {

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<MetaModelServicesMenu> {
    }

    private final MimeType mimeTypeTextCsv;

    public MetaModelServicesMenu() {
        try {
            mimeTypeTextCsv = new MimeType("text", "csv");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    // //////////////////////////////////////

    public static class DownloadMetaModelEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = DownloadMetaModelEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Meta Model (CSV)"
    )
    @MemberOrder(sequence="500.500.1")
    public Clob downloadMetaModel(
            @ParameterLayout(named = ".csv file name")
            final String csvFileName) {

        final List<DomainMember> rows =  metaModelService.export();
        final List<String> list = asList(rows);
        final StringBuilder buf = asBuf(list);

        return new Clob(
                Util.withSuffix(csvFileName, "csv"),
                mimeTypeTextCsv, buf.toString().toCharArray());
    }

    public String default0DownloadMetaModel() {
        return "metamodel.csv";
    }

    // //////////////////////////////////////

    private static StringBuilder asBuf(final List<String> list) {
        final StringBuilder buf = new StringBuilder();
        for (final String row : list) {
            buf.append(row).append("\n");
        }
        return buf;
    }

    private static List<String> asList(final List<DomainMember> rows) {
        final List<String> list = Lists.newArrayList();
        list.add(header());
        for (final DomainMember row : rows) {
            list.add(asTextCsv(row));
        }
        return list;
    }



    private static String header() {
        return "classType,packageName,className,memberType,memberName,numParams,contributed?,contributedBy,mixedIn?,mixin,hidden,disabled,choices,autoComplete,default,validate";
    }

    private static String asTextCsv(final DomainMember row) {
        return Joiner.on(",").join(
                row.getClassType(),
                row.getPackageName(),
                row.getClassName(),
                row.getType(),
                row.getMemberName(),
                row.getNumParams(),
                row.isContributed() ? "Y" : "",
                row.getContributedBy(),
                row.isMixedIn() ? "Y" : "",
                row.getMixin(),
                row.getHidden(),
                row.getDisabled(),
                row.getChoices(),
                row.getAutoComplete(),
                row.getDefault(),
                row.getValidate());
    }


    @javax.inject.Inject
    MetaModelService metaModelService;

}
