# Guía de Clase: Creando la App del Tiempo (Paso 1)

Hola a todos. En esta guía vamos a construir desde cero nuestra aplicación para consultar el tiempo, `MiTiempo`. Aprenderemos a conectarnos a una API real (la de AEMET), a manejar respuestas en formato JSON y a mostrar los datos en pantalla. ¡Y descubriremos una herramienta que nos ahorrará horas de trabajo!

## Objetivo

Crear una app que, dado un código de municipio, muestre la temperatura máxima y mínima para hoy obtenida desde la API de la AEMET (Agencia Estatal de Meteorología).

## Paso 1: Configuración del Proyecto

Lo primero es preparar nuestro entorno.

### 1.1 - Dependencias

Asegúrate de que tu fichero `build.gradle.kts` (el del módulo `app`) tiene las siguientes librerías. Son las que nos permitirán hacer llamadas de red (Retrofit) y procesar los datos (Gson).

```kotlin
dependencies {
    // ... otras dependencias

    // Retrofit para las llamadas a la API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Interceptor para ver las trazas de las llamadas (¡muy importante!)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Para usar lifecycleScope, la forma moderna de gestionar corrutinas
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
}
```

### 1.2 - API Key Segura

Nunca debemos escribir una clave de API directamente en el código. Vamos a guardarla de forma segura:

1.  **Abre `local.properties`**: Este fichero es para tus datos locales y no se sube a repositorios como Git.
2.  **Añade tu clave**: `AEMET_API_KEY="TU_CLAVE_REAL_DE_AEMET"`
3.  **Lee la clave en `build.gradle.kts`**: Añade este bloque para que la clave esté disponible en el código de forma segura a través de `BuildConfig`.

    ```kotlin
    // En build.gradle.kts (módulo app)
    android {
        // ...
        buildFeatures {
            buildConfig = true
        }
    }

    // Lee el fichero local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
    }

    android.defaultConfig {
        // ...
        // Expone la clave como un campo en BuildConfig
        buildConfigField("String", "AEMET_API_KEY", "\"${localProperties.getProperty("AEMET_API_KEY")}\"")
    }
    ```

## Paso 2: Entendiendo la API de AEMET (El Reto de los 2 Pasos)

La API de AEMET tiene una particularidad: no nos da los datos directamente. El proceso es:

1.  **Llamada 1**: Le enviamos nuestra API Key y el código del municipio. La API nos responde con un JSON que contiene una **URL temporal** donde están los datos que queremos.
2.  **Llamada 2**: Hacemos una nueva petición a esa URL temporal que nos acaban de dar. ¡Y esta segunda llamada es la que por fin nos devuelve la predicción del tiempo!

## Paso 3: El Espía - Ver el JSON con `HttpLoggingInterceptor`

Si no sabemos qué aspecto tiene el JSON que nos devuelve la API, ¿cómo vamos a crear las clases de Kotlin para manejarlo? ¡Imposible! Por eso, necesitamos un "espía".

En tu función `getRetrofit`, vamos a configurar un interceptor que imprimirá en el **Logcat** toda la comunicación de red.

```kotlin
private fun getRetrofit(baseUrl: String): Retrofit {
    // 1. Creamos el interceptor
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // BODY para ver todo: cabeceras y cuerpo
    }

    // 2. Creamos un cliente OkHttp y le añadimos el interceptor
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // ... (el resto de tu función)

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client) // 3. ¡IMPORTANTE! Usamos el cliente en Retrofit
        .addConverterFactory(GsonConverterFactory.create(...))
        .build()
}
```

## Paso 4: Generando las Clases con `JsonToKotlinClass`

¡Aquí viene la magia! Vamos a usar el plugin que instalamos para que nos haga el trabajo sucio.

### 4.1 - Clases para la Llamada 1 (Obtener la URL)

1.  Ejecuta la app una primera vez con el interceptor activado.
2.  Busca en el **Logcat** (filtrando por `OkHttp`) la respuesta de la primera llamada. Verás un JSON pequeño como este:

    ```json
    {
      "descripcion": "exito",
      "estado": 200,
      "datos": "https://opendata.aemet.es/opendata/sh/...",
      "metadatos": "https://opendata.aemet.es/opendata/sh/..."
    }
    ```
3.  **Copia ese JSON**.
4.  Ve a la vista de proyecto, haz clic derecho en tu paquete `data/remote/dto` -> `New` -> `Kotlin data class File from JSON`.
5.  Pega el JSON, ponle el nombre `PrediccionMunicipioResponse`, y haz clic en `Generate`.

    El plugin creará la clase `data class PrediccionMunicipioResponse(...)` por ti.

### 4.2 - Clases para la Llamada 2 (Obtener la Predicción)

1.  El JSON de la segunda llamada es mucho más grande y complejo. Búscalo también en el Logcat. Empieza con `[` y contiene toda la predicción.
2.  **Copia TODO ese JSON**.
3.  Repite el proceso: Clic derecho en el paquete `data` -> `New` -> `Kotlin data class File from JSON`.
4.  Pega el JSON gigante, ponle el nombre `PrediccionMunicipioResponseItem` y dale a `Generate`. El plugin creará esta clase y todas las demás que necesita (`Prediccion`, `Dia`, `Temperatura`, etc.) en un solo fichero.

