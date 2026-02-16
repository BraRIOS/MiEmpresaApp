package com.brios.miempresa.core.util

private val SHEETS_URL_REGEX = Regex("""/spreadsheets/d/([a-zA-Z0-9-_]+)""")
private val SHEET_ID_TOKEN_REGEX = Regex("""([a-zA-Z0-9-_]{20,})""")

fun normalizeSheetId(rawValue: String?): String? {
    val normalized = rawValue?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val fromUrl = SHEETS_URL_REGEX.find(normalized)?.groupValues?.getOrNull(1)
    return fromUrl ?: SHEET_ID_TOKEN_REGEX.find(normalized)?.groupValues?.getOrNull(1)
}
