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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.lang.StringUtils;

public abstract class CellBinding {

    private boolean found = false;
    private int column = -1;

    private ScenarioCell headCell = null;
    private ScenarioCell currentCell;

    private final String name;
    private final List<String> headTexts;
    private final boolean autoCreate;
    private final boolean ditto;
    private final boolean optional;

    // ///////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////

    protected CellBinding(final String name, final boolean autoCreate, final boolean ditto, final boolean optional, final String[] headTexts) {
        this.name = name;
        this.autoCreate = autoCreate;
        this.ditto = ditto;
        this.optional = optional;
        if (headTexts.length == 0) {
            throw new IllegalArgumentException("Require at least one heading text");
        }
        final List<String> headTextList = new ArrayList<String>();
        for (final String headText : headTexts) {
            headTextList.add(StringUtils.camel(headText).toLowerCase());
        }
        this.headTexts = headTextList;
    }

    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    private void ditto(final ScenarioCell previousCell) {
        copy(previousCell, getCurrentCell());
    }

    public List<String> getHeadTexts() {
        return headTexts;
    }

    public boolean isDitto() {
        return ditto;
    }

    // ///////////////////////////////////////////////////////////////
    // matches (for searching head column)
    // ///////////////////////////////////////////////////////////////

    /**
     * For the BDD framework integration to search whether this particular
     * {@link CellBinding} corresponds to a particular head text.
     */
    public boolean matches(final String candidateText) {
        final String candidateTextCamelLower = StringUtils.camel(candidateText).toLowerCase();
        for (final String headText : getHeadTexts()) {
            if (headText.equalsIgnoreCase(candidateTextCamelLower)) {
                return true;
            }
        }
        return false;
    }

    // ///////////////////////////////////////////////////////////////
    // set Head Column
    // ///////////////////////////////////////////////////////////////

    /**
     * For the BDD framework integration to indicate the head column, but
     * without no {@link #getHeadCell() head cell}.
     */
    public void setHeadColumn(final int column) {
        this.found = true;
        setColumnAndHeadCell(column, null);
    }

    /**
     * For the BDD framework integration to indicate that the head cell has been
     * found.
     */
    public void setHeadColumn(final int column, final ScenarioCell headCell) {
        this.found = true;
        setColumnAndHeadCell(column, headCell);
    }

    /**
     * For the BDD framework integration to indicate that the head cell was not
     * found and has been created.
     */
    public void createHeadCell(final int column, final ScenarioCell headCell) {
        setColumnAndHeadCell(column, headCell);
    }

    /**
     * Whether the head cell was found.
     */
    public boolean isFound() {
        return found;
    }

    /**
     * The column that is found, if any.
     */
    public int getColumn() {
        return column;
    }

    /**
     * The head cell with the text, if any.
     */
    public ScenarioCell getHeadCell() {
        return headCell;
    }

    private void setColumnAndHeadCell(final int column, final ScenarioCell headCell) {
        this.column = column;
        this.headCell = headCell;
    }

    /**
     * Holds onto a current (body) cell.
     */
    public ScenarioCell getCurrentCell() {
        return currentCell;
    }

    /**
     * @see #getCurrentCell()
     */
    public void setCurrentCell(final ScenarioCell cell) {
        this.currentCell = cell;
    }

    // ///////////////////////////////////////////////////////////////
    // capture current value
    // ///////////////////////////////////////////////////////////////

    private boolean dittoed;

    /**
     * Captures the current value, but also checking that the column in which
     * the {@link ScenarioCell value} has been provided corresponds to the
     * {@link #getColumn() column} of this binding.
     * 
     * @see #captureCurrent(ScenarioCell)
     */
    public void captureCurrent(final ScenarioCell cell, final int column) {
        if (column != getColumn()) {
            return;
        }
        captureCurrent(cell);
    }

    /**
     * Captures the current {@link ScenarioCell value} for this binding.
     * 
     * <p>
     * For implementations where we already know that the value provided is for
     * this particular binding.
     */
    public void captureCurrent(final ScenarioCell cell) {
        final ScenarioCell previousCell = getCurrentCell();
        setCurrentCell(cell);
        final boolean shouldDitto = StringUtils.isNullOrEmpty(cell.getText());
        final boolean canDitto = isDitto() && previousCell != null;
        if (shouldDitto && canDitto) {
            ditto(previousCell);
            dittoed = true;
        } else {
            dittoed = false;
        }
    }

    /**
     * Whether the most recent call to {@link #captureCurrent(ScenarioCell)}
     * resulted in a ditto.
     */
    public boolean isDittoed() {
        return dittoed;
    }

    protected abstract void copy(final ScenarioCell from, ScenarioCell to);

    // ///////////////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return found ? ("found, current=" + getCurrentCell().getText()) : "not found";
    }

}
