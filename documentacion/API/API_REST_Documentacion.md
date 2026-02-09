# Renaix API REST - Documentacion Sprint 2

## Informacion General

| Dato | Valor |
|------|-------|
| **Base URL** | `http://localhost:8069/api/v1` |
| **Formato** | JSON |
| **Autenticacion** | JWT Bearer Token |
| **Modulo Odoo** | `renaix_api` |
| **Version Odoo** | 18 |

### Autenticacion

Todos los endpoints protegidos requieren el header:

```
Authorization: Bearer <access_token>
```

Los tokens se obtienen mediante `/api/v1/auth/login` o `/api/v1/auth/register`.

- **Access Token**: Expira en 1 hora
- **Refresh Token**: Expira en 7 dias

### Formato de Respuesta Estandar

**Exito:**
```json
{
    "success": true,
    "message": "Operacion exitosa",
    "data": { ... }
}
```

**Error:**
```json
{
    "success": false,
    "error": "Descripcion del error",
    "code": "CODIGO_ERROR"
}
```

**Paginacion:**
```json
{
    "success": true,
    "message": "Datos recuperados",
    "data": [ ... ],
    "pagination": {
        "total": 50,
        "page": 1,
        "limit": 20,
        "total_pages": 3,
        "has_next": true,
        "has_prev": false
    }
}
```

---

## Entidades y Endpoints

### 1. Autenticacion (`Auth`)

Gestion del ciclo de vida de sesiones de usuario: registro, login, renovacion de tokens y logout.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Registro | `POST` | `/api/v1/auth/register` | No |
| Login | `POST` | `/api/v1/auth/login` | No |
| Renovar Token | `POST` | `/api/v1/auth/refresh` | No |
| Logout | `POST` | `/api/v1/auth/logout` | Si |

#### 1.1 Registro de Usuario

**`POST /api/v1/auth/register`**

Request:
```json
{
    "name": "Juan Perez",
    "email": "juan@example.com",
    "password": "miPassword123",
    "phone": "612345678"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Usuario registrado exitosamente",
    "data": {
        "access_token": "eyJ0eXAiOiJKV1Q...",
        "refresh_token": "eyJ0eXAiOiJKV1Q...",
        "user": {
            "id": 1,
            "name": "Juan Perez",
            "email": "juan@example.com",
            "phone": "612345678"
        }
    }
}
```

Error - Email duplicado (400):
```json
{
    "success": false,
    "error": "Ya existe un usuario con este email",
    "code": "VALIDATION_ERROR"
}
```

Error - Datos faltantes (400):
```json
{
    "success": false,
    "error": "Campos requeridos faltantes: name, email",
    "code": "VALIDATION_ERROR"
}
```

#### 1.2 Login

**`POST /api/v1/auth/login`**

Request:
```json
{
    "email": "juan@example.com",
    "password": "miPassword123"
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Login exitoso",
    "data": {
        "access_token": "eyJ0eXAiOiJKV1Q...",
        "refresh_token": "eyJ0eXAiOiJKV1Q...",
        "user": {
            "id": 1,
            "name": "Juan Perez",
            "email": "juan@example.com"
        }
    }
}
```

Error - Credenciales invalidas (401):
```json
{
    "success": false,
    "error": "Credenciales invalidas",
    "code": "UNAUTHORIZED"
}
```

#### 1.3 Renovar Token

**`POST /api/v1/auth/refresh`**

Request:
```json
{
    "refresh_token": "eyJ0eXAiOiJKV1Q..."
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Token renovado exitosamente",
    "data": {
        "access_token": "eyJ0eXAiOiJKV1Q..."
    }
}
```

Error - Token invalido (401):
```json
{
    "success": false,
    "error": "Token invalido o expirado",
    "code": "UNAUTHORIZED"
}
```

#### 1.4 Logout

**`POST /api/v1/auth/logout`** (Requiere autenticacion)

Sin body. Solo enviar header `Authorization`.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Logout exitoso"
}
```

---

### 2. Usuarios

Gestion del perfil del usuario autenticado, incluyendo datos personales, imagen de perfil, contrasena, y consulta de productos, compras, ventas, valoraciones y estadisticas propias.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Ver mi perfil | `GET` | `/api/v1/usuarios/perfil` | Si |
| Actualizar perfil | `PUT` | `/api/v1/usuarios/perfil` | Si |
| Subir imagen de perfil | `POST` | `/api/v1/usuarios/perfil/imagen` | Si |
| Eliminar imagen de perfil | `DELETE` | `/api/v1/usuarios/perfil/imagen` | Si |
| Cambiar contrasena | `PUT` | `/api/v1/usuarios/perfil/password` | Si |
| Ver perfil publico | `GET` | `/api/v1/usuarios/<user_id>` | No |
| Mis productos | `GET` | `/api/v1/usuarios/perfil/productos` | Si |
| Mis compras | `GET` | `/api/v1/usuarios/perfil/compras` | Si |
| Mis ventas | `GET` | `/api/v1/usuarios/perfil/ventas` | Si |
| Mis valoraciones | `GET` | `/api/v1/usuarios/perfil/valoraciones` | Si |
| Mis estadisticas | `GET` | `/api/v1/usuarios/perfil/estadisticas` | Si |

#### 2.1 Ver Mi Perfil

**`GET /api/v1/usuarios/perfil`** (Requiere autenticacion)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Perfil recuperado",
    "data": {
        "id": 1,
        "name": "Juan Perez",
        "email": "juan@example.com",
        "phone": "612345678",
        "image_url": "/web/image/res.partner/1/image_128",
        "productos_en_venta": 3,
        "productos_vendidos": 5,
        "valoracion_promedio": 4.5
    }
}
```

