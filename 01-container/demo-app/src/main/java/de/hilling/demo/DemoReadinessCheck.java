package de.hilling.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness-Check für /q/health/ready (Kapitel 21): spiegelt den Zustand, den
 * POST /admin/health/toggle umschaltet. Die Liveness (/q/health/live) bleibt davon unberührt.
 */
@Readiness
@ApplicationScoped
public class DemoReadinessCheck implements HealthCheck {

    @Inject
    HealthState state;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("demo-app").status(state.isHealthy()).build();
    }
}
