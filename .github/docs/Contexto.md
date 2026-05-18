# Contexto

Sanos y Salvos gestiona simultáneamente múltiples reportes, los cuales provienen de diferentes fuentes.

- Dueños que registran mascotas perdidas.
- Personas que encuentran animales en la calle.
- Clínicas veterinarias que reciben animales sin identificación.
- Refugios y municipalidades

Actualmente, gran parte de esa información se gestiona mediante formulario web simple, correo electrónico y redes sociales, lo que provoca problemas como manejar información incompleta o desestructurada, dificultad para visualizar zonas con mayor incidencia de extravíos y problemas de coordinación entre entidades que se ofrecen para colaborar en la búsqueda y rescate.

Debido a estas limitaciones muchas coincidencias entre mascotas perdidas y encontradas no se detectan a tiempo, reduciendo las probabilidades de encontrar a sus dueños.

Para mejorar esta situación Sanos y Salvos busca desarrollar una plataforma tecnológica centralizada que permita registrar reportes estructurados, visualizar información geográfica y utilizar algoritmos para identificar posibles coincidencias entre mascotas perdidas y encontradas.

La solución debe contemplar tres módulos principales:

- **Gestión de Mascotas**: Permitir el registro estructurado de mascotas perdidas o encontradas, incluyendo características físicas, fotografías, ubicación geográfica y datos de contacto.
- **Sistema de Geolocalización**: Permite visualizar en mapa los reportes de mascotas perdidas o encontradas, identificando zonas con mayor incidencia de extravíos.
- **Motor de coincidencias**: Analizar reportes de mascotas perdidas y encotnradas utilizando algoritmos que permitan identificar posibles coincidencias en función de raza, color, tamaño, ubicación y fecha del reporte.

# Requerimientos Técnicos

Los/as estudiantes deberán diseñar una arquitectura de microservicios escalable, aplicando patrones de diseño y arquetipos arquitectónicos que permitan la modularización del sistema. Para ello, deberán:

- Definir los microservicios clave, asegurando separación de responsabilidades y escalabilidad.
- Diseñar una API Gateway que gestione la comunicación entre microservicios y el frontend.
- Implementar patrones como Repository Pattern para la persistencia de datos, Factory Method para la creación de instancias y Circuit Breaker para manejar fallos en la comunicación entre servicios.
- Asegurar que los servicios sean escalables y desacoplados, permitiendo futuras mejoras sin afectar el funcionamiento del sistema.
- Documentar las decisiones arquitectónicas y justificar la selección de patrones.