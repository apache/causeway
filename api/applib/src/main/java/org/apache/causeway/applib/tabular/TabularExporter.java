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
package org.apache.causeway.applib.tabular;

import java.io.File;
import java.nio.file.Files;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.tabular.TabularModel;

import lombok.SneakyThrows;

/**
 * SPI to provide file export to table views.
 *
 * @since 3.2 {@index}
 */
public interface TabularExporter {

    /**
     * Implementing exporters need to write given tabular data from
     * {@link org.apache.causeway.commons.tabular.TabularModel.TabularSheet} into the {@link File exportFile},
     * which is provided by the framework for the duration of a single request cycle.
     *
     * @param dataTable data model for the table
     * @param exportFile destination, this exporter writes its data to
     */
    void export(TabularModel.TabularSheet tabularSheet, File exportFile);

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
     * Whether this exporter applies to given {@code elementType}.
     * If <code>false</code>, this exporter is not provided to end users.
     */
    default boolean appliesTo(final Class<?> elementType) { return true; }

    /**
     * Writes given tabular data to a {@link Blob}, using given sheet's name as blob name.
     */
    @SneakyThrows
    default Blob exportToBlob(final TabularModel.TabularSheet tabularSheet) {
        var tempFile = File.createTempFile(this.getClass().getCanonicalName(), tabularSheet.sheetName());
        try {
            export(tabularSheet, tempFile);
            return Blob.of(tabularSheet.sheetName(), getMimeType(), DataSource.ofFile(tempFile).bytes());
        } finally {
            Files.deleteIfExists(tempFile.toPath()); // cleanup
        }
    }

}
