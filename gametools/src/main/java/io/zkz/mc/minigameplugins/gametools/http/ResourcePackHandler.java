package io.zkz.mc.minigameplugins.gametools.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourcePackService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourcePackHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Ensure it is a GET request
        if (!exchange.getRequestMethod().equals("GET")) {
            String response = "Invalid method";
            exchange.sendResponseHeaders(405, response.length());
            return;
        }

        // Get resource pack path
        Path resourcePack = ResourcePackService.getInstance().getResourcePackPath();

        // Write response
        byte[] response = Files.readAllBytes(resourcePack);
        exchange.sendResponseHeaders(200, response.length);
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Content-Type", "application/zip");
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}
