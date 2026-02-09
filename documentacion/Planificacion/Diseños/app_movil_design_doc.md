# 🦋 PROYECTO RENAIX - Continuación del Desarrollo Android

## 📋 CONTEXTO DEL PROYECTO

Estoy desarrollando una app Android para un marketplace de segunda mano llamado **Renaix**. 

Ya tengo:
- ✅ Backend API REST en Odoo funcionando (documentación en `API_REST_Documentacion.md`)
- ✅ Estructura base del proyecto Android con dependencias configuradas
- ✅ SQLDelight schema definido
- ✅ Constants.kt con configuración de API
- ✅ Paleta de colores (morado/purple based)

## 🎯 OBJETIVO

Necesito que me ayudes a crear **TODO el código faltante** para tener una app Android funcional que consuma mi API REST de Odoo.

## 📚 DOCUMENTACIÓN DISPONIBLE

En este repositorio tienes acceso a:
1. **`API_REST_Documentacion.md`** - Documentación completa de todos los endpoints de mi API REST
2. **`analisis-modulo-core-renaix.md`** - Análisis del módulo core de Odoo
3. **Estructura base del proyecto Android** en la carpeta del proyecto

**POR FAVOR, LEE PRIMERO `API_REST_Documentacion.md` PARA VER:**
- Todos los endpoints exactos (URLs, métodos HTTP, request/response)
- Formato de respuestas estándar
- Sistema de autenticación JWT
- Estructura de datos (DTOs)

## 🏗️ ARQUITECTURA A SEGUIR

**Clean Architecture + MVVM**
```
Presentation Layer (Jetpack Compose + ViewModels)
    ↓
Domain Layer (Use Cases + Models + Repository Interfaces)
    ↓
Data Layer (Repository Implementations + Data Sources)
    ↓
    ├─ Remote (Ktor Client → API REST)
    └─ Local (SQLDelight + EncryptedSharedPreferences)
```

## 🛠️ STACK TECNOLÓGICO

- **UI:** Jetpack Compose + Material 3
- **Navegación:** Navigation Compose
- **Networking:** Ktor Client
- **Serialización:** Kotlinx Serialization
- **Base de Datos:** SQLDelight (schema ya definido)
- **Tokens:** EncryptedSharedPreferences
- **Imágenes:** Coil
- **Mapas:** Google Maps Compose
- **Async:** Coroutines + Flow
- **DI:** Manual (AppContainer pattern)

## 📦 CONFIGURACIÓN EXISTENTE
```kotlin
// API Configuration
BASE_URL = "http://10.0.2.2:8069"
API_VERSION = "/api/v1"
API_BASE_URL = "http://10.0.2.2:8069/api/v1"

// Google Maps
MAPS_API_KEY = "AIzaSyC5_APswRVmkJs91rK1r5Z3SpJ_MpMvCfY"

// Package
package = "com.renaix"
```

## 🎨 TEMA VISUAL

Colores principales (basados en logo morado):
```kotlin
Primary: Purple500 (#9C27B0)
Secondary: PurpleAccent (#CE93D8)
Background: BackgroundLight (#FAFAFA)
```

## 📁 ESTRUCTURA DE ARCHIVOS A CREAR

Necesito que crees TODOS estos archivos siguiendo Clean Architecture:

### **1. UI/Theme (Completo)**
- `ui/theme/Theme.kt` - Theme completo con light/dark mode
- `ui/theme/Type.kt` - Typography definitions
- `ui/theme/Shape.kt` - Shape definitions

### **2. Data Layer - Local**
- `data/local/preferences/SecurePreferences.kt` - Wrapper de EncryptedSharedPreferences
- `data/local/preferences/PreferencesManager.kt` - Manager para tokens y sesión
- `data/local/database/DatabaseDriverFactory.kt` - Factory para SQLDelight

### **3. Data Layer - Remote**
- `data/remote/api/KtorClient.kt` - Cliente Ktor configurado con:
  - Base URL
  - JSON serialization
  - Logging
  - Timeout
  - Auth interceptor (añadir Bearer token automáticamente)
- `data/remote/api/RenaixApi.kt` - Interface con TODOS los endpoints de la API

### **4. Data Layer - DTOs**
Crear DTOs para TODOS los endpoints según `API_REST_Documentacion.md`:

**Request DTOs:**
- `data/remote/dto/request/LoginRequest.kt`
- `data/remote/dto/request/RegisterRequest.kt`
- `data/remote/dto/request/CreateProductRequest.kt`
- `data/remote/dto/request/UpdateProductRequest.kt`
- `data/remote/dto/request/CreatePurchaseRequest.kt`
- `data/remote/dto/request/SendMessageRequest.kt`
- Y todos los demás según documentación...

**Response DTOs:**
- `data/remote/dto/response/ApiResponse.kt` - Wrapper genérico
- `data/remote/dto/response/AuthResponse.kt`
- `data/remote/dto/response/ProductResponse.kt`
- `data/remote/dto/response/UserResponse.kt`
- `data/remote/dto/response/CategoryResponse.kt`
- Y todos los demás según documentación...

