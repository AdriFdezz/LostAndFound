# Preguntas a responder

## Ciclo de vida del dato (5b):

- **¿Cómo se gestionan los datos desde su generación hasta su eliminación en tu proyecto?**

    - Voy a dividir cómo gestionamos los datos en la aplicación desde su generación hasta la eliminación:
        - **Se generan y almacenan los datos:** Ocurre cuando un usuario crea un post de una mascota perdida, donde se recogen los datos en una especie de formulario dentro de la aplicación y se almacenan en la base de datos Firebase Firestore, y las imágenes en Firebase Storage.
        - **Se procesan los datos:** Estos datos generados y almacenados pueden ser vistos por otros usuarios en la pantalla principal. También se pueden llegar a usar para generar avistamientos y notificaciones en tiempo real mediante Firestore Listener.
        - **Se pueden actualizar:** Los datos almacenados pueden o no ser actualizados, es decir, los creadores de los posts pueden actualizar los datos mediante una pantalla de edición y, de esta forma, actualizar la base de datos de Firestore.
        - **Se eliminan los datos:** Cuando un usuario dueño de un post decide cerrarlo, todos los datos e imágenes asociadas a ese post son eliminados tanto de Firestore como de Firebase Storage. También los avistamientos sobre ese post son eliminados automáticamente de la colección "avistamientos" en la base de datos de Firestore.

- **¿Qué estrategia sigues para garantizar la consistencia e integridad de los datos?**

    - Para asegurar tanto la integridad como la consistencia de los datos hemos usado varias estrategias:
        - **Autenticacion y control de acceso:** Solo los usuarios registrados pueden ver, agregar o modificar los datos ademas nos aseguramos que cada post este asociado con un usuarioId para garantizar que solo ese usuario creador del post pueda modificar o eliminar esos datos y nadie mas.
        - **Reglas de seguridad especificas para Firestore y Storage:** Se usan reglas sobre el almacenamiento y la base de datos para que solo usuarios autorizados puedan acceder a esos datos y modificarlos aqui te dejo alguna de las reglas que hemos establecido.
        - **Validacion de datos:** Se revisan los datos que se introducen en los campos de registro, inicio de sesion etc... de esta forma aseguramos que no se introducen campos vacios y enviamos a Firestore los datos correctos.
        - **Eliminacion de datos** En el caso de que eliminemos un post se eliminara todo lo relacionado con ese post para conseguir mantener una coherencia en la base de datos.

      ```
      // Reglas para Firestore Database
      rules_version = '2';
      service cloud.firestore {
          match /databases/{database}/documents {
  
              // Reglas para la colección "usuarios"
              match /usuarios/{userId} {
                  // Permitir crear un usuario SIN necesidad de estar autenticado (para registro)
                  allow read: if true;
  
                  // Solo el usuario dueño de la cuenta puede leer y modificar su propio perfil
                  allow read, write, update, delete: if request.auth != null && request.auth.uid == userId;
              }
  
              // Reglas para la colección "mascotas_perdidas"
              match /mascotas_perdidas/{postId} {
        
                  // Todos los usuarios autenticados pueden ver las publicaciones
                  allow read: if request.auth != null;
  
                  // Permitir crear publicaciones a cualquier usuario autenticado
                  allow create: if request.auth != null;
  
                  // Solo el dueño de la publicación puede modificar o eliminarla
                  allow update, delete: if request.auth != null && request.auth.uid == resource.data.usuarioId;
              }
  
              // Reglas para la colección "avistamientos"
              match /avistamientos/{document=**} {
                  
                  // Permite leer y escribir avistamientos si están autenticados
                  allow read, write: if request.auth != null;
              }
          }
      }
      ```
      ```
      // Reglas para Firebase Storage
      rules_version = '2';
      service firebase.storage {
          match /b/{bucket}/o {
              
              // Reglas para la carpeta "fotos_mascotas"
              match /fotos_mascotas/{userId}/{fileName} {
  
                  // Permitir que todos los usuarios autenticados lean cualquier archivo si estan autenticados.
                  allow read: if request.auth != null;
  
                  // Permitir que los usuarios escriban o eliminen solo sus propios archivos
                  allow write: if request.auth != null && request.auth.uid == userId;
              }
          }
      }
      ```

