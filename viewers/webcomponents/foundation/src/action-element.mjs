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
import {
  composeActionTooltip,
  normalizeActionPresentation,
  normalizeAuthoredActionPromptStyle
} from './action-presentation.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {CausewayContextConsumerElement} from './context-consumer-element.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {
  actionParameterConfiguration,
  normalizeActionParameterConfigurations
} from './parameter-element.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';
import {
  normalizeStandaloneCollectionPresentation,
  standaloneCollectionPresentation
} from './standalone-collection-presentation.mjs';

let actionSequence = 0;
const initialParameterConfigurations = new WeakMap();
const initialResultPresentations = new WeakMap();

export function captureDeclarativeActionParameters(root = globalThis.document) {
  if (!root?.querySelectorAll) return;
  const actions = root.localName === 'cw-action' ? [root] : root.querySelectorAll('cw-action');
  for (const action of actions) {
    const configurations = [...(action.children ?? action.childNodes ?? [])]
      .filter(child => child?.localName === 'cw-parameter' || child?.configuration?.parameter)
      .map(child => child?.configuration ?? actionParameterConfiguration(child));
    if (configurations.length > 0) {
      initialParameterConfigurations.set(action, normalizeActionParameterConfigurations(configurations));
    }
    const resultDeclarations = [...(action.children ?? action.childNodes ?? [])]
      .filter(isStandaloneCollectionDeclaration);
    if (resultDeclarations.length === 1) {
      initialResultPresentations.set(action, standaloneCollectionPresentation(resultDeclarations[0]));
      action.removeAttribute?.('data-causeway-action-result-presentation-error');
    } else if (resultDeclarations.length > 1) {
      initialResultPresentations.set(action, null);
      action.setAttribute?.('data-causeway-action-result-presentation-error', 'duplicate');
    }
  }
}

export class CausewayActionElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['id', 'named', 'label', 'prompt-style', 'data-testid'];
  }

  constructor() {
    super();
    const sequence = ++actionSequence;
    this.descriptionId = `causeway-action-description-${sequence}`;
    this.reasonId = `causeway-action-reason-${sequence}`;
    this._parameterPresentations = [];
    this._resultPresentation = null;
    this._connectionGeneration = 0;
    this.addEventListener(CausewaySemanticEvent.ACTION_PARAMETER_CONFIGURATION, event => {
      if (event.target?.parentNode !== this) return;
      event.stopPropagation();
      this.#acceptParameterPresentation(event.detail?.parameter);
    });
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
    const generation = ++this._connectionGeneration;
    captureDeclarativeActionParameters(this);
    this.#captureDeclarativePresentations();
    document.addEventListener(CAUSEWAY_ACTION_WIDGET_POLICY_EVENT, this._actionWidgetPolicyListener);
    queueMicrotask(() => {
      if (!this.isConnected || generation !== this._connectionGeneration) return;
      captureDeclarativeActionParameters(this);
      this.#captureDeclarativePresentations();
      super.connectedCallback();
    });
  }

  disconnectedCallback() {
    this._connectionGeneration += 1;
    document.removeEventListener(CAUSEWAY_ACTION_WIDGET_POLICY_EVENT, this._actionWidgetPolicyListener);
    super.disconnectedCallback();
  }

  get named() {
    return this.getAttribute('named') || '';
  }

  set named(value) {
    if (value == null) this.removeAttribute('named');
    else this.setAttribute('named', value);
  }

  get parameterPresentations() {
    return normalizeActionParameterConfigurations(this._parameterPresentations);
  }

  get resultPresentation() {
    return normalizeStandaloneCollectionPresentation(this._resultPresentation);
  }

  get promptStyle() {
    return normalizeAuthoredActionPromptStyle(this.getAttribute('prompt-style')) || '';
  }

  set promptStyle(value) {
    if (value == null || String(value).trim() === '') this.removeAttribute('prompt-style');
    else this.setAttribute('prompt-style', value);
  }

  get label() {
    return this.getAttribute('label') || '';
  }

  set label(value) {
    if (value == null) this.removeAttribute('label');
    else this.setAttribute('label', value);
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
        context,
        presentation: this.actionPresentation(state)
      }),
      {cancelable: true}
    ));
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    const presentation = this.actionPresentation(state);
    const label = presentation.name;
    const description = presentation.description;
    const descriptionMarkup = description
      ? `<span id="${this.descriptionId}" class="causeway-action-description">${escapeHtml(description)}</span>`
      : '';
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      this.hidden = false;
      this.#renderMarkup(`<div class="causeway-action causeway-loading" aria-busy="true"><span>${escapeHtml(label)}</span>${descriptionMarkup}<span role="status">Loading action…</span></div>`);
      return;
    }
    if (['terminal-error', 'unsupported', 'partial-error'].includes(state.status)) {
      this.hidden = false;
      this.#renderMarkup(`<div class="causeway-action causeway-error" role="alert"><span>${escapeHtml(label)}</span>${descriptionMarkup}<span>${escapeHtml(errorMessage(state))}</span></div>`);
      return;
    }
    if (state.data?.hidden === true) {
      this.hidden = true;
      this.#renderMarkup('');
      return;
    }
    this.hidden = false;
    const disabledReason = typeof state.data?.disabled === 'string'
      ? state.data.disabled
      : state.data?.disabled === true ? 'Disabled' : '';
    const describedBy = [description ? this.descriptionId : '', disabledReason ? this.reasonId : '']
      .filter(Boolean)
      .join(' ');
    const tooltip = composeActionTooltip(description, disabledReason);
    const testId = this.getAttribute('data-testid');
    const control = {
      label,
      describedBy,
      disabled: Boolean(disabledReason),
      testId: testId ? `${testId}-control` : '',
      icon: presentation.icon
    };
    const controlMarkup = useCausewayActionWidget()
      ? renderCausewayActionWidget(control)
      : renderNativeCausewayActionButton(control);
    const tooltipAttributes = tooltip
      ? ` class="causeway-action-control-tooltip" data-tooltip="${escapeHtml(tooltip)}"${disabledReason ? ' tabindex="0"' : ''}`
      : '';
    this.#renderMarkup(`<div class="causeway-action${disabledReason ? ' causeway-disabled' : ''}">
  <span${tooltipAttributes}>${controlMarkup}</span>
  ${descriptionMarkup}
  ${disabledReason ? `<span id="${this.reasonId}" class="causeway-action-disabled-reason causeway-visually-hidden">${escapeHtml(disabledReason)}</span>` : ''}
