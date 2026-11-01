var Rt=globalThis,$t=Rt.ShadowRoot&&(Rt.ShadyCSS===void 0||Rt.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,Ui=Symbol(),$s=new WeakMap,Ue=class{constructor(i,e,t){if(this._$cssResult$=!0,t!==Ui)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=i,this.t=e}get styleSheet(){let i=this.o,e=this.t;if($t&&i===void 0){let t=e!==void 0&&e.length===1;t&&(i=$s.get(e)),i===void 0&&((this.o=i=new CSSStyleSheet).replaceSync(this.cssText),t&&$s.set(e,i))}return i}toString(){return this.cssText}},C=s=>new Ue(typeof s=="string"?s:s+"",void 0,Ui),p=(s,...i)=>{let e=s.length===1?s[0]:i.reduce((t,r,o)=>t+(n=>{if(n._$cssResult$===!0)return n.cssText;if(typeof n=="number")return n;throw Error("Value passed to 'css' function must be a 'css' function result: "+n+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(r)+s[o+1],s[0]);return new Ue(e,s,Ui)},Lt=(s,i)=>{if($t)s.adoptedStyleSheets=i.map(e=>e instanceof CSSStyleSheet?e:e.styleSheet);else for(let e of i){let t=document.createElement("style"),r=Rt.litNonce;r!==void 0&&t.setAttribute("nonce",r),t.textContent=e.cssText,s.appendChild(t)}},ji=$t?s=>s:s=>s instanceof CSSStyleSheet?(i=>{let e="";for(let t of i.cssRules)e+=t.cssText;return C(e)})(s):s;var{is:Ha,defineProperty:Ua,getOwnPropertyDescriptor:ja,getOwnPropertyNames:Wa,getOwnPropertySymbols:qa,getPrototypeOf:Ga}=Object,zt=globalThis,Ls=zt.trustedTypes,Ka=Ls?Ls.emptyScript:"",Ya=zt.reactiveElementPolyfillSupport,vt=(s,i)=>s,Wi={toAttribute(s,i){switch(i){case Boolean:s=s?Ka:null;break;case Object:case Array:s=s==null?s:JSON.stringify(s)}return s},fromAttribute(s,i){let e=s;switch(i){case Boolean:e=s!==null;break;case Number:e=s===null?null:Number(s);break;case Object:case Array:try{e=JSON.parse(s)}catch{e=null}}return e}},Bt=(s,i)=>!Ha(s,i),zs={attribute:!0,type:String,converter:Wi,reflect:!1,useDefault:!1,hasChanged:Bt};Symbol.metadata??=Symbol("metadata"),zt.litPropertyMetadata??=new WeakMap;var ce=class extends HTMLElement{static addInitializer(i){this._$Ei(),(this.l??=[]).push(i)}static get observedAttributes(){return this.finalize(),this._$Eh&&[...this._$Eh.keys()]}static createProperty(i,e=zs){if(e.state&&(e.attribute=!1),this._$Ei(),this.prototype.hasOwnProperty(i)&&((e=Object.create(e)).wrapped=!0),this.elementProperties.set(i,e),!e.noAccessor){let t=Symbol(),r=this.getPropertyDescriptor(i,t,e);r!==void 0&&Ua(this.prototype,i,r)}}static getPropertyDescriptor(i,e,t){let{get:r,set:o}=ja(this.prototype,i)??{get(){return this[e]},set(n){this[e]=n}};return{get:r,set(n){let a=r?.call(this);o?.call(this,n),this.requestUpdate(i,a,t)},configurable:!0,enumerable:!0}}static getPropertyOptions(i){return this.elementProperties.get(i)??zs}static _$Ei(){if(this.hasOwnProperty(vt("elementProperties")))return;let i=Ga(this);i.finalize(),i.l!==void 0&&(this.l=[...i.l]),this.elementProperties=new Map(i.elementProperties)}static finalize(){if(this.hasOwnProperty(vt("finalized")))return;if(this.finalized=!0,this._$Ei(),this.hasOwnProperty(vt("properties"))){let e=this.properties,t=[...Wa(e),...qa(e)];for(let r of t)this.createProperty(r,e[r])}let i=this[Symbol.metadata];if(i!==null){let e=litPropertyMetadata.get(i);if(e!==void 0)for(let[t,r]of e)this.elementProperties.set(t,r)}this._$Eh=new Map;for(let[e,t]of this.elementProperties){let r=this._$Eu(e,t);r!==void 0&&this._$Eh.set(r,e)}this.elementStyles=this.finalizeStyles(this.styles)}static finalizeStyles(i){let e=[];if(Array.isArray(i)){let t=new Set(i.flat(1/0).reverse());for(let r of t)e.unshift(ji(r))}else i!==void 0&&e.push(ji(i));return e}static _$Eu(i,e){let t=e.attribute;return t===!1?void 0:typeof t=="string"?t:typeof i=="string"?i.toLowerCase():void 0}constructor(){super(),this._$Ep=void 0,this.isUpdatePending=!1,this.hasUpdated=!1,this._$Em=null,this._$Ev()}_$Ev(){this._$ES=new Promise(i=>this.enableUpdating=i),this._$AL=new Map,this._$E_(),this.requestUpdate(),this.constructor.l?.forEach(i=>i(this))}addController(i){(this._$EO??=new Set).add(i),this.renderRoot!==void 0&&this.isConnected&&i.hostConnected?.()}removeController(i){this._$EO?.delete(i)}_$E_(){let i=new Map,e=this.constructor.elementProperties;for(let t of e.keys())this.hasOwnProperty(t)&&(i.set(t,this[t]),delete this[t]);i.size>0&&(this._$Ep=i)}createRenderRoot(){let i=this.shadowRoot??this.attachShadow(this.constructor.shadowRootOptions);return Lt(i,this.constructor.elementStyles),i}connectedCallback(){this.renderRoot??=this.createRenderRoot(),this.enableUpdating(!0),this._$EO?.forEach(i=>i.hostConnected?.())}enableUpdating(i){}disconnectedCallback(){this._$EO?.forEach(i=>i.hostDisconnected?.())}attributeChangedCallback(i,e,t){this._$AK(i,t)}_$ET(i,e){let t=this.constructor.elementProperties.get(i),r=this.constructor._$Eu(i,t);if(r!==void 0&&t.reflect===!0){let o=(t.converter?.toAttribute!==void 0?t.converter:Wi).toAttribute(e,t.type);this._$Em=i,o==null?this.removeAttribute(r):this.setAttribute(r,o),this._$Em=null}}_$AK(i,e){let t=this.constructor,r=t._$Eh.get(i);if(r!==void 0&&this._$Em!==r){let o=t.getPropertyOptions(r),n=typeof o.converter=="function"?{fromAttribute:o.converter}:o.converter?.fromAttribute!==void 0?o.converter:Wi;this._$Em=r;let a=n.fromAttribute(e,o.type);this[r]=a??this._$Ej?.get(r)??a,this._$Em=null}}requestUpdate(i,e,t,r=!1,o){if(i!==void 0){let n=this.constructor;if(r===!1&&(o=this[i]),t??=n.getPropertyOptions(i),!((t.hasChanged??Bt)(o,e)||t.useDefault&&t.reflect&&o===this._$Ej?.get(i)&&!this.hasAttribute(n._$Eu(i,t))))return;this.C(i,e,t)}this.isUpdatePending===!1&&(this._$ES=this._$EP())}C(i,e,{useDefault:t,reflect:r,wrapped:o},n){t&&!(this._$Ej??=new Map).has(i)&&(this._$Ej.set(i,n??e??this[i]),o!==!0||n!==void 0)||(this._$AL.has(i)||(this.hasUpdated||t||(e=void 0),this._$AL.set(i,e)),r===!0&&this._$Em!==i&&(this._$Eq??=new Set).add(i))}async _$EP(){this.isUpdatePending=!0;try{await this._$ES}catch(e){Promise.reject(e)}let i=this.scheduleUpdate();return i!=null&&await i,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){if(!this.isUpdatePending)return;if(!this.hasUpdated){if(this.renderRoot??=this.createRenderRoot(),this._$Ep){for(let[r,o]of this._$Ep)this[r]=o;this._$Ep=void 0}let t=this.constructor.elementProperties;if(t.size>0)for(let[r,o]of t){let{wrapped:n}=o,a=this[r];n!==!0||this._$AL.has(r)||a===void 0||this.C(r,void 0,o,a)}}let i=!1,e=this._$AL;try{i=this.shouldUpdate(e),i?(this.willUpdate(e),this._$EO?.forEach(t=>t.hostUpdate?.()),this.update(e)):this._$EM()}catch(t){throw i=!1,this._$EM(),t}i&&this._$AE(e)}willUpdate(i){}_$AE(i){this._$EO?.forEach(e=>e.hostUpdated?.()),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(i)),this.updated(i)}_$EM(){this._$AL=new Map,this.isUpdatePending=!1}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$ES}shouldUpdate(i){return!0}update(i){this._$Eq&&=this._$Eq.forEach(e=>this._$ET(e,this[e])),this._$EM()}updated(i){}firstUpdated(i){}};ce.elementStyles=[],ce.shadowRootOptions={mode:"open"},ce[vt("elementProperties")]=new Map,ce[vt("finalized")]=new Map,Ya?.({ReactiveElement:ce}),(zt.reactiveElementVersions??=[]).push("2.1.2");var Zi=globalThis,Bs=s=>s,Vt=Zi.trustedTypes,Vs=Vt?Vt.createPolicy("lit-html",{createHTML:s=>s}):void 0,qs="$lit$",be=`lit$${Math.random().toFixed(9).slice(2)}$`,Gs="?"+be,Qa=`<${Gs}>`,Fe=document,yt=()=>Fe.createComment(""),xt=s=>s===null||typeof s!="object"&&typeof s!="function",Ji=Array.isArray,Xa=s=>Ji(s)||typeof s?.[Symbol.iterator]=="function",qi=`[ 	
\f\r]`,bt=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,Ns=/-->/g,Hs=/>/g,Me=RegExp(`>|${qi}(?:([^\\s"'>=/]+)(${qi}*=${qi}*(?:[^ 	
\f\r"'\`<>=]|("|')|))|$)`,"g"),Us=/'/g,js=/"/g,Ks=/^(?:script|style|textarea|title)$/i,er=s=>(i,...e)=>({_$litType$:s,strings:i,values:e}),u=er(1),$d=er(2),Ld=er(3),Oe=Symbol.for("lit-noChange"),M=Symbol.for("lit-nothing"),Ws=new WeakMap,Pe=Fe.createTreeWalker(Fe,129);function Ys(s,i){if(!Ji(s)||!s.hasOwnProperty("raw"))throw Error("invalid template strings array");return Vs!==void 0?Vs.createHTML(i):i}var Za=(s,i)=>{let e=s.length-1,t=[],r,o=i===2?"<svg>":i===3?"<math>":"",n=bt;for(let a=0;a<e;a++){let l=s[a],d,h,c=-1,f=0;for(;f<l.length&&(n.lastIndex=f,h=n.exec(l),h!==null);)f=n.lastIndex,n===bt?h[1]==="!--"?n=Ns:h[1]!==void 0?n=Hs:h[2]!==void 0?(Ks.test(h[2])&&(r=RegExp("</"+h[2],"g")),n=Me):h[3]!==void 0&&(n=Me):n===Me?h[0]===">"?(n=r??bt,c=-1):h[1]===void 0?c=-2:(c=n.lastIndex-h[2].length,d=h[1],n=h[3]===void 0?Me:h[3]==='"'?js:Us):n===js||n===Us?n=Me:n===Ns||n===Hs?n=bt:(n=Me,r=void 0);let y=n===Me&&s[a+1].startsWith("/>")?" ":"";o+=n===bt?l+Qa:c>=0?(t.push(d),l.slice(0,c)+qs+l.slice(c)+be+y):l+be+(c===-2?a:y)}return[Ys(s,o+(s[e]||"<?>")+(i===2?"</svg>":i===3?"</math>":"")),t]},Ct=class s{constructor({strings:i,_$litType$:e},t){let r;this.parts=[];let o=0,n=0,a=i.length-1,l=this.parts,[d,h]=Za(i,e);if(this.el=s.createElement(d,t),Pe.currentNode=this.el.content,e===2||e===3){let c=this.el.content.firstChild;c.replaceWith(...c.childNodes)}for(;(r=Pe.nextNode())!==null&&l.length<a;){if(r.nodeType===1){if(r.hasAttributes())for(let c of r.getAttributeNames())if(c.endsWith(qs)){let f=h[n++],y=r.getAttribute(c).split(be),w=/([.?@])?(.*)/.exec(f);l.push({type:1,index:o,name:w[2],strings:y,ctor:w[1]==="."?Ki:w[1]==="?"?Yi:w[1]==="@"?Qi:We}),r.removeAttribute(c)}else c.startsWith(be)&&(l.push({type:6,index:o}),r.removeAttribute(c));if(Ks.test(r.tagName)){let c=r.textContent.split(be),f=c.length-1;if(f>0){r.textContent=Vt?Vt.emptyScript:"";for(let y=0;y<f;y++)r.append(c[y],yt()),Pe.nextNode(),l.push({type:2,index:++o});r.append(c[f],yt())}}}else if(r.nodeType===8)if(r.data===Gs)l.push({type:2,index:o});else{let c=-1;for(;(c=r.data.indexOf(be,c+1))!==-1;)l.push({type:7,index:o}),c+=be.length-1}o++}}static createElement(i,e){let t=Fe.createElement("template");return t.innerHTML=i,t}};function je(s,i,e=s,t){if(i===Oe)return i;let r=t!==void 0?e._$Co?.[t]:e._$Cl,o=xt(i)?void 0:i._$litDirective$;return r?.constructor!==o&&(r?._$AO?.(!1),o===void 0?r=void 0:(r=new o(s),r._$AT(s,e,t)),t!==void 0?(e._$Co??=[])[t]=r:e._$Cl=r),r!==void 0&&(i=je(s,r._$AS(s,i.values),r,t)),i}var Gi=class{constructor(i,e){this._$AV=[],this._$AN=void 0,this._$AD=i,this._$AM=e}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(i){let{el:{content:e},parts:t}=this._$AD,r=(i?.creationScope??Fe).importNode(e,!0);Pe.currentNode=r;let o=Pe.nextNode(),n=0,a=0,l=t[0];for(;l!==void 0;){if(n===l.index){let d;l.type===2?d=new wt(o,o.nextSibling,this,i):l.type===1?d=new l.ctor(o,l.name,l.strings,this,i):l.type===6&&(d=new Xi(o,this,i)),this._$AV.push(d),l=t[++a]}n!==l?.index&&(o=Pe.nextNode(),n++)}return Pe.currentNode=Fe,r}p(i){let e=0;for(let t of this._$AV)t!==void 0&&(t.strings!==void 0?(t._$AI(i,t,e),e+=t.strings.length-2):t._$AI(i[e])),e++}},wt=class s{get _$AU(){return this._$AM?._$AU??this._$Cv}constructor(i,e,t,r){this.type=2,this._$AH=M,this._$AN=void 0,this._$AA=i,this._$AB=e,this._$AM=t,this.options=r,this._$Cv=r?.isConnected??!0}get parentNode(){let i=this._$AA.parentNode,e=this._$AM;return e!==void 0&&i?.nodeType===11&&(i=e.parentNode),i}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(i,e=this){i=je(this,i,e),xt(i)?i===M||i==null||i===""?(this._$AH!==M&&this._$AR(),this._$AH=M):i!==this._$AH&&i!==Oe&&this._(i):i._$litType$!==void 0?this.$(i):i.nodeType!==void 0?this.T(i):Xa(i)?this.k(i):this._(i)}O(i){return this._$AA.parentNode.insertBefore(i,this._$AB)}T(i){this._$AH!==i&&(this._$AR(),this._$AH=this.O(i))}_(i){this._$AH!==M&&xt(this._$AH)?this._$AA.nextSibling.data=i:this.T(Fe.createTextNode(i)),this._$AH=i}$(i){let{values:e,_$litType$:t}=i,r=typeof t=="number"?this._$AC(i):(t.el===void 0&&(t.el=Ct.createElement(Ys(t.h,t.h[0]),this.options)),t);if(this._$AH?._$AD===r)this._$AH.p(e);else{let o=new Gi(r,this),n=o.u(this.options);o.p(e),this.T(n),this._$AH=o}}_$AC(i){let e=Ws.get(i.strings);return e===void 0&&Ws.set(i.strings,e=new Ct(i)),e}k(i){Ji(this._$AH)||(this._$AH=[],this._$AR());let e=this._$AH,t,r=0;for(let o of i)r===e.length?e.push(t=new s(this.O(yt()),this.O(yt()),this,this.options)):t=e[r],t._$AI(o),r++;r<e.length&&(this._$AR(t&&t._$AB.nextSibling,r),e.length=r)}_$AR(i=this._$AA.nextSibling,e){for(this._$AP?.(!1,!0,e);i!==this._$AB;){let t=Bs(i).nextSibling;Bs(i).remove(),i=t}}setConnected(i){this._$AM===void 0&&(this._$Cv=i,this._$AP?.(i))}},We=class{get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}constructor(i,e,t,r,o){this.type=1,this._$AH=M,this._$AN=void 0,this.element=i,this.name=e,this._$AM=r,this.options=o,t.length>2||t[0]!==""||t[1]!==""?(this._$AH=Array(t.length-1).fill(new String),this.strings=t):this._$AH=M}_$AI(i,e=this,t,r){let o=this.strings,n=!1;if(o===void 0)i=je(this,i,e,0),n=!xt(i)||i!==this._$AH&&i!==Oe,n&&(this._$AH=i);else{let a=i,l,d;for(i=o[0],l=0;l<o.length-1;l++)d=je(this,a[t+l],e,l),d===Oe&&(d=this._$AH[l]),n||=!xt(d)||d!==this._$AH[l],d===M?i=M:i!==M&&(i+=(d??"")+o[l+1]),this._$AH[l]=d}n&&!r&&this.j(i)}j(i){i===M?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,i??"")}},Ki=class extends We{constructor(){super(...arguments),this.type=3}j(i){this.element[this.name]=i===M?void 0:i}},Yi=class extends We{constructor(){super(...arguments),this.type=4}j(i){this.element.toggleAttribute(this.name,!!i&&i!==M)}},Qi=class extends We{constructor(i,e,t,r,o){super(i,e,t,r,o),this.type=5}_$AI(i,e=this){if((i=je(this,i,e,0)??M)===Oe)return;let t=this._$AH,r=i===M&&t!==M||i.capture!==t.capture||i.once!==t.once||i.passive!==t.passive,o=i!==M&&(t===M||r);r&&this.element.removeEventListener(this.name,this,t),o&&this.element.addEventListener(this.name,this,i),this._$AH=i}handleEvent(i){typeof this._$AH=="function"?this._$AH.call(this.options?.host??this.element,i):this._$AH.handleEvent(i)}},Xi=class{constructor(i,e,t){this.element=i,this.type=6,this._$AN=void 0,this._$AM=e,this.options=t}get _$AU(){return this._$AM._$AU}_$AI(i){je(this,i)}};var Ja=Zi.litHtmlPolyfillSupport;Ja?.(Ct,wt),(Zi.litHtmlVersions??=[]).push("3.3.3");var Nt=(s,i,e)=>{let t=e?.renderBefore??i,r=t._$litPart$;if(r===void 0){let o=e?.renderBefore??null;t._$litPart$=r=new wt(i.insertBefore(yt(),o),o,void 0,e??{})}return r._$AI(s),r};var tr=globalThis,_=class extends ce{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0}createRenderRoot(){let i=super.createRenderRoot();return this.renderOptions.renderBefore??=i.firstChild,i}update(i){let e=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(i),this._$Do=Nt(e,this.renderRoot,this.renderOptions)}connectedCallback(){super.connectedCallback(),this._$Do?.setConnected(!0)}disconnectedCallback(){super.disconnectedCallback(),this._$Do?.setConnected(!1)}render(){return Oe}};_._$litElement$=!0,_.finalized=!0,tr.litElementHydrateSupport?.({LitElement:_});var el=tr.litElementPolyfillSupport;el?.({LitElement:_});(tr.litElementVersions??=[]).push("4.2.2");window.Vaadin||={};window.Vaadin.featureFlags||={};function tl(s){return s.replace(/-[a-z]/gu,i=>i[1].toUpperCase())}var ue={};function m(s,i="25.2.8"){if(Object.defineProperty(s,"version",{get(){return i}}),s.experimental){let t=typeof s.experimental=="string"?s.experimental:`${tl(s.is.split("-").slice(1).join("-"))}Component`;if(!window.Vaadin.featureFlags[t]&&!ue[t]){ue[t]=new Set,ue[t].add(s),Object.defineProperty(window.Vaadin.featureFlags,t,{get(){return ue[t].size===0},set(r){r&&ue[t].size>0&&(ue[t].forEach(o=>{customElements.define(o.is,o)}),ue[t].clear())}});return}else if(ue[t]){ue[t].add(s);return}}let e=customElements.get(s.is);if(!e)customElements.define(s.is,s);else{let t=e.version;t&&s.version&&t===s.version?console.warn(`The component ${s.is} has been loaded twice`):console.error(`Tried to define ${s.is} version ${s.version} when version ${e.version} is already in use. Something will probably break.`)}}var il=/\/\*[\*!]\s+vaadin-dev-mode:start([\s\S]*)vaadin-dev-mode:end\s+\*\*\//i,Ht=window.Vaadin&&window.Vaadin.Flow&&window.Vaadin.Flow.clients;function rl(){function s(){return!0}return Qs(s)}function sl(){try{return ol()?!0:nl()?Ht?!al():!rl():!1}catch{return!1}}function ol(){return localStorage.getItem("vaadin.developmentmode.force")}function nl(){return["localhost","127.0.0.1"].indexOf(window.location.hostname)>=0}function al(){return!!(Ht&&Object.keys(Ht).map(i=>Ht[i]).filter(i=>i.productionMode).length>0)}function Qs(s,i){if(typeof s!="function")return;let e=il.exec(s.toString());if(e)try{s=new Function(e[1])}catch(t){console.log("vaadin-development-mode-detector: uncommentAndRun() failed",t)}return s(i)}window.Vaadin=window.Vaadin||{};var ir=function(s,i){if(window.Vaadin.developmentMode)return Qs(s,i)};window.Vaadin.developmentMode===void 0&&(window.Vaadin.developmentMode=sl());function ll(){}var Xs=function(){if(typeof ir=="function")return ir(ll)};var Zs=0,Js=0,qe=[],rr=!1;function dl(){rr=!1;let s=qe.length;for(let i=0;i<s;i++){let e=qe[i];if(e)try{e()}catch(t){setTimeout(()=>{throw t})}}qe.splice(0,s),Js+=s}var R={after(s){return{run(i){return window.setTimeout(i,s)},cancel(i){window.clearTimeout(i)}}},run(s,i){return window.setTimeout(s,i)},cancel(s){window.clearTimeout(s)}};var ie={run(s){return window.requestAnimationFrame(s)},cancel(s){window.cancelAnimationFrame(s)}};var Ut={run(s){return window.requestIdleCallback?window.requestIdleCallback(s):window.setTimeout(s,16)},cancel(s){window.cancelIdleCallback?window.cancelIdleCallback(s):window.clearTimeout(s)}};var z={run(s){rr||(rr=!0,queueMicrotask(()=>dl())),qe.push(s);let i=Zs;return Zs+=1,i},cancel(s){let i=s-Js;if(i>=0){if(!qe[i])throw new Error(`invalid async handle: ${s}`);qe[i]=null}}};var At=new Set,x=class s{static debounce(i,e,t){return i instanceof s?i._cancelAsync():i=new s,i.setConfig(e,t),i}constructor(){this._asyncModule=null,this._callback=null,this._timer=null}setConfig(i,e){this._asyncModule=i,this._callback=e,this._timer=this._asyncModule.run(()=>{this._timer=null,At.delete(this),this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),At.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&(this.cancel(),this._callback())}isActive(){return this._timer!=null}};function jt(s){At.add(s)}function hl(){let s=!!At.size;return At.forEach(i=>{try{i.flush()}catch(e){setTimeout(()=>{throw e})}}),s}var Re=()=>{let s;do s=hl();while(s)};var pe=[];function sr(s,i,e=s.getAttribute("dir")){i?s.setAttribute("dir",i):e!=null&&s.removeAttribute("dir")}function or(){return document.documentElement.getAttribute("dir")}function cl(){let s=or();pe.forEach(i=>{sr(i,s)})}var ul=new MutationObserver(cl);ul.observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]});var I=s=>class extends s{static get properties(){return{dir:{type:String,value:"",reflectToAttribute:!0,converter:{fromAttribute:e=>e||"",toAttribute:e=>e===""?null:e}}}}get __isRTL(){return this.getAttribute("dir")==="rtl"}connectedCallback(){super.connectedCallback(),(!this.hasAttribute("dir")||this.__restoreSubscription)&&(this.__subscribe(),sr(this,or(),null))}attributeChangedCallback(e,t,r){if(super.attributeChangedCallback(e,t,r),e!=="dir")return;let o=or(),n=r===o&&pe.indexOf(this)===-1,a=!r&&t&&pe.indexOf(this)===-1;n||a?(this.__subscribe(),sr(this,o,r)):r!==o&&t===o&&this.__unsubscribe()}disconnectedCallback(){super.disconnectedCallback(),this.__restoreSubscription=pe.includes(this),this.__unsubscribe()}_valueToNodeAttribute(e,t,r){r==="dir"&&t===""&&!e.hasAttribute("dir")||super._valueToNodeAttribute(e,t,r)}_attributeToProperty(e,t,r){e==="dir"&&!t?this.dir="":super._attributeToProperty(e,t,r)}__subscribe(){pe.includes(this)||pe.push(this)}__unsubscribe(){pe.includes(this)&&pe.splice(pe.indexOf(this),1)}};window.Vaadin||(window.Vaadin={});window.Vaadin.registrations||(window.Vaadin.registrations=[]);window.Vaadin.developmentModeCallback||(window.Vaadin.developmentModeCallback={});window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]=function(){Xs()};var nr,eo=new Set,A=s=>class extends I(s){static _ensureRegistrations(){let{is:e}=this;if(e&&!eo.has(e)){window.Vaadin.registrations.push(this),eo.add(e);let t=window.Vaadin.developmentModeCallback;t&&(nr=x.debounce(nr,Ut,()=>{t["vaadin-usage-statistics"]()}),jt(nr))}}constructor(){super(),document.doctype===null&&console.warn('Vaadin components require the "standards mode" declaration. Please add <!DOCTYPE html> to the HTML document.'),this.constructor._ensureRegistrations()}};var to=new WeakMap;function pl(s,i){let e=i;for(;e;){if(to.get(e)===s)return!0;e=Object.getPrototypeOf(e)}return!1}function P(s){return i=>{if(pl(s,i))return i;let e=s(i);return to.set(e,s),e}}function G(s,i){return s.split(".").reduce((e,t)=>e?e[t]:void 0,i)}function io(s,i,e){let t=s.split("."),r=t.pop(),o=t.reduce((n,a)=>n[a],e);o[r]=i}var ar={},_l=/([A-Z])/gu;function ro(s){return ar[s]||(ar[s]=s.replace(_l,"-$1").toLowerCase()),ar[s]}function so(s){return s[0].toUpperCase()+s.substring(1)}function lr(s){let[i,e]=s.split("("),t=e.replace(")","").split(",").map(r=>r.trim());return{method:i,observerProps:t}}function dr(s,i){return Object.prototype.hasOwnProperty.call(s,i)||(s[i]=new Map(s[i])),s[i]}var ml=s=>{class i extends s{static enabledWarnings=[];static createProperty(t,r){[String,Boolean,Number,Array].includes(r)&&(r={type:r}),r?.reflectToAttribute&&(r.reflect=!0),super.createProperty(t,r)}static getOrCreateMap(t){return dr(this,t)}static finalize(){if(window.litIssuedWarnings&&(window.litIssuedWarnings.add("no-override-create-property"),window.litIssuedWarnings.add("no-override-get-property-descriptor")),super.finalize(),Array.isArray(this.observers)){let t=this.getOrCreateMap("__complexObservers");this.observers.forEach(r=>{let{method:o,observerProps:n}=lr(r);t.set(o,n)})}}static addCheckedInitializer(t){super.addInitializer(r=>{r instanceof this&&t(r)})}static getPropertyDescriptor(t,r,o){let n=super.getPropertyDescriptor(t,r,o),a=n;if(this.getOrCreateMap("__propKeys").set(t,r),o.sync&&(a={get:n.get,set(l){let d=this[t];Bt(l,d)&&(this[r]=l,this.requestUpdate(t,d,o),this.hasUpdated&&this.performUpdate())},configurable:!0,enumerable:!0}),o.readOnly){let l=a.set;this.addCheckedInitializer(d=>{d[`_set${so(t)}`]=function(h){l.call(d,h)}}),a={get:a.get,set(){},configurable:!0,enumerable:!0}}if("value"in o&&this.addCheckedInitializer(l=>{let d=typeof o.value=="function"?o.value.call(l):o.value;o.readOnly?l[`_set${so(t)}`](d):l[t]=d}),o.observer){let l=o.observer;this.getOrCreateMap("__observers").set(t,l),this.addCheckedInitializer(d=>{d[l]||console.warn(`observer method ${l} not defined`)})}if(o.notify){if(!this.__notifyProps)this.__notifyProps=new Set;else if(!this.hasOwnProperty("__notifyProps")){let l=this.__notifyProps;this.__notifyProps=new Set(l)}this.__notifyProps.add(t)}if(o.computed){let l=`__assignComputed${t}`,d=lr(o.computed);this.prototype[l]=function(...h){this[t]=this[d.method](...h)},this.getOrCreateMap("__computedObservers").set(l,d.observerProps)}return o.attribute||(o.attribute=ro(t)),a}static get polylitConfig(){return{asyncFirstRender:!1}}connectedCallback(){super.connectedCallback();let{polylitConfig:t}=this.constructor;!this.hasUpdated&&!t.asyncFirstRender&&this.performUpdate()}firstUpdated(){super.firstUpdated(),this.$||(this.$={}),this.renderRoot.querySelectorAll("[id]").forEach(t=>{this.$[t.id]=t})}ready(){}willUpdate(t){this.constructor.__computedObservers&&this.__runComplexObservers(t,this.constructor.__computedObservers)}updated(t){let r=this.__isReadyInvoked;this.__isReadyInvoked=!0,this.constructor.__observers&&this.__runObservers(t,this.constructor.__observers),this.constructor.__complexObservers&&this.__runComplexObservers(t,this.constructor.__complexObservers),this.__dynamicPropertyObservers&&this.__runDynamicObservers(t,this.__dynamicPropertyObservers),this.__dynamicMethodObservers&&this.__runComplexObservers(t,this.__dynamicMethodObservers),this.constructor.__notifyProps&&this.__runNotifyProps(t,this.constructor.__notifyProps),r||this.ready()}setProperties(t){Object.entries(t).forEach(([r,o])=>{let n=this.constructor.__propKeys.get(r),a=this[n];this[n]=o,this.requestUpdate(r,a)}),this.hasUpdated&&this.performUpdate()}_createMethodObserver(t){let r=dr(this,"__dynamicMethodObservers"),{method:o,observerProps:n}=lr(t);r.set(o,n)}_createPropertyObserver(t,r){dr(this,"__dynamicPropertyObservers").set(r,t)}__runComplexObservers(t,r){r.forEach((o,n)=>{o.some(a=>t.has(a))&&(this[n]?this[n](...o.map(a=>this[a])):console.warn(`observer method ${n} not defined`))})}__runDynamicObservers(t,r){r.forEach((o,n)=>{t.has(o)&&this[n]&&this[n](this[o],t.get(o))})}__runObservers(t,r){t.forEach((o,n)=>{let a=r.get(n);a!==void 0&&this[a]&&this[a](this[n],o)})}__runNotifyProps(t,r){t.forEach((o,n)=>{r.has(n)&&this.dispatchEvent(new CustomEvent(`${ro(n)}-changed`,{detail:{value:this[n]}}))})}_get(t,r){return G(t,r)}_set(t,r,o){io(t,r,o)}}return i},g=P(ml);function oo(s){let i=[];for(;s;){if(s.nodeType===Node.DOCUMENT_NODE){i.push(s);break}if(s.nodeType===Node.DOCUMENT_FRAGMENT_NODE){i.push(s),s=s.host;continue}if(s.assignedSlot){s=s.assignedSlot;continue}s=s.parentNode}return i}function hr(s,i){return i?i.closest?.(s)||hr(s,i.getRootNode().host):null}function Wt(s){return s?new Set(s.split(" ")):new Set}function Et(s){return s?[...s].join(" "):""}function cr(s,i,e){let t=Wt(s.getAttribute(i));t.add(e),s.setAttribute(i,Et(t))}function no(s,i,e){let t=Wt(s.getAttribute(i));if(t.delete(e),t.size===0){s.removeAttribute(i);return}s.setAttribute(i,Et(t))}function ao(s){return s.nodeType===Node.TEXT_NODE&&s.textContent.trim()===""}var re=class{constructor(i,e,t={}){this.target=i,this.callback=e,this.forceInitial=t.forceInitial,this._storedNodes=[],this._isSlot=i instanceof HTMLSlotElement,this._connected=!1,this._scheduled=!1,this._boundSchedule=()=>{this._schedule()},this.connect(),t.syncInitial?this.flush():this._schedule()}connect(){this.target.addEventListener("slotchange",this._boundSchedule),this._connected=!0}disconnect(){this.target.removeEventListener("slotchange",this._boundSchedule),this._connected=!1}_schedule(){this._scheduled||(this._scheduled=!0,queueMicrotask(()=>{this._scheduled&&this.flush()}))}flush(){this._connected&&(this._scheduled=!1,this._processNodes())}_collectNodes(){let i=this._isSlot?[this.target]:[...this.target.querySelectorAll("slot")];return[...new Set(i.flatMap(e=>e.assignedNodes({flatten:!0})))]}_groupNodesBySlot(i){let e=new Map;return i.forEach(t=>{let r=t.assignedSlot;e.set(r,e.get(r)??[]),e.get(r).push(t)}),e}_collectMovedNodes(i){let e=this._groupNodesBySlot(i),t=this._groupNodesBySlot(this._storedNodes),r=[];return e.forEach((o,n)=>{let a=t.get(n)||[];new Set(a).difference(new Set(o)).size>0||a.forEach((l,d)=>{o.indexOf(l)!==d&&r.push(l)})}),r}_processNodes(){let i=this._collectNodes(),e=i.filter(o=>!this._storedNodes.includes(o)),t=this._storedNodes.filter(o=>!i.includes(o)),r=this._collectMovedNodes(i);(e.length||t.length||r.length||this.forceInitial)&&this.callback({addedNodes:e,currentNodes:i,movedNodes:r,removedNodes:t}),this.forceInitial&&(this.forceInitial=!1),this._storedNodes=i}};var fl=0;function ye(){return fl++}var T=class extends EventTarget{static generateId(i,e="default"){return`${e}-${i.localName}-${ye()}`}constructor(i,e,t,r={}){super();let{initializer:o,multiple:n,observe:a,useUniqueId:l,uniqueIdPrefix:d}=r;this.host=i,this.slotName=e,this.tagName=t,this.observe=typeof a=="boolean"?a:!0,this.multiple=typeof n=="boolean"?n:!1,this.slotInitializer=o,n&&(this.nodes=[]),l&&(this.defaultId=this.constructor.generateId(i,d||e))}hostConnected(){this.initialized||(this.multiple?this.initMultiple():this.initSingle(),this.observe&&this.observeSlot(),this.initialized=!0)}initSingle(){let i=this.getSlotChild();i?(this.node=i,this.initAddedNode(i)):(i=this.attachDefaultNode(),this.initNode(i))}initMultiple(){let i=this.getSlotChildren();if(i.length===0){let e=this.attachDefaultNode();e&&(this.nodes=[e],this.initNode(e))}else this.nodes=i,i.forEach(e=>{this.initAddedNode(e)})}attachDefaultNode(){let{host:i,slotName:e,tagName:t}=this,r=this.defaultNode;return!r&&t&&(r=document.createElement(t),r instanceof Element&&(e!==""&&r.setAttribute("slot",e),this.defaultNode=r)),r&&(this.node=r,i.appendChild(r)),r}getSlotChildren(){let{slotName:i}=this;return Array.from(this.host.childNodes).filter(e=>e.nodeType===Node.ELEMENT_NODE&&e.hasAttribute("data-slot-ignore")?!1:e.nodeType===Node.ELEMENT_NODE&&e.slot===i||e.nodeType===Node.TEXT_NODE&&e.textContent.trim()&&i==="")}getSlotChild(){return this.getSlotChildren()[0]}initNode(i){let{slotInitializer:e}=this;e&&e(i,this.host)}initCustomNode(i){}teardownNode(i){}initAddedNode(i){i!==this.defaultNode&&(this.initCustomNode(i),this.initNode(i))}observeSlot(){let{slotName:i}=this,e=i===""?"slot:not([name])":`slot[name=${i}]`,t=this.host.shadowRoot.querySelector(e);this.__slotObserver=new re(t,({addedNodes:r,removedNodes:o})=>{let n=this.multiple?this.nodes:[this.node],a=r.filter(l=>!ao(l)&&!n.includes(l)&&!(l.nodeType===Node.ELEMENT_NODE&&l.hasAttribute("data-slot-ignore")));o.length&&(this.nodes=n.filter(l=>!o.includes(l)),o.forEach(l=>{this.teardownNode(l)})),a?.length>0&&(this.multiple?(this.defaultNode&&this.defaultNode.remove(),this.nodes=[...n,...a].filter(l=>l!==this.defaultNode),a.forEach(l=>{this.initAddedNode(l)})):(this.node&&this.node.remove(),this.node=a[0],this.initAddedNode(this.node)))})}};var D=class extends T{constructor(i){super(i,"tooltip"),this.setTarget(i),this.__onContentChange=this.__onContentChange.bind(this)}initCustomNode(i){i.target=this.target,this.ariaTarget!==void 0&&(i.ariaTarget=this.ariaTarget),this.context!==void 0&&(i.context=this.context),this.manual!==void 0&&(i.manual=this.manual),this.position!==void 0&&(i._position=this.position),this.shouldShow!==void 0&&(i.shouldShow=this.shouldShow),this.manual||this.host.setAttribute("has-tooltip",""),this.__notifyChange(i),i.addEventListener("content-changed",this.__onContentChange)}teardownNode(i){this.manual||this.host.removeAttribute("has-tooltip"),i.removeEventListener("content-changed",this.__onContentChange),this.__notifyChange(null)}setAriaTarget(i){this.ariaTarget=i;let e=this.node;e&&(e.ariaTarget=i)}setContext(i){this.context=i;let e=this.node;e&&(e.context=i)}setManual(i){this.manual=i;let e=this.node;e&&(e.manual=i)}setPosition(i){this.position=i;let e=this.node;e&&(e._position=i)}setShouldShow(i){this.shouldShow=i;let e=this.node;e&&(e.shouldShow=i)}setTarget(i){this.target=i;let e=this.node;e&&(e.target=i)}open(i){let e=this.node;e?.isConnected&&e._stateController.open(i)}close(i){let e=this.node;e&&e._stateController.close(i)}__onContentChange(i){this.__notifyChange(i.target)}__notifyChange(i){this.dispatchEvent(new CustomEvent("tooltip-changed",{detail:{node:i}}))}};function qt(s){try{CSS.registerProperty(s)}catch(i){if(i instanceof DOMException&&i.name==="InvalidModificationError")console.warn(`The CSS property ${s.name} has already been registered.`);else throw i}}var lo=(s,...i)=>{let e=document.createElement("style");e.id=s,e.textContent=i.map(t=>t.toString()).join(`
`),document.head.insertAdjacentElement("afterbegin",e)};var Gt=class s extends EventTarget{#e;#r=new Set;#i;#t=!1;constructor(i){super(),this.#e=i,this.#i=new CSSStyleSheet}#s(i){let{propertyName:e}=i;this.#r.has(e)&&this.dispatchEvent(new CustomEvent("property-changed",{detail:{propertyName:e}}))}observe(i){this.connect(),!this.#r.has(i)&&(this.#r.add(i),this.#i.replaceSync(`
      :root::before, :host::before {
        content: '' !important;
        position: absolute !important;
        top: -9999px !important;
        left: -9999px !important;
        visibility: hidden !important;
        transition: 1ms allow-discrete step-end !important;
        transition-property: ${[...this.#r].join(", ")} !important;
      }
    `))}connect(){this.#t||(this.#e.adoptedStyleSheets.unshift(this.#i),this.#o.addEventListener("transitionstart",i=>this.#s(i)),this.#o.addEventListener("transitionend",i=>this.#s(i)),this.#t=!0)}disconnect(){this.#r.clear(),this.#e.adoptedStyleSheets=this.#e.adoptedStyleSheets.filter(i=>i!==this.#i),this.#o.removeEventListener("transitionstart",this.#s),this.#o.removeEventListener("transitionend",this.#s),this.#t=!1}get#o(){return this.#e.documentElement??this.#e.host}static for(i){return i.__cssPropertyObserver||=new s(i),i.__cssPropertyObserver}};function gl(s){let{baseStyles:i,themeStyles:e,elementStyles:t,lumoInjector:r}=s.constructor,o=s.__lumoStyleSheet;return o?[...r.includeBaseStyles?i??t:[],o,...e??[]]:t}function ur(s){Lt(s.shadowRoot,gl(s))}function pr(s,i){s.__lumoStyleSheet=i,ur(s)}function Kt(s){s.__lumoStyleSheet=void 0,ur(s)}var ho=new Set;function _r(s){ho.has(s)||(ho.add(s),console.warn(s))}var co=new WeakMap;function uo(s){try{return s.media.mediaText}catch{return _r('[LumoInjector] Browser denied to access property "mediaText" for some CSS rules, so they were skipped.'),""}}function vl(s){try{return s.cssRules}catch{return _r('[LumoInjector] Browser denied to access property "cssRules" for some CSS stylesheets, so they were skipped.'),[]}}function po(s,i={tags:new Map,modules:new Map}){for(let e of vl(s)){if(e instanceof CSSImportRule){let t=uo(e);t.startsWith("lumo_")?i.modules.set(t,[...e.styleSheet.cssRules]):po(e.styleSheet,i);continue}if(e instanceof CSSMediaRule){let t=uo(e);t.startsWith("lumo_")&&i.modules.set(t,[...e.cssRules]);continue}if(e instanceof CSSStyleRule&&e.cssText.includes("-inject")){for(let t of e.style){let r=t.match(/^--_lumo-(.*)-inject-modules$/u)?.[1];if(!r)continue;let o=e.style.getPropertyValue(t);i.tags.set(r,o.split(",").map(n=>n.trim().replace(/'|"/gu,"")))}continue}}return i}function _o(s){let i=new Map,e=new Map;for(let t of s){let r=co.get(t);r||(r=po(t),co.set(t,r)),i=new Map([...i,...r.tags]),e=new Map([...e,...r.modules])}return{tags:i,modules:e}}function mr(s){return`--_lumo-${s.is}-inject`}var Yt=class{#e;#r;#i=new Map;#t=new Map;constructor(i=document){this.#e=i,this.handlePropertyChange=this.handlePropertyChange.bind(this),this.#r=Gt.for(i),this.#r.addEventListener("property-changed",this.handlePropertyChange)}disconnect(){this.#r.removeEventListener("property-changed",this.handlePropertyChange),this.#i.clear(),this.#t.values().forEach(i=>i.forEach(Kt))}forceUpdate(){for(let i of this.#i.keys())this.#o(i)}componentConnected(i){let{lumoInjector:e}=i.constructor,{is:t}=e;this.#t.set(t,this.#t.get(t)??new Set),this.#t.get(t).add(i);let r=this.#i.get(t);if(r){r.cssRules.length>0&&pr(i,r);return}this.#s(t);let o=mr(e);this.#r.observe(o)}componentDisconnected(i){let{is:e}=i.constructor.lumoInjector;this.#t.get(e)?.delete(i),Kt(i)}handlePropertyChange(i){let{propertyName:e}=i.detail,t=e.match(/^--_lumo-(.*)-inject$/u)?.[1];t&&this.#o(t)}#s(i){this.#i.set(i,new CSSStyleSheet),this.#o(i)}#o(i){let{tags:e,modules:t}=_o(this.#d),r=(e.get(i)??[]).flatMap(n=>t.get(n)??[]).map(n=>n.cssText).join(`
`),o=this.#i.get(i);o.replaceSync(r),this.#t.get(i)?.forEach(n=>{r?pr(n,o):Kt(n)})}get#d(){let i=new Set;for(let e of[this.#e,document])i=i.union(new Set(e.styleSheets)),i=i.union(new Set(e.adoptedStyleSheets));return[...i]}};var mo=new Set;function fo(s){let i=s.getRootNode();return i.host&&i.host.constructor.version?fo(i.host):i}var b=s=>class extends s{static finalize(){super.finalize();let e=mr(this.lumoInjector);this.is&&!mo.has(e)&&(mo.add(e),qt({name:e,syntax:"<number>",inherits:!0,initialValue:"0"}))}static get lumoInjector(){return{is:this.is,includeBaseStyles:!1}}connectedCallback(){super.connectedCallback();let e=fo(this);e.__lumoInjectorDisabled||this.isConnected&&(e.__lumoInjector||=new Yt(e),this.__lumoInjector=e.__lumoInjector,this.__lumoInjector.componentConnected(this))}disconnectedCallback(){super.disconnectedCallback(),this.__lumoInjector&&(this.__lumoInjector.componentDisconnected(this),this.__lumoInjector=void 0)}};var Qt=s=>class extends s{static get properties(){return{_theme:{type:String,readOnly:!0}}}static get observedAttributes(){return[...super.observedAttributes,"theme"]}attributeChangedCallback(e,t,r){super.attributeChangedCallback(e,t,r),e==="theme"&&this._set_theme(r)}};var fr=[],bl=new Set,yl=new Set;function xl(s){return s&&Object.prototype.hasOwnProperty.call(s,"__themes")}function Cl(s,i){return(s||"").split(" ").some(e=>new RegExp(`^${e.split("*").join(".*")}$`,"u").test(i))}function wl(s){return s.map(i=>i.cssText).join(`
`)}var Al="vaadin-themable-mixin-style";function El(s,i){let e=document.createElement("style");e.id=Al,e.textContent=wl(s),i.content.appendChild(e)}function Il(s=""){let i=0;return s.startsWith("lumo-")||s.startsWith("material-")?i=1:s.startsWith("vaadin-")&&(i=2),i}function go(s){let i=[];return s.include&&[].concat(s.include).forEach(e=>{let t=fr.find(r=>r.moduleId===e);t?i.push(...go(t),...t.styles):console.warn(`Included moduleId ${e} not found in style registry`)},s.styles),i}function Sl(s){let i=`${s}-default-theme`,e=fr.filter(t=>t.moduleId!==i&&Cl(t.themeFor,s)).map(t=>({...t,styles:[...go(t),...t.styles],includePriority:Il(t.moduleId)})).sort((t,r)=>r.includePriority-t.includePriority);return e.length>0?e:fr.filter(t=>t.moduleId===i)}var v=s=>class extends Qt(s){constructor(){super(),bl.add(new WeakRef(this))}static finalize(){if(super.finalize(),this.is&&yl.add(this.is),this.elementStyles)return;let e=this.prototype._template;!e||xl(this)||El(this.getStylesForThis(),e)}static finalizeStyles(e){return this.baseStyles=e?[e].flat(1/0):[],this.themeStyles=this.getStylesForThis(),[...this.baseStyles,...this.themeStyles]}static getStylesForThis(){let e=s.__themes||[],t=Object.getPrototypeOf(this.prototype),r=(t?t.constructor.__themes:[])||[];this.__themes=[...e,...r,...Sl(this.is)];let o=this.__themes.flatMap(n=>n.styles);return o.filter((n,a)=>a===o.lastIndexOf(n))}};["--vaadin-text-color","--vaadin-text-color-disabled","--vaadin-text-color-secondary","--vaadin-border-color","--vaadin-border-color-secondary","--vaadin-background-color"].forEach(s=>{qt({name:s,syntax:"<color>",inherits:!0,initialValue:"transparent"})});lo("vaadin-base",p`
    @layer vaadin.base {
      html {
        /* Background color */
        --vaadin-background-color: light-dark(#fff, #222);

        /* Container colors */
        --vaadin-background-container: color-mix(in oklab, var(--vaadin-text-color) 5%, var(--vaadin-background-color));
        --vaadin-background-container-strong: color-mix(
          in oklab,
          var(--vaadin-text-color) 10%,
          var(--vaadin-background-color)
        );

        /* Border colors */
        --vaadin-border-color-secondary: color-mix(in oklab, var(--vaadin-text-color) 24%, transparent);
        --vaadin-border-color: color-mix(in oklab, var(--vaadin-text-color) 48%, transparent); /* Above 3:1 contrast */

        /* Text colors */
        /* Above 3:1 contrast */
        --vaadin-text-color-disabled: color-mix(in oklab, var(--vaadin-text-color) 48%, transparent);
        /* Above 4.5:1 contrast */
        --vaadin-text-color-secondary: color-mix(in oklab, var(--vaadin-text-color) 68%, transparent);
        /* Above 7:1 contrast */
        --vaadin-text-color: light-dark(#1f1f1f, white);

        /* Padding */
        --vaadin-padding-xs: 6px;
        --vaadin-padding-s: 8px;
        --vaadin-padding-m: 12px;
        --vaadin-padding-l: 16px;
        --vaadin-padding-xl: 24px;
        --vaadin-padding-block-container: var(--vaadin-padding-xs);
        --vaadin-padding-inline-container: var(--vaadin-padding-s);

        /* Gap/spacing */
        --vaadin-gap-xs: 6px;
        --vaadin-gap-s: 8px;
        --vaadin-gap-m: 12px;
        --vaadin-gap-l: 16px;
        --vaadin-gap-xl: 24px;

        /* Border radius */
        --vaadin-radius-s: 3px;
        --vaadin-radius-m: 6px;
        --vaadin-radius-l: 12px;

        /* Focus outline */
        --vaadin-focus-ring-width: 2px;
        --vaadin-focus-ring-color: var(--vaadin-text-color);

        /* Icons, used as mask-image */
        --_vaadin-icon-arrow-up: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 7-7 7 7"/><path d="M12 19V5"/></svg>');
        --_vaadin-icon-calendar: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/></svg>');
        --_vaadin-icon-checkmark: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5" /></svg>');
        --_vaadin-icon-chevron-down: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>');
        --_vaadin-icon-chevron-right: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>');
        --_vaadin-icon-clock: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 6v6l4 2"/><circle cx="12" cy="12" r="10"/></svg>');
        --_vaadin-icon-cross: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" /></svg>');
        --_vaadin-icon-drag: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><path d="M11 7c0 .82843-.6716 1.5-1.5 1.5C8.67157 8.5 8 7.82843 8 7s.67157-1.5 1.5-1.5c.8284 0 1.5.67157 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5-.82843 0-1.5-.6716-1.5-1.5s.67157-1.5 1.5-1.5c.8284 0 1.5.6716 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5-.82843 0-1.5-.6716-1.5-1.5s.67157-1.5 1.5-1.5c.8284 0 1.5.6716 1.5 1.5Zm5-10c0 .82843-.6716 1.5-1.5 1.5S13 7.82843 13 7s.6716-1.5 1.5-1.5S16 6.17157 16 7Zm0 5c0 .8284-.6716 1.5-1.5 1.5S13 12.8284 13 12s.6716-1.5 1.5-1.5 1.5.6716 1.5 1.5Zm0 5c0 .8284-.6716 1.5-1.5 1.5S13 17.8284 13 17s.6716-1.5 1.5-1.5 1.5.6716 1.5 1.5Z" fill="currentColor"/></svg>');
        --_vaadin-icon-ellipsis: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>');
        --_vaadin-icon-eye: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" /></svg>');
        --_vaadin-icon-eye-slash: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>');
        --_vaadin-icon-file: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/></svg>');
        --_vaadin-icon-fullscreen: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 3.75v4.5m0-4.5h4.5m-4.5 0L9 9M3.75 20.25v-4.5m0 4.5h4.5m-4.5 0L9 15M20.25 3.75h-4.5m4.5 0v4.5m0-4.5L15 9m5.25 11.25h-4.5m4.5 0v-4.5m0 4.5L15 15" /></svg>');
        --_vaadin-icon-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/></svg>');
        --_vaadin-icon-link: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>');
        --_vaadin-icon-menu: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" /></svg>');
        --_vaadin-icon-minus: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/></svg>');
        --_vaadin-icon-paper-airplane: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6 12 3.269 3.125A59.769 59.769 0 0 1 21.485 12 59.768 59.768 0 0 1 3.27 20.875L5.999 12Zm0 0h7.5" /></svg>');
        --_vaadin-icon-pen: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/></svg>');
        --_vaadin-icon-play: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M5.25 5.653c0-.856.917-1.398 1.667-.986l11.54 6.347a1.125 1.125 0 0 1 0 1.972l-11.54 6.347a1.125 1.125 0 0 1-1.667-.986V5.653Z" /></svg>');
        --_vaadin-icon-plus: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="M12 5v14"/></svg>');
        --_vaadin-icon-redo: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 7v6h-6"/><path d="M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3l3 2.7"/></svg>');
        --_vaadin-icon-refresh: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><path d="M22 10C22 10 19.995 7.26822 18.3662 5.63824C16.7373 4.00827 14.4864 3 12 3C7.02944 3 3 7.02944 3 12C3 16.9706 7.02944 21 12 21C16.1031 21 19.5649 18.2543 20.6482 14.5M22 10V4M22 10H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>');
        --_vaadin-icon-resize: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><path fill-rule="evenodd" clip-rule="evenodd" d="M18.5303 7.46967c.2929.29289.2929.76777 0 1.06066L8.53033 18.5304c-.29289.2929-.76777.2929-1.06066 0s-.29289-.7678 0-1.0607L17.4697 7.46967c.2929-.29289.7677-.29289 1.0606 0Zm0 4.50003c.2929.2929.2929.7678 0 1.0607l-5.5 5.5c-.2929.2928-.7677.2928-1.0606 0-.2929-.2929-.2929-.7678 0-1.0607l5.4999-5.5c.2929-.2929.7678-.2929 1.0607 0Zm0 4.5c.2929.2928.2929.7677 0 1.0606l-1 1.0001c-.2929.2928-.7677.2929-1.0606 0-.2929-.2929-.2929-.7678 0-1.0607l1-1c.2929-.2929.7677-.2929 1.0606 0Z" fill="currentColor"/></svg>');
        --_vaadin-icon-slash: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"><rect x="13.7812" y="4.22583" width="1.5" height="16" rx="0.75" transform="rotate(20 13.7812 4.22583)" fill="currentColor"/></svg>');
        --_vaadin-icon-sort: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="8" height="12" viewBox="0 0 8 12" fill="none"><path d="M7.49854 6.99951C7.92795 6.99951 8.15791 7.50528 7.87549 7.82861L4.37646 11.8296C4.17728 12.0571 3.82272 12.0571 3.62354 11.8296L0.125488 7.82861C-0.157248 7.50531 0.0719873 6.99956 0.501465 6.99951H7.49854ZM3.62354 0.17041C3.82275 -0.0573875 4.17725 -0.0573848 4.37646 0.17041L7.87549 4.17041C8.15825 4.49373 7.92806 5.00049 7.49854 5.00049L0.501465 4.99951C0.0719873 4.99946 -0.157248 4.49371 0.125488 4.17041L3.62354 0.17041Z" fill="black"/></svg>');
        --_vaadin-icon-undo: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 7v6h6"/><path d="M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13"/></svg>');
        --_vaadin-icon-upload: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12"/><path d="m17 8-5-5-5 5"/><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/></svg>');
        --_vaadin-icon-user: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>');
        --_vaadin-icon-warn: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>');

        /* Cursors for interactive elements */
        --vaadin-clickable-cursor: pointer;
        --vaadin-disabled-cursor: not-allowed;

        /* Use units so that the values can be used in calc() */
        --safe-area-inset-top: env(safe-area-inset-top, 0px);
        --safe-area-inset-right: env(safe-area-inset-right, 0px);
        --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
        --safe-area-inset-left: env(safe-area-inset-left, 0px);
        --safe-area-inset-inline-start: var(--safe-area-inset-left);
        --safe-area-inset-inline-end: var(--safe-area-inset-right);

        &:dir(rtl) {
          --safe-area-inset-inline-start: var(--safe-area-inset-right);
          --safe-area-inset-inline-end: var(--safe-area-inset-left);
        }
      }

      @supports not (color: hsl(0 0 0)) {
        html {
          --_vaadin-safari-17-deg: 1deg;
        }
      }

      @media (forced-colors: active) {
        html {
          --vaadin-background-color: Canvas;
          --vaadin-border-color: CanvasText;
          --vaadin-border-color-secondary: CanvasText;
          --vaadin-text-color-disabled: CanvasText;
          --vaadin-text-color-secondary: CanvasText;
          --vaadin-text-color: CanvasText;
          --vaadin-icon-color: CanvasText;
          --vaadin-focus-ring-color: Highlight;
        }
      }
    }
  `);var vo=p`
  :host {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    gap: var(--vaadin-button-gap, 0 var(--vaadin-gap-s));
    white-space: var(--vaadin-button-label-wrap, normal);
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    cursor: var(--vaadin-clickable-cursor);
    box-sizing: border-box;
    flex-shrink: 0;
    height: var(--vaadin-button-height, fit-content);
    margin: var(--vaadin-button-margin, 0);
    padding: var(--vaadin-button-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    font-family: var(--vaadin-button-font-family, inherit);
    font-size: var(--vaadin-button-font-size, inherit);
    line-height: var(--vaadin-button-line-height, inherit);
    font-weight: var(--vaadin-button-font-weight, 500);
    color: var(--vaadin-button-text-color, var(--vaadin-text-color));
    background: var(--vaadin-button-background, var(--vaadin-background-container));
    background-origin: border-box;
    border: var(--vaadin-button-border-width, 1px) solid
      var(--vaadin-button-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
    touch-action: manipulation;
  }

  :host([hidden]) {
    display: none !important;
  }

  .vaadin-button-container,
  [part='prefix'],
  [part='suffix'] {
    display: contents;
  }

  [part='label'] {
    overflow: hidden;
    text-overflow: ellipsis;
  }

  :host(:is([focus-ring], :focus-visible)) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: 1px;
  }

  :host([theme~='primary']) {
    --vaadin-button-background: var(--vaadin-text-color);
    --vaadin-button-text-color: var(--vaadin-background-color);
    --vaadin-button-border-color: transparent;
  }

  :host([theme~='tertiary']) {
    background: transparent;
    border-color: transparent;
  }

  :host([disabled]) {
    pointer-events: var(--_vaadin-button-disabled-pointer-events, none);
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
  }

  :host([disabled][theme~='primary']) {
    --vaadin-button-text-color: var(--vaadin-background-container-strong);
    --vaadin-button-background: var(--vaadin-text-color-disabled);
  }

  @media (forced-colors: active) {
    :host {
      --vaadin-button-border-width: 1px;
      --vaadin-button-background: ButtonFace;
      --vaadin-button-text-color: ButtonText;
    }

    :host([theme~='primary']) {
      forced-color-adjust: none;
      --vaadin-button-background: CanvasText;
      --vaadin-button-text-color: Canvas;
      --vaadin-icon-color: Canvas;
    }

    ::slotted(*) {
      forced-color-adjust: auto;
    }

    :host([disabled]) {
      --vaadin-button-background: transparent !important;
      --vaadin-button-border-color: GrayText !important;
      --vaadin-button-text-color: GrayText !important;
      opacity: 1;
    }
  }
`;var Tl=!1,kl=s=>s,xr=typeof document.head.style.touchAction=="string",br="__polymerGestures",gr="__polymerGesturesHandled",yr="__polymerGesturesTouchAction",bo=25,yo=5,Dl=2,Ml=["mousedown","mousemove","mouseup","click"],Pl=[0,1,4,2],Fl=(function(){try{return new MouseEvent("test",{buttons:1}).buttons===1}catch{return!1}})();function Cr(s){return Ml.indexOf(s)>-1}var wo=!1;(function(){try{let s=Object.defineProperty({},"passive",{get(){wo=!0}});window.addEventListener("test",null,s),window.removeEventListener("test",null,s)}catch{}})();function Ol(s){if(!(Cr(s)||s==="touchend")&&xr&&wo&&Tl)return{passive:!0}}var Rl=navigator.userAgent.match(/iP(?:[oa]d|hone)|Android/u),$l={button:!0,command:!0,fieldset:!0,input:!0,keygen:!0,optgroup:!0,option:!0,select:!0,textarea:!0};function Le(s){let i=s.type;if(!Cr(i))return!1;if(i==="mousemove"){let t=s.buttons??1;return s instanceof window.MouseEvent&&!Fl&&(t=Pl[s.which]||0),!!(t&1)}return(s.button??0)===0}function Ll(s){if(s.type==="click"){if(s.detail===0)return!0;let i=xe(s);if(!i.nodeType||i.nodeType!==Node.ELEMENT_NODE)return!0;let e=i.getBoundingClientRect(),t=s.pageX,r=s.pageY;return!(t>=e.left&&t<=e.right&&r>=e.top&&r<=e.bottom)}return!1}var _e={mouse:{target:null,mouseIgnoreJob:null},touch:{x:0,y:0,id:-1,scrollDecided:!1}};function zl(s){let i="auto",e=Eo(s);for(let t=0,r;t<e.length;t++)if(r=e[t],r[yr]){i=r[yr];break}return i}function Ao(s,i,e){s.movefn=i,s.upfn=e,document.addEventListener("mousemove",i),document.addEventListener("mouseup",e)}function Ge(s){document.removeEventListener("mousemove",s.movefn),document.removeEventListener("mouseup",s.upfn),s.movefn=null,s.upfn=null}var Eo=window.ShadyDOM&&window.ShadyDOM.noPatch?window.ShadyDOM.composedPath:s=>s.composedPath&&s.composedPath()||[],wr={},$e=[];function Bl(s,i){let e=document.elementFromPoint(s,i),t=e;for(;t?.shadowRoot&&!window.ShadyDOM;){let r=t;if(t=t.shadowRoot.elementFromPoint(s,i),r===t)break;t&&(e=t)}return e}function xe(s){let i=Eo(s);return i.length>0?i[0]:s.target}function Vl(s){let i=s.type,t=s.currentTarget[br];if(!t)return;let r=t[i];if(!r)return;if(!s[gr]&&(s[gr]={},i.startsWith("touch"))){let n=s.changedTouches[0];if(i==="touchstart"&&s.touches.length===1&&(_e.touch.id=n.identifier),_e.touch.id!==n.identifier)return;xr||(i==="touchstart"||i==="touchmove")&&Nl(s)}let o=s[gr];if(!o.skip){for(let n=0,a;n<$e.length;n++)a=$e[n],r[a.name]&&!o[a.name]&&a.flow&&a.flow.start.indexOf(s.type)>-1&&a.reset&&a.reset();for(let n=0,a;n<$e.length;n++)a=$e[n],r[a.name]&&!o[a.name]&&(o[a.name]=!0,a[i](s))}}function Nl(s){let i=s.changedTouches[0],e=s.type;if(e==="touchstart")_e.touch.x=i.clientX,_e.touch.y=i.clientY,_e.touch.scrollDecided=!1;else if(e==="touchmove"){if(_e.touch.scrollDecided)return;_e.touch.scrollDecided=!0;let t=zl(s),r=!1,o=Math.abs(_e.touch.x-i.clientX),n=Math.abs(_e.touch.y-i.clientY);s.cancelable&&(t==="none"?r=!0:t==="pan-x"?r=n>o:t==="pan-y"&&(r=o>n)),r?s.preventDefault():Xt("track")}}function se(s,i,e){return wr[i]?(Hl(s,i,e),!0):!1}function Hl(s,i,e){let t=wr[i],r=t.deps,o=t.name,n=s[br];n||(s[br]=n={});for(let a=0,l,d;a<r.length;a++)l=r[a],!(Rl&&Cr(l)&&l!=="click")&&(d=n[l],d||(n[l]=d={_count:0}),d._count===0&&s.addEventListener(l,Vl,Ol(l)),d[o]=(d[o]||0)+1,d._count=(d._count||0)+1);s.addEventListener(i,e),t.touchAction&&Zt(s,t.touchAction)}function Ar(s){$e.push(s),s.emits.forEach(i=>{wr[i]=s})}function Ul(s){for(let i=0,e;i<$e.length;i++){e=$e[i];for(let t=0,r;t<e.emits.length;t++)if(r=e.emits[t],r===s)return e}return null}function Zt(s,i){xr&&s instanceof HTMLElement&&z.run(()=>{s.style.touchAction=i}),s[yr]=i}function Er(s,i,e){let t=new Event(i,{bubbles:!0,cancelable:!0,composed:!0});if(t.detail=e,kl(s).dispatchEvent(t),t.defaultPrevented){let r=e.preventer||e.sourceEvent;r?.preventDefault&&r.preventDefault()}}function Xt(s){let i=Ul(s);i.info&&(i.info.prevent=!0)}Ar({name:"downup",deps:["mousedown","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["down","up"],info:{movefn:null,upfn:null},reset(){Ge(this.info)},mousedown(s){if(!Le(s))return;let i=xe(s),e=this,t=o=>{Le(o)||(It("up",i,o),Ge(e.info))},r=o=>{Le(o)&&It("up",i,o),Ge(e.info)};Ao(this.info,t,r),It("down",i,s)},touchstart(s){It("down",xe(s),s.changedTouches[0],s)},touchend(s){It("up",xe(s),s.changedTouches[0],s)}});function It(s,i,e,t){i&&Er(i,s,{x:e.clientX,y:e.clientY,sourceEvent:e,preventer:t,prevent(r){return Xt(r)}})}Ar({name:"track",touchAction:"none",deps:["mousedown","touchstart","touchmove","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["track"],info:{x:0,y:0,state:"start",started:!1,moves:[],addMove(s){this.moves.length>Dl&&this.moves.shift(),this.moves.push(s)},movefn:null,upfn:null,prevent:!1},reset(){this.info.state="start",this.info.started=!1,this.info.moves=[],this.info.x=0,this.info.y=0,this.info.prevent=!1,Ge(this.info)},mousedown(s){if(!Le(s))return;let i=xe(s),e=this,t=o=>{let n=o.clientX,a=o.clientY;xo(e.info,n,a)&&(e.info.state=e.info.started?o.type==="mouseup"?"end":"track":"start",e.info.state==="start"&&Xt("tap"),e.info.addMove({x:n,y:a}),Le(o)||(e.info.state="end",Ge(e.info)),i&&vr(e.info,i,o),e.info.started=!0)},r=o=>{e.info.started&&t(o),Ge(e.info)};Ao(this.info,t,r),this.info.x=s.clientX,this.info.y=s.clientY},touchstart(s){let i=s.changedTouches[0];this.info.x=i.clientX,this.info.y=i.clientY},touchmove(s){let i=xe(s),e=s.changedTouches[0],t=e.clientX,r=e.clientY;xo(this.info,t,r)&&(this.info.state==="start"&&Xt("tap"),this.info.addMove({x:t,y:r}),vr(this.info,i,e),this.info.state="track",this.info.started=!0)},touchend(s){let i=xe(s),e=s.changedTouches[0];this.info.started&&(this.info.state="end",this.info.addMove({x:e.clientX,y:e.clientY}),vr(this.info,i,e))}});function xo(s,i,e){if(s.prevent)return!1;if(s.started)return!0;let t=Math.abs(s.x-i),r=Math.abs(s.y-e);return t>=yo||r>=yo}function vr(s,i,e){if(!i)return;let t=s.moves[s.moves.length-2],r=s.moves[s.moves.length-1],o=r.x-s.x,n=r.y-s.y,a,l=0;t&&(a=r.x-t.x,l=r.y-t.y),Er(i,"track",{state:s.state,x:e.clientX,y:e.clientY,dx:o,dy:n,ddx:a,ddy:l,sourceEvent:e,hover(){return Bl(e.clientX,e.clientY)}})}Ar({name:"tap",deps:["mousedown","click","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["click","touchend"]},emits:["tap"],info:{x:NaN,y:NaN,prevent:!1},reset(){this.info.x=NaN,this.info.y=NaN,this.info.prevent=!1},mousedown(s){Le(s)&&(this.info.x=s.clientX,this.info.y=s.clientY)},click(s){Le(s)&&Co(this.info,s)},touchstart(s){let i=s.changedTouches[0];this.info.x=i.clientX,this.info.y=i.clientY},touchend(s){Co(this.info,s.changedTouches[0],s)}});function Co(s,i,e){let t=Math.abs(i.clientX-s.x),r=Math.abs(i.clientY-s.y),o=xe(e||i);!o||$l[o.localName]&&o.hasAttribute("disabled")||(isNaN(t)||isNaN(r)||t<=bo&&r<=bo||Ll(i))&&(s.prevent||Er(o,"tap",{x:i.clientX,y:i.clientY,sourceEvent:i,preventer:e}))}var jl=s=>class extends s{static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}_disabledChanged(e){this._setAriaDisabled(e)}_setAriaDisabled(e){e?this.setAttribute("aria-disabled","true"):this.removeAttribute("aria-disabled")}click(){this.disabled||super.click()}},le=P(jl);var Wl=s=>class extends s{ready(){super.ready(),this.addEventListener("keydown",e=>{this._onKeyDown(e)}),this.addEventListener("keyup",e=>{this._onKeyUp(e)})}_onKeyDown(e){switch(e.key){case"Enter":this._onEnter(e);break;case"Escape":this._onEscape(e);break;default:break}}_onKeyUp(e){}_onEnter(e){}_onEscape(e){}},U=P(Wl);var Ke=s=>class extends le(U(s)){get _activeKeys(){return[" "]}ready(){super.ready(),se(this,"down",e=>{this._shouldSetActive(e)&&this._setActive(!0)}),se(this,"up",()=>{this._setActive(!1)})}disconnectedCallback(){super.disconnectedCallback(),this._setActive(!1)}_shouldSetActive(e){return!this.disabled}_onKeyDown(e){super._onKeyDown(e),this._shouldSetActive(e)&&this._activeKeys.includes(e.key)&&(this._setActive(!0),document.addEventListener("keyup",t=>{this._activeKeys.includes(t.key)&&this._setActive(!1)},{once:!0}))}_setActive(e){this.toggleAttribute("active",e)}};var Sr=!1;window.addEventListener("keydown",()=>{Sr=!0},{capture:!0});window.addEventListener("mousedown",()=>{Sr=!1},{capture:!0});function St(){let s=document.activeElement||document.body;for(;s.shadowRoot&&s.shadowRoot.activeElement;)s=s.shadowRoot.activeElement;return s}function B(){return Sr}function Io(s){let i=s.style;if(i.visibility==="hidden"||i.display==="none")return!0;let e=window.getComputedStyle(s);return e.visibility==="hidden"||e.display==="none"}function ql(s,i){let e=Math.max(s.tabIndex,0),t=Math.max(i.tabIndex,0);return e===0||t===0?t>e:e>t}function Gl(s,i){let e=[];for(;s.length>0&&i.length>0;)ql(s[0],i[0])?e.push(i.shift()):e.push(s.shift());return e.concat(s,i)}function Ir(s){let i=s.length;if(i<2)return s;let e=Math.ceil(i/2),t=Ir(s.slice(0,e)),r=Ir(s.slice(e));return Gl(t,r)}function me(s){return s.checkVisibility?!s.checkVisibility({visibilityProperty:!0}):s.offsetParent===null&&s.clientWidth===0&&s.clientHeight===0?!0:Io(s)}function ze(s){return s.matches('[tabindex="-1"]')?!1:s.matches("input, select, textarea, button, object")?s.matches(":not([disabled])"):s.matches("a[href], area[href], iframe, [tabindex], [contentEditable]")}function Ce(s){return s.getRootNode().activeElement===s}function Kl(s){if(!ze(s))return-1;let i=s.getAttribute("tabindex")||0;return Number(i)}function So(s,i){if(s.nodeType!==Node.ELEMENT_NODE||Io(s))return!1;let e=s,t=Kl(e),r=t>0;t>=0&&i.push(e);let o=[];return e.localName==="slot"?o=e.assignedNodes({flatten:!0}):o=(e.shadowRoot||e).children,[...o].forEach(n=>{r=So(n,i)||r}),r}function To(s){let i=[];return So(s,i)?Ir(i):i}var Yl=s=>class extends s{get _keyboardActive(){return B()}ready(){this.addEventListener("focusin",e=>{this._shouldSetFocus(e)&&this._setFocused(!0)}),this.addEventListener("focusout",e=>{this._shouldRemoveFocus(e)&&this._setFocused(!1)}),super.ready()}disconnectedCallback(){super.disconnectedCallback(),this.hasAttribute("focused")&&this._setFocused(!1)}focus(e){super.focus(e),e?.focusVisible!==!1&&this.setAttribute("focus-ring","")}_setFocused(e){this.toggleAttribute("focused",e),this.toggleAttribute("focus-ring",e&&this._keyboardActive)}_shouldSetFocus(e){return!0}_shouldRemoveFocus(e){return!0}},j=P(Yl);var Ye=s=>class extends le(s){static get properties(){return{tabindex:{type:Number,reflectToAttribute:!0,observer:"_tabindexChanged",sync:!0},_lastTabIndex:{type:Number}}}_disabledChanged(e,t){super._disabledChanged(e,t),!this.__shouldAllowFocusWhenDisabled()&&(e?(this.tabindex!==void 0&&(this._lastTabIndex=this.tabindex),this.setAttribute("tabindex","-1")):t&&(this._lastTabIndex!==void 0?this.setAttribute("tabindex",this._lastTabIndex):this.tabindex=void 0))}_tabindexChanged(e){this.__shouldAllowFocusWhenDisabled()||this.disabled&&e!==-1&&(this._lastTabIndex=e,this.setAttribute("tabindex","-1"))}focus(e){(!this.disabled||this.__shouldAllowFocusWhenDisabled())&&super.focus(e)}__shouldAllowFocusWhenDisabled(){return!1}};var Ql=["mousedown","mouseup","click","dblclick","keypress","keydown","keyup"],Jt=s=>class extends Ke(Ye(j(s))){constructor(){super(),this.__onInteractionEvent=this.__onInteractionEvent.bind(this),Ql.forEach(e=>{this.addEventListener(e,this.__onInteractionEvent,!0)}),this.tabindex=0}get _activeKeys(){return["Enter"," "]}ready(){super.ready(),this.hasAttribute("role")||this.setAttribute("role","button"),this.__shouldAllowFocusWhenDisabled()&&this.style.setProperty("--_vaadin-button-disabled-pointer-events","auto")}_onKeyDown(e){super._onKeyDown(e),!(e.altKey||e.shiftKey||e.ctrlKey||e.metaKey)&&this._activeKeys.includes(e.key)&&(e.preventDefault(),this.click())}__onInteractionEvent(e){this.__shouldSuppressInteractionEvent(e)&&e.stopImmediatePropagation()}__shouldSuppressInteractionEvent(e){return this.disabled}};var Tr=class extends Jt(A(v(g(b(_))))){static get is(){return"vaadin-button"}static get styles(){return vo}static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0,sync:!0}}}render(){return u`
      <div class="vaadin-button-container">
        <span part="prefix" aria-hidden="true">
          <slot name="prefix"></slot>
        </span>
        <span part="label">
          <slot></slot>
        </span>
        <span part="suffix" aria-hidden="true">
          <slot name="suffix"></slot>
        </span>

        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new D(this),this.addController(this._tooltipController)}__shouldAllowFocusWhenDisabled(){return window.Vaadin.featureFlags.accessibleDisabledButtons}};m(Tr);var ko=(s,i=s)=>p`
  :host {
    align-items: baseline;
    column-gap: var(--vaadin-${C(i)}-gap, var(--vaadin-gap-s));
    grid-template: none;
    grid-template-columns: auto 1fr;
    grid-template-rows: repeat(auto-fill, minmax(0, max-content));
    -webkit-tap-highlight-color: transparent;
    --_cursor: var(--vaadin-clickable-cursor);
  }

  :host([disabled]) {
    --_cursor: var(--vaadin-disabled-cursor);
  }

  :host(:not([has-label])) {
    column-gap: 0;
  }

  [part='${C(s)}'],
  ::slotted(input),
  [part='label'],
  ::slotted(label) {
    grid-row: 1;
  }

  [part='label'],
  ::slotted(label) {
    font-size: var(--vaadin-${C(i)}-label-font-size, var(--vaadin-input-field-label-font-size, inherit));
    line-height: var(--vaadin-${C(i)}-label-line-height, var(--vaadin-input-field-label-line-height, inherit));
    font-weight: var(--vaadin-${C(i)}-label-font-weight, var(--vaadin-input-field-label-font-weight, 500));
    color: var(--vaadin-${C(i)}-label-color, var(--vaadin-input-field-label-color, var(--vaadin-text-color)));
    word-break: break-word;
    cursor: var(--_cursor);
  }

  [part='${C(s)}'],
  ::slotted(input) {
    grid-column: 1;
  }

  [part='label'],
  [part='helper-text'],
  [part='error-message'] {
    margin-bottom: 0;
    grid-column: 2;
    width: auto;
    min-width: auto;
  }

  [part='helper-text'],
  [part='error-message'] {
    margin-top: var(--_gap-s);
    grid-row: auto;
  }

  /* Baseline vertical alignment */
  :host::before {
    grid-row: 1;
    margin: 0;
    padding: 0;
    border: 0;
  }

  /* visually hidden */
  ::slotted(input) {
    cursor: inherit;
    align-self: stretch;
    appearance: none;
    cursor: var(--_cursor);
    /* Ensure minimum click target (WCAG) */
    margin: min(0px, (24px - 100%) / -2) !important;
    /* Extend the input to cover the gap between the checkbox/radio and label */
    margin-inline-end: calc(min(0px, (24px - 100%) / -2) - var(--vaadin-${C(i)}-gap, var(--vaadin-gap-s))) !important;
  }

  /* Control container (checkbox, radio button) */
  [part='${C(s)}'] {
    background: var(--vaadin-${C(i)}-background, var(--vaadin-background-color));
    border-color: var(--vaadin-${C(i)}-border-color, var(--vaadin-input-field-border-color, var(--vaadin-border-color)));
    border-radius: var(--vaadin-${C(i)}-border-radius, var(--vaadin-radius-s));
    border-style: var(--_border-style, solid);
    --_border-width: var(--vaadin-${C(i)}-border-width, var(--vaadin-input-field-border-width, 1px));
    border-width: var(--_border-width);
    box-sizing: border-box;
    --_color: var(--vaadin-${C(i)}-marker-color, var(--vaadin-${C(i)}-background, var(--vaadin-background-color)));
    color: var(--_color);
    height: var(--vaadin-${C(i)}-size, 1lh);
    width: var(--vaadin-${C(i)}-size, 1lh);
    position: relative;
    cursor: var(--_cursor);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  :host(:is([checked], [indeterminate])) {
    --vaadin-${C(i)}-background: var(--vaadin-text-color);
    --vaadin-${C(i)}-border-color: transparent;
  }

  :host([disabled]) {
    --vaadin-${C(i)}-background: var(--vaadin-input-field-disabled-background, var(--vaadin-background-container-strong));
    --vaadin-${C(i)}-border-color: transparent;
    --vaadin-${C(i)}-marker-color: var(--vaadin-text-color-disabled);
  }

  /* Focus ring */
  :host([focus-ring]) [part='${C(s)}'] {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--_border-width) * -1);
  }

  :host([focus-ring]:is([checked], [indeterminate])) [part='${C(s)}'] {
    outline-offset: 1px;
  }

  :host([readonly][focus-ring]) [part='${C(s)}'] {
    --vaadin-${C(i)}-border-color: transparent;
    outline-offset: calc(var(--_border-width) * -1);
    outline-style: dashed;
  }

  /* Checked indicator (checkmark, dot) */
  [part='${C(s)}']::after {
    content: '\\2003' / '';
    background: currentColor;
    border-radius: inherit;
    display: flex;
    align-items: center;
    --_filter: var(--vaadin-${C(i)}-marker-color, saturate(0) invert(1) hue-rotate(180deg) contrast(100) brightness(100));
    filter: var(--_filter);
  }

  :host(:not([checked], [indeterminate])) [part='${C(s)}']::after {
    opacity: 0;
  }

  @media (forced-colors: active) {
    :host(:is([checked], [indeterminate])) {
      --vaadin-${C(i)}-border-color: CanvasText !important;
    }

    :host(:is([checked], [indeterminate])) [part='${C(s)}'] {
      background: SelectedItem !important;
    }

    :host(:is([checked], [indeterminate])) [part='${C(s)}']::after {
      background: SelectedItemText !important;
    }

    :host([readonly]) [part='${C(s)}']::after {
      background: CanvasText !important;
    }

    :host([disabled]) {
      --vaadin-${C(i)}-border-color: GrayText !important;
    }

    :host([disabled]) [part='${C(s)}']::after {
      background: GrayText !important;
    }
  }
`;var ei=p`
  :host {
    --_helper-below-field: initial;
    --_helper-above-field: ;
    --_no-label: initial;
    --_has-label: ;
    --_no-helper: initial;
    --_has-helper: ;
    --_no-error: initial;
    --_has-error: ;
    --_gap: var(--vaadin-input-field-container-gap, var(--vaadin-gap-xs));
    --_gap-s: round(var(--_gap) / 3, 2px);
    display: inline-grid;
    grid-template:
      'label' auto var(--_helper-above-field, 'helper' auto) 'baseline' 0 'input' 1fr var(
        --_helper-below-field,
        'helper' auto
      )
      'error' auto / 100%;
    height: fit-content;
    outline: none;
    cursor: default;
    -webkit-tap-highlight-color: transparent;
  }

  :host([has-label]) {
    --_has-label: initial;
    --_no-label: ;
  }

  :host([has-helper]) {
    --_has-helper: initial;
    --_no-helper: ;
  }

  :host([has-error-message]) {
    --_has-error: initial;
    --_no-error: ;
  }

  :host([hidden]) {
    display: none !important;
  }

  :host(:not([has-label])) [part='label'],
  :host(:not([has-helper])) [part='helper-text'],
  :host(:not([has-error-message])) [part='error-message'] {
    display: none;
  }

  /* Baseline alignment guide */
  :host::before {
    content: '\\2003' / '';
    grid-column: 1;
    grid-row: var(--_has-label, label / baseline) var(--_no-label, label / input);
    align-self: var(--_has-label, end) var(--_no-label, start);
    font-size: var(--vaadin-input-field-value-font-size, inherit);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
    border: var(--vaadin-input-field-border-width, 1px) solid transparent;
    pointer-events: none;
    margin-bottom: var(--_no-label, 0)
      var(
        --_has-label,
        calc(
          var(
              --vaadin-field-baseline-input-height,
              (1lh + var(--vaadin-padding-block-container) * 2 + var(--vaadin-input-field-border-width, 1px) * 2)
            ) *
            -1
        )
      );
  }

  [class$='container'] {
    display: contents;
  }

  [part] {
    grid-column: 1;
  }

  [part='label'] {
    font-size: var(--vaadin-input-field-label-font-size, inherit);
    line-height: var(--vaadin-input-field-label-line-height, inherit);
    font-weight: var(--vaadin-input-field-label-font-weight, 500);
    color: var(--vaadin-input-field-label-color, var(--vaadin-text-color));
    word-break: break-word;
    position: relative;
    grid-area: label;
    margin-bottom: var(--_helper-below-field, var(--_gap)) var(--_helper-above-field, var(--_no-helper, var(--_gap)));
  }

  ::slotted(label) {
    cursor: inherit;
  }

  :host([disabled]) [part='label'],
  :host([disabled]) ::slotted(label) {
    opacity: 0.5;
  }

  :host([disabled]) [part='label'] ::slotted(label) {
    opacity: 1;
  }

  :host([required]) [part='label'] {
    padding-inline-end: 1em;
  }

  [part='required-indicator'] {
    display: inline-block;
    position: absolute;
    width: 1em;
    text-align: center;
    color: var(--vaadin-input-field-required-indicator-color, var(--vaadin-text-color-secondary));
  }

  [part='required-indicator']::after {
    content: var(--vaadin-input-field-required-indicator, '*');
  }

  :host(:not([required])) [part='required-indicator'] {
    display: none;
  }

  [part='label'],
  [part='helper-text'],
  [part='error-message'] {
    width: min-content;
    min-width: 100%;
    box-sizing: border-box;
  }

  [part='input-field'],
  [part='group-field'],
  [part='input-fields'] {
    grid-area: input;
  }

  [part='input-field'] {
    width: var(--vaadin-field-default-width, 12em);
    max-width: 100%;
    min-width: 100%;
  }

  :host([readonly]) [part='input-field'] {
    cursor: default;
  }

  :host([disabled]) [part='input-field'] {
    cursor: var(--vaadin-disabled-cursor);
  }

  [part='helper-text'] {
    font-size: var(--vaadin-input-field-helper-font-size, inherit);
    line-height: var(--vaadin-input-field-helper-line-height, inherit);
    font-weight: var(--vaadin-input-field-helper-font-weight, 400);
    color: var(--vaadin-input-field-helper-color, var(--vaadin-text-color-secondary));
    grid-area: helper;
    margin-top: var(--_helper-above-field, var(--_gap-s)) var(--_helper-below-field, var(--_gap));
    margin-bottom: var(--_helper-above-field, var(--_gap));
  }

  [part='error-message'] {
    font-size: var(--vaadin-input-field-error-font-size, inherit);
    line-height: var(--vaadin-input-field-error-line-height, inherit);
    font-weight: var(--vaadin-input-field-error-font-weight, 400);
    color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
    display: flex;
    gap: var(--vaadin-gap-xs);
    grid-area: error;
    margin-top: var(--_has-helper, var(--_helper-below-field, var(--_gap-s)) var(--_helper-above-field, var(--_gap)))
      var(--_no-helper, var(--_gap));
  }

  [part='error-message']::before {
    content: '';
    display: inline-block;
    flex: none;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-warn) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    background: currentColor;
  }

  :host([theme~='helper-above-field']) {
    --_helper-above-field: initial;
    --_helper-below-field: ;
  }

  @media (forced-colors: active) {
    [part='error-message']::before {
      background: CanvasText;
    }
  }
`;var Xl=p`
  [part='checkbox'] {
    color: var(--vaadin-checkbox-checkmark-color, var(--_color));
  }

  [part='checkbox']::after {
    inset: 0;
    mask: var(--_vaadin-icon-checkmark) 50% /
      var(--vaadin-checkbox-checkmark-size, var(--vaadin-checkbox-marker-size, 100%)) no-repeat;
    filter: var(--vaadin-checkbox-checkmark-color, var(--_filter));
  }

  :host([readonly]) {
    --vaadin-checkbox-background: transparent;
    --vaadin-checkbox-border-color: var(--vaadin-border-color);
    --vaadin-checkbox-marker-color: var(--vaadin-text-color);
    --_border-style: dashed;
  }

  :host([indeterminate]) [part='checkbox']::after {
    mask-image: var(--_vaadin-icon-minus);
  }
`,Do=[ei,ko("checkbox"),Xl];var Zl=s=>class extends j(Ye(s)){static get properties(){return{autofocus:{type:Boolean},focusElement:{type:Object,readOnly:!0,observer:"_focusElementChanged",sync:!0},_lastTabIndex:{value:0}}}constructor(){super(),this._boundOnBlur=this._onBlur.bind(this),this._boundOnFocus=this._onFocus.bind(this)}ready(){super.ready(),this.autofocus&&!this.disabled&&requestAnimationFrame(()=>{this.focus()})}focus(e){this.focusElement&&!this.disabled&&(this.focusElement.focus(),e?.focusVisible!==!1&&this.setAttribute("focus-ring",""))}blur(){this.focusElement&&this.focusElement.blur()}click(){this.focusElement&&!this.disabled&&this.focusElement.click()}_focusElementChanged(e,t){e?(e.disabled=this.disabled,this._addFocusListeners(e),this.__forwardTabIndex(this.tabindex)):t&&this._removeFocusListeners(t)}_addFocusListeners(e){e.addEventListener("blur",this._boundOnBlur),e.addEventListener("focus",this._boundOnFocus)}_removeFocusListeners(e){e.removeEventListener("blur",this._boundOnBlur),e.removeEventListener("focus",this._boundOnFocus)}_onFocus(e){e.stopPropagation(),this.dispatchEvent(new Event("focus"))}_onBlur(e){e.stopPropagation(),this.dispatchEvent(new Event("blur"))}_shouldSetFocus(e){return e.target===this.focusElement}_shouldRemoveFocus(e){return e.target===this.focusElement}_disabledChanged(e,t){super._disabledChanged(e,t),this.focusElement&&(this.focusElement.disabled=e),e&&this.blur()}_tabindexChanged(e){this.__forwardTabIndex(e)}__forwardTabIndex(e){e!==void 0&&this.focusElement&&(this.focusElement.tabIndex=e,e!==-1&&(this.tabindex=void 0)),this.disabled&&e&&(e!==-1&&(this._lastTabIndex=e),this.tabindex=void 0),e===void 0&&this.hasAttribute("tabindex")&&this.removeAttribute("tabindex")}},we=P(Zl);var kr=new WeakMap;function Jl(s){return kr.has(s)||kr.set(s,new Set),kr.get(s)}function ed(s,i){let e=document.createElement("style");e.textContent=s,i===document?document.head.appendChild(e):i.insertBefore(e,i.firstChild)}var td=s=>class extends s{get slotStyles(){return[]}connectedCallback(){super.connectedCallback(),this.__applySlotStyles()}__applySlotStyles(){let e=this.getRootNode(),t=Jl(e);this.slotStyles.forEach(r=>{t.has(r)||(ed(r,e),t.add(r))})}},ti=P(td);var id=s=>class extends s{static get properties(){return{stateTarget:{type:Object,observer:"_stateTargetChanged"}}}static get delegateAttrs(){return[]}static get delegateProps(){return[]}ready(){super.ready(),this._createDelegateAttrsObserver(),this._createDelegatePropsObserver()}_stateTargetChanged(e){e&&(this._ensureAttrsDelegated(),this._ensurePropsDelegated())}_createDelegateAttrsObserver(){this._createMethodObserver(`_delegateAttrsChanged(${this.constructor.delegateAttrs.join(", ")})`)}_createDelegatePropsObserver(){this._createMethodObserver(`_delegatePropsChanged(${this.constructor.delegateProps.join(", ")})`)}_ensureAttrsDelegated(){this.constructor.delegateAttrs.forEach(e=>{this._delegateAttribute(e,this[e])})}_ensurePropsDelegated(){this.constructor.delegateProps.forEach(e=>{this._delegateProperty(e,this[e])})}_delegateAttrsChanged(...e){this.constructor.delegateAttrs.forEach((t,r)=>{this._delegateAttribute(t,e[r])})}_delegatePropsChanged(...e){this.constructor.delegateProps.forEach((t,r)=>{this._delegateProperty(t,e[r])})}_delegateAttribute(e,t){this.stateTarget&&(e==="invalid"&&this._delegateAttribute("aria-invalid",t?"true":!1),typeof t=="boolean"?this.stateTarget.toggleAttribute(e,t):t?this.stateTarget.setAttribute(e,t):this.stateTarget.removeAttribute(e))}_delegateProperty(e,t){this.stateTarget&&(this.stateTarget[e]=t)}},Qe=P(id);var rd=s=>class extends s{static get properties(){return{inputElement:{type:Object,readOnly:!0,observer:"_inputElementChanged",sync:!0},type:{type:String,readOnly:!0},value:{type:String,value:"",observer:"_valueChanged",notify:!0,sync:!0}}}constructor(){super(),this._boundOnInput=this._onInput.bind(this),this._boundOnChange=this._onChange.bind(this)}get _hasValue(){return this.value!=null&&this.value!==""}get _inputElementValueProperty(){return"value"}get _inputElementValue(){return this.inputElement?this.inputElement[this._inputElementValueProperty]:void 0}set _inputElementValue(e){this.inputElement&&(this.inputElement[this._inputElementValueProperty]=e)}clear(){this.value="",this._inputElementValue=""}_addInputListeners(e){e.addEventListener("input",this._boundOnInput),e.addEventListener("change",this._boundOnChange)}_removeInputListeners(e){e.removeEventListener("input",this._boundOnInput),e.removeEventListener("change",this._boundOnChange)}_forwardInputValue(e){this.inputElement&&(this._inputElementValue=e??"")}_inputElementChanged(e,t){e?this._addInputListeners(e):t&&this._removeInputListeners(t)}_onInput(e){let t=e.composedPath()[0];this.__userInput=e.isTrusted,this.value=t.value,this.__userInput=!1}_onChange(e){}_toggleHasValue(e){this.toggleAttribute("has-value",e)}_valueChanged(e,t){this._toggleHasValue(this._hasValue),!(e===""&&t===void 0)&&(this.__userInput||this._forwardInputValue(e))}},Ae=P(rd);var Mo=s=>class extends Qe(le(Ae(s))){static get properties(){return{checked:{type:Boolean,value:!1,notify:!0,reflectToAttribute:!0,sync:!0}}}static get delegateProps(){return[...super.delegateProps,"checked"]}_onChange(e){let t=e.target;this._toggleChecked(t.checked)}_toggleChecked(e){this.checked=e}};var Dr=new Map;function Mr(s){return Dr.has(s)||Dr.set(s,new WeakMap),Dr.get(s)}function Po(s,i){s&&s.removeAttribute(i)}function Fo(s,i){if(!s||!i)return;let e=Mr(i);if(e.has(s))return;let t=Wt(s.getAttribute(i));e.set(s,new Set(t))}function Oo(s,i){if(!s||!i)return;let e=Mr(i),t=e.get(s);!t||t.size===0?s.removeAttribute(i):cr(s,i,Et(t)),e.delete(s)}function Xe(s,i,e={newId:null,oldId:null,fromUser:!1}){if(!s||!i)return;let{newId:t,oldId:r,fromUser:o}=e,n=Mr(i),a=n.get(s);if(!o&&a){r&&a.delete(r),t&&a.add(t);return}o&&(a?t||n.delete(s):Fo(s,i),Po(s,i)),no(s,i,r);let l=t||Et(a);l&&cr(s,i,l)}function Ro(s,i){Fo(s,i),Po(s,i)}var ii=class{constructor(i){this.host=i,this.__required=!1}setTarget(i){this.__target=i,this.__setAriaRequiredAttribute(this.__required),this.__setLabelIdToAriaAttribute(this.__labelId,this.__labelId),this.__labelIdFromUser!=null&&this.__setLabelIdToAriaAttribute(this.__labelIdFromUser,this.__labelIdFromUser,!0),this.__setErrorIdToAriaAttribute(this.__errorId),this.__setHelperIdToAriaAttribute(this.__helperId),this.setAriaLabel(this.__label)}setRequired(i){this.__setAriaRequiredAttribute(i),this.__required=i}setAriaLabel(i){this.__setAriaLabelToAttribute(i),this.__label=i}setLabelId(i,e=!1){let t=e?this.__labelIdFromUser:this.__labelId;this.__setLabelIdToAriaAttribute(i,t,e),e?this.__labelIdFromUser=i:this.__labelId=i}setErrorId(i){this.__setErrorIdToAriaAttribute(i,this.__errorId),this.__errorId=i}setHelperId(i){this.__setHelperIdToAriaAttribute(i,this.__helperId),this.__helperId=i}__setAriaLabelToAttribute(i){this.__target&&(i?(Ro(this.__target,"aria-labelledby"),this.__target.setAttribute("aria-label",i)):this.__label&&(Oo(this.__target,"aria-labelledby"),this.__target.removeAttribute("aria-label")))}__setLabelIdToAriaAttribute(i,e,t){Xe(this.__target,"aria-labelledby",{newId:i,oldId:e,fromUser:t})}__setErrorIdToAriaAttribute(i,e){Xe(this.__target,"aria-describedby",{newId:i,oldId:e,fromUser:!1})}__setHelperIdToAriaAttribute(i,e){Xe(this.__target,"aria-describedby",{newId:i,oldId:e,fromUser:!1})}__setAriaRequiredAttribute(i){this.__target&&(["input","textarea"].includes(this.__target.localName)||(i?this.__target.setAttribute("aria-required","true"):this.__target.removeAttribute("aria-required")))}};var oe=document.createElement("div");oe.style.position="fixed";oe.style.clip="rect(0px, 0px, 0px, 0px)";oe.setAttribute("aria-live","polite");document.body.appendChild(oe);var ri;function ne(s,i={}){let e=i.mode||"polite",t=i.timeout??150;e==="alert"?(oe.removeAttribute("aria-live"),oe.removeAttribute("role"),ri=x.debounce(ri,ie,()=>{oe.setAttribute("role","alert")})):(ri&&ri.cancel(),oe.removeAttribute("role"),oe.setAttribute("aria-live",e)),oe.textContent="",setTimeout(()=>{oe.textContent=s},t)}var Ee=class extends T{constructor(i,e,t,r={}){super(i,e,t,{...r,useUniqueId:!0})}initCustomNode(i){this.__updateNodeId(i),this.__notifyChange(i)}teardownNode(i){let e=this.getSlotChild();e&&e!==this.defaultNode?this.__notifyChange(e):(this.restoreDefaultNode(),this.updateDefaultNode(this.node))}attachDefaultNode(){let i=super.attachDefaultNode();return i&&this.__updateNodeId(i),i}restoreDefaultNode(){}updateDefaultNode(i){this.__notifyChange(i)}observeNode(i){this.__nodeObserver&&this.__nodeObserver.disconnect(),this.__nodeObserver=new MutationObserver(e=>{e.forEach(t=>{let r=t.target,o=r===this.node;t.type==="attributes"?o&&this.__updateNodeId(r):(o||r.parentElement===this.node)&&this.__notifyChange(this.node)})}),this.__nodeObserver.observe(i,{attributes:!0,attributeFilter:["id"],childList:!0,subtree:!0,characterData:!0})}__hasContent(i){return i?i.nodeType===Node.ELEMENT_NODE&&(customElements.get(i.localName)||i.children.length>0)||i.textContent&&i.textContent.trim()!=="":!1}__notifyChange(i){this.dispatchEvent(new CustomEvent("slot-content-changed",{detail:{hasContent:this.__hasContent(i),node:i}}))}__updateNodeId(i){let e=!this.nodes||i===this.nodes[0];i.nodeType===Node.ELEMENT_NODE&&(!this.multiple||e)&&!i.id&&(i.id=this.defaultId)}};var si=class extends Ee{constructor(i){super(i,"error-message","div")}setErrorMessage(i){this.errorMessage=i,this.updateDefaultNode(this.node)}setInvalid(i){this.invalid=i,this.updateDefaultNode(this.node)}initAddedNode(i){i!==this.defaultNode&&this.initCustomNode(i)}initNode(i){this.updateDefaultNode(i)}initCustomNode(i){i.textContent&&!this.errorMessage&&(this.errorMessage=i.textContent.trim()),super.initCustomNode(i)}restoreDefaultNode(){this.attachDefaultNode()}updateDefaultNode(i){let{errorMessage:e,invalid:t}=this,r=!!(t&&e&&e.trim()!=="");i&&(i.textContent=r?e:"",i.hidden=!r,r&&ne(e,{mode:"assertive"})),super.updateDefaultNode(i)}};var oi=class extends Ee{constructor(i){super(i,"helper",null)}setHelperText(i){this.helperText=i,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{helperText:i}=this;if(i&&i.trim()!==""){this.tagName="div";let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(i){i&&(i.textContent=this.helperText),super.updateDefaultNode(i)}initCustomNode(i){super.initCustomNode(i),this.observeNode(i)}};var Ze=class extends Ee{constructor(i){super(i,"label","label")}setLabel(i){this.label=i,this.getSlotChild()||this.restoreDefaultNode(),this.node===this.defaultNode&&this.updateDefaultNode(this.node)}restoreDefaultNode(){let{label:i}=this;if(i&&i.trim()!==""){let e=this.attachDefaultNode();this.observeNode(e)}}updateDefaultNode(i){i&&(i.textContent=this.label),super.updateDefaultNode(i)}initCustomNode(i){super.initCustomNode(i),this.observeNode(i)}};var $o=s=>class extends s{static get properties(){return{label:{type:String,observer:"_labelChanged"}}}constructor(){super(),this._labelController=new Ze(this),this._labelController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-label",e.detail.hasContent)})}get _labelId(){return this._labelNode?.id}get _labelNode(){return this._labelController.node}ready(){super.ready(),this.addController(this._labelController)}_labelChanged(e){this._labelController.setLabel(e)}};var sd=s=>class extends s{static get properties(){return{invalid:{type:Boolean,reflectToAttribute:!0,notify:!0,value:!1,sync:!0},manualValidation:{type:Boolean,value:!1},required:{type:Boolean,reflectToAttribute:!0,sync:!0}}}validate(){let i=this.checkValidity();return this._setInvalid(!i),this.dispatchEvent(new CustomEvent("validated",{detail:{valid:i}})),i}checkValidity(){return!this.required||!!this.value}_setInvalid(i){this._shouldSetInvalid(i)&&(this.invalid=i)}_shouldSetInvalid(i){return!0}_requestValidation(){this.manualValidation||this.validate()}},Je=P(sd);var Ie=s=>class extends Je($o(s)){static get properties(){return{ariaTarget:{type:Object,observer:"_ariaTargetChanged"},errorMessage:{type:String,observer:"_errorMessageChanged"},helperText:{type:String,observer:"_helperTextChanged"},accessibleName:{type:String,observer:"_accessibleNameChanged"},accessibleNameRef:{type:String,observer:"_accessibleNameRefChanged"}}}static get observers(){return["_invalidChanged(invalid)","_requiredChanged(required)"]}constructor(){super(),this._fieldAriaController=new ii(this),this._helperController=new oi(this),this._errorController=new si(this),this._errorController.addEventListener("slot-content-changed",e=>{this.toggleAttribute("has-error-message",e.detail.hasContent)}),this._labelController.addEventListener("slot-content-changed",e=>{let{hasContent:t,node:r}=e.detail;this.__labelChanged(t,r)}),this._helperController.addEventListener("slot-content-changed",e=>{let{hasContent:t,node:r}=e.detail;this.toggleAttribute("has-helper",t),this.__helperChanged(t,r)})}get _errorNode(){return this._errorController.node}get _helperNode(){return this._helperController.node}ready(){super.ready(),this.addController(this._fieldAriaController),this.addController(this._helperController),this.addController(this._errorController)}__helperChanged(e,t){e?this._fieldAriaController.setHelperId(t.id):this._fieldAriaController.setHelperId(null)}_accessibleNameChanged(e){this._fieldAriaController.setAriaLabel(e)}_accessibleNameRefChanged(e){this._fieldAriaController.setLabelId(e,!0)}__labelChanged(e,t){e?this._fieldAriaController.setLabelId(t.id):this._fieldAriaController.setLabelId(null)}_errorMessageChanged(e){this._errorController.setErrorMessage(e)}_helperTextChanged(e){this._helperController.setHelperText(e)}_ariaTargetChanged(e){e&&this._fieldAriaController.setTarget(e)}_requiredChanged(e){this._fieldAriaController.setRequired(e)}_invalidChanged(e){this._errorController.setInvalid(e),setTimeout(()=>{if(e){let t=this._errorNode;this._fieldAriaController.setErrorId(t?.id)}else this._fieldAriaController.setErrorId(null)})}};var W=class extends T{constructor(i,e,t={}){let{uniqueIdPrefix:r}=t;super(i,"input","input",{initializer:(o,n)=>{n.value&&(o.value=n.value),n.type&&o.setAttribute("type",n.type),o.id=this.defaultId,typeof e=="function"&&e(o)},useUniqueId:!0,uniqueIdPrefix:r})}};var V=class{constructor(i,e){this.input=i,this.__preventDuplicateLabelClick=this.__preventDuplicateLabelClick.bind(this),e.addEventListener("slot-content-changed",t=>{this.__initLabel(t.detail.node)}),this.__initLabel(e.node)}__initLabel(i){i&&(i.addEventListener("click",this.__preventDuplicateLabelClick),this.input&&i.setAttribute("for",this.input.id))}__preventDuplicateLabelClick(){let i=e=>{e.stopImmediatePropagation(),this.input.removeEventListener("click",i)};this.input.addEventListener("click",i)}};var Lo=s=>class extends ti(Ie(Mo(we(Ke(s))))){static get properties(){return{indeterminate:{type:Boolean,notify:!0,value:!1,reflectToAttribute:!0},name:{type:String,value:""},readonly:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["__readonlyChanged(readonly, inputElement)"]}static get delegateProps(){return[...super.delegateProps,"indeterminate"]}static get delegateAttrs(){return[...super.delegateAttrs,"name","invalid","required"]}constructor(){super(),this._setType("checkbox"),this._boundOnInputClick=this._onInputClick.bind(this),this.value="on",this.tabindex=0}get slotStyles(){return[`
          ${this.localName} > input[slot='input'] {
            opacity: 0;
          }
        `]}ready(){super.ready(),this.addController(new W(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new V(this.inputElement,this._labelController)),this._createPropertyObserver("checked","_checkedChanged")}_shouldSetActive(e){let[t]=e.composedPath(),r=t===this.inputElement||t.part.contains("required-indicator")||this._labelNode.contains(t)&&!t.closest("a");return this.readonly||!r?!1:super._shouldSetActive(e)}_addInputListeners(e){super._addInputListeners(e),e.addEventListener("click",this._boundOnInputClick)}_removeInputListeners(e){super._removeInputListeners(e),e.removeEventListener("click",this._boundOnInputClick)}_onInputClick(e){this.readonly&&e.preventDefault()}__readonlyChanged(e,t){t&&(e?t.setAttribute("aria-readonly","true"):t.removeAttribute("aria-readonly"))}_toggleChecked(e){this.indeterminate&&(this.indeterminate=!1),super._toggleChecked(e)}checkValidity(){return!this.required||!!this.checked}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}_checkedChanged(e,t){(e||t)&&this._requestValidation()}_requiredChanged(e){super._requiredChanged(e),e===!1&&this._requestValidation()}_onRequiredIndicatorClick(){this._labelNode.click()}};var Pr=class extends Lo(A(v(g(b(_))))){static get is(){return"vaadin-checkbox"}static get styles(){return Do}render(){return u`
      <div class="vaadin-checkbox-container">
        <div part="checkbox" aria-hidden="true"></div>
        <slot name="input"></slot>
        <div part="label">
          <slot name="label"></slot>
          <div part="required-indicator" @click="${this._onRequiredIndicatorClick}"></div>
        </div>
        <div part="helper-text">
          <slot name="helper"></slot>
        </div>
        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>
      <slot name="tooltip"></slot>
    `}ready(){super.ready(),this._tooltipController=new D(this),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}};m(Pr);var zo=p`
  :host {
    display: flex;
    align-items: center;
    --_radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
    border-radius:
      /* See https://developer.mozilla.org/en-US/docs/Web/CSS/border-radius */
      var(--vaadin-input-field-top-start-radius, var(--_radius))
      var(--vaadin-input-field-top-end-radius, var(--_radius))
      var(--vaadin-input-field-bottom-end-radius, var(--_radius))
      var(--vaadin-input-field-bottom-start-radius, var(--_radius));
    border: var(--vaadin-input-field-border-width, 1px) solid
      var(--vaadin-input-field-border-color, var(--vaadin-border-color));
    box-sizing: border-box;
    cursor: text;
    padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
    gap: var(--vaadin-input-field-gap, var(--vaadin-gap-s));
    background: var(--vaadin-input-field-background, var(--vaadin-background-color));
    color: var(--vaadin-input-field-value-color, var(--vaadin-text-color));
    font-size: var(--vaadin-input-field-value-font-size, inherit);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    font-weight: var(--vaadin-input-field-value-font-weight, 400);
  }

  :host([dir='rtl']) {
    --_radius: var(--vaadin-input-field-border-radius, var(--vaadin-radius-m));
    border-radius:
      /* Don't use logical props, see https://github.com/vaadin/vaadin-time-picker/issues/145 */
      var(--vaadin-input-field-top-end-radius, var(--_radius))
      var(--vaadin-input-field-top-start-radius, var(--_radius))
      var(--vaadin-input-field-bottom-start-radius, var(--_radius))
      var(--vaadin-input-field-bottom-end-radius, var(--_radius));
  }

  :host([hidden]) {
    display: none !important;
  }

  /* Reset the native input styles */
  ::slotted(:is(input, textarea)) {
    appearance: none;
    align-self: stretch;
    box-sizing: border-box;
    flex: auto;
    white-space: nowrap;
    overflow: hidden;
    width: 100%;
    height: auto;
    outline: none;
    margin: 0;
    padding: 0;
    border: 0;
    border-radius: 0;
    min-width: 0;
    font: inherit;
    font-size: 1em;
    color: inherit;
    background: transparent;
    cursor: inherit;
    text-align: inherit;
    caret-color: var(--vaadin-input-field-value-color);
  }

  ::slotted(*) {
    flex: none;
  }

  slot[name$='fix'] {
    cursor: auto;
  }

  ::slotted(:is(input, textarea))::placeholder {
    /* Use ::slotted(:is(input, textarea):placeholder-shown) to style the placeholder */
    /* because ::slotted(...)::placeholder does not work in Safari. */
    font: inherit;
    color: inherit;
  }

  ::slotted(:is(input, textarea):placeholder-shown) {
    color: var(--vaadin-input-field-placeholder-color, var(--vaadin-text-color-secondary));
  }

  :host(:focus-within) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-input-field-border-width, 1px) * -1);
  }

  :host([invalid]) {
    --vaadin-input-field-border-color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
  }

  :host([readonly]) {
    border-style: dashed;
  }

  :host([readonly]:focus-within) {
    outline-style: dashed;
    --vaadin-input-field-border-color: transparent;
  }

  :host([disabled]) {
    --vaadin-input-field-value-color: var(--vaadin-input-field-disabled-text-color, var(--vaadin-text-color-disabled));
    --vaadin-input-field-background: var(
      --vaadin-input-field-disabled-background,
      var(--vaadin-background-container-strong)
    );
    --vaadin-input-field-border-color: transparent;
  }

  :host([theme~='align-start']) slot:not([name])::slotted(*) {
    text-align: start;
  }

  :host([theme~='align-center']) slot:not([name])::slotted(*) {
    text-align: center;
  }

  :host([theme~='align-end']) slot:not([name])::slotted(*) {
    text-align: end;
  }

  :host([theme~='align-left']) slot:not([name])::slotted(*) {
    text-align: left;
  }

  :host([theme~='align-right']) slot:not([name])::slotted(*) {
    text-align: right;
  }

  @media (forced-colors: active) {
    :host {
      --vaadin-input-field-background: Field;
      --vaadin-input-field-value-color: FieldText;
      --vaadin-input-field-placeholder-color: GrayText;
    }

    :host([disabled]) {
      --vaadin-input-field-value-color: GrayText;
      --vaadin-icon-color: GrayText;
    }
  }
`;var Tt=class extends v(I(g(b(_)))){static get is(){return"vaadin-input-container"}static get styles(){return zo}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0},readonly:{type:Boolean,reflectToAttribute:!0},invalid:{type:Boolean,reflectToAttribute:!0}}}render(){return u`
      <slot name="prefix"></slot>
      <slot></slot>
      <slot name="suffix"></slot>
    `}ready(){super.ready(),this.addEventListener("pointerdown",i=>{i.target===this&&i.preventDefault()}),this.addEventListener("click",i=>{i.target===this&&this.shadowRoot.querySelector("slot:not([name])").assignedNodes({flatten:!0}).forEach(e=>e.focus&&e.focus())})}};m(Tt);var Se=p`
  :host {
    align-items: center;
    border-radius: var(--vaadin-item-border-radius, var(--vaadin-radius-m));
    box-sizing: border-box;
    cursor: var(--vaadin-clickable-cursor);
    display: flex;
    column-gap: var(--vaadin-item-gap, var(--vaadin-gap-s));
    height: var(--vaadin-item-height, auto);
    padding: var(--vaadin-item-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    -webkit-tap-highlight-color: transparent;
  }

  :host([focus-ring]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) / -1);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
    pointer-events: var(--_vaadin-item-disabled-pointer-events, none);
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='checkmark'] {
    color: var(--vaadin-item-checkmark-color, inherit);
    display: var(--vaadin-item-checkmark-display, none);
    visibility: hidden;
  }

  [part='checkmark']::before {
    content: '';
    display: block;
    background: currentColor;
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-checkmark) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    width: var(--vaadin-icon-size, 1lh);
  }

  :host([selected]) [part='checkmark'] {
    visibility: visible;
  }

  [part='content'] {
    flex: 1;
    display: flex;
    align-items: center;
    column-gap: inherit;
    justify-content: var(--vaadin-item-text-align, start);
  }

  @media (forced-colors: active) {
    [part='checkmark']::before {
      background: CanvasText;
    }
  }
`;var et=p`
  :host([focused]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) / -1);
  }
`;var tt=s=>class extends s{static get properties(){return{index:{type:Number},item:{type:Object},label:{type:String},selected:{type:Boolean,value:!1,reflectToAttribute:!0},focused:{type:Boolean,value:!1,reflectToAttribute:!0},renderer:{type:Function}}}static get observers(){return["__rendererOrItemChanged(renderer, index, item, selected, focused)","__updateLabel(label, renderer)"]}static get observedAttributes(){return[...super.observedAttributes,"hidden"]}attributeChangedCallback(e,t,r){e==="hidden"&&r!==null?this.index=void 0:super.attributeChangedCallback(e,t,r)}connectedCallback(){super.connectedCallback(),this._owner=this.parentNode.owner;let e=this._getHostDir();e&&this.setAttribute("dir",e)}_getHostDir(){return this._owner&&this._owner.$.overlay.getAttribute("dir")}requestContentUpdate(){if(!this.renderer||this.hidden)return;let e={index:this.index,item:this.item,focused:this.focused,selected:this.selected};this.renderer(this,this._owner,e)}__rendererOrItemChanged(e,t,r){r===void 0||t===void 0||(this._oldRenderer!==e&&(this.innerHTML="",delete this._$litPart$),e&&(this._oldRenderer=e,this.requestContentUpdate()))}__updateLabel(e,t){t||(this.textContent=e)}};var Fr=class extends tt(v(I(g(b(_))))){static get is(){return"vaadin-combo-box-item"}static get styles(){return[Se,et]}render(){return u`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(Fr);var Z=p`
  :host {
    z-index: 200;
    position: fixed;

    /* Despite of what the names say, <vaadin-overlay> is just a container
          for position/sizing/alignment. The actual overlay is the overlay part. */

    /* Default position constraints. Themes can
          override this to adjust the gap between the overlay and the viewport. */
    inset: max(env(safe-area-inset-top, 0px), var(--vaadin-overlay-viewport-inset, 8px))
      max(env(safe-area-inset-right, 0px), var(--vaadin-overlay-viewport-inset, 8px))
      max(env(safe-area-inset-bottom, 0px), var(--vaadin-overlay-viewport-bottom))
      max(env(safe-area-inset-left, 0px), var(--vaadin-overlay-viewport-inset, 8px));

    /* Override native [popover] user agent styles */
    width: auto;
    height: auto;
    border: none;
    padding: 0;
    background-color: transparent;
    overflow: visible;

    /* Use flexbox alignment for the overlay part. */
    display: flex;
    flex-direction: column; /* makes dropdowns sizing easier */
    /* Align to center by default. */
    align-items: center;
    justify-content: center;

    /* Allow centering when max-width/max-height applies. */
    margin: auto;

    /* The host is not clickable, only the overlay part is. */
    pointer-events: none;

    /* Remove tap highlight on touch devices. */
    -webkit-tap-highlight-color: transparent;

    /* CSS API for host */
    --vaadin-overlay-viewport-bottom: 8px;
  }

  :host([hidden]),
  :host(:not([opened]):not([closing])),
  :host(:not([opened]):not([closing])) [part='overlay'] {
    display: none !important;
  }

  [part='overlay'] {
    color: var(--vaadin-overlay-text-color, var(--vaadin-text-color));
    background: var(--vaadin-overlay-background, var(--vaadin-background-color));
    border: var(--vaadin-overlay-border-width, 1px) solid
      var(--vaadin-overlay-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-overlay-border-radius, var(--vaadin-radius-m));
    box-shadow: var(--vaadin-overlay-shadow, 0 8px 24px -4px rgba(0, 0, 0, 0.3));
    box-sizing: border-box;
    max-width: 100%;
    overflow: auto;
    overscroll-behavior: contain;
    pointer-events: auto;
    -webkit-tap-highlight-color: initial;

    /* CSS reset for font styles */
    font: initial;
    letter-spacing: initial;
    text-align: initial;
    text-decoration: initial;
    text-indent: initial;
    text-transform: initial;
    user-select: text;
    white-space: initial;
    word-spacing: initial;

    /* Inherit font-family */
    font-family: inherit;
  }

  [part='backdrop'] {
    background: var(--vaadin-overlay-backdrop-background, rgba(0, 0, 0, 0.2));
    content: '';
    inset: 0;
    pointer-events: auto;
    position: fixed;
    z-index: -1;
  }

  [part='overlay']:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  @media (forced-colors: active) {
    [part='overlay'] {
      border: 3px solid !important;
    }
  }
`;var ni=s=>s.test(navigator.userAgent),Or=s=>s.test(navigator.platform),od=s=>s.test(navigator.vendor),Rr=ni(/Android/u),kt=ni(/Chrome/u)&&od(/Google Inc/u),Bo=ni(/Firefox/u),nd=Or(/^iPad/u)||Or(/^Mac/u)&&navigator.maxTouchPoints>1,ad=Or(/^iPhone/u),Be=ad||nd,it=ni(/^((?!chrome|android).)*safari/iu),K=(()=>{try{return document.createEvent("TouchEvent"),!0}catch{return!1}})();var ai=class{saveFocus(i){this.focusNode=i||St()}restoreFocus(i){let e=this.focusNode;if(!e)return;let t={preventScroll:i?i.preventScroll:!1,focusVisible:i?i.focusVisible:!1};St()===document.body?setTimeout(()=>e.focus(t)):e.focus(t),this.focusNode=null}};var $r=[];var li=class{constructor(i){this.host=i,this.__trapNode=null,this.__onKeyDown=this.__onKeyDown.bind(this)}get __focusableElements(){return To(this.__trapNode)}get __focusedElementIndex(){let i=this.__focusableElements;return i.indexOf(i.filter(Ce).pop())}hostConnected(){document.addEventListener("keydown",this.__onKeyDown)}hostDisconnected(){document.removeEventListener("keydown",this.__onKeyDown)}trapFocus(i){if(this.__trapNode=i,this.__focusableElements.length===0)throw this.__trapNode=null,new Error("The trap node should have at least one focusable descendant or be focusable itself.");$r.push(this),this.__focusedElementIndex===-1&&this.__focusableElements[0].focus({focusVisible:B()})}releaseFocus(){this.__trapNode=null,$r.pop()}__onKeyDown(i){if(this.__trapNode&&this===Array.from($r).pop()&&i.key==="Tab"){if(i.defaultPrevented)return;i.preventDefault();let e=i.shiftKey;this.__focusNextElement(e)}}__focusNextElement(i=!1){let e=this.__focusableElements,t=i?-1:1,r=this.__focusedElementIndex,o=(e.length+r+t)%e.length,n=e[o];n.focus({focusVisible:!0}),n.localName==="input"&&n.select()}};var Vo=s=>class extends s{static get properties(){return{focusTrap:{type:Boolean,value:!1},restoreFocusOnClose:{type:Boolean,value:!1},restoreFocusNode:{type:HTMLElement}}}constructor(){super(),this.__focusTrapController=new li(this),this.__focusRestorationController=new ai}get _contentRoot(){return this}ready(){super.ready(),this.addController(this.__focusTrapController),this.addController(this.__focusRestorationController)}get _focusTrapRoot(){return this.$.overlay}_resetFocus(){if(this.focusTrap&&this.__focusTrapController.releaseFocus(),this.restoreFocusOnClose&&this._shouldRestoreFocus()){let e=B(),t=!e;this.__focusRestorationController.restoreFocus({preventScroll:t,focusVisible:e})}}_saveFocus(){this.restoreFocusOnClose&&this.__focusRestorationController.saveFocus(this.restoreFocusNode)}_trapFocus(){this.focusTrap&&!me(this._focusTrapRoot)&&this.__focusTrapController.trapFocus(this._focusTrapRoot)}_shouldRestoreFocus(){let e=St();return e===document.body||this._deepContains(e)}_deepContains(e){if(this._contentRoot.contains(e))return!0;let t=e,r=e.ownerDocument;for(;t&&t!==r&&t!==this._contentRoot;)t=t.parentNode||t.host;return t===this._contentRoot}};var di=new Set,hi=()=>[...di].filter(s=>!s.hasAttribute("closing")),Lr=s=>{let i=hi(),e=i.indexOf(s);return e===-1?[]:i.slice(e+1)},zr=(s,i)=>s._deepContains(i),No=(s,i=e=>!0)=>{let e=hi().filter(i);return s===e.pop()},Ho=s=>class extends s{get _last(){return No(this)}get _isAttached(){return di.has(this)}bringToFront(){if(No(this))return;let e=Lr(this),t=e.filter(r=>r._hasOverlayPositionMixin&&zr(this,r));t.length!==e.length&&[this,...t].forEach(r=>{r.matches(":popover-open")&&(r.hidePopover(),r.showPopover()),r._removeAttachedInstance(),r._appendAttachedInstance()})}_enterModalState(){document.body.style.pointerEvents!=="none"&&(this._previousDocumentPointerEvents=document.body.style.pointerEvents,document.body.style.pointerEvents="none"),hi().forEach(e=>{e!==this&&(e.$.overlay.style.pointerEvents="none")})}_exitModalState(){this._previousDocumentPointerEvents!==void 0&&(document.body.style.pointerEvents=this._previousDocumentPointerEvents,delete this._previousDocumentPointerEvents);let e=hi(),t;for(;(t=e.pop())&&!(t!==this&&(t.$.overlay.style.removeProperty("pointer-events"),!t.modeless)););}_appendAttachedInstance(){di.add(this)}_removeAttachedInstance(){this._isAttached&&di.delete(this)}};function Uo(s,i){let e=null,t,r=document.documentElement;function o(){t&&clearTimeout(t),e?.disconnect(),e=null}function n(a=!1,l=1){o();let{left:d,top:h,width:c,height:f}=s.getBoundingClientRect();if(a||i(),!c||!f)return;let y=Math.floor(h),w=Math.floor(r.clientWidth-(d+c)),S=Math.floor(r.clientHeight-(h+f)),Q=Math.floor(d),O={rootMargin:`${-y}px ${-w}px ${-S}px ${-Q}px`,threshold:Math.max(0,Math.min(1,l))||1},X=!0;function Rs(He){let Hi=He[0].intersectionRatio;if(Hi!==l){if(!X)return n();Hi?n(!1,Hi):t=setTimeout(()=>{n(!1,1e-7)},1e3)}X=!1}e=new IntersectionObserver(Rs,O),e.observe(s)}return n(!0),o}function F(s,i,e){let t=[s];s.owner&&t.push(s.owner),typeof e=="string"?t.forEach(r=>{r.setAttribute(i,e)}):e?t.forEach(r=>{r.setAttribute(i,"")}):t.forEach(r=>{r.removeAttribute(i)})}var J=s=>class extends Vo(Ho(s)){static get properties(){return{opened:{type:Boolean,notify:!0,observer:"_openedChanged",reflectToAttribute:!0,sync:!0},owner:{type:Object,sync:!0},model:{type:Object,sync:!0},renderer:{type:Object,sync:!0},modeless:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_modelessChanged",sync:!0},hidden:{type:Boolean,reflectToAttribute:!0,observer:"_hiddenChanged",sync:!0},withBackdrop:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_withBackdropChanged",sync:!0}}}static get observers(){return["_rendererOrDataChanged(renderer, owner, model, opened)"]}get _rendererRoot(){return this}constructor(){super(),this._boundMouseDownListener=this._mouseDownListener.bind(this),this._boundMouseUpListener=this._mouseUpListener.bind(this),this._boundOutsideClickListener=this._outsideClickListener.bind(this),this._boundKeydownListener=this._keydownListener.bind(this),Be&&(this._boundIosResizeListener=()=>this._detectIosNavbar())}firstUpdated(){super.firstUpdated(),this.popover="manual",this.addEventListener("click",()=>{}),this.$.backdrop&&this.$.backdrop.addEventListener("click",()=>{}),this.addEventListener("mouseup",()=>{document.activeElement===document.body&&this.$.overlay.getAttribute("tabindex")==="0"&&this.$.overlay.focus()}),this.addEventListener("animationcancel",()=>{this._flushAnimation("opening"),this._flushAnimation("closing")})}connectedCallback(){super.connectedCallback(),this._boundIosResizeListener&&(this._detectIosNavbar(),window.addEventListener("resize",this._boundIosResizeListener)),this.opened&&this._attachOverlay()}disconnectedCallback(){super.disconnectedCallback(),this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._boundIosResizeListener&&window.removeEventListener("resize",this._boundIosResizeListener)}requestContentUpdate(){this.renderer&&this.renderer.call(this.owner,this._rendererRoot,this.owner,this.model)}close(e){let t=new CustomEvent("vaadin-overlay-close",{bubbles:!0,cancelable:!0,detail:{overlay:this,sourceEvent:e}});this.dispatchEvent(t),document.body.dispatchEvent(t),t.defaultPrevented||(this.opened=!1)}setBounds(e,t=!0){let r=this.$.overlay,o={...e};t&&r.style.position!=="absolute"&&(r.style.position="absolute"),Object.keys(o).forEach(n=>{o[n]!==null&&!isNaN(o[n])&&(o[n]=`${o[n]}px`)}),Object.assign(r.style,o)}_detectIosNavbar(){if(!this.opened)return;let e=window.innerHeight,r=window.innerWidth>e,o=document.documentElement.clientHeight;r&&o>e?this.style.setProperty("--vaadin-overlay-viewport-bottom",`${o-e}px`):this.style.setProperty("--vaadin-overlay-viewport-bottom","0px")}_shouldAddGlobalListeners(){return!this.modeless}_addGlobalListeners(){this.__hasGlobalListeners||(this.__hasGlobalListeners=!0,document.addEventListener("mousedown",this._boundMouseDownListener),document.addEventListener("mouseup",this._boundMouseUpListener),document.documentElement.addEventListener("click",this._boundOutsideClickListener,!0))}_removeGlobalListeners(){this.__hasGlobalListeners&&(this.__hasGlobalListeners=!1,document.removeEventListener("mousedown",this._boundMouseDownListener),document.removeEventListener("mouseup",this._boundMouseUpListener),document.documentElement.removeEventListener("click",this._boundOutsideClickListener,!0))}_rendererOrDataChanged(e,t,r,o){let n=this._oldOwner!==t||this._oldModel!==r;this._oldModel=r,this._oldOwner=t;let a=this._oldRenderer!==e,l=this._oldRenderer!==void 0;this._oldRenderer=e;let d=this._oldOpened!==o;this._oldOpened=o,a&&l&&(this._rendererRoot.innerHTML="",delete this._rendererRoot._$litPart$),o&&e&&(a||d||n)&&this.requestContentUpdate()}_modelessChanged(e){this.opened&&(this._shouldAddGlobalListeners()?this._addGlobalListeners():this._removeGlobalListeners()),e?this._exitModalState():this.opened&&this._enterModalState(),F(this,"modeless",e)}_withBackdropChanged(e){F(this,"with-backdrop",e)}_openedChanged(e,t){if(e){if(!this.isConnected){this.opened=!1;return}this._saveFocus(),this._animatedOpening(),this.__scheduledOpen=requestAnimationFrame(()=>{setTimeout(()=>{this._trapFocus();let r=new CustomEvent("vaadin-overlay-open",{detail:{overlay:this},bubbles:!0});this.dispatchEvent(r),document.body.dispatchEvent(r)})}),document.addEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._addGlobalListeners()}else t&&(this.__scheduledOpen&&(cancelAnimationFrame(this.__scheduledOpen),this.__scheduledOpen=null),this._resetFocus(),this._animatedClosing(),document.removeEventListener("keydown",this._boundKeydownListener),this._shouldAddGlobalListeners()&&this._removeGlobalListeners())}_hiddenChanged(e){e&&this.hasAttribute("closing")&&this._flushAnimation("closing")}_shouldAnimate(){let e=getComputedStyle(this),t=e.getPropertyValue("animation-name");return!(e.getPropertyValue("display")==="none")&&t&&t!=="none"}_enqueueAnimation(e,t){let r=`__${e}Handler`,o=n=>{n&&n.target!==this||(t(),this.removeEventListener("animationend",o),delete this[r])};this[r]=o,this.addEventListener("animationend",o)}_flushAnimation(e){let t=`__${e}Handler`;typeof this[t]=="function"&&this[t]()}_animatedOpening(){this._isAttached&&this.hasAttribute("closing")&&this._flushAnimation("closing"),this._attachOverlay(),this._appendAttachedInstance(),this.bringToFront(),this.modeless||this._enterModalState(),F(this,"opening",!0),this._shouldAnimate()?this._enqueueAnimation("opening",()=>{this._finishOpening()}):this._finishOpening()}_attachOverlay(){this.matches(":popover-open")||this.showPopover()}_finishOpening(){F(this,"opening",!1)}_finishClosing(){this._detachOverlay(),this._removeAttachedInstance(),this.$.overlay.style.removeProperty("pointer-events"),F(this,"closing",!1),this.dispatchEvent(new CustomEvent("vaadin-overlay-closed"))}_animatedClosing(){this.hasAttribute("opening")&&this._flushAnimation("opening"),this._isAttached&&(this._exitModalState(),F(this,"closing",!0),this.dispatchEvent(new CustomEvent("vaadin-overlay-closing")),this._shouldAnimate()?this._enqueueAnimation("closing",()=>{this._finishClosing()}):this._finishClosing())}_detachOverlay(){this.hidePopover()}_mouseDownListener(e){this._mouseDownInside=e.composedPath().indexOf(this.$.overlay)>=0}_mouseUpListener(e){this._mouseUpInside=e.composedPath().indexOf(this.$.overlay)>=0}_shouldCloseOnOutsideClick(e){return this._last}_outsideClickListener(e){if(e.composedPath().includes(this.$.overlay)||this._mouseDownInside||this._mouseUpInside){this._mouseDownInside=!1,this._mouseUpInside=!1;return}if(!this._shouldCloseOnOutsideClick(e))return;let t=new CustomEvent("vaadin-overlay-outside-click",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(t),this.opened&&!t.defaultPrevented&&(this.close(e),!this.opened&&!this.modeless&&e.preventDefault())}_keydownListener(e){if(!(!this._last||e.defaultPrevented)&&!(!this._shouldAddGlobalListeners()&&!e.composedPath().includes(this._focusTrapRoot))&&e.key==="Escape"){let t=new CustomEvent("vaadin-overlay-escape-press",{cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(t),this.opened&&!t.defaultPrevented&&this.close(e)}}};var ci=p`
  @keyframes fade-in {
    0% {
      opacity: 0;
    }
  }

  @keyframes spin {
    to {
      rotate: 1turn;
    }
  }

  [part='loader'] {
    animation:
      spin var(--vaadin-spinner-animation-duration, 0.7s) linear infinite,
      fade-in 0.15s 0.3s both;
    border: var(--vaadin-spinner-width, 2px) solid var(--vaadin-spinner-color, var(--vaadin-text-color));
    border-radius: 50%;
    box-sizing: border-box;
    height: var(--vaadin-spinner-size, 1lh);
    mask-image: radial-gradient(circle at 50% var(--vaadin-spinner-width, 2px), transparent 40%, #000 70%);
    pointer-events: none;
    width: var(--vaadin-spinner-size, 1lh);
  }

  :host(:not([loading])) [part~='loader'] {
    display: none;
  }

  @media (forced-colors: active) {
    [part='loader'] {
      forced-color-adjust: none;
      --vaadin-spinner-color: CanvasText;
    }
  }
`;var ui=[ci,p`
    :host {
      --vaadin-item-checkmark-display: block;
    }

    [part='overlay'] {
      position: relative;
      width: var(--vaadin-combo-box-overlay-width, var(--_vaadin-combo-box-overlay-default-width, auto));
    }

    [part='content'] {
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    :host([loading]) [part='content'] {
      --_items-min-height: calc(var(--vaadin-icon-size, 1lh) + 4px);
    }

    [part='loader'] {
      position: absolute;
      inset: var(--vaadin-item-overlay-padding, 4px);
      inset-block-end: auto;
      inset-inline-start: auto;
      margin: 2px;
    }
  `];var Br={start:"top",end:"bottom"},Vr={start:"left",end:"right"},jo=new ResizeObserver(s=>{setTimeout(()=>{s.forEach(i=>{i.target.__overlay&&i.target.__overlay._updatePosition()})})}),rt=s=>class extends s{static get properties(){return{positionTarget:{type:Object,value:null,sync:!0},horizontalAlign:{type:String,value:"start",sync:!0},verticalAlign:{type:String,value:"top",sync:!0},noHorizontalOverlap:{type:Boolean,value:!1,sync:!0},noVerticalOverlap:{type:Boolean,value:!1,sync:!0},requiredVerticalSpace:{type:Number,value:0,sync:!0}}}constructor(){super(),this._hasOverlayPositionMixin=!0,this.__onScroll=this.__onScroll.bind(this),this._updatePosition=this._updatePosition.bind(this)}connectedCallback(){super.connectedCallback(),this.opened&&this.__addUpdatePositionEventListeners()}disconnectedCallback(){super.disconnectedCallback(),this.__removeUpdatePositionEventListeners()}updated(e){if(super.updated(e),e.has("positionTarget")){let r=e.get("positionTarget");this.__oldContentWidth=void 0,this.__oldContentHeight=void 0,(!this.positionTarget&&r||this.positionTarget&&!r&&this.__margins)&&this.__resetPosition()}(e.has("opened")||e.has("positionTarget"))&&this.__updatePositionSettings(this.opened,this.positionTarget),["horizontalAlign","verticalAlign","noHorizontalOverlap","noVerticalOverlap","requiredVerticalSpace"].some(r=>e.has(r))&&this._updatePosition()}__addUpdatePositionEventListeners(){window.visualViewport.addEventListener("resize",this._updatePosition),window.visualViewport.addEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes=oo(this.positionTarget),this.__positionTargetAncestorRootNodes.forEach(e=>{e.addEventListener("scroll",this.__onScroll,!0)}),this.positionTarget&&(this.__observePositionTargetMove=Uo(this.positionTarget,()=>{this._updatePosition()}))}__removeUpdatePositionEventListeners(){window.visualViewport.removeEventListener("resize",this._updatePosition),window.visualViewport.removeEventListener("scroll",this.__onScroll,!0),this.__positionTargetAncestorRootNodes&&(this.__positionTargetAncestorRootNodes.forEach(e=>{e.removeEventListener("scroll",this.__onScroll,!0)}),this.__positionTargetAncestorRootNodes=null),this.__observePositionTargetMove&&(this.__observePositionTargetMove(),this.__observePositionTargetMove=null)}__updatePositionSettings(e,t){if(this.__removeUpdatePositionEventListeners(),t&&(t.__overlay=null,jo.unobserve(t),e&&(this.__addUpdatePositionEventListeners(),t.__overlay=this,jo.observe(t))),e){let r=getComputedStyle(this);this.__margins||(this.__margins={},["top","bottom","left","right"].forEach(o=>{this.__margins[o]=parseInt(r[o],10)})),this._updatePosition(),requestAnimationFrame(()=>this._updatePosition())}}__onScroll(e){e.target instanceof Node&&this._deepContains(e.target)||this._updatePosition()}__resetPosition(){this.__margins=null,Object.assign(this.style,{justifyContent:"",alignItems:"",top:"",bottom:"",left:"",right:""}),F(this,"bottom-aligned",!1),F(this,"top-aligned",!1),F(this,"end-aligned",!1),F(this,"start-aligned",!1)}_updatePosition(){if(!this.positionTarget||!this.opened||!this.__margins)return;let e=this.positionTarget.getBoundingClientRect();if(e.width===0&&e.height===0&&this.opened){this.opened=!1;return}let t=this.__shouldAlignStartVertically(e);this.style.justifyContent=t?"flex-start":"flex-end";let r=this.__isRTL,o=this.__shouldAlignStartHorizontally(e,r),n=!r&&o||r&&!o;this.style.alignItems=n?"flex-start":"flex-end";let a=this.getBoundingClientRect(),l=this.__calculatePositionInOneDimension(e,a,this.noVerticalOverlap,Br,this,t),d=this.__calculatePositionInOneDimension(e,a,this.noHorizontalOverlap,Vr,this,o);Object.assign(this.style,l,d),F(this,"bottom-aligned",!t),F(this,"top-aligned",t),F(this,"end-aligned",!n),F(this,"start-aligned",n)}__shouldAlignStartHorizontally(e,t){let r=Math.max(this.__oldContentWidth||0,this.$.overlay.offsetWidth);this.__oldContentWidth=this.$.overlay.offsetWidth;let o=Math.min(window.innerWidth,document.documentElement.clientWidth),n=!t&&this.horizontalAlign==="start"||t&&this.horizontalAlign==="end";return this.__shouldAlignStart(e,r,o,this.__margins,n,this.noHorizontalOverlap,Vr)}__shouldAlignStartVertically(e){let t=this.requiredVerticalSpace||Math.max(this.__oldContentHeight||0,this.$.overlay.offsetHeight);this.__oldContentHeight=this.$.overlay.offsetHeight;let r=Math.min(window.innerHeight,document.documentElement.clientHeight),o=this.verticalAlign==="top";return this.__shouldAlignStart(e,t,r,this.__margins,o,this.noVerticalOverlap,Br)}__shouldAlignStart(e,t,r,o,n,a,l){let d=r-e[a?l.end:l.start]-o[l.end],h=e[a?l.start:l.end]-o[l.start],c=n?d:h,y=c>(n?h:d)||c>t;return n===y}__adjustBottomProperty(e,t,r){let o;if(e===t.end){if(t.end===Br.end){let n=Math.min(window.innerHeight,document.documentElement.clientHeight);if(r>n&&this.__oldViewportHeight){let a=this.__oldViewportHeight-n;o=r-a}this.__oldViewportHeight=n}if(t.end===Vr.end){let n=Math.min(window.innerWidth,document.documentElement.clientWidth);if(r>n&&this.__oldViewportWidth){let a=this.__oldViewportWidth-n;o=r-a}this.__oldViewportWidth=n}}return o}__calculatePositionInOneDimension(e,t,r,o,n,a){let l=a?o.start:o.end,d=a?o.end:o.start,h=parseFloat(n.style[l]||getComputedStyle(n)[l]),c=this.__adjustBottomProperty(l,o,h),f=t[a?o.start:o.end]-e[r===a?o.end:o.start],y=c?`${c}px`:`${h+f*(a?-1:1)}px`;return{[l]:y,[d]:""}}};var st=s=>class extends rt(s){static get observers(){return["_setOverlayWidth(positionTarget, opened)"]}constructor(){super(),this.requiredVerticalSpace=200}_shouldCloseOnOutsideClick(e){let t=e.composedPath();return!t.includes(this.positionTarget)&&!t.includes(this)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!ze(e.composedPath()[0])&&e.preventDefault()}_updateOverlayWidth(){this.style.setProperty(`--_${this.localName}-default-width`,`${this.positionTarget.offsetWidth}px`)}_setOverlayWidth(e,t){e&&t&&(this._updateOverlayWidth(),this._updatePosition())}};var Nr=class extends st(J(I(v(g(b(_)))))){static get is(){return"vaadin-combo-box-overlay"}static get styles(){return[Z,ui]}render(){return u`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};m(Nr);var Ve=p`
  :host {
    /* Fixes scrollbar disappearing when 'Show scroll bars: Always' enabled in Safari */
    box-shadow: 0 0 0 white;
    display: block;
    min-height: 1px;
    overflow: auto;
    /* Fixes item background from getting on top of scrollbars on Safari */
    transform: translate3d(0, 0, 0);
  }

  #selector {
    border: 0 solid transparent;
    border-width: var(--vaadin-item-overlay-padding, 4px);
    position: relative;
    forced-color-adjust: none;
    min-height: var(--_items-min-height, auto);
  }

  #selector > * {
    forced-color-adjust: auto;
  }
`;var Wo=navigator.userAgent.match(/iP(?:hone|ad;(?: U;)? CPU) OS (\d+)/u),ld=Wo&&Wo[1]>=8,qo=3,Go={_ratio:.5,_scrollerPaddingTop:0,_scrollPosition:0,_physicalSize:0,_physicalAverage:0,_physicalAverageCount:0,_physicalTop:0,_virtualCount:0,_estScrollHeight:0,_scrollHeight:0,_viewportHeight:0,_viewportWidth:0,_physicalItems:null,_physicalSizes:null,_firstVisibleIndexVal:null,_lastVisibleIndexVal:null,_maxPages:2,_templateCost:0,get _physicalBottom(){return this._physicalTop+this._physicalSize},get _scrollBottom(){return this._scrollPosition+this._viewportHeight},get _virtualEnd(){return this._virtualStart+this._physicalCount-1},get _hiddenContentSize(){return this._physicalSize-this._viewportHeight},get _maxScrollTop(){return this._estScrollHeight-this._viewportHeight+this._scrollOffset},get _maxVirtualStart(){let s=this._virtualCount;return Math.max(0,s-this._physicalCount)},get _virtualStart(){return this._virtualStartVal||0},set _virtualStart(s){s=this._clamp(s,0,this._maxVirtualStart),this._virtualStartVal=s},get _physicalStart(){return this._physicalStartVal||0},set _physicalStart(s){s%=this._physicalCount,s<0&&(s=this._physicalCount+s),this._physicalStartVal=s},get _physicalEnd(){return(this._physicalStart+this._physicalCount-1)%this._physicalCount},get _physicalCount(){return this._physicalCountVal||0},set _physicalCount(s){this._physicalCountVal=s},get _optPhysicalSize(){return this._viewportHeight===0?1/0:this._viewportHeight*this._maxPages},get _isVisible(){return!!(this.offsetWidth||this.offsetHeight)},get firstVisibleIndex(){let s=this._firstVisibleIndexVal;if(s==null){let i=this._physicalTop+this._scrollOffset;s=this._iterateItems((e,t)=>{if(i+=this._getPhysicalSizeIncrement(e),i>this._scrollPosition)return t})||0,this._firstVisibleIndexVal=s}return s},get lastVisibleIndex(){let s=this._lastVisibleIndexVal;if(s==null){let i=this._physicalTop+this._scrollOffset;this._iterateItems((e,t)=>{i<this._scrollBottom&&(s=t),i+=this._getPhysicalSizeIncrement(e)}),this._lastVisibleIndexVal=s}return s},get _scrollOffset(){return this._scrollerPaddingTop+this.scrollOffset},_scrollHandler(){let s=Math.max(0,Math.min(this._maxScrollTop,this._scrollTop)),i=s-this._scrollPosition,e=i>=0;if(this._scrollPosition=s,this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,Math.abs(i)>this._physicalSize&&this._physicalSize>0){i-=this._scrollOffset;let t=Math.round(i/this._physicalAverage);this._virtualStart+=t,this._physicalStart+=t,this._physicalTop=Math.min(Math.floor(this._virtualStart)*this._physicalAverage,this._scrollPosition),this._update()}else if(this._physicalCount>0){let t=this._getReusables(e);e?(this._physicalTop=t.physicalTop,this._virtualStart+=t.indexes.length,this._physicalStart+=t.indexes.length):(this._virtualStart-=t.indexes.length,this._physicalStart-=t.indexes.length),this._update(t.indexes,e?null:t.indexes),this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,0),z)}},_getReusables(s){let i,e,t,r=[],o=this._hiddenContentSize*this._ratio,n=this._virtualStart,a=this._virtualEnd,l=this._physicalCount,d=this._physicalTop+this._scrollOffset,h=this._physicalBottom+this._scrollOffset,c=this._scrollPosition,f=this._scrollBottom;for(s?(i=this._physicalStart,e=c-d):(i=this._physicalEnd,e=h-f);t=this._getPhysicalSizeIncrement(i),e-=t,!(r.length>=l||e<=o);)if(s){if(a+r.length+1>=this._virtualCount||d+t>=c-this._scrollOffset)break;r.push(i),d+=t,i=(i+1)%l}else{if(n-r.length<=0||d+this._physicalSize-t<=f)break;r.push(i),d-=t,i=i===0?l-1:i-1}return{indexes:r,physicalTop:d-this._scrollOffset}},_update(s,i){if(!(s&&s.length===0||this._physicalCount===0)){if(this._assignModels(s),this._updateMetrics(s),i)for(;i.length;){let e=i.pop();this._physicalTop-=this._getPhysicalSizeIncrement(e)}this._positionItems(),this._updateScrollerSize()}},_isClientFull(){return this._scrollBottom!==0&&this._physicalBottom-1>=this._scrollBottom&&this._physicalTop<=this._scrollPosition},_increasePoolIfNeeded(s){let e=this._clamp(this._physicalCount+s,qo,this._virtualCount-this._virtualStart)-this._physicalCount,t=Math.round(this._physicalCount*.5);if(!(e<0)){if(e>0){let r=window.performance.now();[].push.apply(this._physicalItems,this._createPool(e));for(let o=0;o<e;o++)this._physicalSizes.push(0);this._physicalCount+=e,this._physicalStart>this._physicalEnd&&this._isIndexRendered(this._focusedVirtualIndex)&&this._getPhysicalIndex(this._focusedVirtualIndex)<this._physicalEnd&&(this._physicalStart+=e),this._update(),this._templateCost=(window.performance.now()-r)/e,t=Math.round(this._physicalCount*.5)}this._virtualEnd>=this._virtualCount-1||t===0||(this._isClientFull()?this._physicalSize<this._optPhysicalSize&&this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,this._clamp(Math.round(50/this._templateCost),1,t)),Ut):this._debounce("_increasePoolIfNeeded",this._increasePoolIfNeeded.bind(this,t),z))}},_render(){if(!(!this.isAttached||!this._isVisible))if(this._physicalCount!==0){let s=this._getReusables(!0);this._physicalTop=s.physicalTop,this._virtualStart+=s.indexes.length,this._physicalStart+=s.indexes.length,this._update(s.indexes),this._update(),this._increasePoolIfNeeded(0)}else this._virtualCount>0&&(this.updateViewportBoundaries(),this._increasePoolIfNeeded(qo))},_itemsChanged(s){s.path==="items"&&(this._virtualStart=0,this._physicalTop=0,this._virtualCount=this.items?this.items.length:0,this._physicalIndexForKey={},this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._physicalItems||(this._physicalItems=[]),this._physicalSizes||(this._physicalSizes=[]),this._physicalStart=0,this._scrollTop>this._scrollOffset&&this._resetScrollPosition(0),this._debounce("_render",this._render,ie))},_iterateItems(s,i){let e,t,r,o;if(arguments.length===2&&i){for(o=0;o<i.length;o++)if(e=i[o],t=this._computeVidx(e),(r=s.call(this,e,t))!=null)return r}else{for(e=this._physicalStart,t=this._virtualStart;e<this._physicalCount;e++,t++)if((r=s.call(this,e,t))!=null)return r;for(e=0;e<this._physicalStart;e++,t++)if((r=s.call(this,e,t))!=null)return r}},_computeVidx(s){return s>=this._physicalStart?this._virtualStart+(s-this._physicalStart):this._virtualStart+(this._physicalCount-this._physicalStart)+s},_positionItems(){this._adjustScrollPosition();let s=this._physicalTop;this._iterateItems(i=>{this.translate3d(0,`${s}px`,0,this._physicalItems[i]),s+=this._physicalSizes[i]})},_getPhysicalSizeIncrement(s){return this._physicalSizes[s]},_adjustScrollPosition(){let s=this._virtualStart===0?this._physicalTop:Math.min(this._scrollPosition+this._physicalTop,0);if(s!==0){this._physicalTop-=s;let i=this._scrollPosition;!ld&&i>0&&this._resetScrollPosition(i-s)}},_resetScrollPosition(s){this.scrollTarget&&s>=0&&(this._scrollTop=s,this._scrollPosition=this._scrollTop)},_updateScrollerSize(s){let i=this._physicalBottom+Math.max(this._virtualCount-this._physicalCount-this._virtualStart,0)*this._physicalAverage;this._estScrollHeight=i,(s||this._scrollHeight===0||this._scrollPosition>=i-this._physicalSize||Math.abs(i-this._scrollHeight)>=this._viewportHeight)&&(this.$.items.style.height=`${i}px`,this._scrollHeight=i)},scrollToIndex(s){if(typeof s!="number"||s<0||s>this.items.length-1||(Re(),this._physicalCount===0))return;s=this._clamp(s,0,this._virtualCount-1),(!this._isIndexRendered(s)||s>=this._maxVirtualStart)&&(this._virtualStart=s-1),this._assignModels(),this._updateMetrics(),this._physicalTop=this._virtualStart*this._physicalAverage;let i=this._physicalStart,e=this._virtualStart,t=0,r=this._hiddenContentSize;for(;e<s&&t<=r;)t+=this._getPhysicalSizeIncrement(i),i=(i+1)%this._physicalCount,e+=1;this._updateScrollerSize(!0),this._positionItems(),this._resetScrollPosition(this._physicalTop+this._scrollOffset+t),this._increasePoolIfNeeded(0),this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null},_resetAverage(){this._physicalAverage=0,this._physicalAverageCount=0},_resizeHandler(){this._debounce("_render",()=>{this._firstVisibleIndexVal=null,this._lastVisibleIndexVal=null,this._isVisible?(this.updateViewportBoundaries(),this.toggleScrollListener(!0),this._resetAverage(),this._render()):this.toggleScrollListener(!1)},ie)},_isIndexRendered(s){return s>=this._virtualStart&&s<=this._virtualEnd},_getPhysicalIndex(s){return(this._physicalStart+(s-this._virtualStart))%this._physicalCount},_clamp(s,i,e){return Math.min(e,Math.max(i,s))},_debounce(s,i,e){this._debouncers||(this._debouncers={}),this._debouncers[s]=x.debounce(this._debouncers[s],e,i.bind(this)),jt(this._debouncers[s])}};var dd=1e5,Hr=1e3,Dt=class{constructor({createElements:i,updateElement:e,scrollTarget:t,scrollContainer:r,reorderElements:o,elementsContainer:n,__disableHeightPlaceholder:a}){this.isAttached=!0,this._vidxOffset=0,this.createElements=i,this.updateElement=e,this.scrollTarget=t,this.scrollContainer=r,this.reorderElements=o,this.elementsContainer=n||r,this.__disableHeightPlaceholder=a??!1,this._maxPages=1.3,this.__placeholderHeight=200,this.__elementHeightQueue=Array(10),this.timeouts={SCROLL_REORDER:500,PREVENT_OVERSCROLL:500,FIX_INVALID_ITEM_POSITIONING:100},this.__resizeObserver=new ResizeObserver(()=>this._resizeHandler()),getComputedStyle(this.scrollTarget).overflow==="visible"&&(this.scrollTarget.style.overflow="auto"),getComputedStyle(this.scrollContainer).position==="static"&&(this.scrollContainer.style.position="relative"),this.__resizeObserver.observe(this.scrollTarget),this.scrollTarget.addEventListener("scroll",()=>this._scrollHandler()),new ResizeObserver(([{contentRect:d}])=>{let h=d.width===0&&d.height===0;!h&&this.__scrollTargetHidden&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition),this.__scrollTargetHidden=h}).observe(this.scrollTarget),this.scrollTarget.addEventListener("virtualizer-element-focused",d=>this.__onElementFocused(d)),this.elementsContainer.addEventListener("focusin",()=>{this.scrollTarget.dispatchEvent(new CustomEvent("virtualizer-element-focused",{detail:{element:this.__getFocusedElement()}}))}),this.reorderElements&&(this.scrollTarget.addEventListener("mousedown",d=>{d.target===this.scrollTarget&&(this.__mouseDown=!0)}),this.scrollTarget.addEventListener("mouseup",()=>{this.__mouseDown=!1,this.__pendingReorder&&this.__reorderElements()}))}get scrollOffset(){return 0}get adjustedFirstVisibleIndex(){return this.firstVisibleIndex+this._vidxOffset}get adjustedLastVisibleIndex(){return this.lastVisibleIndex+this._vidxOffset}get _maxVirtualIndexOffset(){return this.size-this._virtualCount}__hasPlaceholders(){return this.__getVisibleElements().some(i=>i.__virtualizerPlaceholder)}scrollToIndex(i){if(typeof i!="number"||isNaN(i)||this.size===0||!this.scrollTarget.offsetHeight)return;delete this.__pendingScrollToIndex,this._physicalCount<=3&&this.flush(),i=this._clamp(i,0,this.size-1);let e=this.__getVisibleElements().length,t=Math.floor(i/this.size*this._virtualCount);this._virtualCount-t<e?(t=this._virtualCount-(this.size-i),this._vidxOffset=this._maxVirtualIndexOffset):t<e?i<Hr?(t=i,this._vidxOffset=0):(t=Hr,this._vidxOffset=i-t):this._vidxOffset=i-t,this.__skipNextVirtualIndexAdjust=!0,super.scrollToIndex(t),this.adjustedFirstVisibleIndex!==i&&this._scrollTop<this._maxScrollTop&&!this.grid&&(this._scrollTop-=this.__getIndexScrollOffset(i)||0),this._scrollHandler(),this.__hasPlaceholders()&&(this.__pendingScrollToIndex=i)}flush(){this.scrollTarget.offsetHeight!==0&&(this._resizeHandler(),Re(),this._scrollHandler(),this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.flush(),this.__scrollReorderDebouncer&&this.__scrollReorderDebouncer.flush(),this.__debouncerWheelAnimationFrame&&this.__debouncerWheelAnimationFrame.flush())}hostConnected(){this.scrollTarget.offsetParent&&this.scrollTarget.scrollTop!==this._scrollPosition&&(this.scrollTarget.scrollTop=this._scrollPosition)}update(i=0,e=this.size-1){let t=[];this.__getVisibleElements().forEach(r=>{r.__virtualIndex>=i&&r.__virtualIndex<=e&&(this.__updateElement(r,r.__virtualIndex,!0),t.push(r))}),this.__afterElementsUpdated(t)}_updateMetrics(i){Re();let e=0,t=0,r=this._physicalAverageCount,o=this._physicalAverage;this._iterateItems((n,a)=>{t+=this._physicalSizes[n];let l=this._physicalSizes[n];this._physicalSizes[n]=Math.ceil(this.__getBorderBoxHeight(this._physicalItems[n])),this._physicalSizes[n]!==l&&(this.__resizeObserver.unobserve(this._physicalItems[n]),this.__resizeObserver.observe(this._physicalItems[n],{box:"border-box"})),e+=this._physicalSizes[n],this._physicalAverageCount+=this._physicalSizes[n]?1:0},i),this._physicalSize=this._physicalSize+e-t,this._physicalAverageCount!==r&&(this._physicalAverage=Math.round((o*r+e)/this._physicalAverageCount))}__getBorderBoxHeight(i){let e=getComputedStyle(i),t=parseFloat(e.height)||0;if(e.boxSizing==="border-box")return t;let r=parseFloat(e.paddingBottom)||0,o=parseFloat(e.paddingTop)||0,n=parseFloat(e.borderBottomWidth)||0,a=parseFloat(e.borderTopWidth)||0;return t+r+o+n+a}__updateElement(i,e,t){i.__virtualizerPlaceholder&&(i.style.paddingTop="",i.style.opacity="",i.__virtualizerPlaceholder=!1),!this.__preventElementUpdates&&(i.__lastUpdatedIndex!==e||t)&&(this.updateElement(i,e),i.__lastUpdatedIndex=e)}__afterElementsUpdated(i){this.__disableHeightPlaceholder||i.forEach(e=>{let t=e.offsetHeight;if(t===0)e.style.paddingTop=`${this.__placeholderHeight}px`,e.style.opacity="0",e.__virtualizerPlaceholder=!0,this.__placeholderClearDebouncer=x.debounce(this.__placeholderClearDebouncer,ie,()=>this._resizeHandler());else{this.__elementHeightQueue.push(t),this.__elementHeightQueue.shift();let r=this.__elementHeightQueue.filter(o=>o!==void 0);this.__placeholderHeight=Math.round(r.reduce((o,n)=>o+n,0)/r.length)}}),this.__pendingScrollToIndex!==void 0&&!this.__hasPlaceholders()&&this.scrollToIndex(this.__pendingScrollToIndex)}__getIndexScrollOffset(i){let e=this.__getVisibleElements().find(t=>t.__virtualIndex===i);return e?this.scrollTarget.getBoundingClientRect().top-e.getBoundingClientRect().top:void 0}__restoreScrollOffset(i,e){let t=this.__getIndexScrollOffset(i);e!==void 0&&t!==void 0&&(this._scrollTop+=e-t)}get size(){return this.__size}set size(i){if(i===this.size)return;this.__fixInvalidItemPositioningDebouncer&&this.__fixInvalidItemPositioningDebouncer.cancel(),this._debouncers&&this._debouncers._increasePoolIfNeeded&&this._debouncers._increasePoolIfNeeded.cancel();let e=i>0&&this._scrollTop>0,t,r;e&&(t=this.adjustedFirstVisibleIndex,r=this.__getIndexScrollOffset(t)),this.__size=i,this.__preventElementUpdates=e,this._itemsChanged({path:"items"}),Re(),e&&(t=Math.min(t,i-1),this.scrollToIndex(t),this.__restoreScrollOffset(t,r)),this.__preventElementUpdates=!1,this._isVisible||this._assignModels(),this.elementsContainer.children.length||requestAnimationFrame(()=>this._resizeHandler()),this._updateScrollerSize(!0),this._resizeHandler(),Re(),this._debounce("_update",this._update,z)}get _scrollTop(){return this.scrollTarget.scrollTop}set _scrollTop(i){this.scrollTarget.scrollTop=i}get items(){return{length:Math.min(this.size,dd)}}get offsetHeight(){return this.scrollTarget.offsetHeight}get $(){return{items:this.scrollContainer}}updateViewportBoundaries(){let i=window.getComputedStyle(this.scrollTarget);this._scrollerPaddingTop=this.scrollTarget===this?0:parseInt(i["padding-top"],10),this._isRTL=i.direction==="rtl",this._viewportWidth=this.elementsContainer.offsetWidth,this._viewportHeight=this.scrollTarget.offsetHeight}setAttribute(){}_createPool(i){let e=this.createElements(i),t=document.createDocumentFragment();return e.forEach(r=>{r.style.position="absolute",t.appendChild(r),this.__resizeObserver.observe(r,{box:"border-box"})}),this.elementsContainer.appendChild(t),e}_assignModels(i){let e=[];this._iterateItems((t,r)=>{let o=this._physicalItems[t];o.hidden=r>=this.size,o.hidden?delete o.__lastUpdatedIndex:(o.__virtualIndex=r+(this._vidxOffset||0),this.__updateElement(o,o.__virtualIndex),e.push(o))},i),this.__afterElementsUpdated(e)}_isClientFull(){return setTimeout(()=>{this.__clientFull=!0}),this.__clientFull||super._isClientFull()}translate3d(i,e,t,r){r.style.transform=`translateY(${e})`}toggleScrollListener(){}__getFocusedElement(i=this.__getVisibleElements()){let e=document.activeElement;for(;e?.shadowRoot?.activeElement;)e=e.shadowRoot.activeElement;for(;e&&!i.includes(e);)e=e.assignedSlot||e.parentNode||e.host;return e}__nextFocusableSiblingMissing(i,e){return e.indexOf(i)===e.length-1&&this.size>i.__virtualIndex+1}__previousFocusableSiblingMissing(i,e){return e.indexOf(i)===0&&i.__virtualIndex>0}__onElementFocused(i){if(!this.reorderElements)return;let e=i.detail.element;if(!e)return;let t=this.__getVisibleElements();(this.__previousFocusableSiblingMissing(e,t)||this.__nextFocusableSiblingMissing(e,t))&&this.flush();let r=this.__getVisibleElements();this.__nextFocusableSiblingMissing(e,r)?(this._scrollTop+=Math.ceil(e.getBoundingClientRect().bottom)-Math.floor(this.scrollTarget.getBoundingClientRect().bottom-1),this.flush()):this.__previousFocusableSiblingMissing(e,r)&&(this._scrollTop-=Math.ceil(this.scrollTarget.getBoundingClientRect().top+1)-Math.floor(e.getBoundingClientRect().top),this.flush())}_scrollHandler(){if(this.scrollTarget.offsetHeight===0)return;this._adjustVirtualIndexOffset(this._scrollTop-this._scrollPosition);let i=this._scrollTop-this._scrollPosition;if(super._scrollHandler(),this._physicalCount!==0){let e=i>=0,t=this._getReusables(!e);t.indexes.length&&(this._physicalTop=t.physicalTop,e?(this._virtualStart-=t.indexes.length,this._physicalStart-=t.indexes.length):(this._virtualStart+=t.indexes.length,this._physicalStart+=t.indexes.length),this._resizeHandler())}i&&(this.__fixInvalidItemPositioningDebouncer=x.debounce(this.__fixInvalidItemPositioningDebouncer,R.after(this.timeouts.FIX_INVALID_ITEM_POSITIONING),()=>this.__fixInvalidItemPositioning()),this.__overscrollDebouncer?.isActive()||(this.scrollTarget.style.overscrollBehavior="none"),this.__overscrollDebouncer=x.debounce(this.__overscrollDebouncer,R.after(this.timeouts.PREVENT_OVERSCROLL),()=>{this.scrollTarget.style.overscrollBehavior=null})),this.reorderElements&&(this.__scrollReorderDebouncer=x.debounce(this.__scrollReorderDebouncer,R.after(this.timeouts.SCROLL_REORDER),()=>this.__reorderElements())),this._scrollPosition===0&&this.firstVisibleIndex!==0&&Math.abs(i)>0&&this.scrollToIndex(0)}_resizeHandler(){super._resizeHandler();let i=this.adjustedLastVisibleIndex===this.size-1,e=this._physicalTop-this._scrollPosition;if(i&&e>0){let t=Math.ceil(e/this._physicalAverage);this._virtualStart=Math.max(0,this._virtualStart-t),this._physicalStart=Math.max(0,this._physicalStart-t),super.scrollToIndex(this._virtualCount-1),this.scrollTarget.scrollTop=this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight}}__fixInvalidItemPositioning(){if(!this.scrollTarget.isConnected)return;let i=this._physicalTop>this._scrollTop,e=this._physicalBottom<this._scrollBottom,t=this.adjustedFirstVisibleIndex===0,r=this.adjustedLastVisibleIndex===this.size-1;if(i&&!t||e&&!r){let o=e,n=this._ratio;this._ratio=0,this._scrollPosition=this._scrollTop+(o?-1:1),this._scrollHandler(),this._ratio=n}}_increasePoolIfNeeded(i){if(this._physicalCount>2&&this._physicalAverage>0&&i>0){let t=Math.ceil(this._optPhysicalSize/this._physicalAverage)-this._physicalCount;super._increasePoolIfNeeded(Math.max(i,Math.min(100,t)))}else super._increasePoolIfNeeded(i)}get _optPhysicalSize(){let i=super._optPhysicalSize;return i<=0||this.__hasPlaceholders()?i:i+this.__getItemHeightBuffer()}__getItemHeightBuffer(){if(this._physicalCount===0)return 0;let i=Math.ceil(this._viewportHeight*(this._maxPages-1)/2),e=Math.max(...this._physicalSizes);return e>Math.min(...this._physicalSizes)?Math.max(0,e-i):0}__getVisibleElements(){return Array.from(this.elementsContainer.children).filter(i=>!i.hidden)}__reorderElements(){if(this.__mouseDown){this.__pendingReorder=!0;return}this.__pendingReorder=!1;let i=this._virtualStart+(this._vidxOffset||0),e=this.__getVisibleElements(),t=this.__getFocusedElement(e)||e[0];if(!t)return;let r=t.__virtualIndex-i,o=e.indexOf(t)-r;if(o>0)for(let n=0;n<o;n++)this.elementsContainer.appendChild(e[n]);else if(o<0)for(let n=e.length+o;n<e.length;n++)this.elementsContainer.insertBefore(e[n],e[0]);if(it){let{transform:n}=this.scrollTarget.style;this.scrollTarget.style.transform="translateZ(0)",setTimeout(()=>{this.scrollTarget.style.transform=n})}}_adjustVirtualIndexOffset(i){let e=this._maxVirtualIndexOffset;if(this._virtualCount>=this.size)this._vidxOffset=0;else if(this.__skipNextVirtualIndexAdjust)this.__skipNextVirtualIndexAdjust=!1;else if(Math.abs(i)>1e4){let t=this._scrollTop/(this.scrollTarget.scrollHeight-this.scrollTarget.clientHeight);this._vidxOffset=Math.round(t*e)}else{let t=this._vidxOffset,r=Hr,o=100,n,a,l=()=>{n=this.adjustedFirstVisibleIndex,a=this.__getIndexScrollOffset(n)};this._scrollTop===0?t!==0&&(l(),this._vidxOffset=0,super.scrollToIndex(0)):this.firstVisibleIndex<r&&this._vidxOffset>0&&(l(),this._vidxOffset-=Math.min(this._vidxOffset,o),super.scrollToIndex(this.firstVisibleIndex+(t-this._vidxOffset))),this._scrollTop>=this._maxScrollTop&&this._maxScrollTop>0?t!==e&&(l(),this._vidxOffset=e,super.scrollToIndex(this._virtualCount-1)):this.firstVisibleIndex>this._virtualCount-r&&this._vidxOffset<e&&(l(),this._vidxOffset+=Math.min(e-this._vidxOffset,o),super.scrollToIndex(this.firstVisibleIndex-(this._vidxOffset-t))),n!==void 0&&this.__restoreScrollOffset(n,a)}}};Object.setPrototypeOf(Dt.prototype,Go);var ot=class{constructor(i){this.__adapter=new Dt(i)}get firstVisibleIndex(){return this.__adapter.adjustedFirstVisibleIndex}get lastVisibleIndex(){return this.__adapter.adjustedLastVisibleIndex}get size(){return this.__adapter.size}set size(i){this.__adapter.size=i}scrollToIndex(i){this.__adapter.scrollToIndex(i)}update(i=0,e=this.size-1){this.__adapter.update(i,e)}flush(){this.__adapter.flush()}hostConnected(){this.__adapter.hostConnected()}};var L=class{toString(){return""}};var nt=s=>class extends s{static get properties(){return{items:{type:Array,sync:!0,observer:"__itemsChanged"},focusedIndex:{type:Number,sync:!0,observer:"__focusedIndexChanged"},loading:{type:Boolean,sync:!0,observer:"__loadingChanged"},opened:{type:Boolean,sync:!0,observer:"__openedChanged"},selectedItem:{type:Object,sync:!0,observer:"__selectedItemChanged"},itemClassNameGenerator:{type:Object,observer:"__itemClassNameGeneratorChanged"},itemIdPath:{type:String},owner:{type:Object},getItemLabel:{type:Object},renderer:{type:Object,sync:!0,observer:"__rendererChanged"},theme:{type:String}}}constructor(){super(),this.__boundOnItemClick=this.__onItemClick.bind(this)}get _viewportTotalPaddingBottom(){if(this._cachedViewportTotalPaddingBottom===void 0){let e=window.getComputedStyle(this.$.selector);this._cachedViewportTotalPaddingBottom=[e.paddingBottom,e.borderBottomWidth].map(t=>parseInt(t,10)).reduce((t,r)=>t+r)}return this._cachedViewportTotalPaddingBottom}ready(){super.ready(),this.setAttribute("role","listbox"),this.id=`${this.localName}-${ye()}`,this.__hostTagName=this.constructor.is.replace("-scroller",""),this.addEventListener("click",e=>e.stopPropagation()),this.__patchWheelOverScrolling()}requestContentUpdate(){this.__virtualizer&&(this.items&&(this.__virtualizer.size=this.items.length),this.opened&&this.__virtualizer.update())}scrollIntoView(e,t=!1){if(!this.__virtualizer||!(this.opened&&e>=0))return;let r=[...this.children].find(h=>!h.hidden&&h.index===e);if(!t&&r){let h=r.getBoundingClientRect(),c=this.getBoundingClientRect();if(h.top>=c.top&&h.bottom+this._viewportTotalPaddingBottom<=c.bottom)return}let o=e;if(!t){let h=this._visibleItemsCount();e>this.__virtualizer.lastVisibleIndex-1?(this.__virtualizer.scrollToIndex(e),o=e-h+1):e>this.__virtualizer.firstVisibleIndex&&(o=this.__virtualizer.firstVisibleIndex)}this.__virtualizer.scrollToIndex(Math.max(0,o)),this.__virtualizer.flush();let n=[...this.children].find(h=>!h.hidden&&h.index===e);if(!n)return;if(t){n.scrollIntoView({block:"center"});return}let a=n.getBoundingClientRect(),l=this.getBoundingClientRect(),d=a.bottom+this._viewportTotalPaddingBottom;d>l.bottom?this.scrollTop+=d-l.bottom:a.top<l.top&&(this.scrollTop-=l.top-a.top)}_isItemSelected(e,t,r){return e instanceof L?!1:r&&e!==void 0&&t!==void 0?G(r,e)===G(r,t):e===t}__initVirtualizer(){this.__virtualizer=new ot({createElements:this.__createElements.bind(this),updateElement:this._updateElement.bind(this),elementsContainer:this,scrollTarget:this,scrollContainer:this.$.selector,reorderElements:!0,__disableHeightPlaceholder:!0})}__itemsChanged(e){e&&this.__virtualizer&&this.requestContentUpdate()}__loadingChanged(){this.requestContentUpdate()}__openedChanged(e){if(e){this.__virtualizer||this.__initVirtualizer(),this.requestContentUpdate();return}let t=this.__virtualizer&&this.__virtualizer.__adapter;t&&t._scrollPosition>0&&(this.scrollTop=0,t._scrollPosition=0)}__selectedItemChanged(){this.requestContentUpdate()}__itemClassNameGeneratorChanged(e,t){(e||t)&&this.requestContentUpdate()}__focusedIndexChanged(e,t){e!==t&&this.requestContentUpdate(),e>=0&&!this.loading&&this.scrollIntoView(e)}__rendererChanged(e,t){(e||t)&&this.requestContentUpdate()}__createElements(e){return[...Array(e)].map(()=>{let t=document.createElement(`${this.__hostTagName}-item`);return t.addEventListener("click",this.__boundOnItemClick),t.tabIndex="-1",t.style.width="100%",t})}_updateElement(e,t){let r=this.items[t],o=this.focusedIndex,n=this._isItemSelected(r,this.selectedItem,this.itemIdPath);e.setProperties({item:r,index:t,label:this.getItemLabel(r),selected:n,renderer:this.renderer,focused:!this.loading&&o===t}),typeof this.itemClassNameGenerator=="function"?e.className=this.itemClassNameGenerator(r):e.className!==""&&(e.className=""),e.id=`${this.__hostTagName}-item-${t}`,e.setAttribute("role",t!==void 0?"option":!1),e.setAttribute("aria-selected",n.toString()),e.setAttribute("aria-posinset",t+1),e.setAttribute("aria-setsize",this.items.length),this.theme?e.setAttribute("theme",this.theme):e.removeAttribute("theme"),r instanceof L&&this.__requestItemByIndex(t)}__onItemClick(e){this.dispatchEvent(new CustomEvent("selection-changed",{detail:{item:e.currentTarget.item}}))}__patchWheelOverScrolling(){this.$.selector.addEventListener("wheel",e=>{let t=this.scrollTop===0,r=this.scrollHeight-this.scrollTop-this.clientHeight<=1;(t&&e.deltaY<0||r&&e.deltaY>0)&&e.preventDefault()})}__requestItemByIndex(e){requestAnimationFrame(()=>{this.dispatchEvent(new CustomEvent("index-requested",{detail:{index:e}}))})}_visibleItemsCount(){return this.__virtualizer.scrollToIndex(this.__virtualizer.firstVisibleIndex),this.__virtualizer.size>0?this.__virtualizer.lastVisibleIndex-this.__virtualizer.firstVisibleIndex+1:0}};var Ur=class extends nt(g(_)){static get is(){return"vaadin-combo-box-scroller"}static get styles(){return Ve}render(){return u`
      <div id="selector">
        <slot></slot>
      </div>
    `}};m(Ur);var k=s=>s??M;var Ko=s=>class extends Ae(U(s)){static get properties(){return{clearButtonVisible:{type:Boolean,reflectToAttribute:!0,value:!1}}}get clearElement(){return console.warn(`Please implement the 'clearElement' property in <${this.localName}>`),null}ready(){super.ready(),this.clearElement&&(this.clearElement.addEventListener("mousedown",e=>this._onClearButtonMouseDown(e)),this.clearElement.addEventListener("click",e=>this._onClearButtonClick(e)))}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onClearButtonMouseDown(e){this._shouldKeepFocusOnClearMousedown()&&e.preventDefault(),K||this.inputElement.focus()}_onEscape(e){super._onEscape(e),this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onClearAction(){this._inputElementValue="",this.inputElement.dispatchEvent(new Event("input",{bubbles:!0,composed:!0})),this.inputElement.dispatchEvent(new Event("change",{bubbles:!0}))}_shouldKeepFocusOnClearMousedown(){return Ce(this.inputElement)}};var hd=s=>class extends Qe(Je(Ae(s))){static get constraints(){return["required"]}static get delegateAttrs(){return[...super.delegateAttrs,"required"]}ready(){super.ready(),this._createConstraintsObserver()}checkValidity(){return this.inputElement&&this._hasValidConstraints(this.constructor.constraints.map(e=>this[e]))?this.inputElement.checkValidity():!this.invalid}_hasValidConstraints(e){return e.some(t=>this.__isValidConstraint(t))}_createConstraintsObserver(){this._createMethodObserver(`_constraintsChanged(stateTarget, ${this.constructor.constraints.join(", ")})`)}_constraintsChanged(e,...t){if(!e)return;let r=this._hasValidConstraints(t),o=this.__previousHasConstraints&&!r;(this._hasValue||this.invalid)&&r?this._requestValidation():o&&!this.manualValidation&&this._setInvalid(!1),this.__previousHasConstraints=r}_onChange(e){e.stopPropagation(),this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{detail:{sourceEvent:e},bubbles:e.bubbles,cancelable:e.cancelable}))}__isValidConstraint(e){return!!e||e===0}},at=P(hd);var de=s=>class extends ti(we(at(Ie(Ko(U(s)))))){static get properties(){return{allowedCharPattern:{type:String,observer:"_allowedCharPatternChanged"},autoselect:{type:Boolean,value:!1},name:{type:String,reflectToAttribute:!0},placeholder:{type:String,reflectToAttribute:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},title:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"name","type","placeholder","readonly","invalid","title"]}constructor(){super(),this._boundOnPaste=this._onPaste.bind(this),this._boundOnDrop=this._onDrop.bind(this),this._boundOnBeforeInput=this._onBeforeInput.bind(this)}get slotStyles(){let e=this.localName;return[`
          /* Needed for Safari, where ::slotted(...)::placeholder does not work */
          ${e} > :is(input[slot='input'], textarea[slot='textarea'])::placeholder {
            font: inherit;
            color: inherit;
          }

          /* Override built-in autofill styles */
          ${e} > input[slot='input']:autofill {
            -webkit-text-fill-color: var(--vaadin-input-field-autofill-color, black) !important;
            background-clip: text !important;
          }

          ${e}:has(> input[slot='input']:autofill)::part(input-field) {
            --vaadin-input-field-background: var(--vaadin-input-field-autofill-background, lightyellow) !important;
            --vaadin-input-field-value-color: var(--vaadin-input-field-autofill-color, black) !important;
            --vaadin-input-field-button-text-color: var(--vaadin-input-field-autofill-color, black) !important;
          }
        `]}_onFocus(e){super._onFocus(e),this.autoselect&&this.inputElement&&this.inputElement.select()}_addInputListeners(e){super._addInputListeners(e),e.addEventListener("paste",this._boundOnPaste),e.addEventListener("drop",this._boundOnDrop),e.addEventListener("beforeinput",this._boundOnBeforeInput)}_removeInputListeners(e){super._removeInputListeners(e),e.removeEventListener("paste",this._boundOnPaste),e.removeEventListener("drop",this._boundOnDrop),e.removeEventListener("beforeinput",this._boundOnBeforeInput)}_onKeyDown(e){super._onKeyDown(e),this.allowedCharPattern&&!this.__shouldAcceptKey(e)&&e.target===this.inputElement&&(e.preventDefault(),this._markInputPrevented())}_markInputPrevented(){this.setAttribute("input-prevented",""),this._preventInputDebouncer=x.debounce(this._preventInputDebouncer,R.after(200),()=>{this.removeAttribute("input-prevented")})}__shouldAcceptKey(e){return e.metaKey||e.ctrlKey||!e.key||e.key.length!==1||this.__allowedCharRegExp.test(e.key)}_onPaste(e){if(this.allowedCharPattern){let t=e.clipboardData.getData("text");this.__allowedTextRegExp.test(t)||(e.preventDefault(),this._markInputPrevented())}}_onDrop(e){if(this.allowedCharPattern){let t=e.dataTransfer.getData("text");this.__allowedTextRegExp.test(t)||(e.preventDefault(),this._markInputPrevented())}}_onBeforeInput(e){this.allowedCharPattern&&e.data&&!this.__allowedTextRegExp.test(e.data)&&(e.preventDefault(),this._markInputPrevented())}_allowedCharPatternChanged(e){if(e)try{this.__allowedCharRegExp=new RegExp(`^${e}$`,"u"),this.__allowedTextRegExp=new RegExp(`^${e}*$`,"u")}catch(t){console.error(t)}}};var pi=s=>class extends at(s){static get properties(){return{pattern:{type:String}}}static get delegateAttrs(){return[...super.delegateAttrs,"pattern"]}static get constraints(){return[...super.constraints,"pattern"]}};var Yo=p`
  [part$='button'] {
    color: var(--vaadin-input-field-button-text-color, var(--vaadin-text-color-secondary));
    cursor: var(--vaadin-clickable-cursor);
    touch-action: manipulation;
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    /* Ensure minimum click target (WCAG) */
    padding: max(0px, (24px - var(--vaadin-icon-size, 1lh)) / 2);
    margin: min(0px, (24px - var(--vaadin-icon-size, 1lh)) / -2);
  }

  /* Icon */
  [part$='button']::before {
    background: currentColor;
    content: '';
    display: block;
    height: var(--vaadin-icon-size, 1lh);
    width: var(--vaadin-icon-size, 1lh);
    mask-size: var(--vaadin-icon-visual-size, 100%);
    mask-position: 50%;
    mask-repeat: no-repeat;
  }

  :host(:is(:not([clear-button-visible][has-value]), [disabled], [readonly])) [part~='clear-button'] {
    display: none;
  }

  [part~='clear-button']::before {
    mask-image: var(--_vaadin-icon-cross);
  }

  :host(:is([readonly], [disabled])) [part$='button'] {
    color: var(--vaadin-text-color-disabled);
    cursor: var(--vaadin-disabled-cursor);
  }

  @media (forced-colors: active) {
    [part$='button']::before {
      background: CanvasText;
    }

    :host([disabled]) [part$='button'] {
      color: GrayText;
    }

    :host([disabled]) [part$='button']::before {
      background: GrayText;
    }
  }
`;var N=[ei,Yo];var _i=p`
  :host([opened]) {
    pointer-events: auto;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-chevron-down);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }
`;var mi=class s{context;items=[];pendingRequests={};#e={};#r;#i=0;#t=0;constructor(i,e,t,r,o){this.context=i,this.pageSize=e,this.size=t,this.parentCache=r,this.parentCacheIndex=o,this.#t=t||0}get parentItem(){return this.parentCache&&this.parentCache.items[this.parentCacheIndex]}get subCaches(){return Object.values(this.#e)}get isLoading(){return Object.keys(this.pendingRequests).length>0?!0:this.subCaches.some(i=>i.isLoading)}get flatSize(){return this.#t}get pageSize(){return this.#r}set pageSize(i){this.#r=i,this.pendingRequests={},this.subCaches.forEach(e=>{e.pageSize=i})}get size(){return this.#i}set size(i){if(this.#i!==i){if(this.#i=i,this.context.placeholder!==void 0){this.items.length=i||0;for(let t=0;t<i;t++)this.items[t]||=this.context.placeholder}this.items.length>i&&(this.items.length=i||0),Object.keys(this.pendingRequests).forEach(t=>{parseInt(t)*this.pageSize>=this.size&&delete this.pendingRequests[t]})}}recalculateFlatSize(){this.#t=!this.parentItem||this.context.isExpanded(this.parentItem)?this.size+this.subCaches.reduce((i,e)=>(e.recalculateFlatSize(),i+e.flatSize),0):0}setPage(i,e){let t=i*this.pageSize;e.forEach((r,o)=>{let n=t+o;(this.size===void 0||n<this.size)&&(this.items[n]=r)})}getSubCache(i){return this.#e[i]}removeSubCache(i){delete this.#e[i]}removeSubCaches(){this.#e={}}createSubCache(i){let e=new s(this.context,this.pageSize,0,this,i);return this.#e[i]=e,e}getFlatIndex(i){let e=Math.max(0,Math.min(this.size-1,i));return this.subCaches.reduce((t,r)=>{let o=r.parentCacheIndex;return e>o?t+r.flatSize:t},e)}};function jr(s,i,e=0){let t=i;for(let r of s.subCaches){let o=r.parentCacheIndex;if(t<=o)break;if(t<=o+r.flatSize)return jr(r,t-o-1,e+1);t-=r.flatSize}return{cache:s,item:s.items[t],index:t,page:Math.floor(t/s.pageSize),level:e}}function Wr({getItemId:s},i,e,t=0,r=0){for(let o=0;o<i.items.length;o++){let n=i.items[o];if(n&&s(n)===s(e))return{cache:i,level:t,item:n,index:o,page:Math.floor(o/i.pageSize),subCache:i.getSubCache(o),flatIndex:r+i.getFlatIndex(o)}}for(let o of i.subCaches){let n=r+i.getFlatIndex(o.parentCacheIndex),a=Wr({getItemId:s},o,e,t+1,n+1);if(a)return a}}function qr(s,[i,...e],t=0){i===1/0&&(i=s.size-1);let r=s.getFlatIndex(i),o=s.getSubCache(i);return o?.flatSize>0&&e.length?qr(o,e,t+r+1):t+r}var lt=class extends EventTarget{host;dataProvider;dataProviderParams;isExpanded;getItemId;rootCache;placeholder;isPlaceholder;constructor(i,{size:e,pageSize:t,isExpanded:r,getItemId:o,isPlaceholder:n,placeholder:a,dataProvider:l,dataProviderParams:d}){super(),this.host=i,this.getItemId=o,this.isExpanded=r,this.placeholder=a,this.isPlaceholder=n,this.dataProvider=l,this.dataProviderParams=d,this.rootCache=this.#r(t,e)}get flatSize(){return this.rootCache.flatSize}get pageSize(){return this.rootCache.pageSize}get#e(){return{isExpanded:this.isExpanded,placeholder:this.placeholder}}isLoading(){return this.rootCache.isLoading}setPageSize(i){this.rootCache.pageSize=i}setDataProvider(i){this.dataProvider=i}recalculateFlatSize(){this.rootCache.recalculateFlatSize()}clearCache(){this.rootCache=this.#r(this.rootCache.pageSize,this.rootCache.size)}getFlatIndexContext(i){return jr(this.rootCache,i)}getItemContext(i){return Wr({getItemId:this.getItemId},this.rootCache,i)}getFlatIndexByPath(i){return qr(this.rootCache,i)}ensureFlatIndexLoaded(i){let{cache:e,page:t,item:r}=this.getFlatIndexContext(i);this.#t(r)||this.#i(e,t)}ensureFlatIndexHierarchy(i){let{cache:e,item:t,index:r}=this.getFlatIndexContext(i);if(this.#t(t)&&this.isExpanded(t)&&!e.getSubCache(r)){let o=e.createSubCache(r);this.#i(o,0)}}loadFirstPage(){this.#i(this.rootCache,0)}_shouldLoadCachePage(i,e){return!0}#r(i,e){return new mi(this.#e,i,e)}#i(i,e){if(!this.dataProvider||i.pendingRequests[e]||!this._shouldLoadCachePage(i,e))return;let t={page:e,pageSize:i.pageSize,parentItem:i.parentItem};this.dataProviderParams&&(t={...t,...this.dataProviderParams()});let r=(o,n)=>{i.pendingRequests[e]===r&&(n!==void 0?i.size=n:t.parentItem&&(i.size=o.length),i.setPage(e,o),this.recalculateFlatSize(),this.dispatchEvent(new CustomEvent("page-received")),delete i.pendingRequests[e],this.dispatchEvent(new CustomEvent("page-loaded")))};i.pendingRequests[e]=r,this.dispatchEvent(new CustomEvent("page-requested")),this.dataProvider(t,r)}#t(i){return this.isPlaceholder?!this.isPlaceholder(i):this.placeholder?i!==this.placeholder:!!i}};var fi=s=>class extends s{static get properties(){return{pageSize:{type:Number,value:50,observer:"_pageSizeChanged",sync:!0},size:{type:Number,observer:"_sizeChanged",sync:!0},dataProvider:{type:Object,observer:"_dataProviderChanged",sync:!0}}}static get observers(){return["_dataProviderFilterChanged(filter)","_ensureFirstPage(opened)"]}constructor(){super(),this.__dataProviderInitialized=!1,this.__previousDataProviderFilter,this.__dataProviderController=new lt(this,{placeholder:new L,isPlaceholder:e=>e instanceof L,dataProviderParams:()=>({filter:this.filter})}),this.__dataProviderController.addEventListener("page-requested",this.__onDataProviderPageRequested.bind(this)),this.__dataProviderController.addEventListener("page-loaded",this.__onDataProviderPageLoaded.bind(this))}ready(){super.ready(),this._scroller.addEventListener("index-requested",e=>{if(!this._shouldFetchData())return;let t=e.detail.index;t!==void 0&&this.__dataProviderController.ensureFlatIndexLoaded(t)}),this.__dataProviderInitialized=!0,this.dataProvider&&this.__synchronizeControllerState()}_dataProviderFilterChanged(e){if(this.__previousDataProviderFilter===void 0&&e===""){this.__previousDataProviderFilter=e;return}this.__previousDataProviderFilter!==e&&(this.__previousDataProviderFilter=e,this.__keepOverlayOpened=!0,this.size=void 0,this.clearCache(),this.__keepOverlayOpened=!1)}_shouldFetchData(){return this.dataProvider?this.opened||this.filter&&this.filter.length:!1}_ensureFirstPage(e){!this._shouldFetchData()||!e||(this._forceNextRequest||this.size===void 0?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this.size>0&&this.__dataProviderController.ensureFlatIndexLoaded(0))}__onDataProviderPageRequested(){this.loading=!0}__onDataProviderPageLoaded(){let{rootCache:e}=this.__dataProviderController;e.items=[...e.items],this.__synchronizeControllerState(),!this.opened&&!this._isInputFocused()&&this._commitValue()}clearCache(){this.dataProvider&&(this.__dataProviderController.clearCache(),this.__synchronizeControllerState(),this._shouldFetchData()?(this._forceNextRequest=!1,this.__dataProviderController.loadFirstPage()):this._forceNextRequest=!0)}_sizeChanged(e){let{rootCache:t}=this.__dataProviderController;t.size!==e&&(t.size=e,t.items=[...t.items],this.__synchronizeControllerState())}_filteredItemsChanged(e){if(super._filteredItemsChanged(e),this.dataProvider&&e){let{rootCache:t}=this.__dataProviderController;t.items!==e&&(t.items=e,this.__synchronizeControllerState())}}__synchronizeControllerState(){if(this.__dataProviderInitialized&&this.dataProvider){let{rootCache:e}=this.__dataProviderController;this.size=e.size,this.filteredItems=e.items,this.loading=this.__dataProviderController.isLoading()}}_pageSizeChanged(e,t){if(Math.floor(e)!==e||e<1)throw this.pageSize=t,new Error("`pageSize` value must be an integer > 0");this.__dataProviderController.setPageSize(e),this.clearCache()}_dataProviderChanged(e,t){this._ensureItemsOrDataProvider(()=>{this.dataProvider=t}),this.__dataProviderController.setDataProvider(e),this.clearCache()}_ensureItemsOrDataProvider(e){if(this.items!==void 0&&this.dataProvider!==void 0)throw e(),new Error("Using `items` and `dataProvider` together is not supported")}};var gi=s=>class extends s{static get observers(){return["__clearPendingFocusOnFilter(filter)"]}__focusIndex(e){if(!(typeof e!="number"||Number.isNaN(e)||e<0)){if(!this._overlayOpened||!this._dropdownItems||this._dropdownItems.length===0){this.__pendingFocusIndex=e;return}if(!(e>=this._dropdownItems.length)){if(this._focusedIndex=e,this._scrollIntoView(e,!0),this.loading){this.__pendingFocusIndex=e;return}delete this.__pendingFocusIndex,requestAnimationFrame(()=>{this.isConnected&&this._updateActiveDescendant(e)})}}}__focusPendingIndexIfNeeded(){this.__pendingFocusIndex!==void 0&&!this.loading&&this.__focusIndex(this.__pendingFocusIndex)}__clearPendingFocusOnFilter(){delete this.__pendingFocusIndex}_onOpened(){super._onOpened(),this.__focusPendingIndexIfNeeded()}__onDataProviderPageLoaded(){super.__onDataProviderPageLoaded(),this.__focusPendingIndexIfNeeded()}};var vi=s=>class extends U(Ae(le(j(s)))){static get properties(){return{opened:{type:Boolean,notify:!0,value:!1,reflectToAttribute:!0,sync:!0,observer:"_openedChanged"},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},_focusedIndex:{type:Number,observer:"_focusedIndexChanged",value:-1,sync:!0},_toggleElement:{type:Object,observer:"_toggleElementChanged"},_dropdownItems:{type:Array,sync:!0},_overlayOpened:{type:Boolean,sync:!0,observer:"_overlayOpenedChanged"}}}constructor(){super(),this._scroller,this._closeOnBlurIsPrevented,this._boundOverlaySelectedItemChanged=this._overlaySelectedItemChanged.bind(this),this._boundOnClearButtonMouseDown=this.__onClearButtonMouseDown.bind(this),this._boundOnClick=this._onClick.bind(this),this._boundOnOverlayTouchAction=this._onOverlayTouchAction.bind(this),this._boundOnTouchend=this._onTouchend.bind(this)}get _tagNamePrefix(){return"vaadin-combo-box"}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.autocapitalize="off",e.setAttribute("role","combobox"),e.setAttribute("aria-autocomplete","list"),e.setAttribute("aria-expanded",!!this.opened),e.setAttribute("spellcheck","false"),e.setAttribute("autocorrect","off"))}firstUpdated(){super.firstUpdated(),this._initScroller()}ready(){super.ready(),this._initOverlay(),this.addEventListener("click",this._boundOnClick),this.addEventListener("touchend",this._boundOnTouchend),this.clearElement&&this.clearElement.addEventListener("mousedown",this._boundOnClearButtonMouseDown)}disconnectedCallback(){super.disconnectedCallback(),this.close()}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.opened=!1}_initOverlay(){let e=this.$.overlay;e.addEventListener("touchend",this._boundOnOverlayTouchAction),e.addEventListener("touchmove",this._boundOnOverlayTouchAction),e.addEventListener("mousedown",t=>t.preventDefault()),e.addEventListener("opened-changed",t=>{this._overlayOpened=t.detail.value}),e.addEventListener("vaadin-overlay-closed",()=>{this._scroller.items=[],this._onOverlayClosed()}),this._overlayElement=e}_initScroller(){let e=document.createElement(`${this._tagNamePrefix}-scroller`);e.owner=this,e.getItemLabel=this._getItemLabel.bind(this),e.addEventListener("selection-changed",this._boundOverlaySelectedItemChanged),this._renderScroller(e),this._scroller=e}_renderScroller(e){e.setAttribute("slot","overlay"),e.setAttribute("tabindex","-1"),this.appendChild(e)}get _hasDropdownItems(){return!!(this._dropdownItems&&this._dropdownItems.length)}_overlayOpenedChanged(e,t){e?this._onOpened():t&&this._hasDropdownItems&&this.close()}_focusedIndexChanged(e,t){t!==void 0&&this._updateActiveDescendant(e)}_isInputFocused(){return this.inputElement&&Ce(this.inputElement)}_updateActiveDescendant(e){let t=this.inputElement;if(!t)return;let r=this._getItemElements().find(o=>o.index===e);r?t.setAttribute("aria-activedescendant",r.id):t.removeAttribute("aria-activedescendant")}_openedChanged(e,t){if(t===void 0)return;e?!this._isInputFocused()&&!K&&this.inputElement&&this.inputElement.focus():(this.autoselect&&(this.__autoselectPending=!0),this._onClosed());let r=this.inputElement;r&&(r.setAttribute("aria-expanded",!!e),e?r.setAttribute("aria-controls",this._scroller.id):r.removeAttribute("aria-controls"))}_onOverlayTouchAction(){this._closeOnBlurIsPrevented=!0,this.inputElement.blur(),this._closeOnBlurIsPrevented=!1}_isClearButton(e){return e.composedPath()[0]===this.clearElement}__onClearButtonMouseDown(e){e.preventDefault(),this.inputElement.focus()}_onClearButtonClick(e){e.preventDefault(),this._onClearAction()}_onToggleButtonClick(e){e.preventDefault(),this.opened?this.close():this.open()}_onHostClick(e){this.autoOpenDisabled||(e.preventDefault(),this.open())}_onClick(e){this.autoselect&&this.inputElement&&this.__autoselectPending&&(this.inputElement.selectionStart!==this.inputElement.selectionEnd||this.inputElement.select()),this.__autoselectPending=!1,this._isClearButton(e)?this._onClearButtonClick(e):e.composedPath().includes(this._toggleElement)?this._onToggleButtonClick(e):this._onHostClick(e)}_onTouchend(e){!this.clearElement||e.composedPath()[0]!==this.clearElement||(e.preventDefault(),this._onClearAction())}_onKeyDown(e){super._onKeyDown(e),e.key==="ArrowDown"?(this._onArrowDown(),e.preventDefault()):e.key==="ArrowUp"&&(this._onArrowUp(),e.preventDefault())}_getItemLabel(e){return e?e.toString():""}_onArrowDown(){if(this.opened){let e=this._dropdownItems;e&&(this._focusedIndex=Math.min(e.length-1,this._focusedIndex+1),this._prefillFocusedItemLabel())}else this.open()}_onArrowUp(){if(this.opened){if(this._focusedIndex>-1)this._focusedIndex=Math.max(0,this._focusedIndex-1);else{let e=this._dropdownItems;e&&(this._focusedIndex=e.length-1)}this._prefillFocusedItemLabel()}else this.open()}_prefillFocusedItemLabel(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this._inputElementValue=this._getItemLabel(e),this._markAllSelectionRange()}}_setSelectionRange(e,t){this._isInputFocused()&&this.inputElement.setSelectionRange&&this.inputElement.setSelectionRange(e,t)}_markAllSelectionRange(){this._inputElementValue!==void 0&&this._setSelectionRange(0,this._inputElementValue.length)}_clearSelectionRange(){if(this._inputElementValue!==void 0){let e=this._inputElementValue?this._inputElementValue.length:0;this._setSelectionRange(e,e)}}_closeOrCommit(){this.opened?this.close():this._commitValue()}_onEnter(e){if(!this._hasValidInputValue()){e.preventDefault(),e.stopPropagation();return}this.opened&&(e.preventDefault(),e.stopPropagation()),this._closeOrCommit()}_hasValidInputValue(){return!0}_onEscape(e){this.autoOpenDisabled&&(this.opened||this.value!==this._inputElementValue&&this._inputElementValue.length>0)?(e.stopPropagation(),this._focusedIndex=-1,this._onEscapeCancel()):this.opened?(e.stopPropagation(),this._focusedIndex>-1?(this._focusedIndex=-1,this._revertInputValue()):this._onEscapeCancel()):this.clearButtonVisible&&this.value&&!this.readonly&&(e.stopPropagation(),this._onClearAction())}_onEscapeCancel(){}_toggleElementChanged(e){e&&(e.addEventListener("mousedown",t=>t.preventDefault()),e.addEventListener("click",()=>{K&&!this._isInputFocused()&&document.activeElement.blur()}))}_onClearAction(){}_onOpened(){}_onClosed(){}_onOverlayClosed(){}_commitValue(){}_revertInputValue(){this._inputElementValue=this.value,this._clearSelectionRange()}_onInput(e){!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(this.opened=!0)}_getItemElements(){return Array.from(this._scroller.querySelectorAll(`${this._tagNamePrefix}-item`))}_scrollIntoView(e,t=!1){this._scroller&&this._scroller.scrollIntoView(e,t)}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(e.detail.item instanceof L||this.opened&&(this._focusedIndex=this._dropdownItems.indexOf(e.detail.item),this.close()))}_setFocused(e){super._setFocused(e),e||(this.__autoselectPending=!1),!e&&!this.readonly&&!this._closeOnBlurIsPrevented&&this._handleFocusOut()}_handleFocusOut(){if(B()){this._closeOrCommit();return}this.opened?this._overlayOpened||this.close():this._commitValue()}_shouldRemoveFocus(e){return e.relatedTarget&&e.relatedTarget.localName===`${this._tagNamePrefix}-item`?!1:e.relatedTarget===this._overlayElement?(e.composedPath()[0].focus(),!1):!0}};function cd(s){return s!=null}function Qo(s,i){return s.findIndex(e=>e instanceof L?!1:i(e))}var bi=s=>class extends vi(s){static get properties(){return{items:{type:Array,sync:!0,observer:"_itemsChanged"},filteredItems:{type:Array,observer:"_filteredItemsChanged",sync:!0},filter:{type:String,value:"",notify:!0,sync:!0},itemLabelGenerator:{type:Object},itemLabelPath:{type:String,value:"label",observer:"_itemLabelPathChanged",sync:!0},itemValuePath:{type:String,value:"value",sync:!0}}}updated(e){super.updated(e),e.has("filter")&&this._filterChanged(this.filter),e.has("itemLabelGenerator")&&this.requestContentUpdate()}_onInput(e){let t=this._inputElementValue,r={};this.filter===t?this._filterChanged(this.filter):r.filter=t,!this.opened&&!this._isClearButton(e)&&!this.autoOpenDisabled&&(r.opened=!0),this.setProperties(r)}_getItemLabel(e){if(typeof this.itemLabelGenerator=="function"&&e)return this.itemLabelGenerator(e)||"";let t=e&&this.itemLabelPath?G(this.itemLabelPath,e):void 0;return t==null&&(t=e?e.toString():""),t}_getItemValue(e){let t=e&&this.itemValuePath?G(this.itemValuePath,e):void 0;return t===void 0&&(t=e?e.toString():""),t}_itemLabelPathChanged(e){typeof e!="string"&&console.error("You should set itemLabelPath to a valid string")}_filterChanged(e){this._scrollIntoView(0),this._focusedIndex=-1,this.items?this.filteredItems=this._filterItems(this.items,e):this._filteredItemsChanged(this.filteredItems)}_itemsChanged(e,t){this._ensureItemsOrDataProvider(()=>{this.items=t}),e?this.filteredItems=e.slice(0):t&&(this.filteredItems=null)}_filteredItemsChanged(e){this._setDropdownItems(e)}_setDropdownItems(){}_filterItems(e,t){return e&&e.filter(o=>(t=t?t.toString().toLowerCase():"",this._getItemLabel(o).toString().toLowerCase().indexOf(t)>-1))}__getItemIndexByValue(e,t){return!e||!cd(t)?-1:Qo(e,r=>this._getItemValue(r)===t)}__getItemIndexByLabel(e,t){return!e||!t?-1:Qo(e,r=>this._getItemLabel(r).toString().toLowerCase()===t.toString().toLowerCase())}};function ud(s){return s!=null}var Xo=s=>class extends Je(bi(s)){static get properties(){return{renderer:{type:Object,sync:!0},allowCustomValue:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItem:{type:Object,notify:!0,sync:!0},itemClassNameGenerator:{type:Object},itemIdPath:{type:String,sync:!0},__keepOverlayOpened:{type:Boolean,sync:!0}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","_selectedItemChanged(selectedItem, itemValuePath, itemLabelPath)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme)"]}ready(){super.ready(),this._lastCommittedValue=this.value}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer","selectedItem"].forEach(t=>{e.has(t)&&(this._scroller[t]=this[t])})}_updateScroller(e,t,r,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let n=this.hasAttribute("closing");this._scroller.setProperties({items:e||n?t:[],opened:e,focusedIndex:r,theme:o})}_openedOrItemsChanged(e,t,r,o){this._overlayOpened=e&&(o||r||!!t?.length)}_onClearButtonClick(e){super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_inputElementChanged(e){super._inputElementChanged(e),e&&this._revertInputValueToValue()}_closeOrCommit(){!this.opened&&!this.loading?this._commitValue():this.close()}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!==""&&this._getItemLabel(this.selectedItem)!==this._inputElementValue;return this.allowCustomValue||!e}_onEscapeCancel(){this.cancel()}_onClearAction(){this.selectedItem=null,this.allowCustomValue&&(this.value=""),this._detectAndDispatchChange()}_clearFilter(){this.filter=""}cancel(){this._revertInputValueToValue(),this._lastCommittedValue=this.value,this._closeOrCommit()}_onOpened(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-opened",{bubbles:!0,composed:!0})),this._lastCommittedValue=this.value}_onOverlayClosed(){this.dispatchEvent(new CustomEvent("vaadin-combo-box-dropdown-closed",{bubbles:!0,composed:!0}))}_onClosed(){(!this.loading||this.allowCustomValue)&&this._commitValue()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.selectedItem!==e&&(this.selectedItem=e),this._inputElementValue=this._getItemLabel(this.selectedItem),this._focusedIndex=-1}else if(this._inputElementValue===""||this._inputElementValue===void 0)this.selectedItem=null,this.allowCustomValue&&(this.value="");else{let e=[this.selectedItem,...this._dropdownItems||[]],t=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!t){let r=this._inputElementValue;this._lastCustomValue=r;let o=new CustomEvent("custom-value-set",{detail:r,composed:!0,cancelable:!0,bubbles:!0});this.dispatchEvent(o),o.defaultPrevented||(this.value=r)}else!this.allowCustomValue&&!this.opened&&t?this.value=this._getItemValue(t):this._revertInputValueToValue()}this._detectAndDispatchChange(),this._clearSelectionRange(),this._clearFilter()}_onChange(e){e.stopPropagation()}_revertInputValue(){this.filter!==""?this._inputElementValue=this.filter:this._revertInputValueToValue(),this._clearSelectionRange()}_revertInputValueToValue(){this.allowCustomValue&&!this.selectedItem?this._inputElementValue=this.value:this._inputElementValue=this._getItemLabel(this.selectedItem)}_selectedItemChanged(e){if(e==null)this.filteredItems&&(this.allowCustomValue||(this.value=""),this._toggleHasValue(this._hasValue),this._inputElementValue=this.value);else{let t=this._getItemValue(e);if(this.value!==t&&(this.value=t,this.value!==t))return;this._toggleHasValue(!0),this._inputElementValue=this._getItemLabel(e)}}_valueChanged(e,t){e===""&&t===void 0||(ud(e)?(this._getItemValue(this.selectedItem)!==e&&this._selectItemForValue(e),!this.selectedItem&&this.allowCustomValue&&(this._inputElementValue=e),this._toggleHasValue(this._hasValue)):this.selectedItem=null,this._clearFilter(),this._lastCommittedValue=void 0)}_detectAndDispatchChange(){document.hasFocus()&&this._requestValidation(),this.value!==this._lastCommittedValue&&(this.dispatchEvent(new CustomEvent("change",{bubbles:!0})),this._lastCommittedValue=this.value)}_selectItemForValue(e){let t=this.__getItemIndexByValue(this.filteredItems,e),r=this.selectedItem;t>=0?this.selectedItem=this.filteredItems[t]:this.dataProvider&&this.selectedItem===void 0?this.selectedItem=void 0:this.selectedItem=null,this.selectedItem===null&&r===null&&this._selectedItemChanged(this.selectedItem)}_setDropdownItems(e){let t=this._dropdownItems;this._dropdownItems=e;let r=t?t[this._focusedIndex]:null,o=this.__getItemIndexByValue(e,this.value);if((this.selectedItem===null||this.selectedItem===void 0)&&o>=0&&(this.selectedItem=e[o]),t&&t[this._focusedIndex]instanceof L&&e[this._focusedIndex]instanceof L)return;let n=this.__getItemIndexByValue(e,this._getItemValue(r));n>-1?this._focusedIndex=n:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_handleFocusOut(){if(!this.opened&&this.allowCustomValue&&this._inputElementValue===this._lastCustomValue){delete this._lastCustomValue;return}super._handleFocusOut()}};var Gr=class extends gi(fi(Xo(pi(de(v(A(g(b(_))))))))){static get is(){return"vaadin-combo-box"}static get styles(){return[N,_i]}static get properties(){return{_positionTarget:{type:Object}}}get clearElement(){return this.$.clearButton}render(){return u`
      <div class="vaadin-combo-box-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-combo-box-overlay
        id="overlay"
        exportparts="overlay, content, loader"
        .owner="${this}"
        .dir="${this.dir}"
        .opened="${this._overlayOpened}"
        ?loading="${this.loading}"
        theme="${k(this._theme)}"
        .positionTarget="${this._positionTarget}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-combo-box-overlay>
    `}ready(){super.ready(),this.addController(new W(this,i=>{this._setInputElement(i),this._setFocusElement(i),this.stateTarget=i,this.ariaTarget=i})),this.addController(new V(this.inputElement,this._labelController)),this._tooltipController=new D(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(i=>!i.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this._toggleElement=this.$.toggleButton}updated(i){super.updated(i),(i.has("dataProvider")||i.has("value"))&&this._warnDataProviderValue(this.dataProvider,this.value)}_onClearButtonClick(i){i.stopPropagation(),super._onClearButtonClick(i)}_onHostClick(i){let e=i.composedPath();(e.includes(this._labelNode)||e.includes(this._positionTarget))&&super._onHostClick(i)}_warnDataProviderValue(i,e){if(i&&e!==""&&(this.selectedItem===void 0||this.selectedItem===null)){let t=this.__getItemIndexByValue(this.filteredItems,e);(t<0||!this._getItemLabel(this.filteredItems[t]))&&console.warn("Warning: unable to determine the label for the provided `value`. Nothing to display in the text field. This usually happens when setting an initial `value` before any items are returned from the `dataProvider` callback. Consider setting `selectedItem` instead of `value`")}}};m(Gr);var Zo=p`
  [part='overlay'] {
    display: flex;
    flex: auto;
    max-height: var(--vaadin-date-picker-overlay-max-height, 30rem);
    box-sizing: content-box;
    width: var(
      --vaadin-date-picker-overlay-width,
      round(
        var(--vaadin-date-picker-date-width, 2rem) * 7 +
          var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s)) * 2 +
          var(--vaadin-date-picker-year-scroller-width, 3rem),
        1px
      )
    );
    cursor: default;
  }

  :host([fullscreen]) [part='backdrop'] {
    display: block;
  }

  :host([fullscreen]) [part='overlay'] {
    border: none;
    border-radius: 0;
    max-height: 75vh;
    width: 100%;
  }

  [part~='content'] {
    flex: auto;
  }

  @media (max-width: 450px), (max-height: 450px) {
    :host {
      inset: auto 0 0 !important;
    }
  }
`;var Jo=s=>class extends rt(J(s)){_shouldCloseOnOutsideClick(e){return!e.composedPath().includes(this.positionTarget)}_mouseDownListener(e){super._mouseDownListener(e),this._shouldCloseOnOutsideClick(e)&&!ze(e.composedPath()[0])&&e.preventDefault()}};var Kr=class extends Jo(I(v(g(b(_))))){static get is(){return"vaadin-date-picker-overlay"}static get styles(){return[Z,Zo]}render(){return u`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}get _contentRoot(){return this.owner._overlayContent}};m(Kr);function en(s){let i=s.getDay();i===0&&(i=7);let e=4-i,t=new Date(s.getTime()+e*24*3600*1e3),r=new Date(0,0);r.setFullYear(t.getFullYear());let o=t.getTime()-r.getTime(),n=Math.round(o/(24*3600*1e3));return Math.floor(n/7+1)}function yi(s){let i=new Date(s);return i.setHours(0,0,0,0),i}function Mt(s){return new Date(Date.UTC(s.getUTCFullYear(),s.getUTCMonth(),s.getUTCDate(),0,0,0,0))}function $(s,i,e=yi){return s instanceof Date&&i instanceof Date&&e(s).getTime()===e(i).getTime()}function Yr(s){return{day:s.getDate(),month:s.getMonth(),year:s.getFullYear()}}function fe(s,i,e,t){let r=!1;if(typeof t=="function"&&s){let o=Yr(s);r=t(o)}return(!i||s>=i)&&(!e||s<=e)&&!r}function xi(s,i){return i.filter(e=>e!==void 0).reduce((e,t)=>{if(!t)return e;if(!e)return t;let r=Math.abs(s.getTime()-t.getTime()),o=Math.abs(e.getTime()-s.getTime());return r<o?t:e})}function Ci(s){let i=new Date,e=new Date(i);return e.setDate(1),e.setMonth(parseInt(s)+i.getMonth()),e}function tn(s,i,e=0,t=1){if(i>99)throw new Error("The provided year cannot have more than 2 digits.");if(i<0)throw new Error("The provided year cannot be negative.");let r=i+Math.floor(s.getFullYear()/100)*100;return s<new Date(r-50,e,t)?r-=100:s>new Date(r+50,e,t)&&(r+=100),r}function dt(s){let i=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(s);if(!i)return;let e=new Date(0,0);return e.setFullYear(parseInt(i[1],10)),e.setMonth(parseInt(i[2],10)-1),e.setDate(parseInt(i[3],10)),e}function rn(s){let i=/^([-+]\d{1}|\d{2,4}|[-+]\d{6})-(\d{1,2})-(\d{1,2})$/u.exec(s);if(!i)return;let e=new Date(Date.UTC(0,0));return e.setUTCFullYear(parseInt(i[1],10)),e.setUTCMonth(parseInt(i[2],10)-1),e.setUTCDate(parseInt(i[3],10)),e}function sn(s){let i=(l,d="00")=>(d+l).substr((d+l).length-d.length),e="",t="0000",r=s.year;r<0?(r=-r,e="-",t="000000"):s.year>=1e4&&(e="+",t="000000");let o=e+i(r,t),n=i(s.month+1),a=i(s.day);return[o,n,a].join("-")}function on(s){return s instanceof Date?sn({year:s.getFullYear(),month:s.getMonth(),day:s.getDate()}):""}function nn(s){return s instanceof Date?sn({year:s.getUTCFullYear(),month:s.getUTCMonth(),day:s.getUTCDate()}):""}var an=document.createElement("template");an.innerHTML=`
  <style>
    :host {
      display: block;
      overflow: hidden;
      height: 500px;
    }

    #scroller {
      position: relative;
      height: 100%;
      overflow: auto;
      /* Prevent browser scroll anchoring from overriding the virtual scroll position. */
      overflow-anchor: none;
      outline: none;
      overflow-x: hidden;
      scrollbar-width: none;
    }

    #scroller::-webkit-scrollbar {
      display: none;
    }

    .buffer {
      position: absolute;
      width: var(--vaadin-infinite-scroller-buffer-width, 100%);
      box-sizing: border-box;
      top: var(--vaadin-infinite-scroller-buffer-offset, 0);
    }
  </style>

  <div id="scroller" tabindex="-1">
    <div class="buffer"></div>
    <div class="buffer"></div>
    <div id="fullHeight"></div>
  </div>
`;var ht=class extends HTMLElement{constructor(){super(),this.attachShadow({mode:"open"}).appendChild(an.content.cloneNode(!0)),this.bufferSize=20,this._initialScroll=5e5,this._initialIndex=0,this._activated=!1}get active(){return this._activated}set active(i){i&&!this._activated&&(this._createPool(),this._activated=!0)}get bufferOffset(){return this._buffers[0].offsetTop}get itemHeight(){if(!this._itemHeightVal){let i=getComputedStyle(this).getPropertyValue("--vaadin-infinite-scroller-item-height"),e="background-position";this.$.fullHeight.style.setProperty(e,i);let t=getComputedStyle(this.$.fullHeight).getPropertyValue(e);this.$.fullHeight.style.removeProperty(e),this._itemHeightVal=parseFloat(t)}return this._itemHeightVal}get _bufferHeight(){return this.itemHeight*this.bufferSize}get position(){return(this.$.scroller.scrollTop-this._buffers[0].translateY)/this.itemHeight+this._firstIndex}set position(i){this._preventScrollEvent=!0,i>this._firstIndex&&i<this._firstIndex+this.bufferSize*2?this.$.scroller.scrollTop=this.itemHeight*(i-this._firstIndex)+this._buffers[0].translateY:(this._initialIndex=~~i,this._reset(),this._scrollDisabled=!0,this.$.scroller.scrollTop+=i%1*this.itemHeight,this._scrollDisabled=!1)}connectedCallback(){this._ready||(this._ready=!0,this.$={},this.shadowRoot.querySelectorAll("[id]").forEach(i=>{this.$[i.id]=i}),this.$.scroller.addEventListener("scroll",()=>this._scroll()),this._buffers=[...this.shadowRoot.querySelectorAll(".buffer")],this.$.fullHeight.style.height=`${this._initialScroll*2}px`)}disconnectedCallback(){this._debouncerScrollFinish&&this._debouncerScrollFinish.cancel(),this._debouncerUpdateClones&&this._debouncerUpdateClones.cancel(),this.__pendingFinishInit&&cancelAnimationFrame(this.__pendingFinishInit)}forceUpdate(){this._debouncerScrollFinish&&this._debouncerScrollFinish.flush(),this._debouncerUpdateClones&&(this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(),this._debouncerUpdateClones.cancel())}_createElement(){}_updateElement(i,e){}_finishInit(){this._initDone||(this._buffers.forEach(i=>{[...i.children].forEach(e=>{this._ensureStampedInstance(e._itemWrapper)})}),this._buffers[0].translateY||this._reset(),this._initDone=!0,this.dispatchEvent(new CustomEvent("init-done")))}_translateBuffer(i){let e=i?1:0;this._buffers[e].translateY=this._buffers[e?0:1].translateY+this._bufferHeight*(e?-1:1),this._buffers[e].style.transform=`translate3d(0, ${this._buffers[e].translateY}px, 0)`,this._buffers[e].updated=!1,this._buffers.reverse()}_scroll(){if(this._scrollDisabled)return;let i=this.$.scroller.scrollTop;(i<this._bufferHeight||i>this._initialScroll*2-this._bufferHeight)&&(this._initialIndex=~~this.position,this._reset());let e=this.itemHeight+this.bufferOffset,t=i>this._buffers[1].translateY+e,r=i<this._buffers[0].translateY+e;(t||r)&&(this._translateBuffer(r),this._updateClones()),this._preventScrollEvent||this.dispatchEvent(new CustomEvent("custom-scroll",{bubbles:!1,composed:!0})),this._preventScrollEvent=!1,this._debouncerScrollFinish=x.debounce(this._debouncerScrollFinish,R.after(200),()=>{let o=this.$.scroller.getBoundingClientRect();!this._isVisible(this._buffers[0],o)&&!this._isVisible(this._buffers[1],o)&&(this.position=this.position)})}_reset(){this._scrollDisabled=!0,this.$.scroller.scrollTop=this._initialScroll,this._buffers[0].translateY=this._initialScroll-this._bufferHeight,this._buffers[1].translateY=this._initialScroll,this._buffers.forEach(i=>{i.style.transform=`translate3d(0, ${i.translateY}px, 0)`}),this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones(!0),this._debouncerUpdateClones=x.debounce(this._debouncerUpdateClones,R.after(200),()=>{this._buffers[0].updated=this._buffers[1].updated=!1,this._updateClones()}),this._scrollDisabled=!1}_createPool(){let i=this.innerHeight;this._buffers.forEach(e=>{for(let t=0;t<this.bufferSize;t++){let r=document.createElement("div");r.style.height=`${this.itemHeight}px`,r.instance={};let o=`vaadin-infinite-scroller-item-content-${ye()}`,n=document.createElement("slot");n.setAttribute("name",o),n._itemWrapper=r,e.appendChild(n),r.setAttribute("slot",o),this.appendChild(r),this.itemHeight*t<=i&&this._ensureStampedInstance(r)}}),this.__pendingFinishInit=requestAnimationFrame(()=>{this._finishInit(),this.__pendingFinishInit=null})}_ensureStampedInstance(i){if(i.firstElementChild)return;let e=i.instance;i.instance=this._createElement(),i.appendChild(i.instance),Object.keys(e).forEach(t=>{i.instance[t]=e[t]})}_updateClones(i){this._firstIndex=Math.round((this._buffers[0].translateY-this._initialScroll)/this.itemHeight)+this._initialIndex;let e=i?this.$.scroller.getBoundingClientRect():void 0;this._buffers.forEach((t,r)=>{if(!t.updated){let o=this._firstIndex+this.bufferSize*r;[...t.children].forEach((n,a)=>{let l=n._itemWrapper;(!i||this._isVisible(l,e))&&this._updateElement(l.instance,o+a)}),t.updated=!0}})}_isVisible(i,e){let t=i.getBoundingClientRect();return t.bottom>e.top&&t.top<e.bottom}};var ln=document.createElement("template");ln.innerHTML=`
  <style>
    :host {
      --vaadin-infinite-scroller-item-height: 270px;
      grid-area: months;
      height: auto;
    }
  </style>
`;var Qr=class extends ht{static get is(){return"vaadin-date-picker-month-scroller"}constructor(){super(),this.bufferSize=3,this.shadowRoot.appendChild(ln.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-month-calendar")}_updateElement(i,e){i.month=Ci(e)}};m(Qr);var dn=document.createElement("template");dn.innerHTML=`
  <style>
    :host {
      --vaadin-infinite-scroller-item-height: 80px;
      width: 50px;
      display: block;
      position: relative;
      grid-area: years;
      height: auto;
      -webkit-tap-highlight-color: transparent;
      -webkit-user-select: none;
      user-select: none;
      /* Center the year scroller position. */
      --vaadin-infinite-scroller-buffer-offset: 50%;
    }

    :host::before {
      content: '';
      display: block;
      background: transparent;
      width: 0;
      height: 0;
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      border-width: 6px;
      border-style: solid;
      border-color: transparent;
      border-left-color: #000;
    }
  </style>
`;var Xr=class extends ht{static get is(){return"vaadin-date-picker-year-scroller"}constructor(){super(),this.bufferSize=12,this.shadowRoot.appendChild(dn.content.cloneNode(!0))}_createElement(){return document.createElement("vaadin-date-picker-year")}_updateElement(i,e){i.year=this._yearAfterXYears(e)}_yearAfterXYears(i){let e=new Date,t=new Date(e);return t.setFullYear(parseInt(i)+e.getFullYear()),t.getFullYear()}};m(Xr);var hn=p`
  :host {
    display: block;
    height: 100%;
  }

  [part='year-number'] {
    align-items: center;
    display: flex;
    height: 50%;
    justify-content: center;
    transform: translateY(-50%);
    color: var(--vaadin-text-color-secondary);
  }

  :host([current]) [part='year-number'] {
    color: var(--vaadin-date-picker-year-scroller-current-year-color, var(--vaadin-text-color));
  }
`;var Zr=class extends v(g(b(_))){static get is(){return"vaadin-date-picker-year"}static get styles(){return hn}static get properties(){return{year:{type:String,sync:!0},selectedDate:{type:Object,sync:!0}}}render(){return u`
      <div part="year-number">${this.year}</div>
      <div part="year-separator" aria-hidden="true"></div>
    `}updated(i){super.updated(i),i.has("year")&&this.toggleAttribute("current",this.year===new Date().getFullYear()),(i.has("year")||i.has("selectedDate"))&&this.toggleAttribute("selected",this.selectedDate&&this.selectedDate.getFullYear()===this.year)}};m(Zr);var cn=p`
  :host {
    display: block;
    padding: var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s));
  }

  [part='month-header'] {
    color: var(--vaadin-date-picker-month-header-color, var(--vaadin-text-color));
    font-size: var(--vaadin-date-picker-month-header-font-size, 0.9375rem);
    font-weight: var(--vaadin-date-picker-month-header-font-weight, 500);
    line-height: 1;
    margin-bottom: 0.75rem;
    text-align: center;
  }

  table {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
  }

  thead,
  tbody,
  tr {
    display: contents;
  }

  [part~='weekday'] {
    color: var(--vaadin-date-picker-weekday-color, var(--vaadin-text-color-secondary));
    font-size: var(--vaadin-date-picker-weekday-font-size, 0.75rem);
    font-weight: var(--vaadin-date-picker-weekday-font-weight, 500);
    margin-bottom: 0.375rem;
  }

  /* Week numbers are on a separate row, don't reserve space on weekday row. */
  [part~='weekday']:empty {
    display: none;
  }

  [part~='week-number'] {
    grid-column: -1 / 1;
    color: var(--vaadin-date-picker-week-number-color, var(--vaadin-text-color-secondary));
    font-size: var(--vaadin-date-picker-week-number-font-size, 0.7rem);
    line-height: 1;
    margin-top: 0.125em;
    margin-bottom: 0.125em;
    gap: 0.25em;
  }

  [part~='week-number']::after {
    content: '';
    height: 1px;
    flex: 1;
    background: var(
      --vaadin-date-picker-week-divider-color,
      var(--vaadin-divider-color, var(--vaadin-border-color-secondary))
    );
  }

  [part~='weekday'],
  [part~='week-number'],
  [part~='date'] {
    align-items: center;
    display: flex;
    justify-content: center;
    padding: 0;
  }

  [part~='date'] {
    border-radius: var(--vaadin-date-picker-date-border-radius, var(--vaadin-radius-m));
    position: relative;
    height: var(--vaadin-date-picker-date-height, 2rem);
    cursor: var(--vaadin-clickable-cursor);
    outline: none;
  }

  [part~='date']:empty {
    pointer-events: none !important;
  }

  [part~='date']::after {
    border-radius: inherit;
    content: '';
    position: absolute;
    z-index: -1;
    height: min(2em, 100%);
    aspect-ratio: 1;
  }

  :where([part~='date']:focus-visible)::after {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  [part~='today'] {
    color: var(--vaadin-date-picker-date-today-color, var(--vaadin-text-color));
  }

  [part~='selected'] {
    color: var(--vaadin-date-picker-date-selected-color, var(--vaadin-background-color));
  }

  [part~='selected']::after {
    background: var(--vaadin-date-picker-date-selected-background, var(--vaadin-text-color));
    outline-offset: 1px;
  }

  [disabled] {
    cursor: var(--vaadin-disabled-cursor);
    color: var(--vaadin-date-picker-date-disabled-color, var(--vaadin-text-color-disabled));
    opacity: 0.7;
  }

  [hidden] {
    display: none;
  }

  @media (forced-colors: active) {
    [part~='week-number']::after {
      background: CanvasText;
    }

    [part~='today'] {
      font-weight: 600;
    }

    [part~='selected'] {
      forced-color-adjust: none;
      --vaadin-date-picker-date-selected-color: SelectedItemText;
      color: SelectedItemText !important;
      --vaadin-date-picker-date-selected-background: SelectedItem;
    }

    [disabled] {
      color: GrayText !important;
    }
  }
`;var un=s=>class extends j(s){static get properties(){return{month:{type:Object,value:new Date,sync:!0},selectedDate:{type:Object,notify:!0,sync:!0},focusedDate:{type:Object},showWeekNumbers:{type:Boolean,value:!1},i18n:{type:Object},ignoreTaps:{type:Boolean},minDate:{type:Date,value:null,sync:!0},maxDate:{type:Date,value:null,sync:!0},isDateDisabled:{type:Function,value:()=>!1},enteredDate:{type:Date},disabled:{type:Boolean,reflectToAttribute:!0,computed:"__computeDisabled(month, minDate, maxDate)"},_days:{type:Array,computed:"__computeDays(month, i18n, minDate, maxDate, isDateDisabled)"},_weeks:{type:Array,computed:"__computeWeeks(_days)"},_notTapping:{type:Boolean},__hasFocus:{type:Boolean}}}static get observers(){return["__focusedDateChanged(focusedDate, _days)","_showWeekNumbersChanged(showWeekNumbers, i18n)"]}get focusableDateElement(){return[...this.shadowRoot.querySelectorAll("[part~=date]")].find(e=>$(e.date,this.focusedDate))}ready(){super.ready(),se(this.$.monthGrid,"tap",this._handleTap.bind(this))}_setFocused(e){super._setFocused(e),this.__hasFocus=e}__computeDisabled(e,t,r){let o=new Date(0,0);o.setFullYear(e.getFullYear()),o.setMonth(e.getMonth()),o.setDate(1);let n=new Date(0,0);return n.setFullYear(e.getFullYear()),n.setMonth(e.getMonth()+1),n.setDate(0),t&&r&&t.getMonth()===r.getMonth()&&t.getMonth()===e.getMonth()&&r.getDate()-t.getDate()>=0?!1:!fe(o,t,r)&&!fe(n,t,r)}_getTitle(e,t){if(!(e===void 0||t===void 0))return t.formatTitle(t.monthNames[e.getMonth()],e.getFullYear())}_onMonthGridTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300)}_dateAdd(e,t){e.setDate(e.getDate()+t)}_applyFirstDayOfWeek(e,t){if(!(e===void 0||t===void 0))return e.slice(t).concat(e.slice(0,t))}__computeWeekDayNames(e,t){if(e===void 0||t===void 0)return[];let{weekdays:r,weekdaysShort:o,firstDayOfWeek:n}=e,a=this._applyFirstDayOfWeek(o,n);return this._applyFirstDayOfWeek(r,n).map((d,h)=>({weekDay:d,weekDayShort:a[h]})).slice(0,7)}__focusedDateChanged(e,t){Array.isArray(t)&&t.some(r=>$(r,e))?this.removeAttribute("aria-hidden"):this.setAttribute("aria-hidden","true")}_getDate(e){return e?e.getDate():""}__computeShowWeekSeparator(e,t){return e&&t?.firstDayOfWeek===1}_isToday(e){return $(new Date,e)}__computeDays(e,t){if(e===void 0||t===void 0)return[];let r=new Date(0,0);for(r.setFullYear(e.getFullYear()),r.setMonth(e.getMonth()),r.setDate(1);r.getDay()!==t.firstDayOfWeek;)this._dateAdd(r,-1);let o=[],n=r.getMonth(),a=e.getMonth();for(;r.getMonth()===a||r.getMonth()===n;)o.push(r.getMonth()===a?new Date(r.getTime()):null),this._dateAdd(r,1);return o}__computeWeeks(e){return e.reduce((t,r,o)=>(o%7===0&&t.push([]),t[t.length-1].push(r),t),[])}_handleTap(e){!this.ignoreTaps&&!this._notTapping&&e.target.date&&!e.target.hasAttribute("disabled")&&(this.selectedDate=e.target.date,this.dispatchEvent(new CustomEvent("date-tap",{detail:{date:e.target.date},bubbles:!0,composed:!0})))}_preventDefault(e){e.preventDefault()}__computeWeekNumber(e){let t=e.reduce((r,o)=>!r&&o?o:r);return en(t)}__computeDayAriaLabel(e){if(!e)return"";let t=`${this._getDate(e)} ${this.i18n.monthNames[e.getMonth()]} ${e.getFullYear()}, ${this.i18n.weekdays[e.getDay()]}`;return this._isToday(e)&&(t+=`, ${this.i18n.today}`),t}_showWeekNumbersChanged(e,t){this.__computeShowWeekSeparator(e,t)?this.setAttribute("week-numbers",""):this.removeAttribute("week-numbers")}__computeDatePart(e,t,r,o,n,a,l,d){let h=["date"];return this.__isDayDisabled(e,o,n,a)&&h.push("disabled"),$(e,t)&&(d||$(e,l))&&h.push("focused"),this.__isDaySelected(e,r)&&h.push("selected"),this._isToday(e)&&h.push("today"),e<yi(new Date)&&h.push("past"),e>yi(new Date)&&h.push("future"),h.join(" ")}__isDaySelected(e,t){return $(e,t)}__computeDayAriaSelected(e,t){return String(this.__isDaySelected(e,t))}__isDayDisabled(e,t,r,o){return!fe(e,t,r,o)}__computeDayAriaDisabled(e,t,r,o){return e===void 0||t===void 0&&r===void 0&&o===void 0?"false":String(this.__isDayDisabled(e,t,r,o))}__computeDayTabIndex(e,t){return $(e,t)?"0":"-1"}};var Jr=class extends un(v(g(b(_)))){static get is(){return"vaadin-month-calendar"}static get styles(){return cn}render(){let i=this.__computeWeekDayNames(this.i18n,this.showWeekNumbers),e=this._weeks,t=!this.__computeShowWeekSeparator(this.showWeekNumbers,this.i18n);return u`
      <div part="month-header" id="month-header" aria-hidden="true">${this._getTitle(this.month,this.i18n)}</div>
      <table
        id="monthGrid"
        role="grid"
        aria-labelledby="month-header"
        @touchend="${this._preventDefault}"
        @touchstart="${this._onMonthGridTouchStart}"
      >
        <thead id="weekdays-container">
          <tr role="row" part="weekdays">
            <th part="weekday" aria-hidden="true" ?hidden="${t}"></th>
            ${i.map(r=>u`
                <th role="columnheader" part="weekday" scope="col" abbr="${r.weekDay}" aria-hidden="true">
                  ${r.weekDayShort}
                </th>
              `)}
          </tr>
        </thead>
        <tbody id="days-container">
          ${e.map(r=>u`
              <tr role="row">
                <td part="week-number" aria-hidden="true" ?hidden="${t}">
                  ${this.__computeWeekNumber(r)}
                </td>
                ${r.map(o=>u`
                    <td
                      role="gridcell"
                      part="${this.__computeDatePart(o,this.focusedDate,this.selectedDate,this.minDate,this.maxDate,this.isDateDisabled,this.enteredDate,this.__hasFocus)}"
                      .date="${o}"
                      ?disabled="${this.__isDayDisabled(o,this.minDate,this.maxDate,this.isDateDisabled)}"
                      tabindex="${this.__computeDayTabIndex(o,this.focusedDate)}"
                      aria-selected="${this.__computeDayAriaSelected(o,this.selectedDate)}"
                      aria-disabled="${this.__computeDayAriaDisabled(o,this.minDate,this.maxDate,this.isDateDisabled)}"
                      aria-label="${this.__computeDayAriaLabel(o)}"
                      >${this._getDate(o)}</td
                    >
                  `)}
              </tr>
            `)}
        </tbody>
      </table>
    `}};m(Jr);var pn=p`
  :host {
    display: grid;
    grid-template-areas:
      'header header'
      'months years'
      'toolbar years';
    grid-template-columns: minmax(0, 1fr) 0;
    height: 100%;
    outline: none;
    overflow: hidden;
  }

  :host([desktop]) {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  :host([fullscreen][years-visible]) {
    grid-template-columns: minmax(0, 1fr) auto;
  }

  [part='years-toggle-button'] {
    display: inline-flex;
    align-items: center;
    border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
    color: var(--vaadin-text-color);
    font-size: var(--vaadin-button-font-size, inherit);
    font-weight: var(--vaadin-button-font-weight, 500);
    height: var(--vaadin-button-height, auto);
    line-height: var(--vaadin-button-line-height, inherit);
    padding: var(--vaadin-button-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    cursor: var(--vaadin-clickable-cursor);
  }

  :host([years-visible]) [part='years-toggle-button'] {
    background: var(--vaadin-text-color);
    color: var(--vaadin-background-color);
  }

  [hidden] {
    display: none !important;
  }

  ::slotted([slot='months']) {
    --vaadin-infinite-scroller-item-height: round(
      var(--vaadin-date-picker-month-header-font-size, 0.9375rem) + 0.75rem +
        var(--vaadin-date-picker-date-height, 2rem) * 7 + var(--_vaadin-date-picker-week-numbers-visible, 0) *
        (
          var(--vaadin-date-picker-week-number-font-size, 0.7rem) * 6.25 +
            var(--vaadin-date-picker-month-padding, var(--vaadin-padding-s)) * 3
        ),
      1px
    );
  }

  :host(:not([fullscreen])) ::slotted([slot='months']) {
    border-bottom: 1px solid var(--vaadin-border-color-secondary);
  }

  ::slotted([slot='years']) {
    visibility: hidden;
    background: var(--vaadin-date-picker-year-scroller-background, var(--vaadin-background-container));
    width: var(--vaadin-date-picker-year-scroller-width, 3rem);
    box-sizing: border-box;
    border-inline-start: 1px solid
      var(--vaadin-date-picker-year-scroller-border-color, var(--vaadin-border-color-secondary));
    overflow: visible;
    min-height: 0;
    clip-path: inset(0);
  }

  ::slotted([slot='years'])::before {
    background: var(--vaadin-overlay-background, var(--vaadin-background-color));
    border: 1px solid var(--vaadin-date-picker-year-scroller-border-color, var(--vaadin-border-color-secondary));
    width: 16px;
    height: 16px;
    position: absolute;
    left: auto;
    z-index: 1;
    rotate: 45deg;
    translate: calc(-50% - 1px) -50%;
    transform: none;
  }

  :host([dir='rtl']) ::slotted([slot='years'])::before {
    translate: calc(50% + 1px) -50%;
  }

  :host([desktop]) ::slotted([slot='years']),
  :host([years-visible]) ::slotted([slot='years']) {
    visibility: visible;
  }

  [part='toolbar'] {
    display: flex;
    grid-area: toolbar;
    justify-content: space-between;
    padding: var(--vaadin-date-picker-toolbar-padding, var(--vaadin-padding-s));
  }

  :host([fullscreen]) [part='toolbar'] {
    grid-area: header;
    border-bottom: 1px solid var(--vaadin-border-color-secondary);
  }
`;var Te=class{constructor(i,e){this.query=i,this.callback=e,this._boundQueryHandler=this._queryHandler.bind(this)}hostConnected(){this._removeListener(),this._mediaQuery=window.matchMedia(this.query),this._addListener(),this._queryHandler(this._mediaQuery)}hostDisconnected(){this._removeListener()}_addListener(){this._mediaQuery&&this._mediaQuery.addListener(this._boundQueryHandler)}_removeListener(){this._mediaQuery&&this._mediaQuery.removeListener(this._boundQueryHandler),this._mediaQuery=null}_queryHandler(i){typeof this.callback=="function"&&this.callback(i.matches)}};var _n=s=>class extends s{static get properties(){return{scrollDuration:{type:Number,value:300},selectedDate:{type:Object,value:null,sync:!0},focusedDate:{type:Object,notify:!0,observer:"_focusedDateChanged",sync:!0},_focusedMonthDate:Number,initialPosition:{type:Object,observer:"_initialPositionChanged",sync:!0},_originDate:{type:Object,value:new Date},_visibleMonthIndex:Number,_desktopMode:{type:Boolean,observer:"_desktopModeChanged"},_desktopMediaQuery:{type:String,value:"(min-width: 375px)"},i18n:{type:Object},showWeekNumbers:{type:Boolean,value:!1},_ignoreTaps:Boolean,_notTapping:Boolean,minDate:{type:Object,sync:!0},maxDate:{type:Object,sync:!0},isDateDisabled:{type:Function},enteredDate:{type:Date,sync:!0},label:String,_cancelButton:{type:Object},_todayButton:{type:Object},calendars:{type:Array,value:()=>[]},years:{type:Array,value:()=>[]}}}static get observers(){return["__updateCalendars(calendars, i18n, minDate, maxDate, selectedDate, focusedDate, showWeekNumbers, _ignoreTaps, _theme, isDateDisabled, enteredDate)","__updateCancelButton(_cancelButton, i18n)","__updateTodayButton(_todayButton, i18n, minDate, maxDate, isDateDisabled)","__updateYears(years, selectedDate, _theme)"]}get __useSubMonthScrolling(){return this._monthScroller.clientHeight<this._monthScroller.itemHeight+this._monthScroller.bufferOffset}get focusableDateElement(){return this.calendars.map(e=>e.focusableDateElement).find(Boolean)}_initControllers(){this.addController(new Te(this._desktopMediaQuery,e=>{this._desktopMode=e})),this.addController(new T(this,"today-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",t=>this.__onTodayButtonKeyDown(t)),e.addEventListener("click",this._onTodayTap.bind(this)),this._todayButton=e}})),this.addController(new T(this,"cancel-button","vaadin-button",{observe:!1,initializer:e=>{e.setAttribute("theme","tertiary"),e.addEventListener("keydown",t=>this.__onCancelButtonKeyDown(t)),e.addEventListener("click",this._cancel.bind(this)),this._cancelButton=e}})),this.__initMonthScroller(),this.__initYearScroller()}reset(){this._closeYearScroller()}focusCancel(){this._cancelButton.focus()}scrollToDate(e,t){let r=this.__useSubMonthScrolling?this._calculateWeekScrollOffset(e):0;this._scrollToPosition(this._differenceInMonths(e,this._originDate)+r,t),this._monthScroller.forceUpdate()}__initMonthScroller(){this.addController(new T(this,"months","vaadin-date-picker-month-scroller",{observe:!1,initializer:e=>{e.addEventListener("custom-scroll",()=>{this._onMonthScroll()}),e.addEventListener("touchstart",()=>{this._onMonthScrollTouchStart()}),e.addEventListener("keydown",t=>{this.__onMonthCalendarKeyDown(t)}),e.addEventListener("init-done",()=>{let t=[...this.querySelectorAll("vaadin-month-calendar")];t.forEach(r=>{r.addEventListener("selected-date-changed",o=>{this.selectedDate=o.detail.value})}),this.calendars=t}),this._monthScroller=e}}))}__initYearScroller(){this.addController(new T(this,"years","vaadin-date-picker-year-scroller",{observe:!1,initializer:e=>{e.setAttribute("aria-hidden","true"),se(e,"tap",t=>{this._onYearTap(t)}),e.addEventListener("custom-scroll",()=>{this._onYearScroll()}),e.addEventListener("touchstart",()=>{this._onYearScrollTouchStart()}),e.addEventListener("init-done",()=>{this.years=[...this.querySelectorAll("vaadin-date-picker-year")]}),this._yearScroller=e}}))}__updateCancelButton(e,t){e&&(e.textContent=t?.cancel)}__updateTodayButton(e,t,r,o,n){e&&(e.textContent=t?.today,e.disabled=!this._isTodayAllowed(r,o,n))}__updateCalendars(e,t,r,o,n,a,l,d,h,c,f){e?.length&&e.forEach(y=>{y.i18n=t,y.minDate=r,y.maxDate=o,y.isDateDisabled=c,y.focusedDate=a,y.selectedDate=n,y.showWeekNumbers=l,y.ignoreTaps=d,y.enteredDate=f,h?y.setAttribute("theme",h):y.removeAttribute("theme")})}__updateYears(e,t,r){e?.length&&e.forEach(o=>{o.selectedDate=t,r?o.setAttribute("theme",r):o.removeAttribute("theme")})}_selectDate(e){return this._dateAllowed(e)?(this.selectedDate=e,this.dispatchEvent(new CustomEvent("date-selected",{detail:{date:e},bubbles:!0,composed:!0})),!0):!1}_desktopModeChanged(e){this.toggleAttribute("desktop",e)}_focusedDateChanged(e){this.revealDate(e)}revealDate(e,t=!0){if(!e)return;let r=this._differenceInMonths(e,this._originDate);if(this.__useSubMonthScrolling){let d=this._calculateWeekScrollOffset(e);this._scrollToPosition(r+d,t);return}let o=this._monthScroller.position>r,a=Math.max(this._monthScroller.itemHeight,this._monthScroller.clientHeight-this._monthScroller.bufferOffset*2)/this._monthScroller.itemHeight,l=this._monthScroller.position+a-1<r;o?this._scrollToPosition(r,t):l&&this._scrollToPosition(r-a+1,t)}_calculateWeekScrollOffset(e){let t=new Date(0,0);t.setFullYear(e.getFullYear()),t.setMonth(e.getMonth()),t.setDate(1);let r=0;for(;t.getDate()<e.getDate();)t.setDate(t.getDate()+1),t.getDay()===this.i18n.firstDayOfWeek&&(r+=1);return r/6}_initialPositionChanged(e){this._monthScroller&&this._yearScroller&&(this._monthScroller.active=!0,this._yearScroller.active=!0),this.scrollToDate(e)}_repositionYearScroller(){let e=this._monthScroller.position;this._visibleMonthIndex=Math.floor(e),this._yearScroller.position=(e+this._originDate.getMonth())/12}_repositionMonthScroller(){this._monthScroller.position=this._yearScroller.position*12-this._originDate.getMonth(),this._visibleMonthIndex=Math.floor(this._monthScroller.position)}_onMonthScroll(){this._repositionYearScroller(),this._doIgnoreTaps()}_onYearScroll(){this._repositionMonthScroller(),this._doIgnoreTaps()}_onYearScrollTouchStart(){this._notTapping=!1,setTimeout(()=>{this._notTapping=!0},300),this._repositionMonthScroller()}_onMonthScrollTouchStart(){this._repositionYearScroller()}_doIgnoreTaps(){this._ignoreTaps=!0,this._debouncer=x.debounce(this._debouncer,R.after(300),()=>{this._ignoreTaps=!1})}_onTodayTap(){let e=this._getTodayMidnight();Math.abs(this._monthScroller.position-this._differenceInMonths(e,this._originDate))<.001?(this._selectDate(e),this._close()):this._scrollToCurrentMonth()}_scrollToCurrentMonth(){this.focusedDate&&(this.focusedDate=new Date),this.scrollToDate(new Date,!0)}_onYearTap(e){if(!this._ignoreTaps&&!this._notTapping){let r=(e.detail.y-(this._yearScroller.getBoundingClientRect().top+this._yearScroller.clientHeight/2))/this._yearScroller.itemHeight;this._scrollToPosition(this._monthScroller.position+r*12,!0)}}_scrollToPosition(e,t){if(this._targetPosition!==void 0){this._targetPosition=e;return}if(!t){this._monthScroller.position=e,this._monthScroller.forceUpdate(),this._targetPosition=void 0,this._repositionYearScroller(),this.__tryFocusDate();return}this._targetPosition=e;let r;this._revealPromise=new Promise(d=>{r=d});let o=(d,h,c,f)=>(d/=f/2,d<1?c/2*d*d+h:(d-=1,-c/2*(d*(d-2)-1)+h)),n=0,a=this._monthScroller.position,l=d=>{n||(n=d);let h=d-n;if(h<this.scrollDuration){let c=o(h,a,this._targetPosition-a,this.scrollDuration);this._monthScroller.position=c,window.requestAnimationFrame(l)}else this.dispatchEvent(new CustomEvent("scroll-animation-finished",{bubbles:!0,composed:!0,detail:{position:this._targetPosition,oldPosition:a}})),this._monthScroller.position=this._targetPosition,this._monthScroller.forceUpdate(),this._targetPosition=void 0,r(),this._revealPromise=void 0;setTimeout(this._repositionYearScroller.bind(this),1)};window.requestAnimationFrame(l)}_toggleYearScroller(){this.toggleAttribute("years-visible")}_closeYearScroller(){this.removeAttribute("years-visible")}_yearAfterXMonths(e){return Ci(e).getFullYear()}_differenceInMonths(e,t){return(e.getFullYear()-t.getFullYear())*12-t.getMonth()+e.getMonth()}_clear(){this._selectDate("")}_close(){this.dispatchEvent(new CustomEvent("close",{bubbles:!0,composed:!0}))}_cancel(){this.focusedDate=this.selectedDate,this._close()}__toggleDate(e){$(e,this.selectedDate)?(this._clear(),this.focusedDate=e):this._selectDate(e)}__onMonthCalendarKeyDown(e){let t=!1;switch(e.key){case"ArrowDown":this._moveFocusByDays(7),t=!0;break;case"ArrowUp":this._moveFocusByDays(-7),t=!0;break;case"ArrowRight":this._moveFocusByDays(this.__isRTL?-1:1),t=!0;break;case"ArrowLeft":this._moveFocusByDays(this.__isRTL?1:-1),t=!0;break;case"Enter":this._selectDate(this.focusedDate)&&(this._close(),t=!0);break;case" ":this.__toggleDate(this.focusedDate),t=!0;break;case"Home":this._moveFocusInsideMonth(this.focusedDate,"minDate"),t=!0;break;case"End":this._moveFocusInsideMonth(this.focusedDate,"maxDate"),t=!0;break;case"PageDown":this._moveFocusByMonths(e.shiftKey?12:1),t=!0;break;case"PageUp":this._moveFocusByMonths(e.shiftKey?-12:-1),t=!0;break;case"Tab":this._onTabKeyDown(e,"calendar");break;default:break}t&&(e.preventDefault(),e.stopPropagation())}_onTabKeyDown(e,t){switch(e.stopPropagation(),t){case"calendar":e.shiftKey&&(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusCancel():this.__focusInput());break;case"today":e.shiftKey&&(e.preventDefault(),this.focusDateElement());break;case"cancel":e.shiftKey||(e.preventDefault(),this.hasAttribute("fullscreen")?this.focusDateElement():this.__focusInput());break;default:break}}__onTodayButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"today")}__onCancelButtonKeyDown(e){e.key==="Tab"&&this._onTabKeyDown(e,"cancel")}__focusInput(){this.dispatchEvent(new CustomEvent("focus-input",{bubbles:!0,composed:!0}))}__tryFocusDate(){if(this.__pendingDateFocus){let t=this.focusableDateElement;t&&$(t.date,this.__pendingDateFocus)&&(delete this.__pendingDateFocus,t.focus())}}async focusDate(e,t){let r=e||this.selectedDate||this.initialPosition||new Date;this.focusedDate=r,t||(this._focusedMonthDate=r.getDate()),await this.focusDateElement(!1)}async focusDateElement(e=!0){this.__pendingDateFocus=this.focusedDate,this.calendars.length||await new Promise(t=>{requestAnimationFrame(()=>{setTimeout(()=>{t()})})}),e&&this.revealDate(this.focusedDate),this._revealPromise&&await this._revealPromise,this.__tryFocusDate()}_focusClosestDate(e){this.focusDate(xi(e,[this.minDate,this.maxDate]))}_focusAllowedDate(e,t,r){this._dateAllowed(e,void 0,void 0,()=>!1)?this.focusDate(e,r):this._dateAllowed(this.focusedDate)?t>0?this.focusDate(this.maxDate):this.focusDate(this.minDate):this._focusClosestDate(this.focusedDate)}_getDateDiff(e,t){let r=new Date(0,0);return r.setFullYear(this.focusedDate.getFullYear()),r.setMonth(this.focusedDate.getMonth()+e),t&&r.setDate(this.focusedDate.getDate()+t),r}_moveFocusByDays(e){let t=this._getDateDiff(0,e);this._focusAllowedDate(t,e,!1)}_moveFocusByMonths(e){let t=this._getDateDiff(e),r=t.getMonth();this._focusedMonthDate||(this._focusedMonthDate=this.focusedDate.getDate()),t.setDate(this._focusedMonthDate),t.getMonth()!==r&&t.setDate(0),this._focusAllowedDate(t,e,!0)}_moveFocusInsideMonth(e,t){let r=new Date(0,0);r.setFullYear(e.getFullYear()),t==="minDate"?(r.setMonth(e.getMonth()),r.setDate(1)):(r.setMonth(e.getMonth()+1),r.setDate(0)),this._dateAllowed(r)?this.focusDate(r):this._dateAllowed(e)?this.focusDate(this[t]):this._focusClosestDate(e)}_dateAllowed(e,t=this.minDate,r=this.maxDate,o=this.isDateDisabled){return fe(e,t,r,o)}_isTodayAllowed(e,t,r){return this._dateAllowed(this._getTodayMidnight(),e,t,r)}_getTodayMidnight(){let e=new Date,t=new Date(0,0);return t.setFullYear(e.getFullYear()),t.setMonth(e.getMonth()),t.setDate(e.getDate()),t}};var es=class extends _n(v(I(g(b(_))))){static get is(){return"vaadin-date-picker-overlay-content"}static get styles(){return pn}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return u`
      <slot name="months"></slot>
      <slot name="years"></slot>

      <div role="toolbar" part="toolbar">
        <slot name="today-button"></slot>
        <div
          part="years-toggle-button"
          ?hidden="${this._desktopMode}"
          aria-hidden="true"
          @click="${this._toggleYearScroller}"
        >
          ${this._yearAfterXMonths(this._visibleMonthIndex)}
        </div>
        <slot name="cancel-button"></slot>
      </div>
    `}firstUpdated(){super.firstUpdated(),this.setAttribute("role","dialog"),this._initControllers()}};m(es);var mn=p`
  :host([opened]) {
    pointer-events: auto;
  }

  :host([week-numbers]) {
    --_vaadin-date-picker-week-numbers-visible: 1;
  }

  :host([dir='rtl']) [part='input-field'] {
    direction: ltr;
  }

  :host([dir='rtl']) [part='input-field'] ::slotted(input)::placeholder {
    direction: rtl;
    text-align: left;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-calendar);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }
`;var ct=new WeakMap,wi=new WeakMap,Ai={},ts=0,fn=s=>s?.nodeType===Node.ELEMENT_NODE,is=(...s)=>{console.error(`Error: ${s.join(" ")}. Skip setting aria-hidden.`)},pd=(s,i)=>fn(s)?i.map(e=>{if(!fn(e))return is(e,"is not a valid element"),null;let t=e;for(;t&&t!==s;){if(s.contains(t))return e;t=t.getRootNode().host}return is(e,"is not contained inside",s),null}).filter(e=>!!e):(is(s,"is not a valid element"),[]),_d=(s,i,e,t)=>{let r=pd(i,Array.isArray(s)?s:[s]);Ai[e]||(Ai[e]=new WeakMap);let o=Ai[e],n=[],a=new Set,l=new Set(r),d=c=>{if(!c||a.has(c))return;a.add(c);let f=c.assignedSlot;f&&d(f),d(c.parentNode||c.host)};r.forEach(d);let h=c=>{if(!c||l.has(c))return;let f=c.shadowRoot;(f?[...c.children,...f.children]:[...c.children]).forEach(w=>{if(!["template","script","style"].includes(w.localName))if(a.has(w))h(w);else{let S=w.getAttribute(t),Q=S!==null&&S!=="false",te=(ct.get(w)||0)+1,O=(o.get(w)||0)+1;ct.set(w,te),o.set(w,O),n.push(w),te===1&&Q&&wi.set(w,!0),O===1&&w.setAttribute(e,"true"),Q||w.setAttribute(t,"true")}})};return h(i),a.clear(),ts+=1,()=>{n.forEach(c=>{let f=ct.get(c)-1,y=o.get(c)-1;ct.set(c,f),o.set(c,y),f||(wi.has(c)?wi.delete(c):c.removeAttribute(t)),y||c.removeAttribute(e)}),ts-=1,ts||(ct=new WeakMap,ct=new WeakMap,wi=new WeakMap,Ai={})}},gn=(s,i=document.body,e="data-aria-hidden")=>{let t=Array.from(Array.isArray(s)?s:[s]);return i&&t.push(...Array.from(i.querySelectorAll("[aria-live]"))),_d(t,i,e,"aria-hidden")};var Cv="inert"in HTMLElement.prototype;function vn(s,...i){let e=o=>Array.isArray(o),t=o=>o&&typeof o=="object"&&!e(o),r=(o,n)=>{t(n)&&t(o)&&Object.keys(n).forEach(a=>{let l=n[a];t(l)?(o[a]||(o[a]={}),r(o[a],l)):e(l)?o[a]=[...l]:l!=null&&(o[a]=l)})};return i.forEach(o=>{r(s,o)}),s}var ee=s=>class extends s{static get properties(){return{i18n:{type:Object},__effectiveI18n:{type:Object,sync:!0}}}static get defaultI18n(){return{}}constructor(){super(),this.i18n=vn({},this.constructor.defaultI18n)}get i18n(){return this.__customI18n}set i18n(e){e!==this.__customI18n&&(this.__customI18n=e,this.__effectiveI18n=vn({},this.constructor.defaultI18n,this.__customI18n))}};var Ei=class{constructor(i){this.host=i,i.addEventListener("opened-changed",()=>{i.opened||this.__setVirtualKeyboardEnabled(!1)}),i.addEventListener("blur",()=>this.__setVirtualKeyboardEnabled(!0)),i.addEventListener("touchstart",()=>this.__setVirtualKeyboardEnabled(!0))}__setVirtualKeyboardEnabled(i){this.host.inputElement&&(this.host.inputElement.inputMode=i?"":"none")}};var Ii=Object.freeze({monthNames:["January","February","March","April","May","June","July","August","September","October","November","December"],weekdays:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],weekdaysShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],firstDayOfWeek:0,today:"Today",cancel:"Cancel",referenceDate:"",formatDate(s){let i=String(s.year).replace(/\d+/u,e=>"0000".substr(e.length)+e);return[s.month+1,s.day,i].join("/")},parseDate(s){let i=s.split("/"),e=new Date,t,r=e.getMonth(),o=e.getFullYear();if(i.length===3){if(r=parseInt(i[0])-1,t=parseInt(i[1]),o=parseInt(i[2]),i[2].length<3&&o>=0){let n=this.referenceDate?dt(this.referenceDate):new Date;o=tn(n,o,r,t)}}else i.length===2?(r=parseInt(i[0])-1,t=parseInt(i[1])):i.length===1&&(t=parseInt(i[0]));if(t!==void 0)return{day:t,month:r,year:o}},formatTitle:(s,i)=>`${s} ${i}`}),bn=s=>class extends ee(we(at(U(s)))){static get properties(){return{_selectedDate:{type:Object,sync:!0},_focusedDate:{type:Object,sync:!0},value:{type:String,notify:!0,value:"",sync:!0},initialPosition:{type:String},opened:{type:Boolean,reflectToAttribute:!0,notify:!0,observer:"_openedChanged",sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},_fullscreen:{type:Boolean,value:!1,sync:!0},_fullscreenMediaQuery:{value:"(max-width: 450px), (max-height: 450px)"},min:{type:String,sync:!0},max:{type:String,sync:!0},isDateDisabled:{type:Function},_minDate:{type:Date,computed:"__computeMinOrMaxDate(min)"},_maxDate:{type:Date,computed:"__computeMinOrMaxDate(max)"},_noInput:{type:Boolean,computed:"_isNoInput(inputElement, _fullscreen, _ios, __effectiveI18n, opened, autoOpenDisabled)"},_ios:{type:Boolean,value:Be},_focusOverlayOnOpen:Boolean,_overlayContent:{type:Object,sync:!0},__enteredDate:{type:Date,sync:!0}}}static get observers(){return["_selectedDateChanged(_selectedDate, __effectiveI18n)","_focusedDateChanged(_focusedDate, __effectiveI18n)","__updateOverlayContent(_overlayContent, __effectiveI18n, label, _minDate, _maxDate, _focusedDate, _selectedDate, showWeekNumbers, isDateDisabled, __enteredDate)","__updateOverlayContentTheme(_overlayContent, _theme)","__updateOverlayContentFullScreen(_overlayContent, _fullscreen)"]}static get defaultI18n(){return Ii}static get constraints(){return[...super.constraints,"min","max"]}constructor(){super(),this._boundOnClick=this._onClick.bind(this),this._boundOnScroll=this._onScroll.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get _inputElementValue(){return super._inputElementValue}set _inputElementValue(e){super._inputElementValue=e;let t=this.__parseDate(e);this.__setEnteredDate(t)}get __unparsableValue(){return!this._inputElementValue||this.__parseDate(this._inputElementValue)?"":this._inputElementValue}_onFocus(e){super._onFocus(e),this._noInput&&!B()&&e.target.blur()}_onBlur(e){super._onBlur(e),this.opened||(this.__commitParsedOrFocusedDate(),document.hasFocus()&&this._requestValidation())}ready(){super.ready(),this.addEventListener("click",this._boundOnClick),this.addController(new Te(this._fullscreenMediaQuery,e=>{this._fullscreen=e})),this.addController(new Ei(this)),this._overlayElement=this.$.overlay}updated(e){super.updated(e),(e.has("showWeekNumbers")||e.has("__effectiveI18n"))&&this.toggleAttribute("week-numbers",this.showWeekNumbers&&this.__effectiveI18n.firstDayOfWeek===1)}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}focus(e){this._noInput&&!B()?this.open():super.focus(e)}open(){!this.disabled&&!this.readonly&&(this.opened=!0)}close(){this.$.overlay.close()}__ensureContent(){if(this._overlayContent)return;let e=document.createElement("vaadin-date-picker-overlay-content");e.setAttribute("slot","overlay"),this.appendChild(e),this._overlayContent=e,e.addEventListener("close",()=>{this._close()}),e.addEventListener("focus-input",this._focusAndSelect.bind(this)),e.addEventListener("date-tap",t=>{this.__commitDate(t.detail.date),this._close()}),e.addEventListener("date-selected",t=>{this.__commitDate(t.detail.date)}),e.addEventListener("focusin",()=>{this._keyboardActive&&this._setFocused(!0)}),e.addEventListener("focusout",t=>{this._shouldRemoveFocus(t)&&this._setFocused(!1)}),e.addEventListener("focused-date-changed",t=>{this._focusedDate=t.detail.value}),e.addEventListener("click",t=>t.stopPropagation())}__parseDate(e){if(!this.__effectiveI18n.parseDate)return;let t=this.__effectiveI18n.parseDate(e);if(t&&(t=dt(`${t.year}-${t.month+1}-${t.day}`)),t&&!isNaN(t.getTime()))return t}__formatDate(e){if(this.__effectiveI18n.formatDate)return this.__effectiveI18n.formatDate(Yr(e))}checkValidity(){let e=this._inputElementValue,t=!e||!!this._selectedDate&&e===this.__formatDate(this._selectedDate),r=!this._selectedDate||fe(this._selectedDate,this._minDate,this._maxDate,this.isDateDisabled),o=!0;return this.inputElement&&this.inputElement.checkValidity&&(o=this.inputElement.checkValidity()),t&&r&&o}_shouldSetFocus(e){return!this._shouldKeepFocusRing}_shouldKeepFocusOnClearMousedown(){return this.opened?!0:super._shouldKeepFocusOnClearMousedown()}_shouldRemoveFocus(e){let{relatedTarget:t}=e;return this.opened&&t!==null&&t!==document.body&&!this.contains(t)&&!this._overlayContent.contains(t)?!0:!this.opened}_setFocused(e){super._setFocused(e),this._shouldKeepFocusRing=e&&this._keyboardActive}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__commitDate(e){this.__keepCommittedValue=!0,this._selectedDate=e,this.__keepCommittedValue=!1,this.__commitValueChange()}_close(){this._focus(),this.close()}_isNoInput(e,t,r,o,n,a){return!e||t&&(!a||n)||r&&n||!o.parseDate}_formatISO(e){return on(e)}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.autocomplete="off",e.setAttribute("role","combobox"),e.setAttribute("aria-haspopup","dialog"),e.setAttribute("aria-expanded",!!this.opened),this._applyInputValue(this._selectedDate))}_openedChanged(e){e&&this.__ensureContent(),this.inputElement&&this.inputElement.setAttribute("aria-expanded",e)}_selectedDateChanged(e,t){e===void 0||t===void 0||(this.__keepInputValue||this._applyInputValue(e),this.value=this._formatISO(e),this._ignoreFocusedDateChange=!0,this._focusedDate=e,this._ignoreFocusedDateChange=!1)}_focusedDateChanged(e,t){e===void 0||t===void 0||!this._ignoreFocusedDateChange&&!this._noInput&&this._applyInputValue(e)}_valueChanged(e,t){let r=dt(e);if(e&&!r){this.value=t;return}e?$(this._selectedDate,r)||(this._selectedDate=r,t!==void 0&&this._requestValidation()):this._selectedDate=null,this.__keepCommittedValue||(this.__committedValue=this.value,this.__committedUnparsableValue=""),this._toggleHasValue(this._hasValue)}__updateOverlayContent(e,t,r,o,n,a,l,d,h,c){e&&(e.i18n=t,e.label=r,e.minDate=o,e.maxDate=n,e.focusedDate=a,e.selectedDate=l,e.showWeekNumbers=d,e.isDateDisabled=h,e.enteredDate=c)}__updateOverlayContentTheme(e,t){e&&(t?e.setAttribute("theme",t):e.removeAttribute("theme"))}__updateOverlayContentFullScreen(e,t){e&&e.toggleAttribute("fullscreen",t)}_onOverlayEscapePress(e){e.stopPropagation(),this._focusedDate=this._selectedDate,this._applyInputValue(this._selectedDate),this._close()}_onOverlayOpened(){let e=this._overlayContent;e.reset();let t=this._getInitialPosition();e.initialPosition=t;let r=e.focusedDate||t;e.scrollToDate(r),this._ignoreFocusedDateChange=!0,e.focusedDate=r,this._ignoreFocusedDateChange=!1,window.addEventListener("scroll",this._boundOnScroll,!0),this._focusOverlayOnOpen?(e.focusDateElement(),this._focusOverlayOnOpen=!1):this._focus();let o=this.inputElement;this._noInput&&o&&(o.blur(),this._overlayContent.focusDateElement());let n=this._noInput?e:this;this.__showOthers=gn(n)}_getInitialPosition(){let e=dt(this.initialPosition),t=this._selectedDate||this._overlayContent.initialPosition||e||new Date;return e||fe(t,this._minDate,this._maxDate,this.isDateDisabled)?t:this._minDate||this._maxDate?xi(t,[this._minDate,this._maxDate]):new Date}__commitParsedOrFocusedDate(){if(this._ignoreFocusedDateChange=!0,this.__effectiveI18n.parseDate){let e=this._inputElementValue||"",t=this.__parseDate(e);t?this.__commitDate(t):(this.__keepInputValue=!0,this.__commitDate(null),this.__keepInputValue=!1)}else this._focusedDate&&this.__commitDate(this._focusedDate);this._ignoreFocusedDateChange=!1}_onOverlayClosed(){this.__showOthers&&(this.__showOthers(),this.__showOthers=null),window.removeEventListener("scroll",this._boundOnScroll,!0),this.__commitParsedOrFocusedDate(),this.inputElement&&this.inputElement.selectionStart&&(this.inputElement.selectionStart=this.inputElement.selectionEnd),!this.value&&!this._keyboardActive&&this._requestValidation()}_onScroll(e){(e.target===window||!this._overlayContent.contains(e.target))&&this._overlayContent._repositionYearScroller()}_focus(){this._noInput||this.inputElement.focus()}_focusAndSelect(){this._focus(),this._setSelectionRange(0,this._inputElementValue.length)}_applyInputValue(e){this._inputElementValue=e?this.__formatDate(e):""}_setSelectionRange(e,t){this.inputElement&&this.inputElement.setSelectionRange(e,t)}_onChange(e){e.stopPropagation()}_onClick(e){e.composedPath().includes(this._overlayElement)||this._isClearButton(e)||this._onHostClick(e)}_onHostClick(e){(!this.autoOpenDisabled||this._noInput)&&(e.preventDefault(),this.open())}_onClearButtonClick(e){e.preventDefault(),this.__commitDate(null)}_onKeyDown(e){switch(super._onKeyDown(e),this._noInput&&["Tab","Escape"].indexOf(e.key)===-1&&e.preventDefault(),e.key){case"ArrowDown":case"ArrowUp":e.preventDefault(),this.opened?this._overlayContent.focusDateElement():(this._focusOverlayOnOpen=!0,this.open());break;case"Tab":this.opened&&(e.preventDefault(),e.stopPropagation(),this._setSelectionRange(0,0),e.shiftKey?this._overlayContent.focusCancel():this._overlayContent.focusDateElement());break;default:break}}_onEnter(e){e.composedPath().includes(this._overlayContent)||(this.opened?this.close():this.__commitParsedOrFocusedDate())}_onEscape(e){if(this.opened){this._onOverlayEscapePress(e);return}if(this.clearButtonVisible&&this.value&&!this.readonly){e.stopPropagation(),this._onClearButtonClick(e);return}this.inputElement.value===""?this.__commitDate(null):this._applyInputValue(this._selectedDate)}_isClearButton(e){return e.composedPath()[0]===this.clearElement}_onInput(){!this.opened&&this._inputElementValue&&!this.autoOpenDisabled&&this.open();let e=this.__parseDate(this._inputElementValue||"");e&&(this._ignoreFocusedDateChange=!0,$(e,this._focusedDate)||(this._focusedDate=e),this._ignoreFocusedDateChange=!1),this.__setEnteredDate(e)}__setEnteredDate(e){e?$(this.__enteredDate,e)||(this.__enteredDate=e):this.__enteredDate=null}__computeMinOrMaxDate(e){return dt(e)}};var rs=class extends bn(de(v(A(g(b(_)))))){static get is(){return"vaadin-date-picker"}static get styles(){return[N,mn]}static get properties(){return{_positionTarget:{type:Object,sync:!0}}}get clearElement(){return this.$.clearButton}render(){return u`
      <div class="vaadin-date-picker-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div part="field-button toggle-button" slot="suffix" aria-hidden="true" @click="${this._toggle}"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-date-picker-overlay
        id="overlay"
        .owner="${this}"
        ?fullscreen="${this._fullscreen}"
        theme="${k(this._theme)}"
        .opened="${this.opened}"
        @opened-changed="${this._onOpenedChanged}"
        @vaadin-overlay-open="${this._onOverlayOpened}"
        @vaadin-overlay-close="${this._onVaadinOverlayClose}"
        @vaadin-overlay-closing="${this._onOverlayClosed}"
        restore-focus-on-close
        no-vertical-overlap
        exportparts="backdrop, overlay, content"
        .restoreFocusNode="${this.inputElement}"
        .positionTarget="${this._positionTarget}"
      >
        <slot name="overlay"></slot>
      </vaadin-date-picker-overlay>
    `}ready(){super.ready(),this.addController(new W(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new V(this.inputElement,this._labelController)),this._tooltipController=new D(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._positionTarget=this.shadowRoot.querySelector('[part="input-field"]'),this.shadowRoot.querySelector('[part="field-button toggle-button"]').addEventListener("mousedown",e=>e.preventDefault())}_onOpenedChanged(i){this.opened=i.detail.value}_onVaadinOverlayClose(i){let e=i.detail.sourceEvent;e?.composedPath().includes(this)&&!e.composedPath().includes(this._overlayElement)&&i.preventDefault()}_toggle(i){i.stopPropagation(),this.$.overlay.opened?this.close():this.open()}};m(rs);var ss=class extends tt(v(I(g(b(_))))){static get is(){return"vaadin-time-picker-item"}static get styles(){return[Se,et]}render(){return u`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(ss);var yn=p`
  :host {
    --vaadin-item-checkmark-display: block;
  }

  #overlay {
    width: var(--vaadin-time-picker-overlay-width, var(--_vaadin-time-picker-overlay-default-width, auto));
  }

  [part='content'] {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
`;var os=class extends st(J(I(v(g(b(_)))))){static get is(){return"vaadin-time-picker-overlay"}static get styles(){return[Z,yn]}render(){return u`
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}};m(os);var ns=class extends nt(g(_)){static get is(){return"vaadin-time-picker-scroller"}static get styles(){return Ve}render(){return u`
      <div id="selector">
        <slot></slot>
      </div>
    `}};m(ns);var xn=p`
  :host([opened]) {
    pointer-events: auto;
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-clock);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }

  /* See https://github.com/vaadin/vaadin-time-picker/issues/145 */
  :host([dir='rtl']) [part='input-field'] {
    direction: ltr;
  }

  :host([dir='rtl']) [part='input-field'] ::slotted(input)::placeholder {
    direction: rtl;
    text-align: left;
  }
`;function ut(s){if(!s)return"";let i=(t=0,r="00")=>(r+t).substr((r+t).length-r.length),e=`${i(s.hours)}:${i(s.minutes)}`;return s.seconds!==void 0&&(e+=`:${i(s.seconds)}`),s.milliseconds!==void 0&&(e+=`.${i(s.milliseconds,"000")}`),e}var md="(\\d|[0-1]\\d|2[0-3])",Cn="(\\d|[0-5]\\d)",fd=Cn,gd="(\\d{1,3})",vd=new RegExp(`^${md}(?::${Cn}(?::${fd}(?:\\.${gd})?)?)?$`,"u");function ae(s){let i=vd.exec(s);if(i){if(i[4])for(;i[4].length<3;)i[4]+="0";return{hours:i[1],minutes:i[2],seconds:i[3],milliseconds:i[4]}}}function bd(s){let i=s==null?60:parseFloat(s);if(i%3600===0)return 1;if(i%60===0||!i)return 2;if(i%1===0)return 3;if(i<1)return 4}function he(s,i){if(!s)return s;let e=bd(i);return{...s,hours:parseInt(s.hours),minutes:parseInt(s.minutes||0),seconds:e<3?void 0:parseInt(s.seconds||0),milliseconds:e<4?void 0:parseInt(s.milliseconds||0)}}var Si=Object.freeze({formatTime:ut,parseTime:ae}),wn="00:00:00.000",An="23:59:59.999",En=s=>class extends ee(pi(vi(de(s)))){static get properties(){return{value:{type:String,notify:!0,value:"",sync:!0},min:{type:String,value:"",sync:!0},max:{type:String,value:"",sync:!0},step:{type:Number,sync:!0},_comboBoxValue:{type:String,sync:!0,observer:"__comboBoxValueChanged"},_inputContainer:{type:Object}}}static get observers(){return["_openedOrItemsChanged(opened, _dropdownItems)","_updateScroller(opened, _dropdownItems, _focusedIndex, _theme, _comboBoxValue)","__updateAriaAttributes(_dropdownItems, opened, inputElement)","__updateDropdownItems(__effectiveI18n, min, max, step)"]}static get defaultI18n(){return Si}static get constraints(){return[...super.constraints,"min","max"]}get _tagNamePrefix(){return"vaadin-time-picker"}get clearElement(){return this.$.clearButton}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __unparsableValue(){return this._inputElementValue&&!this.__effectiveI18n.parseTime(this._inputElementValue)?this._inputElementValue:""}ready(){super.ready(),this.addController(new W(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e},{uniqueIdPrefix:"search-input"})),this.addController(new V(this.inputElement,this._labelController)),this._inputContainer=this.shadowRoot.querySelector('[part~="input-field"]'),this._toggleElement=this.$.toggleButton,this._tooltipController=new D(this),this._tooltipController.setShouldShow(e=>!e.opened),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}checkValidity(){return!!(this.inputElement.checkValidity()&&(!this.value||this._timeAllowed(ae(this.value)))&&(!this._comboBoxValue||this.__effectiveI18n.parseTime(this._comboBoxValue)))}_getItemLabel(e){return e?e.label:""}_updateScroller(e,t,r,o,n){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh"),this._scroller.setProperties({items:e?t:[],opened:e,focusedIndex:r,theme:o,selectedItem:t?.find(a=>a.value===n)})}_openedOrItemsChanged(e,t){this._overlayOpened=e&&!!t?.length}_onClosed(){this._commitValue()}_onEscapeCancel(){this._inputElementValue=this._comboBoxValue,this._closeOrCommit()}_onClearAction(){this._comboBoxValue="",this._inputElementValue="",this.__commitValueChange()}_commitValue(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex],t=this._getItemLabel(e);this._inputElementValue=t,this._comboBoxValue=t,this._focusedIndex=-1}else this._inputElementValue===""||this._inputElementValue===void 0?this._comboBoxValue="":this._comboBoxValue=this._inputElementValue;this.__commitValueChange(),this._clearSelectionRange()}_closeOrCommit(){this.opened?this.close():this._commitValue()}_revertInputValue(){this._inputElementValue=this._comboBoxValue,this._clearSelectionRange()}_setFocused(e){super._setFocused(e),!e&&!this._closeOnBlurIsPrevented&&document.hasFocus()&&this._requestValidation()}__validDayDivisor(e){return!e||24*3600%e===0||e<1&&e%1*1e3%1===0}_onKeyDown(e){if(super._onKeyDown(e),this.readonly||this.disabled||this._dropdownItems.length)return;let t=this.__validDayDivisor(this.step)&&this.step||60;e.keyCode===40?this.__onArrowPressWithStep(-t):e.keyCode===38&&this.__onArrowPressWithStep(t)}__onArrowPressWithStep(e){let t=this.__addStep(this.__getMsec(this.__memoValue),e,!0);this.__memoValue=t,this.__useMemo=!0,this._comboBoxValue=this.__effectiveI18n.formatTime(t),this.__useMemo=!1,this.__commitValueChange()}__commitValueChange(){let e=this.__unparsableValue;this.__committedValue!==this.value?(this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))):this.__committedUnparsableValue!==e&&(this._requestValidation(),this.dispatchEvent(new CustomEvent("unparsable-change"))),this.__committedValue=this.value,this.__committedUnparsableValue=e}__getMsec(e){let t=(e?.hours||0)*60*60*1e3;return t+=(e?.minutes||0)*60*1e3,t+=(e?.seconds||0)*1e3,t+=parseInt(e?.milliseconds)||0,t}__getSec(e){let t=(e?.hours||0)*60*60;return t+=(e?.minutes||0)*60,t+=e?.seconds||0,t+=(e?.milliseconds||0)/1e3,t}__addStep(e,t,r){e===0&&t<0&&(e=1440*60*1e3);let o=t*1e3,n=e%o;o<0&&n&&r?e-=n:o>0&&n&&r?e-=n-o:e+=o;let a=Math.floor(e/1e3/60/60);e-=a*1e3*60*60;let l=Math.floor(e/1e3/60);e-=l*1e3*60;let d=Math.floor(e/1e3);return e-=d*1e3,{hours:a<24?a:0,minutes:l,seconds:d,milliseconds:e}}__updateDropdownItems(e,t,r,o){let n=he(ae(t||wn),o),a=this.__getSec(n),l=he(ae(r||An),o),d=this.__getSec(l);this._dropdownItems=this.__generateDropdownList(a,d,o);let h=he(ae(this.value),o);o!==this.__oldStep&&(this.__oldStep=o,this.__updateValue(h)),this.value&&(this._comboBoxValue=e.formatTime(h))}__updateAriaAttributes(e,t,r){e===void 0||r===void 0||(e.length===0?(r.removeAttribute("role"),r.removeAttribute("aria-expanded")):(r.setAttribute("role","combobox"),r.setAttribute("aria-expanded",!!t)))}__generateDropdownList(e,t,r){if(r<900||!this.__validDayDivisor(r))return[];let o=[];r||(r=3600);let n=-r+e;for(;n+r>=e&&n+r<=t;){let a=he(this.__addStep(n*1e3,r),r);n+=r;let l=this.__effectiveI18n.formatTime(a);o.push({label:l,value:l})}return o}_valueChanged(e,t){let r=this.__memoValue=ae(e),o=ut(r)||"";this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),e!==""&&e!==null&&!r?this.value=t??"":e!==o?this.value=o:this.__keepInvalidInput?delete this.__keepInvalidInput:this.__updateInputValue(r),this._toggleHasValue(this._hasValue)}__comboBoxValueChanged(e,t){if(e===""&&t===void 0)return;let r=this.__useMemo?this.__memoValue:this.__effectiveI18n.parseTime(e),o=he(r,this.step),n=this.__effectiveI18n.formatTime(o)||"";o?e!==n?this._comboBoxValue=n:(this.__keepCommittedValue=!0,this.__updateValue(o),this.__keepCommittedValue=!1):(this.value!==""&&e!==""&&(this.__keepInvalidInput=!0),this.__keepCommittedValue=!0,this.value="",this.__keepCommittedValue=!1)}__updateValue(e){let t=ut(he(e,this.step))||"";this.value=t,this.__updateInputValue(e)}__updateInputValue(e){let t=this.__effectiveI18n.formatTime(he(e,this.step))||"";this._inputElementValue=t,this._comboBoxValue=t}_timeAllowed(e){let t=ae(this.min||wn),r=ae(this.max||An);return(!t||this.__getMsec(e)>=this.__getMsec(t))&&(!r||this.__getMsec(e)<=this.__getMsec(r))}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this._scroller.requestContentUpdate()}_onHostClick(e){let t=e.composedPath();(t.includes(this._labelNode)||t.includes(this._inputContainer))&&super._onHostClick(e)}_onChange(e){e.stopPropagation()}};var as=class extends En(v(A(g(b(_))))){static get is(){return"vaadin-time-picker"}static get styles(){return[N,xn]}render(){return u`
      <div class="vaadin-time-picker-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-time-picker-overlay
        id="overlay"
        dir="ltr"
        .owner="${this}"
        .opened="${this._overlayOpened}"
        theme="${k(this._theme)}"
        .positionTarget="${this._inputContainer}"
        no-vertical-overlap
        exportparts="overlay, content"
      >
        <slot name="overlay"></slot>
      </vaadin-time-picker-overlay>
    `}};m(as);var In=p`
  .vaadin-date-time-picker-container {
    width: calc(var(--vaadin-field-default-width, 12em) * 2 + var(--vaadin-date-time-picker-gap, var(--vaadin-gap-s)));
  }

  [part='input-fields'] {
    display: flex;
    gap: var(--vaadin-date-time-picker-gap, var(--vaadin-gap-s));
  }

  [part='input-fields'] ::slotted([slot='date-picker']) {
    min-width: 0;
    flex: 1 1 auto;
  }

  [part='input-fields'] ::slotted([slot='time-picker']) {
    min-width: 0;
    flex: 1 1.65 auto;
  }
`;var yd=Object.keys(Ii),xd=Object.keys(Si),Cd={...Ii,...Si},Ti=class extends T{constructor(i,e){super(i,`${e}-picker`,`vaadin-${e}-picker`,{initializer:(t,r)=>{let o=`__${e}Picker`;r[o]=t}})}},Sn=s=>class extends ee(Ie(j(le(s)))){static get properties(){return{name:{type:String},value:{type:String,notify:!0,value:"",observer:"__valueChanged",sync:!0},min:{type:String,observer:"__minChanged",sync:!0},max:{type:String,observer:"__maxChanged",sync:!0},__minDateTime:{type:Date,value:"",sync:!0},__maxDateTime:{type:Date,value:"",sync:!0},datePlaceholder:{type:String,sync:!0},timePlaceholder:{type:String,sync:!0},step:{type:Number,sync:!0},initialPosition:{type:String,sync:!0},showWeekNumbers:{type:Boolean,value:!1,sync:!0},autoOpenDisabled:{type:Boolean,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autofocus:{type:Boolean},__selectedDateTime:{type:Date,sync:!0},__datePicker:{type:Object,sync:!0,observer:"__datePickerChanged"},__timePicker:{type:Object,sync:!0,observer:"__timePickerChanged"}}}static get observers(){return["__selectedDateTimeChanged(__selectedDateTime)","__datePlaceholderChanged(datePlaceholder, __datePicker)","__timePlaceholderChanged(timePlaceholder, __timePicker)","__stepChanged(step, __timePicker)","__initialPositionChanged(initialPosition, __datePicker)","__showWeekNumbersChanged(showWeekNumbers, __datePicker)","__requiredChanged(required, __datePicker, __timePicker)","__invalidChanged(invalid, __datePicker, __timePicker)","__disabledChanged(disabled, __datePicker, __timePicker)","__readonlyChanged(readonly, __datePicker, __timePicker)","__i18nChanged(__effectiveI18n, __datePicker, __timePicker)","__autoOpenDisabledChanged(autoOpenDisabled, __datePicker, __timePicker)","__themeChanged(_theme, __datePicker, __timePicker)","__pickersChanged(__datePicker, __timePicker)","__labelOrAccessibleNameChanged(label, accessibleName, __effectiveI18n, __datePicker, __timePicker)"]}static get defaultI18n(){return Cd}constructor(){super(),this.__defaultDateMinMaxValue=void 0,this.__defaultTimeMinValue="00:00:00.000",this.__defaultTimeMaxValue="23:59:59.999",this.__onGlobalClick=this.__onGlobalClick.bind(this),this.__changeEventHandler=this.__changeEventHandler.bind(this),this.__valueChangedEventHandler=this.__valueChangedEventHandler.bind(this),this.__openedChangedEventHandler=this.__openedChangedEventHandler.bind(this)}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __pickers(){return[this.__datePicker,this.__timePicker]}get __filledPickers(){return this.__pickers.filter(e=>e.value||e.__unparsableValue)}get __formattedValue(){let e=this.__pickers.map(t=>t.value);return e.every(Boolean)?e.join("T"):""}get __unparsableValue(){return this.__filledPickers.length>0&&!this.__pickers.every(e=>e.value)?this.__pickers.map(e=>e.value||e.__unparsableValue).join("T"):""}ready(){super.ready(),this._datePickerController=new Ti(this,"date"),this.addController(this._datePickerController),this._timePickerController=new Ti(this,"time"),this.addController(this._timePickerController),this.autofocus&&!this.disabled&&window.requestAnimationFrame(()=>this.focus()),this.setAttribute("role","group"),this._tooltipController=new D(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setShouldShow(e=>e.__datePicker&&!e.__datePicker.opened&&e.__timePicker&&!e.__timePicker.opened),this.ariaTarget=this}connectedCallback(){super.connectedCallback(),document.addEventListener("click",this.__onGlobalClick,!0)}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("click",this.__onGlobalClick,!0)}focus(e){this.__datePicker&&this.__datePicker.focus(e)}__onGlobalClick(e){if(!(this.__datePicker.opened||this.__timePicker.opened))return;e.composedPath().every(o=>![this.__datePicker,this.__datePicker.$.overlay,this.__timePicker,this.__timePicker.$.overlay].includes(o))&&(this.__outsideClickInProgress=!0,setTimeout(()=>{this.__outsideClickInProgress=!1}))}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this.__commitPendingValueChange()}_shouldRemoveFocus(e){let t=e.relatedTarget;return!(this.__datePicker.opened||this.__timePicker.opened||this.__datePicker.contains(t)||this.__timePicker.contains(t))}__syncI18n(e,t,r){let o={};r.forEach(n=>{t?.hasOwnProperty(n)&&(o[n]=t[n])}),e.i18n=o}__changeEventHandler(e){e.stopPropagation();let t=this.invalid,r=this.__filledPickers;r.length===1&&r[0].checkValidity()&&!t||this.__hasPendingValueChange&&this.__commitPendingValueChange()}__openedChangedEventHandler(){let e=this.__datePicker.opened||this.__timePicker.opened;this.style.pointerEvents=e?"auto":"",!e&&this.__outsideClickInProgress&&this.__commitPendingValueChange()}__addInputListeners(e){e.addEventListener("change",this.__changeEventHandler),e.addEventListener("unparsable-change",this.__changeEventHandler),e.addEventListener("value-changed",this.__valueChangedEventHandler),e.addEventListener("opened-changed",this.__openedChangedEventHandler)}__removeInputListeners(e){e.removeEventListener("change",this.__changeEventHandler),e.removeEventListener("unparsable-change",this.__changeEventHandler),e.removeEventListener("value-changed",this.__valueChangedEventHandler),e.removeEventListener("opened-changed",this.__openedChangedEventHandler)}__isDefaultPicker(e,t){let r=this[`_${t}PickerController`];return r&&e===r.defaultNode}__datePickerChanged(e,t){e&&(t&&(this.__removeInputListeners(t),t.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"date")||(this.datePlaceholder=e.placeholder,this.initialPosition=e.initialPosition,this.showWeekNumbers=e.showWeekNumbers),e.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue),e.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue),e.manualValidation=!0)}__timePickerChanged(e,t){e&&(t&&(this.__removeInputListeners(t),t.remove()),this.__addInputListeners(e),this.__isDefaultPicker(e,"time")||(this.timePlaceholder=e.placeholder,this.step=e.step),this.__updateTimePickerMinMax(),e.manualValidation=!0)}__updateTimePickerMinMax(){if(this.__timePicker&&this.__datePicker){let e=this.__parseDate(this.__datePicker.value),t=$(this.__minDateTime,this.__maxDateTime,Mt);this.__minDateTime&&$(e,this.__minDateTime,Mt)||t?this.__timePicker.min=this.__dateToIsoTimeString(this.__minDateTime):this.__timePicker.min=this.__defaultTimeMinValue,this.__maxDateTime&&$(e,this.__maxDateTime,Mt)||t?this.__timePicker.max=this.__dateToIsoTimeString(this.__maxDateTime):this.__timePicker.max=this.__defaultTimeMaxValue}}__i18nChanged(e,t,r){t&&this.__isDefaultPicker(t,"date")&&this.__syncI18n(t,e,yd),r&&this.__isDefaultPicker(r,"time")&&this.__syncI18n(r,e,xd)}__labelOrAccessibleNameChanged(e,t,r,o,n){let a=t||e||"";o&&(o.accessibleName=`${a} ${r.dateLabel||""}`.trim()),n&&(n.accessibleName=`${a} ${r.timeLabel||""}`.trim())}__datePlaceholderChanged(e,t){t&&(t.placeholder=e)}__timePlaceholderChanged(e,t){t&&(t.placeholder=e)}__stepChanged(e,t){t&&t.step!==e&&(t.step=e)}__initialPositionChanged(e,t){t&&(t.initialPosition=e)}__showWeekNumbersChanged(e,t){t&&(t.showWeekNumbers=e)}__invalidChanged(e,t,r){t&&(t.invalid=e),r&&(r.invalid=e)}__requiredChanged(e,t,r){t&&(t.required=e),r&&(r.required=e),this.__oldRequired&&!e&&this._requestValidation(),this.__oldRequired=e}__disabledChanged(e,t,r){t&&(t.disabled=e),r&&(r.disabled=e)}__readonlyChanged(e,t,r){t&&(t.readonly=e),r&&(r.readonly=e)}__parseDate(e){return rn(e)}__formatDateISO(e,t){return e?nn(e):t}__parseDateTime(e){let[t,r]=e.split("T");if(!(t&&r))return;let o=this.__parseDate(t);if(!o)return;let n=ae(r);if(n)return o.setUTCHours(parseInt(n.hours)),o.setUTCMinutes(parseInt(n.minutes||0)),o.setUTCSeconds(parseInt(n.seconds||0)),o.setUTCMilliseconds(parseInt(n.milliseconds||0)),o}__formatDateTime(e){if(!e)return"";let t=this.__formatDateISO(e,""),r=this.__dateToIsoTimeString(e);return`${t}T${r}`}__dateToIsoTimeString(e){return ut(he({hours:e.getUTCHours(),minutes:e.getUTCMinutes(),seconds:e.getUTCSeconds(),milliseconds:e.getUTCMilliseconds()},this.step))}checkValidity(){let e=this.__pickers.some(o=>!o.checkValidity()),t=this.__filledPickers.length===1,r=this.required&&this.__pickers.some(o=>!o.value);return!e&&!r&&!t}__commitPendingValueChange(){this._requestValidation(),this.__committedValue!==this.value?this.dispatchEvent(new CustomEvent("change",{bubbles:!0})):this.__committedUnparsableValue!==this.__unparsableValue&&this.dispatchEvent(new CustomEvent("unparsable-change")),this.__committedValue=this.value,this.__committedUnparsableValue=this.__unparsableValue}get __hasPendingValueChange(){return this.__committedValue!==this.value||this.__committedUnparsableValue!==this.__unparsableValue}__dateTimeEquals(e,t){return $(e,t,Mt)?e.getUTCHours()===t.getUTCHours()&&e.getUTCMinutes()===t.getUTCMinutes()&&e.getUTCSeconds()===t.getUTCSeconds()&&e.getUTCMilliseconds()===t.getUTCMilliseconds():!1}__handleDateTimeChange(e,t,r,o){if(!r){this[e]="",this[t]="";return}let n=this.__parseDateTime(r);if(!n){this[e]=o;return}this.__dateTimeEquals(this[t],n)||(this[t]=n)}__valueChanged(e,t){this.__handleDateTimeChange("value","__selectedDateTime",e,t),this.__keepCommittedValue||(this.__committedValue=e,this.__committedUnparsableValue=""),this.toggleAttribute("has-value",!!e),this.__updateTimePickerMinMax()}__dispatchChange(){this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__minChanged(e,t){this.__handleDateTimeChange("min","__minDateTime",e,t),this.__datePicker&&(this.__datePicker.min=this.__formatDateISO(this.__minDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__maxChanged(e,t){this.__handleDateTimeChange("max","__maxDateTime",e,t),this.__datePicker&&(this.__datePicker.max=this.__formatDateISO(this.__maxDateTime,this.__defaultDateMinMaxValue)),this.__updateTimePickerMinMax(),this.__datePicker&&this.__timePicker&&this.value&&this._requestValidation()}__selectedDateTimeChanged(e){let t=this.__formatDateTime(e);if(this.value!==t&&(this.value=t),!!(this.__datePicker&&this.__datePicker.$)&&!this.__ignoreInputValueChange){this.__ignoreInputValueChange=!0;let[o,n]=this.value.split("T");this.__datePicker.value=o||"",this.__timePicker.value=n||"",this.__ignoreInputValueChange=!1}}__valueChangedEventHandler(){this.__ignoreInputValueChange||(this.__ignoreInputValueChange=!0,this.__keepCommittedValue=!0,this.__updateTimePickerMinMax(),this.value=this.__formattedValue,this.__keepCommittedValue=!1,this.__ignoreInputValueChange=!1)}__autoOpenDisabledChanged(e,t,r){t&&(t.autoOpenDisabled=e),r&&(r.autoOpenDisabled=e)}__themeChanged(e,t,r){!t||!r||[t,r].forEach(o=>{e?o.setAttribute("theme",e):o.removeAttribute("theme")})}__pickersChanged(e,t){!e||!t||this.__isDefaultPicker(e,"date")===this.__isDefaultPicker(t,"time")&&(e.value?this.__valueChangedEventHandler():this.value&&(this.__selectedDateTimeChanged(this.__selectedDateTime),(this.min&&this.__minDateTime||this.max&&this.__maxDateTime)&&this._requestValidation()))}};var ls=class extends Sn(v(A(g(b(_))))){static get is(){return"vaadin-date-time-picker"}static get styles(){return[N,In]}render(){return u`
      <div class="vaadin-date-time-picker-container">
        <div part="label" @click="${this.focus}">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true"></span>
        </div>

        <div part="input-fields">
          <slot name="date-picker" id="dateSlot"></slot>
          <slot name="time-picker" id="timeSlot"></slot>
        </div>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>

      <slot name="tooltip"></slot>
    `}};m(ls);var wd=p`
  /* Optical centering */
  :host::before,
  :host::after {
    content: '';
    flex-basis: 0;
    flex-grow: 1;
  }

  :host::after {
    flex-grow: 1.1;
  }

  :host {
    cursor: default;
    --_overflow-indicator-height: var(--vaadin-dialog-overflow-indicator-height, 1px);
    --_overflow-indicator-color: var(--vaadin-dialog-overflow-indicator-color, var(--vaadin-border-color-secondary));
  }

  [part='overlay']:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  [part='overlay'] {
    color: var(--vaadin-dialog-text-color, var(--vaadin-overlay-text-color, var(--vaadin-text-color)));
    background: var(--vaadin-dialog-background, var(--vaadin-overlay-background, var(--vaadin-background-color)));
    background-origin: border-box;
    border: var(--vaadin-dialog-border-width, var(--vaadin-overlay-border-width, 1px)) solid
      var(--vaadin-dialog-border-color, var(--vaadin-overlay-border-color, var(--vaadin-border-color-secondary)));
    box-shadow: var(--vaadin-dialog-shadow, var(--vaadin-overlay-shadow, 0 8px 24px -4px rgba(0, 0, 0, 0.3)));
    border-radius: var(--vaadin-dialog-border-radius, var(--vaadin-radius-l));
    width: max-content;
    min-width: min(var(--vaadin-dialog-min-width, 4em), 100%);
    max-width: min(var(--vaadin-dialog-max-width, 100%), 100%);
    max-height: 100%;
  }

  [part='header'],
  [part='header-content'],
  [part='footer'] {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    flex: none;
    pointer-events: none;
    z-index: 1;
    gap: var(--vaadin-dialog-toolbar-gap, var(--vaadin-gap-s));
  }

  ::slotted(*) {
    pointer-events: auto;
  }

  [part='header'],
  [part='content'],
  [part='footer'] {
    padding: var(--vaadin-dialog-padding, var(--vaadin-padding-l));
  }

  :host([theme~='no-padding']) [part='content'] {
    padding: 0 !important;
  }

  :host(:is([has-header], [has-title])) [part='content'] {
    padding-top: 0;
  }

  :host([has-footer]) [part='content'] {
    padding-bottom: 0;
  }

  [part='header'] {
    flex-wrap: nowrap;
  }

  ::slotted([slot='header-content']),
  ::slotted([slot='title']),
  ::slotted([slot='footer']) {
    display: contents;
  }

  ::slotted([slot='title']) {
    font: inherit !important;
    color: inherit !important;
    overflow-wrap: anywhere;
  }

  [part='title'] {
    color: var(--vaadin-dialog-title-color, var(--vaadin-text-color));
    font-weight: var(--vaadin-dialog-title-font-weight, 600);
    font-size: var(--vaadin-dialog-title-font-size, 1em);
    line-height: var(--vaadin-dialog-title-line-height, inherit);
  }

  [part='header-content'] {
    flex: 1;
  }

  :host([has-title]) [part='header-content'],
  [part='footer'] {
    justify-content: flex-end;
  }

  :host(:not([has-title]):not([has-header])) [part='header'],
  :host(:not([has-header])) [part='header-content'],
  :host(:not([has-title])) [part='title'],
  :host(:not([has-footer])) [part='footer'] {
    display: none !important;
  }

  [part='header'],
  [part='footer'] {
    position: relative;

    &::after {
      content: '';
      opacity: 0;
      position: absolute;
      pointer-events: none;
      height: var(--_overflow-indicator-height);
      top: 100%;
      inset-inline: 0;
      background: linear-gradient(
        var(--_overflow-indicator-dir, to bottom),
        var(--_overflow-indicator-color),
        var(--_overflow-indicator-color) 1px,
        transparent
      );
    }
  }

  [part='footer']::after {
    top: auto;
    bottom: 100%;
    --_overflow-indicator-dir: to top;
  }

  :host([overflow~='top']) [part='header']::after,
  :host([overflow~='bottom']) [part='footer']::after {
    opacity: 1;
  }
`,Ad=p`
  [part='overlay'] {
    position: relative;
    overflow: visible;
    display: flex;
  }

  :host([has-bounds-set]) [part='overlay'] {
    min-width: 0;
  }

  :host([has-bounds-set]:not([keep-in-viewport])) [part='overlay'] {
    max-width: none;
    max-height: none;
  }

  /* Content part scrolls by default */
  [part='content'] {
    flex: 1;
    min-height: 0;
    overflow: auto;
    overscroll-behavior: contain;
    clip-path: border-box;
  }

  [part='header'],
  :host(:not([has-title], [has-header])) [part='content'] {
    border-top-left-radius: inherit;
    border-top-right-radius: inherit;
  }

  [part='footer'],
  :host(:not([has-footer])) [part='content'] {
    border-bottom-left-radius: inherit;
    border-bottom-right-radius: inherit;
  }

  .resizer-container {
    display: flex;
    flex-direction: column;
    flex-grow: 1;
    max-width: 100%;
    border-radius: calc(
      var(--vaadin-dialog-border-radius, var(--vaadin-radius-l)) - var(
          --vaadin-dialog-border-width,
          var(--vaadin-overlay-border-width, 1px)
        )
    );
  }

  :host(:not([resizable])) .resizer {
    display: none;
  }

  .resizer {
    position: absolute;
    height: 16px;
    width: 16px;
  }

  .resizer.edge {
    height: 8px;
    width: 8px;
    inset: -4px;
  }

  .resizer.edge.n {
    width: auto;
    bottom: auto;
    cursor: ns-resize;
  }

  .resizer.ne {
    top: -4px;
    right: -4px;
    cursor: nesw-resize;
  }

  .resizer.edge.e {
    height: auto;
    left: auto;
    cursor: ew-resize;
  }

  .resizer.se {
    bottom: -4px;
    right: -4px;
    cursor: nwse-resize;
  }

  .resizer.edge.s {
    width: auto;
    top: auto;
    cursor: ns-resize;
  }

  .resizer.sw {
    bottom: -4px;
    left: -4px;
    cursor: nesw-resize;
  }

  .resizer.edge.w {
    height: auto;
    right: auto;
    cursor: ew-resize;
  }

  .resizer.nw {
    top: -4px;
    left: -4px;
    cursor: nwse-resize;
  }
`,Tn=[Z,wd,Ad];var ki=class{constructor(i){this.host=i}hostConnected(){if(!this.__initialized){this.__initialized=!0;let{host:i}=this;this.__resizeObserver=new ResizeObserver(()=>this.__updateState()),this.__resizeObserver.observe(i.$.resizerContainer),i.$.content.addEventListener("scroll",()=>this.__updateState(!0)),i.shadowRoot.addEventListener("slotchange",()=>this.__updateState(!0))}}update(){this.__updateState(!0)}__updateState(i=!1){cancelAnimationFrame(this.__updateRaf),i?this.__writeState(this.__readState()):this.__updateRaf=requestAnimationFrame(()=>this.__writeState(this.__readState()))}__readState(){let i=this.host.$.content,e="";return i.scrollTop>0&&(e+=" top"),i.scrollTop<i.scrollHeight-i.clientHeight&&(e+=" bottom"),e.trim()}__writeState(i){let{host:e}=this;i.length>0?F(e,"overflow",i):F(e,"overflow",null)}};var kn=s=>class extends J(s){static get properties(){return{headerTitle:{type:String},headerRenderer:{type:Object},footerRenderer:{type:Object},keepInViewport:{type:Boolean,reflectToAttribute:!0}}}static get observers(){return["_headerFooterRendererChange(headerRenderer, footerRenderer, opened)","_headerTitleChanged(headerTitle, opened)"]}get _contentRoot(){return this.owner}get _rendererRoot(){if(!this.__savedRoot){let e=document.createElement("vaadin-dialog-content");e.style.display="contents",this.owner.appendChild(e),this.__savedRoot=e}return this.__savedRoot}ready(){super.ready(),this.__overflowController=new ki(this),this.addController(this.__overflowController),this.__resizeObserver=new ResizeObserver(()=>{requestAnimationFrame(()=>{this.__adjustPosition()})}),this.__resizeObserver.observe(this.$.resizerContainer);let e=this.shadowRoot.querySelector('slot[name="header-content"]');this.__headerSlotObserver=new re(e,({currentNodes:r})=>{F(this,"has-header",r.length>0),this.__overflowController.update()});let t=this.shadowRoot.querySelector('slot[name="footer"]');this.__footerSlotObserver=new re(t,({currentNodes:r})=>{F(this,"has-footer",r.length>0),this.__overflowController.update()}),this.__handleWindowResize=this.__handleWindowResize.bind(this)}updated(e){super.updated(e),(e.has("opened")||e.has("keepInViewport"))&&(this.opened&&this.keepInViewport?window.addEventListener("resize",this.__handleWindowResize):window.removeEventListener("resize",this.__handleWindowResize))}__createContainer(e){let t=document.createElement("vaadin-dialog-content");return t.setAttribute("slot",e),t}__clearContainer(e){e.innerHTML="",delete e._$litPart$}__initContainer(e,t){return e?this.__clearContainer(e):(e=this.__createContainer(t),this.owner.appendChild(e)),e}_headerFooterRendererChange(e,t,r){let o=this.__oldHeaderRenderer!==e;this.__oldHeaderRenderer=e;let n=this.__oldFooterRenderer!==t;this.__oldFooterRenderer=t;let a=this._oldOpenedFooterHeader!==r;this._oldOpenedFooterHeader=r,o&&(e?this.headerContainer=this.__initContainer(this.headerContainer,"header-content"):this.headerContainer&&(this.headerContainer.remove(),this.headerContainer=null)),n&&(t?this.footerContainer=this.__initContainer(this.footerContainer,"footer"):this.footerContainer&&(this.footerContainer.remove(),this.footerContainer=null)),(e&&(o||a)||t&&(n||a))&&r&&this.requestContentUpdate()}_headerTitleChanged(e,t){F(this,"has-title",!!e),t&&(e||this._oldHeaderTitle)&&this.requestContentUpdate(),this._oldHeaderTitle=e}_headerTitleRenderer(){this.headerTitle?(this.headerTitleElement||(this.headerTitleElement=document.createElement("h2"),this.headerTitleElement.setAttribute("slot","title"),this.headerTitleElement.classList.add("draggable")),this.owner.appendChild(this.headerTitleElement),this.headerTitleElement.textContent=this.headerTitle):this.headerTitleElement&&(this.headerTitleElement.remove(),this.headerTitleElement=null)}requestContentUpdate(){super.requestContentUpdate(),this.headerContainer&&this.headerRenderer&&this.headerRenderer.call(this.owner,this.headerContainer,this.owner),this.footerContainer&&this.footerRenderer&&this.footerRenderer.call(this.owner,this.footerContainer,this.owner),this._headerTitleRenderer(),this.__overflowController?.update()}getBounds(){let e=this.$.overlay.getBoundingClientRect(),t=this.getBoundingClientRect(),r=e.top-t.top,o=e.left-t.left,n=e.width,a=e.height;return{top:r,left:o,width:n,height:a}}setBounds(e,t=!0){super.setBounds(e,t),this.__adjustPosition()}__handleWindowResize(){this.__adjustPosition()}__adjustPosition(){if(!this.opened||!this.keepInViewport)return;let e=getComputedStyle(this.$.overlay);if(e.position!=="absolute")return;let t=this.getBoundingClientRect(),r=this.getBounds(),o=parseFloat(e.width)||r.width,n=parseFloat(e.height)||r.height,a=t.right-t.left-o,l=t.bottom-t.top-n;if(r.left>a||r.top>l){let d=Math.max(0,Math.min(r.left,a)),h=Math.max(0,Math.min(r.top,l));Object.assign(this.$.overlay.style,{left:`${d}px`,top:`${h}px`})}}};var Di=class s extends kn(I(v(g(b(_))))){static get is(){return"vaadin-dialog-overlay"}static get styles(){return Tn}get _focusTrapRoot(){return this.owner}render(){return u`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <section id="resizerContainer" class="resizer-container">
          <header part="header">
            <div part="title"><slot name="title"></slot></div>
            <div part="header-content"><slot name="header-content"></slot></div>
          </header>
          <div part="content" id="content"><slot></slot></div>
          <footer part="footer"><slot name="footer"></slot></footer>
        </section>
      </div>
    `}bringToFront(i){if(i instanceof Event){let e=i.composedPath();if(Lr(this).some(r=>e.includes(r)&&zr(this,r)&&r instanceof s))return}super.bringToFront()}};m(Di);var Dn=s=>class extends s{static get properties(){return{opened:{type:Boolean,reflectToAttribute:!0,value:!1,notify:!0,sync:!0},noCloseOnOutsideClick:{type:Boolean,value:!1},noCloseOnEsc:{type:Boolean,value:!1},modeless:{type:Boolean,value:!1},noFocusTrap:{type:Boolean,value:!1},top:{type:String},left:{type:String},overlayRole:{type:String},keepInViewport:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["__positionChanged(top, left)"]}ready(){super.ready();let e=this.$.overlay;e.addEventListener("vaadin-overlay-outside-click",this._handleOutsideClick.bind(this)),e.addEventListener("vaadin-overlay-escape-press",this._handleEscPress.bind(this)),e.addEventListener("vaadin-overlay-closed",this.__handleOverlayClosed.bind(this)),this._overlayElement=e,this.hasAttribute("role")||(this.role="dialog"),this.setAttribute("tabindex","0")}updated(e){super.updated(e),e.has("overlayRole")&&(this.role=this.overlayRole||"dialog"),e.has("modeless")&&(this.modeless?this.removeAttribute("aria-modal"):this.setAttribute("aria-modal","true"))}__handleOverlayClosed(){this.dispatchEvent(new CustomEvent("closed"))}connectedCallback(){super.connectedCallback(),this.__restoreOpened&&(this.opened=!0)}disconnectedCallback(){super.disconnectedCallback(),setTimeout(()=>{this.isConnected||(this.__restoreOpened=this.opened,this.opened=!1)})}_onOverlayOpened(e){e.detail.value===!1&&(this.opened=!1)}_handleOutsideClick(e){this.noCloseOnOutsideClick&&e.preventDefault()}_handleEscPress(e){this.noCloseOnEsc&&e.preventDefault()}_bringOverlayToFront(e){this.modeless&&this._overlayElement.bringToFront(e)}__positionChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({top:e,left:t}))}__sizeChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({width:e,height:t},!1))}};function pt(s){return s.touches?s.touches[0]:s}function Mi(s){return s.clientX>=0&&s.clientX<=window.innerWidth&&s.clientY>=0&&s.clientY<=window.innerHeight}var Mn=s=>class extends s{static get properties(){return{draggable:{type:Boolean,value:!1,reflectToAttribute:!0},_touchDevice:{type:Boolean,value:K},__dragHandleClassName:{type:String}}}ready(){super.ready(),this._originalBounds={},this._originalMouseCoords={},this._startDrag=this._startDrag.bind(this),this._drag=this._drag.bind(this),this._stopDrag=this._stopDrag.bind(this),this.$.overlay.$.overlay.addEventListener("mousedown",this._startDrag),this.$.overlay.$.overlay.addEventListener("touchstart",this._startDrag)}_startDrag(e){if(!(e.type==="touchstart"&&e.touches.length>1)&&!e.defaultPrevented&&this.draggable&&(e.button===0||e.touches)){let t=this.$.overlay.$.resizerContainer,r=e.target===t,o=e.offsetX>t.clientWidth||e.offsetY>t.clientHeight,n=e.target===this.$.overlay.$.content,a=e.composedPath().some((l,d)=>{if(!l.classList)return!1;let h=l.classList.contains(this.__dragHandleClassName||"draggable"),c=l.classList.contains("draggable-leaf-only"),f=d===0;return c&&f||h&&(!c||f)});if(r&&!o||n||a){e.preventDefault(),this._originalBounds=this.$.overlay.getBounds();let l=pt(e);this._originalMouseCoords={top:l.pageY,left:l.pageX},window.addEventListener("mouseup",this._stopDrag),window.addEventListener("touchend",this._stopDrag),window.addEventListener("mousemove",this._drag),window.addEventListener("touchmove",this._drag);let{top:d,left:h,width:c,height:f}=this._originalBounds;this.$.overlay.$.overlay.style.position!=="absolute"&&(this.top=d,this.left=h),this.dispatchEvent(new CustomEvent("drag-start",{detail:{width:c,height:f,top:d,left:h}}))}}}_drag(e){let t=pt(e);if(Mi(t)){let r=this._originalBounds.top+(t.pageY-this._originalMouseCoords.top),o=this._originalBounds.left+(t.pageX-this._originalMouseCoords.left);if(this.keepInViewport){let{width:n,height:a}=this._originalBounds,l=this.$.overlay.getBoundingClientRect(),d=l.right-l.left-n,h=l.bottom-l.top-a;o=Math.max(0,Math.min(o,d)),r=Math.max(0,Math.min(r,h))}this.top=r,this.left=o}}_stopDrag(){this.dispatchEvent(new CustomEvent("dragged",{detail:{top:this.top,left:this.left}})),window.removeEventListener("mouseup",this._stopDrag),window.removeEventListener("touchend",this._stopDrag),window.removeEventListener("mousemove",this._drag),window.removeEventListener("touchmove",this._drag)}};var Pn=s=>class extends s{static get properties(){return{renderer:{type:Object},headerTitle:{type:String},headerRenderer:{type:Object},footerRenderer:{type:Object}}}requestContentUpdate(){this._overlayElement&&this._overlayElement.requestContentUpdate()}};var Fn=s=>class extends s{static get properties(){return{resizable:{type:Boolean,value:!1,reflectToAttribute:!0}}}ready(){super.ready(),this._originalBounds={},this._originalMouseCoords={},this._resizeListeners={start:{},resize:{},stop:{}},this._addResizeListeners()}_addResizeListeners(){["n","e","s","w","nw","ne","se","sw"].forEach(e=>{let t=document.createElement("div");this._resizeListeners.start[e]=r=>this._startResize(r,e),this._resizeListeners.resize[e]=r=>this._resize(r,e),this._resizeListeners.stop[e]=()=>this._stopResize(e),e.length===1&&t.classList.add("edge"),t.classList.add("resizer"),t.classList.add(e),t.addEventListener("mousedown",this._resizeListeners.start[e]),t.addEventListener("touchstart",this._resizeListeners.start[e]),this.$.overlay.$.resizerContainer.appendChild(t)})}_startResize(e,t){if(!(e.type==="touchstart"&&e.touches.length>1)&&(e.button===0||e.touches)){e.preventDefault(),this._originalBounds=this.$.overlay.getBounds();let r=pt(e);this._originalMouseCoords={top:r.pageY,left:r.pageX},window.addEventListener("mousemove",this._resizeListeners.resize[t]),window.addEventListener("touchmove",this._resizeListeners.resize[t]),window.addEventListener("mouseup",this._resizeListeners.stop[t]),window.addEventListener("touchend",this._resizeListeners.stop[t]),this.$.overlay.setBounds(this._originalBounds),this.$.overlay.setAttribute("has-bounds-set","");let{width:o,height:n,top:a,left:l}=this._originalBounds;this.dispatchEvent(new CustomEvent("resize-start",{detail:{width:o,height:n,top:a,left:l}}))}}_resize(e,t){let r=pt(e);Mi(r)&&t.split("").forEach(n=>{switch(n){case"n":{let a=this._originalBounds.height-(r.pageY-this._originalMouseCoords.top),l=this._originalBounds.top+(r.pageY-this._originalMouseCoords.top);a>40&&(this.top=l,this.height=a);break}case"e":{let a=this._originalBounds.width+(r.pageX-this._originalMouseCoords.left);a>40&&(this.width=a);break}case"s":{let a=this._originalBounds.height+(r.pageY-this._originalMouseCoords.top);a>40&&(this.height=a);break}case"w":{let a=this._originalBounds.width-(r.pageX-this._originalMouseCoords.left),l=this._originalBounds.left+(r.pageX-this._originalMouseCoords.left);a>40&&(this.left=l,this.width=a);break}default:break}})}_stopResize(e){window.removeEventListener("mousemove",this._resizeListeners.resize[e]),window.removeEventListener("touchmove",this._resizeListeners.resize[e]),window.removeEventListener("mouseup",this._resizeListeners.stop[e]),window.removeEventListener("touchend",this._resizeListeners.stop[e]),this.dispatchEvent(new CustomEvent("resize",{detail:this._getResizeDimensions()}))}_getResizeDimensions(){let{width:e,height:t,top:r,left:o}=getComputedStyle(this.$.overlay.$.overlay);return{width:e,height:t,top:r,left:o}}};var On=s=>class extends s{static get properties(){return{width:{type:String},height:{type:String}}}static get observers(){return["__sizeChanged(width, height)"]}__sizeChanged(e,t){requestAnimationFrame(()=>this.$.overlay.setBounds({width:e,height:t},!1))}};var ds=class extends On(Mn(Fn(Pn(Dn(Qt(A(g(_)))))))){static get is(){return"vaadin-dialog"}static get styles(){return p`
      :host([opened]),
      :host([opening]),
      :host([closing]) {
        display: block !important;
        position: fixed;
        outline: none;
      }

      :host,
      :host([hidden]) {
        display: none !important;
      }

      :host(:focus-visible) ::part(overlay) {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
      }
    `}render(){return u`
      <vaadin-dialog-overlay
        id="overlay"
        .owner="${this}"
        .opened="${this.opened}"
        .headerTitle="${this.headerTitle}"
        .renderer="${this.renderer}"
        .headerRenderer="${this.headerRenderer}"
        .footerRenderer="${this.footerRenderer}"
        .keepInViewport="${this.keepInViewport}"
        @opened-changed="${this._onOverlayOpened}"
        @mousedown="${this._bringOverlayToFront}"
        @touchstart="${this._bringOverlayToFront}"
        theme="${k(this._theme)}"
        .modeless="${this.modeless}"
        .withBackdrop="${!this.modeless}"
        ?resizable="${this.resizable}"
        restore-focus-on-close
        ?focus-trap="${!this.noFocusTrap}"
        exportparts="backdrop, overlay, header, title, header-content, content, footer"
      >
        <slot name="title" slot="title"></slot>
        <slot name="header-content" slot="header-content"></slot>
        <slot name="footer" slot="footer"></slot>
        <slot></slot>
      </vaadin-dialog-overlay>
    `}updated(i){super.updated(i),i.has("headerTitle")&&(this.ariaLabel=this.headerTitle)}};m(ds);function Y(s){return s.__cells||Array.from(s.querySelectorAll('[part~="cell"]:not([part~="details-cell"])'))}function H(s,i){[...s.children].forEach(i)}function ge(s,i){Y(s).forEach(i),s.__detailsCell&&i(s.__detailsCell)}function Rn(s,i,e){let t=1;s.forEach(r=>{t%10===0&&(t+=1),r._order=e+t*i,t+=1})}function Ft(s,i,e){switch(typeof e){case"boolean":s.toggleAttribute(i,e);break;case"string":s.setAttribute(i,e);break;default:s.removeAttribute(i);break}}function E(s,i,e){s.classList.toggle(i,e||e===""),s.part.toggle(i,e||e===""),s.part.length===0&&s.removeAttribute("part")}function Pt(s,i,e){s.forEach(t=>{E(t,i,e)})}function ke(s,i){let e=Y(s);Object.entries(i).forEach(([t,r])=>{Ft(s,t,r);let o=`${t}-row`;E(s,o,r),Pt(e,`${o}-cell`,r)})}function hs(s,i){let e=Y(s);Object.entries(i).forEach(([t,r])=>{let o=s.getAttribute(t);if(Ft(s,t,r),o){let n=`${t}-${o}-row`;E(s,n,!1),Pt(e,`${n}-cell`,!1)}if(r){let n=`${t}-${r}-row`;E(s,n,r),Pt(e,`${n}-cell`,r)}})}function ve(s,i,e,t,r){Ft(s,i,e),r&&E(s,r,!1),E(s,t||`${i}-cell`,e)}function $n(s){return Y(s).find(i=>i._content.querySelector("vaadin-grid-tree-toggle"))}var _t=class s{constructor(i,e){this.__host=i,this.__callback=e,this.__currentSlots=[],this.__onMutation=this.__onMutation.bind(this),this.__observer=new MutationObserver(this.__onMutation),this.__observer.observe(i,{childList:!0}),this.__initialCallDebouncer=x.debounce(this.__initialCallDebouncer,z,()=>this.__onMutation())}disconnect(){this.__observer.disconnect(),this.__initialCallDebouncer.cancel(),this.__toggleSlotChangeListeners(!1)}flush(){this.__onMutation()}__toggleSlotChangeListeners(i){this.__currentSlots.forEach(e=>{i?e.addEventListener("slotchange",this.__onMutation):e.removeEventListener("slotchange",this.__onMutation)})}__onMutation(){let i=!this.__currentColumns;this.__currentColumns||=[];let e=s.getColumns(this.__host),t=e.filter(a=>!this.__currentColumns.includes(a)),r=this.__currentColumns.filter(a=>!e.includes(a)),o=this.__currentColumns.some((a,l)=>a!==e[l]);this.__currentColumns=e,this.__toggleSlotChangeListeners(!1),this.__currentSlots=[...this.__host.children].filter(a=>a instanceof HTMLSlotElement),this.__toggleSlotChangeListeners(!0),(i||t.length||r.length||o)&&this.__callback(t,r)}static __isColumnElement(i){return i.nodeType===Node.ELEMENT_NODE&&/\bcolumn\b/u.test(i.localName)}static getColumns(i){let e=[],t=i._isColumnElement||s.__isColumnElement;return[...i.children].forEach(r=>{t(r)?e.push(r):r instanceof HTMLSlotElement&&[...r.assignedElements({flatten:!0})].filter(o=>t(o)).forEach(o=>e.push(o))}),e}};var Ln=s=>class extends s{static get properties(){return{resizable:{type:Boolean,sync:!0,value(){if(this.localName==="vaadin-grid-column-group")return;let e=this.parentNode;return e?.localName==="vaadin-grid-column-group"&&e.resizable||!1}},frozen:{type:Boolean,value:!1,sync:!0},frozenToEnd:{type:Boolean,value:!1,sync:!0},rowHeader:{type:Boolean,value:!1,sync:!0},hidden:{type:Boolean,value:!1,sync:!0},header:{type:String,sync:!0},textAlign:{type:String,sync:!0},headerPartName:{type:String,sync:!0},footerPartName:{type:String,sync:!0},_lastFrozen:{type:Boolean,value:!1,sync:!0},_bodyContentHidden:{type:Boolean,value:!1,sync:!0},_firstFrozenToEnd:{type:Boolean,value:!1,sync:!0},_order:{type:Number,sync:!0},_reorderStatus:{type:Boolean,sync:!0},_emptyCells:Array,_headerCell:{type:Object,sync:!0},_footerCell:{type:Object,sync:!0},_grid:Object,__initialized:{type:Boolean,value:!0},headerRenderer:{type:Function,sync:!0},_headerRenderer:{type:Function,computed:"_computeHeaderRenderer(headerRenderer, header, __initialized)"},footerRenderer:{type:Function,sync:!0},_footerRenderer:{type:Function,computed:"_computeFooterRenderer(footerRenderer, __initialized)"},__gridColumnElement:{type:Boolean,value:!0}}}static get observers(){return["_widthChanged(width, _headerCell, _footerCell, _cells)","_frozenChanged(frozen, _headerCell, _footerCell, _cells)","_frozenToEndChanged(frozenToEnd, _headerCell, _footerCell, _cells)","_flexGrowChanged(flexGrow, _headerCell, _footerCell, _cells)","_textAlignChanged(textAlign, _cells, _headerCell, _footerCell)","_lastFrozenChanged(_lastFrozen)","_firstFrozenToEndChanged(_firstFrozenToEnd)","_onRendererOrBindingChanged(_renderer, _cells, _bodyContentHidden, path)","_onHeaderRendererOrBindingChanged(_headerRenderer, _headerCell, path, header)","_onFooterRendererOrBindingChanged(_footerRenderer, _footerCell)","_resizableChanged(resizable, _headerCell)","_reorderStatusChanged(_reorderStatus, _headerCell, _footerCell, _cells)","_hiddenChanged(hidden, _headerCell, _footerCell, _cells)","_rowHeaderChanged(rowHeader, _cells)","__headerFooterPartNameChanged(_headerCell, _footerCell, headerPartName, footerPartName)"]}get _grid(){return this._gridValue||(this._gridValue=this._findHostGrid()),this._gridValue}get _allCells(){return[].concat(this._cells||[]).concat(this._emptyCells||[]).concat(this._headerCell).concat(this._footerCell).filter(e=>e)}connectedCallback(){super.connectedCallback(),requestAnimationFrame(()=>{this._grid&&this._allCells.forEach(e=>{e._content.parentNode||this._grid.appendChild(e._content)})})}disconnectedCallback(){super.disconnectedCallback(),requestAnimationFrame(()=>{this._grid||this._allCells.forEach(e=>{e._content.parentNode&&e._content.parentNode.removeChild(e._content)})}),this._gridValue=void 0}_findHostGrid(){let e=this;for(;e&&!/^vaadin.*grid(-pro)?$/u.test(e.localName);)e=e.assignedSlot?e.assignedSlot.parentNode:e.parentNode;return e||void 0}_renderHeaderAndFooter(){this._renderHeaderCellContent(this._headerRenderer,this._headerCell),this._renderFooterCellContent(this._footerRenderer,this._footerCell)}_flexGrowChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("flexGrow"),this._allCells.forEach(t=>{t.style.flexGrow=e})}_widthChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("width"),this._allCells.forEach(t=>{t.style.width=e})}_frozenChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("frozen",e),this._allCells.forEach(t=>{ve(t,"frozen",e)}),this._grid&&this._grid._frozenCellsChanged&&this._grid._frozenCellsChanged()}_frozenToEndChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("frozenToEnd",e),this._allCells.forEach(t=>{this._grid&&t.parentElement===this._grid.$.sizer||ve(t,"frozen-to-end",e)}),this._grid&&this._grid._frozenCellsChanged&&this._grid._frozenCellsChanged()}_lastFrozenChanged(e){this._allCells.forEach(t=>{ve(t,"last-frozen",e)}),this.parentElement&&this.parentElement._columnPropChanged&&(this.parentElement._lastFrozen=e)}_firstFrozenToEndChanged(e){this._allCells.forEach(t=>{this._grid&&t.parentElement===this._grid.$.sizer||ve(t,"first-frozen-to-end",e)}),this.parentElement&&this.parentElement._columnPropChanged&&(this.parentElement._firstFrozenToEnd=e)}_rowHeaderChanged(e,t){t&&t.forEach(r=>{r.setAttribute("role",e?"rowheader":"gridcell")})}_generateHeader(e){return e.substr(e.lastIndexOf(".")+1).replace(/([A-Z])/gu,"-$1").toLowerCase().replace(/-/gu," ").replace(/^./u,t=>t.toUpperCase())}_reorderStatusChanged(e){let t=this.__previousReorderStatus,r=t?`reorder-${t}-cell`:"",o=`reorder-${e}-cell`;this._allCells.forEach(n=>{ve(n,"reorder-status",e,o,r)}),this.__previousReorderStatus=e}_resizableChanged(e,t){e===void 0||t===void 0||t&&[t].concat(this._emptyCells).forEach(r=>{if(r){let o=r.querySelector('[part~="resize-handle"]');if(o&&r.removeChild(o),e){let n=document.createElement("div");E(n,"resize-handle",!0),r.appendChild(n)}}})}_textAlignChanged(e){if(!(e===void 0||this._grid===void 0)){if(["start","end","center"].indexOf(e)===-1){console.warn('textAlign can only be set as "start", "end" or "center"');return}this._allCells.forEach(t=>{t._content.style.textAlign=e})}}_hiddenChanged(e){this.parentElement&&this.parentElement._columnPropChanged&&this.parentElement._columnPropChanged("hidden",e),!!e!=!!this._previousHidden&&this._grid&&(e===!0&&this._allCells.forEach(t=>{t._content.parentNode&&t._content.parentNode.removeChild(t._content)}),this._grid._debouncerHiddenChanged=x.debounce(this._grid._debouncerHiddenChanged,ie,()=>{this._grid&&this._grid._renderColumnTree&&this._grid._renderColumnTree(this._grid._columnTree)}),this._grid._debounceUpdateFrozenColumn&&this._grid._debounceUpdateFrozenColumn(),this._grid._resetKeyboardNavigation&&this._grid._resetKeyboardNavigation()),this._previousHidden=e}_runRenderer(e,t,r){let o=r?.item&&!t.parentElement.hidden;if(!(o||e===this._headerRenderer||e===this._footerRenderer))return;let a=[t._content,this];o&&a.push(r),e.apply(this,a)}__renderCellsContent(e,t){this.hidden||!this._grid||t.forEach(r=>{if(!r.parentElement)return;let o=this._grid.__getRowModel(r.parentElement);e&&(r._renderer!==e&&this._clearCellContent(r),r._renderer=e,this._runRenderer(e,r,o))})}_clearCellContent(e){e._content.innerHTML="",delete e._content._$litPart$}_renderHeaderCellContent(e,t){!t||!e||(this.__renderCellsContent(e,[t]),this._grid&&t.parentElement&&this._grid.__debounceUpdateHeaderFooterRowVisibility(t.parentElement))}_onHeaderRendererOrBindingChanged(e,t,...r){this._renderHeaderCellContent(e,t)}__headerFooterPartNameChanged(e,t,r,o){[{cell:e,partName:r},{cell:t,partName:o}].forEach(({cell:n,partName:a})=>{if(n){let l=n.__customParts||[];n.part.remove(...l),n.__customParts=a?a.trim().split(" "):[],n.part.add(...n.__customParts)}})}_renderBodyCellsContent(e,t){!t||!e||this.__renderCellsContent(e,t)}_onRendererOrBindingChanged(e,t,...r){this._renderBodyCellsContent(e,t)}_renderFooterCellContent(e,t){!t||!e||(this.__renderCellsContent(e,[t]),this._grid&&t.parentElement&&this._grid.__debounceUpdateHeaderFooterRowVisibility(t.parentElement))}_onFooterRendererOrBindingChanged(e,t){this._renderFooterCellContent(e,t)}__setTextContent(e,t){e.textContent!==t&&(e.textContent=t)}__textHeaderRenderer(){this.__setTextContent(this._headerCell._content,this.header)}_defaultHeaderRenderer(){this.path&&this.__setTextContent(this._headerCell._content,this._generateHeader(this.path))}_defaultRenderer(e,t,{item:r}){this.path&&this.__setTextContent(e,G(this.path,r))}_defaultFooterRenderer(){}_computeHeaderRenderer(e,t){return e||(t!=null?this.__textHeaderRenderer:this._defaultHeaderRenderer)}_computeRenderer(e){return e||this._defaultRenderer}_computeFooterRenderer(e){return e||this._defaultFooterRenderer}},zn=s=>class extends Ln(I(s)){static get properties(){return{width:{type:String,value:"100px",sync:!0},flexGrow:{type:Number,value:1,sync:!0},renderer:{type:Function,sync:!0},_renderer:{type:Function,computed:"_computeRenderer(renderer, __initialized)"},path:{type:String,sync:!0},autoWidth:{type:Boolean,value:!1},_focusButtonMode:{type:Boolean,value:!1},_cells:{type:Array,sync:!0}}}};var cs=class extends zn(g(_)){static get is(){return"vaadin-grid-column"}};m(cs);var Bn=p`
  /* stylelint-disable no-duplicate-selectors */
  :host {
    display: flex;
    max-width: 100%;
    height: 400px;
    min-height: var(--_grid-min-height, 0);
    flex: 1 1 auto;
    align-self: stretch;
    position: relative;
    box-sizing: border-box;
    overflow: hidden;
    -webkit-tap-highlight-color: transparent;
    background: var(--vaadin-grid-background, var(--vaadin-background-color));
    border: var(--vaadin-grid-border-width, 1px) solid var(--_border-color);
    cursor: default;
    --_border-color: var(--vaadin-grid-border-color, var(--vaadin-border-color-secondary));
    --_row-border-width: var(--vaadin-grid-row-border-width, 1px);
    --_column-border-width: var(--vaadin-grid-column-border-width, 0px);
    --_default-cell-padding: var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container);
    border-radius: var(--vaadin-grid-border-radius, var(--vaadin-radius-m));
  }

  :host([hidden]),
  [hidden] {
    display: none !important;
  }

  :host([disabled]) {
    pointer-events: none;
    opacity: 0.7;
  }

  /* Variant: No outer border */
  :host([theme~='no-border']) {
    border-width: 0;
    border-radius: 0;
  }

  :host([all-rows-visible]) {
    height: auto;
    align-self: flex-start;
    min-height: auto;
    flex-grow: 0;
    flex-shrink: 0;
    width: 100%;
  }

  #scroller {
    contain: layout;
    position: relative;
    display: flex;
    width: 100%;
    min-width: 0;
    min-height: 0;
    align-self: stretch;
    overflow: hidden;
  }

  #items {
    flex-grow: 1;
    flex-shrink: 0;
    display: block;
    position: sticky;
    width: 100%;
    left: 0;
    min-height: 1px;
    z-index: 1;
  }

  #table {
    display: flex;
    flex-direction: column;
    width: 100%;
    overflow: auto;
    position: relative;
    border-radius: inherit;
    /* Workaround for a Chrome bug: new stacking context here prevents the scrollbar from getting hidden */
    z-index: 0;
  }

  [no-scrollbars]:is([safari], [firefox]) #table {
    overflow: hidden;
  }

  #header,
  #footer {
    display: block;
    position: sticky;
    left: 0;
    width: 100%;
    z-index: 2;
  }

  :host([dir='rtl']) #items,
  :host([dir='rtl']) #header,
  :host([dir='rtl']) #footer {
    left: auto;
  }

  #header {
    top: 0;
  }

  #footer {
    bottom: 0;
  }

  .header-cell {
    text-align: inherit;
  }

  .header-cell,
  .reorder-ghost {
    font-size: var(--vaadin-grid-header-font-size, 1em);
    font-weight: var(--vaadin-grid-header-font-weight, 500);
    color: var(--vaadin-grid-header-text-color, var(--vaadin-text-color));
  }

  .row {
    display: flex;
    width: 100%;
    box-sizing: border-box;
    margin: 0;
    position: relative;
  }

  .row:not(:focus-within) {
    --_non-focused-row-none: none;
  }

  .body-row[loading] ::slotted(vaadin-grid-cell-content) {
    visibility: hidden;
  }

  #scroller[column-rendering='lazy'] .body-cell:not(.frozen-cell, .frozen-to-end-cell) {
    transform: translateX(var(--_grid-lazy-columns-start));
  }

  .body-row:empty {
    height: 100%;
  }

  .cell {
    padding: 0;
    box-sizing: border-box;
  }

  .cell:where(:not(.details-cell)) {
    flex-shrink: 0;
    flex-grow: 1;
    display: flex;
    width: 100%;
    position: relative;
    align-items: center;
    white-space: nowrap;
  }

  /*
    Block borders

    ::after - row and cell focus outline
    ::before - header bottom and footer top borders that only appear when scrolling
  */

  .row::after {
    top: 0;
    bottom: calc(var(--_row-border-width) * -1);
  }

  .body-row {
    scroll-margin-bottom: var(--_row-border-width);
  }

  .cell {
    border-block: var(--_row-border-width) var(--_border-color);
    border-top-style: solid;
  }

  .cell::after {
    top: calc(var(--_row-border-width) * -1);
    bottom: calc(var(--_row-border-width) * -1);
  }

  /* Block borders / Last header row and first footer row */

  .last-header-row::before,
  .first-footer-row::before {
    position: absolute;
    inset-inline: 0;
    border-block: var(--_row-border-width) var(--_border-color);
    transform: translateX(var(--_grid-horizontal-scroll-position));
  }

  /* Block borders / First header row */

  .first-header-row-cell {
    border-top-style: none;
  }

  .first-header-row-cell::after {
    top: 0;
  }

  /* Block borders / Last header row */

  :host([overflow~='top']) .last-header-row::before {
    content: '';
    bottom: calc(var(--_row-border-width) * -1);
    border-bottom-style: solid;
  }

  /* Block borders / First body row */

  #table:not([has-header]) .first-row-cell {
    border-top-style: none;
  }

  #table:not([has-header]) .first-row-cell::after {
    top: 0;
  }

  /* Block borders / Last body row */

  .last-row::after {
    bottom: 0;
  }

  .last-row .details-cell,
  .last-row-cell:not(.details-opened-row-cell) {
    border-bottom-style: solid;
  }

  /* Block borders / Last body row without footer */

  :host([all-rows-visible]),
  :host([overflow~='top']),
  :host([overflow~='bottom']) {
    #table:not([has-footer]) .last-row .details-cell,
    #table:not([has-footer]) .last-row-cell:not(.details-opened-row-cell) {
      border-bottom-style: none;

      &::after {
        bottom: 0;
      }
    }
  }

  /* Block borders / First footer row */

  .first-footer-row::after {
    top: calc(var(--_row-border-width) * -1);
  }

  .first-footer-row-cell {
    border-top-style: none;
  }

  :host([overflow~='bottom']),
  :host(:not([overflow~='top'], [all-rows-visible])) #scroller:not([empty-state]) {
    .first-footer-row::before {
      content: '';
      top: calc(var(--_row-border-width) * -1);
      border-top-style: solid;
    }
  }

  /* Block borders / Last footer row */

  .last-footer-row::after,
  .last-footer-row-cell::after {
    bottom: 0;
  }

  /* Inline borders */

  .cell {
    border-inline: var(--_column-border-width) var(--_border-color);
  }

  .header-cell:not(.first-column-cell),
  .footer-cell:not(.first-column-cell),
  .body-cell:not(.first-column-cell) {
    border-inline-start-style: solid;
  }

  .last-frozen-cell:not(.last-column-cell) {
    border-inline-end-style: solid;

    & + .cell {
      border-inline-start-style: none;
    }
  }

  /* Row and cell background */

  .row {
    background-color: var(--vaadin-grid-row-background-color, var(--vaadin-background-color));
  }

  .cell {
    --_cell-background-image: linear-gradient(
      var(--vaadin-grid-cell-background-color, transparent),
      var(--vaadin-grid-cell-background-color, transparent)
    );

    background-color: inherit;
    background-repeat: no-repeat;
    background-origin: padding-box;
    background-image: var(--_cell-background-image);
  }

  .body-cell {
    --_cell-highlight-background-image: linear-gradient(
      var(--vaadin-grid-row-highlight-background-color, transparent),
      var(--vaadin-grid-row-highlight-background-color, transparent)
    );

    background-image:
      var(--_row-hover-background-image, none), var(--_row-selected-background-image, none),
      var(--_cell-highlight-background-image, none), var(--_row-odd-background-image, none),
      var(--_cell-background-image, none);
  }

  .selected-row {
    --_row-selected-background-color: var(
      --vaadin-grid-row-selected-background-color,
      color-mix(in srgb, currentColor 8%, transparent)
    );
    --_row-selected-background-image: linear-gradient(
      var(--_row-selected-background-color),
      var(--_row-selected-background-color)
    );
  }

  @media (any-hover: hover) {
    .body-row:hover {
      --_row-hover-background-color: var(--vaadin-grid-row-hover-background-color, transparent);
      --_row-hover-background-image: linear-gradient(
        var(--_row-hover-background-color),
        var(--_row-hover-background-color)
      );
    }
  }

  :host([theme~='row-stripes']) .odd-row {
    --_row-odd-background-color: var(
      --vaadin-grid-row-odd-background-color,
      color-mix(in srgb, var(--vaadin-text-color) 4%, transparent)
    );
    --_row-odd-background-image: linear-gradient(var(--_row-odd-background-color), var(--_row-odd-background-color));
  }

  /* Variant: wrap cell contents */

  :host([theme~='wrap-cell-content']) .cell:not(.details-cell) {
    white-space: normal;
  }

  /* Raise highlighted rows above others */
  .row,
  .frozen-cell,
  .frozen-to-end-cell {
    &:focus,
    &:focus-within {
      z-index: 3;
    }
  }

  .details-cell {
    position: absolute;
    bottom: 0;
    width: 100%;
  }

  ::slotted(vaadin-grid-cell-content) {
    display: block;
    overflow: hidden;
    text-overflow: var(--vaadin-grid-cell-text-overflow, ellipsis);
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding));
    flex: 1;
    min-height: 1lh;
    min-width: 0;
  }

  .details-cell,
  .frozen-cell,
  .frozen-to-end-cell {
    z-index: 2;
  }

  /* Empty state */
  #scroller:not([empty-state]) #emptystatebody,
  #scroller[empty-state] #items {
    display: none;
  }

  #emptystatebody {
    display: flex;
    position: sticky;
    inset: 0;
    flex: 1;
    overflow: hidden;
  }

  #emptystaterow {
    display: flex;
    flex: 1;
  }

  #emptystatecell {
    display: block;
    flex: 1;
    overflow: auto;
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding));
    outline: none;
    border-block: var(--_row-border-width) var(--_border-color);
  }

  #table[has-header] #emptystatecell {
    border-top-style: solid;
  }

  #table[has-footer] #emptystatecell {
    border-bottom-style: solid;
  }

  #emptystatecell:focus-visible {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  /* Reordering styles */
  :host([reordering]) ::slotted(vaadin-grid-cell-content),
  :host([reordering]) .resize-handle,
  #scroller[no-content-pointer-events] ::slotted(vaadin-grid-cell-content) {
    pointer-events: none;
  }

  .reorder-ghost {
    visibility: hidden;
    position: fixed;
    pointer-events: none;
    box-shadow:
      0 0 0 1px hsla(0deg, 0%, 0%, 0.2),
      0 8px 24px -2px hsla(0deg, 0%, 0%, 0.2);
    padding: var(--vaadin-grid-cell-padding, var(--_default-cell-padding)) !important;
    border-radius: 3px;

    /* Prevent overflowing the grid in Firefox */
    top: 0;
    inset-inline-start: 0;
  }

  :host([reordering]) {
    -webkit-user-select: none;
    user-select: none;
  }

  :host([reordering]) .cell {
    /* TODO expose a custom property to control this */
    --_reorder-curtain-filter: brightness(0.9) contrast(1.1);
  }

  :host([reordering]) .cell::after {
    content: '';
    position: absolute;
    inset: 0;
    z-index: 1;
    -webkit-backdrop-filter: var(--_reorder-curtain-filter);
    backdrop-filter: var(--_reorder-curtain-filter);
    outline: 0;
  }

  :host([reordering]) .reorder-allowed-cell {
    /* TODO expose a custom property to control this */
    --_reorder-curtain-filter: brightness(0.94) contrast(1.07);
  }

  :host([reordering]) .reorder-dragging-cell {
    --_reorder-curtain-filter: none;
  }

  /* Resizing styles */
  .resize-handle {
    position: absolute;
    top: 0;
    inset-inline-end: 0;
    height: 100%;
    cursor: col-resize;
    z-index: 1;
    opacity: 0;
    width: var(--vaadin-focus-ring-width);
    background: var(--vaadin-grid-column-resize-handle-color, var(--vaadin-focus-ring-color));
    transition: opacity 0.2s;
    translate: var(--_column-border-width);
  }

  .last-column-cell .resize-handle {
    translate: 0;
  }

  :host(:not([reordering])) #scroller:not([column-resizing]) .resize-handle:hover,
  .resize-handle:active {
    opacity: 1;
    transition-delay: 0.15s;
  }

  .resize-handle::before {
    position: absolute;
    content: '';
    height: 100%;
    width: 16px;
    translate: calc(-50% + var(--vaadin-focus-ring-width) / 2);
  }

  :host([dir='rtl']) .resize-handle::before {
    translate: calc(50% - var(--vaadin-focus-ring-width) / 2);
  }

  :is(.last-column-cell, .last-frozen-cell, .first-frozen-to-end-cell) .resize-handle::before {
    width: 8px;
    translate: 0;
  }

  :is(.last-column-cell, .last-frozen-cell) .resize-handle::before {
    inset-inline-end: 0;
  }

  .frozen-to-end-cell :is(.resize-handle, .resize-handle::before) {
    inset-inline: 0 auto;
  }

  .frozen-to-end-cell .resize-handle {
    translate: calc(var(--_column-border-width) * -1);
  }

  .first-frozen-to-end-cell {
    margin-inline-start: auto;
  }

  #scroller:is([column-resizing], [range-selecting]) {
    -webkit-user-select: none;
    user-select: none;
  }

  /* Focus outline element, also used for d'n'd indication */
  :is(.row, .cell)::after {
    position: absolute;
    z-index: 3;
    inset-inline: 0;
    pointer-events: none;
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  .row::after {
    transform: translateX(var(--_grid-horizontal-scroll-position));
  }

  .cell:where(:not(.details-cell))::after {
    inset-inline: calc(var(--_column-border-width) * -1);
  }

  .first-column-cell::after {
    inset-inline-start: 0;
  }

  .last-column-cell::after {
    inset-inline-end: 0;
  }

  :host([navigating]) .row:focus,
  :host([navigating]) .cell:focus {
    outline: 0;
  }

  .row:focus-visible,
  .cell:focus-visible {
    outline: 0;
  }

  .row:focus-visible::after,
  .cell:focus-visible::after,
  :host([navigating]) .row:focus::after,
  :host([navigating]) .cell:focus::after {
    content: '';
  }

  /* Drag'n'drop styles */
  :host([dragover]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-grid-border-width, 1px) * -1);
  }

  /* Styles applied to draggable cell content; see _filterDragAndDrop in vaadin-grid-drag-and-drop-mixin.js. */
  ::slotted(vaadin-grid-cell-content[draggable-source]) {
    -webkit-user-drag: element;
    -webkit-user-select: none;
    user-select: none;
  }

  .row[dragover] {
    z-index: 100 !important;
  }

  .row[dragover]::after {
    content: '';
  }

  .dragover-above-row::after {
    outline: 0;
    border-top: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  .dragover-above-row:not(.first-row)::after {
    top: calc(var(--vaadin-focus-ring-width) / -2);
  }

  .dragover-below-row::after {
    outline: 0;
    border-bottom: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
  }

  .dragover-below-row:not(.last-row)::after {
    bottom: calc(var(--vaadin-focus-ring-width) / -2);
  }

  .dragstart-row-cell {
    border-block: none !important;
    padding-block: var(--_row-border-width) !important;
  }

  .dragstart-row-cell.last-column-cell {
    border-radius: 0 3px 3px 0;
  }

  .dragstart-row-cell.first-column-cell {
    border-radius: 3px 0 0 3px;
  }

  /* Indicates the number of dragged rows */
  /* TODO export custom properties to control styles */
  #scroller .dragstart-row:not([dragstart=''])::before {
    position: absolute;
    left: var(--_grid-drag-start-x);
    top: var(--_grid-drag-start-y);
    z-index: 100;
    content: attr(dragstart);
    box-sizing: border-box;
    padding: 0.3em;
    color: white;
    background-color: red;
    border-radius: 1em;
    font-size: 0.75rem;
    line-height: 1;
    font-weight: 500;
    min-width: 1.6em;
    text-align: center;
  }

  /* Sizer styles */
  #sizer {
    display: flex;
    visibility: hidden;
  }

  #sizer .details-cell,
  #sizer ::slotted(vaadin-grid-cell-content) {
    display: none !important;
  }

  #sizer .cell {
    display: block;
    flex-shrink: 0;
    line-height: 0;
    height: 0 !important;
    min-height: 0 !important;
    max-height: 0 !important;
    padding: 0 !important;
    border: none !important;
  }

  #sizer .cell::before {
    content: '-';
  }
`;var Vn=s=>class extends s{static get properties(){return{accessibleName:{type:String}}}static get observers(){return["__a11yUpdateGridSize(size, _columnTree, __emptyState)"]}__a11yGetHeaderRowCount(e){return e.filter(t=>t.some(r=>r.headerRenderer||r.path&&r.header!==null||r.header)).length}__a11yGetFooterRowCount(e){return e.filter(t=>t.some(r=>r.footerRenderer)).length}__a11yUpdateGridSize(e,t,r){if(e===void 0||t===void 0)return;let o=this.__a11yGetHeaderRowCount(t),n=this.__a11yGetFooterRowCount(t),l=(r?1:e)+o+n;this.$.table.setAttribute("aria-rowcount",l);let d=t[t.length-1],h=r?1:l&&d?.length||0;this.$.table.setAttribute("aria-colcount",h),this.__a11yUpdateHeaderRows(),this.__a11yUpdateFooterRows()}__a11yUpdateHeaderRows(){H(this.$.header,(e,t)=>{e.setAttribute("aria-rowindex",t+1)})}__a11yUpdateFooterRows(){H(this.$.footer,(e,t)=>{e.setAttribute("aria-rowindex",this.__a11yGetHeaderRowCount(this._columnTree)+this.size+t+1)})}__a11yUpdateRowRowindex(e){e.setAttribute("aria-rowindex",e.index+this.__a11yGetHeaderRowCount(this._columnTree)+1)}__a11yUpdateRowSelected(e,t){e.setAttribute("aria-selected",!!t),ge(e,r=>{r.setAttribute("aria-selected",!!t)})}__a11yUpdateRowExpanded(e){let t=$n(e);this.__isRowExpandable(e)?(e.setAttribute("aria-expanded","false"),t&&t.setAttribute("aria-expanded","false")):this.__isRowCollapsible(e)?(e.setAttribute("aria-expanded","true"),t&&t.setAttribute("aria-expanded","true")):(e.removeAttribute("aria-expanded"),t&&t.removeAttribute("aria-expanded"))}__a11yUpdateRowLevel(e,t){t>0||this.__isRowCollapsible(e)||this.__isRowExpandable(e)?e.setAttribute("aria-level",t+1):e.removeAttribute("aria-level")}__a11ySetRowDetailsCell(e,t){ge(e,r=>{r!==t&&r.setAttribute("aria-controls",t.id)})}__a11yUpdateCellColspan(e,t){e.setAttribute("aria-colspan",Number(t))}__a11yUpdateSorters(){Array.from(this.querySelectorAll("vaadin-grid-sorter")).forEach(e=>{let t=e.parentNode;for(;t&&t.localName!=="vaadin-grid-cell-content";)t=t.parentNode;t?.assignedSlot&&t.assignedSlot.parentNode.setAttribute("aria-sort",{asc:"ascending",desc:"descending"}[String(e.direction)]||"none")})}};var Ed=s=>s.offsetParent&&!s.part.contains("body-cell")&&ze(s)&&getComputedStyle(s).visibility!=="hidden",Nn=s=>class extends s{static get properties(){return{activeItem:{type:Object,notify:!0,value:null,sync:!0}}}ready(){super.ready(),this.$.scroller.addEventListener("click",this._onClick.bind(this)),this.addEventListener("cell-activate",this._activateItem.bind(this)),this.addEventListener("row-activate",this._activateItem.bind(this))}_activateItem(e){let t=e.detail.model,r=t?t.item:null;r&&(this.activeItem=this._itemsEqual(this.activeItem,r)?null:r)}_shouldPreventCellActivationOnClick(e){let{cell:t}=this._getGridEventLocation(e);return e.defaultPrevented||e.skipCellActivate||!t||t.part.contains("details-cell")||t===this.$.emptystatecell||t._content.contains(this.getRootNode().activeElement)||this._isFocusable(e.target)||e.target instanceof HTMLLabelElement}_onClick(e){if(this._shouldPreventCellActivationOnClick(e))return;let{cell:t}=this._getGridEventLocation(e);t&&this.dispatchEvent(new CustomEvent("cell-activate",{detail:{model:this.__getRowModel(t.parentElement)}}))}_isFocusable(e){return Ed(e)}};function mt(s,i){return s.split(".").reduce((e,t)=>e[t],i)}function Hn(s,i,e){if(e.length===0)return!1;let t=!0;return s.forEach(({path:r})=>{if(!r||r.indexOf(".")===-1)return;let o=r.replace(/\.[^.]*$/u,"");mt(o,e[0])===void 0&&(console.warn(`Path "${r}" used for ${i} does not exist in all of the items, ${i} is disabled.`),t=!1)}),t}function Pi(s){return[void 0,null].indexOf(s)>=0?"":isNaN(s)?s.toString():s}function Un(s,i){return s=Pi(s),i=Pi(i),s<i?-1:s>i?1:0}function Id(s,i){return s.sort((e,t)=>i.map(r=>r.direction==="asc"?Un(mt(r.path,e),mt(r.path,t)):r.direction==="desc"?Un(mt(r.path,t),mt(r.path,e)):0).reduce((r,o)=>r!==0?r:o,0))}function Sd(s,i){return s.filter(e=>i.every(t=>{let r=Pi(mt(t.path,e)),o=Pi(t.value).toString().toLowerCase();return r.toString().toLowerCase().includes(o)}))}var jn=s=>(i,e)=>{let t=s?[...s]:[];i.filters&&Hn(i.filters,"filtering",t)&&(t=Sd(t,i.filters)),Array.isArray(i.sortOrders)&&i.sortOrders.length&&Hn(i.sortOrders,"sorting",t)&&(t=Id(t,i.sortOrders));let r=Math.min(t.length,i.pageSize),o=i.page*r,n=o+r,a=t.slice(o,n);e(a,t.length)};var Wn=s=>class extends s{static get properties(){return{items:{type:Array,sync:!0}}}static get observers(){return["__dataProviderOrItemsChanged(dataProvider, items, isAttached, items.*)"]}__setArrayDataProvider(e){let t=jn(this.items,{});t.__items=e,this._arrayDataProvider=t,this.size=e.length,this.dataProvider=t}_onDataProviderPageReceived(){super._onDataProviderPageReceived(),this._arrayDataProvider&&(this.size=this._flatSize)}__dataProviderOrItemsChanged(e,t,r){r&&(this._arrayDataProvider?e!==this._arrayDataProvider?(this._arrayDataProvider=void 0,this.items=void 0):t?this._arrayDataProvider.__items===t?this.clearCache():this.__setArrayDataProvider(t):(this._arrayDataProvider=void 0,this.dataProvider=void 0,this.size=0,this.clearCache()):t&&this.__setArrayDataProvider(t))}};var qn=s=>class extends s{static get properties(){return{__pendingRecalculateColumnWidths:{type:Boolean,value:!0}}}static get observers(){return["__dataProviderChangedAutoWidth(dataProvider)","__columnTreeChangedAutoWidth(_columnTree)","__flatSizeChangedAutoWidth(_flatSize)"]}updated(i){super.updated(i),i.has("__hostVisible")&&!i.get("__hostVisible")&&this.__tryToRecalculateColumnWidthsIfPending()}__dataProviderChangedAutoWidth(i){this.__hasHadRenderedRowsForColumnWidthCalculation||this.recalculateColumnWidths()}__columnTreeChangedAutoWidth(i){queueMicrotask(()=>this.recalculateColumnWidths())}__flatSizeChangedAutoWidth(i){requestAnimationFrame(()=>{i&&!this.__hasHadRenderedRowsForColumnWidthCalculation?this.recalculateColumnWidths():this.__tryToRecalculateColumnWidthsIfPending()})}_onDataProviderPageLoaded(){super._onDataProviderPageLoaded(),this.__tryToRecalculateColumnWidthsIfPending()}_updateFrozenColumn(){super._updateFrozenColumn(),this.__tryToRecalculateColumnWidthsIfPending()}__getIntrinsicWidth(i){return this.__intrinsicWidthCache.has(i)||this.__calculateAndCacheIntrinsicWidths([i]),this.__intrinsicWidthCache.get(i)}__getDistributedWidth(i,e){if(i==null||i===this)return 0;let t=Math.max(this.__getIntrinsicWidth(i),this.__getDistributedWidth(this.__getParentColumnGroup(i),i));if(!e)return t;let r=i,o=t,n=r._visibleChildColumns.map(h=>this.__getIntrinsicWidth(h)).reduce((h,c)=>h+c,0),a=Math.max(0,o-n),d=this.__getIntrinsicWidth(e)/n*a;return this.__getIntrinsicWidth(e)+d}_recalculateColumnWidths(){this.__virtualizer.flush(),[...this.$.header.children,...this.$.footer.children].forEach(o=>{o.__debounceUpdateHeaderFooterRowVisibility&&o.__debounceUpdateHeaderFooterRowVisibility.flush()}),this.__hasHadRenderedRowsForColumnWidthCalculation||=this._getRenderedRows().length>0,this.__intrinsicWidthCache=new Map;let i=this._firstVisibleIndex,e=this._lastVisibleIndex;this.__viewportRowsCache=this._getRenderedRows().filter(o=>o.index>=i&&o.index<=e);let t=this.__getAutoWidthColumns(),r=new Set;for(let o of t){let n=this.__getParentColumnGroup(o);for(;n&&!r.has(n);)r.add(n),n=this.__getParentColumnGroup(n)}this.__calculateAndCacheIntrinsicWidths([...t,...r]),t.forEach(o=>{o.width=`${this.__getDistributedWidth(o)}px`}),this.__intrinsicWidthCache.clear()}__getParentColumnGroup(i){let e=(i.assignedSlot||i).parentElement;return e&&e!==this?e:null}__setVisibleCellContentAutoWidth(i,e){i._allCells.filter(t=>this.$.items.contains(t)?this.__viewportRowsCache.includes(t.parentElement):!0).forEach(t=>{t.__measuringAutoWidth=e,t.__measuringAutoWidth?(t.__originalWidth=t.style.width,t.style.width="auto",t.style.position="absolute"):(t.style.width=t.__originalWidth,delete t.__originalWidth,t.style.position="")}),e?this.$.scroller.setAttribute("measuring-auto-width",""):this.$.scroller.removeAttribute("measuring-auto-width")}__getAutoWidthCellsMaxWidth(i){return i._allCells.reduce((e,t)=>t.__measuringAutoWidth?Math.max(e,t.offsetWidth+1):e,0)}__calculateAndCacheIntrinsicWidths(i){i.forEach(e=>this.__setVisibleCellContentAutoWidth(e,!0)),i.forEach(e=>{let t=this.__getAutoWidthCellsMaxWidth(e);this.__intrinsicWidthCache.set(e,t)}),i.forEach(e=>this.__setVisibleCellContentAutoWidth(e,!1))}recalculateColumnWidths(){if(!this.__isReadyForColumnWidthCalculation()){this.__pendingRecalculateColumnWidths=!0;return}this._recalculateColumnWidths()}__tryToRecalculateColumnWidthsIfPending(){this.__pendingRecalculateColumnWidths&&(this.__pendingRecalculateColumnWidths=!1,this.recalculateColumnWidths())}__getAutoWidthColumns(){return this._getColumns().filter(i=>!i.hidden&&i.autoWidth)}__isReadyForColumnWidthCalculation(){if(!this._columnTree)return!1;let i=this.__getAutoWidthColumns().filter(n=>!customElements.get(n.localName));if(i.length)return Promise.all(i.map(n=>customElements.whenDefined(n.localName))).then(()=>{this.__tryToRecalculateColumnWidthsIfPending()}),!1;let e=[...this.$.items.children].some(n=>n.index===void 0),t=this._debouncerHiddenChanged&&this._debouncerHiddenChanged.isActive(),r=this.__debounceUpdateFrozenColumn&&this.__debounceUpdateFrozenColumn.isActive(),o=this.clientHeight>0;return!this._dataProviderController.isLoading()&&!e&&!me(this)&&!t&&!r&&o}};var Gn=s=>class extends s{static get properties(){return{columnReorderingAllowed:{type:Boolean,value:!1},_orderBaseScope:{type:Number,value:1e7}}}static get observers(){return["_updateOrders(_columnTree)"]}ready(){super.ready(),se(this,"track",this._onTrackEvent),this._reorderGhost=this.shadowRoot.querySelector('[part="reorder-ghost"]'),this.addEventListener("touchstart",this._onTouchStart.bind(this)),this.addEventListener("touchmove",this._onTouchMove.bind(this)),this.addEventListener("touchend",this._onTouchEnd.bind(this)),this.addEventListener("contextmenu",this._onContextMenu.bind(this))}_onContextMenu(e){this.hasAttribute("reordering")&&(e.preventDefault(),K||this._onTrackEnd())}_cancelReorderForMultiTouch(e){return e.touches.length>1?(clearTimeout(this._startTouchReorderTimeout),this._draggedColumn&&this._onTrackEnd(),!0):!1}_onTouchStart(e){this._cancelReorderForMultiTouch(e)||(this._startTouchReorderTimeout=setTimeout(()=>{this._onTrackStart({detail:{x:e.touches[0].clientX,y:e.touches[0].clientY}})},100))}_onTouchMove(e){this._cancelReorderForMultiTouch(e)||(this._draggedColumn&&e.preventDefault(),clearTimeout(this._startTouchReorderTimeout))}_onTouchEnd(){clearTimeout(this._startTouchReorderTimeout),this._onTrackEnd()}_onTrackEvent(e){if(e.detail.state==="start"){let t=e.composedPath(),r=t[t.indexOf(this.$.header)-2];if(!r||!r._content||r._content.contains(this.getRootNode().activeElement)||this.$.scroller.hasAttribute("column-resizing"))return;this._touchDevice||this._onTrackStart(e)}else e.detail.state==="track"?this._onTrack(e):e.detail.state==="end"&&this._onTrackEnd(e)}_onTrackStart(e){if(!this.columnReorderingAllowed)return;let t=e.composedPath?.();if(t?.slice(0,Math.max(0,t.indexOf(this))).some(o=>o.draggable))return;let r=this._cellFromPoint(e.detail.x,e.detail.y);if(!(!r||!r.part.contains("header-cell"))){for(this.toggleAttribute("reordering",!0),this._draggedColumn=r._column;this._draggedColumn.parentElement.childElementCount===1;)this._draggedColumn=this._draggedColumn.parentElement;this._setSiblingsReorderStatus(this._draggedColumn,"allowed"),this._draggedColumn._reorderStatus="dragging",this._updateGhost(r),this._reorderGhost.style.visibility="visible",this._updateGhostPosition(e.detail.x,this._touchDevice?e.detail.y-50:e.detail.y),this._autoScroller()}}_onTrack(e){if(!this._draggedColumn)return;let t=this._cellFromPoint(e.detail.x,e.detail.y);if(!t)return;let r=this._getTargetColumn(t,this._draggedColumn);if(this._isSwapAllowed(this._draggedColumn,r)&&this._isSwappableByPosition(r,e.detail.x)){let o=this._columnTree.findIndex(h=>h.includes(r)),n=this._getColumnsInOrder(o),a=n.indexOf(this._draggedColumn),l=n.indexOf(r),d=a<l?1:-1;for(let h=a;h!==l;h+=d)this._swapColumnOrders(this._draggedColumn,n[h+d])}this._updateGhostPosition(e.detail.x,this._touchDevice?e.detail.y-50:e.detail.y),this._lastDragClientX=e.detail.x}_onTrackEnd(){this._draggedColumn&&(this.toggleAttribute("reordering",!1),this._draggedColumn._reorderStatus="",this._setSiblingsReorderStatus(this._draggedColumn,""),this._draggedColumn=null,this._lastDragClientX=null,this._reorderGhost.style.visibility="hidden",this.dispatchEvent(new CustomEvent("column-reorder",{detail:{columns:this._getColumnsInOrder()}})))}_getColumnsInOrder(e=this._columnTree.length-1){return this._columnTree[e].filter(t=>!t.hidden).sort((t,r)=>t._order-r._order)}_cellFromPoint(e=0,t=0){this._draggedColumn||this.$.scroller.toggleAttribute("no-content-pointer-events",!0);let r=this.shadowRoot.elementFromPoint(e,t);return this.$.scroller.toggleAttribute("no-content-pointer-events",!1),this._getCellFromElement(r)}_getCellFromElement(e){if(e){if(e._column)return e;let{parentElement:t}=e;if(t?._focusButton===e)return t}return null}_updateGhostPosition(e,t){let r=this._reorderGhost.getBoundingClientRect(),o=e-r.width/2,n=t-r.height/2,a=parseInt(this._reorderGhost._left||0),l=parseInt(this._reorderGhost._top||0);this._reorderGhost._left=a-(r.left-o),this._reorderGhost._top=l-(r.top-n),this._reorderGhost.style.transform=`translate(${this._reorderGhost._left}px, ${this._reorderGhost._top}px)`}_updateGhost(e){let t=this._reorderGhost;t.textContent=e._content.innerText;let r=window.getComputedStyle(e);return["boxSizing","display","width","height","background","alignItems","padding","border","flex-direction","overflow"].forEach(o=>{t.style[o]=r[o]}),t}_updateOrders(e){e!==void 0&&(e[0].forEach(t=>{t._order=0}),Rn(e[0],this._orderBaseScope,0))}_resetColumnOrder(){this._columnTree===void 0||this._columnTree.every(t=>t.every((r,o)=>o===0||r._order>=t[o-1]._order))||(this._columnTree=this._getColumnTree())}_setSiblingsReorderStatus(e,t){H(e.parentNode,r=>{/column/u.test(r.localName)&&this._isSwapAllowed(r,e)&&(r._reorderStatus=t)})}_autoScroller(){if(this._lastDragClientX){let e=this._lastDragClientX-this.getBoundingClientRect().right+50,t=this.getBoundingClientRect().left-this._lastDragClientX+50;e>0?this.$.table.scrollLeft+=e/10:t>0&&(this.$.table.scrollLeft-=t/10)}this._draggedColumn&&setTimeout(()=>this._autoScroller(),10)}_isSwapAllowed(e,t){if(e&&t){let r=e!==t,o=e.parentElement===t.parentElement,n=e.frozen&&t.frozen||e.frozenToEnd&&t.frozenToEnd||!e.frozen&&!e.frozenToEnd&&!t.frozen&&!t.frozenToEnd;return r&&o&&n}}_isSwappableByPosition(e,t){let r=Array.from(this.$.header.querySelectorAll('tr:not([hidden]) [part~="cell"]')).find(a=>e.contains(a._column)),o=this.$.header.querySelector("tr:not([hidden]) [reorder-status=dragging]").getBoundingClientRect(),n=r.getBoundingClientRect();return n.left>o.left?t>n.right-o.width:t<n.left+o.width}_swapColumnOrders(e,t){[e._order,t._order]=[t._order,e._order];let[r,o]=e._order<t._order?[e,t]:[t,e];[...this.$.header.children,...this.$.footer.children,...this.$.items.children,this.$.sizer].forEach(n=>{let a=Y(n),l=a.filter(h=>r.contains(h._column)),d=a.find(h=>o.contains(h._column));l.forEach(h=>d.before(h)),n.__cells&&(n.__cells=n.__cells.toSorted((h,c)=>h._column._order-c._column._order))}),this._debounceUpdateFrozenColumn(),this._updateFirstAndLastColumn()}_getTargetColumn(e,t){if(e&&t){let r=e._column;for(;r.parentElement!==t.parentElement&&r!==this;)r=r.parentElement;return r.parentElement===t.parentElement?r:e._column}}};var Kn=s=>class extends s{ready(){super.ready();let e=this.$.scroller;se(e,"track",this._onHeaderTrack.bind(this)),e.addEventListener("touchstart",t=>{t.touches.length>1&&e.removeAttribute("column-resizing")}),e.addEventListener("touchmove",t=>{if(t.touches.length>1){e.removeAttribute("column-resizing");return}e.hasAttribute("column-resizing")&&t.preventDefault()}),e.addEventListener("contextmenu",t=>t.target.part.contains("resize-handle")&&t.preventDefault()),e.addEventListener("mousedown",t=>t.target.part.contains("resize-handle")&&t.preventDefault())}_onHeaderTrack(e){let t=e.target;if(t.part.contains("resize-handle")){if(e.detail.state!=="start"&&!this.$.scroller.hasAttribute("column-resizing"))return;let o=t.parentElement._column;for(this.$.scroller.toggleAttribute("column-resizing",!0);o.localName==="vaadin-grid-column-group";)o=o._childColumns.slice(0).sort((c,f)=>c._order-f._order).filter(c=>!c.hidden).pop();let n=this.__isRTL,a=e.detail.x,l=Array.from(this.$.header.querySelectorAll('[part~="row"]:last-child [part~="cell"]')),d=l.find(c=>c._column===o);if(d.offsetWidth){let c=getComputedStyle(d._content),f=10+parseInt(c.paddingLeft)+parseInt(c.paddingRight)+parseInt(c.borderLeftWidth)+parseInt(c.borderRightWidth)+parseInt(c.marginLeft)+parseInt(c.marginRight),y,w=d.offsetWidth,S=d.getBoundingClientRect();d.hasAttribute("frozen-to-end")?y=w+(n?a-S.right:S.left-a):y=w+(n?S.left-a:a-S.right),o.width=`${Math.max(f,y)}px`,o.flexGrow=0}l.slice(0,l.indexOf(d)).forEach(c=>{c._column.width=`${c.offsetWidth}px`,c._column.flexGrow=0});let h=this._frozenToEndCells[0];if(h&&this.$.table.scrollWidth>this.$.table.offsetWidth){let c=h.getBoundingClientRect(),f=a-(n?c.right:c.left);(n&&f<=0||!n&&f>=0)&&(this.$.table.scrollLeft+=f)}e.detail.state==="end"&&(this.$.scroller.toggleAttribute("column-resizing",!1),this.dispatchEvent(new CustomEvent("column-resize",{detail:{resizedColumn:o}}))),this._resizeHandler()}}};var Yn=s=>class extends s{static get properties(){return{size:{type:Number,notify:!0,sync:!0},_flatSize:{type:Number,sync:!0},pageSize:{type:Number,value:50,observer:"_pageSizeChanged",sync:!0},dataProvider:{type:Object,notify:!0,observer:"_dataProviderChanged",sync:!0},loading:{type:Boolean,notify:!0,readOnly:!0,reflectToAttribute:!0},_hasData:{type:Boolean,value:!1,sync:!0},itemHasChildrenPath:{type:String,value:"children",observer:"__itemHasChildrenPathChanged",sync:!0},itemIdPath:{type:String,value:null,sync:!0},expandedItems:{type:Object,notify:!0,value:()=>[],sync:!0},__expandedKeys:{type:Object,computed:"__computeExpandedKeys(itemIdPath, expandedItems)"}}}static get observers(){return["_sizeChanged(size)","_expandedItemsChanged(expandedItems)"]}constructor(){super(),this._dataProviderController=new lt(this,{size:this.size||0,pageSize:this.pageSize,getItemId:this.getItemId.bind(this),isExpanded:this._isExpanded.bind(this),dataProvider:this.dataProvider?this.dataProvider.bind(this):null,dataProviderParams:()=>({sortOrders:this._mapSorters(),filters:this._mapFilters()})}),this._dataProviderController.addEventListener("page-requested",this._onDataProviderPageRequested.bind(this)),this._dataProviderController.addEventListener("page-received",this._onDataProviderPageReceived.bind(this)),this._dataProviderController.addEventListener("page-loaded",this._onDataProviderPageLoaded.bind(this))}_sizeChanged(e){this._dataProviderController.rootCache.size=e,this._dataProviderController.recalculateFlatSize(),this._flatSize=this._dataProviderController.flatSize}__itemHasChildrenPathChanged(e,t){!t&&e==="children"||this.requestContentUpdate()}__getRowLevel(e){let{level:t}=this._dataProviderController.getFlatIndexContext(e.index);return t}__getRowItem(e){let{item:t}=this._dataProviderController.getFlatIndexContext(e.index);return t}__ensureRowItem(e){this._dataProviderController.ensureFlatIndexLoaded(e.index)}__ensureRowHierarchy(e){this._dataProviderController.ensureFlatIndexHierarchy(e.index)}getItemId(e){return this.itemIdPath?G(this.itemIdPath,e):e}_isExpanded(e){return this.__expandedKeys&&this.__expandedKeys.has(this.getItemId(e))}_hasChildren(e){return this.itemHasChildrenPath&&e&&!!G(this.itemHasChildrenPath,e)}_expandedItemsChanged(){this._dataProviderController.recalculateFlatSize(),this._flatSize=this._dataProviderController.flatSize,this.__updateVisibleRows()}__computeExpandedKeys(e,t){let r=t||[],o=new Set;return r.forEach(n=>{o.add(this.getItemId(n))}),o}expandItem(e){this._isExpanded(e)||(this.expandedItems=[...this.expandedItems,e])}collapseItem(e){this._isExpanded(e)&&(this.expandedItems=this.expandedItems.filter(t=>!this._itemsEqual(t,e)))}_onDataProviderPageRequested(){this._setLoading(!0)}_onDataProviderPageReceived(){this._flatSize!==this._dataProviderController.flatSize&&(this._shouldLoadAllRenderedRowsAfterPageLoad=!0,this._flatSize=this._dataProviderController.flatSize),this._getRenderedRows().forEach(e=>this.__ensureRowHierarchy(e)),this._hasData=!0}_onDataProviderPageLoaded(){this._debouncerApplyCachedData=x.debounce(this._debouncerApplyCachedData,R.after(0),()=>{this._setLoading(!1);let e=this._shouldLoadAllRenderedRowsAfterPageLoad;this._shouldLoadAllRenderedRowsAfterPageLoad=!1,this._getRenderedRows().forEach(t=>{this.__updateRow(t),e&&this.__ensureRowItem(t)}),this.__scrollToPendingIndexes(),this.__dispatchPendingBodyCellFocus()}),this._dataProviderController.isLoading()||this._debouncerApplyCachedData.flush()}__debounceClearCache(){this.__clearCacheDebouncer=x.debounce(this.__clearCacheDebouncer,z,()=>this.clearCache())}clearCache(){this._dataProviderController.clearCache(),this._dataProviderController.rootCache.size=this.size||0,this._dataProviderController.recalculateFlatSize(),this._hasData=!1,this.__updateVisibleRows(),(!this.__virtualizer||!this.__virtualizer.size)&&this._dataProviderController.loadFirstPage()}_pageSizeChanged(e,t){this._dataProviderController.setPageSize(e),t!==void 0&&e!==t&&this.clearCache()}_checkSize(){this.size===void 0&&this._flatSize===0&&console.warn("The <vaadin-grid> needs the total number of items in order to display rows, which you can specify either by setting the `size` property, or by providing it to the second argument of the `dataProvider` function `callback` call.")}_dataProviderChanged(e,t){this._dataProviderController.setDataProvider(e?e.bind(this):null),t!==void 0&&this.clearCache(),this._ensureFirstPageLoaded(),this._debouncerCheckSize=x.debounce(this._debouncerCheckSize,R.after(2e3),this._checkSize.bind(this))}_ensureFirstPageLoaded(){this._hasData||this._dataProviderController.loadFirstPage()}_itemsEqual(e,t){return this.getItemId(e)===this.getItemId(t)}scrollToIndex(...e){if(!this.__virtualizer||!this.clientHeight||!this._columnTree){this.__pendingScrollToIndexes=e;return}let t;for(;t!==(t=this._dataProviderController.getFlatIndexByPath(e));)this._scrollToFlatIndex(t);this._dataProviderController.isLoading()&&(this.__pendingScrollToIndexes=e)}__scrollToPendingIndexes(){if(this.__pendingScrollToIndexes&&this.$.items.children.length){let e=this.__pendingScrollToIndexes;delete this.__pendingScrollToIndexes,this.scrollToIndex(...e)}}};var Ot={BETWEEN:"between",ON_TOP:"on-top",ON_TOP_OR_BETWEEN:"on-top-or-between",ON_GRID:"on-grid"},De={ON_TOP:"on-top",ABOVE:"above",BELOW:"below",EMPTY:"empty"},Qn=s=>class extends s{static get properties(){return{dropMode:{type:String,sync:!0},rowsDraggable:{type:Boolean,sync:!0},dragFilter:{type:Function,sync:!0},dropFilter:{type:Function,sync:!0},__dndAutoScrollThreshold:{value:50},__draggedItems:{value:()=>[]}}}static get observers(){return["_dragDropAccessChanged(rowsDraggable, dropMode, dragFilter, dropFilter, loading)"]}constructor(){super(),this.__onDocumentDragStart=this.__onDocumentDragStart.bind(this)}ready(){super.ready(),this.$.table.addEventListener("dragstart",this._onDragStart.bind(this)),this.$.table.addEventListener("dragend",this._onDragEnd.bind(this)),this.$.table.addEventListener("dragover",this._onDragOver.bind(this)),this.$.table.addEventListener("dragleave",this._onDragLeave.bind(this)),this.$.table.addEventListener("drop",this._onDrop.bind(this)),this.$.table.addEventListener("dragenter",e=>{this.dropMode&&(e.preventDefault(),e.stopPropagation())})}connectedCallback(){super.connectedCallback(),document.addEventListener("dragstart",this.__onDocumentDragStart,{capture:!0})}disconnectedCallback(){super.disconnectedCallback(),document.removeEventListener("dragstart",this.__onDocumentDragStart,{capture:!0})}_onDragStart(e){if(this.rowsDraggable){let t=e.target;if(t.localName==="vaadin-grid-cell-content"&&(t=t.assignedSlot.parentNode.parentNode),t.parentNode!==this.$.items)return;if(e.stopPropagation(),this.toggleAttribute("dragging-rows",!0),this._safari){let a=t.style.transform;t.style.top=/translateY\((.*)\)/u.exec(a)[1],t.style.transform="none",requestAnimationFrame(()=>{t.style.top="",t.style.transform=a})}let r=t.getBoundingClientRect();e.dataTransfer.setDragImage(t,e.clientX-r.left,e.clientY-r.top);let o=[t];this._isSelected(t._item)&&(o=this.__getViewportRows().filter(a=>this._isSelected(a._item)).filter(a=>!this.dragFilter||this.dragFilter(this.__getRowModel(a)))),this.__draggedItems=o.map(a=>a._item),e.dataTransfer.setData("text",this.__formatDefaultTransferData(o)),ke(t,{dragstart:o.length>1?`${o.length}`:""}),this.style.setProperty("--_grid-drag-start-x",`${e.clientX-r.left+20}px`),this.style.setProperty("--_grid-drag-start-y",`${e.clientY-r.top+10}px`),requestAnimationFrame(()=>{ke(t,{dragstart:!1}),this.style.setProperty("--_grid-drag-start-x",""),this.style.setProperty("--_grid-drag-start-y",""),this.requestContentUpdate()});let n=new CustomEvent("grid-dragstart",{detail:{draggedItems:[...this.__draggedItems],setDragData:(a,l)=>e.dataTransfer.setData(a,l),setDraggedItemsCount:a=>t.setAttribute("dragstart",a)}});n.originalEvent=e,this.dispatchEvent(n)}}_onDragEnd(e){this.toggleAttribute("dragging-rows",!1),e.stopPropagation();let t=new CustomEvent("grid-dragend");t.originalEvent=e,this.dispatchEvent(t),this.__draggedItems=[],this.requestContentUpdate()}_onDragLeave(e){this.dropMode&&(e.stopPropagation(),this._clearDragStyles())}_onDragOver(e){if(this.dropMode){if(this._dropLocation=void 0,this._dragOverItem=void 0,this.__dndAutoScroll(e.clientY)){this._clearDragStyles();return}let t=e.composedPath().find(r=>r.localName==="tr");if(this.__updateRowScrollPositionProperty(t),!this._flatSize||this.dropMode===Ot.ON_GRID)this._dropLocation=De.EMPTY;else if(!t||t.parentNode!==this.$.items){if(t)return;if(this.dropMode===Ot.BETWEEN||this.dropMode===Ot.ON_TOP_OR_BETWEEN)t=Array.from(this.$.items.children).filter(r=>!r.hidden).pop(),this._dropLocation=De.BELOW;else return}else{let r=t.getBoundingClientRect();if(this._dropLocation=De.ON_TOP,this.dropMode===Ot.BETWEEN){let o=e.clientY-r.top<r.bottom-e.clientY;this._dropLocation=o?De.ABOVE:De.BELOW}else this.dropMode===Ot.ON_TOP_OR_BETWEEN&&(e.clientY-r.top<r.height/3?this._dropLocation=De.ABOVE:e.clientY-r.top>r.height/3*2&&(this._dropLocation=De.BELOW))}if(t?.hasAttribute("drop-disabled")){this._dropLocation=void 0;return}e.stopPropagation(),e.preventDefault(),this._dropLocation===De.EMPTY?this.toggleAttribute("dragover",!0):t?(this._dragOverItem=t._item,t.getAttribute("dragover")!==this._dropLocation&&hs(t,{dragover:this._dropLocation})):this._clearDragStyles()}}__onDocumentDragStart(e){if(e.target.contains(this)){let t=[e.target,this.$.items,this.$.scroller],r=t.map(o=>o.style.cssText);this.$.table.scrollHeight>2e4&&(this.$.scroller.style.display="none"),kt&&(e.target.style.willChange="transform"),it&&(this.$.items.style.flexShrink=1),requestAnimationFrame(()=>{t.forEach((o,n)=>{o.style.cssText=r[n]})})}}__dndAutoScroll(e){if(this.__dndAutoScrolling)return!0;let t=this.$.header.getBoundingClientRect().bottom,r=this.$.footer.getBoundingClientRect().top,o=t-e+this.__dndAutoScrollThreshold,n=e-r+this.__dndAutoScrollThreshold,a=0;if(n>0?a=n*2:o>0&&(a=-o*2),a){let l=this.$.table.scrollTop;if(this.$.table.scrollTop+=a,l!==this.$.table.scrollTop)return this.__dndAutoScrolling=!0,setTimeout(()=>{this.__dndAutoScrolling=!1},20),!0}}__getViewportRows(){let e=this.$.header.getBoundingClientRect().bottom,t=this.$.footer.getBoundingClientRect().top;return Array.from(this.$.items.children).filter(r=>{let o=r.getBoundingClientRect();return o.bottom>e&&o.top<t})}_clearDragStyles(){this.removeAttribute("dragover"),H(this.$.items,e=>{hs(e,{dragover:null})})}__updateDragSourceParts(e,t){ke(e,{"drag-source":this.__draggedItems.includes(t.item)})}_onDrop(e){if(this.dropMode&&this._dropLocation){e.stopPropagation(),e.preventDefault();let t=e.dataTransfer.types&&Array.from(e.dataTransfer.types).map(o=>({type:o,data:e.dataTransfer.getData(o)}));this._clearDragStyles();let r=new CustomEvent("grid-drop",{bubbles:e.bubbles,cancelable:e.cancelable,detail:{dropTargetItem:this._dragOverItem,dropLocation:this._dropLocation,dragData:t}});r.originalEvent=e,this.dispatchEvent(r)}}__formatDefaultTransferData(e){return e.map(t=>Y(t).filter(r=>!r.hidden).map(r=>r._content.textContent.trim()).filter(r=>r).join("	")).join(`
`)}_dragDropAccessChanged(){this.filterDragAndDrop()}filterDragAndDrop(){H(this.$.items,e=>{e.hidden||this._filterDragAndDrop(e,this.__getRowModel(e))})}_filterDragAndDrop(e,t){let r=this.loading||e.hasAttribute("loading"),o=!this.rowsDraggable||r||this.dragFilter&&!this.dragFilter(t),n=!this.dropMode||r||this.dropFilter&&!this.dropFilter(t),a=kt?"draggable-source":"draggable";ge(e,l=>{o?l._content.removeAttribute(a):l._content.setAttribute(a,!0)}),ke(e,{"drag-disabled":!!o,"drop-disabled":!!n})}};function Xn(s,i){if(!s||!i||s.length!==i.length)return!1;for(let e=0,t=s.length;e<t;e++)if(s[e]instanceof Array&&i[e]instanceof Array){if(!Xn(s[e],i[e]))return!1}else if(s[e]!==i[e])return!1;return!0}var Zn=s=>class extends s{static get properties(){return{_columnTree:{type:Object,sync:!0}}}ready(){super.ready(),this._addNodeObserver()}_hasColumnGroups(e){return e.some(t=>t.localName==="vaadin-grid-column-group")}_getChildColumns(e){return _t.getColumns(e)}_flattenColumnGroups(e){return e.map(t=>t.localName==="vaadin-grid-column-group"?this._getChildColumns(t):[t]).reduce((t,r)=>t.concat(r),[])}_getColumnTree(){let e=_t.getColumns(this),t=[e],r=e;for(;this._hasColumnGroups(r);)r=this._flattenColumnGroups(r),t.push(r);return t}_debounceUpdateColumnTree(){this.__updateColumnTreeDebouncer=x.debounce(this.__updateColumnTreeDebouncer,z,()=>this._updateColumnTree())}_updateColumnTree(){let e=this._getColumnTree();Xn(e,this._columnTree)||(this._columnTree=e)}_addNodeObserver(){this._observer=new _t(this,(e,t)=>{let r=t.flatMap(n=>n._allCells),o=n=>r.filter(a=>a?._content.contains(n)).length;this.__removeSorters(this._sorters.filter(o)),this.__removeFilters(this._filters.filter(o)),this._debounceUpdateColumnTree(),this._debouncerCheckImports=x.debounce(this._debouncerCheckImports,R.after(2e3),this._checkImports.bind(this)),this._ensureFirstPageLoaded()})}_checkImports(){["vaadin-grid-column-group","vaadin-grid-filter","vaadin-grid-filter-column","vaadin-grid-tree-toggle","vaadin-grid-selection-column","vaadin-grid-sort-column","vaadin-grid-sorter"].forEach(e=>{this.querySelector(e)&&!customElements.get(e)&&console.warn(`Make sure you have imported the required module for <${e}> element.`)})}_updateFirstAndLastColumn(){Array.from(this.shadowRoot.querySelectorAll("tr")).forEach(e=>this._updateFirstAndLastColumnForRow(e))}_updateFirstAndLastColumnForRow(e){Y(e).forEach((t,r,o)=>{ve(t,"first-column",r===0),ve(t,"last-column",r===o.length-1)})}_isColumnElement(e){return e.nodeType===Node.ELEMENT_NODE&&/\bcolumn\b/u.test(e.localName)}};var Jn=s=>class extends s{getEventContext(e){let t={},{cell:r}=this._getGridEventLocation(e);return r&&(t.section=["body","header","footer","details"].find(o=>r.part.contains(`${o}-cell`)),r._column&&(t.column=r._column),(t.section==="body"||t.section==="details")&&Object.assign(t,this.__getRowModel(r.__parentRow))),t}};var ea=s=>class extends s{static get properties(){return{_filters:{type:Array,value:()=>[]}}}constructor(){super(),this._filterChanged=this._filterChanged.bind(this),this.addEventListener("filter-changed",this._filterChanged)}_filterChanged(e){e.stopPropagation(),this.__addFilter(e.target),this.__applyFilters()}__removeFilters(e){e.length!==0&&(this._filters=this._filters.filter(t=>e.indexOf(t)<0),this.__applyFilters())}__addFilter(e){this._filters.indexOf(e)===-1&&this._filters.push(e)}__applyFilters(){this.dataProvider&&this.isAttached&&this.clearCache()}_mapFilters(){return this._filters.map(e=>({path:e.path,value:e.value}))}};function Fi(s){return s instanceof HTMLTableRowElement}function Oi(s){return s instanceof HTMLTableCellElement}function Ne(s){return s.matches('[part~="details-cell"]')}var ta=s=>class extends s{static get properties(){return{_headerFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_itemsFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_footerFocusable:{type:Object,observer:"_focusableChanged",sync:!0},_navigatingIsHidden:Boolean,_focusedItemIndex:{type:Number,value:0},_focusedColumnOrder:Number,_focusedCell:{type:Object,observer:"_focusedCellChanged",sync:!0},interacting:{type:Boolean,value:!1,reflectToAttribute:!0,readOnly:!0,observer:"_interactingChanged"}}}get __rowFocusMode(){return[this._headerFocusable,this._itemsFocusable,this._footerFocusable].some(Fi)}set __rowFocusMode(e){["_itemsFocusable","_footerFocusable","_headerFocusable"].forEach(t=>{let r=this[t];if(e){let o=r?.parentElement;Oi(r)?this[t]=o:Oi(o)&&(this[t]=o.parentElement)}else if(!e&&Fi(r)){let o=r.firstElementChild;this[t]=o._focusButton||o}})}get _visibleItemsCount(){return this._lastVisibleIndex-this._firstVisibleIndex-1}ready(){super.ready(),!(this._ios||this._android)&&(this.addEventListener("keydown",this._onKeyDown),this.addEventListener("keyup",this._onKeyUp),this.addEventListener("focusin",this._onFocusIn),this.addEventListener("focusout",this._onFocusOut),this.$.table.addEventListener("focusin",this._onContentFocusIn.bind(this)),this.addEventListener("mousedown",()=>{this.toggleAttribute("navigating",!1),this._isMousedown=!0,this._focusedColumnOrder=void 0}),this.addEventListener("mouseup",()=>{this._isMousedown=!1}))}_focusableChanged(e,t){t&&t.setAttribute("tabindex","-1"),e&&this._updateGridSectionFocusTarget(e)}_focusedCellChanged(e,t){t&&E(t,"focused-cell",!1),e&&E(e,"focused-cell",!0)}_interactingChanged(){this._updateGridSectionFocusTarget(this._headerFocusable),this._updateGridSectionFocusTarget(this._itemsFocusable),this._updateGridSectionFocusTarget(this._footerFocusable)}__updateItemsFocusable(){if(!this._itemsFocusable)return;let e=this.shadowRoot.activeElement===this._itemsFocusable;this._getRenderedRows().forEach(t=>{if(t.index===this._focusedItemIndex)if(this.__rowFocusMode)this._itemsFocusable=t;else{let r=this._itemsFocusable.parentElement,o=this._itemsFocusable;if(r){Oi(r)&&(o=r,r=r.parentElement);let n=[...r.children].indexOf(o);this._itemsFocusable=this.__getFocusable(t,t.children[n])}}}),e&&this._itemsFocusable.focus()}_onKeyDown(e){let t=e.key,r;switch(t){case"ArrowUp":case"ArrowDown":case"ArrowLeft":case"ArrowRight":case"PageUp":case"PageDown":case"Home":case"End":r="Navigation";break;case"Enter":case"Escape":case"F2":r="Interaction";break;case"Tab":r="Tab";break;case" ":r="Space";break;default:break}this._detectInteracting(e),this.interacting&&r!=="Interaction"&&(r=void 0),r&&this[`_on${r}KeyDown`](e,t)}__ensureFlatIndexInViewport(e){let t=[...this.$.items.children].find(r=>r.index===e);t?this.__scrollIntoViewport(t):this._scrollToFlatIndex(e)}__isRowExpandable(e){return this._hasChildren(e._item)&&!this._isExpanded(e._item)}__isRowCollapsible(e){return this._isExpanded(e._item)}_onNavigationKeyDown(e,t){e.preventDefault();let r=this.__isRTL,o=e.composedPath().find(Fi),n=e.composedPath().find(Oi),a=0,l=0;switch(t){case"ArrowRight":a=r?-1:1;break;case"ArrowLeft":a=r?1:-1;break;case"Home":this.__rowFocusMode||e.ctrlKey?l=-1/0:a=-1/0;break;case"End":this.__rowFocusMode||e.ctrlKey?l=1/0:a=1/0;break;case"ArrowDown":l=1;break;case"ArrowUp":l=-1;break;case"PageDown":if(this.$.items.contains(o)){let c=this.__getIndexInGroup(o,this._focusedItemIndex);this._scrollToFlatIndex(c)}l=this._visibleItemsCount;break;case"PageUp":l=-this._visibleItemsCount;break;default:break}if(this.__rowFocusMode&&!o||!this.__rowFocusMode&&!n)return;let d=r?"ArrowLeft":"ArrowRight",h=r?"ArrowRight":"ArrowLeft";if(t===d){if(this.__rowFocusMode){if(this.__isRowExpandable(o)){this.expandItem(o._item);return}this.__rowFocusMode=!1,this._onCellNavigation(o.firstElementChild,0,0);return}}else if(t===h){if(this.__rowFocusMode){if(this.__isRowCollapsible(o)){this.collapseItem(o._item);return}}else if(n===o.firstElementChild||Ne(n)){this.__rowFocusMode=!0,this._onRowNavigation(o,0);return}}this.__rowFocusMode?this._onRowNavigation(o,l):this._onCellNavigation(n,a,l)}_onRowNavigation(e,t){let{dstRow:r}=this.__navigateRows(t,e);r&&r.focus()}__getIndexInGroup(e,t){let r=e.parentNode;return r===this.$.items?t??e.index:[...r.children].indexOf(e)}__navigateRows(e,t,r){let o=this.__getIndexInGroup(t,this._focusedItemIndex),n=t.parentNode,a=(n===this.$.items?this._flatSize:n.children.length)-1,l=Math.max(0,Math.min(o+e,a));if(n!==this.$.items){if(l>o)for(;l<a&&n.children[l].hidden;)l+=1;else if(l<o)for(;l>0&&n.children[l].hidden;)l-=1;return this.toggleAttribute("navigating",!0),{dstRow:n.children[l]}}let d=!1;if(r){let h=Ne(r);if(n===this.$.items){let c=t._item,{item:f}=this._dataProviderController.getFlatIndexContext(l);h?d=e===0:d=e===1&&this._isDetailsOpened(c)||e===-1&&l!==o&&this._isDetailsOpened(f),d!==h&&(e===1&&d||e===-1&&!d)&&(l=o)}}return this.__ensureFlatIndexInViewport(l),this._focusedItemIndex=l,this.toggleAttribute("navigating",!0),{dstRow:[...n.children].find(h=>!h.hidden&&h.index===l),dstIsRowDetails:d}}_onCellNavigation(e,t,r){let o=e.parentNode,{dstRow:n,dstIsRowDetails:a}=this.__navigateRows(r,o,e);if(!n)return;let l=Ne(e),d=o.parentNode;if(this._focusedColumnOrder===void 0&&(l?this._focusedColumnOrder=0:this._focusedColumnOrder=e._column._order),a)[...n.children].find(Ne).focus();else{let h=this.__getIndexInGroup(n,this._focusedItemIndex),c=this._getColumns(d,h).filter(O=>!O.hidden),f=c.map(O=>O._order).sort((O,X)=>O-X),y=f.length-1,w=f.indexOf(f.slice(0).sort((O,X)=>Math.abs(O-this._focusedColumnOrder)-Math.abs(X-this._focusedColumnOrder))[0]),S=r===0&&l?w:Math.max(0,Math.min(w+t,y));S!==w&&(this._focusedColumnOrder=void 0);let Q=c.find(O=>O._order===f[S]),te;if(this.$.items.contains(e)){let O=[...this.$.sizer.children].find(X=>X._column===Q);this._lazyColumns&&(this.__isColumnInViewport(O._column)||O.scrollIntoView(),this.__updateColumnsBodyContentHidden(),this.__updateHorizontalScrollPosition()),te=[...n.children].find(X=>X._column===O._column),this._scrollHorizontallyToCell(te)}else te=[...n.children].find(O=>O._column.contains(Q)),this._scrollHorizontallyToCell(te);te.focus({preventScroll:!0})}}_onInteractionKeyDown(e,t){let r=e.composedPath()[0],o=r.localName==="input"&&!/^(button|checkbox|color|file|image|radio|range|reset|submit)$/iu.test(r.type),n;switch(t){case"Enter":n=this.interacting?!o:!0;break;case"Escape":n=!1;break;case"F2":n=!this.interacting;break;default:break}let{cell:a}=this._getGridEventLocation(e);if(this.interacting!==n&&a!==null)if(n){let l=a._content.querySelector("[focus-target]")||[...a._content.querySelectorAll("*")].find(d=>this._isFocusable(d));l&&(e.preventDefault(),l.focus(),this._setInteracting(!0),this.toggleAttribute("navigating",!1))}else e.preventDefault(),this._focusedColumnOrder=void 0,a.focus(),this._setInteracting(!1),this.toggleAttribute("navigating",!0);t==="Escape"&&this._hideTooltip(!0)}_predictFocusStepTarget(e,t){let r=[this.$.table,this._headerFocusable,this.__emptyState?this.$.emptystatecell:this._itemsFocusable,this._footerFocusable,this.$.focusexit],o=r.indexOf(e);for(o+=t;o>=0&&o<=r.length-1;){let a=r[o];if(a&&!this.__rowFocusMode&&(a=r[o].parentNode),!a||a.hidden)o+=t;else break}let n=r[o];if(n&&!this.__isHorizontallyInViewport(n)){let a=this._getColumnsInOrder().find(l=>this.__isColumnInViewport(l));if(a)if(n===this._headerFocusable)n=a._headerCell;else if(n===this._itemsFocusable){let l=n._column._cells.indexOf(n);n=a._cells[l]}else n===this._footerFocusable&&(n=a._footerCell)}return n}_onTabKeyDown(e){let t=this._predictFocusStepTarget(e.composedPath()[0],e.shiftKey?-1:1);t&&(e.stopPropagation(),t===this._itemsFocusable&&(this.__ensureFlatIndexInViewport(this._focusedItemIndex),this.__updateItemsFocusable(),t=this._itemsFocusable),t.focus(),t!==this.$.table&&t!==this.$.focusexit&&e.preventDefault(),this.toggleAttribute("navigating",!0))}_onSpaceKeyDown(e){e.preventDefault();let t=e.composedPath()[0],r=Fi(t);(r||!t._content||!t._content.firstElementChild)&&this.dispatchEvent(new CustomEvent(r?"row-activate":"cell-activate",{detail:{model:this.__getRowModel(r?t:t.parentElement)}}))}_onKeyUp(e){if(!/^( |SpaceBar)$/u.test(e.key)||this.interacting)return;e.preventDefault();let t=e.composedPath()[0],r=t._content&&t._content.firstElementChild||t,o=this.hasAttribute("navigating"),n=new MouseEvent("click",{shiftKey:e.shiftKey,bubbles:!0,composed:!0,cancelable:!0});n.skipCellActivate=r===t,r.dispatchEvent(n),this.toggleAttribute("navigating",o)}_onFocusIn(e){this._isMousedown||this.toggleAttribute("navigating",!0);let t=e.composedPath()[0];t===this.$.table||t===this.$.focusexit?(this._isMousedown||this._predictFocusStepTarget(t,t===this.$.table?1:-1).focus(),this._setInteracting(!1)):this._detectInteracting(e)}_onFocusOut(e){this.toggleAttribute("navigating",!1),this._detectInteracting(e),this._hideTooltip(),this._focusedCell=null}_onContentFocusIn(e){let{section:t,cell:r,row:o}=this._getGridEventLocation(e);if(!(!r&&!this.__rowFocusMode)&&(this._detectInteracting(e),t&&(r||o)))if(this._activeRowGroup=t,t===this.$.header?this._headerFocusable=this.__getFocusable(o,r):t===this.$.items?(this._itemsFocusable=this.__getFocusable(o,r),this._focusedItemIndex=o.index):t===this.$.footer&&(this._footerFocusable=this.__getFocusable(o,r)),r){let n=this.getEventContext(e);this.__pendingBodyCellFocus=this.loading&&n.section==="body",!this.__pendingBodyCellFocus&&r!==this.$.emptystatecell&&r.dispatchEvent(new CustomEvent("cell-focus",{bubbles:!0,composed:!0,detail:{context:n}})),this._focusedCell=r._focusButton||r,B()&&e.target===r&&this._showTooltip(e)}else this._focusedCell=null}__dispatchPendingBodyCellFocus(){this.__pendingBodyCellFocus&&this.shadowRoot.activeElement===this._itemsFocusable&&this._itemsFocusable.dispatchEvent(new Event("focusin",{bubbles:!0,composed:!0}))}__getFocusable(e,t){return this.__rowFocusMode?e:t._focusButton||t}_detectInteracting(e){let t=e.composedPath().some(r=>r.localName==="slot"&&this.shadowRoot.contains(r));this._setInteracting(t),this.__updateHorizontalScrollPosition()}_updateGridSectionFocusTarget(e){if(!e)return;let t=this._getGridSectionFromFocusTarget(e),r=this.interacting&&t===this._activeRowGroup;e.tabIndex=r?-1:0}_preventScrollerRotatingCellFocus(){this._activeRowGroup===this.$.items&&(this.__preventScrollerRotatingCellFocusDebouncer=x.debounce(this.__preventScrollerRotatingCellFocusDebouncer,ie,()=>{let e=this._activeRowGroup===this.$.items;this._getRenderedRows().some(r=>r.index===this._focusedItemIndex)?(this.__updateItemsFocusable(),e&&!this.__rowFocusMode&&(this._focusedCell=this._itemsFocusable),this._navigatingIsHidden&&(this.toggleAttribute("navigating",!0),this._navigatingIsHidden=!1)):e&&(this._focusedCell=null,this.hasAttribute("navigating")&&(this._navigatingIsHidden=!0,this.toggleAttribute("navigating",!1)))}))}_getColumns(e,t){let r=this._columnTree.length-1;return e===this.$.header?r=t:e===this.$.footer&&(r=this._columnTree.length-1-t),this._columnTree[r]}__isValidFocusable(e){return this.$.table.contains(e)&&e.offsetHeight}_resetKeyboardNavigation(){if(!this.$&&this.performUpdate&&this.performUpdate(),["header","footer"].forEach(e=>{if(!this.__isValidFocusable(this[`_${e}Focusable`])){let t=[...this.$[e].children].find(o=>o.offsetHeight),r=t?[...t.children].find(o=>!o.hidden):null;t&&r&&(this[`_${e}Focusable`]=this.__getFocusable(t,r))}}),!this.__isValidFocusable(this._itemsFocusable)&&this.$.items.firstElementChild){let e=this.__getFirstVisibleItem(),t=e?[...e.children].find(r=>!r.hidden):null;t&&e&&(this._focusedColumnOrder=void 0,this._itemsFocusable=this.__getFocusable(e,t))}else this.__updateItemsFocusable()}_scrollHorizontallyToCell(e){if(e.hasAttribute("frozen")||e.hasAttribute("frozen-to-end")||Ne(e))return;let t=e.getBoundingClientRect(),r=e.parentNode,o=Array.from(r.children).indexOf(e),n=this.$.table.getBoundingClientRect(),a=this.$.table.clientWidth-this.$.table.offsetWidth,l=n.left-(this.__isRTL?a:0),d=n.right+(this.__isRTL?0:a);for(let h=o-1;h>=0;h--){let c=r.children[h];if(!(c.hasAttribute("hidden")||Ne(c))&&(c.hasAttribute("frozen")||c.hasAttribute("frozen-to-end"))){l=c.getBoundingClientRect().right;break}}for(let h=o+1;h<r.children.length;h++){let c=r.children[h];if(!(c.hasAttribute("hidden")||Ne(c))&&(c.hasAttribute("frozen")||c.hasAttribute("frozen-to-end"))){d=c.getBoundingClientRect().left;break}}t.left<l&&(this.$.table.scrollLeft+=t.left-l),t.right>d&&(this.$.table.scrollLeft+=t.right-d)}_getGridEventLocation(e){let t=e.__composedPath||e.composedPath(),r=t.indexOf(this.$.table),o=r>=1?t[r-1]:null,n=r>=2?t[r-2]:null,a=r>=3?t[r-3]:null;return{section:o,row:n,cell:a}}_getGridSectionFromFocusTarget(e){return e===this._headerFocusable?this.$.header:e===this._itemsFocusable?this.$.items:e===this._footerFocusable?this.$.footer:null}};var ia=s=>class extends s{static get properties(){return{__hostVisible:{type:Boolean,value:!1},__tableRect:Object,__headerRect:Object,__itemsRect:Object,__footerRect:Object}}ready(){super.ready();let i=new ResizeObserver(e=>{e.findLast(({target:l})=>l===this)&&(this.__hostVisible=this.checkVisibility());let r=e.findLast(({target:l})=>l===this.$.table);r&&(this.__tableRect=r.contentRect);let o=e.findLast(({target:l})=>l===this.$.header);o&&(this.__headerRect=o.contentRect);let n=e.findLast(({target:l})=>l===this.$.items);n&&(this.__itemsRect=n.contentRect);let a=e.findLast(({target:l})=>l===this.$.footer);a&&(this.__footerRect=a.contentRect)});i.observe(this),i.observe(this.$.table),i.observe(this.$.header),i.observe(this.$.items),i.observe(this.$.footer)}};var ra=s=>class extends s{static get properties(){return{detailsOpenedItems:{type:Array,value:()=>[],sync:!0},rowDetailsRenderer:{type:Function,sync:!0},_detailsCells:{type:Array},__detailsOpenedKeys:{type:Object,computed:"__computeDetailsOpenedKeys(itemIdPath, detailsOpenedItems)"}}}static get observers(){return["_detailsOpenedItemsChanged(detailsOpenedItems, rowDetailsRenderer)","_rowDetailsRendererChanged(rowDetailsRenderer)"]}ready(){super.ready(),this._detailsCellResizeObserver=new ResizeObserver(e=>{e.forEach(({target:t})=>{this._updateDetailsCellHeight(t.parentElement)})})}_rowDetailsRendererChanged(e){e&&this._columnTree&&this._getRenderedRows().forEach(t=>{if(!t.querySelector("[part~=details-cell]")){this.__initRow(t,this._columnTree[this._columnTree.length-1]),this.__updateRow(t);return}t.hasAttribute("details-opened")&&this.__updateRow(t)})}_detailsOpenedItemsChanged(e,t){this._getRenderedRows().forEach(r=>{r.hasAttribute("details-opened")!==!!(t&&this._isDetailsOpened(r._item))&&this.__updateRow(r)})}_configureDetailsCell(e){E(e,"cell",!0),E(e,"details-cell",!0),e.toggleAttribute("frozen",!0),this._detailsCellResizeObserver.observe(e)}_toggleDetailsCell(e,t){let r=e.querySelector('[part~="details-cell"]');r&&(r.hidden=!t,!r.hidden&&this.rowDetailsRenderer&&(r._renderer=this.rowDetailsRenderer))}_updateDetailsCellHeight(e){let t=e.querySelector('[part~="details-cell"]');t&&(this.__updateDetailsRowPadding(e,t),requestAnimationFrame(()=>this.__updateDetailsRowPadding(e,t)))}__updateDetailsRowPadding(e,t){t.hidden?e.style.removeProperty("padding-bottom"):e.style.setProperty("padding-bottom",`${t.offsetHeight}px`)}_updateDetailsCellHeights(){this._getRenderedRows().forEach(e=>{this._updateDetailsCellHeight(e)})}_isDetailsOpened(e){return this.__detailsOpenedKeys&&this.__detailsOpenedKeys.has(this.getItemId(e))}__computeDetailsOpenedKeys(e,t){let r=t||[],o=new Set;return r.forEach(n=>{o.add(this.getItemId(n))}),o}openItemDetails(e){this._isDetailsOpened(e)||(this.detailsOpenedItems=[...this.detailsOpenedItems,e])}closeItemDetails(e){this._isDetailsOpened(e)&&(this.detailsOpenedItems=this.detailsOpenedItems.filter(t=>!this._itemsEqual(t,e)))}};function ft(s,i){let{scrollLeft:e}=s;return i!=="rtl"?e:s.scrollWidth-s.clientWidth+e}function sa(s,i,e){i!=="rtl"?s.scrollLeft=e:s.scrollLeft=s.clientWidth-s.scrollWidth+e}var Ri=class{constructor(i,e){this.host=i,this.scrollTarget=e||i,this.__boundOnScroll=this.__onScroll.bind(this)}hostConnected(){this.initialized||(this.initialized=!0,this.observe())}observe(){let{host:i}=this;this.__resizeObserver=new ResizeObserver(()=>this.__onResize()),this.__resizeObserver.observe(i),[...i.children].forEach(e=>{this.__resizeObserver.observe(e)}),this.__childObserver=new MutationObserver(e=>{e.forEach(({addedNodes:t,removedNodes:r})=>{t.forEach(o=>{o.nodeType===Node.ELEMENT_NODE&&this.__resizeObserver.observe(o)}),r.forEach(o=>{o.nodeType===Node.ELEMENT_NODE&&this.__resizeObserver.unobserve(o)}),t.length===0&&r.length>0&&this.__updateState({sync:!0})})}),this.__childObserver.observe(i,{childList:!0}),this.scrollTarget.addEventListener("scroll",this.__boundOnScroll)}__onResize(){this.__updateState({sync:!1})}__onScroll(){this.__updateState({sync:!0})}__updateState({sync:i}){cancelAnimationFrame(this.__resizeRaf);let e=this.__readState();i?this.__writeState(e):this.__resizeRaf=requestAnimationFrame(()=>this.__writeState(e))}__readState(){let i=this.scrollTarget,e="";i.scrollTop>0&&(e+=" top"),Math.ceil(i.scrollTop)<Math.ceil(i.scrollHeight-i.clientHeight)&&(e+=" bottom");let t=Math.abs(i.scrollLeft);return t>0&&(e+=" start"),Math.ceil(t)<Math.ceil(i.scrollWidth-i.clientWidth)&&(e+=" end"),{overflow:e.trim()}}__writeState({overflow:i}){i?this.host.setAttribute("overflow",i):this.host.removeAttribute("overflow")}};var oa={SCROLLING:500,UPDATE_CONTENT_VISIBILITY:100},na=s=>class extends s{static get properties(){return{columnRendering:{type:String,value:"eager",sync:!0},_frozenCells:{type:Array,value:()=>[]},_frozenToEndCells:{type:Array,value:()=>[]}}}static get observers(){return["__columnRenderingChanged(_columnTree, columnRendering)"]}get _scrollLeft(){return this.$.table.scrollLeft}get _scrollTop(){return this.$.table.scrollTop}set _scrollTop(e){this.$.table.scrollTop=e}get _lazyColumns(){return this.columnRendering==="lazy"}ready(){super.ready(),this.scrollTarget=this.$.table,this.$.items.addEventListener("focusin",e=>{let t=e.composedPath(),r=t[t.indexOf(this.$.items)-1];if(r){if(!this._isMousedown){let o=this.$.table.clientHeight,n=this.$.header.clientHeight,a=this.$.footer.clientHeight,l=o-n-a,h=r.clientHeight>l?e.target:r;this.__scrollIntoViewport(h)}this.$.table.contains(e.relatedTarget)||this.$.table.dispatchEvent(new CustomEvent("virtualizer-element-focused",{detail:{element:r}}))}}),this.$.table.addEventListener("scroll",()=>this._afterScroll()),this.__overflowController=new Ri(this,this.$.table),this.addController(this.__overflowController)}_scrollToFlatIndex(e){e=Math.min(this._flatSize-1,Math.max(0,e)),this.__virtualizer.scrollToIndex(e);let t=[...this.$.items.children].find(r=>r.index===e);this.__scrollIntoViewport(t)}scrollToColumn(e){if(!this._columnTree){this.__pendingScrollToColumn=e;return}let t=this._getColumnsInOrder(),r;if(typeof e=="number"){if(r=t[e],!r){console.warn(`Column index ${e} is out of bounds`);return}}else if(r=e,!t.includes(r)){console.warn("Column is not a visible column of this grid");return}r.frozen||r.frozenToEnd||(this._scrollHorizontallyToCell(r._headerCell),this.__updateHorizontalScrollPosition(),this.__updateColumnsBodyContentHidden())}__scrollToPendingColumn(){if(this.__pendingScrollToColumn!==void 0){let e=this.__pendingScrollToColumn;delete this.__pendingScrollToColumn,this.scrollToColumn(e)}}__scrollIntoViewport(e){if(!e)return;let t=e.getBoundingClientRect(),r=getComputedStyle(e),o=t.top+parseInt(r.scrollMarginTop||0),n=t.bottom+parseInt(r.scrollMarginBottom||0),a=this.$.footer.getBoundingClientRect().top,l=this.$.header.getBoundingClientRect().bottom;n>a?this.$.table.scrollTop+=n-a:o<l&&(this.$.table.scrollTop-=l-o)}_scheduleScrolling(){this._scrollingFrame||(this._scrollingFrame=requestAnimationFrame(()=>this.$.scroller.toggleAttribute("scrolling",!0))),this._debounceScrolling=x.debounce(this._debounceScrolling,R.after(oa.SCROLLING),()=>{cancelAnimationFrame(this._scrollingFrame),delete this._scrollingFrame,this.$.scroller.toggleAttribute("scrolling",!1)})}_afterScroll(){this.__updateHorizontalScrollPosition(),this.hasAttribute("reordering")||this._scheduleScrolling(),this.hasAttribute("navigating")||this._hideTooltip(!0),this._debounceColumnContentVisibility=x.debounce(this._debounceColumnContentVisibility,R.after(oa.UPDATE_CONTENT_VISIBILITY),()=>{this._lazyColumns&&this.__cachedScrollLeft!==this._scrollLeft&&(this.__cachedScrollLeft=this._scrollLeft,this.__updateColumnsBodyContentHidden())})}__updateColumnsBodyContentHidden(){if(!this._columnTree||!this._areSizerCellsAssigned())return;this.__scrollToPendingColumn();let e=this._getColumnsInOrder(),t=!1;if(e.forEach(r=>{let o=this._lazyColumns&&!this.__isColumnInViewport(r);r._bodyContentHidden!==o&&(t=!0,r._cells.forEach(n=>{if(n!==r._sizerCell){if(o)n.remove();else if(n.__parentRow){let a=[...n.__parentRow.children].find(l=>e.indexOf(l._column)>e.indexOf(r));n.__parentRow.insertBefore(n,a)}}})),r._bodyContentHidden=o}),t&&this._frozenCellsChanged(),this._lazyColumns){let r=[...e].reverse().find(a=>a.frozen),o=this.__getColumnEnd(r),n=e.find(a=>!a.frozen&&!a._bodyContentHidden);this.__lazyColumnsStart=this.__getColumnStart(n)-o,this.$.items.style.setProperty("--_grid-lazy-columns-start",`${this.__lazyColumnsStart}px`),this._resetKeyboardNavigation()}}__getColumnEnd(e){return e?e._sizerCell.offsetLeft+(this.__isRTL?0:e._sizerCell.offsetWidth):this.__isRTL?this.$.table.clientWidth:0}__getColumnStart(e){return e?e._sizerCell.offsetLeft+(this.__isRTL?e._sizerCell.offsetWidth:0):this.__isRTL?this.$.table.clientWidth:0}__isColumnInViewport(e){return e.frozen||e.frozenToEnd?!0:this.__isHorizontallyInViewport(e._sizerCell)}__isHorizontallyInViewport(e){return e.offsetLeft+e.offsetWidth>=this._scrollLeft&&e.offsetLeft<=this._scrollLeft+this.clientWidth}__columnRenderingChanged(e,t){t==="eager"?this.$.scroller.removeAttribute("column-rendering"):this.$.scroller.setAttribute("column-rendering",t),this.__updateColumnsBodyContentHidden()}_frozenCellsChanged(){this._debouncerCacheElements=x.debounce(this._debouncerCacheElements,z,()=>{Array.from(this.shadowRoot.querySelectorAll('[part~="cell"]')).forEach(e=>{e.style.transform=""}),this._frozenCells=Array.prototype.slice.call(this.$.table.querySelectorAll("[frozen]")),this._frozenToEndCells=Array.prototype.slice.call(this.$.table.querySelectorAll("[frozen-to-end]")),this.__updateHorizontalScrollPosition()}),this._debounceUpdateFrozenColumn()}_debounceUpdateFrozenColumn(){this.__debounceUpdateFrozenColumn=x.debounce(this.__debounceUpdateFrozenColumn,z,()=>this._updateFrozenColumn())}_updateFrozenColumn(){if(!this._columnTree)return;let e=this._columnTree[this._columnTree.length-1].slice(0);e.sort((o,n)=>o._order-n._order);let t,r;for(let o=0;o<e.length;o++){let n=e[o];n._lastFrozen=!1,n._firstFrozenToEnd=!1,r===void 0&&n.frozenToEnd&&!n.hidden&&(r=o),n.frozen&&!n.hidden&&(t=o)}t!==void 0&&(e[t]._lastFrozen=!0),r!==void 0&&(e[r]._firstFrozenToEnd=!0),this.__updateColumnsBodyContentHidden()}__updateHorizontalScrollPosition(){if(!this._columnTree)return;let e=this.$.table.scrollWidth,t=this.$.table.clientWidth,r=Math.max(0,this.$.table.scrollLeft),o=ft(this.$.table,this.getAttribute("dir")),n=`translate(${-r}px, 0)`;this.$.header.style.transform=n,this.$.footer.style.transform=n,this.$.items.style.transform=n;let a=this.__isRTL?o+t-e:r;this.__horizontalScrollPosition=a;let l=`translate(${a}px, 0)`;this._frozenCells.forEach(S=>{S.style.transform=l});let d=this.__isRTL?o:r+t-e,h=`translate(${d}px, 0)`,c=h;if(this._lazyColumns&&this._areSizerCellsAssigned()){let S=this._getColumnsInOrder(),Q=[...S].reverse().find(He=>!He.frozenToEnd&&!He._bodyContentHidden),te=this.__getColumnEnd(Q),O=S.find(He=>He.frozenToEnd),X=this.__getColumnStart(O);c=`translate(${d+(X-te)+this.__lazyColumnsStart}px, 0)`}this._frozenToEndCells.forEach(S=>{this.$.items.contains(S)?S.style.transform=c:S.style.transform=h});let f=this.shadowRoot.querySelector("[part~='row']:focus");f&&this.__updateRowScrollPositionProperty(f);let y=this.$.header.querySelector("[part~='last-header-row']");y&&this.__updateRowScrollPositionProperty(y);let w=this.$.footer.querySelector("[part~='first-footer-row']");w&&this.__updateRowScrollPositionProperty(w)}__updateRowScrollPositionProperty(e){if(!(e instanceof HTMLTableRowElement))return;let t=`${this.__horizontalScrollPosition}px`;e.style.getPropertyValue("--_grid-horizontal-scroll-position")!==t&&e.style.setProperty("--_grid-horizontal-scroll-position",t)}_areSizerCellsAssigned(){return this._getColumnsInOrder().every(e=>e._sizerCell)}};var aa=s=>class extends s{static get properties(){return{selectedItems:{type:Object,notify:!0,value:()=>[],sync:!0},isItemSelectable:{type:Function},__selectedKeys:{type:Object,computed:"__computeSelectedKeys(itemIdPath, selectedItems)"}}}static get observers(){return["__selectedItemsChanged(itemIdPath, selectedItems, isItemSelectable)"]}_isSelected(e){return this.__selectedKeys.has(this.getItemId(e))}__isItemSelectable(e){return!this.isItemSelectable||!e?!0:this.isItemSelectable(e)}selectItem(e){this._isSelected(e)||(this.selectedItems=[...this.selectedItems,e])}deselectItem(e){this._isSelected(e)&&(this.selectedItems=this.selectedItems.filter(t=>!this._itemsEqual(t,e)))}updated(e){super.updated(e),e.has("isItemSelectable")&&this.dispatchEvent(new CustomEvent("is-item-selectable-changed"))}__selectedItemsChanged(){this._getRenderedRows().forEach(e=>{(e.hasAttribute("selected")!==this._isSelected(e._item)||e.hasAttribute("nonselectable")!==!this.__isItemSelectable(e._item))&&this.__updateRow(e)})}__computeSelectedKeys(e,t){let r=t||[],o=new Set;return r.forEach(n=>{o.add(this.getItemId(n))}),o}};var la="prepend",da=s=>class extends s{static get properties(){return{multiSort:{type:Boolean,value:!1},multiSortPriority:{type:String,value:()=>la},multiSortOnShiftClick:{type:Boolean,value:!1},_sorters:{type:Array,value:()=>[]},_previousSorters:{type:Array,value:()=>[]}}}static setDefaultMultiSortPriority(e){la=["append","prepend"].includes(e)?e:"prepend"}ready(){super.ready(),this.addEventListener("sorter-changed",this._onSorterChanged)}_onSorterChanged(e){let t=e.target;e.stopPropagation(),t._grid=this,this.__updateSorter(t,e.detail.shiftClick,e.detail.fromSorterClick),this.__applySorters()}__removeSorters(e){e.length!==0&&(this._sorters=this._sorters.filter(t=>!e.includes(t)),this.__applySorters())}__updateSortOrders(){this._sorters.forEach(t=>{t._order=null});let e=this._getActiveSorters();e.length>1&&e.forEach((t,r)=>{t._order=r})}__updateSorter(e,t,r){if(!e.direction&&!this._sorters.includes(e))return;e._order=null;let o=this._sorters.filter(n=>n!==e);this.multiSort&&(!this.multiSortOnShiftClick||!r)||this.multiSortOnShiftClick&&t?this.multiSortPriority==="append"?this._sorters=[...o,e]:this._sorters=[e,...o]:(e.direction||this.multiSortOnShiftClick)&&(this._sorters=e.direction?[e]:[],o.forEach(n=>{n._order=null,n.direction=null}))}__applySorters(){this.__updateSortOrders(),this.dataProvider&&this.isAttached&&JSON.stringify(this._previousSorters)!==JSON.stringify(this._mapSorters())&&this.__debounceClearCache(),this.__a11yUpdateSorters(),this._previousSorters=this._mapSorters()}_getActiveSorters(){return this._sorters.filter(e=>e.direction&&e.isConnected)}_mapSorters(){return this._getActiveSorters().map(e=>({path:e.path,direction:e.direction}))}};var ha=s=>class extends s{static get properties(){return{cellPartNameGenerator:{type:Function,sync:!0}}}static get observers(){return["__cellPartNameGeneratorChanged(cellPartNameGenerator)"]}__cellPartNameGeneratorChanged(){this.generateCellPartNames()}generateCellPartNames(){H(this.$.items,e=>{e.hidden||this._generateCellPartNames(e,this.__getRowModel(e))})}_generateCellPartNames(e,t){ge(e,r=>{if(r.__generatedParts&&r.__generatedParts.forEach(o=>{E(r,o,null)}),this.cellPartNameGenerator&&!e.hasAttribute("loading")){let o=this.cellPartNameGenerator(r._column,t);r.__generatedParts=o&&o.split(" ").filter(n=>n.length>0),r.__generatedParts&&r.__generatedParts.forEach(n=>{E(r,n,!0)})}})}};var ca=s=>class extends qn(Wn(Yn(Zn(Nn(na(aa(da(ra(ta(Vn(ea(Gn(Kn(Jn(Qn(ha(Ye(ia(s))))))))))))))))))){static get observers(){return["_columnTreeChanged(_columnTree)","_flatSizeChanged(_flatSize, __virtualizer, _hasData, _columnTree)"]}static get properties(){return{_safari:{type:Boolean,value:it},_ios:{type:Boolean,value:Be},_firefox:{type:Boolean,value:Bo},_android:{type:Boolean,value:Rr},_touchDevice:{type:Boolean,value:K},allRowsVisible:{type:Boolean,value:!1,reflectToAttribute:!0},isAttached:{value:!1},__gridElement:{type:Boolean,value:!0},__hasEmptyStateContent:{type:Boolean,value:!1},__emptyState:{type:Boolean,computed:"__computeEmptyState(_flatSize, __hasEmptyStateContent)"}}}get _firstVisibleIndex(){let i=this.__getFirstVisibleItem();return i?i.index:void 0}get _lastVisibleIndex(){let i=this.__getLastVisibleItem();return i?i.index:void 0}connectedCallback(){super.connectedCallback(),this.isAttached=!0,this.__virtualizer.hostConnected()}disconnectedCallback(){super.disconnectedCallback(),this.isAttached=!1,this._hideTooltip(!0)}__getFirstVisibleItem(){return this._getRenderedRows().find(i=>this._isInViewport(i))}__getLastVisibleItem(){return this._getRenderedRows().reverse().find(i=>this._isInViewport(i))}_isInViewport(i){let e=this.$.table.getBoundingClientRect(),t=i.getBoundingClientRect(),r=this.$.header.getBoundingClientRect().height,o=this.$.footer.getBoundingClientRect().height;return t.bottom>e.top+r&&t.top<e.bottom-o}_getRenderedRows(){return Array.from(this.$.items.children).filter(i=>!i.hidden).sort((i,e)=>i.index-e.index)}_getRowContainingNode(i){let e=hr("vaadin-grid-cell-content",i);return e?e.assignedSlot.parentElement.parentElement:void 0}_isItemAssignedToRow(i,e){let t=this.__getRowModel(e);return this.getItemId(i)===this.getItemId(t.item)}ready(){super.ready(),Zt(this,""),Zt(this.$.scroller,""),this.__virtualizer=new ot({createElements:i=>this.__createVirtualizerElements(i),updateElement:(i,e)=>{this.__updateVirtualizerElement(i,e)},scrollContainer:this.$.items,scrollTarget:this.$.table,reorderElements:!0,__disableHeightPlaceholder:!0}),this._tooltipController=new D(this),this.addController(this._tooltipController),this._tooltipController.setManual(!0),this.__emptyStateContentObserver=new re(this.$.emptystateslot,({currentNodes:i})=>{this.$.emptystatecell._content=i[0],this.__hasEmptyStateContent=!!this.$.emptystatecell._content})}updated(i){super.updated(i),i.has("__hostVisible")&&!i.get("__hostVisible")&&(this._resetKeyboardNavigation(),requestAnimationFrame(()=>this.__scrollToPendingIndexes())),(i.has("__headerRect")||i.has("__footerRect")||i.has("__itemsRect"))&&setTimeout(()=>this.__updateMinHeight()),i.has("__tableRect")&&(setTimeout(()=>this.__updateColumnsBodyContentHidden()),this.__updateHorizontalScrollPosition())}__getBodyCellCoordinates(i){if(this.$.items.contains(i)&&i.localName==="td")return{item:i.parentElement._item,column:i._column}}__focusBodyCell({item:i,column:e}){let t=this._getRenderedRows().find(o=>o._item===i),r=t&&[...t.children].find(o=>o._column===e);r&&r.focus()}_focusFirstVisibleRow(){let i=this.__getFirstVisibleItem();this.__rowFocusMode=!0,i.focus()}_flatSizeChanged(i,e,t,r){if(e&&t&&r){let o=this.shadowRoot.activeElement,n=this.__getBodyCellCoordinates(o),a=e.size||0;e.size=i,e.update(a-1,a-1),i<a&&e.update(i-1,i-1),n&&o.parentElement.hidden&&this.__focusBodyCell(n),this._resetKeyboardNavigation()}}__createVirtualizerElements(i){let e=[];for(let t=0;t<i;t++){let r=document.createElement("tr");r.setAttribute("role","row"),r.setAttribute("tabindex","-1"),E(r,"row",!0),E(r,"body-row",!0),this._columnTree&&this.__initRow(r,this._columnTree[this._columnTree.length-1],"body",!1,!0),e.push(r)}return this._columnTree&&this._columnTree[this._columnTree.length-1].forEach(t=>{t.isConnected&&t._cells&&(t._cells=[...t._cells])}),e}_createCell(i,e){let r=`vaadin-grid-cell-content-${this._contentIndex=this._contentIndex+1||0}`,o=document.createElement("vaadin-grid-cell-content");o.setAttribute("slot",r);let n=document.createElement(i);n.id=r.replace("-content-","-"),n.setAttribute("role",i==="td"?"gridcell":"columnheader"),!Rr&&!Be&&(n.addEventListener("mouseenter",l=>{this.$.scroller.hasAttribute("scrolling")||this._showTooltip(l)}),n.addEventListener("mouseleave",()=>{this._hideTooltip()}),n.addEventListener("mousedown",()=>{this._hideTooltip(!0)}));let a=document.createElement("slot");if(a.setAttribute("name",r),e?._focusButtonMode){let l=document.createElement("div");l.setAttribute("role","button"),l.setAttribute("tabindex","-1"),n.appendChild(l),n._focusButton=l,n.focus=function(d){n._focusButton.focus(d)},l.appendChild(a)}else n.setAttribute("tabindex","-1"),n.appendChild(a);return n._content=o,o.addEventListener("mousedown",()=>{if(kt){let l=d=>{let h=o.contains(this.getRootNode().activeElement),c=d.composedPath().includes(o);!h&&c&&n.focus({preventScroll:!0}),document.removeEventListener("mouseup",l,!0)};document.addEventListener("mouseup",l,!0)}else setTimeout(()=>{o.contains(this.getRootNode().activeElement)||n.focus({preventScroll:!0})})}),n}__initRow(i,e,t="body",r=!1,o=!1){let n=document.createDocumentFragment();ge(i,a=>{a._vacant=!0}),i.innerHTML="",t==="body"&&(i.__cells=[],i.__detailsCell=null),e.filter(a=>!a.hidden).toSorted((a,l)=>a._order-l._order).forEach((a,l,d)=>{let h;if(t==="body"){a._cells||(a._cells=[]),h=a._cells.find(f=>f._vacant),h||(h=this._createCell("td",a),a._onCellKeyDown&&h.addEventListener("keydown",a._onCellKeyDown.bind(a)),a._cells.push(h)),E(h,"cell",!0),E(h,"body-cell",!0),h.__parentRow=i,i.__cells.push(h);let c=i===this.$.sizer;if((!a._bodyContentHidden||c)&&i.appendChild(h),c&&(a._sizerCell=h),l===d.length-1&&this.rowDetailsRenderer){this._detailsCells||(this._detailsCells=[]);let f=this._detailsCells.find(y=>y._vacant)||this._createCell("td");this._detailsCells.indexOf(f)===-1&&this._detailsCells.push(f),f._content.parentElement||n.appendChild(f._content),this._configureDetailsCell(f),f.__parentRow=i,i.appendChild(f),i.__detailsCell=f,this.__a11ySetRowDetailsCell(i,f),f._vacant=!1}o||(a._cells=[...a._cells])}else{let c=t==="header"?"th":"td";r||a.localName==="vaadin-grid-column-group"?(h=a[`_${t}Cell`],h||(h=this._createCell(c),a._onCellKeyDown&&h.addEventListener("keydown",a._onCellKeyDown.bind(a))),h._column=a,i.appendChild(h),a[`_${t}Cell`]=h):(a._emptyCells||(a._emptyCells=[]),h=a._emptyCells.find(f=>f._vacant)||this._createCell(c),h._column=a,i.appendChild(h),a._emptyCells.indexOf(h)===-1&&a._emptyCells.push(h)),E(h,"cell",!0),E(h,`${t}-cell`,!0)}h._content.parentElement||n.appendChild(h._content),h._vacant=!1,h._column=a}),t!=="body"&&this.__debounceUpdateHeaderFooterRowVisibility(i),this.appendChild(n),this._frozenCellsChanged(),this._updateFirstAndLastColumnForRow(i)}__debounceUpdateHeaderFooterRowVisibility(i){i.__debounceUpdateHeaderFooterRowVisibility=x.debounce(i.__debounceUpdateHeaderFooterRowVisibility,z,()=>this.__updateHeaderFooterRowVisibility(i))}__updateHeaderFooterRowVisibility(i){if(!i)return;let e=Array.from(i.children).filter(t=>{let r=t._column;if(r._emptyCells&&r._emptyCells.indexOf(t)>-1)return!1;if(i.parentElement===this.$.header){if(r.headerRenderer)return!0;if(r.header===null)return!1;if(r.path||r.header!==void 0)return!0}else if(r.footerRenderer)return!0;return!1});i.hidden!==!e.length&&(i.hidden=!e.length),i.parentElement===this.$.header&&(this.$.table.toggleAttribute("has-header",this.$.header.querySelector("tr:not([hidden])")),this.__updateHeaderFooterRowParts("header")),i.parentElement===this.$.footer&&(this.$.table.toggleAttribute("has-footer",this.$.footer.querySelector("tr:not([hidden])")),this.__updateHeaderFooterRowParts("footer")),this._resetKeyboardNavigation(),this.__a11yUpdateGridSize(this.size,this._columnTree,this.__emptyState)}__updateVirtualizerElement(i,e){this._preventScrollerRotatingCellFocus(i,e),this._columnTree&&(i.index=e,this.__ensureRowItem(i),this.__ensureRowHierarchy(i),this.__updateRow(i))}_columnTreeChanged(i){this._renderColumnTree(i),this.__updateColumnsBodyContentHidden()}__updateRowOrderParts(i){ke(i,{first:i.index===0,last:i.index===this._flatSize-1,odd:i.index%2!==0,even:i.index%2===0})}__updateRowStateParts(i,{item:e,expanded:t,selected:r,detailsOpened:o}){ke(i,{expanded:t,collapsed:this.__isRowExpandable(i),selected:r,nonselectable:this.__isItemSelectable(e)===!1,"details-opened":o})}__computeEmptyState(i,e){return i===0&&e}_renderColumnTree(i){for(H(this.$.items,e=>{this.__initRow(e,i[i.length-1],"body",!1,!0),this.__updateRow(e)});this.$.header.children.length<i.length;){let e=document.createElement("tr");e.setAttribute("role","row"),e.setAttribute("tabindex","-1"),E(e,"row",!0),E(e,"header-row",!0),this.$.header.appendChild(e);let t=document.createElement("tr");t.setAttribute("role","row"),t.setAttribute("tabindex","-1"),E(t,"row",!0),E(t,"footer-row",!0),this.$.footer.appendChild(t)}for(;this.$.header.children.length>i.length;)this.$.header.removeChild(this.$.header.firstElementChild),this.$.footer.removeChild(this.$.footer.firstElementChild);H(this.$.header,(e,t)=>{this.__initRow(e,i[t],"header",t===i.length-1)}),H(this.$.footer,(e,t)=>{this.__initRow(e,i[i.length-1-t],"footer",t===0)}),this.__initRow(this.$.sizer,i[i.length-1]),this.__updateHeaderFooterRowParts("header"),this.__updateHeaderFooterRowParts("footer"),this._resizeHandler(),this._frozenCellsChanged(),this._updateFirstAndLastColumn(),this._resetKeyboardNavigation(),this.__a11yUpdateHeaderRows(),this.__a11yUpdateFooterRows(),this.generateCellPartNames(),this.__updateHeaderAndFooter()}__updateHeaderFooterRowParts(i){let e=[...this.$[i].querySelectorAll("tr:not([hidden])")];[...this.$[i].children].forEach(t=>{E(t,`first-${i}-row`,t===e.at(0)),E(t,`last-${i}-row`,t===e.at(-1)),Y(t).forEach(r=>{E(r,`first-${i}-row-cell`,t===e.at(0)),E(r,`last-${i}-row-cell`,t===e.at(-1))})})}__updateRowLoading(i,e){let t=Y(i);Ft(i,"loading",e),Pt(t,"loading-row-cell",e),e&&this._generateCellPartNames(i)}__updateRow(i){this.__a11yUpdateRowRowindex(i),this.__updateRowOrderParts(i);let e=this.__getRowItem(i);if(e)this.__updateRowLoading(i,!1);else{this.__updateRowLoading(i,!0);return}i._item=e;let t=this.__getRowModel(i);this._toggleDetailsCell(i,t.detailsOpened),this.__a11yUpdateRowLevel(i,t.level),this.__a11yUpdateRowSelected(i,t.selected),this.__updateRowStateParts(i,t),this._generateCellPartNames(i,t),this._filterDragAndDrop(i,t),this.__updateDragSourceParts(i,t),H(i,r=>{if(!(r._column&&!r._column.isConnected)&&r._renderer){let o=r._column||this;r._renderer.call(o,r._content,o,t)}}),this._updateDetailsCellHeight(i),this.__a11yUpdateRowExpanded(i,t.expanded)}_resizeHandler(){this._updateDetailsCellHeights(),this.__updateHorizontalScrollPosition()}__getRowModel(i){return{index:i.index,item:i._item,level:this.__getRowLevel(i),expanded:this._isExpanded(i._item),selected:this._isSelected(i._item),hasChildren:this._hasChildren(i._item),detailsOpened:!!this.rowDetailsRenderer&&this._isDetailsOpened(i._item)}}_showTooltip(i){if(this._tooltipController.node?.isConnected){let t=i.target;if(!this.__isCellFullyVisible(t))return;this._tooltipController.setTarget(t),this._tooltipController.setContext(this.getEventContext(i)),this._tooltipController.open({focus:i.type==="focusin",hover:i.type==="mouseenter"})}}__isCellFullyVisible(i){if(i.hasAttribute("frozen")||i.hasAttribute("frozen-to-end"))return!0;let{left:e,right:t}=this.getBoundingClientRect(),r=[...i.parentNode.children].find(a=>a.hasAttribute("last-frozen"));if(r){let a=r.getBoundingClientRect();e=this.__isRTL?e:a.right,t=this.__isRTL?a.left:t}let o=[...i.parentNode.children].find(a=>a.hasAttribute("first-frozen-to-end"));if(o){let a=o.getBoundingClientRect();e=this.__isRTL?a.right:e,t=this.__isRTL?t:a.left}let n=i.getBoundingClientRect();return n.left>=e&&n.right<=t}_hideTooltip(i){this._tooltipController.close(i)}requestContentUpdate(){this.__updateHeaderAndFooter(),this.__updateVisibleRows()}__updateHeaderAndFooter(){(this._columnTree||[]).forEach(i=>{i.forEach(e=>{e._renderHeaderAndFooter&&e._renderHeaderAndFooter()})})}__updateVisibleRows(i,e){this.__virtualizer?.update(i,e)}__updateMinHeight(){let e=this.$.header.clientHeight,t=this.$.footer.clientHeight,r=this.$.table.offsetHeight-this.$.table.clientHeight,o=e+36+t+r;this.__minHeightStyleSheet||(this.__minHeightStyleSheet=new CSSStyleSheet,this.shadowRoot.adoptedStyleSheets.push(this.__minHeightStyleSheet)),this.__minHeightStyleSheet.replaceSync(`:host { --_grid-min-height: ${o}px; }`)}};var us=class extends ca(A(v(g(b(_))))){static get is(){return"vaadin-grid"}static get styles(){return Bn}render(){return u`
      <div
        id="scroller"
        ?safari="${this._safari}"
        ?ios="${this._ios}"
        ?loading="${this.loading}"
        ?column-reordering-allowed="${this.columnReorderingAllowed}"
        ?empty-state="${this.__emptyState}"
      >
        <table
          id="table"
          role="treegrid"
          aria-multiselectable="true"
          tabindex="0"
          aria-label="${k(this.accessibleName)}"
        >
          <caption id="sizer" part="row"></caption>
          <thead id="header" role="rowgroup"></thead>
          <tbody id="items" role="rowgroup"></tbody>
          <tbody id="emptystatebody">
            <tr id="emptystaterow">
              <td part="empty-state" class="empty-state" id="emptystatecell" tabindex="0">
                <slot name="empty-state" id="emptystateslot"></slot>
              </td>
            </tr>
          </tbody>
          <tfoot id="footer" role="rowgroup"></tfoot>
        </table>

        <div part="reorder-ghost" class="reorder-ghost"></div>
      </div>

      <slot name="tooltip"></slot>

      <div id="focusexit" tabindex="0"></div>
    `}};m(us);var ua=p`
  :host {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    box-sizing: border-box;
    gap: var(--vaadin-chip-gap, 0);
    background: var(--vaadin-chip-background, var(--vaadin-background-container));
    color: var(--vaadin-chip-text-color, var(--vaadin-text-color));
    font-size: max(11px, var(--vaadin-chip-font-size, 0.875em));
    font-weight: var(--vaadin-chip-font-weight, 500);
    line-height: var(--vaadin-input-field-value-line-height, inherit);
    padding: 0 var(--vaadin-chip-padding, 0.3em);
    height: var(--vaadin-chip-height, calc(1lh / 0.875));
    border-radius: var(--vaadin-chip-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-chip-border-width, 1px) solid
      var(--vaadin-chip-border-color, var(--vaadin-border-color-secondary));
    cursor: default;
  }

  :host(:not([slot='overflow'])) {
    min-width: min(max-content, var(--vaadin-multi-select-combo-box-chip-min-width, 48px));
  }

  :host([focused]) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-chip-border-width, 1px) * -1);
  }

  [part='label'] {
    overflow: hidden;
    text-overflow: ellipsis;
    margin-block: calc(var(--vaadin-chip-border-width, 1px) * -1);
  }

  [part='remove-button'] {
    flex: none;
    display: block;
    margin-inline-start: auto;
    margin-block: calc(var(--vaadin-chip-border-width, 1px) * -1);
    color: var(--vaadin-chip-remove-button-text-color, var(--vaadin-text-color-secondary));
    cursor: var(--vaadin-clickable-cursor);
    translate: 25%;
  }

  [part='remove-button']::before {
    content: '';
    display: block;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    background: currentColor;
    mask-image: var(--_vaadin-icon-cross);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
  }

  :host([disabled]) [part='label'] {
    --vaadin-chip-text-color: var(--vaadin-text-color-disabled);
  }

  :host([hidden]),
  :host(:is([readonly], [disabled], [slot='overflow'])) [part='remove-button'] {
    display: none !important;
  }

  :host([slot='overflow']) {
    position: relative;
    margin-inline-start: 8px;
    min-width: 1.5em;
  }

  :host([slot='overflow'])::before,
  :host([slot='overflow'])::after {
    content: '';
    position: absolute;
    inset: calc(var(--vaadin-chip-border-width, 1px) * -1);
    border-inline-start: 2px solid var(--vaadin-chip-border-color, var(--vaadin-border-color-secondary));
    border-radius: inherit;
  }

  :host([slot='overflow'])::before {
    left: calc(-4px - var(--vaadin-chip-border-width, 1px));
  }

  :host([slot='overflow'])::after {
    left: calc(-8px - var(--vaadin-chip-border-width, 1px));
  }

  :host([count='2']) {
    margin-inline-start: 4px;
  }

  :host([count='1']) {
    margin-inline-start: 0;
  }

  :host([count='2'])::after,
  :host([count='1'])::before,
  :host([count='1'])::after {
    display: none;
  }

  @media (forced-colors: active) {
    :host {
      border: 1px solid !important;
    }

    [part='remove-button']::before {
      background: CanvasText;
    }
  }
`;var ps=class extends v(g(b(_))){static get is(){return"vaadin-multi-select-combo-box-chip"}static get styles(){return ua}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,reflectToAttribute:!0,sync:!0},label:{type:String,sync:!0},item:{type:Object}}}render(){return u`
      <div part="label">${this.label}</div>
      <div part="remove-button" @click="${this._onRemoveClick}"></div>
    `}_onRemoveClick(i){i.stopPropagation(),this.dispatchEvent(new CustomEvent("item-removed",{detail:{item:this.item},bubbles:!0,composed:!0}))}};m(ps);var _s=class extends Tt{static get is(){return"vaadin-multi-select-combo-box-container"}static get styles(){return[super.styles,p`
        #wrapper {
          display: flex;
          width: 100%;
          min-width: 0;
          gap: var(--_wrapper-gap);
          align-self: start;
        }

        :host([auto-expand-vertically]) #wrapper {
          flex-wrap: wrap;
        }
      `]}static get properties(){return{autoExpandVertically:{type:Boolean,reflectToAttribute:!0}}}render(){return u`
      <div id="wrapper">
        <slot name="prefix"></slot>
        <slot></slot>
      </div>
      <slot name="suffix"></slot>
    `}};m(_s);var ms=class extends tt(v(I(g(b(_))))){static get is(){return"vaadin-multi-select-combo-box-item"}static get styles(){return[Se,et]}render(){return u`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(ms);var pa=[ui,p`
    #overlay {
      width: var(
        --vaadin-multi-select-combo-box-overlay-width,
        var(--_vaadin-multi-select-combo-box-overlay-default-width, auto)
      );
    }
  `];var fs=class extends st(J(I(v(g(b(_)))))){static get is(){return"vaadin-multi-select-combo-box-overlay"}static get styles(){return[Z,pa]}render(){return u`
      <div part="overlay" id="overlay">
        <div part="loader"></div>
        <div part="content" id="content"><slot></slot></div>
      </div>
    `}};m(fs);var _a=Ve;var gs=class extends nt(g(_)){static get is(){return"vaadin-multi-select-combo-box-scroller"}static get styles(){return _a}render(){return u`
      <div id="selector">
        <slot></slot>
      </div>
    `}ready(){super.ready(),this.setAttribute("aria-multiselectable","true")}_isItemSelected(i,e,t){return i instanceof L||this.owner.readonly?!1:this.owner._findIndex(i,this.owner.selectedItems,t)>-1}_updateElement(i,e){super._updateElement(i,e),i.toggleAttribute("readonly",this.owner.readonly)}};m(gs);var ma=[_i,p`
    :host {
      max-width: 100%;
      --_input-min-width: var(--vaadin-multi-select-combo-box-input-min-width, 4rem);
      --_chip-min-width: var(--vaadin-multi-select-combo-box-chip-min-width, 48px);
      --_wrapper-gap: var(--vaadin-multi-select-combo-box-chips-gap, 2px);
    }

    #chips {
      display: flex;
      align-items: center;
      gap: var(--vaadin-multi-select-combo-box-chips-gap, 2px);
    }

    ::slotted(input) {
      box-sizing: border-box;
      flex: 1 0 var(--_input-min-width);
    }

    ::slotted([slot='chip']),
    ::slotted([slot='overflow']) {
      flex: 0 1 auto;
    }

    ::slotted([slot='chip']) {
      overflow: hidden;
    }

    :host(:is([readonly], [disabled])) ::slotted(input) {
      flex-grow: 0;
      flex-basis: 0;
      padding: 0;
    }

    :host([readonly]:not([disabled])) [part~='toggle-button'] {
      display: block;
      color: var(--vaadin-input-field-button-text-color, var(--vaadin-text-color-secondary));
    }

    :host([readonly]:not([disabled])) [part$='button'] {
      cursor: var(--vaadin-clickable-cursor);
    }

    :host([auto-expand-vertically]) #chips {
      display: contents;
    }

    :host([auto-expand-horizontally]) {
      --vaadin-field-default-width: auto;
    }
  `];var $i=new ResizeObserver(s=>{setTimeout(()=>{s.forEach(i=>{i.target.isConnected&&(i.target.resizables?i.target.resizables.forEach(e=>{e._onResize(i.contentRect)}):i.target._onResize(i.contentRect))})})}),Td=s=>class extends s{get _observeParent(){return!1}connectedCallback(){if(super.connectedCallback(),$i.observe(this),this._observeParent){let e=this.parentNode instanceof ShadowRoot?this.parentNode.host:this.parentNode;e.resizables||(e.resizables=new Set,$i.observe(e)),e.resizables.add(this),this.__parent=e}}disconnectedCallback(){super.disconnectedCallback(),$i.unobserve(this);let e=this.__parent;if(this._observeParent&&e){let t=e.resizables;t&&(t.delete(this),t.size===0&&$i.unobserve(e)),this.__parent=null}}_onResize(e){}},gt=P(Td);var kd={cleared:"Selection cleared",focused:"focused. Press Backspace to remove",selected:"added to selection",deselected:"removed from selection",total:"{count} items selected"},fa=s=>class extends ee(gi(fi(bi(de(gt(s)))))){static get properties(){return{autoExpandHorizontally:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},autoExpandVertically:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},collapseChips:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},itemClassNameGenerator:{type:Object,sync:!0},itemIdPath:{type:String,sync:!0},keepFilter:{type:Boolean,value:!1},loading:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0,sync:!0},selectedItems:{type:Array,value:()=>[],notify:!0,sync:!0},allowCustomValue:{type:Boolean,value:!1},placeholder:{type:String,observer:"_placeholderChanged",reflectToAttribute:!0,sync:!0},renderer:{type:Function,sync:!0},selectedItemsOnTop:{type:Boolean,value:!1,sync:!0},value:{type:String},_overflowItems:{type:Array,value:()=>[],sync:!0},_focusedChipIndex:{type:Number,value:-1,observer:"_focusedChipIndexChanged"},_lastFilter:{type:String,sync:!0},_topGroup:{type:Array,observer:"_topGroupChanged",sync:!0},_inputField:{type:Object}}}static get observers(){return["_selectedItemsChanged(selectedItems)","__openedOrItemsChanged(opened, _dropdownItems, loading, __keepOverlayOpened)","__updateOverflowChip(_overflow, _overflowItems, disabled, readonly)","__updateScroller(opened, _dropdownItems, _focusedIndex, _theme)","__updateTopGroup(selectedItemsOnTop, selectedItems, opened)"]}static get defaultI18n(){return kd}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get slotStyles(){let e=this.localName;return[...super.slotStyles,`
        ${e}[has-value] input::placeholder {
          color: transparent !important;
          forced-color-adjust: none;
        }
      `]}get clearElement(){return this.$.clearButton}get _chips(){return[...this.querySelectorAll('[slot="chip"]')]}get _hasValue(){return this.selectedItems&&this.selectedItems.length>0}get _tagNamePrefix(){return"vaadin-multi-select-combo-box"}ready(){super.ready(),this.addController(new W(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new V(this.inputElement,this._labelController)),this._tooltipController=new D(this),this.addController(this._tooltipController),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this._tooltipController.setShouldShow(e=>!e.opened),this._toggleElement=this.$.toggleButton,this._inputField=this.shadowRoot.querySelector('[part="input-field"]'),this._overflowController=new T(this,"overflow","vaadin-multi-select-combo-box-chip",{initializer:e=>{e.addEventListener("mousedown",t=>this._preventBlur(t)),this._overflow=e}}),this.addController(this._overflowController)}updated(e){super.updated(e),["loading","itemIdPath","itemClassNameGenerator","renderer"].forEach(r=>{e.has(r)&&(this._scroller[r]=this[r])}),e.has("selectedItems")&&this.opened&&this.$.overlay._updateOverlayWidth(),["autoExpandHorizontally","autoExpandVertically","collapseChips","disabled","readonly","clearButtonVisible","itemClassNameGenerator"].some(r=>e.has(r))&&this.__updateChips(),e.has("readonly")&&(this._setDropdownItems(this.filteredItems),this.dataProvider&&this.clearCache())}checkValidity(){return this.required&&!this.readonly?this._hasValue:!0}open(){!this.disabled&&!(this.readonly&&this.selectedItems.length===0)&&(this.opened=!0)}clear(){this.__updateSelection([]),ne(this.__effectiveI18n.cleared)}__syncTopGroup(){this._topGroup=this.selectedItemsOnTop?[...this.selectedItems]:[]}clearCache(){this.readonly||(super.clearCache(),this.__syncTopGroup())}_itemsChanged(e,t){super._itemsChanged(e,t),this.__syncTopGroup()}requestContentUpdate(){this._scroller&&(this._scroller.requestContentUpdate(),this._getItemElements().forEach(e=>{e.requestContentUpdate()}))}_onClearAction(){this.clear()}_onClosed(){this._ignoreCommitValue=!0,(!this.loading||this.allowCustomValue)&&this._commitValue()}__updateScroller(e,t,r,o){e&&(this._scroller.style.maxHeight=getComputedStyle(this).getPropertyValue(`--${this._tagNamePrefix}-overlay-max-height`)||"65vh");let n=this.hasAttribute("closing");this._scroller.setProperties({items:e||n?t:[],opened:e,focusedIndex:r,theme:o})}__openedOrItemsChanged(e,t,r,o){this._overlayOpened=e&&(o||r||!!t?.length)}_closeOrCommit(){this.opened?this.close():this._commitValue()}_commitValue(){this._lastFilter=this.filter,this._ignoreCommitValue?(this._inputElementValue="",this._focusedIndex=-1,this._ignoreCommitValue=!1):this.__commitUserInput(),(!this.keepFilter||!this.opened)&&(this.filter="")}__commitUserInput(){if(this._focusedIndex>-1){let e=this._dropdownItems[this._focusedIndex];this.__selectItem(e)}else if(this._inputElementValue){let e=[...this._dropdownItems],t=e[this.__getItemIndexByLabel(e,this._inputElementValue)];if(this.allowCustomValue&&!t){let r=this._inputElementValue;this._lastCustomValue=r,this.__clearInternalValue(!0),this.dispatchEvent(new CustomEvent("custom-value-set",{detail:r,composed:!0,bubbles:!0}))}else!this.allowCustomValue&&!this.opened&&t?this.__selectItem(t):this._inputElementValue=""}}_setFocused(e){let t=!e&&!this._closeOnBlurIsPrevented;t&&(this._ignoreCommitValue=!0),super._setFocused(e),t&&document.hasFocus()&&(this._focusedChipIndex=-1,this._requestValidation()),t&&this.readonly&&this.close()}_onResize(){this.__updateChips()}_delegateAttribute(e,t){if(this.stateTarget){if(e==="required"){this._delegateAttribute("aria-required",t?"true":!1);return}super._delegateAttribute(e,t)}}_placeholderChanged(e){let t=this.__tmpA11yPlaceholder;t!==e&&(this.__savedPlaceholder=e,t&&(this.placeholder=t))}_selectedItemsChanged(e){if(this._toggleHasValue(this._hasValue),this._hasValue){let t=this._mergeItemLabels(e);this.__tmpA11yPlaceholder===void 0&&(this.__savedPlaceholder=this.placeholder),this.__tmpA11yPlaceholder=t,this.placeholder=t}else this.__tmpA11yPlaceholder!==void 0&&(delete this.__tmpA11yPlaceholder,this.placeholder=this.__savedPlaceholder);this.__updateChips(),this.requestContentUpdate()}_topGroupChanged(e){e&&this._setDropdownItems(this.filteredItems)}_hasValidInputValue(){let e=this._focusedIndex<0&&this._inputElementValue!=="";return this.allowCustomValue||!e}_shouldFetchData(){return this.readonly?!1:super._shouldFetchData()}_setDropdownItems(e){if(this.readonly){this.__setDropdownItems(this.selectedItems);return}if(this.filter||!this.selectedItemsOnTop){this.__setDropdownItems(e);return}if(e?.length&&this._topGroup?.length){let t=e.filter(r=>this._findIndex(r,this._topGroup,this.itemIdPath)===-1);this.__setDropdownItems(this._topGroup.concat(t));return}this.__setDropdownItems(e)}__setDropdownItems(e){let t=this._dropdownItems;this._dropdownItems=e;let r=t?t[this._focusedIndex]:null;if(t&&t[this._focusedIndex]instanceof L&&e[this._focusedIndex]instanceof L)return;let o=this.__getItemIndexByValue(e,this._getItemValue(r));o>-1?this._focusedIndex=o:this._focusedIndex=this.__getItemIndexByLabel(e,this.filter)}_mergeItemLabels(e){return e.map(t=>this._getItemLabel(t)).join(", ")}_findIndex(e,t,r){if(r&&e){for(let o=0;o<t.length;o++)if(t[o]&&t[o][r]===e[r])return o;return-1}return t.indexOf(e)}__clearInternalValue(e=!1){!this.keepFilter||e?(this.filter="",this._inputElementValue=""):this._inputElementValue=this.filter}__announceItem(e,t,r){let o=t?"selected":"deselected",n=this.__effectiveI18n.total.replace("{count}",r||0);ne(`${e} ${this.__effectiveI18n[o]} ${n}`)}__removeItem(e){let t=[...this.selectedItems];t.splice(t.indexOf(e),1),this.__updateSelection(t);let r=this._getItemLabel(e);this.__announceItem(r,!1,t.length)}__selectItem(e){let t=[...this.selectedItems],r=this._findIndex(e,t,this.itemIdPath),o=this._getItemLabel(e),n=!1;if(r!==-1){if(this._lastFilter?.toLowerCase()===o.toLowerCase()){this.__clearInternalValue();return}t.splice(r,1)}else t.push(e),n=!0;this.__updateSelection(t),this.__clearInternalValue(),this.__announceItem(o,n,t.length)}__updateSelection(e){this.selectedItems=e,this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0}))}__updateTopGroup(e,t,r){e?(!r||this.__needToSyncTopGroup())&&(this._topGroup=[...t]):this._topGroup=[]}__needToSyncTopGroup(){return this.itemIdPath?this._topGroup&&this._topGroup.some(e=>{let t=this.selectedItems[this._findIndex(e,this.selectedItems,this.itemIdPath)];return t&&e!==t}):!1}__createChip(e){let t=document.createElement("vaadin-multi-select-combo-box-chip");t.setAttribute("slot","chip"),t.item=e,t.disabled=this.disabled,t.readonly=this.readonly;let r=this._getItemLabel(e);return t.label=r,t.setAttribute("title",r),typeof this.itemClassNameGenerator=="function"&&(t.className=this.itemClassNameGenerator(e)),t.addEventListener("item-removed",o=>this._onItemRemoved(o)),t.addEventListener("mousedown",o=>this._preventBlur(o)),t}__getWrapperWidth(){return this._inputField.$.wrapper.clientWidth}__getOverflowWidth(){let e=this._overflow;e.style.visibility="hidden",e.removeAttribute("hidden");let t=e.getAttribute("count");e.setAttribute("count","99");let r=getComputedStyle(e),o=e.clientWidth+parseInt(r.marginInlineStart);return e.setAttribute("count",t),e.setAttribute("hidden",""),e.style.visibility="",o}__updateChips(){if(!this._inputField||!this.inputElement)return;if(this._chips.forEach(t=>{t.remove()}),this.selectedItems.length===0){this._overflowItems=[];return}if(this.autoExpandVertically){this.selectedItems.forEach(t=>{this.appendChild(this.__createChip(t))}),this._overflowItems=[];return}let e=parseInt(getComputedStyle(this.inputElement).flexBasis);this.collapseChips?this._overflowItems=this.__updateChipsCollapsed(this.selectedItems,e):this.autoExpandHorizontally?this._overflowItems=this.__updateChipsHorizontalExpand(this.selectedItems,e):this._overflowItems=this.__updateChipsDefault(this.selectedItems,e)}__renderAllChips(e,t){let r=e.map(n=>{let a=this.__createChip(n);return this.appendChild(a),a}),o=this.__getWrapperWidth()-this.$.chips.clientWidth>=t;return{chips:r,allChipsFit:o}}__updateChipsCollapsed(e,t){let{chips:r,allChipsFit:o}=this.__renderAllChips(e,t);return o?[]:(r.forEach(n=>n.remove()),e.slice())}__updateChipsHorizontalExpand(e,t){let{chips:r,allChipsFit:o}=this.__renderAllChips(e,t);if(o)return[];let n=this.__getOverflowWidth(),a=r.length;for(;a>1&&(a-=1,r[a].remove(),!(this.__getWrapperWidth()-this.$.chips.clientWidth>=t+n)););if(a===1){let l=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width")),d=this.__getWrapperWidth()-t-n;r[0].style.maxWidth=`${Math.max(l,d)}px`}return e.slice(a)}__updateChipsDefault(e,t){let r=this.__getWrapperWidth()-t;e.length>1&&(r-=this.__getOverflowWidth());let o=parseInt(getComputedStyle(this).getPropertyValue("--_chip-min-width"));for(let n=e.length-1,a=null;n>=0;n--){let l=this.__createChip(e[n]);if(this.insertBefore(l,a),this.$.chips.clientWidth>r&&(r<o||a!==null))return l.remove(),e.slice(0,n+1);l.style.maxWidth=`${r}px`,a=l}return[]}__updateOverflowChip(e,t,r,o){if(e){let n=t.length;e.label=`${n}`,e.setAttribute("count",`${n}`),e.setAttribute("title",this._mergeItemLabels(t)),e.toggleAttribute("hidden",n===0),e.disabled=r,e.readonly=o}}_onClearButtonClick(e){e.stopPropagation(),super._onClearButtonClick(e),this.opened&&this.requestContentUpdate()}_onChange(e){e.stopPropagation()}_onEscape(e){if(this.readonly){e.stopPropagation(),this.opened&&this.close();return}this.clearButtonVisible&&!this.opened&&this.selectedItems&&this.selectedItems.length&&(e.stopPropagation(),this._onClearAction()),super._onEscape(e)}_onEscapeCancel(){this._closeOrCommit()}_onEnter(e){if(this.opened){if(e.preventDefault(),e.stopPropagation(),this.readonly)this.close();else if(this._hasValidInputValue()){let t=this._dropdownItems[this._focusedIndex];this._commitValue(),this._focusedIndex=this._dropdownItems.indexOf(t)}return}super._onEnter(e)}_onArrowDown(){this.readonly?this.opened||this.open():super._onArrowDown()}_onArrowUp(){this.readonly?this.opened||this.open():super._onArrowUp()}_onKeyDown(e){super._onKeyDown(e);let t=this._chips;if(!this.readonly&&t.length>0)switch(e.key){case"Backspace":this._onBackSpace(t);break;case"ArrowLeft":this._onArrowLeft(t,e);break;case"ArrowRight":this._onArrowRight(t,e);break;default:this._focusedChipIndex=-1;break}}_onArrowLeft(e,t){if(this.inputElement.selectionStart!==0)return;let r=this._focusedChipIndex;r!==-1&&t.preventDefault();let o;this.__isRTL?r===e.length-1?o=-1:r>-1&&(o=r+1):r===-1?o=e.length-1:r>0&&(o=r-1),o!==void 0&&(this._focusedChipIndex=o)}_onArrowRight(e,t){if(this.inputElement.selectionStart!==0)return;let r=this._focusedChipIndex;r!==-1&&t.preventDefault();let o;this.__isRTL?r===-1?o=e.length-1:r>0&&(o=r-1):r===e.length-1?o=-1:r>-1&&(o=r+1),o!==void 0&&(this._focusedChipIndex=o)}_onBackSpace(e){if(this.inputElement.selectionStart!==0)return;let t=this._focusedChipIndex;t===-1?this._focusedChipIndex=e.length-1:(this.__removeItem(e[t].item),this._focusedChipIndex=-1)}_focusedChipIndexChanged(e,t){if(e>-1||t>-1){let r=this._chips;if(r.forEach((o,n)=>{o.toggleAttribute("focused",n===e)}),e>-1){let o=r[e].item,n=this._getItemLabel(o);ne(`${n} ${this.__effectiveI18n.focused}`)}}}_overlaySelectedItemChanged(e){e.stopPropagation(),!this.hasAttribute("closing")&&(this.readonly||e.detail.item instanceof L||this.opened&&(this._lastFilter=this._inputElementValue,this.__selectItem(e.detail.item)))}_onItemRemoved(e){this.__removeItem(e.detail.item)}_preventBlur(e){e.preventDefault()}};var vs=class extends fa(v(A(g(b(_))))){static get is(){return"vaadin-multi-select-combo-box"}static get styles(){return[N,ma]}render(){return u`
      <div class="vaadin-multi-select-combo-box-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-multi-select-combo-box-container
          part="input-field"
          .autoExpandVertically="${this.autoExpandVertically}"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
        >
          <slot name="overflow" slot="prefix"></slot>
          <div id="chips" part="chips" slot="prefix">
            <slot name="chip"></slot>
          </div>
          <slot name="input"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
          <div id="toggleButton" part="field-button toggle-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-multi-select-combo-box-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>

      <vaadin-multi-select-combo-box-overlay
        id="overlay"
        exportparts="overlay, content, loader"
        .owner="${this}"
        .dir="${this.dir}"
        .opened="${this._overlayOpened}"
        ?loading="${this.loading}"
        theme="${k(this._theme)}"
        .positionTarget="${this._inputField}"
        no-vertical-overlap
      >
        <slot name="overlay"></slot>
      </vaadin-multi-select-combo-box-overlay>
    `}};m(vs);var Li=s=>class extends Ke(j(s)){static get properties(){return{_hasVaadinItemMixin:{value:!0},selected:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_selectedChanged",sync:!0},_value:String}}get _activeKeys(){return["Enter"," "]}get value(){return this._value??this.textContent.trim()}set value(e){this._value=e}ready(){super.ready();let e=this.getAttribute("value");e!==null&&(this.value=e),this.__shouldAllowFocusWhenDisabled()&&this.style.setProperty("--_vaadin-item-disabled-pointer-events","auto")}focus(e){this.disabled&&!this.__shouldAllowFocusWhenDisabled()||super.focus(e)}_shouldSetActive(e){return!this.disabled&&!(e.type==="keydown"&&e.defaultPrevented)}_selectedChanged(e){this.setAttribute("aria-selected",e)}_disabledChanged(e){super._disabledChanged(e),e&&(this.selected=!1,this.__shouldAllowFocusWhenDisabled()||this.blur())}_onKeyDown(e){super._onKeyDown(e),this._activeKeys.includes(e.key)&&!e.defaultPrevented&&(e.preventDefault(),this.click())}__shouldAllowFocusWhenDisabled(){return!1}};var bs=class extends Li(v(I(g(b(_))))){static get is(){return"vaadin-select-item"}static get styles(){return Se}static get properties(){return{role:{type:String,value:"option",reflectToAttribute:!0}}}render(){return u`
      <span part="checkmark" aria-hidden="true"></span>
      <div part="content">
        <slot></slot>
      </div>
    `}};m(bs);var ga=s=>class extends U(s){get focused(){return(this._getItems()||[]).find(Ce)}get _vertical(){return!0}get _tabNavigation(){return!1}focus(e){let t=this._getFocusableIndex();t>=0&&this._focus(t,e)}_getFocusableIndex(){let e=this._getItems();return Array.isArray(e)?this._getAvailableIndex(e,0,null,t=>!me(t)):-1}_getItems(){return Array.from(this.children)}_onKeyDown(e){if(super._onKeyDown(e),e.metaKey||e.ctrlKey)return;let{key:t,shiftKey:r}=e,o=this._getItems()||[],n=o.indexOf(this.focused),a,l,h=!this._vertical&&this.getAttribute("dir")==="rtl"?-1:1;this.__isPrevKeyPressed(t,r)?(l=-h,a=n-h):this.__isNextKeyPressed(t,r)?(l=h,a=n+h):t==="Home"?(l=1,a=0):t==="End"&&(l=-1,a=o.length-1),a=this._getAvailableIndex(o,a,l,c=>!me(c)),!(this._tabNavigation&&t==="Tab"&&(a>n&&e.shiftKey||a<n&&!e.shiftKey||a===n))&&a>=0&&(e.preventDefault(),this._focus(a,{focusVisible:!0,preventScroll:!0},!0))}__isPrevKeyPressed(e,t){return this._vertical?e==="ArrowUp":e==="ArrowLeft"||this._tabNavigation&&e==="Tab"&&t}__isNextKeyPressed(e,t){return this._vertical?e==="ArrowDown":e==="ArrowRight"||this._tabNavigation&&e==="Tab"&&!t}_focus(e,t,r=!1){let o=this._getItems();this._focusItem(o[e],t,r)}_focusItem(e,t){e&&e.focus(t)}_getAvailableIndex(e,t,r,o){let n=e.length,a=t;for(let l=0;typeof a=="number"&&l<n;l+=1,a+=r||1){a<0?a=n-1:a>=n&&(a=0);let d=e[a];if(this._isItemFocusable(d)&&this.__isMatchingItem(d,o))return a}return-1}__isMatchingItem(e,t){return typeof t=="function"?t(e):!0}_isItemFocusable(e){return!e.hasAttribute("disabled")}};var zi=s=>class extends ga(s){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},selected:{type:Number,reflectToAttribute:!0,notify:!0,sync:!0},orientation:{type:String,reflectToAttribute:!0,value:""},items:{type:Array,readOnly:!0,notify:!0},_searchBuf:{type:String,value:""}}}static get observers(){return["_enhanceItems(items, orientation, selected, disabled)"]}get _isRTL(){return!this._vertical&&this.getAttribute("dir")==="rtl"}get _scrollerElement(){return console.warn(`Please implement the '_scrollerElement' property in <${this.localName}>`),this}get _vertical(){return this.orientation!=="horizontal"}focus(e){this._observer&&this._observer.flush();let t=Array.isArray(this.items)?this.items:[],r=this._getAvailableIndex(t,0,null,o=>o.tabIndex===0&&!me(o));r>=0?this._focus(r,e):super.focus(e)}ready(){super.ready(),this.addEventListener("click",t=>this._onClick(t));let e=this.shadowRoot.querySelector("slot:not([name])");this._observer=new re(e,()=>{this._setItems(this._filterItems([...this.children]))})}_getItems(){return this.items}_enhanceItems(e,t,r,o){if(!o&&e){this.setAttribute("aria-orientation",t||"vertical"),e.forEach(a=>{t?a.setAttribute("orientation",t):a.removeAttribute("orientation")}),this._setFocusable(r<0||!r?0:r);let n=e[r];e.forEach(a=>{a.selected=a===n}),n&&!n.disabled&&this._scrollToItem(r)}}_filterItems(e){return e.filter(t=>t._hasVaadinItemMixin)}_onClick(e){if(e.metaKey||e.shiftKey||e.ctrlKey||e.defaultPrevented)return;let t=this._filterItems(e.composedPath())[0],r;t&&!t.disabled&&(r=this.items.indexOf(t))>=0&&(this.selected=r)}_searchKey(e,t){this._searchReset=x.debounce(this._searchReset,R.after(500),()=>{this._searchBuf=""}),this._searchBuf+=t.toLowerCase(),this.items.some(o=>this.__isMatchingKey(o))||(this._searchBuf=t.toLowerCase());let r=this._searchBuf.length===1?e+1:e;return this._getAvailableIndex(this.items,r,1,o=>this.__isMatchingKey(o)&&getComputedStyle(o).display!=="none")}__isMatchingKey(e){return e.textContent.replace(/[^\p{L}\p{Nd}]/gu,"").toLowerCase().startsWith(this._searchBuf)}_onKeyDown(e){if(e.metaKey||e.ctrlKey)return;let t=e.key,r=this.items.indexOf(this.focused);if(/[\p{L}\p{Nd}]/u.test(t)&&t.length===1){let o=this._searchKey(r,t);o>=0&&this._focus(o);return}super._onKeyDown(e)}_setFocusable(e){e=this._getAvailableIndex(this.items,e,1);let t=this.items[e];this.items.forEach(r=>{r.tabIndex=r===t?0:-1})}_focus(e,t){this.items.forEach((r,o)=>{r.focused=o===e}),this._setFocusable(e),this._scrollToItem(e),super._focus(e,t??{preventScroll:!0})}_scrollToItem(e){let t=this._getItems()[e];t&&t.scrollIntoView({block:"nearest",inline:"nearest"})}_scroll(e){if(this._vertical)this._scrollerElement.scrollTop+=e;else{let t=this.getAttribute("dir")||"ltr",r=ft(this._scrollerElement,t)+e;sa(this._scrollerElement,t,r)}}_isItemFocusable(e){return e.disabled&&e.__shouldAllowFocusWhenDisabled?e.__shouldAllowFocusWhenDisabled():super._isItemFocusable(e)}};var va=p`
  :host {
    --vaadin-item-checkmark-display: block;
    display: flex;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='items'] {
    height: 100%;
    overflow-y: auto;
    width: 100%;
  }

  [part='items'] ::slotted(hr) {
    border-color: var(--vaadin-divider-color, var(--vaadin-border-color-secondary));
    border-width: 0 0 1px;
    margin: 4px 8px;
    margin-inline-start: calc(var(--vaadin-icon-size, 1lh) + var(--vaadin-item-gap, var(--vaadin-gap-s)) + 8px);
  }
`;var ys=class extends zi(v(I(g(b(_))))){static get is(){return"vaadin-select-list-box"}static get styles(){return va}static get properties(){return{orientation:{readOnly:!0}}}get _scrollerElement(){return this.shadowRoot.querySelector('[part="items"]')}render(){return u`
      <div part="items">
        <slot></slot>
      </div>
    `}ready(){super.ready(),this.setAttribute("role","listbox")}};m(ys);var ba=p`
  :host {
    align-items: flex-start;
    justify-content: flex-start;
  }

  [part='overlay'] {
    min-width: var(--vaadin-select-overlay-width, var(--_vaadin-select-overlay-default-width));
  }

  [part='content'] {
    padding: var(--vaadin-item-overlay-padding, 4px);
  }

  [part='backdrop'] {
    background: transparent;
  }
`;var ya=s=>class extends rt(J(I(s))){static get observers(){return["_updateOverlayWidth(opened, positionTarget)"]}ready(){super.ready(),this.restoreFocusOnClose=!0}get _contentRoot(){return this._rendererRoot}get _rendererRoot(){if(!this.__savedRoot){let e=document.createElement("div");e.setAttribute("slot","overlay"),this.owner.appendChild(e),this.__savedRoot=e}return this.__savedRoot}_shouldCloseOnOutsideClick(e){return!0}_mouseDownListener(e){super._mouseDownListener(e),e.preventDefault()}_getMenuElement(){return Array.from(this._rendererRoot.children).find(e=>e.localName!=="style")}_updateOverlayWidth(e,t){e&&t&&this.style.setProperty("--_vaadin-select-overlay-default-width",`${t.offsetWidth}px`)}requestContentUpdate(){if(super.requestContentUpdate(),this.owner){let e=this._getMenuElement();this.owner._assignMenuElement(e)}}};var xs=class extends ya(v(g(b(_)))){static get is(){return"vaadin-select-overlay"}static get styles(){return[Z,ba]}render(){return u`
      <div id="backdrop" part="backdrop" ?hidden="${!this.withBackdrop}"></div>
      <div part="overlay" id="overlay">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}updated(i){super.updated(i),i.has("renderer")&&this.requestContentUpdate()}};m(xs);var xa=p`
  :host {
    min-height: 1lh;
    outline: none;
    overflow: hidden;
    white-space: nowrap;
    width: 100%;
    display: flex;
    align-items: center;
  }

  ::slotted(*) {
    padding: 0;
    cursor: inherit;
  }

  .vaadin-button-container,
  [part='label'] {
    display: contents;
  }

  :host([placeholder]) {
    color: var(--vaadin-input-field-placeholder-color, var(--vaadin-text-color-secondary));
  }

  :host([disabled]) {
    pointer-events: none;
  }
`;var Cs=class extends Jt(v(g(b(_)))){static get is(){return"vaadin-select-value-button"}static get styles(){return xa}render(){return u`
      <div class="vaadin-button-container">
        <span part="label">
          <slot></slot>
        </span>
      </div>
    `}};m(Cs);var Ca=p`
  .sr-only {
    border: 0 !important;
    clip: rect(1px, 1px, 1px, 1px) !important;
    clip-path: inset(50%) !important;
    height: 1px !important;
    margin: -1px !important;
    overflow: hidden !important;
    padding: 0 !important;
    position: absolute !important;
    width: 1px !important;
    white-space: nowrap !important;
  }
`;var wa=p`
  :host {
    position: relative;
  }

  ::slotted([slot='value']) {
    flex: 1;
  }

  ::slotted(div[slot='overlay']) {
    display: contents;
  }

  :host(:not([focus-ring])) [part='input-field'] {
    outline: none;
  }

  :host([readonly]:not([focus-ring])) [part='input-field'] {
    --vaadin-input-field-border-color: inherit;
  }

  [part='input-field'],
  :host(:not([readonly])) ::slotted([slot='value']) {
    cursor: var(--vaadin-clickable-cursor);
  }

  [part~='toggle-button']::before {
    mask-image: var(--_vaadin-icon-chevron-down);
  }

  :host([readonly]) [part~='toggle-button'] {
    display: none;
  }

  :host([theme~='align-start']) {
    --vaadin-item-text-align: start;
  }

  :host([theme~='align-center']) {
    --vaadin-item-text-align: center;
  }

  :host([theme~='align-end']) {
    --vaadin-item-text-align: end;
  }

  :host([theme~='align-left']) {
    --vaadin-item-text-align: left;
  }

  :host([theme~='align-right']) {
    --vaadin-item-text-align: right;
  }

  :host([theme~='align-start']) ::slotted([slot='value']) {
    justify-content: start;
  }

  :host([theme~='align-center']) ::slotted([slot='value']) {
    justify-content: center;
  }

  :host([theme~='align-end']) ::slotted([slot='value']) {
    justify-content: end;
  }

  :host([theme~='align-left']) ::slotted([slot='value']) {
    justify-content: left;
  }

  :host([theme~='align-right']) ::slotted([slot='value']) {
    justify-content: right;
  }
`;var Bi=class extends T{constructor(i){super(i,"value","vaadin-select-value-button",{initializer:(e,t)=>{t._setFocusElement(e),t.ariaTarget=e,t.stateTarget=e,e.setAttribute("aria-haspopup","listbox")}})}};var Aa=s=>class extends we(Qe(U(Ie(s)))){static get properties(){return{items:{type:Array,observer:"__itemsChanged"},opened:{type:Boolean,value:!1,notify:!0,observer:"_openedChanged",reflectToAttribute:!0,sync:!0},renderer:{type:Object},value:{type:String,value:"",notify:!0,observer:"_valueChanged",sync:!0},name:{type:String},placeholder:{type:String},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},noVerticalOverlap:{type:Boolean,value:!1},_phone:Boolean,_phoneMediaQuery:{value:"(max-width: 450px), (max-height: 450px)"},_inputContainer:Object,_items:Object}}static get delegateAttrs(){return[...super.delegateAttrs,"invalid"]}static get observers(){return["_updateAriaExpanded(opened, focusElement)","_updateSelectedItem(value, _items, placeholder, focusElement)"]}constructor(){super(),this._itemId=`value-${this.localName}-${ye()}`,this._srLabelController=new Ze(this),this._srLabelController.slotName="sr-label"}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}ready(){super.ready(),this._inputContainer=this.shadowRoot.querySelector('[part~="input-field"]'),this._overlayElement=this.$.overlay,this._valueButtonController=new Bi(this),this.addController(this._valueButtonController),this.addController(this._srLabelController),this.addController(new Te(this._phoneMediaQuery,e=>{this._phone=e})),this._tooltipController=new D(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.focusElement),this.addController(this._tooltipController)}updated(e){super.updated(e),e.has("_phone")&&this.toggleAttribute("phone",this._phone)}requestContentUpdate(){this._overlayElement&&this._overlayElement.requestContentUpdate()}_requiredChanged(e){super._requiredChanged(e),e===!1&&this._requestValidation()}__itemsChanged(e,t){(e||t)&&this.requestContentUpdate()}_assignMenuElement(e){e&&e!==this.__lastMenuElement&&(this._menuElement=e,this.__initMenuItems(e),e.addEventListener("items-changed",()=>{this.__initMenuItems(e)}),e.addEventListener("selected-changed",()=>this.__updateValueButton()),e.addEventListener("keydown",t=>this._onKeyDownInside(t),!0),e.addEventListener("click",t=>{let r=t.composedPath().find(o=>o._hasVaadinItemMixin);this.__dispatchChangePending=r?.value!==void 0&&r.value!==this.value,this.opened=!1},!0),this.__lastMenuElement=e),this._menuElement&&this._menuElement.items&&this._updateSelectedItem(this.value,this._menuElement.items)}__initMenuItems(e){e.items&&(this._items=e.items)}_valueChanged(e,t){this.toggleAttribute("has-value",!!e),t!==void 0&&!this.__dispatchChangePending&&this._requestValidation()}_onClick(e){this.disabled||(e.preventDefault(),this.opened=!this.readonly)}_onEscape(e){this.opened&&(e.stopPropagation(),this.opened=!1)}_onToggleMouseDown(e){e.preventDefault(),this.opened||this.focusElement.focus()}_onKeyDown(e){if(super._onKeyDown(e),!(e.altKey||e.shiftKey||e.ctrlKey||e.metaKey)&&e.target===this.focusElement&&!this.readonly&&!this.disabled&&!this.opened){if(/^(Enter|SpaceBar|\s|ArrowDown|Down|ArrowUp|Up)$/u.test(e.key))e.preventDefault(),this.opened=!0;else if(/[\p{L}\p{Nd}]/u.test(e.key)&&e.key.length===1){let r=this._menuElement.selected??-1,o=this._menuElement._searchKey(r,e.key);o>=0&&(this.__dispatchChangePending=!0,this._updateAriaLive(!0),this._menuElement.selected=o)}}}_onKeyDownInside(e){e.key==="Tab"&&(this.focusElement.setAttribute("tabindex","-1"),this._overlayElement.restoreFocusOnClose=!1,this.opened=!1,setTimeout(()=>{this.focusElement.setAttribute("tabindex","0"),this._overlayElement.restoreFocusOnClose=!0}))}_openedChanged(e,t){if(e){if(this.disabled||this.readonly){this.opened=!1;return}this._updateAriaLive(!1);let r=this.hasAttribute("focus-ring");this._openedWithFocusRing=r,r&&this.removeAttribute("focus-ring")}else t&&(this._openedWithFocusRing&&this.setAttribute("focus-ring",""),!this.__dispatchChangePending&&!this._keyboardActive&&this._requestValidation())}_updateAriaExpanded(e,t){t&&t.setAttribute("aria-expanded",e?"true":"false")}_updateAriaLive(e){this.focusElement&&(e?this.focusElement.setAttribute("aria-live","polite"):this.focusElement.removeAttribute("aria-live"))}__attachSelectedItem(e){let t,r=e.getAttribute("label");r?t=this.__createItemElement({label:r}):t=e.cloneNode(!0),t._sourceItem=e,this.__appendValueItemElement(t,this.focusElement),t.selected=!0}__createItemElement(e){let t=document.createElement(e.component||"vaadin-select-item");return e.label&&(t.textContent=e.label),e.value&&(t.value=e.value),e.disabled&&(t.disabled=e.disabled),e.className&&(t.className=e.className),t}__appendValueItemElement(e,t){t.appendChild(e),e.removeAttribute("tabindex"),e.removeAttribute("aria-selected"),e.removeAttribute("role"),e.removeAttribute("focused"),e.removeAttribute("focus-ring"),e.removeAttribute("active"),e.setAttribute("id",this._itemId)}_accessibleNameChanged(e){this._srLabelController.setLabel(e),this._setCustomAriaLabelledBy(e?this._srLabelController.defaultId:null)}_accessibleNameRefChanged(e){this._setCustomAriaLabelledBy(e)}_setCustomAriaLabelledBy(e){let t=this._getLabelIdWithItemId(e);this._fieldAriaController.setLabelId(t,!0)}_getLabelIdWithItemId(e){let r=(this._items?this._items[this._menuElement.selected]:!1)||this.placeholder?this._itemId:"";return e?`${e} ${r}`.trim():null}__updateValueButton(){let e=this.focusElement;if(!e)return;e.innerHTML="";let t=this._items?this._items[this._menuElement.selected]:void 0;if(e.removeAttribute("placeholder"),this._hasContent(t))this.__attachSelectedItem(t);else if(this.placeholder){let o=this.__createItemElement({label:this.placeholder});this.__appendValueItemElement(o,e),e.setAttribute("placeholder","")}!this._valueChanging&&t&&(this._selectedChanging=!0,this.value=t.value||"",this.__dispatchChangePending&&this.__dispatchChange(),delete this._selectedChanging);let r=t||this.placeholder?{newId:this._itemId}:{oldId:this._itemId};Xe(e,"aria-labelledby",r),(this.accessibleName||this.accessibleNameRef)&&this._setCustomAriaLabelledBy(this.accessibleNameRef||this._srLabelController.defaultId)}_hasContent(e){if(!e)return!1;let t=!!(e.hasAttribute("label")?e.getAttribute("label"):e.textContent.trim()),r=e.childElementCount>0;return t||r}_updateSelectedItem(e,t,r){if(t){let o=e==null?e:e.toString();this._menuElement.selected=t.reduce((n,a,l)=>n===void 0&&a.value===o?l:n,void 0),this._selectedChanging||(this._valueChanging=!0,this.__updateValueButton(),delete this._valueChanging)}else r&&this.__updateValueButton()}_shouldRemoveFocus(e){return!this.contains(e.relatedTarget)}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}checkValidity(){return!this.required||this.readonly||!!this.value}__defaultRenderer(e,t){if(!this.items||this.items.length===0){e.textContent="";return}let r=e.firstElementChild;r||(r=document.createElement("vaadin-select-list-box"),e.appendChild(r)),r.textContent="",this.items.forEach(o=>{r.appendChild(this.__createItemElement(o))})}__dispatchChange(){this._requestValidation(),this.dispatchEvent(new CustomEvent("change",{bubbles:!0})),this.__dispatchChangePending=!1}};var ws=class extends Aa(A(v(g(b(_))))){static get is(){return"vaadin-select"}static get styles(){return[N,Ca,wa]}render(){return u`
      <div class="vaadin-select-container">
        <div part="label" @click="${this._onClick}">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
          @click="${this._onClick}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="value"></slot>
          <div
            part="field-button toggle-button"
            slot="suffix"
            aria-hidden="true"
            @mousedown="${this._onToggleMouseDown}"
          ></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>

      <vaadin-select-overlay
        id="overlay"
        .owner="${this}"
        .positionTarget="${this._inputContainer}"
        .opened="${this.opened}"
        .withBackdrop="${this._phone}"
        .renderer="${this.renderer||this.__defaultRenderer}"
        ?phone="${this._phone}"
        theme="${k(this._theme)}"
        ?no-vertical-overlap="${this.noVerticalOverlap}"
        exportparts="backdrop, overlay, content"
        @opened-changed="${this._onOpenedChanged}"
        @vaadin-overlay-open="${this._onOverlayOpen}"
      >
        <slot name="overlay"></slot>
      </vaadin-select-overlay>

      <slot name="tooltip"></slot>
      <div class="sr-only">
        <slot name="sr-label"></slot>
      </div>
    `}_onOpenedChanged(i){this.opened=i.detail.value}_onOverlayOpen(){this._menuElement&&this._menuElement.focus({focusVisible:B()})}};m(ws);var Ea=p`
  :host {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--vaadin-tab-gap, var(--vaadin-gap-s));
    padding: var(--vaadin-tab-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    cursor: var(--vaadin-clickable-cursor);
    font-size: var(--vaadin-tab-font-size, 1em);
    font-weight: var(--vaadin-tab-font-weight, 500);
    line-height: var(--vaadin-tab-line-height, inherit);
    color: var(--vaadin-tab-text-color, var(--vaadin-text-color-secondary));
    background: var(--vaadin-tab-background, transparent);
    border-radius: var(--vaadin-tab-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-tab-border-width, 0) solid var(--vaadin-tab-border-color, var(--vaadin-border-color-secondary));
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    touch-action: manipulation;
    position: relative;
  }

  :host([hidden]) {
    display: none !important;
  }

  :host([orientation='vertical']) {
    justify-content: start;
  }

  :host([selected]) {
    --vaadin-tab-background: var(--vaadin-background-container);
    --vaadin-tab-text-color: var(--vaadin-text-color);
  }

  :host([disabled]) {
    cursor: var(--vaadin-disabled-cursor);
    opacity: 0.5;
  }

  :host(:is([focus-ring], :focus-visible)) {
    outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
  }

  slot {
    gap: inherit;
    align-items: inherit;
    justify-content: inherit;
  }

  ::slotted(a) {
    color: inherit !important;
    cursor: inherit !important;
    text-decoration: inherit !important;
    display: flex;
    align-items: inherit;
    justify-content: inherit;
    gap: inherit;
  }

  ::slotted(a)::before {
    content: '';
    position: absolute;
    inset: 0;
  }

  @media (forced-colors: active) {
    :host {
      border: 1px solid Canvas;
    }

    :host([selected]) {
      color: Highlight;
      border-color: Highlight;
    }

    :host([disabled]) {
      color: GrayText;
      opacity: 1;
    }
  }
`;var As=class extends Li(v(A(g(b(_))))){static get is(){return"vaadin-tab"}static get styles(){return Ea}render(){return u`
      <slot></slot>
      <slot name="tooltip"></slot>
    `}ready(){super.ready(),this.setAttribute("role","tab"),this._tooltipController=new D(this),this.addController(this._tooltipController)}_onKeyUp(i){let e=this.hasAttribute("active");if(super._onKeyUp(i),e){let t=this.querySelector("a");t&&t.click()}}};m(As);var Ia=p`
  :host {
    display: flex;
    max-width: 100%;
    max-height: 100%;
    position: relative;
    box-sizing: border-box;
    padding: var(--vaadin-tabs-padding);
    background: var(--vaadin-tabs-background);
    border-radius: var(--vaadin-tabs-border-radius);
    border: var(--vaadin-tabs-border-width, 0) solid
      var(--vaadin-tabs-border-color, var(--vaadin-border-color-secondary));
  }

  :host([hidden]) {
    display: none !important;
  }

  :host([orientation='vertical']) {
    flex-direction: column;
  }

  [part='tabs'] {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: var(--vaadin-tabs-gap, var(--vaadin-gap-s));
    animation: enable-smooth-scroll-after-first-render 1s both;
    width: 100%;
    height: 100%;
  }

  :host([overflow]) [part='tabs'] {
    overflow: auto;
  }

  :host([overflow][orientation='horizontal']) [part='tabs'] {
    overscroll-behavior: contain auto;
  }

  :host([overflow][orientation='vertical']) [part='tabs'] {
    overscroll-behavior: auto contain;
  }

  @keyframes enable-smooth-scroll-after-first-render {
    100% {
      scroll-behavior: smooth;
    }
  }

  :host([orientation='horizontal']) [part='tabs'] {
    flex-direction: row;
    scrollbar-width: none;
  }

  /* scrollbar-width is supported in Safari 18.2, use the following for earlier */
  :host([orientation='horizontal']) [part='tabs']::-webkit-scrollbar {
    display: none;
  }

  [part$='button'] {
    position: absolute;
    z-index: 1;
    pointer-events: none;
    opacity: 0;
    cursor: var(--vaadin-clickable-cursor);
    box-sizing: border-box;
    height: 100%;
    padding: var(--vaadin-tab-padding, var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container));
    background: var(--vaadin-background-color);
    display: flex;
    align-items: center;
    justify-content: center;
    -webkit-tap-highlight-color: transparent;
    -webkit-user-select: none;
    user-select: none;
    touch-action: manipulation;
  }

  [part='forward-button'] {
    inset-inline-end: 0;
  }

  :host([overflow~='start']) [part='back-button'],
  :host([overflow~='end']) [part='forward-button'] {
    pointer-events: auto;
    opacity: 1;
  }

  [part$='button']::before {
    content: '';
    display: block;
    width: var(--vaadin-icon-size, 1lh);
    height: var(--vaadin-icon-size, 1lh);
    background: currentColor;
    mask: var(--_vaadin-icon-chevron-down) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    rotate: 90deg;
  }

  [part='forward-button']::before {
    rotate: -90deg;
  }

  :host(:is([orientation='vertical'], [theme~='hide-scroll-buttons'])) [part$='button'] {
    display: none;
  }

  @media (pointer: coarse) {
    :host(:not([theme~='show-scroll-buttons'])) [part$='button'] {
      display: none;
    }
  }

  :host([dir='rtl']) [part$='button']::before {
    scale: 1 -1;
  }

  @media (forced-colors: active) {
    [part$='button']::before {
      background: CanvasText;
    }
  }
`;var Sa=s=>class extends gt(zi(s)){static get properties(){return{orientation:{value:"horizontal",type:String,reflectToAttribute:!0,sync:!0},selected:{value:0,type:Number,reflectToAttribute:!0}}}static get observers(){return["__tabsItemsChanged(items)"]}constructor(){super(),this.__itemsResizeObserver=new ResizeObserver(()=>{setTimeout(()=>this._updateOverflow())})}get _scrollOffset(){return this._vertical?this._scrollerElement.offsetHeight:this._scrollerElement.offsetWidth}get _scrollerElement(){return this.$.scroll}get __direction(){return!this._vertical&&this.__isRTL?1:-1}ready(){super.ready(),this._updateOverflow(),this._scrollerElement.addEventListener("scroll",()=>this._updateOverflow()),this.setAttribute("role","tablist")}_onResize(){this._updateOverflow()}__tabsItemsChanged(e){this.__itemsResizeObserver.disconnect(),(e||[]).forEach(t=>{this.__itemsResizeObserver.observe(t)}),this._updateOverflow()}_scrollToItem(e){let t=this.items[e],r=t.getBoundingClientRect(),o=this._scrollerElement.getBoundingClientRect(),n=this._vertical?10:20;if(this._vertical)r.bottom>o.bottom-n&&(this._scrollerElement.scrollTop=t.offsetTop-(o.height-r.height)+n),r.top<o.top+n&&(this._scrollerElement.scrollTop=t.offsetTop-n);else{let a=this.shadowRoot.querySelector('[part="back-button"]').offsetWidth,l=this.shadowRoot.querySelector('[part="forward-button"]').offsetWidth;r.right>o.right-l-n&&(this._scrollerElement.scrollLeft=t.offsetLeft-(o.width-r.width)+l+n),r.left<o.left+a+n&&(this._scrollerElement.scrollLeft=t.offsetLeft-a-n)}}_scrollForward(e){(e===void 0||this.__scrollTimer===void 0)&&this._scroll(this.__direction*(this._scrollOffset/2)*-1)}_scrollBack(e){(e===void 0||this.__scrollTimer===void 0)&&this._scroll(this.__direction*(this._scrollOffset/2))}_startScrollForward(e){e.button===0&&(this._scrollForward(),this.__scrollTimer=setInterval(this._scrollForward.bind(this),300))}_startScrollBack(e){e.button===0&&(this._scrollBack(),this.__scrollTimer=setInterval(this._scrollBack.bind(this),300))}_stopScroll(){clearTimeout(this.__scrollTimer)}_updateOverflow(){let e=this._vertical?this._scrollerElement.scrollTop:ft(this._scrollerElement,this.getAttribute("dir")),t=this._vertical?this._scrollerElement.scrollHeight:this._scrollerElement.scrollWidth,r=Math.floor(e)>1?"start":"";Math.ceil(e)<Math.ceil(t-this._scrollOffset)&&(r+=" end"),this.__direction===1&&(r=r.replace(/start|end/giu,o=>o==="start"?"end":"start")),r?this.setAttribute("overflow",r.trim()):this.removeAttribute("overflow")}};var Es=class extends Sa(A(v(g(b(_))))){static get is(){return"vaadin-tabs"}static get styles(){return Ia}render(){return u`
      <div
        @pointerdown="${this._startScrollBack}"
        @pointerup="${this._stopScroll}"
        @pointerleave="${this._stopScroll}"
        @pointercancel="${this._stopScroll}"
        @click="${this._scrollBack}"
        part="back-button"
        aria-hidden="true"
      ></div>

      <div id="scroll" part="tabs" tabindex="-1">
        <slot></slot>
      </div>

      <div
        @pointerdown="${this._startScrollForward}"
        @pointerup="${this._stopScroll}"
        @pointerleave="${this._stopScroll}"
        @pointercancel="${this._stopScroll}"
        @click="${this._scrollForward}"
        part="forward-button"
        aria-hidden="true"
      ></div>
    `}};m(Es);var Ta=p`
  :host {
    height: auto;
  }

  [part='input-field'] {
    overflow: auto;
    scroll-padding: var(
      --vaadin-input-field-padding,
      var(--vaadin-padding-block-container) var(--vaadin-padding-inline-container)
    );
  }

  ::slotted(textarea) {
    resize: none;
    white-space: pre-wrap;
  }

  [part='input-field'] ::slotted(:not(textarea)),
  [part~='clear-button'] {
    align-self: flex-start;
    position: sticky;
    top: 0;
  }

  [part~='clear-button'] {
    top: min(0px, (24px - 1lh) / -2);
  }

  /* Workaround https://bugzilla.mozilla.org/show_bug.cgi?id=1739079 */
  :host([disabled]) ::slotted(textarea) {
    user-select: none;
  }
`;var Vi=s=>class extends de(s){static get properties(){return{autocomplete:{type:String},autocorrect:{type:String,reflectToAttribute:!0},autocapitalize:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"autocapitalize","autocomplete","autocorrect"]}_inputElementChanged(e){super._inputElementChanged(e),e&&(e.value&&e.value!==this.value&&(console.warn(`Please define value on the <${this.localName}> component!`),e.value=""),this.value&&(e.value=this.value))}_setFocused(e){super._setFocused(e),!e&&document.hasFocus()&&this._requestValidation()}_onInput(e){super._onInput(e),this.invalid&&this._requestValidation()}_valueChanged(e,t){super._valueChanged(e,t),t!==void 0&&this.invalid&&this._requestValidation()}};var Ni=class extends T{constructor(i,e){super(i,"textarea","textarea",{initializer:(t,r)=>{let o=r.getAttribute("value");o&&(t.value=o);let n=r.getAttribute("name");n&&t.setAttribute("name",n),t.id=this.defaultId,typeof e=="function"&&e(t)},useUniqueId:!0})}};var ka=s=>class extends gt(Vi(s)){static get properties(){return{maxlength:{type:Number},minlength:{type:Number},pattern:{type:String},minRows:{type:Number,value:2,observer:"__minRowsChanged"},maxRows:{type:Number}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength","pattern"]}static get constraints(){return[...super.constraints,"maxlength","minlength","pattern"]}static get observers(){return["__updateMinHeight(minRows, inputElement)","__updateMaxHeight(maxRows, inputElement, _inputField)"]}get clearElement(){return this.$.clearButton}_onResize(){this._updateHeight(),this.__scrollPositionUpdated()}_onScroll(){this.__scrollPositionUpdated()}ready(){super.ready(),this.__textAreaController=new Ni(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e}),this.addController(this.__textAreaController),this.addController(new V(this.inputElement,this._labelController)),this._inputField=this.shadowRoot.querySelector("[part=input-field]"),this._inputField.addEventListener("wheel",e=>{let t=this._inputField.scrollTop;this._inputField.scrollTop+=e.deltaY,t!==this._inputField.scrollTop&&(e.preventDefault(),this.__scrollPositionUpdated())}),this._updateHeight(),this.__scrollPositionUpdated()}__scrollPositionUpdated(){this._inputField.style.setProperty("--_text-area-vertical-scroll-position","0px"),this._inputField.style.setProperty("--_text-area-vertical-scroll-position",`${this._inputField.scrollTop}px`)}_valueChanged(e,t){super._valueChanged(e,t),this._updateHeight()}_updateHeight(){let e=this.inputElement,t=this._inputField;if(!e||!t)return;let r=t.scrollTop,o=parseFloat(e.style.height),n=this.value?this.value.length:0;if(this._oldValueLength>=n){let l=getComputedStyle(t).height,d=getComputedStyle(e).width;t.style.height=l,e.style.maxWidth=d,e.style.alignSelf="flex-start",e.style.height="auto"}this._oldValueLength=n;let a=e.scrollHeight;Math.abs(a-o)<=1?e.style.height=`${o}px`:a>e.clientHeight&&(e.style.height=`${a}px`),e.style.removeProperty("max-width"),e.style.removeProperty("align-self"),t.style.removeProperty("height"),t.scrollTop=r,this.__updateMaxHeight(this.maxRows)}__updateMinHeight(e){this.inputElement&&this.inputElement===this.__textAreaController.defaultNode&&(this.inputElement.rows=Math.max(e,1))}__updateMaxHeight(e){if(!(!this._inputField||!this.inputElement))if(e){let t=getComputedStyle(this.inputElement),r=getComputedStyle(this._inputField),n=parseFloat(t.lineHeight)*e,a=parseFloat(t.paddingTop)+parseFloat(t.paddingBottom)+parseFloat(t.marginTop)+parseFloat(t.marginBottom)+parseFloat(r.borderTopWidth)+parseFloat(r.borderBottomWidth)+parseFloat(r.paddingTop)+parseFloat(r.paddingBottom),l=Math.ceil(n+a);this._inputField.style.setProperty("max-height",`${l}px`)}else this._inputField.style.removeProperty("max-height")}__minRowsChanged(e){e<1&&console.warn("<vaadin-text-area> minRows must be at least 1.")}scrollToStart(){this._inputField.scrollTop=0}scrollToEnd(){this._inputField.scrollTop=this._inputField.scrollHeight}checkValidity(){if(!super.checkValidity())return!1;if(!this.pattern||!this.inputElement.value)return!0;try{let e=this.inputElement.value.match(this.pattern);return e?e[0]===e.input:!1}catch{return!0}}};var Is=class extends ka(v(A(g(b(_))))){static get is(){return"vaadin-text-area"}static get styles(){return[N,Ta]}render(){return u`
      <div class="vaadin-text-area-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
          @scroll="${this._onScroll}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="textarea"></slot>
          <slot name="suffix" slot="suffix"></slot>
          <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>

        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new D(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}};m(Is);var Da=s=>class extends Vi(s){static get properties(){return{maxlength:{type:Number},minlength:{type:Number},pattern:{type:String}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength","pattern"]}static get constraints(){return[...super.constraints,"maxlength","minlength","pattern"]}constructor(){super(),this._setType("text")}get clearElement(){return this.$.clearButton}ready(){super.ready(),this.addController(new W(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new V(this.inputElement,this._labelController))}};var Ss=class extends Da(v(A(g(b(_))))){static get is(){return"vaadin-text-field"}static get styles(){return[N]}render(){return u`
      <div class="vaadin-field-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" @click="${this.focus}"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          .readonly="${this.readonly}"
          .disabled="${this.disabled}"
          .invalid="${this.invalid}"
          theme="${k(this._theme)}"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          ${this._renderSuffix()}
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
        <slot name="tooltip"></slot>
      </div>
    `}ready(){super.ready(),this._tooltipController=new D(this),this._tooltipController.setPosition("top"),this._tooltipController.setAriaTarget(this.inputElement),this.addController(this._tooltipController)}_renderSuffix(){return u`
      <slot name="suffix" slot="suffix"></slot>
      <div id="clearButton" part="field-button clear-button" slot="suffix" aria-hidden="true"></div>
    `}};m(Ss);var Ma=p`
  :host {
    display: inline-flex;
  }

  :host::before {
    background: var(--vaadin-upload-icon-color, currentColor);
    content: '';
    display: inline-block;
    flex: none;
    height: var(--vaadin-icon-size, 1lh);
    mask: var(--_vaadin-icon-upload) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
    width: var(--vaadin-icon-size, 1lh);
  }

  :host([hidden]) {
    display: none !important;
  }

  @media (forced-colors: active) {
    :host::before {
      background: CanvasText;
    }
  }
`;var Ts=class extends v(b(_)){static get is(){return"vaadin-upload-icon"}static get styles(){return Ma}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return u``}};m(Ts);var Pa=document.createElement("template");Pa.innerHTML=`
  <style>
    @font-face {
      font-family: 'vaadin-upload-icons';
      src: url(data:application/font-woff;charset=utf-8;base64,d09GRgABAAAAAAasAAsAAAAABmAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAABPUy8yAAABCAAAAGAAAABgDxIF5mNtYXAAAAFoAAAAVAAAAFQXVtKMZ2FzcAAAAbwAAAAIAAAACAAAABBnbHlmAAABxAAAAfQAAAH0bBJxYWhlYWQAAAO4AAAANgAAADYPD267aGhlYQAAA/AAAAAkAAAAJAfCA8tobXR4AAAEFAAAACgAAAAoHgAAx2xvY2EAAAQ8AAAAFgAAABYCSgHsbWF4cAAABFQAAAAgAAAAIAAOADVuYW1lAAAEdAAAAhYAAAIWmmcHf3Bvc3QAAAaMAAAAIAAAACAAAwAAAAMDtwGQAAUAAAKZAswAAACPApkCzAAAAesAMwEJAAAAAAAAAAAAAAAAAAAAARAAAAAAAAAAAAAAAAAAAAAAQAAA6QUDwP/AAEADwABAAAAAAQAAAAAAAAAAAAAAIAAAAAAAAwAAAAMAAAAcAAEAAwAAABwAAwABAAAAHAAEADgAAAAKAAgAAgACAAEAIOkF//3//wAAAAAAIOkA//3//wAB/+MXBAADAAEAAAAAAAAAAAAAAAEAAf//AA8AAQAAAAAAAAAAAAIAADc5AQAAAAABAAAAAAAAAAAAAgAANzkBAAAAAAEAAAAAAAAAAAACAAA3OQEAAAAAAgAA/8AEAAPAABkAMgAAEz4DMzIeAhczLgMjIg4CBycRIScFIRcOAyMiLgInIx4DMzI+AjcXphZGWmo6SH9kQwyADFiGrmJIhXJbIEYBAFoDWv76YBZGXGw8Rn5lRQyADFmIrWBIhHReIkYCWjJVPSIyVnVDXqN5RiVEYTxG/wBa2loyVT0iMlZ1Q16jeUYnRWE5RgAAAAABAIAAAAOAA4AAAgAAExEBgAMAA4D8gAHAAAAAAwAAAAAEAAOAAAIADgASAAAJASElIiY1NDYzMhYVFAYnETMRAgD+AAQA/gAdIyMdHSMjXYADgPyAgCMdHSMjHR0jwAEA/wAAAQANADMD5gNaAAUAACUBNwUBFwHT/jptATMBppMzAU2a4AIgdAAAAAEAOv/6A8YDhgALAAABJwkBBwkBFwkBNwEDxoz+xv7GjAFA/sCMAToBOoz+wAL6jP7AAUCM/sb+xowBQP7AjAE6AAAAAwAA/8AEAAPAAAcACwASAAABFSE1IREhEQEjNTMJAjMRIRECwP6A/sAEAP0AgIACQP7A/sDAAQABQICA/oABgP8AgAHAAUD+wP6AAYAAAAABAAAAAQAAdhiEdV8PPPUACwQAAAAAANX4FR8AAAAA1fgVHwAA/8AEAAPAAAAACAACAAAAAAAAAAEAAAPA/8AAAAQAAAAAAAQAAAEAAAAAAAAAAAAAAAAAAAAKBAAAAAAAAAAAAAAAAgAAAAQAAAAEAACABAAAAAQAAA0EAAA6BAAAAAAAAAAACgAUAB4AagB4AJwAsADSAPoAAAABAAAACgAzAAMAAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAADgCuAAEAAAAAAAEAEwAAAAEAAAAAAAIABwDMAAEAAAAAAAMAEwBaAAEAAAAAAAQAEwDhAAEAAAAAAAUACwA5AAEAAAAAAAYAEwCTAAEAAAAAAAoAGgEaAAMAAQQJAAEAJgATAAMAAQQJAAIADgDTAAMAAQQJAAMAJgBtAAMAAQQJAAQAJgD0AAMAAQQJAAUAFgBEAAMAAQQJAAYAJgCmAAMAAQQJAAoANAE0dmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzVmVyc2lvbiAxLjAAVgBlAHIAcwBpAG8AbgAgADEALgAwdmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzdmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzUmVndWxhcgBSAGUAZwB1AGwAYQBydmFhZGluLXVwbG9hZC1pY29ucwB2AGEAYQBkAGkAbgAtAHUAcABsAG8AYQBkAC0AaQBjAG8AbgBzRm9udCBnZW5lcmF0ZWQgYnkgSWNvTW9vbi4ARgBvAG4AdAAgAGcAZQBuAGUAcgBhAHQAZQBkACAAYgB5ACAASQBjAG8ATQBvAG8AbgAuAAAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==) format('woff');
      font-weight: normal;
      font-style: normal;
    }
  </style>
`;document.head.appendChild(Pa.content);var Fa=p`
  :host {
    display: block;
    width: 100%; /* prevent collapsing inside non-stretching column flex */
    height: var(--vaadin-progress-bar-height, 0.5lh);
    contain: layout size;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='bar'] {
    box-sizing: border-box;
    height: 100%;
    --_padding: var(--vaadin-progress-bar-padding, 0px);
    padding: var(--_padding);
    background: var(--vaadin-progress-bar-background, var(--vaadin-background-container));
    border-radius: var(--vaadin-progress-bar-border-radius, var(--vaadin-radius-m));
    border: var(--vaadin-progress-bar-border-width, 1px) solid
      var(--vaadin-progress-bar-border-color, var(--vaadin-border-color-secondary));
  }

  [part='value'] {
    box-sizing: border-box;
    height: 100%;
    width: calc(var(--vaadin-progress-value) * 100%);
    background: var(--vaadin-progress-bar-value-background, var(--vaadin-border-color));
    border-radius: calc(
      var(--vaadin-progress-bar-border-radius, var(--vaadin-radius-m)) - var(
          --vaadin-progress-bar-border-width,
          1px
        ) - var(--_padding)
    );
    transition: width 150ms;
  }

  /* Indeterminate progress */
  :host([indeterminate]) [part='value'] {
    --_w-min: clamp(8px, 5%, 16px);
    --_w-max: clamp(16px, 20%, 128px);
    animation: indeterminate var(--vaadin-progress-bar-animation-duration, 1s) linear infinite alternate;
    width: var(--_w-min);
  }

  :host([indeterminate][aria-valuenow]) [part='value'] {
    animation-delay: 150ms;
  }

  @keyframes indeterminate {
    0% {
      animation-timing-function: ease-in;
    }

    20% {
      margin-inline-start: 0%;
      width: var(--_w-max);
    }

    50% {
      margin-inline-start: calc(50% - var(--_w-max) / 2);
    }

    80% {
      width: var(--_w-max);
      margin-inline-start: calc(100% - var(--_w-max));
      animation-timing-function: ease-out;
    }

    100% {
      width: var(--_w-min);
      margin-inline-start: calc(100% - var(--_w-min));
    }
  }

  @keyframes indeterminate-reduced {
    100% {
      opacity: 0.2;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    [part='value'] {
      transition: none;
    }

    :host([indeterminate]) [part='value'] {
      width: 25%;
      animation: indeterminate-reduced 2s linear infinite alternate;
    }
  }

  @media (forced-colors: active) {
    [part='bar'] {
      border-width: max(1px, var(--vaadin-progress-bar-border-width));
    }

    [part='value'] {
      background: CanvasText !important;
    }
  }
`;var Oa=s=>class extends s{static get properties(){return{value:{type:Number,observer:"_valueChanged"},min:{type:Number,value:0,observer:"_minChanged"},max:{type:Number,value:1,observer:"_maxChanged"},indeterminate:{type:Boolean,value:!1,reflectToAttribute:!0}}}static get observers(){return["_normalizedValueChanged(value, min, max)"]}ready(){super.ready(),this.setAttribute("role","progressbar")}_normalizedValueChanged(e,t,r){let o=this._normalizeValue(e,t,r);this.style.setProperty("--vaadin-progress-value",o)}_valueChanged(e){this.setAttribute("aria-valuenow",e)}_minChanged(e){this.setAttribute("aria-valuemin",e)}_maxChanged(e){this.setAttribute("aria-valuemax",e)}_normalizeValue(e,t,r){let o;return!e&&e!==0?o=0:t>=r?o=1:(o=(e-t)/(r-t),o=Math.min(Math.max(o,0),1)),o}};var ks=class extends Oa(A(v(g(b(_))))){static get is(){return"vaadin-progress-bar"}static get styles(){return Fa}render(){return u`
      <div part="bar">
        <div part="value"></div>
      </div>
    `}};m(ks);var Ra=[ci,p`
    :host {
      align-items: baseline;
      display: grid;
      gap: var(--vaadin-upload-file-gap, var(--vaadin-gap-s));
      grid-template-columns: var(--vaadin-icon-size, 1lh) minmax(0, 1fr) auto;
      padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
      border-radius: var(--vaadin-upload-file-border-radius, var(--vaadin-radius-m));
    }

    :host(:focus-visible) {
      outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
      outline-offset: calc(var(--vaadin-focus-ring-width) * -1);
    }

    [hidden] {
      display: none;
    }

    [part='thumbnail'],
    [part='loader'] {
      display: none;
      grid-column: 1;
    }

    [part='done-icon']:not([hidden]),
    [part='warning-icon']:not([hidden]) {
      display: flex;
      grid-column: 1;
    }

    [part='loader']::before,
    [part='done-icon']::before,
    [part='warning-icon']::before {
      content: '\\2003' / '';
      display: inline-flex;
      align-items: center;
      flex: none;
      height: var(--vaadin-icon-size, 1lh);
      width: var(--vaadin-icon-size, 1lh);
    }

    [part='loader']::before {
      margin: calc(var(--vaadin-spinner-width, 2px) * -1);
    }

    :is([part$='icon'], [part$='button'])::before {
      mask-size: var(--vaadin-icon-visual-size, 100%);
      mask-position: 50%;
      mask-repeat: no-repeat;
    }

    [part='done-icon']::before {
      background: var(--vaadin-upload-file-done-color, currentColor);
      mask-image: var(--_vaadin-icon-checkmark);
    }

    [part='warning-icon']::before {
      background: var(--vaadin-upload-file-warning-color, currentColor);
      mask-image: var(--_vaadin-icon-warn);
    }

    [part='meta'] {
      grid-column: 2;

      & > div {
        cursor: inherit;
      }
    }

    [part='name'] {
      color: var(--vaadin-upload-file-name-color, var(--vaadin-text-color));
      font-size: var(--vaadin-upload-file-name-font-size, inherit);
      font-weight: var(--vaadin-upload-file-name-font-weight, inherit);
      line-height: var(--vaadin-upload-file-name-line-height, inherit);
      overflow: hidden;
      text-overflow: ellipsis;
    }

    [part='status'] {
      color: var(--vaadin-upload-file-status-color, var(--vaadin-text-color-secondary));
      font-size: var(--vaadin-upload-file-status-font-size, inherit);
      font-weight: var(--vaadin-upload-file-status-font-weight, inherit);
      line-height: var(--vaadin-upload-file-status-line-height, inherit);
    }

    [part='error'] {
      color: var(--vaadin-upload-file-error-color, var(--vaadin-text-color));
      font-size: var(--vaadin-upload-file-error-font-size, inherit);
      font-weight: var(--vaadin-upload-file-error-font-weight, inherit);
      line-height: var(--vaadin-upload-file-error-line-height, inherit);
    }

    [part='commands'] {
      display: flex;
      align-items: center;
      gap: var(--vaadin-gap-xs);
      height: var(--vaadin-icon-size, 1lh);
      align-self: center;
    }

    button {
      background: var(--vaadin-upload-file-button-background, transparent);
      border: var(--vaadin-upload-file-button-border-width, 0) solid
        var(--vaadin-upload-file-button-border-color, var(--vaadin-border-color-secondary));
      border-radius: var(--vaadin-upload-file-button-border-radius, var(--vaadin-radius-m));
      color: var(--vaadin-upload-file-button-text-color, var(--vaadin-text-color));
      cursor: var(--vaadin-clickable-cursor);
      flex-shrink: 0;
      font: inherit;
      /* Ensure minimum click target (WCAG) */
      padding: var(--vaadin-upload-file-button-padding, max(0px, (24px - var(--vaadin-icon-size, 1lh)) / 2));
    }

    button:focus-visible {
      outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
    }

    [part='start-button']::before,
    [part='retry-button']::before,
    [part='remove-button']::before {
      background: currentColor;
      content: '\\2003' / '';
      display: flex;
      align-items: center;
      height: var(--vaadin-icon-size, 1lh);
      width: var(--vaadin-icon-size, 1lh);
    }

    [part='start-button']::before {
      mask-image: var(--_vaadin-icon-play);
    }

    [part='retry-button']::before {
      mask-image: var(--_vaadin-icon-refresh);
    }

    [part='remove-button']::before {
      mask-image: var(--_vaadin-icon-cross);
    }

    ::slotted([slot='progress']) {
      grid-column: 2 / -1;
      width: auto;
    }

    :host([complete]) ::slotted([slot='progress']),
    :host([error]) ::slotted([slot='progress']) {
      display: none;
    }

    /* THUMBNAILS VARIANT */

    :host([theme~='thumbnails']) {
      grid-template-columns: max-content 1fr max-content;
      align-items: center;
      background: var(--vaadin-background-container);
      padding: 0;
      contain: content;

      & [part] {
        grid-row: 1;
      }

      & [part='thumbnail'],
      & [part$='icon'] {
        grid-column: 1;
        align-self: stretch;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: 0.2s opacity linear;
        background: var(--vaadin-background-container-strong);
        padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
        contain: content;

        &[hidden] {
          opacity: 0;
        }
      }

      & [part='thumbnail'] > img {
        object-fit: cover;
        position: absolute;
        width: 100%;
        height: 100%;
      }

      & [part='loader']:not([hidden]) {
        place-self: center;
        display: flex;
      }

      & [part='done-icon']::before {
        background: var(--vaadin-upload-file-done-color, currentColor);
        mask-image: var(--_vaadin-icon-file);
      }

      & [part='meta'] {
        padding: var(--vaadin-upload-file-padding, var(--vaadin-padding-s));
        padding-inline: 0;
      }

      & [part='name'] {
        word-break: break-all;
      }

      & [part='commands'] {
        padding: var(--vaadin-upload-padding, var(--vaadin-padding-s));
        padding-inline-start: 0;
      }

      & [part='status'] {
        clip-path: inset(50%);
        width: 1px;
        height: 1px;
        margin: 0;
        overflow: hidden;
        position: absolute;
        white-space: nowrap;
      }

      & [part='error'] {
        font-size: 0.875em;
        line-height: 1.25;
      }

      & ::slotted([slot='progress']) {
        grid-row: auto;
        grid-column: auto;
        position: absolute;
        inset: 0;
        opacity: 0.3;
        pointer-events: none;
        --vaadin-progress-bar-height: auto;
        --vaadin-progress-bar-border-width: 0px;
        --vaadin-progress-bar-border-radius: 0px;
        --vaadin-progress-bar-background: transparent;
      }

      & ::slotted([slot='progress'][indeterminate]) {
        opacity: 0;
      }
    }

    :host([theme~='thumbnails']:not([complete])) [part='thumbnail'],
    :host([theme~='thumbnails'][complete]) [part='thumbnail']:not([hidden]) + [part='done-icon'] {
      display: none;
    }

    /* TODO: queued state styles (no attribute makes this difficult to target) */

    @media (forced-colors: active) {
      :is([part$='icon'], [part$='button'])::before {
        background: CanvasText;
      }
    }
  `];var $a=s=>class extends j(s){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},complete:{type:Boolean,value:!1,reflectToAttribute:!0},errorMessage:{type:String,value:"",observer:"_errorMessageChanged"},file:{type:Object},fileName:{type:String},held:{type:Boolean,value:!1},indeterminate:{type:Boolean,value:!1,reflectToAttribute:!0},i18n:{type:Object},progress:{type:Number},status:{type:String},tabindex:{type:Number,value:0},uploading:{type:Boolean,value:!1,reflectToAttribute:!0},_progress:{type:Object},__thumbnail:{type:String}}}static get observers(){return["__updateTabindex(tabindex, disabled)","__updateProgress(_progress, progress, indeterminate)","__updateThumbnail(file)"]}ready(){super.ready(),this.addController(new T(this,"progress","vaadin-progress-bar",{initializer:e=>{this._progress=e}})),this.shadowRoot.addEventListener("focusin",e=>{e.composedPath()[0].getAttribute("part").endsWith("button")&&this._setFocused(!1)}),this.shadowRoot.addEventListener("focusout",e=>{e.relatedTarget===this&&this._setFocused(!0)})}_shouldSetFocus(e){return e.composedPath()[0]===this}__disabledChanged(e){e?this.removeAttribute("tabindex"):this.setAttribute("tabindex",this.tabindex)}_errorMessageChanged(e){this.toggleAttribute("error",!!e)}__updateTabindex(e,t){t?this.removeAttribute("tabindex"):this.setAttribute("tabindex",e)}__updateProgress(e,t,r){e&&(e.value=isNaN(t)?0:t/100,e.indeterminate=r)}_fireFileEvent(e){return e.preventDefault(),this.dispatchEvent(new CustomEvent(e.target.getAttribute("file-event"),{detail:{file:this.file},bubbles:!0,composed:!0}))}__updateThumbnail(e){if(this.__thumbnailReader&&(this.__thumbnailReader.abort(),this.__thumbnailReader=null),!e){this.__thumbnail="";return}if(e.type&&e.type.startsWith("image/")&&e instanceof Blob){let t=new FileReader;this.__thumbnailReader=t,t.onload=r=>{this.__thumbnail=r.target.result,this.__thumbnailReader=null},t.readAsDataURL(e)}else this.__thumbnail=""}};var Ds=class extends $a(v(g(b(_)))){static get is(){return"vaadin-upload-file"}static get styles(){return Ra}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){let i=this.held&&!this.uploading&&!this.complete,e=this.errorMessage;return u`
      <div part="loader" ?hidden="${!this.uploading}" aria-hidden="true"></div>

      ${this.__thumbnail?u`<div part="thumbnail">
            <img src="${this.__thumbnail}" alt="${this.fileName}" />
          </div>`:M}

      <div part="done-icon" ?hidden="${!this.complete}" aria-hidden="true"></div>
      <div part="warning-icon" ?hidden="${!this.errorMessage}" aria-hidden="true"></div>

      <div part="meta">
        <div part="name" id="name">${this.fileName}</div>
        <div part="status" ?hidden="${!this.status}" id="status">${this.status}</div>
        <div part="error" id="error" ?hidden="${!this.errorMessage}">${this.errorMessage}</div>
      </div>

      <div part="commands">
        <button
          type="button"
          part="start-button"
          file-event="file-start"
          @click="${this._fireFileEvent}"
          ?hidden="${!i}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.start:M}"
          aria-describedby="name"
        ></button>
        <button
          type="button"
          part="retry-button"
          file-event="file-retry"
          @click="${this._fireFileEvent}"
          ?hidden="${!e}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.retry:M}"
          aria-describedby="name"
        ></button>
        <button
          type="button"
          part="remove-button"
          file-event="file-abort"
          @click="${this._fireFileEvent}"
          ?disabled="${this.disabled}"
          aria-label="${this.i18n?this.i18n.file.remove:M}"
          aria-describedby="name"
        ></button>
      </div>

      <slot name="progress"></slot>
    `}};m(Ds);var La=p`
  :host {
    display: block;
    overflow: auto;
  }

  :host([hidden]) {
    display: none !important;
  }

  [part='list'] {
    list-style-type: none;
    margin: 0;
    padding: 0;
  }

  ::slotted(:first-child) {
    margin-top: var(--vaadin-upload-gap, var(--vaadin-gap-s));
  }

  ::slotted(li:not(:last-of-type)) {
    border-bottom: var(--vaadin-upload-file-list-divider-width, 1px) solid
      var(--vaadin-upload-file-list-divider-color, var(--vaadin-border-color-secondary));
  }

  /* Thumbnails theme variant */
  :host([theme~='thumbnails']) [part='list'] {
    display: flex;
    flex-wrap: wrap;
    gap: var(--vaadin-gap-s);
  }

  :host([theme~='thumbnails']) ::slotted(:first-child) {
    margin-top: 0;
  }

  :host([theme~='thumbnails']) ::slotted(li) {
    border-bottom: none;
  }
`;var q=class extends EventTarget{#e=[];#r=!1;#i=!1;#t=[];#s=0;#o="POST";#d=1/0;#u=3;#p={};constructor(i={}){super(),this.target=i.target||"",this.method=i.method||"POST",this.headers=i.headers||{},this.timeout=i.timeout||0,this.maxFiles=i.maxFiles??1/0,this.maxFileSize=i.maxFileSize??1/0,this.accept=i.accept||"",this.noAuto=i.noAuto??!1,this.withCredentials=i.withCredentials??!1,this.uploadFormat=i.uploadFormat||"raw",this.maxConcurrentUploads=i.maxConcurrentUploads??3,this.formDataName=i.formDataName||"file",this.disabled=i.disabled??!1}get method(){return this.#o}set method(i){if(i!=="POST"&&i!=="PUT")throw new Error(`Invalid method "${i}". Only POST and PUT are allowed.`);this.#o=i}get maxFiles(){return this.#d}set maxFiles(i){if(i<0)throw new Error(`Invalid maxFiles "${i}". Value must be non-negative.`);this.#d=i,this.#_()}get maxConcurrentUploads(){return this.#u}set maxConcurrentUploads(i){if(i<=0)throw new Error(`Invalid maxConcurrentUploads "${i}". Value must be positive.`);this.#u=i}get headers(){return this.#p}set headers(i){this.#p={...i}}get files(){return[...this.#e]}set files(i){let e=[];for(let t of i){if(this.#e.includes(t)){e.push(t);continue}let r=this.#m(t,e.length);if(r){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:t,error:r}}));continue}e.push(t)}this.#h(e)}#h(i){this.#e=i,this.#_(),this.#n()}get maxFilesReached(){return this.#r}get disabled(){return this.#i}set disabled(i){let e=!!i;e!==this.#i&&(this.#i=e,this.dispatchEvent(new CustomEvent("disabled-changed",{detail:{value:e}})))}addFiles(i){Array.from(i).forEach(e=>this.#b(e))}uploadFiles(i=this.#e){i&&!Array.isArray(i)&&(i=[i]),i.filter(e=>this.#e.includes(e)&&!e.complete).forEach(e=>this.#c(e))}retryUpload(i){this.#C(i)}abortUpload(i){this.#w(i)}removeFile(i){this.#f(i)}get#v(){if(!this.accept)return null;let i=this.accept.split(",").map(e=>{let t=e.trim();return t=t.replaceAll(/[+.]/gu,String.raw`\$&`),t.startsWith(String.raw`\.`)&&(t=`.*${t}$`),t.replaceAll("/*","/.*")});return new RegExp(`^(${i.join("|")})$`,"iu")}#_(){let i=this.maxFiles>=0&&this.#e.length>=this.maxFiles;i!==this.#r&&(this.#r=i,this.dispatchEvent(new CustomEvent("max-files-reached-changed",{detail:{value:i}})))}#m(i,e){if(e>=this.maxFiles)return"tooManyFiles";if(this.maxFileSize>=0&&i.size>this.maxFileSize)return"fileIsTooBig";let t=this.#v;return t&&!(t.test(i.type)||t.test(i.name))?"incorrectFileType":null}#b(i){let e=this.#m(i,this.#e.length);if(e){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:i,error:e}}));return}i.loaded=0,i.held=!0,i.formDataName=this.formDataName,this.#h([i,...this.#e]),this.noAuto||this.#c(i)}#f(i){this.#t=this.#t.filter(t=>t!==i),i.uploading&&!i.held&&!i.abort&&i.xhr&&(i.abort=!0,i.xhr.abort());let e=this.#e.indexOf(i);e>=0&&(this.#h(this.#e.filter(t=>t!==i)),this.dispatchEvent(new CustomEvent("file-remove",{detail:{file:i,fileIndex:e}})))}#c(i){i.uploading||this.#t.includes(i)||(i.loaded=0,i.progress=0,i.held=!0,i.uploading=i.indeterminate=!0,i.complete=i.abort=i.errorKey=!1,i.stalled=!1,this.#n(),this.#t.push(i),this.#a())}#a(){for(;this.#t.length>0&&this.#s<this.maxConcurrentUploads;){let i=this.#t.shift();i&&this.#y(i)}}#y(i){this.#s+=1;let e=Date.now(),t=i.xhr=this._createXhr(),r;t.upload.onprogress=h=>{clearTimeout(r);let c=(Date.now()-e)/1e3,f=h.loaded,y=h.total,w=y>0?Math.trunc(f/y*100):100,S=Math.max(0,Math.min(100,w));i.loaded=f,i.progress=S,i.indeterminate=y>0?f<=0||f>=y:!1,i.stalled&&(i.stalled=!1),i.errorKey?i.indeterminate=i.status=void 0:i.abort||S<100&&(this.#A(i,y,f,c),r=setTimeout(()=>{i.uploading&&!i.abort&&(i.stalled=!0,this.#n())},2e3)),this.#n(),this.dispatchEvent(new CustomEvent("upload-progress",{detail:{file:i,xhr:t}}))},t.onabort=()=>{clearTimeout(r),this.#s-=1,this.#l(t),this.#a()},t.ontimeout=()=>{clearTimeout(r),i.indeterminate=i.uploading=!1,i.errorKey="timeout",i.status="",this.#s-=1,this.#a(),this.#l(t),this.dispatchEvent(new CustomEvent("upload-error",{detail:{file:i,xhr:t}})),this.#n()},t.onreadystatechange=()=>{if(t.readyState===4){if(clearTimeout(r),i.indeterminate=i.uploading=!1,this.#s-=1,this.#a(),this.#l(t),i.abort||i.errorKey||(i.status="",!this.dispatchEvent(new CustomEvent("upload-response",{detail:{file:i,xhr:t},cancelable:!0}))))return;t.status===0?i.errorKey="serverUnavailable":t.status>=500?i.errorKey="unexpectedServerError":t.status===413?i.errorKey="fileTooLarge":t.status>=400&&(i.errorKey="forbidden"),i.complete=!i.errorKey;let c=i.errorKey?"upload-error":"upload-success";this.dispatchEvent(new CustomEvent(c,{detail:{file:i,xhr:t}})),i.xhr=null,this.#n()}};let o=this.uploadFormat==="raw";if(i.uploadTarget||(i.uploadTarget=this.target),!this.dispatchEvent(new CustomEvent("upload-before",{detail:{file:i,xhr:t},cancelable:!0}))){this.#g(i);return}if(!this.#e.includes(i)){i.abort||(this.#s-=1),this.#l(t),this.#a();return}let a;if(o)a=i;else{let h=new FormData;h.append(i.formDataName||this.formDataName,i,i.name),a=h}t.open(this.method,i.uploadTarget,!0),this.#x(t,i,o),i.held=!1,t.upload.onloadstart=()=>{this.dispatchEvent(new CustomEvent("upload-start",{detail:{file:i,xhr:t}})),this.#n()};let l={file:i,xhr:t,uploadFormat:this.uploadFormat,requestBody:a};if(o||(l.formData=a),!this.dispatchEvent(new CustomEvent("upload-request",{detail:l,cancelable:!0}))){this.#g(i);return}if(!this.#e.includes(i)){i.abort||(this.#s-=1),this.#l(t),this.#a();return}try{t.send(a)}catch(h){this.#s-=1,i.uploading=!1,i.indeterminate=!1,i.errorKey=h.message||"sendFailed",this.#l(t),this.#n(),this.#a()}}_createXhr(){return new XMLHttpRequest}#g(i){this.#s-=1,i.uploading=!1,i.indeterminate=!1,i.held=!0,this.#n(),this.#a()}#l(i){i&&(i.upload.onprogress=null,i.upload.onloadstart=null,i.onreadystatechange=null,i.onabort=null,i.ontimeout=null)}#x(i,e,t){Object.entries(this.headers).forEach(([r,o])=>{i.setRequestHeader(r,o)}),t&&(i.setRequestHeader("Content-Type",e.type||"application/octet-stream"),i.setRequestHeader("X-Filename",encodeURIComponent(e.name))),this.timeout&&(i.timeout=this.timeout),i.withCredentials=this.withCredentials}#C(i){this.dispatchEvent(new CustomEvent("upload-retry",{detail:{file:i,xhr:i.xhr},cancelable:!0}))&&(i.uploading=!1,this.#t=this.#t.filter(t=>t!==i),this.#c(i))}#w(i){this.dispatchEvent(new CustomEvent("upload-abort",{detail:{file:i,xhr:i.xhr},cancelable:!0}))&&(i.abort=!0,i.xhr&&i.xhr.abort(),this.#f(i))}#A(i,e,t,r){i.elapsed=r,i.remaining=t>0?Math.ceil(r*(e/t-1)):0,i.speed=r>0?Math.trunc(t/r/1024):0,i.total=e}#n(){this.dispatchEvent(new CustomEvent("files-changed",{detail:{value:this.#e}}))}};var Dd={file:{retry:"Retry",start:"Start",remove:"Remove"},error:{tooManyFiles:"Too Many Files.",fileIsTooBig:"File is Too Big.",incorrectFileType:"Incorrect File Type."},uploading:{status:{connecting:"Connecting...",stalled:"Stalled",processing:"Processing File...",held:"Queued"},remainingTime:{prefix:"remaining time: ",unknown:"unknown remaining time"},error:{serverUnavailable:"Upload failed, please try again later",unexpectedServerError:"Upload failed due to server error",forbidden:"Upload forbidden",fileTooLarge:"File is too large"}},units:{size:["B","kB","MB","GB","TB","PB","EB","ZB","YB"]}},za=s=>class extends ee(s){static get properties(){return{items:{type:Array},disabled:{type:Boolean,value:!1,reflectToAttribute:!0},manager:{type:Object,value:null,observer:"__managerChanged"}}}static get observers(){return["__updateItems(items, __effectiveI18n, disabled, _theme)"]}static get defaultI18n(){return Dd}get i18n(){return super.i18n}set i18n(e){super.i18n=e}constructor(){super(),this.__onManagerFilesChanged=this.__onManagerFilesChanged.bind(this),this.__onManagerDisabledChanged=this.__onManagerDisabledChanged.bind(this),this.__onFileRetry=this.__onFileRetry.bind(this),this.__onFileAbort=this.__onFileAbort.bind(this),this.__onFileStart=this.__onFileStart.bind(this),this.__onFileRemove=this.__onFileRemove.bind(this)}ready(){super.ready(),this.addEventListener("file-retry",this.__onFileRetry),this.addEventListener("file-abort",this.__onFileAbort),this.addEventListener("file-start",this.__onFileStart),this.addEventListener("file-remove",this.__onFileRemove)}disconnectedCallback(){super.disconnectedCallback(),this.manager instanceof q&&(this.manager.removeEventListener("files-changed",this.__onManagerFilesChanged),this.manager.removeEventListener("disabled-changed",this.__onManagerDisabledChanged))}connectedCallback(){super.connectedCallback(),this.manager instanceof q&&(this.manager.addEventListener("files-changed",this.__onManagerFilesChanged),this.manager.addEventListener("disabled-changed",this.__onManagerDisabledChanged),this.__syncFromManager())}__managerChanged(e,t){t instanceof q&&(t.removeEventListener("files-changed",this.__onManagerFilesChanged),t.removeEventListener("disabled-changed",this.__onManagerDisabledChanged)),this.isConnected&&e instanceof q?(e.addEventListener("files-changed",this.__onManagerFilesChanged),e.addEventListener("disabled-changed",this.__onManagerDisabledChanged),this.__syncFromManager()):this.items=[]}__onManagerFilesChanged(){this.__syncFromManager()}__onManagerDisabledChanged(){this.requestContentUpdate()}__syncFromManager(){this.manager instanceof q&&(this.items=[...this.manager.files])}__onFileRetry(e){this.manager instanceof q&&(e.stopPropagation(),this.manager.retryUpload(e.detail.file))}__onFileAbort(e){this.manager instanceof q&&(e.stopPropagation(),this.manager.abortUpload(e.detail.file))}__onFileStart(e){this.manager instanceof q&&(e.stopPropagation(),this.manager.uploadFiles(e.detail.file))}__onFileRemove(e){this.manager instanceof q&&(e.stopPropagation(),this.manager.removeFile(e.detail.file))}__updateItems(e,t,r,o){e&&t&&(e.forEach(n=>this.__applyI18nToFile(n)),this.requestContentUpdate())}__applyI18nToFile(e){let t=this.__effectiveI18n;e.total&&this.__applyFileSizeStrings(e),e.status=this.__getFileStatus(e,t),this.__applyFileError(e,t)}__applyFileSizeStrings(e){e.totalStr=this.__formatSize(e.total),e.loadedStr=this.__formatSize(e.loaded||0),e.elapsed!=null&&(e.elapsedStr=this.__formatTime(e.elapsed,this.__splitTimeByUnits(e.elapsed))),e.remaining!=null&&(e.remainingStr=this.__formatTime(e.remaining,this.__splitTimeByUnits(e.remaining)))}__getFileStatus(e,t){return e.held&&!e.error?t.uploading.status.held:e.stalled?t.uploading.status.stalled:e.uploading&&e.indeterminate&&!e.held?e.progress===100?t.uploading.status.processing:t.uploading.status.connecting:e.uploading&&e.progress<100&&e.total?this.__formatFileProgress(e):e.status}__applyFileError(e,t){e.errorKey&&t.uploading.error[e.errorKey]?e.error=t.uploading.error[e.errorKey]:!e.errorKey&&this.manager instanceof q&&(e.error="")}__formatSize(e){let t=this.__effectiveI18n;if(typeof t.formatSize=="function")return t.formatSize(e);let r=t.units.sizeBase||1e3,o=Math.trunc(Math.log(e)/Math.log(r)),n=Math.max(0,Math.min(3,o-1));return`${Number.parseFloat((e/r**o).toFixed(n))} ${t.units.size[o]}`}__splitTimeByUnits(e){let t=[60,60,24,1/0],r=[0];for(let o=0;o<t.length&&e>0;o++)r[o]=e%t[o],e=Math.floor(e/t[o]);return r}__formatTime(e,t){let r=this.__effectiveI18n;if(typeof r.formatTime=="function")return r.formatTime(e,t);for(;t.length<3;)t.push(0);return t.reverse().map(o=>(o<10?"0":"")+o).join(":")}__formatFileProgress(e){let t=this.__effectiveI18n,r=e.loaded>0?t.uploading.remainingTime.prefix+e.remainingStr:t.uploading.remainingTime.unknown;return`${e.totalStr}: ${e.progress}% (${r})`}requestContentUpdate(){let{items:e,__effectiveI18n:t,disabled:r}=this,o=this.manager instanceof q&&this.manager.disabled,n=r||o;Nt(u`
          ${e.map(a=>u`
              <li>
                <vaadin-upload-file
                  .disabled="${n}"
                  .file="${a}"
                  .complete="${a.complete}"
                  .errorMessage="${a.error}"
                  .fileName="${a.name}"
                  .held="${a.held}"
                  .indeterminate="${a.indeterminate}"
                  .progress="${a.progress}"
                  .status="${a.status}"
                  .uploading="${a.uploading}"
                  .i18n="${t}"
                  theme="${k(this._theme)}"
                ></vaadin-upload-file>
              </li>
            `)}
        `,this)}};var Ms=class extends za(v(g(_))){static get is(){return"vaadin-upload-file-list"}static get styles(){return La}render(){return u`
      <ul part="list">
        <slot></slot>
      </ul>
    `}};m(Ms);var Ba=p`
  :host {
    background: var(--vaadin-upload-background, transparent);
    border: var(--vaadin-upload-border-width, 1px) solid
      var(--vaadin-upload-border-color, var(--vaadin-border-color-secondary));
    border-radius: var(--vaadin-upload-border-radius, var(--vaadin-radius-m));
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    padding: var(--vaadin-upload-padding, var(--vaadin-padding-s));
    position: relative;
  }

  :host([dragover-valid]) {
    --vaadin-upload-background: var(--vaadin-background-container);
    --vaadin-upload-border-color: var(--vaadin-text-color);
    border-style: dashed;
  }

  :host([hidden]) {
    display: none !important;
  }

  [hidden] {
    display: none !important;
  }

  [part='primary-buttons'] {
    align-items: center;
    display: flex;
    gap: var(--vaadin-gap-s);
  }

  [part='drop-label'] {
    align-items: center;
    color: var(--vaadin-upload-drop-label-color, var(--vaadin-text-color));
    display: flex;
    font-size: var(--vaadin-upload-drop-label-font-size, inherit);
    font-weight: var(--vaadin-upload-drop-label-font-weight, inherit);
    gap: var(--vaadin-upload-drop-label-gap, var(--vaadin-gap-s));
    line-height: var(--vaadin-upload-drop-label-line-height, inherit);
  }
`;function Va(s){async function i(r){if(r.isFile)return new Promise(o=>{r.file(o,()=>o([]))});if(r.isDirectory){let o=r.createReader(),n=await new Promise(l=>{o.readEntries(l,()=>l([]))});return(await Promise.all(n.map(i))).flat()}}if(!Array.from(s.dataTransfer.items).filter(r=>!!r).filter(r=>typeof r.webkitGetAsEntry=="function").map(r=>r.webkitGetAsEntry()).some(r=>!!r?.isDirectory))return Promise.resolve(s.dataTransfer.files?Array.from(s.dataTransfer.files):[]);let t=Array.from(s.dataTransfer.items).map(r=>r.webkitGetAsEntry()).filter(r=>!!r).map(i);return Promise.all(t).then(r=>r.flat())}var Md={dropFiles:{one:"Drop file here",many:"Drop files here"},addFiles:{one:"Upload File...",many:"Upload Files..."},error:{tooManyFiles:"Too Many Files.",fileIsTooBig:"File is Too Big.",incorrectFileType:"Incorrect File Type."},uploading:{status:{connecting:"Connecting...",stalled:"Stalled",processing:"Processing File...",held:"Queued"},remainingTime:{prefix:"remaining time: ",unknown:"unknown remaining time"},error:{serverUnavailable:"Upload failed, please try again later",unexpectedServerError:"Upload failed due to server error",forbidden:"Upload forbidden",fileTooLarge:"File is too large"}},file:{retry:"Retry",start:"Start",remove:"Remove"},units:{size:["B","kB","MB","GB","TB","PB","EB","ZB","YB"]}},Ps=class extends T{constructor(i){super(i,"add-button","vaadin-button")}initNode(i){i._isDefault&&(this.defaultNode=i),i.addEventListener("touchend",e=>{this.host._onAddFilesTouchEnd(e)}),i.addEventListener("click",e=>{this.host._onAddFilesClick(e)}),this.host._addButton=i}},Fs=class extends T{constructor(i){super(i,"drop-label","span")}initNode(i){i._isDefault&&(this.defaultNode=i),this.host._dropLabel=i}},Na=s=>class extends ee(s){static get properties(){return{disabled:{type:Boolean,value:!1,reflectToAttribute:!0},nodrop:{type:Boolean,reflectToAttribute:!0,value:K},target:{type:String,value:""},method:{type:String,value:"POST"},headers:{type:Object,value:{}},timeout:{type:Number,value:0},_dragover:{type:Boolean,value:!1,observer:"_dragoverChanged"},files:{type:Array,notify:!0,value:()=>[],sync:!0},maxFiles:{type:Number,value:1/0,sync:!0},maxFilesReached:{type:Boolean,value:!1,notify:!0,readOnly:!0,reflectToAttribute:!0},accept:{type:String,value:""},maxFileSize:{type:Number,value:1/0},_dragoverValid:{type:Boolean,value:!1,observer:"_dragoverValidChanged"},formDataName:{type:String,value:"file"},noAuto:{type:Boolean,value:!1},withCredentials:{type:Boolean,value:!1},uploadFormat:{type:String,value:"raw"},maxConcurrentUploads:{type:Number,value:3,sync:!0},capture:{type:String},_addButton:{type:Object},_dropLabel:{type:Object},_fileList:{type:Object},_files:{type:Array},_uploadQueue:{type:Array,value:()=>[]},_activeUploads:{type:Number,value:0}}}static get observers(){return["__updateAddButton(_addButton, maxFiles, __effectiveI18n, maxFilesReached, disabled)","__updateDropLabel(_dropLabel, maxFiles, __effectiveI18n)","__updateFileList(_fileList, files, __effectiveI18n, disabled, _theme)","__updateMaxFilesReached(maxFiles, files)"]}static get defaultI18n(){return Md}get i18n(){return super.i18n}set i18n(e){super.i18n=e}get __acceptRegexp(){if(!this.accept)return null;let e=this.accept.split(",").map(t=>{let r=t.trim();return r=r.replace(/[+.]/gu,"\\$&"),r.startsWith("\\.")&&(r=`.*${r}$`),r.replace(/\/\*/gu,"/.*")});return new RegExp(`^(${e.join("|")})$`,"iu")}ready(){super.ready(),this.addEventListener("dragover",this._onDragover.bind(this)),this.addEventListener("dragleave",this._onDragleave.bind(this)),this.addEventListener("drop",this._onDrop.bind(this)),this.addEventListener("file-retry",this._onFileRetry.bind(this)),this.addEventListener("file-abort",this._onFileAbort.bind(this)),this.addEventListener("file-start",this._onFileStart.bind(this)),this.addEventListener("file-reject",this._onFileReject.bind(this)),this.addEventListener("upload-start",this._onUploadStart.bind(this)),this.addEventListener("upload-success",this._onUploadSuccess.bind(this)),this.addEventListener("upload-error",this._onUploadError.bind(this)),this._addButtonController=new Ps(this),this.addController(this._addButtonController),this._dropLabelController=new Fs(this),this.addController(this._dropLabelController),this.addController(new T(this,"file-list","vaadin-upload-file-list",{initializer:e=>{this._fileList=e}})),this.addController(new T(this,"drop-label-icon","vaadin-upload-icon"))}_formatSize(e){if(typeof this.__effectiveI18n.formatSize=="function")return this.__effectiveI18n.formatSize(e);let t=this.__effectiveI18n.units.sizeBase||1e3,r=~~(Math.log(e)/Math.log(t)),o=Math.max(0,Math.min(3,r-1));return`${parseFloat((e/t**r).toFixed(o))} ${this.__effectiveI18n.units.size[r]}`}_splitTimeByUnits(e){let t=[60,60,24,1/0],r=[0];for(let o=0;o<t.length&&e>0;o++)r[o]=e%t[o],e=Math.floor(e/t[o]);return r}_formatTime(e,t){if(typeof this.__effectiveI18n.formatTime=="function")return this.__effectiveI18n.formatTime(e,t);for(;t.length<3;)t.push(0);return t.reverse().map(r=>(r<10?"0":"")+r).join(":")}_formatFileProgress(e){let t=e.loaded>0?this.__effectiveI18n.uploading.remainingTime.prefix+e.remainingStr:this.__effectiveI18n.uploading.remainingTime.unknown;return`${e.totalStr}: ${e.progress}% (${t})`}__updateMaxFilesReached(e,t){this._setMaxFilesReached(e>=0&&t.length>=e)}__updateAddButton(e,t,r,o,n){e&&(e.disabled=n||o,e===this._addButtonController.defaultNode&&(e.textContent=this._i18nPlural(t,r.addFiles)))}__updateDropLabel(e,t,r){e&&e===this._dropLabelController.defaultNode&&(e.textContent=this._i18nPlural(t,r.dropFiles))}__updateFileList(e,t,r,o){e&&(e.items=[...t],e.i18n=r,e.disabled=o,this._theme?e.setAttribute("theme",this._theme):e.removeAttribute("theme"))}_onDragover(e){e.preventDefault(),!this.nodrop&&!this._dragover&&(this._dragoverValid=!this.maxFilesReached&&!this.disabled,this._dragover=!0),e.dataTransfer.dropEffect=!this._dragoverValid||this.nodrop?"none":"copy"}_onDragleave(e){e.preventDefault(),this._dragover&&!this.nodrop&&(this._dragover=this._dragoverValid=!1)}async _onDrop(e){if(!this.nodrop&&!this.disabled){e.preventDefault(),this._dragover=this._dragoverValid=!1;let t=await Va(e);this._addFiles(t)}}_createXhr(){return new XMLHttpRequest}_configureXhr(e,t=null,r=!1){if(typeof this.headers=="string")try{this.headers=JSON.parse(this.headers)}catch{this.headers=void 0}Object.entries(this.headers).forEach(([o,n])=>{e.setRequestHeader(o,n)}),r&&t&&(e.setRequestHeader("Content-Type",t.type||"application/octet-stream"),e.setRequestHeader("X-Filename",encodeURIComponent(t.name))),this.timeout&&(e.timeout=this.timeout),e.withCredentials=this.withCredentials}_setStatus(e,t,r,o){e.elapsed=o,e.elapsedStr=this._formatTime(e.elapsed,this._splitTimeByUnits(e.elapsed)),e.remaining=Math.ceil(o*(t/r-1)),e.remainingStr=this._formatTime(e.remaining,this._splitTimeByUnits(e.remaining)),e.speed=~~(t/o/1024),e.totalStr=this._formatSize(t),e.loadedStr=this._formatSize(r),e.status=this._formatFileProgress(e)}uploadFiles(e=this.files){e&&!Array.isArray(e)&&(e=[e]),e.filter(t=>!t.complete).forEach(t=>this._queueFileUpload(t))}_queueFileUpload(e){e.uploading||(e.held=!0,e.uploading=e.indeterminate=!0,e.complete=e.abort=e.error=!1,e.status=this.__effectiveI18n.uploading.status.held,this._renderFileList(),this._uploadQueue.push(e),this._processUploadQueue())}_processUploadQueue(){for(;this._uploadQueue.length>0&&this._activeUploads<this.maxConcurrentUploads;){let e=this._uploadQueue.shift();e&&this._uploadFile(e)}}_uploadFile(e){this._activeUploads+=1;let t=Date.now(),r=e.xhr=this._createXhr(),o,n;r.upload.onprogress=f=>{clearTimeout(o),n=Date.now();let y=(n-t)/1e3,w=f.loaded,S=f.total,Q=~~(w/S*100);e.loaded=w,e.progress=Q,e.indeterminate=w<=0||w>=S,e.error?e.indeterminate=e.status=void 0:e.abort||(Q<100?(this._setStatus(e,S,w,y),o=setTimeout(()=>{e.status=this.__effectiveI18n.uploading.status.stalled,this._renderFileList()},2e3)):(e.loadedStr=e.totalStr,e.status=this.__effectiveI18n.uploading.status.processing)),this._renderFileList(),this.dispatchEvent(new CustomEvent("upload-progress",{detail:{file:e,xhr:r}}))},r.onabort=()=>{this._activeUploads-=1,this._processUploadQueue()},r.onreadystatechange=()=>{if(r.readyState===4){if(clearTimeout(o),e.indeterminate=e.uploading=!1,this._activeUploads-=1,this._processUploadQueue(),e.abort||(e.status="",!this.dispatchEvent(new CustomEvent("upload-response",{detail:{file:e,xhr:r},cancelable:!0}))))return;r.status===0?e.error=this.__effectiveI18n.uploading.error.serverUnavailable:r.status>=500?e.error=this.__effectiveI18n.uploading.error.unexpectedServerError:r.status===413?e.error=this.__effectiveI18n.uploading.error.fileTooLarge:r.status>=400&&(e.error=this.__effectiveI18n.uploading.error.forbidden),e.complete=!e.error,this.dispatchEvent(new CustomEvent(`upload-${e.error?"error":"success"}`,{detail:{file:e,xhr:r}})),this._renderFileList()}};let a=this.uploadFormat==="raw";if(e.uploadTarget||(e.uploadTarget=this.target||""),a||(e.formDataName=this.formDataName),!this.dispatchEvent(new CustomEvent("upload-before",{detail:{file:e,xhr:r},cancelable:!0})))return;let d;if(a)d=e;else{let f=new FormData;f.append(e.formDataName,e,e.name),d=f}r.open(this.method,e.uploadTarget,!0),this._configureXhr(r,e,a),e.held=!1,e.status=this.__effectiveI18n.uploading.status.connecting,r.upload.onloadstart=()=>{this.dispatchEvent(new CustomEvent("upload-start",{detail:{file:e,xhr:r}})),this._renderFileList()};let h={file:e,xhr:r,uploadFormat:this.uploadFormat,requestBody:d};a||(h.formData=d),this.dispatchEvent(new CustomEvent("upload-request",{detail:h,cancelable:!0}))&&r.send(d)}_retryFileUpload(e){this.dispatchEvent(new CustomEvent("upload-retry",{detail:{file:e,xhr:e.xhr},cancelable:!0}))&&(this._queueFileUpload(e),this._updateFocus(this.files.indexOf(e)))}_abortFileUpload(e){this.dispatchEvent(new CustomEvent("upload-abort",{detail:{file:e,xhr:e.xhr},cancelable:!0}))&&(e.abort=!0,e.xhr&&e.xhr.abort(),this._removeFile(e))}_renderFileList(){this._fileList&&typeof this._fileList.requestContentUpdate=="function"&&this._fileList.requestContentUpdate()}_addFiles(e){Array.prototype.forEach.call(e,this._addFile.bind(this))}_addFile(e){if(this.maxFilesReached){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.tooManyFiles}}));return}if(this.maxFileSize>=0&&e.size>this.maxFileSize){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.fileIsTooBig}}));return}let t=this.__acceptRegexp;if(t&&!(t.test(e.type)||t.test(e.name))){this.dispatchEvent(new CustomEvent("file-reject",{detail:{file:e,error:this.__effectiveI18n.error.incorrectFileType}}));return}e.loaded=0,e.held=!0,e.status=this.__effectiveI18n.uploading.status.held,this.files=[e,...this.files],this.noAuto||this._queueFileUpload(e)}_updateFocus(e){if(this.files.length===0){this._addButton.focus({focusVisible:B()});return}e===this.files.length&&(e-=1),this._fileList.children[e].firstElementChild.focus({focusVisible:B()})}_removeFile(e){this._uploadQueue=this._uploadQueue.filter(r=>r!==e),this._processUploadQueue();let t=this.files.indexOf(e);t>=0&&(this.files=this.files.filter(r=>r!==e),this.dispatchEvent(new CustomEvent("file-remove",{detail:{file:e},bubbles:!0,composed:!0})),this._updateFocus(t))}_onAddFilesTouchEnd(e){e.preventDefault(),this._onAddFilesClick(e)}_onAddFilesClick(e){this.maxFilesReached||(e.stopPropagation(),this.$.fileInput.value="",this.$.fileInput.click())}_onFileInputChange(e){this._addFiles(e.target.files)}_onFileStart(e){this._queueFileUpload(e.detail.file)}_onFileRetry(e){this._retryFileUpload(e.detail.file)}_onFileAbort(e){this._abortFileUpload(e.detail.file)}_onFileReject(e){ne(`${e.detail.file.name}: ${e.detail.error}`,{mode:"alert"})}_onUploadStart(e){ne(`${e.detail.file.name}: 0%`,{mode:"alert"})}_onUploadSuccess(e){ne(`${e.detail.file.name}: 100%`,{mode:"alert"})}_onUploadError(e){ne(`${e.detail.file.name}: ${e.detail.file.error}`,{mode:"alert"})}_dragoverChanged(e){e?this.setAttribute("dragover",e):this.removeAttribute("dragover")}_dragoverValidChanged(e){e?this.setAttribute("dragover-valid",e):this.removeAttribute("dragover-valid")}_i18nPlural(e,t){return e===1?t.one:t.many}_isMultiple(e){return e!==1}};var Os=class extends Na(A(v(g(b(_))))){static get is(){return"vaadin-upload"}static get styles(){return Ba}static get lumoInjector(){return{...super.lumoInjector,includeBaseStyles:!0}}render(){return u`
      <div part="primary-buttons">
        <slot name="add-button"></slot>
        <div part="drop-label" ?hidden="${this.nodrop}" id="dropLabelContainer" aria-hidden="true">
          <slot name="drop-label-icon"></slot>
          <slot name="drop-label"></slot>
        </div>
      </div>
      <slot name="file-list"></slot>
      <slot></slot>
      <input
        type="file"
        id="fileInput"
        hidden
        @change="${this._onFileInputChange}"
        accept="${this.accept}"
        ?multiple="${this._isMultiple(this.maxFiles)}"
        capture="${k(this.capture)}"
      />
    `}};m(Os);
/*! Bundled license information:

@lit/reactive-element/css-tag.js:
  (**
   * @license
   * Copyright 2019 Google LLC
   * SPDX-License-Identifier: BSD-3-Clause
   *)

@lit/reactive-element/reactive-element.js:
lit-html/lit-html.js:
lit-element/lit-element.js:
  (**
   * @license
   * Copyright 2017 Google LLC
   * SPDX-License-Identifier: BSD-3-Clause
   *)

lit-html/is-server.js:
  (**
   * @license
   * Copyright 2022 Google LLC
   * SPDX-License-Identifier: BSD-3-Clause
   *)

@vaadin/component-base/src/define.js:
@vaadin/component-base/src/dir-mixin.js:
@vaadin/component-base/src/element-mixin.js:
@vaadin/component-base/src/polylit-mixin.js:
@vaadin/component-base/src/dom-utils.js:
@vaadin/component-base/src/unique-id-utils.js:
@vaadin/component-base/src/slot-controller.js:
@vaadin/vaadin-themable-mixin/src/css-property-observer.js:
@vaadin/vaadin-themable-mixin/src/css-utils.js:
@vaadin/vaadin-themable-mixin/src/lumo-injector.js:
@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js:
@vaadin/a11y-base/src/disabled-mixin.js:
@vaadin/a11y-base/src/keyboard-mixin.js:
@vaadin/a11y-base/src/active-mixin.js:
@vaadin/a11y-base/src/focus-utils.js:
@vaadin/a11y-base/src/focus-mixin.js:
@vaadin/a11y-base/src/tabindex-mixin.js:
@vaadin/field-base/src/styles/checkable-base-styles.js:
@vaadin/field-base/src/styles/field-base-styles.js:
@vaadin/a11y-base/src/delegate-focus-mixin.js:
@vaadin/component-base/src/slot-styles-mixin.js:
@vaadin/component-base/src/delegate-state-mixin.js:
@vaadin/field-base/src/input-mixin.js:
@vaadin/field-base/src/checked-mixin.js:
@vaadin/a11y-base/src/field-aria-controller.js:
@vaadin/field-base/src/error-controller.js:
@vaadin/field-base/src/helper-controller.js:
@vaadin/field-base/src/label-controller.js:
@vaadin/field-base/src/label-mixin.js:
@vaadin/field-base/src/validate-mixin.js:
@vaadin/field-base/src/field-mixin.js:
@vaadin/field-base/src/input-controller.js:
@vaadin/field-base/src/labelled-input-controller.js:
@vaadin/input-container/src/styles/vaadin-input-container-base-styles.js:
@vaadin/input-container/src/vaadin-input-container.js:
@vaadin/component-base/src/browser-utils.js:
@vaadin/a11y-base/src/focus-restoration-controller.js:
@vaadin/a11y-base/src/focus-trap-controller.js:
@vaadin/component-base/src/virtualizer-iron-list-adapter.js:
@vaadin/field-base/src/clear-button-mixin.js:
@vaadin/field-base/src/input-constraints-mixin.js:
@vaadin/field-base/src/input-control-mixin.js:
@vaadin/field-base/src/pattern-mixin.js:
@vaadin/field-base/src/styles/button-base-styles.js:
@vaadin/field-base/src/styles/input-field-shared-styles.js:
@vaadin/component-base/src/data-provider-controller/cache.js:
@vaadin/component-base/src/data-provider-controller/helpers.js:
@vaadin/component-base/src/data-provider-controller/data-provider-controller.js:
@vaadin/component-base/src/media-query-controller.js:
@vaadin/field-base/src/virtual-keyboard-controller.js:
@vaadin/component-base/src/dir-utils.js:
@vaadin/component-base/src/overflow-controller.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-chip-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-chip.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-container.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-item.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-overlay-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-overlay.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-scroller-base-styles.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-scroller.js:
@vaadin/multi-select-combo-box/src/styles/vaadin-multi-select-combo-box-base-styles.js:
@vaadin/component-base/src/resize-mixin.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box-mixin.js:
@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js:
@vaadin/a11y-base/src/styles/sr-only-styles.js:
@vaadin/text-area/src/styles/vaadin-text-area-base-styles.js:
@vaadin/field-base/src/input-field-mixin.js:
@vaadin/field-base/src/text-area-controller.js:
@vaadin/text-area/src/vaadin-text-area-mixin.js:
@vaadin/text-area/src/vaadin-text-area.js:
@vaadin/text-field/src/vaadin-text-field-mixin.js:
  (**
   * @license
   * Copyright (c) 2021 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/vaadin-usage-statistics/vaadin-usage-statistics-collect.js:
  (*! vaadin-dev-mode:start
    (function () {
  'use strict';
  
  var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
    return typeof obj;
  } : function (obj) {
    return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
  };
  
  var classCallCheck = function (instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  };
  
  var createClass = function () {
    function defineProperties(target, props) {
      for (var i = 0; i < props.length; i++) {
        var descriptor = props[i];
        descriptor.enumerable = descriptor.enumerable || false;
        descriptor.configurable = true;
        if ("value" in descriptor) descriptor.writable = true;
        Object.defineProperty(target, descriptor.key, descriptor);
      }
    }
  
    return function (Constructor, protoProps, staticProps) {
      if (protoProps) defineProperties(Constructor.prototype, protoProps);
      if (staticProps) defineProperties(Constructor, staticProps);
      return Constructor;
    };
  }();
  
  var getPolymerVersion = function getPolymerVersion() {
    return window.Polymer && window.Polymer.version;
  };
  
  var StatisticsGatherer = function () {
    function StatisticsGatherer(logger) {
      classCallCheck(this, StatisticsGatherer);
  
      this.now = new Date().getTime();
      this.logger = logger;
    }
  
    createClass(StatisticsGatherer, [{
      key: 'frameworkVersionDetectors',
      value: function frameworkVersionDetectors() {
        return {
          'Flow': function Flow() {
            if (window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.clients) {
              var flowVersions = Object.keys(window.Vaadin.Flow.clients).map(function (key) {
                return window.Vaadin.Flow.clients[key];
              }).filter(function (client) {
                return client.getVersionInfo;
              }).map(function (client) {
                return client.getVersionInfo().flow;
              });
              if (flowVersions.length > 0) {
                return flowVersions[0];
              }
            }
          },
          'Vaadin Framework': function VaadinFramework() {
            if (window.vaadin && window.vaadin.clients) {
              var frameworkVersions = Object.values(window.vaadin.clients).filter(function (client) {
                return client.getVersionInfo;
              }).map(function (client) {
                return client.getVersionInfo().vaadinVersion;
              });
              if (frameworkVersions.length > 0) {
                return frameworkVersions[0];
              }
            }
          },
          'AngularJs': function AngularJs() {
            if (window.angular && window.angular.version && window.angular.version) {
              return window.angular.version.full;
            }
          },
          'Angular': function Angular() {
            if (window.ng) {
              var tags = document.querySelectorAll("[ng-version]");
              if (tags.length > 0) {
                return tags[0].getAttribute("ng-version");
              }
              return "Unknown";
            }
          },
          'Backbone.js': function BackboneJs() {
            if (window.Backbone) {
              return window.Backbone.VERSION;
            }
          },
          'React': function React() {
            var reactSelector = '[data-reactroot], [data-reactid]';
            if (!!document.querySelector(reactSelector)) {
              // React does not publish the version by default
              return "unknown";
            }
          },
          'Ember': function Ember() {
            if (window.Em && window.Em.VERSION) {
              return window.Em.VERSION;
            } else if (window.Ember && window.Ember.VERSION) {
              return window.Ember.VERSION;
            }
          },
          'jQuery': function (_jQuery) {
            function jQuery() {
              return _jQuery.apply(this, arguments);
            }
  
            jQuery.toString = function () {
              return _jQuery.toString();
            };
  
            return jQuery;
          }(function () {
            if (typeof jQuery === 'function' && jQuery.prototype.jquery !== undefined) {
              return jQuery.prototype.jquery;
            }
          }),
          'Polymer': function Polymer() {
            var version = getPolymerVersion();
            if (version) {
              return version;
            }
          },
          'LitElement': function LitElement() {
            var version = window.litElementVersions && window.litElementVersions[0];
            if (version) {
              return version;
            }
          },
          'LitHtml': function LitHtml() {
            var version = window.litHtmlVersions && window.litHtmlVersions[0];
            if (version) {
              return version;
            }
          },
          'Vue.js': function VueJs() {
            if (window.Vue) {
              return window.Vue.version;
            }
          }
        };
      }
    }, {
      key: 'getUsedVaadinElements',
      value: function getUsedVaadinElements(elements) {
        var version = getPolymerVersion();
        var elementClasses = void 0;
        // NOTE: In case you edit the code here, YOU MUST UPDATE any statistics reporting code in Flow.
        // Check all locations calling the method getEntries() in
        // https://github.com/vaadin/flow/blob/master/flow-server/src/main/java/com/vaadin/flow/internal/UsageStatistics.java#L106
        // Currently it is only used by BootstrapHandler.
        if (version && version.indexOf('2') === 0) {
          // Polymer 2: components classes are stored in window.Vaadin
          elementClasses = Object.keys(window.Vaadin).map(function (c) {
            return window.Vaadin[c];
          }).filter(function (c) {
            return c.is;
          });
        } else {
          // Polymer 3: components classes are stored in window.Vaadin.registrations
          elementClasses = window.Vaadin.registrations || [];
        }
        elementClasses.forEach(function (klass) {
          var version = klass.version ? klass.version : "0.0.0";
          elements[klass.is] = { version: version };
        });
      }
    }, {
      key: 'getUsedVaadinThemes',
      value: function getUsedVaadinThemes(themes) {
        ['Lumo', 'Material'].forEach(function (themeName) {
          var theme;
          var version = getPolymerVersion();
          if (version && version.indexOf('2') === 0) {
            // Polymer 2: themes are stored in window.Vaadin
            theme = window.Vaadin[themeName];
          } else {
            // Polymer 3: themes are stored in custom element registry
            theme = customElements.get('vaadin-' + themeName.toLowerCase() + '-styles');
          }
          if (theme && theme.version) {
            themes[themeName] = { version: theme.version };
          }
        });
      }
    }, {
      key: 'getFrameworks',
      value: function getFrameworks(frameworks) {
        var detectors = this.frameworkVersionDetectors();
        Object.keys(detectors).forEach(function (framework) {
          var detector = detectors[framework];
          try {
            var version = detector();
            if (version) {
              frameworks[framework] = { version: version };
            }
          } catch (e) {}
        });
      }
    }, {
      key: 'gather',
      value: function gather(storage) {
        var storedStats = storage.read();
        var gatheredStats = {};
        var types = ["elements", "frameworks", "themes"];
  
        types.forEach(function (type) {
          gatheredStats[type] = {};
          if (!storedStats[type]) {
            storedStats[type] = {};
          }
        });
  
        var previousStats = JSON.stringify(storedStats);
  
        this.getUsedVaadinElements(gatheredStats.elements);
        this.getFrameworks(gatheredStats.frameworks);
        this.getUsedVaadinThemes(gatheredStats.themes);
  
        var now = this.now;
        types.forEach(function (type) {
          var keys = Object.keys(gatheredStats[type]);
          keys.forEach(function (key) {
            if (!storedStats[type][key] || _typeof(storedStats[type][key]) != _typeof({})) {
              storedStats[type][key] = { firstUsed: now };
            }
            // Discards any previously logged version number
            storedStats[type][key].version = gatheredStats[type][key].version;
            storedStats[type][key].lastUsed = now;
          });
        });
  
        var newStats = JSON.stringify(storedStats);
        storage.write(newStats);
        if (newStats != previousStats && Object.keys(storedStats).length > 0) {
          this.logger.debug("New stats: " + newStats);
        }
      }
    }]);
    return StatisticsGatherer;
  }();
  
  var StatisticsStorage = function () {
    function StatisticsStorage(key) {
      classCallCheck(this, StatisticsStorage);
  
      this.key = key;
    }
  
    createClass(StatisticsStorage, [{
      key: 'read',
      value: function read() {
        var localStorageStatsString = localStorage.getItem(this.key);
        try {
          return JSON.parse(localStorageStatsString ? localStorageStatsString : '{}');
        } catch (e) {
          return {};
        }
      }
    }, {
      key: 'write',
      value: function write(data) {
        localStorage.setItem(this.key, data);
      }
    }, {
      key: 'clear',
      value: function clear() {
        localStorage.removeItem(this.key);
      }
    }, {
      key: 'isEmpty',
      value: function isEmpty() {
        var storedStats = this.read();
        var empty = true;
        Object.keys(storedStats).forEach(function (key) {
          if (Object.keys(storedStats[key]).length > 0) {
            empty = false;
          }
        });
  
        return empty;
      }
    }]);
    return StatisticsStorage;
  }();
  
  var StatisticsSender = function () {
    function StatisticsSender(url, logger) {
      classCallCheck(this, StatisticsSender);
  
      this.url = url;
      this.logger = logger;
    }
  
    createClass(StatisticsSender, [{
      key: 'send',
      value: function send(data, errorHandler) {
        var logger = this.logger;
  
        if (navigator.onLine === false) {
          logger.debug("Offline, can't send");
          errorHandler();
          return;
        }
        logger.debug("Sending data to " + this.url);
  
        var req = new XMLHttpRequest();
        req.withCredentials = true;
        req.addEventListener("load", function () {
          // Stats sent, nothing more to do
          logger.debug("Response: " + req.responseText);
        });
        req.addEventListener("error", function () {
          logger.debug("Send failed");
          errorHandler();
        });
        req.addEventListener("abort", function () {
          logger.debug("Send aborted");
          errorHandler();
        });
        req.open("POST", this.url);
        req.setRequestHeader("Content-Type", "application/json");
        req.send(data);
      }
    }]);
    return StatisticsSender;
  }();
  
  var StatisticsLogger = function () {
    function StatisticsLogger(id) {
      classCallCheck(this, StatisticsLogger);
  
      this.id = id;
    }
  
    createClass(StatisticsLogger, [{
      key: '_isDebug',
      value: function _isDebug() {
        return localStorage.getItem("vaadin." + this.id + ".debug");
      }
    }, {
      key: 'debug',
      value: function debug(msg) {
        if (this._isDebug()) {
          console.info(this.id + ": " + msg);
        }
      }
    }]);
    return StatisticsLogger;
  }();
  
  var UsageStatistics = function () {
    function UsageStatistics() {
      classCallCheck(this, UsageStatistics);
  
      this.now = new Date();
      this.timeNow = this.now.getTime();
      this.gatherDelay = 10; // Delay between loading this file and gathering stats
      this.initialDelay = 24 * 60 * 60;
  
      this.logger = new StatisticsLogger("statistics");
      this.storage = new StatisticsStorage("vaadin.statistics.basket");
      this.gatherer = new StatisticsGatherer(this.logger);
      this.sender = new StatisticsSender("https://tools.vaadin.com/usage-stats/submit", this.logger);
    }
  
    createClass(UsageStatistics, [{
      key: 'maybeGatherAndSend',
      value: function maybeGatherAndSend() {
        var _this = this;
  
        if (localStorage.getItem(UsageStatistics.optOutKey)) {
          return;
        }
        this.gatherer.gather(this.storage);
        setTimeout(function () {
          _this.maybeSend();
        }, this.gatherDelay * 1000);
      }
    }, {
      key: 'lottery',
      value: function lottery() {
        return true;
      }
    }, {
      key: 'currentMonth',
      value: function currentMonth() {
        return this.now.getYear() * 12 + this.now.getMonth();
      }
    }, {
      key: 'maybeSend',
      value: function maybeSend() {
        var firstUse = Number(localStorage.getItem(UsageStatistics.firstUseKey));
        var monthProcessed = Number(localStorage.getItem(UsageStatistics.monthProcessedKey));
  
        if (!firstUse) {
          // Use a grace period to avoid interfering with tests, incognito mode etc
          firstUse = this.timeNow;
          localStorage.setItem(UsageStatistics.firstUseKey, firstUse);
        }
  
        if (this.timeNow < firstUse + this.initialDelay * 1000) {
          this.logger.debug("No statistics will be sent until the initial delay of " + this.initialDelay + "s has passed");
          return;
        }
        if (this.currentMonth() <= monthProcessed) {
          this.logger.debug("This month has already been processed");
          return;
        }
        localStorage.setItem(UsageStatistics.monthProcessedKey, this.currentMonth());
        // Use random sampling
        if (this.lottery()) {
          this.logger.debug("Congratulations, we have a winner!");
        } else {
          this.logger.debug("Sorry, no stats from you this time");
          return;
        }
  
        this.send();
      }
    }, {
      key: 'send',
      value: function send() {
        // Ensure we have the latest data
        this.gatherer.gather(this.storage);
  
        // Read, send and clean up
        var data = this.storage.read();
        data["firstUse"] = Number(localStorage.getItem(UsageStatistics.firstUseKey));
        data["usageStatisticsVersion"] = UsageStatistics.version;
        var info = 'This request contains usage statistics gathered from the application running in development mode. \n\nStatistics gathering is automatically disabled and excluded from production builds.\n\nFor details and to opt-out, see https://github.com/vaadin/vaadin-usage-statistics.\n\n\n\n';
        var self = this;
        this.sender.send(info + JSON.stringify(data), function () {
          // Revert the 'month processed' flag
          localStorage.setItem(UsageStatistics.monthProcessedKey, self.currentMonth() - 1);
        });
      }
    }], [{
      key: 'version',
      get: function get$1() {
        return '2.1.2';
      }
    }, {
      key: 'firstUseKey',
      get: function get$1() {
        return 'vaadin.statistics.firstuse';
      }
    }, {
      key: 'monthProcessedKey',
      get: function get$1() {
        return 'vaadin.statistics.monthProcessed';
      }
    }, {
      key: 'optOutKey',
      get: function get$1() {
        return 'vaadin.statistics.optout';
      }
    }]);
    return UsageStatistics;
  }();
  
  try {
    window.Vaadin = window.Vaadin || {};
    window.Vaadin.usageStatsChecker = window.Vaadin.usageStatsChecker || new UsageStatistics();
    window.Vaadin.usageStatsChecker.maybeGatherAndSend();
  } catch (e) {
    // Intentionally ignored as this is not a problem in the app being developed
  }
  
  }());
  
    vaadin-dev-mode:end **)

@vaadin/component-base/src/async.js:
  (**
   * @license
   * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
   * This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
   * The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
   * The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
   * Code distributed by Google as part of the polymer project is also
   * subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
   *)

@vaadin/component-base/src/debounce.js:
@vaadin/component-base/src/gestures.js:
  (**
  @license
  Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
  This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
  The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
  The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
  Code distributed by Google as part of the polymer project is also
  subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
  *)

@vaadin/component-base/src/path-utils.js:
@vaadin/component-base/src/slot-observer.js:
@vaadin/a11y-base/src/aria-id-reference.js:
@vaadin/select/src/button-controller.js:
  (**
   * @license
   * Copyright (c) 2023 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/component-base/src/tooltip-controller.js:
@vaadin/a11y-base/src/announce.js:
@vaadin/component-base/src/slot-child-observe-controller.js:
@vaadin/a11y-base/src/keyboard-direction-mixin.js:
  (**
   * @license
   * Copyright (c) 2022 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/component-base/src/css-utils.js:
@vaadin/component-base/src/styles/loader-styles.js:
@vaadin/component-base/src/i18n-mixin.js:
  (**
   * @license
   * Copyright (c) 2025 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/component-base/src/warnings.js:
@vaadin/vaadin-themable-mixin/src/lumo-modules.js:
@vaadin/component-base/src/virtualizer.js:
@vaadin/grid/src/array-data-provider.js:
@vaadin/upload/src/vaadin-upload-manager.js:
@vaadin/upload/src/vaadin-upload-helpers.js:
  (**
   * @license
   * Copyright (c) 2000 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/vaadin-themable-mixin/vaadin-theme-property-mixin.js:
@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js:
@vaadin/component-base/src/styles/style-props.js:
@vaadin/button/src/styles/vaadin-button-base-styles.js:
@vaadin/button/src/vaadin-button-mixin.js:
@vaadin/button/src/vaadin-button.js:
@vaadin/checkbox/src/styles/vaadin-checkbox-base-styles.js:
@vaadin/checkbox/src/vaadin-checkbox-mixin.js:
@vaadin/checkbox/src/vaadin-checkbox.js:
@vaadin/item/src/styles/vaadin-item-base-styles.js:
@vaadin/overlay/src/styles/vaadin-overlay-base-styles.js:
@vaadin/overlay/src/vaadin-overlay-focus-mixin.js:
@vaadin/overlay/src/vaadin-overlay-stack-mixin.js:
@vaadin/overlay/src/vaadin-overlay-mixin.js:
@vaadin/overlay/src/vaadin-overlay-position-mixin.js:
@vaadin/dialog/src/styles/vaadin-dialog-overlay-base-styles.js:
@vaadin/dialog/src/vaadin-dialog-overflow-controller.js:
@vaadin/dialog/src/vaadin-dialog-overlay-mixin.js:
@vaadin/dialog/src/vaadin-dialog-overlay.js:
@vaadin/dialog/src/vaadin-dialog-base-mixin.js:
@vaadin/dialog/src/vaadin-dialog-utils.js:
@vaadin/dialog/src/vaadin-dialog-draggable-mixin.js:
@vaadin/dialog/src/vaadin-dialog-renderer-mixin.js:
@vaadin/dialog/src/vaadin-dialog-resizable-mixin.js:
@vaadin/dialog/src/vaadin-dialog-size-mixin.js:
@vaadin/dialog/src/vaadin-dialog.js:
@vaadin/item/src/vaadin-item-mixin.js:
@vaadin/select/src/vaadin-select-item.js:
@vaadin/a11y-base/src/list-mixin.js:
@vaadin/list-box/src/styles/vaadin-list-box-base-styles.js:
@vaadin/select/src/vaadin-select-list-box.js:
@vaadin/select/src/styles/vaadin-select-overlay-base-styles.js:
@vaadin/select/src/vaadin-select-overlay-mixin.js:
@vaadin/select/src/vaadin-select-overlay.js:
@vaadin/select/src/styles/vaadin-select-value-button-base-styles.js:
@vaadin/select/src/vaadin-select-value-button.js:
@vaadin/select/src/styles/vaadin-select-base-styles.js:
@vaadin/select/src/vaadin-select-base-mixin.js:
@vaadin/select/src/vaadin-select.js:
@vaadin/tabs/src/styles/vaadin-tab-base-styles.js:
@vaadin/tabs/src/vaadin-tab.js:
@vaadin/tabs/src/styles/vaadin-tabs-base-styles.js:
@vaadin/tabs/src/vaadin-tabs-mixin.js:
@vaadin/tabs/src/vaadin-tabs.js:
@vaadin/text-field/src/vaadin-text-field.js:
@vaadin/progress-bar/src/styles/vaadin-progress-bar-base-styles.js:
@vaadin/progress-bar/src/vaadin-progress-mixin.js:
@vaadin/progress-bar/src/vaadin-progress-bar.js:
  (**
   * @license
   * Copyright (c) 2017 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/combo-box/src/styles/vaadin-combo-box-item-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-item-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-item.js:
@vaadin/combo-box/src/styles/vaadin-combo-box-overlay-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-overlay-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-overlay.js:
@vaadin/combo-box/src/styles/vaadin-combo-box-scroller-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-placeholder.js:
@vaadin/combo-box/src/vaadin-combo-box-scroller-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-scroller.js:
@vaadin/combo-box/src/styles/vaadin-combo-box-base-styles.js:
@vaadin/combo-box/src/vaadin-combo-box-data-provider-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-focus-index-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-base-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-items-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box-mixin.js:
@vaadin/combo-box/src/vaadin-combo-box.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay-mixin.js:
  (**
   * @license
   * Copyright (c) 2015 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/overlay/src/vaadin-overlay-utils.js:
  (**
   * @license
   * Copyright (c) 2024 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/component-base/src/iron-list-core.js:
  (**
   * @license
   * Copyright (c) 2016 The Polymer Project Authors. All rights reserved.
   * This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
   * The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
   * The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
   * Code distributed by Google as part of the polymer project is also
   * subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
   *)

lit-html/directives/if-defined.js:
  (**
   * @license
   * Copyright 2018 Google LLC
   * SPDX-License-Identifier: BSD-3-Clause
   *)

@vaadin/date-picker/src/styles/vaadin-date-picker-overlay-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay.js:
@vaadin/date-picker/src/vaadin-date-picker-helper.js:
@vaadin/date-picker/src/vaadin-infinite-scroller.js:
@vaadin/date-picker/src/vaadin-date-picker-month-scroller.js:
@vaadin/date-picker/src/vaadin-date-picker-year-scroller.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-year-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-year.js:
@vaadin/date-picker/src/styles/vaadin-month-calendar-base-styles.js:
@vaadin/date-picker/src/vaadin-month-calendar-mixin.js:
@vaadin/date-picker/src/vaadin-month-calendar.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-overlay-content-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay-content-mixin.js:
@vaadin/date-picker/src/vaadin-date-picker-overlay-content.js:
@vaadin/date-picker/src/styles/vaadin-date-picker-base-styles.js:
@vaadin/date-picker/src/vaadin-date-picker-mixin.js:
@vaadin/date-picker/src/vaadin-date-picker.js:
@vaadin/grid/src/vaadin-grid-helpers.js:
@vaadin/grid/src/vaadin-grid-column-mixin.js:
@vaadin/grid/src/vaadin-grid-column.js:
@vaadin/grid/src/styles/vaadin-grid-base-styles.js:
@vaadin/grid/src/vaadin-grid-a11y-mixin.js:
@vaadin/grid/src/vaadin-grid-active-item-mixin.js:
@vaadin/grid/src/vaadin-grid-array-data-provider-mixin.js:
@vaadin/grid/src/vaadin-grid-column-auto-width-mixin.js:
@vaadin/grid/src/vaadin-grid-column-reordering-mixin.js:
@vaadin/grid/src/vaadin-grid-column-resizing-mixin.js:
@vaadin/grid/src/vaadin-grid-data-provider-mixin.js:
@vaadin/grid/src/vaadin-grid-drag-and-drop-mixin.js:
@vaadin/grid/src/vaadin-grid-dynamic-columns-mixin.js:
@vaadin/grid/src/vaadin-grid-event-context-mixin.js:
@vaadin/grid/src/vaadin-grid-filter-mixin.js:
@vaadin/grid/src/vaadin-grid-keyboard-navigation-mixin.js:
@vaadin/grid/src/vaadin-grid-resize-mixin.js:
@vaadin/grid/src/vaadin-grid-row-details-mixin.js:
@vaadin/grid/src/vaadin-grid-scroll-mixin.js:
@vaadin/grid/src/vaadin-grid-selection-mixin.js:
@vaadin/grid/src/vaadin-grid-sort-mixin.js:
@vaadin/grid/src/vaadin-grid-styling-mixin.js:
@vaadin/grid/src/vaadin-grid-mixin.js:
@vaadin/grid/src/vaadin-grid.js:
@vaadin/upload/src/styles/vaadin-upload-icon-base-styles.js:
@vaadin/upload/src/vaadin-upload-icon.js:
@vaadin/upload/src/vaadin-upload-icons.js:
@vaadin/upload/src/styles/vaadin-upload-file-base-styles.js:
@vaadin/upload/src/vaadin-upload-file-mixin.js:
@vaadin/upload/src/vaadin-upload-file.js:
@vaadin/upload/src/styles/vaadin-upload-file-list-base-styles.js:
@vaadin/upload/src/vaadin-upload-file-list-mixin.js:
@vaadin/upload/src/vaadin-upload-file-list.js:
@vaadin/upload/src/styles/vaadin-upload-base-styles.js:
@vaadin/upload/src/vaadin-upload-mixin.js:
@vaadin/upload/src/vaadin-upload.js:
  (**
   * @license
   * Copyright (c) 2016 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/a11y-base/src/aria-hidden.js:
  (**
   * @license
   * Copyright (c) 2017 Anton Korzunov
   * SPDX-License-Identifier: MIT
   *)

@vaadin/time-picker/src/vaadin-time-picker-item.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-overlay-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-overlay.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-scroller-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-scroller.js:
@vaadin/time-picker/src/styles/vaadin-time-picker-base-styles.js:
@vaadin/time-picker/src/vaadin-time-picker-helper.js:
@vaadin/time-picker/src/vaadin-time-picker-mixin.js:
@vaadin/time-picker/src/vaadin-time-picker.js:
  (**
   * @license
   * Copyright (c) 2018 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)

@vaadin/date-time-picker/src/styles/vaadin-date-time-picker-base-styles.js:
@vaadin/date-time-picker/src/vaadin-date-time-picker-mixin.js:
@vaadin/date-time-picker/src/vaadin-date-time-picker.js:
  (**
   * @license
   * Copyright (c) 2019 - 2026 Vaadin Ltd.
   * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
   *)
*/
