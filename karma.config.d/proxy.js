var old = module.exports;

module.exports = function(config) {
    var temp = new Object();
    temp.set = function(c) {
	temp.conf = c;
    };
    var oldConfig = old(temp);
    temp.conf.proxies = {
	'/tmp/_karma_webpack_/': 'http://localhost:8088/'
    }
    config.set(temp.conf);
};