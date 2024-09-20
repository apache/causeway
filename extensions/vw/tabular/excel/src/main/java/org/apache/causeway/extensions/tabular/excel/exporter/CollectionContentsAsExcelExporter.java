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
package org.apache.causeway.extensions.tabular.excel.exporter;

import java.io.File;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.metamodel.tabular.simple.CollectionContentsExporter;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

@Component
public class CollectionContentsAsExcelExporter
implements CollectionContentsExporter {

    @Override
    public void createExport(final DataTable dataTable, final File tempFile,
            final @Nullable CollectionContentsExporter.AccessMode accessMode) {
        ExcelExporter.of(accessMode!=null
                ? accessMode
                : CollectionContentsExporter.AccessMode.USER)
            .accept(dataTable, tempFile);
    }

    @Override
    public CommonMimeType getMimeType() {
        return CommonMimeType.XLSX;
    }

    @Override
    public String getTitleLabel() {
        return "Excel Download";
    }

    @Override
    public String getCssClass() {
        return "fa-solid fa-file-excel";
    }

    @Override
    public int orderOfAppearanceInUiDropdown() {
        return 2500;
    }

}
