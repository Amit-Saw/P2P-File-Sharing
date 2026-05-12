package P2P.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import P2P.utils.UploadUtils;

public class FileSharer {
  private final ConcurrentHashMap<Integer, String> availableFiles;
  private static final int MAX_PORT_RETRIES = 100;
  
  public FileSharer() {
    this.availableFiles = new ConcurrentHashMap<>();
  }

  public int offerFile(String filePath) {
    for (int attempts = 0; attempts < MAX_PORT_RETRIES; attempts++) {
      int port = UploadUtils.generateCode();
      if (availableFiles.putIfAbsent(port, filePath) == null) {
        return port;
      }
    }
    throw new RuntimeException("Failed to allocate port for file after " + MAX_PORT_RETRIES + " attempts");
  }

  public void startFileServer(int port) {
    String filePath = availableFiles.get(port);
    if(filePath == null){
      System.out.println("File not found for port: " + port);
      return;
    }
    
    try(ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Serving file: " + filePath + " on port: " + port);
      Socket clientSocket = serverSocket.accept();
      // Handle file transfer to clientSocket
      System.err.println("Client connection : " + clientSocket.getInetAddress());
      
      new Thread(new FileSenderHandler(clientSocket, filePath)).start();

    } catch (IOException e) {
      System.err.println("Error starting file server: " + e.getMessage());
    } finally {
      // Clean up the port mapping after server closes
      availableFiles.remove(port);
      System.out.println("File server for port " + port + " stopped and cleaned up");
    }
  }

  private static class FileSenderHandler implements Runnable {
    private final Socket clientSocket;
    private final String filePath;

    public FileSenderHandler(Socket clientSocket, String filePath) {
      this.clientSocket = clientSocket;
      this.filePath = filePath;
    }

    @Override
    public void run() {
      try(FileInputStream fileInputStream = new FileInputStream(filePath);
          OutputStream outputStream = clientSocket.getOutputStream()) {

            String fileName = new File(filePath).getName();
            String header = "FILENAME:" + fileName + "\n";
            outputStream.write(header.getBytes());
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("File " + fileName + " sent to " + clientSocket.getInetAddress());
            outputStream.flush();
        
      } catch (Exception e) {
        System.err.println("Error sending file: " + e.getMessage());
      } finally{
        try {
          clientSocket.close();
        } catch (IOException e) {
          System.err.println("Error closing client socket: " + e.getMessage());
        }
      }
    }
  }
}
