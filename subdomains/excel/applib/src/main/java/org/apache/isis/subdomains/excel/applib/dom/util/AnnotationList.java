package org.apache.isis.subdomains.excel.applib.dom.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationList {

    AnnotationList(List<AnnotationTriplet> list){
        this.list = list;
    }

    List<AnnotationTriplet> list;

    List<AnnotationTriplet> getByAnnotation_OrderBy_OrderAscending(String annotation){
        List<AnnotationTriplet> result = new ArrayList<>();
        for (AnnotationTriplet a : list){
            if (a.getAnnotation().equals(annotation)){
                result.add(a);
            }
        }
        Collections.sort(result);
        return result;
    }

}
