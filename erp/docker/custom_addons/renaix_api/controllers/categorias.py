# -*- coding: utf-8 -*-
"""Controlador de Categorías"""

import logging
from odoo import http
from odoo.http import request
from ..models.utils import response_helpers, serializers

_logger = logging.getLogger(__name__)

class CategoriasController(http.Controller):
    
    @http.route('/api/v1/categorias', type='http', auth='none', methods=['GET'], csrf=False, cors='*')
    def listar_categorias(self, **params):
        try:
            categorias = request.env['renaix.categoria'].sudo().search([], order='name ASC')
            categorias_data = [serializers.serialize_categoria(c) for c in categorias]
            
            return response_helpers.success_response(data=categorias_data, message='Categorías recuperadas')
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
