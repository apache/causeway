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
package org.apache.isis.viewer.bdd.concordion.internal.concordion;

import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.Row;
import org.concordion.internal.TableSupport;
import org.concordion.internal.command.ExecuteCommand;

/**
 * Handles tables slightly differently from Concordion's usual strategy.
 * 
 * <p>
 * Specifically, it will call execute for the header row as well as for the body
 * rows. This is required in order to set up the bindings of the column names to
 * the positions.
 */
public class IsisExecuteCommandWithHeader extends ExecuteCommand {

    public static enum Context {
        TABLE, INLINE
    }

    public static enum TableRow {
        HEADER, BODY
    }

    /**
     * Provides a mechanism for the fixture to determine whether the command is
     * being executed in the context of a table or inline.
     */
    public static ThreadLocal<Context> context = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return null;
        }
    };

    /**
     * When executing in a {@link Context#TABLE table context}, provides a
     * mechanism for the fixture to determine whether the header of a table or
     * the body is being processed.
     */
    public static ThreadLocal<TableRow> tableRow = new ThreadLocal<TableRow>() {
        @Override
        protected TableRow initialValue() {
            return TableRow.BODY;
        }
    };

    @Override
    public void execute(final org.concordion.api.CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        if (commandCall.getElement().isNamed("table")) {
            // special handling for tables
            final Context contextIfAny = context.get();
            final boolean setContext = contextIfAny == null;
            if (setContext) {
                context.set(Context.TABLE);
            }
            executeTable(commandCall, evaluator, resultRecorder);
            if (setContext) {
                context.set(null);
            }
        } else {
            // basically the same as Concordion's original ExecuteCommand
            final Context contextIfAny = context.get();
            final boolean setContext = contextIfAny == null;
            if (setContext) {
                context.set(Context.INLINE);
            }
            super.execute(commandCall, evaluator, resultRecorder);
            if (setContext) {
                context.set(null);
            }
        }
    }

    private void executeTable(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        final TableSupport tableSupport = new TableSupport(commandCall);

        // this is the bit that's different: also execute on the header
        tableRow.set(TableRow.HEADER);
        final Row headerRow = tableSupport.getLastHeaderRow();
        commandCall.setElement(headerRow.getElement());
        commandCall.execute(evaluator, resultRecorder);

        // the rest is copied from Concordion's original implementation
        tableRow.set(TableRow.BODY);
        final Row[] detailRows = tableSupport.getDetailRows();
        for (final Row detailRow : detailRows) {
            if (detailRow.getCells().length != tableSupport.getColumnCount()) {
                throw new RuntimeException("The <table> 'execute' command only supports rows with an equal number of columns.");
            }
            commandCall.setElement(detailRow.getElement());
            tableSupport.copyCommandCallsTo(detailRow);
            commandCall.execute(evaluator, resultRecorder);
        }
        tableRow.set(null);
    }

}
