/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */
/*ISIS-3071 file-input nesting issue*/
let scalarFrames = document.querySelectorAll("span.uploadFile");
scalarFrames.forEach((scalarFrame) => {
	let fileInputFrames = scalarFrame.querySelectorAll("div.file-input-ajax-new");
	let isNested = fileInputFrames.length>1;
	/* replace the outermost with the innermost */
	if(isNested) {
		let outermost = fileInputFrames[0]; 
		let innermost = fileInputFrames[fileInputFrames.length-1];
		outermost.parentNode.replaceChild(innermost, outermost);
	}
})