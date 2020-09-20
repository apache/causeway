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

package org.apache.isis.legacy.applib.value;

import org.apache.isis.applib.annotation.Value;

/**
 * Color is simple numerical representation of a color using the brightness of
 * red, green and blue (RGB) components.
 *
 * <p>
 * Where there is no basic colors (RGB all equal 0) then you get black; where
 * each color is at maximum (RGB all equal 255) you get white.
 *
 * @deprecated
 */
@Deprecated
@Value(semanticsProviderName = "org.apache.isis.core.metamodel.facets.value.color.ColorValueSemanticsProvider")
public class Color extends Magnitude<Color> {

    private static final long serialVersionUID = 1L;

    public static final Color WHITE = new Color(0xffffff);
    public static final Color BLACK = new Color(0);

    private final int color;

    public Color(final int color) {
        this.color = color;
    }

    public int intValue() {
        return color;
    }

    /**
     * returns true if the number of this object has the same value as the
     * specified number
     */
    @Override
    public boolean isEqualTo(final Color number) {
        return (number).color == color;
    }

    /**
     * Returns true if this value is less than the specified value.
     */
    @Override
    public boolean isLessThan(final Color value) {
        return color < (value).color;
    }

    public String title() {
        if (color == 0) {
            return "Black";
        } else if (color == 0xffffff) {
            return "White";
        } else {
            return "#" + Integer.toHexString(color).toUpperCase();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Color other = (Color) obj;
        if (color != other.color) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + color;
        return result;
    }

    @Override
    public String toString() {
        return "Color: #" + Integer.toHexString(color).toUpperCase();
    }
}
