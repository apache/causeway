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

const TYPE_REF_SELECTION = `
  kind
  name
  ofType {
    kind
    name
    ofType {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
        }
      }
    }
  }`;

const TYPE_DESCRIPTION_SELECTION = `
    kind
    name
    description
    fields(includeDeprecated: true) {
      name
      description
      args(includeDeprecated: true) {
        name
        description
        defaultValue
        type {${TYPE_REF_SELECTION}
        }
      }
      type {${TYPE_REF_SELECTION}
      }
    }`;

export function buildDescribeTypesOperation(typeNames) {
  const uniqueNames = [...new Set(typeNames)];
  if (uniqueNames.length === 0) {
    throw new Error('At least one GraphQL type name is required for introspection.');
  }
  const variables = {};
  const declarations = [];
  const selections = [];
  uniqueNames.forEach((typeName, index) => {
    const variable = `type${index}`;
    const alias = `describedType${index}`;
    variables[variable] = typeName;
    declarations.push(`$${variable}: String!`);
    selections.push(`  ${alias}: __type(name: $${variable}) {${TYPE_DESCRIPTION_SELECTION}\n  }`);
  });
  return Object.freeze({
    document: `query CausewayDescribeTypes(${declarations.join(', ')}) {\n${selections.join('\n')}\n}`,
    variables,
    operationName: 'CausewayDescribeTypes',
    aliases: new Map(uniqueNames.map((typeName, index) => [typeName, `describedType${index}`]))
  });
}

export function namedType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current?.name ?? null;
}

export function normalizeTypeDescription(type) {
  if (!type) {
    return null;
  }
  assertGraphQLName(type.name, 'introspected type');
  return Object.freeze({
    kind: type.kind,
    name: type.name,
    description: type.description ?? null,
    fields: Object.freeze((type.fields ?? []).map(field => Object.freeze({
      name: assertGraphQLName(field.name, 'introspected field'),
      description: field.description ?? null,
      args: Object.freeze((field.args ?? []).map(argument => Object.freeze({
        name: assertGraphQLName(argument.name, 'introspected argument'),
        description: argument.description ?? null,
        defaultValue: argument.defaultValue ?? null,
        type: freezeTypeRef(argument.type)
      }))),
      type: freezeTypeRef(field.type)
    })))
  });
}

function freezeTypeRef(typeRef) {
  if (!typeRef) {
    return null;
  }
  return Object.freeze({
    kind: typeRef.kind,
    name: typeRef.name ?? null,
    ofType: freezeTypeRef(typeRef.ofType)
  });
}

export function fieldsByName(typeDescription) {
  return new Map((typeDescription?.fields ?? []).map(field => [field.name, field]));
}
