<div id="top" align="center">
    <a href="https://github.com/KatsuteDev/simplehttpserver">
        <img src="https://raw.githubusercontent.com/KatsuteDev/simplehttpserver/main/assets/icon.png" alt="icon" width="100" height="100">
    </a>
    <h3>SimpleHttpServer</h3>
    <strong>A simple and efficient HTTP server for Java</strong>
    <div>
        <a href="https://docs.katsute.dev/simplehttpserver">Documentation</a>
        <br>
        <a href="https://mvnrepository.com/artifact/dev.katsute/simplehttpserver">Maven Central</a>
        •
        <a href="https://github.com/KatsuteDev/simplehttpserver/packages/1569580">GitHub Packages</a>
        •
        <a href="https://github.com/KatsuteDev/simplehttpserver/releases">Releases</a>
    </div>
</div>

<br>

> ⚠️ simplehttpserver5 is not compatible with any previous version of [simplehttpserver](https://github.com/Ktt-Development/simplehttpserver).

Simplified httpserver experience for Java 8. Includes extensible servers and handlers for complex operations.

## Installation

This library requires at least Java 8. No additional dependencies are required.

Compiled binaries can be installed from:

 * [Maven Central](https://mvnrepository.com/artifact/dev.katsute/simplehttpserver)
 * [GitHub Packages](https://github.com/KatsuteDev/simplehttpserver/packages/1569580)
 * [Releases](https://github.com/KatsuteDev/simplehttpserver/releases)

Refer to the [documentation](https://docs.katsute.dev/simplehttpserver) to learn how to use servers and handlers.

<div align="right"><a href="#top"><code>▲</code></a></div>

## Features

### Complicated tasks made easy

Simplified exchange methods for:

 * Parsing `GET`/`POST` requests, including `multipart/form-data` support.
 * Accessing cookies.
 * Sending byte arrays, strings, and files to clients.
 * Sending gzip compressed responses.

```java
SimpleHttpHandler handler = new SimpleHttpHandler(){
    @Override
    public void handle(SimpleHttpExchange exchange){
        Map POST = exchange.getPostMap();
        MultipartFormData form = exchange.getMultipartFormData();
        Record record = form.getRecord("record");
        FileRecord file = form.getRecord("file").asFile();
        exchange.send(new File("OK.png"), true);
    }
};
```

### More Features

Features not included with a regular HTTP server:

 * Cookies
 * Sessions
 * Multithreaded Servers

```java
SimpleHttpServer server = new SimpleHttpServer(8080);
server.setHttpSessionHandler(new HttpSessionHandler());
SimpleHttpHandler handler = new SimpleHttpHandler(){
    @Override
    public void handle(SimpleHttpExchange exchange){
        HttpSession session = server.getHttpSession(exchange);
        String session_id = session.getSessionID();
        Map<String,String> cookies = exchange.getCookies();
        exchange.close();
    }
};
```

### Simplified Handlers

Simple and extensible request handlers:

 * Redirect Handler
 * Predicate Handler
 * Root `/` Handler
 * File Handler
 * Server-Sent-Events (SSE) Handler
 * Temporary Handler
 * Timeout Handler
 * Throttled Handler

```java
RedirectHandler redirect = new RedirectHandler("https://github.com/");
FileHandler fileHandler = new FileHandler();
fileHandler.addFile(new File("index.html"));
fileHandler.addDirectory(new File("/site"));
SSEHandler SSE = new SSEHandler();
SSE.push("Server sent events!");
ThrottledHandler throttled = new ThrottledHandler(new ServerExchangeThrottler(), new HttpHandler());
```

<div align="right"><a href="#top"><code>▲</code></a></div>

## Contributing

<!-- Copilot -->
<table>
    <img alt="GitHub Copilot" align="left" src="https://raw.githubusercontent.com/Katsute/Manager/main/assets/copilot-dark.png#gh-dark-mode-only" width="50">
    <img alt="Open AI" align="left" src="https://raw.githubusercontent.com/Katsute/Manager/main/assets/openai-dark.png#gh-dark-mode-only" width="50">
    <img alt="GitHub Copilot" align="left" src="https://raw.githubusercontent.com/Katsute/Manager/main/assets/copilot-light.png#gh-light-mode-only" width="50">
    <img alt="Open AI" align="left" src="https://raw.githubusercontent.com/Katsute/Manager/main/assets/openai-light.png#gh-light-mode-only" width="50">
    <p>AI generated code is <b>strictly prohibited</b> on this repository.</p>
</table>
<!-- Copilot -->

#### Running Tests Locally

For local tests you can use Java 8+, however only methods in the Java 8 API may be used. The `src/main/java9` folder should not be marked as a source root.

#### Running Tests using GitHub Actions

Each commit automatically triggers the Java CI workflow, make sure you have actions enabled on your forks.

<div align="right"><a href="#top"><code>▲</code></a></div>

## &nbsp;

This library is released under the [GNU General Public License (GPL) v2.0](https://github.com/KatsuteDev/simplehttpserver/blob/main/LICENSE).