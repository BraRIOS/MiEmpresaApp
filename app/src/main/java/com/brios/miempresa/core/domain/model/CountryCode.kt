package com.brios.miempresa.core.domain.model

data class CountryCode(
    val isoCode: String,
    val emoji: String,
    val dialCode: String,
    val name: String
) {
    companion object {
        fun fromIso(isoCode: String, dialCode: String, name: String): CountryCode {
            val emoji = isoCode.uppercase().let { iso ->
                if (iso.length == 2) {
                    // Convert two-letter ISO code to emoji flag
                    val offset = 0x1F1E6 - 'A'.code
                    buildString {
                        iso.forEach { char ->
                            val codePoint = char.code + offset
                            append(String(Character.toChars(codePoint)))
                        }
                    }
                } else {
                    iso // Fallback if code is invalid
                }
            }
            return CountryCode(isoCode, emoji, dialCode, name)
        }
    }
}

val defaultCountryCodes =
    listOf(
        CountryCode.fromIso("AR", "+54", "Argentina"),
        CountryCode.fromIso("MX", "+52", "México"),
        CountryCode.fromIso("CO", "+57", "Colombia"),
        CountryCode.fromIso("CL", "+56", "Chile"),
        CountryCode.fromIso("BR", "+55", "Brasil"),
        CountryCode.fromIso("PE", "+51", "Perú"),
        CountryCode.fromIso("UY", "+598", "Uruguay"),
        CountryCode.fromIso("EC", "+593", "Ecuador"),
        CountryCode.fromIso("US", "+1", "Estados Unidos"),
        CountryCode.fromIso("ES", "+34", "España"),
    )
