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

import java.awt.Image;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.value.image.ImageValueSemanticsProviderAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ImageValueSemanticsProviderAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private FacetHolder mockFacetHolder;

    @Mock
    private ServiceInjector mockServicesInjector;

    private TestImageSemanticsProvider adapter;

    @Before
    public void setUp() throws Exception {

        adapter = new TestImageSemanticsProvider(mockFacetHolder);
    }

    @Test
    public void testImageData() throws Exception {

        final String data = adapter.toEncodedString(null);
        final int[][] array = adapter.doRestore(data);

        assertEquals(0xFF000000, array[0][0]);
        assertEquals(0xFF3F218A, array[0][1]);
        assertEquals(0xFF123456, array[0][3]);
        assertEquals(0xFF7FFFFF, array[0][4]);
        assertEquals(-0x7FFFFF, array[0][5]);
        assertEquals(-0x700000, array[0][6]);
    }
}

class TestImageSemanticsProvider extends ImageValueSemanticsProviderAbstract<int[][]> {

    TestImageSemanticsProvider(final FacetHolder holder) {
        super(holder, null);
    }

    @Override
    protected int[][] getPixels(final Object object) {
        final int[][] array = new int[10][10];
        array[0][1] = 0x3F218A;
        array[0][3] = 0x123456;
        array[0][4] = 0x7FFFFF;
        array[0][5] = -0x7FFFFF;
        array[0][6] = -0x700000;
        return array;
    }

    @Override
    protected int[][] setPixels(final int[][] pixels) {
        return pixels;
    }

    @Override
    public int getHeight(final ManagedObject object) {
        return 0;
    }

    @Override
    public Image getImage(final ManagedObject object) {
        return null;
    }

    @Override
    public int getWidth(final ManagedObject object) {
        return 0;
    }

    public ManagedObject setImage(final ManagedObject object, final Image image) {
        return null;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public ManagedObject createValue(final Image image) {
        return null;
    }
}
