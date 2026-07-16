# Rimemba

App Android nativa (Kotlin + Jetpack Compose) para anotar notas y recordatorios
por voz con un solo botón — pensada para gente olvidadiza. Todo funciona
offline, sin backend ni cuentas.

## Funcionalidades

- Un botón (en la app y en un widget de home screen) que graba voz, transcribe,
  guarda y confirma por voz lo anotado.
- Detección automática de nota vs. recordatorio a partir de la fecha/hora
  mencionada ("mañana", "el lunes a las 5", "en 2 horas"...).
- Recordatorios recurrentes: diarios, semanales o en días concretos ("cada
  lunes y miércoles").
- Notificación de recordatorio con acciones **Completar** y **Posponer**; al
  posponer puedes dictar o elegir una sugerencia rápida ("una hora", "esta
  tarde", "mañana").
- Widget de botón grande para anotar sin abrir la app.
- Widget de resumen del día con tus notas/recordatorios pendientes.

## Cómo compilar y probar

Este proyecto se desarrolló en un entorno sin SDK de Android y sin acceso de
red a `maven.google.com`/`dl.google.com` (de donde salen el Android Gradle
Plugin y todas las librerías AndroidX/Compose/Room/Glance), así que **no pudo
compilarse ni ejecutarse dentro de ese entorno**. El código está escrito con
cuidado y la lógica más propensa a bugs (el parser de fecha/hora) se verificó
de forma aislada con Kotlin puro + JUnit (ver más abajo), pero el resto
necesita compilarse en una máquina con SDK de Android y acceso normal a
internet.

1. Abre la carpeta `rimemba/` con Android Studio (Ladybug o más reciente) y
   deja que sincronice Gradle, **o** desde línea de comandos:
   ```
   cd rimemba
   ./gradlew assembleDebug
   ```
2. **Antes de compilar**, revisa `gradle/libs.versions.toml`: los números de
   versión (AGP, Kotlin, Compose BOM, Room, Glance, KSP...) se fijaron según
   la información disponible al escribir este proyecto, pero no se pudieron
   verificar en vivo. Si Android Studio sugiere actualizar algo (o si KSP da
   error de versión incompatible con Kotlin), acepta la actualización — es
   más fiable que lo que hay hardcodeado aquí.
3. Instala el APK en tu móvil o emulador (`./gradlew installDebug` o el botón
   Run de Android Studio).

### Ejecutar los tests

```
./gradlew test                 # unit tests (parser, scheduler) — no requieren dispositivo
./gradlew connectedAndroidTest  # ItemDaoTest — requiere un emulador o móvil conectado
```

### Permisos que tendrás que aceptar la primera vez

- **Micrófono**: se pide la primera vez que tocas el botón de grabar.
- **Notificaciones** (Android 13+): se pide la primera vez que creas un
  recordatorio con fecha/hora.
- **Alarmas exactas** (Android 12+): si el sistema no te lo concede
  automáticamente, la app sigue funcionando pero los recordatorios pueden
  llegar con algo de retraso (usa un fallback inexacto en vez de fallar).
- **Optimización de batería**: en algunos fabricantes (Xiaomi, Huawei,
  Samsung...) el sistema puede retrasar o silenciar los recordatorios si la
  app está en la lista de optimización de batería. Si notas que llegan tarde,
  busca "Rimemba" en los ajustes de batería del fabricante y desactiva la
  optimización para esta app.

## Estructura del proyecto

```
app/src/main/java/com/marcroldan/rimemba/
├── core/              utilidades compartidas (notificaciones, tiempo, constantes)
├── data/              Room (entidades, DAO, base de datos) y repositorio
├── domain/
│   ├── model/         modelos de dominio (Item, TipoItem, Recurrencia)
│   ├── parser/         parser de fecha/hora en español (sin librería externa)
│   ├── scheduling/     AlarmScheduler + cálculo de próxima ocurrencia recurrente
│   └── usecase/        flujo compartido "texto transcrito -> parsear -> guardar"
├── voice/             controladores de SpeechRecognizer y TextToSpeech
├── alarm/              BroadcastReceivers de alarmas y acciones de notificación
├── ui/                 pantallas Compose (lista, captura rápida del widget)
├── widget/              widgets Glance (botón grande, resumen del día)
└── di/                  contenedor de dependencias manual (sin Hilt)
```

## Verificación realizada durante el desarrollo

El parser de fecha/hora (`domain/parser`) y el cálculo de próxima ocurrencia
(`domain/scheduling/NextOccurrenceCalculator`) son Kotlin puro (sin
dependencias de Android), así que se pudieron compilar y testear de verdad
con un proyecto Gradle temporal usando solo Maven Central (JUnit). Los 25
tests unitarios de `SpanishDateTimeParserTest`, `NextOccurrenceCalculatorTest`
y `SnoozeReplyInterpreterTest` pasan. El resto del código (Compose, Room,
AlarmManager, notificaciones, Glance) sigue las APIs estándar de Android
documentadas, pero no se pudo compilar en este entorno — revísalo al abrirlo
en Android Studio, que marcará cualquier error de compilación real.

## Limitaciones conocidas del MVP

- El parser de horas sin calificador ("a las 5") se interpreta literal en
  formato 24h, no intenta adivinar AM/PM.
- "Posponer" desde la notificación usa el teclado del sistema para dictar (con
  su propio icono de micrófono), no una UI de voz propia — es el
  comportamiento estándar de `RemoteInput` en un teléfono (no Wear OS).
- Sin sincronización entre dispositivos ni copia en la nube más allá del
  backup automático de Android (Auto Backup).
