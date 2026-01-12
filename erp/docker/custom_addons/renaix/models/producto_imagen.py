# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class ProductoImagen(models.Model):
    """
    Modelo: Imagen de Producto
    Descripción: Imágenes asociadas a productos (mínimo 1, máximo 10)
    """
    _name = 'renaix.producto.imagen'
    _description = 'Imagen de Producto'
    _order = 'secuencia, id'
    
    # Relación con producto
    producto_id = fields.Many2one(
        'renaix.producto',
        string='Producto',
        required=True,
        ondelete='cascade',
        help='Producto al que pertenece esta imagen'
    )
    
    # La imagen en sí
    imagen = fields.Image(
        string='Imagen',
        required=True,
        max_width=1920,
        max_height=1920,
        help='Imagen del producto (máx 1920x1920px)'
    )
    
    # Miniatura para listados
    imagen_small = fields.Image(
        string='Miniatura',
        related='imagen',
        max_width=256,
        max_height=256,
        store=True
    )
    
    # Secuencia para ordenar las imágenes
    secuencia = fields.Integer(
        string='Orden',
        default=10,
        help='Orden de aparición de la imagen (la primera será la principal)'
    )
    
    # Indicar si es la imagen principal
    es_principal = fields.Boolean(
        string='Imagen Principal',
        default=False,
        help='Si es True, esta imagen se mostrará como principal en listados'
    )
    
    # Descripción opcional de la imagen
    descripcion = fields.Char(
        string='Descripción',
        help='Descripción opcional de la imagen'
    )
    
    # URL de la imagen (se genera automáticamente)
    url_imagen = fields.Char(
        string='URL',
        compute='_compute_url_imagen',
        help='URL pública de la imagen para la API'
    )
    
    # Tamaño del archivo (informativo)
    tamano_kb = fields.Integer(
        string='Tamaño (KB)',
        compute='_compute_tamano',
        help='Tamaño aproximado de la imagen en KB'
    )
    
    @api.depends('imagen')
    def _compute_url_imagen(self):
        """Genera la URL de acceso a la imagen"""
        for imagen in self:
            if imagen.id and imagen.imagen:
                base_url = self.env['ir.config_parameter'].sudo().get_param('web.base.url')
                imagen.url_imagen = f'{base_url}/web/image/renaix.producto.imagen/{imagen.id}/imagen'
            else:
                imagen.url_imagen = False
    
    @api.depends('imagen')
    def _compute_tamano(self):
        """Calcula el tamaño aproximado de la imagen"""
        for imagen in self:
            if imagen.imagen:
                # Estimar tamaño en KB (base64 es ~33% más grande que binario)
                imagen.tamano_kb = len(imagen.imagen) * 3 // 4 // 1024
            else:
                imagen.tamano_kb = 0
    
    @api.model
    def create(self, vals):
        """Al crear: si es la primera imagen, marcarla como principal"""
        imagen = super(ProductoImagen, self).create(vals)
        
        # Si es la primera imagen del producto, marcarla como principal
        if imagen.producto_id:
            imagenes_producto = self.search([
                ('producto_id', '=', imagen.producto_id.id),
                ('id', '!=', imagen.id)
            ])
            if not imagenes_producto:
                imagen.es_principal = True
        
        return imagen
    
    @api.constrains('producto_id')
    def _check_max_imagenes_producto(self):
        """Valida que un producto no tenga más de 10 imágenes"""
        for imagen in self:
            if imagen.producto_id:
                total = self.search_count([('producto_id', '=', imagen.producto_id.id)])
                if total > 10:
                    raise ValidationError(
                        f'El producto "{imagen.producto_id.name}" ya tiene el máximo de 10 imágenes.'
                    )
    
    def write(self, vals):
        """Al marcar como principal, desmarcar las demás"""
        if vals.get('es_principal', False):
            for imagen in self:
                # Desmarcar otras imágenes como principal
                otras_imagenes = self.search([
                    ('producto_id', '=', imagen.producto_id.id),
                    ('id', '!=', imagen.id),
                    ('es_principal', '=', True)
                ])
                if otras_imagenes:
                    otras_imagenes.write({'es_principal': False})
        
        return super(ProductoImagen, self).write(vals)
    
    def action_marcar_principal(self):
        """Marca esta imagen como principal"""
        self.ensure_one()
        self.es_principal = True
    
    def name_get(self):
        """Personaliza cómo se muestra en selects"""
        result = []
        for imagen in self:
            if imagen.producto_id:
                name = f"Imagen {imagen.secuencia} de {imagen.producto_id.name}"
                if imagen.es_principal:
                    name += " (Principal)"
            else:
                name = f"Imagen #{imagen.id}"
            result.append((imagen.id, name))
        return result