package com.example.proyectopanaderia.domain.validation

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Money {
    fun parseCents(value: String): Long? = try {
        val text = value.trim().replace(',', '.')
        if (!Regex("^[0-9]+(\\.[0-9]{1,2})?$").matches(text)) null
        else BigDecimal(text).movePointRight(2).longValueExact().takeIf { it in 0..100_000_000L }
    } catch (_: ArithmeticException) { null }
    catch (_: NumberFormatException) { null }
    fun editValue(cents: Long): String = BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString()
    fun format(cents: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-EC")).apply {
        currency = Currency.getInstance("USD")
    }.format(BigDecimal.valueOf(cents, 2))
}