#### 2.2 Actualizar Perfil

**`PUT /api/v1/usuarios/perfil`** (Requiere autenticacion)

Request:
```json
{
    "name": "Juan P. Garcia",
    "phone": "698765432",
    "image": "data:image/jpeg;base64,/9j/4AAQ..."
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Perfil actualizado",
    "data": { ... }
}
```

Error - Telefono invalido (400):
```json
{
    "success": false,
    "error": "Formato de telefono invalido",
    "code": "VALIDATION_ERROR"
}
```

#### 2.3 Subir Imagen de Perfil

**`POST /api/v1/usuarios/perfil/imagen`** (Requiere autenticacion)

Request:
```json
{
    "image": "data:image/jpeg;base64,/9j/4AAQ..."
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Imagen de perfil actualizada",
    "data": { ... }
}
```

Error - Imagen muy grande (400):
```json
{
    "success": false,
    "error": "La imagen es demasiado grande. Tamano maximo: 5MB",
    "code": "VALIDATION_ERROR"
}
```

#### 2.4 Eliminar Imagen de Perfil

**`DELETE /api/v1/usuarios/perfil/imagen`** (Requiere autenticacion)

Sin body.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Imagen de perfil eliminada",
    "data": { ... }
}
```

#### 2.5 Cambiar Contrasena

**`PUT /api/v1/usuarios/perfil/password`** (Requiere autenticacion)

Request:
```json
{
    "password_actual": "miPasswordVieja",
    "password_nueva": "miPasswordNueva123"
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Contrasena actualizada correctamente"
}
```

Error - Contrasena actual incorrecta (400):
```json
{
    "success": false,
    "error": "La contrasena actual es incorrecta",
    "code": "VALIDATION_ERROR"
}
```

Error - Contrasena nueva debil (400):
```json
{
    "success": false,
    "error": "La contrasena debe tener al menos 6 caracteres",
    "code": "VALIDATION_ERROR"
}
```

#### 2.6 Ver Perfil Publico

**`GET /api/v1/usuarios/15`** (Sin autenticacion)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Usuario encontrado",
    "data": {
        "id": 15,
        "name": "Maria Lopez",
        "image_url": "/web/image/res.partner/15/image_128",
        "valoracion_promedio": 4.8
    }
}
```

Error - Usuario no encontrado (404):
```json
{
    "success": false,
    "error": "Usuario no encontrado",
    "code": "NOT_FOUND"
}
```

#### 2.7 Mis Productos

**`GET /api/v1/usuarios/perfil/productos?page=1&limit=10`** (Requiere autenticacion)

Respuesta exitosa (200) - paginada:
```json
{
    "success": true,
    "message": "Productos recuperados",
    "data": [
        {
            "id": 1,
            "nombre": "iPhone 12",
            "precio": 350.00,
            "estado_venta": "disponible",
            "imagenes": [...]
        }
    ],
    "pagination": {
        "total": 3,
        "page": 1,
        "limit": 10,
        "total_pages": 1,
        "has_next": false,
        "has_prev": false
    }
}
```

#### 2.8 Mis Compras / Mis Ventas / Mis Valoraciones / Mis Estadisticas

- **`GET /api/v1/usuarios/perfil/compras`** - Lista de compras realizadas
- **`GET /api/v1/usuarios/perfil/ventas`** - Lista de ventas realizadas
- **`GET /api/v1/usuarios/perfil/valoraciones`** - Valoraciones recibidas
- **`GET /api/v1/usuarios/perfil/estadisticas`** - Estadisticas del usuario

Ejemplo de estadisticas (200):
```json
{
    "success": true,
    "message": "Estadisticas recuperadas",
    "data": {
        "productos_en_venta": 3,
        "productos_vendidos": 5,
        "productos_comprados": 2,
        "valoracion_promedio": 4.5,
        "total_comentarios": 12,
        "total_denuncias_realizadas": 0
    }
}
```

---

### 3. Productos

Gestion completa del ciclo de vida de productos: listado publico, busqueda avanzada, creacion (en borrador), edicion, publicacion, gestion de imagenes y eliminacion.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Listar productos | `GET` | `/api/v1/productos` | No |
| Detalle producto | `GET` | `/api/v1/productos/<id>` | No |
| Busqueda avanzada | `GET` | `/api/v1/productos/buscar` | No |
| Crear producto | `POST` | `/api/v1/productos` | Si |
| Actualizar producto | `PUT` | `/api/v1/productos/<id>` | Si |
| Eliminar producto | `DELETE` | `/api/v1/productos/<id>` | Si |
| Publicar producto | `POST` | `/api/v1/productos/<id>/publicar` | Si |
| Anadir imagen | `POST` | `/api/v1/productos/<id>/imagenes` | Si |
| Eliminar imagen | `DELETE` | `/api/v1/productos/<id>/imagenes/<img_id>` | Si |

