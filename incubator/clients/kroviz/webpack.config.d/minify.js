if (!config.devServer) {
    ;(function() {
        const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

        config.optimization = {
            minimizer: [
                 new UglifyJSPlugin({
                    uglifyOptions: {
                        compress: {
                            unused: false
                        }
                    }
                 })
            ]
        }
    })();
}
