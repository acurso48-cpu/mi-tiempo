package com.curso.mitiempo

data class PrediccionResponse(
    val elaborado : String,
    val nombre: String,
    val provincia: String,
    val prediccion: PrediccionDia
)

data class PrediccionDia(
    val dia: List<Any>
)