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

package org.apache.isis.viewer.dnd.form;

import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.composite.StandardFields;

public abstract class AbstractFormSpecification extends AbstractObjectViewSpecification {

    @Override
    protected ViewFactory createFieldFactory() {
        return new StandardFields() {
            @Override
            protected int collectionRequirement() {
                return AbstractFormSpecification.this.collectionRequirement();
            }

            @Override
            protected boolean include(final Content content, final int sequence) {
                return AbstractFormSpecification.this.include(content, sequence);
            }
        };
    }

    protected int collectionRequirement() {
        return ViewRequirement.OPEN | ViewRequirement.SUBVIEW;
    }

    protected boolean include(final Content content, final int sequence) {
        return true;
    }

}
