/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import {CausewayGraphQLClientElement} from './graphql-client-element.mjs';
import {CausewayObjectContextElement} from './object-context-element.mjs';
import {CausewayObjectHeaderElement} from './object-header-element.mjs';
import {CausewayPropertyElement} from './property-element.mjs';

const DEFINITIONS = Object.freeze([
  ['causeway-graphql-client', CausewayGraphQLClientElement],
  ['causeway-object-context', CausewayObjectContextElement],
  ['causeway-object-header', CausewayObjectHeaderElement],
  ['causeway-property', CausewayPropertyElement]
]);

export function defineCausewayWebComponents(registry = globalThis.customElements) {
  if (!registry) {
    return;
  }
  for (const [name, constructor] of DEFINITIONS) {
    if (!registry.get(name)) {
      registry.define(name, constructor);
    }
  }
}
