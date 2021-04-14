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
package demoapp.dom.featured.customui.latlng;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.val;
import lombok.experimental.UtilityClass;


@UtilityClass
public class LatLngUtils {

    public static BigDecimal toBigDecimal(final String val) {
        return new BigDecimal(val);
    }

    public static String toString(BigDecimal val) {
        return val.toPlainString();
    }

    public static String add(final String val, final int hundredths) {
        val scaleBd = new BigDecimal(hundredths).setScale(2, RoundingMode.HALF_UP);
        val scaleDividedBy100 = scaleBd.divide(new BigDecimal(100), RoundingMode.HALF_UP);
        val bd = toBigDecimal(val);
        val newVal = bd.add(scaleDividedBy100);
        return toString(newVal);
    }
}
