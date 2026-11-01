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

const SVG_NAMESPACE = 'http://www.w3.org/2000/svg';
export const PREVIEW_TOGGLE_ICON_CLASS = 'causeway-collection-preview-icon';
export const PREVIEW_TOGGLE_ICON_VIEW_BOX = '0 0 20 20';
export const PREVIEW_TOGGLE_ICON_PATH = 'M7 4l6 6-6 6';

export const PREVIEW_TOGGLE_ICON_MARKUP = `<svg class="${PREVIEW_TOGGLE_ICON_CLASS}" aria-hidden="true" focusable="false" viewBox="${PREVIEW_TOGGLE_ICON_VIEW_BOX}"><path d="${PREVIEW_TOGGLE_ICON_PATH}"></path></svg>`;

export function createPreviewToggleIcon(ownerDocument = globalThis.document) {
    if (!ownerDocument) return null;
    const create = name => ownerDocument.createElementNS?.(SVG_NAMESPACE, name) ?? ownerDocument.createElement?.(name);
    const icon = create('svg');
    const path = create('path');
    if (!icon || !path) return null;
    icon.setAttribute('class', PREVIEW_TOGGLE_ICON_CLASS);
    icon.setAttribute('aria-hidden', 'true');
    icon.setAttribute('focusable', 'false');
    icon.setAttribute('viewBox', PREVIEW_TOGGLE_ICON_VIEW_BOX);
    path.setAttribute('d', PREVIEW_TOGGLE_ICON_PATH);
    icon.appendChild(path);
    return icon;
}
