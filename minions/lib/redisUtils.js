var redis = require("redis");
var config = require('../config');

var client = redis.createClient(config.redis.port, config.redis.address);
//client.auth(config.redis.passwd, function(){console.log("redis authRouter ok, redisUtils initializes ok")});

/**
 * keys命令
 */
exports.keys = function (regex, callback) {
    if(regex){
        client.keys(regex,function (err, reply) {
            if (err)
                console.log('redisUtils keys ' + regex + ' error: ' + err);
            //console.log('redisUtils keys: [' + regex + '] reply is:' + reply);
            callback && callback(err, reply);
        });
    }
};


/**
 * 设置一个不过期的值
 *
 * @param key
 * @param value
 * @param callback
 */
exports.set = function (key, value, callback) {
    if(key){
        client.set(key, value,function (err, reply) {
            if (err)
                console.log('redisUtils set ' + key + ' error: ' + err);
            console.log('redisUtils set: [' + key + ']:[' + value + '] reply is:' + reply);
            callback && callback(err, reply);
        });
    }
};

/**
 * 设置带过期时间的值
 *
 * @param key
 * @param value
 * @param seconds 多少秒后过期
 * @param callback
 */
exports.setWithExpire = function(key, value, seconds, callback){
    if(key && seconds){
        client.setex(key, seconds, value,function (err, reply) {
            if (err)
                console.log('redisUtils setex ' + key + ' error: ' + err);
            console.log('redisUtils setex: [' + key + ']:[' + value + '] reply is:' + reply);
            callback && callback(err, reply);
        });
    }
};

/**
 * 获取值
 *
 * @param key
 * @param callback
 */
exports.get = function (key, callback) {
    client.get(key, function (err, reply) {
        if (err)
            console.log('redisUtils get ' + key + ' error:' + err);
        callback && callback(err, reply);
    });
};
