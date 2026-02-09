# 🦋 RENAIX - App Móvil Android

Marketplace de productos de segunda mano con Jetpack Compose.

## 📋 INFORMACIÓN DEL PROYECTO

- **Nombre:** Renaix
- **Package:** com.renaix
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **API Backend:** http://10.0.2.2:8069/api/v1
- **Google Maps API Key:** AIzaSyC5_APswRVmkJs91rK1r5Z3SpJ_MpMvCfY

## 🏗️ ARQUITECTURA

```
Clean Architecture + MVVM
├── Presentation Layer (UI + ViewModels)
├── Domain Layer (Models + Use Cases)
└── Data Layer (Repositories + Data Sources)
```

## 📦 STACK TECNOLÓGICO

- **UI:** Jetpack Compose + Material 3
- **Navegación:** Navigation Compose
- **Networking:** Ktor Client
- **Serialización:** Kotlinx Serialization
- **Base de Datos:** SQLDelight
- **Almacenamiento Seguro:** EncryptedSharedPreferences
- **Imágenes:** Coil
- **Mapas:** Google Maps Compose
- **Async:** Coroutines + Flow
- **DI:** Manual (AppContainer)

## 🚀 INSTRUCCIONES DE IMPORTACIÓN

### OPCIÓN 1: Crear desde cero en Android Studio

1. **Crear nuevo proyecto:**
   - New Project → Empty Activity (Compose)
   - Name: Renaix
   - Package: com.renaix
   - Min SDK: 26
   - Build configuration: Kotlin DSL

2. **Reemplazar archivos:**
   - Copia `build.gradle.kts` (root)
   - Copia `build.gradle.kts` (app)
   - Copia `settings.gradle.kts`
   - Copia `AndroidManifest.xml`

3. **Sync Gradle:**
   - Click en "Sync Now"
   - Espera a que descargue dependencias (5-10 min)

4. **Copiar estructura de carpetas:**
   - Copia toda la carpeta `app/src/main/java/com/renaix`
   - Copia `app/src/main/sqldelight`

### OPCIÓN 2: Importar proyecto existente

1. Extrae el ZIP completo
2. Android Studio → Open → Selecciona la carpeta del proyecto
3. Sync Gradle
4. Run

## 📁 ESTRUCTURA DE CARPETAS

```
app/src/main/java/com/renaix/
│
├── data/
│   ├── local/
│   │   ├── database/          # SQLDelight
│   │   └── preferences/       # EncryptedSharedPrefs
│   ├── remote/
│   │   ├── api/              # Ktor Client
│   │   ├── dto/              # Request/Response
│   │   └── datasource/       # Remote Data Sources
│   └── repository/           # Repository Implementations
│
├── domain/
│   ├── model/                # Business Models
│   ├── repository/           # Repository Interfaces
│   └── usecase/              # Use Cases
│
├── presentation/
│   ├── screens/              # Pantallas Compose
│   ├── navigation/           # NavGraph
│   └── common/               # Componentes reutilizables
│
├── di/                       # Dependency Injection
├── ui/theme/                 # Theme (Colors, Typography)
└── util/                     # Constants, Extensions
```

## 🗄️ SCHEMA DE BASE DE DATOS (SQLDelight)

Crear archivo: `app/src/main/sqldelight/com/renaix/data/local/database/RenaixDatabase.sq`

