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
package org.apache.causeway.viewer.commons.applib.tabular.export;

import java.io.File;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

/**
 * SPI to provide file export to table views.
 *
 * @since 2.0 {@index}}
 */
public interface CollectionContentsExporter {

    File createExportFile(DataTable dataTable);

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
     * Whether activation of this table presentation view should result in a full page reload.
     */
    default boolean isPageReloadRequiredOnTableViewActivation() { return false; }
}
