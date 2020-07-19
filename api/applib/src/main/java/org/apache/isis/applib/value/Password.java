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

package org.apache.isis.applib.value;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Value;

// tag::refguide[]
// end::refguide[]
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.password.PasswordValueSemanticsProvider")
@XmlAccessorType(XmlAccessType.FIELD)
// @XmlJavaTypeAdapter(Password.JaxbToStringAdapter.class) // TODO: not automatically registered because not secure enough.  Instead we should set up some sort of mechanism to encrypt.
@lombok.Value
public class Password implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final String STARS = "********************";
    
    private final String password;

    // in support of XML un-marshaling
    @SuppressWarnings("unused")
    private Password() {
        this("");
    }
    
    public Password(String password) {
        this.password = password;
    }

    public boolean checkPassword(final String password) {
        return Objects.equals(this.password, password);
    }

    @Override
    public String toString() {
        if (password == null) {
            return "";
        }
        return STARS;
    }


    public static class JaxbToStringAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, Password> {
        @Override
        public Password unmarshal(String str) throws Exception {
            return str != null
                    ? new Password(str)
                    : null;
        }

        @Override
        public String marshal(Password password) throws Exception {
            return password != null
                    ? password.getPassword()
                    : null;
        }
    }
}
