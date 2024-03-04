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
package org.apache.causeway.testdomain.fixtures;

import java.util.Collection;
import java.util.List;

import org.apache.causeway.testdomain.util.dto.IBook;

public interface InventoryJaxbVm<T extends IBook> {
    String title();
    String getName();
    List<T> listBooks();
    T getFavoriteBook();
    void setName(String string);
    void setFavoriteBook(T favoriteBook);
    void setBooks(Collection<T> books);
    void setBooksForTab1(Collection<T> books);
    void setBooksForTab2(Collection<T> books);

    @org.apache.causeway.applib.annotation.Collection
    List<? extends BookView<T>> getBookViews();
}
