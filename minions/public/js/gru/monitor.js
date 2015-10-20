(function (G) {
    G.dashboard = G.dashboard || {};
    var _this = G.dashboard = {
        data: {
            autoGetSpearsListener: null,
            autoGetSpearGroupsListener: null
        },
        init: function () {
            //查询当前存活的spear节点
            $("#btnGetSpears").click(function () {
                $("#spear_stat").empty();
                _this.getSpears();
            });

            //点击某个spear
            $("body").on("click", ".btnGetSpearGroups", function () {
                var spearId = $(this).attr("data-id");
                $("#group_area").empty();
                $("#group_place_holder").empty();

                //获取点击节点下的所有群组
                $.ajax({
                    url: '/m/' + spearId + '/groups/',
                    type: 'get',
                    data: {},
                    dataType: 'json',
                    success: function (result) {
                        //console.dir(result);
                        if (result) {
                            $("#groups_area").empty();
                            $("#spear_place_holder").text("【节点" + spearId + ": "+ result.length +"个群组】");
                            result.sort(_this.sortFunc);
                            for (var i = 0; i < result.length; i++) {
                                var nodeGroup = result[i].slice("gru_stat:spear".length);
                                $("#groups_area").append('<a class="ui blue circular label btnGetGroup" data-id="spear' + nodeGroup + '">' + nodeGroup + '</a>');
                            }
                        }
                    },
                    error: function () {
                        console.log('error');
                    }
                });

                //获取点击节点下的所有人数
                _this.getSpearStat(spearId);
            });


            //查询当前 节点-群组 对
            $("#btnGetGroups").click(function () {
                _this.getAllSpearGroups();
            });

            $("#btnClear").click(function(){
                $("#group_area").empty();
                $("#groups_area").empty();
                $("#spear_place_holder").empty();
                $("#group_place_holder").empty();
            });

            //查出某个节点某个群组下的所有人
            $("body").on("click", ".btnGetGroup", function () {
                var groupId = $(this).attr("data-id");
                $("#toKickGroupId").val(groupId);
                $.ajax({
                    url: '/m/group/' + groupId,
                    type: 'get',
                    data: {},
                    dataType: 'json',
                    success: function (result) {
                        if (result && result.success) {
                            var userIds = result.data.userIds;
                            $("#group_area").empty();

                            if(!userIds){
                                $("#group_area").html("无数据");
                                return;
                            }

                            userIds.sort();
                            $("#group_place_holder").text("【群组" + groupId.split("_")[1] +": "+ userIds.length + "个用户】");


                            /**
                             * 分节点展示
                             */


                            for (var i = 0; i < userIds.length; i++) {
                                $("#group_area").append('<a class="ui purple label btnGetUser" data-id="'+userIds[i]+'">' + userIds[i] + '</a>');
                            }
                        }else{
                            $("#group_place_holder").text("");
                            $("#group_area").html("出错！");
                        }
                    },
                    error: function () {
                        console.log('error');
                    }
                });
            });

            //点击某个节点某个群组下的某个人
            $("body").on("click", ".btnGetUser", function () {
                var userId = $(this).attr("data-id");
                $("#toKickUserId").val(userId);
            });

            //开启"获取节点"自动刷新
            $('#autoGetSpears').checkbox({
                onChecked: function () {
                    console.log("enable");
                    _this.data.autoGetSpearsListener = setInterval(function () {
                        _this.getSpears();
                    }, 2000);
                },
                onUnchecked: function () {
                    console.log("disabled");
                    clearInterval(_this.data.autoGetSpearsListener);
                }
            });

            //开启"获取节点-群组对"自动刷新
            $('#autoGetSpearGroups').checkbox({
                onChecked: function () {
                    console.log("enable");
                    _this.data.autoGetSpearGroupsListener = setInterval(function () {
                        _this.getAllSpearGroups();
                    }, 2000);
                },
                onUnchecked: function () {
                    console.log("disabled");
                    clearInterval(_this.data.autoGetSpearGroupsListener);
                }
            });
        },

        getSpears: function () {
            $.ajax({
                url: '/m/spears',
                type: 'get',
                data: {},
                dataType: 'json',
                success: function (result) {
                    //console.dir(result);

                    if (result) {

                        $("#spears_area").empty();
                        result.sort();
                        for (var i = 0; i < result.length; i++) {
                            var spear = result[i];

                            $("#spears_area").append('<a class="ui red circular label btnGetSpearGroups" data-id="' + spear + '">' + spear + '</a>');

                            if($("#statistic_"+spear).length>=1){
                                //console.log("存在");
                            }else{
                                //console.log("不存在，添加新统计element");
                                var statistic='<div class="ui statistic" id="statistic_'+spear+'">';
                                statistic+='<div class="value">';
                                statistic+='<i class="small users  icon"></i> <span></span>';
                                statistic+='</div>';
                                statistic+='<div class="label">'+spear;
                                statistic+='</div>';
                                statistic+='</div>';
                                statistic+='</div>';
                                //初始化统计
                                $("#spear_stat").append(statistic);

                            }


                            _this.getSpearStat(spear);
                        }
                    }
                },
                error: function () {
                    console.log('error');
                }
            });
        },

        getSpearStat: function(spear){

            $.ajax({
                url: '/m/' + spear + '/usercount',
                type: 'get',
                data: {},
                dataType: 'json',
                success: function (result) {
                    if (result) {
                        $("#spear_stat #statistic_"+spear+" .value span").text(result);
                        _this.animate(spear, "tada");
                    }else{
                        $("#spear_stat #statistic_"+spear+" .value span").text("~");
                        _this.animate(spear, "flash");
                    }


                },
                error: function () {
                    console.log('error');
                }
            });
        },

        getAllSpearGroups: function () {
            $.ajax({
                url: '/m/groups',
                type: 'get',
                data: {},
                dataType: 'json',
                success: function (result) {
                    //console.dir(result);
                    if (result) {
                        $("#groups_area").empty();
                        $("#spear_place_holder").text("【全部: "+ result.length +"个对】");
                        result.sort(_this.sortFunc);
                        for (var i = 0; i < result.length; i++) {
                            var nodeGroup = result[i].slice("gru_stat:spear".length);

                            $("#groups_area").append('<a class="ui blue circular label btnGetGroup" data-id="' + "spear"+nodeGroup + '">' + nodeGroup + '</a>');
                        }
                    }
                },
                error: function () {
                    console.log('error');
                }
            });
        },

        sortFunc: function(a, b){
            var aId = parseInt(a.slice("gru_stat:spear".length).split("_")[1]);
            var bId = parseInt(b.slice("gru_stat:spear".length).split("_")[1]);
            return aId-bId;
        },

        animate:function(spear,x) {
            $('a[data-id='+spear+']').removeClass(x + ' animated').addClass(x + ' animated').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function () {
                $(this).removeClass(x + ' animated');
            });
        }

    }
})(gru);