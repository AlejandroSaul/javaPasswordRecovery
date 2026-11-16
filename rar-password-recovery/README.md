# RAR Password Recovery (Java 8)

Aplicación de escritorio Swing para comprobar, de forma secuencial, contraseñas de una columna de un Excel contra **un archivo RAR propio o para el que tengas autorización**.

## Requisitos

- JDK 8 (no solo JRE).
- Eclipse IDE o Spring Tool Suite (STS).
- Maven integrado en Eclipse/STS.
- 7-Zip instalado en Windows. Descarga: https://www.7-zip.org/
- Un Excel `.xlsx` con contraseñas en la primera columna de la primera hoja.

## Importar en Eclipse / STS

1. Descomprime el ZIP.
2. Selecciona `File > Import > Maven > Existing Maven Projects`.
3. En `Root Directory`, selecciona la carpeta `rar-password-recovery`.
4. Selecciona el `pom.xml` y termina la importación.
5. Clic derecho al proyecto > `Maven > Update Project`.
6. Comprueba que el proyecto use JDK 8: `Properties > Java Build Path` y `Java Compiler`.
7. Ejecuta `src/main/java/com/recovery/Main.java` como `Java Application`.

## Uso

1. En la aplicación, selecciona el archivo `.rar`.
2. Selecciona el Excel `.xlsx`.
3. Selecciona `7z.exe` (normalmente `C:\Program Files\7-Zip\7z.exe` o `C:\Program Files (x86)\7-Zip\7z.exe`).
4. Pulsa **Iniciar**.
5. El programa prueba las filas de la primera columna, en orden. Se detiene al encontrar una contraseña que permita completar `7z t`.
6. Puedes guardar el resultado en un TXT.

## Formato del Excel

- Primera hoja del libro.
- Primera columna (A).
- Una contraseña por celda.
- Por defecto se empieza en la fila 1 de Excel. Si tienes encabezado, activa la casilla correspondiente en la aplicación.
- Las celdas se leen como texto con `DataFormatter`; para contraseñas con ceros iniciales, es mejor que las celdas estén guardadas como texto en Excel.

## Notas importantes

- El programa **no extrae** el contenido del RAR. Ejecuta `7z t` para probar la integridad.
- Un código de salida 0 de 7-Zip se considera una prueba exitosa. Los errores de archivo dañado, archivo no compatible o ejecución se muestran como error; no se marcan como contraseña incorrecta.
- 7-Zip recibe la contraseña como argumento de proceso (`-p...`). En Windows, otros procesos locales con permisos suficientes podrían verla temporalmente en la línea de comandos. Usa el programa en un equipo privado y no ejecutes la recuperación en un equipo compartido.
- No se incluyen diccionarios ni funciones para atacar servicios o archivos de terceros.
- El rendimiento depende del algoritmo y del tamaño del RAR. Los RAR con cifrado moderno pueden ser lentos de comprobar.

## Estructura

- `view`: interfaz Swing.
- `controller`: coordinación y ejecución en segundo plano con `SwingWorker`.
- `service`: lectura Excel y prueba del archivo con 7-Zip.
- `model`: objetos de datos y resultado.
- `util`: validaciones y detección de 7-Zip.
