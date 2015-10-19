var util = require("util");
var fs = require("fs");
var path = require("path");
var crypto = require("crypto");

exports.checkLogin = function (req, res, next) {
    if (req && req.session && req.session.admin) {
        next();
    } else {
        return res.render('error', {
            msg: '请先登录再执行相关操作',
            success: false
        });
    }
};

exports.md5 = function (str, salt) {
    var md5sum = crypto.createHash("md5");
    if (salt)
        md5sum.update(str + "_" + salt, "utf-8");//记得添加‘utf-8’
    else
        md5sum.update(str, "utf-8");
    str = md5sum.digest("hex");
    return str;
};

exports.formatDate = function (date, friendly) {
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    var second = date.getSeconds();

    if (friendly) {
        var now = new Date();
        var mseconds = -(date.getTime() - now.getTime());
        var time_std = [1000, 60 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000];
        if (mseconds < time_std[3]) {
            if (mseconds > 0 && mseconds < time_std[1]) {
                return Math.floor(mseconds / time_std[0]).toString() + ' 秒前';
            }
            if (mseconds > time_std[1] && mseconds < time_std[2]) {
                return Math.floor(mseconds / time_std[1]).toString() + ' 分钟前';
            }
            if (mseconds > time_std[2]) {
                return Math.floor(mseconds / time_std[2]).toString() + ' 小时前';
            }
        }
    }

    hour = ((hour < 10) ? '0' : '') + hour;
    minute = ((minute < 10) ? '0' : '') + minute;
    second = ((second < 10) ? '0' : '') + second;

    return year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
};

exports.isArray = function (param) {
    return util.isArray(param);
};

/**
 * 是否为字符串
 *
 * @param {Mixed} str
 * @return {Boolean}
 */
exports.isString = function (str) {
    return (typeof str === 'string');
};

/**
 * 是否为整数
 *
 * @param {Mixed} str
 * @return {Boolean}
 */
exports.isInteger = function (str) {
    return (Math.round(str) === Number(str));
};

/**
 * 是否为数字
 *
 * @param {Mixed} str
 * @return {Boolean}
 */
exports.isNumber = function (str) {
    return (!isNaN(str));
};


/**
 * md5加密
 * @param text 要加密的串
 * @return  16进制加密穿2
 */
exports.formalMd5 = function (text) {
    return crypto.createHash('md5').update(text).digest('hex');
};


/**
 * 加密信息
 *
 * @param data
 * @param secret
 * @return
 */
exports.encryptData = function (data, secret) {
    var str = JSON.stringify(data);
    var cipher = crypto.createCipher('aes192', secret);
    var enc = cipher.update(str, 'utf8', 'hex');
    enc += cipher.final('hex');
    return enc;
};

/**
 * 解密信息
 *
 * @param str
 * @param secret
 * @return
 */
exports.decryptData = function (str, secret) {
    var decipher = crypto.createDecipher('aes192', secret);
    var dec = decipher.update(str, 'hex', 'utf8');
    dec += decipher.final('utf8');
    var data = JSON.parse(dec);
    return data;
};


/**
 * 产生随机字符串
 *
 * @param size
 * @param chars
 * @return
 */
exports.randomString = function (size, chars) {
    size = size || 6;
    var code_string = chars || 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var max_num = code_string.length + 1;
    var new_pass = '';
    while (size > 0) {
        new_pass += code_string.charAt(Math.floor(Math.random() * max_num));
        size--;
    }
    return new_pass;
};


/**
 * 产生随机数字字符串
 *
 * @param size
 * @return
 */
exports.randomNumber = function (size) {
    size = size || 6;
    var code_string = '0123456789';
    var max_num = code_string.length + 1;
    var new_pass = '';
    while (size > 0) {
        new_pass += code_string.charAt(Math.floor(Math.random() * max_num));
        size--;
    }
    return new_pass;
};


/**
 * 产生随机字母字符串
 *
 * @param size
 * @return
 */
exports.randomLetter = function (size) {
    size = size || 6;
    var code_string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
    var max_num = code_string.length + 1;
    var new_pass = '';
    while (size > 0) {
        new_pass += code_string.charAt(Math.floor(Math.random() * max_num));
        size--;
    }
    return new_pass;
};


console.log(exports.md5("47_xss4", "token_gen_for_ticket@sumory.com"));