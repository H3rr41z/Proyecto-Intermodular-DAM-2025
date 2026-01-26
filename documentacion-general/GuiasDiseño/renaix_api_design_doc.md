# 📘 Renaix API REST - Guía Completa de Diseño e Implementación

**Proyecto:** Marketplace de Segunda Mano - Sprint 2  
**Autores:** Javier Herraiz & Alejandro Sánchez  
**Versión:** 1.0.0  
**Fecha:** Enero 2025

---

## 📑 Tabla de Contenidos

1. [Introducción al Proyecto](#1-introducción-al-proyecto)
2. [Conceptos Fundamentales](#2-conceptos-fundamentales)
3. [Arquitectura de la Solución](#3-arquitectura-de-la-solución)
4. [Diseño de Endpoints](#4-diseño-de-endpoints)
5. [Estructura del Módulo](#5-estructura-del-módulo)
6. [Buenas Prácticas de Desarrollo](#6-buenas-prácticas-de-desarrollo)
7. [Logging y Monitorización](#7-logging-y-monitorización)
8. [Seguridad y Configuración](#8-seguridad-y-configuración)
9. [Librerías Recomendadas](#9-librerías-recomendadas)
10. [Checklist de Implementación](#10-checklist-de-implementación)
11. [Anexos](#11-anexos)

---

## 1. Introducción al Proyecto

### 1.1 Objetivo del Sprint 2

Diseñar e implementar una **API REST segura con JWT** que permita el funcionamiento completo de una aplicación móvil de compra-venta de productos de segunda mano, utilizando Odoo como backend.

### 1.2 ¿Qué estamos construyendo?

Una **capa API REST** que actúa como puente entre la app móvil y el módulo Renaix:

```
┌─────────────────┐         ┌──────────────┐         ┌─────────────────┐
│   App Móvil     │ ◄──────►│  API REST    │ ◄──────►│  Módulo Renaix  │
│   (Kotlin)      │   JSON  │  (renaix_api)│   ORM   │  (Base de datos)│
│                 │   JWT   │              │         │                 │
└─────────────────┘         └──────────────┘         └─────────────────┘
```

### 1.3 Requisitos Técnicos Obligatorios

- ✅ Autenticación basada en **JWT** (PyJWT)
- ✅ Token con **caducidad**
- ✅ Verificación del token en **TODOS** los endpoints protegidos
- ✅ Función **centralizada** de verificación reutilizable
- ✅ Respuestas en **JSON** con códigos HTTP coherentes
- ✅ **Versionado** de API: `/api/v1/...`

### 1.4 Funcionalidades Mínimas

La API debe permitir:

- Registro e inicio de sesión de usuarios
- Publicar y gestionar productos (CRUD)
- Búsqueda y consulta de productos
- Compra de productos con cambio de estado
- Comentarios en productos
- Valoraciones de usuarios
- Denuncias de contenido
- Chat/mensajería entre usuarios
- Gestión de imágenes de productos

---

## 2. Conceptos Fundamentales

### 2.1 Flujo de una Petición API

```
1. 📱 App móvil → Envía petición HTTP con JWT
2. 🔐 API → Valida token JWT
3. 🔍 API → Busca/crea/modifica datos en modelos de renaix
4. 📦 API → Devuelve JSON a la app
5. 📱 App móvil → Muestra datos al usuario
```

### 2.2 Separación de Responsabilidades (MVC)

```
┌─────────────────────────────────────────────────────────┐
│                  MÓDULO renaix_api                       │
├─────────────────────────────────────────────────────────┤
│ CONTROLLERS (C)                                          │
│  ├── auth.py          → Login, registro                 │
│  ├── productos.py     → CRUD productos                   │
│  └── ...              → Resto de endpoints               │
│                                                          │
│ UTILS (Lógica compartida)                               │
│  ├── jwt_utils.py     → Generar/verificar tokens        │
│  └── validators.py    → Validaciones comunes            │
└─────────────────────────────────────────────────────────┘
                         ▼ Usa modelos de
┌─────────────────────────────────────────────────────────┐
│                  MÓDULO renaix                           │
├─────────────────────────────────────────────────────────┤
│ MODELS (M)                                               │
│  ├── producto.py      → Lógica de negocio productos     │
│  ├── compra.py        → Lógica transacciones            │
│  └── ...              → Resto de modelos                 │
└─────────────────────────────────────────────────────────┘
```

**Principio clave:** La API **NO duplica lógica de negocio**, solo expone endpoints HTTP que usan los modelos existentes.

### 2.3 Actores del Sistema

```
┌─────────────────────────────────────────────────────────┐
│                     ACTORES                              │
├─────────────────────────────────────────────────────────┤
│ 1. USUARIO NO REGISTRADO (Anónimo)                     │
│    - Ver productos públicos                             │
│    - Registrarse                                        │
│    - Hacer login                                        │
│                                                          │
│ 2. USUARIO REGISTRADO (Comprador/Vendedor)             │
│    - CRUD sus productos                                 │
│    - Comprar productos                                  │
│    - Comentar productos                                 │
│    - Valorar compradores/vendedores                     │
│    - Denunciar contenido                                │
│    - Enviar mensajes privados                           │
│    - Ver su perfil y estadísticas                       │
│                                                          │
│ 3. EMPLEADO/MODERADOR (Desde Odoo ERP)                 │
│    - Gestionar denuncias                                │
│    - Suspender usuarios                                 │
│    - Ver estadísticas globales                          │
│    ⚠️ NO necesita endpoints API - usa Odoo web          │
└─────────────────────────────────────────────────────────┘
```

### 2.4 Entidades del Dominio

```
ENTIDADES PRINCIPALES:
├── res.partner (Usuario)
├── renaix.producto (Producto)
├── renaix.producto.imagen (Imágenes)
├── renaix.categoria (Categoría)
├── renaix.etiqueta (Etiqueta)
├── renaix.compra (Transacción/Compra)
├── renaix.valoracion (Valoración)
├── renaix.comentario (Comentario)
├── renaix.mensaje (Mensaje/Chat)
└── renaix.denuncia (Denuncia)
```

---

## 3. Arquitectura de la Solución

### 3.1 Estructura del Módulo renaix_api

```
renaix_api/
│
├── __manifest__.py                     # Dependencias y metadatos
├── __init__.py                         # Importa controllers y models
│
├── controllers/
│   ├── __init__.py
│   ├── auth.py                         # Login, registro, refresh token
│   ├── usuarios.py                     # Perfil usuario
│   ├── productos.py                    # CRUD productos + búsqueda
│   ├── compras.py                      # Flujo compra-venta
│   ├── comentarios.py                  # Comentarios
│   ├── valoraciones.py                 # Valoraciones
│   ├── denuncias.py                    # Denuncias
│   ├── mensajes.py                     # Chat
│   ├── etiquetas.py                    # Listar etiquetas
│   └── categorias.py                   # Listar categorías
│
├── models/
│   ├── __init__.py
│   └── utils/
│       ├── __init__.py
│       ├── jwt_utils.py                # ⭐ Funciones JWT centralizadas
│       ├── validators.py               # Validaciones reutilizables
│       ├── serializers.py              # Serialización modelos → JSON
│       ├── response_helpers.py         # Respuestas HTTP estandarizadas
│       └── decorators.py               # Decoradores (logging, etc.)
│
├── config/
│   └── settings.py                     # Configuración (SECRET_KEY, etc.)
│
└── tests/                              # ⚠️ OPCIONAL pero recomendado
    ├── test_auth.py
    ├── test_productos.py
    └── ...
```

### 3.2 Principios REST

1. **Recursos como sustantivos**: `/productos` ✅ no `/obtenerProductos` ❌
2. **Métodos HTTP** indican la acción:
   - `GET` → Leer/Consultar
   - `POST` → Crear
   - `PUT` / `PATCH` → Actualizar
   - `DELETE` → Eliminar
3. **URLs jerárquicas**: `/productos/123/comentarios`
4. **Versionado**: `/api/v1/...`

---

## 4. Diseño de Endpoints

### 4.1 Autenticación (Sin token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `POST` | `/api/v1/auth/login` | Login de usuario | `{email, password}` | `{token, user}` |
| `POST` | `/api/v1/auth/register` | Registro de usuario | `{name, email, password, telefono}` | `{token, user}` |
| `GET` | `/api/v1/auth/verify` | Verificar token válido | - | `{valid, user_id}` |
| `POST` | `/api/v1/auth/refresh` | Renovar token (OPCIONAL) | `{refresh_token}` | `{token}` |

**Ejemplo Request Login:**
```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "mipassword123"
}
```

**Ejemplo Response Login (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 42,
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "telefono": "+34600123456",
    "puntuacion_media": 4.5,
    "productos_vendidos": 12
  }
}
```

**Ejemplo Response Error (401):**
```json
{
  "error": "Credenciales inválidas"
}
```

---

### 4.2 Usuarios (Token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `GET` | `/api/v1/usuarios/perfil` | Obtener perfil propio | - | `{id, name, email, ...}` |
| `PUT` | `/api/v1/usuarios/perfil` | Actualizar perfil | `{name, telefono}` | `{message, user}` |
| `GET` | `/api/v1/usuarios/{id}` | Ver perfil público de otro usuario | - | `{id, name, puntuacion, ...}` |
| `GET` | `/api/v1/usuarios/perfil/productos` | Mis productos publicados | - | `[{producto}, ...]` |
| `GET` | `/api/v1/usuarios/perfil/compras` | Mis compras realizadas | - | `[{compra}, ...]` |
| `GET` | `/api/v1/usuarios/perfil/ventas` | Mis ventas | - | `[{venta}, ...]` |
| `GET` | `/api/v1/usuarios/perfil/valoraciones` | Mis valoraciones recibidas | - | `[{valoracion}, ...]` |

**Ejemplo Request Perfil:**
```
GET /api/v1/usuarios/perfil
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo Response (200):**
```json
{
  "id": 42,
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "+34600123456",
  "puntuacion_media": 4.5,
  "productos_vendidos": 12,
  "productos_comprados": 8,
  "estado": "activo",
  "fecha_registro": "2024-12-15T10:30:00Z"
}
```

---

### 4.3 Productos (Token requerido)

| Método | Endpoint | Descripción | Query Params | Body | Response |
|--------|----------|-------------|--------------|------|----------|
| `GET` | `/api/v1/productos` | Listar productos | `?categoria_id=1&search=iphone&estado=disponible&limit=20&offset=0` | - | `{productos: [...], total, pagination}` |
| `GET` | `/api/v1/productos/{id}` | Detalle de producto | - | - | `{id, nombre, precio, vendedor, imagenes, comentarios, ...}` |
| `POST` | `/api/v1/productos` | Crear producto | - | `{nombre, descripcion, precio, categoria_id, etiqueta_ids, imagenes[]}` | `{message, producto}` |
| `PUT` | `/api/v1/productos/{id}` | Actualizar producto | - | `{nombre?, precio?, descripcion?, estado?}` | `{message, producto}` |
| `DELETE` | `/api/v1/productos/{id}` | Eliminar producto | - | - | `{message}` |
| `POST` | `/api/v1/productos/{id}/imagenes` | Añadir imagen | - | `multipart/form-data: {imagen}` | `{message, imagen_id}` |

**Ejemplo Request Listar Productos:**
```
GET /api/v1/productos?categoria_id=2&search=iphone&limit=10&offset=0
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Ejemplo Response (200):**
```json
{
  "productos": [
    {
      "id": 15,
      "nombre": "iPhone 12 Pro 128GB",
      "descripcion": "Seminuevo, estado impecable",
      "precio": 650.00,
      "estado": "disponible",
      "vendedor": {
        "id": 42,
        "name": "Juan Pérez",
        "puntuacion": 4.5
      },
      "categoria": {
        "id": 2,
        "nombre": "Electrónica"
      },
      "etiquetas": [
        {"id": 5, "nombre": "smartphone"},
        {"id": 12, "nombre": "apple"}
      ],
      "imagenes": ["base64_imagen_1...", "base64_imagen_2..."],
      "fecha_publicacion": "2025-01-15T10:00:00Z"
    }
  ],
  "total": 42,
  "limit": 10,
  "offset": 0
}
```

**Ejemplo Request Crear Producto:**
```json
POST /api/v1/productos
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "nombre": "iPhone 12 Pro 128GB",
  "descripcion": "Seminuevo, comprado hace 1 año",
  "precio": 650.00,
  "categoria_id": 2,
  "etiqueta_ids": [5, 12, 23],
  "imagenes": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
  ]
}
```

**Ejemplo Response Crear (201):**
```json
{
  "message": "Producto creado correctamente",
  "producto": {
    "id": 15,
    "nombre": "iPhone 12 Pro 128GB",
    "precio": 650.00,
    "estado": "borrador"
  }
}
```

---

### 4.4 Categorías y Etiquetas (Públicas)

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| `GET` | `/api/v1/categorias` | Listar categorías | `[{id, nombre, icono}, ...]` |
| `GET` | `/api/v1/etiquetas` | Listar etiquetas populares | `[{id, nombre}, ...]` |

---

### 4.5 Comentarios (Token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `GET` | `/api/v1/productos/{id}/comentarios` | Comentarios de un producto | - | `[{id, usuario, texto, fecha}, ...]` |
| `POST` | `/api/v1/productos/{id}/comentarios` | Añadir comentario | `{texto}` | `{message, comentario}` |
| `DELETE` | `/api/v1/comentarios/{id}` | Eliminar comentario propio | - | `{message}` |

---

### 4.6 Compras/Transacciones (Token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `POST` | `/api/v1/compras` | Crear compra (reservar) | `{producto_id}` | `{message, compra}` |
| `GET` | `/api/v1/compras/{id}` | Detalle de compra | - | `{id, producto, comprador, estado, ...}` |
| `PUT` | `/api/v1/compras/{id}/confirmar` | Confirmar compra (vendedor) | - | `{message, compra}` |
| `PUT` | `/api/v1/compras/{id}/completar` | Marcar como completada | - | `{message, compra}` |
| `PUT` | `/api/v1/compras/{id}/cancelar` | Cancelar compra | `{motivo}` | `{message, compra}` |

**Flujo de Compra:**
```
1. Comprador: POST /api/v1/compras {producto_id: 15}
   → Estado compra: "pendiente"
   → Estado producto: "reservado"
   → Notificación al vendedor

2. Vendedor: PUT /api/v1/compras/42/confirmar
   → Estado compra: "confirmada"

3. Tras entrega: PUT /api/v1/compras/42/completar
   → Estado compra: "completada"
   → Estado producto: "vendido"
   → Permite crear valoración
```

---

### 4.7 Valoraciones (Token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `POST` | `/api/v1/valoraciones` | Crear valoración (tras compra) | `{compra_id, puntuacion, comentario}` | `{message, valoracion}` |
| `GET` | `/api/v1/usuarios/{id}/valoraciones` | Valoraciones de un usuario | - | `[{puntuacion, comentario, fecha}, ...]` |

---

### 4.8 Mensajes/Chat (Token requerido)

| Método | Endpoint | Descripción | Query Params | Body | Response |
|--------|----------|-------------|--------------|------|----------|
| `GET` | `/api/v1/mensajes/conversaciones` | Listar conversaciones | - | - | `[{usuario, ultimo_mensaje, no_leidos}, ...]` |
| `GET` | `/api/v1/mensajes` | Mensajes con un usuario | `?usuario_id=123` | - | `[{id, texto, fecha, leido}, ...]` |
| `POST` | `/api/v1/mensajes` | Enviar mensaje | - | `{destinatario_id, texto, producto_id?}` | `{message, mensaje}` |
| `PUT` | `/api/v1/mensajes/{id}/leer` | Marcar como leído | - | - | `{message}` |

---

### 4.9 Denuncias (Token requerido)

| Método | Endpoint | Descripción | Body | Response |
|--------|----------|-------------|------|----------|
| `POST` | `/api/v1/denuncias` | Crear denuncia | `{tipo, motivo, producto_id?, comentario_id?, usuario_id?}` | `{message, denuncia}` |
| `GET` | `/api/v1/denuncias/mis-denuncias` | Ver mis denuncias | - | `[{id, tipo, estado, fecha}, ...]` |

---

## 5. Estructura del Módulo

### 5.1 `__manifest__.py`

```python
{
    'name': 'Renaix API REST',
    'version': '1.0.0',
    'category': 'API',
    'summary': 'API REST con JWT para app móvil de Renaix',
    'author': 'Javier Herraiz & Alejandro Sánchez',
    'depends': [
        'base',
        'renaix',  # ⚠️ CRÍTICO: Depende del módulo base
    ],
    'external_dependencies': {
        'python': ['PyJWT'],  # pip install PyJWT
    },
    'installable': True,
    'application': False,
}
```

### 5.2 `models/utils/jwt_utils.py` ⭐

```python
import jwt
import datetime
from odoo.http import request

SECRET_KEY = 'renaix_super_secret_key_2025'
ALGORITHM = 'HS256'
TOKEN_EXPIRATION_HOURS = 24

def generate_token(user_id, email):
    """Genera un JWT para un usuario autenticado."""
    payload = {
        'user_id': user_id,
        'email': email,
        'exp': datetime.datetime.utcnow() + datetime.timedelta(hours=TOKEN_EXPIRATION_HOURS),
        'iat': datetime.datetime.utcnow()
    }
    token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
    return token

def verify_token():
    """
    ⭐ FUNCIÓN CENTRALIZADA
    Verifica el token JWT en la cabecera Authorization.
    
    Returns:
        tuple: (payload, error_dict, http_status)
    """
    auth_header = request.httprequest.headers.get('Authorization')
    
    if not auth_header:
        return None, {'error': 'Token no proporcionado'}, 401
    
    if not auth_header.startswith('Bearer '):
        return None, {'error': 'Formato inválido'}, 401
    
    try:
        token = auth_header.split(' ')[1]
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload, None, None
    except jwt.ExpiredSignatureError:
        return None, {'error': 'Token expirado'}, 401
    except jwt.InvalidTokenError:
        return None, {'error': 'Token inválido'}, 401
```

### 5.3 `controllers/auth.py`

```python
import json
from odoo import http
from odoo.http import request
from ..models.utils.jwt_utils import generate_token, verify_token

class AuthController(http.Controller):
    
    @http.route('/api/v1/auth/login', type='http', auth='public', 
                methods=['POST'], csrf=False)
    def login(self, **kwargs):
        """Login: Autentica usuario y devuelve JWT"""
        try:
            data = json.loads(request.httprequest.data)
            email = data.get('email')
            password = data.get('password')
            
            if not email or not password:
                return request.make_json_response(
                    {'error': 'Email y contraseña obligatorios'},
                    status=400
                )
            
            # Buscar usuario
            user = request.env['res.partner'].sudo().search([
                ('email', '=', email),
                ('es_usuario_app', '=', True)
            ], limit=1)
            
            if not user or user.password != password:
                return request.make_json_response(
                    {'error': 'Credenciales inválidas'},
                    status=401
                )
            
            if user.estado_usuario == 'suspendido':
                return request.make_json_response(
                    {'error': 'Usuario suspendido'},
                    status=403
                )
            
            # Generar token
            token = generate_token(user.id, user.email)
            
            return request.make_json_response({
                'token': token,
                'user': {
                    'id': user.id,
                    'name': user.name,
                    'email': user.email,
                    'telefono': user.telefono,
                }
            }, status=200)
            
        except Exception as e:
            return request.make_json_response(
                {'error': str(e)},
                status=500
            )
```

### 5.4 `controllers/productos.py` (Ejemplo GET)

```python
from odoo import http
from odoo.http import request
from ..models.utils.jwt_utils import verify_token

class ProductosController(http.Controller):
    
    @http.route('/api/v1/productos', type='http', auth='public',
                methods=['GET'], csrf=False)
    def get_productos(self, **kwargs):
        """Lista todos los productos disponibles"""
        
        # ⭐ Verificar token (SIEMPRE en endpoints protegidos)
        payload, error, status = verify_token()
        if error:
            return request.make_json_response(error, status=status)
        
        try:
            # Construir dominio
            domain = [('estado', '=', 'disponible')]
            
            if kwargs.get('categoria_id'):
                domain.append(('categoria_id', '=', int(kwargs['categoria_id'])))
            
            if kwargs.get('search'):
                search_term = kwargs['search']
                domain.append('|')
                domain.append(('nombre', 'ilike', search_term))
                domain.append(('descripcion', 'ilike', search_term))
            
            # Paginación
            limit = int(kwargs.get('limit', 50))
            offset = int(kwargs.get('offset', 0))
            
            # Buscar
            productos = request.env['renaix.producto'].sudo().search(
                domain,
                limit=limit,
                offset=offset,
                order='create_date DESC'
            )
            
            total = request.env['renaix.producto'].sudo().search_count(domain)
            
            # Serializar
            productos_data = []
            for producto in productos:
                productos_data.append({
                    'id': producto.id,
                    'nombre': producto.nombre,
                    'precio': producto.precio,
                    'vendedor': {
                        'id': producto.propietario_id.id,
                        'name': producto.propietario_id.name,
                    },
                    # ... más campos
                })
            
            return request.make_json_response({
                'productos': productos_data,
                'total': total,
                'limit': limit,
                'offset': offset
            }, status=200)
            
        except Exception as e:
            return request.make_json_response(
                {'error': str(e)},
                status=500
            )
```

---

## 6. Buenas Prácticas de Desarrollo

### 6.1 Centralizar Lógica Reutilizable

#### ❌ MAL - Código duplicado
```python
# En productos.py
@http.route('/api/v1/productos', ...)
def get_productos(self):
    auth_header = request.httprequest.headers.get('Authorization')
    if not auth_header:
        return request.make_json_response({'error': 'Token no proporcionado'}, status=401)
    # ... repetir en cada endpoint
```

#### ✅ BIEN - Función centralizada
```python
# models/utils/jwt_utils.py
def verify_token():
    """⭐ UNA SOLA función reutilizable"""
    # ... lógica de validación

# Uso en TODOS los controladores
from ..models.utils.jwt_utils import verify_token

@http.route('/api/v1/productos', ...)
def get_productos(self):
    payload, error, status = verify_token()
    if error:
        return request.make_json_response(error, status=status)
    # ... lógica
```

### 6.2 Validaciones Reutilizables

```python
# models/utils/validators.py

def validate_required_fields(data, required_fields):
    """Valida campos obligatorios"""
    missing = [f for f in required_fields if not data.get(f)]
    if missing:
        return False, f"Campos faltantes: {', '.join(missing)}"
    return True, None

def validate_email(email):
    """Valida formato email"""
    import re
    pattern = r'^[\w\.-]+@[\w\.-]+\.\w+$'
    return re.match(pattern, email) is not None

def validate_price(price):
    """Valida precio válido"""
    try:
        return float(price) > 0
    except:
        return False

def validate_phone(phone):
    """Valida teléfono español"""
    import re
    pattern = r'^(\+34)?[6-9]\d{8}$'
    return re.match(pattern, phone) is not None
```

**Uso:**
```python
from ..models.utils.validators import validate_required_fields, validate_email

@http.route('/api/v1/auth/register', ...)
def register(self):
    data = json.loads(request.httprequest.data)
    
    # Validar campos obligatorios
    is_valid, error = validate_required_fields(data, ['name', 'email', 'password'])
    if not is_valid:
        return request.make_json_response({'error': error}, status=400)
    
    # Validar email
    if not validate_email(data['email']):
        return request.make_json_response({'error': 'Email inválido'}, status=400)
    
    # ... resto de lógica
```

### 6.3 Serialización de Modelos a JSON

```python
# models/utils/serializers.py

def serialize_partner(partner):
    """Serializa res.partner a JSON"""
    if not partner:
        return None
    
    return {
        'id': partner.id,
        'name': partner.name,
        'email': partner.email,
        'telefono': partner.telefono,
        'puntuacion_media': partner.puntuacion_media,
        'productos_vendidos': partner.productos_vendidos_count,
        'estado': partner.estado_usuario,
        'fecha_registro': partner.create_date.isoformat() if partner.create_date else None,
    }

def serialize_producto(producto, include_comments=False):
    """Serializa renaix.producto a JSON"""
    if not producto:
        return None
    
    data = {
        'id': producto.id,
        'nombre': producto.nombre,
        'descripcion': producto.descripcion,
        'precio': producto.precio,
        'estado': producto.estado,
        'vendedor': serialize_partner(producto.propietario_id),
        'categoria': {
            'id': producto.categoria_id.id,
            'nombre': producto.categoria_id.nombre
        } if producto.categoria_id else None,
        'etiquetas': [
            {'id': e.id, 'nombre': e.nombre} 
            for e in producto.etiqueta_ids
        ],
        'imagenes': [img.imagen for img in producto.imagen_ids],
        'fecha_publicacion': producto.create_date.isoformat() if producto.create_date else None,
    }
    
    if include_comments:
        data['comentarios'] = [
            serialize_comentario(c) for c in producto.comentario_ids
        ]
    
    return data

def serialize_comentario(comentario):
    """Serializa comentario"""
    return {
        'id': comentario.id,
        'usuario': {
            'id': comentario.usuario_id.id,
            'name': comentario.usuario_id.name,
        },
        'texto': comentario.texto,
        'fecha': comentario.create_date.isoformat() if comentario.create_date else None,
    }
```

**Uso simplificado:**
```python
from ..models.utils.serializers import serialize_producto

@http.route('/api/v1/productos/<int:producto_id>', ...)
def get_producto(self, producto_id):
    payload, error, status = verify_token()
    if error:
        return request.make_json_response(error, status=status)
    
    producto = request.env['renaix.producto'].sudo().browse(producto_id)
    
    if not producto.exists():
        return request.make_json_response({'error': 'No encontrado'}, status=404)
    
    # ⭐ Serialización en UNA línea
    return request.make_json_response(
        serialize_producto(producto, include_comments=True), 
        status=200
    )
```

### 6.4 Helpers de Respuestas HTTP

```python
# models/utils/response_helpers.py

from odoo.http import request

def success_response(data, status=200):
    """Respuesta exitosa estandarizada"""
    return request.make_json_response({
        'success': True,
        'data': data
    }, status=status)

def error_response(message, status=400, error_code=None):
    """Respuesta de error estandarizada"""
    error_data = {
        'success': False,
        'error': {'message': message}
    }
    if error_code:
        error_data['error']['code'] = error_code
    
    return request.make_json_response(error_data, status=status)

def paginated_response(items, total, limit, offset):
    """Respuesta paginada"""
    return request.make_json_response({
        'success': True,
        'data': items,
        'pagination': {
            'total': total,
            'limit': limit,
            'offset': offset,
            'has_more': (offset + limit) < total
        }
    }, status=200)
```

---

## 7. Logging y Monitorización

### 7.1 Sistema de Logging de Odoo

```python
import logging
from odoo import http
from odoo.http import request

# ⭐ Crear logger específico
_logger = logging.getLogger(__name__)

class ProductosController(http.Controller):
    
    @http.route('/api/v1/productos', auth='public', methods=['GET'], csrf=False)
    def get_productos(self, **kwargs):
        _logger.info('GET /api/v1/productos - Solicitado')
        
        payload, error, status = verify_token()
        if error:
            _logger.warning(f'Token inválido: {error}')
            return request.make_json_response(error, status=status)
        
        try:
            user_id = payload['user_id']
            _logger.info(f'Usuario autenticado: {user_id}')
            
            # ... lógica
            
            _logger.info(f'Productos devueltos: {len(productos)}')
            return request.make_json_response(productos_data, status=200)
            
        except Exception as e:
            _logger.error(f'Error en get_productos: {str(e)}', exc_info=True)
            return request.make_json_response({'error': 'Error interno'}, status=500)
```

### 7.2 Niveles de Logging

```python
_logger.debug('Información de depuración (solo desarrollo)')
_logger.info('Información general (flujo normal)')
_logger.warning('Advertencia (algo inesperado)')
_logger.error('Error (algo falló)')
_logger.critical('Error crítico (fallo grave)')
```

### 7.3 Configurar en odoo.conf

```bash
[options]
log_level = info
log_handler = :INFO,werkzeug:WARNING,odoo.addons.renaix_api:DEBUG
```

### 7.4 Decorator para Logging Automático

```python
# models/utils/decorators.py

import logging
import functools
import time
from odoo.http import request

_logger = logging.getLogger(__name__)

def log_api_call(func):
    """Decorator que registra automáticamente llamadas a endpoints"""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start_time = time.time()
        
        method = request.httprequest.method
        path = request.httprequest.path
        user_id = 'Anonymous'
        
        # Intentar obtener user_id del token
        auth_header = request.httprequest.headers.get('Authorization')
        if auth_header:
            try:
                import jwt
                token = auth_header.split(' ')[1]
                payload = jwt.decode(token, verify=False)
                user_id = payload.get('user_id', 'Unknown')
            except:
                pass
        
        _logger.info(f'API CALL: {method} {path} - User: {user_id}')
        
        try:
            result = func(*args, **kwargs)
            elapsed_time = time.time() - start_time
            _logger.info(f'API RESPONSE: {method} {path} - Time: {elapsed_time:.3f}s')
            return result
            
        except Exception as e:
            elapsed_time = time.time() - start_time
            _logger.error(f'API ERROR: {method} {path} - {str(e)}', exc_info=True)
            raise
    
    return wrapper
```

**Uso:**
```python
from ..models.utils.decorators import log_api_call

class ProductosController(http.Controller):
    
    @log_api_call  # ⭐ Logging automático
    @http.route('/api/v1/productos', auth='public', methods=['GET'], csrf=False)
    def get_productos(self, **kwargs):
        # ... tu lógica
        pass
```

---

## 8. Seguridad y Configuración

### 8.1 NO Hardcodear Claves Secretas

#### ❌ MAL:
```python
SECRET_KEY = 'renaix_super_secret_key_2025'  # Hardcodeado
```

#### ✅ BIEN - Variables de entorno:
```python
import os

SECRET_KEY = os.environ.get('RENAIX_JWT_SECRET', 'clave_desarrollo')
```

#### ✅ BIEN - Configuración Odoo:
```python
# config/settings.py
from odoo.tools import config

def get_jwt_secret():
    """Obtiene clave JWT desde odoo.conf"""
    return config.get('renaix_jwt_secret', 'fallback_secret')

def get_jwt_expiration():
    """Obtiene tiempo de expiración"""
    return int(config.get('renaix_jwt_expiration_hours', 24))
```

```bash
# odoo.conf
[options]
renaix_jwt_secret = mi_clave_produccion_2025
renaix_jwt_expiration_hours = 24
```

### 8.2 Validación de Permisos

```python
@http.route('/api/v1/productos/<int:producto_id>', methods=['PUT'], ...)
def update_producto(self, producto_id):
    payload, error, status = verify_token()
    if error:
        return request.make_json_response(error, status=status)
    
    user_id = payload['user_id']
    producto = request.env['renaix.producto'].sudo().browse(producto_id)
    
    # ⭐ Verificar que el usuario es el propietario
    if producto.propietario_id.id != user_id:
        return request.make_json_response(
            {'error': 'No tienes permiso para editar este producto'},
            status=403
        )
    
    # ... actualizar
```

---

## 9. Librerías Recomendadas

### 9.1 PyJWT (Obligatorio)

```bash
pip install PyJWT
```

```python
import jwt

# Generar token
token = jwt.encode(payload, SECRET_KEY, algorithm='HS256')

# Verificar token
payload = jwt.decode(token, SECRET_KEY, algorithms=['HS256'])
```

### 9.2 Marshmallow (OPCIONAL - Validación avanzada)

```bash
pip install marshmallow
```

```python
from marshmallow import Schema, fields, validate, ValidationError

class ProductoSchema(Schema):
    nombre = fields.Str(required=True, validate=validate.Length(min=3, max=100))
    precio = fields.Float(required=True, validate=validate.Range(min=0.01))
    descripcion = fields.Str(validate=validate.Length(max=500))

# Uso
schema = ProductoSchema()
try:
    data = schema.load(request_data)
except ValidationError as err:
    return error_response(err.messages, status=400)
```

---

## 10. Checklist de Implementación

### ✅ Fase 1: Diseño (PASO 1)

- [ ] Identificar actores y casos de uso
- [ ] Listar pantallas de la app móvil
- [ ] Mapear entidades del dominio
- [ ] Diseñar endpoints RESTful
- [ ] Documentar estructura JSON de requests/responses
- [ ] Priorizar endpoints (qué hacer primero)

### ✅ Fase 2: Estructura Base (PASO 2)

- [ ] Crear estructura de carpetas del módulo
- [ ] Crear `__manifest__.py` con dependencias
- [ ] Crear `__init__.py` en raíz
- [ ] Crear `controllers/__init__.py`
- [ ] Crear `models/__init__.py`
- [ ] Crear carpeta `models/utils/`

### ✅ Fase 3: Utils (PASO 3)

- [ ] Implementar `jwt_utils.py` (generate_token, verify_token)
- [ ] Implementar `validators.py`
- [ ] Implementar `serializers.py`
- [ ] Implementar `response_helpers.py`
- [ ] Implementar `decorators.py` (OPCIONAL)
- [ ] Crear `config/settings.py`

### ✅ Fase 4: Autenticación (PASO 4)

- [ ] Implementar `controllers/auth.py`
- [ ] Endpoint `/api/v1/auth/login`
- [ ] Endpoint `/api/v1/auth/register`
- [ ] Endpoint `/api/v1/auth/verify`
- [ ] Probar con Postman/Thunder Client

### ✅ Fase 5: Usuarios (PASO 5)

- [ ] Implementar `controllers/usuarios.py`
- [ ] Endpoint GET `/api/v1/usuarios/perfil`
- [ ] Endpoint PUT `/api/v1/usuarios/perfil`
- [ ] Endpoint GET `/api/v1/usuarios/{id}`
- [ ] Probar autenticación con token

### ✅ Fase 6: Productos (PASO 6)

- [ ] Implementar `controllers/productos.py`
- [ ] Endpoint GET `/api/v1/productos` (listar + filtros)
- [ ] Endpoint GET `/api/v1/productos/{id}`
- [ ] Endpoint POST `/api/v1/productos` (crear)
- [ ] Endpoint PUT `/api/v1/productos/{id}`
- [ ] Endpoint DELETE `/api/v1/productos/{id}`
- [ ] Gestión de imágenes

### ✅ Fase 7: Categorías y Etiquetas (PASO 7)

- [ ] Implementar `controllers/categorias.py`
- [ ] Implementar `controllers/etiquetas.py`
- [ ] Endpoint GET `/api/v1/categorias`
- [ ] Endpoint GET `/api/v1/etiquetas`

### ✅ Fase 8: Comentarios (PASO 8)

- [ ] Implementar `controllers/comentarios.py`
- [ ] Endpoint GET `/api/v1/productos/{id}/comentarios`
- [ ] Endpoint POST `/api/v1/productos/{id}/comentarios`
- [ ] Endpoint DELETE `/api/v1/comentarios/{id}`
- [ ] Notificación al propietario del producto

### ✅ Fase 9: Compras (PASO 9)

- [ ] Implementar `controllers/compras.py`
- [ ] Endpoint POST `/api/v1/compras` (crear compra)
- [ ] Endpoint GET `/api/v1/compras/{id}`
- [ ] Endpoint PUT `/api/v1/compras/{id}/confirmar`
- [ ] Endpoint PUT `/api/v1/compras/{id}/completar`
- [ ] Endpoint PUT `/api/v1/compras/{id}/cancelar`
- [ ] Cambios de estado de producto
- [ ] Notificación al vendedor

### ✅ Fase 10: Valoraciones (PASO 10)

- [ ] Implementar `controllers/valoraciones.py`
- [ ] Endpoint POST `/api/v1/valoraciones`
- [ ] Endpoint GET `/api/v1/usuarios/{id}/valoraciones`
- [ ] Validar que solo se valore tras compra completada

### ✅ Fase 11: Mensajes/Chat (PASO 11)

- [ ] Implementar `controllers/mensajes.py`
- [ ] Endpoint GET `/api/v1/mensajes/conversaciones`
- [ ] Endpoint GET `/api/v1/mensajes?usuario_id=X`
- [ ] Endpoint POST `/api/v1/mensajes`
- [ ] Endpoint PUT `/api/v1/mensajes/{id}/leer`

### ✅ Fase 12: Denuncias (PASO 12)

- [ ] Implementar `controllers/denuncias.py`
- [ ] Endpoint POST `/api/v1/denuncias`
- [ ] Endpoint GET `/api/v1/denuncias/mis-denuncias`
- [ ] Registro visible para empleados en Odoo

### ✅ Fase 13: Testing y Documentación (PASO 13)

- [ ] Probar TODOS los endpoints con Postman/Thunder Client
- [ ] Documentar casos de éxito y error
- [ ] Crear colección de Postman
- [ ] Capturas de pruebas para entrega
- [ ] Documento PDF/Markdown de evidencias

---

## 11. Anexos

### 11.1 Códigos HTTP Comunes

| Código | Significado | Uso en API |
|--------|-------------|------------|
| `200` | OK | Operación exitosa (GET, PUT) |
| `201` | Created | Recurso creado (POST) |
| `204` | No Content | Eliminación exitosa (DELETE) |
| `400` | Bad Request | Datos inválidos, validación fallida |
| `401` | Unauthorized | Token ausente/inválido/expirado |
| `403` | Forbidden | Usuario sin permisos |
| `404` | Not Found | Recurso no encontrado |
| `409` | Conflict | Conflicto (email duplicado, etc.) |
| `500` | Internal Server Error | Error del servidor |

### 11.2 Ejemplo Completo de Endpoint

```python
@http.route('/api/v1/productos', type='http', auth='public', 
            methods=['POST'], csrf=False)
def create_producto(self, **kwargs):
    """
    Crea un nuevo producto.
    
    Headers:
        Authorization: Bearer <token>
    
    Body JSON:
        {
            "nombre": "iPhone 12",
            "descripcion": "Seminuevo",
            "precio": 450.00,
            "categoria_id": 1,
            "etiqueta_ids": [1, 2, 3],
            "imagenes": ["base64..."]
        }
    
    Response 201:
        {
            "message": "Producto creado correctamente",
            "producto": { ... }
        }
    
    Response 400:
        {
            "error": "El campo nombre es obligatorio"
        }
    
    Response 401:
        {
            "error": "Token inválido"
        }
    """
    # 1. Verificar token
    payload, error, status = verify_token()
    if error:
        return request.make_json_response(error, status=status)
    
    try:
        # 2. Parsear JSON
        data = json.loads(request.httprequest.data)
        user_id = payload['user_id']
        
        # 3. Validar campos obligatorios
        is_valid, error_msg = validate_required_fields(
            data, ['nombre', 'precio']
        )
        if not is_valid:
            return request.make_json_response(
                {'error': error_msg}, 
                status=400
            )
        
        # 4. Validar precio
        if not validate_price(data['precio']):
            return request.make_json_response(
                {'error': 'Precio inválido'}, 
                status=400
            )
        
        # 5. Crear producto
        producto_vals = {
            'nombre': data['nombre'],
            'descripcion': data.get('descripcion', ''),
            'precio': float(data['precio']),
            'propietario_id': user_id,
            'estado': 'borrador',
        }
        
        if data.get('categoria_id'):
            producto_vals['categoria_id'] = data['categoria_id']
        
        if data.get('etiqueta_ids'):
            producto_vals['etiqueta_ids'] = [(6, 0, data['etiqueta_ids'])]
        
        producto = request.env['renaix.producto'].sudo().create(producto_vals)
        
        # 6. Crear imágenes
        if data.get('imagenes'):
            for imagen_base64 in data['imagenes']:
                request.env['renaix.producto.imagen'].sudo().create({
                    'producto_id': producto.id,
                    'imagen': imagen_base64,
                })
        
        # 7. Log
        _logger.info(f'Producto {producto.id} creado por usuario {user_id}')
        
        # 8. Respuesta exitosa
        return request.make_json_response({
            'message': 'Producto creado correctamente',
            'producto': serialize_producto(producto)
        }, status=201)
        
    except json.JSONDecodeError:
        return request.make_json_response(
            {'error': 'JSON inválido'}, 
            status=400
        )
    except Exception as e:
        _logger.error(f'Error al crear producto: {str(e)}', exc_info=True)
        return request.make_json_response(
            {'error': 'Error interno del servidor'}, 
            status=500
        )
```

### 11.3 Estructura JSON Completa de Producto

```json
{
  "id": 42,
  "nombre": "iPhone 12 Pro 128GB",
  "descripcion": "Seminuevo, comprado hace 1 año. Estado impecable, sin arañazos.",
  "precio": 650.00,
  "estado": "disponible",
  "vendedor": {
    "id": 15,
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "telefono": "+34600123456",
    "puntuacion_media": 4.8,
    "productos_vendidos": 23,
    "productos_comprados": 12
  },
  "categoria": {
    "id": 2,
    "nombre": "Electrónica",
    "icono": "📱"
  },
  "etiquetas": [
    {"id": 5, "nombre": "smartphone"},
    {"id": 12, "nombre": "apple"},
    {"id": 23, "nombre": "seminuevo"}
  ],
  "imagenes": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
  ],
  "comentarios": [
    {
      "id": 101,
      "usuario": {
        "id": 20,
        "name": "María López"
      },
      "texto": "¿Incluye cargador original?",
      "fecha": "2025-01-20T15:30:00Z"
    },
    {
      "id": 102,
      "usuario": {
        "id": 15,
        "name": "Juan Pérez"
      },
      "texto": "Sí, incluye cargador y caja original",
      "fecha": "2025-01-20T16:00:00Z"
    }
  ],
  "fecha_publicacion": "2025-01-15T10:00:00Z",
  "fecha_actualizacion": "2025-01-20T12:00:00Z"
}
```

---

## PASO 2: Crear la Estructura de Carpetas

### 2.1 Crear el Módulo Base

```bash
# Navegar a la carpeta de addons de Odoo
cd /ruta/a/tu/odoo/addons

# Crear estructura base
mkdir -p renaix_api/controllers
mkdir -p renaix_api/models/utils
mkdir -p renaix_api/config

# Crear archivos iniciales
touch renaix_api/__init__.py
touch renaix_api/__manifest__.py
touch renaix_api/controllers/__init__.py
touch renaix_api/models/__init__.py
touch renaix_api/models/utils/__init__.py
```

### 2.2 Estructura Completa

```
renaix_api/
│
├── __init__.py
├── __manifest__.py
│
├── controllers/
│   ├── __init__.py
│   ├── auth.py
│   ├── usuarios.py
│   ├── productos.py
│   ├── compras.py
│   ├── comentarios.py
│   ├── valoraciones.py
│   ├── denuncias.py
│   ├── mensajes.py
│   ├── etiquetas.py
│   └── categorias.py
│
├── models/
│   ├── __init__.py
│   └── utils/
│       ├── __init__.py
│       ├── jwt_utils.py
│       ├── validators.py
│       ├── serializers.py
│       ├── response_helpers.py
│       └── decorators.py
│
└── config/
    └── settings.py
```

### 2.3 Contenido de Archivos Iniciales

#### `__init__.py` (raíz)
```python
# -*- coding: utf-8 -*-
from . import controllers
from . import models
```

#### `controllers/__init__.py`
```python
# -*- coding: utf-8 -*-
from . import auth
from . import usuarios
from . import productos
from . import compras
from . import comentarios
from . import valoraciones
from . import denuncias
from . import mensajes
from . import etiquetas
from . import categorias
```

#### `models/__init__.py`
```python
# -*- coding: utf-8 -*-
from . import utils
```

#### `models/utils/__init__.py`
```python
# -*- coding: utf-8 -*-
from . import jwt_utils
from . import validators
from . import serializers
from . import response_helpers
from . import decorators
```

### 2.4 Instalar PyJWT

```bash
pip install PyJWT
```

### 2.5 Verificar Instalación

```bash
# Listar el módulo
ls -la renaix_api/

# Debería mostrar:
# __init__.py
# __manifest__.py
# controllers/
# models/
# config/
```

---

## PRÓXIMOS PASOS

Una vez creada la estructura:

1. **PASO 3**: Implementar archivos utils (jwt_utils.py, validators.py, etc.)
2. **PASO 4**: Implementar autenticación (auth.py)
3. **PASO 5**: Implementar resto de controladores
4. **PASO 6**: Instalar módulo en Odoo
5. **PASO 7**: Probar con Postman/Thunder Client

---

## 📚 Referencias

- **Documentación Odoo Controllers**: https://www.odoo.com/documentation/17.0/developer/reference/backend/http.html
- **PyJWT**: https://pyjwt.readthedocs.io/
- **REST API Best Practices**: https://restfulapi.net/
- **HTTP Status Codes**: https://httpstatuses.com/

---

**Fin del Documento**