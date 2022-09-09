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
package org.apache.isis.core.metamodel.facets.object.ident.cssclass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Introspection;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.core.metamodel._testing.MethodRemover_forTesting;
import org.apache.isis.core.metamodel.facets.AbstractTestWithMetaModelContext;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.support.ObjectSupportFacetFactory;

import lombok.val;

//FIXME[ISIS-3207]
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class CssClassFacetMethodTest
extends AbstractTestWithMetaModelContext {

    public static final String CSS_CLASS = "someCssClass";

    @DomainObject(introspection = Introspection.ENCAPSULATION_ENABLED)
    static class DomainObjectInCssClassMethod {
        @MemberSupport public String cssClass() {
            return CSS_CLASS;
        }
    }

    ObjectSupportFacetFactory fa;

    @BeforeEach
    void setUp() throws Exception {
        super.setupWithDefaultProgrammingModel();
//
//        super.setupWithProgrammingModel((mmc, prog)->
//            prog.addFactory(FacetProcessingOrder.F1_LAYOUT, fa = new ObjectSupportFacetFactory(mmc)));
    }

    @Test
    void test() {

        //getSpecificationLoader().reloadSpecification(DomainObjectInCssClassMethod.class);

        val spec = getSpecificationLoader().loadSpecification(DomainObjectInCssClassMethod.class);

        val ctx = new FacetFactory.ProcessClassContext(
                DomainObjectInCssClassMethod.class,
                IntrospectionPolicy.ENCAPSULATION_ENABLED,
                new MethodRemover_forTesting(), spec);

        val fa = new ObjectSupportFacetFactory(getMetaModelContext());

        fa.process(ctx);


        val pojo = new DomainObjectInCssClassMethod();
        val object = getObjectManager().adapt(pojo);

        val cssClass = spec
                //object.getSpecification()
                .getCssClass(object);

        assertThat(cssClass, is(equalTo(CSS_CLASS)));
    }

}
