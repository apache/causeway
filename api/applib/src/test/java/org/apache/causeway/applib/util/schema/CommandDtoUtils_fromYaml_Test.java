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
package org.apache.causeway.applib.util.schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.util.StreamUtils;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.schema.cmd.v2.ActionDto;
import org.apache.causeway.schema.cmd.v2.CommandDto;
import org.apache.causeway.schema.cmd.v2.ParamDto;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.common.v2.ValueType;

public class CommandDtoUtils_fromYaml_Test {

    @Test
    public void scalarValues() throws IOException {
        var commandDtos = loadCommands("commands-with-scalar-params.yaml");

        assertScalarCommands(commandDtos);
    }

    @Test
    public void scalarValuesAsMultiDocument() throws IOException {
        var commandDtos = loadCommands("commands-with-scalar-params-multi-document.yaml");

        assertScalarCommands(commandDtos);
    }

    private static void assertScalarCommands(final List<CommandDto> commandDtos) {

        Assertions.assertThat(commandDtos).hasSize(2);

        ActionDto firstAction = (ActionDto) commandDtos.get(0).getMember();
        Assertions.assertThat(firstAction.getLogicalMemberIdentifier())
                .isEqualTo("outgoing.invoiceforlease.InvoiceForLease#approve");
        Assertions.assertThat(firstAction.getInteractionType())
                .isEqualTo(InteractionType.ACTION_INVOCATION);
        Assertions.assertThat(commandDtos.get(0).getTargets().getOid())
                .singleElement()
                .satisfies(oid -> {
                    Assertions.assertThat(oid.getType()).isEqualTo("outgoing.invoiceforlease.InvoiceForLease");
                    Assertions.assertThat(oid.getId()).isEqualTo("419264");
                });

        ActionDto secondAction = (ActionDto) commandDtos.get(1).getMember();
        Assertions.assertThat(secondAction.getLogicalMemberIdentifier())
                .isEqualTo("outgoing.invoiceforlease.InvoiceForLease#invoice");
        Assertions.assertThat(secondAction.getInteractionType())
                .isEqualTo(InteractionType.ACTION_INVOCATION);

        List<ParamDto> scalarParams = secondAction.getParameters().getParameter();
        Assertions.assertThat(scalarParams).hasSize(2);

        ParamDto invoiceDate = scalarParams.get(0);
        Assertions.assertThat(invoiceDate.getName()).isEqualTo("Invoice Date");
        Assertions.assertThat(invoiceDate.getType()).isEqualTo(ValueType.LOCAL_DATE);
        Assertions.assertThat(invoiceDate.getLocalDate()).isNotNull();
        Assertions.assertThat(invoiceDate.getLocalDate().toXMLFormat()).isEqualTo("2026-04-20");

        ParamDto allowFuture = scalarParams.get(1);
        Assertions.assertThat(allowFuture.getName()).isEqualTo("Allow Invoice Date In Future");
        Assertions.assertThat(allowFuture.getType()).isEqualTo(ValueType.BOOLEAN);
        Assertions.assertThat(allowFuture.isBoolean()).isFalse();
    }

    @Test
    public void collectionValues() throws IOException {
        var commandDtos = loadCommands("commands-with-collection-param.yaml");

        Assertions.assertThat(commandDtos).hasSize(1);

        ActionDto action = (ActionDto) commandDtos.get(0).getMember();
        Assertions.assertThat(action.getLogicalMemberIdentifier())
                .isEqualTo("outgoing.lease.Lease#calculate");
        Assertions.assertThat(action.getInteractionType())
                .isEqualTo(InteractionType.ACTION_INVOCATION);

        List<ParamDto> params = action.getParameters().getParameter();
        Assertions.assertThat(params).hasSize(7);

        ParamDto leaseItemTypes = params.get(1);
        Assertions.assertThat(leaseItemTypes.getName()).isEqualTo("Lease Item Types");
        Assertions.assertThat(leaseItemTypes.getType()).isEqualTo(ValueType.COLLECTION);
        Assertions.assertThat(leaseItemTypes.isNull()).isFalse();
        Assertions.assertThat(leaseItemTypes.getCollection()).isNotNull();
        Assertions.assertThat(leaseItemTypes.getCollection().getType()).isEqualTo(ValueType.ENUM);

        var items = leaseItemTypes.getCollection().getValue();
        Assertions.assertThat(items).hasSize(13);
        Assertions.assertThat(items).allSatisfy(item -> Assertions.assertThat(item.getEnum()).isNotNull());

        Assertions.assertThat(items.get(0).getEnum().getEnumType())
                .isEqualTo("org.estatio.module.lease.dom.LeaseItemType");
        Assertions.assertThat(items.get(0).getEnum().getEnumName()).isEqualTo("RENT");

        Assertions.assertThat(items.get(6).getEnum().getEnumName()).isEqualTo("SERVICE_CHARGE_DISCOUNT_FIXED");
        Assertions.assertThat(items.get(12).getEnum().getEnumName()).isEqualTo("RETAIL_TAX");

        ParamDto nullableTagName = params.get(5);
        Assertions.assertThat(nullableTagName.getName()).isEqualTo("Tag Name");
        Assertions.assertThat(nullableTagName.getType()).isEqualTo(ValueType.STRING);
        Assertions.assertThat(nullableTagName.isNull()).isTrue();

        ParamDto invoiceDueDate = params.get(2);
        Assertions.assertThat(invoiceDueDate.getName()).isEqualTo("Invoice Due Date");
        Assertions.assertThat(invoiceDueDate.getType()).isEqualTo(ValueType.LOCAL_DATE);
        Assertions.assertThat(invoiceDueDate.getLocalDate().toXMLFormat()).isEqualTo("2026-06-30");

        ParamDto startDueDate = params.get(3);
        Assertions.assertThat(startDueDate.getName()).isEqualTo("Start Due Date");
        Assertions.assertThat(startDueDate.getType()).isEqualTo(ValueType.LOCAL_DATE);
        Assertions.assertThat(startDueDate.getLocalDate().toXMLFormat()).isEqualTo("2026-06-30");

        ParamDto nextDueDate = params.get(4);
        Assertions.assertThat(nextDueDate.getName()).isEqualTo("Next Due Date");
        Assertions.assertThat(nextDueDate.getType()).isEqualTo(ValueType.LOCAL_DATE);
        Assertions.assertThat(nextDueDate.getLocalDate().toXMLFormat()).isEqualTo("2026-07-01");

        ParamDto newTagName = params.get(6);
        Assertions.assertThat(newTagName.getName()).isEqualTo("New Tag Name");
        Assertions.assertThat(newTagName.getType()).isEqualTo(ValueType.STRING);
        Assertions.assertThat(newTagName.getString()).isEqualTo("JDOJPA-T1");
    }

    private List<CommandDto> loadCommands(final String yamlFileName) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(getClass().getSimpleName() + "." + yamlFileName);
        byte[] bytes = StreamUtils.copyToByteArray(resourceAsStream);

        Blob commandsYaml = new Blob(
                yamlFileName,
                NamedWithMimeType.CommonMimeType.YAML.getMimeType(),
                bytes);

        return CommandDtoUtils.fromYaml(commandsYaml.asDataSource());
    }
}