### **5. Data Layer - Data Sources**
- `data/remote/datasource/AuthRemoteDataSource.kt`
- `data/remote/datasource/ProductRemoteDataSource.kt`
- `data/remote/datasource/UserRemoteDataSource.kt`
- `data/remote/datasource/ChatRemoteDataSource.kt`
- Y todos los demás...

### **6. Data Layer - Repositories (Implementations)**
- `data/repository/AuthRepositoryImpl.kt`
- `data/repository/ProductRepositoryImpl.kt`
- `data/repository/UserRepositoryImpl.kt`
- Y todos los demás...

### **7. Domain Layer - Models**
Modelos de negocio (NO DTOs):
- `domain/model/User.kt`
- `domain/model/Product.kt`
- `domain/model/Category.kt`
- `domain/model/Tag.kt`
- `domain/model/Purchase.kt`
- `domain/model/Message.kt`
- `domain/model/Comment.kt`
- `domain/model/Rating.kt`
- Y todos los demás...

### **8. Domain Layer - Repository Interfaces**
- `domain/repository/AuthRepository.kt`
- `domain/repository/ProductRepository.kt`
- `domain/repository/UserRepository.kt`
- Y todos los demás...

### **9. Domain Layer - Use Cases**
Crear use cases para todas las operaciones:

**Auth:**
- `domain/usecase/auth/LoginUseCase.kt`
- `domain/usecase/auth/RegisterUseCase.kt`
- `domain/usecase/auth/LogoutUseCase.kt`
- `domain/usecase/auth/RefreshTokenUseCase.kt`

**Products:**
- `domain/usecase/product/GetProductsUseCase.kt`
- `domain/usecase/product/GetProductDetailUseCase.kt`
- `domain/usecase/product/CreateProductUseCase.kt`
- `domain/usecase/product/SearchProductsUseCase.kt`
- Y todos los demás...

**User:**
- `domain/usecase/user/GetProfileUseCase.kt`
- `domain/usecase/user/UpdateProfileUseCase.kt`
- Y todos los demás...

### **10. Presentation - Common**
- `presentation/common/state/UiState.kt` - Sealed class (Loading, Success, Error)
- `presentation/common/components/RenaixButton.kt`
- `presentation/common/components/RenaixTextField.kt`
- `presentation/common/components/ProductCard.kt`
- `presentation/common/components/LoadingIndicator.kt`
- `presentation/common/components/ErrorView.kt`
- `presentation/common/components/EmptyStateView.kt`

### **11. Presentation - Navigation**
- `presentation/navigation/Screen.kt` - Sealed class con todas las rutas
- `presentation/navigation/NavGraph.kt` - NavHost completo

### **12. Presentation - Screens**
Crear ViewModels + Screens para:

**Auth:**
- `presentation/screens/splash/SplashScreen.kt`
- `presentation/screens/splash/SplashViewModel.kt`
- `presentation/screens/auth/login/LoginScreen.kt`
- `presentation/screens/auth/login/LoginViewModel.kt`
- `presentation/screens/auth/register/RegisterScreen.kt`
- `presentation/screens/auth/register/RegisterViewModel.kt`

**Main:**
- `presentation/screens/main/MainScreen.kt` - Scaffold con BottomNavigation
- `presentation/screens/main/MainViewModel.kt`

**Products:**
- `presentation/screens/products/list/ProductListScreen.kt`
- `presentation/screens/products/list/ProductListViewModel.kt`
- `presentation/screens/products/detail/ProductDetailScreen.kt`
- `presentation/screens/products/detail/ProductDetailViewModel.kt`
- `presentation/screens/products/create/CreateProductScreen.kt`
- `presentation/screens/products/create/CreateProductViewModel.kt`
- `presentation/screens/products/search/SearchScreen.kt`
- `presentation/screens/products/search/SearchViewModel.kt`

**Profile:**
- `presentation/screens/profile/ProfileScreen.kt`
- `presentation/screens/profile/ProfileViewModel.kt`

**Map:**
- `presentation/screens/map/MapScreen.kt`
- `presentation/screens/map/MapViewModel.kt`

**Chat:**
- `presentation/screens/chat/conversations/ConversationsScreen.kt`
- `presentation/screens/chat/conversations/ConversationsViewModel.kt`
- `presentation/screens/chat/detail/ChatScreen.kt`
- `presentation/screens/chat/detail/ChatViewModel.kt`

### **13. DI Layer**
- `di/AppContainer.kt` - Contenedor de dependencias manual

### **14. MainActivity**
- `MainActivity.kt` - Punto de entrada con NavHost

### **15. Util**
- `util/Extensions.kt` - Extensiones útiles
- `util/Validators.kt` - Validaciones de formularios
- `util/DateUtils.kt` - Utilidades de fechas
- `util/ImageUtils.kt` - Utilidades de imágenes

## 🎯 REQUISITOS FUNCIONALES OBLIGATORIOS

La app DEBE cumplir estos requisitos (según rúbrica de evaluación):

