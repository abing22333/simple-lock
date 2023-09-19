## 锁实现

根据《操作系统导论》的第二十八章，借助AtomicInteger，LockSupport的帮助，实现了以下功能的锁。

* 基于队列休眠的公平锁
* 基于自旋的公平锁
* 基于自旋的非公平锁
* 线程可重入锁
* 带有condition的锁

> 按道理来说是使用Unsafe提供的各种原子操作，而不是AtomicInteger。但是考虑到不同版本jdk的Unsafe使用方法有差异，所有使用AtomicInteger来代替。

## 基准测试

### 打包

```bash
mvn clean package -DskipTests=true
``` 

### 运行

```bash
java -jar benchmark/target/benchmark.jar
```
 

