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
package org.apache.causeway.testdomain.value;

import java.util.Locale;

import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Utils {

    InteractionContext interactionContext() {
        return InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build();
    }

    // eg.. <ValueWithTypeDto type="string"><com:string>anotherString</com:string></ValueWithTypeDto>
    String valueDtoToXml(final ValueWithTypeDto valueWithTypeDto) {
        var rawXml = Try.call(()->JaxbUtils.toStringUtf8(valueWithTypeDto, opts->opts
                .useContextCache(true)
                .formattedOutput(true)))
        .getValue().orElseThrow();

        return TextUtils.cutter(rawXml)
                .dropBefore("<ValueWithTypeDto")
                .keepBeforeLast("</ValueWithTypeDto>")
                .getValue()
                .replace(" null=\"false\" xmlns:com=\"https://causeway.apache.org/schema/common\" xmlns:cmd=\"https://causeway.apache.org/schema/cmd\"", "")
                + "</ValueWithTypeDto>";

    }

}
