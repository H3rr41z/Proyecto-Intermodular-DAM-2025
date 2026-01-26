# 📊 Análisis del Módulo Core: **Renaix**

## 🎯 Descripción General

**Renaix** es un marketplace de compraventa de productos de segunda mano desarrollado en Odoo 17. El módulo gestiona la plataforma desde el lado del ERP, integrándose con una aplicación móvil en Kotlin.

**Autores**: Javier Herraiz Calatayud & Alejandro Sánchez Serrano
**Versión**: 1.0.0
**Licencia**: LGPL-3

---

## 📁 Arquitectura del Módulo

### **Ubicación**:
`erp/docker/custom_addons/renaix/`

### **Estructura del proyecto**:
```
renaix/
├── models/          # 11 modelos de datos
├── views/           # 13 archivos de vistas XML
├── security/        # Sistema de permisos (4 niveles)
├── data/            # Datos iniciales y secuencias
├── reports/         # Informes PDF (QWeb)
├── static/          # Recursos estáticos
└── __manifest__.py  # Configuración del módulo
```

---

## 🗄️ Modelos de Datos (11 Modelos)

### **1. res.partner (Extendido)**
**Archivo**: `models/res_partner.py`
**Descripción**: Usuarios de la app móvil

**Características clave**:
- ✅ `partner_gid`: UUID para sincronización con app móvil
- ✅ `es_usuario_app`: Flag para identificar usuarios de la app
- ✅ Campos computados almacenados:
  - `valoracion_promedio`: Media de estrellas recibidas como vendedor (0-5)
  - `productos_en_venta`: Cantidad de productos disponibles
  - `productos_vendidos`: Total de productos vendidos
  - `productos_comprados`: Total de productos comprados
  - `total_comentarios`: Cantidad de comentarios realizados
  - `total_denuncias_realizadas`: Denuncias realizadas por el usuario

**Relaciones**:
- `producto_ids`: Productos publicados (One2many)
- `compra_comprador_ids`: Compras realizadas (One2many)
- `compra_vendedor_ids`: Ventas realizadas (One2many)
- `valoracion_ids`: Valoraciones recibidas (One2many)
- `comentario_ids`: Comentarios realizados (One2many)
- `denuncia_ids`: Denuncias realizadas (One2many)
- `mensaje_enviado_ids`: Mensajes enviados (One2many)
- `mensaje_recibido_ids`: Mensajes recibidos (One2many)

**Control de cuenta**:
- `cuenta_activa`: Boolean para activar/desactivar acceso
- `fecha_registro_app`: Fecha de registro
- `fecha_ultima_actividad`: Última acción en la app
- `api_token`: Token de autenticación para API REST

**Métodos destacados**:
```python
- action_view_productos()      # Ver productos del usuario
- action_view_compras()        # Ver compras realizadas
- action_view_ventas()         # Ver ventas realizadas
- action_desactivar_cuenta()   # Suspender cuenta
- action_activar_cuenta()      # Reactivar cuenta
- action_regenerar_gid()       # Regenerar UUID
```

**Validaciones**:
- Genera automáticamente UUID al crear usuario app
- Calcula estadísticas en tiempo real mediante `@api.depends`
- `name_get()` personalizado: muestra icono 📱 y valoración

---

### **2. renaix.categoria**
**Archivo**: `models/categoria.py`
**Descripción**: Categorías predefinidas para clasificar productos

**Campos principales**:
```python
- name: Nombre de la categoría (requerido, único)
- descripcion: Descripción detallada
- image: Imagen representativa (512x512px)
- sequence: Orden de aparición
- color: Color para vistas kanban
- active: Control de visibilidad
- producto_count: Nº de productos (computado, almacenado)
```

**Relaciones**:
- `producto_ids`: Productos en esta categoría (One2many)

**Características**:
- Hereda `mail.thread` y `mail.activity.mixin` (Chatter)
- Constraint SQL: nombre único
- `name_get()`: Muestra "Nombre (X productos)"
- Método `action_view_productos()`: Ver todos los productos de la categoría

**Ejemplos**: Electrónica, Ropa, Muebles, Deportes, etc.

---

### **3. renaix.etiqueta**
**Archivo**: `models/etiqueta.py`
**Descripción**: Etiquetas/tags para clasificar productos (máximo 5 por producto)

**Características únicas**:
- 🔤 **Normalización automática**: lowercase, sin espacios extras
- 📊 `producto_count` almacenado
- ✅ **Constraint SQL**: nombre único (case-insensitive)
- 🎨 Compatible con widget `many2many_tags`
- 🔥 Método `get_etiquetas_mas_usadas(limit)`: Para sugerencias en la app

**Campos**:
```python
- name: Nombre de la etiqueta (índice para búsquedas rápidas)
- color: Color para visualización
- active: Control de estado
- producto_count: Nº productos con esta etiqueta
```

**Validaciones**:
- Longitud: 2-30 caracteres
- Normalización en `create()` y `write()`
- `name_create()`: Creación rápida desde Many2many

**Normalización**:
```python
def _normalize_name(self, name):
    # Convierte a minúsculas
    # Elimina espacios al inicio/final
    # Reemplaza múltiples espacios por uno solo
```

**Ejemplos**: #gaming, #vintage, #nuevo, #deportivo

---

### **4. renaix.producto**
**Archivo**: `models/producto.py`
**Descripción**: Productos de segunda mano publicados por usuarios

