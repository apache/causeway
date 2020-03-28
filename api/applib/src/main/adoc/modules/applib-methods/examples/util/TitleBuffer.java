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

package org.apache.isis.applib.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.core.commons.internal._Constants;

/**
 * Title buffer is a utility class to help produce titles for objects without
 * having to add lots of guard code. It provides two basic method: one to
 * concatenate a title to the buffer; another to append a title with a joiner
 * string, taking care adding in necessary spaces. The benefits of using this
 * class is that null references are safely ignored (rather than appearing as
 * 'null'), and joiners (a space by default) are only added when needed.
 */
public class TitleBuffer {
    private static final String SPACE = " ";

    /**
     * Determines if the specified object's title is empty (or null).
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     */
    public static boolean isEmpty(final Object object) {
        final String title = titleFor(object);
        return isEmpty(title);
    }

    /**
     * Reflectively run the <tt>String title()</tt> method if it exists, else
     * fall back to the <tt>toString()</tt> method.
     */
    private static String titleFor(final Object object) {
        if (object == null) {
            return null;
        }
        if(object instanceof String) {
            return object.toString();
        }

        try {
            Method method = object.getClass().getMethod("title", _Constants.emptyClasses);
            return (String) method.invoke(object, _Constants.emptyObjects);
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new TitleBufferException(e);
        } catch (final NoSuchMethodException e) {
            return object.toString();
        }
    }

    /**
     * Determines if the specified text is empty. Will return true if either:
     * the specified reference is null; or if the reference is an empty string.
     */
    public static boolean isEmpty(final String text) {
        return text == null || text.equals("");
    }

    private final StringBuilder title;

    /**
     * Creates a new, empty, {@link org.apache.isis.applib.util.TitleBuffer}.
     */
    public TitleBuffer() {
        title = new StringBuilder();
    }

    /**
     * Creates a new {@link org.apache.isis.applib.util.TitleBuffer}, containing the title of the specified object.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     */
    public TitleBuffer(final Object object) {
        this();
        concat(object);
    }

    /**
     * Creates a new title object, containing the title of the specified object.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     */
    public TitleBuffer(final Object object, final String defaultTitle) {
        this();
        String title = titleFor(object);
        if (isEmpty(title)) {
            concat(defaultTitle);
        } else {
            concat(title);
        }
    }

    /**
     * Creates a new title object, containing the specified text.
     */
    public TitleBuffer(final String text) {
        this();
        concat(text);
    }

    /**
     *
     */
    public TitleBuffer append(final int number) {
        append(String.valueOf(number));
        return this;
    }

    /**
     * Append the title of the specified object to this {@link org.apache.isis.applib.util.TitleBuffer}.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     */
    public TitleBuffer append(final Object object) {
        String title = titleFor(object);
        if (!isEmpty(title)) {
            appendWithSpace(title);
        }
        return this;
    }

    /**
     * Appends the title of the specified object, or the specified text if the
     * objects title is null or empty. Prepends a space if there is already some
     * text in this title object.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     *
     * @param object
     *            the object whose title is to be appended to this title.
     * @param defaultValue
     *            a textual value to be used if the object's title is null or
     *            empty.
     * @return a reference to the called object (itself).
     */
    public TitleBuffer append(final Object object, final String defaultValue) {
        String title = titleFor(object);
        if (!isEmpty(title)) {
            appendWithSpace(title);
        } else {
            appendWithSpace(defaultValue);
        }
        return this;
    }

    /**
     * Appends a space (if there is already some text in this title object) and
     * then the specified text.
     *
     * @return a reference to the called object (itself).
     */
    public TitleBuffer append(final String text) {
        if (!isEmpty(text)) {
            appendWithSpace(text);
        }
        return this;
    }

    /**
     * Appends the joining string and the title of the specified object. If the object is empty then nothing
     * will be appended.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     *
     * @see #isEmpty(Object)
     */
    public TitleBuffer append(final String joiner, final Object object) {
        String title = titleFor(object);
        if (!isEmpty(title)) {
            appendJoiner(joiner);
            appendWithSpace(title);
        }
        return this;
    }

    /**
     * Append the <code>joiner</code> text, a space, and the title of the
     * specified object to the text of this {@link org.apache.isis.applib.util.TitleBuffer}. If the title of
     * the specified object is null then use the <code>defaultValue</code> text.
     * If both the objects title and the default value are null or equate to a
     * zero-length string then no text will be appended ; not even the joiner
     * text.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     *
     * @param joiner
     *            text to append before the title
     * @param object
     *            object whose title needs to be appended
     * @param defaultTitle
     *            the text to use if the the object's title is null.
     * @return a reference to the called object (itself).
     */
    public TitleBuffer append(final String joiner, final Object object, final String defaultTitle) {
        appendJoiner(joiner);
        String title = titleFor(object);
        if (!isEmpty(title)) {
            appendWithSpace(title);
        } else {
            appendWithSpace(defaultTitle);
        }
        return this;
    }

