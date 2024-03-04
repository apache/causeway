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
package org.apache.causeway.testdomain.model.good;

import java.time.LocalDate;
import java.util.Random;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.testdomain.model.interaction.InteractionDemoItem;

import lombok.Getter;
import lombok.Setter;

/**
 * A draft:
 * <p>
 * Without any addition to existing programming model annotations, I could think
 * of adding a new feature, that is to allow for actions to return mixin
 * instances. In this example you see an action updateDemoItemGeneric that
 * randomly chooses between 2 different mixed in actions, which to return: its
 * either updateDemoItemName or updateDemoItemDate both mixins implementing the
 * 'I am a mixin' marker interface DemoItemUpdateMixin. The meta-model figures
 * out on type inspection (compile time types that is) which types are mixin
 * types. Hence the marker interface.
 * <p>
 * instead of having to define a marker interface yourself, the framework could
 * just provide one out of the box for this use-case. Something along those
 * lines: interface MixinAsReturnType {} with special semantics, that is, those
 * stay hidden in the UI and are only considered as action return types.
 *
 * @see "https://the-asf.slack.com/archives/CFC42LWBV/p1662261916803059?thread_ts=1661485836.027909&cid=CFC42LWBV"
 */
@Domain.Exclude// just a draft
//@Named("testdomain.ProperMixinAsReturnType")
//@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperMixinAsReturnType {

    @Inject
    FactoryService factoryService;

    @Property
    @Getter
    @Setter
    private InteractionDemoItem demoItem;

    @Action
    public MixinAsReturnType updateDemoItemGeneric() {
        return new Random().nextBoolean()
                ? factoryService.mixin(ProperMixinAsReturnType.updateDemoItemName.class, this)
                : factoryService.mixin(ProperMixinAsReturnType.updateDemoItemDate.class, this);
    }

    // -- MIXINS

    @DomainObject(nature = Nature.MIXIN)
    interface MixinAsReturnType {
    }

    @Action
    public class updateDemoItemName implements MixinAsReturnType {

        @MemberSupport
        public ProperMixinAsReturnType act(@Parameter final String newName) {
            demoItem.setName(newName);
            return ProperMixinAsReturnType.this;
        }
    }

    @Action
    public class updateDemoItemDate implements MixinAsReturnType {

        @MemberSupport
        public ProperMixinAsReturnType act(@Parameter final LocalDate newDate) {
            demoItem.setDate(newDate);
            return ProperMixinAsReturnType.this;
        }
    }

}
