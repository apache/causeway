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


package org.apache.isis.example.library.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.example.library.dom.Book;

public class BookRepository extends AbstractFactoryAndRepository {

    public List<Book> findByTitle(@Named("Title") final String title) {
        Book bookPattern = newTransientInstance(Book.class);
        bookPattern.setCoverTitle(title);
        List<Book> matchingBooks = allMatches(Book.class, bookPattern);
        List<Book> availableBooks = new ArrayList<Book>();
        Iterator<Book> iterator = matchingBooks.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getOnLoan() == null) {
                availableBooks.add(book);
            }
        }
        return availableBooks;
    }
    
    public Book findOneByTitle(String title) {
        List<Book> findByTitle = findByTitle(title);
        return findByTitle.size() == 0 ? null: findByTitle.get(0);
    }
    
    public Book newBook() {
       return newTransientInstance(Book.class);
    }
    
    public void removeBook(Book book) {
        remove(book);
        informUser(book.getCoverTitle() + " has been removed from system");
    }
    
    @Exploration
    public List<Book> allInstances() {
        return allInstances(Book.class);
    }
    
    public String title() {
        return "Books";
    }
    
    public String getId() {
        return "books";
    }
    
    public String iconName() {
        return "Book";
    }
}