**Campos principales**:
```python
# Básicos
- name: Nombre del producto (requerido, indexado)
- descripcion: Descripción detallada
- precio: Precio en euros (requerido, validado)
- antiguedad: Antigüedad del producto
- ubicacion: Ubicación física

# Estados
- estado_producto: nuevo | como_nuevo | buen_estado | aceptable | para_reparar
- estado_venta: borrador | disponible | reservado | vendido | eliminado
- active: Control de visibilidad

# Fechas
- fecha_publicacion: Fecha de publicación
- fecha_actualizacion: Última modificación

# Relaciones
- propietario_id: Usuario propietario (Many2one -> res.partner)
- categoria_id: Categoría (Many2one, requerido)
- etiqueta_ids: Etiquetas (Many2many, máx 5)
- imagen_ids: Imágenes (One2many, mín 1, máx 10)
- comentario_ids: Comentarios (One2many)
- compra_id: Compra asociada si vendido (Many2one)
- denuncia_ids: Denuncias (One2many)

# Computados
- total_comentarios: Cantidad de comentarios
- total_denuncias: Cantidad de denuncias
- total_imagenes: Cantidad de imágenes
- dias_publicado: Días desde publicación
```

**Validaciones estrictas**:
```python
@api.constrains('etiqueta_ids')
# Máximo 5 etiquetas

@api.constrains('imagen_ids')
# Mínimo 1 imagen (excepto borrador)
# Máximo 10 imágenes

@api.constrains('precio')
# Precio >= 0
# Precio <= 1.000.000€
```

**Métodos de acción**:
```python
- action_publicar()          # Borrador → Disponible (valida imágenes)
- action_reservar()          # Disponible → Reservado
- action_marcar_vendido()    # → Vendido
- action_view_comentarios()  # Ver comentarios
- action_view_denuncias()    # Ver denuncias
```

**Herencias**:
- `mail.thread`: Chatter y seguimiento
- `mail.activity.mixin`: Actividades
- `image.mixin`: Gestión de imágenes

**Notificaciones**:
- Al crear: notifica a propietario
- Al cambiar estado: registra en Chatter

---

### **5. renaix.producto.imagen**
**Archivo**: `models/producto_imagen.py`
**Descripción**: Imágenes asociadas a productos (1-10 por producto)

**Campos**:
```python
- producto_id: Producto asociado (Many2one, cascade)
- imagen: Imagen principal (max 1920x1920px)
- imagen_small: Miniatura (256x256px, relacionado)
- secuencia: Orden de aparición
- es_principal: Flag de imagen principal
- descripcion: Descripción opcional
- url_imagen: URL pública (computado para API)
- tamano_kb: Tamaño en KB (computado)
```

**Comportamiento automático**:
- La primera imagen se marca automáticamente como principal
- Al marcar una como principal, desmarca las demás
- Genera URL pública para acceso desde API

**Validaciones**:
- Máximo 10 imágenes por producto
- Solo una imagen puede ser principal

**Método útil**:
```python
_compute_url_imagen()
# Genera: {base_url}/web/image/renaix.producto.imagen/{id}/imagen
```

---

### **6. renaix.compra**
**Archivo**: `models/compra.py`
**Descripción**: Transacciones de compra-venta entre usuarios

**Campos principales**:
```python
# Identificación
- codigo: Código único (secuencia automática: COMP/2025/0001)

# Relaciones
- producto_id: Producto comprado (Many2one, requerido)
- comprador_id: Usuario comprador (Many2one, requerido)
- vendedor_id: Usuario vendedor (computado del propietario)

# Transacción
- fecha_compra: Fecha de la transacción
- precio_final: Precio acordado (por si difiere del original)
- estado: pendiente | confirmada | completada | cancelada
- notas: Notas adicionales

# Valoraciones
- valoracion_comprador_ids: Valoraciones del comprador (One2many)
- valoracion_vendedor_ids: Valoraciones del vendedor (One2many)
- comprador_valoro: Boolean computado
- vendedor_valoro: Boolean computado

# Resolución
- empleado_asignado_id: Empleado si hay problemas
- resolucion: Descripción de resolución
```

**Estados del flujo**:
```
pendiente → confirmada → completada
          ↘ cancelada ←┘
```

**Validaciones críticas**:
```python
@api.constrains('comprador_id', 'producto_id')
# ❌ No se puede comprar el propio producto

@api.constrains('producto_id')
# ✅ Producto debe estar disponible/reservado
```

**Constraint SQL**:
```python
- precio_positivo: CHECK(precio_final >= 0)
- codigo_unique: UNIQUE(codigo)
```

**Flujo automático al crear**:
```python
1. Genera código único de secuencia
2. Copia precio del producto si no se especifica
3. Añade comprador y vendedor como seguidores
4. Marca producto como RESERVADO
5. Notifica al vendedor con datos del comprador
6. Notifica al comprador confirmando la compra
```

**Métodos de acción**:
```python
- action_confirmar()           # Pendiente → Confirmada
- action_completar()           # → Completada + vendido
                               # Crea actividad para valorar en 2 días
- action_cancelar()            # → Cancelada + libera producto
- action_solicitar_valoraciones() # Recordatorio para valorar
```

**Notificaciones automáticas**:
- HTML formateado con datos de contacto
- Emails a comprador y vendedor
- Registro en Chatter de producto

---

### **7. renaix.valoracion**
**Archivo**: `models/valoracion.py`
**Descripción**: Sistema de valoraciones bidireccional (1-5 estrellas)

**Tipos de valoración**:
```python
- comprador_a_vendedor: Comprador valora al vendedor
- vendedor_a_comprador: Vendedor valora al comprador
```

**Campos**:
```python
- compra_id: Compra asociada (Many2one, requerido, cascade)
- tipo_valoracion: Tipo (requerido)
- usuario_valorador_id: Quien valora (Many2one)
- usuario_valorado_id: Quien es valorado (Many2one)
- puntuacion: 1-5 estrellas (requerido)
- comentario: Texto opcional
- fecha: Fecha de valoración

# Campos relacionados (para búsquedas)
- comprador_id: relacionado de compra_id
- vendedor_id: relacionado de compra_id
- producto_id: relacionado de compra_id
```

