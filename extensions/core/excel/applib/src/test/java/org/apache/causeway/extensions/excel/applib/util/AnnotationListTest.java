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
package org.apache.causeway.extensions.excel.applib.util;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AnnotationListTest {

    @Test
    public void testGetByAnnotation_OrderBy_OrderAscending() throws Exception {

        // given
        AnnotationTriplet t0 = new AnnotationTriplet("value", 0, 0);
        AnnotationTriplet t1 = new AnnotationTriplet("value", 1, 1);
        AnnotationTriplet t2 = new AnnotationTriplet("deco", 2, 1);
        AnnotationTriplet t3 = new AnnotationTriplet("deco", 3, 2);

        AnnotationList list = new AnnotationList(Arrays.asList(t2, t1, t3, t0));

        // when
        List<AnnotationTriplet> result = list.getByAnnotation_OrderBy_OrderAscending("value");

        // assert
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get(0)).isEqualTo(t0);
        Assertions.assertThat(result.get(1)).isEqualTo(t1);

    }


}
