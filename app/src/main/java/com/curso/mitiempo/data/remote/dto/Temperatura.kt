package com.curso.mitiempo.data.remote.dto

data class Temperatura(
    val dato: List<Dato>,
    val maxima: Int,
    val minima: Int
)