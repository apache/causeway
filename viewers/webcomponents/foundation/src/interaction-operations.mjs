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

export function resultSelectionForType(typeRef, types) {
  const kind = innermostType(typeRef)?.kind;
  if (kind === 'SCALAR' || kind === 'ENUM') {
    return null;
  }
  const typeDescription = types.get(namedType(typeRef));
  if (!typeDescription) {
    return {__typename: true};
  }
  const fields = fieldsByName(typeDescription);
  if (fields.has('_meta')) {
    return {_meta: {id: true, logicalTypeName: true, title: true, version: true}};
  }
  const scalarFields = typeDescription.fields
    .filter(field => ['SCALAR', 'ENUM'].includes(innermostType(field.type)?.kind))
    .slice(0, 8);
  return scalarFields.length > 0
    ? Object.fromEntries(scalarFields.map(field => [field.name, true]))
    : {__typename: true};
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
