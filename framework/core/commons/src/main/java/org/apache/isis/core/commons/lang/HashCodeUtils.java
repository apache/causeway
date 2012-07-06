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

package org.apache.isis.core.commons.lang;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>,
 * based on Josh Bloch's Effective Java.
 * 
 * <p>
 * Example use case:
 * 
 * <pre>
 * public int hashCode() {
 *     int result = HashCodeUtil.SEED;
 *     // collect the contributions of various fields
 *     result = HashCodeUtil.hash(result, fPrimitive);
 *     result = HashCodeUtil.hash(result, fObject);
 *     result = HashCodeUtil.hash(result, fArray);
 *     return result;
 * }
 * </pre>
 * 
 * @see http://www.javapractices.com/Topic28.cjp
 */
public final class HashCodeUtils {

    private HashCodeUtils() {
    }

    /**
     * An initial value for a <code>hashCode</code>, to which is added
     * contributions from fields. Using a non-zero value decreases collisons of
     * <code>hashCode</code> values.
     */
    public static final int SEED = 23;

    /**
     * booleans.
     */
    public static int hash(final int aSeed, final boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    /**
     * chars.
     */
    public static int hash(final int aSeed, final char aChar) {
        return firstTerm(aSeed) + aChar;
    }

    /**
     * ints.
     * 
     * <p>
     * Note that byte and short are handled by this method, through implicit
     * conversion.
     */
    public static int hash(final int aSeed, final int aInt) {
        return firstTerm(aSeed) + aInt;
    }

    /**
     * longs.
     */
    public static int hash(final int aSeed, final long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    /**
     * floats.
     */
    public static int hash(final int aSeed, final float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /**
     * doubles.
     */
    public static int hash(final int aSeed, final double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an
     * array.
     * 
     * If <code>aObject</code> is an array, then each element may be a primitive
     * or a possibly-null object.
     */
    public static int hash(final int aSeed, final Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            final int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                final Object item = Array.get(aObject, idx);
                // recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }

    private static final int ODD_PRIME_NUMBER = 37;

    private static int firstTerm(final int aSeed) {
        return ODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(final Object aObject) {
        return aObject.getClass().isArray();
    }
}
