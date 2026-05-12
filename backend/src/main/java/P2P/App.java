package P2P;

import P2P.controller.FileController;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            int port = 8080;
            String portEnv = System.getenv("PORT");
            if (portEnv != null && !portEnv.isBlank()) {
                port = Integer.parseInt(portEnv);
            }
            FileController fileController = new FileController(port);
            fileController.start();
            System.out.println("FileController started on port " + port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                fileController.stop();
                System.out.println("FileController stopped");
            }));

            System.out.println("Press Ctrl+C to stop the server...");
            System.in.read();

        } catch (Exception e) {
            System.err.println("Error starting FileController: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
