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

import {CausewaySemanticEvent} from './component-contracts.mjs';
import {createSemanticEvent} from './context-events.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayCollectionColumnElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['member', 'label'];
  }

  get member() {
    return this.getAttribute('member') || '';
  }

  set member(value) {
    this.setAttribute('member', value);
  }

  get label() {
    return this.getAttribute('label') || humanize(this.member);
  }

  set label(value) {
    this.setAttribute('label', value);
  }

  get configuration() {
    return Object.freeze({
      member: this.member,
      label: this.label,
      testId: this.getAttribute('data-testid') || null
    });
  }

  connectedCallback() {
    this.hidden = true;
    this.#notify();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue && this.isConnected) {
      this.#notify();
    }
  }

  #notify() {
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.COLLECTION_CONFIGURATION,
      {column: this.configuration}
    ));
  }
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
