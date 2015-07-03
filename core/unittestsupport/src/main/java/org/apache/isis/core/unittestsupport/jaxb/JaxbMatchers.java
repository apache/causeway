/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.unittestsupport.jaxb;

import com.google.common.base.Objects;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * For example usage, see <a href="https://github.com/isisaddons/isis-module-publishmq">Isis addons' publishmq module</a> (non-ASF)
 */
public class JaxbMatchers {

    private JaxbMatchers(){}

    /**
     * Performs an equality comparison of a {@link javax.xml.bind.annotation.XmlRootElement}-annotated class
     * to another by converting into XML first.
     */
    public static <T> Matcher<? super T> isEquivalentTo(final T expected) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(final T item) {
                final String expectedXml = JaxbUtil.toXml(expected);
                final String itemXml = JaxbUtil.toXml(item);
                return Objects.equal(expectedXml, itemXml);
            }

            @Override
            public void describeTo(final org.hamcrest.Description description) {
                final String expectedXml = JaxbUtil.toXml(expected);
                description.appendText("is equivalent to ").appendValue(expectedXml);
            }
        };
    }

}
