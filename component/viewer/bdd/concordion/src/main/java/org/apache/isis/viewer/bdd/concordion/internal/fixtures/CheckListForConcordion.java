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
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.ScenarioCellDefault;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListConstants;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer;
import org.apache.isis.viewer.bdd.common.fixtures.CheckListPeer.CheckMode;

public class CheckListForConcordion extends AbstractFixture<CheckListPeer> {

    public CheckListForConcordion(final AliasRegistry aliasRegistry, final String listAlias) {
        super(new CheckListPeer(aliasRegistry, listAlias, CheckMode.NOT_EXACT, titleBinding()));
    }

    private static CellBindingDefault titleBinding() {
        return CellBindingDefault.builder(CheckListConstants.TITLE_NAME, CheckListConstants.TITLE_HEAD_SET).ditto().build();
    }

    public String executeHeader(final String title) {
        return setupHeader(title);
    }

    private String setupHeader(final String title) {
        int colNum = 0;
        getPeer().getTitleBinding().setHeadColumn(colNum++);
        return ""; // ok
    }

    public String executeRow(final String title) {

        setupHeader(title);

        // capture current
        getPeer().getTitleBinding().captureCurrent(new ScenarioCellDefault(title));

        // execute
        return checkExists();
    }

    private String checkExists() {
        if (!getPeer().findAndAddObject()) {
            return getTitle() + " not found";
        }
        return "ok";
    }

    private String getTitle() {
        final ScenarioCell currentCell = getPeer().getTitleBinding().getCurrentCell();
        return currentCell != null ? currentCell.getText() : "(no title provided)";
    }

}
