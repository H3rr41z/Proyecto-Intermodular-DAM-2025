# -*- coding: utf-8 -*-

from odoo import models, fields, api


class ResPartner(models.Model):
    """
    Modelo: res.partner extendido
    Descripción: Hereda de res.partner (Contactos) para añadir campos
                 específicos de usuarios de la app móvil Renaix
    """
    _inherit = 'res.partner'
    
    # Campo para identificar usuarios de la app
    es_usuario_app = fields.Boolean(
        string='Es Usuario App',
        default=False,
        help='Indica si este contacto es un usuario de la app móvil Renaix',
        tracking=True
    )
    
    # Fecha de registro en la app
    fecha_registro_app = fields.Datetime(
        string='Fecha Registro App',
        default=fields.Datetime.now,
        help='Fecha y hora de registro en la aplicación móvil'
    )
    
    # Valoración promedio del usuario (como vendedor)
    valoracion_promedio = fields.Float(
        string='Valoración Promedio',
        compute='_compute_valoracion_promedio',
        store=True,  # ✅ Para poder filtrar por valoración
        help='Media de valoraciones recibidas como vendedor (0-5 estrellas)'
    )
    
    # Estadísticas del usuario (campos computados)
    productos_en_venta = fields.Integer(
        string='Productos en Venta',
        compute='_compute_estadisticas_productos',
        store=True,  # ✅ Para poder filtrar
        help='Cantidad de productos disponibles actualmente'
    )
    
    productos_vendidos = fields.Integer(
        string='Productos Vendidos',
        compute='_compute_estadisticas_productos',
        store=True,  # ✅ Para poder filtrar
        help='Total de productos vendidos'
    )
    
    productos_comprados = fields.Integer(
        string='Productos Comprados',
        compute='_compute_estadisticas_productos',
        store=True,  # ✅ Para poder filtrar
        help='Total de productos comprados'
    )
    
    total_comentarios = fields.Integer(
        string='Total Comentarios',
        compute='_compute_estadisticas_actividad',
        store=True,  # ✅ Para poder filtrar
        help='Cantidad de comentarios realizados'
    )
    
    total_denuncias_realizadas = fields.Integer(
        string='Denuncias Realizadas',
        compute='_compute_estadisticas_actividad',
        store=True,  # ✅ Para poder filtrar
        help='Cantidad de denuncias realizadas por el usuario'
    )
    
    # Relaciones con otros modelos
    producto_ids = fields.One2many(
        'renaix.producto',
        'propietario_id',
        string='Productos Publicados'
    )
    
    compra_comprador_ids = fields.One2many(
        'renaix.compra',
        'comprador_id',
        string='Compras Realizadas'
    )
    
    compra_vendedor_ids = fields.One2many(
        'renaix.compra',
        'vendedor_id',
        string='Ventas Realizadas'
    )
    
    # TEMPORAL: Comentadas hasta crear los modelos
    # Se descomentarán cuando existan: renaix.valoracion, renaix.comentario, 
    # renaix.denuncia, renaix.mensaje
    
    valoracion_ids = fields.One2many(
        'renaix.valoracion',
        'usuario_valorado_id',
        string='Valoraciones Recibidas'
    )
    
    comentario_ids = fields.One2many(
        'renaix.comentario',
        'usuario_id',
        string='Comentarios Realizados'
    )
    
    denuncia_ids = fields.One2many(
        'renaix.denuncia',
        'usuario_reportante_id',
        string='Denuncias Realizadas'
    )
    
    mensaje_enviado_ids = fields.One2many(
        'renaix.mensaje',
        'emisor_id',
        string='Mensajes Enviados'
    )
    
    mensaje_recibido_ids = fields.One2many(
        'renaix.mensaje',
        'receptor_id',
        string='Mensajes Recibidos'
    )
    
    # Token para API REST (opcional, para autenticación)
    api_token = fields.Char(
        string='API Token',
        copy=False,
        groups='base.group_system',
        help='Token de autenticación para la API REST'
    )
    
    # Campos para control de cuenta
    cuenta_activa = fields.Boolean(
        string='Cuenta Activa',
        default=True,
        tracking=True,
        help='Si está desactivada, el usuario no puede acceder a la app'
    )
    
    fecha_ultima_actividad = fields.Datetime(
        string='Última Actividad',
        help='Fecha de la última acción del usuario en la app'
    )
    
    @api.depends('valoracion_ids.puntuacion')
    def _compute_valoracion_promedio(self):
        """Calcula la valoración promedio del usuario como vendedor"""
        for partner in self:
            valoraciones = partner.valoracion_ids.filtered(lambda v: v.puntuacion > 0)
            if valoraciones:
                total = sum(valoraciones.mapped('puntuacion'))
                partner.valoracion_promedio = total / len(valoraciones)
            else:
                partner.valoracion_promedio = 0.0
    
    @api.depends('producto_ids', 'producto_ids.estado_venta', 
                 'compra_comprador_ids', 'compra_vendedor_ids')
    def _compute_estadisticas_productos(self):
        """Calcula estadísticas de productos del usuario"""
        for partner in self:
            # Productos en venta (disponibles actualmente)
            partner.productos_en_venta = len(
                partner.producto_ids.filtered(
                    lambda p: p.estado_venta == 'disponible' and p.active
                )
            )
            
            # Productos vendidos (con compra completada)
            partner.productos_vendidos = len(
                partner.compra_vendedor_ids.filtered(
                    lambda c: c.estado == 'completada'
                )
            )
            
            # Productos comprados
            partner.productos_comprados = len(partner.compra_comprador_ids)
    
    @api.depends('comentario_ids', 'denuncia_ids')
    def _compute_estadisticas_actividad(self):
        """Calcula estadísticas de actividad del usuario"""
        for partner in self:
            partner.total_comentarios = len(partner.comentario_ids)
            partner.total_denuncias_realizadas = len(partner.denuncia_ids)
    
    def name_get(self):
        """Personaliza cómo se muestra el nombre en selects"""
        result = []
        for partner in self:
            if partner.es_usuario_app:
                # Para usuarios app, mostrar con icono y valoración
                if partner.valoracion_promedio > 0:
                    name = f"📱 {partner.name} ({partner.valoracion_promedio:.1f}⭐)"
                else:
                    name = f"📱 {partner.name}"
            else:
                name = partner.name
            result.append((partner.id, name))
        return result
    
    def action_view_productos(self):
        """Acción para ver todos los productos del usuario"""
        self.ensure_one()
        return {
            'name': f'Productos de {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.producto',
            'view_mode': 'kanban,tree,form',
            'domain': [('propietario_id', '=', self.id)],
            'context': {'default_propietario_id': self.id},
        }
    
    def action_view_compras(self):
        """Acción para ver todas las compras del usuario"""
        self.ensure_one()
        return {
            'name': f'Compras de {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.compra',
            'view_mode': 'tree,form',
            'domain': [('comprador_id', '=', self.id)],
        }
    
    def action_view_ventas(self):
        """Acción para ver todas las ventas del usuario"""
        self.ensure_one()
        return {
            'name': f'Ventas de {self.name}',
            'type': 'ir.actions.act_window',
            'res_model': 'renaix.compra',
            'view_mode': 'tree,form',
            'domain': [('vendedor_id', '=', self.id)],
        }
    
    def action_desactivar_cuenta(self):
        """Desactiva la cuenta del usuario"""
        for partner in self:
            partner.cuenta_activa = False
            partner.message_post(
                body='Cuenta de usuario desactivada',
                subject='Cuenta Desactivada',
                message_type='notification'
            )
    
    def action_activar_cuenta(self):
        """Activa la cuenta del usuario"""
        for partner in self:
            partner.cuenta_activa = True
            partner.message_post(
                body='Cuenta de usuario activada',
                subject='Cuenta Activada',
                message_type='notification'
            )
