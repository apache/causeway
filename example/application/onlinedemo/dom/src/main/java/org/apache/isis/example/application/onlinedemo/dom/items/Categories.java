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

package org.apache.isis.example.application.onlinedemo.dom.items;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.QueryOnly;

/**
 * A repository for {@link Category}.
 * 
 * <p>
 * The implementation depends on the configured object store.
 */
@Hidden
@Named("Categories")
public class Categories extends AbstractFactoryAndRepository {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "categories";
    }

    public String iconName() {
        return "Category";
    }
    // }}
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<Category> all() {
        return allInstances(Category.class);
    }

    @Hidden
    // intended for fixtures only, so is hidden in the UI
    public Category newCategory(String description) {
        Category category = find(description);
        if (category != null) {
            return category;
        }
        category = newTransientInstance(Category.class);
        category.setDescription(description);
        persist(category);
        return category;
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public Category find(@Named("Description") String description) {
        return firstMatch(Category.class, Category.matching(description));
    }
    
}
