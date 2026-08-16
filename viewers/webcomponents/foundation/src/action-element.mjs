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
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';

let actionSequence = 0;

export class CausewayActionElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['member', 'label'];
  }

  constructor() {
    super();
    const sequence = ++actionSequence;
    this.descriptionId = `causeway-action-description-${sequence}`;
    this.reasonId = `causeway-action-reason-${sequence}`;
    this.addEventListener('click', event => {
      if (event.target !== this) {
        this.activate();
      }
    });
  }

  get member() {
    return this.getAttribute('member') || '';
  }

  set member(value) {
    this.setAttribute('member', value);
  }

  get label() {
    return this.getAttribute('label') || '';
  }

  set label(value) {
    this.setAttribute('label', value);
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
    return {kind: 'action', member: this.member};
  }

  activate() {
    const state = this.componentState;
    const disabled = state?.status !== 'ready'
      || state?.data?.hidden === true
      || Boolean(state?.data?.disabled);
    if (disabled) {
      return false;
    }
    const context = this._resolvedContext;
    return this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.ACTION_REQUEST,
      Object.freeze({
        actionId: this.member,
        identity: context?.identity ?? null,
        context
      }),
      {cancelable: true}
    ));
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    const label = this.label || humanize(this.member);
    const candidateDescription = state.descriptor?.description || '';
    const description = candidateDescription.trim().toLocaleLowerCase() === label.trim().toLocaleLowerCase()
      ? ''
      : candidateDescription;
    const descriptionMarkup = description
      ? `<span id="${this.descriptionId}" class="causeway-action-description">${escapeHtml(description)}</span>`
      : '';
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      this.hidden = false;
      this.innerHTML = `<div class="causeway-action causeway-loading" aria-busy="true"><span>${escapeHtml(label)}</span>${descriptionMarkup}<span role="status">Loading action…</span></div>`;
      return;
    }
    if (['terminal-error', 'unsupported', 'partial-error'].includes(state.status)) {
      this.hidden = false;
      this.innerHTML = `<div class="causeway-action causeway-error" role="alert"><span>${escapeHtml(label)}</span>${descriptionMarkup}<span>${escapeHtml(errorMessage(state))}</span></div>`;
      return;
    }
    if (state.data?.hidden === true) {
      this.hidden = true;
      this.innerHTML = '';
      return;
    }
    this.hidden = false;
    const disabledReason = typeof state.data?.disabled === 'string'
      ? state.data.disabled
      : state.data?.disabled === true ? 'Disabled' : '';
    const describedBy = [description ? this.descriptionId : '', disabledReason ? this.reasonId : '']
      .filter(Boolean)
      .join(' ');
    this.innerHTML = `<div class="causeway-action${disabledReason ? ' causeway-disabled' : ''}">
  <button type="button"${disabledReason ? ' disabled aria-disabled="true"' : ''}${describedBy ? ` aria-describedby="${describedBy}"` : ''}>${escapeHtml(label)}</button>
  ${descriptionMarkup}
  ${disabledReason ? `<span id="${this.reasonId}" class="causeway-action-disabled-reason">${escapeHtml(disabledReason)}</span>` : ''}
</div>`;
  }
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