---

## Almacenamiento en la nube (5f):

- **Si tu software utiliza almacenamiento en la nube, ¿cómo garantizas la seguridad y disponibilidad de los datos?**

    - En mi aplicación sí usamos servicios de almacenamiento en la nube, aunque ya casi que se resolvió esa duda en la respuesta de la pregunta anterior:
        - Utilizamos las herramientas de desarrollo de aplicaciones de Google Firebase, que incluyen varios servicios de almacenamiento en la nube.
            - Usamos **Cloud Firestore**, que es una base de datos NoSQL que almacena los datos en la nube en forma de colecciones y documentos.
            - Usamos **Firebase Storage**, que es un sistema de almacenamiento en la nube para guardar y recuperar archivos. En mi caso, lo uso para guardar las fotos de los posts de las mascotas.

    - Para garantizar la seguridad y disponibilidad de los datos en la nube, utilizamos:
        - **Reglas de seguridad en Firestore y Storage:** (mencionado más en profundidad en la respuesta anterior). De esta forma, solo pueden acceder a los datos los usuarios autorizados.
        - **Autenticación con Firebase Authentication:** Solo los usuarios registrados en la aplicación pueden interactuar con los datos almacenados en la nube.
        - **Datos en tiempo real:** Firestore nos permite que los datos sean actualizados y sincronizados en tiempo real, así no necesitamos cerrar y abrir la aplicación para que se actualicen los datos.
        - **Eliminación de datos automática:** Cuando se elimina un post, se elimina todo lo relacionado con ese post de forma automática para evitar inconsistencias.

- **¿Qué alternativas consideraste para almacenar datos y por qué elegiste tu solución actual?**

    - Desde un primer momento, valoré hacerlo con una base de datos local, pero rápidamente llegué a la conclusión de que, si necesito acceder a los datos desde cualquier lugar y de forma rápida, necesitaba usar servicios de almacenamiento en la nube.
    - Me informé a través de un conocido que trabaja en desarrollo de aplicaciones Android y me mencionó Firestore como una solución fácil de integrar y gratuita para proyectos a pequeña escala.
    - Además, permite a múltiples usuarios interactuar a la vez en tiempo real, que era uno de los requisitos que estaba buscando.
    - Cuando me surgió la necesidad de guardar archivos multimedia, como en mi caso fotos, pensé en usar Amazon S3, Google Drive o opciones similares, pero tendría que hacer una integración diferente a la de Firestore. Poco después, descubrí la existencia de Firebase Storage.
    - Firebase Storage facilita todo, aunque está limitado a 1GB de espacio gratuito (para imágenes y con un proyecto a pequeña escala, suficiente). De esta forma, tendría integración nativa con Firestore y se facilitaría la gestión de los permisos y el acceso a los datos.

- **Si no usas la nube, ¿cómo podrías integrarla en futuras versiones?**

    - En mi caso, sí uso la nube en la aplicación, pero en un futuro se podría realizar una copia de seguridad de los datos en otro servicio en la nube diferente para aumentar la protección de los datos de los usuarios.

---

## Seguridad y regulación (5i):

- **¿Qué medidas de seguridad implementaste para proteger los datos o procesos en tu proyecto?**

    - **Autenticación de los usuarios:** Solo los usuarios registrados pueden acceder a las funcionalidades de la aplicación, limitando así el acceso a los datos.
    - **Cifrado TLS en las peticiones HTTPS con Firebase:** Firebase utiliza un cifrado sobre los datos que viajan entre la aplicación y Firebase para que no puedan ser interceptados.
    - **Se establecen unas reglas de seguridad:** Gracias a estas reglas, podemos limitar qué usuarios pueden acceder a los datos de la aplicación y podemos especificar qué datos pueden ser públicos para todos y quién tiene la posibilidad de editarlos.
    - **Se limita la frecuencia de algunas acciones:** Como, por ejemplo, la opción de recuperar contraseña, que contiene una cuenta atrás de 1 minuto para poder mandar otra petición. Así evitamos la posibilidad de un ataque de denegación de servicio por múltiples peticiones.
    - **Filtros anti-spam:** La opción de recuperar contraseña también contiene un filtro de Firebase, el cual solo puede recibir de 5 a 10 peticiones por hora desde ese correo electrónico para evitar spam. Si se supera, se bloquea ese correo electrónico.
    - **Validaciones en los campos:** Validamos la información que queremos que llegue a Firestore, evitando así inyecciones de datos no válidos.

