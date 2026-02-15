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
        total: Double,
    ): String {
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"))
        val body =
            buildString {
                appendLine("¡Hola! Quiero hacer este pedido a $companyName:")
                appendLine()
                items.forEach { item ->
                    appendLine("• ${item.productName} x${item.quantity} - ${currencyFormatter.format(item.subtotal)}")
                }
                appendLine()
                appendLine("Total: ${currencyFormatter.format(total)}")
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
