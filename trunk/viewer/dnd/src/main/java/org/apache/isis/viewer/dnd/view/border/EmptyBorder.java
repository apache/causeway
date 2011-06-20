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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

public class EmptyBorder extends AbstractBorder {

    public EmptyBorder(final int width, final View view) {
        super(view);
        left = top = right = bottom = width;
    }

    public EmptyBorder(final int topBottom, final int leftRight, final View view) {
        super(view);
        left = right = bottom = leftRight;
        top = bottom = topBottom;
    }

    public EmptyBorder(final int left, final int top, final int right, final int bottom, final View view) {
        super(view);
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

}
