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

export class CausewayBreadcrumbsElement extends CausewayContextConsumerElement {
  createRequirement() {
    return {kind: 'breadcrumbs'};
  }

  renderComponentState(state) {
    if (['idle', 'schema-loading', 'object-loading'].includes(state.status)) {
      this.hidden = false;
      this.innerHTML = `<nav class="causeway-breadcrumbs" aria-label="Breadcrumb" aria-busy="true">
  <span role="status">Loading breadcrumbs…</span>
</nav>`;
      return;
    }
    if (['terminal-error', 'unsupported', 'partial-error'].includes(state.status)) {
      this.hidden = false;
      this.innerHTML = `<nav class="causeway-breadcrumbs causeway-error" aria-label="Breadcrumb" aria-busy="false">
  <span role="alert">${escapeHtml(errorMessage(state))}</span>
</nav>`;
      return;
    }
    const metadata = state.data ?? {};
    const ancestors = Array.isArray(metadata.breadcrumbs)
      ? metadata.breadcrumbs.filter(validBreadcrumb)
      : [];
    if (ancestors.length === 0) {
      this.hidden = true;
      this.innerHTML = '';
      return;
    }
    this.hidden = false;
    const currentTitle = metadata.title || `${metadata.logicalTypeName ?? ''}:${metadata.id ?? ''}`;
    const ancestorItems = ancestors.map(ancestor => `<li class="causeway-breadcrumbs-item">
    <cw-object-link logical-type="${escapeHtml(ancestor.logicalTypeName)}" object-id="${escapeHtml(ancestor.id)}" title="${escapeHtml(ancestor.title)}"${ancestor.icon ? ` icon="${escapeHtml(ancestor.icon)}"` : ''}></cw-object-link>
  </li>`).join('\n  ');
    this.innerHTML = `<nav class="causeway-breadcrumbs" aria-label="Breadcrumb" aria-busy="false">
  <ol class="causeway-breadcrumbs-list">
  ${ancestorItems}${ancestorItems ? '\n  ' : ''}<li class="causeway-breadcrumbs-item causeway-breadcrumbs-current" aria-current="page">${escapeHtml(currentTitle)}</li>
  </ol>
</nav>`;
  }
}

function validBreadcrumb(entry) {
  return entry
    && typeof entry.logicalTypeName === 'string' && entry.logicalTypeName.length > 0
    && typeof entry.id === 'string' && entry.id.length > 0
    && typeof entry.title === 'string' && entry.title.length > 0;
}
