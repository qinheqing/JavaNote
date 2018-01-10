# servlet 容器
	servlet容器如Tomcat分为四个等级，真正管理 Servlet 的容器是 Context 容器，一个 Context 对应一个 Web 工程，在 Tomcat 的配置文件中可以很容易发现这一点

# servlet容器的启动过程
	1. tomcat7新增了一个启动类，startup.tomcat创建实例对象并调用start方法可以很容易的启动tomcat，我们可以利用这个类进行动态增加Context,servlet等
		Tomcat tomcat = getTomcatInstance(); 			# 创建一个tomcat容器
		File appDir = new File(getBuildDirectory(), "webapps/examples");  #获取文件路径
		tomcat.addWebapp(null, "/examples", appDir.getAbsolutePath());  #在tomcat实例中添加一个app
		tomcat.start(); 				# 启动应用
		ByteChunk res = getUrl("http://localhost:" + getPort() + 
		              "/examples/servlets/servlet/HelloWorldExample");  # 测试请求
		assertTrue(res.toString().indexOf("<h1>Hello World!</h1>") > 0);

# tomcat中的增加app的方法：
	public Context addWebapp(Host host, String url, String path) { 
       silence(url);   	# 处理url
       Context ctx = new StandardContext();   //通过tomcat容器生成context容器
       ctx.setPath( url ); 
       ctx.setDocBase(path);  
       if (defaultRealm == null) { 
           initSimpleAuth(); 
       } 
       ctx.setRealm(defaultRealm); 
       ctx.addLifecycleListener(new DefaultWebXmlListener()); # 添加生命周期
       ContextConfig ctxCfg = new ContextConfig(); #添加配置文件
       ctx.addLifecycleListener(ctxCfg); 
       ctxCfg.setDefaultWebXml("org/apache/catalin/startup/NO_DEFAULT_XML"); 
       if (host == null) { 
           getHost().addChild(ctx); 
       } else { 
           host.addChild(ctx); 
       } 
       return ctx; 
	}

# 接下去是执行tomcat中的start方法