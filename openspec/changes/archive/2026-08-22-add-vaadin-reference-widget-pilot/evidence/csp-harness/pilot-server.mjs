/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createServer} from 'node:http';
import {existsSync, readFileSync} from 'node:fs';
import {dirname, extname, resolve, sep} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const project = findProject(directory);
const foundation = resolve(project, 'viewers/webcomponents/foundation/src');
const production = resolve(project, 'viewers/webcomponents/foundation/vaadin-reference/generated/assets');
const matrix = JSON.parse(readFileSync(resolve(directory, '../results/csp-matrix.json'), 'utf8'));
const hashes = matrix.discoveredStyleHashes.map(hash => `'${hash}'`).join(' ');
const policy = `default-src 'self'; script-src 'self'; style-src 'self' ${hashes}; style-src-elem 'self' ${hashes}; style-src-attr 'none'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'`;
const types = {'.css': 'text/css; charset=utf-8', '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.mjs': 'text/javascript; charset=utf-8'};

export async function startPilotServer() {
  const server = createServer((request, response) => {
    try {
      const url = new URL(request.url, 'http://127.0.0.1');
      const file = resolveFile(url.pathname);
      response.writeHead(200, {
        'Content-Type': types[extname(file)] ?? 'application/octet-stream',
        'Content-Security-Policy': policy,
        'Cache-Control': 'no-store',
        'X-Content-Type-Options': 'nosniff'
      });
      response.end(readFileSync(file));
    } catch (error) {
      response.writeHead(404, {'Content-Type': 'text/plain; charset=utf-8'});
      response.end(`Not found: ${error.message}`);
    }
  });
  await new Promise(resolveListen => server.listen(0, '127.0.0.1', resolveListen));
  return {server, origin: `http://127.0.0.1:${server.address().port}`};
}

function findProject(start) {
  let current = resolve(start);
  while (dirname(current) !== current) {
    if (existsSync(resolve(current, 'viewers/webcomponents/foundation/src'))) {
      return current;
    }
    current = dirname(current);
  }
  throw new Error('Cannot locate the project root');
}

function resolveFile(pathname) {
  if (pathname === '/' || pathname === '/pilot.html') return resolve(directory, 'pilot.html');
  if (pathname === '/pilot.css') return resolve(directory, 'pilot.css');
  if (pathname === '/pilot-bootstrap.mjs') return resolve(directory, 'pilot-bootstrap.mjs');
  if (pathname === '/axe.js') return resolve(directory, 'node_modules/axe-core/axe.min.js');
  if (pathname === '/production/vaadin-reference.js') return safeResolve(production, 'vaadin-reference.js');
  if (pathname.startsWith('/foundation/')) return safeResolve(foundation, pathname.slice('/foundation/'.length));
  throw new Error('Unsupported path');
}

function safeResolve(root, relative) {
  const file = resolve(root, relative);
  if (!file.startsWith(`${root}${sep}`) || !existsSync(file)) throw new Error('Unsafe or missing path');
  return file;
}
