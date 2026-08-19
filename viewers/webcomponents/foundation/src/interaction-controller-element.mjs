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
import {defaultEditorRegistry, parseCausewayEditorValue, renderCausewayEditor} from './editor-registry.mjs';
import {escapeHtml} from './rendering.mjs';
import {InteractionStatus} from './types.mjs';

let controllerSequence = 0;

export class CausewayInteractionControllerElement extends HTMLElement {
  constructor() {
    super();
    const sequence = ++controllerSequence;
    this.titleId = `causeway-action-prompt-title-${sequence}`;
    this.errorId = `causeway-action-prompt-error-${sequence}`;
    this._editorRegistry = defaultEditorRegistry;
    this.promptState = null;
    this.resultState = null;
    this.generation = 0;
    this.parameterTimer = null;
    this.scope = null;
    this.onActionRequest = event => {
      const actionId = event.detail?.actionId;
      const context = event.detail?.context;
      const source = event.target;
      queueMicrotask(() => {
        if (!event.defaultPrevented && actionId && context) {
          void this.beginAction(actionId, context, source);
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
    this.addEventListener('keydown', event => {
      if (event.key === 'Escape' && this.promptState) {
        event.preventDefault();
        this.cancelPrompt();
      } else if (event.key === 'Tab' && this.promptState) {
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
    this.scope?.removeEventListener(CausewaySemanticEvent.ACTION_REQUEST, this.onActionRequest);
    this.scope = null;
    this.generation += 1;
  }

  async beginAction(actionId, context, source = null) {
    if (this.promptState?.status === InteractionStatus.INVOKING) {
      return false;
    }
    const generation = ++this.generation;
    this.promptState = Object.freeze({
      status: InteractionStatus.PREPARING,
      actionId,
      context,
      source,
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

  async setParameterValue(parameterId, value, {recompute = true} = {}) {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    const focusState = this.#captureControlFocus();
    const generation = ++this.generation;
    const values = Object.freeze({...this.promptState.values, [parameterId]: value});
    this.promptState = Object.freeze({...this.promptState, values, error: null, status: InteractionStatus.EDITING});
    this.#publishPromptState();
    if (!recompute) {
      this.render();
      this.#restoreControlFocus(focusState);
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
    this.#restoreControlFocus(focusState);
    return true;
  }

  async submitPrompt() {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    const focusState = this.#captureControlFocus();
    const generation = ++this.generation;
    this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.VALIDATING, error: null});
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
      this.#restoreControlFocus(focusState);
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
    const source = this.promptState.source;
    const actionId = this.promptState.actionId;
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
    this.innerHTML = `<div class="causeway-interaction-controller">${promptMarkup}${resultMarkup}</div>`;
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
      return false;
    }
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

  #captureParameter(event) {
    const parameterId = event.target?.getAttribute?.('data-causeway-editor');
    if (!parameterId || !this.promptState) {
      return;
    }
    const parameter = this.promptState.parameters.find(candidate => candidate.id === parameterId);
    if (!parameter) {
      return;
    }
    const choices = parameter.state?.choices ?? parameter.enumValues ?? [];
    const rendered = renderCausewayEditor(this.#parameterEditorContext(parameter, choices), this._editorRegistry);
    const value = parseCausewayEditorValue(rendered.editor, {
      value: event.target.value,
      checked: event.target.checked,
      choices,
      suggestions: parameter.state?.suggestions ?? [],
      inputType: parameter.inputType
    });
    if (event.type === 'input' && parameter.fields.has('autoComplete')) {
      this.promptState = Object.freeze({
        ...this.promptState,
        values: Object.freeze({...this.promptState.values, [parameterId]: value}),
        error: null
      });
      clearTimeout(this.parameterTimer);
      this.parameterTimer = setTimeout(() => void this.setParameterValue(parameterId, value), 250);
      return;
    }
    void this.setParameterValue(parameterId, value);
  }

  #promptMarkup() {
    const state = this.promptState;
    if (state.status === InteractionStatus.PREPARING) {
      return `<div class="causeway-action-prompt causeway-loading" role="status" data-testid="action-prompt">Preparing action…</div>`;
    }
    if (state.parameters.length === 0 && state.status === InteractionStatus.INVOKING) {
      return `<div class="causeway-action-prompt causeway-loading" role="status" data-testid="action-prompt">Invoking ${escapeHtml(humanize(state.actionId))}…</div>`;
    }
    const parameterMarkup = state.parameters.map(parameter => this.#parameterMarkup(parameter)).join('');
    const busy = [InteractionStatus.VALIDATING, InteractionStatus.INVOKING].includes(state.status);
    const errorMarkup = state.error
      ? `<div id="${this.errorId}" class="causeway-action-prompt-error" role="alert">${escapeHtml(state.error)}</div>`
      : '';
    return `<dialog open class="causeway-action-prompt" role="dialog" aria-modal="true" aria-labelledby="${this.titleId}"${state.error ? ` aria-describedby="${this.errorId}"` : ''} data-testid="action-prompt">
  <form method="dialog" novalidate>
    <h2 id="${this.titleId}">${escapeHtml(humanize(state.actionId))}</h2>
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
    const disabledReason = typeof parameter.state?.disabled === 'string' ? parameter.state.disabled : '';
    const validityReason = typeof parameter.state?.validity === 'string' ? parameter.state.validity : '';
    const reason = parameter.state?.error || validityReason || disabledReason;
    return `<div class="causeway-action-parameter${reason ? ' causeway-error' : ''}" data-parameter="${escapeHtml(parameter.id)}">
  <label id="${editorContext.labelId}" for="${editorContext.inputId}">${escapeHtml(label)}</label>
  ${description ? `<span id="${editorContext.descriptionId}" class="causeway-action-parameter-description">${escapeHtml(description)}</span>` : ''}
  ${rendered.html}
  ${reason ? `<span id="${editorContext.errorId}" class="causeway-action-parameter-reason" role="alert">${escapeHtml(reason)}</span>` : ''}
</div>`;
  }

  #parameterEditorContext(parameter, choices) {
    const state = this.promptState;
    const reason = parameter.state?.error || parameter.state?.validity || parameter.state?.disabled || '';
    return {
      name: parameter.id,
      value: Object.prototype.hasOwnProperty.call(state.values, parameter.id)
        ? state.values[parameter.id]
        : parameter.state?.default ?? null,
      choices,
      suggestions: parameter.state?.suggestions ?? [],
      autoComplete: parameter.fields.has('autoComplete'),
      enumValues: parameter.enumValues,
      inputType: parameter.inputType,
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
      values: this.promptState.values,
      error: this.promptState.error,
      ...interactionTargetDetail(this.promptState.context)
    })));
  }

  #focusFirstControl() {
    queueMicrotask(() => this.querySelector?.('[data-causeway-editor]')?.focus?.());
  }

  #captureControlFocus() {
    const active = globalThis.document?.activeElement;
    if (!active || !this.contains?.(active)) {
      return null;
    }
    return Object.freeze({
      editor: active.getAttribute?.('data-causeway-editor') ?? null,
      action: active.getAttribute?.('data-causeway-action') ?? null,
      selectionStart: active.selectionStart ?? null,
      selectionEnd: active.selectionEnd ?? null
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
