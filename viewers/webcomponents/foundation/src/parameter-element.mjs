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
import {normalizeDescriptionPresentation} from './description-presentation.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
const MAX_PARAMETER_ID = 256;
const MAX_PARAMETER_NAME = 512;
const MAX_PARAMETER_DESCRIPTION = 2_048;
const MAX_TEMPORAL_BOUND = 128;
const MAX_MULTI_LINE = 50;

export class CausewayParameterElement extends HTMLElementBase {
  static get observedAttributes() {
    return ['id', 'named', 'described-as', 'description-as', 'multi-line', 'min', 'max'];
  }

  get named() {
    return this.getAttribute('named') || '';
  }

  set named(value) {
    setOptionalAttribute(this, 'named', value);
  }

  get describedAs() {
    return this.getAttribute('described-as') || '';
  }

  set describedAs(value) {
    setOptionalAttribute(this, 'described-as', value);
  }

  get descriptionAs() {
    return normalizeDescriptionPresentation(this.getAttribute('description-as'));
  }

  set descriptionAs(value) {
    setOptionalAttribute(this, 'description-as', value);
  }

  get multiLine() {
    return normalizedMultiLine(this.getAttribute('multi-line')) ?? 0;
  }

  set multiLine(value) {
    const rows = normalizedMultiLine(value);
    if (rows) this.setAttribute('multi-line', String(rows));
    else this.removeAttribute('multi-line');
  }

  get min() {
    return this.getAttribute('min') || '';
  }

  set min(value) {
    setOptionalAttribute(this, 'min', value);
  }

  get max() {
    return this.getAttribute('max') || '';
  }

  set max(value) {
    setOptionalAttribute(this, 'max', value);
  }

  get configuration() {
    return actionParameterConfiguration(this);
  }

  connectedCallback() {
    this.hidden = true;
    this.#notify();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue !== newValue && this.isConnected) this.#notify();
  }

  #notify() {
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.ACTION_PARAMETER_CONFIGURATION,
      {parameter: this.configuration}
    ));
  }
}

export function actionParameterConfiguration(element) {
  return normalizeActionParameterConfiguration({
    parameter: element?.id,
    named: authoredAttribute(element, 'named'),
    describedAs: authoredAttribute(element, 'described-as'),
    descriptionAs: authoredAttribute(element, 'description-as'),
    multiLine: authoredAttribute(element, 'multi-line'),
    min: authoredAttribute(element, 'min'),
    max: authoredAttribute(element, 'max')
  });
}

export function normalizeActionParameterConfiguration(value = {}) {
  const parameter = boundedText(value.parameter ?? value.id, MAX_PARAMETER_ID).trim();
  const named = optionalBoundedText(value.named, MAX_PARAMETER_NAME);
  const describedAs = optionalBoundedText(value.describedAs, MAX_PARAMETER_DESCRIPTION);
  const descriptionAs = value.descriptionAs == null
    ? null
    : normalizeDescriptionPresentation(value.descriptionAs);
  const multiLine = normalizedMultiLine(value.multiLine);
  const min = optionalBoundedText(value.min, MAX_TEMPORAL_BOUND);
  const max = optionalBoundedText(value.max, MAX_TEMPORAL_BOUND);
  return Object.freeze({parameter, named, describedAs, descriptionAs, multiLine, min, max});
}

export function normalizeActionParameterConfigurations(values = []) {
  const byParameter = new Map();
  for (const value of values ?? []) {
    const configuration = normalizeActionParameterConfiguration(value);
    if (configuration.parameter) byParameter.set(configuration.parameter, configuration);
  }
  return Object.freeze([...byParameter.values()]);
}

function authoredAttribute(element, name) {
  return element?.hasAttribute?.(name) ? element.getAttribute(name) : null;
}

function normalizedMultiLine(value) {
  if (value == null || value === '') return null;
  const rows = Number(value);
  return Number.isSafeInteger(rows) && rows > 1 ? Math.min(rows, MAX_MULTI_LINE) : null;
}

function optionalBoundedText(value, maximum) {
  return value == null ? null : boundedText(value, maximum);
}

function boundedText(value, maximum) {
  return String(value ?? '').replace(/[\u0000-\u001f\u007f]/g, '').slice(0, maximum);
}

function setOptionalAttribute(element, name, value) {
  if (value == null) element.removeAttribute(name);
  else element.setAttribute(name, String(value));
}
