### 集群部署

集群化部署需要的环境和支持如下：

- 各模块打包方式使用的是maven，建议先了解maven，并挨个熟悉各模块的pom.xml
- 各模块的职责介绍参看项目readme文档
- 集群间服务的发现注册等通过zookeeper实现
- 组件之间的API调用使用的是Dubbo
- 使用Redis来做状态的持久化，如当前的在线状态、群组统计等
- 多个长连接服务节点间的通信通过Redis或者RocketMQ来做转发，可配置
- 监控平台基于Node.js，监控获取数据的API是通过stat模块的HTTP API实现

#### 部署idgen

`idgen`是id生成模块：

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
