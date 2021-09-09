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
package demoapp.dom.domain.objects.other.embedded.jpa;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Value;

import lombok.AccessLevel;
import lombok.val;

import demoapp.dom.domain.objects.other.embedded.ComplexNumber;

// tag::class[]
//@Entity                                                                 // <.>
@javax.persistence.Embeddable                                                             // <.>
@Value(semanticsProviderClass = ComplexNumberJpaValueSemantics.class)   // <.>
@lombok.Getter                                                          // <.>
@lombok.Setter(AccessLevel.PRIVATE)                                     // <.>
@lombok.AllArgsConstructor(staticName = "of")
@lombok.NoArgsConstructor
public class ComplexNumberJpa
        implements ComplexNumber {

    @javax.persistence.Column(nullable = false)
    private double re;

    @javax.persistence.Column(nullable = false)
    private double im;

// end::class[]

// tag::title[]
    @ObjectSupport public String title() {
        return im >= 0
                ? "" + re + " + " +  im + "i"
                : "" + re + " - " + (-im) + "i";
    }
// end::title[]

// tag::parse[]
    private static final Pattern PATTERN =
        Pattern.compile("^(?<re>\\S*)\\W*(?<sign>[+-])\\W*(?<im>\\S+)i$");

    public static Optional<ComplexNumberJpa> parse(final String parse) {
        val m = PATTERN.matcher(parse);
        return m.matches() ?
                Optional.of(ComplexNumberJpa.of(
                    realFrom(m), signFrom(m) * imaginaryFrom(m)))
                : Optional.empty();
    }

    private static double realFrom(final Matcher m) {
        return Double.parseDouble(m.group("re"));
    }
    private static double signFrom(final Matcher m) {
        return m.group("sign").equals("-") ? -1.0d : +1.0d;
    }
    private static double imaginaryFrom(final Matcher m) {
        return Double.parseDouble(m.group("im"));
    }
// end::parse[]

// tag::class[]
    // ...
}
// end::class[]
