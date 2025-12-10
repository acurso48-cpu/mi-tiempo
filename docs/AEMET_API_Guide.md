# Guía rápida: usar la API OpenData de AEMET

Ruta recomendada del archivo: \`documents/AEMET_API_Guide.md\`

Resumen
- Registrarse en el centro de descargas de AEMET para obtener una clave API.
- Usar la base: \`https://opendata.aemet.es/opendata/api/\`.
- Muchas llamadas devuelven un objeto JSON con un campo \`datos\` que contiene la URL final del fichero con la información; hay que solicitar esa URL para obtener los datos reales.

1. Obtener la API Key
- Crear cuenta en https://opendata.aemet.es/centrodedescargas/inicio.
- En "Mis claves" copiar la clave (API key).

2. Flujo habitual
1. Hacer una petición GET al endpoint del servicio añadiendo \`api_key=TU_API_KEY\` como parámetro query.
   Ej: \`https://opendata.aemet.es/opendata/api/algún/endpoint?api_key=TU_API_KEY\`
2. La respuesta JSON suele incluir metadata y un campo \`datos\` con una URL.
3. Hacer GET a la URL de \`datos\` para descargar el JSON/CSV real (normalmente no requiere volver a enviar la key).

3. Ejemplo con curl
- Paso 1: pedir metadata
  curl -s "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/28079?api_key=TU_API_KEY"
- Paso 2: en la respuesta hay \`datos\`: hacer GET a esa URL
  curl -s "URL_DE_DATOS"

4. Ejemplo Kotlin con OkHttp (simple)
- Dependencia Gradle:
  implementation\("com.squareup.okhttp3:okhttp:4.11.0"\)
- Código (Kotlin):

  import okhttp3.OkHttpClient
  import okhttp3.Request
  import com.google.gson.JsonParser

  val client = OkHttpClient()
  // 1) pedir metadata
  val reqMeta = Request.Builder()
  .url("https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/28079?api_key=TU_API_KEY")
  .build()
  client.newCall(reqMeta).execute().use { resp ->
  val metaBody = resp.body?.string() ?: ""
  val metaJson = JsonParser.parseString(metaBody).asJsonObject
  val datosUrl = metaJson.get("datos").asString
  // 2) pedir datos reales
  val reqDatos = Request.Builder().url(datosUrl).build()
  client.newCall(reqDatos).execute().use { r2 ->
  val datos = r2.body?.string()
  // procesar 'datos' (JSON)
  }
  }

5. Ejemplo con Retrofit (recomendado para proyectos Android)
- Gradle:
  implementation\("com.squareup.retrofit2:retrofit:2.9.0"\)
  implementation\("com.squareup.retrofit2:converter-gson:2.9.0"\)
- Idea: definir servicio para la llamada inicial (metadata). Posteriormente obtener la URL \`datos\` y usar Retrofit/OkHttp para descargar el JSON final.

6. Buenas prácticas y notas
- Respeto de límites y uso responsable \(consultar condiciones en la web\).
- Manejar errores HTTP y estados JSON devueltos por la API.
- Algunos \`datos\` vienen comprimidos o en CSV; comprobar \`Content-Type\`.
- Cache y retries: usar políticas razonables para no sobrecargar el servicio.
- Para producción, no incrustar la API key en el cliente sin protección; usar un backend si la clave debe mantenerse secreta.

---

## 7. Integración Segura de la API Key en Android

Para evitar exponer la clave de la API en el control de versiones (Git), hemos seguido un método seguro para integrarla en el proyecto Android.

### 7.1. Crear `local.properties`

En la raíz del proyecto (`D:/CursoAndroid25/Mitiempo/`), se crea un archivo llamado `local.properties`. Este archivo es ignorado por Git por defecto, lo que lo hace ideal para almacenar información sensible.

Contenido de `local.properties` con una clave de ejemplo (recuerda usar la tuya):

```properties
# Guarda aquí tus claves de API.
# Este archivo no debe ser añadido al control de versiones (Git).

AEMET_API_KEY="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGVtYWlsLmNvbSIsImlhdCI6MTYxNjI0NDQyMiwianRpIjoiMTEyMjMzNDQtNTU2Ni03Nzg4LTk5MDAtYWFiYmNjZGRlZWZmIn0.u8aDDdZnJjY7i-2J_a9Ggf5c_dDXm9B1v-tB9_ZmqsA"
```

### 7.2. Configurar `app/build.gradle.kts`

Modificamos el archivo `build.gradle.kts` del módulo `app` para que lea el valor de `AEMET_API_KEY` desde `local.properties` y lo haga accesible en nuestro código de forma segura.

Se añadieron las siguientes configuraciones:

- **Lectura del archivo de propiedades:**
  ```kotlin
  import java.util.Properties

  // ...

  val localProperties = Properties()
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
      localProperties.load(localPropertiesFile.inputStream())
  }
  ```

- **Exposición de la clave en `BuildConfig`:**
  Dentro del bloque `android { defaultConfig { ... } }`, se añadió:
  ```kotlin
  // Expone la clave AEMET_API_KEY desde local.properties al código de la app.
  buildConfigField("String", "AEMET_API_KEY", "\"${localProperties.getProperty("AEMET_API_KEY") ?: ""}\"")
  ```

- **Habilitar `buildConfig`:**
  Se aseguró que `buildConfig` estuviera habilitado en el bloque `android { buildFeatures { ... } }`:
  ```kotlin
  buildFeatures {
      buildConfig = true
  }
  ```

### 7.3. Acceder a la Clave en el Código Kotlin

Con la configuración anterior, Gradle genera automáticamente la clase `BuildConfig` en tiempo de compilación. Ahora podemos acceder a la clave desde cualquier parte de nuestra aplicación (por ejemplo, en `MainActivity.kt`) de la siguiente manera:

```kotlin
val apiKey = BuildConfig.AEMET_API_KEY

// Es una buena práctica comprobar que la clave no sea la de ejemplo o esté vacía.
if (apiKey.isEmpty() || apiKey.startsWith("eyJhbGci")) {
    Log.w("MainActivity", "La clave de AEMET no está configurada o es la de ejemplo.")
} else {
    // Usar la clave para las llamadas a la API
    Log.d("MainActivity", "API Key: $apiKey")
}
```

Este enfoque garantiza que la clave de la API no se filtre en el repositorio de código, manteniendo la seguridad del proyecto.

---

8. Enlaces útiles
- Centro de descargas: https://opendata.aemet.es/centrodedescargas/inicio
- Documentación / ejemplos: revisar la sección de cada servicio en la web de AEMET
