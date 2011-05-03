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

package org.apache.isis.viewer.dnd.view;

import java.util.Vector;

public class UndoStack {

    private final Vector<Command> commands = new Vector<Command>();

    public void add(final Command command) {
        commands.addElement(command);
        command.execute();
    }

    public void undoLastCommand() {
        final Command lastCommand = commands.lastElement();
        lastCommand.undo();
        commands.removeElement(lastCommand);
    }

    public String descriptionOfUndo() {
        final Command lastCommand = commands.lastElement();
        return lastCommand.getDescription();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public String getNameOfUndo() {
        final Command lastCommand = commands.lastElement();
        return lastCommand.getName();
    }
}
