package com.curso.mitiempo.data.model

import com.curso.mitiempo.data.remote.dto.CotaNieveProv
import com.curso.mitiempo.data.remote.dto.EstadoCielo
import com.curso.mitiempo.data.remote.dto.HumedadRelativa
import com.curso.mitiempo.data.remote.dto.ProbPrecipitacion
import com.curso.mitiempo.data.remote.dto.RachaMax
import com.curso.mitiempo.data.remote.dto.SensTermica
import com.curso.mitiempo.data.remote.dto.Temperatura
import com.curso.mitiempo.data.remote.dto.Viento

data class Dia(
    val cotaNieveProv: List<CotaNieveProv>,
    val estadoCielo: List<EstadoCielo>,
    val fecha: String,
    val humedadRelativa: HumedadRelativa,
    val probPrecipitacion: List<ProbPrecipitacion>,
    val rachaMax: List<RachaMax>,
    val sensTermica: SensTermica,
    val temperatura: Temperatura,
    val uvMax: Int,
    val viento: List<Viento>
)