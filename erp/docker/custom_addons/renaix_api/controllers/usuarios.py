# -*- coding: utf-8 -*-
"""
Controlador de Usuarios
Endpoints: perfil, actualizar perfil, productos del usuario, compras, ventas, valoraciones, estadísticas
"""

import json
import logging
from odoo import http
from odoo.http import request
from ..models.utils import jwt_utils, auth_helpers, validators, response_helpers, serializers

_logger = logging.getLogger(__name__)


class UsuariosController(http.Controller):
    
    @http.route('/api/v1/usuarios/perfil', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_perfil(self, **params):
        """
        Obtener perfil del usuario autenticado.
        
        Headers:
            Authorization: Bearer <access_token>
        
        Returns:
            JSON: {user}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            return response_helpers.success_response(
                data=serializers.serialize_partner(partner, full=True),
                message='Perfil recuperado'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener perfil: {str(e)}')
            return response_helpers.unauthorized_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil', type='http', auth='public', 
                methods=['PUT'], csrf=False, cors='*')
    def update_perfil(self, **params):
        """
        Actualizar perfil del usuario autenticado.
        
        Body JSON:
        {
            "name": "Nuevo Nombre",  # opcional
            "phone": "612345678",    # opcional
            "mobile": "612345679"    # opcional
        }
        
        Returns:
            JSON: {user}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Obtener datos
            data = json.loads(request.httprequest.data.decode('utf-8'))
            
            # Campos permitidos para actualizar
            allowed_fields = ['name', 'phone', 'mobile']
            update_vals = {}
            
            for field in allowed_fields:
                if field in data:
                    update_vals[field] = data[field]
            
            # Validar teléfonos si se proporcionan
            if update_vals.get('phone') and not auth_helpers.validate_phone_number(update_vals['phone']):
                return response_helpers.validation_error_response('Formato de teléfono inválido')

            if update_vals.get('mobile') and not auth_helpers.validate_phone_number(update_vals['mobile']):
                return response_helpers.validation_error_response('Formato de móvil inválido')
            
            # Actualizar
            if update_vals:
                partner.sudo().write(update_vals)
            
            return response_helpers.success_response(
                data=serializers.serialize_partner(partner, full=True),
                message='Perfil actualizado'
            )
            
        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        
        except Exception as e:
            _logger.error(f'Error al actualizar perfil: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/<int:user_id>', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_usuario_publico(self, user_id, **params):
        """
        Obtener perfil público de un usuario.
        
        Returns:
            JSON: {user}
        """
        try:
            # Buscar usuario
            partner = request.env['res.partner'].sudo().browse(user_id)
            
            if not partner.exists() or not partner.es_usuario_app:
                return response_helpers.not_found_response('Usuario no encontrado')
            
            # Devolver solo información pública
            return response_helpers.success_response(
                data=serializers.serialize_partner(partner, full=True),
                message='Usuario encontrado'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener usuario: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil/productos', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_mis_productos(self, **params):
        """
        Obtener productos del usuario autenticado.
        
        Query params:
            page: Número de página (default: 1)
            limit: Elementos por página (default: 20)
        
        Returns:
            JSON: {productos} (paginado)
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Parámetros de paginación
            page, limit = validators.validate_pagination_params(
                params.get('page'),
                params.get('limit')
            )
            
            # Buscar productos
            productos = request.env['renaix.producto'].sudo().search([
                ('propietario_id', '=', partner.id)
            ], order='fecha_publicacion DESC')
            
            total = len(productos)
            offset = (page - 1) * limit
            productos_pagina = productos[offset:offset + limit]
            
            # Serializar
            productos_data = [serializers.serialize_producto(p, include_images=True) for p in productos_pagina]
            
            return response_helpers.paginated_response(
                items=productos_data,
                total=total,
                page=page,
                limit=limit,
                message='Productos recuperados'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener productos: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil/compras', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_mis_compras(self, **params):
        """
        Obtener compras del usuario autenticado.
        
        Returns:
            JSON: {compras}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar compras
            compras = request.env['renaix.compra'].sudo().search([
                ('comprador_id', '=', partner.id)
            ], order='fecha_compra DESC')
            
            # Serializar
            compras_data = [serializers.serialize_compra(c) for c in compras]
            
            return response_helpers.success_response(
                data=compras_data,
                message='Compras recuperadas'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener compras: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil/ventas', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_mis_ventas(self, **params):
        """
        Obtener ventas del usuario autenticado.
        
        Returns:
            JSON: {ventas}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar ventas
            ventas = request.env['renaix.compra'].sudo().search([
                ('vendedor_id', '=', partner.id)
            ], order='fecha_compra DESC')
            
            # Serializar
            ventas_data = [serializers.serialize_compra(v) for v in ventas]
            
            return response_helpers.success_response(
                data=ventas_data,
                message='Ventas recuperadas'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener ventas: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil/valoraciones', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_mis_valoraciones(self, **params):
        """
        Obtener valoraciones recibidas del usuario autenticado.
        
        Returns:
            JSON: {valoraciones}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar valoraciones recibidas
            valoraciones = request.env['renaix.valoracion'].sudo().search([
                ('usuario_valorado_id', '=', partner.id)
            ], order='fecha DESC')
            
            # Serializar
            valoraciones_data = [serializers.serialize_valoracion(v) for v in valoraciones]
            
            return response_helpers.success_response(
                data=valoraciones_data,
                message='Valoraciones recuperadas'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener valoraciones: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/usuarios/perfil/estadisticas', type='http', auth='public', 
                methods=['GET'], csrf=False, cors='*')
    def get_estadisticas(self, **params):
        """
        Obtener estadísticas del usuario autenticado.
        
        Returns:
            JSON: {estadisticas}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            estadisticas = {
                'productos_en_venta': partner.productos_en_venta,
                'productos_vendidos': partner.productos_vendidos,
                'productos_comprados': partner.productos_comprados,
                'valoracion_promedio': round(partner.valoracion_promedio, 2),
                'total_comentarios': partner.total_comentarios,
                'total_denuncias_realizadas': partner.total_denuncias_realizadas,
            }
            
            return response_helpers.success_response(
                data=estadisticas,
                message='Estadísticas recuperadas'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener estadísticas: {str(e)}')
            return response_helpers.server_error_response(str(e))