#### 3.1 Listar Productos (Publico)

**`GET /api/v1/productos?page=1&limit=20&estado_venta=disponible`**

Parametros opcionales: `page`, `limit`, `estado_venta`

Respuesta exitosa (200) - paginada:
```json
{
    "success": true,
    "message": "Productos recuperados",
    "data": [
        {
            "id": 1,
            "nombre": "iPhone 12",
            "descripcion": "Buen estado, 128GB",
            "precio": 350.00,
            "estado_venta": "disponible",
            "estado_producto": "como_nuevo",
            "ubicacion": "Madrid",
            "propietario": { "id": 1, "name": "Juan" },
            "categoria": { "id": 1, "name": "Electronica" },
            "imagenes": [...]
        }
    ],
    "pagination": { "total": 50, "page": 1, "limit": 20, "total_pages": 3 }
}
```

#### 3.2 Detalle de Producto

**`GET /api/v1/productos/1`**

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Producto encontrado",
    "data": {
        "id": 1,
        "nombre": "iPhone 12",
        "descripcion": "Buen estado, 128GB",
        "precio": 350.00,
        "estado_venta": "disponible",
        "estado_producto": "como_nuevo",
        "ubicacion": "Madrid",
        "propietario": { "id": 1, "name": "Juan", "valoracion_promedio": 4.5 },
        "categoria": { "id": 1, "name": "Electronica" },
        "etiquetas": [{ "id": 1, "name": "apple" }],
        "imagenes": [...],
        "comentarios": [...]
    }
}
```

Error - No encontrado (404):
```json
{
    "success": false,
    "error": "Producto no encontrado",
    "code": "NOT_FOUND"
}
```

#### 3.3 Busqueda Avanzada (Publico)

**`GET /api/v1/productos/buscar?query=iphone&categoria_id=1&precio_min=100&precio_max=500&orden=precio_asc`**

Parametros opcionales:
- `query` - Texto a buscar en nombre y descripcion
- `categoria_id` - Filtrar por categoria
- `etiquetas` - IDs separados por coma
- `precio_min`, `precio_max` - Rango de precio
- `estado_producto` - Estado fisico del producto
- `ubicacion` - Filtrar por ubicacion
- `orden` - `precio_asc`, `precio_desc`, `fecha_desc`, `fecha_asc`
- `page`, `limit` - Paginacion

Respuesta exitosa (200) - paginada:
```json
{
    "success": true,
    "message": "Se encontraron 5 productos",
    "data": [...],
    "pagination": { "total": 5, "page": 1, "limit": 20, "total_pages": 1 }
}
```

#### 3.4 Crear Producto

**`POST /api/v1/productos`** (Requiere autenticacion)

El producto se crea en estado `borrador`. Hay que anadir al menos una imagen y luego publicar.

Las etiquetas se pueden asignar de dos formas:
- `etiqueta_ids`: Array de IDs de etiquetas existentes
- `etiqueta_nombres`: Array de nombres de etiquetas (se crean automaticamente si no existen)

Ambas opciones se pueden combinar en la misma peticion.

Request:
```json
{
    "nombre": "iPhone 12 128GB",
    "descripcion": "Buen estado, sin arañazos. Incluye cargador.",
    "precio": 350.00,
    "categoria_id": 1,
    "estado_producto": "como_nuevo",
    "antiguedad": "6_meses",
    "ubicacion": "Madrid",
    "etiqueta_ids": [1, 3],
    "etiqueta_nombres": ["apple", "smartphone", "segunda-mano"]
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Producto creado exitosamente",
    "data": {
        "id": 10,
        "nombre": "iPhone 12 128GB",
        "precio": 350.00,
        "estado_venta": "borrador",
        "imagenes": []
    }
}
```

Error - Campos faltantes (400):
```json
{
    "success": false,
    "error": "Campos requeridos faltantes: nombre, precio, categoria_id",
    "code": "VALIDATION_ERROR"
}
```

Error - Precio invalido (400):
```json
{
    "success": false,
    "error": "Precio invalido. Debe estar entre 0 y 1.000.000",
    "code": "VALIDATION_ERROR"
}
```

#### 3.5 Actualizar Producto

**`PUT /api/v1/productos/10`** (Requiere autenticacion - solo propietario)

Request (todos los campos opcionales):
```json
{
    "nombre": "iPhone 12 128GB - REBAJADO",
    "precio": 299.00,
    "descripcion": "Rebajado! Buen estado."
}
```

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Producto actualizado",
    "data": { ... }
}
```

Error - Sin permiso (403):
```json
{
    "success": false,
    "error": "No tienes permiso para editar este producto",
    "code": "FORBIDDEN"
}
```

#### 3.6 Eliminar Producto

