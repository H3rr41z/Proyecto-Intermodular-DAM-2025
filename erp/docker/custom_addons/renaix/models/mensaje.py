# -*- coding: utf-8 -*-

from odoo import models, fields, api
from odoo.exceptions import ValidationError


class Mensaje(models.Model):
    """
    Modelo: Mensaje
    Descripción: Sistema de mensajería privada entre usuarios
                 Los mensajes quedan vinculados al producto sobre el que se habla
    """
    _name = 'renaix.mensaje'
    _description = 'Mensaje entre Usuarios'
    _order = 'fecha desc, id desc'
    
    # Usuario emisor
    emisor_id = fields.Many2one(
        'res.partner',
        string='De',
        required=True,
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        help='Usuario que envía el mensaje'
    )
    
    # Usuario receptor
    receptor_id = fields.Many2one(
        'res.partner',
        string='Para',
        required=True,
        ondelete='restrict',
        domain=[('es_usuario_app', '=', True)],
        help='Usuario que recibe el mensaje'
    )
    
    # Producto sobre el que se habla (opcional pero recomendado)
    producto_id = fields.Many2one(
        'renaix.producto',
        string='Producto',
        ondelete='set null',
        help='Producto sobre el que trata la conversación'
    )
    
    # Hilo de conversación (opcional, para agrupar mensajes)
    hilo_id = fields.Char(
        string='ID Hilo',
        help='Identificador para agrupar mensajes en una conversación',
        index=True
    )
    
    # Contenido del mensaje
    texto = fields.Text(
        string='Mensaje',
        required=True,
        help='Contenido del mensaje'
    )
    
    # Fecha y hora del mensaje
    fecha = fields.Datetime(
        string='Fecha',
        default=fields.Datetime.now,
        required=True,
        readonly=True
    )
    
    # Estado del mensaje
    leido = fields.Boolean(
        string='Leído',
        default=False,
        help='Indica si el receptor ha leído el mensaje'
    )

    fecha_lectura = fields.Datetime(
        string='Fecha de Lectura',
        readonly=True,
        help='Fecha y hora en que se leyó el mensaje'
    )

    # Tipo de mensaje (para sistema de ofertas/negociación)
    tipo_mensaje = fields.Selection([
        ('text', 'Texto'),
        ('offer', 'Oferta'),
        ('offer_accepted', 'Oferta Aceptada'),
        ('offer_rejected', 'Oferta Rechazada'),
        ('counter_offer', 'Contraoferta')
    ], string='Tipo de Mensaje', default='text', required=True,
       help='Tipo de mensaje: texto normal u oferta de negociación')

    # Campos para ofertas de negociación
    precio_ofertado = fields.Float(
        string='Precio Ofertado',
        digits=(10, 2),
        help='Precio propuesto en la oferta'
    )

    precio_original = fields.Float(
        string='Precio Original',
        digits=(10, 2),
        help='Precio original del producto al momento de la oferta'
    )

    # Referencia a la oferta relacionada (para aceptar/rechazar)
    oferta_relacionada_id = fields.Many2one(
        'renaix.mensaje',
        string='Oferta Relacionada',
        ondelete='set null',
        help='Mensaje de oferta original relacionado'
    )
    
    # Campos relacionados (para facilitar búsquedas)
    emisor_nombre = fields.Char(
        related='emisor_id.name',
        string='Emisor',
        store=True,
        readonly=True
    )
    
    receptor_nombre = fields.Char(
        related='receptor_id.name',
        string='Receptor',
        store=True,
        readonly=True
    )
    
    producto_nombre = fields.Char(
        related='producto_id.name',
        string='Producto',
        store=True,
        readonly=True
    )
    
    @api.constrains('texto')
    def _check_texto(self):
        """Validaciones del texto del mensaje"""
        for mensaje in self:
            if not mensaje.texto or not mensaje.texto.strip():
                raise ValidationError('El mensaje no puede estar vacío.')
            
            if len(mensaje.texto) < 1:
                raise ValidationError('El mensaje debe tener al menos 1 carácter.')
            
            if len(mensaje.texto) > 2000:
                raise ValidationError('El mensaje no puede superar 2000 caracteres.')
    
    @api.constrains('emisor_id', 'receptor_id')
    def _check_no_auto_mensaje(self):
        """Valida que no se envíen mensajes a uno mismo"""
        for mensaje in self:
            if mensaje.emisor_id == mensaje.receptor_id:
                raise ValidationError('No puedes enviarte mensajes a ti mismo.')
    
    @api.model
    def create(self, vals):
        """Al crear: generar hilo_id si no existe y notificar"""
        # Generar hilo_id automático si no se proporciona
        if 'hilo_id' not in vals or not vals['hilo_id']:
            emisor_id = vals.get('emisor_id')
            receptor_id = vals.get('receptor_id')
            producto_id = vals.get('producto_id') or 0

            if not producto_id:
                # Sin producto: reutilizar el hilo existente entre estos dos usuarios
                existing = self.search([
                    '|',
                    '&', ('emisor_id', '=', emisor_id), ('receptor_id', '=', receptor_id),
                    '&', ('emisor_id', '=', receptor_id), ('receptor_id', '=', emisor_id),
                ], limit=1, order='fecha desc')
                if existing and existing.hilo_id:
                    vals['hilo_id'] = existing.hilo_id

            # Si todavía no hay hilo_id, generar uno nuevo
            if 'hilo_id' not in vals or not vals['hilo_id']:
                # Ordenar IDs para que el hilo sea el mismo independientemente del emisor
                ids_ordenados = sorted([emisor_id, receptor_id])
                vals['hilo_id'] = f"hilo_{ids_ordenados[0]}_{ids_ordenados[1]}_{producto_id}"
        
        mensaje = super(Mensaje, self).create(vals)
        
        # Notificar al receptor
        # Nota: En producción esto se haría vía email, push notification, etc.
        # Aquí solo registramos en el sistema
        
        return mensaje
    
    def action_marcar_leido(self):
        """Marca el mensaje como leído"""
        for mensaje in self:
            if not mensaje.leido:
                mensaje.write({
                    'leido': True,
                    'fecha_lectura': fields.Datetime.now()
                })
    
    def action_marcar_no_leido(self):
        """Marca el mensaje como no leído"""
        for mensaje in self:
            mensaje.write({
                'leido': False,
                'fecha_lectura': False
            })
    
    @api.model
    def get_conversacion(self, user_id, other_user_id, producto_id=None):
        """
        Obtiene todos los mensajes de una conversación entre dos usuarios.
        Útil para la API REST.
        """
        domain = [
            '|',
            '&', ('emisor_id', '=', user_id), ('receptor_id', '=', other_user_id),
            '&', ('emisor_id', '=', other_user_id), ('receptor_id', '=', user_id)
        ]
        
        if producto_id:
            domain.append(('producto_id', '=', producto_id))
        
        mensajes = self.search(domain, order='fecha asc')
        return mensajes
    
    @api.model
    def get_mensajes_no_leidos(self, user_id):
        """
        Obtiene todos los mensajes no leídos de un usuario.
        Útil para la API REST.
        """
        return self.search([
            ('receptor_id', '=', user_id),
            ('leido', '=', False)
        ], order='fecha desc')
    
    def name_get(self):
        """Personaliza cómo se muestra en selects"""
        result = []
        for mensaje in self:
            # Truncar texto si es muy largo
            texto_preview = mensaje.texto[:30] + '...' if len(mensaje.texto) > 30 else mensaje.texto
            name = f"{mensaje.emisor_id.name} → {mensaje.receptor_id.name}: {texto_preview}"
            if not mensaje.leido:
                name = f"🔴 {name}"
            result.append((mensaje.id, name))
        return result
