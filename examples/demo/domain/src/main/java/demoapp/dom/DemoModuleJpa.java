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
package demoapp.dom;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import org.apache.isis.extensions.commandlog.jpa.IsisModuleExtCommandLogJpa;
import org.apache.isis.persistence.jpa.eclipselink.IsisModuleJpaEclipselink;

import demoapp.dom.types.javalang.booleans.jpa.WrapperBooleanJpa;
import demoapp.dom.types.javalang.bytes.jpa.WrapperByteJpa;
import demoapp.dom.types.javalang.characters.jpa.WrapperCharacterJpa;
import demoapp.dom.types.javalang.doubles.jpa.WrapperDoubleJpa;
import demoapp.dom.types.javalang.floats.jpa.WrapperFloatJpa;
import demoapp.dom.types.javalang.integers.jpa.WrapperIntegerJpa;
import demoapp.dom.types.javalang.longs.jpa.WrapperLongJpa;
import demoapp.dom.types.javalang.shorts.jpa.WrapperShortJpa;
import demoapp.dom.types.javalang.strings.jpa.JavaLangStringJpa;

@Configuration
@Profile("demo-jpa")
@Import({
    DemoModuleCommon.class,
    IsisModuleJpaEclipselink.class,
    IsisModuleExtCommandLogJpa.class,
})
@EntityScan(basePackageClasses = {
        JavaLangStringJpa.class,
        WrapperBooleanJpa.class,
        WrapperDoubleJpa.class,
        WrapperFloatJpa.class,
        WrapperCharacterJpa.class,
        WrapperLongJpa.class,
        WrapperIntegerJpa.class,
        WrapperShortJpa.class,
        WrapperByteJpa.class,
})
public class DemoModuleJpa {

}
