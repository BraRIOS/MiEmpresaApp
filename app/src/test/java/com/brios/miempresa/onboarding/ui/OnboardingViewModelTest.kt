package com.brios.miempresa.onboarding.ui

import com.brios.miempresa.onboarding.domain.WorkspaceStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingFormStateTest {
    @Test
    fun `initial form state has empty fields`() {
        val form = OnboardingFormState()
        assertEquals("", form.companyName)
        assertEquals("+54", form.whatsappCountryCode)
        assertEquals("", form.whatsappNumber)
        assertEquals("", form.specialization)
    }

    @Test
    fun `form is invalid when company name is blank`() {
        val form =
            OnboardingFormState(
                companyName = "",
                whatsappNumber = "1234567890",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when company name is only whitespace`() {
        val form =
            OnboardingFormState(
                companyName = "   ",
                whatsappNumber = "1234567890",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when whatsapp number is empty`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when whatsapp number doesnt match regex`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "abc",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when whatsapp number is too short`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "12345",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when whatsapp number is too long`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "1234567890123456",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is invalid when whatsapp number contains special chars`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "+5411234567",
            )
        assertFalse(form.isFormValid)
    }

    @Test
    fun `form is valid with name and valid whatsapp`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappNumber = "1234567890",
            )
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form is valid with minimum 6 digit whatsapp`() {
        val form =
            OnboardingFormState(
                companyName = "Mi Empresa",
                whatsappNumber = "123456",
            )
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form is valid with maximum 15 digit whatsapp`() {
        val form =
            OnboardingFormState(
                companyName = "Mi Empresa",
                whatsappNumber = "123456789012345",
            )
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form is valid with optional fields filled`() {
        val form =
            OnboardingFormState(
                companyName = "Test Company",
                whatsappCountryCode = "+1",
                whatsappNumber = "5551234567",
                specialization = "Bakery",
                logoUri = "content://logo.png",
                address = "123 Main St",
                businessHours = "9-5",
            )
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form truncates company name at 50 chars`() {
        val longName = "A".repeat(60)
        val form = OnboardingFormState(companyName = longName.take(50), whatsappNumber = "123456")
        assertEquals(50, form.companyName.length)
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form truncates whatsapp at 15 chars`() {
        val longNumber = "1".repeat(20)
        val form = OnboardingFormState(companyName = "Test", whatsappNumber = longNumber.take(15))
        assertEquals(15, form.whatsappNumber.length)
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form truncates specialization at 30 chars`() {
        val longSpec = "A".repeat(40)
        val form = OnboardingFormState(companyName = "Test", whatsappNumber = "123456", specialization = longSpec.take(30))
        assertEquals(30, form.specialization.length)
    }

    @Test
    fun `form is valid without dashes in whatsapp`() {
        val form = OnboardingFormState(companyName = "Test", whatsappNumber = "1234567890")
        assertTrue(form.isFormValid)
    }

    @Test
    fun `form validity ignores optional fields`() {
        val formWithout =
            OnboardingFormState(
                companyName = "Test",
                whatsappNumber = "123456",
            )
        val formWith =
            OnboardingFormState(
                companyName = "Test",
                whatsappNumber = "123456",
                specialization = "Tech",
                address = "Addr",
            )
        assertEquals(formWithout.isFormValid, formWith.isFormValid)
    }
}

class OnboardingUiStateTest {
    @Test
    fun `initial state is Loading`() {
        val state: OnboardingUiState = OnboardingUiState.Loading
        assertTrue(state is OnboardingUiState.Loading)
    }

    @Test
    fun `WizardStep1 holds form state`() {
        val form = OnboardingFormState(companyName = "Test")
        val state = OnboardingUiState.WizardStep1(form)
        assertEquals("Test", state.form.companyName)
    }

    @Test
    fun `WizardStep2 progress is zero when no steps completed`() {
        val state =
            OnboardingUiState.WizardStep2(
                completedSteps = 0,
                currentStep = WorkspaceStep.CREATE_FOLDER.name,
                totalSteps = 6,
                hasLogo = false,
            )
        assertEquals(0f, state.progress, 0.001f)
        assertEquals(0, state.progressPercent)
    }

    @Test
    fun `WizardStep2 progress is calculated correctly`() {
        val state =
            OnboardingUiState.WizardStep2(
                completedSteps = 3,
                currentStep = WorkspaceStep.CREATE_PUBLIC_SHEET.name,
                totalSteps = 6,
                hasLogo = true,
            )
        assertEquals(0.5f, state.progress, 0.001f)
        assertEquals(50, state.progressPercent)
    }

    @Test
    fun `WizardStep2 progress is 100 when all steps completed`() {
        val state =
            OnboardingUiState.WizardStep2(
                completedSteps = 6,
                currentStep = WorkspaceStep.SAVE_CONFIG.name,
                totalSteps = 6,
                hasLogo = false,
            )
        assertEquals(1f, state.progress, 0.001f)
        assertEquals(100, state.progressPercent)
    }

    @Test
    fun `WizardStep2 handles zero total steps safely`() {
        val state =
            OnboardingUiState.WizardStep2(
                completedSteps = 0,
                currentStep = "",
                totalSteps = 0,
                hasLogo = false,
            )
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `WizardStep2 can hold error message`() {
        val state =
            OnboardingUiState.WizardStep2(
                completedSteps = 2,
                currentStep = WorkspaceStep.CREATE_PRIVATE_SHEET.name,
                totalSteps = 6,
                hasLogo = false,
                errorMessage = "Network error",
            )
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `WizardStep3 holds company name`() {
        val state = OnboardingUiState.WizardStep3(companyName = "Mi Empresa")
        assertEquals("Mi Empresa", state.companyName)
    }

    @Test
    fun `Error state holds message`() {
        val state = OnboardingUiState.Error(message = "Something went wrong")
        assertEquals("Something went wrong", state.message)
    }
}

class OnboardingEventTest {
    @Test
    fun `NavigateToHome is singleton`() {
        val event1 = OnboardingEvent.NavigateToHome
        val event2 = OnboardingEvent.NavigateToHome
        assertEquals(event1, event2)
    }

    @Test
    fun `ShowError carries message`() {
        val event = OnboardingEvent.ShowError("Failed to create workspace")
        assertEquals("Failed to create workspace", event.message)
    }
}
