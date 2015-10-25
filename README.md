## Gru

**Gru**是一个长连接服务解决方案，可用于各种类型的实时交互应用。


#### 目前主要模块

- common 通用的一些代码、工具类等
- idgen 消息id、某些业务id生成服务
- ticket 
  - 处于最前端，客户端首先请求该服务拿到ticket才能建立长连接
  - 目前实现了基于一致性hash的负载均衡，将客户端路由到不同的spear节点
  - 其它位于前端的策略，如准入机制等可在此模块扩展
- stat 统计模块、业务模块
  - 目前实现了在线统计，数据存储在redis
  - 若还需要后端接入其他业务服务，可在此模块扩展
- spear 长连接服务模块
  - 提供长连接接入的模块
  - 最小化模式部署时只需要此模块的一个实例即可
- minions 监控模块
  - 目前可监控集群长连接服务spear节点数、每个节点的用户数等
  - 节点间管理、用户管理可在此模块扩展
- spear-client 客户端模拟，压测示例代码
- 示例项目: [gru-example](https://github.com/sumory/gru-example)
  - 以IM作为示例展示基于Gru构建实时应用
  - 支持群聊和单聊


#### 特性

- 支持单点部署和集群模式部署
- 采用[socket.io](http://socket.io)协议
- 各模块均支持水平扩展
- 单节点可服务10W+以上长连接，具体为在不断发消息的情况下(1000条/秒)，单长连接服务节点支持的稳定连接数量在10W+(8核16G)
- 节点间通讯支持多种方式：进程内、redis、rocketmq


#### 安装部署

git clone https://github.com/sumory/gru.git /data/tmp/gru

##### 最小安装，单点部署

如果只想体验单节点的部署模式，不需要监控、负载均衡及其它业务服务，只需要部署一个spear节点即可。

```
cd /data/tmp/gru
mvn install #本地安装
cd spear
mvn clean package -Pdev #生成spear可运行包，使用dev的配置文件
cd target/release #该目录下有所有spear运行需要的文件

```

##### 集群方式部署

集群的安装配置较为复杂，详见[Gru集群安装配置](docs/install_cluster.md)


#### 监控后台screenshots

<table>
    <tr>
        <td width="50%"><img src="docs/dashboard-client.png"/></td>
        <td width="50%"><img src="docs/dashboard-monitor.png"/></td>
    </tr>
</table>


