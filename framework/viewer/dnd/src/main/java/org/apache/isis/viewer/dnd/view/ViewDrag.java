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

import org.apache.isis.viewer.dnd.drawing.Location;

/**
 * Details a drag event that affects a view. The target of a ViewDrag is always
 * the workspace of the source view.
 * 
 * <p>
 * An overlay view, as returned by the pickup() method on the source view, is
 * moved by this drag objects so its location follows the pointer by an offset
 * equivalent to the mouse location within the view.
 */
public interface ViewDrag extends Drag {

    View getSourceView();

    Location getLocation();

    Location getViewDropLocation();

    void subtract(int left, int top);

}
