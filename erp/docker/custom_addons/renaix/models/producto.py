# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class Producto(models.Model):
    """
    Modelo: Producto
    Descripción: Productos de segunda mano publicados por usuarios
    """
    _name = 'renaix.producto'
    _description = 'Producto de Segunda Mano'
    _inherit = ['mail.thread', 'mail.activity.mixin', 'image.mixin']
    _order = 'fecha_publicacion desc, id desc'
    
    # Campos básicos
    name = fields.Char(
        string='Nombre del Producto',
        required=True,
        tracking=True,
        index=True,
        help='Nombre descriptivo del producto'
    )
    
    descripcion = fields.Text(
        string='Descripción',
        help='Descripción detallada del producto'
    )
    
    precio = fields.Float(
        string='Precio',
        required=True,
        tracking=True,
        help='Precio del producto en euros'
    )
    
    # Campos de estado y antigüedad
    antiguedad = fields.Char(
        string='Antigüedad',
        help='Antigüedad del producto (ej: 6 meses, 2 años)'
    )
    
    estado_producto = fields.Selection([
        ('nuevo', 'Nuevo'),
        ('como_nuevo', 'Como Nuevo'),
        ('buen_estado', 'Buen Estado'),
        ('aceptable', 'Estado Aceptable'),
        ('para_reparar', 'Para Reparar'),
    ], string='Estado del Producto', 
       default='buen_estado',
       required=True,
       tracking=True,
       help='Condición física del producto'
    )
    
    estado_venta = fields.Selection([
        ('borrador', 'Borrador'),
        ('disponible', 'Disponible'),
        ('reservado', 'Reservado'),
        ('vendido', 'Vendido'),
        ('eliminado', 'Eliminado'),
    ], string='Estado de Venta',
       default='borrador',
       required=True,
       tracking=True,
       help='Estado actual de la venta'
    )
    
    # Ubicación
    ubicacion = fields.Char(
        string='Ubicación',
        help='Ubicación del producto (ciudad, código postal, etc.)'
    )
    
    # Campos de fecha
    fecha_publicacion = fields.Datetime(
        string='Fecha de Publicación',
        default=fields.Datetime.now,
        required=True,
        tracking=True
    )
    
    fecha_actualizacion = fields.Datetime(
        string='Última Actualización',
        default=fields.Datetime.now
    )
    
    # Control de estado
    active = fields.Boolean(
        string='Activo',
        default=True,
        tracking=True,
        help='Si está inactivo, no se mostrará en la app'
    )
    
    # Relación con propietario (Usuario App)
    propietario_id = fields.Many2one(
        'res.partner',
        string='Propietario',
        required=True,
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        tracking=True,
        help='Usuario que publica el producto'
    )
    
    # Relación con categoría
    categoria_id = fields.Many2one(
        'renaix.categoria',
        string='Categoría',
        required=True,
        ondelete='restrict',
        tracking=True,
        help='Categoría del producto'
    )
    
    # Relación con etiquetas (Many2many, máx 5)
    etiqueta_ids = fields.Many2many(
        'renaix.etiqueta',
        'renaix_producto_etiqueta_rel',
        'producto_id',
        'etiqueta_id',
        string='Etiquetas',
        help='Etiquetas para facilitar búsquedas (máximo 5)'
    )
    
    # Relación con imágenes (One2many, mín 1, máx 10)
    imagen_ids = fields.One2many(
        'renaix.producto.imagen',
        'producto_id',
        string='Imágenes',
        help='Imágenes del producto (mínimo 1, máximo 10)'
    )
    
    # Relación con comentarios
    comentario_ids = fields.One2many(
        'renaix.comentario',
        'producto_id',
        string='Comentarios'
    )
    
    # Relación con compra
    compra_id = fields.Many2one(
        'renaix.compra',
        string='Compra',
        ondelete='set null',
        help='Compra asociada si el producto fue vendido'
    )
    
    # Relación con denuncias
    denuncia_ids = fields.One2many(
        'renaix.denuncia',
        'producto_id',
        string='Denuncias'
    )
    
    # Campos computados
    total_comentarios = fields.Integer(
        string='Nº Comentarios',
        compute='_compute_estadisticas',
        store=True  # ✅ Para poder filtrar
    )
    
    total_denuncias = fields.Integer(
        string='Nº Denuncias',
        compute='_compute_estadisticas',
        store=True  # ✅ Para poder filtrar
    )
    
    total_imagenes = fields.Integer(
        string='Nº Imágenes',
        compute='_compute_total_imagenes'
    )
    
    dias_publicado = fields.Integer(
        string='Días Publicado',
        compute='_compute_dias_publicado'
    )
    
    # Campos relacionados (para facilitar búsquedas y filtros)
    propietario_email = fields.Char(
        related='propietario_id.email',
        string='Email Propietario',
        readonly=True,
        store=True
    )
    
    propietario_phone = fields.Char(
        related='propietario_id.phone',
        string='Teléfono Propietario',
        readonly=True,
        store=True
    )
    
    # Currency para el widget monetary
    currency_id = fields.Many2one(
        'res.currency',
        string='Moneda',
        default=lambda self: self.env.company.currency_id,
        required=True
    )
    
    # Constraints SQL
    _sql_constraints = [
        ('precio_positivo', 'CHECK(precio >= 0)', 'El precio debe ser mayor o igual a 0.'),
    ]
    
    @api.depends('comentario_ids', 'denuncia_ids')
    def _compute_estadisticas(self):
        """Calcula estadísticas del producto"""
        for producto in self:
            producto.total_comentarios = len(producto.comentario_ids)
            producto.total_denuncias = len(producto.denuncia_ids)
    
    @api.depends('imagen_ids')
    def _compute_total_imagenes(self):
        """Cuenta el total de imágenes"""
        for producto in self:
            producto.total_imagenes = len(producto.imagen_ids)
    
    @api.depends('fecha_publicacion')
    def _compute_dias_publicado(self):
        """Calcula días desde la publicación"""
        for producto in self:
            if producto.fecha_publicacion:
                delta = fields.Datetime.now() - producto.fecha_publicacion
                producto.dias_publicado = delta.days
            else:
                producto.dias_publicado = 0
    
    @api.constrains('etiqueta_ids')
    def _check_max_etiquetas(self):
        """Valida que no haya más de 5 etiquetas"""
        for producto in self:
            if len(producto.etiqueta_ids) > 5:
                raise ValidationError('Un producto no puede tener más de 5 etiquetas.')
    
    @api.constrains('imagen_ids')
    def _check_imagenes(self):
        """Valida cantidad de imágenes (mín 1, máx 10)"""
        for producto in self:
            total_img = len(producto.imagen_ids)
            if producto.estado_venta != 'borrador' and total_img < 1:
                raise ValidationError('El producto debe tener al menos 1 imagen.')
            if total_img > 10:
                raise ValidationError('El producto no puede tener más de 10 imágenes.')
    
    @api.constrains('precio')
    def _check_precio(self):
        """Valida que el precio sea razonable"""
        for producto in self:
            if producto.precio < 0:
                raise ValidationError('El precio no puede ser negativo.')
            if producto.precio > 1000000:
                raise ValidationError('El precio parece demasiado alto. Por favor, verifica.')
    
    @api.model
    def create(self, vals):
        """Al crear: añadir propietario como seguidor"""
        producto = super(Producto, self).create(vals)
        
        # Añadir propietario como seguidor para recibir notificaciones
        if producto.propietario_id:
            producto.message_subscribe(partner_ids=[producto.propietario_id.id])
        
        # Mensaje de creación
        producto.message_post(
            body=f'Producto "{producto.name}" creado por {producto.propietario_id.name}',
            subject='Producto Creado'
        )
        
        return producto
    
    def write(self, vals):
        """Al actualizar: registrar cambios importantes"""
        # Detectar cambio de estado
        if 'estado_venta' in vals:
            for producto in self:
                estado_anterior = producto.estado_venta
                estado_nuevo = vals['estado_venta']
                
                if estado_anterior != estado_nuevo:
                    producto.message_post(
                        body=f'Estado cambió de "{dict(producto._fields["estado_venta"].selection)[estado_anterior]}" '
                             f'a "{dict(producto._fields["estado_venta"].selection)[estado_nuevo]}"',
                        subject='Cambio de Estado'
                    )
        
        # Actualizar fecha de modificación
        if 'fecha_actualizacion' not in vals:
            vals['fecha_actualizacion'] = fields.Datetime.now()
        
        return super(Producto, self).write(vals)
    
    def action_publicar(self):
        """Publica el producto (cambia estado a disponible)"""
        for producto in self:
            if producto.estado_venta == 'borrador':
                # Validar que tenga al menos 1 imagen
                if not producto.imagen_ids:
                    raise ValidationError('Debe añadir al menos 1 imagen antes de publicar.')
                
                producto.estado_venta = 'disponible'
                producto.fecha_publicacion = fields.Datetime.now()
                
                producto.message_post(
                    body=f'Producto publicado y disponible para venta',
                    subject='Producto Publicado',
                    message_type='notification'
                )
    
    def action_reservar(self):
        """Marca el producto como reservado"""
        for producto in self:
            if producto.estado_venta == 'disponible':
                producto.estado_venta = 'reservado'
    
    def action_marcar_vendido(self):
        """Marca el producto como vendido"""
        for producto in self:
            if producto.estado_venta in ['disponible', 'reservado']:
                producto.estado_venta = 'vendido'
    
    def action_view_comentarios(self):
        """Ver todos los comentarios del producto"""
        self.ensure_one()
        return {
            'name': f'Comentarios de {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.comentario',
            'view_mode': 'tree,form',
            'domain': [('producto_id', '=', self.id)],
            'context': {'default_producto_id': self.id},
        }
    
    def action_view_denuncias(self):
        """Ver todas las denuncias del producto"""
        self.ensure_one()
        return {
            'name': f'Denuncias de {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.denuncia',
            'view_mode': 'tree,form',
            'domain': [('producto_id', '=', self.id)],
        }