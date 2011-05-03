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
package org.apache.isis.viewer.bdd.common.fixtures;

import java.util.List;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public class DebugServicesPeer extends AbstractFixturePeer {

    public DebugServicesPeer(final AliasRegistry aliasesRegistry, final CellBinding... cellBindings) {
        super(aliasesRegistry, cellBindings);
    }

    public String debugServices() {
        final DebugString debug = new DebugString();

        final List<Object> services = getServices();

        for (final Object service : services) {
            debug.append(service.getClass().getName());
            debug.append("\n");
        }
        return debug.toString().replaceAll("\n", "<br>");
    }

}
