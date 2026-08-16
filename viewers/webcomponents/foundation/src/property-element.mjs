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
import {
  defaultEditorRegistry,
  parseCausewayEditorValue,
  renderCausewayEditor
} from './editor-registry.mjs';
import {errorMessage, escapeHtml} from './rendering.mjs';
import {InteractionStatus} from './types.mjs';
import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

let propertySequence = 0;

export class CausewayPropertyElement extends CausewayContextConsumerElement {
  static get observedAttributes() {
    return ['member', 'label', 'editable'];
  }

  constructor() {
    super();
    const sequence = ++propertySequence;
    this.labelId = `causeway-property-label-${sequence}`;
    this.descriptionId = `causeway-property-description-${sequence}`;
    this.reasonId = `causeway-property-reason-${sequence}`;
    this.inputId = `causeway-property-input-${sequence}`;
    this.errorId = `causeway-property-error-${sequence}`;
    this._rendererRegistry = defaultValueRendererRegistry;
    this._editorRegistry = defaultEditorRegistry;
    this.interactionState = null;
    this.interactionGeneration = 0;
    this.validationTimer = null;
    this.renderingInteraction = false;
    this.addEventListener('click', event => {
      const action = event.target?.getAttribute?.('data-causeway-action') ?? event.target?.dataset?.causewayAction;
      if (action === 'edit') {
        void this.beginEdit();
      } else if (action === 'cancel') {
        this.cancelEdit();
      } else if (action === 'save') {
        void this.saveEdit();
      }
    });
    this.addEventListener('input', event => this.#captureEditorEvent(event, true));
    this.addEventListener('change', event => this.#captureEditorEvent(event, false));
  }

  get member() {
    return this.getAttribute('member') || '';
  }

  set member(value) {
    this.setAttribute('member', value);
  }

  get editable() {
    return this.hasAttribute('editable');
  }

  set editable(value) {
    if (value) {
      this.setAttribute('editable', '');
    } else {
      this.removeAttribute('editable');
    }
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

  get editorRegistry() {
    return this._editorRegistry;
  }

  set editorRegistry(value) {
    this._editorRegistry = value ?? defaultEditorRegistry;
    if (this.componentState) {
      this.renderComponentState(this.componentState);
    }
  }

  disconnectedCallback() {
    clearTimeout(this.validationTimer);
    super.disconnectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.isConnected) {
      return;
    }
    if (name === 'member') {
      this.interactionState = null;
      this.reconnectRequirement();
    } else {
      this.renderComponentState(this.componentState);
    }
  }

  createRequirement() {
    return {kind: 'property', member: this.member};
  }

  async beginEdit() {
    const state = this.componentState;
    if (!this.#canOfferEdit(state) || this.interactionState) {
      return false;
    }
    const generation = ++this.interactionGeneration;
    this.#setInteraction({status: InteractionStatus.PREPARING, pendingValue: state.data?.get, error: null});
    const result = await this._resolvedContext.prepareProperty(this.member);
    if (generation !== this.interactionGeneration) {
      return false;
    }
    if (result.status !== InteractionStatus.SUCCESS) {
      this.#setInteraction({
        status: result.status,
        pendingValue: state.data?.get,
        error: result.errors?.[0]?.message ?? 'Property editing is unavailable.'
      });
      return false;
    }
    const editorContext = this.#editorContext({
      pendingValue: state.data?.get,
      capabilities: result.data.capabilities,
      choices: result.data.choices,
      error: null
    });
    const renderedEditor = renderCausewayEditor(editorContext, this._editorRegistry);
    if (renderedEditor.editorId === 'unsupported') {
      this.#setInteraction({
        status: InteractionStatus.UNSUPPORTED,
        pendingValue: state.data?.get,
        capabilities: result.data.capabilities,
        choices: result.data.choices,
        editor: renderedEditor.editor,
        error: `No editor supports '${result.data.capabilities.inputType?.name ?? 'this input type'}'.`
      });
      return false;
    }
    this.#setInteraction({
      status: InteractionStatus.EDITING,
      pendingValue: state.data?.get,
      capabilities: result.data.capabilities,
      choices: result.data.choices,
      suggestions: Object.freeze([]),
      editor: renderedEditor.editor,
      error: null
    });
    queueMicrotask(() => this.querySelector?.('[data-causeway-editor]')?.focus?.());
    return true;
  }

  cancelEdit() {
    if (!this.interactionState) {
      return false;
    }
    clearTimeout(this.validationTimer);
    this.interactionGeneration += 1;
    this.interactionState = null;
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.PROPERTY_INTERACTION_STATE, Object.freeze({
      member: this.member,
      status: InteractionStatus.CANCELLED,
      value: this.componentState?.data?.get
    })));
    this.renderComponentState(this.componentState);
    queueMicrotask(() => this.querySelector?.('[data-causeway-action="edit"]')?.focus?.());
    return true;
  }

  setPendingValue(value, {validate = false} = {}) {
    if (!this.interactionState) {
      return false;
    }
    if (this.interactionState.status === InteractionStatus.VALIDATING) {
      this.interactionGeneration += 1;
    }
    this.interactionState = Object.freeze({...this.interactionState, status: InteractionStatus.EDITING, pendingValue: value, error: null});
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.PROPERTY_INTERACTION_STATE, Object.freeze({
      member: this.member,
      status: InteractionStatus.EDITING,
      value,
      error: null
    })));
    if (validate) {
      clearTimeout(this.validationTimer);
      this.validationTimer = setTimeout(async () => {
        if (this.interactionState?.capabilities?.autoComplete) {
          await this.loadAutoComplete(String(this.interactionState.pendingValue ?? ''));
        }
        await this.validatePending();
      }, 250);
    }
    return true;
  }

  async loadAutoComplete(search) {
    if (!this.interactionState?.capabilities?.autoComplete) {
      return {status: InteractionStatus.UNSUPPORTED, data: null, errors: []};
    }
    const generation = this.interactionGeneration;
    const result = await this._resolvedContext.autoCompleteProperty(this.member, search);
    if (generation !== this.interactionGeneration || result.status !== InteractionStatus.SUCCESS) {
      return result;
    }
    this.interactionState = Object.freeze({
      ...this.interactionState,
      suggestions: Object.freeze([...(result.data ?? [])])
    });
    this.renderComponentState(this.componentState);
    return result;
  }

  async validatePending() {
    if (!this.interactionState?.capabilities?.validate) {
      return {status: InteractionStatus.SUCCESS, data: null, errors: []};
    }
    clearTimeout(this.validationTimer);
    const generation = ++this.interactionGeneration;
    const pendingValue = this.interactionState.pendingValue;
    this.#setInteraction({...this.interactionState, status: InteractionStatus.VALIDATING, error: null});
    const result = await this._resolvedContext.validateProperty(this.member, pendingValue);
    if (generation !== this.interactionGeneration || result.status === InteractionStatus.OBSOLETE) {
      return result;
    }
    const validationReason = result.status === InteractionStatus.SUCCESS && typeof result.data === 'string'
      ? result.data
      : result.errors?.[0]?.message ?? null;
    this.#setInteraction({
      ...this.interactionState,
      status: validationReason ? InteractionStatus.FAILED : InteractionStatus.EDITING,
      error: validationReason
    });
    return result;
  }

  async saveEdit() {
    if (!this.interactionState || [InteractionStatus.SAVING, InteractionStatus.VALIDATING].includes(this.interactionState.status)) {
      return false;
    }
    const valueBeingValidated = this.interactionState.pendingValue;
    const validation = await this.validatePending();
    if (!this.interactionState
        || this.interactionState.pendingValue !== valueBeingValidated
        || this.interactionState.error
        || validation.status !== InteractionStatus.SUCCESS) {
      return false;
    }
    const generation = ++this.interactionGeneration;
    const pendingValue = this.interactionState.pendingValue;
    this.#setInteraction({...this.interactionState, status: InteractionStatus.SAVING, error: null});
    const result = await this._resolvedContext.updateProperty(this.member, pendingValue);
    if (generation !== this.interactionGeneration) {
      return false;
    }
    if (result.status !== InteractionStatus.SUCCESS) {
      this.#setInteraction({
        ...this.interactionState,
        status: InteractionStatus.FAILED,
        error: result.errors?.[0]?.message ?? 'Property update failed.'
      });
      return false;
    }
    this.interactionState = null;
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.PROPERTY_UPDATED, Object.freeze({
      member: this.member,
      value: pendingValue,
      identity: this._resolvedContext?.identity ?? null,
      result
    })));
    this.renderComponentState(this.componentState);
    queueMicrotask(() => this.querySelector?.('[data-causeway-action="edit"]')?.focus?.());
    return true;
  }

  renderComponentState(state) {
    if (!state) {
      return;
    }
    if (this.interactionState) {
      this.#renderInteraction(state);
      return;
    }
    const presentation = this.#presentation(state);
    if (presentation.loading) {
      this.innerHTML = `<div class="causeway-property" aria-busy="true"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(presentation.label)}</span>${presentation.descriptionMarkup}<span role="status">Loading value…</span></div>`;
      return;
    }
    if (presentation.error) {
      this.innerHTML = `<div class="causeway-property causeway-error" role="alert"><span id="${this.labelId}" class="causeway-property-label">${escapeHtml(presentation.label)}</span>${presentation.descriptionMarkup}<span>${escapeHtml(errorMessage(state))}</span></div>`;
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
    const rendered = renderCausewayValue({value: propertyState.get, descriptor: state.descriptor}, this._rendererRegistry);
    this.setAttribute('data-renderer', rendered.rendererId);
    const editMarkup = this.#canOfferEdit(state)
      ? `<button type="button" class="causeway-property-edit" data-causeway-action="edit"${this.#testId('edit')}>Edit ${escapeHtml(presentation.label)}</button>`
      : '';
    this.innerHTML = `<div class="causeway-property${presentation.disabledReason ? ' causeway-disabled' : ''}" aria-busy="false"${presentation.disabledReason ? ' data-disabled="true"' : ''}>
  <span id="${this.labelId}" class="causeway-property-label">${escapeHtml(presentation.label)}</span>
  ${presentation.descriptionMarkup}
  <output class="causeway-property-value" aria-labelledby="${this.labelId}"${presentation.describedBy ? ` aria-describedby="${presentation.describedBy}"` : ''}>${rendered.html}</output>
  ${editMarkup}
  ${presentation.disabledMarkup}
</div>`;
  }

  #captureEditorEvent(event, debounce) {
    if (this.renderingInteraction
        || ![InteractionStatus.EDITING, InteractionStatus.VALIDATING, InteractionStatus.FAILED].includes(this.interactionState?.status)
        || !event.target?.getAttribute?.('data-causeway-editor')) {
      return;
    }
    const editor = this.interactionState.editor;
    const value = parseCausewayEditorValue(editor, {
      value: event.target.value,
      checked: event.target.checked,
      choices: this.interactionState.choices ?? [],
      suggestions: this.interactionState.suggestions ?? [],
      inputType: this.interactionState.capabilities?.inputType
    });
    this.setPendingValue(value, {validate: debounce && editor.id === 'text'});
    if (!debounce || editor.id !== 'text') {
      void this.validatePending();
    }
  }

  #canOfferEdit(state) {
    const propertyState = state?.data ?? {};
    return this.editable
      && state?.status === 'ready'
      && propertyState.hidden !== true
      && !propertyState.disabled
      && typeof this._resolvedContext?.prepareProperty === 'function';
  }

  #presentation(state) {
    const label = this.getAttribute('label') || humanize(this.member);
    const candidateDescription = state.descriptor?.description || '';
    const description = candidateDescription.trim().toLocaleLowerCase() === label.trim().toLocaleLowerCase()
      ? ''
      : candidateDescription;
    const descriptionMarkup = description
      ? `<span id="${this.descriptionId}" class="causeway-property-description">${escapeHtml(description)}</span>`
      : '';
    const disabledReason = typeof state.data?.disabled === 'string'
      ? state.data.disabled
      : state.data?.disabled === true ? 'Disabled' : '';
    const disabledMarkup = disabledReason
      ? `<p id="${this.reasonId}" class="causeway-property-disabled-reason">${escapeHtml(disabledReason)}</p>`
      : '';
    return {
      label,
      description,
      descriptionMarkup,
      disabledReason,
      disabledMarkup,
      describedBy: [description ? this.descriptionId : '', disabledReason ? this.reasonId : ''].filter(Boolean).join(' '),
      loading: ['idle', 'schema-loading', 'object-loading'].includes(state.status),
      error: ['terminal-error', 'unsupported', 'partial-error'].includes(state.status)
    };
  }

  #editorContext(interaction = this.interactionState) {
    const presentation = this.#presentation(this.componentState);
    return {
      name: this.member,
      value: interaction.pendingValue,
      choices: interaction.choices ?? [],
      suggestions: interaction.suggestions ?? [],
      autoComplete: interaction.capabilities?.autoComplete === true,
      enumValues: interaction.capabilities?.enumValues ?? [],
      inputType: interaction.capabilities?.inputType,
      inputId: this.inputId,
      labelId: this.labelId,
      descriptionId: presentation.description ? this.descriptionId : '',
      errorId: interaction.error ? this.errorId : '',
      testId: this.#testIdValue('editor'),
      disabled: interaction.status === InteractionStatus.SAVING
    };
  }

  #renderInteraction(state) {
    const presentation = this.#presentation(state);
    const interaction = this.interactionState;
    const activeElement = globalThis.document?.activeElement;
    const ownsFocus = typeof this.contains === 'function' && this.contains(activeElement);
    const activeEditor = ownsFocus && activeElement?.getAttribute?.('data-causeway-editor');
    const activeAction = ownsFocus && activeElement?.getAttribute?.('data-causeway-action');
    const selectionStart = activeEditor ? activeElement.selectionStart : null;
    const selectionEnd = activeEditor ? activeElement.selectionEnd : null;
    const busy = [InteractionStatus.PREPARING, InteractionStatus.VALIDATING, InteractionStatus.SAVING].includes(interaction.status);
    const renderedEditor = interaction.editor
      ? {editor: interaction.editor, editorId: interaction.editor.id, html: interaction.editor.render(this.#editorContext())}
      : {editorId: 'pending', html: '<span role="status">Preparing editor…</span>'};
    const errorMarkup = interaction.error
      ? `<p id="${this.errorId}" class="causeway-property-validation" role="alert">${escapeHtml(interaction.error)}</p>`
      : '';
    this.setAttribute('data-editor', renderedEditor.editorId);
    this.renderingInteraction = true;
    try {
      this.innerHTML = `<div class="causeway-property causeway-property-editing${interaction.error ? ' causeway-error' : ''}" aria-busy="${busy}">
  <span id="${this.labelId}" class="causeway-property-label">${escapeHtml(presentation.label)}</span>
  ${presentation.descriptionMarkup}
  <div class="causeway-property-editor">${renderedEditor.html}</div>
  ${errorMarkup}
  <div class="causeway-property-editor-actions">
    <button type="button" data-causeway-action="save"${this.#testId('save')} ${busy || interaction.error ? 'disabled' : ''}>Save</button>
    <button type="button" data-causeway-action="cancel"${this.#testId('cancel')} ${interaction.status === InteractionStatus.SAVING ? 'disabled' : ''}>Cancel</button>
  </div>
  <span class="causeway-property-interaction-status" role="status">${escapeHtml(interactionStatusLabel(interaction.status))}</span>
</div>`;
    } finally {
      this.renderingInteraction = false;
    }
    const focusSelector = activeEditor
      ? '[data-causeway-editor]'
      : activeAction ? `[data-causeway-action="${activeAction}"]` : '';
    if (focusSelector) {
      queueMicrotask(() => {
        const focusTarget = this.querySelector?.(focusSelector);
        focusTarget?.focus?.();
        if (activeEditor && selectionStart !== null && typeof focusTarget?.setSelectionRange === 'function') {
          focusTarget.setSelectionRange(selectionStart, selectionEnd);
        }
      });
    }
  }

  #setInteraction(next) {
    this.interactionState = Object.freeze({...next});
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.PROPERTY_INTERACTION_STATE, Object.freeze({
      member: this.member,
      status: next.status,
      value: next.pendingValue,
      error: next.error ?? null
    })));
    this.renderComponentState(this.componentState);
  }

  #testId(suffix) {
    const value = this.#testIdValue(suffix);
    return value ? ` data-testid="${escapeHtml(value)}"` : '';
  }

  #testIdValue(suffix) {
    const host = this.getAttribute('data-testid');
    return host ? `${host}-${suffix}` : '';
  }
}

function interactionStatusLabel(status) {
  return {
    [InteractionStatus.PREPARING]: 'Preparing editor',
    [InteractionStatus.EDITING]: 'Editing',
    [InteractionStatus.VALIDATING]: 'Validating',
    [InteractionStatus.SAVING]: 'Saving',
    [InteractionStatus.FAILED]: 'Correction required',
    [InteractionStatus.UNSUPPORTED]: 'Editing unsupported'
  }[status] ?? status;
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
