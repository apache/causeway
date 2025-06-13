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
package org.apache.causeway.applib.value;

import java.io.Serializable;
import java.util.Objects;

import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;

/**
 * Represents a password that will not displayed to the UI but can be persisted.
 *
 * @since 1.x {@index}
 */
@Named(CausewayModuleApplib.NAMESPACE + ".value.Password")
@Value
@XmlAccessorType(XmlAccessType.FIELD)
//TODO: not automatically registered because not encrypted
// @XmlJavaTypeAdapter(Password.JaxbToStringAdapter.class)
public record Password(String password) implements Serializable {

    public static Password of(final String password) {
        return new Password(password);
    }

    public boolean checkPassword(final String password) {
        return Objects.equals(this.password, password);
    }

    @Override
    public String toString() {
        return PlaceholderRenderService.fallback().asText(PlaceholderLiteral.SUPPRESSED);
    }

    public static class JaxbToStringAdapter
    extends jakarta.xml.bind.annotation.adapters.XmlAdapter<String, Password> {
        @Override
        public Password unmarshal(final String str) throws Exception {
            return str != null
                    ? new Password(str)
                    : null;
        }

        @Override
        public String marshal(final Password password) throws Exception {
            return password != null
                    ? password.password()
                    : null;
        }
    }
}
