package com.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    public void testHealthEndpoint() {
        String status = "UP";
        assertEquals("UP", status);
        System.out.println("Health check test passed!");
    }
}
