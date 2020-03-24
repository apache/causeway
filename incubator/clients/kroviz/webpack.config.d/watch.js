if (defined.PRODUCTION === false || defined.PRODUCTION === 'false') {
    config.devServer = {
        watchOptions: {
            aggregateTimeout: 5000,
            poll: 500
        }
    }
}
