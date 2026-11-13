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

import {GRAPHQL_CLIENT_REQUEST_EVENT} from './context-events.mjs';
import {CausewayGraphQLClient} from './graphql-client.mjs';
import {createFetchGraphQLExecutor} from './graphql-executor.mjs';
import {RichSchemaNames} from './schema-names.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayGraphQLClientElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['endpoint', 'rich-root-field', 'lookup-argument-name', 'object-field-prefix', 'object-field-suffix'];
  }

  constructor() {
    super();
    this._executor = null;
    this._client = null;
    this.addEventListener(GRAPHQL_CLIENT_REQUEST_EVENT, event => {
      if (event.detail?.provide) {
        event.detail.provide(this.client);
        event.stopPropagation();
      }
    });
  }

  get endpoint() {
    return this.getAttribute('endpoint') || '/graphql';
  }

  set endpoint(value) {
    this.setAttribute('endpoint', value);
  }

  get executor() {
    return this._executor;
  }

  set executor(value) {
    this._executor = value;
    this._client = null;
  }

  get client() {
    if (!this._client) {
      const schemaNames = new RichSchemaNames({
        richRootField: this.hasAttribute('rich-root-field') ? this.getAttribute('rich-root-field') : 'rich',
        lookupArgumentName: this.getAttribute('lookup-argument-name') || 'object',
        objectFieldPrefix: this.getAttribute('object-field-prefix') || '',
        objectFieldSuffix: this.getAttribute('object-field-suffix') || ''
      });
      this._client = new CausewayGraphQLClient({
        executor: this._executor ?? createFetchGraphQLExecutor({endpoint: this.endpoint}),
        schemaNames
      });
    }
    return this._client;
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue) {
      this._client = null;
    }
  }
}
