package com.curso.mitiempo.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.curso.mitiempo.BuildConfig
import com.curso.mitiempo.data.remote.dto.Prediccion // Asegúrate de importar tu modelo
import com.curso.mitiempo.data.remote.APIService
import com.curso.mitiempo.data.remote.dto.PrediccionMunicipioResponse
import com.curso.mitiempo.data.remote.dto.PrediccionMunicipioResponseItem
import com.curso.mitiempo.databinding.ActivityMainBinding // <-- 1. Importar la clase de Binding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private val TAG = "MiTiempo"
    private val miApiKey = BuildConfig.AEMET_API_KEY
    private val urlMunicipio = "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/"

    // 2. Declarar una única variable para el binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 3. Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (miApiKey.isEmpty() || miApiKey == "PON_TU_API_KEY_AQUÍ") {
            Log.w(TAG, "La clave de AEMET no está configurada o es la de ejemplo.")
            showError("API Key no configurada.")
        } else {
            Log.d(TAG, "API Key: $miApiKey")
            getDatosAemet("28065")
        }
    }

    // 4. Usar 'binding' para acceder a las vistas
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.weatherDataContainer.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

   private fun showWeatherData(prediccion: Prediccion) {
        val hoy = prediccion.dia.first() // Tomamos el primer día de la lista
        Log.d(TAG, "Temperatura máxima hoy: ${hoy.temperatura.maxima}")

        binding.maxTempTextView.text = "Máx: ${hoy.temperatura.maxima}°C"
        binding.minTempTextView.text = "Mín: ${hoy.temperatura.minima}°C"
        binding.progressBar.visibility = View.GONE
        binding.weatherDataContainer.visibility = View.VISIBLE
        binding.errorTextView.visibility = View.GONE


    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.progressBar.visibility = View.GONE
        binding.weatherDataContainer.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun getRetrofit(baseUrl: String): Retrofit {
        // val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        // val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
         // .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getDatosAemet(codigoPoblacion: String) {
        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofitPaso1 = getRetrofit(urlMunicipio)
                val servicePaso1 = retrofitPaso1.create(APIService::class.java)
                val responseUrl = servicePaso1.getUrlAemet(codigoPoblacion, miApiKey)

                if (responseUrl.isSuccessful && responseUrl.body()?.estado == 200) {
                    val urlDatosFinales = responseUrl.body()!!.datos
                    Log.d(TAG, "URL de datos obtenida: $urlDatosFinales")

                    delay(2000)

                    val retrofitPaso2 = getRetrofit("https://opendata.aemet.es/")
                    val servicePaso2 = retrofitPaso2.create(APIService::class.java)
                    val responsePrediccion = servicePaso2.getPrediccion(urlDatosFinales)

                    withContext(Dispatchers.Main) {
                        if (responsePrediccion.isSuccessful && responsePrediccion.body() != null) {
                            val prediccionCompleta = responsePrediccion.body()!!.first()
                            val prediccionHoy = prediccionCompleta.prediccion.dia.first()

                            Log.d(TAG, "Datos de predicción de temperatura recibidos: ${prediccionHoy.temperatura}")
                            Log.d(TAG, "Temperatura máxima: ${prediccionHoy.temperatura.maxima}ªC")
                            Log.d(TAG, "Temperatura mínima: ${prediccionHoy.temperatura.minima}ªC")

                          showWeatherData(prediccionCompleta.prediccion)
                        } else {
                            val errorMsg = "Error en la segunda llamada: ${responsePrediccion.errorBody()?.string()}"
                            Log.e(TAG, errorMsg)
                            showError(errorMsg)
                        }
                    }
                } else {
                    val errorMsg = "Error en la primera llamada: ${responseUrl.body()?.descripcion ?: responseUrl.errorBody()?.string()}"
                    Log.e(TAG, errorMsg)
                    withContext(Dispatchers.Main) {
                        showError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción en getDatosAemet: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showError("Fallo en la conexión: ${e.message}")
                }
            }
        }
    }
}
