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

import {COMPONENT_STATE_EVENT, createSemanticEvent, requestObjectContext} from './context-events.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayContextConsumerElement extends HTMLElementBase {
  constructor() {
    super();
    this._injectedContext = null;
    this._resolvedContext = null;
    this._release = null;
    this.componentState = null;
  }

  get context() {
    return this._injectedContext;
  }

  set context(value) {
    this._injectedContext = value;
    if (this.isConnected) {
      this.reconnectRequirement();
    }
  }

  connectedCallback() {
    this.reconnectRequirement();
  }

  disconnectedCallback() {
    this.releaseRequirement();
  }

  reconnectRequirement() {
    this.releaseRequirement();
    this._resolvedContext = this._injectedContext ?? requestObjectContext(this);
    if (!this._resolvedContext) {
      this.acceptComponentState(Object.freeze({
        status: 'terminal-error',
        errors: Object.freeze([{message: 'No Causeway object context is available.', path: [], extensions: {}}]),
        generation: 0,
        data: null,
        descriptor: null
      }));
      return;
    }
    this._release = this._resolvedContext.registerRequirement(
      this.createRequirement(),
      state => this.acceptComponentState(state)
    );
  }

  releaseRequirement() {
    this._release?.();
    this._release = null;
    this._resolvedContext = null;
  }

  createRequirement() {
    throw new Error('Causeway context consumer must define createRequirement().');
  }

  acceptComponentState(state) {
    this.componentState = state;
    this.renderComponentState(state);
    this.dispatchEvent(createSemanticEvent(COMPONENT_STATE_EVENT, {
      element: this,
      member: state?.requirement?.member ?? null,
      state
    }));
  }

  renderComponentState() {}
}
