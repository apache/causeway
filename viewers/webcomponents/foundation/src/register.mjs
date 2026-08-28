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
import {CAUSEWAY_ACTION_CONTROL, CausewayActionControlElement} from './action-widget.mjs';
import {CausewayCollectionColumnElement} from './collection-column-element.mjs';
import {captureDeclarativeCollectionColumns, CausewayCollectionElement} from './collection-element.mjs';
import {CausewayElementName} from './component-contracts.mjs';
import {CAUSEWAY_FIELD_EDITOR, CausewayFieldEditorElement} from './field-widget.mjs';
import {CausewayGraphQLClientElement} from './graphql-client-element.mjs';
import {CAUSEWAY_COLLECTION_GRID, CausewayCollectionGridElement} from './grid-widget.mjs';
import {CausewayInteractionControllerElement} from './interaction-controller-element.mjs';
import {CAUSEWAY_MENUBAR_CONTROL, CausewayMenubarControlElement} from './menubar-widget.mjs';
import {
  CausewayMenubarPrimaryElement,
  CausewayMenubarSecondaryElement,
  CausewayMenubarTertiaryElement
} from './menubar-element.mjs';
import {CausewayMenubarsElement} from './menubars-element.mjs';
import {CausewayObjectContextElement} from './object-context-element.mjs';
import {CausewayObjectElement} from './object-element.mjs';
import {CausewayObjectHeaderElement} from './object-header-element.mjs';
import {CausewayObjectLinkElement} from './object-link-element.mjs';
import {CausewayPropertyElement} from './property-element.mjs';
import {CausewayReferenceEditorElement} from './reference-widget.mjs';
import {CausewayValueElement} from './value-element.mjs';

const DEFINITIONS = Object.freeze([
  [CausewayElementName.GRAPHQL_CLIENT, CausewayGraphQLClientElement],
  [CausewayElementName.OBJECT_CONTEXT, CausewayObjectContextElement],
  [CausewayElementName.OBJECT, CausewayObjectElement],
  [CausewayElementName.OBJECT_HEADER, CausewayObjectHeaderElement],
  [CausewayElementName.PROPERTY, CausewayPropertyElement],
  [CausewayElementName.VALUE, CausewayValueElement],
  [CausewayElementName.OBJECT_LINK, CausewayObjectLinkElement],
  [CausewayElementName.ACTION, CausewayActionElement],
  [CAUSEWAY_ACTION_CONTROL, CausewayActionControlElement],
  [CausewayElementName.INTERACTION_CONTROLLER, CausewayInteractionControllerElement],
  [CAUSEWAY_FIELD_EDITOR, CausewayFieldEditorElement],
  [CausewayElementName.REFERENCE_EDITOR, CausewayReferenceEditorElement],
  [CausewayElementName.MENUBARS, CausewayMenubarsElement],
  [CausewayElementName.MENUBAR_PRIMARY, CausewayMenubarPrimaryElement],
  [CausewayElementName.MENUBAR_SECONDARY, CausewayMenubarSecondaryElement],
  [CausewayElementName.MENUBAR_TERTIARY, CausewayMenubarTertiaryElement],
  [CAUSEWAY_MENUBAR_CONTROL, CausewayMenubarControlElement],
  [CausewayElementName.COLLECTION_COLUMN, CausewayCollectionColumnElement],
  [CausewayElementName.COLLECTION, CausewayCollectionElement],
  [CAUSEWAY_COLLECTION_GRID, CausewayCollectionGridElement]
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
