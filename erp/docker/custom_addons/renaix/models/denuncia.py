# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class Denuncia(models.Model):
    """
    Modelo: Denuncia
    Descripción: Sistema de denuncias para moderar contenido inapropiado
                 Puede denunciar: productos, comentarios o usuarios
    """
    _name = 'renaix.denuncia'
    _description = 'Denuncia'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha_denuncia desc, id desc'
    
    # Tipo de denuncia (qué se está denunciando)
    tipo = fields.Selection([
        ('producto', 'Producto'),
        ('comentario', 'Comentario'),
        ('usuario', 'Usuario')
    ], string='Tipo',
       required=True,
       tracking=True,
       help='Tipo de contenido denunciado'
    )
    
    # Referencias polimórficas (solo uno será usado según el tipo)
    producto_id = fields.Many2one(
        'renaix.producto',
        string='Producto Denunciado',
        ondelete='cascade',
        help='Producto denunciado (si tipo=producto)'
    )
    
    comentario_id = fields.Many2one(
        'renaix.comentario',
        string='Comentario Denunciado',
        ondelete='cascade',
        help='Comentario denunciado (si tipo=comentario)'
    )
    
    usuario_reportado_id = fields.Many2one(
        'res.partner',
        string='Usuario Denunciado',
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        help='Usuario denunciado (si tipo=usuario)'
    )
    
    # Usuario que hace la denuncia
    usuario_reportante_id = fields.Many2one(
        'res.partner',
        string='Denunciado por',
        required=True,
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        tracking=True,
        help='Usuario que realiza la denuncia'
    )
    
    # Motivo de la denuncia
    motivo = fields.Text(
        string='Motivo',
        required=True,
        help='Descripción del motivo de la denuncia'
    )
    
    # Categoría de la denuncia
    categoria = fields.Selection([
        ('contenido_inapropiado', 'Contenido Inapropiado'),
        ('spam', 'Spam'),
        ('fraude', 'Fraude / Estafa'),
        ('violencia', 'Violencia'),
        ('informacion_falsa', 'Información Falsa'),
        ('otro', 'Otro')
    ], string='Categoría',
       required=True,
       tracking=True,
       help='Categoría de la denuncia'
    )
    
    # Estado de la denuncia
    estado = fields.Selection([
        ('pendiente', 'Pendiente'),
        ('en_revision', 'En Revisión'),
        ('resuelta', 'Resuelta'),
        ('rechazada', 'Rechazada')
    ], string='Estado',
       default='pendiente',
       required=True,
       tracking=True,
       help='Estado actual de la denuncia'
    )
    
    # Empleado asignado para revisar
    empleado_asignado_id = fields.Many2one(
        'res.users',
        string='Asignado a',
        ondelete='set null',
        tracking=True,
        help='Empleado responsable de revisar la denuncia'
    )
    
    # Fechas
    fecha_denuncia = fields.Datetime(
        string='Fecha de Denuncia',
        default=fields.Datetime.now,
        required=True,
        readonly=True
    )
    
    fecha_resolucion = fields.Datetime(
        string='Fecha de Resolución',
        readonly=True,
        help='Fecha en que se resolvió o rechazó la denuncia'
    )
    
    # Resolución
    resolucion = fields.Text(
        string='Resolución',
        help='Descripción de la acción tomada'
    )
    
    # Campos computados para mostrar info del denunciado
    denunciado_nombre = fields.Char(
        string='Denunciado',
        compute='_compute_denunciado_nombre',
        store=True,
        help='Nombre de lo que fue denunciado'
    )
    
    @api.depends('tipo', 'producto_id', 'comentario_id', 'usuario_reportado_id')
    def _compute_denunciado_nombre(self):
        """Obtiene el nombre de lo que fue denunciado"""
        for denuncia in self:
            if denuncia.tipo == 'producto' and denuncia.producto_id:
                denuncia.denunciado_nombre = denuncia.producto_id.name
            elif denuncia.tipo == 'comentario' and denuncia.comentario_id:
                denuncia.denunciado_nombre = f"Comentario de {denuncia.comentario_id.usuario_id.name}"
            elif denuncia.tipo == 'usuario' and denuncia.usuario_reportado_id:
                denuncia.denunciado_nombre = denuncia.usuario_reportado_id.name
            else:
                denuncia.denunciado_nombre = 'Desconocido'
    
    @api.constrains('tipo', 'producto_id', 'comentario_id', 'usuario_reportado_id')
    def _check_referencias_coherentes(self):
        """Valida que las referencias sean coherentes con el tipo"""
        for denuncia in self:
            if denuncia.tipo == 'producto':
                if not denuncia.producto_id:
                    raise ValidationError('Debes especificar el producto denunciado.')
                if denuncia.comentario_id or denuncia.usuario_reportado_id:
                    raise ValidationError('Solo puedes denunciar un producto.')
            
            elif denuncia.tipo == 'comentario':
                if not denuncia.comentario_id:
                    raise ValidationError('Debes especificar el comentario denunciado.')
                if denuncia.producto_id or denuncia.usuario_reportado_id:
                    raise ValidationError('Solo puedes denunciar un comentario.')
            
            elif denuncia.tipo == 'usuario':
                if not denuncia.usuario_reportado_id:
                    raise ValidationError('Debes especificar el usuario denunciado.')
                if denuncia.producto_id or denuncia.comentario_id:
                    raise ValidationError('Solo puedes denunciar un usuario.')
    
    @api.constrains('motivo')
    def _check_motivo(self):
        """Validaciones del motivo"""
        for denuncia in self:
            if not denuncia.motivo or not denuncia.motivo.strip():
                raise ValidationError('Debes especificar el motivo de la denuncia.')
            
            if len(denuncia.motivo) < 10:
                raise ValidationError('El motivo debe tener al menos 10 caracteres.')
    
    @api.model
    def create(self, vals):
        """Al crear: notificar a moderadores"""
        denuncia = super(Denuncia, self).create(vals)
        
        # Notificar a moderadores
        grupo_moderadores = self.env.ref('renaix.group_renaix_moderador', raise_if_not_found=False)
        
        if grupo_moderadores:
            moderadores = grupo_moderadores.users
            
            if moderadores:
                denuncia.message_post(
                    body=f"""
                        <h3>🚨 Nueva denuncia recibida</h3>
                        <p><b>Tipo:</b> {dict(denuncia._fields['tipo'].selection)[denuncia.tipo]}</p>
                        <p><b>Categoría:</b> {dict(denuncia._fields['categoria'].selection)[denuncia.categoria]}</p>
                        <p><b>Denunciado:</b> {denuncia.denunciado_nombre}</p>
                        <p><b>Reportado por:</b> {denuncia.usuario_reportante_id.name}</p>
                        <p><b>Motivo:</b> {denuncia.motivo}</p>
                    """,
                    subject='Nueva Denuncia para Revisar',
                    partner_ids=moderadores.mapped('partner_id').ids
                )
                
                # Crear actividad para que alguien la revise
                denuncia.activity_schedule(
                    'mail.mail_activity_data_todo',
                    summary='Revisar denuncia',
                    user_id=moderadores[0].id
                )
        
        return denuncia
    
    def action_asignar_a_mi(self):
        """Asigna la denuncia al usuario actual"""
        for denuncia in self:
            denuncia.empleado_asignado_id = self.env.user
            denuncia.estado = 'en_revision'
    
    def action_resolver(self):
        """Marca la denuncia como resuelta"""
        for denuncia in self:
            if denuncia.estado in ['pendiente', 'en_revision']:
                denuncia.write({
                    'estado': 'resuelta',
                    'fecha_resolucion': fields.Datetime.now()
                })
                
                denuncia.message_post(
                    body='Denuncia resuelta',
                    subject='Denuncia Resuelta',
                    partner_ids=[denuncia.usuario_reportante_id.id]
                )
    
    def action_rechazar(self):
        """Rechaza la denuncia"""
        for denuncia in self:
            if denuncia.estado in ['pendiente', 'en_revision']:
                denuncia.write({
                    'estado': 'rechazada',
                    'fecha_resolucion': fields.Datetime.now()
                })
                
                denuncia.message_post(
                    body='Denuncia rechazada por no proceder',
                    subject='Denuncia Rechazada',
                    partner_ids=[denuncia.usuario_reportante_id.id]
                )
    
    def name_get(self):
        """Personaliza cómo se muestra en selects"""
        result = []
        for denuncia in self:
            tipo_label = dict(denuncia._fields['tipo'].selection)[denuncia.tipo]
            name = f"[{tipo_label}] {denuncia.denunciado_nombre} - {denuncia.categoria}"
            result.append((denuncia.id, name))
        return result
    
    def action_view_producto(self):
        """Abre el producto denunciado"""
        self.ensure_one()
        if self.tipo == 'producto' and self.producto_id:
            return {
                'name': f'Producto Denunciado: {self.producto_id.name}',
                'type': 'ir.actions.act_window',
                'res_model': 'renaix.producto',
                'view_mode': 'form',
                'res_id': self.producto_id.id,
                'target': 'current',
            }
        return False
