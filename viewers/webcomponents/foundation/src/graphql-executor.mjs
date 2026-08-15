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

export class GraphQLTransportError extends Error {
  constructor(message, {status = null, cause = null, responseBody = null} = {}) {
    super(message, cause ? {cause} : undefined);
    this.name = 'GraphQLTransportError';
    this.status = status;
    this.responseBody = responseBody;
  }
}

export function createFetchGraphQLExecutor({endpoint = '/graphql', fetchImpl = globalThis.fetch, headers = {}} = {}) {
  if (typeof fetchImpl !== 'function') {
    throw new GraphQLTransportError('No Fetch API implementation is available.');
  }
  return async ({document, variables = {}, operationName, signal} = {}) => {
    if (typeof document !== 'string' || document.trim().length === 0) {
      throw new GraphQLTransportError('A non-empty GraphQL document is required.');
    }
    const requestHeaders = typeof headers === 'function' ? await headers() : headers;
    let response;
    try {
      response = await fetchImpl(endpoint, {
        method: 'POST',
        headers: {
          accept: 'application/graphql-response+json, application/json',
          'content-type': 'application/json',
          ...requestHeaders
        },
        body: JSON.stringify({query: document, variables, operationName}),
        signal
      });
    } catch (cause) {
      if (cause?.name === 'AbortError') {
        throw cause;
      }
      throw new GraphQLTransportError(`GraphQL request to '${endpoint}' failed.`, {cause});
    }
    const responseText = await response.text();
    let responseBody;
    try {
      responseBody = responseText.length === 0 ? {} : JSON.parse(responseText);
    } catch (cause) {
      throw new GraphQLTransportError('GraphQL response was not valid JSON.', {
        status: response.status,
        cause,
        responseBody: responseText
      });
    }
    if (!response.ok) {
      throw new GraphQLTransportError(`GraphQL request failed with HTTP ${response.status}.`, {
        status: response.status,
        responseBody
      });
    }
    return responseBody;
  };
}

export function normalizeExecutor(executor) {
  if (typeof executor === 'function') {
    return executor;
  }
  if (executor && typeof executor.execute === 'function') {
    return request => executor.execute(request);
  }
  throw new GraphQLTransportError('GraphQL executor must be a function or expose execute(request).');
}
