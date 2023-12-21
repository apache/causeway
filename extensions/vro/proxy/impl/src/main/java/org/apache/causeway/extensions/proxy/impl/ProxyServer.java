package org.apache.causeway.extensions.proxy.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


@Service
@Slf4j
public class ProxyServer {
    public static void run() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(9999);
        log.info("Proxy server running on port 9999");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    final Socket inputSocket = new Socket("destination_server_address", 80);
                    final InputStream clientInput = clientSocket.getInputStream();
                    final OutputStream clientOutput = clientSocket.getOutputStream();
                    final InputStream serverInput = inputSocket.getInputStream();
                    final OutputStream serverOutput = inputSocket.getOutputStream();

                    // Transfer data from client to server
                    new Thread(() -> {
                        try {
                            int data;
                            while ((data = clientInput.read()) != -1) {
                                serverOutput.write(data);
                            }
                        } catch (IOException e) {
                            log.error("Error reading from client", e);
                        }
                    }).start();

                    // Transfer data from server to client
                    new Thread(() -> {
                        try {
                            int data;
                            while ((data = serverInput.read()) != -1) {
                                clientOutput.write(data);
                            }
                        } catch (IOException e) {
                            log.error("Error writing to client", e);
                        }
                    }).start();
                } catch (IOException e) {
                    log.error("Error creating socket for <destination_server_address>");
                }
            }).start();
        }
    }
}
