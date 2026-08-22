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

/**
 * Stable public custom-element names for the framework-neutral Causeway component vocabulary.
 */
export const CausewayElementName = Object.freeze({
  GRAPHQL_CLIENT: 'causeway-graphql-client',
  OBJECT_CONTEXT: 'causeway-object-context',
  OBJECT: 'causeway-object',
  OBJECT_HEADER: 'causeway-object-header',
  PROPERTY: 'causeway-property',
  VALUE: 'causeway-value',
  OBJECT_LINK: 'causeway-object-link',
  ACTION: 'causeway-action',
  INTERACTION_CONTROLLER: 'causeway-interaction-controller',
  REFERENCE_EDITOR: 'causeway-reference-editor',
  COLLECTION: 'causeway-collection',
  COLLECTION_COLUMN: 'causeway-collection-column',
  MENUBARS: 'causeway-menubars',
  MENUBAR_PRIMARY: 'causeway-menubar-primary',
  MENUBAR_SECONDARY: 'causeway-menubar-secondary',
  MENUBAR_TERTIARY: 'causeway-menubar-tertiary'
});

/**
 * Stable semantic host classes emitted by public components.
 */
export const CausewayHostClass = Object.freeze({
  OBJECT: 'causeway-object',
  OBJECT_HEADER: 'causeway-object-header',
  PROPERTY: 'causeway-property',
  VALUE: 'causeway-value',
  OBJECT_LINK: 'causeway-object-link',
  ACTION: 'causeway-action',
  INTERACTION_CONTROLLER: 'causeway-interaction-controller',
  ACTION_PROMPT: 'causeway-action-prompt',
  ACTION_RESULT: 'causeway-action-result',
  PROPERTY_EDITOR: 'causeway-property-editor',
  COLLECTION: 'causeway-collection',
  COLLECTION_COLUMN: 'causeway-collection-column',
  MENUBARS: 'causeway-menubars',
  MENUBAR: 'causeway-menubar',
  MENU: 'causeway-menu',
  MENU_SECTION: 'causeway-menu-section',
  SERVICE_ACTION: 'causeway-service-action',
  LOADING: 'causeway-loading',
  DISABLED: 'causeway-disabled',
  EMPTY: 'causeway-empty',
  ERROR: 'causeway-error',
  UNSUPPORTED: 'causeway-unsupported'
});

/**
 * Stable attributes shared by semantic member components.
 */
export const CausewayAttribute = Object.freeze({
  MEMBER: 'member',
  LABEL: 'label',
  ACTIVE: 'active',
  LOGICAL_TYPE: 'logical-type',
  OBJECT_ID: 'object-id',
  TITLE: 'title',
  DISABLED: 'disabled',
  EDITABLE: 'editable',
  BAR: 'bar'
});

/**
 * Structured event payload contracts.
 *
 * Navigation detail: `{target: {logicalTypeName, id, title}, sourceContext}`.
 * Action detail: `{actionId, identity, context}`.
 */
export const CausewaySemanticEvent = Object.freeze({
  NAVIGATION_REQUEST: 'causeway-navigation-request',
  ACTION_REQUEST: 'causeway-action-request',
  ACTION_PROMPT_STATE: 'causeway-action-prompt-state-change',
  ACTION_RESULT: 'causeway-action-result',
  PROPERTY_INTERACTION_STATE: 'causeway-property-interaction-state-change',
  PROPERTY_UPDATED: 'causeway-property-updated',
  COLLECTION_STATE: 'causeway-collection-state-change',
  COLLECTION_CONFIGURATION: 'causeway-collection-configuration-change',
  OBJECT_LAYOUT_STATE: 'causeway-object-layout-state-change',
  OBJECT_LAYOUT_DIAGNOSTIC: 'causeway-object-layout-diagnostic',
  MENU_BARS_STATE: 'causeway-menubars-state-change',
  MENU_BARS_DIAGNOSTIC: 'causeway-menubars-diagnostic'
});