</div>`);
  }

  actionPresentation(state = this.componentState) {
    const metadata = state?.data?.metadata ?? {};
    let resultPresentation = this.resultPresentation;
    if (resultPresentation && state?.status === 'ready' && !metadata.resultElementLogicalTypeName) {
      resultPresentation = null;
      this.setAttribute('data-causeway-action-result-presentation-error', 'inapplicable');
    } else if (this.getAttribute('data-causeway-action-result-presentation-error') === 'inapplicable') {
      this.removeAttribute('data-causeway-action-result-presentation-error');
    }
    const presentation = normalizeActionPresentation({
      name: this.named || this.label || metadata.friendlyName || humanize(this.id),
      description: metadata.description || state?.descriptor?.description || '',
      areYouSure: metadata.areYouSure,
      promptStyle: this.promptStyle || metadata.promptStyle,
      cssClassFa: metadata.cssClassFa,
      cssClassFaPosition: metadata.cssClassFaPosition,
      resultElementLogicalTypeName: metadata.resultElementLogicalTypeName,
      resultPresentation
    });
    const parameters = this.parameterPresentations;
    return parameters.length > 0 ? Object.freeze({...presentation, parameters}) : presentation;
  }

  #acceptParameterPresentation(configuration) {
    const parameters = normalizeActionParameterConfigurations([...this._parameterPresentations, configuration]);
    this._parameterPresentations = [...parameters];
  }

  #renderMarkup(markup) {
    const declarations = [...(this.children ?? this.childNodes ?? [])]
      .filter(child => child?.localName === 'cw-parameter'
        || child?.configuration?.parameter
        || isStandaloneCollectionDeclaration(child));
    const resultDeclarations = declarations.filter(isStandaloneCollectionDeclaration);
    if (resultDeclarations.length === 1) {
      this._resultPresentation = standaloneCollectionPresentation(resultDeclarations[0]);
      this.removeAttribute('data-causeway-action-result-presentation-error');
    } else if (resultDeclarations.length > 1) {
      this._resultPresentation = null;
      this.setAttribute('data-causeway-action-result-presentation-error', 'duplicate');
    }
    for (const declaration of declarations) {
      if (!isStandaloneCollectionDeclaration(declaration)) {
        this.#acceptParameterPresentation(declaration.configuration ?? actionParameterConfiguration(declaration));
      }
      if (declaration.parentNode === this) this.removeChild(declaration);
    }
    this.innerHTML = markup;
    for (const declaration of declarations) {
      this.appendChild(declaration);
      if (isStandaloneCollectionDeclaration(declaration)) declaration.hidden = true;
    }
  }

  #captureDeclarativePresentations() {
    for (const configuration of initialParameterConfigurations.get(this) ?? []) {
      this.#acceptParameterPresentation(configuration);
    }
    initialParameterConfigurations.delete(this);
    if (initialResultPresentations.has(this)) {
      this._resultPresentation = initialResultPresentations.get(this);
      initialResultPresentations.delete(this);
    }
    const resultDeclarations = [];
    for (const child of this.childNodes ?? []) {
      if (child?.localName === 'cw-parameter') {
        this.#acceptParameterPresentation(child?.configuration ?? actionParameterConfiguration(child));
      } else if (isStandaloneCollectionDeclaration(child)) {
        child.hidden = true;
        resultDeclarations.push(child);
      }
    }
    if (resultDeclarations.length === 1) {
      this._resultPresentation = standaloneCollectionPresentation(resultDeclarations[0]);
      this.removeAttribute('data-causeway-action-result-presentation-error');
    } else if (resultDeclarations.length > 1) {
      this._resultPresentation = null;
      this.setAttribute('data-causeway-action-result-presentation-error', 'duplicate');
    }
  }
}

function isStandaloneCollectionDeclaration(candidate) {
  return candidate?.localName === 'cw-standalone-collection'
    || candidate?.constructor?.name === 'CausewayStandaloneCollectionElement';
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
