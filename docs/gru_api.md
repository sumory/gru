#### Gru对外API

#### 1. 获取长连接服务地址


描述: 以返回结果的`data.addr`作为连接URL，连接失败，如果backupNodes不为空，依次尝试backupNodes.  

地址: http://${host}:30000/ticket?userType=1&userName=student1&userId=123&token1=afsfdfdsfdsf  

参数: 

```
userType: 0或1
userName: 用户的姓名  
userId: 用户id  
token1: 业务系统返回的授权token
```

方法: `Get`  
结果: json

```
正常结果：
{
	success: true,
	errorCode: 0,
	msg: "描述信息", //可能为空
	data: {
		time: 1425959699934, //服务器时间戳
		addr: "http://10.60.0.67:11111", //长连接服务节点
		name: "node_1", //长连接服务节点的唯一标识,
		token: "FeruUfeERkREle834uaiorhUlDbNnpp", //授权token，连接长连接节点时使用
		backupNodes:[//备选服务节点
			{
				addr: "http://10.60.0.67:11113",
				name: "node_2"
			},
			{
				addr: "http://10.60.0.67:11115",
				name: "node_3"
			}
		]
	}
}

错误结果：
{
	success: false,
	errorCode: 400,//各种错误码
	msg: "Not found" //错误描述，可能为空
}

异常：
其他http请求失败或异常等
```


#### 2. 登录长连接服务器

描述: 连接时需使用`获取长连接服务地址`api返回的addr和token  

地址: 通过上一个api获取的`addr`    
方法: socket.io客户端实现的方法api，如connect、emit、on等
结果: "on" event

```
# 客户端请求连接示例:

//addr为上个api获得的长连接服务地址，如http://10.60.0.67:31001
socket = io.connect(addr,{"reconnect":true,"auto connect":true,"force new connection":true});
socket.on('connect',function() {
	socket.emit('auth', JSON.stringify({
		id : 123,	//用户id
		name : "张三", //用户姓名
		groupId: 10, //群组id
		token1: token1, //从业务系统获取的token
		token2: token2, //上个api获得的token
		type:0 //用户类型
	}));
});


# 客户端正常响应示例:
socket.on('auth_result', function(data) {
	//返回的data格式：
	/*
	{
		"data": {
			"userType": "TEACHER"
		},
		"errorCode": 0, //状态码
		"msg": "", //描述
		"success": true //是否正常建立连接
	}
	*/
});

#客户端异常响应为连接不成功，其他各种异常情况，比如请求不合法、参数错误、超时等服务端会直接断开该链接
```



#### 3. 发送、接收消息

描述: 连接成功后即可发送各种消息，为了兼容以后的各种实现，目前发送的消息都以”字符串“的形式来表示，由业务端自行定义，建议使用json格式.
  
方法: socket.io客户端实现的发送消息方法emit、事件监听方法on
结果: "on" event

```
# 客户端发送消息示例:
var msg ={
	type: 1, //1 广播，0 单播给指定target
	target: { //单播时该字段有效
		id: 10, //"用户id"
		type: 1 //用户类型
	},
	content: "字符串"
}

msg = JSON.stringify(msg);//转化为可发送的字符串形式，java的话建议使用fastjson序列化对象为json字符串
socket.emit('msg', msg);


# 客户端收到消息回执[对应emit("msg")动作]示例:
socket.on('msg_result', function(msg_result) {
	//msg_result格式示例如下：
	{
	    success: false,// 消息发送是否成功
	    errorCode: -2,// 状态码
	    msg: "消息类型不正确，请注明类型" //描述
	}
});


# 客户端监听/接受消息示例:
socket.on('msg', function(msg) {
	//msg格式如下：
	{
	    id: 402251433564438500, //该消息的id，递增，全局唯一
	    createTime: 1426232324279,//该消息从服务端发出的时间（毫秒）
	    content: "{userName:\"王老师\",message:\"跳转到第三页\"}",// 该消息的具体内容，数据格式是业务端自定义的
	    expireTime: 0 //该消息的有效期，用于以后扩展
	}
});
```



#### 4. 获取用户在线列表
  
方法: socket.io客户端实现的发送消息方法emit、事件监听方法on
结果: "on" event

```
# 发送获取在线列表请求示例:
socket.emit('online', "10");//参数


# 监听结果:
socket.on('online_result',function(data) {
	output("在线人："+JSON.stringify(data));//data为在线用户id列表
});

```