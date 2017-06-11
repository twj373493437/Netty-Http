# Netty-Http
http server based on netty，基于Netty的HTTP服务

主要设计的目标是可以嵌入式和独立使用，简单易用，
易于和其他Java框架结合使用，具有较高的性能，占用资源比较少等。

特点：<br>
1.类似Spring MVC的控制器开发方式，基于注解@Controller 和 @Mapping。

2.支持静态文件，配置静态文件的路径即可。可以是jar包内，也可以是磁盘上的文件夹。

3.基于netty开发，可以在mapping 注解处配置 isAsyn从而异步执行，而不是用netty的工作线程。

4.路由使用了Java动态编码技术，避免反射执行invoke带来的性能开销。

5.支持直接注解注入spring中的bean，可以用名称或者类型匹配获取，前提是初始化server前传入
Spring的context。

简单使用：
        
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring/ApplicationContext-dataSource.xml",
                "spring/ApplicationContext-main.xml");
        HttpServer server = new HttpServer(8089, false);
        ServerContext context = server.getServerContext();
        context.setSpringContext(ac);
        String config = "/server.properties";
        try {
            context.initByConfigFile(config);
            server.start();
        } catch (Exception e) {
            log.error("出现了异常", e);
        }