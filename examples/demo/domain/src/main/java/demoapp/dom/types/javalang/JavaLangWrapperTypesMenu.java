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
package demoapp.dom.types.javalang;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.types.javalang.booleans.WrapperBooleans;
import demoapp.dom.types.javalang.bytes.WrapperBytes;
import demoapp.dom.types.javalang.characters.WrapperCharacters;
import demoapp.dom.types.javalang.doubles.WrapperDoubles;
import demoapp.dom.types.javalang.floats.WrapperFloats;
import demoapp.dom.types.javalang.integers.WrapperIntegers;
import demoapp.dom.types.javalang.longs.WrapperLongs;
import demoapp.dom.types.javalang.shorts.WrapperShorts;

@Named("demo.JavaLangWrapperTypesMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@DomainObjectLayout(
        named="JavaLangWrapperTypes"
)
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
public class JavaLangWrapperTypesMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperBytes bytes(){
        return new WrapperBytes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperShorts shorts(){
        return new WrapperShorts();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperIntegers integers(){
        return new WrapperIntegers();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperLongs longs(){
        return new WrapperLongs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperFloats floats(){
        return new WrapperFloats();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public WrapperDoubles doubles(){
        return new WrapperDoubles();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-font")
    public WrapperCharacters characters(){
        return new WrapperCharacters();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-check-square")
    public WrapperBooleans booleans(){
        return new WrapperBooleans();
    }

}
