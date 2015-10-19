var path = require('path');

// 通过NODE_ENV设置环境变量，默认为dev环境
var env = process.env.NODE_ENV || 'dev';
env = env.toLowerCase();

var file = path.resolve(__dirname, env);
try {
	var config = module.exports = require(file);
	console.log('Load config: [%s] %s', env, file);
} catch (err) {
	console.error('Error when load config: [%s] %s', env, file);
	throw err;
}