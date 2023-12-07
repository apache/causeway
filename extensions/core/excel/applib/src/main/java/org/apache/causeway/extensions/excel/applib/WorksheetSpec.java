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
package org.apache.causeway.extensions.excel.applib;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.internal.base._Casts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * @since 2.0 {@index}
 */
public class WorksheetSpec {

    /**
     * Maximum supported by Microsoft Excel UI.
     *
     * @see <a href="http://stackoverflow.com/questions/3681868/is-there-a-limit-on-an-excel-worksheets-name-length">stackoverflow.com</a>
     */
    private static final int SHEET_NAME_MAX_LEN = 31;
    private static final String ROW_HANDLER_SUFFIX = "RowHandler";

    public interface RowFactory<Q> {
        @Programmatic
        Q create();

        @Programmatic
        Class<?> getCls();

        @RequiredArgsConstructor
        class Default<T> implements RowFactory<T> {

            @Getter
            private final Class<T> cls;

            @Override @SneakyThrows
            public T create() {
                final T t = cls.getConstructor().newInstance();
                return servicesInjector.injectServicesInto(t);
            }

            @Inject @Setter
            private ServiceInjector servicesInjector;
        }

    }

    private final RowFactory<?> factory;
    private final Mode mode;
    private final String sheetName;

    /**
     * @param viewModelClass
     * @param sheetName - must be 31 chars or less
     * @param <T>
     */
    public <T> WorksheetSpec(final Class<T> viewModelClass, final String sheetName) {
        this(viewModelClass, sheetName, Mode.STRICT);
    }

    public <T> WorksheetSpec(final Class<T> viewModelClass, final String sheetName, final Mode mode) {
        this(new RowFactory.Default<>(viewModelClass), sheetName, mode);
    }

    public <T> WorksheetSpec(final RowFactory<T> factory, String sheetName, final Mode mode) {
        this.factory = factory;
        this.mode = mode;
        if(sheetName == null) {
            throw new IllegalArgumentException("Sheet name must be specified");
        }
        if(isTooLong(sheetName) && hasSuffix(sheetName)) {
            sheetName = prefix(sheetName);
        }
        if(isTooLong(sheetName)) {
            throw new IllegalArgumentException(
                    String.format("Sheet name must be less than 30 characters (was '%s'", sheetName));
        }
        this.sheetName = sheetName;
    }

    public static String prefix(final String sheetName) {
        return sheetName.substring(0, sheetName.lastIndexOf(ROW_HANDLER_SUFFIX));
    }

    @Programmatic
    public <T> RowFactory<T> getFactory() { return _Casts.uncheckedCast(factory); }

    @Programmatic
    public String getSheetName() {
        return sheetName;
    }

    @Programmatic
    public Mode getMode() {
        return mode;
    }

    public static boolean isTooLong(final String sheetName) {
        return sheetName.length() > SHEET_NAME_MAX_LEN;
    }

    public static String trim(final String sheetName) {
        return sheetName.substring(0, SHEET_NAME_MAX_LEN);
    }

    public static boolean hasSuffix(final String sheetName) {
        return sheetName.endsWith(ROW_HANDLER_SUFFIX);
    }

    public interface Matcher {
        /**
         * @return non-null to indicate how the sheet should be handled, otherwise <code>null</code> to ignore
         */
        @Programmatic
        WorksheetSpec fromSheet(String sheetName);
    }

    public interface Sequencer {
        @Programmatic
        List<WorksheetSpec> sequence(List<WorksheetSpec> specs);
    }

}
