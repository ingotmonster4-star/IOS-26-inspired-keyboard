package com.example.data

data class KeySpec(
    val id: String,
    val primary: String,
    val secondaryHint: String? = null,
    val longPressVariants: List<String> = emptyList(),
    val weight: Float = 1f,
)

object KeyboardLayouts {

    val numberRow: List<KeySpec> = (1..10).map { num ->
        val digit = if (num == 10) "0" else num.toString()
        val variants = when (digit) {
            "1" -> listOf("¹", "½", "⅓", "¼", "⅛")
            "2" -> listOf("²", "⅔")
            "3" -> listOf("³", "¾", "⅜")
            "4" -> listOf("⁴", "¼")
            "5" -> listOf("⁵", "⅝")
            "6" -> listOf("⁶")
            "7" -> listOf("⁷", "⅞")
            "8" -> listOf("⁸", "∞")
            "9" -> listOf("⁹")
            "0" -> listOf("⁰", "°", "∅", "ⁿ")
            else -> emptyList()
        }
        KeySpec(id = "num_$digit", primary = digit, longPressVariants = variants)
    }

    private val letterVariants: Map<String, List<String>> = mapOf(
        "q" to listOf("1", "q"),
        "w" to listOf("2", "ŵ", "ẁ", "ẃ", "ẅ"),
        "e" to listOf("3", "è", "é", "ê", "ë", "ē", "ė", "ę", "€"),
        "r" to listOf("4", "ř", "ŕ", "®"),
        "t" to listOf("5", "þ", "ť", "ț", "™"),
        "y" to listOf("6", "ÿ", "ý", "ŷ", "¥"),
        "u" to listOf("7", "ù", "ú", "û", "ü", "ū", "ů", "ű"),
        "i" to listOf("8", "ì", "í", "î", "ï", "ī", "į", "ı"),
        "o" to listOf("9", "ò", "ó", "ô", "ö", "œ", "ø", "ō", "õ"),
        "p" to listOf("0", "π", "¶"),
        "a" to listOf("à", "á", "â", "ä", "æ", "ã", "å", "ā", "@"),
        "s" to listOf("ß", "ś", "š", "ş", "§", "$"),
        "d" to listOf("ð", "ď", "đ", "°", "∆"),
        "f" to listOf("ƒ", "℉"),
        "g" to listOf("ğ", "ĝ", "ġ", "ģ"),
        "h" to listOf("ĥ", "ħ", "#"),
        "j" to listOf("ĵ"),
        "k" to listOf("ķ", "κ"),
        "l" to listOf("ł", "ĺ", "ľ", "ļ", "£"),
        "z" to listOf("ž", "ź", "ż"),
        "x" to listOf("×", "ξ"),
        "c" to listOf("ç", "ć", "č", "¢", "©"),
        "v" to listOf("✓", "√"),
        "b" to listOf("₿", "•"),
        "n" to listOf("ñ", "ń", "ň", "ņ", "ŋ"),
        "m" to listOf("μ", "—", "−"),
    )

    val alphaRow1: List<KeySpec> = listOf(
        "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
        "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
    ).map { (letter, hint) ->
        KeySpec(
            id = "alpha_$letter",
            primary = letter,
            secondaryHint = hint,
            longPressVariants = letterVariants[letter].orEmpty(),
        )
    }

    val alphaRow2: List<KeySpec> = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { letter ->
        KeySpec(
            id = "alpha_$letter",
            primary = letter,
            secondaryHint = letterVariants[letter]?.firstOrNull(),
            longPressVariants = letterVariants[letter].orEmpty(),
        )
    }

    val alphaRow3Letters: List<KeySpec> = listOf("z", "x", "c", "v", "b", "n", "m").map { letter ->
        KeySpec(
            id = "alpha_$letter",
            primary = letter,
            secondaryHint = letterVariants[letter]?.firstOrNull(),
            longPressVariants = letterVariants[letter].orEmpty(),
        )
    }

    val symbol1Row1: List<KeySpec> = numberRow

