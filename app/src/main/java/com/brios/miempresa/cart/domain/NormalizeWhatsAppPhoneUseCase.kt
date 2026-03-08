package com.brios.miempresa.cart.domain

import javax.inject.Inject

class NormalizeWhatsAppPhoneUseCase
    @Inject
    constructor() {
        operator fun invoke(value: String): String? {
            val normalized = value.replace(Regex("\\D"), "")
            return normalized.takeIf { it.isNotBlank() }
        }
    }
