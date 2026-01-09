# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class Categoria(models.Model):
    """
    Modelo: Categoría
    Descripción: Categorías predefinidas para clasificar productos
    Ejemplo: Electrónica, Ropa, Muebles, Deportes, etc.
    """
    _name = 'renaix.categoria'
    _description = 'Categoría de Producto'
    _inherit = ['mail.thread', 'mail.activity.mixin']  # Chatter
    _order = 'sequence, name'
    
    # Campos básicos
    name = fields.Char(
        string='Nombre',
        required=True,
        tracking=True,
        help='Nombre de la categoría (ej: Electrónica, Ropa, Muebles)'
    )
    
    descripcion = fields.Text(
        string='Descripción',
        tracking=True,
        help='Descripción detallada de la categoría'
    )
    
    # Imagen de la categoría
    image = fields.Image(
        string='Imagen',
        max_width=512,
        max_height=512,
        help='Imagen representativa de la categoría'
    )
    
    # Para ordenar las categorías
    sequence = fields.Integer(
        string='Secuencia',
        default=10,
        help='Orden de aparición en listados'
    )
    
    # Control de estado
    active = fields.Boolean(
        string='Activo',
        default=True,
        tracking=True,
        help='Si está inactivo, no se mostrará en la app'
    )
    
    # Campos computados
    producto_count = fields.Integer(
        string='Nº Productos',
        compute='_compute_producto_count',
        help='Cantidad de productos en esta categoría'
    )
    
    # Relaciones
    producto_ids = fields.One2many(
        'renaix.producto',
        'categoria_id',
        string='Productos'
    )
    
    # Color para la interfaz (opcional)
    color = fields.Integer(
        string='Color',
        help='Color para representar la categoría en vistas kanban'
    )
    
    # Constraints SQL
    _sql_constraints = [
        ('name_unique', 'UNIQUE(name)', 'Ya existe una categoría con este nombre.')
    ]
    
    @api.depends('producto_ids')
    def _compute_producto_count(self):
        """Calcula cuántos productos hay en cada categoría"""
        for categoria in self:
            categoria.producto_count = len(categoria.producto_ids)
    
    @api.constrains('name')
    def _check_name(self):
        """Validación: el nombre no puede estar vacío después de quitar espacios"""
        for categoria in self:
            if categoria.name and not categoria.name.strip():
                raise ValidationError('El nombre de la categoría no puede estar vacío.')
    
    def name_get(self):
        """
        Personaliza cómo se muestra el nombre en selects y referencias.
        Muestra: "Nombre (X productos)"
        """
        result = []
        for categoria in self:
            if categoria.producto_count > 0:
                name = f"{categoria.name} ({categoria.producto_count} productos)"
            else:
                name = categoria.name
            result.append((categoria.id, name))
        return result
    
    def action_view_productos(self):
        """
        Acción para ver todos los productos de esta categoría.
        Se usa desde un botón en la vista form.
        """
        self.ensure_one()
        return {
            'name': f'Productos en {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.producto',
            'view_mode': 'kanban,tree,form',
            'domain': [('categoria_id', '=', self.id)],
            'context': {'default_categoria_id': self.id},
        }