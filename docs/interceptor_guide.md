# Guía para Implementar un Interceptor con OkHttp y Retrofit

Este documento explica cómo añadir un interceptor a tus llamadas de red para poder ver las trazas de las mismas. Esto es muy útil para depurar y entender qué está enviando y recibiendo tu aplicación.

## 1. Añadir la dependencia

Asegúrate de que tienes la dependencia de `logging-interceptor` en tu archivo `build.gradle.kts` (o `build.gradle`):

```kotlin
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```
*Nota: Revisa siempre la última versión de la librería.*

## 2. Crear el Interceptor

En tu código, donde configuras Retrofit, necesitas crear una instancia de `HttpLoggingInterceptor`.

```kotlin
val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

El `level` determina cuánta información se mostrará. Las opciones son:
- `NONE`: No se muestran trazas.
- `BASIC`: Muestra la línea de la petición y la respuesta.
- `HEADERS`: Muestra las cabeceras de la petición y la respuesta.
- `BODY`: Muestra las cabeceras y el cuerpo de la petición y la respuesta.

## 3. Añadir el Interceptor al Cliente OkHttp

Ahora, crea un cliente de OkHttp y añádele el interceptor.

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()
```

## 4. Usar el Cliente en Retrofit

Finalmente, al construir tu instancia de Retrofit, asigna el cliente que acabas de crear.

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl(tuUrlBase)
    .client(client) // Aquí es donde se usa el cliente
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## Ejemplo Completo de `MainActivity.kt`

Aquí tienes un ejemplo de cómo se integraría en una función que devuelve una instancia de Retrofit:

```kotlin
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
    val gson = GsonBuilder()
        .setLenient()
        .create()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client) // Usamos el cliente con el interceptor
        .addConverterFactory(GsonConverterFactory.create(gson)) // Usamos el conversor Gson
        .build()
}
```

Con esta configuración, cada vez que hagas una llamada con Retrofit, verás en el Logcat (con el tag "OkHttp") los detalles de la petición y la respuesta.
