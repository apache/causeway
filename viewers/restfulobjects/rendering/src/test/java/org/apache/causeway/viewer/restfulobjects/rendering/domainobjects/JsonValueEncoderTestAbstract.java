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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.BigIntegerValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.BooleanValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.ByteValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.DoubleValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.FloatValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.IntValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.LongValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.ShortValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.StringValueSemantics;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderServiceDefault;

abstract class JsonValueEncoderTestAbstract {

    protected MetaModelContext mmc;
    protected JsonValueEncoderServiceDefault jsonValueEncoder;

    protected void setUp() {
        mmc = MetaModelContext_forTesting.builder()
                .build()
                .withValueSemantics(new StringValueSemantics())
                .withValueSemantics(new BooleanValueSemantics())
                .withValueSemantics(new BigDecimalValueSemantics())
                .withValueSemantics(new BigIntegerValueSemantics())
                .withValueSemantics(new LongValueSemantics())
                .withValueSemantics(new IntValueSemantics())
                .withValueSemantics(new ShortValueSemantics())
                .withValueSemantics(new ByteValueSemantics())
                .withValueSemantics(new DoubleValueSemantics())
                .withValueSemantics(new FloatValueSemantics())
                ;

        jsonValueEncoder = JsonValueEncoderServiceDefault.forTesting(mmc.getSpecificationLoader());
    }

    protected ObjectSpecification specFor(final Class<?> cls) {
        return mmc.getSpecificationLoader().specForTypeElseFail(cls);
    }

}
