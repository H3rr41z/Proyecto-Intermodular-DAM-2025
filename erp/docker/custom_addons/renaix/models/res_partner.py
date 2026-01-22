# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError
from werkzeug.security import generate_password_hash, check_password_hash


class ResPartner(models.Model):
    """
    Modelo: res.partner extendido
    Descripción: Hereda de res.partner (Contactos) para añadir campos
                 específicos de usuarios de la app móvil Renaix
    """
    _inherit = 'res.partner'
    
    # ========================================
    # CAMPO GID (Global ID)
    # ========================================
    partner_gid = fields.Char(
        string='Global ID',
        help='Identificador único global (UUID) para sincronización con app móvil',
        readonly=True,
        copy=False,
        index=True  # ✅ Indexado para búsquedas rápidas
    )
    
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
        help='Token de autenticación para la API REST (refresh token)'
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
    
    # Campo de información adicional
    additional_info = fields.Text(
        string='Información Adicional',
        help='Notas o información adicional sobre el usuario de la app'
    )
    
    # ========================================
    # ⭐ NUEVOS CAMPOS PARA AUTENTICACIÓN API
    # ========================================
    
    password_hash = fields.Char(
        string='Password Hash',
        copy=False,
        groups='base.group_system',  # Solo administradores pueden verlo
        help='Hash de la contraseña para autenticación en la app móvil'
    )
    
    # ========================================
    # CONSTRAINTS SQL
    # ========================================
    
    _sql_constraints = [
        ('email_unique_app_user', 
         'UNIQUE(email) WHERE es_usuario_app = true',
         'Ya existe un usuario de la app con este email.')
    ]
    
    # ========================================
    # MÉTODOS COMPUTE
    # ========================================
    
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
    
    # ========================================
    # MÉTODOS CRUD
    # ========================================
    
    @api.model_create_multi
    def create(self, vals_list):
        """
        Override create para generar partner_gid automáticamente
        Solo para usuarios de la app móvil
        """
        import uuid
        
        for vals in vals_list:
            # Si es usuario app y no tiene GID, generarlo
            if vals.get('es_usuario_app') and not vals.get('partner_gid'):
                vals['partner_gid'] = str(uuid.uuid4())
        
        return super().create(vals_list)
    
    # ========================================
    # ⭐ NUEVOS MÉTODOS PARA GESTIÓN DE PASSWORD
    # ========================================
    
    def set_password(self, password):
        """
        Establece la contraseña del usuario (hasheada con werkzeug).
        Solo para usuarios de la app móvil.
        
        Args:
            password (str): Contraseña en texto plano
            
        Raises:
            ValidationError: Si el usuario no es de la app
        """
        self.ensure_one()
        
        if not self.es_usuario_app:
            raise ValidationError(
                'Solo los usuarios de la app móvil pueden tener contraseña.'
            )
        
        # Validar longitud mínima
        if not password or len(password) < 6:
            raise ValidationError(
                'La contraseña debe tener al menos 6 caracteres.'
            )
        
        # Hashear y guardar
        self.password_hash = generate_password_hash(password)
    
    def check_password(self, password):
        """
        Verifica si la contraseña proporcionada es correcta.
        
        Args:
            password (str): Contraseña en texto plano
            
        Returns:
            bool: True si la contraseña es correcta, False en caso contrario
        """
        self.ensure_one()
        
        # Si no hay hash guardado, la contraseña es incorrecta
        if not self.password_hash:
            return False
        
        # Verificar usando werkzeug
        return check_password_hash(self.password_hash, password)
    
    @api.model
    def authenticate_app_user(self, email, password):
        """
        Autentica un usuario de la app usando email y contraseña.
        
        Args:
            email (str): Email del usuario
            password (str): Contraseña en texto plano
            
        Returns:
            res.partner: Usuario autenticado si las credenciales son correctas
            False: Si las credenciales son incorrectas
            
        Raises:
            ValidationError: Si la cuenta está desactivada
        """
        # Buscar usuario por email (solo usuarios app)
        partner = self.search([
            ('email', '=', email),
            ('es_usuario_app', '=', True)
        ], limit=1)
        
        # Si no existe el usuario
        if not partner:
            return False
        
        # Verificar si la cuenta está activa
        if not partner.cuenta_activa:
            raise ValidationError(
                'Tu cuenta ha sido desactivada. Contacta con soporte.'
            )
        
        # Verificar contraseña
        if not partner.check_password(password):
            return False
        
        # Actualizar última actividad
        partner.fecha_ultima_actividad = fields.Datetime.now()
        
        return partner
    
    # ========================================
    # MÉTODOS DE VISUALIZACIÓN Y ACCIONES
    # ========================================
    
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
    
    def action_regenerar_gid(self):
        """Regenera el GID del usuario (solo para casos especiales)"""
        import uuid
        for partner in self:
            if partner.es_usuario_app:
                old_gid = partner.partner_gid
                partner.partner_gid = str(uuid.uuid4())
                partner.message_post(
                    body=f'GID regenerado: {old_gid} → {partner.partner_gid}',
                    subject='GID Regenerado',
                    message_type='notification'
                )