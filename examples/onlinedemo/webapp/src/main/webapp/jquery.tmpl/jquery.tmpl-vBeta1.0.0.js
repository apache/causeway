


<!DOCTYPE html>
<html>
  <head>
    <meta charset='utf-8'>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
        <title>jquery.tmpl.js at master from jquery/jquery-tmpl - GitHub</title>
    <link rel="search" type="application/opensearchdescription+xml" href="/opensearch.xml" title="GitHub" />
    <link rel="fluid-icon" href="https://github.com/fluidicon.png" title="GitHub" />

    
    

    <meta content="authenticity_token" name="csrf-param" />
<meta content="QcSoMAbLBOEGQzq5iMFhEa5wh9zuaQqLbwvmSg5xFFw=" name="csrf-token" />

    <link href="https://a248.e.akamai.net/assets.github.com/stylesheets/bundles/github-182944099c59274f5fb79ea97c47f923a5864f79.css" media="screen" rel="stylesheet" type="text/css" />
    

    <script src="https://a248.e.akamai.net/assets.github.com/javascripts/bundles/jquery-2bdf48207f435863de9c5786265d27d992c7f6c0.js" type="text/javascript"></script>
    <script src="https://a248.e.akamai.net/assets.github.com/javascripts/bundles/github-68d680d89cf702cc67c2ebb0b261548622d05ce3.js" type="text/javascript"></script>
    

      <link rel='permalink' href='/jquery/jquery-tmpl/blob/04b5af07a579b0928d93cd018cda056097e58180/jquery.tmpl.js'>

    <meta name="description" content="jquery-tmpl - A templating plugin for jQuery. BETA. NO LONGER IN ACTIVE DEVELOPMENT OR MAINTENANCE. Issues remain open but are not being worked." />
  <link href="https://github.com/jquery/jquery-tmpl/commits/master.atom" rel="alternate" title="Recent Commits to jquery-tmpl:master" type="application/atom+xml" />

  </head>


  <body class="logged_out page-blob windows  env-production ">
    


    

      <div id="header" class="true clearfix">
        <div class="container clearfix">
          <a class="site-logo" href="https://github.com">
            <!--[if IE]>
            <img alt="GitHub" class="github-logo" src="https://a248.e.akamai.net/assets.github.com/images/modules/header/logov7.png?1323882717" />
            <img alt="GitHub" class="github-logo-hover" src="https://a248.e.akamai.net/assets.github.com/images/modules/header/logov7-hover.png?1324325376" />
            <![endif]-->
            <img alt="GitHub" class="github-logo-4x" height="30" src="https://a248.e.akamai.net/assets.github.com/images/modules/header/logov7@4x.png?1323882717" />
            <img alt="GitHub" class="github-logo-4x-hover" height="30" src="https://a248.e.akamai.net/assets.github.com/images/modules/header/logov7@4x-hover.png?1324325376" />
          </a>

                  <!--
      make sure to use fully qualified URLs here since this nav
      is used on error pages on other domains
    -->
    <ul class="top-nav logged_out">
        <li class="pricing"><a href="https://github.com/plans">Signup and Pricing</a></li>
        <li class="explore"><a href="https://github.com/explore">Explore GitHub</a></li>
      <li class="features"><a href="https://github.com/features">Features</a></li>
        <li class="blog"><a href="https://github.com/blog">Blog</a></li>
      <li class="login"><a href="https://github.com/login?return_to=%2Fjquery%2Fjquery-tmpl%2Fblob%2Fmaster%2Fjquery.tmpl.js">Login</a></li>
    </ul>



          
        </div>
      </div>

      

            <div class="site">
      <div class="container">
        <div class="pagehead repohead instapaper_ignore readability-menu">


        <div class="title-actions-bar">
          <h1>
            <a href="/jquery">jquery</a> /
            <strong><a href="/jquery/jquery-tmpl" class="js-current-repository">jquery-tmpl</a></strong>
          </h1>
          



              <ul class="pagehead-actions">


          <li><a href="/login?return_to=%2Fjquery%2Fjquery-tmpl" class="minibutton btn-watch watch-button entice tooltipped leftwards" rel="nofollow" title="You must be logged in to use this feature"><span><span class="icon"></span>Watch</span></a></li>
          <li><a href="/login?return_to=%2Fjquery%2Fjquery-tmpl" class="minibutton btn-fork fork-button entice tooltipped leftwards" rel="nofollow" title="You must be logged in to use this feature"><span><span class="icon"></span>Fork</span></a></li>


      <li class="repostats">
        <ul class="repo-stats">
          <li class="watchers ">
            <a href="/jquery/jquery-tmpl/watchers" title="Watchers" class="tooltipped downwards">
              1,777
            </a>
          </li>
          <li class="forks">
            <a href="/jquery/jquery-tmpl/network" title="Forks" class="tooltipped downwards">
              181
            </a>
          </li>
        </ul>
      </li>
    </ul>

        </div>

          

  <ul class="tabs">
    <li><a href="/jquery/jquery-tmpl" class="selected" highlight="repo_sourcerepo_downloadsrepo_commitsrepo_tagsrepo_branches">Code</a></li>
    <li><a href="/jquery/jquery-tmpl/network" highlight="repo_networkrepo_fork_queue">Network</a>
    <li><a href="/jquery/jquery-tmpl/pulls" highlight="repo_pulls">Pull Requests <span class='counter'>0</span></a></li>

      <li><a href="/jquery/jquery-tmpl/issues" highlight="repo_issues">Issues <span class='counter'>54</span></a></li>


    <li><a href="/jquery/jquery-tmpl/graphs" highlight="repo_graphsrepo_contributors">Stats &amp; Graphs</a></li>

  </ul>

  
<div class="frame frame-center tree-finder" style="display:none"
      data-tree-list-url="/jquery/jquery-tmpl/tree-list/04b5af07a579b0928d93cd018cda056097e58180"
      data-blob-url-prefix="/jquery/jquery-tmpl/blob/04b5af07a579b0928d93cd018cda056097e58180"
    >

  <div class="breadcrumb">
    <b><a href="/jquery/jquery-tmpl">jquery-tmpl</a></b> /
    <input class="tree-finder-input js-navigation-enable" type="text" name="query" autocomplete="off" spellcheck="false">
  </div>

    <div class="octotip">
      <p>
        <a href="/jquery/jquery-tmpl/dismiss-tree-finder-help" class="dismiss js-dismiss-tree-list-help" title="Hide this notice forever" rel="nofollow">Dismiss</a>
        <strong>Octotip:</strong> You've activated the <em>file finder</em>
        by pressing <span class="kbd">t</span> Start typing to filter the
        file list. Use <span class="kbd badmono">↑</span> and
        <span class="kbd badmono">↓</span> to navigate,
        <span class="kbd">enter</span> to view files.
      </p>
    </div>

  <table class="tree-browser" cellpadding="0" cellspacing="0">
    <tr class="js-header"><th>&nbsp;</th><th>name</th></tr>
    <tr class="js-no-results no-results" style="display: none">
      <th colspan="2">No matching files</th>
    </tr>
    <tbody class="js-results-list js-navigation-container" data-navigation-enable-mouse>
    </tbody>
  </table>
</div>

<div id="jump-to-line" style="display:none">
  <h2>Jump to Line</h2>
  <form>
    <input class="textfield" type="text">
    <div class="full-button">
      <button type="submit" class="classy">
        <span>Go</span>
      </button>
    </div>
  </form>
</div>


<div class="subnav-bar">

  <ul class="actions">
    
      <li class="switcher">

        <div class="context-menu-container js-menu-container">
          <span class="text">Current branch:</span>
          <a href="#"
             class="minibutton bigger switcher context-menu-button js-menu-target js-commitish-button btn-branch repo-tree"
             data-master-branch="master"
             data-ref="master">
            <span><span class="icon"></span>master</span>
          </a>

          <div class="context-pane commitish-context js-menu-content">
            <a href="javascript:;" class="close js-menu-close"></a>
            <div class="context-title">Switch Branches/Tags</div>
            <div class="context-body pane-selector commitish-selector js-filterable-commitishes">
              <div class="filterbar">
                <div class="placeholder-field js-placeholder-field">
                  <label class="placeholder" for="context-commitish-filter-field" data-placeholder-mode="sticky">Filter branches/tags</label>
                  <input type="text" id="context-commitish-filter-field" class="commitish-filter" />
                </div>

                <ul class="tabs">
                  <li><a href="#" data-filter="branches" class="selected">Branches</a></li>
                  <li><a href="#" data-filter="tags">Tags</a></li>
                </ul>
              </div>

                <div class="commitish-item branch-commitish selector-item">
                  <h4>
                      <a href="/jquery/jquery-tmpl/blob/gh-pages/jquery.tmpl.js" data-name="gh-pages" rel="nofollow">gh-pages</a>
                  </h4>
                </div>
                <div class="commitish-item branch-commitish selector-item">
                  <h4>
                      <a href="/jquery/jquery-tmpl/blob/master/jquery.tmpl.js" data-name="master" rel="nofollow">master</a>
                  </h4>
                </div>

                <div class="commitish-item tag-commitish selector-item">
                  <h4>
                      <a href="/jquery/jquery-tmpl/blob/vBeta1.0.0/jquery.tmpl.js" data-name="vBeta1.0.0" rel="nofollow">vBeta1.0.0</a>
                  </h4>
                </div>

              <div class="no-results" style="display:none">Nothing to show</div>
            </div>
          </div><!-- /.commitish-context-context -->
        </div>

      </li>
  </ul>

  <ul class="subnav">
    <li><a href="/jquery/jquery-tmpl" class="selected" highlight="repo_source">Files</a></li>
    <li><a href="/jquery/jquery-tmpl/commits/master" highlight="repo_commits">Commits</a></li>
    <li><a href="/jquery/jquery-tmpl/branches" class="" highlight="repo_branches" rel="nofollow">Branches <span class="counter">2</span></a></li>
    <li><a href="/jquery/jquery-tmpl/tags" class="" highlight="repo_tags">Tags <span class="counter">1</span></a></li>
    <li><a href="/jquery/jquery-tmpl/downloads" class="blank" highlight="repo_downloads">Downloads <span class="counter">0</span></a></li>
  </ul>

</div>

  
  
  


          

        </div><!-- /.repohead -->

        




  
  <p class="last-commit">Latest commit to the <strong>master</strong> branch</p>

<div class="commit commit-tease js-details-container">
  <p class="commit-title ">
      <a href="/jquery/jquery-tmpl/commit/04b5af07a579b0928d93cd018cda056097e58180" class="message">README: Added link to jsRender/jsView blog post by Boris Moore</a>
      
  </p>
  <div class="commit-meta">
    <a href="/jquery/jquery-tmpl/commit/04b5af07a579b0928d93cd018cda056097e58180" class="sha-block">commit <span class="sha">04b5af07a5</span></a>

    <div class="authorship">
      <img class="gravatar" height="20" src="https://secure.gravatar.com/avatar/d92ea7772f465256ad836de1ce642b37?s=140&amp;d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png" width="20" />
      <span class="author-name"><a href="/rdworth">rdworth</a></span>
      authored <time class="js-relative-date" datetime="2011-10-27T03:28:17-07:00" title="2011-10-27 03:28:17">October 27, 2011</time>

    </div>
  </div>
</div>


<!-- block_view_fragment_key: views4/v8/blob:v15:547820:jquery/jquery-tmpl:7e850f9d061d1e833dd90c1b47d4fbb12483ec0f:8d66f46ab5b937c06a116ecd2c2d0d4c -->
  <div id="slider">

    <div class="breadcrumb" data-path="jquery.tmpl.js/">
      <b><a href="/jquery/jquery-tmpl/tree/04b5af07a579b0928d93cd018cda056097e58180" class="js-rewrite-sha">jquery-tmpl</a></b> / jquery.tmpl.js       <span style="display:none" id="clippy_506" class="clippy-text">jquery.tmpl.js</span>
      
      <object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000"
              width="110"
              height="14"
              class="clippy"
              id="clippy" >
      <param name="movie" value="https://a248.e.akamai.net/assets.github.com/flash/clippy.swf?1284681402?v5"/>
      <param name="allowScriptAccess" value="always" />
      <param name="quality" value="high" />
      <param name="scale" value="noscale" />
      <param NAME="FlashVars" value="id=clippy_506&amp;copied=copied!&amp;copyto=copy to clipboard">
      <param name="bgcolor" value="#FFFFFF">
      <param name="wmode" value="opaque">
      <embed src="https://a248.e.akamai.net/assets.github.com/flash/clippy.swf?1284681402?v5"
             width="110"
             height="14"
             name="clippy"
             quality="high"
             allowScriptAccess="always"
             type="application/x-shockwave-flash"
             pluginspage="http://www.macromedia.com/go/getflashplayer"
             FlashVars="id=clippy_506&amp;copied=copied!&amp;copyto=copy to clipboard"
             bgcolor="#FFFFFF"
             wmode="opaque"
      />
      </object>
      

    </div>

    <div class="frames">
      <div class="frame frame-center" data-path="jquery.tmpl.js/" data-permalink-url="/jquery/jquery-tmpl/blob/04b5af07a579b0928d93cd018cda056097e58180/jquery.tmpl.js" data-title="jquery.tmpl.js at master from jquery/jquery-tmpl - GitHub" data-type="blob">
          <ul class="big-actions">
            <li><a class="file-edit-link minibutton js-rewrite-sha" href="/jquery/jquery-tmpl/edit/04b5af07a579b0928d93cd018cda056097e58180/jquery.tmpl.js" data-method="post" rel="nofollow"><span>Edit this file</span></a></li>
          </ul>

        <div id="files">
          <div class="file">
            <div class="meta">
              <div class="info">
                <span class="icon"><img alt="Txt" height="16" src="https://a248.e.akamai.net/assets.github.com/images/icons/txt.png?1284681402" width="16" /></span>
                <span class="mode" title="File Mode">100644</span>
                  <span>485 lines (450 sloc)</span>
                <span>19.087 kb</span>
              </div>
              <ul class="actions">
                <li><a href="/jquery/jquery-tmpl/raw/master/jquery.tmpl.js" id="raw-url">raw</a></li>
                  <li><a href="/jquery/jquery-tmpl/blame/master/jquery.tmpl.js">blame</a></li>
                <li><a href="/jquery/jquery-tmpl/commits/master/jquery.tmpl.js" rel="nofollow">history</a></li>
              </ul>
            </div>
              <div class="data type-javascript">
      <table cellpadding="0" cellspacing="0" class="lines">
        <tr>
          <td>
            <pre class="line_numbers"><span id="L1" rel="#L1">1</span>
