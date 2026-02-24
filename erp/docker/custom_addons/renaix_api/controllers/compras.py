# -*- coding: utf-8 -*-
"""
Controlador de Compras
Endpoints: crear compra, detalle, confirmar, completar, cancelar
"""

import json
import logging
from odoo import http
from odoo.http import request
from ..models.utils import jwt_utils, response_helpers, serializers

_logger = logging.getLogger(__name__)


class ComprasController(http.Controller):
    
    @http.route('/api/v1/compras', type='http', auth='public', 
                methods=['POST'], csrf=False, cors='*')
    def crear_compra(self, **params):
        """
        Comprar un producto.
        
        Body JSON:
        {
            "producto_id": 123,
            "notas": "Comentarios opcionales"
        }
        
        Returns:
            JSON: {compra}
        """
        try:
            partner = jwt_utils.verify_token(request)
            data = json.loads(request.httprequest.data.decode('utf-8'))

            if not data.get('producto_id'):
                return response_helpers.validation_error_response('producto_id requerido')

            producto = request.env['renaix.producto'].sudo().browse(data['producto_id'])

            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')

            if producto.estado_venta != 'disponible':
                return response_helpers.validation_error_response('Producto no disponible')

            if producto.propietario_id.id == partner.id:
                return response_helpers.validation_error_response('No puedes comprar tu propio producto')

            compra_vals = {
                'producto_id': producto.id,
                'comprador_id': partner.id,
                'precio_final': producto.precio,
                'notas': data.get('notas', ''),
            }

            # Usamos savepoint para que un fallo del ORM no deje la transacción
            # en estado abortado, lo que causaría una respuesta HTML en vez de JSON
            with request.env.cr.savepoint():
                compra = request.env['renaix.compra'].sudo().create(compra_vals)

            _logger.info(f'Compra creada: {compra.id}')

            return response_helpers.success_response(
                data=serializers.serialize_compra(compra),
                message='Compra creada exitosamente',
                status=201
            )

        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        except Exception as e:
            _logger.error(f'Error al crear compra: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/compras/<int:compra_id>', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def detalle_compra(self, compra_id, **params):
        """Obtener detalle de una compra."""
        try:
            partner = jwt_utils.verify_token(request)
            compra = request.env['renaix.compra'].sudo().browse(compra_id)
            
            if not compra.exists():
                return response_helpers.not_found_response('Compra no encontrada')
            
            if compra.comprador_id.id != partner.id and compra.vendedor_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')
            
            return response_helpers.success_response(
                data=serializers.serialize_compra(compra, include_valoraciones=True),
                message='Compra encontrada'
            )
            
        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/compras/<int:compra_id>/confirmar', type='http', auth='public',
                methods=['POST'], csrf=False, cors='*')
    def confirmar_compra(self, compra_id, **params):
        """Confirmar compra (vendedor)."""
        try:
            partner = jwt_utils.verify_token(request)
            compra = request.env['renaix.compra'].sudo().browse(compra_id)

            if not compra.exists():
                return response_helpers.not_found_response('Compra no encontrada')

            if compra.vendedor_id.id != partner.id:
                return response_helpers.forbidden_response('Solo el vendedor puede confirmar')

            if compra.estado != 'pendiente':
                return response_helpers.validation_error_response('La compra no está en estado pendiente')

            compra.sudo().action_confirmar()

            return response_helpers.success_response(
                data=serializers.serialize_compra(compra),
                message='Compra confirmada'
            )

        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/compras/<int:compra_id>/completar', type='http', auth='public',
                methods=['POST'], csrf=False, cors='*')
    def completar_compra(self, compra_id, **params):
        """Completar compra (comprador confirma recepción)."""
        try:
            partner = jwt_utils.verify_token(request)
            compra = request.env['renaix.compra'].sudo().browse(compra_id)

            if not compra.exists():
                return response_helpers.not_found_response('Compra no encontrada')

            if compra.comprador_id.id != partner.id:
                return response_helpers.forbidden_response('Solo el comprador puede completar')

            if compra.estado != 'confirmada':
                return response_helpers.validation_error_response('La compra debe estar confirmada primero')

            compra.sudo().action_completar()

            return response_helpers.success_response(
                data=serializers.serialize_compra(compra),
                message='Compra completada'
            )

        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/compras/<int:compra_id>/cancelar', type='http', auth='public',
                methods=['POST'], csrf=False, cors='*')
    def cancelar_compra(self, compra_id, **params):
        """Cancelar compra."""
        try:
            partner = jwt_utils.verify_token(request)
            compra = request.env['renaix.compra'].sudo().browse(compra_id)

            if not compra.exists():
                return response_helpers.not_found_response('Compra no encontrada')

            if compra.comprador_id.id != partner.id and compra.vendedor_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')

            if compra.estado == 'completada':
                return response_helpers.validation_error_response('No se puede cancelar una compra completada')

            compra.sudo().action_cancelar()

            return response_helpers.success_response(
                data=serializers.serialize_compra(compra),
                message='Compra cancelada'
            )

        except Exception as e:
            _logger.error(f'Error: {str(e)}')
            return response_helpers.server_error_response(str(e))
