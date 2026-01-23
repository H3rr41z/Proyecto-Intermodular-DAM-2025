# -*- coding: utf-8 -*-
"""
Controlador de Comentarios
"""

import json
import logging
from odoo import http
from odoo.http import request
from ..models.utils import jwt_utils, validators, response_helpers, serializers

_logger = logging.getLogger(__name__)


class ComentariosController(http.Controller):
    
    @http.route('/api/v1/productos/<int:producto_id>/comentarios', type='http', auth='none', 
                methods=['GET'], csrf=False, cors='*')
    def listar_comentarios(self, producto_id, **params):
        """Listar comentarios de un producto."""
        try:
            comentarios = request.env['renaix.comentario'].sudo().search([
                ('producto_id', '=', producto_id),
                ('active', '=', True)
            ], order='fecha DESC')
            
            comentarios_data = [serializers.serialize_comentario(c) for c in comentarios]
            
            return response_helpers.success_response(
                data=comentarios_data,
                message='Comentarios recuperados'
            )
            
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>/comentarios', type='http', auth='public', 
                methods=['POST'], csrf=False, cors='*')
    def crear_comentario(self, producto_id, **params):
        """Crear comentario en un producto."""
        try:
            partner = jwt_utils.verify_token(request)
            data = json.loads(request.httprequest.data.decode('utf-8'))
            
            is_valid, error_msg = validators.validate_comentario_data(data)
            if not is_valid:
                return response_helpers.validation_error_response(error_msg)
            
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            comentario_vals = {
                'producto_id': producto.id,
                'usuario_id': partner.id,
                'texto': data['texto'],
            }
            
            comentario = request.env['renaix.comentario'].sudo().create(comentario_vals)
            
            return response_helpers.success_response(
                data=serializers.serialize_comentario(comentario),
                message='Comentario creado',
                status=201
            )
            
        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/comentarios/<int:comentario_id>', type='http', auth='public', 
                methods=['DELETE'], csrf=False, cors='*')
    def eliminar_comentario(self, comentario_id, **params):
        """Eliminar propio comentario."""
        try:
            partner = jwt_utils.verify_token(request)
            comentario = request.env['renaix.comentario'].sudo().browse(comentario_id)
            
            if not comentario.exists():
                return response_helpers.not_found_response('Comentario no encontrado')
            
            if comentario.usuario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')
            
            comentario.sudo().write({'active': False})
            
            return response_helpers.success_response(message='Comentario eliminado')
            
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
