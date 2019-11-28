package org.apache.isis.extensions.excel.dom.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.extensions.excel.dom.util.AnnotationTriplet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AnnotationTripletTest {

    @Test
    public void testCompareTo() throws Exception {

        // given
        AnnotationTriplet t0 = new AnnotationTriplet("column", 0, 0);
        AnnotationTriplet t1 = new AnnotationTriplet("column", 1, 1);
        AnnotationTriplet t2 = new AnnotationTriplet("deco", 2, 1);
        AnnotationTriplet t3 = new AnnotationTriplet("deco", 3, 2);
        AnnotationTriplet t4 = new AnnotationTriplet("row", 4, 1);
        AnnotationTriplet t5 = new AnnotationTriplet("row", 5, 2);
        AnnotationTriplet t6 = new AnnotationTriplet("skip", 6, 1);
        AnnotationTriplet t7 = new AnnotationTriplet("skip", 7, 2);
        AnnotationTriplet t8 = new AnnotationTriplet("value", 8, 1);
        AnnotationTriplet t9 = new AnnotationTriplet("value", 9, 2);


        List<AnnotationTriplet> l = Arrays.asList(t1, t3, t5, t4, t7, t6, t9, t8, t0, t2);

        // when
        Collections.sort(l);

        // then
        Assertions.assertThat(l.get(0)).isEqualTo(t0);
        Assertions.assertThat(l.get(1)).isEqualTo(t1);
        Assertions.assertThat(l.get(2)).isEqualTo(t2);
        Assertions.assertThat(l.get(3)).isEqualTo(t3);
        Assertions.assertThat(l.get(4)).isEqualTo(t4);
        Assertions.assertThat(l.get(5)).isEqualTo(t5);
        Assertions.assertThat(l.get(6)).isEqualTo(t6);
        Assertions.assertThat(l.get(7)).isEqualTo(t7);
        Assertions.assertThat(l.get(8)).isEqualTo(t8);
        Assertions.assertThat(l.get(9)).isEqualTo(t9);

    }


} 
