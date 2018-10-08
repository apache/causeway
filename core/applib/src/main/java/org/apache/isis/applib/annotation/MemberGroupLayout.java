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
import java.util.List;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Specifies the positioning of an entity's (groups of) properties and of its collections, on a page, column by column.
 * 
 * <p>
 * The left column and middle column determine the ordering of the entity's (groups of) properties.  The
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
 *
 * <p>
 *     The recommended alternative is to use the <code>Xxx.layout.xml</code> file, where <code>Xxx</code> is the domain object name.
 * </p>
 *
 * @deprecated
 */
@Deprecated
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberGroupLayout {

    /**
     * The relative widths of the columns of members.
     */
    public static class ColumnSpans {
        
        private final int left;
        private final int middle;
        private final int right;
        private final int collections;
        
        public static ColumnSpans valueOf(String str) {
            final Iterable<String> split = Splitter.on(",").split(str);
            try {
                final List<Integer> list = Lists.newArrayList(Iterables.transform(split, new Function<String,Integer>() {
                    @Override
                    public Integer apply(String input) {
                        return Integer.parseInt(input);
                    }
                }));
                return asSpans(list);
            } catch(RuntimeException ex) {
                return null;
            }
        }
        public static ColumnSpans asSpans(int... columnSpans) {
            List<Integer> list = Lists.<Integer>newArrayList();
            for (int i : columnSpans) {
                list.add(i);
            }
            return asSpans(list);
        }
        private static ColumnSpans asSpans(List<Integer> list) {
            return new ColumnSpans(list);
        }
        private ColumnSpans(List<Integer> list) {
            this.left = getElse(list,0,4);
            this.middle = getElse(list,1,0);
            this.right = getElse(list,2,0);
            this.collections = getElse(list,3,8);
        }
        private static int getElse(List<Integer> list, int i, int dflt) {
            return list != null && list.size() > i? list.get(i): dflt;
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
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + collections;
            result = prime * result + left;
            result = prime * result + middle;
            result = prime * result + right;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ColumnSpans other = (ColumnSpans) obj;
            if (collections != other.collections)
                return false;
            if (left != other.left)
                return false;
            if (middle != other.middle)
                return false;
            if (right != other.right)
                return false;
            return true;
        }
        public String name() {
            return String.format("[%d,%d,%d,%d]", left, middle, right, collections);
        }
        public boolean exceedsRow() {
            return getLeft() + getMiddle() + getRight() + getCollections() > 12;
        }

        public String toString() {
            return name();
        }
    }

    /**
     * Specify the spans of each of the columns.
     * 
     * <p>
     * The sum of the spans is always 12.
     */
    int[] columnSpans() default {4,0,0,8};

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
     * If the value of this attribute is non-empty but the {@link #columnSpans()} specifies a zero size, then the
     * framework will not boot and will instead indicate a meta-model validation exception.
     */
    String[] middle() default {};

    /**
     * As {@link #right()}, but for the right column in a page.
     * 
     * <p>
     * If the value of this attribute is non-empty but the {@link #columnSpans()} specifies a zero size, then the
     * framework will not boot and will instead indicate a meta-model validation exception.
     */
    String[] right() default {};

}