- **¿Qué normativas (e.g., GDPR) podrían afectar el uso de tu software y cómo las has tenido en cuenta?**

    - Cumplo algunos puntos de la GDPR, pero podrían ser más. Aquí te dejo los que se implementan en el proyecto y los que podrían implementarse en un futuro.
        - **Implementados:**
            - **Derecho al olvido:** Los usuarios, en cualquier momento, pueden eliminar todos sus posts e informaciones relacionadas con los mismos. Tambien existe la posibilidad a traves de un botón en perfil de eliminar la cuenta por completo y no dejar ratro de nada, borrando posts, avistamientos y la cuenta completamente.
            - **Acceso a los datos:** Solo el dueño del post puede editarlo y eliminarlo completamente.
            - **Almacenamiento seguro:** Los datos que el usuario introduce en la aplicación se almacenan en Firebase, que cumple con los estándares de seguridad y regulaciones internacionales.
            - **Consentimiento del usuario:** Implementada una checkbox para que los usuarios acepten los términos antes de registrar una cuenta y aclarar que sus datos pueden ser recogidos.
            - **Uso responsable de los datos:** La aplicación solo solicita los datos que sean estrictamente necesarios para que funcione correctamente y no se solicita información sensible (como datos bancarios) ni se retienen datos innecesarios (cuando se elimina algo, es permanente).

        - **No implementados, pero podrían implementarse en un futuro:**
            - **Avisos de violaciones de seguridad:** Implementar un sistema que avise a todos los usuarios de la aplicación en caso de que se encuentre una brecha de seguridad y sus datos puedan estar en peligro.
            - **Protección de menores:** Limitar la recogida de datos de menores de edad sin la supervisión de un tutor.

- **Si no implementaste medidas de seguridad, ¿qué riesgos potenciales identificas y cómo los abordarías en el futuro?**

    - Aunque hay algunas medidas de seguridad establecidas, me gustaría mejorar la validación de la información porque creo que puede ser aún más estricta.
    - Otra implementación que me gustaría realizar sería la autenticación en dos pasos para proteger las cuentas de los usuarios de accesos no autorizados.

---

## Implicación de las THD en negocio y planta (2e):

- **¿Qué impacto tendría tu software en un entorno de negocio o en una planta industrial?**

    - La aplicación no está desarrollada para una planta industrial o negocio con el propósito de sacar rédito de ello, aunque podría cubrir un servicio importante en lugares como refugios de animales o protectoras.
    - De esta forma, podría usarse para obtener información sobre mascotas perdidas y avistamientos en tiempo real en la zona de operación de estos refugios o protectoras.

- **¿Cómo crees que tu solución podría mejorar procesos operativos o la toma de decisiones?**

    - La aplicación podría ayudar a centralizar los reportes de mascotas perdidas y avistamientos en una sola plataforma.
    - Los refugios y protectoras podrían llegar a usar la aplicación para registrar las mascotas encontradas y avisar a los dueños de dónde se encuentran.
    - Mediante un pequeño análisis de los datos de mascotas perdidas por zonas y localidades, se podrían generar mapas de calor para identificar qué zonas son las más comunes donde se pierden o encuentran mascotas.

- **Si tu proyecto no aplica directamente a negocio o planta, ¿qué otros entornos podrían beneficiarse?**

    - Como ya mencioné, creo que los principales beneficiados pueden ser asociaciones de mascotas perdidas, refugios y protectoras.
    - También se podrían beneficiar, a una escala mayor, ayuntamientos o entidades gubernamentales para mejorar la gestión de mascotas perdidas en la ciudad o territorio sobre el que gobiernen.
    - Otra opción sería para vecindarios o comunidades, permitiendo que los vecinos estén al tanto de las mascotas perdidas de sus alrededores y aumentando la posibilidad de que sean encontradas.

