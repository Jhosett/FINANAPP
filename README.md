# FinanApp

Aplicación móvil Android para **registrar, consultar y administrar ingresos y gastos personales**, visualizar el balance disponible y analizar el comportamiento de los movimientos. Desarrollada en Kotlin con Jetpack Compose como proyecto académico del segundo corte.

> **Integrantes:**
Paula Johana García Rodríguez - 0192092
Stefanny Ximena Páez Bayona - 0192019
Darwin Andrey Gómez - 0191967
Darwin Jhosett Bermúdez Romero - 0192106

> **Asignatura / Docente:** Didier Guerrero

---

## Tabla de contenido

1. [Descripción del proyecto](#1-descripción-del-proyecto)
2. [Funcionalidades](#2-funcionalidades)
3. [Tecnologías utilizadas](#3-tecnologías-utilizadas)
4. [Arquitectura y estructura del proyecto](#4-arquitectura-y-estructura-del-proyecto)
5. [Solución de persistencia: Room](#5-solución-de-persistencia-room)
6. [Instrucciones de ejecución](#6-instrucciones-de-ejecución)
7. [Flujo de prueba sugerido](#7-flujo-de-prueba-sugerido)

---

## 1. Descripción del proyecto

FinanApp es una aplicación móvil que permite a cada usuario llevar el control de su dinero: registra ingresos y gastos, los clasifica por categoría y fecha, y calcula automáticamente el saldo disponible. Además ofrece un historial editable y una sección de estadísticas con gráficas.

La interfaz usa una paleta de **tonos verdes**, coherente con el contexto de una aplicación de finanzas: verde para ingresos y rojo para gastos, de modo que se distingan de facilmente.

Todos los datos se guardan **de forma local en el dispositivo** (no requiere internet) gracias a RoomDatabase y cada usuario ve únicamente sus propios movimientos.

---

## 2. Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Registro de usuarios** | Crea una cuenta con nombre completo, correo, usuario y contraseña. Valida formato de correo, longitud mínima y coincidencia de contraseña e impide usuarios o correos repetidos. |
| **Inicio de sesión** | Valida las credenciales con la base de datos. Visualiza un mensaje de error si falla el usuario o la contraseña en caso de que estos no sean ingresados correctamente. |
| **Panel principal (Dashboard)** | Muestra el saldo disponible, el total de ingresos y gastos y los últimos movimientos. Da acceso al resto de pantallas con una barra de navegación inferior. |
| **Registrar movimiento** | Formulario para ingresos y gastos: descripción, valor, categoría, fecha y nota opcional. Diferencia visualmente ingreso y gasto y valida que los obligatorios estén completos y que el valor sea mayor que cero. |
| **Historial (Gestión)** | Lista todos los movimientos, permite **editarlos y eliminarlos**, y filtrarlos por tipo o categoría. Recalcula el balance automáticamente. |
| **Estadísticas** | Muestra un apartado visual de Ingresos vs. gastos, gastos por categoría (dona e histograma), ranking de categorías y actividad de los últimos 7 días. |

---

## 3. Tecnologías utilizadas

| Tecnología | Uso en el proyecto |
|---|---|
| **Kotlin** | Lenguaje principal. |
| **Jetpack Compose + Material 3** | Interfaz de usuario declarativa y sistema de diseño. |
| **Navigation 3** (`navigation3-runtime`, `navigation3-ui`) | Navegación entre pantallas con `NavDisplay` y una pila (`backStack`) administrada por la app. |
| **Room** (`room-runtime`, `room-ktx`) | Persistencia local sobre SQLite. |
| **KSP** (Kotlin Symbol Processing) | Genera en compilación el código de Room (consultas y base de datos). |
| **ViewModel + StateFlow** (`lifecycle-viewmodel-compose`) | Estado de las pantallas y comunicación con la capa de datos. |
| **Corrutinas y Flow** | Operaciones fuera del hilo principal y datos reactivos. |
| **Material Icons Extended** | Íconos de la interfaz. |
| **PBKDF2 (`javax.crypto`)** | Protección de contraseñas con hash y sal. |
| **Gradle (Kotlin DSL)** | Construcción y gestión de dependencias. |

**Requisitos mínimos del dispositivo:** Android 8.0 (API 26) o superior. El hash de contraseñas usa `PBKDF2WithHmacSHA256` y la pantalla de registro usa `java.time`, que necesitan API 26.

---

## 4. Arquitectura y estructura del proyecto

El proyecto separa la **interfaz** (`ui`) de la **capa de datos** (`data`). Por lo tanto permite un manejo organizado de las vistas y base de datos dentro de la aplicación de manera individual.

### Estructura de carpetas

```
com.ufpso.finanapp/
│
├── MainActivity.kt              ← Punto de entrada; aplica el tema y lanza FinanApp()
│
├── ui/                          ← CAPA DE INTERFAZ
│   ├── MyApp.kt                 ← FinanApp(): define la navegación y conecta pantallas con ViewModels
│   ├── keys/                    ← Rutas de navegación (LoginRoute, RegisterRoute, DashboardRoute, ...)
│   ├── components/              ← Componentes reutilizables (FinanBottomBar)
│   ├── screens/                 ← Pantallas de la aplicación
│   ├── theme/                   ← Colores, tipografía y tema Material 3
│   └── viewmodel/               ← ViewModels (estado de las pantallas)
│
└── data/                        ← CAPA DE DATOS (independiente de la interfaz)
    ├── local/
    │   ├── entity/              ← Entidades: las tablas de Room
    │   ├── dao/                 ← DAOs: las consultas SQL
    │   └── database/            ← La instancia de la base de datos
    ├── repository/              ← Repositorios: intermediarios entre ViewModel y DAO
    └── security/                ← Utilidades de seguridad (hash de contraseñas)
```

### Carpeta `ui/`

| Archivo | Función |
|---|---|
| `MyApp.kt` | `FinanApp()` mantiene la pila de navegación y asocia cada ruta con su pantalla. Crea los ViewModels y les pasa los datos a las pantallas. |
| `keys/` | Objetos que identifican cada pantalla (`LoginRoute`, `RegisterRoute`, `DashboardRoute`, `RegisterMovementRoute`, `ManagementRoute`, `StatisticsRoute`). |
| `components/FinanBottomBar.kt` | Barra de navegación inferior (Panel, Registrar, Historial, Estadísticas) compartida por las pantallas principales. |
| `screens/LoginScreen.kt` | Inicio de sesión con validación de campos. |
| `screens/RegisterScreen.kt` | Registro de usuarios con validaciones por campo. |
| `screens/DashboardScreen.kt` | Panel con saldo, totales y últimos movimientos. |
| `screens/RegisterMovementScreen.kt` | Formulario para registrar ingresos y gastos. |
| `screens/ManagementScreen.kt` | Historial con filtros, edición y eliminación. |
| `screens/StatisticsScreen.kt` | Gráficas y resumen estadístico. |
| `screens/WeeklyTrendCard.kt` | Gráfica de barras de ingresos y gastos de los últimos 7 días. |
| `theme/` | Paleta verde (`Color.kt`), tipografía y `FINANAPPTheme`. |
| `viewmodel/AuthViewModel.kt` | Registro e inicio de sesión; guarda el usuario con sesión activa. |
| `viewmodel/MovementViewModel.kt` | Guarda, edita y elimina movimientos y expone el estado del Dashboard, el Historial y las Estadísticas. |

Las pantallas **no reciben el ViewModel**: reciben datos y funciones (*state hoisting*). Así cada pantalla puede previsualizarse con `@Preview` sin necesidad de una base de datos.

### Carpeta `data/`

| Archivo | Función |
|---|---|
| `local/entity/UserEntity.kt` | Tabla `users`: id, nombre completo, correo, usuario, hash de contraseña y fecha de creación. |
| `local/entity/TransactionEntity.kt` | Tabla `transactions`: cada ingreso o gasto, ligado a un usuario. |
| `local/dao/UserDao.kt` | Insertar un usuario y buscarlo por nombre de usuario o por correo. |
| `local/dao/TransactionDao.kt` | Insertar, editar y eliminar movimientos, y consultas de lectura (últimos movimientos, todos, totales, gastos por categoría y totales por día). Define también las clases `CategoryTotal` y `DailyTotal` para los resultados agregados. |
| `local/database/AppDatabase.kt` | Declara las entidades y la versión de la base de datos, expone los DAOs y entrega una única instancia (singleton). Incluye la migración de la versión 1 a la 2. |
| `repository/UserRepository.kt` | Lógica de registro e inicio de sesión: normaliza datos, comprueba duplicados, aplica el hash y devuelve un `AuthResult` (éxito o error con mensaje). |
| `repository/TransactionRepository.kt` | Punto de acceso a los movimientos: limpia los datos y delega en el DAO. |
| `security/PasswordHasher.kt` | Genera y verifica hashes de contraseña con PBKDF2 y sal aleatoria. |

---

## 5. Solución de persistencia: Room

### ¿Por qué Room?

Para garantizar que los registros financieros del usuario se almacenen de forma segura, rápida y sin depender de una conexión constante a internet, se implementó Room Database como solución de persistencia local. Ya que proporciona las siguientes ventajas dentro de la
aplicación:

- **Eficiencia y bajo consumo::** Al estar construida sobre SQLite, la base de datos ocupa un espacio ínfimo en el dispositivo (pocos kilobytes para su estructura y apenas un par de megabytes tras años de registros), sin impactar el rendimiento de la aplicación.
- **Programación reactiva con Flow:** Permite observar las consultas en tiempo real, actualizando de forma automática las interfaces de Jetpack Compose cada vez que se ejecutan operaciones CRUD (crear, actualizar o eliminar movimientos).
- **Arquitectura limpia:** Facilita una separación clara de responsabilidades gracias a su estructura modular basada en Entidades, DAOs y Repositorios, manteniendo el código escalable y fácil de mantener.
- **Funciona sin conexión:** todo se almacena en el dispositivo.
- **Cálculos en la base de datos:** los totales y las agrupaciones se resuelven con SUM y GROUP BY, no recorriendo listas en memoria.
- **Seguridad en tiempo de compilación:** un error en una consulta SQL se detecta al compilar, no al ejecutar.


### Base de datos `finanapp.db`

**Tabla `users`**

| Columna | Descripción |
|---|---|
| `id` | Identificador generado automáticamente. |
| `fullName` | Nombre completo (se usa para saludar en el Dashboard). |
| `email` | Correo, **único** (índice `unique`). Se guarda en minúsculas. |
| `username` | Nombre de usuario, **único**. Se guarda en minúsculas. |
| `passwordHash` | Hash de la contraseña con sal (`sal:hash`). Nunca el texto original. |
| `createdAt` | Fecha de creación en milisegundos. |

**Tabla `transactions`**

| Columna | Descripción |
|---|---|
| `id` | Identificador generado automáticamente. |
| `userId` | Usuario dueño del movimiento. Es llave foránea hacia `users.id` y tiene índice. |
| `description` | Descripción corta del movimiento. |
| `amount` | Valor siempre positivo. El campo `isIncome` indica si suma o resta. |
| `isIncome` | `true` = ingreso, `false` = gasto. |
| `category` | Categoría elegida (Alimentación, Salario, etc.). |
| `dateEpochDay` | Fecha del movimiento como número de días desde 1970 (`LocalDate.toEpochDay()`). Facilita ordenar y agrupar por día. |
| `note` | Nota opcional. |
| `createdAt` | Fecha de creación del registro. |

### Cómo funciona cada pieza de Room

- **Entity** (`@Entity`): una clase Kotlin que representa una tabla. Cada propiedad es una columna y cada objeto, una fila.
- **DAO** (`@Dao`): una interfaz con las operaciones sobre la base de datos (`@Insert`, `@Query`). Room escribe la implementación.
- **Database** (`@Database`): une las entidades y los DAOs y entrega la instancia única de la base de datos.
- **Repository**: oculta el acceso a datos al resto de la app y centraliza las reglas (por ejemplo, normalizar y comprobar duplicados antes de insertar).
- **ViewModel**: obtiene los `Flow` del repositorio, los combina (por ejemplo, ingresos y gastos para calcular el saldo) y los expone como `StateFlow` a la interfaz.

### Consultas principales (`TransactionDao`)

| Consulta | Uso |
|---|---|
| `observeLatest` | Últimos movimientos del Dashboard. |
| `observeAll` | Listado completo del Historial. |
| `observeTotalIncome` / `observeTotalExpense` | Totales para el saldo (`ingresos − gastos`). |
| `observeExpenseByCategory` | Gastos agrupados por categoría (dona, histograma y ranking). |
| `observeDailyTotals` | Ingresos y gastos por día (gráfica de los últimos 7 días). |
| `updateById` / `deleteById` | Editar y eliminar un movimiento del usuario. |

### Seguridad de las contraseñas

Las contraseñas **no se guardan en texto plano**. `PasswordHasher` aplica:

- **PBKDF2 con HMAC-SHA256**, 120 000 iteraciones y clave de 256 bits.
- Una **sal aleatoria de 16 bytes** distinta por usuario, de modo que dos personas con la misma contraseña tienen hashes diferentes.
- Comparación en tiempo constante (`MessageDigest.isEqual`) al verificar.

El hash se calcula fuera del hilo principal para no congelar la pantalla.

---

## 6. Instrucciones de ejecución

### Requisitos

- **Android Studio** en una versión reciente (con soporte para Compose y el SDK 37).
- **JDK** incluido con Android Studio.
- Un **emulador** o un teléfono con **Android 8.0 (API 26) o superior**.
- Conexión a internet solo para la primera sincronización de Gradle.

### Pasos

1. **Clonar** el repositorio:
   ```bash
   git clone <URL-DEL-REPOSITORIO>
   ```
2. Abrir la carpeta del proyecto en **Android Studio** (`File → Open`).
3. Esperar a que termine la **sincronización de Gradle** (`File → Sync Project with Gradle Files` si no arranca sola).
4. Elegir un dispositivo:
   - **Emulador:** `Device Manager → Create Device` (por ejemplo Pixel con Android 14), o
   - **Teléfono:** activar *Opciones de desarrollador* y *Depuración USB*, y conectarlo por cable.
5. Pulsar **Run ▶** (`Shift + F10`) con el módulo `app` seleccionado.

### Generar un APK para instalar en un teléfono

1. `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
2. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.
3. Copiarlo al teléfono, abrirlo y permitir la instalación de apps desconocidas cuando se solicite.

El APK *debug* se firma automáticamente con una clave de depuración, por lo que no requiere configuración adicional.

---

## 7. Flujo de prueba sugerido

Para validar el correcto funcionamiento de **FinanApp**, las validaciones de seguridad, la reactividad de la interfaz y la persistencia local con Room, te sugerimos seguir paso a paso este flujo de evaluación:

1. **Autenticación (Login y Registro):**
   - Abre la aplicación para visualizar la pantalla de inicio de sesión.
   - Selecciona **«Regístrate aquí»** y crea un nuevo usuario.
   - *Casos de error a probar:* Intenta registrarte con un correo inválido, contraseñas que no coincidan o un nombre de usuario repetido para comprobar las validaciones.
   - Inicia sesión con las credenciales creadas (prueba introduciendo una contraseña incorrecta para verificar el control de errores).

2. **Estado Inicial del Panel (Dashboard):**
   - Al ingresar, verifica que los valores monetarios se muestren en `$ 0` y aparezca el aviso indicando que aún no existen movimientos registrados.

3. **Registro de Movimientos Financieros:**
   - Dirígete a la sección de registro y añade diversos ingresos y gastos, variando las categorías, montos y fechas.
   - *Casos de error a probar:* Intenta guardar con campos vacíos o un valor en `0` para comprobar las restricciones del formulario.

4. **Sincronización en Tiempo Real:**
   - Regresa al Panel Principal y comprueba que el saldo actual, los totales y la lista de últimos movimientos se actualicen de forma automática gracias al flujo reactivo de la base de datos.

5. **Gestión e Historial:**
   - Entra a la sección de historial para filtrar los movimientos por tipo y categoría.
   - Edita un registro existente y elimina otro, verificando que el saldo general se recalcule de inmediato.

6. **Estadísticas y Análisis:**
   - Explora la sección de estadísticas para revisar las gráficas de comportamiento financiero e interactúa seleccionando un día específico en la vista detallada de 7 días.