**Validaciones estrictas**:
```python
# Constraint SQL
- puntuacion_rango: CHECK(puntuacion >= 1 AND puntuacion <= 5)
- valoracion_unica: UNIQUE(compra_id, tipo_valoracion)
  # Solo 1 valoración por tipo por compra

# Python constraints
@api.constrains('puntuacion')
# Puntuación entre 1-5

@api.constrains('usuario_valorador_id', 'usuario_valorado_id')
# ❌ No autovaloración

@api.constrains('compra_id', 'tipo_valoracion', 'usuario_valorador_id')
# Coherencia total:
# - Comprador solo puede valorar a vendedor
# - Vendedor solo puede valorar a comprador
# - Valorador debe ser parte de la compra

@api.constrains('compra_id')
# ✅ Solo valorar compras COMPLETADAS
```

**Notificaciones**:
```python
# Al crear valoración:
- Notifica al usuario valorado con estrellas ⭐
- Notifica en la compra
- Formato HTML bonito
```

**`name_get()` personalizado**:
```python
"Usuario1 → Usuario2 (⭐⭐⭐⭐)"
```

---

### **8. renaix.comentario**
**Archivo**: `models/comentario.py`
**Descripción**: Comentarios que los usuarios hacen en productos

**Campos**:
```python
- producto_id: Producto comentado (Many2one, cascade)
- usuario_id: Usuario que comenta (Many2one)
- texto: Contenido del comentario (requerido)
- fecha: Fecha del comentario (readonly, automática)
- active: Control para moderación (eliminar sin borrar)

# Relacionados
- producto_nombre: relacionado de producto_id
- usuario_nombre: relacionado de usuario_id
- propietario_producto_id: Propietario del producto
```

**Validaciones**:
```python
@api.constrains('texto')
# Texto no vacío
# Mínimo 3 caracteres
# Máximo 1000 caracteres

@api.constrains('usuario_id', 'producto_id')
# Anti-spam: máximo 5 comentarios/día por usuario en mismo producto
```

**Comportamiento**:
```python
# Al crear:
- Notifica al propietario del producto (si no es el mismo usuario)
- Mensaje HTML formateado en Chatter del producto

# Al desactivar (active=False):
- Notifica al usuario que su comentario fue eliminado
```

**Métodos**:
```python
- action_eliminar()    # active = False (moderación)
- action_restaurar()   # active = True
```

**Herencias**:
- `mail.thread`: Chatter
- `mail.activity.mixin`: Actividades

---

### **9. renaix.mensaje**
**Archivo**: `models/mensaje.py`
**Descripción**: Sistema de mensajería privada entre usuarios

**Campos**:
```python
# Usuarios
- emisor_id: Usuario que envía (Many2one, requerido)
- receptor_id: Usuario que recibe (Many2one, requerido)

# Contexto
- producto_id: Producto sobre el que se habla (Many2one, opcional)
- hilo_id: ID de hilo de conversación (generado automáticamente)

# Mensaje
- texto: Contenido (1-2000 caracteres)
- fecha: Fecha de envío (readonly)

# Estado
- leido: Boolean (default False)
- fecha_lectura: Fecha de lectura (readonly)

# Relacionados
- emisor_nombre, receptor_nombre, producto_nombre
```

**Generación automática de `hilo_id`**:
```python
# Formato: hilo_{id_menor}_{id_mayor}_{producto_id}
# Ejemplo: hilo_5_12_23
# Los IDs se ordenan para que el hilo sea único independiente del emisor
```

**Validaciones**:
```python
@api.constrains('texto')
# Texto no vacío
# Mínimo 1 carácter
# Máximo 2000 caracteres

@api.constrains('emisor_id', 'receptor_id')
# ❌ No auto-mensajes
```

**Métodos útiles para API**:
```python
@api.model
def get_conversacion(user_id, other_user_id, producto_id=None):
    # Obtiene todos los mensajes de una conversación
    # Retorna ordenado por fecha ascendente

@api.model
def get_mensajes_no_leidos(user_id):
    # Obtiene mensajes sin leer de un usuario
    # Útil para notificaciones

def action_marcar_leido():
    # Marca mensaje como leído + fecha_lectura

def action_marcar_no_leido():
    # Desmarca como leído
```

**`name_get()` personalizado**:
```python
# Si no leído: "🔴 Emisor → Receptor: Texto..."
# Si leído: "Emisor → Receptor: Texto..."
```

---

### **10. renaix.denuncia**
**Archivo**: `models/denuncia.py`
**Descripción**: Sistema de denuncias para moderar contenido inapropiado

**Tipos de denuncia**:
```python
- producto: Denuncia de producto
- comentario: Denuncia de comentario
- usuario: Denuncia de usuario
```

**Campos**:
```python
# Tipo y referencias polimórficas
- tipo: producto | comentario | usuario
- producto_id: Si tipo=producto (cascade)
- comentario_id: Si tipo=comentario (cascade)
- usuario_reportado_id: Si tipo=usuario

# Denunciante
- usuario_reportante_id: Quien denuncia (requerido)

# Motivo
- motivo: Descripción (requerido, mín 10 caracteres)
- categoria: contenido_inapropiado | spam | fraude | violencia |
             informacion_falsa | otro

# Estado
- estado: pendiente | en_revision | resuelta | rechazada
- empleado_asignado_id: Empleado que revisa (Many2one)

# Fechas
- fecha_denuncia: Fecha de creación (readonly)
- fecha_resolucion: Fecha de resolución

# Resolución
- resolucion: Descripción de la acción tomada

# Computado
- denunciado_nombre: Nombre de lo denunciado (computado)
```

**Validaciones**:
```python
@api.constrains('tipo', 'producto_id', 'comentario_id', 'usuario_reportado_id')
# Referencias coherentes:
# - Si tipo=producto → producto_id requerido, otros False
# - Si tipo=comentario → comentario_id requerido, otros False
# - Si tipo=usuario → usuario_reportado_id requerido, otros False

@api.constrains('motivo')
# Motivo no vacío
# Mínimo 10 caracteres
```

**Flujo automático al crear**:
```python
1. Busca grupo de moderadores (renaix.group_renaix_moderador)
2. Notifica a todos los moderadores vía Chatter
3. Mensaje HTML formateado con todos los detalles
4. Crea actividad "Revisar denuncia" para primer moderador
```

