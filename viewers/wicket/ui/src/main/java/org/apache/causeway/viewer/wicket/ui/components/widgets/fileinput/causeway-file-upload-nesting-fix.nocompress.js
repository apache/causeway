/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
// ISIS-3071 file-input nesting issue
document.querySelectorAll("span.uploadFile")
.forEach((scalarFrame) => {
	let fileInputFrames = scalarFrame.querySelectorAll("div.file-input");
	let isNested = fileInputFrames.length>1;
	// replace the outermost with the innermost
	if(isNested) {
		let outermost = fileInputFrames[0];
		//let outermostInput = outermost.querySelector("input");
		//let outermostPreview = outermost.querySelector("div.file-preview");
		//console.log("outermostInput: " + outermostInput);
		//console.log("outermostPreview: " + outermostPreview);
		 
		let innermost = fileInputFrames[fileInputFrames.length-1];
		//let innermostInput = innermost.querySelector("input");
		//let innermostPreview = innermost.querySelector("div.file-preview");
		
		//console.log("innermostInput: " + innermostInput);
		//console.log("innermostPreview: " + innermostPreview);
		
		//innermostInput.parentNode.replaceChild(outermostInput, innermostInput);
		//innermostPreview.parentNode.replaceChild(outermostPreview, innermostPreview);
		outermost.parentNode.replaceChild(innermost, outermost);
	}
    
    // remove unused caption-icon and fix css
    scalarFrame.querySelectorAll('span.file-caption-icon')
        .forEach((node)=>node.remove());
    scalarFrame.querySelectorAll('span.hidden-xs')
            .forEach((node)=>node.remove());
        
    scalarFrame.querySelectorAll('div.file-caption')
        .forEach((node)=>{
            node.classList.remove('icon-visible');
            node.classList.remove('input-group-sm');
        });
    scalarFrame.querySelectorAll('div.input-group')
            .forEach((node)=>{
                node.classList.add('input-group-sm');
            });
            
    // replace bi icons with fa
    scalarFrame.querySelectorAll('i.bi-folder2-open')
        .forEach((node)=>{
            node.classList.remove('bi-folder2-open');
            node.classList.add('fa-regular');
            node.classList.add('fa-folder-open');
        });    
    scalarFrame.querySelectorAll('i.bi-trash')
            .forEach((node)=>{
                node.classList.remove('bi-trash');
                node.classList.add('fa-regular');
                node.classList.add('fa-trash-can');
            });
    scalarFrame.querySelectorAll('i.bi-slash-circle')
            .forEach((node)=>{
                node.classList.remove('bi-slash-circle');
                node.classList.add('fa-solid');
                node.classList.add('fa-ban');
            });
})

    

    