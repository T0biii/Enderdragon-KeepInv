package dev.t0biii.enderdragonkeepinv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCheckerTest {

    @Test
    void detectsNewerVersions() {
        assertTrue(UpdateChecker.isNewer("v1.0.1", "1.0.0"));
        assertTrue(UpdateChecker.isNewer("1.1.0", "1.0.9"));
        assertTrue(UpdateChecker.isNewer("2.0.0", "1.99.99"));
        assertTrue(UpdateChecker.isNewer("1.0.10", "1.0.9"));
        assertTrue(UpdateChecker.isNewer("1.0.0.1", "1.0.0"));
    }

    @Test
    void ignoresSameOrOlderVersions() {
        assertFalse(UpdateChecker.isNewer("v1.0.0", "1.0.0"));
        assertFalse(UpdateChecker.isNewer("1.0.0", "1.0.1"));
        assertFalse(UpdateChecker.isNewer("1.0", "1.0.0"));
        assertFalse(UpdateChecker.isNewer("1.0.0", "1.0.0-SNAPSHOT"));
    }
}
