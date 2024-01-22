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
package org.apache.causeway.viewer.graphql.model.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import graphql.Scalars;
import graphql.schema.GraphQLType;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ScalarMapper {

    private static List<Class<?>> integerEquivalents = Arrays.asList(
            int.class, Integer.class, Short.class, short.class, BigInteger.class);
    private static List<Class<?>> longEquivalents = Arrays.asList(Long.class, long.class, BigDecimal.class);
    private static List<Class<?>> booleanEquivalents = Arrays.asList(Boolean.class, boolean.class);

    public static GraphQLType typeFor(final Class<?> c){
        if (integerEquivalents.contains(c)){
            return Scalars.GraphQLInt;
        }
        if (longEquivalents.contains(c)){
            return Scalars.GraphQLFloat;
        }
        if (booleanEquivalents.contains(c)){
            return Scalars.GraphQLBoolean;
        }
        return Scalars.GraphQLString;
    }


}
