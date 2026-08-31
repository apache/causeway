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
import {fieldsByName, namedType} from './introspection.mjs';
import {INLINE_FRAGMENTS} from './selection.mjs';

export const MAX_DIRECT_FRAGMENT_TYPES = 8;

const AUTO_COMPLETE_WINDOW_FIELDS = Object.freeze([
  'offset',
  'requestedSize',
  'returnedCount',
  'totalCount',
  'maximumSize',
  'hasPrevious',
  'hasNext',
  'ordering'
]);

export function autoCompleteWindowPlan(field, types) {
  const typeName = namedType(field?.type);
  const typeDescription = types.get(typeName) ?? null;
  const fields = fieldsByName(typeDescription);
  const itemsField = fields.get('items');
  if (!field || !typeDescription || !itemsField) {
    return null;
  }
  const args = fieldsByName({fields: field.args ?? []});
  const selection = Object.fromEntries(AUTO_COMPLETE_WINDOW_FIELDS
    .filter(name => fields.has(name))
    .map(name => [name, true]));
  selection.items = resultSelectionForType(itemsField.type, types) ?? true;
  return Object.freeze({
    typeName,
    typeDescription,
    itemsField,
    selection: Object.freeze(selection),
    offsetDefault: integerDefault(args.get('offset')?.defaultValue, 0),
    sizeDefault: integerDefault(args.get('size')?.defaultValue, null)
  });
}

export function normalizeAutoCompleteWindow(value, {legacy = false, offset = 0, requestedSize = null} = {}) {
  const items = Array.isArray(legacy ? value : value?.items) ? [...(legacy ? value : value.items)] : [];
  const totalCount = legacy ? items.length : nonNegativeInteger(value?.totalCount, items.length);
  const effectiveOffset = legacy ? offset : nonNegativeInteger(value?.offset, offset);
  return Object.freeze({
    items: Object.freeze(items),
    offset: effectiveOffset,
    requestedSize: legacy ? requestedSize : positiveInteger(value?.requestedSize, requestedSize ?? items.length),
    returnedCount: legacy ? items.length : nonNegativeInteger(value?.returnedCount, items.length),
    totalCount,
    maximumSize: legacy ? null : positiveInteger(value?.maximumSize, null),
    hasPrevious: legacy ? effectiveOffset > 0 : value?.hasPrevious === true,
    hasNext: legacy ? false : value?.hasNext === true,
    ordering: legacy ? 'LEGACY' : String(value?.ordering ?? 'APPLICATION'),
    windowed: !legacy
  });
}

export function buildDescribeOperationRootsOperation() {
  return Object.freeze({
    document: 'query CausewayDescribeOperationRoots { __schema { queryType { name } mutationType { name } } }',
    variables: {},
    operationName: 'CausewayDescribeOperationRoots'
  });
}

export function buildObjectInteractionOperation({
  description,
  identity,
  selection,
  schemaNames,
  operationName = 'CausewayObjectInteraction'
}) {
  const variables = {object: {id: identity.id}};
  const declarations = [`$object: ${description.generatedInputTypeName}!`];
  const objectType = description.types.get(description.generatedTypeName);
  const renderedSelection = renderExecutableSelection({
    selection,
    typeDescription: objectType,
    types: description.types,
    variables,
    declarations,
    variablePrefix: 'input',
    indentation: schemaNames.richRootField ? '      ' : '    '
  });
  const objectField = assertGraphQLName(description.generatedFieldName, 'object field');
  const lookupArgument = assertGraphQLName(schemaNames.lookupArgumentName, 'lookup argument');
  const objectRead = schemaNames.richRootField
    ? `  ${assertGraphQLName(schemaNames.richRootField, 'rich root field')} {\n    ${objectField}(${lookupArgument}: $object) {\n${renderedSelection}\n    }\n  }`
    : `  ${objectField}(${lookupArgument}: $object) {\n${renderedSelection}\n  }`;
  return Object.freeze({
    document: `query ${assertGraphQLName(operationName, 'operation name')}(${declarations.join(', ')}) {\n${objectRead}\n}`,
    variables,
    operationName,
    objectPath: schemaNames.richRootField
      ? [schemaNames.richRootField, objectField]
      : [objectField]
  });
}

export function buildServiceInteractionOperation({
  description,
  selection,
  schemaNames,
  operationName = 'CausewayServiceInteraction'
}) {
  const variables = {};
  const declarations = [];
  const serviceType = description.types.get(description.generatedTypeName);
  const renderedSelection = renderExecutableSelection({
    selection,
    typeDescription: serviceType,
    types: description.types,
    variables,
    declarations,
    variablePrefix: 'input',
    indentation: schemaNames.richRootField ? '      ' : '    '
  });
  const serviceField = assertGraphQLName(description.generatedFieldName, 'service field');
  const serviceRead = schemaNames.richRootField
    ? `  ${assertGraphQLName(schemaNames.richRootField, 'rich root field')} {\n    ${serviceField} {\n${renderedSelection}\n    }\n  }`
    : `  ${serviceField} {\n${renderedSelection}\n  }`;
  const declarationsText = declarations.length > 0 ? `(${declarations.join(', ')})` : '';
  return Object.freeze({
    document: `query ${assertGraphQLName(operationName, 'operation name')}${declarationsText} {\n${serviceRead}\n}`,
    variables,
    operationName,
    servicePath: schemaNames.richRootField
      ? [schemaNames.richRootField, serviceField]
      : [serviceField]
  });
}

