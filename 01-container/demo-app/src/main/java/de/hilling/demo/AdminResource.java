package de.hilling.demo;

import io.quarkus.runtime.Quarkus;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Endpunkte, um Fehler zu simulieren (Kapitel 10).
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    HealthState state;

    /** Kippt den Health-Zustand (UP ↔ DOWN). */
    @POST
    @Path("health/toggle")
    public Map<String, String> toggleHealth() {
        return Map.of("status", state.toggle() ? "UP" : "DOWN");
    }

    /** Beendet den Prozess nach kurzer Verzögerung (simulierter Absturz). */
    @POST
    @Path("restart")
    public Response restart() {
        Thread shutdown = new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Quarkus.asyncExit(1);
        });
        shutdown.setDaemon(true);
        shutdown.start();
        return Response.accepted(Map.of("status", "restarting")).build();
    }
}
