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

import {
  createSemanticEvent,
  OBJECT_CONTEXT_REQUEST_EVENT,
  OBJECT_CONTEXT_STATE_EVENT,
  requestGraphQLClient
} from './context-events.mjs';
import {ObjectContextController} from './object-context-controller.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayObjectContextElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['logical-type', 'object-id'];
  }

  constructor() {
    super();
    this._client = null;
    this._injectedContext = null;
    this._hydration = null;
    this._context = null;
    this._unsubscribe = null;
    this.addEventListener(OBJECT_CONTEXT_REQUEST_EVENT, event => {
      if (this._context && event.detail?.provide) {
        event.detail.provide(this._context);
        event.stopPropagation();
      }
    });
  }

  get logicalTypeName() {
    return this.getAttribute('logical-type') || '';
  }

  set logicalTypeName(value) {
    this.setAttribute('logical-type', value);
  }

  get objectId() {
    return this.getAttribute('object-id') || '';
  }

  set objectId(value) {
    this.setAttribute('object-id', value);
  }

  get client() {
    return this._client;
  }

  set client(value) {
    this._client = value;
    if (this.isConnected) {
      this.#start();
    }
  }

  get context() {
    return this._context;
  }

  set context(value) {
    this._injectedContext = value;
    if (this.isConnected) {
      this.#start();
    }
  }

  get hydration() {
    return this._hydration;
  }

  set hydration(value) {
    this._hydration = value;
    if (this.isConnected && !this._injectedContext) {
      this.#start();
    }
  }

  get state() {
    return this._context?.state ?? null;
  }

  connectedCallback() {
    this.#start();
  }

  disconnectedCallback() {
    this.#stop();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue && this.isConnected) {
      this.#start();
    }
  }

  #start() {
    this.#stop();
    if (this._injectedContext) {
      this._context = this._injectedContext;
    } else {
      const client = this._client ?? requestGraphQLClient(this);
      this._context = new ObjectContextController({
        client,
        logicalTypeName: this.logicalTypeName,
        objectId: this.objectId,
        hydration: this._hydration
      });
    }
    this._unsubscribe = this._context.subscribe?.(state => {
      this.dispatchEvent(createSemanticEvent(
        OBJECT_CONTEXT_STATE_EVENT,
        {state, context: this._context},
        {bubbles: true, composed: true}
      ));
    }) ?? null;
  }

  #stop() {
    this._unsubscribe?.();
    this._unsubscribe = null;
    if (this._context && this._context !== this._injectedContext) {
      this._context.disconnect?.();
    }
    this._context = null;
  }
}
