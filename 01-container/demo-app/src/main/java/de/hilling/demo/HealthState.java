package de.hilling.demo;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Health-Zustand der Anwendung. Liegt nur im Speicher – nach einem Neustart
 * ist die Anwendung wieder UP.
 */
@ApplicationScoped
public class HealthState {

    private final AtomicBoolean healthy = new AtomicBoolean(true);

    public boolean isHealthy() {
        return healthy.get();
    }

    public boolean toggle() {
        boolean newValue = !healthy.get();
        healthy.set(newValue);
        return newValue;
    }
}
