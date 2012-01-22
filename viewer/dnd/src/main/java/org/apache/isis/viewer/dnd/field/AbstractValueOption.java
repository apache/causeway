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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public abstract class AbstractValueOption extends UserActionAbstract {
    protected final AbstractField field;

    AbstractValueOption(final AbstractField field, final String name) {
        super(name);
        this.field = field;
    }

    protected ObjectAdapter getValue(final View view) {
        final TextParseableContent vc = (TextParseableContent) view.getContent();
        final ObjectAdapter value = vc.getAdapter();
        return value;
    }

    protected void updateParent(final View view) {
        // have commented this out because it isn't needed; the transaction
        // manager will do this
        // for us on endTransaction. Still, if I'm wrong and it is needed,
        // hopefully this
        // comment will help...
        // IsisContext.getObjectPersistor().objectChangedAllDirty();

        view.markDamaged();
        view.getParent().invalidateContent();
    }

    protected boolean isEmpty(final View view) {
        return field.isEmpty();
    }
}
