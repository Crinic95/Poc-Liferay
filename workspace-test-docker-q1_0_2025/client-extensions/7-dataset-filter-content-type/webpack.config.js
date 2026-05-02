const path = require('path');
const webpack = require('webpack');

module.exports = {
	entry: {
		index: './src/index.ts',
	},
	experiments: {
		outputModule: true,
	},
	mode: 'production',
	module: {
		rules: [
			{
				exclude: /node_modules/,
				test: /\.ts$/,
				use: {
					loader: 'ts-loader',
				},
			},
		],
	},
	output: {
		chunkFormat: 'module',
		clean: true,
		filename: 'index.[contenthash].js',
		library: {
			type: 'module',
		},
		path: path.resolve(__dirname, 'build/static'),
	},
	plugins: [
		new webpack.optimize.LimitChunkCountPlugin({
			maxChunks: 1,
		}),
	],
	resolve: {
		extensions: ['.ts', '.js'],
	},
};