**Métodos de acción**:
```python
- action_asignar_a_mi()  # Asigna al usuario actual + estado en_revision
- action_resolver()      # Estado resuelta + fecha_resolucion + notifica
- action_rechazar()      # Estado rechazada + fecha_resolucion + notifica
- action_view_producto() # Abre formulario del producto denunciado
```

**Herencias**:
- `mail.thread`: Chatter completo
- `mail.activity.mixin`: Actividades

**`name_get()` personalizado**:
```python
"[Producto] Bicicleta Vintage - fraude"
```

---

### **11. res.company (Extendido)**
**Archivo**: `models/res_company.py`
**Descripción**: Extensión de la compañía para configuración de Renaix

*Nota: Este modelo no fue analizado en detalle en el análisis anterior.*

---

## 🔒 Sistema de Seguridad

### **Grupos de Seguridad** (`security/security.xml`)

#### **Jerarquía de 4 niveles**:

```
┌─────────────────────────┐
│  base.group_system      │ Sistema (técnico)
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│  group_renaix_admin     │ Administrador
│  - Control total        │
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│  group_renaix_moderador │ Moderador
│  - Gestión productos    │
│  - Gestión denuncias    │
│  - Moderación           │
└───────────┬─────────────┘
            │
┌───────────▼─────────────┐
│  group_renaix_user      │ Usuario
│  - Solo lectura         │
└─────────────────────────┘
```

#### **1. Usuario (group_renaix_user)**
**Permisos**:
- ✅ **Leer**: Categorías activas, Etiquetas activas, Productos publicados
- ✅ **Leer**: Comentarios activos, Mensajes, Compras, Valoraciones
- ❌ **Escribir/Crear/Eliminar**: Ninguno

**Reglas de registro**:
- Categorías: solo `active=True`
- Etiquetas: solo `active=True`
- Productos: solo `active=True` y `estado_venta in ['disponible', 'reservado', 'vendido']`
- Comentarios: solo `active=True`
- Mensajes: acceso total (read/write/create)
- Compras: solo lectura
- Valoraciones: solo lectura

#### **2. Moderador (group_renaix_moderador)**
**Permisos** (hereda de Usuario):
- ✅ **Gestión completa**: Productos, Comentarios, Denuncias
- ✅ **Gestión**: Compras (sin eliminar), Valoraciones
- ✅ **Ver inactivos**: Productos en borrador, Comentarios eliminados

**Capacidades especiales**:
- Revisar denuncias
- Eliminar/restaurar comentarios
- Moderar productos
- Gestionar transacciones problemáticas

#### **3. Administrador (group_renaix_admin)**
**Permisos** (hereda de Moderador):
- ✅ **Control total**: Categorías, Etiquetas
- ✅ **CRUD completo**: Todos los modelos
- ✅ **Configuración**: Parámetros del sistema

#### **4. Sistema (base.group_system)**
**Permisos**:
- ✅ Acceso técnico a todos los campos
- ✅ Ver `api_token` de usuarios

---

### **Archivo de Control de Acceso** (`security/ir.model.access.csv`)

Estructura típica:
```csv
id,name,model_id:id,group_id:id,perm_read,perm_write,perm_create,perm_unlink
access_producto_user,renaix.producto.user,model_renaix_producto,group_renaix_user,1,0,0,0
access_producto_moderador,renaix.producto.moderador,model_renaix_producto,group_renaix_moderador,1,1,1,1
```

---

### **Record Rules (Reglas de Registro)**

#### **Categorías**:
```xml
<!-- Usuario: solo activas, solo lectura -->
<record id="categoria_user_rule">
    <field name="domain_force">[('active', '=', True)]</field>
    <field name="perm_read">True</field>
</record>

<!-- Admin: todas, CRUD completo -->
<record id="categoria_admin_rule">
    <field name="domain_force">[(1, '=', 1)]</field>
    <field name="perm_read,write,create,unlink">True</field>
</record>
```

#### **Productos**:
```xml
<!-- Usuario: solo publicados -->
<record id="producto_user_rule">
    <field name="domain_force">
        [('active', '=', True),
         ('estado_venta', 'in', ['disponible', 'reservado', 'vendido'])]
    </field>
</record>

<!-- Moderador: todos (incluye borradores) -->
<record id="producto_moderador_rule">
    <field name="domain_force">[(1, '=', 1)]</field>
</record>
```

#### **Denuncias**:
```xml
<!-- Solo moderadores pueden ver denuncias -->
<record id="denuncia_moderador_rule">
    <field name="domain_force">[(1, '=', 1)]</field>
    <field name="groups">group_renaix_moderador</field>
</record>
```

---

## 📊 Estadísticas y Reportes

### **Campos Computados Almacenados**

#### **Por Usuario** (`res.partner`):
```python
@api.depends('valoracion_ids.puntuacion')
def _compute_valoracion_promedio(self):
    # Media de estrellas: 0.0 - 5.0

@api.depends('producto_ids', 'compra_comprador_ids', 'compra_vendedor_ids')
def _compute_estadisticas_productos(self):
    # productos_en_venta, productos_vendidos, productos_comprados

@api.depends('comentario_ids', 'denuncia_ids')
def _compute_estadisticas_actividad(self):
    # total_comentarios, total_denuncias_realizadas
```

#### **Por Producto** (`renaix.producto`):
```python
@api.depends('comentario_ids', 'denuncia_ids')
def _compute_estadisticas(self):
    # total_comentarios, total_denuncias

@api.depends('fecha_publicacion')
def _compute_dias_publicado(self):
    # Días desde publicación
```

#### **Por Categoría/Etiqueta**:
```python
@api.depends('producto_ids')
def _compute_producto_count(self):
    # Cantidad de productos
```

---

### **Vistas de Estadísticas** (`views/estadisticas_views.xml`)

