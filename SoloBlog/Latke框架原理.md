# Latke框架基本原理

# 框架的核心组件：
	1. 模板引擎，使用的是FreeMarker作为模板引擎

	2. IoC容器，实现的是JSR-330规范，默认提供了控制器（@RequestProcessor）,服务（@Service）、DAO（@Repository）的构造型。

	3. 事件通知：通过事件管理其进行事件监听器注册，事件发布/订阅

	4. ORM: 提供了对json对象的增删改查，支持关系型数据库和redis的存储

	5. 插件，插件包含完整的前后端能，可以在不改变原有代码前提下扩展功能

	6. 国际化，在模板中可以直接使用${xxx}的形式读取语言配置，后端提供了语言服务进行获取配置

	7. 服务：
		缓存;图片处理;邮件;http客户端;用户管理;日志;任务队列;多语言

# 项目的包结构：
	~~~Java
	└─hello
    │  nb-configuration.xml
    │  nbactions.xml
    │  pom.xml
    │
    └─src
        └─main
            ├─java
            │  └─org
            │      └─b3log
            │          └─latke
            │              └─demo
            │                  └─hello
            │                      │  HelloServletListener.java
            │                      │  Starter.java
            │                      │
            │                      ├─processor
            │                      │      HelloProcessor.java
            │                      │      RegisterProcessor.java
            │                      │
            │                      ├─repository
            │                      │      UserRepository.java
            │                      │
            │                      └─service
            │                              UserService.java
            │
            ├─resources
            │  │  latke.properties
            │  │  local.properties
            │  │  log4j.properties
            │  │  repository.json
            │  │
            │  └─META-INF
            │          beans.xml
            │
            └─webapp
                ├─skins
                │  └─classic
                │      │  hello.ftl
                │      │  index.ftl
                │      │  register.ftl
                │      │
                │      └─images
                │              logo.png
                │
                └─WEB-INF
                        static-resources.xml        
                        web.xml

	~~~~

# 项目中各个组件的职能
	1. 静态资源定义（src/main/webapp/WEB-INF/static-resources.xml），用于定义应用中用到的静态资源路径
	
	2. 框架通用配置（src/main/resources/latke.properties），定义了服务器访问信息、IoC 扫描包、运行环境、运行模式、部分服务实现（缓存服务、用户服务）、缓存容量、静态资源版本等
	
	3. 框架本地实现配置（src/main/resources/local.properties），定义了本地容器相关参数，例如数据库、JDBC 配置等
	
	4. 数据库表结构（src/main/resources/repository.json），定义了数据库表结构，用于生成建表语句以及持久化时的校验
	部署描述符（src/main/webapp/WEB-INF/web.xml），定义了框架启动 Servlet 监听器、请求分发器等

