package org.apache.isis.extensions.excel.dom.util;

public class AnnotationTriplet implements Comparable<AnnotationTriplet>{

    AnnotationTriplet(final String annotation, final Integer colNumber, final Integer order){
        this.annotation = annotation;
        this.colNumber = colNumber;
        this.order = order;
    }

    private String annotation;
    private Integer order;
    private Integer colNumber;

    String getAnnotation() {
        return annotation;
    }

    Integer getColnumber() {
        return colNumber;
    }

    @Override public int compareTo(final AnnotationTriplet o) {

        if (this.annotation.equals(o.annotation)){
            return this.order.compareTo(o.order);
        } else {
            return this.annotation.compareTo(o.annotation);
        }

    }
}
