package com.example.proyectopanaderia

import com.example.proyectopanaderia.domain.validation.Money
import org.junit.Assert.*
import org.junit.Test

class MoneyTest {
    @Test fun acceptsBothDecimalSeparatorsWithoutFloatingPointRounding() {
        assertEquals(150L, Money.parseCents("1,50"))
        assertEquals(325L, Money.parseCents("3.25"))
        assertEquals(0L, Money.parseCents("0"))
        assertEquals("3.25", Money.editValue(325))
    }
    @Test fun rejectsAmbiguousNegativeOrExcessivePrices() {
        listOf("", "-1", "1.999", "1,234.56", "1e3", "1000001", "999999999999999999999999").forEach {
            assertNull("Invalid price: $it", Money.parseCents(it))
        }
    }
}
