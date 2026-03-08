package com.brios.miempresa

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivitySheetIdParserTest {
    @Test
    fun `parse incoming payload supports clean sheet id from shared text`() {
        assertEquals(
            SHEET_ID,
            extractSheetIdFromIncomingPayload(
                action = Intent.ACTION_SEND,
                deeplinkPayload = null,
                sharedTextPayload = SHEET_ID,
            ),
        )
    }

    @Test
    fun `parse incoming payload supports sheets url from shared text`() {
        assertEquals(
            SHEET_ID,
            extractSheetIdFromIncomingPayload(
                action = Intent.ACTION_SEND,
                deeplinkPayload = null,
                sharedTextPayload = "https://docs.google.com/spreadsheets/d/$SHEET_ID/edit?gid=0",
            ),
        )
    }

    @Test
    fun `parse incoming payload supports prefixed chat text from shared text`() {
        assertEquals(
            SHEET_ID,
            extractSheetIdFromIncomingPayload(
                action = Intent.ACTION_SEND,
                deeplinkPayload = null,
                sharedTextPayload = "Mi catálogo en MiEmpresa: $SHEET_ID",
            ),
        )
    }

    @Test
    fun `parse incoming payload supports custom deeplink text`() {
        assertEquals(
            SHEET_ID,
            extractSheetIdFromIncomingPayload(
                action = Intent.ACTION_VIEW,
                deeplinkPayload = "miempresa://catalogo?sheetId=Mi catálogo en MiEmpresa: $SHEET_ID",
                sharedTextPayload = null,
            ),
        )
    }

    @Test
    fun `parse incoming payload falls back to deeplink when shared text is missing`() {
        assertEquals(
            SHEET_ID,
            extractSheetIdFromIncomingPayload(
                action = Intent.ACTION_SEND,
                deeplinkPayload = "miempresa://catalogo?sheetId=$SHEET_ID",
                sharedTextPayload = null,
            ),
        )
    }

    private companion object {
        const val SHEET_ID = "1AbCDeFgHiJkLmNoPqRsTuVwXyZ_1234567890"
    }
}