export function buildMutationInteractionOperation({
  mutationType,
  fieldName,
  args,
  resultSelection,
  types,
  operationName = 'CausewayMutationInteraction'
}) {
  const field = fieldsByName(mutationType).get(fieldName);
  if (!field) {
    throw new Error(`Mutation field '${fieldName}' is unavailable.`);
  }
  const variables = {};
  const declarations = [];
  const renderedArgs = renderArguments({
    args,
    field,
    variables,
    declarations,
    variablePrefix: 'input'
  });
  const namedResultType = types.get(namedType(field.type)) ?? null;
  const renderedResult = resultSelection && namedResultType
    ? ` {\n${renderExecutableSelection({
        selection: resultSelection,
        typeDescription: namedResultType,
        types,
        variables,
        declarations,
        variablePrefix: 'resultInput',
        indentation: '    '
      })}\n  }`
    : '';
  const declarationsText = declarations.length > 0 ? `(${declarations.join(', ')})` : '';
  return Object.freeze({
    document: `mutation ${assertGraphQLName(operationName, 'operation name')}${declarationsText} {\n  ${assertGraphQLName(fieldName, 'mutation field')}${renderedArgs}${renderedResult}\n}`,
    variables,
    operationName,
    resultPath: [fieldName],
    resultType: field.type
  });
}

export function commandSelection(args = {}, select = true) {
  return Object.freeze({__args: Object.freeze({...args}), __select: select});
}

export function argumentsFromValues(field, values) {
  const result = {};
  for (const argument of field?.args ?? []) {
    if (Object.prototype.hasOwnProperty.call(values, argument.name)) {
      result[argument.name] = normalizeInteractionInput(values[argument.name]);
    }
  }
  return result;
}

export function normalizeInteractionInput(value) {
  if (Array.isArray(value)) {
    return value.map(normalizeInteractionInput);
  }
  if (!value || typeof value !== 'object') {
    return value;
  }
  const metadata = value._meta;
  if (metadata?.id) {
    return {id: metadata.id};
  }
  if (value.id && value.logicalTypeName) {
    return {id: value.id};
  }
  return Object.fromEntries(Object.entries(value)
    .filter(([name]) => name !== '__typename')
    .map(([name, nested]) => [name, normalizeInteractionInput(nested)]));
}

export function renderGraphQLType(typeRef) {
  if (!typeRef) {
    throw new Error('Cannot render a missing GraphQL type reference.');
  }
  if (typeRef.kind === 'NON_NULL') {
    return `${renderGraphQLType(typeRef.ofType)}!`;
  }
  if (typeRef.kind === 'LIST') {
    return `[${renderGraphQLType(typeRef.ofType)}]`;
  }
  return assertGraphQLName(typeRef.name, 'GraphQL type');
}

export function metadataSelectionForType(typeRef, types) {
  if (innermostType(typeRef)?.kind !== 'OBJECT') {
    return null;
  }
  const typeDescription = types.get(namedType(typeRef));
  const metadataField = fieldsByName(typeDescription).get('_meta');
  const metadataType = metadataField ? types.get(namedType(metadataField.type)) ?? null : null;
  const metadataFields = fieldsByName(metadataType);
  const selection = Object.fromEntries(
    ['id', 'logicalTypeName', 'title', 'version', 'icon']
      .filter(fieldName => metadataFields.has(fieldName))
      .map(fieldName => [fieldName, true]));
  return Object.keys(selection).length > 0 ? {_meta: selection} : null;
}

export function resultSelectionForType(typeRef, types) {
  const kind = innermostType(typeRef)?.kind;
  if (kind === 'SCALAR' || kind === 'ENUM') {
    return null;
  }
  const typeDescription = types.get(namedType(typeRef));
  if (!typeDescription) {
    return {__typename: true};
  }
  if (['INTERFACE', 'UNION'].includes(kind)) {
    const possibleTypes = [...(typeDescription.possibleTypes ?? [])]
      .map(candidate => candidate.name)
      .sort();
    if (possibleTypes.length === 0
        || possibleTypes.length > MAX_DIRECT_FRAGMENT_TYPES
        || possibleTypes.some(typeName => !types.has(typeName))) {
      return {__typename: true};
    }
    return {
      __typename: true,
      [INLINE_FRAGMENTS]: Object.fromEntries(possibleTypes.map(typeName => {
        const concreteRef = {kind: 'OBJECT', name: typeName, ofType: null};
        return [typeName, metadataSelectionForType(concreteRef, types) ?? {__typename: true}];
      }))
    };
  }
  const metadataSelection = metadataSelectionForType(typeRef, types);
  if (metadataSelection) {
    return metadataSelection;
  }
  const scalarFields = typeDescription.fields
    .filter(field => ['SCALAR', 'ENUM'].includes(innermostType(field.type)?.kind))
    .slice(0, 8);
  return scalarFields.length > 0
    ? Object.fromEntries(scalarFields.map(field => [field.name, true]))
    : {__typename: true};
}

