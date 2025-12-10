package com.curso.mitiempo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

    val TAG = "Mi tiempo"
    val miApiKey = BuildConfig.AEMET_API_KEY
    val urlMunicipio =
        "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        "28065"

// Es una buena práctica comprobar que la clave no sea la de ejemplo o esté vacía.
        if (miApiKey.isEmpty()) {
            Log.w(TAG, "La clave de AEMET no está configurada o es la de ejemplo.")
        } else {
            // Usar la clave para las llamadas a la API
            Log.d(TAG, "API Key: $miApiKey")
        }

        getDatosAemet("28065")
    }


    private fun getRetrofit(baseUrl: String): Retrofit {
        // Creamos el interceptor para ver las trazas de la llamada
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Creamos un cliente OkHttp y le añadimos el interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        // Creamos un constructor de Gson más permisivo (lenient)
        // Esto ayuda si la API devuelve JSON con pequeñas imperfecciones.
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client) // Usamos el cliente con el interceptor
            .addConverterFactory(GsonConverterFactory.create(gson)) // Usamos el conversor Gson
            .build()
    }

    /**
     * Realiza la llamada a la API en un hilo secundario usando corutinas.
     */
    private fun getDatosAemet(codigoPoblacion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // --- PRIMERA LLAMADA: Obtener la URL de los datos ---
                // Nota: La URL base para Retrofit debe terminar en /
                val retrofitPaso1 = getRetrofit(urlMunicipio)
                val callPaso1 = retrofitPaso1.create(APIService::class.java)
                    .getUrlAemet(codigoPoblacion, miApiKey)

                if (callPaso1.isSuccessful) {
                    val respuestaUrl = callPaso1.body()
                    // Comprobamos que la respuesta no sea nula y que el estado sea 200 (OK)
                    if (respuestaUrl != null && respuestaUrl.estado == 200) {
                        val urlDatosFinales = respuestaUrl.datos
                        Log.d(TAG, "URL de datos obtenida: $urlDatosFinales")

                        delay(2000)
                        // --- SEGUNDA LLAMADA: Obtener los datos del tiempo ---
                        // Para la segunda llamada, la URL completa viene en la respuesta anterior.
                        // Por eso, la URL base de Retrofit debe ser una genérica, y pasamos la URL completa en la llamada.
                        val retrofitPaso2 =
                            getRetrofit("https://opendata.aemet.es/") // URL base genérica
                        val callPaso2 = retrofitPaso2.create(APIService::class.java)
                            .getPrediccion(urlDatosFinales)
                        val prediccion = callPaso2.body()

                        // Cambiamos al hilo principal (Main) para actualizar la UI
                        withContext(Dispatchers.Main) {
                            if (callPaso2.isSuccessful && prediccion != null) {
                                Log.d(TAG,
                                    "Datos de predicción recibidos: $prediccion"
                                )

                                //Conseguir la temperatura mínima y máxima de hoy
                                Log.d(TAG, "Fecha elaboración: ${prediccion.first().elaborado}")
                                val hoy = prediccion.first().prediccion.dia[0]
                                Log.d(TAG, "Temperatura máxima de hoy: ${hoy.temperatura.maxima}")

                            } else {
                                Log.e(
                                    TAG, "Error en la segunda llamada: ${callPaso2.errorBody()?.string()
                                    }"
                                )
                            }
                        }
                    } else {
                        Log.e(TAG, "Respuesta del servidor (Paso 1) no fue exitosa: ${respuestaUrl?.descripcion}"
                        )
                    }
                } else {
                    Log.e(TAG, "Error en la primera llamada: ${callPaso1.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Capturamos cualquier excepción para que la app no crashee
                Log.e(TAG, "Excepción en getDatosAemet: ${e.message}", e)
            }
        }

    }
}