Según el manifest, incluye:
- ✅ **4 Graph views**: Gráficos de barras/líneas/pastel
- ✅ **2 Listados avanzados**: Reportes tabulares
- ✅ **Dashboard jerárquico**: Panel de control

**Posibles gráficos**:
- Productos por categoría
- Valoraciones promedio por usuario
- Evolución de ventas por mes
- Denuncias por tipo/estado

---

### **Informes PDF** (`reports/report_partner_activity.xml`)

**Informe QWeb profesional**:
- Actividad de usuarios
- Productos publicados/vendidos
- Valoraciones recibidas
- Estadísticas generales

**Características**:
- Formato profesional
- Logo de la empresa
- Datos estructurados
- Imprimible

---

## 🎨 Interfaz de Usuario

### **13 Archivos de Vistas XML**

| Archivo | Modelos | Vistas |
|---------|---------|--------|
| `categoria_views.xml` | renaix.categoria | Tree, Form, Search, Kanban |
| `etiqueta_views.xml` | renaix.etiqueta | Tree, Form, Search, Kanban |
| `res_partner_views.xml` | res.partner | Form extendido, Tree, Search |
| `producto_views.xml` | renaix.producto | Tree, Form, Search, Kanban |
| `producto_imagen_views.xml` | renaix.producto.imagen | Tree, Form |
| `compra_views.xml` | renaix.compra | Tree, Form, Search |
| `valoracion_views.xml` | renaix.valoracion | Tree, Form, Search |
| `comentario_views.xml` | renaix.comentario | Tree, Form, Search |
| `mensaje_views.xml` | renaix.mensaje | Tree, Form, Search |
| `denuncia_views.xml` | renaix.denuncia | Tree, Form, Search |
| `estadisticas_views.xml` | Múltiples | Graph, Pivot |
| `menu.xml` | - | Menús jerárquicos |

### **Tipos de Vistas**:
- **Tree**: Listados tabulares
- **Form**: Formularios de edición
- **Search**: Filtros y agrupaciones
- **Kanban**: Tarjetas visuales
- **Graph**: Gráficos estadísticos
- **Pivot**: Tablas dinámicas

### **Widgets Especiales**:
```xml
<field name="precio" widget="monetary"/>
<field name="image" widget="image"/>
<field name="etiqueta_ids" widget="many2many_tags"/>
<field name="estado_venta" widget="badge"/>
<field name="puntuacion" widget="priority"/>
```

---

### **Estructura de Menús** (`views/menu.xml`)

```
📱 Renaix
├── 📊 Dashboard
├── 🛍️ Productos
│   ├── Todos los Productos
│   ├── Categorías
│   └── Etiquetas
├── 👥 Usuarios
│   ├── Usuarios de la App
│   └── Valoraciones
├── 💰 Transacciones
│   ├── Compras/Ventas
│   └── Historial
├── 💬 Comunicación
│   ├── Comentarios
│   └── Mensajes
├── ⚠️ Moderación
│   ├── Denuncias Pendientes
│   ├── Denuncias en Revisión
│   └── Historial
└── 📈 Estadísticas
    ├── Productos por Categoría
    ├── Ventas por Mes
    └── Valoraciones
```

---

## 📦 Datos de Demostración

### **Sprint 1 - Completado** ✅

**Datos incluidos**:
```
✅ 9 usuarios de la app (8 activos + 1 suspendido)
✅ 10 etiquetas populares
✅ 17 productos variados:
   - 14 disponibles
   - 2 vendidos
   - 1 borrador
✅ 5 transacciones en diferentes estados
✅ 6 valoraciones bidireccionales (promedio 4.83⭐)
✅ 13 comentarios en productos
✅ 9 mensajes privados (7 leídos, 2 sin leer)
✅ 7 denuncias para gestión
```

### **Archivos de Datos** (`data/`)

**Orden de carga** (crítico para integridad referencial):
```xml
1. sequences.xml           # Secuencias automáticas
2. categorias_data.xml     # Categorías base
3. usuarios_data.xml       # Usuarios de la app
4. etiquetas_data.xml      # Etiquetas populares
5. productos_data.xml      # Productos con imágenes
6. compras_data.xml        # Transacciones
7. valoraciones_data.xml   # Valoraciones bidireccionales
8. comentarios_data.xml    # Comentarios en productos
9. mensajes_data.xml       # Mensajes entre usuarios
10. denuncias_data.xml     # Denuncias para moderar
```

**Secuencias** (`data/sequences.xml`):
```xml
<record id="seq_compra" model="ir.sequence">
    <field name="name">Secuencia Compras</field>
    <field name="code">renaix.compra</field>
    <field name="prefix">COMP/%(year)s/</field>
    <field name="padding">4</field>
</record>
```
Genera: `COMP/2025/0001`, `COMP/2025/0002`, etc.

---

## 🔧 Características Técnicas Destacadas

### **1. Herencia de Mixins**
```python
# Mail features
_inherit = ['mail.thread', 'mail.activity.mixin']
# Proporciona: Chatter, seguidores, actividades, notificaciones

# Image features
_inherit = ['mail.thread', 'mail.activity.mixin', 'image.mixin']
# Proporciona: image, image_medium, image_small
```

### **2. Campos Computados Almacenados**
```python
valoracion_promedio = fields.Float(
    compute='_compute_valoracion_promedio',
    store=True,  # ✅ Almacenado en BD → filtrable y buscable
)
```
**Ventajas**:
- Performance: no recalcula en cada lectura
- Filtrable en búsquedas
- Ordenable en vistas

### **3. Constraints SQL vs Python**

**SQL** (más rápido, a nivel de BD):
```python
_sql_constraints = [
    ('precio_positivo', 'CHECK(precio >= 0)', 'El precio debe ser >= 0.'),
    ('name_unique', 'UNIQUE(name)', 'Ya existe con este nombre.'),
]
```

