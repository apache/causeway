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
package org.apache.isis.commons.internal.image;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import lombok.val;

class _ImagesTest {

    @Test
    void pixelRoundtrip() {
        // given
        val bufferedImage = getBufferedImage();

        // when 
        val imageAfterRoundtrip = _Images.fromPixels(_Images.toPixels(bufferedImage));
        
        // then
        assertArrayEquals(getPixels(), _Images.toPixels(imageAfterRoundtrip));
    }

    @Test
    void bytesRoundtrip() {
        // given
        val bufferedImage = getBufferedImage();

        // when 
        val imageAfterRoundtrip = _Images.fromBytes(_Images.toBytes(bufferedImage));
        
        // then
        assertArrayEquals(getPixels(), _Images.toPixels(imageAfterRoundtrip));
    }
    
    @Test
    void base64Roundtrip() {
        // given
        val bufferedImage = getBufferedImage();

        // when 
        val imageAfterRoundtrip = _Images.fromBase64(_Images.toBase64(bufferedImage));
        
        // then
        assertArrayEquals(getPixels(), _Images.toPixels(imageAfterRoundtrip));
    }
    
    // -- SAMPLER
    
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
    
    private BufferedImage getBufferedImage() {
        return _Images.fromPixels(getPixels());
    }
    
    
}
