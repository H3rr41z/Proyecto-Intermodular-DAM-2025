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
            "name": "Nuevo Nombre",     # opcional
            "phone": "612345678",       # opcional
            "mobile": "612345679",      # opcional
            "image": "base64_string"    # opcional - imagen en base64
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

            # Manejar imagen si se proporciona (null significa "no cambiar", "" significa "eliminar")
            if 'image' in data and data['image'] is not None:
                image_data = data['image']

                # Si es una cadena vacía, eliminar la imagen
                if not image_data:
                    update_vals['image_1920'] = False
                else:
                    # Validar que sea base64 válido
                    try:
                        import base64

                        # Verificar si tiene el prefijo data:image y extraerlo
                        if isinstance(image_data, str):
                            if image_data.startswith('data:image'):
                                # Extraer solo la parte base64
                                image_data = image_data.split(',', 1)[1]

                            # Limpiar espacios en blanco
                            image_data = image_data.strip()

                        # Intentar decodificar para validar formato base64
                        image_bytes = base64.b64decode(image_data, validate=True)

                        # Validar tamaño (máximo 5MB)
                        if len(image_bytes) > 5 * 1024 * 1024:
                            return response_helpers.validation_error_response(
                                'La imagen es demasiado grande. Tamaño máximo: 5MB'
                            )

                        # Validar que tenga un tamaño mínimo razonable (al menos 100 bytes)
                        if len(image_bytes) < 100:
                            return response_helpers.validation_error_response(
                                'La imagen es demasiado pequeña o está corrupta'
                            )

                        # Guardar la imagen en base64 (Odoo valida internamente el formato)
                        update_vals['image_1920'] = image_data

                    except (base64.binascii.Error, ValueError) as b64_error:
                        _logger.error(f'Error al decodificar base64: {str(b64_error)}')
                        return response_helpers.validation_error_response('Imagen en formato base64 inválido')
                    except Exception as img_error:
                        _logger.error(f'Error al procesar imagen: {str(img_error)}')
                        return response_helpers.validation_error_response(f'Error al procesar imagen: {str(img_error)}')

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
    
    
    @http.route('/api/v1/usuarios/perfil/imagen', type='http', auth='public',
                methods=['POST', 'DELETE'], csrf=False, cors='*')
    def update_imagen_perfil(self, **params):
        """
        Actualizar o eliminar la imagen de perfil del usuario autenticado.

        POST - Subir nueva imagen:
        Body JSON:
        {
            "image": "base64_string"  # imagen en base64
        }

        DELETE - Eliminar imagen actual:
        Sin body

        Returns:
            JSON: {user}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)

            # DELETE - Eliminar imagen
            if request.httprequest.method == 'DELETE':
                partner.sudo().write({'image_1920': False})
                return response_helpers.success_response(
                    data=serializers.serialize_partner(partner, full=True),
                    message='Imagen de perfil eliminada'
                )

            # POST - Subir nueva imagen
            data = json.loads(request.httprequest.data.decode('utf-8'))

            if 'image' not in data:
                return response_helpers.validation_error_response('Campo "image" requerido')

            image_data = data['image']

            if not image_data:
                return response_helpers.validation_error_response('La imagen no puede estar vacía')

            # Validar y procesar imagen
            try:
                import base64

                # Verificar si tiene el prefijo data:image y extraerlo
                if isinstance(image_data, str):
                    if image_data.startswith('data:image'):
                        # Extraer solo la parte base64
                        image_data = image_data.split(',', 1)[1]

                    # Limpiar espacios en blanco
                    image_data = image_data.strip()

                # Intentar decodificar para validar formato base64
                image_bytes = base64.b64decode(image_data, validate=True)

                # Validar tamaño (máximo 5MB)
                if len(image_bytes) > 5 * 1024 * 1024:
                    return response_helpers.validation_error_response(
                        'La imagen es demasiado grande. Tamaño máximo: 5MB'
                    )

                # Validar que tenga un tamaño mínimo razonable (al menos 100 bytes)
                if len(image_bytes) < 100:
                    return response_helpers.validation_error_response(
                        'La imagen es demasiado pequeña o está corrupta'
                    )

                # Guardar la imagen (Odoo valida internamente el formato)
                partner.sudo().write({'image_1920': image_data})

                return response_helpers.success_response(
                    data=serializers.serialize_partner(partner, full=True),
                    message='Imagen de perfil actualizada'
                )

            except (base64.binascii.Error, ValueError) as b64_error:
                _logger.error(f'Error al decodificar base64: {str(b64_error)}')
                return response_helpers.validation_error_response('Imagen en formato base64 inválido')
            except Exception as img_error:
                _logger.error(f'Error al procesar imagen: {str(img_error)}')
                return response_helpers.validation_error_response(f'Error al procesar imagen: {str(img_error)}')

        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')

        except Exception as e:
            _logger.error(f'Error al actualizar imagen de perfil: {str(e)}')
            return response_helpers.server_error_response(str(e))


    @http.route('/api/v1/usuarios/perfil/password', type='http', auth='public',
                methods=['PUT'], csrf=False, cors='*')
    def cambiar_password(self, **params):
        """
        Cambiar contraseña del usuario autenticado.

        Body JSON:
        {
            "password_actual": "contraseña_actual",
            "password_nueva": "nueva_contraseña"
        }

        Returns:
            JSON: {message}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)

            # Obtener datos
            data = json.loads(request.httprequest.data.decode('utf-8'))

            if not data.get('password_actual'):
                return response_helpers.validation_error_response('Campo "password_actual" requerido')

            if not data.get('password_nueva'):
                return response_helpers.validation_error_response('Campo "password_nueva" requerido')

            # Verificar contraseña actual
            if not auth_helpers.verify_password(data['password_actual'], partner.password_hash):
                return response_helpers.validation_error_response('La contraseña actual es incorrecta')

            # Validar fortaleza de la nueva contraseña
            is_valid, error_msg = auth_helpers.validate_password_strength(data['password_nueva'])
            if not is_valid:
                return response_helpers.validation_error_response(error_msg)

            # Cambiar contraseña usando el método del modelo
            partner.sudo().set_password(data['password_nueva'])

            _logger.info(f'Contraseña cambiada para usuario {partner.id}')

            return response_helpers.success_response(
                message='Contraseña actualizada correctamente'
            )

        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')

        except Exception as e:
            _logger.error(f'Error al cambiar contraseña: {str(e)}')
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


    @http.route('/api/v1/usuarios/<int:user_id>/productos', type='http', auth='none',
                methods=['GET'], csrf=False, cors='*')
    def get_productos_usuario_publico(self, user_id, **params):
        """
        Obtener productos disponibles de un usuario público.

        Path params:
            user_id: ID del usuario

        Query params:
            page: Número de página (default: 1)
            limit: Elementos por página (default: 20)

        Returns:
            JSON: {productos} (paginado)
        """
        try:
            # Verificar que el usuario existe
            partner = request.env['res.partner'].sudo().browse(user_id)

            if not partner.exists() or not partner.es_usuario_app:
                return response_helpers.not_found_response('Usuario no encontrado')

            # Parámetros de paginación
            page, limit = validators.validate_pagination_params(
                params.get('page'),
                params.get('limit')
            )

            # Buscar productos disponibles del usuario
            productos = request.env['renaix.producto'].sudo().search([
                ('propietario_id', '=', user_id),
                ('active', '=', True),
                ('estado_venta', '=', 'disponible')
            ], order='fecha_publicacion DESC')

            total = len(productos)
            offset = (page - 1) * limit
            productos_pagina = productos[offset:offset + limit]

            productos_data = [serializers.serialize_producto(p, include_images=True) for p in productos_pagina]

            return response_helpers.paginated_response(
                items=productos_data,
                total=total,
                page=page,
                limit=limit,
                message='Productos recuperados'
            )

        except Exception as e:
            _logger.error(f'Error al obtener productos del usuario: {str(e)}')
            return response_helpers.server_error_response(str(e))


    @http.route('/api/v1/usuarios/<int:partner_id>/imagen', type='http', auth='none',
                methods=['GET'], csrf=False, cors='*')
    def get_imagen_usuario(self, partner_id, **params):
        """
        Sirve la imagen de perfil de un usuario (público, sin autenticación).
        Necesario porque /web/image/ requiere sesión web, no Bearer token.

        Returns:
            HTTP binary response con la imagen de perfil
        """
        try:
            import base64 as b64
            partner = request.env['res.partner'].sudo().browse(partner_id)
            if not partner.exists() or not partner.image_1920:
                return request.make_response('Not found', status=404)

            image_data = b64.b64decode(partner.image_1920)
            headers = [
                ('Content-Type', 'image/jpeg'),
                ('Cache-Control', 'public, max-age=86400'),
            ]
            return request.make_response(image_data, headers=headers)

        except Exception as e:
            _logger.error(f'Error al servir imagen de usuario {partner_id}: {str(e)}')
            return request.make_response('Error', status=500)
