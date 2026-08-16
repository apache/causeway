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

import {CausewayActionElement} from './action-element.mjs';
import {CausewayCollectionColumnElement} from './collection-column-element.mjs';
import {captureDeclarativeCollectionColumns, CausewayCollectionElement} from './collection-element.mjs';
import {CausewayElementName} from './component-contracts.mjs';
import {CausewayGraphQLClientElement} from './graphql-client-element.mjs';
import {CausewayInteractionControllerElement} from './interaction-controller-element.mjs';
import {CausewayObjectContextElement} from './object-context-element.mjs';
import {CausewayObjectHeaderElement} from './object-header-element.mjs';
import {CausewayObjectLinkElement} from './object-link-element.mjs';
import {CausewayPropertyElement} from './property-element.mjs';
import {CausewayValueElement} from './value-element.mjs';

const DEFINITIONS = Object.freeze([
  [CausewayElementName.GRAPHQL_CLIENT, CausewayGraphQLClientElement],
  [CausewayElementName.OBJECT_CONTEXT, CausewayObjectContextElement],
  [CausewayElementName.OBJECT_HEADER, CausewayObjectHeaderElement],
  [CausewayElementName.PROPERTY, CausewayPropertyElement],
  [CausewayElementName.VALUE, CausewayValueElement],
  [CausewayElementName.OBJECT_LINK, CausewayObjectLinkElement],
  [CausewayElementName.ACTION, CausewayActionElement],
  [CausewayElementName.INTERACTION_CONTROLLER, CausewayInteractionControllerElement],
  [CausewayElementName.COLLECTION_COLUMN, CausewayCollectionColumnElement],
  [CausewayElementName.COLLECTION, CausewayCollectionElement]
]);

export function defineCausewayWebComponents(registry = globalThis.customElements) {
  if (!registry) {
    return;
  }
  captureDeclarativeCollectionColumns();
  for (const [name, constructor] of DEFINITIONS) {
    if (!registry.get(name)) {
      registry.define(name, constructor);
    }
  }
}
