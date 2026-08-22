/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {build} from 'esbuild';
import {mkdirSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const directory = fileURLToPath(new URL('.', import.meta.url));
const generated = resolve(directory, 'generated');
mkdirSync(generated, {recursive: true});

await build({
  entryPoints: [resolve(directory, 'candidate-entry.mjs')],
  outfile: resolve(generated, 'candidate.js'),
  bundle: true,
  format: 'esm',
  platform: 'browser',
  target: ['es2022'],
  minify: true,
  legalComments: 'eof',
  sourcemap: true,
  metafile: true,
  define: {
    'process.env.NODE_ENV': '"production"'
  }
}).then(result => {
  writeFileSync(resolve(generated, 'metafile.json'), `${JSON.stringify(result.metafile)}\n`);
});
