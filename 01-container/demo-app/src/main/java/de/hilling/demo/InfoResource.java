package de.hilling.demo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Zeigt Version, Hostname (= Pod-Name) und eine konfigurierbare Nachricht – nützlich,
 * um Load-Balancing, Rolling Updates und Konfiguration (Kapitel 21) sichtbar zu machen.
 */
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
public class InfoResource {

    @ConfigProperty(name = "demo.version", defaultValue = "dev")
    String version;

    @ConfigProperty(name = "demo.message", defaultValue = "Hallo aus der Demo-App")
    String message;

    @GET
    public Map<String, String> info() throws UnknownHostException {
        return Map.of("version", version, "hostname", InetAddress.getLocalHost().getHostName(), "message", message);
    }
}
