package P2P.controller;

import P2P.Service.FileSharer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
  private final FileSharer fileSharer;
  private final HttpServer httpServer;
  private final String uploadDir;
  private final String downloadDir;
  private final ExecutorService executorService;

  public FileController() throws IOException {
    this(8080);
  }

  public FileController(int port) throws IOException {
    this.fileSharer = new FileSharer();
    this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    this.uploadDir = System.getProperty("user.home") + File.separator + "P2PUploads";
    this.downloadDir = System.getProperty("user.home") + File.separator + "P2PDownloads";
    this.executorService = Executors.newCachedThreadPool();

    Files.createDirectories(Paths.get(uploadDir));
    Files.createDirectories(Paths.get(downloadDir));

    httpServer.createContext("/upload", new UploadHandler());
    httpServer.createContext("/download", new DownloadHandler());
    httpServer.createContext("/", new CorsHandler());
    httpServer.setExecutor(executorService);
  }

  public void start() {
    httpServer.start();
    System.out.println("API server started on port " + httpServer.getAddress().getPort());
  }

  public void stop() {
    httpServer.stop(0);
    executorService.shutdown();
    System.out.println("API server stopped.");
  }

  private static void applyCors(Headers headers) {
    headers.set("Access-Control-Allow-Origin", "*");
    headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    headers.set("Access-Control-Allow-Headers", "Content-Type");
  }

  private static void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.sendResponseHeaders(statusCode, bytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(bytes);
    }
  }

  private static Map<String, String> parseQuery(String query) {
    Map<String, String> params = new HashMap<>();
    if (query == null || query.isBlank()) {
      return params;
    }
    for (String part : query.split("&")) {
      String[] keyValue = part.split("=", 2);
      String key = keyValue[0];
      String value = keyValue.length > 1 ? keyValue[1] : "";
      params.put(key, value);
    }
    return params;
  }

  private static byte[] readAllBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, bytesRead);
    }
    return baos.toByteArray();
  }

  private static UploadedFile parseMultipart(byte[] body, String boundary) throws IOException {
    String bodyText = new String(body, StandardCharsets.ISO_8859_1);
    String marker = "filename=\"";
    int fileNameStart = bodyText.indexOf(marker);
    if (fileNameStart < 0) {
      throw new IOException("Filename not found in multipart data");
    }
    fileNameStart += marker.length();
    int fileNameEnd = bodyText.indexOf('"', fileNameStart);
    if (fileNameEnd < 0) {
      throw new IOException("Filename not found in multipart data");
    }

    int partHeaderEnd = bodyText.indexOf("\r\n\r\n", fileNameEnd);
    if (partHeaderEnd < 0) {
      throw new IOException("Multipart header terminator not found");
    }

    int fileContentStart = partHeaderEnd + 4;
    String closingBoundary = "\r\n--" + boundary;
    int fileContentEnd = bodyText.indexOf(closingBoundary, fileContentStart);
    if (fileContentEnd < 0) {
      fileContentEnd = body.length;
    }

    String fileName = bodyText.substring(fileNameStart, fileNameEnd);
    byte[] fileContent = new byte[fileContentEnd - fileContentStart];
    System.arraycopy(body, fileContentStart, fileContent, 0, fileContent.length);
    return new UploadedFile(fileName, fileContent);
  }

  private final class CorsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      applyCors(exchange.getResponseHeaders());
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1);
      } else {
        exchange.sendResponseHeaders(200, -1);
      }
      exchange.close();
    }
  }

  private final class UploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      applyCors(exchange.getResponseHeaders());

      if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        writeResponse(exchange, 405, "Method Not Allowed");
        return;
      }

      String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
      if (contentType == null || !contentType.startsWith("multipart/form-data")) {
        writeResponse(exchange, 400, "Unsupported Media Type");
        return;
      }

      int boundaryIndex = contentType.indexOf("boundary=");
      if (boundaryIndex < 0) {
        writeResponse(exchange, 400, "Multipart boundary missing");
        return;
      }

      String boundary = contentType.substring(boundaryIndex + "boundary=".length());
      byte[] requestBody = readAllBytes(exchange.getRequestBody());
      UploadedFile uploadedFile = parseMultipart(requestBody, boundary);

      String safeFileName = Paths.get(uploadedFile.fileName).getFileName().toString();
      Path filePath = Paths.get(uploadDir, System.currentTimeMillis() + "_" + safeFileName);
      Files.write(filePath, uploadedFile.content);

      int port = fileSharer.offerFile(filePath.toString());
      new Thread(() -> fileSharer.startFileServer(port)).start();

      String jsonResponse = "{\"port\": " + port + ", \"fileName\": \"" + safeFileName + "\"}";
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      writeResponse(exchange, 200, jsonResponse);
    }
  }

  private final class DownloadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      applyCors(exchange.getResponseHeaders());

      if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
        writeResponse(exchange, 405, "Method Not Allowed");
        return;
      }

      Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
      String portValue = params.get("port");
      if (portValue == null || portValue.isBlank()) {
        writeResponse(exchange, 400, "{\"error\": \"Missing port parameter\"}");
        return;
      }

      int port;
      try {
        port = Integer.parseInt(portValue);
      } catch (NumberFormatException e) {
        writeResponse(exchange, 400, "{\"error\": \"Invalid port parameter\"}");
        return;
      }

      if (port < 1 || port > 65535) {
        writeResponse(exchange, 400, "{\"error\": \"Port must be between 1 and 65535\"}");
        return;
      }

      Path tempFile = null;
      String fileName = "downloaded_file";

      try {
        tempFile = Files.createTempFile(Paths.get(downloadDir), "download_", ".tmp");
        
        Socket socket = null;
        try {
          socket = new Socket("localhost", port);
          InputStream inputStream = socket.getInputStream();
          FileOutputStream fos = new FileOutputStream(tempFile.toFile());

          ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
          int b;
          int timeoutCount = 0;
          while ((b = inputStream.read()) != -1 && timeoutCount < 1000) {
            if (b == '\n') {
              break;
            }
            headerBuffer.write(b);
            timeoutCount++;
          }

          String header = headerBuffer.toString(StandardCharsets.UTF_8);
          if (header.startsWith("FILENAME:")) {
            fileName = header.substring("FILENAME:".length()).trim();
          }

          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
          }
          fos.close();
          inputStream.close();
          
        } catch (IOException e) {
          writeResponse(exchange, 503, "{\"error\": \"Failed to connect to file server on port " + port + ": " + e.getMessage() + "\"}");
          return;
        } finally {
          if (socket != null) {
            try {
              socket.close();
            } catch (IOException e) {
              System.err.println("Error closing socket: " + e.getMessage());
            }
          }
        }

        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.sendResponseHeaders(200, Files.size(tempFile));

        try (OutputStream responseBody = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(tempFile.toFile())) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = fis.read(buffer)) != -1) {
            responseBody.write(buffer, 0, bytesRead);
          }
        }
      } catch (Exception e) {
        System.err.println("Error in DownloadHandler: " + e.getMessage());
        e.printStackTrace();
        try {
          writeResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        } catch (Exception ignored) {}
      } finally {
        if (tempFile != null) {
          try {
            Files.deleteIfExists(tempFile);
          } catch (IOException e) {
            System.err.println("Failed to delete temp file: " + e.getMessage());
          }
        }
      }
    }
  }

  private static final class UploadedFile {
    private final String fileName;
    private final byte[] content;

    private UploadedFile(String fileName, byte[] content) {
      this.fileName = fileName;
      this.content = content;
    }
  }
}