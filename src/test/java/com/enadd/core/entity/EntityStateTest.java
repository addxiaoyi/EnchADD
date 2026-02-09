package com.enadd.core.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityStateTest {

    @Test
    public void testStateIsAlive() {
        assertTrue(EntityState.ACTIVE.isAlive());
        assertTrue(EntityState.RUNNING.isAlive());
        assertTrue(EntityState.ACTIVATING.isAlive());
        assertFalse(EntityState.DESTROYED.isAlive());
        assertFalse(EntityState.PAUSED.isAlive());
    }

    @Test
    public void testStateIsValid() {
        assertTrue(EntityState.INIT.isValid());
        assertTrue(EntityState.ACTIVE.isValid());
        assertTrue(EntityState.RUNNING.isValid());
        assertFalse(EntityState.DESTROYED.isValid());
        assertFalse(EntityState.ORPHAN.isValid());
    }

    @Test
    public void testStateIsTransitioning() {
        assertTrue(EntityState.ACTIVATING.isTransitioning());
        assertTrue(EntityState.DESTROYING.isTransitioning());
        assertTrue(EntityState.PAUSED.isTransitioning());
        assertFalse(EntityState.ACTIVE.isTransitioning());
        assertFalse(EntityState.DESTROYED.isTransitioning());
    }
}
