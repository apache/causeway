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
import {errorMessage, escapeHtml} from './rendering.mjs';
import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

let propertySequence = 0;

export class CausewayPropertyElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['member', 'label'];
  }

  constructor() {
    super();
    const sequence = ++propertySequence;
    this.labelId = `causeway-property-label-${sequence}`;
    this.descriptionId = `causeway-property-description-${sequence}`;
    this.reasonId = `causeway-property-reason-${sequence}`;
    this._rendererRegistry = defaultValueRendererRegistry;
  }

  get member() {
    return this.getAttribute('member') || '';
  }

  set member(value) {
    this.setAttribute('member', value);
  }

  get rendererRegistry() {
    return this._rendererRegistry;
  }

  set rendererRegistry(value) {
    this._rendererRegistry = value ?? defaultValueRendererRegistry;
    if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.isConnected) {
      return;
    }
    if (name === 'member') {
      this.reconnectRequirement();
    } else {
      this.renderComponentState(this.componentState);
    }
  }

  createRequirement() {
    return {kind: 'property', member: this.member};
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    const label = this.getAttribute('label') || humanize(this.member);
    const candidateDescription = state.descriptor?.description || '';
    const description = candidateDescription.trim().toLocaleLowerCase() === label.trim().toLocaleLowerCase()
      ? ''
      : candidateDescription;
    const descriptionMarkup = description
      ? `<span id="${this.descriptionId}" class="causeway-property-description">${escapeHtml(description)}</span>`
      : '';
    if (state.status === 'idle' || state.status === 'schema-loading' || state.status === 'object-loading') {
      this.innerHTML = `<div class="causeway-property" aria-busy="true"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span>${descriptionMarkup}<span role="status">Loading value…</span></div>`;
      return;
    }
    if (state.status === 'terminal-error' || state.status === 'unsupported' || state.status === 'partial-error') {
      this.innerHTML = `<div class="causeway-property causeway-error" role="alert"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span>${descriptionMarkup}<span>${escapeHtml(errorMessage(state))}</span></div>`;
      return;
    }
    const propertyState = state.data ?? {};
    if (propertyState.hidden === true) {
      this.innerHTML = '';
      this.hidden = true;
      this.removeAttribute('data-renderer');
      return;
    }
    this.hidden = false;
    const disabledReason = typeof propertyState.disabled === 'string'
      ? propertyState.disabled
      : propertyState.disabled === true ? 'Disabled' : '';
    const disabledMarkup = disabledReason
      ? `<p id="${this.reasonId}" class="causeway-property-disabled-reason">${escapeHtml(disabledReason)}</p>`
      : '';
    const describedBy = [description ? this.descriptionId : '', disabledReason ? this.reasonId : '']
      .filter(Boolean)
      .join(' ');
    const rendered = renderCausewayValue({
      value: propertyState.get,
      descriptor: state.descriptor
    }, this._rendererRegistry);
    this.setAttribute('data-renderer', rendered.rendererId);
    this.innerHTML = `<div class="causeway-property${disabledReason ? ' causeway-disabled' : ''}" aria-busy="false"${disabledReason ? ' data-disabled="true"' : ''}>
  <span id="${this.labelId}" class="causeway-property-label">${escapeHtml(label)}</span>
  ${descriptionMarkup}
  <output class="causeway-property-value" aria-labelledby="${this.labelId}"${describedBy ? ` aria-describedby="${describedBy}"` : ''}>${rendered.html}</output>
  ${disabledMarkup}
</div>`;
  }
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
