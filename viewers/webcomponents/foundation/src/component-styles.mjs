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

export const CAUSEWAY_COMPONENT_STYLES = `
causeway-object {
  container-name: causeway-object;
  container-type: inline-size;
  display: block;
  --causeway-object-gap: 1rem;
  --causeway-object-group-border: 1px solid color-mix(in srgb, currentColor 24%, transparent);
  --causeway-object-group-radius: 0.5rem;
  --causeway-object-group-padding: 1rem;
  --causeway-object-tab-accent: LinkText;
}
.causeway-object {
  display: grid;
  gap: var(--causeway-object-gap);
}
.causeway-object-row {
  display: grid;
  gap: var(--causeway-object-gap);
  grid-template-columns: repeat(12, minmax(0, 1fr));
}
.causeway-object-column {
  grid-column: span var(--causeway-column-span, 12);
  min-inline-size: 0;
}
.causeway-object-group,
.causeway-object-tabs {
  border: var(--causeway-object-group-border);
  border-radius: var(--causeway-object-group-radius);
  margin-block: 0.5rem;
  min-inline-size: 0;
  padding: var(--causeway-object-group-padding);
}
.causeway-object-group > h2 {
  font-size: 1.1rem;
  margin-block: 0 0.75rem;
}
.causeway-object-tabs [role="tablist"] {
  border-block-end: var(--causeway-object-group-border);
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-block-end: 0.75rem;
}
.causeway-object-tabs [role="tab"] {
  background: none;
  border: 0;
  border-block-end: 0.2rem solid transparent;
  color: inherit;
  cursor: pointer;
  font: inherit;
  padding: 0.5rem 0.75rem;
}
.causeway-object-tabs [role="tab"][aria-selected="true"] {
  border-block-end-color: var(--causeway-object-tab-accent);
  color: var(--causeway-object-tab-accent);
  font-weight: 700;
}
.causeway-object-tabs [role="tab"]:focus-visible {
  outline: 0.2rem solid var(--causeway-object-tab-accent);
  outline-offset: 0.1rem;
}
.causeway-object [data-empty="true"] {
  display: none;
}
.causeway-object-layout-status {
  margin-block: 0.75rem;
}
.causeway-visually-hidden {
  block-size: 1px;
  clip: rect(0 0 0 0);
  clip-path: inset(50%);
  inline-size: 1px;
  overflow: hidden;
  position: absolute;
  white-space: nowrap;
}
@container causeway-object (max-width: 48rem) {
  .causeway-object-column {
    grid-column: 1 / -1;
  }
}
.causeway-object-header,
.causeway-property,
.causeway-action,
.causeway-collection {
  margin-block: 0.75rem;
}
.causeway-property,
.causeway-collection-table {
  inline-size: 100%;
}
.causeway-property-editing,
.causeway-action-prompt form {
  display: grid;
  gap: 0.6rem;
}
.causeway-property-editor-actions,
.causeway-action-prompt-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.causeway-property-editor input:not([type="checkbox"]),
.causeway-property-editor select,
.causeway-action-parameter input:not([type="checkbox"]),
.causeway-action-parameter select {
  box-sizing: border-box;
  inline-size: 100%;
  max-inline-size: 32rem;
}
.causeway-action-prompt {
  border: 1px solid currentColor;
  border-radius: 0.5rem;
  max-inline-size: min(36rem, calc(100vw - 2rem));
  padding: 1rem;
}
.causeway-action-parameter {
  display: grid;
  gap: 0.25rem;
  margin-block: 0.75rem;
}
.causeway-action-result {
  margin-block: 1rem;
  padding: 0.75rem;
}
.causeway-property-label,
.causeway-collection-label {
  font-weight: 600;
}
.causeway-object-link {
  align-items: baseline;
  background: none;
  border: 0;
  color: LinkText;
  cursor: pointer;
  display: inline-flex;
  gap: 0.5rem;
  padding: 0;
  text-decoration: underline;
}
.causeway-object-link-identity {
  font-size: 0.8em;
}
.causeway-error {
  color: #a40000;
}
.causeway-empty,
.causeway-value-null {
  font-style: italic;
}
.causeway-collection-table {
  border-collapse: collapse;
}
.causeway-collection-table th,
.causeway-collection-table td {
  border-block-end: 1px solid #d0d0d0;
  padding: 0.35rem 0.5rem;
  text-align: start;
}
`;

export function installCausewayComponentStyles(documentRoot = globalThis.document) {
  if (!documentRoot?.createElement || !documentRoot?.head) {
    return null;
  }
  const existing = documentRoot.getElementById?.('causeway-component-styles');
  if (existing) {
    return existing;
  }
  const style = documentRoot.createElement('style');
  style.id = 'causeway-component-styles';
  style.textContent = CAUSEWAY_COMPONENT_STYLES;
  documentRoot.head.appendChild(style);
  return style;
}
