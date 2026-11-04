# Diagnóstico de Código - Fr33Music

Este documento contiene la estructura del árbol del proyecto y el contenido íntegro de todos los archivos fuente principales.

## 1. Estructura del Árbol de Directorios

```
Fr33Music/
├── .gitignore
├── README.md
├── app
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── fr33music
│           │           ├── Fr33MusicApp.kt
│           │           ├── MainActivity.kt
│           │           ├── di
│           │           │   └── AppModule.kt
│           │           └── ui
│           │               ├── MainScreen.kt
│           │               └── MiniPlayer.kt
│           └── res
│               ├── drawable
│               │   ├── ic_launcher_background.xml
│               │   └── ic_launcher_foreground.xml
│               ├── mipmap-anydpi-v26
│               │   ├── ic_launcher.xml
│               │   └── ic_launcher_round.xml
│               └── values
│                   ├── strings.xml
│                   └── themes.xml
├── build.gradle.kts
├── core
│   ├── database
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           ├── AndroidManifest.xml
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── core
│   │                           └── database
│   │                               ├── AppDatabase.kt
│   │                               ├── OfflineAudioSourceImpl.kt
│   │                               ├── dao
│   │                               │   ├── AlbumDao.kt
│   │                               │   ├── ArtistDao.kt
│   │                               │   ├── DownloadDao.kt
│   │                               │   ├── LyricsDao.kt
│   │                               │   ├── PlaylistDao.kt
│   │                               │   ├── SearchHistoryDao.kt
│   │                               │   └── TrackDao.kt
│   │                               ├── datastore
│   │                               │   └── QueueDataStore.kt
│   │                               ├── di
│   │                               │   └── DatabaseModule.kt
│   │                               ├── download
│   │                               │   ├── DownloadManager.kt
│   │                               │   ├── DownloadRepository.kt
│   │                               │   └── DownloadWorker.kt
│   │                               ├── entity
│   │                               │   ├── AlbumEntity.kt
│   │                               │   ├── ArtistEntity.kt
│   │                               │   ├── DownloadEntity.kt
│   │                               │   ├── LyricsEntity.kt
│   │                               │   ├── PlaylistEntity.kt
│   │                               │   ├── PlaylistSongCrossRef.kt
│   │                               │   ├── SearchHistoryEntity.kt
│   │                               │   └── TrackEntity.kt
│   │                               ├── lyrics
│   │                               │   └── LyricsRepository.kt
│   │                               └── repository
│   │                                   ├── LibraryRepository.kt
│   │                                   └── LibraryRepositoryImpl.kt
│   ├── designsystem
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           ├── java
│   │           │   └── com
│   │           │       └── fr33music
│   │           │           └── core
│   │           │               └── designsystem
│   │           │                   ├── component
│   │           │                   │   ├── ErrorView.kt
│   │           │                   │   ├── IconButtonM3.kt
│   │           │                   │   ├── LoadingSkeleton.kt
│   │           │                   │   ├── PrimaryButton.kt
│   │           │                   │   ├── TrackRow.kt
│   │           │                   │   └── TrackThumbnail.kt
│   │           │                   └── theme
│   │           │                       ├── Color.kt
│   │           │                       ├── Spacing.kt
│   │           │                       ├── Theme.kt
│   │           │                       └── Type.kt
│   │           └── res
│   │               └── drawable
│   │                   └── ic_music_notification.xml
│   ├── domain
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── core
│   │                           └── domain
│   │                               └── model
│   │                                   ├── AlbumCard.kt
│   │                                   ├── AudioFormat.kt
│   │                                   ├── AudioStream.kt
│   │                                   ├── ExtractorError.kt
│   │                                   ├── LookingSearchResult.kt
│   │                                   ├── OfflineAudioSource.kt
│   │                                   ├── PlaybackError.kt
│   │                                   └── Track.kt
│   ├── extractor
│   │   ├── build.gradle.kts
│   │   └── src
│   │       ├── main
│   │       │   └── java
│   │       │       └── com
│   │       │           └── fr33music
│   │       │               └── core
│   │       │                   └── extractor
│   │       │                       ├── AudioStreamSelector.kt
│   │       │                       ├── ExtractorInfo.kt
│   │       │                       ├── NewPipeDownloader.kt
│   │       │                       ├── NewPipeSearchSource.kt
│   │       │                       ├── NewPipeStreamResolver.kt
│   │       │                       ├── SearchSource.kt
│   │       │                       └── StreamResolver.kt
│   │       └── test
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── core
│   │                           └── extractor
│   │                               └── AudioStreamSelectorTest.kt
│   └── player
│       ├── build.gradle.kts
│       └── src
│           └── main
│               ├── AndroidManifest.xml
│               ├── java
│               │   └── com
│               │       └── fr33music
│               │           └── core
│               │               └── player
│               │                   ├── PlaybackState.kt
│               │                   ├── PlayerRepository.kt
│               │                   ├── PlayerRepositoryImpl.kt
│               │                   ├── di
│               │                   │   └── PlayerModule.kt
│               │                   └── service
│               │                       └── Fr33MusicPlayerService.kt
│               └── res
│                   └── drawable
│                       └── ic_notification.xml
├── feature
│   ├── home
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── feature
│   │                           └── home
│   │                               ├── HomeScreen.kt
│   │                               └── HomeViewModel.kt
│   ├── library
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── feature
│   │                           └── library
│   │                               ├── LibraryScreen.kt
│   │                               └── LibraryViewModel.kt
│   ├── player
│   │   ├── build.gradle.kts
│   │   └── src
│   │       └── main
│   │           └── java
│   │               └── com
│   │                   └── fr33music
│   │                       └── feature
│   │                           └── player
│   │                               ├── PlayerScreen.kt
│   │                               ├── PlayerUiState.kt
│   │                               └── PlayerViewModel.kt
│   └── search
│       ├── build.gradle.kts
│       └── src
│           └── main
│               └── java
│                   └── com
│                       └── fr33music
│                           └── feature
│                               └── search
│                                   ├── SearchScreen.kt
│                                   ├── SearchUiState.kt
│                                   └── SearchViewModel.kt
├── gradle
│   ├── libs.versions.toml
│   └── wrapper
│       └── gradle-wrapper.properties
├── gradle.properties
├── local.properties
└── settings.gradle.kts
```

---

## 2. Contenido de Archivos Fuente

### `.gitignore`

```text
*.jks
*.keystore
local.properties
.gradle/
.kotlin/
build/
*/build/
*/*/build/
.idea/
*.iml
```

---

### `README.md`

```markdown
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
```

---

### `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fr33music"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:extractor"))
    implementation(project(":core:player"))
    implementation(project(":feature:home"))
    implementation(project(":feature:search"))
    implementation(project(":feature:player"))
    implementation(project(":feature:library"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.coil.compose)
}
```

---

### `app/proguard-rules.pro`

```pro
# Proguard rules for Fr33Music
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.schabi.newpipe.extractor.**
```

---

### `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".Fr33MusicApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Fr33Music">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/Theme.Fr33Music">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

---

### `app/src/main/java/com/fr33music/Fr33MusicApp.kt`

```kotlin
package com.fr33music

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Fr33MusicApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(imageLoader)
    }

    override fun newImageLoader(): ImageLoader = imageLoader
}
```

---

### `app/src/main/java/com/fr33music/MainActivity.kt`

```kotlin
package com.fr33music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.fr33music.core.designsystem.theme.Fr33MusicTheme
import com.fr33music.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        setContent {
            Fr33MusicTheme {
                MainScreen()
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
```

---

### `app/src/main/java/com/fr33music/di/AppModule.kt`

```kotlin
package com.fr33music.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB para miniaturas y carátulas persistentes offline
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
```

---

### `app/src/main/java/com/fr33music/ui/MainScreen.kt`

```kotlin
package com.fr33music.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.player.PlayerRepository
import com.fr33music.feature.home.HomeScreen
import com.fr33music.feature.library.LibraryScreen
import com.fr33music.feature.player.PlayerScreen
import com.fr33music.feature.search.SearchScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Principal", Icons.Default.Home),
    SEARCH("Buscar", Icons.Default.Search),
    LIBRARY("Biblioteca", Icons.Default.MusicNote)
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val playerRepository: PlayerRepository
) : ViewModel()

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playerRepository.playbackState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavTab.SEARCH) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Scaffold(
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayPauseClick = { viewModel.playerRepository.togglePlayPause() },
                        onClick = { isPlayerExpanded = true }
                    )

                    NavigationBar(
                        containerColor = ColorSurface,
                        contentColor = ColorTextPrimary
                    ) {
                        NavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(text = tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ColorPrimary,
                                    selectedTextColor = ColorPrimary,
                                    indicatorColor = ColorSurface,
                                    unselectedIconColor = ColorTextTertiary,
                                    unselectedTextColor = ColorTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.HOME -> HomeScreen()
                    NavTab.SEARCH -> SearchScreen(
                        onTrackSelected = {
                            isPlayerExpanded = true
                        }
                    )
                    NavTab.LIBRARY -> LibraryScreen()
                }
            }
        }

        // Full Screen Player Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(
                onCollapse = { isPlayerExpanded = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
```

---

### `app/src/main/java/com/fr33music/ui/MiniPlayer.kt`

```kotlin
package com.fr33music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.player.PlaybackState

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onPlayPauseClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack
    val isVisible = playbackState.queue.isNotEmpty() && track != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (track != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.extraSmall)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(Fr33Radius.medium))
                    .clip(RoundedCornerShape(Fr33Radius.medium))
                    .background(ColorSurface)
                    .clickable(onClick = onClick)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Fr33Spacing.medium, vertical = Fr33Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackThumbnail(
                            thumbnailUrl = track.thumbnailUrl,
                            contentDescription = track.title,
                            size = 48.dp,
                            shapeRadius = Fr33Radius.small
                        )

                        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = Fr33Typography.titleMedium,
                                color = ColorTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = track.artistName,
                                style = Fr33Typography.bodyMedium,
                                color = ColorTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(Fr33Spacing.small))

                        if (playbackState.isLoading) {
                            CircularProgressIndicator(
                                color = ColorPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(32.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            IconButton(onClick = onPlayPauseClick) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState.isPlaying) "Pausar" else "Reproducir",
                                    tint = ColorTextPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Progress bar line at the bottom
                    val progress = if (track.durationMs > 0) {
                        (playbackState.positionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = ColorPrimary,
                        trackColor = ColorSurfaceVariant
                    )
                }
            }
        }
    }
}
```

---

### `app/src/main/res/drawable/ic_launcher_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#030303"
        android:pathData="M0,0h108v108h-108z" />
</vector>
```

---

### `app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FF0000"
        android:pathData="M54,24c-16.57,0 -30,13.43 -30,30s13.43,30 30,30 30,-13.43 30,-30 -13.43,-30 -30,-30zM48,69V39l20,15 -20,15z" />
</vector>
```

---

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

---

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

---

### `app/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">Fr33Music</string>
</resources>
```

---

### `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Fr33Music" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#030303</item>
        <item name="android:navigationBarColor">#212121</item>
        <item name="android:windowBackground">#030303</item>
    </style>
</resources>
```

---

### `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

### `core/database/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.core.database"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:extractor"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // OkHttp & Serialization
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
}
```

---

### `core/database/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

</manifest>
```

---

### `core/database/src/main/java/com/fr33music/core/database/AppDatabase.kt`

```kotlin
package com.fr33music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.dao.PlaylistDao
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.database.entity.LyricsEntity
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.SearchHistoryEntity
import com.fr33music.core.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        LyricsEntity::class,
        SearchHistoryEntity::class,
        DownloadEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun playlistDao(): PlaylistDao
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/OfflineAudioSourceImpl.kt`

```kotlin
package com.fr33music.core.database

