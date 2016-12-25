# paxos算法实现

author:
赵振佐，黄群<br/>
程序运行实例如下：
1.启动com.paxos.AppStart
  假设结点有三个，分别为：192.168.0.100:10051,192.168.0.100:10052,192.168.0.100:10053
 
 三个结点启动需要传入jvm参数，
 
 结点1(192.168.0.100:10051)启动jvm配置：
-Dip=192.168.0.100  //当前结点ip
-Dport=10051 //当前结点port
-Dnodes=192.168.0.100:10051,192.168.0.100:10052,192.168.0.100:10053 //结点集合
-Dserver.port=8081 //web端口

 结点2(192.168.0.100:10052)启动jvm配置：
-Dip=192.168.0.100  //当前结点ip
-Dport=10051 //当前结点port
-Dnodes=192.168.0.100:10051,192.168.0.100:10052,192.168.0.100:10053 //结点集合
-Dserver.port=8082 //web端口

 结点3(192.168.0.100:10051)启动jvm配置：
-Dip=192.168.0.100  //当前结点ip
-Dport=10053 //当前结点port
-Dnodes=192.168.0.100:10051,192.168.0.100:10052,192.168.0.100:10053 //结点集合
-Dserver.port=8083 //web端口

2.三个结点启动后会选举出一个leader，可从管理界面查看
  管理界面地址：
  http://localhost:8081/getLeaderMember,
  http://localhost:8082/getLeaderMember,
  http://localhost:8083/getLeaderMember
  分别对应上述例子三个结点的管理页
  