    val symbol1Row2: List<KeySpec> = listOf(
        KeySpec("s1_at", "@"),
        KeySpec("s1_hash", "#"),
        KeySpec("s1_dollar", "$", longPressVariants = listOf("€", "£", "¥", "₹", "₿", "¢")),
        KeySpec("s1_under", "_"),
        KeySpec("s1_amp", "&"),
        KeySpec("s1_minus", "-", longPressVariants = listOf("–", "—", "−", "±")),
        KeySpec("s1_plus", "+", longPressVariants = listOf("±", "⊕")),
        KeySpec("s1_lparen", "(", longPressVariants = listOf("[", "{", "<", "⟨")),
        KeySpec("s1_rparen", ")", longPressVariants = listOf("]", "}", ">", "⟩")),
        KeySpec("s1_slash", "/", longPressVariants = listOf("\\", "÷")),
    )

    val symbol1Row3: List<KeySpec> = listOf(
        KeySpec("s1_star", "*", longPressVariants = listOf("★", "☆", "✦", "×", "•")),
        KeySpec("s1_dquote", "\"", longPressVariants = listOf("“", "”", "«", "»", "„")),
        KeySpec("s1_squote", "'", longPressVariants = listOf("‘", "’", "′", "″")),
        KeySpec("s1_colon", ":", longPressVariants = listOf("∶", "∷")),
        KeySpec("s1_semi", ";"),
        KeySpec("s1_bang", "!", longPressVariants = listOf("¡", "‽", "≠")),
        KeySpec("s1_qmark", "?", longPressVariants = listOf("¿", "‽")),
    )

    val symbol2Row1: List<KeySpec> = listOf(
        KeySpec("s2_tilde", "~", longPressVariants = listOf("≈", "≃", "≅")),
        KeySpec("s2_grave", "`"),
        KeySpec("s2_pipe", "|", longPressVariants = listOf("¦", "∥")),
        KeySpec("s2_bullet", "•", longPressVariants = listOf("·", "‣", "◦", "○", "●")),
        KeySpec("s2_sqrt", "√", longPressVariants = listOf("∛", "∜")),
        KeySpec("s2_pi", "π", longPressVariants = listOf("Π", "τ", "φ")),
        KeySpec("s2_div", "÷"),
        KeySpec("s2_mul", "×"),
        KeySpec("s2_sec", "§", longPressVariants = listOf("¶", "†", "‡")),
        KeySpec("s2_delta", "Δ", longPressVariants = listOf("δ", "∇", "∂")),
    )

    val symbol2Row2: List<KeySpec> = listOf(
        KeySpec("s2_pound", "£"),
        KeySpec("s2_cent", "¢"),
        KeySpec("s2_euro", "€"),
        KeySpec("s2_yen", "¥"),
        KeySpec("s2_caret", "^", longPressVariants = listOf("↑", "∧")),
        KeySpec("s2_deg", "°", longPressVariants = listOf("℃", "℉", "′", "″")),
        KeySpec("s2_eq", "=", longPressVariants = listOf("≠", "≈", "≡", "≤", "≥")),
        KeySpec("s2_lbrace", "{"),
        KeySpec("s2_rbrace", "}"),
        KeySpec("s2_bslash", "\\"),
    )

    val symbol2Row3: List<KeySpec> = listOf(
        KeySpec("s2_pct", "%", longPressVariants = listOf("‰", "‱")),
        KeySpec("s2_copy", "©", longPressVariants = listOf("℗", "℠")),
        KeySpec("s2_reg", "®"),
        KeySpec("s2_tm", "™"),
        KeySpec("s2_check", "✓", longPressVariants = listOf("✔", "✕", "✖")),
        KeySpec("s2_lbrack", "["),
        KeySpec("s2_rbrack", "]"),
    )

    val periodLongPressVariants: List<String> = listOf(
        ".", ",", "!", "?", "…", ":", ";", "'", "\"", "-", "/", "@", "#", "&", "(", ")", "→", "★"
    )

    val commaLongPressVariants: List<String> = listOf(
        ",", "—", "–", ";", ":", "•", "·", "<", ">"
    )

    val numericPadRows: List<List<KeySpec>> = listOf(
        listOf(KeySpec("np_1", "1"), KeySpec("np_2", "2"), KeySpec("np_3", "3"), KeySpec("np_minus", "-")),
        listOf(KeySpec("np_4", "4"), KeySpec("np_5", "5"), KeySpec("np_6", "6"), KeySpec("np_plus", "+")),
        listOf(KeySpec("np_7", "7"), KeySpec("np_8", "8"), KeySpec("np_9", "9"), KeySpec("np_star", "*")),
        listOf(KeySpec("np_dot", "."), KeySpec("np_0", "0", longPressVariants = listOf("+")), KeySpec("np_hash", "#")),
    )
}
