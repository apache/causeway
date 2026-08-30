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

import {normalizeActionPresentation} from './action-presentation.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {defaultEditorRegistry, parseCausewayEditorValue, renderCausewayEditor} from './editor-registry.mjs';
import {causewayReferenceWidgetConfiguration} from './reference-widget.mjs';
import {escapeHtml} from './rendering.mjs';
import {InteractionStatus} from './types.mjs';
import {CausewayValueCodecError, semanticTypeName} from './value-codecs.mjs';

let controllerSequence = 0;

export class CausewayInteractionControllerElement extends HTMLElement {
  constructor() {
    super();
    const sequence = ++controllerSequence;
    this.titleId = `causeway-action-prompt-title-${sequence}`;
    this.descriptionId = `causeway-action-prompt-description-${sequence}`;
    this.errorId = `causeway-action-prompt-error-${sequence}`;
    this._editorRegistry = defaultEditorRegistry;
    this.promptState = null;
    this.resultState = null;
    this.generation = 0;
    this.parameterTimer = null;
    this.validatedParameterIds = new Set();
    this.pendingParameterFocusState = null;
    this.renderingPrompt = false;
    this.autoCompleteController = null;
    this.autoCompleteGeneration = 0;
    this.scope = null;
    this.onActionRequest = event => {
      const actionId = event.detail?.actionId;
      const context = event.detail?.context;
      const presentation = event.detail?.presentation;
      const source = focusRestoreTarget(event.target);
      queueMicrotask(() => {
        if (!event.defaultPrevented && actionId && context) {
          void this.beginAction(actionId, context, source, presentation);
        }
      });
    };
    this.addEventListener('click', event => {
      const action = event.target?.getAttribute?.('data-causeway-action') ?? event.target?.dataset?.causewayAction;
      if (action === 'cancel') {
        this.cancelPrompt();
      } else if (action === 'submit') {
        void this.submitPrompt();
      } else if (action === 'dismiss-result') {
        this.dismissResult();
      }
    });
    this.addEventListener('input', event => this.#captureParameter(event));
    this.addEventListener('change', event => this.#captureParameter(event));
    this.addEventListener('focusout', event => this.#captureParameter(event, {commit: true}));
    this.addEventListener('causeway-editor-commit', event => {
      if (this.renderingPrompt) {
        event.stopPropagation();
        return;
      }
      const parameterId = event.detail?.name;
      if (!parameterId) {
        return;
      }
      event.stopPropagation();
      this.#captureParameter({
        type: 'focusout',
        target: {
          getAttribute: name => name === 'data-causeway-editor' ? parameterId : null,
          value: event.target?.value,
          checked: event.target?.checked
        },
        relatedTarget: {
          getAttribute: name => name === 'data-causeway-editor'
            ? event.detail?.nextEditor ?? null
            : name === 'data-causeway-action' ? event.detail?.nextAction ?? null : null
        }
      }, {commit: true});
    });
    this.addEventListener('causeway-reference-search', event => {
      const parameterId = event.detail?.name;
      if (!parameterId || !this.promptState) {
        return;
      }
      event.stopPropagation();
      const request = event.detail;
      if (typeof request.respond === 'function') {
        void this.loadParameterAutoComplete(parameterId, request.search, {
          offset: request.offset,
          size: request.size,
          publish: false
        }).then(result => request.respond(result));
      } else {
        void this.loadParameterAutoComplete(parameterId, request.search);
      }
    });
    const cancelToolkitEditor = event => {
      if (this.promptState) {
        event.stopPropagation();
        this.cancelPrompt();
      }
    };
    this.addEventListener('causeway-reference-escape', cancelToolkitEditor);
    this.addEventListener('causeway-field-escape', cancelToolkitEditor);
    const rerenderFailedToolkitEditor = event => {
      if (this.promptState) {
        event.stopPropagation();
        this.render();
        this.#focusFirstControl();
      }
    };
    this.addEventListener('causeway-reference-load-failed', rerenderFailedToolkitEditor);
    this.addEventListener('causeway-field-load-failed', rerenderFailedToolkitEditor);
    this.addEventListener('keydown', event => {
      if (event.key === 'Escape' && this.promptState) {
        event.preventDefault();
        this.cancelPrompt();
      } else if (event.key === 'Tab' && this.promptState) {
        this.#recordPromptFocusDestination(event);
        this.#containPromptFocus(event);
      }
    });
  }

  get editorRegistry() {
    return this._editorRegistry;
  }

  set editorRegistry(value) {
    this._editorRegistry = value ?? defaultEditorRegistry;
    this.render();
  }

  connectedCallback() {
    this.scope = this.parentNode ?? this;
    this.scope.addEventListener(CausewaySemanticEvent.ACTION_REQUEST, this.onActionRequest);
    this.render();
  }

  disconnectedCallback() {
    clearTimeout(this.parameterTimer);
    this.autoCompleteController?.abort();
    this.autoCompleteController = null;
    this.autoCompleteGeneration += 1;
    this.scope?.removeEventListener(CausewaySemanticEvent.ACTION_REQUEST, this.onActionRequest);
    this.scope = null;
    this.generation += 1;
  }

  async beginAction(actionId, context, source = null, presentation = null) {
    if (this.promptState?.status === InteractionStatus.INVOKING) {
      return false;
    }
    const generation = ++this.generation;
    this.validatedParameterIds.clear();
    this.pendingParameterFocusState = null;
    this.promptState = Object.freeze({
      status: InteractionStatus.PREPARING,
      actionId,
      context,
      source,
      presentation: normalizeActionPresentation({
        name: presentation?.name || humanize(actionId),
        description: presentation?.description,
        cssClassFa: presentation?.icon?.classes?.join(' '),
        cssClassFaPosition: presentation?.icon?.position
      }),
      values: Object.freeze({}),
      parameters: Object.freeze([]),
      error: null
    });
    this.#publishPromptState();
    this.render();
    const prepared = await context.prepareAction(actionId, {});
    if (generation !== this.generation) {
      return false;
    }
    if (prepared.status !== InteractionStatus.SUCCESS) {
      this.promptState = Object.freeze({
        ...this.promptState,
        status: prepared.status,
        error: prepared.errors?.[0]?.message ?? 'Action interaction is unavailable.'
      });
      this.#publishPromptState();
      this.render();
      return false;
    }
    let parameters = prepared.data.parameters ?? [];
    if (parameters.length === 0) {
      return this.#invoke(actionId, context, {}, source, generation);
    }
    const values = {};
    for (const parameter of parameters) {
      if (parameter.state?.default !== undefined && parameter.state.default !== null) {
        values[parameter.id] = parameter.state.default;
      }
    }
    if (Object.keys(values).length > 0) {
      const preparedWithDefaults = await context.prepareAction(actionId, values);
      if (generation !== this.generation) {
        return false;
      }
      if (preparedWithDefaults.status === InteractionStatus.SUCCESS) {
        parameters = preparedWithDefaults.data.parameters ?? parameters;
      }
    }
    this.promptState = Object.freeze({
      ...this.promptState,
      status: InteractionStatus.EDITING,
      values: Object.freeze(values),
      parameters,
      error: null
    });
    this.#publishPromptState();
    this.render();
    this.#focusFirstControl();
    return true;
  }

  async setParameterValue(parameterId, value, {recompute = true, revealValidation = true, focusState} = {}) {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    if (revealValidation) {
      this.validatedParameterIds.add(parameterId);
    }
    clearTimeout(this.parameterTimer);
    this.autoCompleteController?.abort();
    this.autoCompleteController = null;
    this.autoCompleteGeneration += 1;
    const effectiveFocusState = focusState === undefined ? this.#captureControlFocus() : focusState;
    const generation = ++this.generation;
    const values = Object.freeze({...this.promptState.values, [parameterId]: value});
    this.promptState = Object.freeze({...this.promptState, values, error: null, status: InteractionStatus.EDITING});
    this.#publishPromptState();
    if (!recompute) {
      this.render();
      this.#restoreControlFocus(effectiveFocusState);
      return true;
    }
    const prepared = await this.promptState.context.prepareAction(this.promptState.actionId, values);
    if (generation !== this.generation || prepared.status === InteractionStatus.OBSOLETE) {
      return false;
    }
    if (prepared.status !== InteractionStatus.SUCCESS) {
      this.promptState = Object.freeze({
        ...this.promptState,
        status: InteractionStatus.FAILED,
        error: prepared.errors?.[0]?.message ?? 'Unable to recompute parameter state.'
      });
    } else {
      let parameters = prepared.data.parameters;
      const changedParameter = parameters.find(parameter => parameter.id === parameterId);
      if (changedParameter?.fields.has('autoComplete') && typeof value === 'string' && value.length > 0) {
        const suggestions = await this.promptState.context.autoCompleteActionParameter(
          this.promptState.actionId,
          parameterId,
          value,
          values
        );
        if (generation !== this.generation) {
          return false;
        }
        if (suggestions.status === InteractionStatus.SUCCESS) {
          parameters = Object.freeze(parameters.map(parameter => parameter.id === parameterId
            ? Object.freeze({...parameter, state: Object.freeze({...parameter.state, suggestions: suggestions.data ?? []})})
            : parameter));
        }
      }
      this.promptState = Object.freeze({
        ...this.promptState,
        status: InteractionStatus.EDITING,
        parameters,
        error: null
      });
    }
    this.#publishPromptState();
    this.render();
    this.#restoreControlFocus(effectiveFocusState);
    return true;
  }

  async loadParameterAutoComplete(parameterId, search, {offset = 0, size = null, publish = true} = {}) {
    const parameter = this.promptState?.parameters.find(candidate => candidate.id === parameterId);
    if (!parameter?.fields.has('autoComplete') || this.promptState.status === InteractionStatus.INVOKING) {
      return {status: InteractionStatus.UNSUPPORTED, data: null, errors: []};
    }
    this.autoCompleteController?.abort();
    const controller = new AbortController();
    this.autoCompleteController = controller;
    const generation = ++this.autoCompleteGeneration;
    const {maximumResults} = causewayReferenceWidgetConfiguration();
    const requestedSize = size ?? parameter.autoCompleteWindow?.sizeDefault ?? maximumResults;
    const context = this.promptState.context;
    const result = typeof context.autoCompleteActionParameterWindow === 'function'
      ? await context.autoCompleteActionParameterWindow(
          this.promptState.actionId,
          parameterId,
          search,
          this.promptState.values,
          {offset, size: requestedSize, signal: controller.signal}
        )
      : legacyWindowResult(await context.autoCompleteActionParameter(
          this.promptState.actionId,
          parameterId,
          search,
          this.promptState.values,
          {signal: controller.signal}
        ), offset, requestedSize);
    if (generation !== this.autoCompleteGeneration || controller.signal.aborted || !this.promptState) {
      return {...result, status: InteractionStatus.OBSOLETE};
    }
    if (result.status !== InteractionStatus.SUCCESS) {
      if (result.status !== InteractionStatus.OBSOLETE) {
        this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.FAILED, error: result.errors?.[0]?.message ?? 'Reference search failed.'});
        this.#publishPromptState();
        this.render();
      }
      return result;
    }
    const window = result.data;
    const suggestions = [...(window?.items ?? [])];
    if (window?.windowed !== true && suggestions.length > maximumResults) {
      const message = `More than ${maximumResults} references matched. Refine the search.`;
      this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.FAILED, error: message});
      this.#publishPromptState();
      this.render();
      return {status: InteractionStatus.FAILED, data: null, errors: [{message, code: 'AUTOCOMPLETE_RESULT_LIMIT'}]};
    }
    if (publish) {
      const parameters = Object.freeze(this.promptState.parameters.map(candidate => candidate.id === parameterId
        ? Object.freeze({...candidate, state: Object.freeze({
            ...candidate.state,
            suggestions: Object.freeze(suggestions),
            autoCompleteWindow: window
          })})
        : candidate));
      this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.EDITING, parameters, error: null});
      this.#publishPromptState();
      this.render();
      this.#focusFirstControl();
    }
    return result;
  }

  async submitPrompt() {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    clearTimeout(this.parameterTimer);
    const focusState = this.#captureControlFocus();
    const generation = ++this.generation;
    for (const parameter of this.promptState.parameters) {
      this.validatedParameterIds.add(parameter.id);
    }
    this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.VALIDATING, error: null});
    this.#publishPromptState();
    this.render();
    this.#restoreControlFocus(focusState);
    const prepared = await this.promptState.context.prepareAction(this.promptState.actionId, this.promptState.values);
    if (generation !== this.generation || prepared.status === InteractionStatus.OBSOLETE) {
      return false;
    }
    if (prepared.status !== InteractionStatus.SUCCESS) {
      this.promptState = Object.freeze({
        ...this.promptState,
        status: InteractionStatus.FAILED,
        error: prepared.errors?.[0]?.message ?? 'Unable to validate parameter state.'
      });
      this.#publishPromptState();
      this.render();
      this.#focusFirstInvalidControl();
      return false;
    }
    this.promptState = Object.freeze({...this.promptState, parameters: prepared.data.parameters ?? this.promptState.parameters});
    this.#publishPromptState();
    this.render();
    this.#restoreControlFocus(focusState);
    const validation = await this.promptState.context.validateAction(this.promptState.actionId, this.promptState.values);
    if (generation !== this.generation) {
      return false;
    }
    const validationReason = validation.status === InteractionStatus.SUCCESS && typeof validation.data === 'string'
      ? validation.data
      : validation.errors?.[0]?.message ?? null;
    if (validationReason) {
      this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.FAILED, error: validationReason});
      this.#publishPromptState();
      this.render();
      this.#focusFirstInvalidControl();
      return false;
    }
    return this.#invoke(
      this.promptState.actionId,
      this.promptState.context,
      this.promptState.values,
      this.promptState.source,
      generation
    );
  }

  cancelPrompt() {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    clearTimeout(this.parameterTimer);
    this.autoCompleteController?.abort();
    this.autoCompleteController = null;
    this.autoCompleteGeneration += 1;
    const source = this.promptState.source;
    const actionId = this.promptState.actionId;
    this.validatedParameterIds.clear();
    this.pendingParameterFocusState = null;
    const interactionContext = this.promptState.context;
    this.generation += 1;
    this.promptState = null;
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.ACTION_PROMPT_STATE, Object.freeze({
      actionId,
      status: InteractionStatus.CANCELLED,
      ...interactionTargetDetail(interactionContext)
    })));
    this.render();
    this.#restoreFocus(source);
    return true;
  }

  dismissResult() {
    if (!this.resultState) {
      return false;
    }
    this.resultState = null;
    this.render();
    return true;
  }

  render() {
    const promptMarkup = this.promptState ? this.#promptMarkup() : '';
    const resultMarkup = this.resultState ? this.#resultMarkup() : '';
    this.renderingPrompt = true;
    try {
      this.innerHTML = `<div class="causeway-interaction-controller">${promptMarkup}${resultMarkup}</div>`;
    } finally {
      this.renderingPrompt = false;
    }
  }

  async #invoke(actionId, context, values, source, generation) {
    const promptWasOpen = (this.promptState?.parameters?.length ?? 0) > 0;
    this.promptState = Object.freeze({
      ...(this.promptState ?? {actionId, context, source, values, parameters: []}),
      status: InteractionStatus.INVOKING,
      error: null
    });
    this.#publishPromptState();
    this.render();
    let result;
    try {
      result = await context.invokeAction(actionId, values);
    } catch {
      if (generation === this.generation) {
        this.promptState = Object.freeze({
          ...this.promptState,
          status: InteractionStatus.FAILED,
          error: 'Action invocation failed.'
        });
        this.#publishPromptState();
        this.render();
        this.#focusFirstInvalidControl();
      }
      return false;
    }
    if (generation !== this.generation) {
      return false;
    }
    if (result.status !== InteractionStatus.SUCCESS) {
      this.promptState = Object.freeze({
        ...this.promptState,
        status: InteractionStatus.FAILED,
        error: result.errors?.[0]?.message ?? 'Action invocation failed.'
      });
      this.#publishPromptState();
      this.render();
      this.#focusFirstInvalidControl();
      return false;
    }
    this.validatedParameterIds.clear();
    this.promptState = null;
    this.resultState = Object.freeze({actionId, result: result.data, ...interactionTargetDetail(context)});
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.ACTION_RESULT, Object.freeze({
      actionId,
      ...interactionTargetDetail(context),
      result: result.data,
      context
    })));
    this.render();
    if (promptWasOpen) {
      this.#restoreFocus(source);
    }
    return true;
  }

  #captureParameter(event, {commit = false} = {}) {
    if (this.renderingPrompt) {
      return;
    }
    const parameterId = event.target?.getAttribute?.('data-causeway-editor');
    if (!parameterId || !this.promptState) {
      if (commit) {
        this.pendingParameterFocusState = null;
      }
      return;
    }
    const parameter = this.promptState.parameters.find(candidate => candidate.id === parameterId);
    if (!parameter) {
      return;
    }
    const choices = parameter.state?.choices ?? parameter.enumValues ?? [];
    const clearPresentedValidation = !commit && (
      this.validatedParameterIds.has(parameterId)
      || this.promptState.status === InteractionStatus.FAILED
      || Boolean(this.promptState.error));
    const editingFocusState = clearPresentedValidation ? this.#captureControlFocus() : null;
    const rendered = renderCausewayEditor(this.#parameterEditorContext(parameter, choices), this._editorRegistry);
    if (rendered.editor.id === 'autocomplete' && event.type === 'input') {
      clearTimeout(this.parameterTimer);
      const search = String(event.target.value ?? '');
      this.parameterTimer = setTimeout(() => void this.loadParameterAutoComplete(parameterId, search), 250);
      return;
    }
    try {
      const value = parseCausewayEditorValue(rendered.editor, {
        value: event.target.value,
        checked: event.target.checked,
        choices,
        suggestions: parameter.state?.suggestions ?? [],
        inputType: parameter.inputType
      });
      if (commit) {
        const focusState = this.#controlFocusState(event.relatedTarget) ?? this.pendingParameterFocusState;
        this.pendingParameterFocusState = null;
        void this.setParameterValue(parameterId, value, {focusState});
        return;
      }
      this.validatedParameterIds.delete(parameterId);
      const parameters = Object.freeze(this.promptState.parameters.map(candidate => candidate.id === parameterId
        ? Object.freeze({...candidate, state: Object.freeze({...candidate.state, error: null})})
        : candidate));
      this.promptState = Object.freeze({
        ...this.promptState,
        values: Object.freeze({...this.promptState.values, [parameterId]: value}),
        parameters,
        error: null,
        status: InteractionStatus.EDITING
      });
      clearTimeout(this.parameterTimer);
      if (clearPresentedValidation) {
        this.#publishPromptState();
        this.render();
        this.#restoreControlFocus(editingFocusState);
      }
    } catch (error) {
      if (!(error instanceof CausewayValueCodecError)) {
        throw error;
      }
      const values = Object.freeze({...this.promptState.values, [parameterId]: event.target.value});
      if (!commit) {
        this.validatedParameterIds.delete(parameterId);
        this.promptState = Object.freeze({...this.promptState, values, status: InteractionStatus.EDITING, error: null});
        clearTimeout(this.parameterTimer);
        if (clearPresentedValidation) {
          this.#publishPromptState();
          this.render();
          this.#restoreControlFocus(editingFocusState);
        }
        return;
      }
      this.validatedParameterIds.add(parameterId);
      const parameters = Object.freeze(this.promptState.parameters.map(candidate => candidate.id === parameterId
        ? Object.freeze({...candidate, state: Object.freeze({...candidate.state, error: error.message})})
        : candidate));
      this.promptState = Object.freeze({
        ...this.promptState,
        values,
        parameters,
        status: InteractionStatus.FAILED,
        error: error.message
      });
      this.#publishPromptState();
      this.render();
    }
  }

  #promptMarkup() {
    const state = this.promptState;
    if (state.status === InteractionStatus.PREPARING) {
      return `<div class="causeway-action-prompt causeway-loading" role="status" data-testid="action-prompt">Preparing action…</div>`;
    }
    if (state.parameters.length === 0 && state.status === InteractionStatus.INVOKING) {
      return `<div class="causeway-action-prompt causeway-loading" role="status" data-testid="action-prompt">Invoking ${escapeHtml(state.presentation?.name || humanize(state.actionId))}…</div>`;
    }
    const parameterMarkup = state.parameters.map(parameter => this.#parameterMarkup(parameter)).join('');
    const busy = [InteractionStatus.VALIDATING, InteractionStatus.INVOKING].includes(state.status);
    const publicError = protectedPromptText(state, state.error);
    const errorMarkup = publicError
      ? `<div id="${this.errorId}" class="causeway-action-prompt-error" role="alert">${escapeHtml(publicError)}</div>`
      : '';
    const description = state.presentation?.description || '';
    const descriptionMarkup = description
      ? `<p id="${this.descriptionId}" class="causeway-action-prompt-description">${escapeHtml(description)}</p>`
      : '';
    const describedBy = [description ? this.descriptionId : '', state.error ? this.errorId : ''].filter(Boolean).join(' ');
    return `<dialog open class="causeway-action-prompt" role="dialog" aria-modal="true" aria-labelledby="${this.titleId}"${describedBy ? ` aria-describedby="${describedBy}"` : ''} data-testid="action-prompt">
  <form method="dialog" novalidate>
    <h2 id="${this.titleId}">${escapeHtml(state.presentation?.name || humanize(state.actionId))}</h2>
    ${descriptionMarkup}
    ${parameterMarkup}
    ${errorMarkup}
    <div class="causeway-action-prompt-actions">
      <button type="button" data-causeway-action="submit" data-testid="action-prompt-submit" ${busy || state.error ? 'disabled' : ''}>${state.status === InteractionStatus.INVOKING ? 'Invoking…' : 'Invoke'}</button>
      <button type="button" data-causeway-action="cancel" data-testid="action-prompt-cancel" ${state.status === InteractionStatus.INVOKING ? 'disabled' : ''}>Cancel</button>
    </div>
    <span class="causeway-action-prompt-status" role="status">${escapeHtml(promptStatusLabel(state.status))}</span>
  </form>
</dialog>`;
  }

  #parameterMarkup(parameter) {
    if (parameter.state?.hidden === true) {
      return '';
    }
    const choices = parameter.state?.choices ?? parameter.enumValues ?? [];
    const editorContext = this.#parameterEditorContext(parameter, choices);
    const rendered = renderCausewayEditor(editorContext, this._editorRegistry);
    const label = humanize(parameter.id);
    const description = this.#parameterDescription(parameter, label);
    const reason = protectedPromptText(this.promptState, this.#parameterReason(parameter));
    return `<div class="causeway-action-parameter${reason ? ' causeway-error' : ''}" data-parameter="${escapeHtml(parameter.id)}">
  <label id="${editorContext.labelId}" for="${editorContext.inputId}">${escapeHtml(label)}</label>
  ${description ? `<span id="${editorContext.descriptionId}" class="causeway-action-parameter-description">${escapeHtml(description)}</span>` : ''}
  ${rendered.html}
  ${reason ? `<span id="${editorContext.errorId}" class="causeway-action-parameter-reason" role="alert">${escapeHtml(reason)}</span>` : ''}
</div>`;
  }

  #parameterReason(parameter) {
    const validationReason = this.validatedParameterIds.has(parameter.id)
      ? parameter.state?.error || parameter.state?.validity || ''
      : '';
    return validationReason || parameter.state?.disabled || '';
  }

  #parameterEditorContext(parameter, choices) {
    const state = this.promptState;
    const reason = this.#parameterReason(parameter);
    return {
      name: parameter.id,
      label: humanize(parameter.id),
      value: Object.prototype.hasOwnProperty.call(state.values, parameter.id)
        ? state.values[parameter.id]
        : parameter.state?.default ?? null,
      choices,
      suggestions: parameter.state?.suggestions ?? [],
      autoComplete: parameter.fields.has('autoComplete'),
      autoCompleteWindow: Boolean(parameter.autoCompleteWindow),
      autoCompletePageSize: parameter.autoCompleteWindow?.sizeDefault ?? null,
      hasMoreSuggestions: parameter.state?.autoCompleteWindow?.hasNext === true,
      enumValues: parameter.enumValues,
      inputType: parameter.inputType,
      semanticType: parameter.state?.datatype ?? null,
      required: parameter.inputType?.kind === 'NON_NULL',
      inputId: `causeway-action-parameter-${parameter.id}`,
      labelId: `causeway-action-parameter-${parameter.id}-label`,
      descriptionId: this.#parameterDescription(parameter) ? `causeway-action-parameter-${parameter.id}-description` : '',
      errorId: reason ? `causeway-action-parameter-${parameter.id}-error` : '',
      testId: `action-prompt-parameter-${parameter.id}`,
      disabled: Boolean(parameter.state?.disabled) || state.status === InteractionStatus.INVOKING
    };
  }

  #parameterDescription(parameter, label = humanize(parameter.id)) {
    const description = String(parameter.description ?? '').trim();
    return description.toLocaleLowerCase() === label.trim().toLocaleLowerCase() ? '' : description;
  }

  #resultMarkup() {
    const result = this.resultState.result;
    return `<section class="causeway-action-result" aria-live="polite" data-testid="action-result">
  <h2>Action result</h2>
  <p><strong>${escapeHtml(humanize(this.resultState.actionId))}</strong></p>
  <output>${escapeHtml(resultLabel(result))}</output>
  <button type="button" data-causeway-action="dismiss-result">Dismiss result</button>
</section>`;
  }

  #publishPromptState() {
    this.dispatchEvent(createSemanticEvent(CausewaySemanticEvent.ACTION_PROMPT_STATE, Object.freeze({
      actionId: this.promptState.actionId,
      status: this.promptState.status,
      values: publicPromptValues(this.promptState),
      error: protectedPromptText(this.promptState, this.promptState.error),
      ...interactionTargetDetail(this.promptState.context)
    })));
  }

  #focusFirstControl() {
    queueMicrotask(() => this.querySelector?.('[data-causeway-editor]')?.focus?.());
  }

  #focusFirstInvalidControl() {
    queueMicrotask(() => {
      const invalid = this.querySelector?.('.causeway-action-parameter.causeway-error [data-causeway-editor]')
        ?? this.querySelector?.('[data-causeway-editor]');
      invalid?.focus?.();
    });
  }

  #captureControlFocus() {
    const active = globalThis.document?.activeElement;
    return active && this.contains?.(active) ? this.#controlFocusState(active) : null;
  }

  #controlFocusState(control) {
    if (!control) {
      return null;
    }
    const editor = control.getAttribute?.('data-causeway-editor') ?? null;
    const action = control.getAttribute?.('data-causeway-action') ?? null;
    if (!editor && !action) {
      return null;
    }
    return Object.freeze({
      editor,
      action,
      selectionStart: control.selectionStart ?? null,
      selectionEnd: control.selectionEnd ?? null
    });
  }

  #restoreControlFocus(focusState) {
    if (!focusState) {
      return;
    }
    queueMicrotask(() => {
      const controls = [...(this.querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])];
      const control = controls.find(candidate => focusState.editor
        ? candidate.getAttribute?.('data-causeway-editor') === focusState.editor
        : candidate.getAttribute?.('data-causeway-action') === focusState.action);
      control?.focus?.();
      if (focusState.selectionStart != null && typeof control?.setSelectionRange === 'function') {
        control.setSelectionRange(focusState.selectionStart, focusState.selectionEnd ?? focusState.selectionStart);
      }
    });
  }

  #recordPromptFocusDestination(event) {
    const controls = [...(this.querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])]
      .filter(control => !control.disabled);
    const active = event.target?.closest?.('[data-causeway-editor], [data-causeway-action]')
      ?? globalThis.document?.activeElement;
    const index = controls.indexOf(active);
    if (index < 0) {
      this.pendingParameterFocusState = null;
      return;
    }
    const nextIndex = event.shiftKey
      ? (index - 1 + controls.length) % controls.length
      : (index + 1) % controls.length;
    this.pendingParameterFocusState = this.#controlFocusState(controls[nextIndex]);
  }

  #containPromptFocus(event) {
    const controls = [...(this.querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])]
      .filter(control => !control.disabled);
    if (controls.length === 0) {
      return;
    }
    const first = controls[0];
    const last = controls.at(-1);
    if (event.shiftKey && globalThis.document?.activeElement === first) {
      event.preventDefault();
      last.focus?.();
    } else if (!event.shiftKey && globalThis.document?.activeElement === last) {
      event.preventDefault();
      first.focus?.();
    }
  }

  #restoreFocus(source) {
    queueMicrotask(() => {
      const focusTarget = source?.matches?.('button, [href], input, select, textarea, [tabindex]')
        ? source
        : source?.querySelector?.('button, [href], input, select, textarea, [tabindex]');
      focusTarget?.focus?.();
    });
  }
}

