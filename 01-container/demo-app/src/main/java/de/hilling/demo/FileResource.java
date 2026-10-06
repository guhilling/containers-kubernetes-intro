package de.hilling.demo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Liefert Dateien aus dem HTML-Verzeichnis aus (Default: /var/demo/html,
 * konfigurierbar über die Umgebungsvariable DEMO_HTML_DIR).
 */
@Path("/")
public class FileResource {

    @ConfigProperty(name = "demo.html-dir", defaultValue = "/var/demo/html")
    String htmlDir;

    @GET
    public Response index() throws IOException {
        return serveFile("index.html");
    }

    @GET
    @Path("{filename: [^/]+}")
    public Response file(@PathParam("filename") String filename) throws IOException {
        return serveFile(filename);
    }

    private Response serveFile(String filename) throws IOException {
        java.nio.file.Path baseDir = java.nio.file.Path.of(htmlDir).toAbsolutePath().normalize();
        java.nio.file.Path filePath = baseDir.resolve(filename).normalize();
        if (!filePath.startsWith(baseDir)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!Files.isRegularFile(filePath)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String mediaType = filename.endsWith(".html") ? MediaType.TEXT_HTML : MediaType.TEXT_PLAIN;
        return Response.ok(Files.readString(filePath), mediaType + ";charset=UTF-8").build();
    }
}
