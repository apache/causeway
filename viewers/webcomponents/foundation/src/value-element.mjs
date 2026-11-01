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

import {defaultValueRendererRegistry, renderCausewayValue} from './value-renderers.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};

export class CausewayValueElement extends HTMLElementBase {
  constructor() {
    super();
    this._valueState = Object.freeze({value: null});
    this._rendererRegistry = defaultValueRendererRegistry;
    this.rendered = null;
  }

  get valueState() {
    return this._valueState;
  }

  set valueState(value) {
    this._valueState = Object.freeze({...value});
    this.render();
  }

  get rendererRegistry() {
    return this._rendererRegistry;
  }

  set rendererRegistry(value) {
    this._rendererRegistry = value ?? defaultValueRendererRegistry;
    this.render();
  }

  connectedCallback() {
    this.render();
  }

  render() {
    this.rendered = renderCausewayValue(this._valueState, this._rendererRegistry);
    this.setAttribute?.('data-renderer', this.rendered.rendererId);
    this.innerHTML = this.rendered.html;
  }
}
