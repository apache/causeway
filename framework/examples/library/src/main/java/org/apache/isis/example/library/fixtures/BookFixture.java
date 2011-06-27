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


package org.apache.isis.example.library.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.library.dom.Book;


public class BookFixture extends AbstractFixture {

    public void install() {
        createBook("Dr Suess", "J3342", "The Cat in the Hat");
        createBook("Dr Suess", "J3344", "Green Eggs and Ham");
        createBook("Enid Blyton", "J3391", "Well Done Secret Seven");
        createBook("Enid Blyton", "J3394", "Secret Seven Adventure");
        createBook("Enid Blyton", "J3395", "Secret Seven on the Trail");
        createBook("Jeremy Strong", "J4551", "Invasion of the Christmas Puddings");   
        createBook("Jeremy Strong", "J4570", "Beware! Killer Tomatoes");   
    }

    private void createBook(String author, String code, String title) {
        Book book = newTransientInstance(Book.class);
        book.setAuthor(author);
        book.setCode(code);
        book.setCoverTitle(title);
        persist(book);
    }
}

