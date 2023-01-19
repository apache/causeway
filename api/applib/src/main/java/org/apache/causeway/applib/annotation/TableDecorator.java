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
package org.apache.causeway.applib.annotation;

public interface TableDecorator {

    public default String cssClass() {
        return null;
    }

    public default String documentReadyJavaScript() {
        return null;
    }

    // -- BUILT-IN IMPLEMENTATIONS

    /**
     * The collection's table representation is NOT decorated.
     */
    public class Default implements TableDecorator {
    }

    /**
     * The collection's table representation is decorated using
     * <a href="https://datatables.net>datatables.net</a>
     * for client-side paging and filtering.
     * <p>
     * Use subclasses for custom options.
     */
    public class DatatablesNet implements TableDecorator {

        @Override
        public String cssClass() {
            return "table-decoration-dn-default";
        }

        /**
         * If specified, then the string is passed verbatim as the initialization options for the
         * <a href="https://datatables.net">https://datatables.net</a> table decoration
         * (as defined by {@link DomainObjectLayout#tableDecorator()} or by
         * {@link CollectionLayout#tableDecorator()}).
         *
         * <p>
         *     For example, a value of "info: false, pagingType: 'numbers'" will result in
         *     datatables.net being initialized using:
         *
         *     <pre>
         *     $(document).ready(function () {
         *       $('table.table-decoration').DataTable({
         *         info: false, pagingType: 'numbers'
         *       });
         *     });
         *     </pre>
         *     thus switching off the info panel and using the simple 'numbers' paging type.
         * </p>
         *
         * @see <a href="https://datatables.net/examples/basic_init/index.html">https://datatables.net/examples/basic_init/index.html</a>
         */
        public String getOpts() {
            return "";
        }

        @Override
        public String documentReadyJavaScript() {
            return
                "$('div." + cssClass() + " table.contents').DataTable({"
                + getOpts()
                + "});";
        }

    }

}
