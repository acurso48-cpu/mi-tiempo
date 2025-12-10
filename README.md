# Proyecto MiTiempo - App de Ejemplo del Curso de Android

Bienvenido al proyecto `MiTiempo`, una aplicación de ejemplo desarrollada para enseñar conceptos fundamentales del desarrollo de aplicaciones Android modernas.

El objetivo de esta app es mostrar cómo conectarse a una API real (la de AEMET), gestionar respuestas de red complejas, y mostrar los datos al usuario de una manera robusta y eficiente.

## ¿Qué hace esta aplicación?

La aplicación permite al usuario introducir el código de un municipio español y, al pulsar un botón, obtiene y muestra la predicción de la temperatura máxima y mínima para el día actual.

## Tecnologías y Conceptos Clave

Este proyecto es una demostración práctica de las siguientes tecnologías y patrones:

*   **Kotlin**: El lenguaje oficial para el desarrollo de Android.
*   **Corrutinas de Kotlin (`lifecycleScope`)**: Para manejar operaciones asíncronas (como llamadas de red) de forma segura y eficiente, atando su ciclo de vida al de la pantalla.
*   **Retrofit**: Para realizar llamadas HTTP a la API de AEMET de forma declarativa.
*   **OkHttp (`HttpLoggingInterceptor`)**: Para inspeccionar y depurar las llamadas de red, visualizando las peticiones y respuestas directamente en el Logcat.
*   **Gson**: Para convertir las respuestas JSON de la API en objetos de Kotlin (`data class`).
*   **ViewBinding**: Para interactuar con las vistas del layout de forma segura y sin `findViewById`.
*   **Manejo de secretos (`BuildConfig` y `local.properties`)**: Para almacenar y usar la API Key sin exponerla en el código fuente, una práctica de seguridad esencial.
*   **Flujo de API en dos pasos**: Se implementa la lógica necesaria para trabajar con la API de AEMET, que requiere una primera llamada para obtener una URL de datos y una segunda para obtener la predicción final.

## Guías de Aprendizaje

Dentro de la carpeta `/docs` encontrarás varios documentos en formato Markdown que explican en detalle algunos de los conceptos más importantes implementados en este proyecto:

1.  **`Paso1.md`**: Una guía completa que explica cómo construir esta aplicación desde cero.
2.  **`interceptor_guide.md`**: Explica cómo y por qué se usa un `HttpLoggingInterceptor`.
3.  **`json_to_kotlin_guide.md`**: Un tutorial sobre cómo usar el plugin `JsonToKotlinClass` para automatizar la creación de modelos de datos.
4.  **`lifecycleScope_guide.md`**: Detalla la importancia de usar `lifecycleScope` para gestionar corrutinas ligadas al ciclo de vida de los componentes de Android.

## Configuración Inicial

Para poder ejecutar este proyecto, necesitas una clave de API de AEMET OpenData.

1.  Consigue tu API Key en la [web de AEMET OpenData](https://opendata.aemet.es/centro-de-descargas/alta-usuario).
2.  En la raíz del proyecto, abre el fichero `local.properties` (si no existe, créalo).
3.  Añade la siguiente línea, reemplazando `TU_API_KEY` por la clave que has obtenido:

    ```properties
    AEMET_API_KEY="TU_API_KEY"
    ```
4.  Sincroniza el proyecto con Gradle. ¡Y listo para ejecutar!
