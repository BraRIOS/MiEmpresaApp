package com.brios.miempresa.categories.domain

object EmojiData {
    val food =
        listOf(
            "🍕", "🍔", "🌮", "🍣", "🍩", "🍰", "🍪", "🥗", "🍝", "🥐",
            "🍦", "🧁", "🥤", "☕", "🍷", "🍺", "🥂", "🧃", "🍽️", "🥘",
        )

    val objects =
        listOf(
            "📱", "💻", "🖥️", "⌚", "📷", "🎧", "🔧", "🔨", "💡", "🔑",
            "📦", "🎁", "🧰", "🪛", "🔩", "📐", "✂️", "📎", "🖊️", "📝",
        )

    val clothing =
        listOf(
            "👕", "👖", "👗", "👟", "👠", "🧢", "👜", "👓", "🧤", "🧣",
            "🧥", "👔", "👘", "🥿", "🩴", "🎒", "💼", "🧳", "👑", "💍",
        )

    val symbols =
        listOf(
            "⭐", "❤️", "🔥", "✨", "💎", "🏆", "🎯", "💰", "🏷️", "📊",
            "✅", "❌", "⚡", "🔔", "💬", "📌", "🎵", "🌈", "♻️", "🔒",
        )

    val nature =
        listOf(
            "🌿", "🌸", "🌻", "🍀", "🌲", "🍂", "🌊", "☀️", "🌙", "⛰️",
            "🐾", "🦋", "🐝", "🌺", "🍄", "🌵", "🌾", "🍃", "💐", "🪻",
        )

    val travel =
        listOf(
            "🏠", "🏢", "🏪", "🚗", "✈️", "🚀", "🛒", "🏥", "🏫", "🏭",
            "🗺️", "🏖️", "⛺", "🎡", "🏟️", "🚌", "🚲", "⛽", "🅿️", "🚧",
        )

    val allEmojis: List<String> = food + objects + clothing + symbols + nature + travel

    data class EmojiCategory(
        val name: String,
        val emojis: List<String>,
    )

    val categories =
        listOf(
            EmojiCategory("Comida", food),
            EmojiCategory("Objetos", objects),
            EmojiCategory("Ropa", clothing),
            EmojiCategory("Símbolos", symbols),
            EmojiCategory("Naturaleza", nature),
            EmojiCategory("Viajes", travel),
        )
}
