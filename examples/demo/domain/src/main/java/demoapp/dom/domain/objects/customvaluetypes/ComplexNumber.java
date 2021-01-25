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
package demoapp.dom.domain.objects.customvaluetypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Value;

@PersistenceCapable
@EmbeddedOnly
@Value(semanticsProviderClass = ComplexNumberValueSemantics.class)
@lombok.Data
@lombok.AllArgsConstructor(staticName = "of")
public class ComplexNumber {

    public String title() {
        return im >= 0
                ? "" + re + " + " +  im + "i"
                : "" + re + " - " + (-im) + "i";
    }

    private static final Pattern PATTERN = Pattern.compile("^(?<real>\\S*)\\W*(?<sign>[+-])\\W*(?<imaginary>\\S+)i$");
    public static ComplexNumber parse(final String parse) {
        final Matcher matcher = PATTERN.matcher(parse);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return ComplexNumber.of(realFrom(matcher), signFrom(matcher) * imaginaryFrom(matcher));
        } catch(Exception ex) {
            return null;
        }
    }

    private static double imaginaryFrom(Matcher matcher) {
        return Double.parseDouble(matcher.group("imaginary"));
    }

    private static double realFrom(Matcher matcher) {
        return Double.parseDouble(matcher.group("real"));
    }

    private static double signFrom(Matcher matcher) {
        return matcher.group("sign").equals("-") ? -1.0d : +1.0d;
    }

    @javax.jdo.annotations.Column(allowsNull = "false")
    private double re;

    @javax.jdo.annotations.Column(allowsNull = "false")
    private double im;

}