### **Bloque A - Obligatorio (6 pts):**
1. **Autenticación y Sesión (1.5 pts)**
   - Login/Register funcional
   - Persistencia de sesión con EncryptedSharedPreferences
   - Auto-login al abrir app si hay sesión válida
   - Refresh token automático

2. **Arquitectura Desacoplada (1.5 pts)**
   - Clean Architecture bien implementada
   - Separación clara UI ↔ Domain ↔ Data
   - Repository pattern
   - Use Cases

3. **Procesos en Segundo Plano (1 pt)**
   - Todas las operaciones de red en coroutines
   - Estados Loading/Success/Error
   - Indicadores visuales (shimmer, progress)

4. **Gestión de Productos (1.5 pts)**
   - Listar productos con paginación
   - Detalle de producto
   - Crear producto con imágenes (selección múltiple)
   - Comprar producto

5. **Documentación (0.5 pts)**
   - KDoc en clases principales
   - Comentarios explicativos

### **Bloque B - Avanzado (4 pts):**
6. **Google Maps + Geolocalización (1 pt)**
   - Mostrar productos en mapa
   - Filtro por distancia
   - Geolocalización del usuario

7. **Búsqueda Avanzada (1 pt)**
   - Filtros (categoría, precio, estado)
   - Ordenación
   - ModalBottomSheet para filtros

8. **Chat (1 pt)**
   - Lista de conversaciones
   - Chat individual
   - Mensajes no leídos

9. **Estados Avanzados (1 pt)**
   - Sealed classes para estados
   - Pull-to-refresh
   - Paginación infinita
   - Retry en errores

## 📝 INSTRUCCIONES IMPORTANTES

1. **LEE `API_REST_Documentacion.md` PRIMERO** para conocer TODOS los endpoints exactos

2. **Sigue ESTRICTAMENTE Clean Architecture:**
   - Domain no debe conocer Data ni Presentation
   - Data implementa interfaces de Domain
   - Presentation solo depende de Domain

3. **Usa el formato de respuesta estándar de la API:**
```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null,
    val code: String? = null,
    val pagination: Pagination? = null
)
```

4. **Implementa UiState para TODAS las pantallas:**
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

5. **Usa Flow y StateFlow:**
```kotlin
// En Repository
fun getProducts(): Flow<List<Product>>

// En ViewModel
val uiState: StateFlow<UiState<List<Product>>>
```

6. **Implementa caché con SQLDelight:**
   - Mostrar datos de caché primero (instantáneo)
   - Actualizar desde API en segundo plano
   - Si falla API, mostrar caché con aviso

7. **Interceptor de Auth automático en Ktor:**
   - Añadir `Authorization: Bearer <token>` en todas las peticiones
   - Si recibe 401, intentar refresh token
   - Si refresh falla, logout automático

## 🎨 DISEÑO UI

- **Material 3** con tema morado
- **BottomNavigation** con 5 items: Productos, Buscar, Mapa, Chat, Perfil
- **Cards** para productos con imagen, nombre, precio
- **Shimmer effect** para loading de listas
- **Snackbar** para feedback de operaciones
- **Dialog** para confirmaciones importantes

## 🚀 ORDEN DE CREACIÓN RECOMENDADO

1. Theme completo (Color, Theme, Type, Shape)
2. Data Layer - Local (Preferences, Database)
3. Data Layer - Remote (Ktor, API, DTOs, DataSources)
4. Data Layer - Repositories
5. Domain Layer (Models, Repository Interfaces, Use Cases)
6. DI (AppContainer)
7. Presentation - Common (UiState, Components)
8. Presentation - Navigation
9. Presentation - Auth (Splash, Login, Register)
10. Presentation - Main (MainScreen con BottomNav)
11. Presentation - Products (List, Detail, Create, Search)
12. Presentation - Profile
13. Presentation - Map
14. Presentation - Chat
15. MainActivity

## ✅ CRITERIOS DE CALIDAD

- ✅ Código limpio y bien organizado
- ✅ Nombres descriptivos
- ✅ KDoc en clases públicas
- ✅ Manejo de errores en todos los flows
- ✅ Loading states en todas las operaciones
- ✅ Validaciones de formularios
- ✅ Sin code smells
- ✅ Siguiendo principios SOLID

## 🆘 SI TIENES DUDAS

1. **Endpoints exactos:** Consulta `API_REST_Documentacion.md`
2. **Modelos de datos:** Consulta `analisis-modulo-core-renaix.md`
3. **Arquitectura:** Pregúntame si algo no está claro

## 🎯 EMPECEMOS

Por favor, empieza creando los archivos en este orden:

1. `ui/theme/Theme.kt`
2. `ui/theme/Type.kt`
3. `ui/theme/Shape.kt`
4. `data/local/preferences/SecurePreferences.kt`
5. `data/remote/api/KtorClient.kt`

Y continúa con el resto siguiendo el orden recomendado arriba.

**IMPORTANTE:** Antes de crear cualquier DTO o endpoint, LEE `API_REST_Documentacion.md` para usar las estructuras EXACTAS de request/response.

¿Listo para empezar?