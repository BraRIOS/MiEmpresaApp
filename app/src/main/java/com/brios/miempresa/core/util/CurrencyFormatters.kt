package com.brios.miempresa.core.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

private val ArgentinaLocale = Locale.forLanguageTag("es-AR")

fun formatCurrencyAr(value: Double): String =
    NumberFormat.getCurrencyInstance(ArgentinaLocale).format(value)

fun formatPlainDecimal(value: Double): String =
    BigDecimal.valueOf(value)
        .stripTrailingZeros()
        .toPlainString()
