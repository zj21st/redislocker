# 基于spring boot 2.3.1的Redis锁范例


### redis配置
redis配置在config/RedissonConfig.java中
* 使用默认IP（127.0.0.1）和默认端口6379

### 目录结构
* web   网络API
* config   配置
* transactionlocker 事务锁

### API
* 非事务加锁范例 /work/{id}
* 非事务加锁范例 /unlock/{id}
* 事务加锁解锁范例 /getdistributedLocker/{id}

###### created by zhoujian (6396998@qq.com) 2020/6/24 

