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
package org.apache.isis.viewer.bdd.common;

public class CellBindingDefault extends CellBinding {

    public static class Builder {
        private final String name;
        private final String[] headText;
        private boolean autoCreate;
        private boolean ditto;
        private boolean optional;

        public Builder(final String name, final String... headText) {
            this.name = name;
            this.headText = headText;
        }

        public Builder autoCreate() {
            this.autoCreate = true;
            return this;
        }

        public Builder ditto() {
            this.ditto = true;
            return this;
        }

        public Builder optional() {
            this.optional = true;
            return this;
        }

        public CellBindingDefault build() {
            return new CellBindingDefault(name, autoCreate, ditto, optional, headText);
        }
    }

    public static Builder builder(final String name, final String... headText) {
        return new Builder(name, headText);
    }

    private CellBindingDefault(final String name, final boolean autoCreate, final boolean ditto, final boolean optional, final String[] headTexts) {
        super(name, autoCreate, ditto, optional, headTexts);
    }

    @Override
    protected void copy(final ScenarioCell from, final ScenarioCell to) {
        to.setText(from.getText());
    }

}
