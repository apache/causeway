/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {composeMemberTooltip} from './description-presentation.mjs';
import {escapeHtml} from './rendering.mjs';

const MAX_ACTION_NAME = 512;
const MAX_ACTION_DESCRIPTION = 2_048;
const MAX_ICON_NOTATION = 256;
const FONT_AWESOME_TOKEN = /^(?:fa-)?[a-z0-9]+(?:-[a-z0-9]+)*$/;
const STYLE_TOKENS = new Set([
  'fa-brands', 'fa-duotone', 'fa-light', 'fa-regular', 'fa-sharp',
  'fa-sharp-duotone', 'fa-solid', 'fa-thin'
]);

export function normalizeActionPresentation(value = {}) {
  const name = boundedText(value.name ?? value.friendlyName, MAX_ACTION_NAME);
  const description = boundedText(value.description, MAX_ACTION_DESCRIPTION);
  return Object.freeze({
    name,
    description: normalizedDistinctDescription(description, name),
    areYouSure: value.areYouSure === true,
    icon: normalizeFontAwesomeIcon(value.cssClassFa, value.cssClassFaPosition)
  });
}

export function composeActionTooltip(description, disabledReason) {
  return composeMemberTooltip(description, disabledReason);
}

export function normalizeFontAwesomeIcon(value, position) {
  const notation = String(value ?? '').trim().slice(0, MAX_ICON_NOTATION);
  if (!notation) return null;
  const tokens = notation.split(/\s+/)
    .filter(Boolean)
    .map(token => token.startsWith('fa-') ? token.toLowerCase() : `fa-${token.toLowerCase()}`);
  if (tokens.length === 0 || tokens.some(token => !FONT_AWESOME_TOKEN.test(token))) return null;
  if (!tokens.some(token => STYLE_TOKENS.has(token))) tokens.unshift('fa-solid');
  return Object.freeze({
    classes: Object.freeze([...new Set(tokens)]),
    position: String(position ?? 'LEFT').toUpperCase() === 'RIGHT' ? 'RIGHT' : 'LEFT'
  });
}

export function renderActionContent(label, icon) {
  const text = `<span class="causeway-action-label">${escapeHtml(label ?? '')}</span>`;
  if (!icon) return text;
  const iconMarkup = `<i class="causeway-action-icon ${icon.classes.map(escapeHtml).join(' ')}" aria-hidden="true"></i>`;
  return icon.position === 'RIGHT' ? `${text}${iconMarkup}` : `${iconMarkup}${text}`;
}

export function appendActionContent(control, label, icon) {
  control.replaceChildren();
  if (icon && globalThis.document?.createElement) {
    const iconElement = document.createElement('i');
    iconElement.className = `causeway-action-icon ${icon.classes.join(' ')}`;
    iconElement.setAttribute('aria-hidden', 'true');
    if (icon.position !== 'RIGHT') control.appendChild(iconElement);
    appendText(control, label);
    if (icon.position === 'RIGHT') control.appendChild(iconElement);
    return;
  }
  control.textContent = label ?? '';
}

function appendText(control, label) {
  const span = document.createElement('span');
  span.className = 'causeway-action-label';
  span.textContent = label ?? '';
  control.appendChild(span);
}

function normalizedDistinctDescription(description, name) {
  return description && description.toLocaleLowerCase() !== name.toLocaleLowerCase() ? description : '';
}

function boundedText(value, maximum) {
  return String(value ?? '').replace(/[\u0000-\u001f\u007f]/g, '').trim().slice(0, maximum);
}
