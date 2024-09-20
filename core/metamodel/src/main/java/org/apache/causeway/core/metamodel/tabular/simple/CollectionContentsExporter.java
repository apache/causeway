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
package org.apache.causeway.core.metamodel.tabular.simple;

import java.io.File;
import java.nio.file.Files;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.SneakyThrows;

/**
 * SPI to provide file export to table views.
 *
 * @since 2.0 {@index}
 */
public interface CollectionContentsExporter {

    public enum AccessMode {
        /**
         * must be authorized, with transactions, with publishing, with domain events
         */
        USER,
        /**
         * always authorized, no transactions, no publishing, no domain events;
         */
        PASS_THROUGH;
        /**
         * @see #USER
         */
        public boolean isUser() { return this==USER; }
        /**
         * @see #PASS_THROUGH
         */
        public boolean isPassThrough() { return this==PASS_THROUGH; }
    }

    /**
     * Implementing exporters need to write given tabular data from
     * {@link DataTable} into the {@link File tempFile},
     * which is provided by the framework for the duration of a single request cycle.
     *
     * @param dataTable data model for the table
     * @param tempFile destination, this exporter writes its data to
     */
    default void createExport(final DataTable dataTable, final File tempFile) {
        createExport(dataTable, tempFile, AccessMode.USER);
    }

    void createExport(DataTable dataTable, File tempFile, @Nullable AccessMode accessMode);

    /**
     * Writes given tabular data into a {@link Blob} of given name.
     *
     */
    default Blob exportToBlob(final DataTable dataTable, final String name) {
        return exportToBlob(dataTable, name, AccessMode.USER);
    }

    @SneakyThrows
    default Blob exportToBlob(final DataTable dataTable, final String name, final @Nullable AccessMode accessMode) {
        var tempFile = File.createTempFile(this.getClass().getCanonicalName(), name);
        try {
            createExport(dataTable, tempFile, accessMode);
            return Blob.of(name, getMimeType(), DataSource.ofFile(tempFile).bytes());
        } finally {
            Files.deleteIfExists(tempFile.toPath()); // cleanup
        }
    }

    CommonMimeType getMimeType();

    /**
     * @return label for the "View as" dropdown for "collection contents as"
     * component factories
     */
    String getTitleLabel();

    /**
     * @return CSS class for the icon/image next to "View as" dropdown
     * for "collection contents as" component factories
     */
    String getCssClass();

    /**
     * An ordinal, that governs the order of appearance in the UI dropdown.
     * <ul>
     * <li>{@literal 1000..1999} reserved for different table presentations</li>
     * <li>{@literal 2000..2999} reserved for different table exports</li>
     * </ul>
     * <p>
     * Lowest comes first.
     */
    int orderOfAppearanceInUiDropdown();

    /**
     * Whether this exporter applies to given {@code objectType}.
     * If <code>false</code>, this exporter is not provided to the end user.
     */
    default boolean appliesTo(final ObjectSpecification objectType) { return true; }

}
