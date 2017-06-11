
静态文件支持

注解式路由，转发

数据绑定，
单个数据(不用注解)，对象(必须使用注解@Params)

静态文件的缓存，不用从硬盘读，以便更好的支持web项目的小文件，lru 读写
锁

cookie

session(接口化，以便替换其他实现方式) 默认基于本地内存实现

过滤器（拦截器）实现 (还没实现指定url 拦截)

注解的线程池异步调用，防止work线程阻塞，在mapping注解中 isAsyn

支持包内的resource作为静态文件

添加spring 支持，直接在Controller 中使用注解类注入Spring中的bean

todo list
内容压缩 gzip

//websocket 的支持(暂时不实现)

//MVC(基于freemarker)(暂不实现)

http2 实现和测试

//数据校验 (暂不实现)


bug list
在没找到文件时，应该报404， 而不是 500，  捕捉文件异常时判断一下               OK