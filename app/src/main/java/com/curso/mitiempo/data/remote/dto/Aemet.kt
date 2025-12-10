package com.curso.mitiempo.data.remote.dto

data class Aemet(
    val datos: String,
    val descripcion: String,
    val estado: Int,
    val metadatos: String
)