**`DELETE /api/v1/productos/10`** (Requiere autenticacion - solo propietario)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Producto eliminado exitosamente"
}
```

Error - Producto reservado (400):
```json
{
    "success": false,
    "error": "No se puede eliminar un producto reservado o vendido",
    "code": "VALIDATION_ERROR"
}
```

#### 3.7 Publicar Producto

**`POST /api/v1/productos/10/publicar`** (Requiere autenticacion - solo propietario)

Sin body. Cambia el estado de `borrador` a `disponible`. Requiere que el producto tenga al menos una imagen.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Producto publicado exitosamente",
    "data": {
        "id": 10,
        "estado_venta": "disponible",
        "fecha_publicacion": "2025-01-20 10:30:00"
    }
}
```

Error - Sin imagenes (400):
```json
{
    "success": false,
    "error": "El producto debe tener al menos una imagen para ser publicado.",
    "code": "VALIDATION_ERROR"
}
```

Error - Ya publicado (400):
```json
{
    "success": false,
    "error": "El producto ya esta publicado",
    "code": "VALIDATION_ERROR"
}
```

#### 3.8 Anadir Imagen a Producto

**`POST /api/v1/productos/10/imagenes`** (Requiere autenticacion - solo propietario)

Request:
```json
{
    "image": "data:image/jpeg;base64,/9j/4AAQ...",
    "es_principal": true,
    "descripcion": "Vista frontal"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Imagen anadida exitosamente",
    "data": {
        "id": 5,
        "url_imagen": "/web/image/renaix.producto.imagen/5/imagen",
        "es_principal": true,
        "descripcion": "Vista frontal"
    }
}
```

Error - Limite de imagenes (400):
```json
{
    "success": false,
    "error": "Maximo 10 imagenes por producto",
    "code": "VALIDATION_ERROR"
}
```

Error - Base64 invalido (400):
```json
{
    "success": false,
    "error": "Imagen en formato base64 invalido",
    "code": "VALIDATION_ERROR"
}
```

#### 3.9 Eliminar Imagen de Producto

**`DELETE /api/v1/productos/10/imagenes/5`** (Requiere autenticacion - solo propietario)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Imagen eliminada exitosamente"
}
```

---

### 4. Compras

Gestion del flujo de compra-venta: crear solicitud de compra, confirmar (vendedor), completar (comprador confirma recepcion), cancelar (ambas partes).

**Flujo de estados:**
```
pendiente --> confirmada (vendedor) --> completada (comprador)
     |             |
     v             v
  cancelada    cancelada
```

| Accion | Metodo | Endpoint | Auth | Quien |
|--------|--------|----------|------|-------|
| Crear compra | `POST` | `/api/v1/compras` | Si | Comprador |
| Detalle compra | `GET` | `/api/v1/compras/<id>` | Si | Ambas partes |
| Confirmar compra | `POST` | `/api/v1/compras/<id>/confirmar` | Si | Vendedor |
| Completar compra | `POST` | `/api/v1/compras/<id>/completar` | Si | Comprador |
| Cancelar compra | `POST` | `/api/v1/compras/<id>/cancelar` | Si | Ambas partes |

#### 4.1 Crear Compra

**`POST /api/v1/compras`** (Requiere autenticacion)

Request:
```json
{
    "producto_id": 10,
    "notas": "Me interesa, puedo recogerlo manana"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Compra creada exitosamente",
    "data": {
        "id": 1,
        "producto": { "id": 10, "nombre": "iPhone 12" },
        "comprador": { "id": 2, "name": "Maria" },
        "vendedor": { "id": 1, "name": "Juan" },
        "precio_final": 350.00,
        "estado": "pendiente",
        "fecha_compra": "2025-01-20 10:30:00"
    }
}
```

Error - Producto propio (400):
```json
{
    "success": false,
    "error": "No puedes comprar tu propio producto",
    "code": "VALIDATION_ERROR"
}
```

Error - No disponible (400):
```json
{
    "success": false,
    "error": "Producto no disponible",
    "code": "VALIDATION_ERROR"
}
```

#### 4.2 Detalle de Compra

**`GET /api/v1/compras/1`** (Requiere autenticacion - comprador o vendedor)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Compra encontrada",
    "data": {
        "id": 1,
        "producto": { ... },
        "comprador": { ... },
        "vendedor": { ... },
        "precio_final": 350.00,
        "estado": "pendiente",
        "fecha_compra": "2025-01-20 10:30:00",
        "notas": "Me interesa"
    }
}
```

Error - Sin permiso (403):
```json
{
    "success": false,
    "error": "No tienes permiso",
    "code": "FORBIDDEN"
}
```

#### 4.3 Confirmar Compra (Vendedor)

**`POST /api/v1/compras/1/confirmar`** (Requiere autenticacion - solo vendedor)

Sin body. Cambia estado de `pendiente` a `confirmada`. Reserva el producto.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Compra confirmada",
    "data": {
        "id": 1,
        "estado": "confirmada"
    }
}
```

Error - No es vendedor (403):
```json
{
    "success": false,
    "error": "Solo el vendedor puede confirmar",
    "code": "FORBIDDEN"
}
```

Error - Estado incorrecto (400):
```json
{
    "success": false,
    "error": "La compra no esta en estado pendiente",
    "code": "VALIDATION_ERROR"
}
```

#### 4.4 Completar Compra (Comprador)

**`POST /api/v1/compras/1/completar`** (Requiere autenticacion - solo comprador)

Sin body. Cambia estado de `confirmada` a `completada`. Marca el producto como `vendido`.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Compra completada",
    "data": {
        "id": 1,
        "estado": "completada"
    }
}
```

