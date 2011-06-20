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


package org.apache.isis.app.cart;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.value.Money;


public class Product extends AbstractDomainObject {
    private String title;
    private String description;
    private String imageUrl;
    private Money price;

    @MemberOrder(sequence = "1.0")
    @TypicalLength(60)
    public String getTitle() {
        resolve(title);
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        objectChanged();
    }

    public String validateTitle(String title) {
        return (title == null || title.length() < 8) ? "Title too short" : null;
    }

    @MemberOrder(sequence = "2.0")
    @MultiLine(numberOfLines = 12)
    @TypicalLength(60)
    public String getDescription() {
        resolve(description);
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        objectChanged();
    }

    @MemberOrder(sequence = "3.0")
    public String getImageUrl() {
        resolve(imageUrl);
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        objectChanged();
    }

    @MemberOrder(sequence = "4.0")
    public Money getPrice() {
        resolve();
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
        objectChanged();
    }

    public String title() {
        return getTitle();
    }
}
