# 🚀 INSTRUCCIONES DE CONFIGURACIÓN - RENAIX ANDROID

Este archivo contiene los pasos **EXACTOS** para poner en marcha el proyecto.

## ✅ CHECKLIST DE CONFIGURACIÓN

### PASO 1: Importar en Android Studio
- [ ] Extraer ZIP completo
- [ ] Android Studio → Open → Seleccionar carpeta `renaix_android_project`
- [ ] Esperar a que Gradle sincronice (5-10 min)
- [ ] Verificar que no hay errores en Build

### PASO 2: Verificar Dependencias
- [ ] Todas las dependencias descargadas correctamente
- [ ] No hay errores rojos en `build.gradle.kts`
- [ ] SQLDelight plugin configurado

### PASO 3: Generar Código de SQLDelight
```bash
Build → Rebuild Project
```
Esto genera automáticamente las clases Kotlin desde `RenaixDatabase.sq`

### PASO 4: Verificar Conectividad con API
1. Asegúrate de que Odoo corre en tu PC:
   ```
   http://localhost:8069
   ```

2. Desde el navegador de tu PC, verifica:
   ```
   http://localhost:8069/api/v1/categorias
   ```
   Debes ver JSON con categorías.

3. El emulador usará automáticamente:
   ```
   http://10.0.2.2:8069
   ```

### PASO 5: Crear Archivos Pendientes

Los siguientes archivos están pendientes de crear. Te los iré dando conforme avancemos:

#### **Archivos de Tema (UI)**
- [x] `ui/theme/Color.kt` ✅ Creado
- [ ] `ui/theme/Theme.kt`
- [ ] `ui/theme/Type.kt`
- [ ] `ui/theme/Shape.kt`

#### **Data Layer - Local**
- [ ] `data/local/preferences/SecurePreferences.kt`
- [ ] `data/local/preferences/PreferencesManager.kt`
- [ ] `data/local/database/DatabaseDriverFactory.kt`

#### **Data Layer - Remote**
- [ ] `data/remote/api/KtorClient.kt`
- [ ] `data/remote/api/RenaixApi.kt`
- [ ] `data/remote/dto/response/ApiResponse.kt`
- [ ] `data/remote/dto/request/LoginRequest.kt`
- [ ] `data/remote/dto/response/AuthResponse.kt`

#### **Domain Layer**
- [ ] `domain/model/User.kt`
- [ ] `domain/model/Product.kt`
- [ ] `domain/model/Category.kt`
- [ ] `domain/repository/AuthRepository.kt`
- [ ] `domain/usecase/auth/LoginUseCase.kt`

#### **Presentation Layer**
- [ ] `presentation/navigation/Screen.kt`
- [ ] `presentation/navigation/NavGraph.kt`
- [ ] `presentation/screens/splash/SplashScreen.kt`
- [ ] `presentation/screens/auth/login/LoginScreen.kt`
- [ ] `presentation/screens/main/MainScreen.kt`

#### **DI Layer**
- [ ] `di/AppContainer.kt`

#### **MainActivity**
- [ ] `MainActivity.kt`

## 📊 ORDEN DE CREACIÓN RECOMENDADO

### DÍA 1: Setup y Fundamentos
1. Tema (Color, Theme, Type, Shape)
2. Constants (ya creado ✅)
3. SecurePreferences
4. KtorClient
5. AppContainer (DI)

### DÍA 2: Capa de Datos
6. DTOs (Request/Response)
7. RenaixApi
8. DatabaseDriverFactory
9. Repositories

### DÍA 3: Domain y Use Cases
10. Modelos de dominio
11. Use Cases de Auth
12. Use Cases de Products

### DÍA 4-5: UI
13. Navigation (Screen, NavGraph)
14. SplashScreen
15. LoginScreen
16. MainScreen
17. ProductListScreen

## 🔧 TROUBLESHOOTING

### Error: "Cannot resolve symbol RenaixDatabase"
**Solución:**
```
Build → Rebuild Project
```
SQLDelight genera las clases automáticamente al compilar.

### Error: "Unresolved reference: ktor"
**Solución:**
1. Verificar que `build.gradle.kts` tiene todas las dependencias de Ktor
2. Sync Gradle
3. Invalidate Caches → Restart

### Error: Google Maps no funciona
**Solución:**
1. Verificar que la API Key está en `build.gradle.kts`:
   ```kotlin
   manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyC5_APswRVmkJs91rK1r5Z3SpJ_MpMvCfY"
   ```
2. Sync Gradle
3. Clean Project → Rebuild

### Error de conexión a API
**Solución:**
1. Verificar que Odoo está corriendo
2. Desde el navegador del PC, ir a `http://localhost:8069`
3. Verificar que `AndroidManifest.xml` tiene:
   ```xml
   android:usesCleartextTraffic="true"
   ```

## 📝 PRÓXIMOS PASOS

Una vez que Gradle sincronice correctamente:

1. **Confirma que no hay errores:**
   - Build → Make Project
   - No debe haber errores rojos

2. **Contáctame para:**
   - Creación de archivos de tema
   - Configuración de KtorClient
   - Creación de la primera pantalla (Splash/Login)

3. **NO intentes ejecutar todavía:**
   - Faltan archivos esenciales (MainActivity, screens, etc.)
   - Los crearemos paso a paso

## ✅ VERIFICACIÓN FINAL

Antes de continuar, verifica:

- [ ] Proyecto importado sin errores
- [ ] Gradle sincronizado correctamente
- [ ] SQLDelight genera código (Build → Rebuild)
- [ ] Odoo responde en `http://localhost:8069`
- [ ] Tienes emulador configurado

**¿Todo ✅? → Contáctame para crear los siguientes archivos**
**¿Algún ❌? → Envíame el error específico**

---

**Siguiente archivo a crear:** `ui/theme/Theme.kt`