Error - No esta confirmada (400):
```json
{
    "success": false,
    "error": "La compra debe estar confirmada primero",
    "code": "VALIDATION_ERROR"
}
```

#### 4.5 Cancelar Compra

**`POST /api/v1/compras/1/cancelar`** (Requiere autenticacion - comprador o vendedor)

Sin body. Libera el producto si estaba reservado.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Compra cancelada",
    "data": {
        "id": 1,
        "estado": "cancelada"
    }
}
```

Error - Ya completada (400):
```json
{
    "success": false,
    "error": "No se puede cancelar una compra completada",
    "code": "VALIDATION_ERROR"
}
```

---

### 5. Comentarios

Los usuarios pueden comentar en productos. El propietario del producto recibe una notificacion. Los usuarios solo pueden eliminar sus propios comentarios.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Listar comentarios | `GET` | `/api/v1/productos/<id>/comentarios` | No |
| Crear comentario | `POST` | `/api/v1/productos/<id>/comentarios` | Si |
| Eliminar comentario | `DELETE` | `/api/v1/comentarios/<id>` | Si |

#### 5.1 Listar Comentarios de un Producto

**`GET /api/v1/productos/10/comentarios`**

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Comentarios recuperados",
    "data": [
        {
            "id": 1,
            "texto": "Esta en muy buen estado?",
            "usuario": { "id": 2, "name": "Maria" },
            "fecha": "2025-01-20 10:30:00"
        },
        {
            "id": 2,
            "texto": "Haces envios?",
            "usuario": { "id": 3, "name": "Pedro" },
            "fecha": "2025-01-20 11:00:00"
        }
    ]
}
```

#### 5.2 Crear Comentario

**`POST /api/v1/productos/10/comentarios`** (Requiere autenticacion)

Request:
```json
{
    "texto": "Haces envios a Barcelona?"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Comentario creado",
    "data": {
        "id": 3,
        "texto": "Haces envios a Barcelona?",
        "usuario": { "id": 2, "name": "Maria" },
        "fecha": "2025-01-20 12:00:00"
    }
}
```

Error - Comentario vacio (400):
```json
{
    "success": false,
    "error": "El comentario no puede estar vacio",
    "code": "VALIDATION_ERROR"
}
```

Error - Comentario corto (400):
```json
{
    "success": false,
    "error": "El comentario debe tener al menos 3 caracteres",
    "code": "VALIDATION_ERROR"
}
```

#### 5.3 Eliminar Comentario

**`DELETE /api/v1/comentarios/3`** (Requiere autenticacion - solo autor)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Comentario eliminado"
}
```

Error - Sin permiso (403):
```json
{
    "success": false,
    "error": "No tienes permiso",
    "code": "FORBIDDEN"
}
```

---

### 6. Valoraciones

Sistema de reputacion entre compradores y vendedores. Solo se puede valorar una transaccion completada. Cada parte puede valorar a la otra una sola vez por compra (puntuacion de 1 a 5).

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Valorar transaccion | `POST` | `/api/v1/compras/<id>/valorar` | Si |
| Ver valoraciones de usuario | `GET` | `/api/v1/usuarios/<id>/valoraciones` | Si |

#### 6.1 Valorar una Transaccion

**`POST /api/v1/compras/1/valorar`** (Requiere autenticacion - comprador o vendedor)

Request:
```json
{
    "puntuacion": 5,
    "comentario": "Excelente vendedor, producto en perfecto estado"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Valoracion creada",
    "data": {
        "id": 1,
        "puntuacion": 5,
        "comentario": "Excelente vendedor, producto en perfecto estado",
        "tipo_valoracion": "comprador_a_vendedor",
        "fecha": "2025-01-20 14:00:00"
    }
}
```

Error - Compra no completada (400):
```json
{
    "success": false,
    "error": "Solo se pueden valorar compras completadas",
    "code": "VALIDATION_ERROR"
}
```

Error - Ya valorado (400):
```json
{
    "success": false,
    "error": "Ya has valorado esta transaccion",
    "code": "VALIDATION_ERROR"
}
```

Error - Puntuacion invalida (400):
```json
{
    "success": false,
    "error": "La puntuacion debe estar entre 1 y 5",
    "code": "VALIDATION_ERROR"
}
```

#### 6.2 Ver Valoraciones de un Usuario

**`GET /api/v1/usuarios/1/valoraciones`** (Requiere autenticacion)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Valoraciones recuperadas",
    "data": [
        {
            "id": 1,
            "puntuacion": 5,
            "comentario": "Excelente vendedor",
            "tipo_valoracion": "comprador_a_vendedor",
            "valorador": { "id": 2, "name": "Maria" },
            "fecha": "2025-01-20 14:00:00"
        }
    ]
}
```

---

### 7. Mensajes

Sistema de mensajeria directa entre usuarios. Los mensajes se agrupan por hilos de conversacion y pueden estar asociados a un producto.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Listar conversaciones | `GET` | `/api/v1/mensajes/conversaciones` | Si |
| Ver conversacion con usuario | `GET` | `/api/v1/mensajes/conversacion/<user_id>` | Si |
| Mensajes no leidos | `GET` | `/api/v1/mensajes/no-leidos` | Si |
| Enviar mensaje | `POST` | `/api/v1/mensajes` | Si |
| Marcar como leido | `PUT` | `/api/v1/mensajes/<id>/marcar-leido` | Si |

