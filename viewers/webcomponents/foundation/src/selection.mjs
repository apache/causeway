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

import {assertGraphQLName} from './schema-names.mjs';

export function mergeSelections(...selections) {
  const result = {};
  for (const selection of selections) {
    mergeInto(result, selection ?? {});
  }
  return result;
}

function mergeInto(target, source) {
  for (const [field, value] of Object.entries(source)) {
    assertGraphQLName(field, 'selection field');
    if (value === true) {
      target[field] = true;
    } else if (value && typeof value === 'object') {
      if (target[field] !== true) {
        target[field] = target[field] ?? {};
        mergeInto(target[field], value);
      }
    }
  }
}

export function differenceSelection(required, available) {
  const result = {};
  for (const [field, requiredValue] of Object.entries(required ?? {})) {
    const availableValue = available?.[field];
    if (requiredValue === true) {
      if (availableValue !== true) {
        result[field] = true;
      }
    } else if (requiredValue && typeof requiredValue === 'object') {
      if (availableValue === true) {
        continue;
      }
      const nested = differenceSelection(requiredValue, availableValue ?? {});
      if (!isSelectionEmpty(nested)) {
        result[field] = nested;
      }
    }
  }
  return result;
}

export function isSelectionEmpty(selection) {
  return Object.keys(selection ?? {}).length === 0;
}

export function renderSelectionSet(selection, indentation = '      ') {
  const lines = [];
  for (const field of Object.keys(selection).sort()) {
    assertGraphQLName(field, 'selection field');
    const value = selection[field];
    if (value === true) {
      lines.push(`${indentation}${field}`);
    } else {
      lines.push(`${indentation}${field} {`);
      lines.push(renderSelectionSet(value, `${indentation}  `));
      lines.push(`${indentation}}`);
    }
  }
  return lines.join('\n');
}

export function buildObjectReadOperation({description, identity, selection, schemaNames}) {
  if (isSelectionEmpty(selection)) {
    throw new Error('Cannot build a GraphQL object read with an empty selection.');
  }
  const objectField = assertGraphQLName(description.generatedFieldName, 'object field');
  const lookupArgument = assertGraphQLName(schemaNames.lookupArgumentName, 'lookup argument');
  const nestedSelection = renderSelectionSet(selection, schemaNames.richRootField ? '      ' : '    ');
  const objectRead = schemaNames.richRootField
    ? `  ${assertGraphQLName(schemaNames.richRootField, 'rich root field')} {\n    ${objectField}(${lookupArgument}: $object) {\n${nestedSelection}\n    }\n  }`
    : `  ${objectField}(${lookupArgument}: $object) {\n${nestedSelection}\n  }`;
  return Object.freeze({
    document: `query CausewayReadObject($object: ${description.generatedInputTypeName}!) {\n${objectRead}\n}`,
    variables: {object: {id: identity.id}},
    operationName: 'CausewayReadObject',
    objectPath: schemaNames.richRootField
      ? [schemaNames.richRootField, objectField]
      : [objectField]
  });
}

export function buildCollectionWindowReadOperation({
  description,
  identity,
  member,
  rowSelection,
  offset,
  size,
  schemaNames
}) {
  const objectField = assertGraphQLName(description.generatedFieldName, 'object field');
  const collectionField = assertGraphQLName(member, 'collection field');
  const lookupArgument = assertGraphQLName(schemaNames.lookupArgumentName, 'lookup argument');
  const rowIndentation = schemaNames.richRootField ? '              ' : '            ';
  const renderedRows = renderSelectionSet(rowSelection, rowIndentation);
  const windowSelection = `offset\n${rowIndentation.slice(2)}requestedSize\n${rowIndentation.slice(2)}returnedCount\n${rowIndentation.slice(2)}totalCount\n${rowIndentation.slice(2)}maximumSize\n${rowIndentation.slice(2)}hasPrevious\n${rowIndentation.slice(2)}hasNext\n${rowIndentation.slice(2)}ordering\n${rowIndentation.slice(2)}rows {\n${renderedRows}\n${rowIndentation.slice(2)}}`;
  const objectRead = schemaNames.richRootField
    ? `  ${assertGraphQLName(schemaNames.richRootField, 'rich root field')} {\n    ${objectField}(${lookupArgument}: $object) {\n      ${collectionField} {\n        window(offset: $offset, size: $size) {\n          ${windowSelection}\n        }\n      }\n    }\n  }`
    : `  ${objectField}(${lookupArgument}: $object) {\n    ${collectionField} {\n      window(offset: $offset, size: $size) {\n        ${windowSelection}\n      }\n    }\n  }`;
  return Object.freeze({
    document: `query CausewayReadCollectionWindow($object: ${description.generatedInputTypeName}!, $offset: Int!, $size: Int!) {\n${objectRead}\n}`,
    variables: {object: {id: identity.id}, offset, size},
    operationName: 'CausewayReadCollectionWindow',
    objectPath: schemaNames.richRootField
      ? [schemaNames.richRootField, objectField]
      : [objectField]
  });
}

export function deepMerge(target, source) {
  if (!isPlainObject(target) || !isPlainObject(source)) {
    return clone(source);
  }
  const result = {...target};
  for (const [key, value] of Object.entries(source)) {
    result[key] = key in result ? deepMerge(result[key], value) : clone(value);
  }
  return result;
}

function clone(value) {
  if (Array.isArray(value)) {
    return value.map(clone);
  }
  if (isPlainObject(value)) {
    return Object.fromEntries(Object.entries(value).map(([key, nested]) => [key, clone(nested)]));
  }
  return value;
}

function isPlainObject(value) {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
}
