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
package demoapp.dom.annotDomain._commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.annotDomain.Action.command.ActionCommandJdo;

/**
 * Marker interface for mixins to contribute to.
 */
//tag::class[]
public interface ExposePersistedCommands {

    @Service
    @Order(OrderPrecedence.EARLY)
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<CommandJdo> {

        public TableColumnOrderDefault() { super(CommandJdo.class); }

        @Override
        protected List<String> orderParented(Object parent, String collectionId, List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(List<String> propertyIds) {
            return Arrays.asList(
                    "timestamp", "commandDto", "username", "complete", "resultSummary"
            );
        }
    }

}
//end::class[]