#### 7.1 Listar Conversaciones

**`GET /api/v1/mensajes/conversaciones`** (Requiere autenticacion)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Conversaciones recuperadas",
    "data": [
        {
            "hilo_id": "conv_1_2",
            "participantes": [...],
            "ultimo_mensaje": {
                "texto": "Perfecto, quedamos manana",
                "fecha": "2025-01-20 15:00:00"
            },
            "mensajes": [...]
        }
    ]
}
```

#### 7.2 Enviar Mensaje

**`POST /api/v1/mensajes`** (Requiere autenticacion)

Request:
```json
{
    "receptor_id": 15,
    "texto": "Hola, me interesa tu producto. Sigue disponible?",
    "producto_id": 10
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Mensaje enviado",
    "data": {
        "id": 1,
        "emisor": { "id": 2, "name": "Maria" },
        "receptor": { "id": 15, "name": "Juan" },
        "texto": "Hola, me interesa tu producto. Sigue disponible?",
        "leido": false,
        "fecha": "2025-01-20 15:30:00"
    }
}
```

Error - Mensaje vacio (400):
```json
{
    "success": false,
    "error": "El mensaje no puede estar vacio",
    "code": "VALIDATION_ERROR"
}
```

Error - Mensaje largo (400):
```json
{
    "success": false,
    "error": "El mensaje no puede superar 2000 caracteres",
    "code": "VALIDATION_ERROR"
}
```

#### 7.3 Ver Conversacion con Usuario

**`GET /api/v1/mensajes/conversacion/15`** (Requiere autenticacion)

Obtiene todos los mensajes intercambiados con un usuario especifico, ordenados cronologicamente.

Parametros opcionales: `producto_id` (filtrar por producto)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Conversacion recuperada",
    "data": [
        {
            "id": 1,
            "texto": "Hola, sigue disponible?",
            "emisor": { "id": 2, "name": "Maria" },
            "receptor": { "id": 15, "name": "Juan" },
            "leido": true,
            "fecha": "2025-01-20 15:00:00",
            "hilo_id": "hilo_2_15_10"
        },
        {
            "id": 2,
            "texto": "Si, cuando quieras",
            "emisor": { "id": 15, "name": "Juan" },
            "receptor": { "id": 2, "name": "Maria" },
            "leido": false,
            "fecha": "2025-01-20 15:05:00",
            "hilo_id": "hilo_2_15_10"
        }
    ]
}
```

Error - Usuario no encontrado (404):
```json
{
    "success": false,
    "error": "Usuario no encontrado",
    "code": "NOT_FOUND"
}
```

#### 7.4 Mensajes No Leidos

**`GET /api/v1/mensajes/no-leidos`** (Requiere autenticacion)

Obtiene todos los mensajes no leidos del usuario autenticado, con el total.

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Mensajes no leidos recuperados",
    "data": {
        "total": 3,
        "mensajes": [
            {
                "id": 5,
                "texto": "Te interesa hacer el intercambio manana?",
                "emisor": { "id": 15, "name": "Juan" },
                "leido": false,
                "fecha": "2025-01-20 16:00:00"
            }
        ]
    }
}
```

#### 7.5 Marcar Mensaje como Leido

**`PUT /api/v1/mensajes/1/marcar-leido`** (Requiere autenticacion - solo receptor)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Mensaje marcado como leido"
}
```

Error - Sin permiso (403):
```json
{
    "success": false,
    "error": "No tienes permiso",
    "code": "FORBIDDEN"
}
```

---

### 8. Denuncias

Sistema de reporte para contenido inapropiado. Los usuarios pueden denunciar productos, comentarios u otros usuarios indicando el tipo, motivo y categoria de la denuncia.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Crear denuncia | `POST` | `/api/v1/denuncias` | Si |
| Mis denuncias | `GET` | `/api/v1/denuncias/mis-denuncias` | Si |

#### 8.1 Crear Denuncia

**`POST /api/v1/denuncias`** (Requiere autenticacion)

Request (denunciar producto):
```json
{
    "tipo": "producto",
    "motivo": "El producto publicado es falsificado y engana a los compradores",
    "categoria": "fraude",
    "producto_id": 10
}
```

Request (denunciar comentario):
```json
{
    "tipo": "comentario",
    "motivo": "El comentario contiene lenguaje ofensivo e insultos",
    "categoria": "contenido_ofensivo",
    "comentario_id": 5
}
```

Request (denunciar usuario):
```json
{
    "tipo": "usuario",
    "motivo": "Este usuario ha estafado a varios compradores segun resenas",
    "categoria": "estafa",
    "usuario_reportado_id": 15
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Denuncia creada",
    "data": {
        "id": 1,
        "tipo": "producto",
        "motivo": "El producto publicado es falsificado",
        "categoria": "fraude",
        "estado": "pendiente",
        "fecha_denuncia": "2025-01-20 16:00:00"
    }
}
```

