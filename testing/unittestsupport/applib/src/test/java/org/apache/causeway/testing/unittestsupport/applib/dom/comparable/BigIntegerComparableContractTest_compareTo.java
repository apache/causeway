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
package org.apache.causeway.testing.unittestsupport.applib.dom.comparable;

import java.math.BigInteger;
import java.util.List;

public class BigIntegerComparableContractTest_compareTo extends ComparableContractTest_compareTo<BigInteger> {

    /**
     * <code>
     * item0  < item1 = item2 < item3
     * </code>
     */
    @Override
    protected List<List<BigInteger>> orderedTuples() {
        return listOf(
                listOf(
                        BigInteger.ZERO,
                        BigInteger.ONE,
                        BigInteger.valueOf(1L),
                        BigInteger.TWO
                        ),
                listOf(
                        BigInteger.valueOf(Long.MIN_VALUE),
                        BigInteger.valueOf(Long.MIN_VALUE + 1),
                        BigInteger.valueOf(Long.MIN_VALUE + 1),
                        BigInteger.valueOf(Long.MIN_VALUE + 2)
                        ),
                listOf(
                        BigInteger.valueOf(Long.MIN_VALUE),
                        BigInteger.valueOf(Long.MAX_VALUE / 2),
                        BigInteger.valueOf(Long.MAX_VALUE / 2),
                        BigInteger.valueOf(Long.MAX_VALUE)
                        )
                );
    }
}