**Python** (más flexible, lógica compleja):
```python
@api.constrains('etiqueta_ids')
def _check_max_etiquetas(self):
    for producto in self:
        if len(producto.etiqueta_ids) > 5:
            raise ValidationError('Máximo 5 etiquetas.')
```

### **4. Métodos `name_get()` Personalizados**
```python
def name_get(self):
    result = []
    for partner in self:
        if partner.es_usuario_app:
            name = f"📱 {partner.name} ({partner.valoracion_promedio:.1f}⭐)"
        else:
            name = partner.name
        result.append((partner.id, name))
    return result
```
**Resultado**: Mejora UX en selects y Many2one

### **5. Notificaciones Automáticas**
```python
# Al crear compra
compra.message_post(
    body=f"""
        <h3>🎉 ¡Alguien quiere comprar tu producto!</h3>
        <p><b>Comprador:</b> {compra.comprador_id.name}</p>
        <p><b>Email:</b> {compra.comprador_id.email}</p>
    """,
    subject=f"Nueva compra: {compra.producto_id.name}",
    partner_ids=[compra.vendedor_id.id]
)
```

### **6. Tracking de Cambios**
```python
estado_venta = fields.Selection(
    ...,
    tracking=True,  # ✅ Registra cambios en Chatter
)
```

### **7. Seguidores Automáticos**
```python
# Al crear producto
producto.message_subscribe(partner_ids=[producto.propietario_id.id])

# Al crear compra
compra.message_subscribe(partner_ids=[compra.comprador_id.id, compra.vendedor_id.id])
```

### **8. Campos Relacionados Almacenados**
```python
producto_nombre = fields.Char(
    related='producto_id.name',
    string='Producto',
    store=True,  # ✅ Permite búsquedas rápidas
    readonly=True
)
```

### **9. Dominios Dinámicos**
```python
propietario_id = fields.Many2one(
    'res.partner',
    domain=[('es_usuario_app', '=', True)],  # ✅ Filtra automáticamente
)
```

### **10. Métodos Helper para API**
```python
@api.model
def get_conversacion(self, user_id, other_user_id, producto_id=None):
    """Método preparado para llamadas desde API REST"""
    domain = [
        '|',
        '&', ('emisor_id', '=', user_id), ('receptor_id', '=', other_user_id),
        '&', ('emisor_id', '=', other_user_id), ('receptor_id', '=', user_id)
    ]
    if producto_id:
        domain.append(('producto_id', '=', producto_id))
    return self.search(domain, order='fecha asc')
```

---

## 🚀 Preparación para Integración con App Móvil

### **Módulo API REST Separado**
**Ubicación**: `erp/docker/custom_addons/renaix_api/`

**Campos en modelos core para API**:
```python
# res.partner
partner_gid = fields.Char()      # UUID único para sincronización
api_token = fields.Char()        # Token de autenticación

# renaix.producto.imagen
url_imagen = fields.Char()       # URL pública de la imagen

# renaix.mensaje
hilo_id = fields.Char()          # Agrupar conversaciones
```

### **Métodos Útiles para API**

**Mensajería**:
```python
# Obtener conversación entre dos usuarios
mensajes = self.env['renaix.mensaje'].get_conversacion(
    user_id=5,
    other_user_id=12,
    producto_id=23
)

# Obtener mensajes no leídos
no_leidos = self.env['renaix.mensaje'].get_mensajes_no_leidos(user_id=5)
```

**Etiquetas populares**:
```python
# Top 10 etiquetas más usadas
etiquetas = self.env['renaix.etiqueta'].get_etiquetas_mas_usadas(limit=10)
```

### **URLs de Imágenes**
```python
# Generación automática
url = f'{base_url}/web/image/renaix.producto.imagen/{imagen.id}/imagen'

# Acceso público desde app móvil
GET https://erp.renaix.com/web/image/renaix.producto.imagen/42/imagen
```

### **Sincronización con UUID**
```python
# Al crear usuario app
partner_gid = str(uuid.uuid4())
# Ejemplo: "a8f5f167-0e8e-4e24-8f7f-3b9c7c8e3f2a"

# La app móvil usa este UUID para sincronizar
# No depende del ID interno de Odoo
```

---

## 🔄 Flujos de Negocio Principales

### **1. Publicación de Producto**
```
┌─────────────┐
│  BORRADOR   │ Usuario crea producto
└──────┬──────┘
       │ Añadir imágenes (mín 1)
       │ Añadir información completa
       ▼
┌─────────────┐
│  VALIDAR    │ @api.constrains valida:
│  IMÁGENES   │ - Mínimo 1 imagen
└──────┬──────┘ - Máximo 10 imágenes
       │        - Máximo 5 etiquetas
       ▼
┌─────────────┐
│  PUBLICAR   │ action_publicar()
│             │ - estado_venta = 'disponible'
└──────┬──────┘ - fecha_publicacion = now()
       │
       ▼
┌─────────────┐
│ DISPONIBLE  │ Visible en app móvil
└─────────────┘
```

### **2. Proceso de Compra Completo**
```
PRODUCTO: Disponible
       │
       ▼
┌─────────────────────────────────────┐
│  USUARIO INTERESADO                 │
│  - Ve producto en app               │
│  - Click "Comprar"                  │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  CREATE COMPRA                      │
│  ✅ Validar: comprador ≠ propietario │
│  ✅ Validar: producto disponible     │
│  ✅ Generar código: COMP/2025/0001   │
│  ✅ Estado: pendiente                │
│  ✅ Producto → RESERVADO             │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  NOTIFICACIONES AUTOMÁTICAS         │
│  📧 Email al vendedor con datos     │
│  📧 Email al comprador confirmando  │
│  👥 Añadir seguidores (Chatter)     │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  VENDEDOR CONFIRMA                  │
│  action_confirmar()                 │
│  Estado: confirmada                 │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  INTERCAMBIO FÍSICO                 │
│  - Usuarios se encuentran           │
│  - Entregan producto y dinero       │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  COMPLETAR COMPRA                   │
│  action_completar()                 │
│  ✅ Estado: completada               │
│  ✅ Producto → VENDIDO               │
│  ✅ Crear actividad: valorar (2 días)│
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  SOLICITAR VALORACIONES             │
│  (2 días después)                   │
│  📧 Recordatorio a comprador        │
│  📧 Recordatorio a vendedor         │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  VALORACIONES BIDIRECCIONALES       │
│  👍 Comprador valora a vendedor     │
│  👍 Vendedor valora a comprador     │
│  ⭐ Actualiza valoracion_promedio   │
└─────────────────────────────────────┘

FLUJO ALTERNATIVO: Cancelación
       │
       ▼
┌─────────────────────────────────────┐
│  action_cancelar()                  │
│  ✅ Estado: cancelada                │
│  ✅ Producto → DISPONIBLE            │
│  📧 Notifica a ambas partes         │
└─────────────────────────────────────┘
```

