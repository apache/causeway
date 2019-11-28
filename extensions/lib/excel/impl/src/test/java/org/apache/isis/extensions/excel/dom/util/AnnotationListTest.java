package org.apache.isis.extensions.excel.dom.util;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.extensions.excel.dom.util.AnnotationList;
import org.apache.isis.extensions.excel.dom.util.AnnotationTriplet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AnnotationListTest {


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
