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
package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCellDefault;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasItemsInListPeer;

public class AliasItemsInListForConcordion extends AbstractFixture<AliasItemsInListPeer> {

    public AliasItemsInListForConcordion(final AliasRegistry aliasRegistry, final String listAlias) {
        this(aliasRegistry, listAlias, CellBindingDefault.builder(IsisViewerConstants.TITLE_NAME, IsisViewerConstants.TITLE_HEAD).build(), CellBindingDefault.builder(IsisViewerConstants.TYPE_NAME, IsisViewerConstants.TYPE_HEAD).optional().build(), CellBindingDefault
                .builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate().build());
    }

    private AliasItemsInListForConcordion(final AliasRegistry aliasRegistry, final String listAlias, final CellBinding titleBinding, final CellBinding typeBinding, final CellBinding aliasBinding) {
        super(new AliasItemsInListPeer(aliasRegistry, listAlias, titleBinding, typeBinding, aliasBinding));
    }

    public String execute(final String aliasAs, final String title, final String type) {
        final String header = executeHeader(aliasAs, title, type);
        if (header != null) {
            return header;
        }

        final String row = executeRow(aliasAs, title, type);
        if (row != null) {
            return row;
        }

        return "ok"; // ok
    }

    private String executeHeader(final String alias, final String title, final String type) {
        try {
            getPeer().assertIsList();
        } catch (final ScenarioValueException e) {
            return e.getMessage();
        }

        // create bindings
        getPeer().getTitleBinding().setHeadColumn(0);
        getPeer().getAliasBinding().setHeadColumn(1);

        if (type != null) {
            getPeer().getTypeBinding().setHeadColumn(2, new ScenarioCellDefault(type));
        }

        return null;
    }

    private String executeRow(final String aliasAs, final String title, final String type) {
        if (!getPeer().isList()) {
            return null; // skip
        }

        captureCurrent(aliasAs, title, type);

        try {
            getPeer().findAndAlias();
        } catch (final ScenarioBoundValueException e) {
            return e.getMessage();
        }

        return null;
    }

    private void captureCurrent(final String aliasAs, final String title, final String type) {
        getPeer().getAliasBinding().captureCurrent(new ScenarioCellDefault(aliasAs));
        getPeer().getTitleBinding().captureCurrent(new ScenarioCellDefault(title));
        if (type != null) {
            getPeer().getTitleBinding().captureCurrent(new ScenarioCellDefault(type));
        }
    }

}
