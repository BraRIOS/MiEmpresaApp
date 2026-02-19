package com.brios.miempresa.cart.domain

import javax.inject.Inject

sealed interface CheckoutValidationDecision {
    data object ProceedToWhatsApp : CheckoutValidationDecision

    data object ShowPricesUpdatedNotice : CheckoutValidationDecision

    data object ShowItemsUnavailableError : CheckoutValidationDecision

    data object ShowBlockedError : CheckoutValidationDecision
}

class ResolveCheckoutValidationDecisionUseCase
    @Inject
    constructor() {
        operator fun invoke(result: PriceValidationResult): CheckoutValidationDecision {
            return when (result) {
                PriceValidationResult.AllValid -> CheckoutValidationDecision.ProceedToWhatsApp
                is PriceValidationResult.PricesUpdated -> CheckoutValidationDecision.ShowPricesUpdatedNotice
                is PriceValidationResult.ItemsUnavailable -> CheckoutValidationDecision.ShowItemsUnavailableError
                PriceValidationResult.Blocked -> CheckoutValidationDecision.ShowBlockedError
            }
        }
    }
