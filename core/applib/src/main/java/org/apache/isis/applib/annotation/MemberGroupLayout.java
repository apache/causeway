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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A successor to {@link MemberGroups}, specifying the (groups of) members in a page,
 * column by column. 
 * 
 * <p>
 * The left column and middle column determine the ordering of the entity's properties.  The
 * value of the {@link #left() left} list and {@link #middle() middle} list specify the order
 * of the property groups (inferred from each property's {@link MemberOrder#name() MemberOrder.name} attribute.
 * 
 * <p>
 * The right column is for the entity's collections.  The order of this collections is simply as
 * determined by the collection's {@link MemberOrder#sequence() MemberOrder.sequence} attribute
 * 
 * <p>
 * If both this annotation, {@link MemberGroupLayout}, and {@link MemberGroups} annotation
 * are present on an entity, then this one takes precedence.
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberGroupLayout {

    /**
     * The relative widths of the columns of members.
     * 
     * <p>
     * Each value of this enum is in the form <tt>_X_Y_Z_W</tt>.  The 
     * <tt>X</tt>, <tt>Y</tt> and <tt>Z</tt>
     * indicate the relative widths of (up to) three property columns,
     * while <tt>W</tt> indicates the relative width of the collection column.
     */
    public enum ColumnSpans {
        // two column, with collections
        _2_0_0_10,
        _3_0_0_9,
        _4_0_0_8,
        _5_0_0_7,
        _6_0_0_6,
        
        // three column, with collections
        _2_2_0_8,
        _2_3_0_7,
        _2_4_0_6,
        _2_5_0_5,
        _2_6_0_4,
        
        _3_3_0_6,
        _3_4_0_5,
        _3_5_0_4,
        _3_6_0_3,
        
        _4_4_0_4,
        
        // two column, suppress collections
        _2_0_10_0,
        _3_0_9_0,
        _4_0_8_0,
        _5_0_7_0,
        _6_0_6_0,
        
        // three column, suppress collections
        _2_2_8_0,
        _2_3_7_0,
        _2_4_6_0,
        _2_5_5_0,
        _2_6_4_0,
        
        _3_3_6_0,
        _3_4_5_0,
        _3_5_4_0,
        _3_6_3_0,
        
        _4_4_4_0,
        _4_5_3_0,
        _4_6_2_0,
        ;
        
        private final int left;
        private final int middle;
        private final int right;
        private final int collections;
                
        private ColumnSpans() {
            final Pattern namePattern = Pattern.compile("^_(\\d+)_(\\d+)_(\\d+)_(\\d+)$");
            final String name = name();
            Matcher matcher = namePattern.matcher(name);
            if(!matcher.matches()) {
                // call to matches is required; Matcher is a state machine
                throw new IllegalArgumentException("enum constant's name must match " + namePattern.pattern());
            } 
            
            this.left = parseGroup(matcher, 1);
            this.middle = parseGroup(matcher, 2);
            this.right = parseGroup(matcher, 3);
            this.collections = parseGroup(matcher, 4);
        }

        private static int parseGroup(Matcher matcher, final int group) {
            return Integer.parseInt(matcher.group(group));
        }

        public int getLeft() {
            return left;
        }
        public int getMiddle() {
            return middle;
        }
        public int getRight() {
            return right;
        }
        public int getCollections() {
            return collections;
        }
        
    }

    /**
     * Specify the spans of each of the columns.
     * 
     * <p>
     * The sum of the spans is always 12.
     */
    ColumnSpans columnSpans() default ColumnSpans._4_0_0_8;

    /**
     * Order of groups of properties as they appear in the left-most column of a webpage,
     * grouped as they appear as the <tt>name</tt> attribute of the {@link MemberOrder} 
     * annotation.
     * 
     * <p>
     * The order in this list determines the order that the property groups will be rendered.  
     * By convention any {@link MemberOrder} that does not have a {@link MemberOrder#name() name} is considered
     * to be in the default group, whose name is hard-coded as <i>General</i>.
     * 
     * <p>
     * Equivalent to {@link MemberGroups#value()} annotation.
     */
    String[] left() default {};

    /**
     * As {@link #left()}, but for the middle column in a page.
     * 
     * <p>
     * If the value of this attribute is non-empty but the {@link #columnSpans()} specifies a zero size
     * (eg {@link ColumnSpans#_2_0_0_10}, then the framework will not boot and will instead indicate 
     * a meta-model validation exception. 
     */
    String[] middle() default {};

    /**
     * As {@link #right()}, but for the right column in a page.
     * 
     * <p>
     * If the value of this attribute is non-empty but the {@link #columnSpans()} specifies a zero size
     * (eg {@link ColumnSpans#_2_0_0_10}, then the framework will not boot and will instead indicate 
     * a meta-model validation exception.
     */
    String[] right() default {};

}
