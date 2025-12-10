package com.curso.mitiempo

data class PrediccionResponse(
    val elaborado : String,
    val nombre: String,
    val provincia: String,
    val prediccion: List<Any>
)

data class PrediccionDia(
    val dia: List<Any>
)