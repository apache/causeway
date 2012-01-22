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
package org.apache.isis.viewer.bdd.common;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class ScenarioBoundValueException extends Exception {

    private static final long serialVersionUID = 1L;

    private final CellBinding cellBinding;
    private final ScenarioCell storyCell;

    public static ScenarioBoundValueException current(final CellBinding cellBinding, final String message) {
        return new ScenarioBoundValueException(cellBinding, cellBinding.getCurrentCell(), message);
    }

    public static ScenarioBoundValueException arg(final CellBinding cellBinding, final ScenarioCell storyCell, final String message) {
        return new ScenarioBoundValueException(cellBinding, storyCell, message);
    }

    private ScenarioBoundValueException(final CellBinding cellBinding, final ScenarioCell storyCell, final String message) {
        super(message);
        this.cellBinding = cellBinding;
        this.storyCell = storyCell;
    }

    public CellBinding getCellBinding() {
        return cellBinding;
    }

    public ScenarioCell getStoryCell() {
        return storyCell;
    }

    public String asString() {
        final CharArrayWriter caw = new CharArrayWriter();
        this.printStackTrace(new PrintWriter(caw));
        return caw.toString();
    }

}
