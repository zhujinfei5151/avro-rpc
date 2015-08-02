avro-rpc 是一个支持跨语言实现的RPC服务框架。非常轻量级，实现简洁，使用方面，同时也方便使用者进行二次开发，逻辑上该框架分为两层：

1：网络传输层使用Netty的Nio实现。

2：协议层可扩展，目前支持的数据序列化方式有Avro，Protocol Buffers ，Json, Hessian,Java序列化。 使用者可以注册自己的协议格式及序列化方式。



主要特点：

1：客户端传输层与应用层逻辑分离，传输层主要职责包括创建连接，连接查找与复用，传输数据，接收服务端回复后回调应用层；

2：客户端支持同步调用和异步调用。服务异步化能很好的提高系统吞吐量，建议使用异步调用。为防止异步发送请求过快，客户端增加了“请求流量限制”功能；

3：服务端有一个协议注册工厂和序列化注册工厂。这样方便针对不同的应用场景来定制服务方式。RPC应该只是服务方式的一种。在分布式的系统架构中，分布式节点之间的通信会存在多种方式，比如MQ的TOP消息，一个消息可以有多个订阅者。因此avro-rpc不仅仅是一个RPC服务框架，还是一个分布式通信的一个基础骨架，提供了很好的扩展性；

4：Avro序列化框架是Hadoop下的一个子项目，其特点是数据序列化不带标签，因此序列化后的数据非常小。支持动态解析, 不像Thrift 与 Protocol Buffers必须根据IDL来生成代码，这样侵入性有点强。性能很好，基本上和 Protocol Buffers差不多；



运行性能测试实例

1. 启动服务端 code.google.dsf.test.StartServerTest

2. 运行客户端 code.google.dsf.test.performance.RPSTest

测试对象，客户端采用异步方式发送一个POJO（10个属性字段）对象
两台，一台运行客户端，一台运行服务端，配置如下：
•	CPU: E5645 @ 2.40GHz 2 core
•	Memory: 2G
•	Network: 1000Mb

JVM Options: -server -XX:+UseParallelGC  -XX:+AggressiveOpts -XX:+UseFastAccessorMethods -Xms512M -Xmx1024M

最新测试TPS:
avro:100937
Protocol Buffers: 122299

最新测试：http://www.iteye.com/topic/1127125

作者：taohuifei@gmail.com
http://taohuifei.iteye.com/blog/1685160