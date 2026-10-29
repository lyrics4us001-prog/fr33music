# Fr33Music — Fase 1 (Base Reproducible)

Reproductor de música para Android basado en Kotlin, Jetpack Compose y Material 3, con extracción anónima de audio desde YouTube mediante NewPipeExtractor y reproducción en primer/segundo plano con Jetpack Media3 (ExoPlayer).

---

## 📱 Características de la Fase 1

1. **Búsqueda en YouTube:**
   - Búsqueda en tiempo real por texto (título, artista).
   - Visualización de carátula, nombre del tema y canal/artista.
   - Estados de interfaz: Inicial (Idle), Cargando (Shimmer skeleton), Resultados (Success), Sin resultados (Empty) y Error con botón de reintento.
2. **Reproducción de Audio:**
   - Selección inteligente de streams con `AudioStreamSelector`: prioriza streams `OPUS` de alta fidelidad con fallback transparente a `M4A`.
   - Streaming directo vía ExoPlayer inyectando cabeceras reales de navegador (`User-Agent`, `Referer: https://www.youtube.com/`, `Cookie`).
3. **Reproducción en Segundo Plano:**
   - Servicio en primer plano (`Fr33MusicPlayerService`) derivado de `MediaSessionService` con `foregroundServiceType="mediaPlayback"`.
   - Notificación multimedia del sistema con controles interactivos: Play/Pausa, Pista Siguiente, Pista Anterior, carátula y metadatos.
   - Sobrevive a la minimización o cierre de la actividad.
4. **Mini Player Global:**
   - Flota sobre la barra de navegación inferior (`BottomNavigation`).
   - Se activa únicamente cuando hay una cola de reproducción activa.
   - Muestra carátula, título, artista, botón de play/pausa y barra de progreso lineal.
   - Un toque abre el reproductor a pantalla completa (`PlayerScreen`).
5. **Navegación por Pestañas:**
   - Pestaña **Buscar** (100% funcional).
   - Pestaña **Principal** (Placeholder informativo de Fase 2).
   - Pestaña **Biblioteca** (Placeholder informativo de Fase 2).

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material 3 (Material Design 3 dark theme)
- **Inyección de dependencias:** Dagger Hilt 2.51.1 + KSP
- **Reproductor:** AndroidX Media3 1.4.1 (ExoPlayer, MediaSession, SessionService)
- **Extracción de streams:** NewPipeExtractor (v0.26.5 vía JitPack)
- **Carga de imágenes:** Coil Compose 2.7.0
- **Red:** OkHttp 4.12.0
- **Navegación:** Navigation Compose 2.8.3
- **Serialización:** Kotlinx Serialization JSON 1.7.3
- **SDK:** `minSdk = 26`, `compileSdk = 34`, `targetSdk = 34`

---

## 📂 Arquitectura Modular

```
Fr33Music/
├── app/                       # Aplicación principal, navegación, MainActivity, MiniPlayer
├── core/
│   ├── designsystem/          # Tema Dark (#030303, #212121, #FF0000), tokens y componentes (TrackRow, etc.)
│   ├── domain/                # Modelos de datos (Track, AudioStream, AudioFormat) y errores
│   ├── extractor/             # Implementación NewPipeExtractor, Downloader OkHttp y AudioStreamSelector
│   └── player/                # ExoPlayer, PlayerRepository y MediaSessionService
└── feature/
    ├── home/                  # Pantalla Principal (Placeholder Fase 2)
    ├── search/                # Pantalla de Búsqueda funcional (SearchViewModel, SearchScreen)
    ├── player/                # Reproductor a pantalla completa (PlayerViewModel, PlayerScreen)
    └── library/               # Pantalla de Biblioteca (Placeholder Fase 2)
```

---

## 🚀 Guía Rápida para Compilar e Instalar

### 1. Requisitos previos
- Dispositivo Android real con depuración USB activada (Android 8.0 / API 26 o superior; probado en Android 13/14).
- Conexión a Internet en el equipo para la primera compilación.
- El proyecto ya viene preconfigurado con Gradle 8.13 Wrapper y JDK 17 en `gradle.properties`.

### 2. Compilar el APK de depuración
Abre una terminal en la raíz del proyecto y ejecuta:

#### En Windows (PowerShell / CMD):
```powershell
.\gradlew.bat assembleDebug
```

#### En Linux / macOS:
```bash
./gradlew assembleDebug
```

El APK compilado se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---

### 3. Instalar en tu dispositivo Android real

1. Conecta tu teléfono mediante cable USB a la computadora.
2. Asegúrate de autorizar la depuración USB en la pantalla de tu móvil si aparece el aviso.
3. Verifica que tu teléfono sea reconocido:
   ```cmd
   adb devices
   ```
4. Instala el APK directamente con:
   ```cmd
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```
   *(Si tienes `adb` en la ruta por defecto del SDK de Android: `C:\Users\Paisny\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk`)*

---

### 4. Ver logs en tiempo real (Logcat)
Para filtrar únicamente los registros de la aplicación Fr33Music:

```cmd
adb logcat -v time -s Fr33Music:V ExoPlayer:V
```

O para ver todas las salidas y posibles excepciones de la aplicación:
```cmd
adb logcat --pid=$(adb shell pidof -s com.fr33music)
```

---

## 🧪 Ejecutar Pruebas Unitarias
El proyecto incluye tests unitarios obligatorios para el selector de streams (`AudioStreamSelectorTest`):

```powershell
.\gradlew.bat testDebugUnitTest
```
