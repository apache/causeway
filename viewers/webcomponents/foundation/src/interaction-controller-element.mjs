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
  ActionPromptStyle,
  normalizeActionPresentation,
  normalizeActionPromptStyle
} from './action-presentation.mjs';
import {CausewaySemanticEvent} from './component-contracts.mjs';
import {createSemanticEvent} from './context-events.mjs';
import {
  boundedTooltipSection,
  DescriptionPresentation
} from './description-presentation.mjs';
import {defaultEditorRegistry, parseCausewayEditorValue, renderCausewayEditor} from './editor-registry.mjs';
import {causewayReferenceWidgetConfiguration} from './reference-widget.mjs';
import {escapeHtml} from './rendering.mjs';
import {normalizeActionParameterConfigurations} from './parameter-element.mjs';
import {
  CausewayTemporalRangeStatus,
  resolveCausewayTemporalRange,
  validateCausewayTemporalRange
} from './temporal-range.mjs';
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
    this.inlinePromptSurface = null;
    this.inlinePromptRestoration = null;
    this.promptDragState = null;
    this.onActionRequest = event => {
      const actionId = event.detail?.actionId;
      const context = event.detail?.context;
      const presentation = event.detail?.presentation;
      const origin = event.target;
      const source = focusRestoreTarget(origin);
      queueMicrotask(() => {
        if (!event.defaultPrevented && actionId && context) {
          void this.beginAction(actionId, context, source, presentation, origin);
        }
      });
    };
    this.onPromptClick = event => {
      const action = event.target?.getAttribute?.('data-causeway-action') ?? event.target?.dataset?.causewayAction;
      if (action === 'cancel') {
        this.cancelPrompt();
      } else if (action === 'submit') {
        void this.submitPrompt();
      } else if (action === 'confirm') {
        void this.confirmPrompt();
      } else if (action === 'dismiss-result') {
        this.dismissResult();
      }
    };
    this.onPromptInput = event => this.#captureParameter(event);
    this.onPromptChange = event => this.#captureParameter(event);
    this.onPromptFocusout = event => this.#captureParameter(event, {commit: true});
    this.onPromptEditorCommit = event => {
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
    };
    this.onPromptReferenceSearch = event => {
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
    };
    this.onPromptEditorEscape = event => {
      if (this.promptState) {
        event.stopPropagation();
        this.cancelPrompt();
      }
    };
    this.onPromptEditorLoadFailed = event => {
      if (this.promptState) {
        event.stopPropagation();
        this.render();
        this.#focusFirstControl();
      }
    };
    this.onPromptKeydown = event => {
      if (event.key === 'Escape' && this.promptState) {
        event.preventDefault();
        this.cancelPrompt();
      } else if (event.key === 'Tab' && this.promptState) {
        this.#recordPromptFocusDestination(event);
        this.#containPromptFocus(event);
      }
    };
    this.onPromptPointerDown = event => this.#startPromptDrag(event);
    this.onPromptPointerMove = event => this.#movePromptDrag(event);
    this.onPromptPointerUp = () => this.#stopPromptDrag();
    this.#addPromptEventListeners(this);
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
    this.#removeInlinePrompt();
    this.#stopPromptDrag();
    clearTimeout(this.parameterTimer);
    this.autoCompleteController?.abort();
    this.autoCompleteController = null;
    this.autoCompleteGeneration += 1;
    this.scope?.removeEventListener(CausewaySemanticEvent.ACTION_REQUEST, this.onActionRequest);
    this.scope = null;
    this.generation += 1;
  }

  async beginAction(actionId, context, source = null, presentation = null, origin = null) {
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
      origin,
      presentation: normalizeActionPresentation({
        name: presentation?.name || humanize(actionId),
        description: presentation?.description,
        areYouSure: presentation?.areYouSure,
        promptStyle: presentation?.promptStyle,
        cssClassFa: presentation?.icon?.classes?.join(' '),
        cssClassFaPosition: presentation?.icon?.position
      }),
      parameterPresentations: normalizeActionParameterConfigurations(presentation?.parameters),
      values: Object.freeze({}),
      parameters: Object.freeze([]),
      temporalRanges: Object.freeze([]),
      parameterRangeErrors: Object.freeze([]),
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
      return this.promptState.presentation.areYouSure
        ? this.#requestConfirmation()
        : this.#invoke(actionId, context, {}, source, generation);
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
      temporalRanges: resolveParameterTemporalRanges(parameters, this.promptState.parameterPresentations),
      parameterRangeErrors: Object.freeze([]),
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
    const parameterRangeErrors = this.#validateParameterTemporalRanges(values);
    const rangeError = parameterRangeErrors.find(candidate => candidate.parameter === parameterId)?.error ?? null;
    if (parameterRangeErrors.length > 0) {
      if (rangeError) this.validatedParameterIds.add(parameterId);
      this.promptState = Object.freeze({
        ...this.promptState,
        values,
        parameterRangeErrors,
        error: null,
        status: InteractionStatus.FAILED
      });
      this.#publishPromptState();
      this.render();
      this.#restoreControlFocus(effectiveFocusState);
      return false;
    }
    this.promptState = Object.freeze({
      ...this.promptState,
      values,
      parameterRangeErrors,
      error: null,
      status: InteractionStatus.EDITING
    });
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
    const parameterRangeErrors = this.#validateParameterTemporalRanges(this.promptState.values);
    if (parameterRangeErrors.length > 0) {
      this.promptState = Object.freeze({
        ...this.promptState,
        parameterRangeErrors,
        status: InteractionStatus.FAILED,
        error: null
      });
      this.#publishPromptState();
      this.render();
      this.#focusFirstInvalidControl();
      return false;
    }
    this.promptState = Object.freeze({...this.promptState, parameterRangeErrors, status: InteractionStatus.VALIDATING, error: null});
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
    if (this.promptState.presentation.areYouSure) {
      return this.#requestConfirmation();
    }
    return this.#invoke(
      this.promptState.actionId,
      this.promptState.context,
      this.promptState.values,
      this.promptState.source,
      generation
    );
  }

  confirmPrompt() {
    if (!this.promptState || this.promptState.status !== InteractionStatus.CONFIRMING) {
      return false;
    }
    return this.#invoke(
      this.promptState.actionId,
      this.promptState.context,
      this.promptState.values,
      this.promptState.source,
      this.generation
    );
  }

  cancelPrompt() {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    if (this.promptState.status === InteractionStatus.CONFIRMING
        && this.promptState.parameters.length > 0) {
      this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.EDITING, error: null});
      this.#publishPromptState();
      this.render();
      this.#focusFirstControl();
      return true;
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

  #addPromptEventListeners(target) {
    target?.addEventListener?.('click', this.onPromptClick);
    target?.addEventListener?.('input', this.onPromptInput);
    target?.addEventListener?.('change', this.onPromptChange);
    target?.addEventListener?.('focusout', this.onPromptFocusout);
    target?.addEventListener?.('causeway-editor-commit', this.onPromptEditorCommit);
    target?.addEventListener?.('causeway-reference-search', this.onPromptReferenceSearch);
    target?.addEventListener?.('causeway-reference-escape', this.onPromptEditorEscape);
    target?.addEventListener?.('causeway-field-escape', this.onPromptEditorEscape);
    target?.addEventListener?.('causeway-reference-load-failed', this.onPromptEditorLoadFailed);
    target?.addEventListener?.('causeway-field-load-failed', this.onPromptEditorLoadFailed);
    target?.addEventListener?.('keydown', this.onPromptKeydown);
    target?.addEventListener?.('pointerdown', this.onPromptPointerDown);
    target?.addEventListener?.('mousedown', this.onPromptPointerDown);
  }

  #removePromptEventListeners(target) {
    target?.removeEventListener?.('click', this.onPromptClick);
    target?.removeEventListener?.('input', this.onPromptInput);
    target?.removeEventListener?.('change', this.onPromptChange);
    target?.removeEventListener?.('focusout', this.onPromptFocusout);
    target?.removeEventListener?.('causeway-editor-commit', this.onPromptEditorCommit);
    target?.removeEventListener?.('causeway-reference-search', this.onPromptReferenceSearch);
    target?.removeEventListener?.('causeway-reference-escape', this.onPromptEditorEscape);
    target?.removeEventListener?.('causeway-field-escape', this.onPromptEditorEscape);
    target?.removeEventListener?.('causeway-reference-load-failed', this.onPromptEditorLoadFailed);
    target?.removeEventListener?.('causeway-field-load-failed', this.onPromptEditorLoadFailed);
    target?.removeEventListener?.('keydown', this.onPromptKeydown);
    target?.removeEventListener?.('pointerdown', this.onPromptPointerDown);
    target?.removeEventListener?.('mousedown', this.onPromptPointerDown);
  }

  #inlineAssociation() {
    const origin = this.promptState?.origin;
    if (origin?.localName !== 'cw-action') return null;
    const parent = origin.parentNode;
    if (parent?.localName === 'cw-property') {
      const children = [...(parent.children ?? parent.childNodes ?? [])];
      const primary = children.find(child => child?.hasAttribute?.('data-causeway-member-primary'));
      const actions = children.filter(child => child?.localName === 'cw-action');
      return primary ? {container: parent, nodes: [primary, ...actions]} : null;
    }
    if (!parent?.hasAttribute?.('data-causeway-action-group')) return null;
    const composition = parent.parentNode;
    if (!composition?.hasAttribute?.('data-causeway-associated-member')) return null;
    const property = [...(composition.children ?? composition.childNodes ?? [])]
      .find(child => child?.localName === 'cw-property');
    return property ? {container: composition, nodes: [property, parent]} : null;
  }

  #removeInlinePrompt() {
    if (this.inlinePromptSurface) {
      this.#removePromptEventListeners(this.inlinePromptSurface);
      if (typeof this.inlinePromptSurface.remove === 'function') {
        this.inlinePromptSurface.remove();
      } else {
        this.inlinePromptSurface.parentNode?.removeChild?.(this.inlinePromptSurface);
      }
      this.inlinePromptSurface = null;
    }
    if (this.inlinePromptRestoration) {
      for (const {node, hidden} of this.inlinePromptRestoration.nodes) {
        node.hidden = hidden;
      }
      this.inlinePromptRestoration.container.removeAttribute?.('data-causeway-inline-action-prompt');
      this.inlinePromptRestoration = null;
    }
  }

  #installInlinePrompt(markup, association) {
    const surface = globalThis.document?.createElement?.('div');
    if (!surface) return false;
    surface.className = 'causeway-inline-action-prompt-portal';
    surface.setAttribute('data-causeway-inline-action-prompt-portal', '');
    surface.innerHTML = markup;
    this.inlinePromptRestoration = {
      container: association.container,
      nodes: association.nodes.map(node => ({node, hidden: node.hidden === true}))
    };
    for (const node of association.nodes) node.hidden = true;
    association.container.setAttribute?.('data-causeway-inline-action-prompt', '');
    association.container.appendChild?.(surface);
    this.inlinePromptSurface = surface;
    this.#addPromptEventListeners(surface);
    return true;
  }

  #activePromptRoot() {
    return this.inlinePromptSurface ?? this;
  }

  #effectivePromptStyle() {
    if (this.promptState?.status === InteractionStatus.CONFIRMING) {
      return ActionPromptStyle.DIALOG_MODAL;
    }
    const style = normalizeActionPromptStyle(this.promptState?.presentation?.promptStyle);
    return style === ActionPromptStyle.INLINE && !this.#inlineAssociation()
      ? ActionPromptStyle.DIALOG_MODAL
      : style;
  }

  render() {
    this.#removeInlinePrompt();
    this.#stopPromptDrag();
    const promptStyle = this.promptState ? this.#effectivePromptStyle() : ActionPromptStyle.DIALOG_MODAL;
    const promptMarkup = this.promptState ? this.#promptMarkup(promptStyle) : '';
    const inlineAssociation = promptStyle === ActionPromptStyle.INLINE ? this.#inlineAssociation() : null;
    const resultMarkup = this.resultState ? this.#resultMarkup() : '';
    this.renderingPrompt = true;
    try {
      this.innerHTML = `<div class="causeway-interaction-controller">${inlineAssociation ? '' : promptMarkup}${resultMarkup}</div>`;
      if (inlineAssociation) {
        this.#installInlinePrompt(promptMarkup, inlineAssociation);
      }
    } finally {
      this.renderingPrompt = false;
    }
  }

  #requestConfirmation() {
    if (!this.promptState || this.promptState.status === InteractionStatus.INVOKING) {
      return false;
    }
    this.promptState = Object.freeze({...this.promptState, status: InteractionStatus.CONFIRMING, error: null});
    this.#publishPromptState();
    this.render();
    this.#focusFirstControl();
    return true;
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
        parameterRangeErrors: updateParameterRangeErrors(this.promptState.parameterRangeErrors, parameterId, null),
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
        this.promptState = Object.freeze({
          ...this.promptState,
          values,
          parameterRangeErrors: updateParameterRangeErrors(this.promptState.parameterRangeErrors, parameterId, null),
          status: InteractionStatus.EDITING,
          error: null
        });
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
        parameterRangeErrors: updateParameterRangeErrors(this.promptState.parameterRangeErrors, parameterId, null),
        status: InteractionStatus.FAILED,
        error: error.message
      });
      this.#publishPromptState();
      this.render();
    }
  }

  #promptMarkup(promptStyle) {
    const state = this.promptState;
    const actionName = state.presentation?.name || humanize(state.actionId);
    const titleAttributes = promptStyle === ActionPromptStyle.DIALOG_MODAL
      ? ' class="causeway-action-prompt-title" data-causeway-dialog-drag-handle'
      : ' class="causeway-action-prompt-title"';
    if (state.status === InteractionStatus.PREPARING) {
      return `${this.#promptSurfaceOpen(promptStyle)}
  <h2 id="${this.titleId}"${titleAttributes}>${escapeHtml(actionName)}</h2>
  <span role="status">Preparing action…</span>
${this.#promptSurfaceClose(promptStyle)}`;
    }
    if (state.parameters.length === 0 && state.status === InteractionStatus.INVOKING) {
      return `${this.#promptSurfaceOpen(promptStyle)}
  <h2 id="${this.titleId}"${titleAttributes}>${escapeHtml(actionName)}</h2>
  <span role="status">Invoking ${escapeHtml(actionName)}…</span>
${this.#promptSurfaceClose(promptStyle)}`;
    }
    if (state.status === InteractionStatus.CONFIRMING) {
      return `${this.#promptSurfaceOpen(ActionPromptStyle.DIALOG_MODAL, {
        testId: 'action-confirmation',
        role: 'alertdialog',
        describedBy: this.descriptionId,
        extraClass: 'causeway-action-confirmation'
      })}
  <form method="dialog">
    <h2 id="${this.titleId}" class="causeway-action-prompt-title" data-causeway-dialog-drag-handle>Confirm ${escapeHtml(actionName)}</h2>
    <p id="${this.descriptionId}" class="causeway-action-confirmation-message">Are you sure you want to invoke ${escapeHtml(actionName)}? This action cannot be undone.</p>
    <div class="causeway-action-prompt-actions">
      <button type="button" class="causeway-action-confirm" data-causeway-action="confirm" data-testid="action-confirmation-confirm">Confirm</button>
      <button type="button" data-causeway-action="cancel" data-testid="action-confirmation-cancel">Cancel</button>
    </div>
    <span class="causeway-action-prompt-status" role="status">${escapeHtml(promptStatusLabel(state.status))}</span>
  </form>
${this.#promptSurfaceClose(ActionPromptStyle.DIALOG_MODAL)}`;
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
    return `${this.#promptSurfaceOpen(promptStyle, {describedBy})}
  <form${promptStyle === ActionPromptStyle.INLINE ? '' : ' method="dialog"'} novalidate>
    <h2 id="${this.titleId}"${titleAttributes}>${escapeHtml(actionName)}</h2>
    ${descriptionMarkup}
    ${parameterMarkup}
    ${errorMarkup}
    <div class="causeway-action-prompt-actions">
      <button type="button" data-causeway-action="submit" data-testid="action-prompt-submit" ${busy || state.error ? 'disabled' : ''}>${state.status === InteractionStatus.INVOKING ? 'Invoking…' : 'Invoke'}</button>
      <button type="button" data-causeway-action="cancel" data-testid="action-prompt-cancel" ${state.status === InteractionStatus.INVOKING ? 'disabled' : ''}>Cancel</button>
    </div>
    <span class="causeway-action-prompt-status" role="status">${escapeHtml(promptStatusLabel(state.status))}</span>
  </form>
${this.#promptSurfaceClose(promptStyle)}`;
  }

  #promptSurfaceOpen(promptStyle, {
    testId = 'action-prompt',
    role = 'dialog',
    describedBy = '',
    extraClass = ''
  } = {}) {
    const inline = promptStyle === ActionPromptStyle.INLINE;
    const styleClass = promptStyle === ActionPromptStyle.DIALOG_SIDEBAR
      ? 'causeway-action-prompt-sidebar'
      : inline ? 'causeway-action-prompt-inline' : 'causeway-action-prompt-modal';
    const tag = inline ? 'section' : 'dialog';
    const effectiveRole = inline ? 'region' : role;
    return `<${tag}${inline ? '' : ' open'} class="causeway-action-prompt ${styleClass}${extraClass ? ` ${extraClass}` : ''}" role="${effectiveRole}"${inline ? '' : ' aria-modal="true"'} aria-labelledby="${this.titleId}"${describedBy ? ` aria-describedby="${describedBy}"` : ''} data-prompt-style="${promptStyle}" data-testid="${testId}">`;
  }

  #promptSurfaceClose(promptStyle) {
    return promptStyle === ActionPromptStyle.INLINE ? '</section>' : '</dialog>';
  }

  #parameterMarkup(parameter) {
    if (parameter.state?.hidden === true) {
      return '';
    }
    const choices = parameter.state?.choices ?? parameter.enumValues ?? [];
    const presentation = this.#parameterPresentation(parameter);
    const editorContext = this.#parameterEditorContext(parameter, choices, presentation);
    const rendered = renderCausewayEditor(editorContext, this._editorRegistry);
    const temporalRange = this.#parameterTemporalRange(parameter.id);
    const rangeStatus = [CausewayTemporalRangeStatus.VALID, CausewayTemporalRangeStatus.INVALID].includes(temporalRange?.status)
      ? ` data-causeway-temporal-range-status="${temporalRange.status}"`
      : '';
    const reason = protectedPromptText(this.promptState, this.#parameterReason(parameter));
    const tooltip = presentation.descriptionAs === DescriptionPresentation.TOOLTIP
      ? boundedTooltipSection(presentation.description)
      : '';
    const labelClass = `causeway-action-parameter-label${tooltip ? ' causeway-member-tooltip' : ''}`;
    const labelAttributes = tooltip
      ? ` tabindex="0" data-tooltip="${escapeHtml(tooltip)}" aria-describedby="${editorContext.descriptionId}"`
      : '';
    const descriptionClass = `causeway-action-parameter-description${presentation.descriptionAs === DescriptionPresentation.TOOLTIP ? ' causeway-visually-hidden' : ''}`;
    return `<div class="causeway-action-parameter${reason ? ' causeway-error' : ''}" data-parameter="${escapeHtml(parameter.id)}"${rangeStatus}${presentation.multiLine ? ` data-multi-line="${presentation.multiLine}"` : ''}>
  <label id="${editorContext.labelId}" class="${labelClass}" for="${editorContext.inputId}"${labelAttributes}>${escapeHtml(presentation.label)}</label>
  ${presentation.description ? `<span id="${editorContext.descriptionId}" class="${descriptionClass}">${escapeHtml(presentation.description)}</span>` : ''}
  ${rendered.html}
  ${reason ? `<span id="${editorContext.errorId}" class="causeway-action-parameter-reason" role="alert">${escapeHtml(reason)}</span>` : ''}
</div>`;
  }

  #parameterReason(parameter) {
    const localRangeReason = this.validatedParameterIds.has(parameter.id)
      ? this.promptState?.parameterRangeErrors
        ?.find(candidate => candidate.parameter === parameter.id)?.error?.message ?? ''
      : '';
    const validationReason = this.validatedParameterIds.has(parameter.id)
      ? parameter.state?.error || parameter.state?.validity || ''
      : '';
    return localRangeReason || validationReason || parameter.state?.disabled || '';
  }

  #parameterEditorContext(parameter, choices, presentation = this.#parameterPresentation(parameter)) {
    const state = this.promptState;
    const reason = this.#parameterReason(parameter);
    return {
      name: parameter.id,
      label: presentation.label,
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
      min: this.#parameterTemporalRange(parameter.id)?.status === CausewayTemporalRangeStatus.VALID
        ? this.#parameterTemporalRange(parameter.id).min
        : null,
      max: this.#parameterTemporalRange(parameter.id)?.status === CausewayTemporalRangeStatus.VALID
        ? this.#parameterTemporalRange(parameter.id).max
        : null,
      required: parameter.inputType?.kind === 'NON_NULL',
      multiLine: presentation.multiLine,
      inputId: `causeway-action-parameter-${parameter.id}`,
      labelId: `causeway-action-parameter-${parameter.id}-label`,
      descriptionId: presentation.description ? `causeway-action-parameter-${parameter.id}-description` : '',
      errorId: reason ? `causeway-action-parameter-${parameter.id}-error` : '',
      testId: `action-prompt-parameter-${parameter.id}`,
      disabled: Boolean(parameter.state?.disabled) || state.status === InteractionStatus.INVOKING
    };
  }

  #validateParameterTemporalRanges(values) {
    return this.promptState.parameters.reduce((errors, parameter) => {
      const value = Object.prototype.hasOwnProperty.call(values, parameter.id)
        ? values[parameter.id]
        : parameter.state?.default ?? null;
      return updateParameterRangeErrors(
        errors,
        parameter.id,
        validateCausewayTemporalRange(value, this.#parameterTemporalRange(parameter.id))
      );
    }, Object.freeze([]));
  }

  #parameterTemporalRange(parameterId) {
    return this.promptState?.temporalRanges
      ?.find(candidate => candidate.parameter === parameterId)?.range ?? null;
  }

  #parameterPresentation(parameter) {
    const authored = this.promptState?.parameterPresentations
      ?.find(candidate => candidate.parameter === parameter.id);
    const label = authored && authored.named !== null ? authored.named : humanize(parameter.id);
    const candidateDescription = authored && authored.describedAs !== null
      ? authored.describedAs
      : String(parameter.description ?? '');
    const description = candidateDescription.trim().toLocaleLowerCase() === label.trim().toLocaleLowerCase()
      ? ''
      : candidateDescription;
    return Object.freeze({
      label,
      description,
      descriptionAs: authored?.descriptionAs ?? DescriptionPresentation.LABEL,
      multiLine: authored?.multiLine ?? null
    });
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
    queueMicrotask(() => {
      const root = this.#activePromptRoot();
      return (
        root.querySelector?.('[data-causeway-editor]')
        ?? root.querySelector?.('[data-causeway-action="confirm"]')
        ?? root.querySelector?.('[data-causeway-action="submit"]')
      )?.focus?.();
    });
  }

  #focusFirstInvalidControl() {
    queueMicrotask(() => {
      const root = this.#activePromptRoot();
      const invalid = root.querySelector?.('.causeway-action-parameter.causeway-error [data-causeway-editor]')
        ?? root.querySelector?.('[data-causeway-editor]');
      invalid?.focus?.();
    });
  }

  #captureControlFocus() {
    const active = globalThis.document?.activeElement;
    return active && this.#activePromptRoot().contains?.(active) ? this.#controlFocusState(active) : null;
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
      const controls = [...(this.#activePromptRoot().querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])];
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
    const controls = [...(this.#activePromptRoot().querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])]
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
    if (this.#effectivePromptStyle() === ActionPromptStyle.INLINE) {
      return;
    }
    const controls = [...(this.#activePromptRoot().querySelectorAll?.('[data-causeway-editor], [data-causeway-action]') ?? [])]
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

  #startPromptDrag(event) {
    if (this.#effectivePromptStyle() !== ActionPromptStyle.DIALOG_MODAL
        || !event.target?.hasAttribute?.('data-causeway-dialog-drag-handle')) {
      return;
    }
    const dialog = event.target.closest?.('.causeway-action-prompt-modal');
    const rect = dialog?.getBoundingClientRect?.();
    if (!dialog || !rect) return;
    event.preventDefault?.();
    this.#stopPromptDrag();
    this.promptDragState = {
      dialog,
      offsetX: Number(event.clientX ?? rect.left) - rect.left,
      offsetY: Number(event.clientY ?? rect.top) - rect.top,
      width: rect.width,
      height: rect.height
    };
    globalThis.document?.addEventListener?.('pointermove', this.onPromptPointerMove);
    globalThis.document?.addEventListener?.('pointerup', this.onPromptPointerUp);
    globalThis.document?.addEventListener?.('pointercancel', this.onPromptPointerUp);
    globalThis.document?.addEventListener?.('mousemove', this.onPromptPointerMove);
    globalThis.document?.addEventListener?.('mouseup', this.onPromptPointerUp);
  }

  #movePromptDrag(event) {
    const drag = this.promptDragState;
    if (!drag) return;
    const viewportWidth = globalThis.innerWidth ?? globalThis.document?.documentElement?.clientWidth ?? drag.width;
    const viewportHeight = globalThis.innerHeight ?? globalThis.document?.documentElement?.clientHeight ?? drag.height;
    const left = clamp(Number(event.clientX ?? 0) - drag.offsetX, 0, Math.max(0, viewportWidth - drag.width));
    const top = clamp(Number(event.clientY ?? 0) - drag.offsetY, 0, Math.max(0, viewportHeight - drag.height));
    drag.dialog.style.left = `${left}px`;
    drag.dialog.style.top = `${top}px`;
    drag.dialog.style.transform = 'none';
  }

  #stopPromptDrag() {
    this.promptDragState = null;
    globalThis.document?.removeEventListener?.('pointermove', this.onPromptPointerMove);
    globalThis.document?.removeEventListener?.('pointerup', this.onPromptPointerUp);
    globalThis.document?.removeEventListener?.('pointercancel', this.onPromptPointerUp);
    globalThis.document?.removeEventListener?.('mousemove', this.onPromptPointerMove);
    globalThis.document?.removeEventListener?.('mouseup', this.onPromptPointerUp);
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

function resolveParameterTemporalRanges(parameters, presentations) {
  const ranges = [];
  for (const parameter of parameters ?? []) {
    const authored = presentations?.find(candidate => candidate.parameter === parameter.id);
    if (!authored || (authored.min == null && authored.max == null)) continue;
    const range = resolveCausewayTemporalRange({
      semanticType: semanticTypeName({
        semanticType: parameter.state?.datatype,
        inputType: parameter.inputType
      }),
      min: authored.min,
      max: authored.max
    });
    if ([CausewayTemporalRangeStatus.VALID, CausewayTemporalRangeStatus.INVALID].includes(range.status)) {
      ranges.push(Object.freeze({parameter: parameter.id, range}));
    }
  }
  return Object.freeze(ranges);
}

function updateParameterRangeErrors(errors, parameterId, error) {
  const updated = (errors ?? []).filter(candidate => candidate.parameter !== parameterId);
  if (error) updated.push(Object.freeze({parameter: parameterId, error}));
  return Object.freeze(updated);
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
    [InteractionStatus.CONFIRMING]: 'Confirmation required',
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

function clamp(value, minimum, maximum) {
  return Math.min(Math.max(value, minimum), maximum);
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}
