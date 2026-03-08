package com.brios.miempresa.cart.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
import java.util.Locale

object WhatsAppHelper {
    fun buildMessage(
        items: List<CartItem>,
        companyName: String,
    ): String {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
        val hasHiddenPrices = items.any { it.productHidePrice }
        val visibleTotal = items.filterNot { it.productHidePrice }.sumOf { it.subtotal }
        val body =
            buildString {
                appendLine("¡Hola! Quiero hacer este pedido a $companyName:")
                appendLine()
                items.forEach { item ->
                    val priceLabel =
                        if (item.productHidePrice) {
                            "Consultar"
                        } else {
                            currencyFormatter.format(item.subtotal)
                        }
                    appendLine("• ${item.productName} x${item.quantity} - $priceLabel")
                }
                appendLine()
                val totalLabel =
                    if (hasHiddenPrices) {
                        "A consultar"
                    } else {
                        currencyFormatter.format(visibleTotal)
                    }
                appendLine("Total: $totalLabel")
                appendLine()
                append("Enviado desde MiEmpresa")
            }
        return body.trim()
    }

    fun openChat(
        context: Context,
        phoneNumber: String,
        message: String,
    ): Boolean {
        val normalizedPhone = phoneNumber.replace(Regex("\\D"), "")
        if (normalizedPhone.isBlank()) return false

        val url = "https://wa.me/$normalizedPhone?text=${Uri.encode(message)}"
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
