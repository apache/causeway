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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.Perform;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformAbstract;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public abstract class PerformCheckThatAbstract extends PerformAbstract {

    protected static enum OnMemberColumn {
        REQUIRED, NOT_REQUIRED
    }

    private final Map<String, ThatSubcommand> subcommandByKey = new HashMap<String, ThatSubcommand>();
    private ObjectAdapter result;
    private final boolean requiresMember;

    public PerformCheckThatAbstract(final String key, final OnMemberColumn onMemberColumn, final Perform.Mode mode, final ThatSubcommand... thatSubcommands) {
        super(key, mode);
        requiresMember = onMemberColumn == OnMemberColumn.REQUIRED;
        for (final ThatSubcommand thatSubcommand : thatSubcommands) {
            for (final String subKey : thatSubcommand.getSubkeys()) {
                subcommandByKey.put(subKey, thatSubcommand);
            }
        }
    }

    @Override
    public void perform(final PerformContext performContext) throws ScenarioBoundValueException {
        final CellBinding thatItBinding = performContext.getPeer().getThatItBinding();
        if (!thatItBinding.isFound()) {
            final CellBinding performBinding = performContext.getPeer().getPerformBinding();
            throw ScenarioBoundValueException.current(performBinding, "(require " + IsisViewerConstants.THAT_IT_NAME + "' column)");
        }
        final ScenarioCell thatItCell = thatItBinding.getCurrentCell();
        final String thatIt = thatItCell.getText();
        final ThatSubcommand thatSubcommand = subcommandByKey.get(thatIt);
        if (thatSubcommand == null) {
            throw ScenarioBoundValueException.current(thatItBinding, "(unknown '" + IsisViewerConstants.THAT_IT_NAME + "' verb)");
        }
        result = thatSubcommand.that(performContext);
    }

    @Override
    public ObjectAdapter getResult() {
        return result;
    }

    @Override
    public boolean requiresMember() {
        return requiresMember;
    }
}
