package de.hilling.demo;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Health-Endpunkt für die Probes (Kapitel 10): 200 bei UP, 503 bei DOWN.
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    HealthState state;

    @GET
    public Response health() {
        boolean healthy = state.isHealthy();
        Response.Status status = healthy ? Response.Status.OK : Response.Status.SERVICE_UNAVAILABLE;
        return Response.status(status).entity(Map.of("status", healthy ? "UP" : "DOWN")).build();
    }
}