<span id="L2" rel="#L2">2</span>
<span id="L3" rel="#L3">3</span>
<span id="L4" rel="#L4">4</span>
<span id="L5" rel="#L5">5</span>
<span id="L6" rel="#L6">6</span>
<span id="L7" rel="#L7">7</span>
<span id="L8" rel="#L8">8</span>
<span id="L9" rel="#L9">9</span>
<span id="L10" rel="#L10">10</span>
<span id="L11" rel="#L11">11</span>
<span id="L12" rel="#L12">12</span>
<span id="L13" rel="#L13">13</span>
<span id="L14" rel="#L14">14</span>
<span id="L15" rel="#L15">15</span>
<span id="L16" rel="#L16">16</span>
<span id="L17" rel="#L17">17</span>
<span id="L18" rel="#L18">18</span>
<span id="L19" rel="#L19">19</span>
<span id="L20" rel="#L20">20</span>
<span id="L21" rel="#L21">21</span>
<span id="L22" rel="#L22">22</span>
<span id="L23" rel="#L23">23</span>
<span id="L24" rel="#L24">24</span>
<span id="L25" rel="#L25">25</span>
<span id="L26" rel="#L26">26</span>
<span id="L27" rel="#L27">27</span>
<span id="L28" rel="#L28">28</span>
<span id="L29" rel="#L29">29</span>
<span id="L30" rel="#L30">30</span>
<span id="L31" rel="#L31">31</span>
<span id="L32" rel="#L32">32</span>
<span id="L33" rel="#L33">33</span>
<span id="L34" rel="#L34">34</span>
<span id="L35" rel="#L35">35</span>
<span id="L36" rel="#L36">36</span>
<span id="L37" rel="#L37">37</span>
<span id="L38" rel="#L38">38</span>
<span id="L39" rel="#L39">39</span>
<span id="L40" rel="#L40">40</span>
<span id="L41" rel="#L41">41</span>
<span id="L42" rel="#L42">42</span>
<span id="L43" rel="#L43">43</span>
<span id="L44" rel="#L44">44</span>
<span id="L45" rel="#L45">45</span>
<span id="L46" rel="#L46">46</span>
<span id="L47" rel="#L47">47</span>
<span id="L48" rel="#L48">48</span>
<span id="L49" rel="#L49">49</span>
<span id="L50" rel="#L50">50</span>
<span id="L51" rel="#L51">51</span>
<span id="L52" rel="#L52">52</span>
<span id="L53" rel="#L53">53</span>
<span id="L54" rel="#L54">54</span>
<span id="L55" rel="#L55">55</span>
<span id="L56" rel="#L56">56</span>
<span id="L57" rel="#L57">57</span>
<span id="L58" rel="#L58">58</span>
<span id="L59" rel="#L59">59</span>
<span id="L60" rel="#L60">60</span>
<span id="L61" rel="#L61">61</span>
<span id="L62" rel="#L62">62</span>
<span id="L63" rel="#L63">63</span>
<span id="L64" rel="#L64">64</span>
<span id="L65" rel="#L65">65</span>
<span id="L66" rel="#L66">66</span>
<span id="L67" rel="#L67">67</span>
<span id="L68" rel="#L68">68</span>
<span id="L69" rel="#L69">69</span>
<span id="L70" rel="#L70">70</span>
<span id="L71" rel="#L71">71</span>
<span id="L72" rel="#L72">72</span>
<span id="L73" rel="#L73">73</span>
<span id="L74" rel="#L74">74</span>
<span id="L75" rel="#L75">75</span>
<span id="L76" rel="#L76">76</span>
<span id="L77" rel="#L77">77</span>
<span id="L78" rel="#L78">78</span>
<span id="L79" rel="#L79">79</span>
<span id="L80" rel="#L80">80</span>
<span id="L81" rel="#L81">81</span>
<span id="L82" rel="#L82">82</span>
<span id="L83" rel="#L83">83</span>
<span id="L84" rel="#L84">84</span>
<span id="L85" rel="#L85">85</span>
<span id="L86" rel="#L86">86</span>
<span id="L87" rel="#L87">87</span>
<span id="L88" rel="#L88">88</span>
<span id="L89" rel="#L89">89</span>
<span id="L90" rel="#L90">90</span>
<span id="L91" rel="#L91">91</span>
<span id="L92" rel="#L92">92</span>
<span id="L93" rel="#L93">93</span>
<span id="L94" rel="#L94">94</span>
<span id="L95" rel="#L95">95</span>
<span id="L96" rel="#L96">96</span>
<span id="L97" rel="#L97">97</span>
<span id="L98" rel="#L98">98</span>
<span id="L99" rel="#L99">99</span>
<span id="L100" rel="#L100">100</span>
<span id="L101" rel="#L101">101</span>
<span id="L102" rel="#L102">102</span>
<span id="L103" rel="#L103">103</span>
<span id="L104" rel="#L104">104</span>
<span id="L105" rel="#L105">105</span>
<span id="L106" rel="#L106">106</span>
<span id="L107" rel="#L107">107</span>
<span id="L108" rel="#L108">108</span>
<span id="L109" rel="#L109">109</span>
<span id="L110" rel="#L110">110</span>
<span id="L111" rel="#L111">111</span>
<span id="L112" rel="#L112">112</span>
<span id="L113" rel="#L113">113</span>
<span id="L114" rel="#L114">114</span>
<span id="L115" rel="#L115">115</span>
<span id="L116" rel="#L116">116</span>
<span id="L117" rel="#L117">117</span>
<span id="L118" rel="#L118">118</span>
<span id="L119" rel="#L119">119</span>
<span id="L120" rel="#L120">120</span>
<span id="L121" rel="#L121">121</span>
<span id="L122" rel="#L122">122</span>
<span id="L123" rel="#L123">123</span>
<span id="L124" rel="#L124">124</span>
<span id="L125" rel="#L125">125</span>
<span id="L126" rel="#L126">126</span>
<span id="L127" rel="#L127">127</span>
<span id="L128" rel="#L128">128</span>
<span id="L129" rel="#L129">129</span>
<span id="L130" rel="#L130">130</span>
<span id="L131" rel="#L131">131</span>
<span id="L132" rel="#L132">132</span>
<span id="L133" rel="#L133">133</span>
<span id="L134" rel="#L134">134</span>
<span id="L135" rel="#L135">135</span>
<span id="L136" rel="#L136">136</span>
<span id="L137" rel="#L137">137</span>
<span id="L138" rel="#L138">138</span>
<span id="L139" rel="#L139">139</span>
<span id="L140" rel="#L140">140</span>
<span id="L141" rel="#L141">141</span>
<span id="L142" rel="#L142">142</span>
<span id="L143" rel="#L143">143</span>
<span id="L144" rel="#L144">144</span>
<span id="L145" rel="#L145">145</span>
<span id="L146" rel="#L146">146</span>
<span id="L147" rel="#L147">147</span>
<span id="L148" rel="#L148">148</span>
<span id="L149" rel="#L149">149</span>
<span id="L150" rel="#L150">150</span>
<span id="L151" rel="#L151">151</span>
<span id="L152" rel="#L152">152</span>
<span id="L153" rel="#L153">153</span>
<span id="L154" rel="#L154">154</span>
<span id="L155" rel="#L155">155</span>
<span id="L156" rel="#L156">156</span>
<span id="L157" rel="#L157">157</span>
<span id="L158" rel="#L158">158</span>
<span id="L159" rel="#L159">159</span>
<span id="L160" rel="#L160">160</span>
<span id="L161" rel="#L161">161</span>
<span id="L162" rel="#L162">162</span>
<span id="L163" rel="#L163">163</span>
<span id="L164" rel="#L164">164</span>
<span id="L165" rel="#L165">165</span>
<span id="L166" rel="#L166">166</span>
<span id="L167" rel="#L167">167</span>
<span id="L168" rel="#L168">168</span>
<span id="L169" rel="#L169">169</span>
<span id="L170" rel="#L170">170</span>
<span id="L171" rel="#L171">171</span>
<span id="L172" rel="#L172">172</span>
<span id="L173" rel="#L173">173</span>
<span id="L174" rel="#L174">174</span>
<span id="L175" rel="#L175">175</span>
<span id="L176" rel="#L176">176</span>
<span id="L177" rel="#L177">177</span>
<span id="L178" rel="#L178">178</span>
<span id="L179" rel="#L179">179</span>
<span id="L180" rel="#L180">180</span>
<span id="L181" rel="#L181">181</span>
<span id="L182" rel="#L182">182</span>
<span id="L183" rel="#L183">183</span>
<span id="L184" rel="#L184">184</span>
<span id="L185" rel="#L185">185</span>
<span id="L186" rel="#L186">186</span>
<span id="L187" rel="#L187">187</span>
<span id="L188" rel="#L188">188</span>
<span id="L189" rel="#L189">189</span>
<span id="L190" rel="#L190">190</span>
<span id="L191" rel="#L191">191</span>
<span id="L192" rel="#L192">192</span>
<span id="L193" rel="#L193">193</span>
<span id="L194" rel="#L194">194</span>
<span id="L195" rel="#L195">195</span>
<span id="L196" rel="#L196">196</span>
<span id="L197" rel="#L197">197</span>
<span id="L198" rel="#L198">198</span>
<span id="L199" rel="#L199">199</span>
<span id="L200" rel="#L200">200</span>
<span id="L201" rel="#L201">201</span>
<span id="L202" rel="#L202">202</span>
<span id="L203" rel="#L203">203</span>
<span id="L204" rel="#L204">204</span>
<span id="L205" rel="#L205">205</span>
<span id="L206" rel="#L206">206</span>
<span id="L207" rel="#L207">207</span>
<span id="L208" rel="#L208">208</span>
<span id="L209" rel="#L209">209</span>
<span id="L210" rel="#L210">210</span>
<span id="L211" rel="#L211">211</span>
<span id="L212" rel="#L212">212</span>
<span id="L213" rel="#L213">213</span>
<span id="L214" rel="#L214">214</span>
<span id="L215" rel="#L215">215</span>
<span id="L216" rel="#L216">216</span>
<span id="L217" rel="#L217">217</span>
<span id="L218" rel="#L218">218</span>
<span id="L219" rel="#L219">219</span>
<span id="L220" rel="#L220">220</span>
<span id="L221" rel="#L221">221</span>
<span id="L222" rel="#L222">222</span>
<span id="L223" rel="#L223">223</span>
<span id="L224" rel="#L224">224</span>
<span id="L225" rel="#L225">225</span>
<span id="L226" rel="#L226">226</span>
<span id="L227" rel="#L227">227</span>
<span id="L228" rel="#L228">228</span>
<span id="L229" rel="#L229">229</span>
<span id="L230" rel="#L230">230</span>
<span id="L231" rel="#L231">231</span>
<span id="L232" rel="#L232">232</span>
<span id="L233" rel="#L233">233</span>
<span id="L234" rel="#L234">234</span>
<span id="L235" rel="#L235">235</span>
<span id="L236" rel="#L236">236</span>
<span id="L237" rel="#L237">237</span>
<span id="L238" rel="#L238">238</span>
<span id="L239" rel="#L239">239</span>
<span id="L240" rel="#L240">240</span>
<span id="L241" rel="#L241">241</span>
<span id="L242" rel="#L242">242</span>
<span id="L243" rel="#L243">243</span>
<span id="L244" rel="#L244">244</span>
<span id="L245" rel="#L245">245</span>
<span id="L246" rel="#L246">246</span>
<span id="L247" rel="#L247">247</span>
<span id="L248" rel="#L248">248</span>
<span id="L249" rel="#L249">249</span>
<span id="L250" rel="#L250">250</span>
<span id="L251" rel="#L251">251</span>
<span id="L252" rel="#L252">252</span>
<span id="L253" rel="#L253">253</span>
<span id="L254" rel="#L254">254</span>
<span id="L255" rel="#L255">255</span>
<span id="L256" rel="#L256">256</span>
<span id="L257" rel="#L257">257</span>
<span id="L258" rel="#L258">258</span>
<span id="L259" rel="#L259">259</span>
<span id="L260" rel="#L260">260</span>
<span id="L261" rel="#L261">261</span>
<span id="L262" rel="#L262">262</span>
<span id="L263" rel="#L263">263</span>
<span id="L264" rel="#L264">264</span>
<span id="L265" rel="#L265">265</span>
<span id="L266" rel="#L266">266</span>
<span id="L267" rel="#L267">267</span>
<span id="L268" rel="#L268">268</span>
<span id="L269" rel="#L269">269</span>
<span id="L270" rel="#L270">270</span>
<span id="L271" rel="#L271">271</span>
<span id="L272" rel="#L272">272</span>
<span id="L273" rel="#L273">273</span>
<span id="L274" rel="#L274">274</span>
<span id="L275" rel="#L275">275</span>
<span id="L276" rel="#L276">276</span>
<span id="L277" rel="#L277">277</span>
<span id="L278" rel="#L278">278</span>
<span id="L279" rel="#L279">279</span>
<span id="L280" rel="#L280">280</span>
<span id="L281" rel="#L281">281</span>
<span id="L282" rel="#L282">282</span>
<span id="L283" rel="#L283">283</span>
<span id="L284" rel="#L284">284</span>
<span id="L285" rel="#L285">285</span>
<span id="L286" rel="#L286">286</span>
<span id="L287" rel="#L287">287</span>
<span id="L288" rel="#L288">288</span>
<span id="L289" rel="#L289">289</span>
<span id="L290" rel="#L290">290</span>
<span id="L291" rel="#L291">291</span>
<span id="L292" rel="#L292">292</span>
<span id="L293" rel="#L293">293</span>
<span id="L294" rel="#L294">294</span>
<span id="L295" rel="#L295">295</span>
<span id="L296" rel="#L296">296</span>
<span id="L297" rel="#L297">297</span>
<span id="L298" rel="#L298">298</span>
<span id="L299" rel="#L299">299</span>
<span id="L300" rel="#L300">300</span>
<span id="L301" rel="#L301">301</span>
<span id="L302" rel="#L302">302</span>
<span id="L303" rel="#L303">303</span>
<span id="L304" rel="#L304">304</span>
<span id="L305" rel="#L305">305</span>
<span id="L306" rel="#L306">306</span>
<span id="L307" rel="#L307">307</span>
<span id="L308" rel="#L308">308</span>
<span id="L309" rel="#L309">309</span>
<span id="L310" rel="#L310">310</span>
<span id="L311" rel="#L311">311</span>
<span id="L312" rel="#L312">312</span>
<span id="L313" rel="#L313">313</span>
<span id="L314" rel="#L314">314</span>
<span id="L315" rel="#L315">315</span>
<span id="L316" rel="#L316">316</span>
<span id="L317" rel="#L317">317</span>
<span id="L318" rel="#L318">318</span>
<span id="L319" rel="#L319">319</span>
<span id="L320" rel="#L320">320</span>
<span id="L321" rel="#L321">321</span>
<span id="L322" rel="#L322">322</span>
<span id="L323" rel="#L323">323</span>
<span id="L324" rel="#L324">324</span>
<span id="L325" rel="#L325">325</span>
<span id="L326" rel="#L326">326</span>
<span id="L327" rel="#L327">327</span>
<span id="L328" rel="#L328">328</span>
<span id="L329" rel="#L329">329</span>
<span id="L330" rel="#L330">330</span>
<span id="L331" rel="#L331">331</span>
<span id="L332" rel="#L332">332</span>
<span id="L333" rel="#L333">333</span>
<span id="L334" rel="#L334">334</span>
<span id="L335" rel="#L335">335</span>
<span id="L336" rel="#L336">336</span>
<span id="L337" rel="#L337">337</span>
<span id="L338" rel="#L338">338</span>
<span id="L339" rel="#L339">339</span>
<span id="L340" rel="#L340">340</span>
<span id="L341" rel="#L341">341</span>
<span id="L342" rel="#L342">342</span>
<span id="L343" rel="#L343">343</span>
<span id="L344" rel="#L344">344</span>
<span id="L345" rel="#L345">345</span>
<span id="L346" rel="#L346">346</span>
<span id="L347" rel="#L347">347</span>
<span id="L348" rel="#L348">348</span>
<span id="L349" rel="#L349">349</span>
<span id="L350" rel="#L350">350</span>
<span id="L351" rel="#L351">351</span>
<span id="L352" rel="#L352">352</span>
<span id="L353" rel="#L353">353</span>
<span id="L354" rel="#L354">354</span>
<span id="L355" rel="#L355">355</span>
<span id="L356" rel="#L356">356</span>
<span id="L357" rel="#L357">357</span>
<span id="L358" rel="#L358">358</span>
<span id="L359" rel="#L359">359</span>
<span id="L360" rel="#L360">360</span>
<span id="L361" rel="#L361">361</span>
<span id="L362" rel="#L362">362</span>
<span id="L363" rel="#L363">363</span>
<span id="L364" rel="#L364">364</span>
<span id="L365" rel="#L365">365</span>
<span id="L366" rel="#L366">366</span>
<span id="L367" rel="#L367">367</span>
<span id="L368" rel="#L368">368</span>
<span id="L369" rel="#L369">369</span>
<span id="L370" rel="#L370">370</span>
<span id="L371" rel="#L371">371</span>
<span id="L372" rel="#L372">372</span>
<span id="L373" rel="#L373">373</span>
<span id="L374" rel="#L374">374</span>
<span id="L375" rel="#L375">375</span>
<span id="L376" rel="#L376">376</span>
<span id="L377" rel="#L377">377</span>
<span id="L378" rel="#L378">378</span>
<span id="L379" rel="#L379">379</span>
<span id="L380" rel="#L380">380</span>
<span id="L381" rel="#L381">381</span>
<span id="L382" rel="#L382">382</span>
<span id="L383" rel="#L383">383</span>
<span id="L384" rel="#L384">384</span>
<span id="L385" rel="#L385">385</span>
<span id="L386" rel="#L386">386</span>
<span id="L387" rel="#L387">387</span>
<span id="L388" rel="#L388">388</span>
<span id="L389" rel="#L389">389</span>
<span id="L390" rel="#L390">390</span>
<span id="L391" rel="#L391">391</span>
<span id="L392" rel="#L392">392</span>
<span id="L393" rel="#L393">393</span>
<span id="L394" rel="#L394">394</span>
<span id="L395" rel="#L395">395</span>
<span id="L396" rel="#L396">396</span>
<span id="L397" rel="#L397">397</span>
<span id="L398" rel="#L398">398</span>
<span id="L399" rel="#L399">399</span>
<span id="L400" rel="#L400">400</span>
<span id="L401" rel="#L401">401</span>
<span id="L402" rel="#L402">402</span>
<span id="L403" rel="#L403">403</span>
<span id="L404" rel="#L404">404</span>
<span id="L405" rel="#L405">405</span>
<span id="L406" rel="#L406">406</span>
<span id="L407" rel="#L407">407</span>
<span id="L408" rel="#L408">408</span>
<span id="L409" rel="#L409">409</span>
<span id="L410" rel="#L410">410</span>
<span id="L411" rel="#L411">411</span>
<span id="L412" rel="#L412">412</span>
<span id="L413" rel="#L413">413</span>
<span id="L414" rel="#L414">414</span>
<span id="L415" rel="#L415">415</span>
<span id="L416" rel="#L416">416</span>
<span id="L417" rel="#L417">417</span>
<span id="L418" rel="#L418">418</span>
<span id="L419" rel="#L419">419</span>
<span id="L420" rel="#L420">420</span>
<span id="L421" rel="#L421">421</span>
<span id="L422" rel="#L422">422</span>
<span id="L423" rel="#L423">423</span>
<span id="L424" rel="#L424">424</span>
<span id="L425" rel="#L425">425</span>
<span id="L426" rel="#L426">426</span>
<span id="L427" rel="#L427">427</span>
<span id="L428" rel="#L428">428</span>
<span id="L429" rel="#L429">429</span>
<span id="L430" rel="#L430">430</span>
<span id="L431" rel="#L431">431</span>
<span id="L432" rel="#L432">432</span>
<span id="L433" rel="#L433">433</span>
<span id="L434" rel="#L434">434</span>
<span id="L435" rel="#L435">435</span>
<span id="L436" rel="#L436">436</span>
<span id="L437" rel="#L437">437</span>
<span id="L438" rel="#L438">438</span>
<span id="L439" rel="#L439">439</span>
<span id="L440" rel="#L440">440</span>
<span id="L441" rel="#L441">441</span>
<span id="L442" rel="#L442">442</span>
<span id="L443" rel="#L443">443</span>
<span id="L444" rel="#L444">444</span>
<span id="L445" rel="#L445">445</span>
<span id="L446" rel="#L446">446</span>
<span id="L447" rel="#L447">447</span>
<span id="L448" rel="#L448">448</span>
<span id="L449" rel="#L449">449</span>
<span id="L450" rel="#L450">450</span>
<span id="L451" rel="#L451">451</span>
<span id="L452" rel="#L452">452</span>
<span id="L453" rel="#L453">453</span>
<span id="L454" rel="#L454">454</span>
<span id="L455" rel="#L455">455</span>
<span id="L456" rel="#L456">456</span>
<span id="L457" rel="#L457">457</span>
<span id="L458" rel="#L458">458</span>
<span id="L459" rel="#L459">459</span>
<span id="L460" rel="#L460">460</span>
<span id="L461" rel="#L461">461</span>
<span id="L462" rel="#L462">462</span>
<span id="L463" rel="#L463">463</span>
<span id="L464" rel="#L464">464</span>
<span id="L465" rel="#L465">465</span>
<span id="L466" rel="#L466">466</span>
<span id="L467" rel="#L467">467</span>
<span id="L468" rel="#L468">468</span>
<span id="L469" rel="#L469">469</span>
<span id="L470" rel="#L470">470</span>
<span id="L471" rel="#L471">471</span>
<span id="L472" rel="#L472">472</span>
<span id="L473" rel="#L473">473</span>
<span id="L474" rel="#L474">474</span>
<span id="L475" rel="#L475">475</span>
<span id="L476" rel="#L476">476</span>
<span id="L477" rel="#L477">477</span>
<span id="L478" rel="#L478">478</span>
<span id="L479" rel="#L479">479</span>
<span id="L480" rel="#L480">480</span>
<span id="L481" rel="#L481">481</span>
<span id="L482" rel="#L482">482</span>
<span id="L483" rel="#L483">483</span>
<span id="L484" rel="#L484">484</span>
<span id="L485" rel="#L485">485</span>
</pre>
          </td>
          <td width="100%">
                <div class="highlight"><pre><div class='line' id='LC1'><span class="cm">/*!</span></div><div class='line' id='LC2'><span class="cm"> * jQuery Templates Plugin 1.0.0pre</span></div><div class='line' id='LC3'><span class="cm"> * http://github.com/jquery/jquery-tmpl</span></div><div class='line' id='LC4'><span class="cm"> * Requires jQuery 1.4.2</span></div><div class='line' id='LC5'><span class="cm"> *</span></div><div class='line' id='LC6'><span class="cm"> * Copyright Software Freedom Conservancy, Inc.</span></div><div class='line' id='LC7'><span class="cm"> * Dual licensed under the MIT or GPL Version 2 licenses.</span></div><div class='line' id='LC8'><span class="cm"> * http://jquery.org/license</span></div><div class='line' id='LC9'><span class="cm"> */</span></div><div class='line' id='LC10'><span class="p">(</span><span class="kd">function</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">,</span> <span class="kc">undefined</span> <span class="p">){</span></div><div class='line' id='LC11'>	<span class="kd">var</span> <span class="nx">oldManip</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">fn</span><span class="p">.</span><span class="nx">domManip</span><span class="p">,</span> <span class="nx">tmplItmAtt</span> <span class="o">=</span> <span class="s2">&quot;_tmplitem&quot;</span><span class="p">,</span> <span class="nx">htmlExpr</span> <span class="o">=</span> <span class="sr">/^[^&lt;]*(&lt;[\w\W]+&gt;)[^&gt;]*$|\{\{\! /</span><span class="p">,</span></div><div class='line' id='LC12'>		<span class="nx">newTmplItems</span> <span class="o">=</span> <span class="p">{},</span> <span class="nx">wrappedItems</span> <span class="o">=</span> <span class="p">{},</span> <span class="nx">appendToTmplItems</span><span class="p">,</span> <span class="nx">topTmplItem</span> <span class="o">=</span> <span class="p">{</span> <span class="nx">key</span><span class="o">:</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">data</span><span class="o">:</span> <span class="p">{}</span> <span class="p">},</span> <span class="nx">itemKey</span> <span class="o">=</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">cloneIndex</span> <span class="o">=</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">stack</span> <span class="o">=</span> <span class="p">[];</span></div><div class='line' id='LC13'><br/></div><div class='line' id='LC14'>	<span class="kd">function</span> <span class="nx">newTmplItem</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="nx">fn</span><span class="p">,</span> <span class="nx">data</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC15'>		<span class="c1">// Returns a template item data structure for a new rendered instance of a template (a &#39;template item&#39;).</span></div><div class='line' id='LC16'>		<span class="c1">// The content field is a hierarchical array of strings and nested items (to be</span></div><div class='line' id='LC17'>		<span class="c1">// removed and replaced by nodes field of dom elements, once inserted in DOM).</span></div><div class='line' id='LC18'>		<span class="kd">var</span> <span class="nx">newItem</span> <span class="o">=</span> <span class="p">{</span></div><div class='line' id='LC19'>			<span class="nx">data</span><span class="o">:</span> <span class="nx">data</span> <span class="o">||</span> <span class="p">(</span><span class="nx">data</span> <span class="o">===</span> <span class="mi">0</span> <span class="o">||</span> <span class="nx">data</span> <span class="o">===</span> <span class="kc">false</span><span class="p">)</span> <span class="o">?</span> <span class="nx">data</span> <span class="o">:</span> <span class="p">(</span><span class="nx">parentItem</span> <span class="o">?</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">data</span> <span class="o">:</span> <span class="p">{}),</span></div><div class='line' id='LC20'>			<span class="nx">_wrap</span><span class="o">:</span> <span class="nx">parentItem</span> <span class="o">?</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">_wrap</span> <span class="o">:</span> <span class="kc">null</span><span class="p">,</span></div><div class='line' id='LC21'>			<span class="nx">tmpl</span><span class="o">:</span> <span class="kc">null</span><span class="p">,</span></div><div class='line' id='LC22'>			<span class="nx">parent</span><span class="o">:</span> <span class="nx">parentItem</span> <span class="o">||</span> <span class="kc">null</span><span class="p">,</span></div><div class='line' id='LC23'>			<span class="nx">nodes</span><span class="o">:</span> <span class="p">[],</span></div><div class='line' id='LC24'>			<span class="nx">calls</span><span class="o">:</span> <span class="nx">tiCalls</span><span class="p">,</span></div><div class='line' id='LC25'>			<span class="nx">nest</span><span class="o">:</span> <span class="nx">tiNest</span><span class="p">,</span></div><div class='line' id='LC26'>			<span class="nx">wrap</span><span class="o">:</span> <span class="nx">tiWrap</span><span class="p">,</span></div><div class='line' id='LC27'>			<span class="nx">html</span><span class="o">:</span> <span class="nx">tiHtml</span><span class="p">,</span></div><div class='line' id='LC28'>			<span class="nx">update</span><span class="o">:</span> <span class="nx">tiUpdate</span></div><div class='line' id='LC29'>		<span class="p">};</span></div><div class='line' id='LC30'>		<span class="k">if</span> <span class="p">(</span> <span class="nx">options</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC31'>			<span class="nx">jQuery</span><span class="p">.</span><span class="nx">extend</span><span class="p">(</span> <span class="nx">newItem</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="p">{</span> <span class="nx">nodes</span><span class="o">:</span> <span class="p">[],</span> <span class="nx">parent</span><span class="o">:</span> <span class="nx">parentItem</span> <span class="p">});</span></div><div class='line' id='LC32'>		<span class="p">}</span></div><div class='line' id='LC33'>		<span class="k">if</span> <span class="p">(</span> <span class="nx">fn</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC34'>			<span class="c1">// Build the hierarchical content to be used during insertion into DOM</span></div><div class='line' id='LC35'>			<span class="nx">newItem</span><span class="p">.</span><span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">fn</span><span class="p">;</span></div><div class='line' id='LC36'>			<span class="nx">newItem</span><span class="p">.</span><span class="nx">_ctnt</span> <span class="o">=</span> <span class="nx">newItem</span><span class="p">.</span><span class="nx">_ctnt</span> <span class="o">||</span> <span class="nx">newItem</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">,</span> <span class="nx">newItem</span> <span class="p">);</span></div><div class='line' id='LC37'>			<span class="nx">newItem</span><span class="p">.</span><span class="nx">key</span> <span class="o">=</span> <span class="o">++</span><span class="nx">itemKey</span><span class="p">;</span></div><div class='line' id='LC38'>			<span class="c1">// Keep track of new template item, until it is stored as jQuery Data on DOM element</span></div><div class='line' id='LC39'>			<span class="p">(</span><span class="nx">stack</span><span class="p">.</span><span class="nx">length</span> <span class="o">?</span> <span class="nx">wrappedItems</span> <span class="o">:</span> <span class="nx">newTmplItems</span><span class="p">)[</span><span class="nx">itemKey</span><span class="p">]</span> <span class="o">=</span> <span class="nx">newItem</span><span class="p">;</span></div><div class='line' id='LC40'>		<span class="p">}</span></div><div class='line' id='LC41'>		<span class="k">return</span> <span class="nx">newItem</span><span class="p">;</span></div><div class='line' id='LC42'>	<span class="p">}</span></div><div class='line' id='LC43'><br/></div><div class='line' id='LC44'>	<span class="c1">// Override appendTo etc., in order to provide support for targeting multiple elements. (This code would disappear if integrated in jquery core).</span></div><div class='line' id='LC45'>	<span class="nx">jQuery</span><span class="p">.</span><span class="nx">each</span><span class="p">({</span></div><div class='line' id='LC46'>		<span class="nx">appendTo</span><span class="o">:</span> <span class="s2">&quot;append&quot;</span><span class="p">,</span></div><div class='line' id='LC47'>		<span class="nx">prependTo</span><span class="o">:</span> <span class="s2">&quot;prepend&quot;</span><span class="p">,</span></div><div class='line' id='LC48'>		<span class="nx">insertBefore</span><span class="o">:</span> <span class="s2">&quot;before&quot;</span><span class="p">,</span></div><div class='line' id='LC49'>		<span class="nx">insertAfter</span><span class="o">:</span> <span class="s2">&quot;after&quot;</span><span class="p">,</span></div><div class='line' id='LC50'>		<span class="nx">replaceAll</span><span class="o">:</span> <span class="s2">&quot;replaceWith&quot;</span></div><div class='line' id='LC51'>	<span class="p">},</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">name</span><span class="p">,</span> <span class="nx">original</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC52'>		<span class="nx">jQuery</span><span class="p">.</span><span class="nx">fn</span><span class="p">[</span> <span class="nx">name</span> <span class="p">]</span> <span class="o">=</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">selector</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC53'>			<span class="kd">var</span> <span class="nx">ret</span> <span class="o">=</span> <span class="p">[],</span> <span class="nx">insert</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">selector</span> <span class="p">),</span> <span class="nx">elems</span><span class="p">,</span> <span class="nx">i</span><span class="p">,</span> <span class="nx">l</span><span class="p">,</span> <span class="nx">tmplItems</span><span class="p">,</span></div><div class='line' id='LC54'>				<span class="nx">parent</span> <span class="o">=</span> <span class="k">this</span><span class="p">.</span><span class="nx">length</span> <span class="o">===</span> <span class="mi">1</span> <span class="o">&amp;&amp;</span> <span class="k">this</span><span class="p">[</span><span class="mi">0</span><span class="p">].</span><span class="nx">parentNode</span><span class="p">;</span></div><div class='line' id='LC55'><br/></div><div class='line' id='LC56'>			<span class="nx">appendToTmplItems</span> <span class="o">=</span> <span class="nx">newTmplItems</span> <span class="o">||</span> <span class="p">{};</span></div><div class='line' id='LC57'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">parent</span> <span class="o">&amp;&amp;</span> <span class="nx">parent</span><span class="p">.</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">11</span> <span class="o">&amp;&amp;</span> <span class="nx">parent</span><span class="p">.</span><span class="nx">childNodes</span><span class="p">.</span><span class="nx">length</span> <span class="o">===</span> <span class="mi">1</span> <span class="o">&amp;&amp;</span> <span class="nx">insert</span><span class="p">.</span><span class="nx">length</span> <span class="o">===</span> <span class="mi">1</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC58'>				<span class="nx">insert</span><span class="p">[</span> <span class="nx">original</span> <span class="p">](</span> <span class="k">this</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC59'>				<span class="nx">ret</span> <span class="o">=</span> <span class="k">this</span><span class="p">;</span></div><div class='line' id='LC60'>			<span class="p">}</span> <span class="k">else</span> <span class="p">{</span></div><div class='line' id='LC61'>				<span class="k">for</span> <span class="p">(</span> <span class="nx">i</span> <span class="o">=</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">l</span> <span class="o">=</span> <span class="nx">insert</span><span class="p">.</span><span class="nx">length</span><span class="p">;</span> <span class="nx">i</span> <span class="o">&lt;</span> <span class="nx">l</span><span class="p">;</span> <span class="nx">i</span><span class="o">++</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC62'>					<span class="nx">cloneIndex</span> <span class="o">=</span> <span class="nx">i</span><span class="p">;</span></div><div class='line' id='LC63'>					<span class="nx">elems</span> <span class="o">=</span> <span class="p">(</span><span class="nx">i</span> <span class="o">&gt;</span> <span class="mi">0</span> <span class="o">?</span> <span class="k">this</span><span class="p">.</span><span class="nx">clone</span><span class="p">(</span><span class="kc">true</span><span class="p">)</span> <span class="o">:</span> <span class="k">this</span><span class="p">).</span><span class="nx">get</span><span class="p">();</span></div><div class='line' id='LC64'>					<span class="nx">jQuery</span><span class="p">(</span> <span class="nx">insert</span><span class="p">[</span><span class="nx">i</span><span class="p">]</span> <span class="p">)[</span> <span class="nx">original</span> <span class="p">](</span> <span class="nx">elems</span> <span class="p">);</span></div><div class='line' id='LC65'>					<span class="nx">ret</span> <span class="o">=</span> <span class="nx">ret</span><span class="p">.</span><span class="nx">concat</span><span class="p">(</span> <span class="nx">elems</span> <span class="p">);</span></div><div class='line' id='LC66'>				<span class="p">}</span></div><div class='line' id='LC67'>				<span class="nx">cloneIndex</span> <span class="o">=</span> <span class="mi">0</span><span class="p">;</span></div><div class='line' id='LC68'>				<span class="nx">ret</span> <span class="o">=</span> <span class="k">this</span><span class="p">.</span><span class="nx">pushStack</span><span class="p">(</span> <span class="nx">ret</span><span class="p">,</span> <span class="nx">name</span><span class="p">,</span> <span class="nx">insert</span><span class="p">.</span><span class="nx">selector</span> <span class="p">);</span></div><div class='line' id='LC69'>			<span class="p">}</span></div><div class='line' id='LC70'>			<span class="nx">tmplItems</span> <span class="o">=</span> <span class="nx">appendToTmplItems</span><span class="p">;</span></div><div class='line' id='LC71'>			<span class="nx">appendToTmplItems</span> <span class="o">=</span> <span class="kc">null</span><span class="p">;</span></div><div class='line' id='LC72'>			<span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">.</span><span class="nx">complete</span><span class="p">(</span> <span class="nx">tmplItems</span> <span class="p">);</span></div><div class='line' id='LC73'>			<span class="k">return</span> <span class="nx">ret</span><span class="p">;</span></div><div class='line' id='LC74'>		<span class="p">};</span></div><div class='line' id='LC75'>	<span class="p">});</span></div><div class='line' id='LC76'><br/></div><div class='line' id='LC77'>	<span class="nx">jQuery</span><span class="p">.</span><span class="nx">fn</span><span class="p">.</span><span class="nx">extend</span><span class="p">({</span></div><div class='line' id='LC78'>		<span class="c1">// Use first wrapped element as template markup.</span></div><div class='line' id='LC79'>		<span class="c1">// Return wrapped set of template items, obtained by rendering template against data.</span></div><div class='line' id='LC80'>		<span class="nx">tmpl</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC81'>			<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="k">this</span><span class="p">[</span><span class="mi">0</span><span class="p">],</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span> <span class="p">);</span></div><div class='line' id='LC82'>		<span class="p">},</span></div><div class='line' id='LC83'><br/></div><div class='line' id='LC84'>		<span class="c1">// Find which rendered template item the first wrapped DOM element belongs to</span></div><div class='line' id='LC85'>		<span class="nx">tmplItem</span><span class="o">:</span> <span class="kd">function</span><span class="p">()</span> <span class="p">{</span></div><div class='line' id='LC86'>			<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmplItem</span><span class="p">(</span> <span class="k">this</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC87'>		<span class="p">},</span></div><div class='line' id='LC88'><br/></div><div class='line' id='LC89'>		<span class="c1">// Consider the first wrapped element as a template declaration, and get the compiled template or store it as a named template.</span></div><div class='line' id='LC90'>		<span class="nx">template</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">name</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC91'>			<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="nx">name</span><span class="p">,</span> <span class="k">this</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC92'>		<span class="p">},</span></div><div class='line' id='LC93'><br/></div><div class='line' id='LC94'>		<span class="nx">domManip</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">args</span><span class="p">,</span> <span class="nx">table</span><span class="p">,</span> <span class="nx">callback</span><span class="p">,</span> <span class="nx">options</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC95'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">args</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="o">&amp;&amp;</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">isArray</span><span class="p">(</span> <span class="nx">args</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="p">))</span> <span class="p">{</span></div><div class='line' id='LC96'>				<span class="kd">var</span> <span class="nx">dmArgs</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">makeArray</span><span class="p">(</span> <span class="nx">arguments</span> <span class="p">),</span> <span class="nx">elems</span> <span class="o">=</span> <span class="nx">args</span><span class="p">[</span><span class="mi">0</span><span class="p">],</span> <span class="nx">elemsLength</span> <span class="o">=</span> <span class="nx">elems</span><span class="p">.</span><span class="nx">length</span><span class="p">,</span> <span class="nx">i</span> <span class="o">=</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC97'>				<span class="k">while</span> <span class="p">(</span> <span class="nx">i</span> <span class="o">&lt;</span> <span class="nx">elemsLength</span> <span class="o">&amp;&amp;</span> <span class="o">!</span><span class="p">(</span><span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">elems</span><span class="p">[</span><span class="nx">i</span><span class="o">++</span><span class="p">],</span> <span class="s2">&quot;tmplItem&quot;</span> <span class="p">)))</span> <span class="p">{}</span></div><div class='line' id='LC98'>				<span class="k">if</span> <span class="p">(</span> <span class="nx">tmplItem</span> <span class="o">&amp;&amp;</span> <span class="nx">cloneIndex</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC99'>					<span class="nx">dmArgs</span><span class="p">[</span><span class="mi">2</span><span class="p">]</span> <span class="o">=</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">fragClone</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC100'>						<span class="c1">// Handler called by oldManip when rendered template has been inserted into DOM.</span></div><div class='line' id='LC101'>						<span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">.</span><span class="nx">afterManip</span><span class="p">(</span> <span class="k">this</span><span class="p">,</span> <span class="nx">fragClone</span><span class="p">,</span> <span class="nx">callback</span> <span class="p">);</span></div><div class='line' id='LC102'>					<span class="p">};</span></div><div class='line' id='LC103'>				<span class="p">}</span></div><div class='line' id='LC104'>				<span class="nx">oldManip</span><span class="p">.</span><span class="nx">apply</span><span class="p">(</span> <span class="k">this</span><span class="p">,</span> <span class="nx">dmArgs</span> <span class="p">);</span></div><div class='line' id='LC105'>			<span class="p">}</span> <span class="k">else</span> <span class="p">{</span></div><div class='line' id='LC106'>				<span class="nx">oldManip</span><span class="p">.</span><span class="nx">apply</span><span class="p">(</span> <span class="k">this</span><span class="p">,</span> <span class="nx">arguments</span> <span class="p">);</span></div><div class='line' id='LC107'>			<span class="p">}</span></div><div class='line' id='LC108'>			<span class="nx">cloneIndex</span> <span class="o">=</span> <span class="mi">0</span><span class="p">;</span></div><div class='line' id='LC109'>			<span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="nx">appendToTmplItems</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC110'>				<span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">.</span><span class="nx">complete</span><span class="p">(</span> <span class="nx">newTmplItems</span> <span class="p">);</span></div><div class='line' id='LC111'>			<span class="p">}</span></div><div class='line' id='LC112'>			<span class="k">return</span> <span class="k">this</span><span class="p">;</span></div><div class='line' id='LC113'>		<span class="p">}</span></div><div class='line' id='LC114'>	<span class="p">});</span></div><div class='line' id='LC115'><br/></div><div class='line' id='LC116'>	<span class="nx">jQuery</span><span class="p">.</span><span class="nx">extend</span><span class="p">({</span></div><div class='line' id='LC117'>		<span class="c1">// Return wrapped set of template items, obtained by rendering template against data.</span></div><div class='line' id='LC118'>		<span class="nx">tmpl</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC119'>			<span class="kd">var</span> <span class="nx">ret</span><span class="p">,</span> <span class="nx">topLevel</span> <span class="o">=</span> <span class="o">!</span><span class="nx">parentItem</span><span class="p">;</span></div><div class='line' id='LC120'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">topLevel</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC121'>				<span class="c1">// This is a top-level tmpl call (not from a nested template using {{tmpl}})</span></div><div class='line' id='LC122'>				<span class="nx">parentItem</span> <span class="o">=</span> <span class="nx">topTmplItem</span><span class="p">;</span></div><div class='line' id='LC123'>				<span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">[</span><span class="nx">tmpl</span><span class="p">]</span> <span class="o">||</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="kc">null</span><span class="p">,</span> <span class="nx">tmpl</span> <span class="p">);</span></div><div class='line' id='LC124'>				<span class="nx">wrappedItems</span> <span class="o">=</span> <span class="p">{};</span> <span class="c1">// Any wrapped items will be rebuilt, since this is top level</span></div><div class='line' id='LC125'>			<span class="p">}</span> <span class="k">else</span> <span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="nx">tmpl</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC126'>				<span class="c1">// The template item is already associated with DOM - this is a refresh.</span></div><div class='line' id='LC127'>				<span class="c1">// Re-evaluate rendered template for the parentItem</span></div><div class='line' id='LC128'>				<span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">;</span></div><div class='line' id='LC129'>				<span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">parentItem</span><span class="p">.</span><span class="nx">key</span><span class="p">]</span> <span class="o">=</span> <span class="nx">parentItem</span><span class="p">;</span></div><div class='line' id='LC130'>				<span class="nx">parentItem</span><span class="p">.</span><span class="nx">nodes</span> <span class="o">=</span> <span class="p">[];</span></div><div class='line' id='LC131'>				<span class="k">if</span> <span class="p">(</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">wrapped</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC132'>					<span class="nx">updateWrapped</span><span class="p">(</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">wrapped</span> <span class="p">);</span></div><div class='line' id='LC133'>				<span class="p">}</span></div><div class='line' id='LC134'>				<span class="c1">// Rebuild, without creating a new template item</span></div><div class='line' id='LC135'>				<span class="k">return</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">build</span><span class="p">(</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="kc">null</span><span class="p">,</span> <span class="nx">parentItem</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">,</span> <span class="nx">parentItem</span> <span class="p">)</span> <span class="p">));</span></div><div class='line' id='LC136'>			<span class="p">}</span></div><div class='line' id='LC137'>			<span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="nx">tmpl</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC138'>				<span class="k">return</span> <span class="p">[];</span> <span class="c1">// Could throw...</span></div><div class='line' id='LC139'>			<span class="p">}</span></div><div class='line' id='LC140'>			<span class="k">if</span> <span class="p">(</span> <span class="k">typeof</span> <span class="nx">data</span> <span class="o">===</span> <span class="s2">&quot;function&quot;</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC141'>				<span class="nx">data</span> <span class="o">=</span> <span class="nx">data</span><span class="p">.</span><span class="nx">call</span><span class="p">(</span> <span class="nx">parentItem</span> <span class="o">||</span> <span class="p">{}</span> <span class="p">);</span></div><div class='line' id='LC142'>			<span class="p">}</span></div><div class='line' id='LC143'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">options</span> <span class="o">&amp;&amp;</span> <span class="nx">options</span><span class="p">.</span><span class="nx">wrapped</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC144'>				<span class="nx">updateWrapped</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">options</span><span class="p">.</span><span class="nx">wrapped</span> <span class="p">);</span></div><div class='line' id='LC145'>			<span class="p">}</span></div><div class='line' id='LC146'>			<span class="nx">ret</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">isArray</span><span class="p">(</span> <span class="nx">data</span> <span class="p">)</span> <span class="o">?</span></div><div class='line' id='LC147'>				<span class="nx">jQuery</span><span class="p">.</span><span class="nx">map</span><span class="p">(</span> <span class="nx">data</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">dataItem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC148'>					<span class="k">return</span> <span class="nx">dataItem</span> <span class="o">?</span> <span class="nx">newTmplItem</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">dataItem</span> <span class="p">)</span> <span class="o">:</span> <span class="kc">null</span><span class="p">;</span></div><div class='line' id='LC149'>				<span class="p">})</span> <span class="o">:</span></div><div class='line' id='LC150'>				<span class="p">[</span> <span class="nx">newTmplItem</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">data</span> <span class="p">)</span> <span class="p">];</span></div><div class='line' id='LC151'>			<span class="k">return</span> <span class="nx">topLevel</span> <span class="o">?</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">build</span><span class="p">(</span> <span class="nx">parentItem</span><span class="p">,</span> <span class="kc">null</span><span class="p">,</span> <span class="nx">ret</span> <span class="p">)</span> <span class="p">)</span> <span class="o">:</span> <span class="nx">ret</span><span class="p">;</span></div><div class='line' id='LC152'>		<span class="p">},</span></div><div class='line' id='LC153'><br/></div><div class='line' id='LC154'>		<span class="c1">// Return rendered template item for an element.</span></div><div class='line' id='LC155'>		<span class="nx">tmplItem</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">elem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC156'>			<span class="kd">var</span> <span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC157'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">elem</span> <span class="k">instanceof</span> <span class="nx">jQuery</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC158'>				<span class="nx">elem</span> <span class="o">=</span> <span class="nx">elem</span><span class="p">[</span><span class="mi">0</span><span class="p">];</span></div><div class='line' id='LC159'>			<span class="p">}</span></div><div class='line' id='LC160'>			<span class="k">while</span> <span class="p">(</span> <span class="nx">elem</span> <span class="o">&amp;&amp;</span> <span class="nx">elem</span><span class="p">.</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">1</span> <span class="o">&amp;&amp;</span> <span class="o">!</span><span class="p">(</span><span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">elem</span><span class="p">,</span> <span class="s2">&quot;tmplItem&quot;</span> <span class="p">))</span> <span class="o">&amp;&amp;</span> <span class="p">(</span><span class="nx">elem</span> <span class="o">=</span> <span class="nx">elem</span><span class="p">.</span><span class="nx">parentNode</span><span class="p">)</span> <span class="p">)</span> <span class="p">{}</span></div><div class='line' id='LC161'>			<span class="k">return</span> <span class="nx">tmplItem</span> <span class="o">||</span> <span class="nx">topTmplItem</span><span class="p">;</span></div><div class='line' id='LC162'>		<span class="p">},</span></div><div class='line' id='LC163'><br/></div><div class='line' id='LC164'>		<span class="c1">// Set:</span></div><div class='line' id='LC165'>		<span class="c1">// Use $.template( name, tmpl ) to cache a named template,</span></div><div class='line' id='LC166'>		<span class="c1">// where tmpl is a template string, a script element or a jQuery instance wrapping a script element, etc.</span></div><div class='line' id='LC167'>		<span class="c1">// Use $( &quot;selector&quot; ).template( name ) to provide access by name to a script block template declaration.</span></div><div class='line' id='LC168'><br/></div><div class='line' id='LC169'>		<span class="c1">// Get:</span></div><div class='line' id='LC170'>		<span class="c1">// Use $.template( name ) to access a cached template.</span></div><div class='line' id='LC171'>		<span class="c1">// Also $( selectorToScriptBlock ).template(), or $.template( null, templateString )</span></div><div class='line' id='LC172'>		<span class="c1">// will return the compiled template, without adding a name reference.</span></div><div class='line' id='LC173'>		<span class="c1">// If templateString includes at least one HTML tag, $.template( templateString ) is equivalent</span></div><div class='line' id='LC174'>		<span class="c1">// to $.template( null, templateString )</span></div><div class='line' id='LC175'>		<span class="nx">template</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">name</span><span class="p">,</span> <span class="nx">tmpl</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC176'>			<span class="k">if</span> <span class="p">(</span><span class="nx">tmpl</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC177'>				<span class="c1">// Compile template and associate with name</span></div><div class='line' id='LC178'>				<span class="k">if</span> <span class="p">(</span> <span class="k">typeof</span> <span class="nx">tmpl</span> <span class="o">===</span> <span class="s2">&quot;string&quot;</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC179'>					<span class="c1">// This is an HTML string being passed directly in.</span></div><div class='line' id='LC180'>					<span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">buildTmplFn</span><span class="p">(</span> <span class="nx">tmpl</span> <span class="p">);</span></div><div class='line' id='LC181'>				<span class="p">}</span> <span class="k">else</span> <span class="k">if</span> <span class="p">(</span> <span class="nx">tmpl</span> <span class="k">instanceof</span> <span class="nx">jQuery</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC182'>					<span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">tmpl</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="o">||</span> <span class="p">{};</span></div><div class='line' id='LC183'>				<span class="p">}</span></div><div class='line' id='LC184'>				<span class="k">if</span> <span class="p">(</span> <span class="nx">tmpl</span><span class="p">.</span><span class="nx">nodeType</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC185'>					<span class="c1">// If this is a template block, use cached copy, or generate tmpl function and cache.</span></div><div class='line' id='LC186'>					<span class="nx">tmpl</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="s2">&quot;tmpl&quot;</span> <span class="p">)</span> <span class="o">||</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="s2">&quot;tmpl&quot;</span><span class="p">,</span> <span class="nx">buildTmplFn</span><span class="p">(</span> <span class="nx">tmpl</span><span class="p">.</span><span class="nx">innerHTML</span> <span class="p">));</span></div><div class='line' id='LC187'>					<span class="c1">// Issue: In IE, if the container element is not a script block, the innerHTML will remove quotes from attribute values whenever the value does not include white space.</span></div><div class='line' id='LC188'>					<span class="c1">// This means that foo=&quot;${x}&quot; will not work if the value of x includes white space: foo=&quot;${x}&quot; -&gt; foo=value of x.</span></div><div class='line' id='LC189'>					<span class="c1">// To correct this, include space in tag: foo=&quot;${ x }&quot; -&gt; foo=&quot;value of x&quot;</span></div><div class='line' id='LC190'>				<span class="p">}</span></div><div class='line' id='LC191'>				<span class="k">return</span> <span class="k">typeof</span> <span class="nx">name</span> <span class="o">===</span> <span class="s2">&quot;string&quot;</span> <span class="o">?</span> <span class="p">(</span><span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">[</span><span class="nx">name</span><span class="p">]</span> <span class="o">=</span> <span class="nx">tmpl</span><span class="p">)</span> <span class="o">:</span> <span class="nx">tmpl</span><span class="p">;</span></div><div class='line' id='LC192'>			<span class="p">}</span></div><div class='line' id='LC193'>			<span class="c1">// Return named compiled template</span></div><div class='line' id='LC194'>			<span class="k">return</span> <span class="nx">name</span> <span class="o">?</span> <span class="p">(</span><span class="k">typeof</span> <span class="nx">name</span> <span class="o">!==</span> <span class="s2">&quot;string&quot;</span> <span class="o">?</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="kc">null</span><span class="p">,</span> <span class="nx">name</span> <span class="p">)</span><span class="o">:</span></div><div class='line' id='LC195'>				<span class="p">(</span><span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">[</span><span class="nx">name</span><span class="p">]</span> <span class="o">||</span></div><div class='line' id='LC196'>					<span class="c1">// If not in map, and not containing at least on HTML tag, treat as a selector.</span></div><div class='line' id='LC197'>					<span class="c1">// (If integrated with core, use quickExpr.exec)</span></div><div class='line' id='LC198'>					<span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="kc">null</span><span class="p">,</span> <span class="nx">htmlExpr</span><span class="p">.</span><span class="nx">test</span><span class="p">(</span> <span class="nx">name</span> <span class="p">)</span> <span class="o">?</span> <span class="nx">name</span> <span class="o">:</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">name</span> <span class="p">))))</span> <span class="o">:</span> <span class="kc">null</span><span class="p">;</span></div><div class='line' id='LC199'>		<span class="p">},</span></div><div class='line' id='LC200'><br/></div><div class='line' id='LC201'>		<span class="nx">encode</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">text</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC202'>			<span class="c1">// Do HTML encoding replacing &lt; &gt; &amp; and &#39; and &quot; by corresponding entities.</span></div><div class='line' id='LC203'>			<span class="k">return</span> <span class="p">(</span><span class="s2">&quot;&quot;</span> <span class="o">+</span> <span class="nx">text</span><span class="p">).</span><span class="nx">split</span><span class="p">(</span><span class="s2">&quot;&lt;&quot;</span><span class="p">).</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&amp;lt;&quot;</span><span class="p">).</span><span class="nx">split</span><span class="p">(</span><span class="s2">&quot;&gt;&quot;</span><span class="p">).</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&amp;gt;&quot;</span><span class="p">).</span><span class="nx">split</span><span class="p">(</span><span class="s1">&#39;&quot;&#39;</span><span class="p">).</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&amp;#34;&quot;</span><span class="p">).</span><span class="nx">split</span><span class="p">(</span><span class="s2">&quot;&#39;&quot;</span><span class="p">).</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&amp;#39;&quot;</span><span class="p">);</span></div><div class='line' id='LC204'>		<span class="p">}</span></div><div class='line' id='LC205'>	<span class="p">});</span></div><div class='line' id='LC206'><br/></div><div class='line' id='LC207'>	<span class="nx">jQuery</span><span class="p">.</span><span class="nx">extend</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">,</span> <span class="p">{</span></div><div class='line' id='LC208'>		<span class="nx">tag</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC209'>			<span class="s2">&quot;tmpl&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC210'>				<span class="nx">_default</span><span class="o">:</span> <span class="p">{</span> <span class="nx">$2</span><span class="o">:</span> <span class="s2">&quot;null&quot;</span> <span class="p">},</span></div><div class='line' id='LC211'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;if($notnull_1){__=__.concat($item.nest($1,$2));}&quot;</span></div><div class='line' id='LC212'>				<span class="c1">// tmpl target parameter can be of type function, so use $1, not $1a (so not auto detection of functions)</span></div><div class='line' id='LC213'>				<span class="c1">// This means that {{tmpl foo}} treats foo as a template (which IS a function).</span></div><div class='line' id='LC214'>				<span class="c1">// Explicit parens can be used if foo is a function that returns a template: {{tmpl foo()}}.</span></div><div class='line' id='LC215'>			<span class="p">},</span></div><div class='line' id='LC216'>			<span class="s2">&quot;wrap&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC217'>				<span class="nx">_default</span><span class="o">:</span> <span class="p">{</span> <span class="nx">$2</span><span class="o">:</span> <span class="s2">&quot;null&quot;</span> <span class="p">},</span></div><div class='line' id='LC218'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;$item.calls(__,$1,$2);__=[];&quot;</span><span class="p">,</span></div><div class='line' id='LC219'>				<span class="nx">close</span><span class="o">:</span> <span class="s2">&quot;call=$item.calls();__=call._.concat($item.wrap(call,__));&quot;</span></div><div class='line' id='LC220'>			<span class="p">},</span></div><div class='line' id='LC221'>			<span class="s2">&quot;each&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC222'>				<span class="nx">_default</span><span class="o">:</span> <span class="p">{</span> <span class="nx">$2</span><span class="o">:</span> <span class="s2">&quot;$index, $value&quot;</span> <span class="p">},</span></div><div class='line' id='LC223'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;if($notnull_1){$.each($1a,function($2){with(this){&quot;</span><span class="p">,</span></div><div class='line' id='LC224'>				<span class="nx">close</span><span class="o">:</span> <span class="s2">&quot;}});}&quot;</span></div><div class='line' id='LC225'>			<span class="p">},</span></div><div class='line' id='LC226'>			<span class="s2">&quot;if&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC227'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;if(($notnull_1) &amp;&amp; $1a){&quot;</span><span class="p">,</span></div><div class='line' id='LC228'>				<span class="nx">close</span><span class="o">:</span> <span class="s2">&quot;}&quot;</span></div><div class='line' id='LC229'>			<span class="p">},</span></div><div class='line' id='LC230'>			<span class="s2">&quot;else&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC231'>				<span class="nx">_default</span><span class="o">:</span> <span class="p">{</span> <span class="nx">$1</span><span class="o">:</span> <span class="s2">&quot;true&quot;</span> <span class="p">},</span></div><div class='line' id='LC232'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;}else if(($notnull_1) &amp;&amp; $1a){&quot;</span></div><div class='line' id='LC233'>			<span class="p">},</span></div><div class='line' id='LC234'>			<span class="s2">&quot;html&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC235'>				<span class="c1">// Unecoded expression evaluation.</span></div><div class='line' id='LC236'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;if($notnull_1){__.push($1a);}&quot;</span></div><div class='line' id='LC237'>			<span class="p">},</span></div><div class='line' id='LC238'>			<span class="s2">&quot;=&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC239'>				<span class="c1">// Encoded expression evaluation. Abbreviated form is ${}.</span></div><div class='line' id='LC240'>				<span class="nx">_default</span><span class="o">:</span> <span class="p">{</span> <span class="nx">$1</span><span class="o">:</span> <span class="s2">&quot;$data&quot;</span> <span class="p">},</span></div><div class='line' id='LC241'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;if($notnull_1){__.push($.encode($1a));}&quot;</span></div><div class='line' id='LC242'>			<span class="p">},</span></div><div class='line' id='LC243'>			<span class="s2">&quot;!&quot;</span><span class="o">:</span> <span class="p">{</span></div><div class='line' id='LC244'>				<span class="c1">// Comment tag. Skipped by parser</span></div><div class='line' id='LC245'>				<span class="nx">open</span><span class="o">:</span> <span class="s2">&quot;&quot;</span></div><div class='line' id='LC246'>			<span class="p">}</span></div><div class='line' id='LC247'>		<span class="p">},</span></div><div class='line' id='LC248'><br/></div><div class='line' id='LC249'>		<span class="c1">// This stub can be overridden, e.g. in jquery.tmplPlus for providing rendered events</span></div><div class='line' id='LC250'>		<span class="nx">complete</span><span class="o">:</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">items</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC251'>			<span class="nx">newTmplItems</span> <span class="o">=</span> <span class="p">{};</span></div><div class='line' id='LC252'>		<span class="p">},</span></div><div class='line' id='LC253'><br/></div><div class='line' id='LC254'>		<span class="c1">// Call this from code which overrides domManip, or equivalent</span></div><div class='line' id='LC255'>		<span class="c1">// Manage cloning/storing template items etc.</span></div><div class='line' id='LC256'>		<span class="nx">afterManip</span><span class="o">:</span> <span class="kd">function</span> <span class="nx">afterManip</span><span class="p">(</span> <span class="nx">elem</span><span class="p">,</span> <span class="nx">fragClone</span><span class="p">,</span> <span class="nx">callback</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC257'>			<span class="c1">// Provides cloned fragment ready for fixup prior to and after insertion into DOM</span></div><div class='line' id='LC258'>			<span class="kd">var</span> <span class="nx">content</span> <span class="o">=</span> <span class="nx">fragClone</span><span class="p">.</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">11</span> <span class="o">?</span></div><div class='line' id='LC259'>				<span class="nx">jQuery</span><span class="p">.</span><span class="nx">makeArray</span><span class="p">(</span><span class="nx">fragClone</span><span class="p">.</span><span class="nx">childNodes</span><span class="p">)</span> <span class="o">:</span></div><div class='line' id='LC260'>				<span class="nx">fragClone</span><span class="p">.</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">1</span> <span class="o">?</span> <span class="p">[</span><span class="nx">fragClone</span><span class="p">]</span> <span class="o">:</span> <span class="p">[];</span></div><div class='line' id='LC261'><br/></div><div class='line' id='LC262'>			<span class="c1">// Return fragment to original caller (e.g. append) for DOM insertion</span></div><div class='line' id='LC263'>			<span class="nx">callback</span><span class="p">.</span><span class="nx">call</span><span class="p">(</span> <span class="nx">elem</span><span class="p">,</span> <span class="nx">fragClone</span> <span class="p">);</span></div><div class='line' id='LC264'><br/></div><div class='line' id='LC265'>			<span class="c1">// Fragment has been inserted:- Add inserted nodes to tmplItem data structure. Replace inserted element annotations by jQuery.data.</span></div><div class='line' id='LC266'>			<span class="nx">storeTmplItems</span><span class="p">(</span> <span class="nx">content</span> <span class="p">);</span></div><div class='line' id='LC267'>			<span class="nx">cloneIndex</span><span class="o">++</span><span class="p">;</span></div><div class='line' id='LC268'>		<span class="p">}</span></div><div class='line' id='LC269'>	<span class="p">});</span></div><div class='line' id='LC270'><br/></div><div class='line' id='LC271'>	<span class="c1">//========================== Private helper functions, used by code above ==========================</span></div><div class='line' id='LC272'><br/></div><div class='line' id='LC273'>	<span class="kd">function</span> <span class="nx">build</span><span class="p">(</span> <span class="nx">tmplItem</span><span class="p">,</span> <span class="nx">nested</span><span class="p">,</span> <span class="nx">content</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC274'>		<span class="c1">// Convert hierarchical content into flat string array</span></div><div class='line' id='LC275'>		<span class="c1">// and finally return array of fragments ready for DOM insertion</span></div><div class='line' id='LC276'>		<span class="kd">var</span> <span class="nx">frag</span><span class="p">,</span> <span class="nx">ret</span> <span class="o">=</span> <span class="nx">content</span> <span class="o">?</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">map</span><span class="p">(</span> <span class="nx">content</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">item</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC277'>			<span class="k">return</span> <span class="p">(</span><span class="k">typeof</span> <span class="nx">item</span> <span class="o">===</span> <span class="s2">&quot;string&quot;</span><span class="p">)</span> <span class="o">?</span></div><div class='line' id='LC278'>				<span class="c1">// Insert template item annotations, to be converted to jQuery.data( &quot;tmplItem&quot; ) when elems are inserted into DOM.</span></div><div class='line' id='LC279'>				<span class="p">(</span><span class="nx">tmplItem</span><span class="p">.</span><span class="nx">key</span> <span class="o">?</span> <span class="nx">item</span><span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/(&lt;\w+)(?=[\s&gt;])(?![^&gt;]*_tmplitem)([^&gt;]*)/g</span><span class="p">,</span> <span class="s2">&quot;$1 &quot;</span> <span class="o">+</span> <span class="nx">tmplItmAtt</span> <span class="o">+</span> <span class="s2">&quot;=\&quot;&quot;</span> <span class="o">+</span> <span class="nx">tmplItem</span><span class="p">.</span><span class="nx">key</span> <span class="o">+</span> <span class="s2">&quot;\&quot; $2&quot;</span> <span class="p">)</span> <span class="o">:</span> <span class="nx">item</span><span class="p">)</span> <span class="o">:</span></div><div class='line' id='LC280'>				<span class="c1">// This is a child template item. Build nested template.</span></div><div class='line' id='LC281'>				<span class="nx">build</span><span class="p">(</span> <span class="nx">item</span><span class="p">,</span> <span class="nx">tmplItem</span><span class="p">,</span> <span class="nx">item</span><span class="p">.</span><span class="nx">_ctnt</span> <span class="p">);</span></div><div class='line' id='LC282'>		<span class="p">})</span> <span class="o">:</span></div><div class='line' id='LC283'>		<span class="c1">// If content is not defined, insert tmplItem directly. Not a template item. May be a string, or a string array, e.g. from {{html $item.html()}}.</span></div><div class='line' id='LC284'>		<span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC285'>		<span class="k">if</span> <span class="p">(</span> <span class="nx">nested</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC286'>			<span class="k">return</span> <span class="nx">ret</span><span class="p">;</span></div><div class='line' id='LC287'>		<span class="p">}</span></div><div class='line' id='LC288'><br/></div><div class='line' id='LC289'>		<span class="c1">// top-level template</span></div><div class='line' id='LC290'>		<span class="nx">ret</span> <span class="o">=</span> <span class="nx">ret</span><span class="p">.</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC291'><br/></div><div class='line' id='LC292'>		<span class="c1">// Support templates which have initial or final text nodes, or consist only of text</span></div><div class='line' id='LC293'>		<span class="c1">// Also support HTML entities within the HTML markup.</span></div><div class='line' id='LC294'>		<span class="nx">ret</span><span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/^\s*([^&lt;\s][^&lt;]*)?(&lt;[\w\W]+&gt;)([^&gt;]*[^&gt;\s])?\s*$/</span><span class="p">,</span> <span class="kd">function</span><span class="p">(</span> <span class="nx">all</span><span class="p">,</span> <span class="nx">before</span><span class="p">,</span> <span class="nx">middle</span><span class="p">,</span> <span class="nx">after</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC295'>			<span class="nx">frag</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">middle</span> <span class="p">).</span><span class="nx">get</span><span class="p">();</span></div><div class='line' id='LC296'><br/></div><div class='line' id='LC297'>			<span class="nx">storeTmplItems</span><span class="p">(</span> <span class="nx">frag</span> <span class="p">);</span></div><div class='line' id='LC298'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">before</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC299'>				<span class="nx">frag</span> <span class="o">=</span> <span class="nx">unencode</span><span class="p">(</span> <span class="nx">before</span> <span class="p">).</span><span class="nx">concat</span><span class="p">(</span><span class="nx">frag</span><span class="p">);</span></div><div class='line' id='LC300'>			<span class="p">}</span></div><div class='line' id='LC301'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">after</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC302'>				<span class="nx">frag</span> <span class="o">=</span> <span class="nx">frag</span><span class="p">.</span><span class="nx">concat</span><span class="p">(</span><span class="nx">unencode</span><span class="p">(</span> <span class="nx">after</span> <span class="p">));</span></div><div class='line' id='LC303'>			<span class="p">}</span></div><div class='line' id='LC304'>		<span class="p">});</span></div><div class='line' id='LC305'>		<span class="k">return</span> <span class="nx">frag</span> <span class="o">?</span> <span class="nx">frag</span> <span class="o">:</span> <span class="nx">unencode</span><span class="p">(</span> <span class="nx">ret</span> <span class="p">);</span></div><div class='line' id='LC306'>	<span class="p">}</span></div><div class='line' id='LC307'><br/></div><div class='line' id='LC308'>	<span class="kd">function</span> <span class="nx">unencode</span><span class="p">(</span> <span class="nx">text</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC309'>		<span class="c1">// Use createElement, since createTextNode will not render HTML entities correctly</span></div><div class='line' id='LC310'>		<span class="kd">var</span> <span class="nx">el</span> <span class="o">=</span> <span class="nb">document</span><span class="p">.</span><span class="nx">createElement</span><span class="p">(</span> <span class="s2">&quot;div&quot;</span> <span class="p">);</span></div><div class='line' id='LC311'>		<span class="nx">el</span><span class="p">.</span><span class="nx">innerHTML</span> <span class="o">=</span> <span class="nx">text</span><span class="p">;</span></div><div class='line' id='LC312'>		<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">makeArray</span><span class="p">(</span><span class="nx">el</span><span class="p">.</span><span class="nx">childNodes</span><span class="p">);</span></div><div class='line' id='LC313'>	<span class="p">}</span></div><div class='line' id='LC314'><br/></div><div class='line' id='LC315'>	<span class="c1">// Generate a reusable function that will serve to render a template against data</span></div><div class='line' id='LC316'>	<span class="kd">function</span> <span class="nx">buildTmplFn</span><span class="p">(</span> <span class="nx">markup</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC317'>		<span class="k">return</span> <span class="k">new</span> <span class="nb">Function</span><span class="p">(</span><span class="s2">&quot;jQuery&quot;</span><span class="p">,</span><span class="s2">&quot;$item&quot;</span><span class="p">,</span></div><div class='line' id='LC318'>			<span class="c1">// Use the variable __ to hold a string array while building the compiled template. (See https://github.com/jquery/jquery-tmpl/issues#issue/10).</span></div><div class='line' id='LC319'>			<span class="s2">&quot;var $=jQuery,call,__=[],$data=$item.data;&quot;</span> <span class="o">+</span></div><div class='line' id='LC320'><br/></div><div class='line' id='LC321'>			<span class="c1">// Introduce the data as local variables using with(){}</span></div><div class='line' id='LC322'>			<span class="s2">&quot;with($data){__.push(&#39;&quot;</span> <span class="o">+</span></div><div class='line' id='LC323'><br/></div><div class='line' id='LC324'>			<span class="c1">// Convert the template into pure JavaScript</span></div><div class='line' id='LC325'>			<span class="nx">jQuery</span><span class="p">.</span><span class="nx">trim</span><span class="p">(</span><span class="nx">markup</span><span class="p">)</span></div><div class='line' id='LC326'>				<span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/([\\&#39;])/g</span><span class="p">,</span> <span class="s2">&quot;\\$1&quot;</span> <span class="p">)</span></div><div class='line' id='LC327'>				<span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/[\r\t\n]/g</span><span class="p">,</span> <span class="s2">&quot; &quot;</span> <span class="p">)</span></div><div class='line' id='LC328'>				<span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/\$\{([^\}]*)\}/g</span><span class="p">,</span> <span class="s2">&quot;{{= $1}}&quot;</span> <span class="p">)</span></div><div class='line' id='LC329'>				<span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/\{\{(\/?)(\w+|.)(?:\(((?:[^\}]|\}(?!\}))*?)?\))?(?:\s+(.*?)?)?(\(((?:[^\}]|\}(?!\}))*?)\))?\s*\}\}/g</span><span class="p">,</span></div><div class='line' id='LC330'>				<span class="kd">function</span><span class="p">(</span> <span class="nx">all</span><span class="p">,</span> <span class="nx">slash</span><span class="p">,</span> <span class="nx">type</span><span class="p">,</span> <span class="nx">fnargs</span><span class="p">,</span> <span class="nx">target</span><span class="p">,</span> <span class="nx">parens</span><span class="p">,</span> <span class="nx">args</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC331'>					<span class="kd">var</span> <span class="nx">tag</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">.</span><span class="nx">tag</span><span class="p">[</span> <span class="nx">type</span> <span class="p">],</span> <span class="nx">def</span><span class="p">,</span> <span class="nx">expr</span><span class="p">,</span> <span class="nx">exprAutoFnDetect</span><span class="p">;</span></div><div class='line' id='LC332'>					<span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="nx">tag</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC333'>						<span class="k">throw</span> <span class="s2">&quot;Unknown template tag: &quot;</span> <span class="o">+</span> <span class="nx">type</span><span class="p">;</span></div><div class='line' id='LC334'>					<span class="p">}</span></div><div class='line' id='LC335'>					<span class="nx">def</span> <span class="o">=</span> <span class="nx">tag</span><span class="p">.</span><span class="nx">_default</span> <span class="o">||</span> <span class="p">[];</span></div><div class='line' id='LC336'>					<span class="k">if</span> <span class="p">(</span> <span class="nx">parens</span> <span class="o">&amp;&amp;</span> <span class="o">!</span><span class="sr">/\w$/</span><span class="p">.</span><span class="nx">test</span><span class="p">(</span><span class="nx">target</span><span class="p">))</span> <span class="p">{</span></div><div class='line' id='LC337'>						<span class="nx">target</span> <span class="o">+=</span> <span class="nx">parens</span><span class="p">;</span></div><div class='line' id='LC338'>						<span class="nx">parens</span> <span class="o">=</span> <span class="s2">&quot;&quot;</span><span class="p">;</span></div><div class='line' id='LC339'>					<span class="p">}</span></div><div class='line' id='LC340'>					<span class="k">if</span> <span class="p">(</span> <span class="nx">target</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC341'>						<span class="nx">target</span> <span class="o">=</span> <span class="nx">unescape</span><span class="p">(</span> <span class="nx">target</span> <span class="p">);</span></div><div class='line' id='LC342'>						<span class="nx">args</span> <span class="o">=</span> <span class="nx">args</span> <span class="o">?</span> <span class="p">(</span><span class="s2">&quot;,&quot;</span> <span class="o">+</span> <span class="nx">unescape</span><span class="p">(</span> <span class="nx">args</span> <span class="p">)</span> <span class="o">+</span> <span class="s2">&quot;)&quot;</span><span class="p">)</span> <span class="o">:</span> <span class="p">(</span><span class="nx">parens</span> <span class="o">?</span> <span class="s2">&quot;)&quot;</span> <span class="o">:</span> <span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC343'>						<span class="c1">// Support for target being things like a.toLowerCase();</span></div><div class='line' id='LC344'>						<span class="c1">// In that case don&#39;t call with template item as &#39;this&#39; pointer. Just evaluate...</span></div><div class='line' id='LC345'>						<span class="nx">expr</span> <span class="o">=</span> <span class="nx">parens</span> <span class="o">?</span> <span class="p">(</span><span class="nx">target</span><span class="p">.</span><span class="nx">indexOf</span><span class="p">(</span><span class="s2">&quot;.&quot;</span><span class="p">)</span> <span class="o">&gt;</span> <span class="o">-</span><span class="mi">1</span> <span class="o">?</span> <span class="nx">target</span> <span class="o">+</span> <span class="nx">unescape</span><span class="p">(</span> <span class="nx">parens</span> <span class="p">)</span> <span class="o">:</span> <span class="p">(</span><span class="s2">&quot;(&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;).call($item&quot;</span> <span class="o">+</span> <span class="nx">args</span><span class="p">))</span> <span class="o">:</span> <span class="nx">target</span><span class="p">;</span></div><div class='line' id='LC346'>						<span class="nx">exprAutoFnDetect</span> <span class="o">=</span> <span class="nx">parens</span> <span class="o">?</span> <span class="nx">expr</span> <span class="o">:</span> <span class="s2">&quot;(typeof(&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;)===&#39;function&#39;?(&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;).call($item):(&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;))&quot;</span><span class="p">;</span></div><div class='line' id='LC347'>					<span class="p">}</span> <span class="k">else</span> <span class="p">{</span></div><div class='line' id='LC348'>						<span class="nx">exprAutoFnDetect</span> <span class="o">=</span> <span class="nx">expr</span> <span class="o">=</span> <span class="nx">def</span><span class="p">.</span><span class="nx">$1</span> <span class="o">||</span> <span class="s2">&quot;null&quot;</span><span class="p">;</span></div><div class='line' id='LC349'>					<span class="p">}</span></div><div class='line' id='LC350'>					<span class="nx">fnargs</span> <span class="o">=</span> <span class="nx">unescape</span><span class="p">(</span> <span class="nx">fnargs</span> <span class="p">);</span></div><div class='line' id='LC351'>					<span class="k">return</span> <span class="s2">&quot;&#39;);&quot;</span> <span class="o">+</span></div><div class='line' id='LC352'>						<span class="nx">tag</span><span class="p">[</span> <span class="nx">slash</span> <span class="o">?</span> <span class="s2">&quot;close&quot;</span> <span class="o">:</span> <span class="s2">&quot;open&quot;</span> <span class="p">]</span></div><div class='line' id='LC353'>							<span class="p">.</span><span class="nx">split</span><span class="p">(</span> <span class="s2">&quot;$notnull_1&quot;</span> <span class="p">).</span><span class="nx">join</span><span class="p">(</span> <span class="nx">target</span> <span class="o">?</span> <span class="s2">&quot;typeof(&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;)!==&#39;undefined&#39; &amp;&amp; (&quot;</span> <span class="o">+</span> <span class="nx">target</span> <span class="o">+</span> <span class="s2">&quot;)!=null&quot;</span> <span class="o">:</span> <span class="s2">&quot;true&quot;</span> <span class="p">)</span></div><div class='line' id='LC354'>							<span class="p">.</span><span class="nx">split</span><span class="p">(</span> <span class="s2">&quot;$1a&quot;</span> <span class="p">).</span><span class="nx">join</span><span class="p">(</span> <span class="nx">exprAutoFnDetect</span> <span class="p">)</span></div><div class='line' id='LC355'>							<span class="p">.</span><span class="nx">split</span><span class="p">(</span> <span class="s2">&quot;$1&quot;</span> <span class="p">).</span><span class="nx">join</span><span class="p">(</span> <span class="nx">expr</span> <span class="p">)</span></div><div class='line' id='LC356'>							<span class="p">.</span><span class="nx">split</span><span class="p">(</span> <span class="s2">&quot;$2&quot;</span> <span class="p">).</span><span class="nx">join</span><span class="p">(</span> <span class="nx">fnargs</span> <span class="o">||</span> <span class="nx">def</span><span class="p">.</span><span class="nx">$2</span> <span class="o">||</span> <span class="s2">&quot;&quot;</span> <span class="p">)</span> <span class="o">+</span></div><div class='line' id='LC357'>						<span class="s2">&quot;__.push(&#39;&quot;</span><span class="p">;</span></div><div class='line' id='LC358'>				<span class="p">})</span> <span class="o">+</span></div><div class='line' id='LC359'>			<span class="s2">&quot;&#39;);}return __;&quot;</span></div><div class='line' id='LC360'>		<span class="p">);</span></div><div class='line' id='LC361'>	<span class="p">}</span></div><div class='line' id='LC362'>	<span class="kd">function</span> <span class="nx">updateWrapped</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">wrapped</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC363'>		<span class="c1">// Build the wrapped content.</span></div><div class='line' id='LC364'>		<span class="nx">options</span><span class="p">.</span><span class="nx">_wrap</span> <span class="o">=</span> <span class="nx">build</span><span class="p">(</span> <span class="nx">options</span><span class="p">,</span> <span class="kc">true</span><span class="p">,</span></div><div class='line' id='LC365'>			<span class="c1">// Suport imperative scenario in which options.wrapped can be set to a selector or an HTML string.</span></div><div class='line' id='LC366'>			<span class="nx">jQuery</span><span class="p">.</span><span class="nx">isArray</span><span class="p">(</span> <span class="nx">wrapped</span> <span class="p">)</span> <span class="o">?</span> <span class="nx">wrapped</span> <span class="o">:</span> <span class="p">[</span><span class="nx">htmlExpr</span><span class="p">.</span><span class="nx">test</span><span class="p">(</span> <span class="nx">wrapped</span> <span class="p">)</span> <span class="o">?</span> <span class="nx">wrapped</span> <span class="o">:</span> <span class="nx">jQuery</span><span class="p">(</span> <span class="nx">wrapped</span> <span class="p">).</span><span class="nx">html</span><span class="p">()]</span></div><div class='line' id='LC367'>		<span class="p">).</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&quot;</span><span class="p">);</span></div><div class='line' id='LC368'>	<span class="p">}</span></div><div class='line' id='LC369'><br/></div><div class='line' id='LC370'>	<span class="kd">function</span> <span class="nx">unescape</span><span class="p">(</span> <span class="nx">args</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC371'>		<span class="k">return</span> <span class="nx">args</span> <span class="o">?</span> <span class="nx">args</span><span class="p">.</span><span class="nx">replace</span><span class="p">(</span> <span class="sr">/\\&#39;/g</span><span class="p">,</span> <span class="s2">&quot;&#39;&quot;</span><span class="p">).</span><span class="nx">replace</span><span class="p">(</span><span class="sr">/\\\\/g</span><span class="p">,</span> <span class="s2">&quot;\\&quot;</span> <span class="p">)</span> <span class="o">:</span> <span class="kc">null</span><span class="p">;</span></div><div class='line' id='LC372'>	<span class="p">}</span></div><div class='line' id='LC373'>	<span class="kd">function</span> <span class="nx">outerHtml</span><span class="p">(</span> <span class="nx">elem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC374'>		<span class="kd">var</span> <span class="nx">div</span> <span class="o">=</span> <span class="nb">document</span><span class="p">.</span><span class="nx">createElement</span><span class="p">(</span><span class="s2">&quot;div&quot;</span><span class="p">);</span></div><div class='line' id='LC375'>		<span class="nx">div</span><span class="p">.</span><span class="nx">appendChild</span><span class="p">(</span> <span class="nx">elem</span><span class="p">.</span><span class="nx">cloneNode</span><span class="p">(</span><span class="kc">true</span><span class="p">)</span> <span class="p">);</span></div><div class='line' id='LC376'>		<span class="k">return</span> <span class="nx">div</span><span class="p">.</span><span class="nx">innerHTML</span><span class="p">;</span></div><div class='line' id='LC377'>	<span class="p">}</span></div><div class='line' id='LC378'><br/></div><div class='line' id='LC379'>	<span class="c1">// Store template items in jQuery.data(), ensuring a unique tmplItem data data structure for each rendered template instance.</span></div><div class='line' id='LC380'>	<span class="kd">function</span> <span class="nx">storeTmplItems</span><span class="p">(</span> <span class="nx">content</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC381'>		<span class="kd">var</span> <span class="nx">keySuffix</span> <span class="o">=</span> <span class="s2">&quot;_&quot;</span> <span class="o">+</span> <span class="nx">cloneIndex</span><span class="p">,</span> <span class="nx">elem</span><span class="p">,</span> <span class="nx">elems</span><span class="p">,</span> <span class="nx">newClonedItems</span> <span class="o">=</span> <span class="p">{},</span> <span class="nx">i</span><span class="p">,</span> <span class="nx">l</span><span class="p">,</span> <span class="nx">m</span><span class="p">;</span></div><div class='line' id='LC382'>		<span class="k">for</span> <span class="p">(</span> <span class="nx">i</span> <span class="o">=</span> <span class="mi">0</span><span class="p">,</span> <span class="nx">l</span> <span class="o">=</span> <span class="nx">content</span><span class="p">.</span><span class="nx">length</span><span class="p">;</span> <span class="nx">i</span> <span class="o">&lt;</span> <span class="nx">l</span><span class="p">;</span> <span class="nx">i</span><span class="o">++</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC383'>			<span class="k">if</span> <span class="p">(</span> <span class="p">(</span><span class="nx">elem</span> <span class="o">=</span> <span class="nx">content</span><span class="p">[</span><span class="nx">i</span><span class="p">]).</span><span class="nx">nodeType</span> <span class="o">!==</span> <span class="mi">1</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC384'>				<span class="k">continue</span><span class="p">;</span></div><div class='line' id='LC385'>			<span class="p">}</span></div><div class='line' id='LC386'>			<span class="nx">elems</span> <span class="o">=</span> <span class="nx">elem</span><span class="p">.</span><span class="nx">getElementsByTagName</span><span class="p">(</span><span class="s2">&quot;*&quot;</span><span class="p">);</span></div><div class='line' id='LC387'>			<span class="k">for</span> <span class="p">(</span> <span class="nx">m</span> <span class="o">=</span> <span class="nx">elems</span><span class="p">.</span><span class="nx">length</span> <span class="o">-</span> <span class="mi">1</span><span class="p">;</span> <span class="nx">m</span> <span class="o">&gt;=</span> <span class="mi">0</span><span class="p">;</span> <span class="nx">m</span><span class="o">--</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC388'>				<span class="nx">processItemKey</span><span class="p">(</span> <span class="nx">elems</span><span class="p">[</span><span class="nx">m</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC389'>			<span class="p">}</span></div><div class='line' id='LC390'>			<span class="nx">processItemKey</span><span class="p">(</span> <span class="nx">elem</span> <span class="p">);</span></div><div class='line' id='LC391'>		<span class="p">}</span></div><div class='line' id='LC392'>		<span class="kd">function</span> <span class="nx">processItemKey</span><span class="p">(</span> <span class="nx">el</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC393'>			<span class="kd">var</span> <span class="nx">pntKey</span><span class="p">,</span> <span class="nx">pntNode</span> <span class="o">=</span> <span class="nx">el</span><span class="p">,</span> <span class="nx">pntItem</span><span class="p">,</span> <span class="nx">tmplItem</span><span class="p">,</span> <span class="nx">key</span><span class="p">;</span></div><div class='line' id='LC394'>			<span class="c1">// Ensure that each rendered template inserted into the DOM has its own template item,</span></div><div class='line' id='LC395'>			<span class="k">if</span> <span class="p">(</span> <span class="p">(</span><span class="nx">key</span> <span class="o">=</span> <span class="nx">el</span><span class="p">.</span><span class="nx">getAttribute</span><span class="p">(</span> <span class="nx">tmplItmAtt</span> <span class="p">)))</span> <span class="p">{</span></div><div class='line' id='LC396'>				<span class="k">while</span> <span class="p">(</span> <span class="nx">pntNode</span><span class="p">.</span><span class="nx">parentNode</span> <span class="o">&amp;&amp;</span> <span class="p">(</span><span class="nx">pntNode</span> <span class="o">=</span> <span class="nx">pntNode</span><span class="p">.</span><span class="nx">parentNode</span><span class="p">).</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">1</span> <span class="o">&amp;&amp;</span> <span class="o">!</span><span class="p">(</span><span class="nx">pntKey</span> <span class="o">=</span> <span class="nx">pntNode</span><span class="p">.</span><span class="nx">getAttribute</span><span class="p">(</span> <span class="nx">tmplItmAtt</span> <span class="p">)))</span> <span class="p">{</span> <span class="p">}</span></div><div class='line' id='LC397'>				<span class="k">if</span> <span class="p">(</span> <span class="nx">pntKey</span> <span class="o">!==</span> <span class="nx">key</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC398'>					<span class="c1">// The next ancestor with a _tmplitem expando is on a different key than this one.</span></div><div class='line' id='LC399'>					<span class="c1">// So this is a top-level element within this template item</span></div><div class='line' id='LC400'>					<span class="c1">// Set pntNode to the key of the parentNode, or to 0 if pntNode.parentNode is null, or pntNode is a fragment.</span></div><div class='line' id='LC401'>					<span class="nx">pntNode</span> <span class="o">=</span> <span class="nx">pntNode</span><span class="p">.</span><span class="nx">parentNode</span> <span class="o">?</span> <span class="p">(</span><span class="nx">pntNode</span><span class="p">.</span><span class="nx">nodeType</span> <span class="o">===</span> <span class="mi">11</span> <span class="o">?</span> <span class="mi">0</span> <span class="o">:</span> <span class="p">(</span><span class="nx">pntNode</span><span class="p">.</span><span class="nx">getAttribute</span><span class="p">(</span> <span class="nx">tmplItmAtt</span> <span class="p">)</span> <span class="o">||</span> <span class="mi">0</span><span class="p">))</span> <span class="o">:</span> <span class="mi">0</span><span class="p">;</span></div><div class='line' id='LC402'>					<span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="p">(</span><span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">key</span><span class="p">])</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC403'>						<span class="c1">// The item is for wrapped content, and was copied from the temporary parent wrappedItem.</span></div><div class='line' id='LC404'>						<span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">wrappedItems</span><span class="p">[</span><span class="nx">key</span><span class="p">];</span></div><div class='line' id='LC405'>						<span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">newTmplItem</span><span class="p">(</span> <span class="nx">tmplItem</span><span class="p">,</span> <span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">pntNode</span><span class="p">]</span><span class="o">||</span><span class="nx">wrappedItems</span><span class="p">[</span><span class="nx">pntNode</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC406'>						<span class="nx">tmplItem</span><span class="p">.</span><span class="nx">key</span> <span class="o">=</span> <span class="o">++</span><span class="nx">itemKey</span><span class="p">;</span></div><div class='line' id='LC407'>						<span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">itemKey</span><span class="p">]</span> <span class="o">=</span> <span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC408'>					<span class="p">}</span></div><div class='line' id='LC409'>					<span class="k">if</span> <span class="p">(</span> <span class="nx">cloneIndex</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC410'>						<span class="nx">cloneTmplItem</span><span class="p">(</span> <span class="nx">key</span> <span class="p">);</span></div><div class='line' id='LC411'>					<span class="p">}</span></div><div class='line' id='LC412'>				<span class="p">}</span></div><div class='line' id='LC413'>				<span class="nx">el</span><span class="p">.</span><span class="nx">removeAttribute</span><span class="p">(</span> <span class="nx">tmplItmAtt</span> <span class="p">);</span></div><div class='line' id='LC414'>			<span class="p">}</span> <span class="k">else</span> <span class="k">if</span> <span class="p">(</span> <span class="nx">cloneIndex</span> <span class="o">&amp;&amp;</span> <span class="p">(</span><span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">el</span><span class="p">,</span> <span class="s2">&quot;tmplItem&quot;</span> <span class="p">))</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC415'>				<span class="c1">// This was a rendered element, cloned during append or appendTo etc.</span></div><div class='line' id='LC416'>				<span class="c1">// TmplItem stored in jQuery data has already been cloned in cloneCopyEvent. We must replace it with a fresh cloned tmplItem.</span></div><div class='line' id='LC417'>				<span class="nx">cloneTmplItem</span><span class="p">(</span> <span class="nx">tmplItem</span><span class="p">.</span><span class="nx">key</span> <span class="p">);</span></div><div class='line' id='LC418'>				<span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">tmplItem</span><span class="p">.</span><span class="nx">key</span><span class="p">]</span> <span class="o">=</span> <span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC419'>				<span class="nx">pntNode</span> <span class="o">=</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">el</span><span class="p">.</span><span class="nx">parentNode</span><span class="p">,</span> <span class="s2">&quot;tmplItem&quot;</span> <span class="p">);</span></div><div class='line' id='LC420'>				<span class="nx">pntNode</span> <span class="o">=</span> <span class="nx">pntNode</span> <span class="o">?</span> <span class="nx">pntNode</span><span class="p">.</span><span class="nx">key</span> <span class="o">:</span> <span class="mi">0</span><span class="p">;</span></div><div class='line' id='LC421'>			<span class="p">}</span></div><div class='line' id='LC422'>			<span class="k">if</span> <span class="p">(</span> <span class="nx">tmplItem</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC423'>				<span class="nx">pntItem</span> <span class="o">=</span> <span class="nx">tmplItem</span><span class="p">;</span></div><div class='line' id='LC424'>				<span class="c1">// Find the template item of the parent element.</span></div><div class='line' id='LC425'>				<span class="c1">// (Using !=, not !==, since pntItem.key is number, and pntNode may be a string)</span></div><div class='line' id='LC426'>				<span class="k">while</span> <span class="p">(</span> <span class="nx">pntItem</span> <span class="o">&amp;&amp;</span> <span class="nx">pntItem</span><span class="p">.</span><span class="nx">key</span> <span class="o">!=</span> <span class="nx">pntNode</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC427'>					<span class="c1">// Add this element as a top-level node for this rendered template item, as well as for any</span></div><div class='line' id='LC428'>					<span class="c1">// ancestor items between this item and the item of its parent element</span></div><div class='line' id='LC429'>					<span class="nx">pntItem</span><span class="p">.</span><span class="nx">nodes</span><span class="p">.</span><span class="nx">push</span><span class="p">(</span> <span class="nx">el</span> <span class="p">);</span></div><div class='line' id='LC430'>					<span class="nx">pntItem</span> <span class="o">=</span> <span class="nx">pntItem</span><span class="p">.</span><span class="nx">parent</span><span class="p">;</span></div><div class='line' id='LC431'>				<span class="p">}</span></div><div class='line' id='LC432'>				<span class="c1">// Delete content built during rendering - reduce API surface area and memory use, and avoid exposing of stale data after rendering...</span></div><div class='line' id='LC433'>				<span class="k">delete</span> <span class="nx">tmplItem</span><span class="p">.</span><span class="nx">_ctnt</span><span class="p">;</span></div><div class='line' id='LC434'>				<span class="k">delete</span> <span class="nx">tmplItem</span><span class="p">.</span><span class="nx">_wrap</span><span class="p">;</span></div><div class='line' id='LC435'>				<span class="c1">// Store template item as jQuery data on the element</span></div><div class='line' id='LC436'>				<span class="nx">jQuery</span><span class="p">.</span><span class="nx">data</span><span class="p">(</span> <span class="nx">el</span><span class="p">,</span> <span class="s2">&quot;tmplItem&quot;</span><span class="p">,</span> <span class="nx">tmplItem</span> <span class="p">);</span></div><div class='line' id='LC437'>			<span class="p">}</span></div><div class='line' id='LC438'>			<span class="kd">function</span> <span class="nx">cloneTmplItem</span><span class="p">(</span> <span class="nx">key</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC439'>				<span class="nx">key</span> <span class="o">=</span> <span class="nx">key</span> <span class="o">+</span> <span class="nx">keySuffix</span><span class="p">;</span></div><div class='line' id='LC440'>				<span class="nx">tmplItem</span> <span class="o">=</span> <span class="nx">newClonedItems</span><span class="p">[</span><span class="nx">key</span><span class="p">]</span> <span class="o">=</span></div><div class='line' id='LC441'>					<span class="p">(</span><span class="nx">newClonedItems</span><span class="p">[</span><span class="nx">key</span><span class="p">]</span> <span class="o">||</span> <span class="nx">newTmplItem</span><span class="p">(</span> <span class="nx">tmplItem</span><span class="p">,</span> <span class="nx">newTmplItems</span><span class="p">[</span><span class="nx">tmplItem</span><span class="p">.</span><span class="nx">parent</span><span class="p">.</span><span class="nx">key</span> <span class="o">+</span> <span class="nx">keySuffix</span><span class="p">]</span> <span class="o">||</span> <span class="nx">tmplItem</span><span class="p">.</span><span class="nx">parent</span> <span class="p">));</span></div><div class='line' id='LC442'>			<span class="p">}</span></div><div class='line' id='LC443'>		<span class="p">}</span></div><div class='line' id='LC444'>	<span class="p">}</span></div><div class='line' id='LC445'><br/></div><div class='line' id='LC446'>	<span class="c1">//---- Helper functions for template item ----</span></div><div class='line' id='LC447'><br/></div><div class='line' id='LC448'>	<span class="kd">function</span> <span class="nx">tiCalls</span><span class="p">(</span> <span class="nx">content</span><span class="p">,</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC449'>		<span class="k">if</span> <span class="p">(</span> <span class="o">!</span><span class="nx">content</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC450'>			<span class="k">return</span> <span class="nx">stack</span><span class="p">.</span><span class="nx">pop</span><span class="p">();</span></div><div class='line' id='LC451'>		<span class="p">}</span></div><div class='line' id='LC452'>		<span class="nx">stack</span><span class="p">.</span><span class="nx">push</span><span class="p">({</span> <span class="nx">_</span><span class="o">:</span> <span class="nx">content</span><span class="p">,</span> <span class="nx">tmpl</span><span class="o">:</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">item</span><span class="o">:</span><span class="k">this</span><span class="p">,</span> <span class="nx">data</span><span class="o">:</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="o">:</span> <span class="nx">options</span> <span class="p">});</span></div><div class='line' id='LC453'>	<span class="p">}</span></div><div class='line' id='LC454'><br/></div><div class='line' id='LC455'>	<span class="kd">function</span> <span class="nx">tiNest</span><span class="p">(</span> <span class="nx">tmpl</span><span class="p">,</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC456'>		<span class="c1">// nested template, using {{tmpl}} tag</span></div><div class='line' id='LC457'>		<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="nx">tmpl</span> <span class="p">),</span> <span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="k">this</span> <span class="p">);</span></div><div class='line' id='LC458'>	<span class="p">}</span></div><div class='line' id='LC459'><br/></div><div class='line' id='LC460'>	<span class="kd">function</span> <span class="nx">tiWrap</span><span class="p">(</span> <span class="nx">call</span><span class="p">,</span> <span class="nx">wrapped</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC461'>		<span class="c1">// nested template, using {{wrap}} tag</span></div><div class='line' id='LC462'>		<span class="kd">var</span> <span class="nx">options</span> <span class="o">=</span> <span class="nx">call</span><span class="p">.</span><span class="nx">options</span> <span class="o">||</span> <span class="p">{};</span></div><div class='line' id='LC463'>		<span class="nx">options</span><span class="p">.</span><span class="nx">wrapped</span> <span class="o">=</span> <span class="nx">wrapped</span><span class="p">;</span></div><div class='line' id='LC464'>		<span class="c1">// Apply the template, which may incorporate wrapped content,</span></div><div class='line' id='LC465'>		<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">template</span><span class="p">(</span> <span class="nx">call</span><span class="p">.</span><span class="nx">tmpl</span> <span class="p">),</span> <span class="nx">call</span><span class="p">.</span><span class="nx">data</span><span class="p">,</span> <span class="nx">options</span><span class="p">,</span> <span class="nx">call</span><span class="p">.</span><span class="nx">item</span> <span class="p">);</span></div><div class='line' id='LC466'>	<span class="p">}</span></div><div class='line' id='LC467'><br/></div><div class='line' id='LC468'>	<span class="kd">function</span> <span class="nx">tiHtml</span><span class="p">(</span> <span class="nx">filter</span><span class="p">,</span> <span class="nx">textOnly</span> <span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC469'>		<span class="kd">var</span> <span class="nx">wrapped</span> <span class="o">=</span> <span class="k">this</span><span class="p">.</span><span class="nx">_wrap</span><span class="p">;</span></div><div class='line' id='LC470'>		<span class="k">return</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">map</span><span class="p">(</span></div><div class='line' id='LC471'>			<span class="nx">jQuery</span><span class="p">(</span> <span class="nx">jQuery</span><span class="p">.</span><span class="nx">isArray</span><span class="p">(</span> <span class="nx">wrapped</span> <span class="p">)</span> <span class="o">?</span> <span class="nx">wrapped</span><span class="p">.</span><span class="nx">join</span><span class="p">(</span><span class="s2">&quot;&quot;</span><span class="p">)</span> <span class="o">:</span> <span class="nx">wrapped</span> <span class="p">).</span><span class="nx">filter</span><span class="p">(</span> <span class="nx">filter</span> <span class="o">||</span> <span class="s2">&quot;*&quot;</span> <span class="p">),</span></div><div class='line' id='LC472'>			<span class="kd">function</span><span class="p">(</span><span class="nx">e</span><span class="p">)</span> <span class="p">{</span></div><div class='line' id='LC473'>				<span class="k">return</span> <span class="nx">textOnly</span> <span class="o">?</span></div><div class='line' id='LC474'>					<span class="nx">e</span><span class="p">.</span><span class="nx">innerText</span> <span class="o">||</span> <span class="nx">e</span><span class="p">.</span><span class="nx">textContent</span> <span class="o">:</span></div><div class='line' id='LC475'>					<span class="nx">e</span><span class="p">.</span><span class="nx">outerHTML</span> <span class="o">||</span> <span class="nx">outerHtml</span><span class="p">(</span><span class="nx">e</span><span class="p">);</span></div><div class='line' id='LC476'>			<span class="p">});</span></div><div class='line' id='LC477'>	<span class="p">}</span></div><div class='line' id='LC478'><br/></div><div class='line' id='LC479'>	<span class="kd">function</span> <span class="nx">tiUpdate</span><span class="p">()</span> <span class="p">{</span></div><div class='line' id='LC480'>		<span class="kd">var</span> <span class="nx">coll</span> <span class="o">=</span> <span class="k">this</span><span class="p">.</span><span class="nx">nodes</span><span class="p">;</span></div><div class='line' id='LC481'>		<span class="nx">jQuery</span><span class="p">.</span><span class="nx">tmpl</span><span class="p">(</span> <span class="kc">null</span><span class="p">,</span> <span class="kc">null</span><span class="p">,</span> <span class="kc">null</span><span class="p">,</span> <span class="k">this</span><span class="p">).</span><span class="nx">insertBefore</span><span class="p">(</span> <span class="nx">coll</span><span class="p">[</span><span class="mi">0</span><span class="p">]</span> <span class="p">);</span></div><div class='line' id='LC482'>		<span class="nx">jQuery</span><span class="p">(</span> <span class="nx">coll</span> <span class="p">).</span><span class="nx">remove</span><span class="p">();</span></div><div class='line' id='LC483'>	<span class="p">}</span></div><div class='line' id='LC484'><span class="p">})(</span> <span class="nx">jQuery</span> <span class="p">);</span></div><div class='line' id='LC485'><br/></div></pre></div>
          </td>
        </tr>
      </table>
  </div>

          </div>
        </div>
      </div>
    </div>

  </div>

<div class="frame frame-loading" style="display:none;" data-tree-list-url="/jquery/jquery-tmpl/tree-list/04b5af07a579b0928d93cd018cda056097e58180" data-blob-url-prefix="/jquery/jquery-tmpl/blob/04b5af07a579b0928d93cd018cda056097e58180">
  <img src="https://a248.e.akamai.net/assets.github.com/images/modules/ajax/big_spinner_336699.gif?1315928456" height="32" width="32">
</div>

      </div>
      <div class="context-overlay"></div>
    </div>


      <!-- footer -->
      <div id="footer" >
        
  <div class="upper_footer">
     <div class="container clearfix">

       <!--[if IE]><h4 id="blacktocat_ie">GitHub Links</h4><![endif]-->
       <![if !IE]><h4 id="blacktocat">GitHub Links</h4><![endif]>

       <ul class="footer_nav">
         <h4>GitHub</h4>
         <li><a href="https://github.com/about">About</a></li>
         <li><a href="https://github.com/blog">Blog</a></li>
         <li><a href="https://github.com/features">Features</a></li>
         <li><a href="https://github.com/contact">Contact &amp; Support</a></li>
         <li><a href="https://github.com/training">Training</a></li>
         <li><a href="http://enterprise.github.com/">GitHub Enterprise</a></li>
         <li><a href="http://status.github.com/">Site Status</a></li>
       </ul>

       <ul class="footer_nav">
         <h4>Tools</h4>
         <li><a href="http://get.gaug.es/">Gauges: Analyze web traffic</a></li>
         <li><a href="http://speakerdeck.com">Speaker Deck: Presentations</a></li>
         <li><a href="https://gist.github.com">Gist: Code snippets</a></li>
         <li><a href="http://mac.github.com/">GitHub for Mac</a></li>
         <li><a href="http://mobile.github.com/">Issues for iPhone</a></li>
         <li><a href="http://jobs.github.com/">Job Board</a></li>
       </ul>

       <ul class="footer_nav">
         <h4>Extras</h4>
         <li><a href="http://shop.github.com/">GitHub Shop</a></li>
         <li><a href="http://octodex.github.com/">The Octodex</a></li>
       </ul>

       <ul class="footer_nav">
         <h4>Documentation</h4>
         <li><a href="http://help.github.com/">GitHub Help</a></li>
         <li><a href="http://developer.github.com/">Developer API</a></li>
         <li><a href="http://github.github.com/github-flavored-markdown/">GitHub Flavored Markdown</a></li>
         <li><a href="http://pages.github.com/">GitHub Pages</a></li>
       </ul>

     </div><!-- /.site -->
  </div><!-- /.upper_footer -->

<div class="lower_footer">
  <div class="container clearfix">
    <!--[if IE]><div id="legal_ie"><![endif]-->
    <![if !IE]><div id="legal"><![endif]>
      <ul>
          <li><a href="https://github.com/site/terms">Terms of Service</a></li>
          <li><a href="https://github.com/site/privacy">Privacy</a></li>
          <li><a href="https://github.com/security">Security</a></li>
      </ul>

      <p>&copy; 2012 <span id="_rrt" title="0.09206s from fe11.rs.github.com">GitHub</span> Inc. All rights reserved.</p>
    </div><!-- /#legal or /#legal_ie-->

      <div class="sponsor">
        <a href="http://www.rackspace.com" class="logo">
          <img alt="Dedicated Server" height="36" src="https://a248.e.akamai.net/assets.github.com/images/modules/footer/rackspace_logo.png?v2" width="38" />
        </a>
        Powered by the <a href="http://www.rackspace.com ">Dedicated
        Servers</a> and<br/> <a href="http://www.rackspacecloud.com">Cloud
        Computing</a> of Rackspace Hosting<span>&reg;</span>
      </div>
  </div><!-- /.site -->
</div><!-- /.lower_footer -->

      </div><!-- /#footer -->

    

<div id="keyboard_shortcuts_pane" class="instapaper_ignore readability-extra" style="display:none">
  <h2>Keyboard Shortcuts <small><a href="#" class="js-see-all-keyboard-shortcuts">(see all)</a></small></h2>

  <div class="columns threecols">
    <div class="column first">
      <h3>Site wide shortcuts</h3>
      <dl class="keyboard-mappings">
        <dt>s</dt>
        <dd>Focus site search</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>?</dt>
        <dd>Bring up this help dialog</dd>
      </dl>
    </div><!-- /.column.first -->

    <div class="column middle" style='display:none'>
      <h3>Commit list</h3>
      <dl class="keyboard-mappings">
        <dt>j</dt>
        <dd>Move selection down</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>k</dt>
        <dd>Move selection up</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>c <em>or</em> o <em>or</em> enter</dt>
        <dd>Open commit</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>y</dt>
        <dd>Expand URL to its canonical form</dd>
      </dl>
    </div><!-- /.column.first -->

    <div class="column last" style='display:none'>
      <h3>Pull request list</h3>
      <dl class="keyboard-mappings">
        <dt>j</dt>
        <dd>Move selection down</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>k</dt>
        <dd>Move selection up</dd>
      </dl>
      <dl class="keyboard-mappings">
        <dt>o <em>or</em> enter</dt>
        <dd>Open issue</dd>
      </dl>
    </div><!-- /.columns.last -->

  </div><!-- /.columns.equacols -->

  <div style='display:none'>
    <div class="rule"></div>

    <h3>Issues</h3>

    <div class="columns threecols">
      <div class="column first">
        <dl class="keyboard-mappings">
          <dt>j</dt>
          <dd>Move selection down</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>k</dt>
          <dd>Move selection up</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>x</dt>
          <dd>Toggle selection</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>o <em>or</em> enter</dt>
          <dd>Open issue</dd>
        </dl>
      </div><!-- /.column.first -->
      <div class="column middle">
        <dl class="keyboard-mappings">
          <dt>I</dt>
          <dd>Mark selection as read</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>U</dt>
          <dd>Mark selection as unread</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>e</dt>
          <dd>Close selection</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>y</dt>
          <dd>Remove selection from view</dd>
        </dl>
      </div><!-- /.column.middle -->
      <div class="column last">
        <dl class="keyboard-mappings">
          <dt>c</dt>
          <dd>Create issue</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>l</dt>
          <dd>Create label</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>i</dt>
          <dd>Back to inbox</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>u</dt>
          <dd>Back to issues</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>/</dt>
          <dd>Focus issues search</dd>
        </dl>
      </div>
    </div>
  </div>

  <div style='display:none'>
    <div class="rule"></div>

    <h3>Issues Dashboard</h3>

    <div class="columns threecols">
      <div class="column first">
        <dl class="keyboard-mappings">
          <dt>j</dt>
          <dd>Move selection down</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>k</dt>
          <dd>Move selection up</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>o <em>or</em> enter</dt>
          <dd>Open issue</dd>
        </dl>
      </div><!-- /.column.first -->
    </div>
  </div>

  <div style='display:none'>
    <div class="rule"></div>

    <h3>Network Graph</h3>
    <div class="columns equacols">
      <div class="column first">
        <dl class="keyboard-mappings">
          <dt><span class="badmono">←</span> <em>or</em> h</dt>
          <dd>Scroll left</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt><span class="badmono">→</span> <em>or</em> l</dt>
          <dd>Scroll right</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt><span class="badmono">↑</span> <em>or</em> k</dt>
          <dd>Scroll up</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt><span class="badmono">↓</span> <em>or</em> j</dt>
          <dd>Scroll down</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>t</dt>
          <dd>Toggle visibility of head labels</dd>
        </dl>
      </div><!-- /.column.first -->
      <div class="column last">
        <dl class="keyboard-mappings">
          <dt>shift <span class="badmono">←</span> <em>or</em> shift h</dt>
          <dd>Scroll all the way left</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>shift <span class="badmono">→</span> <em>or</em> shift l</dt>
          <dd>Scroll all the way right</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>shift <span class="badmono">↑</span> <em>or</em> shift k</dt>
          <dd>Scroll all the way up</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>shift <span class="badmono">↓</span> <em>or</em> shift j</dt>
          <dd>Scroll all the way down</dd>
        </dl>
      </div><!-- /.column.last -->
    </div>
  </div>

  <div >
    <div class="rule"></div>
    <div class="columns threecols">
      <div class="column first" >
        <h3>Source Code Browsing</h3>
        <dl class="keyboard-mappings">
          <dt>t</dt>
          <dd>Activates the file finder</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>l</dt>
          <dd>Jump to line</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>w</dt>
          <dd>Switch branch/tag</dd>
        </dl>
        <dl class="keyboard-mappings">
          <dt>y</dt>
          <dd>Expand URL to its canonical form</dd>
        </dl>
      </div>
    </div>
  </div>
</div>

    <div id="markdown-help" class="instapaper_ignore readability-extra">
  <h2>Markdown Cheat Sheet</h2>

  <div class="cheatsheet-content">

  <div class="mod">
    <div class="col">
      <h3>Format Text</h3>
      <p>Headers</p>
      <pre>
# This is an &lt;h1&gt; tag
## This is an &lt;h2&gt; tag
###### This is an &lt;h6&gt; tag</pre>
     <p>Text styles</p>
     <pre>
*This text will be italic*
_This will also be italic_
**This text will be bold**
__This will also be bold__

*You **can** combine them*
</pre>
    </div>
    <div class="col">
      <h3>Lists</h3>
      <p>Unordered</p>
      <pre>
* Item 1
* Item 2
  * Item 2a
  * Item 2b</pre>
     <p>Ordered</p>
     <pre>
1. Item 1
2. Item 2
3. Item 3
   * Item 3a
   * Item 3b</pre>
    </div>
    <div class="col">
      <h3>Miscellaneous</h3>
      <p>Images</p>
      <pre>
![GitHub Logo](/images/logo.png)
Format: ![Alt Text](url)
</pre>
     <p>Links</p>
     <pre>
http://github.com - automatic!
[GitHub](http://github.com)</pre>
<p>Blockquotes</p>
     <pre>
As Kanye West said:

> We're living the future so
> the present is our past.
</pre>
    </div>
  </div>
  <div class="rule"></div>

  <h3>Code Examples in Markdown</h3>
  <div class="col">
      <p>Syntax highlighting with <a href="http://github.github.com/github-flavored-markdown/" title="GitHub Flavored Markdown" target="_blank">GFM</a></p>
      <pre>
```javascript
function fancyAlert(arg) {
  if(arg) {
    $.facebox({div:'#foo'})
  }
}
```</pre>
    </div>
    <div class="col">
      <p>Or, indent your code 4 spaces</p>
      <pre>
Here is a Python code example
without syntax highlighting:

    def foo:
      if not bar:
        return true</pre>
    </div>
    <div class="col">
      <p>Inline code for comments</p>
      <pre>
I think you should use an
`&lt;addr&gt;` element here instead.</pre>
    </div>
  </div>

  </div>
</div>


    <div class="ajax-error-message">
      <p><span class="icon"></span> Something went wrong with that request. Please try again. <a href="javascript:;" class="ajax-error-dismiss">Dismiss</a></p>
    </div>

    
    
    
  </body>
</html>

