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

export const GRAPHQL_CLIENT_REQUEST_EVENT = 'causeway-graphql-client-request';
export const OBJECT_CONTEXT_REQUEST_EVENT = 'causeway-object-context-request';
export const OBJECT_CONTEXT_STATE_EVENT = 'causeway-object-context-state-change';
export const COMPONENT_STATE_EVENT = 'causeway-component-state-change';

export function requestGraphQLClient(requester) {
  let client = null;
  requester.dispatchEvent(createSemanticEvent(GRAPHQL_CLIENT_REQUEST_EVENT, {
    provide(candidate) {
      client ??= candidate;
    }
  }));
  return client;
}

export function requestObjectContext(requester) {
  let context = null;
  requester.dispatchEvent(createSemanticEvent(OBJECT_CONTEXT_REQUEST_EVENT, {
    provide(candidate) {
      context ??= candidate;
    }
  }));
  return context;
}

export function createSemanticEvent(type, detail, {bubbles = true, composed = true, cancelable = false} = {}) {
  if (typeof globalThis.CustomEvent === 'function') {
    return new CustomEvent(type, {detail, bubbles, composed, cancelable});
  }
  const event = new Event(type, {bubbles, cancelable});
  Object.defineProperties(event, {
    detail: {value: detail, enumerable: true},
    composed: {value: composed, enumerable: true}
  });
  return event;
}
