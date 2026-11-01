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

import type {ObjectRouteIdentity} from './route-codec';

export const ROUTE_CONTEXT_SELECTOR = 'cw-object-context[data-causeway-route-context]';
export const ROUTE_INTERACTIONS_SELECTOR = 'cw-interaction-controller[data-causeway-route-interactions]';

export interface BoundaryValidation {
  readonly valid: boolean;
  readonly classification?: 'missing-context' | 'duplicate-context' | 'identity' | 'interactions';
  readonly context?: HTMLElement;
}

export function validateRouteBoundary(root: ParentNode, identity: ObjectRouteIdentity): BoundaryValidation {
  const contexts = [...root.querySelectorAll<HTMLElement>(ROUTE_CONTEXT_SELECTOR)]
    .filter(context => !context.parentElement?.closest(ROUTE_CONTEXT_SELECTOR));
  if (contexts.length === 0) return Object.freeze({valid: false, classification: 'missing-context'});
  if (contexts.length !== 1) return Object.freeze({valid: false, classification: 'duplicate-context'});
  const context = contexts[0];
  if (context.getAttribute('logical-type') !== identity.logicalTypeName
      || context.getAttribute('object-id') !== identity.objectId) {
    return Object.freeze({valid: false, classification: 'identity'});
  }
  const controllers = [...context.querySelectorAll<HTMLElement>(ROUTE_INTERACTIONS_SELECTOR)]
    .filter(controller => controller.closest(ROUTE_CONTEXT_SELECTOR) === context);
  if (controllers.length !== 1) return Object.freeze({valid: false, classification: 'interactions'});
  return Object.freeze({valid: true, context});
}

export interface ShellLandmarks {
  readonly shell: HTMLElement;
  readonly client: HTMLElement;
  readonly route: HTMLElement;
  readonly loading: HTMLElement;
  readonly announcement: HTMLElement;
  readonly result: HTMLElement;
}

function exactlyOne(root: ParentNode, selector: string): HTMLElement | null {
  const values = root.querySelectorAll<HTMLElement>(selector);
  return values.length === 1 ? values[0] : null;
}

export function validateShellBoundary(shell: HTMLElement): ShellLandmarks {
  const client = exactlyOne(shell, 'cw-graphql-client[data-causeway-shell-client]');
  const route = exactlyOne(shell, '[data-causeway-router-view]');
  const loading = exactlyOne(shell, '[data-causeway-route-loading]');
  const announcement = exactlyOne(shell, '[data-causeway-route-announcement]');
  const result = exactlyOne(shell, 'cw-action-results[data-causeway-shell-result]');
  if (!client || !route || !loading || !announcement || !result
      || !client.contains(route) || !client.contains(result)) {
    throw new Error('The authored Vue application shell is invalid.');
  }
  return Object.freeze({shell, client, route, loading, announcement, result});
}