function integerDefault(value, fallback) {
  if (value == null) return fallback;
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) ? parsed : fallback;
}

function nonNegativeInteger(value, fallback) {
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) && parsed >= 0 ? parsed : fallback;
}

function positiveInteger(value, fallback) {
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : fallback;
}

function renderExecutableSelection({
  selection,
  typeDescription,
  types,
  variables,
  declarations,
  variablePrefix,
  indentation
}) {
  if (!typeDescription) {
    throw new Error('Cannot render an executable selection without its GraphQL type description.');
  }
  const fields = fieldsByName(typeDescription);
  const lines = [];
  for (const fieldName of Object.keys(selection).sort()) {
    if (fieldName === INLINE_FRAGMENTS) {
      lines.push(renderExecutableFragments({
        fragments: selection[fieldName],
        typeDescription,
        types,
        variables,
        declarations,
        variablePrefix,
        indentation
      }));
      continue;
    }
    assertGraphQLName(fieldName, 'selection field');
    if (fieldName === '__typename' && selection[fieldName] === true) {
      lines.push(`${indentation}__typename`);
      continue;
    }
    const field = fields.get(fieldName);
    if (!field) {
      throw new Error(`Field '${fieldName}' is unavailable on GraphQL type '${typeDescription.name}'.`);
    }
    const node = normalizeCommandNode(selection[fieldName]);
    const renderedArgs = renderArguments({
      args: node.args,
      field,
      variables,
      declarations,
      variablePrefix: `${variablePrefix}${declarations.length}`
    });
    if (node.select === true || node.select == null) {
      lines.push(`${indentation}${fieldName}${renderedArgs}`);
      continue;
    }
    const childType = types.get(namedType(field.type));
    if (!childType) {
      throw new Error(`Selection for '${typeDescription.name}.${fieldName}' requires an undescribed GraphQL type '${namedType(field.type)}'.`);
    }
    lines.push(`${indentation}${fieldName}${renderedArgs} {`);
    lines.push(renderExecutableSelection({
      selection: node.select,
      typeDescription: childType,
      types,
      variables,
      declarations,
      variablePrefix: `${variablePrefix}${declarations.length}`,
      indentation: `${indentation}  `
    }));
    lines.push(`${indentation}}`);
  }
  return lines.join('\n');
}

function renderExecutableFragments({
  fragments,
  typeDescription,
  types,
  variables,
  declarations,
  variablePrefix,
  indentation
}) {
  if (!['INTERFACE', 'UNION'].includes(typeDescription.kind)) {
    throw new Error(`Inline fragments are unavailable on GraphQL type '${typeDescription.name}'.`);
  }
  const advertised = new Set((typeDescription.possibleTypes ?? []).map(candidate => candidate.name));
  const lines = [];
  for (const typeName of Object.keys(fragments ?? {}).sort()) {
    assertGraphQLName(typeName, 'inline fragment type');
    if (!advertised.has(typeName)) {
      throw new Error(`Type '${typeName}' is not advertised by GraphQL type '${typeDescription.name}'.`);
    }
    const concreteType = types.get(typeName) ?? null;
    if (!concreteType || concreteType.kind !== 'OBJECT') {
      throw new Error(`Inline fragment type '${typeName}' is not a described GraphQL object.`);
    }
    lines.push(`${indentation}... on ${typeName} {`);
    lines.push(renderExecutableSelection({
      selection: fragments[typeName],
      typeDescription: concreteType,
      types,
      variables,
      declarations,
      variablePrefix: `${variablePrefix}${declarations.length}`,
      indentation: `${indentation}  `
    }));
    lines.push(`${indentation}}`);
  }
  return lines.join('\n');
}

function normalizeCommandNode(value) {
  if (value === true) {
    return {args: {}, select: true};
  }
  if (value && typeof value === 'object' && ('__args' in value || '__select' in value)) {
    return {args: value.__args ?? {}, select: value.__select ?? true};
  }
  return {args: {}, select: value};
}

function renderArguments({args, field, variables, declarations, variablePrefix}) {
  const entries = Object.entries(args ?? {});
  if (entries.length === 0) {
    return '';
  }
  const argumentDescriptions = new Map(field.args.map(argument => [argument.name, argument]));
  const rendered = entries.map(([argumentName, value], index) => {
    assertGraphQLName(argumentName, 'argument');
    const argument = argumentDescriptions.get(argumentName);
    if (!argument) {
      throw new Error(`Argument '${argumentName}' is unavailable on field '${field.name}'.`);
    }
    const variableName = assertGraphQLName(`${variablePrefix}${index}`, 'variable');
    declarations.push(`$${variableName}: ${renderGraphQLType(argument.type)}`);
    variables[variableName] = value;
    return `${argumentName}: $${variableName}`;
  });
  return `(${rendered.join(', ')})`;
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}
