package com.curso.mitiempo.data.remote

import com.curso.mitiempo.PrediccionResponse
import com.curso.mitiempo.data.remote.dto.Aemet
import com.curso.mitiempo.data.remote.dto.PrediccionMunicipioResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface APIService {

    @GET("/opendata/api/prediccion/especifica/municipio/diaria/{codigoMunicipio}")
    suspend fun getUrlAemet(
        @Path("codigoMunicipio") codigoMuni: String, // Este parámetro reemplazará a {codigoMunicipio} en la URL
        @Header("api_key") apiKey: String             // Es mejor práctica pasar la API Key como un Header
     ): Response<Aemet>

    /**
     * Realiza la segunda llamada a AEMET para obtener la predicción meteorológica final.
     * Esta función ignora la URL base de Retrofit y usa la que se le pasa por parámetro.
     * @param url La URL completa obtenida en la primera llamada (del campo 'datos').
     * @return Una respuesta que contiene una lista de [com.curso.mitiempo.PrediccionResponse].
     */
    @GET
    suspend fun getPrediccion(
        @Url url: String
    ): Response<PrediccionMunicipioResponse> // AEMET devuelve un Array de objetos, por eso es una List
}