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

package org.apache.isis.applib.adapters;

/**
 * Provides a mechanism for parsing and rendering string representations of
 * objects.
 *
 * <p>
 * Specifically, this interface embodies three related capabilities:
 * <ul>
 * <li>to parse a string representation and convert to an object.
 * <li>to provide a string representation of the object, for use as its title.
 * <li>to indicate the typical length of such a string representation.
 * </ul>
 *
 * <p>
 * For custom-written (as opposed to third-party) value types, the ability for
 * the {@link Parser} to provide a title responsibilities overlap with other
 * conventions for domain objects. Specifically, normally we write a
 * <tt>title()</tt> method to return a title. In such cases a typical
 * implementation of {@link Parser} would just delegate to the value type itself
 * to obtain the title (ie invoking the <tt>title()</tt> method directly rather
 * than having the framework do this).
 *
 * <p>
 * Similarly, the ability to return a typical length also overlaps with the
 * {@link TypicalLength} annotation; which is why {@link TypicalLength} cannot
 * be applied to types, only to properties and parameters.
 *
 * <p>
 * For third-party value types, eg {@see http://timeandmoney.sourceforge.net/
 * Time-and-Money} there is no ability to write <tt>title()</tt> methods or
 * annotated with {@link TypicalLength}; so this is the main reason that this
 * interface has to deal with titles and lengths.
 *
 * <p>
 * This interface is used in two complementary ways:
 * <ul>
 * <li>As one option, it allows objects to take control of their own parsing, by
 * implementing directly. However, the instance is used as a factory for itself.
 * The framework will instantiate an instance, invoke the appropriate method
 * method, and use the returned object. The instantiated instance itself will be
 * discarded.</li>
 * <li>Alternatively, an implementor of this interface can be nominated in the
 * {@link org.apache.isis.applib.annotation.Parseable} annotation, allowing a
 * class that needs to be parseable to indicate how it can be parsed.</li>
 *
 * <p>
 * Whatever the class that implements this interface, it must also expose either
 * a <tt>public</tt> no-arg constructor, or (for implementations that also are
 * <tt>Facet</tt>s) a <tt>public</tt> constructor that accepts a single
 * <tt>FacetHolder</tt>. This constructor allows the framework to instantiate
 * the object reflectively.
 *
 * @see DefaultsProvider
 * @see EncoderDecoder
 * @see ValueSemanticsProvider
 */
// tag::refguide[]
public interface Parser<T> {

    // end::refguide[]
    /**
     * Parses a string to an instance of the object.
     *
     * <p>
     * Note that here the implementing class is acting as a factory for itself.
     * @param contextPojo
     *            - the context domain object for which the text is being
     *            parsed. For example +3 might mean add 3 to the current number.
     */
    // tag::refguide[]
    T parseTextEntry(Object contextPojo, String entry);

    // end::refguide[]
    /**
     * The typical length of objects that can be parsed.
     */
    // tag::refguide[]
    int typicalLength();

    // end::refguide[]
    /**
     * The title of the object.
     */
    // tag::refguide[]
    String displayTitleOf(T object);

    // end::refguide[]
    /**
     * The title of the object using a mask.
     */
    // tag::refguide[]
    String displayTitleOf(T object, String usingMask);

    // end::refguide[]
    /**
     * A title for the object that is valid but which may be easier to edit than
     * the title provided by a <code>TitleFacet</code>.
     *
     * <p>
     * The idea here is that the viewer can display a parseable title for an
     * existing object when, for example, the user initially clicks in the
     * field. So, a date might be rendered via a <code>TitleFacet</code> as
     * <tt>May 2, 2007</tt>, but its editable form might be <tt>20070502</tt>.
     */
    // tag::refguide[]
    String parseableTitleOf(T existing);

    // end::refguide[]
    /**
     * The max length of objects that can be parsed (if any).
     * A return type of -1 corresponds to unlimited.
     */
    // tag::refguide[]
    default int maxLength() {
        return -1;
    }

}
// end::refguide[]
