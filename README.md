# **Proyecto Digitalización: LostAndFound**

## **¿Qué es Lost And Found?**

- **Lost And Found** es una aplicación desarrollada para dispositivos Android para facilitar la búsqueda de mascotas perdidas mediante la creación de posts y notificaciones de avistamientos.
* Programada en **Kotlin** junto con **Jetpack Compose** para la interfaz gráfica.
- Está desarrollada en el **IDE Android Studio**.
* Está desarrollada con la ayuda de **Inteligencia Artificial**.
- **Uso de Firebase Console**: Herramienta de desarrollo de aplicaciones de Google, la cual usamos:
  - **Firestore Database**: Base de datos en tiempo real para almacenar datos sobre los posts y usuarios.
  - **Authentication**: Método de registro e inicio de sesión por correo electrónico.
  - **Storage**: Almacenamiento en la nube para guardar archivos multimedia como fotos.

---

## **Cómo funciona y qué nos podemos encontrar**

- Nada más abrir la aplicación, veremos una pantalla de inicio de sesión.
* Si no tenemos una cuenta creada para iniciar sesión, podremos registrarnos.
- En el caso de que tengamos una cuenta ya existente y olvidemos su contraseña, tenemos una funcionalidad de cambio de contraseña por correo electrónico.
* Una vez con una sesión iniciada, podremos crear un post con los datos y la foto de nuestra mascota perdida, la cual aparecerá en el menú principal de la aplicación para que otros usuarios puedan verla.
- Desde una opción en un menú, podremos editar el post o cerrarlo en caso de encontrar nuestra mascota.
* Podremos realizar avisos de avistamientos sobre los posts de otros usuarios, en los cuales aparecerá un botón para emitir una notificación al dueño del post. Este botón no aparecerá si es una publicación del usuario que esta autenticado en ese momento.
- Estos avisos aparecerán en una opción en el menú llamada Notificaciones (solo aparecen las notificaciones de avistamientos sobre nuestros posts).
* Si cerramos un post, este desaparecerá del menú principal y se eliminarán las notificaciones de avistamiento sobre ese post para evitar saturar la aplicación.
- Tenemos tambien en el menu principal de perfil en el cual podremos ver nuestro nombre y correo actual el cual estamos usando con la posibilidad de cambiarlos si el usaurio quiere, para cambiarlo se le enviara un correo de verificación al nuevo correo.
* Tambien existe la posibilidad desde perfil de eliminar la cuenta de tal manera que borraremos todo lo relacionado con la cuenta posts, notificaciones de avistamientos y la propia cuenta de esta manera el usuario que quiera eliminar su cuenta no dejara rastro ni en la aplicaición ni en la base de datos.
- Por último, en el menú podemos encontrar una opción para cerrar sesión, que nos llevará a la ventana de login de la aplicación.
* Como detalle, una vez que inicias sesión, si cierras completamente la aplicación y la vuelves a abrir, permanecerá tu sesión iniciada hasta que cierres sesión manualmente mediante la opción del menú.

---

## **Cómo probar la aplicación**

- Para probar la aplicación, puede ser un poco complejo y tedioso, ya que está desarrollada para Android. **Lo más fácil sería tener un dispositivo Android en el cual instalar la APK.**
* Podemos encontrarla en el directorio `/apk` en este repositorio.
- En el caso de querer hacer la APK desde cero, lo explico con detalle en el video de YouTube, donde se muestra la explicación del proyecto, cómo probarlo y cómo construir la APK.
* Otra opción para probar el código es desde el **emulador de Android Studio**, cuya configuración explico brevemente al inicio del video de YouTube.
- Como última opción, podemos también probar la aplicación con un **emulador gratuito como BlueStacks** (funciona, aunque un poco lento).
* Te proporciono todos los enlaces de interés para la evaluación del proyecto:
  - **[Video de YouTube](https://www.youtube.com/watch?v=dm1smtHQcxA)** _(En el video se explica en qué consiste la aplicación, cómo probarla de diferentes formas y una demostración de las funcionalidades. (50 min de chapa, discúlpame pero esta todo incluido)._
  - **[Descarga de Android Studio](https://developer.android.com/studio?hl=es-419)**
  - **[Descarga de BlueStacks](https://www.bluestacks.com/es/index.html)**
- Para las **preguntas evaluativas**, puedes acceder desde aquí **[preguntas.md](preguntas.md)** o buscar el archivo Markdown en este repositorio.

---

## **Documentación**

- En el código podemos ver varios **comentarios realizados con KDOC** para aclarar qué está pasando y cómo funciona.
* Se genera documentación automática gracias a **Dokka**, disponible en:
  - [dokka_documentation.zip](dokka/dokka_documentacion.zip) _(Encuentras el Zip dentro del directorio dokka)._
    - Para verlo necesitas abrir el zip  y abrir el index.html en tu navegador puede tardar en funcionar del todo un par de minutos.

---

## **Para un futuro**

- Me han quedado cosas sin hacer por falta de tiempo, como por ejemplo alguna funcionalidad con la API de Google Maps para incluir mapas.
- Mejorar el sistema de notificaciones para que notifique al usuario con la aplicación completamente cerrada.
  - Me pasé varios días atascado y no conseguí sacarlo adelante, por lo que opté por hacerlo dentro de la aplicación. Cumple su cometido, pero no como a mí me gustaría.
- En un futuro, si continuamos con el desarrollo, se podrían agregar estas funcionalidades para perfeccionar la aplicación.  