Error - Tipo invalido (400):
```json
{
    "success": false,
    "error": "Tipo invalido. Debe ser uno de: producto, comentario, usuario",
    "code": "VALIDATION_ERROR"
}
```

Error - Motivo corto (400):
```json
{
    "success": false,
    "error": "El motivo debe tener al menos 10 caracteres",
    "code": "VALIDATION_ERROR"
}
```

#### 8.2 Mis Denuncias

**`GET /api/v1/denuncias/mis-denuncias`** (Requiere autenticacion)

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Denuncias recuperadas",
    "data": [
        {
            "id": 1,
            "tipo": "producto",
            "motivo": "El producto publicado es falsificado",
            "estado": "pendiente",
            "fecha_denuncia": "2025-01-20 16:00:00"
        }
    ]
}
```

---

### 9. Categorias

Listado de categorias disponibles para clasificar productos.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Listar categorias | `GET` | `/api/v1/categorias` | No |

#### 9.1 Listar Categorias

**`GET /api/v1/categorias`**

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Categorias recuperadas",
    "data": [
        { "id": 1, "name": "Electronica", "descripcion": "Dispositivos electronicos" },
        { "id": 2, "name": "Moda", "descripcion": "Ropa y accesorios" },
        { "id": 3, "name": "Hogar", "descripcion": "Articulos para el hogar" }
    ]
}
```

---

### 10. Etiquetas

Gestion de etiquetas (tags) para clasificar productos. Los usuarios autenticados pueden crear nuevas etiquetas, que tambien se crean automaticamente al asignar `etiqueta_nombres` al crear/actualizar un producto.

| Accion | Metodo | Endpoint | Auth |
|--------|--------|----------|------|
| Crear etiqueta | `POST` | `/api/v1/etiquetas` | Si |
| Listar populares | `GET` | `/api/v1/etiquetas` | No |
| Buscar etiquetas | `GET` | `/api/v1/etiquetas/buscar?q=<texto>` | No |

#### 10.1 Crear Etiqueta

**`POST /api/v1/etiquetas`** (Requiere autenticacion)

Si ya existe una etiqueta con el mismo nombre (case-insensitive), devuelve la existente sin crear duplicado.

Request:
```json
{
    "nombre": "gaming"
}
```

Respuesta exitosa (201):
```json
{
    "success": true,
    "message": "Etiqueta creada exitosamente",
    "data": {
        "id": 10,
        "nombre": "gaming",
        "producto_count": 0,
        "color": 0
    }
}
```

Respuesta - Ya existente (200):
```json
{
    "success": true,
    "message": "Etiqueta ya existente",
    "data": {
        "id": 3,
        "nombre": "gaming",
        "producto_count": 8,
        "color": 2
    }
}
```

Error - Nombre corto (400):
```json
{
    "success": false,
    "error": "El nombre debe tener al menos 2 caracteres",
    "code": "VALIDATION_ERROR"
}
```

#### 10.2 Listar Etiquetas Populares

