根据《操作系统导论》的第二十八章，借助AtomicInteger，LockSupport的帮助，实现了以下功能的锁。

* 公平、队列存放休眠线程的锁
* 公平、线程自旋的锁
* 非公平、线程自旋的锁

> 按道理来说是使用Unsafe提供的各种原子操作，而不是AtomicInteger。但是我本地无法使用Unsafe，会抛异常，所有用AtomicInteger来代替， AtomicInteger也只是在Unsafe上简单封装一下。