function legacyWindowResult(result, offset, requestedSize) {
  if (result?.status !== InteractionStatus.SUCCESS || !Array.isArray(result.data)) return result;
  return {
    ...result,
    data: Object.freeze({
      items: Object.freeze([...result.data]),
      offset,
      requestedSize,
      returnedCount: result.data.length,
      totalCount: result.data.length,
      maximumSize: null,
      hasPrevious: false,
      hasNext: false,
      ordering: 'LEGACY',
      windowed: false
    })
  };
}

function focusRestoreTarget(source) {
  const panel = source?.closest?.('[data-causeway-menu-panel]');
  const panelId = panel?.id;
  if (!panelId) {
    return source;
  }
  const escapedId = globalThis.CSS?.escape ? CSS.escape(panelId) : panelId;
  return panel.parentNode?.querySelector?.(`[data-causeway-menu-disclosure][aria-controls="${escapedId}"]`) ?? source;
}

function interactionTargetDetail(context) {
  if (context?.interactionTarget?.kind === 'service') {
    return Object.freeze({
      identity: null,
      target: context.interactionTarget,
      serviceLogicalTypeName: context.interactionTarget.logicalTypeName
    });
  }
  return Object.freeze({
    identity: context?.identity ?? null,
    target: context?.identity ? Object.freeze({kind: 'object', ...context.identity}) : null,
    serviceLogicalTypeName: null
  });
}

