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

    public enum ColumnSpans {
        _2_0_10(2,0,10),
        _3_0_9(3,0,9),
        _4_0_8(4,0,8),
        _5_0_7(5,0,7),
        _6_0_6(6,0,6),
        _3_3_6(3,3,6),
        _2_3_7(2,3,7),
        _2_4_6(2,4,6),
        _4_4_4(4,4,4);
        
        private int left;
        private int middle;
        private int right;

        private ColumnSpans(int left, int middle, int right) {
            this.left = left;
            this.middle = middle;
            this.right = right;
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
        
    }

    /**
     * Specify the spans of each of the <i>left</i>_<i>middle</i>_<i>right</i> columns.
     * 
     * <p>
     * The sum of the spans is always 12.
     */
    ColumnSpans columnSpans() default ColumnSpans._4_0_8;

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
     * (eg {@link ColumnSpans#_2_0_10}, then the framework will not boot and will instead indicate 
     * a meta-model validation exception. 
     */
    String[] middle() default {};

}
