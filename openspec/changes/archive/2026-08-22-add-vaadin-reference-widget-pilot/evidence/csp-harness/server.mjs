/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createServer} from 'node:http';
import {readFileSync} from 'node:fs';
import {extname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const basePolicy = "default-src 'self'; script-src 'self'; img-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'self'";
const types = {'.css': 'text/css; charset=utf-8', '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.map': 'application/json', '.mjs': 'text/javascript; charset=utf-8'};

export async function startServer() {
  const server = createServer((request, response) => {
    const url = new URL(request.url, 'http://127.0.0.1');
    const policy = policyFor(url);
    try {
      const file = fileFor(url.pathname);
      const body = readFileSync(file);
      response.writeHead(200, {
        'Content-Type': types[extname(file)] ?? 'application/octet-stream',
        'Content-Security-Policy': policy,
        'Cache-Control': 'no-store',
        'X-Content-Type-Options': 'nosniff'
      });
      response.end(body);
    } catch (error) {
      response.writeHead(404, {'Content-Type': 'text/plain; charset=utf-8'});
      response.end(`Not found: ${error.message}`);
    }
  });
  await new Promise(resolveListen => server.listen(0, '127.0.0.1', resolveListen));
  return {server, origin: `http://127.0.0.1:${server.address().port}`};
}

function fileFor(pathname) {
  if (pathname === '/' || pathname === '/index.html') return resolve(directory, 'index.html');
  if (pathname === '/styles.css') return resolve(directory, 'styles.css');
  if (pathname === '/bootstrap.mjs') return resolve(directory, 'bootstrap.mjs');
  if (pathname === '/generated/candidate.js') return resolve(directory, 'generated/candidate.js');
  if (pathname === '/generated/candidate.js.map') return resolve(directory, 'generated/candidate.js.map');
  throw new Error('Unsupported path');
}

function policyFor(url) {
  const mode = url.searchParams.get('policy') ?? 'exact';
  const hashes = url.searchParams.getAll('hash').map(hash => `'${hash}'`).join(' ');
  if (mode === 'hashes') return `${basePolicy}; style-src 'self' ${hashes}; style-src-elem 'self' ${hashes}; style-src-attr 'none'`;
  if (mode === 'element-inline') return `${basePolicy}; style-src 'self'; style-src-elem 'self' 'unsafe-inline'; style-src-attr 'none'`;
  if (mode === 'attribute-inline') return `${basePolicy}; style-src 'self'; style-src-elem 'self'; style-src-attr 'unsafe-inline'`;
  if (mode === 'nonce' || mode === 'nonce-patched') return `${basePolicy}; style-src 'self' 'nonce-causeway-analysis'; style-src-elem 'self' 'nonce-causeway-analysis'; style-src-attr 'none'`;
  return `${basePolicy}; style-src 'self'`;
}
