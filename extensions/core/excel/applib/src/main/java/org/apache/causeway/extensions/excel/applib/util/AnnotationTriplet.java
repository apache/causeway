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

/**
 * @since 2.0 {@index}
 */
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
