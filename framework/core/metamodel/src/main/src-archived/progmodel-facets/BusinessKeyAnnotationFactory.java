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




import org.apache.isis.applib.annotation.BusinessKey;
import org.apache.isis.noa.annotations.Annotation;
import org.apache.isis.noa.annotations.BusinessKeyAnnotation;

import java.lang.reflect.Method;


public class BusinessKeyAnnotationFactory extends AbstractAnnotationFactory {

    public BusinessKeyAnnotationFactory() {
        super(BusinessKeyAnnotation.class);
    }

    public Annotation process(final Class cls) {
        BusinessKey annotation = (BusinessKey) cls.getAnnotation(BusinessKey.class);
        return create(annotation);
    }

    public Annotation process(final Method method) {
        BusinessKey annotation = (BusinessKey) method.getAnnotation(BusinessKey.class);
        return create(annotation);
    }

    private BusinessKeyAnnotation create(BusinessKey annotation) {
        return annotation == null ? null : new BusinessKeyAnnotation(annotation.value());
    }

}
