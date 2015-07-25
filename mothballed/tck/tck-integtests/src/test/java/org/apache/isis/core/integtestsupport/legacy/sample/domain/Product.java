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

package org.apache.isis.core.integtestsupport.legacy.sample.domain;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.MaxLength;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.TypicalLength;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.util.TitleBuffer;

@Immutable
public class Product extends AbstractDomainObject {

    // use ctrl+space to bring up the NO templates.
    // if you do not wish to subclass AbstractDomainObject,
    // then use the "injc - Inject Container" template.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ Logger
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(Product.class);

    // }}

    // {{ Identification Methods
    /**
     * Defines the title that will be displayed on the user interface in order
     * to identity this object.
     */
    public String title() {
        final TitleBuffer t = new TitleBuffer();
        t.append(getCode());
        t.append(":", getDescription());
        return t.toString();
    }

    // }}

    // {{ Code
    private String code;

    @TypicalLength(9)
    @MaxLength(9)
    @Disabled(when = When.ONCE_PERSISTED)
    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    // }}

    // {{ Description
    private String description;

    @TypicalLength(50)
    @MaxLength(255)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    // }}

    // {{ PlaceOfManufacture
    private Country placeOfManufacture;

    @Optional
    public Country getPlaceOfManufacture() {
        return placeOfManufacture;
    }

    public void setPlaceOfManufacture(final Country placeOfManufacture) {
        this.placeOfManufacture = placeOfManufacture;
    }

    // }}

    // {{ Price
    private Double price;

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(final Double price) {
        this.price = price;
    }

    public String validatePrice(final Double price) {
        if (price.doubleValue() <= 0) {
            return "Price must be positive";
        }
        return null;
    }

    // }}

    // {{ SimilarProducts
    private List<Product> similarProducts = new ArrayList<Product>();

    public List<Product> getSimilarProducts() {
        return this.similarProducts;
    }

    @SuppressWarnings("unused")
    private void setSimilarProducts(final List<Product> similarProducts) {
        this.similarProducts = similarProducts;
    }

    public void addToSimilarProducts(final Product country) {
        getSimilarProducts().add(country);
    }

    public void removeFromSimilarProducts(final Product country) {
        getSimilarProducts().remove(country);
    }

    public String validateAddToSimilarProducts;

    public String validateAddToSimilarProducts(final Product country) {
        return validateAddToSimilarProducts;
    }

    public String validateRemoveFromSimilarProducts;

    public String validateRemoveFromSimilarProducts(final Product country) {
        return validateRemoveFromSimilarProducts;
    }

    public String disableSimilarProducts;

    public String disableSimilarProducts() {
        return this.disableSimilarProducts;
    }

    public boolean hideSimilarProducts;

    public boolean hideSimilarProducts() {
        return this.hideSimilarProducts;
    }

    // }}

    // {{
    /**
     * An action to invoke
     */
    public void foobar() {
    }
    // }}

}
