var express = require('express');
var path = require('path');
var favicon = require('static-favicon');
var methodOverride = require('method-override');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var expressSession = require('express-session');
var multiparty = require('multiparty');
var log4js = require('log4js');


var logger =  require('./lib/log.js').logger('app');
var route = require('./route.js');
var config = require('./config');

var cookie = cookieParser(config.sessionSecret);
var app = express();
app.set('env', config.env);
app.set('port', config.port);
app.set('views', config.views);
app.set('view engine', config.viewEngine);
app.use(favicon());
app.use(log4js.connectLogger(logger, {
    level: "auto"
}));

app.use(bodyParser());
app.use(methodOverride());
app.use(cookie); //须在expressSession前使用cookieParser
var sessionStore = new expressSession.MemoryStore();
app.use(expressSession({
    secret: config.sessionSecret,
    key: 'expressId', //种到cookies里的标识
    store: sessionStore
}));
//app.use(csrf());
app.use(express.static(config.staticPath));

//var whitelist = config.whitelist;
//app.use(function(req, res, next) {//判断是否登录的中间件
//    var requestPath = req.path;//请求的uri
//    var inWhitelist = false;
//    for (var i in whitelist) {
//        if (requestPath == whitelist[i]) {
//            inWhitelist = true;
//            break;
//        }
//    }
//
//    if (inWhitelist) {//在白名单中，不需要过滤
//        next();
//    }else{
//        if(req.session && req.session.admin){//如果存在session则继续
//            next();
//        }else{
//            res.redirect("/auth/login");
//        }
//    }
//});


route(app); //加载routes

//404错，即无匹配请求地址
app.use(function(req, res, next) {
    if (!req.xhr) {
        logger.error('common 404');
        res.status(404);
        res.render('error', {
            msg: "404 未找到"
        });
    } else { //这里遇到xhr的404错误,在返回数据中指明
        logger.error('xhr 404');
        res.status(404);
        res.json({
            success: false,
            status: 404,
            msg: '404错误'
        });
    }
});

//记录错误日志
app.use(function(err, req, res, next) {
    var status = err.status || 500;
    logger.error('【error】', 'status:', status, 'message:', err.message || '');
    logger.error('【stack】\n ', err.stack || '');
    next(err);
});

//处理错误，返回响应
app.use(function(err, req, res, next) {
    var status = err.status || 500;
    if (!req.xhr) {
        //logger.error('common other error');
        res.status(status);
        res.render('error', {
            success: false,
            status: status,
            msg: '500错误'
        });
    } else { //xhr的内部调用的错误会到达这个分支
        //logger.error('xhr other error');
        res.status(status);
        res.json({
            success: false,
            status: status,
            msg: 'xhr发生错误'
        });
    }
    return;
});


var server = require('http').Server(app);
server.listen(app.get('port'), function() {
    console.log('Server listening on port ' + server.address().port, ', env is ' + app.get('env'));
});

process.on('uncaughtException', function(err) {
    console.log('Holy shit!!!!! Fatal Errors!!!!!!! ' + err);
});