### **3. Sistema de Moderación de Denuncias**
```
┌─────────────────────────────────────┐
│  USUARIO DENUNCIA CONTENIDO         │
│  - Selecciona tipo: producto/       │
│    comentario/usuario               │
│  - Selecciona categoría             │
│  - Escribe motivo (mín 10 chars)    │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  CREATE DENUNCIA                    │
│  ✅ Validar referencias coherentes   │
│  ✅ Estado: pendiente                │
│  ✅ Buscar grupo de moderadores      │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  NOTIFICACIÓN AUTOMÁTICA            │
│  🚨 Mensaje a todos los moderadores │
│  📋 Crear actividad para revisar    │
│  📊 HTML formateado con detalles    │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  MODERADOR SE ASIGNA                │
│  action_asignar_a_mi()              │
│  Estado: en_revision                │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  MODERADOR REVISA                   │
│  - Ver contenido denunciado         │
│  - Evaluar gravedad                 │
│  - Decisión                         │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        ▼             ▼
┌──────────────┐  ┌──────────────┐
│   RESOLVER   │  │  RECHAZAR    │
│   ✅ Procede  │  │  ❌ No procede│
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│ Acción tomada│  │Sin acción    │
│ - Eliminar   │  │              │
│ - Suspender  │  │              │
│ - Advertir   │  │              │
└──────┬───────┘  └──────┬───────┘
       │                 │
       └────────┬────────┘
                ▼
┌─────────────────────────────────────┐
│  REGISTRAR RESOLUCIÓN               │
│  ✅ fecha_resolucion = now()         │
│  ✅ resolucion = "Texto acción"      │
│  📧 Notificar al denunciante        │
└─────────────────────────────────────┘
```

### **4. Flujo de Valoraciones**
```
COMPRA: Completada
       │
       ▼
┌─────────────────────────────────────┐
│  ESPERAR 2 DÍAS                     │
│  (Actividad programada)             │
└──────────────┬──────────────────────┘
               ▼
┌─────────────────────────────────────┐
│  SOLICITAR VALORACIONES             │
│  📧 Recordatorio a comprador        │
│  📧 Recordatorio a vendedor         │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        ▼             ▼
┌──────────────┐  ┌──────────────┐
│  COMPRADOR   │  │  VENDEDOR    │
│  VALORA      │  │  VALORA      │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│CREATE        │  │CREATE        │
│VALORACION    │  │VALORACION    │
│tipo: comp→vend│  │tipo: vend→comp│
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────────────────────┐
│  VALIDACIONES                │
│  ✅ Puntuación 1-5            │
│  ✅ Compra completada         │
│  ✅ No autovaloración         │
│  ✅ Coherencia (quien valora) │
│  ✅ 1 valoración por tipo     │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  NOTIFICAR AL VALORADO       │
│  ⭐⭐⭐⭐ "X te valoró con..."  │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  RECALCULAR PROMEDIO         │
│  valoracion_promedio =       │
│  sum(puntuaciones) / count   │
└──────────────────────────────┘
```

---

## 📈 Relaciones Entre Modelos (Diagrama ER)

```
┌──────────────────┐
│   res.partner    │ (Usuario App)
│  - partner_gid   │◄───────┐
│  - es_usuario_app│        │
│  - valoracion_   │        │
│    promedio      │        │
└────────┬─────────┘        │
         │                  │
         │ 1:N              │
         ▼                  │
┌──────────────────┐        │
│ renaix.producto  │        │
│  - name          │        │ M:1
│  - precio        │        │ propietario_id
│  - estado_venta  │────────┘
└────┬────┬────────┘
     │    │ 1:N
     │    └──────────────────┐
     │                       │
     │ M:N                   ▼
     │              ┌──────────────────┐
     │              │ renaix.producto. │
     │              │     imagen       │
     │              │  - imagen        │
     │              │  - url_imagen    │
     │              └──────────────────┘
     │
     ├─────────────────────┬──────────────────┐
     │                     │                  │
     │ M:N                 │ 1:N              │ 1:N
     ▼                     ▼                  ▼
┌──────────┐      ┌──────────────┐   ┌──────────────┐
│ renaix.  │      │  renaix.     │   │  renaix.     │
│etiqueta  │      │  comentario  │   │  denuncia    │
│ - name   │      │  - texto     │   │  - motivo    │
│ - color  │      │  - active    │   │  - estado    │
└──────────┘      └──────────────┘   └──────────────┘

┌──────────────────┐
│ renaix.categoria │
│  - name          │
│  - image         │
└────────┬─────────┘
         │ 1:N
         │
         └───────► renaix.producto

┌──────────────────┐
│ renaix.compra    │ (Transacción)
│  - codigo        │◄──────┬──────────┐
│  - precio_final  │       │          │
│  - estado        │       │          │
└────────┬─────────┘       │          │
         │                 │          │
         │ M:1             │ M:1      │ M:1
         │                 │          │
         │                 │          │
         ▼                 │          │
    producto_id            │          │
                           │          │
                  comprador_id  vendedor_id
                    (partner)    (partner)
         │
         │ 1:N
         ▼
┌──────────────────┐
│ renaix.valoracion│
│  - puntuacion    │
│  - tipo_valoracion│
│  - usuario_      │
│    valorador_id  │
│  - usuario_      │
│    valorado_id   │
└──────────────────┘

┌──────────────────┐
│ renaix.mensaje   │
│  - texto         │
│  - hilo_id       │◄──────┬──────────┐
│  - leido         │       │          │
└──────────────────┘       │          │
                           │          │
                      emisor_id  receptor_id
                      (partner)   (partner)
```