**`GET /api/v1/etiquetas`**

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Etiquetas populares recuperadas",
    "data": [
        { "id": 1, "name": "apple", "producto_count": 15 },
        { "id": 2, "name": "samsung", "producto_count": 12 },
        { "id": 3, "name": "gaming", "producto_count": 8 }
    ]
}
```

#### 10.3 Buscar Etiquetas

**`GET /api/v1/etiquetas/buscar?q=apple`**

Respuesta exitosa (200):
```json
{
    "success": true,
    "message": "Se encontraron 2 etiquetas",
    "data": [
        { "id": 1, "name": "apple", "producto_count": 15 },
        { "id": 5, "name": "apple-watch", "producto_count": 3 }
    ]
}
```

Error - Busqueda corta (400):
```json
{
    "success": false,
    "error": "La busqueda debe tener al menos 2 caracteres",
    "code": "VALIDATION_ERROR"
}
```

---

## Resumen de Errores Comunes

| Codigo HTTP | Codigo Error | Descripcion |
|-------------|-------------|-------------|
| 400 | `VALIDATION_ERROR` | Datos invalidos o faltantes |
| 401 | `UNAUTHORIZED` | Token ausente, invalido o expirado |
| 403 | `FORBIDDEN` | Sin permiso para la accion |
| 404 | `NOT_FOUND` | Recurso no encontrado |
| 500 | `INTERNAL_ERROR` | Error interno del servidor |

---

## Evidencias de Funcionamiento

> **Nota:** A continuacion se deben incluir capturas de pantalla de Postman mostrando las pruebas realizadas. Se debe mostrar tanto casos exitosos como casos de error para cada seccion.

### Casos a probar con Postman

#### Autenticacion
-  Registro exitoso de nuevo usuario 

![Prueba Registro](.\img\registro_prueba.png)![Registro Exitoso](.\img\registro_exitoso.png)

- Error al registrar con email duplicado

![Registro Error](.\img\registro_error.png)

- Login exitoso

![Login Exitoso](.\img\login_exitoso.png)

- Error al hacer login con credenciales incorrectas

![Login Error](.\img\login_error.png)

- Renovacion de token exitosa

![Renovacion Token](.\img\renovacion_token.png)

- Logout exitoso

![Logout Exitoso](.\img\logout_exitoso.png)

#### Usuarios

- Consultar perfil propio

![Consultar Perfil](.\img\usuarios_perfil.png)

- Actualizar nombre del perfil

![Actualizar Perfil](.\img\usuarios_actualizar_perfil.png)

- Subir imagen de perfil (base64)

![Subir Imagen Perfil](.\img\usuarios_subir_imagen.png)

- Eliminar imagen de perfil

![Eliminar Imagen Perfil](.\img\usuarios_eliminar_imagen.png)

- Cambiar contrasena exitosamente

![Cambiar Contrasena](.\img\usuarios_cambiar_password.png)

- Error al cambiar contrasena con la actual incorrecta

![Cambiar Contrasena Error](.\img\usuarios_cambiar_password_error.png)

- Ver perfil publico de otro usuario

![Perfil Publico](.\img\usuarios_perfil_publico.png)

- Ver mis productos / compras / ventas / estadisticas

![Mis Estadisticas](.\img\usuarios_estadisticas.png)

#### Productos

- Listar productos disponibles (publico)

![Listar Productos](.\img\productos_listar.png)

- Ver detalle de un producto

![Detalle Producto](.\img\productos_detalle.png)

- Busqueda por texto, categoria y rango de precio

![Busqueda Avanzada](.\img\productos_busqueda.png)

- Crear producto con etiqueta_nombres (crear etiquetas automaticamente)

![Crear Producto](.\img\productos_crear.png)

- Subir imagen al producto

![Subir Imagen Producto](.\img\productos_subir_imagen.png)

- Publicar producto

![Publicar Producto](.\img\productos_publicar.png)

- Error al publicar sin imagenes

![Publicar Sin Imagenes Error](.\img\productos_publicar_error.png)

- Actualizar producto propio

![Actualizar Producto](.\img\productos_actualizar.png)

- Error al editar producto de otro usuario

![Editar Producto Error](.\img\productos_editar_error.png)

- Eliminar producto propio

![Eliminar Producto](.\img\productos_eliminar.png)

#### Compras (flujo completo)

- Crear compra de un producto

![Crear Compra](.\img\compras_crear.png)

- Error al comprar producto propio

![Comprar Propio Error](.\img\compras_propio_error.png)

- Confirmar compra (como vendedor)

![Confirmar Compra](.\img\compras_confirmar.png)

- Completar compra (como comprador)

![Completar Compra](.\img\compras_completar.png)

- Cancelar compra

![Cancelar Compra](.\img\compras_cancelar.png)

- Error al completar compra no confirmada

![Completar Sin Confirmar Error](.\img\compras_completar_error.png)

#### Comentarios

- Listar comentarios de un producto

![Listar Comentarios](.\img\comentarios_listar.png)

- Crear comentario

![Crear Comentario](.\img\comentarios_crear.png)

- Error con comentario vacio

![Comentario Vacio Error](.\img\comentarios_vacio_error.png)

- Eliminar comentario propio

![Eliminar Comentario](.\img\comentarios_eliminar.png)

- Error al eliminar comentario de otro usuario

![Eliminar Comentario Error](.\img\comentarios_eliminar_error.png)

#### Valoraciones

- Valorar compra completada

![Valorar Compra](.\img\valoraciones_crear.png)

- Error al valorar compra no completada

![Valorar No Completada Error](.\img\valoraciones_no_completada_error.png)

- Error al valorar dos veces

![Valorar Duplicada Error](.\img\valoraciones_duplicada_error.png)

- Ver valoraciones de un usuario

![Ver Valoraciones](.\img\valoraciones_listar.png)

#### Mensajes

- Enviar mensaje a otro usuario

![Enviar Mensaje](.\img\mensajes_enviar.png)

- Listar conversaciones

![Listar Conversaciones](.\img\mensajes_conversaciones.png)

- Ver conversacion con usuario especifico

![Ver Conversacion](.\img\mensajes_conversacion.png)

- Consultar mensajes no leidos

![Mensajes No Leidos](.\img\mensajes_no_leidos.png)

- Marcar mensaje como leido

![Marcar Leido](.\img\mensajes_marcar_leido.png)

#### Denuncias

- Crear denuncia de producto

![Crear Denuncia](.\img\denuncias_crear.png)

- Error con motivo muy corto

![Denuncia Motivo Corto Error](.\img\denuncias_motivo_error.png)

- Ver mis denuncias

![Mis Denuncias](.\img\denuncias_listar.png)

#### Etiquetas

- Crear etiqueta nueva

![Crear Etiqueta](.\img\etiquetas_crear.png)

- Crear etiqueta duplicada (devuelve existente)

![Etiqueta Duplicada](.\img\etiquetas_duplicada.png)

- Listar etiquetas populares

![Listar Etiquetas](.\img\etiquetas_listar.png)

- Buscar etiquetas por nombre

![Buscar Etiquetas](.\img\etiquetas_buscar.png)


<!--
INSTRUCCIONES: Reemplazar los checkboxes con capturas de Postman.
Para cada seccion, incluir al menos:
  - 1 captura de caso exitoso (status 200/201)
  - 1 captura de caso de error (status 400/401/403/404)

Formato sugerido:
![Descripcion](.\img\registro_prueba.png)
-->
