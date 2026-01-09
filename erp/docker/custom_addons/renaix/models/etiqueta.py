# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class Etiqueta(models.Model):
    """
    Modelo: Etiqueta
    Descripción: Etiquetas/tags para clasificar productos (hasta 5 por producto)
    Ejemplo: #nuevo, #vintage, #gaming, #deportivo, etc.
    Normalizado para evitar duplicados y facilitar búsquedas
    """
    _name = 'renaix.etiqueta'
    _description = 'Etiqueta de Producto'
    _order = 'name'
    
    # Campos básicos
    name = fields.Char(
        string='Etiqueta',
        required=True,
        help='Nombre de la etiqueta (ej: gaming, vintage, nuevo)',
        index=True  # Índice para búsquedas rápidas
    )
    
    # Color para visualización (kanban, chips)
    color = fields.Integer(
        string='Color',
        default=0,
        help='Color para mostrar en la interfaz'
    )
    
    # Control de estado
    active = fields.Boolean(
        string='Activo',
        default=True,
        help='Si está inactivo, no se mostrará como opción'
    )
    
    # Campos computados
    producto_count = fields.Integer(
        string='Nº Productos',
        compute='_compute_producto_count',
        help='Cantidad de productos con esta etiqueta'
    )
    
    # Relación Many2many con productos
    producto_ids = fields.Many2many(
        'renaix.producto',
        'renaix_producto_etiqueta_rel',  # Tabla intermedia
        'etiqueta_id',
        'producto_id',
        string='Productos'
    )
    
    # Constraints SQL
    _sql_constraints = [
        ('name_unique', 'UNIQUE(LOWER(name))', 
         'Ya existe una etiqueta con este nombre (no distingue mayúsculas).')
    ]
    
    @api.depends('producto_ids')
    def _compute_producto_count(self):
        """Calcula cuántos productos tienen esta etiqueta"""
        for etiqueta in self:
            etiqueta.producto_count = len(etiqueta.producto_ids)
    
    @api.model
    def create(self, vals):
        """
        Al crear: normaliza el nombre (lowercase, sin espacios extras)
        """
        if 'name' in vals:
            vals['name'] = self._normalize_name(vals['name'])
        return super(Etiqueta, self).create(vals)
    
    def write(self, vals):
        """
        Al actualizar: normaliza el nombre si se está modificando
        """
        if 'name' in vals:
            vals['name'] = self._normalize_name(vals['name'])
        return super(Etiqueta, self).write(vals)
    
    def _normalize_name(self, name):
        """
        Normaliza el nombre de la etiqueta:
        - Convierte a minúsculas
        - Elimina espacios al inicio y final
        - Reemplaza múltiples espacios por uno solo
        - Elimina caracteres especiales (opcional)
        """
        if not name:
            return name
        
        # Convertir a minúsculas y limpiar espacios
        normalized = name.lower().strip()
        
        # Reemplazar múltiples espacios por uno solo
        normalized = ' '.join(normalized.split())
        
        return normalized
    
    @api.constrains('name')
    def _check_name(self):
        """Validaciones adicionales del nombre"""
        for etiqueta in self:
            if etiqueta.name:
                # No puede estar vacío después de normalizar
                if not etiqueta.name.strip():
                    raise ValidationError('El nombre de la etiqueta no puede estar vacío.')
                
                # Longitud mínima y máxima
                if len(etiqueta.name) < 2:
                    raise ValidationError('El nombre de la etiqueta debe tener al menos 2 caracteres.')
                
                if len(etiqueta.name) > 30:
                    raise ValidationError('El nombre de la etiqueta no puede superar 30 caracteres.')
    
    @api.model
    def name_create(self, name):
        """
        Permite crear etiquetas rápidamente desde campos Many2many
        con el widget 'many2many_tags'
        """
        # Normalizar antes de crear
        normalized_name = self._normalize_name(name)
        
        # Buscar si ya existe (case-insensitive)
        existing = self.search([('name', '=ilike', normalized_name)], limit=1)
        if existing:
            return existing.name_get()[0]
        
        # Crear nueva etiqueta
        etiqueta = self.create({'name': normalized_name})
        return etiqueta.name_get()[0]
    
    def name_get(self):
        """
        Personaliza cómo se muestra en selects.
        Formato: "#etiqueta (X productos)"
        """
        result = []
        for etiqueta in self:
            if etiqueta.producto_count > 0:
                name = f"#{etiqueta.name} ({etiqueta.producto_count})"
            else:
                name = f"#{etiqueta.name}"
            result.append((etiqueta.id, name))
        return result
    
    def action_view_productos(self):
        """
        Acción para ver todos los productos con esta etiqueta.
        Se usa desde un botón en la vista form.
        """
        self.ensure_one()
        return {
            'name': f'Productos con #{self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.producto',
            'view_mode': 'kanban,tree,form',
            'domain': [('etiqueta_ids', 'in', [self.id])],
            'context': {'default_etiqueta_ids': [(4, self.id)]},
        }
    
    @api.model
    def get_etiquetas_mas_usadas(self, limit=10):
        """
        Método para obtener las etiquetas más populares.
        Útil para sugerencias en la app móvil.
        """
        etiquetas = self.search([('active', '=', True)])
        etiquetas_ordenadas = sorted(
            etiquetas, 
            key=lambda e: e.producto_count, 
            reverse=True
        )
        return etiquetas_ordenadas[:limit]