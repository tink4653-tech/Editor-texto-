# Video Editor App - README

## Descripción
Aplicación Android para edición básica de videos desarrollada en Kotlin.

## Características Principales

### 1. **Selección de Videos**
- Visualiza todos los videos almacenados en el dispositivo
- Permite seleccionar videos desde la galería
- Muestra miniaturas y duración de cada video

### 2. **Reproducción de Video**
- Reproductor de video integrado
- Control de reproducción (play/pause)
- Barra de progreso con seek bar
- Tiempo actual y tiempo total del video

### 3. **Edición de Video**
- **Recortar (Trim)**: Corta el video seleccionando inicio y duración
- **Agregar Música**: Añade audio de fondo al video
- **Cambiar Velocidad**: Modifica la velocidad de reproducción (2x, 0.5x, etc.)
- **Exportar**: Guarda el video editado en la galería del dispositivo

## Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **FFmpeg Kit**: Para procesamiento de video
- **Glide**: Para carga de imágenes y miniaturas
- **Material Design**: Para la interfaz de usuario

## Estructura del Proyecto

```
VideoEditor/
├── app/
│   ├── src/main/
│   │   ├── java/com/videoeditor/
│   │   │   └── activity/
│   │   │       ├── MainActivity.kt          # Pantalla principal con lista de videos
│   │   │       └── VideoEditActivity.kt     # Editor de video
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_video_edit.xml
│   │   │   │   └── item_video.xml
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── xml/
│   │   │       └── file_paths.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Configuración del Proyecto

### Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 8 o superior
- Android SDK con API level 34

### Pasos para Compilar

1. Abre el proyecto en Android Studio
2. Espera a que Gradle sincronice las dependencias
3. Conecta un dispositivo Android o inicia un emulador
4. Ejecuta la aplicación (Run 'app')

### Permisos Requeridos

La aplicación solicita los siguientes permisos:
- `READ_EXTERNAL_STORAGE`: Para acceder a videos almacenados
- `READ_MEDIA_VIDEO`: Para Android 13+
- `READ_MEDIA_IMAGES`: Para miniaturas
- `CAMERA`: Opcional, para grabar videos

## Comandos FFmpeg Utilizados

### Recortar Video
```bash
ffmpeg -i input.mp4 -ss 0 -t 10 -c copy output.mp4
```

### Agregar Música
```bash
ffmpeg -i video.mp4 -i audio.mp3 -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest output.mp4
```

### Cambiar Velocidad
```bash
ffmpeg -i input.mp4 -filter:v "setpts=PTS/2.0" -filter:a "atempo=2.0" output.mp4
```

### Exportar Video
```bash
ffmpeg -i input.mp4 -c:v libx264 -preset medium -crf 23 -c:a aac -b:a 128k output.mp4
```

## Dependencias Principales

```gradle
implementation 'com.arthenica:ffmpeg-kit-full:6.0-2'
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.android.material:material:1.11.0'
```

## Notas Importantes

1. **FFmpeg Kit**: La aplicación utiliza FFmpeg para procesar videos. Esto puede consumir recursos significativos en dispositivos de gama baja.

2. **Almacenamiento**: Los videos exportados se guardan en la carpeta "Movies/VideoEditor" del almacenamiento interno.

3. **Rendimiento**: El procesamiento de videos largos puede tomar tiempo. Se recomienda usar videos de corta duración para mejores resultados.

4. **Compatibilidad**: La aplicación es compatible con Android 7.0 (API 24) y versiones superiores.

## Futuras Mejoras

- [ ] Añadir filtros de color
- [ ] Incorporar transiciones entre clips
- [ ] Agregar texto y subtítulos
- [ ] Soporte para múltiples pistas de audio
- [ ] Vista previa en tiempo real de efectos
- [ ] Compartir directamente a redes sociales

## Licencia

Este proyecto es de código abierto y puede ser modificado y distribuido libremente.

## Soporte

Para problemas o sugerencias, por favor crea un issue en el repositorio del proyecto.
