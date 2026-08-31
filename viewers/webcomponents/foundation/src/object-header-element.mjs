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

export class CausewayObjectHeaderElement extends CausewayContextConsumerElement {
  createRequirement() {
    return {kind: 'header'};
  }

  renderComponentState(state) {
    if (state.status === 'idle' || state.status === 'schema-loading' || state.status === 'object-loading') {
      this.innerHTML = '<header class="causeway-object-header" aria-busy="true"><span role="status">Loading object…</span></header>';
      return;
    }
    if (state.status === 'terminal-error' || state.status === 'unsupported') {
      this.innerHTML = `<header class="causeway-object-header causeway-error" role="alert">${escapeHtml(errorMessage(state))}</header>`;
      return;
    }
    const metadata = state.data ?? {};
    const logicalTypeName = metadata.logicalTypeName ?? '';
    const id = metadata.id ?? '';
    const title = metadata.title || `${logicalTypeName}:${id}`;
    const icon = metadata.icon ? ` icon="${escapeHtml(metadata.icon)}"` : '';
    this.innerHTML = `<header class="causeway-object-header" aria-busy="false">
  <h1><cw-object-link class="causeway-object-header-link" logical-type="${escapeHtml(logicalTypeName)}" object-id="${escapeHtml(id)}" title="${escapeHtml(title)}"${icon}></cw-object-link></h1>
  <p class="causeway-object-identity" data-logical-type="${escapeHtml(logicalTypeName)}" data-object-id="${escapeHtml(id)}">${escapeHtml(logicalTypeName)}:${escapeHtml(id)}</p>
</header>`;
  }
}
