/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.select2;

import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Select2Choice;
import org.wicketstuff.select2.Select2MultiChoice;
import org.wicketstuff.select2.Settings;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.metamodel.spec.ObjectSpecId;

/**
 * Represents functionality that is common to both {@link Select2Choice} and {@link Select2MultiChoice}, but for
 * which there is no suitable common supertype.
 *
 * Also holds extensions, notable {@link #getSpecId()}.
 */
public interface ChoiceExt {
    void setProvider(final ChoiceProvider<ObjectAdapterMemento> providerForChoices);
    Settings getSettings();

    ObjectSpecId getSpecId();
}
