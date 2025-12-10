package com.curso.mitiempo.data.remote.dto

import com.curso.mitiempo.data.model.Prediccion

data class PrediccionMunicipioResponseItem(
    val elaborado: String,
    val id: Int,
    val nombre: String,
    val origen: Origen,
    val prediccion: Prediccion,
    val provincia: String,
    val version: Double
)