**¡Acabas de ahorrarte una hora de trabajo y posibles errores!**

## Paso 5: Creando el `APIService`

Ahora que tenemos las clases, podemos decirle a Retrofit qué debe esperar. Creamos una interfaz `APIService`.

```kotlin
interface APIService {
    // Llamada 1: Le pasamos el código de municipio y la API Key en la cabecera
    @GET("prediccion/especifica/municipio/diaria/{codigoPoblacion}")
    suspend fun getUrlAemet(
        @Path("codigoPoblacion") codigo: String,
        @Header("api_key") apiKey: String
    ): Response<PrediccionMunicipioResponse> // Devuelve la clase que generamos

    // Llamada 2: Usamos una URL dinámica
    @GET
    suspend fun getPrediccion(@Url url: String): Response<List<PrediccionMunicipioResponseItem>> // Devuelve una lista de la otra clase generada
}
```

## Paso 6: Orquestando las Llamadas en `MainActivity`

Este es el cerebro de la operación. Usaremos `lifecycleScope` para que las corrutinas se cancelen solas si el usuario cierra la app, evitando errores.

```kotlin
// En MainActivity.kt

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

    /**
     * Orquesta el proceso de obtención de datos del tiempo desde la AEMET.
     * Utiliza `lifecycleScope` para lanzar una corrutina de forma segura.
     *
     * @param codigoPoblacion El código del municipio para el cual se solicita la predicción.
     */
    private fun getDatosAemet(codigoPoblacion: String) {
        showLoading()

        // `lifecycleScope` es la forma correcta y segura de lanzar corrutinas en una Activity.
        // Se cancela automáticamente si la pantalla se destruye, evitando errores y fugas de memoria.
        lifecycleScope.launch {
            try {
                // `withContext(Dispatchers.IO)` ejecuta la lógica de red en un hilo secundario
                // para no bloquear la interfaz de usuario. Al finalizar, devuelve el resultado.
                val prediccionFinal = withContext(Dispatchers.IO) {
                    Log.d(TAG, "Iniciando llamadas de red en hilo secundario...")

                    // --- PASO 1: Obtener la URL de los datos ---
                    val retrofitPaso1 = getRetrofit(urlMunicipio)
                    val servicePaso1 = retrofitPaso1.create(APIService::class.java)
                    val responseUrl = servicePaso1.getUrlAemet(codigoPoblacion, miApiKey)

                    // Si la llamada falla o el estado no es 200, lanzamos un error que será
                    // capturado por el bloque 'catch'.
                    if (!responseUrl.isSuccessful || responseUrl.body()?.estado != 200) {
                        throw Exception("Error al obtener la URL de datos: ${responseUrl.body()?.descripcion}")
                    }

                    val urlDatosFinales = responseUrl.body()!!.datos
                    Log.d(TAG, "URL de datos obtenida: $urlDatosFinales")

                    // La API de AEMET requiere una pausa entre la primera y la segunda llamada.
                    delay(2000)

                    // --- PASO 2: Obtener la predicción del tiempo ---
                    val retrofitPaso2 = getRetrofit("https://opendata.aemet.es/")
                    val servicePaso2 = retrofitPaso2.create(APIService::class.java)
                    val responsePrediccion = servicePaso2.getPrediccion(urlDatosFinales)

                    if (!responsePrediccion.isSuccessful) {
                        throw Exception("Error al obtener la predicción final.")
                    }

                    // Se devuelve el primer elemento de la respuesta, que será el resultado de `withContext`.
                    // Este será el objeto `PrediccionMunicipioResponseItem` que esperamos.
                    responsePrediccion.body()!!.first()
                }

                // Al salir de `withContext`, el código continúa en el hilo principal (Main).
                // Aquí podemos actualizar la UI de forma segura con el resultado obtenido.
                Log.d(TAG, "Llamadas de red completadas. Actualizando UI...")
                Log.d(TAG, "Temperatura máxima: ${prediccionFinal.prediccion.dia.first().temperatura.maxima}ªC")
                Log.d(TAG, "Temperatura mínima: ${prediccionFinal.prediccion.dia.first().temperatura.minima}ªC")

                showWeatherData(prediccionFinal.prediccion)

            } catch (e: Exception) {
                // El bloque `catch` maneja cualquier excepción lanzada en el `try`,
                // incluyendo errores de red o las excepciones que lanzamos manualmente.
                // Como este bloque se ejecuta en el hilo principal, podemos actualizar la UI.
                Log.e(TAG, "Excepción en getDatosAemet: ${e.message}", e)
                showError("Fallo al obtener los datos: ${e.message}")
            }
        }
    }

}

```

## Conclusión

¡Felicidades! Has creado una app funcional que consume una API real. Has aprendido a gestionar un proceso de API en dos pasos, a usar un interceptor para depurar, a generar clases automáticamente con `JsonToKotlinClass` y a orquestar todo de forma segura con corrutinas y `lifecycleScope`. Este es el flujo de trabajo que usarás en muchísimas aplicaciones profesionales.
