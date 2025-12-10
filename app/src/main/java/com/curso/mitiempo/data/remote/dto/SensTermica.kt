package com.curso.mitiempo.data.remote.dto

data class SensTermica(
    val dato: List<Dato>,
    val maxima: Int,
    val minima: Int
)