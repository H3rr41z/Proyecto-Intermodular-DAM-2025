# -*- coding: utf-8 -*-
"""Controlador de Etiquetas"""

import logging
from odoo import http
from odoo.http import request
from ..models.utils import response_helpers, serializers

_logger = logging.getLogger(__name__)

class EtiquetasController(http.Controller):
    
    @http.route('/api/v1/etiquetas', type='http', auth='none', methods=['GET'], csrf=False, cors='*')
    def listar_etiquetas(self, **params):
        try:
            etiquetas = request.env['renaix.etiqueta'].sudo().search([], order='producto_count DESC', limit=50)
            etiquetas_data = [serializers.serialize_etiqueta(e) for e in etiquetas]
            
            return response_helpers.success_response(data=etiquetas_data, message='Etiquetas populares recuperadas')
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    @http.route('/api/v1/etiquetas/buscar', type='http', auth='none', methods=['GET'], csrf=False, cors='*')
    def buscar_etiquetas(self, **params):
        try:
            query = params.get('q', '')
            if not query or len(query) < 2:
                return response_helpers.validation_error_response('La búsqueda debe tener al menos 2 caracteres')
            
            etiquetas = request.env['renaix.etiqueta'].sudo().search([('name', 'ilike', query)], limit=20)
            etiquetas_data = [serializers.serialize_etiqueta(e) for e in etiquetas]
            
            return response_helpers.success_response(data=etiquetas_data, message=f'Se encontraron {len(etiquetas)} etiquetas')
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
