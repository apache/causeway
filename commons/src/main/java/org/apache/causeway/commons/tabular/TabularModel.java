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
package org.apache.causeway.commons.tabular;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * General purpose tabular data structure,
 * that maps well onto excel files,
 * but can also be used for other tabular file formats.
 *
 * @since 3.2
 */
public record TabularModel(
        Can<TabularSheet> sheets) {

    public TabularModel(final TabularSheet sheet) {
        this(Can.of(sheet));
    }

    public record TabularSheet(
            String sheetName,
            Can<TabularColumn> columns,
            Can<TabularRow> rows) {
    }

    public record TabularColumn(
            int columnIndex,
            String columnName,
            String columnDescription) {
    }

    /**
     * A cell can have no value {@code cardinality=0},
     * one value {@code cardinality=1},
     * or multiple values {@code cardinality>1}.
     * <p>
     * For the plural case, no pojo is provided.
     * Instead a {@link Stream} of literals (labels) is provided.
     */
    public record TabularCell(
            int cardinality,
            /**
             * When cardinality is 0 then must be Either.right,
             * otherwise no strict policy is enforced.<br>
             * E.g. a TabularCell can decide to provide a label instead of a pojo,
             * even though cardinality is 1.
             */
            @NonNull Either<Object, Supplier<Stream<String>>> eitherValueOrLabelSupplier) {

        // -- FACTORIES

        private static TabularCell EMPTY = new TabularCell(0, Either.right(null));
        public static TabularCell empty() { return EMPTY; }

        public static TabularCell single(@Nullable final Object value) {
            return value==null
                    ? EMPTY
                    : new TabularCell(1, Either.left(value));
        }
        public static TabularCell labeled(final int cardinality, @NonNull final Supplier<Stream<String>> labelSupplier) {
            Objects.requireNonNull(labelSupplier);
            return new TabularCell(cardinality, Either.right(labelSupplier));
        }

        // -- CANONICAL CONSTRUCTOR

        public TabularCell(
                final int cardinality,
                @NonNull final Either<Object, Supplier<Stream<String>>> eitherValueOrLabelSupplier) {
            Objects.requireNonNull(eitherValueOrLabelSupplier);
            if(cardinality<0) throw _Exceptions.illegalArgument("cardinality cannot be negative: %d", cardinality);
            if(cardinality==0) {
                _Assert.assertTrue(eitherValueOrLabelSupplier.isRight(), ()->
                        "cannot provide a value when cardinality is zero");
            }
            this.cardinality = cardinality;
            this.eitherValueOrLabelSupplier = eitherValueOrLabelSupplier;
        }

        // -- LABELS

        public Stream<String> labels() {
            return eitherValueOrLabelSupplier.fold(
                    left->Stream.<String>empty(),
                    right->right.get());
        }
    }

    public record TabularRow(
            Can<TabularCell> cells) {

        public TabularCell getCell(final TabularColumn column) {
            return getCell(column.columnIndex());
        }

        public TabularCell getCell(final int columnIndex) {
            return cells.getElseFail(columnIndex);
        }
    }

}
