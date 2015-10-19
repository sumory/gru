var path = require('path');

module.exports = {
	port: 40000,
	viewEngine: 'ejs',

	views: path.resolve(__dirname, '..', 'views'),
	staticPath: path.resolve(__dirname, '..', 'public'),

	env: 'dev',
	logfile: path.resolve(__dirname, '..', 'logs/access.log'),

	sessionSecret: 'session_secret_random_seed',

	//redis config
	"redis": {"address": "172.18.4.174", "port": "6379", "passwd": ""},

	//不需要过滤是否登陆状态的白名单
	"whitelist": [
		"/",
		"/auth/login",
		"/version"
	],

	zk: {
		addr: "172.18.4.227:2181,172.18.4.162:2181,172.18.4.166:2181",
		spears: "/spearnodes_prod"
	},

	statServer: "http://172.18.4.172:50000"
};