```sql
-- Tabla de Productos (Caché)
CREATE TABLE Product (
    id INTEGER PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    precio REAL NOT NULL,
    categoria_id INTEGER,
    categoria_nombre TEXT,
    estado_producto TEXT NOT NULL,
    estado_venta TEXT NOT NULL,
    imagen_principal TEXT,
    propietario_id INTEGER NOT NULL,
    propietario_nombre TEXT,
    fecha_publicacion INTEGER,
    fecha_actualizacion INTEGER NOT NULL
);

-- Tabla de Categorías (Caché)
CREATE TABLE Category (
    id INTEGER PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL UNIQUE,
    descripcion TEXT,
    imagen_url TEXT,
    producto_count INTEGER NOT NULL DEFAULT 0
);

-- Tabla de Usuario (Perfil actual)
CREATE TABLE UserProfile (
    id INTEGER PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    telefono TEXT,
    imagen_url TEXT,
    valoracion_promedio REAL NOT NULL DEFAULT 0.0,
    productos_en_venta INTEGER NOT NULL DEFAULT 0,
    productos_vendidos INTEGER NOT NULL DEFAULT 0
);

-- Tabla de Favoritos (Local only)
CREATE TABLE Favorite (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    producto_id INTEGER NOT NULL UNIQUE,
    fecha_agregado INTEGER NOT NULL,
    FOREIGN KEY (producto_id) REFERENCES Product(id) ON DELETE CASCADE
);

-- Queries
selectAllProducts:
SELECT * FROM Product WHERE estado_venta = 'disponible' ORDER BY fecha_publicacion DESC;

selectProductById:
SELECT * FROM Product WHERE id = ?;

insertProduct:
INSERT OR REPLACE INTO Product VALUES ?;

deleteProduct:
DELETE FROM Product WHERE id = ?;

selectAllCategories:
SELECT * FROM Category ORDER BY nombre ASC;

insertCategory:
INSERT OR REPLACE INTO Category VALUES ?;

selectUserProfile:
SELECT * FROM UserProfile LIMIT 1;

insertUserProfile:
INSERT OR REPLACE INTO UserProfile VALUES ?;

selectAllFavorites:
SELECT p.* FROM Product p
INNER JOIN Favorite f ON p.id = f.producto_id
ORDER BY f.fecha_agregado DESC;

insertFavorite:
INSERT OR IGNORE INTO Favorite(producto_id, fecha_agregado) VALUES (?, ?);

deleteFavorite:
DELETE FROM Favorite WHERE producto_id = ?;

isFavorite:
SELECT EXISTS(SELECT 1 FROM Favorite WHERE producto_id = ?);
```

## 🎨 TEMA VISUAL

Los colores principales están basados en el logo morado de Renaix:

- **Primary:** Purple500 (#9C27B0)
- **Secondary:** PurpleAccent (#CE93D8)
- **Background:** BackgroundLight (#FAFAFA)
- **Surface:** SurfaceLight (#FFFFFF)

## ⚙️ CONFIGURACIÓN INICIAL

### 1. Verificar que Odoo responde

Abre el navegador en tu PC y ve a:
```
http://localhost:8069/api/v1/categorias
```

Debes ver un JSON con categorías.

### 2. Configurar emulador

En Android Studio:
- Tools → Device Manager
- Crear dispositivo con API 26 o superior
- Iniciar emulador

### 3. Verificar conectividad

Desde el emulador, la app usará:
```
http://10.0.2.2:8069
```

Que automáticamente apunta a `localhost` de tu PC.

## 🔧 PRÓXIMOS PASOS

Una vez importado el proyecto:

1. **Sync Gradle** (importante)
2. **Generar código de SQLDelight:**
   - Build → Rebuild Project
   - Esto genera las clases de SQLDelight automáticamente

3. **Crear archivos faltantes:**
   - Ver ARCHIVOS_PENDIENTES.md para la lista completa

4. **Run en emulador:**
   - Click en Run ▶️
   - Seleccionar emulador
   - Esperar a que instale

## 📱 FUNCIONALIDADES IMPLEMENTADAS

### ✅ Obligatorias (6 puntos)
- [x] Autenticación JWT con persistencia
- [x] Arquitectura Clean + MVVM
- [x] Procesos en segundo plano
- [x] CRUD de productos con imágenes
- [x] Documentación

### ✅ Avanzadas (4 puntos)
- [x] Google Maps + Geolocalización
- [x] Búsqueda con filtros
- [x] Sistema de chat
- [x] Gestión avanzada de estados

## 🐛 TROUBLESHOOTING

### Error: "Cannot resolve symbol"
- Sync Gradle
- Build → Rebuild Project
- Invalidate Caches → Restart

### Error: SQLDelight no genera código
- Sync Gradle
- Build → Rebuild Project
- Verificar que el archivo .sq está en la ruta correcta

### Error de conexión a API
- Verificar que Odoo corre en http://localhost:8069
- Verificar que usas http://10.0.2.2:8069 en la app
- Verificar AndroidManifest tiene `usesCleartextTraffic="true"`

### Error de Maps API Key
- Verificar que la key está en build.gradle.kts
- Sync Gradle
- Clean Project → Rebuild

## 📚 RECURSOS

- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Ktor Client Docs](https://ktor.io/docs/client.html)
- [SQLDelight Docs](https://cashapp.github.io/sqldelight/)
- [Material 3 Guidelines](https://m3.material.io/)

## 👥 AUTORES

Javier Herraiz & Alejandro Sánchez
Proyecto DAM 2025-26

## 📄 LICENCIA

Proyecto educativo - Todos los derechos reservados
