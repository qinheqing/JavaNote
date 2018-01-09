# tomcat的底层实现原理
	Web 服务器也称为超文本传输协议 (HyperText Transfer Protocol, HTTP) 服务器, 因为它使用 Http 与其客户端 (通常是 Web 浏览器) 进行通信, 基于 Java 的 Web 服务器会使用两个重要的类: java.net.Socket 类和 java.net.ServerSocket 类, 并通过发送 Http 消息进行通信. 

## http协议
	Http 允许 Web 服务器和浏览器通过 Internet 发送并接受数据, 是一种基于 "请求 --- 响应" 的协议, 客户端请求一个文件, 服务器端对该请求进行响应. Http 使用可靠的 tcp 连接, tcp 协议默认使用 tcp 80 端口, 
	在 Http 中, 总是由客户端通过建立连接并发送 http 请求来初始化一个事务的. Web 服务器端并不负责联系客户端或建立一个到客户端的回调连接. 客户端或服务器端可提前关闭连接, 例如, 当使用 Web 浏览器浏览网页时, 可以单击浏览器上的 stop 按钮来停止下载文件, 这样就有效的关闭了一个 Web 服务器的 http 连接.

## http请求
	1. 一个http请求应该包 含三个基本的部分：
		-. 请求的方法
		-. 请求头
		-. 实体

	2. 一个标准的请求http例子：
		POST /examples/default.jsp HTTP/1.1  //[方法]-[统一资源标识符 (URI)]—[协议]/[版本]出现在请求的第一行。
		Accept: text/plain; text/html        //浏览器可接受的MIME类型。
		Accept-Language: en-gb   			 //浏览器所希望的语言种类
		Connection: Keep-Alive 				 //表示是否需要持久连接。
		Host: localhost 					 //首部HOST将指出请求的目的地。结合第一行形成完整路径
		User-Agent: Mozilla/4.0 (compatible; MSIE 4.01; Windows 98)  //服务器端和客户端脚本都能够访问它，它是浏览器类型检测
		//Content-Length说明了请求主体的字节数;Content-Type说明了请求主体的内容是如何编码的
		//Accept - Encoding：浏览器能够进行解码的数据编码方式，比如gzip。Servlet能够向支持gzip的浏览器返回经gzip编码的HTML页面。许多情形下这可以减少5到10倍的下载时间。
		Content-Length: 33 Content-Type: application/x-www-form-urlencoded Accept-Encoding: gzip, deflate 

		lastName=Franks&firstName=Michael

	3. 请求的头部包含了关于客户端环境和请求的主体内容的有用信息。例如它可能包括浏览器设置的语言，主体内容的长度等等。每个头部通过一个回车换行符 (CRLF) 来分隔的。

	对于 HTTP 请求格式来说，头部和主体内容之间有一个回车换行符 (CRLF) 是相当重要的。CRLF 告诉 HTTP 服务器主体内容是在什么地方开始的。在一些互联网编程书籍中，CRLF 还被认为是 HTTP 请求的第四部分。

	在前面一个 HTTP 请求中，主体内容只不过是下面一行：
		lastName=Franks&firstName=Michael

## http响应
	1. 组成部分：
		-. 方法—统一资源标识符 (URI)—协议 / 版本
		-. 响应的头部
		-. 主体内容

	2. http响应的例子：
		//第一行该协议使用 HTTP 1.1，请求成功 (200= 成功)，表示一切都运行良好。
		HTTP/1.1 200 OK 
		Server: Microsoft-IIS/4.0 
		Date: Mon, 5 Jan 2004 13:13:33 GMT 
		Content-Type: text/html 
		Last-Modified: Mon, 5 Jan 2004 13:13:12 GMT 
		Content-Length: 112 

		<html> 
		    <head> 
		        <title>HTTP Response Example</title> 
		    </head> 
		    <body> 
		        Welcome to Brainy Software 
		    </body> 
		</html>

## 套接字socket
	1. 套接字是网络连接的一个端点。套接字使得一个应用可以从网络中读取和写入数据。放在两 个不同计算机上的两个应用可以通过连接发送和接受字节流。应用和应用之间通过ip和端口进行匹配连接。在 Java语言中套接字是用了java.net.Socket类进行描述的。

	2. public Socket (java.lang.String host, int port) 创建一个套接字
	一旦创建了套接字，就可以使用套接字进行发送和接收字节流
	
	3. ServerSocket 和 Socket 不同，服务器套接字的角色是等待来自客户端的连接请求。一旦服 务器套接字获得一个连接请求，它创建一个 Socket 实例来与客户端进行通信。
	public ServerSocket(int port, int backLog, InetAddress bindingAddress);


## 基本原理
	1. httpServer类，代表web服务器，也就是程序的入口
	~~~java
	public class HttpServer {
	  public static final String WEB_ROOT =
	    System.getProperty("user.dir") + File.separator  + "webroot";

	  // 关闭命令
	  private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

	  // 是否关闭
	  private boolean shutdown = false;

	  public static void main(String[] args) {
	    HttpServer server = new HttpServer();
	    server.await();
	  }
	}
	~~~
	注解，此方法就是在main方法入口中调用了awaite方法，在此方法中调用socket进行等待用户的请求

	2. 看看方法内部：
	~~~java
	public void await() {
	    ServerSocket serverSocket = null;
	    int port = 8080;
	    try {
	      // 创建一个socket服务器
	      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	    }

	    // 循环等待http请求
	    while (!shutdown) {
	      Socket socket = null;
	      InputStream input = null;
	      OutputStream output = null;
	      try {
	        // 阻塞等待http请求
	        socket = serverSocket.accept();
	        input = socket.getInputStream();
	        output = socket.getOutputStream();

	        // 创建一个Request对象用于解析http请求内容
	        Request request = new Request(input);
	        request.parse();

	        // 创建一个Response 对象，用于发送静态文本
	        Response response = new Response(output);
	        response.setRequest(request);
	        response.sendStaticResource();

	        // 关闭流
	        socket.close();

	        //检查URI中是否有关闭命令
	        shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
	      }
	      catch (Exception e) {
	        e.printStackTrace();
	        continue;
	      }
	    }
	  }

	~~~

	3. 通过该方法，创建了一个socket服务器，循环阻塞监听http请求（指定端口监听），当有http请求来的时候，方法创建了一个Request对象，构造参数是socket获取的流对象，构造的过程就是对这个获取到的流对象进行解析，获取来自http请求的内容。

	4. 然后再创建一个response对象，此对象就是封装了需要返回的数据，并构造成流对象，提供给socket进行发送完成一次请求

##总结，这就是http请求的整个过程的基本原理