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

import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {errorMessage, escapeHtml, formatScalar} from './rendering.mjs';

let propertySequence = 0;

export class CausewayPropertyElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['member'];
  }

  constructor() {
    super();
    this.labelId = `causeway-property-label-${++propertySequence}`;
  }

  get member() {
    return this.getAttribute('member') || '';
  }

  set member(value) {
    this.setAttribute('member', value);
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === 'member' && oldValue !== newValue && this.isConnected) {
      this.reconnectRequirement();
    }
  }

  createRequirement() {
    return {kind: 'property', member: this.member};
  }

  renderComponentState(state) {
    const label = state.descriptor?.description || this.member;
    if (state.status === 'idle' || state.status === 'schema-loading' || state.status === 'object-loading') {
      this.innerHTML = `<div class="causeway-property" aria-busy="true"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span><span role="status">Loading value…</span></div>`;
      return;
    }
    if (state.status === 'terminal-error' || state.status === 'unsupported' || state.status === 'partial-error') {
      this.innerHTML = `<div class="causeway-property causeway-error" role="alert"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span><span>${escapeHtml(errorMessage(state))}</span></div>`;
      return;
    }
    const propertyState = state.data ?? {};
    if (propertyState.hidden === true) {
      this.innerHTML = '';
      this.hidden = true;
      return;
    }
    this.hidden = false;
    const disabledReason = typeof propertyState.disabled === 'string'
      ? propertyState.disabled
      : propertyState.disabled === true ? 'Disabled' : '';
    const disabledMarkup = disabledReason
      ? `<p class="causeway-property-disabled">${escapeHtml(disabledReason)}</p>`
      : '';
    this.innerHTML = `<div class="causeway-property" aria-busy="false"${disabledReason ? ' aria-disabled="true"' : ''}>
  <span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span>
  <output class="causeway-property-value" aria-labelledby="${this.labelId}">${escapeHtml(formatScalar(propertyState.get))}</output>
  ${disabledMarkup}
</div>`;
  }
}