import android.content.Context
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.domain.model.OfflineAudioSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineAudioSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) : OfflineAudioSource {

    companion object {
        private const val MIN_FILE_SIZE_BYTES = 100 * 1024L
    }

    override suspend fun getLocalAudioPath(trackId: String): String? {
        val track = trackDao.getTrackSync(trackId)
        if (track?.isDownloaded == true && !track.localPath.isNullOrBlank()) {
            val file = File(track.localPath)
            if (file.exists() && file.length() >= MIN_FILE_SIZE_BYTES) return file.absolutePath
        }

        // Fallback directo al sistema de archivos privados
        val downloadsDir = File(context.filesDir, "downloads")
        val opus = File(downloadsDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus.absolutePath

        val m4a = File(downloadsDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a.absolutePath

        return null
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/AlbumDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fr33music.core.database.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumSync(id: String): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbum(id: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/ArtistDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fr33music.core.database.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE name = :name LIMIT 1")
    suspend fun getArtistSync(name: String): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Update
    suspend fun updateArtist(artist: ArtistEntity)

    @Query("DELETE FROM artists WHERE name = :name")
    suspend fun deleteArtist(name: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/DownloadDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE trackId = :trackId LIMIT 1")
    fun getDownload(trackId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE trackId = :trackId LIMIT 1")
    suspend fun getDownloadSync(trackId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, localPath = :localPath, progress = :progress, errorMessage = :error WHERE trackId = :trackId")
    suspend fun updateStatus(
        trackId: String,
        status: String,
        localPath: String? = null,
        progress: Int = 0,
        error: String? = null
    )

    @Query("DELETE FROM downloads WHERE trackId = :trackId")
    suspend fun deleteDownload(trackId: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/LyricsDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    fun getLyrics(trackId: String): Flow<LyricsEntity?>

    @Query("SELECT * FROM lyrics WHERE trackId = :trackId LIMIT 1")
    suspend fun getLyricsSync(trackId: String): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE trackId = :trackId")
    suspend fun deleteLyrics(trackId: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/PlaylistDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val thumbnailUrl: String?,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query("""
        SELECT p.id, p.name, p.createdAt, p.thumbnailUrl, 
               (SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlistId = p.id) as songCount 
        FROM playlists p 
        ORDER BY p.createdAt DESC
    """)
    fun getAllPlaylists(): Flow<List<PlaylistWithSongCount>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    fun getPlaylist(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistSync(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :id")
    suspend fun deletePlaylistSongs(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_songs ps ON t.id = ps.trackId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_songs ps ON t.id = ps.trackId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt ASC
    """)
    suspend fun getSongsForPlaylistSync(playlistId: Long): List<TrackEntity>

    @Query("UPDATE playlists SET thumbnailUrl = :thumbnailUrl WHERE id = :playlistId")
    suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUrl: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/SearchHistoryDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fr33music.core.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 20): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/dao/TrackDao.kt`

```kotlin
package com.fr33music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fr33music.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1 ORDER BY timestamp DESC")
    fun getDownloadedTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY timestamp DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    fun getTrack(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackSync(id: String): TrackEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :id AND isSaved = 1)")
    fun isTrackSaved(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM tracks WHERE id = :id AND isDownloaded = 1)")
    fun isTrackDownloaded(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isSaved = :isSaved, timestamp = :timestamp WHERE id = :id")
    suspend fun setSaved(id: String, isSaved: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded, localPath = :localPath, format = :format WHERE id = :id")
    suspend fun setDownloaded(id: String, isDownloaded: Boolean, localPath: String?, format: String?)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    @Query("SELECT DISTINCT artistName FROM tracks WHERE isSaved = 1 OR isDownloaded = 1 ORDER BY artistName ASC")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE artistName = :artistName AND (isSaved = 1 OR isDownloaded = 1) ORDER BY title ASC")
    fun getTracksByArtist(artistName: String): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT albumName FROM tracks WHERE albumName IS NOT NULL AND (isSaved = 1 OR isDownloaded = 1) ORDER BY albumName ASC")
    fun getDistinctAlbums(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE albumName = :albumName AND (isSaved = 1 OR isDownloaded = 1) ORDER BY title ASC")
    fun getTracksByAlbum(albumName: String): Flow<List<TrackEntity>>

    @Query("""
        SELECT 
            COALESCE(NULLIF(albumName, ''), 'Sencillos') AS albumName,
            artistName,
            COUNT(*) AS trackCount,
            MAX(thumbnailUrl) AS thumbnailUrl
        FROM tracks 
        WHERE isSaved = 1 OR isDownloaded = 1
        GROUP BY COALESCE(NULLIF(albumName, ''), 'Sencillos'), artistName
        ORDER BY albumName ASC
    """)
    fun getAlbumSummaries(): Flow<List<AlbumSummary>>

    @Query("SELECT * FROM tracks WHERE COALESCE(NULLIF(albumName, ''), 'Sencillos') = :albumName AND artistName = :artistName AND (isSaved = 1 OR isDownloaded = 1) ORDER BY title ASC")
    fun getTracksByAlbumAndArtist(albumName: String, artistName: String): Flow<List<TrackEntity>>
}

data class AlbumSummary(
    val albumName: String,
    val artistName: String,
    val trackCount: Int,
    val thumbnailUrl: String?
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/datastore/QueueDataStore.kt`

```kotlin
package com.fr33music.core.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fr33music_queue_prefs")

data class PersistedQueueState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val positionMs: Long = 0L,
    val isShuffle: Boolean = false
)

@Singleton
class QueueDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_QUEUE = stringPreferencesKey("queue_json")
        private val KEY_INDEX = intPreferencesKey("current_index")
        private val KEY_POSITION = longPreferencesKey("position_ms")
        private val KEY_SHUFFLE = booleanPreferencesKey("is_shuffle")
    }

    val queueState: Flow<PersistedQueueState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val queueJson = prefs[KEY_QUEUE]
            val queue = if (!queueJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<Track>>(queueJson)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            val index = prefs[KEY_INDEX] ?: 0
            val position = prefs[KEY_POSITION] ?: 0L
            val shuffle = prefs[KEY_SHUFFLE] ?: false

            PersistedQueueState(
                queue = queue,
                currentIndex = index.coerceIn(0, (queue.size - 1).coerceAtLeast(0)),
                positionMs = position,
                isShuffle = shuffle
            )
        }

    suspend fun getQueueStateSync(): PersistedQueueState {
        return queueState.first()
    }

    suspend fun saveQueue(queue: List<Track>, currentIndex: Int, isShuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUEUE] = json.encodeToString(queue)
            prefs[KEY_INDEX] = currentIndex
            prefs[KEY_SHUFFLE] = isShuffle
        }
    }

    suspend fun savePlaybackPosition(positionMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POSITION] = positionMs
        }
    }

    suspend fun saveCurrentIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INDEX] = index
        }
    }

    suspend fun saveShuffle(isShuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHUFFLE] = isShuffle
        }
    }

    suspend fun clearQueue() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_QUEUE)
            prefs.remove(KEY_INDEX)
            prefs.remove(KEY_POSITION)
        }
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/di/DatabaseModule.kt`

```kotlin
package com.fr33music.core.database.di

import android.content.Context
import androidx.room.Room
import com.fr33music.core.database.AppDatabase
import com.fr33music.core.database.OfflineAudioSourceImpl
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadManager
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.database.repository.LibraryRepositoryImpl
import com.fr33music.core.domain.model.OfflineAudioSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fr33music.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao = database.trackDao()

    @Provides
    @Singleton
    fun provideArtistDao(database: AppDatabase): ArtistDao = database.artistDao()

    @Provides
    @Singleton
    fun provideAlbumDao(database: AppDatabase): AlbumDao = database.albumDao()

    @Provides
    @Singleton
    fun provideLyricsDao(database: AppDatabase): LyricsDao = database.lyricsDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): com.fr33music.core.database.dao.PlaylistDao = database.playlistDao()

    @Provides
    @Singleton
    fun provideDownloadRepository(impl: DownloadManager): DownloadRepository = impl

    @Provides
    @Singleton
    fun provideLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository = impl

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideOfflineAudioSource(impl: OfflineAudioSourceImpl): OfflineAudioSource = impl
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/download/DownloadManager.kt`

```kotlin
package com.fr33music.core.database.download

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao
) : DownloadRepository {

    private val workManager = WorkManager.getInstance(context)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "Descarga"
        private const val MIN_FILE_SIZE_BYTES = 100 * 1024L
    }

    init {
        // Sincronizar archivos existentes en filesDir/downloads al iniciar
        managerScope.launch {
            syncDownloadedFiles()
        }
    }

    override fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    override fun getDownload(trackId: String): Flow<DownloadEntity?> = downloadDao.getDownload(trackId)

    override fun getDownloadState(trackId: String): Flow<DownloadState> {
        return downloadDao.getDownload(trackId)
            .map { entity ->
                // Verificar si el archivo ya existe en almacenamiento local y es válido (>= 50KB)
                val localFile = findLocalDownloadedFile(trackId)
                if (localFile != null && localFile.length() >= MIN_FILE_SIZE_BYTES) {
                    if (entity?.status != DownloadStatus.COMPLETED) {
                        managerScope.launch {
                            downloadDao.updateStatus(
                                trackId = trackId,
                                status = DownloadStatus.COMPLETED,
                                localPath = localFile.absolutePath,
                                progress = 100
                            )
                            trackDao.setDownloaded(
                                id = trackId,
                                isDownloaded = true,
                                localPath = localFile.absolutePath,
                                format = if (localFile.name.endsWith(".opus", true)) "OPUS" else "M4A"
                            )
                        }
                    }
                    return@map DownloadState.Completed(localFile.absolutePath)
                }

                when (entity?.status) {
                    DownloadStatus.DOWNLOADING -> DownloadState.Downloading(entity.progress)
                    DownloadStatus.QUEUED -> DownloadState.Queued
                    DownloadStatus.COMPLETED -> {
                        if (!entity.localPath.isNullOrBlank() && File(entity.localPath).exists() && File(entity.localPath).length() >= MIN_FILE_SIZE_BYTES) {
                            DownloadState.Completed(entity.localPath)
                        } else {
                            DownloadState.Idle
                        }
                    }
                    DownloadStatus.ERROR -> DownloadState.Error(entity.errorMessage ?: "Error al descargar")
                    else -> DownloadState.Idle
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun syncDownloadedFiles() {
        try {
            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) return

            val files = downloadsDir.listFiles() ?: return
            for (file in files) {
                if (file.isFile) {
                    if (file.name.endsWith(".tmp", ignoreCase = true)) {
                        file.delete()
                        continue
                    }
                    // Miniaturas e imágenes locales: conservar siempre intactas
                    if (file.name.endsWith(".jpg", ignoreCase = true) ||
                        file.name.endsWith(".jpeg", ignoreCase = true) ||
                        file.name.endsWith(".png", ignoreCase = true)
                    ) {
                        continue
                    }

                    // Solo procesar archivos de audio válidos (.opus o .m4a)
                    val isOpus = file.name.endsWith(".opus", ignoreCase = true)
                    val isM4a = file.name.endsWith(".m4a", ignoreCase = true)
                    if (!isOpus && !isM4a) {
                        continue
                    }

                    if (file.length() >= MIN_FILE_SIZE_BYTES) {
                        val trackId = file.nameWithoutExtension
                        val format = if (isOpus) "OPUS" else "M4A"

                        trackDao.setDownloaded(
                            id = trackId,
                            isDownloaded = true,
                            localPath = file.absolutePath,
                            format = format
                        )
                        downloadDao.updateStatus(
                            trackId = trackId,
                            status = DownloadStatus.COMPLETED,
                            localPath = file.absolutePath,
                            progress = 100
                        )
                        Log.d(TAG, "Sincronizado archivo de audio existente: ${file.name} (${file.length()} bytes)")
                    } else {
                        // Limpiar archivos de audio incompletos
                        Log.w(TAG, "Eliminando archivo de audio incompleto: ${file.name} (${file.length()} bytes)")
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando archivos de descarga: ${e.message}")
        }
    }

    override suspend fun download(track: Track) {
        // 1. Verificar si la pista ya está registrada como COMPLETED en Room y existe en disco
        val existingDownload = downloadDao.getDownloadSync(track.trackId)
        val localFile = findLocalDownloadedFile(track.trackId)
        val fileValid = localFile != null && localFile.length() >= MIN_FILE_SIZE_BYTES

        if (existingDownload?.status == DownloadStatus.COMPLETED && fileValid) {
            Log.d(TAG, "La canción ${track.trackId} ya está descargada y completada. Omitiendo encolar en WorkManager.")
            return
        }

        if (fileValid) {
            Log.d(TAG, "Archivo ya existe en disco para ${track.trackId}: ${localFile!!.absolutePath}")
            downloadDao.updateStatus(
                trackId = track.trackId,
                status = DownloadStatus.COMPLETED,
                localPath = localFile.absolutePath,
                progress = 100
            )
            trackDao.setDownloaded(
                id = track.trackId,
                isDownloaded = true,
                localPath = localFile.absolutePath,
                format = if (localFile.name.endsWith(".opus", true)) "OPUS" else "M4A"
            )
            return
        }

        // 2. Registrar o actualizar metadatos en TrackDao
        val existingTrack = trackDao.getTrackSync(track.trackId)
        if (existingTrack == null) {
            trackDao.insertTrack(
                TrackEntity(
                    id = track.trackId,
                    title = track.title,
                    artistName = track.artistName,
                    albumName = track.albumName,
                    durationMs = track.durationMs,
                    thumbnailUrl = track.thumbnailUrl,
                    isDownloaded = false,
                    isSaved = false
                )
            )
        } else if (existingTrack.thumbnailUrl.isNullOrBlank() && !track.thumbnailUrl.isNullOrBlank()) {
            trackDao.updateTrack(existingTrack.copy(thumbnailUrl = track.thumbnailUrl))
        }

        // Upsert en ArtistEntity y AlbumEntity si tienen thumbnail
        val artist = track.artistName
        val thumb = track.thumbnailUrl
        val album = track.albumName

        if (artist.isNotBlank() && !thumb.isNullOrBlank()) {
            val existingArtist = artistDao.getArtistSync(artist)
            if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank()) {
                artistDao.insertArtist(
                    ArtistEntity(
                        name = artist,
                        thumbnailUrl = thumb
                    )
                )
            }
        }
        if (!album.isNullOrBlank() && !thumb.isNullOrBlank()) {
            val albumId = "${artist}_$album"
            val existingAlbum = albumDao.getAlbumSync(albumId)
            if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank()) {
                albumDao.insertAlbum(
                    AlbumEntity(
                        id = albumId,
                        title = album,
                        artistName = artist,
                        thumbnailUrl = thumb
                    )
                )
            }
        }

        // Registrar en DownloadDao con estado inicial QUEUED
        downloadDao.insertDownload(
            DownloadEntity(
                trackId = track.trackId,
                title = track.title,
                artistName = track.artistName,
                thumbnailUrl = track.thumbnailUrl,
                status = DownloadStatus.QUEUED,
                progress = 0
            )
        )
        Log.d(TAG, "Estado inicial: ${DownloadStatus.QUEUED} para ${track.trackId}")

        // Encolar WorkManager con política de reintentos exponenciales y ExistingWorkPolicy.KEEP
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            DownloadWorker.KEY_TRACK_ID to track.trackId,
            DownloadWorker.KEY_TITLE to track.title,
            DownloadWorker.KEY_ARTIST to track.artistName,
            DownloadWorker.KEY_DURATION to track.durationMs,
            DownloadWorker.KEY_THUMBNAIL to track.thumbnailUrl,
            DownloadWorker.KEY_ALBUM to track.albumName
        )

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                5,
                TimeUnit.SECONDS
            )
            .addTag("download_${track.trackId}")
            .build()

        // Usar ExistingWorkPolicy.KEEP para no reiniciar descargas activas o completadas
        workManager.enqueueUniqueWork(
            "download_${track.trackId}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override suspend fun deleteDownload(trackId: String) {
        workManager.cancelUniqueWork("download_$trackId")

        val download = downloadDao.getDownloadSync(trackId)
        val path = download?.localPath ?: trackDao.getTrackSync(trackId)?.localPath
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }

        val downloadsDir = File(context.filesDir, "downloads")
        File(downloadsDir, "$trackId.opus").takeIf { it.exists() }?.delete()
        File(downloadsDir, "$trackId.m4a").takeIf { it.exists() }?.delete()

        downloadDao.deleteDownload(trackId)
        val track = trackDao.getTrackSync(trackId)
        if (track != null) {
            if (track.isSaved) {
                trackDao.setDownloaded(trackId, isDownloaded = false, localPath = null, format = null)
            } else {
                trackDao.deleteTrack(trackId)
            }
        }
    }

    private fun findLocalDownloadedFile(trackId: String): File? {
        val downloadsDir = File(context.filesDir, "downloads")
        val opus = File(downloadsDir, "$trackId.opus")
        if (opus.exists() && opus.length() >= MIN_FILE_SIZE_BYTES) return opus

        val m4a = File(downloadsDir, "$trackId.m4a")
        if (m4a.exists() && m4a.length() >= MIN_FILE_SIZE_BYTES) return m4a

        return null
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/download/DownloadRepository.kt`

```kotlin
package com.fr33music.core.database.download

import com.fr33music.core.database.entity.DownloadEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.Flow

sealed interface DownloadState {
    data object Idle : DownloadState
    data object Queued : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data class Completed(val localPath: String) : DownloadState
    data class Error(val message: String) : DownloadState
}

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    fun getDownload(trackId: String): Flow<DownloadEntity?>
    fun getDownloadState(trackId: String): Flow<DownloadState>
    suspend fun syncDownloadedFiles()
    suspend fun download(track: Track)
    suspend fun deleteDownload(trackId: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/download/DownloadWorker.kt`

```kotlin
package com.fr33music.core.database.download

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.AudioFormat
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.StreamResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DownloadWorkerEntryPoint {
    fun streamResolver(): StreamResolver
    fun trackDao(): TrackDao
    fun artistDao(): ArtistDao
    fun albumDao(): AlbumDao
    fun downloadDao(): DownloadDao
    fun okHttpClient(): OkHttpClient
}

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "DownloadWorker"
        const val KEY_TRACK_ID = "track_id"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_DURATION = "duration"
        const val KEY_THUMBNAIL = "thumbnail"
        const val KEY_ALBUM = "album"
        const val MIN_FILE_SIZE_BYTES = 100 * 1024L // 100 KB mínimo para evitar archivos corruptos/incompletos
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: run {
            Log.e(TAG, "No se proporcionó KEY_TRACK_ID en inputData")
            return@withContext Result.failure()
        }
        val title = inputData.getString(KEY_TITLE) ?: "Desconocido"
        val artist = inputData.getString(KEY_ARTIST) ?: "Desconocido"
        val duration = inputData.getLong(KEY_DURATION, 0L)
        val thumbnail = inputData.getString(KEY_THUMBNAIL)
        val album = inputData.getString(KEY_ALBUM)

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            DownloadWorkerEntryPoint::class.java
        )
        val streamResolver = entryPoint.streamResolver()
        val trackDao = entryPoint.trackDao()
        val artistDao = entryPoint.artistDao()
        val albumDao = entryPoint.albumDao()
        val downloadDao = entryPoint.downloadDao()
        val okHttpClient = entryPoint.okHttpClient()

        val downloadsDir = File(applicationContext.filesDir, "downloads")
        val opusFile = File(downloadsDir, "$trackId.opus")
        val m4aFile = File(downloadsDir, "$trackId.m4a")
        val validLocalFile = when {
            opusFile.exists() && opusFile.length() >= MIN_FILE_SIZE_BYTES -> opusFile
            m4aFile.exists() && m4aFile.length() >= MIN_FILE_SIZE_BYTES -> m4aFile
            else -> null
        }
        val existingDownload = downloadDao.getDownloadSync(trackId)
        if (existingDownload?.status == DownloadStatus.COMPLETED && validLocalFile != null) {
            Log.i(TAG, "DownloadWorker: La pista $trackId ya está descargada y completa (${validLocalFile.length()} bytes). Omitiendo.")
            return@withContext Result.success(workDataOf("localPath" to validLocalFile.absolutePath))
        }

        Log.i(TAG, "Iniciando descarga (intento $runAttemptCount/3): $trackId - $title ($artist)")

        var tempFile: File? = null
        var outputFile: File? = null

        try {
            downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = 10)

            val track = Track(
                trackId = trackId,
                title = title,
                artistName = artist,
                durationMs = duration,
                thumbnailUrl = thumbnail,
                albumName = album
            )

            // Descargar carátula localmente en almacenamiento privado para soporte 100% offline
            var localThumbnail = thumbnail
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val coverFile = File(downloadsDir, "$trackId.jpg")

            if (!coverFile.exists() || coverFile.length() == 0L) {
                if (!thumbnail.isNullOrBlank()) {
                    try {
                        val hdUrl = Track.upgradeThumbnail(thumbnail) ?: thumbnail
                        Log.d(TAG, "Descargando miniatura HD offline para $trackId desde $hdUrl")
                        var coverReq = Request.Builder().url(hdUrl).build()
                        var coverRes = okHttpClient.newCall(coverReq).execute()

                        // Fallback a SD si maxresdefault devuelve 404 u otro error
                        if (!coverRes.isSuccessful && hdUrl.contains("/maxresdefault.jpg")) {
                            val sdUrl = hdUrl.replace("/maxresdefault.jpg", "/sddefault.jpg")
                            Log.d(TAG, "maxresdefault falló (${coverRes.code}), reintentando con SD: $sdUrl")
                            coverReq = Request.Builder().url(sdUrl).build()
                            coverRes = okHttpClient.newCall(coverReq).execute()
                        }

                        // Fallback a HQ si SD también falló
                        if (!coverRes.isSuccessful && hdUrl.contains("/maxresdefault.jpg")) {
                            val hqUrl = hdUrl.replace("/maxresdefault.jpg", "/hqdefault.jpg")
                            Log.d(TAG, "sddefault falló (${coverRes.code}), reintentando con HQ: $hqUrl")
                            coverReq = Request.Builder().url(hqUrl).build()
                            coverRes = okHttpClient.newCall(coverReq).execute()
                        }

                        if (coverRes.isSuccessful && coverRes.body != null) {
                            FileOutputStream(coverFile).use { out ->
                                val imgBuffer = ByteArray(16 * 1024)
                                coverRes.body!!.byteStream().use { input ->
                                    var bytes: Int
                                    while (input.read(imgBuffer).also { bytes = it } != -1) {
                                        out.write(imgBuffer, 0, bytes)
                                    }
                                }
                                out.flush()
                            }
                            Log.d(TAG, "Miniatura offline guardada para $trackId: ${coverFile.length()} bytes")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "No se pudo descargar carátula local para $trackId: ${e.message}")
                    }
                }
            } else {
                Log.d(TAG, "Miniatura ya existe localmente para $trackId: ${coverFile.length()} bytes")
            }

            if (coverFile.exists() && coverFile.length() > 0) {
                localThumbnail = Uri.fromFile(coverFile).toString()
            }

            val streamResult = streamResolver.resolveAudio(track)
            val stream = streamResult.getOrElse { throwable ->
                val errorMsg = throwable.message ?: "No se pudo obtener el stream de audio"
                Log.e(TAG, "Error al resolver stream para $trackId en intento $runAttemptCount: $errorMsg", throwable)
                if (runAttemptCount < 3) {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0,
                        error = "Reintentando resolver stream (${runAttemptCount + 1}/3)..."
                    )
                    return@withContext Result.retry()
                } else {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.ERROR,
                        error = errorMsg
                    )
                    return@withContext Result.failure()
                }
            }

            val isOpus = stream.format == AudioFormat.OPUS
            val extension = if (isOpus) "opus" else "m4a"

            if (!downloadsDir.exists()) {
                val created = downloadsDir.mkdirs()
                if (!created && !downloadsDir.exists()) {
                    val errorMsg = "No se pudo crear el directorio de descargas"
                    Log.e(TAG, errorMsg)
                    downloadDao.updateStatus(trackId, DownloadStatus.ERROR, error = errorMsg)
                    return@withContext Result.failure()
                }
            }

            // Usar archivo temporal (.tmp) durante la descarga para que nunca quede un archivo corrupto accesible
            tempFile = File(downloadsDir, "$trackId.tmp")
            outputFile = File(downloadsDir, "$trackId.$extension")

            var totalBytes = -1L
            var downloadedBytes = 0L
            var lastReportedProgress = 5

            // Usar descarga por bloques Range de 1 MB para evitar que YouTube limite la velocidad (throttling)
            val chunkSize = 1024 * 1024L // 1 MB por bloque
            val firstChunkReq = Request.Builder().url(stream.url)
            stream.headers.forEach { (k, v) -> if (!k.equals("Range", ignoreCase = true)) firstChunkReq.header(k, v) }
            firstChunkReq.header("Range", "bytes=0-${chunkSize - 1}")

            Log.d(TAG, "Iniciando descarga acelerada por chunks para $trackId con formato ${stream.format}")
            val firstResponse = okHttpClient.newCall(firstChunkReq.build()).execute()

            if (!firstResponse.isSuccessful || firstResponse.body == null) {
                val errorMsg = "HTTP error ${firstResponse.code}: ${firstResponse.message}"
                Log.e(TAG, "Error HTTP al descargar $trackId: $errorMsg")
                tempFile.delete()

                if (runAttemptCount < 3) {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.DOWNLOADING,
                        progress = 0,
                        error = "Reintentando por fallo HTTP (${runAttemptCount + 1}/3)..."
                    )
                    return@withContext Result.retry()
                } else {
                    downloadDao.updateStatus(
                        trackId = trackId,
                        status = DownloadStatus.ERROR,
                        error = errorMsg
                    )
                    return@withContext Result.failure()
                }
            }

            // Verificar Content-Type: rechazar páginas de error HTML de YouTube
            val contentType = firstResponse.header("Content-Type")?.lowercase() ?: ""
            if (contentType.contains("text/html") || contentType.contains("text/plain")) {
                tempFile.delete()
                val errorMsg = "YouTube devolvió página HTML en lugar de audio ($contentType)"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            if (firstResponse.code == 206) {
                // Servidor soporta Range. Extraer tamaño total de Content-Range (ej. bytes 0-1048575/4892011)
                val contentRange = firstResponse.header("Content-Range") ?: ""
                totalBytes = contentRange.substringAfterLast("/", "-1").toLongOrNull() ?: -1L

                FileOutputStream(tempFile).use { fileOut ->
                    firstResponse.body!!.byteStream().use { input ->
                        val buf = ByteArray(32 * 1024)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } != -1) {
                            fileOut.write(buf, 0, bytesRead)
                            downloadedBytes += bytesRead
                        }
                    }
                    fileOut.flush()

                    if (totalBytes > 0) {
                        val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                        downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                    }

                    // Descargar chunks sucesivos de 1MB a toda velocidad
                    var currentStart = downloadedBytes
                    while (totalBytes > 0 && currentStart < totalBytes) {
                        val currentEnd = minOf(currentStart + chunkSize - 1, totalBytes - 1)
                        val chunkReq = Request.Builder().url(stream.url)
                        stream.headers.forEach { (k, v) -> if (!k.equals("Range", ignoreCase = true)) chunkReq.header(k, v) }
                        chunkReq.header("Range", "bytes=$currentStart-$currentEnd")

                        val chunkRes = okHttpClient.newCall(chunkReq.build()).execute()
                        if (!chunkRes.isSuccessful || chunkRes.body == null) {
                            throw IOException("Error en chunk $currentStart-$currentEnd: HTTP ${chunkRes.code}")
                        }

                        chunkRes.body!!.byteStream().use { input ->
                            val buf = ByteArray(32 * 1024)
                            var bytesRead: Int
                            while (input.read(buf).also { bytesRead = it } != -1) {
                                fileOut.write(buf, 0, bytesRead)
                                downloadedBytes += bytesRead
                            }
                        }
                        fileOut.flush()

                        val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                        if (currentProgress >= lastReportedProgress + 10) {
                            lastReportedProgress = currentProgress
                            downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                        }

                        currentStart = downloadedBytes
                    }

                    fileOut.flush()
                    fileOut.fd.sync()
                }
            } else {
                // Si respondió 200 OK (sin Range), leer stream completo continuo con buffer amplio
                val body = firstResponse.body!!
                totalBytes = body.contentLength()

                FileOutputStream(tempFile).use { fileOut ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            fileOut.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalBytes > 0) {
                                val currentProgress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(5, 95)
                                if (currentProgress >= lastReportedProgress + 10) {
                                    lastReportedProgress = currentProgress
                                    downloadDao.updateStatus(trackId, DownloadStatus.DOWNLOADING, progress = currentProgress)
                                }
                            }
                        }
                        fileOut.flush()
                        fileOut.fd.sync()
                    }
                }
            }

            // Verificar que no se haya cortado la conexión antes de tiempo
            if (totalBytes > 0L && downloadedBytes < totalBytes) {
                tempFile.delete()
                val errorMsg = "Descarga incompleta: recibidos $downloadedBytes de $totalBytes bytes"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            // Verificar tamaño mínimo (>= 100 KB) para asegurar que no sea un stub corrupto
            if (!tempFile.exists() || tempFile.length() < MIN_FILE_SIZE_BYTES) {
                val size = tempFile.length()
                tempFile.delete()
                val errorMsg = "Archivo descargado demasiado pequeño ($size bytes < $MIN_FILE_SIZE_BYTES)"
                Log.e(TAG, errorMsg)
                throw IOException(errorMsg)
            }

            // Mover el archivo temporal al destino final (.opus / .m4a) de forma atómica
            if (outputFile.exists()) {
                outputFile.delete()
            }
            val renamed = tempFile.renameTo(outputFile)
            if (!renamed) {
                // Fallback si renameTo falla entre montajes
                tempFile.copyTo(outputFile, overwrite = true)
                tempFile.delete()
            }

            Log.i(TAG, "Descarga completada y verificada: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
            downloadDao.updateStatus(
                trackId = trackId,
                status = DownloadStatus.COMPLETED,
                localPath = outputFile.absolutePath,
                progress = 100
            )

            val isRealAlbum = !album.isNullOrBlank() &&
                    !album.equals(artist, ignoreCase = true) &&
                    !album.equals("Sencillos", ignoreCase = true) &&
                    !album.equals("YouTube", ignoreCase = true)
            val effectiveAlbum: String = if (isRealAlbum) album!! else "Sencillos"
            val finalThumb = localThumbnail ?: thumbnail
            val existing = trackDao.getTrackSync(trackId)
            if (existing != null) {
                trackDao.setDownloaded(
                    id = trackId,
                    isDownloaded = true,
                    localPath = outputFile.absolutePath,
                    format = stream.format.name
                )
                val updatedThumb = if (!finalThumb.isNullOrBlank()) finalThumb else existing.thumbnailUrl
                val updatedAlbum = if (!existing.albumName.isNullOrBlank() && !existing.albumName.equals(artist, ignoreCase = true)) existing.albumName else effectiveAlbum
                trackDao.updateTrack(existing.copy(thumbnailUrl = updatedThumb, albumName = updatedAlbum, isDownloaded = true, localPath = outputFile.absolutePath))
            } else {
                trackDao.insertTrack(
                    TrackEntity(
                        id = trackId,
                        title = title,
                        artistName = artist,
                        albumName = effectiveAlbum,
                        durationMs = duration,
                        thumbnailUrl = finalThumb,
                        format = stream.format.name,
                        isDownloaded = true,
                        isSaved = false,
                        localPath = outputFile.absolutePath
                    )
                )
            }

            // Asegurar que ArtistEntity y AlbumEntity queden guardados con su thumbnail (priorizando file:// si existe)
            if (artist.isNotBlank() && !finalThumb.isNullOrBlank()) {
                val existingArtist = artistDao.getArtistSync(artist)
                if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank() || finalThumb.startsWith("file:")) {
                    artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = finalThumb))
                }
            }
            if (!finalThumb.isNullOrBlank()) {
                val albumId = "${artist}_$effectiveAlbum"
                val existingAlbum = albumDao.getAlbumSync(albumId)
                if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank() || finalThumb.startsWith("file:")) {
                    albumDao.insertAlbum(AlbumEntity(id = albumId, title = effectiveAlbum, artistName = artist, thumbnailUrl = finalThumb))
                }
            }

            Result.success(workDataOf("localPath" to outputFile.absolutePath))
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante la descarga de $trackId (intento $runAttemptCount): ${e.message}", e)
            try {
                tempFile?.takeIf { it.exists() }?.delete()
                outputFile?.takeIf { it.exists() && it.length() < MIN_FILE_SIZE_BYTES }?.delete()
            } catch (_: Exception) {}

            if (runAttemptCount < 3) {
                downloadDao.updateStatus(
                    trackId = trackId,
                    status = DownloadStatus.DOWNLOADING,
                    progress = 0,
                    error = "Reintentando descarga (${runAttemptCount + 1}/3): ${e.message}"
                )
                Result.retry()
            } else {
                downloadDao.updateStatus(
                    trackId = trackId,
                    status = DownloadStatus.ERROR,
                    error = e.message ?: "Fallo al descargar tras varios intentos"
                )
                Result.failure()
            }
        }
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/AlbumEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistName: String,
    val thumbnailUrl: String? = null,
    val trackCount: Int = 1
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/ArtistEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val name: String,
    val trackCount: Int = 1,
    val thumbnailUrl: String? = null
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/DownloadEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

object DownloadStatus {
    const val QUEUED = "QUEUED"
    const val DOWNLOADING = "DOWNLOADING"
    const val COMPLETED = "COMPLETED"
    const val ERROR = "ERROR"
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val trackId: String,
    val title: String,
    val artistName: String,
    val thumbnailUrl: String? = null,
    val status: String = DownloadStatus.QUEUED,
    val localPath: String? = null,
    val progress: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/LyricsEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey
    val trackId: String,
    val plainText: String,
    val source: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/PlaylistEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/PlaylistSongCrossRef.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "trackId"],
    indices = [
        Index("playlistId"),
        Index("trackId")
    ]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val trackId: String,
    val addedAt: Long = System.currentTimeMillis()
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/SearchHistoryEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### `core/database/src/main/java/com/fr33music/core/database/entity/TrackEntity.kt`

```kotlin
package com.fr33music.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fr33music.core.domain.model.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artistName: String,
    val albumName: String? = null,
    val durationMs: Long,
    val thumbnailUrl: String? = null,
    val format: String? = null,
    val isDownloaded: Boolean = false,
    val isSaved: Boolean = false,
    val localPath: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Track = Track(
        trackId = id,
        title = title,
        artistName = artistName,
        albumName = albumName,
        durationMs = durationMs,
        thumbnailUrl = thumbnailUrl
    )

    companion object {
        fun fromDomain(
            track: Track,
            isSaved: Boolean = false,
            isDownloaded: Boolean = false,
            localPath: String? = null,
            format: String? = null
        ): TrackEntity {
            val isRealAlbum = !track.albumName.isNullOrBlank() &&
                    !track.albumName.equals(track.artistName, ignoreCase = true) &&
                    !track.albumName.equals("Sencillos", ignoreCase = true) &&
                    !track.albumName.equals("YouTube", ignoreCase = true)
            val effectiveAlbum = if (isRealAlbum) track.albumName!! else "Sencillos"
            return TrackEntity(
                id = track.trackId,
                title = track.title,
                artistName = track.artistName,
                albumName = effectiveAlbum,
                durationMs = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                format = format,
                isDownloaded = isDownloaded,
                isSaved = isSaved,
                localPath = localPath
            )
        }
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/lyrics/LyricsRepository.kt`

```kotlin
package com.fr33music.core.database.lyrics

import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.entity.LyricsEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val lyricsDao: LyricsDao,
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLyrics(track: Track): String = withContext(Dispatchers.IO) {
        // 1. Revisar caché local en Room
        val cached = lyricsDao.getLyricsSync(track.trackId)
        if (cached != null && cached.plainText.isNotBlank()) {
            return@withContext cached.plainText
        }

        val cleanTitle = cleanTrackTitle(track.title)
        val cleanArtist = cleanArtistName(track.artistName)

        // 2. Intentar con LRCLIB
        val lrclibLyrics = fetchFromLrclib(cleanArtist, cleanTitle)
        if (!lrclibLyrics.isNullOrBlank()) {
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = lrclibLyrics,
                    source = "LRCLIB"
                )
            )
            return@withContext lrclibLyrics
        }

        // 3. Fallback a lyrics.ovh
        val ovhLyrics = fetchFromLyricsOvh(cleanArtist, cleanTitle)
        if (!ovhLyrics.isNullOrBlank()) {
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = ovhLyrics,
                    source = "lyrics.ovh"
                )
            )
            return@withContext ovhLyrics
        }

        "Letra no disponible"
    }

    private fun fetchFromLrclib(artist: String, title: String): String? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject
            val plainLyrics = jsonElement["plainLyrics"]?.jsonPrimitive?.content
            if (!plainLyrics.isNullOrBlank()) {
                return plainLyrics
            }

            // Si solo hay syncedLyrics, quitar las marcas de tiempo para obtener texto plano
            val syncedLyrics = jsonElement["syncedLyrics"]?.jsonPrimitive?.content
            if (!syncedLyrics.isNullOrBlank()) {
                return syncedLyrics.replace(Regex("\\[\\d{2}:\\d{2}\\.\\d{2,3}\\]\\s*"), "").trim()
            }

            null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromLyricsOvh(artist: String, title: String): String? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val url = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject
            jsonElement["lyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanTrackTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\b(official\\s*video|official\\s*audio|official\\s*music\\s*video|video\\s*oficial|audio\\s*oficial|lyric\\s*video|letra)\\b"), "")
            .replace(Regex("(?i)\\[.*?\\]|\\(.*?\\)"), "")
            .replace(Regex("(?i)\\b(ft\\.|feat\\.|featuring).*"), "")
            .trim()
    }

    private fun cleanArtistName(artist: String): String {
        return artist
            .replace(Regex("(?i)\\s*-\\s*topic$"), "")
            .replace(Regex("(?i)\\b(vevo)\\b"), "")
            .trim()
    }
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/repository/LibraryRepository.kt`

```kotlin
package com.fr33music.core.database.repository

import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    suspend fun toggleSave(track: Track, isCurrentlySaved: Boolean)
    suspend fun save(track: Track)
    suspend fun deleteDownload(trackId: String)
    suspend fun remove(trackId: String)

    // Playlists
    fun getPlaylists(): Flow<List<PlaylistWithSongCount>>
    fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(id: Long)
    suspend fun addSongToPlaylist(playlistId: Long, track: Track)
    suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String)
}
```

---

### `core/database/src/main/java/com/fr33music/core/database/repository/LibraryRepositoryImpl.kt`

```kotlin
package com.fr33music.core.database.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.DownloadDao
import com.fr33music.core.database.dao.PlaylistDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val downloadDao: DownloadDao,
    private val playlistDao: PlaylistDao
) : LibraryRepository {

    companion object {
        private const val TAG = "LibraryRepository"
    }

    override suspend fun toggleSave(track: Track, isCurrentlySaved: Boolean) {
        withContext(Dispatchers.IO) {
            if (isCurrentlySaved) {
                Log.d(TAG, "Quitando ${track.trackId} de favoritos")
                val existing = trackDao.getTrackSync(track.trackId)
                if (existing != null) {
                    if (existing.isDownloaded) {
                        trackDao.setSaved(track.trackId, false)
                    } else {
                        trackDao.deleteTrack(track.trackId)
                    }
                }
            } else {
                Log.d(TAG, "Guardando ${track.trackId} en favoritos")
                save(track)
            }
        }
    }

    override suspend fun save(track: Track) {
        withContext(Dispatchers.IO) {
            val existing = trackDao.getTrackSync(track.trackId)
            if (existing != null) {
                trackDao.setSaved(track.trackId, true)
                if (existing.thumbnailUrl.isNullOrBlank() && !track.thumbnailUrl.isNullOrBlank()) {
                    trackDao.updateTrack(existing.copy(thumbnailUrl = track.thumbnailUrl, isSaved = true))
                }
            } else {
                trackDao.insertTrack(
                    TrackEntity.fromDomain(track, isSaved = true)
                )
            }

            val artist = track.artistName
            val isRealAlbum = !track.albumName.isNullOrBlank() &&
                    !track.albumName.equals(artist, ignoreCase = true) &&
                    !track.albumName.equals("Sencillos", ignoreCase = true) &&
                    !track.albumName.equals("YouTube", ignoreCase = true)
            val effectiveAlbum: String = if (isRealAlbum) track.albumName!! else "Sencillos"

            val localCoverFile = File(context.filesDir, "downloads/${track.trackId}.jpg")
            val thumb = if (localCoverFile.exists() && localCoverFile.length() > 0) {
                android.net.Uri.fromFile(localCoverFile).toString()
            } else {
                track.thumbnailUrl
            }

            if (artist.isNotBlank()) {
                val existingArtist = artistDao.getArtistSync(artist)
                val officialAvatar = track.artistThumbnailUrl
                if (!officialAvatar.isNullOrBlank()) {
                    artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = officialAvatar))
                } else if (existingArtist == null || existingArtist.thumbnailUrl.isNullOrBlank()) {
                    if (!thumb.isNullOrBlank()) {
                        artistDao.insertArtist(ArtistEntity(name = artist, thumbnailUrl = thumb))
                    }
                }
            }
            if (!thumb.isNullOrBlank()) {
                val albumId = "${artist}_$effectiveAlbum"
                val existingAlbum = albumDao.getAlbumSync(albumId)
                if (existingAlbum == null || existingAlbum.thumbnailUrl.isNullOrBlank()) {
                    albumDao.insertAlbum(AlbumEntity(id = albumId, title = effectiveAlbum, artistName = artist, thumbnailUrl = thumb))
                }
            }
        }
    }

    override suspend fun deleteDownload(trackId: String) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Eliminando descarga física para trackId: $trackId")

            try {
                WorkManager.getInstance(context).cancelUniqueWork("download_$trackId")
            } catch (e: Exception) {
                Log.w(TAG, "Error cancelando WorkManager: ${e.message}")
            }

            try {
                val downloadsDir = File(context.filesDir, "downloads")
                val opusFile = File(downloadsDir, "$trackId.opus")
                if (opusFile.exists()) {
                    opusFile.delete()
                }

                val m4aFile = File(downloadsDir, "$trackId.m4a")
                if (m4aFile.exists()) {
                    m4aFile.delete()
                }

                File(downloadsDir, "$trackId.tmp").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.jpg").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.png").takeIf { it.exists() }?.delete()
                File(downloadsDir, "$trackId.webp").takeIf { it.exists() }?.delete()

                val download = downloadDao.getDownloadSync(trackId)
                download?.localPath?.let { File(it).takeIf { f -> f.exists() }?.delete() }

                val track = trackDao.getTrackSync(trackId)
                track?.localPath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
            } catch (e: Exception) {
                Log.e(TAG, "Error al borrar archivo físico para $trackId: ${e.message}")
            }

            downloadDao.deleteDownload(trackId)

            val track = trackDao.getTrackSync(trackId)
            if (track != null) {
                if (track.isSaved) {
                    trackDao.setDownloaded(trackId, isDownloaded = false, localPath = null, format = null)
                } else {
                    trackDao.deleteTrack(trackId)
                }
            }
        }
    }

    override suspend fun remove(trackId: String) {
        deleteDownload(trackId)
        withContext(Dispatchers.IO) {
            trackDao.deleteTrack(trackId)
            Log.i(TAG, "Canción $trackId eliminada por completo.")
        }
    }

    // Playlists
    override fun getPlaylists(): Flow<List<PlaylistWithSongCount>> = playlistDao.getAllPlaylists()

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<TrackEntity>> =
        playlistDao.getSongsForPlaylist(playlistId)

    override suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim().ifEmpty { "Mi Playlist" }
        playlistDao.insertPlaylist(PlaylistEntity(name = trimmed))
    }

    override suspend fun deletePlaylist(id: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistSongs(id)
        playlistDao.deletePlaylist(id)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        // Asegurar que la pista existe en Room para que el JOIN funcione
        val existing = trackDao.getTrackSync(track.trackId)
        if (existing == null) {
            trackDao.insertTrack(TrackEntity.fromDomain(track, isSaved = false))
        }

        playlistDao.insertSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, trackId = track.trackId))

        // Si la playlist no tiene miniatura, actualizar con la de esta canción
        val playlist = playlistDao.getPlaylistSync(playlistId)
        val trackThumb = track.thumbnailUrl
        if (playlist != null && playlist.thumbnailUrl.isNullOrBlank() && !trackThumb.isNullOrBlank()) {
            playlistDao.updatePlaylistThumbnail(playlistId, trackThumb)
        }
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, trackId)
    }
}
```

---

### `core/designsystem/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.fr33music.core.designsystem"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.coil.compose)
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/ErrorView.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = ColorError,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = message,
            style = Fr33Typography.bodyLarge,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.extraLarge))

        PrimaryButton(
            text = "Reintentar",
            onClick = onRetry
        )
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/IconButtonM3.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fr33music.core.designsystem.theme.ColorTextPrimary

@Composable
fun IconButtonM3(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = ColorTextPrimary,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/LoadingSkeleton.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing

@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 8
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        Color(0xFF1E1E1E),
        Color(0xFF2D2D2D),
        Color(0xFF1E1E1E)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        repeat(itemCount) {
            SkeletonTrackRow(brush = brush)
        }
    }
}

@Composable
private fun SkeletonTrackRow(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(Fr33Radius.small))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/PrimaryButton.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.Fr33Radius

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(Fr33Radius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimary,
            contentColor = ColorTextPrimary
        )
    ) {
        Text(text = text)
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/TrackRow.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.Track

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = track.thumbnailUrl,
            trackId = track.trackId,
            contentDescription = track.title,
            size = 56.dp
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.title,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = track.artistName,
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(Fr33Spacing.small))
            trailingContent()
        }
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/component/TrackThumbnail.kt`

```kotlin
package com.fr33music.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.domain.model.Track
import java.io.File

import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun TrackThumbnail(
    thumbnailUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    trackId: String? = null,
    size: Dp = 56.dp,
    shapeRadius: Dp = Fr33Radius.small,
    isCircular: Boolean = false,
    fallbackIcon: ImageVector = Icons.Default.MusicNote
) {
    val context = LocalContext.current
    val shape = if (isCircular) CircleShape else RoundedCornerShape(shapeRadius)

    // Si la pista tiene imagen descargada localmente en filesDir/downloads/{trackId}.jpg, usarla prioritariamente
    val localFile: File? = remember(trackId) {
        if (!trackId.isNullOrBlank()) {
            val file = File(context.filesDir, "downloads/$trackId.jpg")
            if (file.exists() && file.length() > 0) file else null
        } else null
    }

    val hdUrl = remember(thumbnailUrl) {
        Track.upgradeThumbnail(thumbnailUrl)
    }

    val sdFallbackUrl = remember(hdUrl) {
        if (hdUrl?.contains("/maxresdefault.jpg") == true) {
            hdUrl.replace("/maxresdefault.jpg", "/sddefault.jpg")
        } else {
            null
        }
    }

    val hqFallbackUrl = remember(hdUrl) {
        if (hdUrl?.contains("/maxresdefault.jpg") == true) {
            hdUrl.replace("/maxresdefault.jpg", "/hqdefault.jpg")
        } else {
            null
        }
    }

    val imageModel: Any? = remember(hdUrl, localFile) {
        if (localFile != null && localFile.exists()) {
            localFile
        } else if (hdUrl?.startsWith("file:") == true) {
            hdUrl
        } else {
            hdUrl
        }
    }

    if (imageModel == null || (imageModel is String && imageModel.isBlank())) {
        Box(modifier = modifier.size(size)) {
            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
        }
        return
    }

    val request = remember(imageModel) {
        ImageRequest.Builder(context)
            .data(imageModel)
            .crossfade(true)
            .build()
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(ColorSurfaceVariant),
        loading = {
            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
        },
        error = {
            if (sdFallbackUrl != null) {
                // Reintento 1: calidad SD si maxresdefault devolvió 404
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(sdFallbackUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        if (hqFallbackUrl != null) {
                            // Reintento 2: calidad HQ si SD también falló
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(hqFallbackUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    if (localFile != null) {
                                        SubcomposeAsyncImage(
                                            model = localFile,
                                            contentDescription = contentDescription,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            error = {
                                                ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                            }
                                        )
                                    } else {
                                        ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                    }
                                }
                            )
                        } else if (localFile != null) {
                            SubcomposeAsyncImage(
                                model = localFile,
                                contentDescription = contentDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                                }
                            )
                        } else {
                            ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                        }
                    }
                )
            } else if (localFile != null) {
                SubcomposeAsyncImage(
                    model = localFile,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
                    }
                )
            } else {
                ElegantPlaceholder(size = size, isCircular = isCircular, fallbackIcon = fallbackIcon)
            }
        }
    )
}

@Composable
private fun ElegantPlaceholder(
    size: Dp,
    isCircular: Boolean = false,
    fallbackIcon: ImageVector = Icons.Default.MusicNote
) {
    val shape = if (isCircular) CircleShape else RoundedCornerShape(Fr33Radius.small)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF242426), Color(0xFF161618))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .clip(CircleShape)
                .background(Color(0xFF1F1F21)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = ColorPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(size * 0.35f)
            )
        }
    }
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/theme/Color.kt`

```kotlin
package com.fr33music.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val ColorBackground = Color(0xFF030303)
val ColorSurface = Color(0xFF212121)
val ColorSurfaceVariant = Color(0xFF2D2D2D)
val ColorPrimary = Color(0xFFFF0000)
val ColorPrimaryDark = Color(0xFFCC0000)
val ColorTextPrimary = Color(0xFFFFFFFF)
val ColorTextSecondary = Color(0xFFAAAAAA)
val ColorTextTertiary = Color(0xFF777777)
val ColorDivider = Color(0xFF1E1E1E)
val ColorError = Color(0xFFFF5252)
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/theme/Spacing.kt`

```kotlin
package com.fr33music.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Fr33Spacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp
    val huge: Dp = 32.dp
}

object Fr33Radius {
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/theme/Theme.kt`

```kotlin
package com.fr33music.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorTextPrimary,
    primaryContainer = ColorPrimaryDark,
    onPrimaryContainer = ColorTextPrimary,
    background = ColorBackground,
    onBackground = ColorTextPrimary,
    surface = ColorSurface,
    onSurface = ColorTextPrimary,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorTextSecondary,
    error = ColorError,
    onError = ColorTextPrimary
)

@Composable
fun Fr33MusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Fr33Typography,
        content = content
    )
}
```

---

### `core/designsystem/src/main/java/com/fr33music/core/designsystem/theme/Type.kt`

```kotlin
package com.fr33music.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Fr33Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = ColorTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = ColorTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = ColorTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = ColorTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = ColorTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = ColorTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = ColorTextTertiary
    )
)
```

---

### `core/designsystem/src/main/res/drawable/ic_music_notification.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="#FFFFFFFF"
      android:pathData="M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z"/>
</vector>
```

---

### `core/domain/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.fr33music.core.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/AlbumCard.kt`

```kotlin
package com.fr33music.core.domain.model

data class AlbumCard(
    val title: String,
    val artistName: String,
    val thumbnailUrl: String?,
    val songCount: Long = 0,
    val url: String? = null,
    val channelId: String? = null,
    val channelUrl: String? = null,
    val durationMs: Long = 0L
)
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/AudioFormat.kt`

```kotlin
package com.fr33music.core.domain.model

enum class AudioFormat {
    OPUS,
    M4A,
    UNKNOWN
}
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/AudioStream.kt`

```kotlin
package com.fr33music.core.domain.model

data class AudioStream(
    val url: String,
    val headers: Map<String, String>,
    val mime: String,
    val bitrateKbps: Int,
    val format: AudioFormat
)
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/ExtractorError.kt`

```kotlin
package com.fr33music.core.domain.model

sealed class ExtractorError(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    data class NetworkError(
        override val message: String? = "Error de red al conectar con el servidor",
        override val cause: Throwable? = null
    ) : ExtractorError(message, cause)

    data class StreamUnavailable(
        override val message: String? = "El stream de audio no está disponible"
    ) : ExtractorError(message)

    object EmptyQuery : ExtractorError("La consulta de búsqueda está vacía")

    data class Unknown(
        override val message: String? = "Ocurrió un error inesperado al extraer el contenido",
        override val cause: Throwable? = null
    ) : ExtractorError(message, cause)
}
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/LookingSearchResult.kt`

```kotlin
package com.fr33music.core.domain.model

data class ChannelCard(
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long = 0,
    val description: String? = null,
    val url: String,
    val channelId: String
)

data class LookingSearchResult(
    val channels: List<ChannelCard> = emptyList(),
    val videos: List<Track> = emptyList(),
    val playlists: List<AlbumCard> = emptyList()
)
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/OfflineAudioSource.kt`

```kotlin
package com.fr33music.core.domain.model

interface OfflineAudioSource {
    suspend fun getLocalAudioPath(trackId: String): String?
}
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/PlaybackError.kt`

```kotlin
package com.fr33music.core.domain.model

sealed class PlaybackError(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    data class SourceError(
        override val message: String? = "Error al reproducir la fuente de audio",
        override val cause: Throwable? = null
    ) : PlaybackError(message, cause)

    data class StreamResolveFailed(
        val track: Track,
        val reason: String
    ) : PlaybackError("No se pudo obtener el stream para ${track.title}: $reason")

    object EmptyQueue : PlaybackError("La cola de reproducción está vacía")

    data class Unknown(
        override val message: String? = "Error desconocido en el reproductor",
        override val cause: Throwable? = null
    ) : PlaybackError(message, cause)
}
```

---

### `core/domain/src/main/java/com/fr33music/core/domain/model/Track.kt`

```kotlin
package com.fr33music.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val trackId: String,
    val title: String,
    val artistName: String,
    val durationMs: Long,
    val thumbnailUrl: String? = null,
    val albumName: String? = null,
    val artistThumbnailUrl: String? = null
) {
    companion object {
        fun upgradeThumbnail(url: String?): String? {
            if (url.isNullOrBlank()) return url
            var result = url
            if (result.contains("/default.jpg")) result = result.replace("/default.jpg", "/maxresdefault.jpg")
            if (result.contains("/mqdefault.jpg")) result = result.replace("/mqdefault.jpg", "/maxresdefault.jpg")
            if (result.contains("/hqdefault.jpg")) result = result.replace("/hqdefault.jpg", "/maxresdefault.jpg")
            if (result.contains("/sddefault.jpg")) result = result.replace("/sddefault.jpg", "/maxresdefault.jpg")
            if (result.contains("hq720.jpg")) result = result.replace("hq720.jpg", "maxresdefault.jpg")
            if (result.contains("=w") || result.contains("=s")) {
                result = result.replace(Regex("=w\\d+-h\\d+.*"), "=w1024-h1024-l90-rj")
                    .replace(Regex("=w\\d+.*"), "=w1024-h1024-l90-rj")
                    .replace(Regex("=s\\d+.*"), "=s1024")
            }
            return result
        }
    }
}
```

---

### `core/extractor/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.fr33music.core.extractor"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    api(libs.newpipe.extractor)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/AudioStreamSelector.kt`

```kotlin
package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioFormat

data class AudioCandidate(
    val url: String,
    val mime: String,
    val bitrateKbps: Int,
    val format: AudioFormat
)

object AudioStreamSelector {

    /**
     * Prioritizes OPUS streams. If OPUS is not available, falls back to M4A.
     * Within the same format, picks the one with the highest bitrate.
     * Returns null if no suitable OPUS or M4A stream is found.
     */
    fun selectBestStream(candidates: List<AudioCandidate>): AudioCandidate? {
        if (candidates.isEmpty()) return null

        val opusStreams = candidates
            .filter { it.format == AudioFormat.OPUS }
            .sortedByDescending { it.bitrateKbps }

        if (opusStreams.isNotEmpty()) {
            return opusStreams.first()
        }

        val m4aStreams = candidates
            .filter { it.format == AudioFormat.M4A }
            .sortedByDescending { it.bitrateKbps }

        if (m4aStreams.isNotEmpty()) {
            return m4aStreams.first()
        }

        return null
    }

    fun parseFormat(formatName: String?, mimeType: String?): AudioFormat {
        val fName = formatName?.uppercase() ?: ""
        val mType = mimeType?.lowercase() ?: ""

        return when {
            fName.contains("OPUS") || mType.contains("opus") || mType.contains("webm") -> AudioFormat.OPUS
            fName.contains("M4A") || fName.contains("MP4") || mType.contains("mp4") || mType.contains("m4a") || mType.contains("aac") -> AudioFormat.M4A
            else -> AudioFormat.UNKNOWN
        }
    }
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/ExtractorInfo.kt`

```kotlin
package com.fr33music.core.extractor

object ExtractorInfo {
    const val VERSION = "0.26.5"
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/NewPipeDownloader.kt`

```kotlin
package com.fr33music.core.extractor

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class NewPipeDownloader(
    val okHttpClient: OkHttpClient = defaultClient()
) : Downloader() {

    private val cookieStore = ConcurrentHashMap<String, String>()

    fun setCookie(key: String, value: String) {
        cookieStore[key] = value
    }

    fun getCookie(key: String): String? = cookieStore[key]

    fun getAllCookies(): Map<String, String> = cookieStore

    override fun execute(request: Request): Response {
        val url = request.url()
        val method = request.httpMethod()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        var body: RequestBody? = null
        if (dataToSend != null) {
            val contentType = headers["Content-Type"]?.firstOrNull()
            body = dataToSend.toRequestBody(contentType?.toMediaTypeOrNull())
        } else if (method.equals("POST", ignoreCase = true) || method.equals("PUT", ignoreCase = true)) {
            body = ByteArray(0).toRequestBody(null)
        }

        val requestBuilder = okhttp3.Request.Builder()
            .url(url)
            .method(method, body)

        for ((name, values) in headers) {
            for (value in values) {
                requestBuilder.addHeader(name, value)
            }
        }

        if (!requestBuilder.build().headers.names().contains("User-Agent")) {
            requestBuilder.addHeader("User-Agent", USER_AGENT)
        }

        if (cookieStore.isNotEmpty() && !requestBuilder.build().headers.names().contains("Cookie")) {
            val cookieHeader = cookieStore.entries.joinToString("; ") { "${it.key}=${it.value}" }
            requestBuilder.addHeader("Cookie", cookieHeader)
        }

        val call = okHttpClient.newCall(requestBuilder.build())
        val okResponse = call.execute()

        val responseBodyBytes = okResponse.body?.bytes() ?: ByteArray(0)
        val responseBodyString = responseBodyBytes.decodeToString()
        val responseHeaders = okResponse.headers.toMultimap()

        // Capture any set-cookie headers safely
        val setCookieHeaders = okResponse.headers("Set-Cookie")
        for (cookie in setCookieHeaders) {
            try {
                val firstPart = cookie.split(";").firstOrNull() ?: continue
                val eqIdx = firstPart.indexOf('=')
                if (eqIdx > 0) {
                    val name = firstPart.substring(0, eqIdx).trim()
                    val value = firstPart.substring(eqIdx + 1).trim()
                    if (name.isNotEmpty()) {
                        setCookie(name, value)
                    }
                }
            } catch (_: Exception) {
                // Ignorar cookies malformadas
            }
        }

        return Response(
            okResponse.code,
            okResponse.message,
            responseHeaders,
            responseBodyString,
            okResponse.request.url.toString()
        )
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        const val REFERER_YOUTUBE = "https://www.youtube.com/"

        fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/NewPipeSearchSource.kt`

```kotlin
package com.fr33music.core.extractor

import android.util.Log
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException

class NewPipeSearchSource(
    private val downloader: NewPipeDownloader = NewPipeDownloader()
) : SearchSource {

    init {
        ensureInitialized(downloader)
    }

    override suspend fun search(
        query: String,
        page: Int
    ): Result<List<Track>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.failure(ExtractorError.EmptyQuery)
        }

        try {
            val service = ServiceList.YouTube
            val extractor = service.getSearchExtractor(trimmedQuery)
            try {
                extractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Error en fetchPage de search para '$trimmedQuery': ${e.message}")
            }

            val initialItems = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
            val tracks = mutableListOf<Track>()

            for (item in initialItems) {
                if (item is StreamInfoItem) {
                    val url = item.url ?: continue
                    val id = extractVideoId(url)
                    if (id.isBlank()) continue
                    val rawThumb = try {
                        item.thumbnails?.maxByOrNull { it.height }?.url
                            ?: item.thumbnails?.firstOrNull()?.url
                    } catch (_: Exception) { null }
                    val thumbUrl = Track.upgradeThumbnail(rawThumb)
                    val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                    val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
                    val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                    tracks.add(
                        Track(
                            trackId = id,
                            title = title,
                            artistName = artist,
                            durationMs = duration,
                            thumbnailUrl = thumbUrl,
                            albumName = artist
                        )
                    )
                }
            }

            Result.success(tracks)
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    override suspend fun searchLookingDetails(query: String): Result<LookingSearchResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.failure(ExtractorError.EmptyQuery)
        }

        try {
            val service = ServiceList.YouTube
            val extractor = service.getSearchExtractor(trimmedQuery)
            try {
                extractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Error en fetchPage de searchLookingDetails para '$trimmedQuery': ${e.message}")
            }

            val items = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()

            val channels = mutableListOf<ChannelCard>()
            val videos = mutableListOf<Track>()
            val playlists = mutableListOf<AlbumCard>()
            val seenChannelIds = mutableSetOf<String>()
            val seenVideoIds = mutableSetOf<String>()
            val seenPlaylistUrls = mutableSetOf<String>()

            // 1. Separar por tipo de resultado
            for (item in items) {
                when (item) {
                    is ChannelInfoItem -> {
                        val url = item.url ?: ""
                        val name = item.name?.takeIf { it.isNotBlank() } ?: "Canal"
                        val channelId = if (url.contains("/channel/")) {
                            url.substringAfter("/channel/").substringBefore("?").substringBefore("/")
                        } else if (url.isNotBlank()) {
                            url.substringAfterLast("/")
                        } else {
                            name
                        }
                        if (channelId.isNotBlank() && seenChannelIds.add(channelId)) {
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val subCount = try { item.subscriberCount } catch (_: Exception) { 0L }
                            val desc = try { item.description } catch (_: Exception) { null }
                            channels.add(
                                ChannelCard(
                                    name = name,
                                    avatarUrl = thumb,
                                    subscriberCount = subCount,
                                    description = desc,
                                    url = url,
                                    channelId = channelId
                                )
                            )
                        }
                    }

                    is PlaylistInfoItem -> {
                        val url = item.url ?: ""
                        if (url.isNotBlank() && seenPlaylistUrls.add(url)) {
                            val title = item.name?.takeIf { it.isNotBlank() } ?: "Playlist"
                            val uploader = item.uploaderName?.takeIf { it.isNotBlank() } ?: "YouTube"
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val streamCount = try { item.streamCount } catch (_: Exception) { 0L }
                            val uploaderUrl = try { item.uploaderUrl } catch (_: Exception) { null }
                            playlists.add(
                                AlbumCard(
                                    title = title,
                                    artistName = uploader,
                                    thumbnailUrl = thumb,
                                    songCount = streamCount,
                                    url = url,
                                    channelUrl = uploaderUrl
                                )
                            )
                        }
                    }

                    is StreamInfoItem -> {
                        val url = item.url ?: continue
                        val id = extractVideoId(url)
                        if (id.isNotBlank() && seenVideoIds.add(id)) {
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                            val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: "Desconocido"
                            val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                            videos.add(
                                Track(
                                    trackId = id,
                                    title = title,
                                    artistName = artist,
                                    durationMs = duration,
                                    thumbnailUrl = thumb,
                                    albumName = artist
                                )
                            )
                        }
                    }
                }
            }

            // 2. Búsqueda enfocada a Canales si YouTube no devolvió el canal o falta coincidencia directa
            val hasExactChannel = channels.any { it.name.trim().equals(trimmedQuery, ignoreCase = true) }
            if (channels.isEmpty() || !hasExactChannel) {
                try {
                    val channelSearchExtractor = service.getSearchExtractor(
                        trimmedQuery,
                        listOf(YoutubeSearchQueryHandlerFactory.CHANNELS),
                        ""
                    )
                    channelSearchExtractor.fetchPage()
                    val channelItems = try { channelSearchExtractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
                    for (item in channelItems) {
                        if (item is ChannelInfoItem) {
                            val url = item.url ?: ""
                            val name = item.name?.takeIf { it.isNotBlank() } ?: "Canal"
                            val channelId = if (url.contains("/channel/")) {
                                url.substringAfter("/channel/").substringBefore("?").substringBefore("/")
                            } else if (url.isNotBlank()) {
                                url.substringAfterLast("/")
                            } else {
                                name
                            }
                            if (channelId.isNotBlank() && seenChannelIds.add(channelId)) {
                                val rawThumb = try {
                                    item.thumbnails?.maxByOrNull { it.height }?.url
                                        ?: item.thumbnails?.firstOrNull()?.url
                                } catch (_: Exception) { null }
                                val thumb = Track.upgradeThumbnail(rawThumb)
                                val subCount = try { item.subscriberCount } catch (_: Exception) { 0L }
                                val desc = try { item.description } catch (_: Exception) { null }
                                val card = ChannelCard(
                                    name = name,
                                    avatarUrl = thumb,
                                    subscriberCount = subCount,
                                    description = desc,
                                    url = url,
                                    channelId = channelId
                                )
                                // Si el nombre coincide con la búsqueda, ponerlo al principio de la lista
                                if (card.name.trim().equals(trimmedQuery, ignoreCase = true)) {
                                    channels.add(0, card)
                                } else {
                                    channels.add(card)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error en búsqueda secundaria de canales para '$trimmedQuery': ${e.message}")
                }
            }

            // Limitar canales a un número razonable para no saturar el LazyRow (máximo 8 canales)
            val sortedChannels = channels.sortedByDescending { 
                if (it.name.trim().equals(trimmedQuery, ignoreCase = true)) 1000L else it.subscriberCount 
            }.take(8)

            Result.success(
                LookingSearchResult(
                    channels = sortedChannels,
                    videos = videos,
                    playlists = playlists
                )
            )
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    override suspend fun getChannelVideos(channel: ChannelCard): Result<List<Track>> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val seenTrackIds = mutableSetOf<String>()
        if (channel.url.isBlank()) {
            return@withContext Result.success(tracks)
        }

        try {
            val service = ServiceList.YouTube
            val channelExtractor = service.getChannelExtractor(channel.url)
            try {
                channelExtractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Fallo al obtener página inicial del canal ${channel.name}: ${e.message}")
            }

            val tabs = try { channelExtractor.tabs } catch (_: Exception) { emptyList() }
            val videoTab = tabs.firstOrNull { tab ->
                try {
                    tab.contentFilters?.any { it.equals("videos", ignoreCase = true) } == true
                } catch (_: Exception) { false }
            } ?: tabs.firstOrNull()

            if (videoTab != null) {
                try {
                    val tabExtractor = service.getChannelTabExtractor(videoTab)
                    tabExtractor.fetchPage()

                    var currentPage = try { tabExtractor.initialPage } catch (_: Exception) { null }
                    var pageCount = 0
                    val maxPages = 4 // ~120 videos

                    while (currentPage != null && pageCount < maxPages) {
                        val items = try { currentPage.items } catch (_: Exception) { null } ?: emptyList()
                        for (item in items) {
                            if (item is StreamInfoItem) {
                                val url = item.url ?: continue
                                val id = extractVideoId(url)
                                if (id.isNotBlank() && seenTrackIds.add(id)) {
                                    val rawThumb = try {
                                        item.thumbnails?.maxByOrNull { it.height }?.url
                                            ?: item.thumbnails?.firstOrNull()?.url
                                    } catch (_: Exception) { null }
                                    val thumb = Track.upgradeThumbnail(rawThumb)
                                    val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                                    val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: channel.name
                                    val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                                    tracks.add(
                                        Track(
                                            trackId = id,
                                            title = title,
                                            artistName = artist,
                                            durationMs = duration,
                                            thumbnailUrl = thumb,
                                            albumName = channel.name,
                                            artistThumbnailUrl = channel.avatarUrl
                                        )
                                    )
                                }
                            }
                        }

                        if (currentPage.hasNextPage() && currentPage.nextPage != null) {
                            try {
                                currentPage = tabExtractor.getPage(currentPage.nextPage)
                                pageCount++
                            } catch (_: Exception) {
                                break
                            }
                        } else break
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error extrayendo pestaña de videos del canal ${channel.name}: ${e.message}")
                }
            }

            // Fallback si no vinieron videos de las pestañas
            if (tracks.isEmpty()) {
                val searchRes = search(channel.name)
                val fallbackTracks = searchRes.getOrElse { emptyList() }.filter {
                    it.artistName.equals(channel.name, ignoreCase = true)
                }
                tracks.addAll(fallbackTracks)
            }

            Result.success(tracks)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener videos de canal ${channel.name}: ${e.message}", e)
            val searchRes = search(channel.name)
            Result.success(searchRes.getOrElse { emptyList() })
        }
    }

    override suspend fun getAlbumTracks(album: AlbumCard): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val url = album.url
            if (!url.isNullOrBlank()) {
                // Si la URL apunta a una canción individual (sencillo)
                if (url.contains("watch?v=") && !url.contains("list=")) {
                    val id = extractVideoId(url)
                    if (id.isNotBlank()) {
                        var duration = album.durationMs
                        if (duration <= 0L) {
                            try {
                                val service = ServiceList.YouTube
                                val streamExtractor = service.getStreamExtractor(url)
                                streamExtractor.fetchPage()
                                val sec = streamExtractor.length
                                if (sec > 0) duration = sec * 1000L
                            } catch (e: Exception) {
                                Log.w(TAG, "No se pudo obtener duración precisa de $id: ${e.message}")
                            }
                        }
                        return@withContext Result.success(listOf(
                            Track(
                                trackId = id,
                                title = album.title,
                                artistName = album.artistName,
                                albumName = album.title,
                                durationMs = duration,
                                thumbnailUrl = album.thumbnailUrl
                            )
                        ))
                    }
                }

                val service = ServiceList.YouTube
                val extractor = service.getPlaylistExtractor(url)
                try {
                    extractor.fetchPage()
                } catch (e: Exception) {
                    Log.w(TAG, "Error en fetchPage de playlist: ${e.message}")
                }

                val tracks = mutableListOf<Track>()
                val items = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
                for (item in items) {
                    if (item is StreamInfoItem) {
                        val id = extractVideoId(item.url)
                        val rawThumb = try {
                            item.thumbnails?.maxByOrNull { it.height }?.url
                                ?: item.thumbnails?.firstOrNull()?.url
                                ?: album.thumbnailUrl
                        } catch (_: Exception) { album.thumbnailUrl }
                        val thumbUrl = Track.upgradeThumbnail(rawThumb)
                        val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                        val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: album.artistName
                        val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                        val track = Track(
                            trackId = id,
                            title = title,
                            artistName = artist,
                            albumName = album.title,
                            durationMs = duration,
                            thumbnailUrl = thumbUrl
                        )
                        tracks.add(track)
                    }
                }
                if (tracks.isNotEmpty()) {
                    return@withContext Result.success(tracks)
                }
            }

            // Fallback si no hay URL o lista vacía: buscar las canciones de ese álbum por texto
            val query = "${album.artistName} ${album.title}"
            val searchRes = search(query)
            val fallbackTracks = searchRes.getOrElse { emptyList() }.map {
                it.copy(albumName = album.title)
            }
            Result.success(fallbackTracks)
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al obtener pistas de playlist: ${e.message}, intentando fallback por texto")
            val query = "${album.artistName} ${album.title}"
            val searchRes = search(query)
            val fallbackTracks = searchRes.getOrElse { emptyList() }.map {
                it.copy(albumName = album.title)
            }
            Result.success(fallbackTracks)
        }
    }

    private fun extractVideoId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        return try {
            if (url.contains("v=")) {
                url.substringAfter("v=").substringBefore("&")
            } else if (url.contains("youtu.be/")) {
                url.substringAfter("youtu.be/").substringBefore("?")
            } else {
                url
            }
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        private const val TAG = "NewPipeSearchSource"
        private var isInitialized = false

        @Synchronized
        fun ensureInitialized(downloader: NewPipeDownloader) {
            if (!isInitialized) {
                NewPipe.init(downloader)
                isInitialized = true
            }
        }
    }
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/NewPipeStreamResolver.kt`

```kotlin
package com.fr33music.core.extractor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fr33music.core.domain.model.AudioFormat
import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.OfflineAudioSource
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.File
import java.io.IOException

class NewPipeStreamResolver(
    private val downloader: NewPipeDownloader = NewPipeDownloader(),
    private val offlineAudioSource: OfflineAudioSource? = null,
    private val context: Context? = null
) : StreamResolver {

    init {
        NewPipeSearchSource.ensureInitialized(downloader)
    }

    override suspend fun resolveAudio(track: Track): Result<AudioStream> = withContext(Dispatchers.IO) {
        try {
            val cleanId = extractVideoId(track.trackId)

            // CRÍTICO: Comprobar primero si el archivo existe localmente en almacenamiento privado
            var localFile: File? = null
            val minSizeBytes = 100 * 1024L

            // 1. A través de offlineAudioSource si está disponible
            val sourcePath = offlineAudioSource?.getLocalAudioPath(cleanId)
                ?: offlineAudioSource?.getLocalAudioPath(track.trackId)
            if (!sourcePath.isNullOrBlank()) {
                val f = File(sourcePath)
                if (f.exists() && f.length() >= minSizeBytes) {
                    localFile = f
                }
            }

            // 2. Comprobación directa en context.filesDir/downloads/
            if (localFile == null && context != null) {
                val downloadsDir = File(context.filesDir, "downloads")
                val opus = File(downloadsDir, "$cleanId.opus")
                val m4a = File(downloadsDir, "$cleanId.m4a")
                if (opus.exists() && opus.length() >= minSizeBytes) {
                    localFile = opus
                } else if (m4a.exists() && m4a.length() >= minSizeBytes) {
                    localFile = m4a
                }
            }

            // Si el archivo local existe y tiene contenido válido (>= 100KB), devolver URI local y NO llamar a YouTube
            if (localFile != null && localFile.exists() && localFile.length() >= minSizeBytes) {
                val isOpus = localFile.name.endsWith(".opus", ignoreCase = true)
                val localUri = Uri.fromFile(localFile).toString()
                Log.i("StreamResolver", "Archivo offline detectado para $cleanId: $localUri (${localFile.length()} bytes). Omitiendo petición de red.")
                val offlineStream = AudioStream(
                    url = localUri,
                    headers = emptyMap(),
                    mime = if (isOpus) "audio/opus" else "audio/mp4",
                    bitrateKbps = 160,
                    format = if (isOpus) AudioFormat.OPUS else AudioFormat.M4A
                )
                return@withContext Result.success(offlineStream)
            }

            val videoUrl = if (cleanId.startsWith("http")) {
                cleanId
            } else {
                "https://www.youtube.com/watch?v=$cleanId"
            }

            // Reintentos automáticos si el extractor o la red fallan
            var lastError: Throwable? = null
            for (attempt in 1..3) {
                try {
                    Log.d("StreamResolver", "Resolviendo stream en red desde YouTube para: $videoUrl (intento $attempt/3)")
                    val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
                    val audioStreams = streamInfo.audioStreams

                    if (audioStreams.isNullOrEmpty()) {
                        throw ExtractorError.StreamUnavailable("No se encontraron streams de audio para este contenido")
                    }

                    val candidates = audioStreams.map { stream ->
                        val format = AudioStreamSelector.parseFormat(
                            formatName = stream.format?.name ?: stream.format?.mimeType,
                            mimeType = stream.format?.mimeType
                        )
                        AudioCandidate(
                            url = stream.content ?: "",
                            mime = stream.format?.mimeType ?: "audio/*",
                            bitrateKbps = stream.averageBitrate.takeIf { it > 0 } ?: stream.bitrate,
                            format = format
                        )
                    }.filter { it.url.isNotEmpty() }

                    val bestCandidate = AudioStreamSelector.selectBestStream(candidates)
                        ?: throw ExtractorError.StreamUnavailable("No se encontró stream compatible (OPUS/M4A)")

                    val headers = mutableMapOf<String, String>(
                        "User-Agent" to NewPipeDownloader.USER_AGENT
                    )

                    val cookieMap = downloader.getAllCookies()
                    if (cookieMap.isNotEmpty()) {
                        headers["Cookie"] = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    }

                    val resolved = AudioStream(
                        url = bestCandidate.url,
                        headers = headers,
                        mime = bestCandidate.mime,
                        bitrateKbps = bestCandidate.bitrateKbps,
                        format = bestCandidate.format
                    )

                    return@withContext Result.success(resolved)
                } catch (e: Exception) {
                    lastError = e
                    Log.w("StreamResolver", "Fallo al resolver stream en intento $attempt/3: ${e.message}")
                    if (attempt < 3) {
                        delay(500L * attempt)
                    }
                }
            }

            val finalError = lastError ?: Exception("Error desconocido al resolver audio")
            if (finalError is IOException) {
                Result.failure(ExtractorError.NetworkError(finalError.message, finalError))
            } else if (finalError is ExtractorError) {
                Result.failure(finalError)
            } else {
                Result.failure(ExtractorError.Unknown(finalError.message, finalError))
            }
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    private fun extractVideoId(urlOrId: String): String {
        return when {
            urlOrId.contains("v=") -> urlOrId.substringAfter("v=").substringBefore("&")
            urlOrId.contains("youtu.be/") -> urlOrId.substringAfter("youtu.be/").substringBefore("?")
            else -> urlOrId.trim()
        }
    }
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/SearchSource.kt`

```kotlin
package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

interface SearchSource {
    suspend fun search(
        query: String,
        page: Int = 0
    ): Result<List<Track>>

    suspend fun searchLookingDetails(
        query: String
    ): Result<LookingSearchResult>

    suspend fun getChannelVideos(
        channel: ChannelCard
    ): Result<List<Track>>

    suspend fun getAlbumTracks(
        album: AlbumCard
    ): Result<List<Track>>
}
```

---

### `core/extractor/src/main/java/com/fr33music/core/extractor/StreamResolver.kt`

```kotlin
package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.Track

interface StreamResolver {
    suspend fun resolveAudio(track: Track): Result<AudioStream>
}
```

---

### `core/extractor/src/test/java/com/fr33music/core/extractor/AudioStreamSelectorTest.kt`

```kotlin
package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AudioStreamSelectorTest {

    @Test
    fun `fixture 1 - OPUS y M4A disponibles - prioriza OPUS con mayor bitrate`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/m4a_128",
                mime = "audio/mp4",
                bitrateKbps = 128,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_160",
                mime = "audio/webm",
                bitrateKbps = 160,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_96",
                mime = "audio/webm",
                bitrateKbps = 96,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/m4a_256",
                mime = "audio/mp4",
                bitrateKbps = 256,
                format = AudioFormat.M4A
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.OPUS, selected?.format)
        assertEquals(160, selected?.bitrateKbps)
        assertEquals("https://audio.stream/opus_160", selected?.url)
    }

    @Test
    fun `fixture 2 - solo M4A disponible - selecciona M4A con mayor bitrate`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/m4a_128",
                mime = "audio/mp4",
                bitrateKbps = 128,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/m4a_256",
                mime = "audio/mp4",
                bitrateKbps = 256,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/unknown_format",
                mime = "audio/unknown",
                bitrateKbps = 320,
                format = AudioFormat.UNKNOWN
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.M4A, selected?.format)
        assertEquals(256, selected?.bitrateKbps)
        assertEquals("https://audio.stream/m4a_256", selected?.url)
    }

    @Test
    fun `fixture 3 - ninguno disponible o lista vacia - devuelve null`() {
        val emptyCandidates = emptyList<AudioCandidate>()
        val selectedEmpty = AudioStreamSelector.selectBestStream(emptyCandidates)
        assertNull(selectedEmpty)

        val onlyUnknown = listOf(
            AudioCandidate(
                url = "https://audio.stream/unknown",
                mime = "audio/unknown",
                bitrateKbps = 128,
                format = AudioFormat.UNKNOWN
            )
        )
        val selectedUnknown = AudioStreamSelector.selectBestStream(onlyUnknown)
        assertNull(selectedUnknown)
    }

    @Test
    fun `parseFormat reconoce OPUS y M4A correctamente`() {
        assertEquals(AudioFormat.OPUS, AudioStreamSelector.parseFormat("WEBMA_OPUS", "audio/webm"))
        assertEquals(AudioFormat.OPUS, AudioStreamSelector.parseFormat("opus", "audio/opus"))
        assertEquals(AudioFormat.M4A, AudioStreamSelector.parseFormat("M4A", "audio/mp4"))
        assertEquals(AudioFormat.M4A, AudioStreamSelector.parseFormat("AAC", "audio/aac"))
        assertEquals(AudioFormat.UNKNOWN, AudioStreamSelector.parseFormat("OGG", "audio/ogg"))
    }
}
```

---

### `core/player/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.core.player"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:extractor"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource.okhttp)

    // Coil para carga de carátulas en segundo plano / notificación
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
}
```

---

### `core/player/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application>
        <service
            android:name=".service.Fr33MusicPlayerService"
            android:exported="true"
            android:foregroundServiceType="mediaPlayback">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService" />
            </intent-filter>
        </service>
    </application>

</manifest>
```

---

### `core/player/src/main/java/com/fr33music/core/player/PlaybackState.kt`

```kotlin
package com.fr33music.core.player

import com.fr33music.core.domain.model.Track

data class PlaybackState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isShuffle: Boolean = false,
    val error: String? = null
) {
    val currentTrack: Track?
        get() = queue.getOrNull(currentIndex)
}
```

---

### `core/player/src/main/java/com/fr33music/core/player/PlayerRepository.kt`

```kotlin
package com.fr33music.core.player

import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playbackState: StateFlow<PlaybackState>
    suspend fun play(track: Track)
    suspend fun setQueue(list: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun toggleShuffle()
    suspend fun next()
    suspend fun previous()
    fun seekTo(positionMs: Long)
}
```

---

### `core/player/src/main/java/com/fr33music/core/player/PlayerRepositoryImpl.kt`

```kotlin
package com.fr33music.core.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.fr33music.core.database.datastore.QueueDataStore
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.NewPipeDownloader
import com.fr33music.core.extractor.StreamResolver
import com.fr33music.core.player.service.Fr33MusicPlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val streamResolver: StreamResolver,
    private val queueDataStore: QueueDataStore
) : PlayerRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private val playedTrackIds = mutableSetOf<String>()
    private var lastFailedTrackId: String? = null
    private var playerRetryCount = 0

    init {
        ensureServiceRunning()

        // Restaurar estado de cola guardado en DataStore (sobrevive al cerrar la app)
        repositoryScope.launch {
            val savedState = queueDataStore.getQueueStateSync()
            if (savedState.queue.isNotEmpty()) {
                val validIndex = savedState.currentIndex.coerceIn(0, savedState.queue.lastIndex)
                _playbackState.update {
                    it.copy(
                        queue = savedState.queue,
                        currentIndex = validIndex,
                        positionMs = savedState.positionMs,
                        isShuffle = savedState.isShuffle,
                        isPlaying = false
                    )
                }
                if (savedState.isShuffle) {
                    savedState.queue.getOrNull(validIndex)?.let { playedTrackIds.add(it.trackId) }
                }
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    repositoryScope.launch {
                        queueDataStore.savePlaybackPosition(exoPlayer.currentPosition)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_READY -> {
                        playerRetryCount = 0
                        lastFailedTrackId = null
                        val currentDur = exoPlayer.duration.coerceAtLeast(0L)
                        _playbackState.update { current ->
                            val updatedQueue = if (currentDur > 0L && current.currentTrack != null && current.currentTrack!!.durationMs <= 0L) {
                                current.queue.mapIndexed { idx, t ->
                                    if (idx == current.currentIndex) t.copy(durationMs = currentDur) else t
                                }
                            } else current.queue

                            current.copy(
                                isLoading = false,
                                error = null,
                                durationMs = if (currentDur > 0L) currentDur else current.durationMs,
                                queue = updatedQueue
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isLoading = false) }
                        repositoryScope.launch { next() }
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.update { it.copy(isLoading = false) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerRepo", "ExoPlayer onPlayerError: ${error.message} (código: ${error.errorCode})", error)
                val currentTrack = _playbackState.value.currentTrack
                val is403OrHttp = error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ||
                        error.message?.contains("403") == true ||
                        error.cause?.message?.contains("403") == true ||
                        error.cause?.javaClass?.simpleName?.contains("HttpDataSource") == true

                if (is403OrHttp && currentTrack != null && (lastFailedTrackId != currentTrack.trackId || playerRetryCount < 2)) {
                    if (lastFailedTrackId != currentTrack.trackId) {
                        lastFailedTrackId = currentTrack.trackId
                        playerRetryCount = 1
                    } else {
                        playerRetryCount++
                    }
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    Log.w("PlayerRepo", "Detectado error 403 / HTTP en pista ${currentTrack.trackId}. Reintentando con stream fresco (intento $playerRetryCount/2)...")
                    repositoryScope.launch {
                        delay(600)
                        resolveAndPlay(currentTrack, pos)
                    }
                    return
                }

                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        error = error.message ?: "Error de reproducción"
                    )
                }
            }
        })
    }

    override suspend fun play(track: Track) {
        val currentQueue = _playbackState.value.queue
        val existingIndex = currentQueue.indexOfFirst { it.trackId == track.trackId }

        val newQueue: List<Track>
        val newIndex: Int
        if (existingIndex >= 0) {
            newQueue = currentQueue
            newIndex = existingIndex
        } else {
            newQueue = currentQueue + track
            newIndex = newQueue.lastIndex
        }

        _playbackState.update {
            it.copy(queue = newQueue, currentIndex = newIndex)
        }
        playedTrackIds.add(track.trackId)
        queueDataStore.saveQueue(newQueue, newIndex, _playbackState.value.isShuffle)
        resolveAndPlay(track)
    }

    override suspend fun setQueue(list: List<Track>, startIndex: Int) {
        if (list.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, list.lastIndex)
        _playbackState.update {
            it.copy(queue = list, currentIndex = validIndex)
        }
        playedTrackIds.clear()
        playedTrackIds.add(list[validIndex].trackId)
        queueDataStore.saveQueue(list, validIndex, _playbackState.value.isShuffle)
        resolveAndPlay(list[validIndex])
    }

    override fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.playbackState == Player.STATE_IDLE && _playbackState.value.currentTrack != null) {
                    repositoryScope.launch {
                        val track = _playbackState.value.currentTrack ?: return@launch
                        resolveAndPlay(track, initialPositionMs = _playbackState.value.positionMs)
                    }
                } else {
                    exoPlayer.play()
                }
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    override fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        _playbackState.update { it.copy(isShuffle = newShuffle) }
        playedTrackIds.clear()
        if (newShuffle) {
            _playbackState.value.currentTrack?.let { playedTrackIds.add(it.trackId) }
        }
        repositoryScope.launch {
            queueDataStore.saveShuffle(newShuffle)
        }
    }

    override suspend fun next() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        val nextIndex: Int
        if (state.isShuffle) {
            // Regla de Shuffle: aleatorio de los que no están en el Set. Si se agota, limpiar y empezar
            val unplayed = state.queue.filter { it.trackId !in playedTrackIds }
            val chosenTrack = if (unplayed.isNotEmpty()) {
                unplayed.random()
            } else {
                playedTrackIds.clear()
                state.currentTrack?.let { playedTrackIds.add(it.trackId) }
                val remaining = state.queue.filter { it.trackId !in playedTrackIds }
                remaining.randomOrNull() ?: state.currentTrack ?: state.queue.firstOrNull()
            }
            if (chosenTrack == null) return
            playedTrackIds.add(chosenTrack.trackId)
            val foundIdx = state.queue.indexOfFirst { it.trackId == chosenTrack.trackId }
            nextIndex = if (foundIdx in state.queue.indices) foundIdx else 0
        } else {
            nextIndex = (state.currentIndex + 1) % state.queue.size
        }

        if (nextIndex in state.queue.indices) {
            _playbackState.update { it.copy(currentIndex = nextIndex) }
            queueDataStore.saveCurrentIndex(nextIndex)
            resolveAndPlay(state.queue[nextIndex])
        }
    }

    override suspend fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.lastIndex
        _playbackState.update { it.copy(currentIndex = prevIndex) }
        queueDataStore.saveCurrentIndex(prevIndex)
        resolveAndPlay(state.queue[prevIndex])
    }

    override fun seekTo(positionMs: Long) {
        try {
            exoPlayer.seekTo(positionMs)
            _playbackState.update { it.copy(positionMs = positionMs) }
            repositoryScope.launch {
                queueDataStore.savePlaybackPosition(positionMs)
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun resolveAndPlay(track: Track, initialPositionMs: Long = 0L) {
        _playbackState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = streamResolver.resolveAudio(track)
            result.fold(
                onSuccess = { stream ->
                    try {
                        val parsedUri = Uri.parse(stream.url)
                        val isLocalFile = stream.url.startsWith("file://") ||
                                stream.url.startsWith("file:/") ||
                                stream.url.startsWith("content://") ||
                                parsedUri.scheme == "file" ||
                                parsedUri.scheme == "content"

                        val isLocalArtwork = track.thumbnailUrl?.startsWith("file:") == true || track.thumbnailUrl?.startsWith("content:") == true

                        // 1. Cargar carátula en ByteArray ANTES de construir el MediaItem
                        // usando la caché de disco de Coil (funciona offline) para evitar cuadros blancos.
                        val artworkBytes = loadArtworkBytes(track.thumbnailUrl)

                        val mediaMetadataBuilder = MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artistName)

                        if (artworkBytes != null) {
                            mediaMetadataBuilder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        }

                        if (isLocalArtwork && !track.thumbnailUrl.isNullOrBlank()) {
                            mediaMetadataBuilder.setArtworkUri(Uri.parse(track.thumbnailUrl))
                        }
                        val metadata = mediaMetadataBuilder.build()

                        val mediaItem = MediaItem.Builder()
                            .setUri(parsedUri)
                            .setMediaId(track.trackId)
                            .setMediaMetadata(metadata)
                            .build()

                        // 2. Configurar la fuente de datos:
                        // Si la URI empieza con file:// o content://, usar DefaultDataSource.Factory(context) SIN headers de red.
                        // Si la URI empieza con http:// o https://, usar DefaultHttpDataSource.Factory con headers.
                        val dataSourceFactory = if (isLocalFile) {
                            Log.i("PlayerRepo", "Reproduciendo archivo local sin headers de red: ${stream.url}")
                            DefaultDataSource.Factory(context)
                        } else {
                            Log.i("PlayerRepo", "Reproduciendo stream remoto desde YouTube con headers HTTP: ${stream.url}")
                            val userAgent = stream.headers["User-Agent"] ?: NewPipeDownloader.USER_AGENT
                            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                                .setUserAgent(userAgent)
                                .setDefaultRequestProperties(stream.headers)
                                .setAllowCrossProtocolRedirects(true)
                            DefaultDataSource.Factory(context, httpDataSourceFactory)
                        }

                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)

                        exoPlayer.setMediaSource(mediaSource)
                        exoPlayer.playlistMetadata = metadata
                        exoPlayer.prepare()
                        if (initialPositionMs > 0L) {
                            exoPlayer.seekTo(initialPositionMs)
                        }
                        exoPlayer.play()

                        ensureServiceRunning()

                        // 3. Si artworkBytes no se cargó de inmediato, intentar cargar en segundo plano y actualizar MediaItem
                        if (artworkBytes == null && !track.thumbnailUrl.isNullOrBlank()) {
                            repositoryScope.launch(Dispatchers.IO) {
                                val bytes = loadArtworkBytes(track.thumbnailUrl)
                                if (bytes != null && _playbackState.value.currentTrack?.trackId == track.trackId) {
                                    val updatedMetadata = MediaMetadata.Builder()
                                        .setTitle(track.title)
                                        .setArtist(track.artistName)
                                        .setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                        .build()

                                    withContext(Dispatchers.Main) {
                                        exoPlayer.playlistMetadata = updatedMetadata
                                        val currentItem = exoPlayer.currentMediaItem
                                        if (currentItem != null) {
                                            val updatedItem = currentItem.buildUpon()
                                                .setMediaMetadata(updatedMetadata)
                                                .build()
                                            exoPlayer.replaceMediaItem(exoPlayer.currentMediaItemIndex, updatedItem)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PlayerRepo", "Error al inicializar el reproductor: ${e.message}", e)
                        _playbackState.update {
                            it.copy(
                                isLoading = false,
                                isPlaying = false,
                                error = e.message ?: "Error al inicializar el reproductor"
                            )
                        }
                    }
                },
                onFailure = { throwable ->
                    Log.e("PlayerRepo", "Fallo al resolver audio para ${track.trackId}: ${throwable.message}", throwable)
                    _playbackState.update {
                        it.copy(
                            isLoading = false,
                            isPlaying = false,
                            error = throwable.message ?: "No se pudo reproducir este tema"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("PlayerRepo", "Excepción al resolver y reproducir: ${e.message}", e)
            _playbackState.update {
                it.copy(
                    isLoading = false,
                    isPlaying = false,
                    error = e.message ?: "Error al obtener el stream de audio"
                )
            }
        }
    }

    private fun ensureServiceRunning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val serviceIntent = Intent(context, Fr33MusicPlayerService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (_: Exception) {
                // Service already running or background restrictions
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = repositoryScope.launch {
            var counter = 0
            while (isActive) {
                val currentPos = exoPlayer.currentPosition
                val currentDur = exoPlayer.duration.coerceAtLeast(0L)
                _playbackState.update { current ->
                    val updatedQueue = if (currentDur > 0L && current.currentTrack != null && current.currentTrack!!.durationMs <= 0L) {
                        current.queue.mapIndexed { idx, t ->
                            if (idx == current.currentIndex) t.copy(durationMs = currentDur) else t
                        }
                    } else current.queue

                    current.copy(
                        positionMs = currentPos,
                        durationMs = if (currentDur > 0L) currentDur else current.durationMs,
                        queue = updatedQueue
                    )
                }
                counter++
                if (counter % 4 == 0) { // Persistir posición cada 2 segundos
                    queueDataStore.savePlaybackPosition(currentPos)
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private suspend fun loadArtworkBytes(url: String?): ByteArray? {
        if (url.isNullOrBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val loader = coil.Coil.imageLoader(context)
                val request = coil.request.ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                val drawable = result.drawable ?: return@withContext null
                val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                    drawable.bitmap
                } else {
                    val bmp = android.graphics.Bitmap.createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1),
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
                stream.toByteArray()
            } catch (e: Exception) {
                Log.e("PlayerRepo", "Error cargando carátula para notificación: ${e.message}")
                null
            }
        }
    }
}
```

---

### `core/player/src/main/java/com/fr33music/core/player/di/PlayerModule.kt`

```kotlin
package com.fr33music.core.player.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.fr33music.core.extractor.NewPipeSearchSource
import com.fr33music.core.extractor.NewPipeStreamResolver
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.extractor.StreamResolver
import com.fr33music.core.player.PlayerRepository
import com.fr33music.core.player.PlayerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerBindingModule {
    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }

    @Provides
    @Singleton
    fun provideSearchSource(): SearchSource {
        return NewPipeSearchSource()
    }

    @Provides
    @Singleton
    fun provideStreamResolver(
        @ApplicationContext context: Context,
        offlineAudioSource: com.fr33music.core.domain.model.OfflineAudioSource
    ): StreamResolver {
        return NewPipeStreamResolver(
            offlineAudioSource = offlineAudioSource,
            context = context
        )
    }
}
```

---

### `core/player/src/main/java/com/fr33music/core/player/service/Fr33MusicPlayerService.kt`

```kotlin
package com.fr33music.core.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Fr33MusicPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var playerRepository: PlayerRepository

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "fr33music_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)

        val forwardingPlayer = object : ForwardingPlayer(exoPlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }

            override fun isCommandAvailable(command: Int): Boolean {
                return command == COMMAND_SEEK_TO_NEXT ||
                        command == COMMAND_SEEK_TO_PREVIOUS ||
                        command == COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ||
                        command == COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM ||
                        super.isCommandAvailable(command)
            }

            override fun seekToNext() {
                serviceScope.launch { playerRepository.next() }
            }

            override fun seekToPrevious() {
                serviceScope.launch { playerRepository.previous() }
            }

            override fun seekToNextMediaItem() {
                serviceScope.launch { playerRepository.next() }
            }

            override fun seekToPreviousMediaItem() {
                serviceScope.launch { playerRepository.previous() }
            }
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val sessionBuilder = MediaSession.Builder(this, forwardingPlayer)
        if (pendingIntent != null) {
            sessionBuilder.setSessionActivity(pendingIntent)
        }

        val session = sessionBuilder.build()
        mediaSession = session
        addSession(session)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            removeSession(this)
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java) ?: return
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Reproducción de Fr33Music",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Controles de reproducción multimedia y notificación en segundo plano"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
```

---

### `core/player/src/main/res/drawable/ic_notification.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path
      android:fillColor="#FF000000"
      android:pathData="M12,3v10.55c-0.59,-0.34 -1.27,-0.55 -2,-0.55 -2.21,0 -4,1.79 -4,4s1.79,4 4,4 4,-1.79 4,-4V7h4V3h-6z"/>
</vector>
```

---

### `feature/home/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.feature.home"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:extractor"))
    implementation(project(":core:player"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

---

### `feature/home/src/main/java/com/fr33music/feature/home/HomeScreen.kt`

```kotlin
package com.fr33music.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.component.LoadingSkeleton
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(Fr33Spacing.small))
                Text(
                    text = "Fr33Music",
                    style = Fr33Typography.headlineMedium,
                    color = ColorTextPrimary
                )
            }

            IconButton(onClick = viewModel::openSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Título de recomendaciones y botón de refresco
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.recommendationTitle,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = viewModel::loadRecommendations) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar recomendaciones",
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Contenido de recomendaciones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingSkeleton(itemCount = 6)
                }

                uiState.recommendations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Fr33Spacing.extraLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ColorTextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(Fr33Spacing.large))
                        Text(
                            text = "Explora y busca música",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = "Busca tus artistas o canciones favoritas en la pestaña Buscar para generar recomendaciones personalizadas.",
                            style = Fr33Typography.bodyMedium,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.recommendations, key = { it.trackId }) { track ->
                            TrackRow(
                                track = track,
                                onClick = { viewModel.playTrack(track) }
                            )
                            HorizontalDivider(
                                color = ColorDivider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Ajustes
    if (uiState.showSettings) {
        AlertDialog(
            onDismissRequest = viewModel::closeSettings,
            containerColor = ColorSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(Fr33Spacing.small))
                    Text(text = "Ajustes", color = ColorTextPrimary, style = Fr33Typography.titleLarge)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fr33Music v1.0.0 (Fase 2)",
                        style = Fr33Typography.titleMedium,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NewPipeExtractor: v${uiState.extractorVersion}",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextSecondary
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    OutlinedButton(
                        onClick = viewModel::clearSearchHistory,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Fr33Radius.medium)
                    ) {
                        Text(text = "Limpiar historial de búsqueda", color = ColorTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.small))

                    OutlinedButton(
                        onClick = viewModel::clearAppCache,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Fr33Radius.medium)
                    ) {
                        Text(text = "Limpiar caché de red e imágenes", color = ColorTextPrimary)
                    }

                    if (uiState.cacheClearedMessage != null) {
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = uiState.cacheClearedMessage.orEmpty(),
                            style = Fr33Typography.labelMedium,
                            color = ColorPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::closeSettings) {
                    Text(text = "Cerrar", color = ColorPrimary)
                }
            }
        )
    }
}
```

---

### `feature/home/src/main/java/com/fr33music/feature/home/HomeViewModel.kt`

```kotlin
package com.fr33music.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.ExtractorInfo
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recommendationTitle: String = "Descubre nueva música",
    val recommendations: List<Track> = emptyList(),
    val showSettings: Boolean = false,
    val extractorVersion: String = ExtractorInfo.VERSION,
    val cacheClearedMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchHistoryDao: SearchHistoryDao,
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Obtener búsquedas recientes
            val recentSearches = searchHistoryDao.getRecentSearches(5).first()
            val query = if (recentSearches.isNotEmpty()) {
                recentSearches.first().query
            } else {
                "Tendencias musicales"
            }

            val title = if (recentSearches.isNotEmpty()) {
                "Recomendado por \"$query\""
            } else {
                "Tendencias destacadas"
            }

            val result = searchSource.search(query)
            result.fold(
                onSuccess = { tracks ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendationTitle = title,
                        recommendations = tracks
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendationTitle = title,
                        recommendations = emptyList()
                    )
                }
            )
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            val list = _uiState.value.recommendations
            val index = list.indexOfFirst { it.trackId == track.trackId }
            if (index >= 0) {
                playerRepository.setQueue(list, index)
            } else {
                playerRepository.play(track)
            }
        }
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true, cacheClearedMessage = null)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
            _uiState.value = _uiState.value.copy(cacheClearedMessage = "Historial de búsqueda eliminado")
            loadRecommendations()
        }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Limpiar caché de imágenes Coil
                val imageLoader = Coil.imageLoader(context)
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()

                // Limpiar caché de archivos temporales
                context.cacheDir.deleteRecursively()

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(cacheClearedMessage = "Caché de red e imágenes limpiada")
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(cacheClearedMessage = "Error al limpiar caché")
                }
            }
        }
    }
}
```

---

### `feature/library/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.feature.library"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:database"))
    implementation(project(":core:player"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Coil para imágenes
    implementation(libs.coil.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
```

---

### `feature/library/src/main/java/com/fr33music/feature/library/LibraryScreen.kt`

```kotlin
package com.fr33music.feature.library

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.Track

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var songForPlaylistDialog by remember { mutableStateOf<Track?>(null) }

    // Interceptar botón atrás físico para volver de la vista de detalle en lugar de cerrar la app
    BackHandler(enabled = uiState.selectedArtist != null) {
        viewModel.selectArtist(null)
    }
    BackHandler(enabled = uiState.selectedAlbum != null) {
        viewModel.selectAlbum(null)
    }
    BackHandler(enabled = uiState.selectedPlaylistId != null) {
        viewModel.selectPlaylist(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Text(
            text = "Biblioteca",
            style = Fr33Typography.headlineMedium,
            color = ColorTextPrimary,
            modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium)
        )

        // Chips de las 4 pestañas: Artistas, Álbumes, Playlists, Todas las canciones
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.small)
        ) {
            items(LibraryTab.entries) { tab ->
                val selected = uiState.selectedTab == tab
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTab(tab) },
                    label = { Text(text = tab.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorPrimary,
                        selectedLabelColor = ColorTextPrimary,
                        containerColor = ColorSurface,
                        labelColor = ColorTextSecondary
                    ),
                    border = null,
                    shape = RoundedCornerShape(Fr33Radius.large)
                )
            }
        }

        Spacer(modifier = Modifier.height(Fr33Spacing.medium))

        // Contenido según la pestaña seleccionada
        val context = LocalContext.current
        val onDownloadAll: (List<Track>) -> Unit = { tracksToDownload ->
            if (tracksToDownload.isNotEmpty()) {
                viewModel.downloadAll(tracksToDownload)
                Toast.makeText(
                    context,
                    "Descargando ${tracksToDownload.size} ${if (tracksToDownload.size == 1) "canción" else "canciones"}...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiState.selectedTab) {
                LibraryTab.ARTISTS -> {
                    val selectedArtist = uiState.selectedArtist
                    if (selectedArtist != null) {
                        val artistTracks = uiState.filteredTracks
                        val artistItem = uiState.artists.firstOrNull { it.name == selectedArtist }
                        ArtistDetailView(
                            artistName = selectedArtist,
                            thumbnailUrl = artistItem?.thumbnailUrl,
                            tracks = artistTracks,
                            onBack = { viewModel.selectArtist(null) },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onAddToPlaylist = { track -> songForPlaylistDialog = track }
                        )
                    } else {
                        if (uiState.artists.isEmpty()) {
                            LibraryEmptyView(
                                icon = Icons.Default.Person,
                                title = "Sin artistas",
                                subtitle = "Guarda o descarga canciones para ver a tus artistas organizados aquí."
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.artists, key = { it.name }) { artistItem ->
                                    GroupRow(
                                        title = artistItem.name,
                                        subtitle = "${artistItem.trackCount} ${if (artistItem.trackCount == 1) "canción" else "canciones"}",
                                        thumbnailUrl = artistItem.thumbnailUrl,
                                        trackId = null,
                                        fallbackIcon = Icons.Default.Person,
                                        isCircular = true,
                                        onClick = { viewModel.selectArtist(artistItem.name) }
                                    )
                                    HorizontalDivider(
                                        color = ColorDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    val selectedAlbum = uiState.selectedAlbum
                    if (selectedAlbum != null) {
                        val albumTracks = uiState.filteredTracks
                        val albumItem = uiState.albums.firstOrNull { it.title == selectedAlbum }
                        AlbumDetailView(
                            albumName = selectedAlbum,
                            thumbnailUrl = albumItem?.thumbnailUrl,
                            tracks = albumTracks,
                            onBack = { viewModel.selectAlbum(null) },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onAddToPlaylist = { track -> songForPlaylistDialog = track }
                        )
                    } else {
                        if (uiState.albums.isEmpty()) {
                            LibraryEmptyView(
                                icon = Icons.Default.Album,
                                title = "Sin álbumes",
                                subtitle = "Las canciones con información de álbum se agruparán en esta sección."
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.albums, key = { "${it.title}_${it.artistName}" }) { albumItem ->
                                    GroupRow(
                                        title = albumItem.title,
                                        subtitle = "${albumItem.artistName} • ${albumItem.trackCount} ${if (albumItem.trackCount == 1) "canción" else "canciones"}",
                                        thumbnailUrl = albumItem.thumbnailUrl,
                                        trackId = albumItem.firstTrackId,
                                        fallbackIcon = Icons.Default.Album,
                                        isCircular = false,
                                        onClick = { viewModel.selectAlbum(albumItem.title) }
                                    )
                                    HorizontalDivider(
                                        color = ColorDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 80.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                LibraryTab.PLAYLISTS -> {
                    val selectedPlaylistId = uiState.selectedPlaylistId
                    if (selectedPlaylistId != null) {
                        val playlistSongs = uiState.playlistSongs
                        PlaylistDetailView(
                            playlistName = uiState.selectedPlaylistName ?: "Playlist",
                            tracks = playlistSongs,
                            onBack = { viewModel.selectPlaylist(null) },
                            onPlayTrack = { track, list -> viewModel.playFromList(track, list) },
                            onPlayAll = { tracks -> viewModel.playAll(tracks) },
                            onDownloadAll = onDownloadAll,
                            onToggleSave = { track, isSaved -> viewModel.toggleSave(track, isSaved) },
                            onDeleteDownload = { trackId -> viewModel.deleteDownload(trackId) },
                            onRemoveFromPlaylist = { trackId ->
                                viewModel.removeSongFromPlaylist(selectedPlaylistId, trackId)
                            },
                            onDeletePlaylist = {
                                viewModel.deletePlaylist(selectedPlaylistId)
                            }
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mis Playlists (${uiState.playlists.size})",
                                    style = Fr33Typography.titleMedium,
                                    color = ColorTextSecondary
                                )
                                Button(
                                    onClick = { showCreatePlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                                    shape = RoundedCornerShape(Fr33Radius.large)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Nueva Playlist",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Nueva", style = Fr33Typography.labelMedium)
                                }
                            }

                            if (uiState.playlists.isEmpty()) {
                                LibraryEmptyView(
                                    icon = Icons.Default.QueueMusic,
                                    title = "Sin playlists",
                                    subtitle = "Toca el botón 'Nueva' para crear tu primera lista de reproducción."
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(uiState.playlists, key = { it.id }) { playlist ->
                                        PlaylistRow(
                                            playlist = playlist,
                                            onClick = { viewModel.selectPlaylist(playlist.id, playlist.name) },
                                            onDelete = { viewModel.deletePlaylist(playlist.id) }
                                        )
                                        HorizontalDivider(
                                            color = ColorDivider,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(start = 80.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LibraryTab.ALL_SONGS -> {
                    if (uiState.allSongs.isEmpty()) {
                        LibraryEmptyView(
                            icon = Icons.Default.MusicNote,
                            title = "Biblioteca vacía",
                            subtitle = "Guarda canciones con el corazón o descárgalas para verlas todas aquí."
                        )
                    } else {
                        val allTracksDomain = uiState.allSongs.map { it.toDomain() }
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.allSongs.size} canciones (A - Z)",
                                    style = Fr33Typography.bodyMedium,
                                    color = ColorTextSecondary
                                )
                                Button(
                                    onClick = { viewModel.playAll(allTracksDomain) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                                    shape = RoundedCornerShape(Fr33Radius.large)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Reproducir todo",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Reproducir todo", style = Fr33Typography.labelMedium)
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(uiState.allSongs, key = { it.id }) { trackEntity ->
                                    val domain = trackEntity.toDomain()
                                    TrackRow(
                                        track = domain,
                                        onClick = { viewModel.playFromList(domain, allTracksDomain) },
                                        trailingContent = {
                                            TrackActionButtons(
                                                isSaved = trackEntity.isSaved,
                                                isDownloaded = trackEntity.isDownloaded,
                                                onToggleSave = { viewModel.toggleSave(domain, trackEntity.isSaved) },
                                                onDeleteDownload = { viewModel.deleteDownload(trackEntity.id) },
                                                onAddToPlaylist = { songForPlaylistDialog = domain }
                                            )
                                        }
                                    )
                                    HorizontalDivider(
                                        color = ColorDivider,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 72.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear nueva playlist
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }

    // Diálogo para añadir canción a una playlist
    if (songForPlaylistDialog != null) {
        val targetTrack = songForPlaylistDialog!!
        AddToPlaylistDialog(
            track = targetTrack,
            playlists = uiState.playlists,
            onDismiss = { songForPlaylistDialog = null },
            onSelectPlaylist = { playlistId ->
                viewModel.addSongToPlaylist(playlistId, targetTrack)
                songForPlaylistDialog = null
            },
            onCreateNewPlaylist = {
                showCreatePlaylistDialog = true
            }
        )
    }
}

@Composable
private fun TrackActionButtons(
    isSaved: Boolean,
    isDownloaded: Boolean,
    onToggleSave: () -> Unit,
    onDeleteDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    isInsidePlaylist: Boolean = false,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Corazón (Favorito)
        IconButton(onClick = onToggleSave) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isSaved) "Quitar de favoritos" else "Guardar en favoritos",
                tint = if (isSaved) ColorPrimary else ColorTextTertiary
            )
        }

        // Bote de basura (solo si está descargada físicamente)
        if (isDownloaded) {
            IconButton(onClick = onDeleteDownload) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar descarga",
                    tint = ColorTextTertiary
                )
            }
        }

        if (isInsidePlaylist) {
            // Quitar de playlist (X)
            if (onRemoveFromPlaylist != null) {
                IconButton(onClick = onRemoveFromPlaylist) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Quitar de playlist",
                        tint = ColorTextTertiary
                    )
                }
            }
        } else {
            // Añadir a playlist (+)
            IconButton(onClick = onAddToPlaylist) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Añadir a playlist",
                    tint = ColorTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistWithSongCount,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = playlist.thumbnailUrl,
            contentDescription = playlist.name,
            size = 56.dp,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = Icons.AutoMirrored.Filled.QueueMusic
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songCount} ${if (playlist.songCount == 1) "canción" else "canciones"}",
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar playlist",
                tint = ColorTextTertiary
            )
        }
    }
}

@Composable
private fun PlaylistDetailView(
    playlistName: String,
    tracks: List<TrackEntity>,
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onRemoveFromPlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    val domainTracks = tracks.map { it.toDomain() }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }

            Text(
                text = playlistName,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (domainTracks.isNotEmpty()) {
                Button(
                    onClick = { onPlayAll(domainTracks) },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    shape = RoundedCornerShape(Fr33Radius.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir todo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reproducir", style = Fr33Typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(Fr33Spacing.small))

                OutlinedButton(
                    onClick = { onDownloadAll(domainTracks) },
                    shape = RoundedCornerShape(Fr33Radius.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar todo",
                        modifier = Modifier.size(18.dp),
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Descargar", style = Fr33Typography.labelMedium, color = ColorPrimary)
                }
            }

            IconButton(onClick = onDeletePlaylist) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar playlist",
                    tint = ColorTextTertiary
                )
            }
        }

        if (tracks.isEmpty()) {
            LibraryEmptyView(
                icon = Icons.Default.MusicNote,
                title = "Playlist vacía",
                subtitle = "Usa el icono '+' en cualquier canción para añadirla a esta lista."
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(tracks, key = { it.id }) { trackEntity ->
                    val domain = trackEntity.toDomain()
                    TrackRow(
                        track = domain,
                        onClick = { onPlayTrack(domain, domainTracks) },
                        trailingContent = {
                            TrackActionButtons(
                                isSaved = trackEntity.isSaved,
                                isDownloaded = trackEntity.isDownloaded,
                                onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                                onDeleteDownload = { onDeleteDownload(trackEntity.id) },
                                onAddToPlaylist = {},
                                isInsidePlaylist = true,
                                onRemoveFromPlaylist = { onRemoveFromPlaylist(trackEntity.id) }
                            )
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupRow(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trackId: String? = null,
    isCircular: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackThumbnail(
            thumbnailUrl = thumbnailUrl,
            trackId = trackId,
            contentDescription = title,
            size = 56.dp,
            isCircular = isCircular,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = fallbackIcon
        )

        Spacer(modifier = Modifier.width(Fr33Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Fr33Typography.titleMedium,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = Fr33Typography.bodyMedium,
                color = ColorTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ArtistDetailView(
    artistName: String,
    thumbnailUrl: String?,
    tracks: List<TrackEntity>,
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    val domainTracks = tracks.map { it.toDomain() }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }

            TrackThumbnail(
                thumbnailUrl = thumbnailUrl,
                contentDescription = artistName,
                size = 40.dp,
                isCircular = true,
                fallbackIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.width(Fr33Spacing.small))

            Text(
                text = artistName,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (domainTracks.isNotEmpty()) {
                OutlinedButton(
                    onClick = { onDownloadAll(domainTracks) },
                    shape = RoundedCornerShape(Fr33Radius.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar todo",
                        modifier = Modifier.size(18.dp),
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Descargar Todo", style = Fr33Typography.labelMedium, color = ColorPrimary)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(tracks, key = { it.id }) { trackEntity ->
                val domain = trackEntity.toDomain()
                TrackRow(
                    track = domain,
                    onClick = { onPlayTrack(domain, domainTracks) },
                    trailingContent = {
                        TrackActionButtons(
                            isSaved = trackEntity.isSaved,
                            isDownloaded = trackEntity.isDownloaded,
                            onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                            onDeleteDownload = { onDeleteDownload(trackEntity.id) },
                            onAddToPlaylist = { onAddToPlaylist(domain) }
                        )
                    }
                )
                HorizontalDivider(
                    color = ColorDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    albumName: String,
    thumbnailUrl: String?,
    tracks: List<TrackEntity>,
    onBack: () -> Unit,
    onPlayTrack: (Track, List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track, Boolean) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onAddToPlaylist: (Track) -> Unit
) {
    val domainTracks = tracks.map { it.toDomain() }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }

            TrackThumbnail(
                thumbnailUrl = thumbnailUrl,
                contentDescription = albumName,
                size = 40.dp,
                shapeRadius = Fr33Radius.small,
                fallbackIcon = Icons.Default.Album
            )
            Spacer(modifier = Modifier.width(Fr33Spacing.small))

            Text(
                text = albumName,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (domainTracks.isNotEmpty()) {
                Button(
                    onClick = { onPlayAll(domainTracks) },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                    shape = RoundedCornerShape(Fr33Radius.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir todo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Reproducir", style = Fr33Typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(Fr33Spacing.small))

                OutlinedButton(
                    onClick = { onDownloadAll(domainTracks) },
                    shape = RoundedCornerShape(Fr33Radius.large)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar todo",
                        modifier = Modifier.size(18.dp),
                        tint = ColorPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Descargar", style = Fr33Typography.labelMedium, color = ColorPrimary)
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(tracks, key = { it.id }) { trackEntity ->
                val domain = trackEntity.toDomain()
                TrackRow(
                    track = domain,
                    onClick = { onPlayTrack(domain, domainTracks) },
                    trailingContent = {
                        TrackActionButtons(
                            isSaved = trackEntity.isSaved,
                            isDownloaded = trackEntity.isDownloaded,
                            onToggleSave = { onToggleSave(domain, trackEntity.isSaved) },
                            onDeleteDownload = { onDeleteDownload(trackEntity.id) },
                            onAddToPlaylist = { onAddToPlaylist(domain) }
                        )
                    }
                )
                HorizontalDivider(
                    color = ColorDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nueva Playlist", color = ColorTextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Crear", color = ColorPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextSecondary)
            }
        },
        containerColor = ColorSurface
    )
}

@Composable
private fun AddToPlaylistDialog(
    track: Track,
    playlists: List<PlaylistWithSongCount>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Añadir a Playlist", color = ColorTextPrimary) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${track.title} - ${track.artistName}",
                    style = Fr33Typography.bodySmall,
                    color = ColorTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No tienes playlists creadas todavía.",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(playlists, key = { it.id }) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPlaylist(playlist.id) }
                                    .padding(vertical = Fr33Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(Fr33Spacing.medium))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        style = Fr33Typography.titleMedium,
                                        color = ColorTextPrimary
                                    )
                                    Text(
                                        text = "${playlist.songCount} canciones",
                                        style = Fr33Typography.bodySmall,
                                        color = ColorTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onCreateNewPlaylist()
                }
            ) {
                Text("+ Nueva Playlist", color = ColorPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = ColorTextSecondary)
            }
        },
        containerColor = ColorSurface
    )
}

@Composable
private fun LibraryEmptyView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = title,
            style = Fr33Typography.titleLarge,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = subtitle,
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}
```

---

### `feature/library/src/main/java/com/fr33music/feature/library/LibraryViewModel.kt`

```kotlin
package com.fr33music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.AlbumDao
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.entity.AlbumEntity
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.Track
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    ARTISTS("Artistas"),
    ALBUMS("Álbumes"),
    PLAYLISTS("Playlists"),
    ALL_SONGS("Todas las canciones")
}

data class ArtistItem(
    val name: String,
    val trackCount: Int,
    val thumbnailUrl: String?,
    val firstTrackId: String? = null
)

data class AlbumItem(
    val title: String,
    val artistName: String,
    val trackCount: Int,
    val thumbnailUrl: String?,
    val firstTrackId: String? = null
)

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.ARTISTS,
    val artists: List<ArtistItem> = emptyList(),
    val albums: List<AlbumItem> = emptyList(),
    val playlists: List<PlaylistWithSongCount> = emptyList(),
    val allSongs: List<TrackEntity> = emptyList(),
    val selectedArtist: String? = null,
    val selectedAlbum: String? = null,
    val selectedPlaylistId: Long? = null,
    val selectedPlaylistName: String? = null,
    val playlistSongs: List<TrackEntity> = emptyList(),
    val filteredTracks: List<TrackEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val downloadRepository: DownloadRepository,
    private val playerRepository: PlayerRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.ARTISTS)
    private val _selectedArtist = MutableStateFlow<String?>(null)
    private val _selectedAlbum = MutableStateFlow<String?>(null)
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    private val _selectedPlaylistName = MutableStateFlow<String?>(null)

    private data class FilterSelection(
        val tab: LibraryTab = LibraryTab.ARTISTS,
        val artist: String? = null,
        val album: String? = null,
        val playlistId: Long? = null,
        val playlistName: String? = null
    )

    private val filterSelection = combine(
        _selectedTab,
        _selectedArtist,
        _selectedAlbum,
        _selectedPlaylistId,
        _selectedPlaylistName
    ) { tab, artist, album, playlistId, playlistName ->
        FilterSelection(tab, artist, album, playlistId, playlistName)
    }

    private data class LibraryTracks(
        val saved: List<TrackEntity>,
        val downloaded: List<TrackEntity>
    )

    private data class LibraryEntities(
        val artists: List<ArtistEntity>,
        val albums: List<AlbumEntity>
    )

    private val tracksFlow = combine(
        trackDao.getSavedTracks(),
        trackDao.getDownloadedTracks()
    ) { saved, downloaded ->
        LibraryTracks(saved, downloaded)
    }

    private val entitiesFlow = combine(
        artistDao.getAllArtists(),
        albumDao.getAllAlbums()
    ) { artists, albums ->
        LibraryEntities(artists, albums)
    }

    private val playlistSongsFlow = _selectedPlaylistId.flatMapLatest { playlistId ->
        if (playlistId != null) {
            libraryRepository.getSongsForPlaylist(playlistId)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        filterSelection,
        tracksFlow,
        entitiesFlow,
        libraryRepository.getPlaylists(),
        playlistSongsFlow
    ) { filter, tracks, entities, playlistsList, playlistSongs ->
        val saved = tracks.saved
        val downloads = tracks.downloaded
        val allLibraryTracks = (saved + downloads).distinctBy { it.id }
        val artistEntities = entities.artists
        val albumEntities = entities.albums

        // Lista de "Todas las canciones": ordenadas alfabéticamente por título
        val allSongsSorted = allLibraryTracks.sortedBy { it.title.lowercase() }

        // Agrupación por artista con fallback de miniatura y trackId
        val artistItems = allLibraryTracks
            .groupBy { it.artistName }
            .map { (artistName, trackList) ->
                val firstTrack = trackList.first()
                val entity = artistEntities.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
                val thumb = entity?.thumbnailUrl
                    ?: trackList.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
                ArtistItem(
                    name = artistName,
                    trackCount = trackList.size,
                    thumbnailUrl = thumb,
                    firstTrackId = null
                )
            }
            .sortedBy { it.name.lowercase() }

        // Agrupación por álbum usando (albumTitle, artistName) para separar "Sencillos" y álbumes reales
        val albumItems = allLibraryTracks
            .groupBy {
                val isReal = !it.albumName.isNullOrBlank() &&
                        !it.albumName.equals(it.artistName, ignoreCase = true) &&
                        !it.albumName.equals("Sencillos", ignoreCase = true)
                val albumTitle = if (isReal) it.albumName!! else "Sencillos"
                Pair(albumTitle, it.artistName)
            }
            .map { (pair, trackList) ->
                val (albumTitle, artistName) = pair
                val firstTrack = trackList.first()
                val albumId = "${artistName}_$albumTitle"
                val entity = albumEntities.firstOrNull { it.id == albumId }
                    ?: albumEntities.firstOrNull { it.title.equals(albumTitle, ignoreCase = true) && it.artistName.equals(artistName, ignoreCase = true) }
                val thumb = entity?.thumbnailUrl
                    ?: trackList.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
                AlbumItem(
                    title = albumTitle,
                    artistName = artistName,
                    trackCount = trackList.size,
                    thumbnailUrl = thumb,
                    firstTrackId = firstTrack.id
                )
            }
            .sortedBy { it.title.lowercase() }

        val filtered = when {
            filter.artist != null -> allLibraryTracks.filter { it.artistName.equals(filter.artist, ignoreCase = true) }
            filter.album != null -> allLibraryTracks.filter {
                val isReal = !it.albumName.isNullOrBlank() &&
                        !it.albumName.equals(it.artistName, ignoreCase = true) &&
                        !it.albumName.equals("Sencillos", ignoreCase = true)
                val albumTitle = if (isReal) it.albumName!! else "Sencillos"
                albumTitle.equals(filter.album, ignoreCase = true)
            }
            else -> emptyList()
        }

        LibraryUiState(
            selectedTab = filter.tab,
            artists = artistItems,
            albums = albumItems,
            playlists = playlistsList,
            allSongs = allSongsSorted,
            selectedArtist = filter.artist,
            selectedAlbum = filter.album,
            selectedPlaylistId = filter.playlistId,
            selectedPlaylistName = filter.playlistName,
            playlistSongs = playlistSongs,
            filteredTracks = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
        _selectedArtist.value = null
        _selectedAlbum.value = null
        _selectedPlaylistId.value = null
        _selectedPlaylistName.value = null
    }

    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    fun selectAlbum(album: String?) {
        _selectedAlbum.value = album
    }

    fun selectPlaylist(playlistId: Long?, playlistName: String? = null) {
        _selectedPlaylistId.value = playlistId
        _selectedPlaylistName.value = playlistName
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            libraryRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            if (_selectedPlaylistId.value == id) {
                _selectedPlaylistId.value = null
                _selectedPlaylistName.value = null
            }
            libraryRepository.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            libraryRepository.addSongToPlaylist(playlistId, track)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            libraryRepository.removeSongFromPlaylist(playlistId, trackId)
        }
    }

    fun playFromList(track: Track, list: List<Track>) {
        if (list.isEmpty()) return
        val startIndex = list.indexOfFirst { it.trackId == track.trackId }.coerceAtLeast(0)
        viewModelScope.launch {
            playerRepository.setQueue(list, startIndex)
        }
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, tracks.lastIndex)
        viewModelScope.launch {
            playerRepository.setQueue(tracks, validIndex)
        }
    }

    fun toggleSave(track: Track, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            libraryRepository.toggleSave(track, isCurrentlySaved)
        }
    }

    fun deleteDownload(trackId: String) {
        viewModelScope.launch {
            libraryRepository.deleteDownload(trackId)
        }
    }

    fun downloadAll(tracks: List<Track>) {
        viewModelScope.launch {
            tracks.forEach { track ->
                downloadRepository.download(track)
            }
        }
    }
}
```

---

### `feature/player/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.feature.player"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:player"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

---

### `feature/player/src/main/java/com/fr33music/feature/player/PlayerScreen.kt`

```kotlin
package com.fr33music.feature.player

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.database.download.DownloadState
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorError
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorSurfaceVariant
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val track = uiState.track

    BackHandler(enabled = true) {
        if (uiState.showLyricsSheet) {
            viewModel.closeLyrics()
        } else {
            onCollapse()
        }
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableFloatStateOf(0f) }

    // Contenedor raíz con fondo sólido 0xFF030303 que consume todos los toques para evitar click-through
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .pointerInput(Unit) {
                detectTapGestures {
                    // Consumir el toque
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Down arrow button to collapse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimizar",
                        tint = ColorTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Large Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.large)
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            // Consumir el toque en la carátula
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                TrackThumbnail(
                    thumbnailUrl = track?.thumbnailUrl,
                    trackId = track?.trackId,
                    contentDescription = track?.title,
                    size = 320.dp,
                    shapeRadius = Fr33Radius.large,
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ColorBackground.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ColorPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.large))

            // Title, Artist y Botón de Favorito / Guardar en Biblioteca (Corazón)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fr33Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "Sin reproducción",
                        style = Fr33Typography.headlineMedium,
                        color = ColorTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                    Text(
                        text = track?.artistName ?: "Selecciona una canción",
                        style = Fr33Typography.bodyLarge,
                        color = ColorTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Botón Guardar en Biblioteca (Corazón)
                IconButton(
                    onClick = viewModel::toggleSave,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = if (uiState.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (uiState.isSaved) "Guardado en biblioteca" else "Guardar en biblioteca",
                        tint = if (uiState.isSaved) ColorPrimary else ColorTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(Fr33Spacing.small))
                Text(
                    text = uiState.error.orEmpty(),
                    style = Fr33Typography.bodyMedium,
                    color = ColorError,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Position Slider & Timers
            val hasValidDuration = uiState.durationMs > 0L
            val totalDuration = if (hasValidDuration) uiState.durationMs.toFloat() else 1f
            val currentPosition = if (isDraggingSlider) {
                dragPositionMs
            } else if (hasValidDuration) {
                uiState.positionMs.toFloat().coerceIn(0f, totalDuration)
            } else {
                0f
            }

            Slider(
                value = if (hasValidDuration) currentPosition.coerceIn(0f, totalDuration) else 0f,
                onValueChange = { newValue ->
                    if (hasValidDuration) {
                        isDraggingSlider = true
                        dragPositionMs = newValue
                    }
                },
                onValueChangeFinished = {
                    if (hasValidDuration) {
                        viewModel.seekTo(dragPositionMs.toLong())
                        isDraggingSlider = false
                    }
                },
                enabled = hasValidDuration,
                valueRange = 0f..totalDuration,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = ColorPrimary,
                    activeTrackColor = ColorPrimary,
                    inactiveTrackColor = ColorSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosition.toLong()),
                    style = Fr33Typography.labelSmall,
                    color = ColorTextTertiary
                )
                Text(
                    text = formatDuration(uiState.durationMs),
                    style = Fr33Typography.labelSmall,
                    color = ColorTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle (Aleatorio sin repetir hasta agotar cola)
                IconButton(
                    onClick = viewModel::toggleShuffle,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (uiState.isShuffle) ColorPrimary else ColorTextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = viewModel::previous,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Anterior",
                        tint = if (track != null) ColorTextPrimary else ColorTextTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play / Pause (Large Primary Button)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = viewModel::togglePlayPause,
                        enabled = track != null,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pausar" else "Reproducir",
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = viewModel::next,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Siguiente",
                        tint = if (track != null) ColorTextPrimary else ColorTextTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Repeat (disabled placeholder)
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repetir",
                        tint = ColorTextTertiary.copy(alpha = 0.38f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Fr33Spacing.medium))

            // Fila de acciones secundarias: Descargar y Letras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Descargar
                val isDownloaded = downloadState is DownloadState.Completed || uiState.isDownloaded
                val isDownloading = downloadState is DownloadState.Downloading || downloadState is DownloadState.Queued
                val isError = downloadState is DownloadState.Error
                val canDownload = track != null && !isDownloaded && !isDownloading

                IconButton(
                    onClick = viewModel::downloadCurrentTrack,
                    enabled = canDownload
                ) {
                    when {
                        isDownloaded -> {
                            Icon(
                                imageVector = Icons.Default.DownloadDone,
                                contentDescription = "Descargado offline",
                                tint = ColorPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        isDownloading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ColorPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        isError -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Error al descargar. Toca para reintentar.",
                                tint = ColorPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Descargar audio OPUS",
                                tint = if (track != null) ColorTextSecondary else ColorTextTertiary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Botón Letras
                IconButton(
                    onClick = viewModel::openLyrics,
                    enabled = track != null
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Article,
                        contentDescription = "Ver letras",
                        tint = if (track != null) ColorTextSecondary else ColorTextTertiary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ModalBottomSheet para mostrar la letra de la canción
        if (uiState.showLyricsSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::closeLyrics,
                sheetState = sheetState,
                containerColor = ColorSurface,
                shape = RoundedCornerShape(topStart = Fr33Radius.large, topEnd = Fr33Radius.large)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Letra",
                        style = Fr33Typography.headlineSmall,
                        color = ColorTextPrimary
                    )
                    Text(
                        text = "${track?.title} — ${track?.artistName}",
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    if (uiState.isLoadingLyrics) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ColorPrimary)
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Text(
                            text = uiState.lyrics ?: "Letra no disponible",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .verticalScroll(scrollState)
                                .padding(vertical = Fr33Spacing.medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
```

---

### `feature/player/src/main/java/com/fr33music/feature/player/PlayerUiState.kt`

```kotlin
package com.fr33music.feature.player

import com.fr33music.core.domain.model.Track

data class PlayerUiState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isShuffle: Boolean = false,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadStatus: String? = null,
    val lyrics: String? = null,
    val isLoadingLyrics: Boolean = false,
    val showLyricsSheet: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)
```

---

### `feature/player/src/main/java/com/fr33music/feature/player/PlayerViewModel.kt`

```kotlin
package com.fr33music.feature.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.download.DownloadState
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.lyrics.LyricsRepository
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val trackDao: TrackDao,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    private val showLyricsSheet = MutableStateFlow(false)
    private val lyricsText = MutableStateFlow<String?>(null)
    private val isLoadingLyrics = MutableStateFlow(false)

    // ID de la pista actual desacoplado del progreso para evitar re-subscripciones constantes
    private val currentTrackId = playerRepository.playbackState
        .map { it.currentTrack?.trackId }
        .distinctUntilChanged()

    // Observar si la pista actual está guardada o descargada en Room
    private val currentTrackEntity = currentTrackId
        .flatMapLatest { id ->
            if (id != null) trackDao.getTrack(id) else flowOf(null)
        }

    // Observar estado de descarga reactivo independiente de play/pause
    val downloadState: StateFlow<DownloadState> = currentTrackId
        .flatMapLatest { id ->
            if (id != null) {
                downloadRepository.getDownloadState(id)
            } else {
                flowOf(DownloadState.Idle)
            }
        }
        .onEach { state ->
            Log.d("Descarga", "Estado actual: $state")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadState.Idle
        )

    private data class LyricsUi(
        val showSheet: Boolean = false,
        val text: String? = null,
        val isLoading: Boolean = false
    )

    private val lyricsUi = combine(
        showLyricsSheet,
        lyricsText,
        isLoadingLyrics
    ) { show, text, loading ->
        LyricsUi(show, text, loading)
    }

    val uiState: StateFlow<PlayerUiState> = combine(
        playerRepository.playbackState,
        currentTrackEntity,
        downloadState,
        lyricsUi
    ) { playback, trackEntity, dlState, lyrics ->
        val track = playback.currentTrack
        val isDownloaded = trackEntity?.isDownloaded == true || dlState is DownloadState.Completed
        val downloadStatusStr = when (dlState) {
            DownloadState.Queued -> DownloadStatus.QUEUED
            is DownloadState.Downloading -> DownloadStatus.DOWNLOADING
            is DownloadState.Completed -> DownloadStatus.COMPLETED
            is DownloadState.Error -> DownloadStatus.ERROR
            DownloadState.Idle -> null
        }
        PlayerUiState(
            track = track,
            isPlaying = playback.isPlaying,
            isLoading = playback.isLoading,
            isShuffle = playback.isShuffle,
            isSaved = trackEntity?.isSaved == true,
            isDownloaded = isDownloaded,
            downloadStatus = downloadStatusStr,
            lyrics = lyrics.text,
            isLoadingLyrics = lyrics.isLoading,
            showLyricsSheet = lyrics.showSheet,
            positionMs = playback.positionMs,
            durationMs = if (playback.durationMs > 0L) playback.durationMs else (track?.durationMs ?: 0L),
            error = playback.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun togglePlayPause() {
        playerRepository.togglePlayPause()
    }

    fun toggleShuffle() {
        playerRepository.toggleShuffle()
    }

    fun next() {
        viewModelScope.launch {
            playerRepository.next()
        }
    }

    fun previous() {
        viewModelScope.launch {
            playerRepository.previous()
        }
    }

    fun seekTo(positionMs: Long) {
        playerRepository.seekTo(positionMs)
    }

    fun toggleSave() {
        val currentTrack = uiState.value.track ?: return
        val currentlySaved = uiState.value.isSaved
        viewModelScope.launch {
            libraryRepository.toggleSave(currentTrack, currentlySaved)
        }
    }

    fun downloadCurrentTrack() {
        val currentTrack = uiState.value.track ?: return
        viewModelScope.launch {
            downloadRepository.download(currentTrack)
        }
    }

    fun openLyrics() {
        val currentTrack = uiState.value.track ?: return
        showLyricsSheet.value = true
        loadLyrics(currentTrack)
    }

    fun closeLyrics() {
        showLyricsSheet.value = false
    }

    private fun loadLyrics(track: com.fr33music.core.domain.model.Track) {
        viewModelScope.launch {
            isLoadingLyrics.value = true
            lyricsText.value = lyricsRepository.getLyrics(track)
            isLoadingLyrics.value = false
        }
    }
}
```

---

### `feature/search/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.fr33music.feature.search"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:extractor"))
    implementation(project(":core:player"))

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coil.compose)
}
```

---

### `feature/search/src/main/java/com/fr33music/feature/search/SearchScreen.kt`

```kotlin
package com.fr33music.feature.search

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fr33music.core.designsystem.component.ErrorView
import com.fr33music.core.designsystem.component.LoadingSkeleton
import com.fr33music.core.designsystem.component.TrackRow
import com.fr33music.core.designsystem.component.TrackThumbnail
import com.fr33music.core.designsystem.theme.ColorBackground
import com.fr33music.core.designsystem.theme.ColorDivider
import com.fr33music.core.designsystem.theme.ColorPrimary
import com.fr33music.core.designsystem.theme.ColorSurface
import com.fr33music.core.designsystem.theme.ColorTextPrimary
import com.fr33music.core.designsystem.theme.ColorTextSecondary
import com.fr33music.core.designsystem.theme.ColorTextTertiary
import com.fr33music.core.designsystem.theme.Fr33Radius
import com.fr33music.core.designsystem.theme.Fr33Spacing
import com.fr33music.core.designsystem.theme.Fr33Typography
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

@Composable
fun SearchScreen(
    onTrackSelected: (Track) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedTrackIds by viewModel.savedTrackIds.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val albumTracks by viewModel.albumTracks.collectAsStateWithLifecycle()
    val isLoadingAlbum by viewModel.isLoadingAlbum.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val channelVideos by viewModel.channelVideos.collectAsStateWithLifecycle()
    val isLoadingChannel by viewModel.isLoadingChannel.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val textToolbar = LocalTextToolbar.current
    var isSearchFocused by remember { mutableStateOf(false) }

    fun clearSearchFocus() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        textToolbar?.hide()
    }

    val context = LocalContext.current
    val onDownloadAll: (List<Track>) -> Unit = { tracksToDownload ->
        if (tracksToDownload.isNotEmpty()) {
            viewModel.downloadAll(tracksToDownload)
            Toast.makeText(
                context,
                "Descargando ${tracksToDownload.size} ${if (tracksToDownload.size == 1) "canción" else "canciones"}...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Limpia el foco y oculta el teclado si el usuario presiona Atrás mientras busca
    BackHandler(enabled = isSearchFocused) {
        clearSearchFocus()
    }

    // Si está en la pantalla de detalle de canal, volver a la búsqueda al pulsar atrás
    if (selectedChannel != null) {
        val currentChannel = selectedChannel!!
        SearchChannelDetailView(
            channel = currentChannel,
            videos = channelVideos,
            isLoading = isLoadingChannel,
            savedTrackIds = savedTrackIds,
            onBack = { viewModel.selectChannel(null) },
            onTrackClick = { track ->
                viewModel.onTrackClick(track)
                onTrackSelected(track)
            },
            onToggleSave = viewModel::toggleSave
        )
        return
    }

    // Si está en la pantalla de detalle de álbum/playlist, volver a la búsqueda al pulsar atrás
    if (selectedAlbum != null) {
        val currentAlbum = selectedAlbum!!
        SearchAlbumDetailView(
            album = currentAlbum,
            tracks = albumTracks,
            isLoading = isLoadingAlbum,
            savedTrackIds = savedTrackIds,
            initialScrollIndex = viewModel.albumScrollIndex,
            initialScrollOffset = viewModel.albumScrollOffset,
            onSaveState = { index, offset ->
                viewModel.albumScrollIndex = index
                viewModel.albumScrollOffset = offset
            },
            onBack = {
                viewModel.albumScrollIndex = 0
                viewModel.albumScrollOffset = 0
                viewModel.selectAlbum(null)
            },
            onTrackClick = { track ->
                viewModel.onTrackClick(track)
                onTrackSelected(track)
            },
            onPlayAll = { tracks, shuffle ->
                if (tracks.isNotEmpty()) {
                    viewModel.playAll(tracks, startIndex = 0, shuffle = shuffle)
                    onTrackSelected(tracks.first())
                }
            },
            onDownloadAll = onDownloadAll,
            onToggleSave = viewModel::toggleSave
        )
        return
    }

    // Limpia el foco si la pantalla deja de estar activa
    DisposableEffect(Unit) {
        onDispose {
            clearSearchFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        clearSearchFocus()
                    }
                )
            }
    ) {
        // Search Input Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.medium)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchFocused = it.isFocused },
                placeholder = {
                    Text(
                        text = "Buscar canciones, artistas o canales...",
                        style = Fr33Typography.bodyLarge,
                        color = ColorTextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = ColorTextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = ColorTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Fr33Radius.large),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorSurface,
                    unfocusedContainerColor = ColorSurface,
                    focusedTextColor = ColorTextPrimary,
                    unfocusedTextColor = ColorTextPrimary,
                    focusedBorderColor = ColorPrimary,
                    unfocusedBorderColor = ColorDivider,
                    cursorColor = ColorPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        clearSearchFocus()
                        viewModel.search()
                    }
                )
            )
        }

        // Search Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    SearchIdleView()
                }
                is SearchUiState.Loading -> {
                    LoadingSkeleton(itemCount = 8)
                }
                is SearchUiState.LookingSuccess -> {
                    LookingResultsView(
                        result = state.result,
                        savedTrackIds = savedTrackIds,
                        onChannelClick = { channel ->
                            clearSearchFocus()
                            viewModel.selectChannel(channel)
                        },
                        onPlaylistClick = { playlist ->
                            clearSearchFocus()
                            viewModel.selectAlbum(playlist)
                        },
                        onTrackClick = { track ->
                            clearSearchFocus()
                            viewModel.onTrackClick(track)
                            onTrackSelected(track)
                        },
                        onToggleSave = viewModel::toggleSave
                    )
                }
                is SearchUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.tracks, key = { it.trackId }) { track ->
                            TrackRow(
                                track = track,
                                onClick = {
                                    clearSearchFocus()
                                    viewModel.onTrackClick(track)
                                    onTrackSelected(track)
                                },
                                trailingContent = {
                                    val isSaved = savedTrackIds.contains(track.trackId)
                                    IconButton(onClick = { viewModel.toggleSave(track) }) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (isSaved) "Guardado" else "Guardar",
                                            tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = ColorDivider,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
                is SearchUiState.Empty -> {
                    SearchEmptyView(query = query)
                }
                is SearchUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

/**
 * Vista de resultados de búsqueda:
 * 1. Canales en la parte superior en un LazyRow (círculos con foto y nombre)
 * 2. Listas de reproducción en un LazyRow con formato de tarjeta
 * 3. Vídeos / canciones en una lista vertical
 */
@Composable
private fun LookingResultsView(
    result: LookingSearchResult,
    savedTrackIds: Set<String>,
    onChannelClick: (ChannelCard) -> Unit,
    onPlaylistClick: (AlbumCard) -> Unit,
    onTrackClick: (Track) -> Unit,
    onToggleSave: (Track) -> Unit
) {
    if (result.channels.isEmpty() && result.playlists.isEmpty() && result.videos.isEmpty()) {
        SearchEmptyView(query = "")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
    ) {
        // 1. Canales (Bolitas en LazyRow horizontal)
        if (result.channels.isNotEmpty()) {
            item {
                Text(
                    text = "Canales",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.large)
                ) {
                    itemsIndexed(
                        items = result.channels,
                        key = { index, channel -> "${channel.channelId}_$index" }
                    ) { _, channel ->
                        ChannelCardItem(
                            channel = channel,
                            onClick = { onChannelClick(channel) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Fr33Spacing.large))
            }
        }

        // 2. Listas de reproducción (Tarjetas en LazyRow horizontal)
        if (result.playlists.isNotEmpty()) {
            item {
                Text(
                    text = "Listas de reproducción",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Fr33Spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.medium)
                ) {
                    itemsIndexed(
                        items = result.playlists,
                        key = { index, playlist -> "${playlist.url?.ifBlank { playlist.title } ?: playlist.title}_$index" }
                    ) { _, playlist ->
                        AlbumCardItem(
                            album = playlist,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Fr33Spacing.large))
            }
        }

        // 3. Vídeos (Vertical en LazyColumn)
        if (result.videos.isNotEmpty()) {
            item {
                Text(
                    text = "Vídeos",
                    style = Fr33Typography.titleMedium,
                    color = ColorTextPrimary,
                    modifier = Modifier.padding(horizontal = Fr33Spacing.large, vertical = Fr33Spacing.small)
                )
            }

            itemsIndexed(
                items = result.videos,
                key = { index, track -> "${track.trackId}_$index" }
            ) { _, track ->
                TrackRow(
                    track = track,
                    onClick = { onTrackClick(track) },
                    trailingContent = {
                        val isSaved = savedTrackIds.contains(track.trackId)
                        IconButton(onClick = { onToggleSave(track) }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isSaved) "Guardado" else "Guardar",
                                tint = if (isSaved) ColorPrimary else ColorTextSecondary
                            )
                        }
                    }
                )
                HorizontalDivider(
                    color = ColorDivider,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

/**
 * Tarjeta individual para canales en el LazyRow (círculo con avatar y nombre debajo)
 */
@Composable
private fun ChannelCardItem(
    channel: ChannelCard,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrackThumbnail(
            thumbnailUrl = channel.avatarUrl,
            contentDescription = channel.name,
            size = 80.dp,
            isCircular = true,
            fallbackIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = channel.name.ifBlank { "Canal" },
            style = Fr33Typography.bodyMedium,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        val subText = if (channel.subscriberCount > 0) {
            formatSubscriberCount(channel.subscriberCount)
        } else {
            "Canal"
        }
        Text(
            text = subText,
            style = Fr33Typography.bodySmall,
            color = ColorTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tarjeta individual para listas de reproducción en el LazyRow
 */
@Composable
private fun AlbumCardItem(
    album: AlbumCard,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        TrackThumbnail(
            thumbnailUrl = album.thumbnailUrl,
            contentDescription = album.title,
            size = 130.dp,
            shapeRadius = Fr33Radius.medium,
            fallbackIcon = Icons.Default.Album
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = album.title.ifBlank { "Lista de reproducción" },
            style = Fr33Typography.bodyMedium,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = when {
                album.songCount > 1L -> "${album.songCount} temas • ${album.artistName}"
                album.songCount == 1L -> "1 tema • ${album.artistName}"
                album.artistName.isNotBlank() -> album.artistName
                else -> "Lista de reproducción"
            },
            style = Fr33Typography.bodySmall,
            color = ColorTextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatSubscriberCount(count: Long): String {
    return try {
        when {
            count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM subs", count / 1_000_000.0)
            count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK subs", count / 1_000.0)
            count > 0 -> "$count subs"
            else -> "Canal"
        }
    } catch (_: Exception) {
        "Canal"
    }
}

/**
 * Pantalla de detalle de canal al tocar un canal.
 * Solo muestra encabezado con foto, nombre y lista de vídeos subidos.
 * (Sin botones de Reproducir, Aleatorio ni Descargar).
 */
@Composable
private fun SearchChannelDetailView(
    channel: ChannelCard,
    videos: List<Track>,
    isLoading: Boolean,
    savedTrackIds: Set<String>,
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onToggleSave: (Track) -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }
            Text(
                text = channel.name,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Fr33Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackThumbnail(
                        thumbnailUrl = channel.avatarUrl,
                        contentDescription = channel.name,
                        size = 110.dp,
                        isCircular = true,
                        fallbackIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                    Text(
                        text = channel.name,
                        style = Fr33Typography.headlineMedium,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                    val subText = if (channel.subscriberCount > 0) {
                        "${formatSubscriberCount(channel.subscriberCount)} • Canal"
                    } else {
                        "Canal de YouTube"
                    }
                    Text(
                        text = subText,
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary,
                        textAlign = TextAlign.Center
                    )

                    val desc = channel.description
                    if (!desc.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Fr33Spacing.small))
                        Text(
                            text = desc,
                            style = Fr33Typography.bodySmall,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vídeos subidos (${videos.size})",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextPrimary
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron vídeos disponibles en este canal",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = videos,
                    key = { index, track -> "${track.trackId}_$index" }
                ) { _, track ->
                    TrackRow(
                        track = track,
                        onClick = { onTrackClick(track) },
                        trailingContent = {
                            val isSaved = savedTrackIds.contains(track.trackId)
                            IconButton(onClick = { onToggleSave(track) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isSaved) "Guardado" else "Guardar",
                                    tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

/**
 * Pantalla de detalle de álbum/playlist al tocar una tarjeta en la búsqueda
 */
@Composable
private fun SearchAlbumDetailView(
    album: AlbumCard,
    tracks: List<Track>,
    isLoading: Boolean,
    savedTrackIds: Set<String>,
    initialScrollIndex: Int = 0,
    initialScrollOffset: Int = 0,
    onSaveState: (index: Int, offset: Int) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayAll: (List<Track>, Boolean) -> Unit,
    onDownloadAll: (List<Track>) -> Unit = {},
    onToggleSave: (Track) -> Unit
) {
    val listState = rememberLazyListState(initialScrollIndex, initialScrollOffset)

    DisposableEffect(listState) {
        onDispose {
            onSaveState(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fr33Spacing.small, vertical = Fr33Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = ColorTextPrimary
                )
            }
            Text(
                text = album.title,
                style = Fr33Typography.titleLarge,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Fr33Spacing.extraLarge)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Fr33Spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TrackThumbnail(
                        thumbnailUrl = album.thumbnailUrl,
                        contentDescription = album.title,
                        size = 140.dp,
                        shapeRadius = Fr33Radius.large,
                        fallbackIcon = Icons.Default.Album
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.medium))

                    Text(
                        text = album.title,
                        style = Fr33Typography.headlineMedium,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.extraSmall))

                    Text(
                        text = if (tracks.isNotEmpty()) {
                            "${album.artistName} • ${tracks.size} canciones"
                        } else {
                            album.artistName
                        },
                        style = Fr33Typography.bodyMedium,
                        color = ColorTextTertiary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Fr33Spacing.medium, Alignment.CenterHorizontally)
                    ) {
                        Button(
                            onClick = { onPlayAll(tracks, false) },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                            shape = RoundedCornerShape(Fr33Radius.large),
                            modifier = Modifier.weight(1f),
                            enabled = tracks.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Fr33Spacing.small))
                            Text("Reproducir", style = Fr33Typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = { onPlayAll(tracks, true) },
                            shape = RoundedCornerShape(Fr33Radius.large),
                            modifier = Modifier.weight(1f),
                            enabled = tracks.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = ColorPrimary
                            )
                            Spacer(modifier = Modifier.width(Fr33Spacing.small))
                            Text("Aleatorio", style = Fr33Typography.labelLarge, color = ColorPrimary)
                        }

                        OutlinedButton(
                            onClick = { onDownloadAll(tracks) },
                            shape = RoundedCornerShape(Fr33Radius.large),
                            modifier = Modifier.weight(1f),
                            enabled = tracks.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = ColorPrimary
                            )
                            Spacer(modifier = Modifier.width(Fr33Spacing.small))
                            Text("Descargar", style = Fr33Typography.labelLarge, color = ColorPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(Fr33Spacing.large))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Canciones (${tracks.size})",
                            style = Fr33Typography.titleMedium,
                            color = ColorTextPrimary
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorPrimary)
                    }
                }
            } else if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Fr33Spacing.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron canciones en esta lista",
                            style = Fr33Typography.bodyLarge,
                            color = ColorTextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = tracks,
                    key = { index, track -> "${track.trackId}_$index" }
                ) { _, track ->
                    TrackRow(
                        track = track,
                        onClick = { onTrackClick(track) },
                        trailingContent = {
                            val isSaved = savedTrackIds.contains(track.trackId)
                            IconButton(onClick = { onToggleSave(track) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isSaved) "Guardado" else "Guardar",
                                    tint = if (isSaved) ColorPrimary else ColorTextSecondary
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        color = ColorDivider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchIdleView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = "Busca canciones, artistas o canales",
            style = Fr33Typography.titleLarge,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = "Encuentra tus pistas, álbumes y canales favoritos de YouTube",
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchEmptyView(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Fr33Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = ColorTextTertiary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.large))

        Text(
            text = if (query.isNotBlank()) "Sin resultados para \"$query\"" else "No se encontraron resultados",
            style = Fr33Typography.titleMedium,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Fr33Spacing.small))

        Text(
            text = "Prueba con otras palabras clave o el nombre del canal",
            style = Fr33Typography.bodyMedium,
            color = ColorTextTertiary,
            textAlign = TextAlign.Center
        )
    }
}
```

---

### `feature/search/src/main/java/com/fr33music/feature/search/SearchUiState.kt`

```kotlin
package com.fr33music.feature.search

import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val tracks: List<Track>) : SearchUiState
    data class LookingSuccess(val result: LookingSearchResult) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}
```

---

### `feature/search/src/main/java/com/fr33music/feature/search/SearchViewModel.kt`

```kotlin
package com.fr33music.feature.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.entity.SearchHistoryEntity
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository,
    private val trackDao: TrackDao,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    var albumScrollIndex: Int = 0
    var albumScrollOffset: Int = 0

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumCard?>(null)
    val selectedAlbum: StateFlow<AlbumCard?> = _selectedAlbum.asStateFlow()

    private val _albumTracks = MutableStateFlow<List<Track>>(emptyList())
    val albumTracks: StateFlow<List<Track>> = _albumTracks.asStateFlow()

    private val _isLoadingAlbum = MutableStateFlow(false)
    val isLoadingAlbum: StateFlow<Boolean> = _isLoadingAlbum.asStateFlow()

    private val _selectedChannel = MutableStateFlow<ChannelCard?>(null)
    val selectedChannel: StateFlow<ChannelCard?> = _selectedChannel.asStateFlow()

    private val _channelVideos = MutableStateFlow<List<Track>>(emptyList())
    val channelVideos: StateFlow<List<Track>> = _channelVideos.asStateFlow()

    private val _isLoadingChannel = MutableStateFlow(false)
    val isLoadingChannel: StateFlow<Boolean> = _isLoadingChannel.asStateFlow()

    fun selectChannel(channel: ChannelCard?) {
        _selectedChannel.value = channel
        if (channel != null) {
            viewModelScope.launch {
                try {
                    _isLoadingChannel.value = true
                    val result = searchSource.getChannelVideos(channel)
                    _channelVideos.value = result.getOrElse { emptyList() }
                } catch (e: Exception) {
                    _channelVideos.value = emptyList()
                } finally {
                    _isLoadingChannel.value = false
                }
            }
        } else {
            _channelVideos.value = emptyList()
            _isLoadingChannel.value = false
        }
    }

    fun selectAlbum(album: AlbumCard?) {
        _selectedAlbum.value = album
        if (album != null) {
            viewModelScope.launch {
                try {
                    _isLoadingAlbum.value = true
                    val result = searchSource.getAlbumTracks(album)
                    _albumTracks.value = result.getOrElse { emptyList() }
                } catch (e: Exception) {
                    _albumTracks.value = emptyList()
                } finally {
                    _isLoadingAlbum.value = false
                }
            }
        } else {
            _albumTracks.value = emptyList()
        }
    }

    val savedTrackIds: StateFlow<Set<String>> = trackDao.getSavedTracks()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.trim().isEmpty()) {
            searchJob?.cancel()
            _uiState.value = SearchUiState.Idle
        }
    }

    fun search() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // Registrar búsqueda en historial para recomendaciones
        viewModelScope.launch {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = currentQuery))
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            try {
                val result = searchSource.searchLookingDetails(currentQuery)
                result.fold(
                    onSuccess = { lookingRes ->
                        if (lookingRes.channels.isEmpty() && lookingRes.videos.isEmpty() && lookingRes.playlists.isEmpty()) {
                            _uiState.value = SearchUiState.Empty
                        } else {
                            _uiState.value = SearchUiState.LookingSuccess(lookingRes)
                        }
                    },
                    onFailure = { throwable ->
                        handleError(throwable)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is ExtractorError.EmptyQuery -> "Introduce un término de búsqueda"
            is ExtractorError.NetworkError -> "Error de conexión. Comprueba tu conexión a Internet."
            is ExtractorError.StreamUnavailable -> "Contenido no disponible en YouTube."
            else -> throwable.message ?: "Ocurrió un error al buscar. Inténtalo de nuevo."
        }
        _uiState.value = SearchUiState.Error(message)
    }

    fun retry() {
        search()
    }

    fun onTrackClick(track: Track) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                when (currentState) {
                    is SearchUiState.LookingSuccess -> {
                        val index = currentState.result.videos.indexOfFirst { it.trackId == track.trackId }
                        if (index >= 0) {
                            playerRepository.setQueue(currentState.result.videos, index)
                            return@launch
                        }
                    }
                    is SearchUiState.Success -> {
                        val index = currentState.tracks.indexOfFirst { it.trackId == track.trackId }
                        if (index >= 0) {
                            playerRepository.setQueue(currentState.tracks, index)
                            return@launch
                        }
                    }
                    else -> {}
                }
                playerRepository.play(track)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al reproducir canción ${track.trackId}: ${e.message}", e)
            }
        }
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        viewModelScope.launch {
            try {
                if (shuffle) {
                    val shuffled = tracks.shuffled()
                    playerRepository.setQueue(shuffled, 0)
                    if (!playerRepository.playbackState.value.isShuffle) {
                        playerRepository.toggleShuffle()
                    }
                } else {
                    val index = startIndex.coerceIn(0, tracks.lastIndex)
                    playerRepository.setQueue(tracks, index)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en playAll: ${e.message}", e)
            }
        }
    }

    fun toggleSave(track: Track) {
        viewModelScope.launch {
            try {
                val isCurrentlySaved = savedTrackIds.value.contains(track.trackId)
                libraryRepository.toggleSave(track, isCurrentlySaved)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en toggleSave: ${e.message}", e)
            }
        }
    }

    fun downloadAll(tracks: List<Track>) {
        viewModelScope.launch {
            try {
                tracks.forEach { track ->
                    downloadRepository.download(track)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en downloadAll: ${e.message}", e)
            }
        }
    }
}
```

---

### `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.java.home=C:\\Users\\Paisny\\.jdks\\temurin-17\\jdk-17.0.20.1+1
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

---

### `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
coreKtx = "1.13.1"
lifecycle = "2.8.6"
activityCompose = "1.9.3"
composeBom = "2024.09.00"
navigationCompose = "2.8.3"
hilt = "2.51.1"
hiltNavigationCompose = "1.2.0"
media3 = "1.4.1"
newPipeExtractor = "v0.26.5"
coil = "2.7.0"
okhttp = "4.12.0"
kotlinxSerialization = "1.7.3"
kotlinxCoroutines = "1.8.1"
desugarJdkLibs = "2.1.2"
room = "2.6.1"
datastore = "1.1.1"
work = "2.9.1"
junit = "4.13.2"
androidxJunit = "1.2.1"
espresso = "3.6.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Compose BOM
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Media3
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
media3-datasource-okhttp = { group = "androidx.media3", name = "media3-datasource-okhttp", version.ref = "media3" }

# NewPipe Extractor & Networking
newpipe-extractor = { group = "com.github.TeamNewPipe", name = "NewPipeExtractor", version.ref = "newPipeExtractor" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Coil
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Kotlinx
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# Desugaring
desugar-jdk-libs = { group = "com.android.tools", name = "desugar_jdk_libs_nio", version.ref = "desugarJdkLibs" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

---

### `gradle/wrapper/gradle-wrapper.properties`

```properties
#Fri Aug 08 18:57:47 VET 2025
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

### `local.properties`

```properties
sdk.dir=C\:\\Users\\Paisny\\AppData\\Local\\Android\\Sdk
```

---

### `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "Fr33Music"

include(":app")
include(":core:designsystem")
include(":core:domain")
include(":core:database")
include(":core:extractor")
include(":core:player")
include(":feature:home")
include(":feature:search")
include(":feature:player")
include(":feature:library")
```

---

