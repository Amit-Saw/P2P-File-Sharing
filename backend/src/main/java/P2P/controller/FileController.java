package P2P.controller;

public class FileController {
  private final FileSharer fileSharer;
  private final HttpServer httpServer;
  private final String uploadDir;
  private final String downloadDir;
  private final ExecutorService executorService;

  public FileController() throws IOException {
    this.fileSharer = new FileSharer();
    this.httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
    this.uploadDir = System.getProperty("user.home") + File.separator + "P2PUploads";
    this.downloadDir = System.getProperty("user.home") + File.separator + "P2PDownloads";
    this.executorService = Executors.newCachedThreadPool(10);

    File upoladDirectory = new File(uploadDir);
    File downloadDirectory = new File(downloadDir);

    if(!uploadDirFile.exists()){
      uploadDirFile.mkdirs();
    }
    
    server.createContext("/upload", new UploadHandler(uploadDir, fileSharer));
    server.createContext("/download", new DownloadHandler(downloadDir));
    server.createContext("/", new CORSHandler());
    server.setExecutor(executorService);

    
    // Create upload and download directories if they don't exist
    new File(uploadDir).mkdirs();
    new File(downloadDir).mkdirs();
  }

  public void start() {
    server.start();
    System.out.println("API server started on port " + server.getAddress().getPort());
  }

  public void stop() {
    server.stop(0);
    executorService.shutdown();
    System.out.println("API server stopped.");
  }

  private class CORSHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      Headers headers = exchange.getResponseHeaders();
      headers.add("Access-Control-Allow-Origin", "*");
      headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
      headers.add("Access-Control-Allow-Headers", "Content-Type");

      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        exchange.sendResponseHeaders(204, -1); // No content for preflight
        return;
      }

      // For other requests, just respond with 200 OK
      exchange.sendResponseHeaders(200, -1);

    }
  } 

  private class UploadHandler implements HttpHandler {
    private final String uploadDir;
    private final FileSharer fileSharer;

    public UploadHandler(String uploadDir, FileSharer fileSharer) {
      this.uploadDir = uploadDir;
      this.fileSharer = fileSharer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        // Handle file upload
        String fileName = "uploaded_" + System.currentTimeMillis();
        File uploadedFile = new File(uploadDir, fileName);
        
        try (InputStream is = exchange.getRequestBody();
             FileOutputStream fos = new FileOutputStream(uploadedFile)) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
          }
        }

        int port = fileSharer.offerFile(uploadedFile.getAbsolutePath());
        String response = "File uploaded successfully. Access it at: http://localhost:" + port;
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      } else {
        exchange.sendResponseHeaders(405, -1); // Method Not Allowed
      }
    }
  }
}
