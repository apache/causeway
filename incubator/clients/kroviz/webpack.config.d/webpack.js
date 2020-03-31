config.resolve.modules.push("../../processedResources/Js/main");
if (!config.devServer && config.output) {
    config.devtool = false
    config.output.filename = "main.bundle.js"
}
if (config.devServer) {
    config.devServer.watchOptions = {
        aggregateTimeout: 300,
        poll: 300
    };
    config.devServer.stats = {
        warnings: false
    };
    config.devServer.clientLogLevel = 'error';
}

class KvWebpackPlugin {
    apply(compiler) {
        const fs = require('fs')
        compiler.hooks.watchRun.tapAsync("KvWebpackPlugin", (compiler, callback) => {
            var runCallback = true;
            for (let item of compiler.removedFiles.values()) {
                if (item == config.entry.main) {
                    if (!fs.existsSync(item)) {
                        fs.watchFile(item, {interval: 50}, (current, previous) => {
                            if (current.ino > 0) {
                                fs.unwatchFile(item);
                                callback();
                            }
                        });
                        runCallback = false;
                    }
                }
            }
            if (runCallback) callback();
        });
    }
};
config.plugins.push(new KvWebpackPlugin())
