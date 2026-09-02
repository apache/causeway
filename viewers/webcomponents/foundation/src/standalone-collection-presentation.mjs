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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {normalizeDescriptionPresentation} from './description-presentation.mjs';

const MAX_PRESENTATION_COLUMNS = 32;

export function standaloneCollectionPresentation(element) {
  if (!element) return null;
  return normalizeStandaloneCollectionPresentation({
    named: element.getAttribute?.('named') ?? element.named,
    describedAs: element.getAttribute?.('described-as') ?? element.describedAs,
    descriptionAs: element.getAttribute?.('description-as') ?? element.descriptionAs,
    resizableColumns: element.hasAttribute?.('resizable-columns') ?? element.resizableColumns,
    reorderableColumns: element.hasAttribute?.('reorderable-columns') ?? element.reorderableColumns,
    columns: declarativeColumns(element)
  });
}

export function normalizeStandaloneCollectionPresentation(value) {
  if (!value || typeof value !== 'object') return null;
  return Object.freeze({
    named: boundedPresentationText(value.named, 512),
    describedAs: boundedPresentationText(value.describedAs, 2_048),
    descriptionAs: normalizeDescriptionPresentation(value.descriptionAs),
    resizableColumns: value.resizableColumns === true,
    reorderableColumns: value.reorderableColumns === true,
    columns: Object.freeze(normalizeColumns(value.columns).slice(0, MAX_PRESENTATION_COLUMNS))
  });
}

export function applyStandaloneCollectionPresentation(element, value) {
  const presentation = normalizeStandaloneCollectionPresentation(value);
  if (!element || !presentation) return null;
  element.named = presentation.named;
  element.describedAs = presentation.describedAs;
  element.descriptionAs = presentation.descriptionAs;
  element.resizableColumns = presentation.resizableColumns;
  element.reorderableColumns = presentation.reorderableColumns;
  element.columns = presentation.columns;
  return presentation;
}

function declarativeColumns(collection) {
  return [...(collection.children ?? collection.childNodes ?? [])]
    .filter(child => child?.localName === 'cw-collection-column' || child?.configuration?.member)
    .map(child => ({
      member: child.configuration?.member ?? child.id ?? '',
      label: child.configuration?.label ?? child.getAttribute?.('label') ?? '',
      testId: child.configuration?.testId ?? child.getAttribute?.('data-testid') ?? null
    }))
    .filter(column => column.member);
}

function normalizeColumns(value) {
  return [...(value ?? [])]
    .map(column => typeof column === 'string' ? {member: column, label: humanize(column)} : {...column})
    .filter(column => column.member)
    .map(column => Object.freeze(column));
}

function humanize(value) {
  return String(value || '').replace(/([a-z0-9])([A-Z])/g, '$1 $2').replace(/^./, character => character.toUpperCase());
}

function boundedPresentationText(value, maximum) {
  return String(value ?? '').replace(/[\u0000-\u001f\u007f]/g, '').trim().slice(0, maximum);
}
