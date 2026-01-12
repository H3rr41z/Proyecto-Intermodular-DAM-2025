# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError
from datetime import timedelta


class Compra(models.Model):
    """
    Modelo: Compra / Transacción
    Descripción: Registra las transacciones de compra-venta entre usuarios
    """
    _name = 'renaix.compra'
    _description = 'Compra / Transacción'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha_compra desc, id desc'
    
    # Código único de compra
    codigo = fields.Char(
        string='Código',
        required=True,
        copy=False,
        readonly=True,
        default='/',
        help='Código único de la compra'
    )
    
    # Relación con producto
    producto_id = fields.Many2one(
        'renaix.producto',
        string='Producto',
        required=True,
        ondelete='restrict',
        tracking=True,
        help='Producto comprado'
    )
    
    # Relación con comprador
    comprador_id = fields.Many2one(
        'res.partner',
        string='Comprador',
        required=True,
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        tracking=True,
        help='Usuario que compra el producto'
    )
    
    # Relación con vendedor (se obtiene del propietario del producto)
    vendedor_id = fields.Many2one(
        'res.partner',
        string='Vendedor',
        compute='_compute_vendedor',
        store=True,
        help='Usuario que vende el producto (propietario)'
    )
    
    # Fecha de la compra
    fecha_compra = fields.Datetime(
        string='Fecha de Compra',
        default=fields.Datetime.now,
        required=True,
        tracking=True
    )
    
    # Precio de la transacción (por si cambia del precio original)
    precio_final = fields.Float(
        string='Precio Final',
        required=True,
        help='Precio acordado en la transacción'
    )
    
    # Currency
    currency_id = fields.Many2one(
        'res.currency',
        string='Moneda',
        default=lambda self: self.env.company.currency_id,
        required=True
    )
    
    # Estado de la compra
    estado = fields.Selection([
        ('pendiente', 'Pendiente'),
        ('confirmada', 'Confirmada'),
        ('completada', 'Completada'),
        ('cancelada', 'Cancelada'),
    ], string='Estado',
       default='pendiente',
       required=True,
       tracking=True,
       help='Estado actual de la transacción'
    )
    
    # Notas de la transacción
    notas = fields.Text(
        string='Notas',
        help='Notas adicionales sobre la transacción'
    )
    
    # Relación con valoraciones
    valoracion_comprador_ids = fields.One2many(
        'renaix.valoracion',
        'compra_id',
        string='Valoraciones',
        domain=[('tipo_valoracion', '=', 'comprador_a_vendedor')]
    )
    
    valoracion_vendedor_ids = fields.One2many(
        'renaix.valoracion',
        'compra_id',
        string='Valoraciones del Vendedor',
        domain=[('tipo_valoracion', '=', 'vendedor_a_comprador')]
    )
    
    # Campos relacionados para búsquedas
    producto_nombre = fields.Char(
        related='producto_id.name',
        string='Producto',
        store=True,
        readonly=True
    )
    
    comprador_nombre = fields.Char(
        related='comprador_id.name',
        string='Comprador',
        store=True,
        readonly=True
    )
    
    vendedor_nombre = fields.Char(
        related='vendedor_id.name',
        string='Vendedor',
        store=True,
        readonly=True
    )
    
    # Indicadores de valoración
    comprador_valoro = fields.Boolean(
        string='Comprador Valoró',
        compute='_compute_valoraciones_realizadas',
        help='Indica si el comprador ya valoró al vendedor'
    )
    
    vendedor_valoro = fields.Boolean(
        string='Vendedor Valoró',
        compute='_compute_valoraciones_realizadas',
        help='Indica si el vendedor ya valoró al comprador'
    )
    
    # Constraints SQL
    _sql_constraints = [
        ('precio_positivo', 'CHECK(precio_final >= 0)', 'El precio debe ser mayor o igual a 0.'),
        ('codigo_unique', 'UNIQUE(codigo)', 'El código de compra debe ser único.'),
    ]
    
    @api.depends('producto_id', 'producto_id.propietario_id')
    def _compute_vendedor(self):
        """Obtiene el vendedor del propietario del producto"""
        for compra in self:
            if compra.producto_id and compra.producto_id.propietario_id:
                compra.vendedor_id = compra.producto_id.propietario_id
            else:
                compra.vendedor_id = False
    
    @api.depends('valoracion_comprador_ids', 'valoracion_vendedor_ids')
    def _compute_valoraciones_realizadas(self):
        """Verifica si ambas partes han valorado"""
        for compra in self:
            compra.comprador_valoro = bool(compra.valoracion_comprador_ids)
            compra.vendedor_valoro = bool(compra.valoracion_vendedor_ids)
    
    @api.constrains('comprador_id', 'producto_id')
    def _check_no_autocompra(self):
        """Valida que el comprador no sea el propietario del producto"""
        for compra in self:
            if compra.comprador_id == compra.producto_id.propietario_id:
                raise ValidationError('No puedes comprar tu propio producto.')
    
    @api.constrains('producto_id')
    def _check_producto_disponible(self):
        """Valida que el producto esté disponible"""
        for compra in self:
            if compra.producto_id.estado_venta not in ['disponible', 'reservado']:
                raise ValidationError(
                    f'El producto "{compra.producto_id.name}" no está disponible para compra.'
                )
    
    @api.model
    def create(self, vals):
        """Al crear: generar código único y notificar"""
        # Generar código único
        if vals.get('codigo', '/') == '/':
            vals['codigo'] = self.env['ir.sequence'].next_by_code('renaix.compra') or '/'
        
        # Obtener precio del producto si no se especifica
        if 'precio_final' not in vals and 'producto_id' in vals:
            producto = self.env['renaix.producto'].browse(vals['producto_id'])
            vals['precio_final'] = producto.precio
        
        compra = super(Compra, self).create(vals)
        
        # Añadir comprador y vendedor como seguidores
        compra.message_subscribe(
            partner_ids=[compra.comprador_id.id, compra.vendedor_id.id]
        )
        
        # Marcar producto como reservado
        if compra.producto_id.estado_venta == 'disponible':
            compra.producto_id.estado_venta = 'reservado'
        
        # Notificación al vendedor
        compra.producto_id.message_post(
            body=f"""
                <h3>🎉 ¡Alguien quiere comprar tu producto!</h3>
                <p><b>Comprador:</b> {compra.comprador_id.name}</p>
                <p><b>Email:</b> {compra.comprador_id.email}</p>
                <p><b>Teléfono:</b> {compra.comprador_id.phone or 'No disponible'}</p>
                <p><b>Precio:</b> {compra.precio_final}€</p>
                <p>Ponte en contacto para acordar la entrega.</p>
            """,
            subject=f"Nueva compra: {compra.producto_id.name}",
            partner_ids=[compra.vendedor_id.id]
        )
        
        # Notificación al comprador
        compra.message_post(
            body=f"""
                <h3>✅ Compra registrada con éxito</h3>
                <p><b>Producto:</b> {compra.producto_id.name}</p>
                <p><b>Precio:</b> {compra.precio_final}€</p>
                <p><b>Vendedor:</b> {compra.vendedor_id.name}</p>
                <p>El vendedor se pondrá en contacto contigo pronto.</p>
            """,
            subject=f"Confirmación de compra: {compra.producto_id.name}",
            partner_ids=[compra.comprador_id.id]
        )
        
        return compra
    
    def action_confirmar(self):
        """Confirma la compra"""
        for compra in self:
            if compra.estado == 'pendiente':
                compra.estado = 'confirmada'
                compra.message_post(
                    body='Compra confirmada por el vendedor',
                    subject='Compra Confirmada'
                )
    
    def action_completar(self):
        """Completa la compra y marca el producto como vendido"""
        for compra in self:
            if compra.estado in ['pendiente', 'confirmada']:
                compra.estado = 'completada'
                compra.producto_id.estado_venta = 'vendido'
                compra.producto_id.compra_id = compra.id
                
                # Crear actividad para solicitar valoraciones en 2 días
                compra.activity_schedule(
                    'mail.mail_activity_data_todo',
                    summary='Solicitar valoraciones',
                    note='Recordar a comprador y vendedor que valoren la transacción',
                    date_deadline=fields.Date.today() + timedelta(days=2)
                )
                
                compra.message_post(
                    body='Compra completada. ¡No olvides valorar la transacción!',
                    subject='Compra Completada'
                )
    
    def action_cancelar(self):
        """Cancela la compra y libera el producto"""
        for compra in self:
            if compra.estado != 'completada':
                compra.estado = 'cancelada'
                
                # Liberar producto si estaba reservado por esta compra
                if compra.producto_id.estado_venta == 'reservado':
                    compra.producto_id.estado_venta = 'disponible'
                
                compra.message_post(
                    body='Compra cancelada',
                    subject='Compra Cancelada'
                )
    
    def action_solicitar_valoraciones(self):
        """Envía recordatorio para valorar"""
        for compra in self:
            if compra.estado == 'completada':
                # Notificar al comprador si no ha valorado
                if not compra.comprador_valoro:
                    compra.message_post(
                        body=f'Recordatorio: Valora tu experiencia con el vendedor {compra.vendedor_id.name}',
                        partner_ids=[compra.comprador_id.id]
                    )
                
                # Notificar al vendedor si no ha valorado
                if not compra.vendedor_valoro:
                    compra.message_post(
                        body=f'Recordatorio: Valora tu experiencia con el comprador {compra.comprador_id.name}',
                        partner_ids=[compra.vendedor_id.id]
                    )
    
    def name_get(self):
        """Personaliza cómo se muestra en selects"""
        result = []
        for compra in self:
            name = f"{compra.codigo} - {compra.producto_id.name}"
            result.append((compra.id, name))
        return result