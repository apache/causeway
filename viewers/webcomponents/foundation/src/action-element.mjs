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
  CAUSEWAY_ACTION_WIDGET_POLICY_EVENT,
  renderCausewayActionWidget,
  renderNativeCausewayActionButton,
  useCausewayActionWidget
} from './action-widget.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';

let actionSequence = 0;

export class CausewayActionElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['id', 'label', 'data-testid'];
  }

  constructor() {
    super();
    const sequence = ++actionSequence;
    this.descriptionId = `causeway-action-description-${sequence}`;
    this.reasonId = `causeway-action-reason-${sequence}`;
    this.addEventListener('click', event => {
      if (originatesFromOrdinaryActionControl(this, event.target)) {
        this.activate();
      }
    });
    this.addEventListener('causeway-action-load-failed', event => event.stopPropagation());
    this._actionWidgetPolicyListener = () => {
      const restoreFocus = this.contains(document.activeElement);
      this.renderComponentState(this.componentState);
      if (restoreFocus) {
        queueMicrotask(() => this.querySelector?.('[data-causeway-action-control], button')?.focus?.());
      }
    };
  }

  connectedCallback() {
    document.addEventListener(CAUSEWAY_ACTION_WIDGET_POLICY_EVENT, this._actionWidgetPolicyListener);
    super.connectedCallback();
  }

  disconnectedCallback() {
    document.removeEventListener(CAUSEWAY_ACTION_WIDGET_POLICY_EVENT, this._actionWidgetPolicyListener);
    super.disconnectedCallback();
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
    if (name === 'id') {
      this.reconnectRequirement();
    } else {
      this.renderComponentState(this.componentState);
    }
  }

  createRequirement() {
    return {kind: 'action', member: this.id};
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
        actionId: this.id,
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
    const label = this.label || humanize(this.id);
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
    const testId = this.getAttribute('data-testid');
    const control = {label, describedBy, disabled: Boolean(disabledReason), testId: testId ? `${testId}-control` : ''};
    const controlMarkup = useCausewayActionWidget()
      ? renderCausewayActionWidget(control)
      : renderNativeCausewayActionButton(control);
    this.innerHTML = `<div class="causeway-action${disabledReason ? ' causeway-disabled' : ''}">
  ${controlMarkup}
  ${descriptionMarkup}
  ${disabledReason ? `<span id="${this.reasonId}" class="causeway-action-disabled-reason">${escapeHtml(disabledReason)}</span>` : ''}
</div>`;
  }
}

function originatesFromOrdinaryActionControl(action, target) {
  for (let current = target; current && current !== action;) {
    if (['button', 'vaadin-button', 'cw-action-control'].includes(current.localName)) return true;
    current = current.parentNode ?? current.host ?? current.getRootNode?.()?.host ?? null;
  }
  return false;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
