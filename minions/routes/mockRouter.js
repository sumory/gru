var express = require('express');
var commonUtils = require("../lib/utils.js");
var logger = require("../lib/log.js").logger("mockRouter");
var config = require("../config");
var router = express.Router();


//生成token
router.get("/genToken", function (req, res, next) {
    var userId = req.query.userId;
    var userName = req.query.userName;
    var appType = req.query.appType;

    console.log("params:", userId, userName, appType);

    var token1 = commonUtils.md5(userId+"_"+userName+"_"+appType, "token_gen_for_ticket@sumory.com");
    var token2 = commonUtils.md5(userId+"_"+userName+"_"+appType+"_"+token1, "token_gen_for_spear@sumory.com");

    res.json({
        success:true,
        token1: token1,
        token2: token2
    })
});


module.exports = router;

