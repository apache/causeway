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
package demoapp.dom.progmodel.customvaluetypes.customvalues;

import lombok.NonNull;

import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.schema.common.v2.ValueType;

//tag::class[]
@Named("demo.EmailAddressValueSemantics")
@Component
public class EmailAddressValueSemantics
        extends ValueSemanticsAbstract<EmailAddress> {   // <.>
    // ...
//end::class[]

    @Override
    public Class<EmailAddress> getCorrespondingClass() {
        return EmailAddress.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

//tag::compose[]
    @Override
    public ValueDecomposition decompose(final EmailAddress value) {
        return decomposeAsNullable(value, EmailAddress::getEmailAddress, ()->null);
    }

    @Override
    public EmailAddress compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getString, EmailAddress::of, ()->null);
    }
//end::compose[]

//tag::getDefaultsProvider[]
    @Override
    public DefaultsProvider<EmailAddress> getDefaultsProvider() {
        return new DefaultsProvider<EmailAddress>() {
            @Override
            public EmailAddress getDefaultValue() {
                return EmailAddress.of("");
            }
        };
    }
//end::getDefaultsProvider[]

//tag::getRenderer[]
    @Override
    public Renderer<EmailAddress> getRenderer() {
        return new Renderer<>() {
            @Override
            public String titlePresentation(Context context, EmailAddress emailAddress) {
                return emailAddress == null ? null : emailAddress.getEmailAddress();
            }
        };
    }

//end::getRenderer[]

//tag::getParser[]
    @Override
    public Parser<EmailAddress> getParser() {
        return new Parser<>() {
            // https://stackoverflow.com/a/47181151
            final Pattern REGEX = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$");

            @Override
            public String parseableTextRepresentation(Context context, EmailAddress value) {
                return renderTitle(value, EmailAddress::getEmailAddress);
            }

            @Override
            public EmailAddress parseTextRepresentation(Context context, String text) {
                if(!REGEX.matcher(text).matches()) {
                    throw new RuntimeException("Invalid email format");
                }
                if (_Strings.isEmpty(text)) return null;
                return EmailAddress.of(text);
            }

            @Override
            public int typicalLength() {
                return 20;
            }

            @Override
            public int maxLength() {
                return 50;
            }
        };
    }
//end::getParser[]

//tag::getIdStringifier[]
    @Override
    public IdStringifier<EmailAddress> getIdStringifier() {
        return new IdStringifier.EntityAgnostic<>() {
            @Override
            public Class<EmailAddress> getCorrespondingClass() {
                return EmailAddressValueSemantics.this.getCorrespondingClass();
            }

            @Override
            public String enstring(@NonNull EmailAddress value) {
                return _Strings.base64UrlEncode(value.getEmailAddress());
            }

            @Override
            public EmailAddress destring(@NonNull String stringified) {
                return EmailAddress.of(_Strings.base64UrlDecode(stringified));
            }
        };
    }
//end::getIdStringifier[]

//tag::class[]
}
//end::class[]
