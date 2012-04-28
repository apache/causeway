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

package org.apache.isis.viewer.dnd.view;

/**
 * Describes a view, and how it is built.
 */
public interface ViewSpecification extends ViewFactory {

    String getName();

    /**
     * Determines if the view created to this specification can display the
     * specified type. Returns true if it can.
     */
    boolean canDisplay(ViewRequirement requirement);

    /**
     * Indicates whether views to this specification are open - displaying the
     * attributes of the content object - or are closed - display only the title
     * of the content object.
     */
    boolean isOpen();

    /**
     * Indicates whether this view can be replaced with another view (for the
     * same value or reference).
     * 
     * @return true if it can be replaced by another view; false if it can't be
     *         replaces
     */
    boolean isReplaceable();

    boolean isSubView();

    /**
     * Return true if the generated views are to have their sizes adjusted so
     * they are consistent with surrounding views.
     */
    // TODO rename
    boolean isAligned();

    /**
     * Indicates if this view can handled being resized. If it can't then the
     * viewer can put it in a scroll border.
     */
    boolean isResizeable();
}
