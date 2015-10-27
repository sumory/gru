### 集群部署

集群化部署需要的环境和支持如下：

- 各模块打包方式使用的是maven，建议先了解maven，并挨个熟悉各模块的pom.xml
- 各模块的职责介绍参看项目readme文档
- 集群间服务的发现注册等通过zookeeper实现
- 组件之间的API调用使用的是Dubbo，服务提供方和使用方不需要知道对方所在，依赖zookeeper做服务发现和治理
- 使用Redis来做状态的持久化，如当前的在线状态、群组统计等
- 多个长连接服务节点间的通信通过Redis或者RocketMQ来做转发，可配置
- 监控平台基于Node.js，监控获取数据的API是通过stat模块的HTTP API实现

<b>注意：</b>

- 部署集群前需先安装好zookeeper、redis，并安装JRE7+和Node.js运行环境。
- 用户应特别注意每个模块的配置文件，将其中的机器ip、端口等改为要部署的机器的对应配置。


#### 1. 部署idgen服务

`idgen`是id生成模块：

- 使用了snowflake算法来生成id，通过dubbo的rpc来提供服务。
- 如果要保证消息id唯一且自增，应保证集群中使用有且只有一个idgen的实例提供服务。为了高可用，可部署两个点，将其中一个点作为正在提供服务的点的backup。
- 如果不要求id严格自增，只需要id唯一，则部署N个idgen服务节点都可以。

<b>构建</b>

部署一个节点：

```
cd idgen
mvn clean package -Ptest1 #以src/main/resources/profiles/test1的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```

部署第二个节点：

将src/main/resources/profiles/test1复制一份，修改其中的配置，同样按上述打包运行方式运行即可。



#### 2. 部署stat服务

`stat`是业务服务模块，当前提供的功能为收集长连接节点spear的状态数据：

- stat基于dubbo，可以任意部署N个节点。

<b>构建</b>

部署一个节点：

```
cd stat
mvn clean package -Ptest1 #以src/main/resources/profiles/test1的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```

部署第二个节点：

```
cd stat
mvn clean package -Ptest2 #以src/main/resources/profiles/test2的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```



#### 3. 部署spear服务

`spear`是长连接服务模块，目前只支持socket.io协议：

- spear集群部署时依赖于idgen、stat服务。
- spear服务启动后会将本节点信息注册到zookeeper，比如本节点的对外访问地址、端口号等。

<b>构建</b>

部署一个节点：

```
cd spear
mvn clean package -Ptest1 #以src/main/resources/profiles/test1的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```

部署第二个节点：

```
cd spear
mvn clean package -Ptest2 #以src/main/resources/profiles/test2的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```

以此类推部署第N个节点。。。

spear可根据具体的长连接数量和机器配置横向扩展，建议8核16G的机器部署一个spear节点，可支持10W长连接。用户可根据连接的繁忙程度和活跃度，自行调节试验出一个比较合适的值。


#### 4. 部署ticket服务

`ticket`是前端模块，负责准入机制（如token验证等）和负载均衡（目前通过一致性hash分配各连接到不同的spear节点）：

- ticket订阅zookeeper事件，当spear集群有变动时，ticket能实时感知并获取最新的spear服务节点的列表。
- ticket对外提供http访问，返回给请求者负载均衡后的可用spear节点
- 此外，ticket会对请求进行校验，简单判断请求是否合法，不合法的请求不予给予spear访问授权
- ticket可部署多个点，前端可由Nginx等7层应用服务器来做负载均衡

<b>构建</b>

部署一个节点：

```
cd ticket
mvn clean package -Ptest1 #以src/main/resources/profiles/test1的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```

部署第二个节点：

```
cd ticket
mvn clean package -Ptest2 #以src/main/resources/profiles/test2的配置文件进行打包
cd target/release #release目录是运行所需要的所有文件
sh bin/start.sh
```


#### 总结

这个集群对外提供服务的只有ticket，stat/ticket/spear均可水平自由扩展，idgen需要根据业务要求维持一个或多个节点。




