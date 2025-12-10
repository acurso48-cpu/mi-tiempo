# Guía para Alumnos: Cómo Convertir JSON a Clases de Kotlin Automáticamente

Hola a todos. En el desarrollo de aplicaciones para Android, es muy común trabajar con APIs que nos devuelven datos en formato JSON. Mapear ese JSON a clases de Kotlin (conocidas como `data class`) a mano puede ser tedioso y propenso a errores.

Por suerte, existen herramientas que hacen este trabajo por nosotros. Hoy vamos a aprender a usar el plugin **JsonToKotlinClass (wuseal)**, uno de los más populares y útiles para Android Studio.

## ¿Por qué usar esta herramienta?

- **Ahorro de tiempo:** Convierte un JSON complejo en clases de Kotlin en segundos.
- **Reducción de errores:** Evita errores de tipeo o de asignación de tipos de datos (por ejemplo, `String` en lugar de `Int`).
- **Manejo de nulabilidad:** Ayuda a generar clases que contemplan si un campo puede ser nulo o no, lo cual es clave en Kotlin.

## Paso 1: Instalar el Plugin

1.  En Android Studio, ve a `File` -> `Settings` (o `Android Studio` -> `Preferences...` en macOS).
2.  Selecciona la sección **Plugins**.
3.  Asegúrate de estar en la pestaña **Marketplace**.
4.  En el buscador, escribe `JsonToKotlinClass`.
5.  Busca el que ponga **(wuseal)** en el nombre. Haz clic en **Install**.
6.  Android Studio te pedirá reiniciar el IDE. Hazlo para que el plugin se active.

![Instalación del plugin](https://i.imgur.com/example.png) *(Nota: Esta es una imagen de ejemplo, el aspecto puede variar)*

## Paso 2: Obtener el JSON que queremos convertir

Antes de usar el plugin, necesitamos una muestra del JSON real que nos devuelve la API. En nuestro proyecto `MiTiempo`, ya hemos configurado un `HttpLoggingInterceptor` que nos muestra las respuestas de la API en el **Logcat**.

1.  Ejecuta la aplicación.
2.  Abre la ventana de **Logcat** en Android Studio.
3.  Filtra por el tag `OkHttp`. Verás la respuesta completa de la API.
4.  Copia **todo el cuerpo del JSON** que aparece en el log. Asegúrate de copiar el JSON de la respuesta final, la que contiene la predicción del tiempo.

## Paso 3: Crear un paquete para los modelos

Es una buena práctica organizar nuestro código. Las clases que representan datos (modelos) suelen ir en su propio paquete.

1.  En la vista de Proyecto, haz clic derecho sobre `com.curso.mitiempo`.
2.  Selecciona `New` -> `Package`.
3.  Nómbralo **`models`** o **`data.models`**.

## Paso 4: ¡Usar el Plugin para crear las clases!

1.  Haz clic derecho sobre el paquete `models` que acabas de crear.
2.  Ve a `New` -> `Kotlin data class File from JSON`.

    ![Menú del plugin](https://i.imgur.com/example2.png) *(Nota: Imagen de ejemplo)*

3.  Se abrirá una ventana. Pega el JSON que copiaste del Logcat en el cuadro de texto superior.
4.  Dale un nombre a la clase principal (por ejemplo, `PrediccionResponse`).
5.  **Configuración recomendada:** En el panel de la derecha (`Settings`), asegúrate de que:
    *   **Type:** `Val` (para propiedades inmutables, es una buena práctica).
    *   **Nullability:** `Auto-detect...` (para que el plugin decida si un campo puede ser nulo basándose en el JSON).
    *   **Annotation:** `Gson` (si usas Gson para la conversión, como en nuestro proyecto).

6.  Haz clic en **Generate**. El plugin creará automáticamente todas las `data class` necesarias, anidadas correctamente, dentro de un único fichero.

## Paso 5: Integrar las nuevas clases

Ahora que tienes las clases, puedes usarlas en tu llamada de Retrofit.

1.  **Actualiza tu `APIService`:**

    ```kotlin
    // En APIService.kt
    @GET
    suspend fun getPrediccion(@Url url: String): Response<List<PrediccionResponse>> // Usa tu nueva clase aquí
    ```

2.  **Usa el objeto en `MainActivity.kt`:**

    El cuerpo de la respuesta (`callPaso2.body()`) ahora será una lista de objetos `PrediccionResponse`, fuertemente tipada y fácil de usar.

    ```kotlin
    // En MainActivity.kt
    val prediccion: List<PrediccionResponse>? = callPaso2.body()

    if (callPaso2.isSuccessful && prediccion != null) {
        val hoy = prediccion.first().prediccion.dia.first()
        Log.d(TAG, "Temperatura máxima de hoy: ${hoy.temperatura.maxima}")
    }
    ```

¡Y eso es todo! Has pasado de un JSON de texto plano a objetos Kotlin con los que puedes trabajar de forma segura y eficiente. Esta herramienta es un gran ahorro de tiempo y te ayudará a evitar muchos bugs en el futuro.
