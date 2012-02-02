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

package org.apache.isis.viewer.html.request;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.viewer.html.image.ImageProviderResourceBased;

public class ImageProviderResourceBasedTest {

    private ImageProviderResourceBased imageProvider;

    @Before
    public void setUp() {
        imageProvider = new ImageProviderResourceBased();
    }

    @After
    public void tearDown() {
        imageProvider = null;
    }

    @Test
    public void canFindDefaultExplicitly() {
        assertThat(imageProvider.image("Default"), equalTo("images/Default.png"));
    }

    @Test
    public void nonExistentImageUsesDefault() {
        assertThat(imageProvider.image("NonExistent"), equalTo("images/Default.png"));
    }

    @Test
    public void nonDefaultPngExistingImageIsReturned() {
        assertThat(imageProvider.image("Service"), equalTo("images/Service.png"));
    }

    @Test
    public void nonDefaultGifExistingImageIsReturned() {
        assertThat(imageProvider.image("Customer"), equalTo("images/Customer.gif"));
    }

}
