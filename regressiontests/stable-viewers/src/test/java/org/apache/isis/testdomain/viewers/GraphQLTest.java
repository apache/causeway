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
package org.apache.isis.testdomain.viewers;

import java.nio.charset.StandardCharsets;

import com.squareup.javapoet.JavaFile;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.testdomain.util.dsl.IsisDsl;
import org.apache.isis.testdomain.util.dsl.JavaSourceCompilingClassLoader;

class GraphQLTest {

    @Test
    void gen() throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        var classLoader = JavaSourceCompilingClassLoader.newInstance();

        String schema = _Strings.readFromResource(
                getClass(), getClass().getSimpleName() + ".graphqls", StandardCharsets.UTF_8);

        final var isisSchema = IsisDsl.parseGraphQL(schema);

        final var dslClassNames = _Lists.<String>newArrayList();

        isisSchema.asGraphQLSchema();
        isisSchema.streamAsTypeSpecs()
        .forEach(typeSpec->{

                String packageName = "testdummies";
                String className = packageName + "." +
                        (String) ReflectionUtils.getField(
                                ReflectionUtils.findField(typeSpec.getClass(), "name"), typeSpec);
                dslClassNames.add(className);

//                System.err.println("=================================");
//                System.err.println("writing: " + className);
//                System.err.println("=================================");

                var javaFile = JavaFile.builder(packageName, typeSpec)
                        .build();
                classLoader.writeJavaSource(className, javaFile::writeTo);
        });

        for(String className : dslClassNames) {

            System.err.println("=================================");
            System.err.println("compiling: " + className);
            System.err.println("=================================");

            Class<?> cls = Class.forName(className, false, classLoader);
            //Object instance = cls.newInstance();
            System.out.println(cls);

        }

    }

}
