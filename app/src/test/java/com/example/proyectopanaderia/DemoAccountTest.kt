package com.example.proyectopanaderia
import com.example.proyectopanaderia.domain.auth.DemoAccount

import org.junit.Assert.*
import org.junit.Test

class DemoAccountTest {
    @Test fun acceptsOnlyTheDemoCredentials() {
        assertTrue(DemoAccount.accepts("  DEMO@PANADERIA.COM ", "Pan12345"))
        assertFalse(DemoAccount.accepts(DemoAccount.email, "incorrecta"))
        assertFalse(DemoAccount.accepts("otra@panaderia.com", DemoAccount.password))
        assertFalse(DemoAccount.accepts(DemoAccount.email, "pan12345"))
        assertFalse(DemoAccount.accepts(DemoAccount.email, " Pan12345 "))
    }

    @Test fun distinguishesMissingOrMalformedEmail() {
        assertNotNull(DemoAccount.emailError(""))
        assertNotNull(DemoAccount.emailError("demo@"))
        assertNotNull(DemoAccount.emailError("de mo@panaderia.com"))
        assertNull(DemoAccount.emailError(" demo@panaderia.com "))
    }
}