---

## 💡 Puntos Fuertes del Módulo

### **1. Arquitectura Sólida**
- ✅ 11 modelos bien estructurados
- ✅ Relaciones claras y coherentes
- ✅ Separación de responsabilidades

### **2. Validaciones Exhaustivas**
- ✅ Constraints SQL para performance
- ✅ Constraints Python para lógica compleja
- ✅ Prevención de datos inconsistentes

### **3. UX Optimizada**
- ✅ `name_get()` personalizados con emojis e info
- ✅ Campos computados para facilitar búsquedas
- ✅ Widgets especializados (monetary, badge, priority)

### **4. Trazabilidad Completa**
- ✅ Chatter en todos los modelos principales
- ✅ Tracking de cambios en campos críticos
- ✅ Notificaciones automáticas
- ✅ Actividades programadas

### **5. Seguridad Robusta**
- ✅ 4 niveles jerárquicos de permisos
- ✅ Record Rules para filtrado automático
- ✅ Dominios en relaciones
- ✅ Grupos especializados

### **6. Preparado para API**
- ✅ Campo `partner_gid` (UUID)
- ✅ Campo `api_token` para autenticación
- ✅ URLs públicas de imágenes
- ✅ Métodos helper: `get_conversacion()`, `get_mensajes_no_leidos()`

### **7. Datos Demo Completos**
- ✅ Escenarios realistas
- ✅ Integridad referencial
- ✅ Facilita testing y demos

### **8. Código Limpio**
- ✅ Docstrings detallados
- ✅ Comentarios útiles
- ✅ Nombres descriptivos
- ✅ Estructura consistente

### **9. Performance Optimizada**
- ✅ Índices en campos clave (`partner_gid`, `name`)
- ✅ Campos computados almacenados
- ✅ Constraints SQL cuando es posible

### **10. Extensibilidad**
- ✅ Hereda de modelos estándar (res.partner)
- ✅ Usa mixins de Odoo (mail.thread, image.mixin)
- ✅ Fácil de extender con nuevos módulos

---

## 🚧 Posibles Mejoras Futuras

### **Funcionalidades**:
1. Sistema de ofertas (negociación de precio)
2. Favoritos/Wishlist
3. Chat en tiempo real (WebSocket)
4. Notificaciones push a app móvil
5. Sistema de reputación avanzado
6. Verificación de usuarios (KYC)
7. Pasarela de pagos integrada
8. Sistema de envíos

### **Técnicas**:
1. Caché de búsquedas frecuentes
2. Índices compuestos en BD
3. Búsqueda full-text con PostgreSQL
4. Compresión de imágenes automática
5. CDN para imágenes
6. Rate limiting en API
7. Logs de auditoría detallados
8. Tests automatizados

---

## 📊 Métricas del Código

### **Estimación de Líneas de Código**:
```
Modelos Python:     ~2,500 líneas
Vistas XML:         ~1,500 líneas
Datos XML:          ~1,000 líneas
Seguridad:          ~400 líneas
Reportes:           ~300 líneas
────────────────────────────────
TOTAL:              ~5,700 líneas
```

### **Complejidad**:
- **Modelos**: Media-Alta
- **Validaciones**: Alta (exhaustivas)
- **Relaciones**: Alta (11 modelos interconectados)
- **Lógica de negocio**: Media-Alta

### **Calidad del Código**:
⭐⭐⭐⭐⭐ **5/5 - Excelente**

**Criterios**:
- ✅ Sigue best practices de Odoo
- ✅ Código limpio y legible
- ✅ Bien documentado
- ✅ Validaciones robustas
- ✅ Sin code smells evidentes
- ✅ Arquitectura escalable

---

## 🎯 Estado del Proyecto

### **Sprint 1**: ✅ **COMPLETADO**

**Entregables**:
- [x] Modelo de datos completo (11 modelos)
- [x] Backend administrativo con vistas List/Form/Search
- [x] Sistema de permisos (4 niveles)
- [x] Estadísticas con gráficos (4 Graph views)
- [x] Listados avanzados (2 listados)
- [x] Dashboard organizado jerárquicamente
- [x] Informe QWeb profesional (PDF)
- [x] Datos de demostración completos

### **Próximos Pasos** (Sprint 2):
- [ ] Módulo API REST (renaix_api)
- [ ] Integración con app móvil
- [ ] Sistema de autenticación OAuth2
- [ ] Endpoints para todas las operaciones CRUD
- [ ] Websockets para mensajería en tiempo real
- [ ] Notificaciones push

---

## 📚 Documentación Adicional

### **Archivos Relacionados**:
- [README.md](../README.md) - Documentación general del proyecto
- [renaix_api/](../erp/docker/custom_addons/renaix_api/) - Módulo API REST

### **Enlaces Útiles**:
- [Odoo Documentation](https://www.odoo.com/documentation/17.0/)
- [GitHub Repository](https://github.com/Alejandro-WOU/projecte-dam-25-26-javier-alejandro)

---

## 👥 Autores

**Javier Herraiz Calatayud** (H3rr41z)
**Alejandro Sánchez Serrano** (Alejandro-WOU)

**Curso**: 2025-26
**Proyecto**: Intermodular DAM

---

## 📄 Licencia

**LGPL-3** - Lesser General Public License v3.0

---

**Fecha del análisis**: 2026-01-21
**Versión del módulo**: 1.0.0
**Odoo version**: 17.0
