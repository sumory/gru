var express = require('express');
var request = require("request");

var commonUtils = require("../lib/utils.js");
var redisUtils = require("../lib/redisUtils");
var zkUtils = require("../lib/zkUtils.js");
var logger = require("../lib/log.js").logger("monitorRouter");
var config = require("../config");
var router = express.Router();


router.get("/", function (req, res, next) {
    res.render("monitor");
});

//获取长连接服务节点
router.get("/spears", function (req, res, next) {
    zkUtils.getSpears(function (err, spears) {
        if (err) {
            res.json([]);
        }
        else {
            res.json(spears);
        }
    });
});

//获取某个长连接服务节点下连接上的所有群组
router.get("/:spear/groups", function (req, res, next) {
    var spear = req.params['spear'];
    redisUtils.keys("gru_stat:" + spear + "_*", function (err, result) {
        //console.log(result);
        res.json(result);
    });
});

//查询某个spear下的所有在线人数
router.get("/:spear/usercount", function (req, res, next) {
    var spear = req.params['spear'];
    redisUtils.get("user_count:" + spear , function (err, result) {
        res.json(result);
    });
});

//获取整个长连接集群下所有的 【节点-群组】 对
router.get("/groups", function (req, res, next) {
    redisUtils.keys("gru_stat:*", function (err, result) {
        console.log("redis上群组数目",result.length);
        res.json(result);
    });
});


//获取某个节点下某个群组的所有人
router.get("/group/:id", function (req, res, next) {
    var id = req.params['id'];
    console.log("取组", id, "的统计信息");

    var groupId = id.split("_")[1];
    request.get({url:config.statServer+"/getGroupStat?groupId="+groupId}, function (error, response, body) {
        logger.info("请求statServer结果:", groupId, error, body);
        if(!error){
            body = JSON.parse(body);
            if(body){
                logger.info("请求statServer success");
                res.json({
                    success:true,
                    data:body
                });
            }else{
                logger.error("请求statServer结果出错");
                res.json({
                    success:false
                });
            }
        }else{
            logger.error("请求statServer错误", error);
            res.json({
                success:false
            });
        }
    });
});



module.exports = router;

