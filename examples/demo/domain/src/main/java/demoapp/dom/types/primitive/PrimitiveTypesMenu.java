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
package demoapp.dom.types.primitive;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainObjectLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;

import demoapp.dom.types.primitive.booleans.PrimitiveBooleans;
import demoapp.dom.types.primitive.bytes.PrimitiveBytes;
import demoapp.dom.types.primitive.chars.PrimitiveChars;
import demoapp.dom.types.primitive.doubles.PrimitiveDoubles;
import demoapp.dom.types.primitive.floats.PrimitiveFloats;
import demoapp.dom.types.primitive.ints.PrimitiveInts;
import demoapp.dom.types.primitive.longs.PrimitiveLongs;
import demoapp.dom.types.primitive.shorts.PrimitiveShorts;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.PrimitiveTypesMenu"
)
@DomainObjectLayout(
        named="PrimitiveTypes"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class PrimitiveTypesMenu {


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveBytes bytes(){
        return new PrimitiveBytes();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveShorts shorts(){
        return new PrimitiveShorts();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveInts ints(){
        return new PrimitiveInts();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveLongs longs(){
        return new PrimitiveLongs();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveFloats floats(){
        return new PrimitiveFloats();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-hashtag")
    public PrimitiveDoubles doubles(){
        return new PrimitiveDoubles();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-font")
    public PrimitiveChars chars(){
        return new PrimitiveChars();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-check-square")
    public PrimitiveBooleans booleans(){
        return new PrimitiveBooleans();
    }

}