    /**
     * Appends the joiner text, a space, and the text to the text of this
     * {@link org.apache.isis.applib.util.TitleBuffer}. If no text yet exists in the object then the joiner
     * text and space are omitted.
     *
     * @return a reference to the called object (itself).
     */
    public TitleBuffer append(final String joiner, final String text) {
        if (!isEmpty(text)) {
            appendJoiner(joiner);
            appendWithSpace(text);
        }
        return this;
    }

    private void appendJoiner(final String joiner) {
        if (title.length() > 0) {
            title.append(joiner);
        }
    }

    /**
     * Append a space to the text of this TitleString object if, and only if,
     * there is some existing text i.e., a space is only added to existing text
     * and will not create a text entry consisting of only one space.
     *
     * @return a reference to the called object (itself).
     */
    public TitleBuffer appendSpace() {
        if (title.length() > 0) {
            title.append(SPACE);
        }
        return this;
    }

    private void appendWithSpace(final Object object) {
        appendSpace();
        title.append(titleFor(object));
    }

    /**
     * Concatenate the the title value (the result of calling an objects label()
     * method) to this TitleString object. If the value is null the no text is
     * added.
     *
     * @param object
     *            the ObjectAdapter to get a title from
     * @return a reference to the called object (itself).
     */
    public final TitleBuffer concat(final Object object) {
        concat(object, "");
        return this;
    }

    /**
     * Concatenate the title of the object value or the specified default value if the title is equal to null or
     * is empty, to this {@link org.apache.isis.applib.util.TitleBuffer}.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     *
     * @param object
     *            the object to get a title from
     * @param defaultValue
     *            the default text to use when the object is null/empty
     *
     * @return a reference to the called object (itself).
     */
    public final TitleBuffer concat(final Object object, final String defaultValue) {
        String title = titleFor(object);
        if (isEmpty(title)) {
            this.title.append(defaultValue);
        } else {
            this.title.append(title);
        }

        return this;
    }

    /**
     * Concatenate the specified text on to the end of the text of this
     * {@link org.apache.isis.applib.util.TitleBuffer}.
     *
     * @param text
     *            text to append
     * @return a reference to the called object (itself).
     */
    public final TitleBuffer concat(final String text) {
        title.append(text);
        return this;
    }

    /**
     * Concatenate the joiner text and the text to the text of this {@link org.apache.isis.applib.util.TitleBuffer}
     * object. If no text yet exists in the object then the joiner text is
     * omitted.
     *
     * @return a reference to the called object (itself).
     */
    public TitleBuffer concat(final String joiner, final String text) {
        if (!isEmpty(text)) {
            appendJoiner(joiner);
            title.append(text);
        }
        return this;
    }

    /**
     * Concatenate the joiner text and the title of the object to the text of
     * this {@link org.apache.isis.applib.util.TitleBuffer}. If no object yet exists in the object then the
     * joiner text is omitted.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>
     *
     * @return a reference to the called object (itself).
     */
    public final TitleBuffer concat(final String joiner, final Object object) {
        String title = titleFor(object);
        if (!isEmpty(title)) {
            appendJoiner(joiner);
            concat(title, "");
        }
        return this;
    }

    /**
     * Concatenate the joiner text and the title of the object to the text of
     * this {@link org.apache.isis.applib.util.TitleBuffer} object. If no object yet exists in the object then
     * defaultValue is used instead.
     *
     * <p>
     *     Note: this method only obtains the title using either <tt>title()</tt> or <tt>toString()</tt>; it doesn't
     *     honour other mechanisms for specifying the title, such as {@link org.apache.isis.applib.annotation.Title}
     *     annotation.  If that functionality is required, first call
     *     {@link org.apache.isis.applib.DomainObjectContainer#titleOf(Object)} on the object and pass in the resultant
     *     string.
     * </p>

     * @return a reference to the called object (itself).
     */
    public final TitleBuffer concat(final String joiner, final Object object, final String defaultValue) {
        String title = titleFor(object);
        if (isEmpty(title)) {
            appendJoiner(joiner);
            this.title.append(defaultValue);
        } else {
            appendJoiner(joiner);
            this.title.append(title);
        }
        return this;
    }

    /**
     * Returns a String that represents the value of this object.
     */
    @Override
    public String toString() {
        return title.toString();
    }

    /**
     * Truncates this title so it has a maximum number of words. Spaces are used
     * to determine words, thus two spaces in a title will cause two words to be
     * mistakenly identified.
     *
     * @param noWords
     *            the number of words to show
     * @return a reference to the called object (itself).
     */
    public TitleBuffer truncate(final int noWords) {
        if (noWords < 1) {
            throw new IllegalArgumentException("Truncation must be to one or more words");
        }
        int pos = 0;
        int spaces = 0;

        while (pos < title.length() && spaces < noWords) {
            if (title.charAt(pos) == ' ') {
                spaces++;
            }
            pos++;
        }
        if (pos < title.length()) {
            title.setLength(pos - 1); // string.delete(pos - 1,
            // string.length());
            title.append("...");
        }
        return this;
    }

}