---

## Mejoras en IT y OT (2f):

- **¿Cómo puede tu software facilitar la integración entre entornos IT y OT?**

    - Mi aplicación no está diseñada para ser integrada específicamente en entornos IT y OT.

- **¿Qué procesos específicos podrían beneficiarse de tu solución en términos de automatización o eficiencia?**

    - La aplicación tiene varios usos en los cuales pueden mejorar la eficiencia y automatización de procesos, que son los siguientes:
        - **Mejora en la búsqueda y localización de mascotas perdidas por sus dueños:** Gracias a la aplicación, podemos estar enterados de las mascotas perdidas en nuestra zona para estar alerta. Los dueños no están solos y cuentan con el apoyo del resto de los usuarios, por lo que se agiliza el proceso de búsqueda de la mascota perdida y se puede reducir el tiempo necesario para recuperarla.
        - **Mejora en la gestión de refugios y protectoras:** Como ya hemos mencionado, la app podría usarse en estos establecimientos para facilitar la búsqueda de los dueños de las mascotas perdidas en su zona con mayor facilidad.
        - **Uso de la aplicación en las clínicas veterinarias:** Se podría utilizar para encontrar rápidamente a los dueños de mascotas perdidas que lleguen a sus establecimientos.

- **Si no aplica a IT u OT, ¿cómo podrías adaptarlo para mejorar procesos tecnológicos concretos?**

    - Se podría implementar inteligencia artificial en la aplicación para reconocer las imágenes de las mascotas perdidas y compararlas con una base de datos de mascotas ya reportadas, facilitando la identificación de sus dueños de manera rápida y sencilla.
    - Se podría realizar un análisis de patrones en las pérdidas de mascotas mediante el uso de Big Data, permitiendo mejorar la prevención en áreas con un mayor porcentaje de pérdidas.
    - Se podrían integrar collares con GPS para que los dueños reciban alertas tempranas si la mascota sale de una zona designada.

---

## Tecnologías Habilitadoras Digitales (2g):

- **¿Qué tecnologías habilitadoras digitales (THD) has utilizado o podrías integrar en tu proyecto?**

    - En mi aplicación se usan un par de THD, pero voy a proponer algunas más para que se puedan integrar en un futuro:
        - **Computación en la nube:** Firebase Firestore y Storage permiten almacenar y gestionar datos en tiempo real, asegurando su accesibilidad y escalabilidad.
        - **Servicios de base de datos en tiempo real:** Nos permite actualizar datos y consultarlos en tiempo real, lo que mejora la rapidez de las interacciones entre la aplicación y los usuarios.
        - **Notificaciones en tiempo real (posible integración):** Implementar un servicio como Cloud Messaging de Firebase para enviar alertas de forma instantánea o actualizaciones sobre el post, incluso con la aplicación apagada.
        - **Machine learning (posible integración):** Podríamos implementar algún tipo de reconocimiento de imágenes de mascotas para facilitar la búsqueda junto a la IA.

- **¿Cómo mejoran estas tecnologías la funcionalidad o el alcance de tu software?**

    - **Escalabilidad:** La aplicación puede manejar un gran volumen de datos sin tener problemas de infraestructura.
    - **Disponibilidad:** Los datos son accesibles desde cualquier dispositivo que tenga una conexión a internet.
    - **Seguridad:** Mediante las reglas de Firebase y la autenticación, se garantiza que solo los usuarios autorizados puedan ver los posts, crearlos, modificarlos o eliminarlos.
    - **Automatización:** Gracias a la base de datos en tiempo real, obtenemos la información actualizada de forma inmediata sin esperas.

- **Si no has utilizado THD, ¿cómo podrías implementarlas para enriquecer tu solución?**

    - En mi aplicación sí usé algunas THD, pero aquí propongo algunas opciones más:
        - **Integración con redes sociales:** Los usuarios podrían compartir los posts de la aplicación en plataformas como Twitter, Facebook o WhatsApp para aumentar la visibilidad.