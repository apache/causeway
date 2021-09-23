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
package org.apache.isis.tooling.dsl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Reflect;

class GraphQLTest {

    @Test
    void dynamicClassGeneration_usingGraphQLInput()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
            SecurityException, IntrospectionException {

        var classLoader = JavaSourceCompilingClassLoader.newInstance();

        String schema = _Strings.readFromResource(
                getClass(), getClass().getSimpleName() + ".graphqls", StandardCharsets.UTF_8);

        final var isisSchema = GraphQLToJavaSourceConverter.parseGraphQL(schema);

        final var dslClassNames = _Lists.<String>newArrayList();

        isisSchema.asGraphQLSchema();
        isisSchema.streamAsJavaModels("testdummies")
        .forEach(javaModel->{

                String className = javaModel.getName().canonicalName();
                dslClassNames.add(className);

//                System.err.println("=================================");
//                System.err.println("writing: " + className);
//                System.err.println("=================================");

                var javaFile = javaModel.buildJavaFile();
                classLoader.writeJavaSource(className, javaFile::writeTo);
        });

        assertEquals(4, dslClassNames.size());

        for(String className : dslClassNames) {

            Class<?> cls = Class.forName(className, true, classLoader);

            assertEquals(className, cls.getName());

            if(cls.isEnum()
                    || cls.isInterface()) {
                continue;
            }

            var getter = _Reflect.getGetter(cls, "id");
            var setter = _Reflect.getSetter(cls, "id");

            Object instance = cls.getConstructor().newInstance();

            // when
            _Reflect.writeToSetterOn(setter, instance, 66L);

            // then
            assertEquals(66L,
                    _Reflect.readFromGetterOn(getter, instance));
        }

    }

}