function publicPromptValues(state) {
  const sensitiveIds = new Set((state.parameters ?? [])
    .filter(parameter => ['Password', 'ProtectedValue'].includes(semanticTypeName({semanticType: parameter.state?.datatype})))
    .map(parameter => parameter.id));
  return Object.freeze(Object.fromEntries(Object.entries(state.values ?? {})
    .map(([name, value]) => [name, sensitiveIds.has(name) ? null : value])));
}

function protectedPromptText(state, text) {
  let result = String(text ?? '');
  if (!result) {
    return result;
  }
  const publicValues = publicPromptValues(state);
  for (const [name, value] of Object.entries(state.values ?? {})) {
    if (publicValues[name] !== null || typeof value !== 'string' || value.length === 0) {
      continue;
    }
    result = result.split(value).join('[protected]');
  }
  return result;
}

function promptStatusLabel(status) {
  return {
    [InteractionStatus.EDITING]: 'Enter action parameters',
    [InteractionStatus.VALIDATING]: 'Validating action',
    [InteractionStatus.INVOKING]: 'Invoking action',
    [InteractionStatus.FAILED]: 'Correction required',
    [InteractionStatus.UNSUPPORTED]: 'Action unsupported'
  }[status] ?? status;
}

function resultLabel(result) {
  if (!result) {
    return 'Completed';
  }
  if (result.kind === 'void') {
    return 'Completed';
  }
  if (result.kind === 'object') {
    const metadata = result.value?._meta ?? result.value;
    return metadata?.title ?? metadata?.id ?? JSON.stringify(result.value);
  }
  if (result.kind === 'collection') {
    return `${result.value?.length ?? 0} result${result.value?.length === 1 ? '' : 's'}`;
  }
  return String(result.value ?? 'Completed');
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
