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

package org.apache.isis.core.metamodel.facets.value;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.apache.isis.applib.value.Image;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProvider;

import lombok.val;

public class ImageValueSemanticsProviderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private FacetHolder mockFacetHolder;

    private ImageValueSemanticsProvider adapter;

    @Before
    public void setUp() throws Exception {
        adapter = new ImageValueSemanticsProvider(mockFacetHolder);
    }

    @Test
    public void roundtrip() throws Exception {
        
        val encodedImageData = adapter.toEncodedString(new Image(getPixels()));
        val restoredImage = adapter.fromEncodedString(encodedImageData);

        val pixels = restoredImage.getImage();

// debug        
//        System.out.println(_Ints.tableForm(getPixels(), 10, Integer::toHexString));
//        System.out.println();
//        System.out.println();
//        System.out.println(_Ints.tableForm(pixels, 10, Integer::toHexString));
        
        assertArrayEquals(getPixels(), pixels);
        
    }
    
    // -- SAMPLE
    
    private int[][] getPixels() {
        final int[][] array = new int[10][10];
        array[0][0] = 0xFEFDFCFB;
        array[0][1] = 0x3F218A;
        array[0][3] = 0x123456;
        array[0][4] = 0x7FFFFF;
        array[0][5] = -0x7FFFFF;
        array[0][6] = -0x700000;
        return array;
    }
    
}
