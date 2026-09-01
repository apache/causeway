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

export * from './action-element.mjs';
export * from './action-widget.mjs';
export * from './breadcrumbs-element.mjs';
export * from './collection-column-element.mjs';
export * from './collection-element.mjs';
export * from './collection-grid-projection.mjs';
export * from './collection-grid-qualification.mjs';
export * from './collection-range-broker.mjs';
export * from './component-contracts.mjs';
export * from './component-styles.mjs';
export * from './context-consumer-element.mjs';
export * from './context-events.mjs';
export * from './editor-registry.mjs';
export * from './field-widget.mjs';
export * from './graphql-client.mjs';
export * from './graphql-client-element.mjs';
export * from './graphql-executor.mjs';
export * from './grid-widget.mjs';
export * from './interaction-controller-element.mjs';
export * from './interaction-operations.mjs';
export * from './introspection.mjs';
export * from './menu-context-controller.mjs';
export * from './menu-layout.mjs';
export * from './menubar-element.mjs';
export * from './menubar-projection.mjs';
export * from './menubar-qualification.mjs';
export * from './menubar-widget.mjs';
export * from './menubars-element.mjs';
export * from './object-context-controller.mjs';
export * from './object-context-element.mjs';
export * from './object-element.mjs';
export * from './object-header-element.mjs';
export * from './object-layout.mjs';
export * from './object-link-element.mjs';
export * from './parameter-element.mjs';
export * from './property-element.mjs';
export * from './reference-widget.mjs';
export * from './register.mjs';
export * from './schema-names.mjs';
export * from './selection.mjs';
export * from './service-action-context.mjs';
export * from './structural-resource.mjs';
export * from './structural-xml.mjs';
export * from './temporal-range.mjs';
export * from './types.mjs';
export * from './value-codecs.mjs';
export * from './value-element.mjs';
export * from './value-renderers.mjs';

import {defineCausewayWebComponents} from './register.mjs';

defineCausewayWebComponents();
