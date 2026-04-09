package com.vishal.manodost.data.model

data class Question(
    val textEn: String,
    val textHi: String,
    val optionsEn: List<String>,
    val optionsHi: List<String>
)