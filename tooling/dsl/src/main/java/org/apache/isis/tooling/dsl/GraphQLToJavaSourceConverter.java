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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.springframework.util.ReflectionUtils;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphQLToJavaSourceConverter {

    public static GraphQLToJavaSourceConverter parseGraphQL(final String rawSchema) {
        return new GraphQLToJavaSourceConverter(
                SchemaGenerator
                .createdMockedSchema(
                        graphQLPreprocessing(rawSchema)));
    }

    private final GraphQLSchema graphQLSchema;

    public GraphQLSchema asGraphQLSchema() {
        return graphQLSchema;
    }

    @Value(staticConstructor = "of")
    public static class JavaModel {
        public static JavaModel of(final String packageName, final TypeSpec typeSpec) {
            final var classSimpleName = (String) ReflectionUtils.getField(
                    ReflectionUtils.findField(typeSpec.getClass(), "name"),
                    typeSpec);
            return of(ClassName.get(packageName, classSimpleName), typeSpec);
        }
        final ClassName name;
        final TypeSpec typeSpec;
        JavaFile buildJavaFile() {
            return JavaFile.builder(name.packageName(), typeSpec)
                    .build();
        }
    }

    public Stream<JavaModel> streamAsJavaModels(final String packageName) {
        return streamAsTypeSpecs()
                .map(typeSpec->JavaModel.of(packageName, typeSpec));
    }

    public Stream<TypeSpec> streamAsTypeSpecs() {
        return graphQLSchema.getAllTypesAsList().stream()
        .filter(t->!isBuiltIn(t))
        .map(t->{

//            System.out.println("==============================================");
//            System.out.println("graphQLSchema: " + t);
//            System.out.println("==============================================");

            if(t instanceof GraphQLEnumType) {
                return enumModel((GraphQLEnumType) t);
            }
            if(t instanceof GraphQLInterfaceType) {
                return interfaceModel((GraphQLInterfaceType) t);
            }
            if(t instanceof GraphQLObjectType) {
                return classModel((GraphQLObjectType) t);
            }
            return null;
        })
        .filter(_NullSafe::isPresent);
    }

    // -- HELPER

    private TypeSpec enumModel(final GraphQLEnumType t) {

        final var typeModelBuilder = TypeSpec.enumBuilder(t.getName())
                .addModifiers(Modifier.PUBLIC);

        t.getValues().forEach(valueDef->{
            typeModelBuilder.addEnumConstant(valueDef.getName());
        });

        return typeModelBuilder.build();
    }

    private TypeSpec interfaceModel(final GraphQLInterfaceType t) {

        final var typeModelBuilder = TypeSpec.interfaceBuilder(t.getName())
                .addModifiers(Modifier.PUBLIC)
                .addMethods(asMethods(t.getFields(), Modifier.PUBLIC, Modifier.ABSTRACT));

        return typeModelBuilder.build();
    }

    private TypeSpec classModel(final GraphQLObjectType t) {

        final var typeModelBuilder = TypeSpec.classBuilder(t.getName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterfaces(asClassNames(t.getInterfaces()))
                .addFields(asFields(t.getFields(), Modifier.PRIVATE))
                .addMethods(asMethods(t.getFields(), Modifier.PUBLIC));
        return typeModelBuilder.build();
    }

    private TypeName asTypeName(final GraphQLNamedOutputType type) {
        return ClassName.get("", type.getName());
    }

    private TypeName asTypeName(final GraphQLFieldDefinition field) {
        return ClassName.get("", asString(field.getDefinition().getType()));
    }

    private String asString(final Type<?> type) {
        if(type instanceof NonNullType) {
            return asString(((NonNullType)type).getType());
        }
        if(type instanceof ListType) {
            return Collection.class.getName() + "<"+asString(((ListType)type).getType())+">" ;
        }
        if(type instanceof graphql.language.TypeName) {
            return substituteID(((graphql.language.TypeName)type).getName());
        }
        if(type instanceof GraphQLScalarType) {
            return substituteID(((GraphQLScalarType)type).getName());
        }
        return "Unk<"+type+">";
    }

    private String substituteID(final String name) {
        return name.equals("ID")?"Long":name;
    }

    private Iterable<TypeName> asClassNames(final List<GraphQLNamedOutputType> types) {
        return types.stream()
        .map(this::asTypeName)
        .collect(Collectors.toList());
    }

    private Iterable<FieldSpec> asFields(
            final List<GraphQLFieldDefinition> fields,
            final Modifier ... modifiers) {
        return fields.stream()
                .map(f->
                        FieldSpec.builder(asTypeName(f), f.getName(), modifiers)
                            .build())
                .collect(Collectors.toList());
    }

    private Iterable<MethodSpec> asMethods(
            final List<GraphQLFieldDefinition> fields,
            final Modifier ... modifiers) {
        return fields.stream()
                .flatMap(f->Stream.of(
                        getter(f, modifiers),
                        setter(f, modifiers)))
                .collect(Collectors.toList());
    }

    private MethodSpec getter(final GraphQLFieldDefinition f, final Modifier ... modifiers) {
        var builder = MethodSpec.methodBuilder("get" + _Strings.capitalize(f.getName()))
        .addModifiers(modifiers)
        .returns(asTypeName(f));
        for(var modifier : modifiers) {
            if(modifier == Modifier.ABSTRACT) {
                return builder.build();
            }
        }
        return builder
                .addStatement("return $N", f.getName())
                .build();
    }

    private MethodSpec setter(final GraphQLFieldDefinition f, final Modifier ... modifiers) {
        var builder = MethodSpec.methodBuilder("set" + _Strings.capitalize(f.getName()))
        .addModifiers(modifiers)
        .addParameter(asTypeName(f), f.getName());
        for(var modifier : modifiers) {
            if(modifier == Modifier.ABSTRACT) {
                return builder.build();
            }
        }
        return builder
                .addStatement("this.$N = $N", f.getName(), f.getName())
                .build();
    }

    private boolean isBuiltIn(final GraphQLNamedType t) {
        return

        Optional.ofNullable(t.getDescription())
        .map(s->s.startsWith("Built-in ")
                || s.equals("Query"))
        .orElse(false)

            ||

        Optional.ofNullable(t.getName())
        .map(s->s.startsWith("__")
                || s.equals("Query"))
        .orElse(false);

    }

    private static String graphQLPreprocessing(final String rawSchema) {
        return "type Query{hello: String}\n\n"
                +
                rawSchema;
    }


}
