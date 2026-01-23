# -*- coding: utf-8 -*-
"""
Controlador de Productos
Endpoints: listar, detalle, crear, actualizar, eliminar, buscar, publicar, imágenes
"""

import json
import logging
import base64
from odoo import http
from odoo.http import request
from ..models.utils import jwt_utils, validators, response_helpers, serializers
from ..config import settings

_logger = logging.getLogger(__name__)


class ProductosController(http.Controller):
    
    @http.route('/api/v1/productos', type='http', auth='none', 
                methods=['GET'], csrf=False, cors='*')
    def listar_productos(self, **params):
        """
        Listar productos disponibles (público).
        
        Query params:
            page: Número de página (default: 1)
            limit: Elementos por página (default: 20)
            estado_venta: filtrar por estado (disponible, reservado, vendido)
        
        Returns:
            JSON: {productos} (paginado)
        """
        try:
            # Parámetros de paginación
            page, limit = validators.validate_pagination_params(
                params.get('page'),
                params.get('limit')
            )
            
            # Construir dominio de búsqueda
            domain = [('active', '=', True)]
            
            # Filtrar por estado de venta si se proporciona
            if params.get('estado_venta'):
                domain.append(('estado_venta', '=', params.get('estado_venta')))
            else:
                # Por defecto, solo productos disponibles
                domain.append(('estado_venta', '=', 'disponible'))
            
            # Buscar productos
            Producto = request.env['renaix.producto'].sudo()
            productos = Producto.search(domain, order='fecha_publicacion DESC')
            
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
            _logger.error(f'Error al listar productos: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>', type='http', auth='none', 
                methods=['GET'], csrf=False, cors='*')
    def detalle_producto(self, producto_id, **params):
        """
        Obtener detalle de un producto (público).
        
        Returns:
            JSON: {producto}
        """
        try:
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Serializar con comentarios
            producto_data = serializers.serialize_producto(
                producto, 
                include_images=True, 
                include_comentarios=True,
                include_propietario_full=True
            )
            
            return response_helpers.success_response(
                data=producto_data,
                message='Producto encontrado'
            )
            
        except Exception as e:
            _logger.error(f'Error al obtener producto: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos', type='http', auth='public', 
                methods=['POST'], csrf=False, cors='*')
    def crear_producto(self, **params):
        """
        Crear nuevo producto (requiere autenticación).
        
        Body JSON:
        {
            "nombre": "iPhone 12",
            "descripcion": "Buen estado",
            "precio": 350.00,
            "categoria_id": 1,
            "estado_producto": "como_nuevo",  # opcional
            "antiguedad": "6_meses",  # opcional
            "ubicacion": "Madrid",  # opcional
            "etiqueta_ids": [1, 2, 3]  # opcional
        }
        
        Returns:
            JSON: {producto}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Obtener datos
            data = json.loads(request.httprequest.data.decode('utf-8'))
            
            # Validar datos
            is_valid, error_msg = validators.validate_producto_data(data)
            if not is_valid:
                return response_helpers.validation_error_response(error_msg)
            
            # Verificar que la categoría existe
            categoria = request.env['renaix.categoria'].sudo().browse(data['categoria_id'])
            if not categoria.exists():
                return response_helpers.validation_error_response('Categoría no encontrada')
            
            # Preparar valores
            producto_vals = {
                'name': data['nombre'],
                'descripcion': data.get('descripcion', ''),
                'precio': data['precio'],
                'propietario_id': partner.id,
                'categoria_id': data['categoria_id'],
                'estado_producto': data.get('estado_producto', 'usado'),
                'antiguedad': data.get('antiguedad', ''),
                'ubicacion': data.get('ubicacion', ''),
                'estado_venta': 'borrador',  # Inicialmente en borrador
            }
            
            # Crear producto
            producto = request.env['renaix.producto'].sudo().create(producto_vals)
            
            # Añadir etiquetas si se proporcionan
            if data.get('etiqueta_ids'):
                producto.sudo().write({'etiqueta_ids': [(6, 0, data['etiqueta_ids'])]})
            
            _logger.info(f'Producto creado: {producto.id} por usuario {partner.id}')
            
            return response_helpers.success_response(
                data=serializers.serialize_producto(producto, include_images=True),
                message='Producto creado exitosamente',
                status=201
            )
            
        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        
        except Exception as e:
            _logger.error(f'Error al crear producto: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>', type='http', auth='public', 
                methods=['PUT'], csrf=False, cors='*')
    def actualizar_producto(self, producto_id, **params):
        """
        Actualizar producto (solo el propietario).
        
        Body JSON: Mismo que crear, todos los campos opcionales
        
        Returns:
            JSON: {producto}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Verificar que sea el propietario
            if producto.propietario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso para editar este producto')
            
            # Obtener datos
            data = json.loads(request.httprequest.data.decode('utf-8'))
            
            # Preparar valores a actualizar
            update_vals = {}
            
            if 'nombre' in data:
                update_vals['name'] = data['nombre']
            
            if 'descripcion' in data:
                update_vals['descripcion'] = data['descripcion']
            
            if 'precio' in data:
                if not validators.validate_price(data['precio']):
                    return response_helpers.validation_error_response('Precio inválido')
                update_vals['precio'] = data['precio']
            
            if 'estado_producto' in data:
                update_vals['estado_producto'] = data['estado_producto']
            
            if 'antiguedad' in data:
                update_vals['antiguedad'] = data['antiguedad']
            
            if 'ubicacion' in data:
                update_vals['ubicacion'] = data['ubicacion']
            
            if 'categoria_id' in data:
                categoria = request.env['renaix.categoria'].sudo().browse(data['categoria_id'])
                if not categoria.exists():
                    return response_helpers.validation_error_response('Categoría no encontrada')
                update_vals['categoria_id'] = data['categoria_id']
            
            # Actualizar
            if update_vals:
                producto.sudo().write(update_vals)
            
            # Actualizar etiquetas si se proporcionan
            if 'etiqueta_ids' in data:
                producto.sudo().write({'etiqueta_ids': [(6, 0, data['etiqueta_ids'])]})
            
            _logger.info(f'Producto actualizado: {producto.id}')
            
            return response_helpers.success_response(
                data=serializers.serialize_producto(producto, include_images=True),
                message='Producto actualizado'
            )
            
        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        
        except Exception as e:
            _logger.error(f'Error al actualizar producto: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>', type='http', auth='public', 
                methods=['DELETE'], csrf=False, cors='*')
    def eliminar_producto(self, producto_id, **params):
        """
        Eliminar producto (solo el propietario).
        
        Returns:
            JSON: {message}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Verificar que sea el propietario
            if producto.propietario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso para eliminar este producto')
            
            # Verificar que no tenga compras activas
            if producto.estado_venta in ['reservado', 'vendido']:
                return response_helpers.validation_error_response('No se puede eliminar un producto reservado o vendido')
            
            # Eliminar (soft delete)
            producto.sudo().write({'active': False})
            
            _logger.info(f'Producto eliminado: {producto.id}')
            
            return response_helpers.success_response(
                message='Producto eliminado exitosamente'
            )
            
        except Exception as e:
            _logger.error(f'Error al eliminar producto: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>/publicar', type='http', auth='public', 
                methods=['POST'], csrf=False, cors='*')
    def publicar_producto(self, producto_id, **params):
        """
        Publicar producto (cambiar estado de borrador a disponible).
        
        Returns:
            JSON: {producto}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Verificar que sea el propietario
            if producto.propietario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')
            
            # Verificar que esté en borrador
            if producto.estado_venta != 'borrador':
                return response_helpers.validation_error_response('El producto ya está publicado')
            
            # Publicar
            producto.sudo().write({'estado_venta': 'disponible'})
            
            _logger.info(f'Producto publicado: {producto.id}')
            
            return response_helpers.success_response(
                data=serializers.serialize_producto(producto, include_images=True),
                message='Producto publicado exitosamente'
            )
            
        except Exception as e:
            _logger.error(f'Error al publicar producto: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/buscar', type='http', auth='none', 
                methods=['GET'], csrf=False, cors='*')
    def buscar_productos(self, **params):
        """
        Búsqueda avanzada de productos (público).
        
        Query params:
            query: Texto a buscar
            categoria_id: ID de categoría
            etiquetas: IDs de etiquetas (separadas por coma)
            precio_min: Precio mínimo
            precio_max: Precio máximo
            estado_producto: Estado del producto
            ubicacion: Ubicación
            orden: precio_asc, precio_desc, fecha_desc, fecha_asc
            page: Número de página
            limit: Elementos por página
        
        Returns:
            JSON: {productos} (paginado)
        """
        try:
            # Validar y limpiar filtros
            filters = validators.validate_search_filters(params)
            
            # Parámetros de paginación
            page, limit = validators.validate_pagination_params(
                params.get('page'),
                params.get('limit')
            )
            
            # Construir dominio
            domain = [
                ('active', '=', True),
                ('estado_venta', '=', 'disponible')
            ]
            
            # Búsqueda de texto
            if filters.get('query'):
                domain.append('|')
                domain.append(('name', 'ilike', filters['query']))
                domain.append(('descripcion', 'ilike', filters['query']))
            
            # Filtro de categoría
            if filters.get('categoria_id'):
                domain.append(('categoria_id', '=', filters['categoria_id']))
            
            # Filtro de etiquetas
            if filters.get('etiquetas'):
                domain.append(('etiqueta_ids', 'in', filters['etiquetas']))
            
            # Filtro de precio
            if filters.get('precio_min'):
                domain.append(('precio', '>=', filters['precio_min']))
            
            if filters.get('precio_max'):
                domain.append(('precio', '<=', filters['precio_max']))
            
            # Filtro de estado del producto
            if filters.get('estado_producto'):
                domain.append(('estado_producto', '=', filters['estado_producto']))
            
            # Filtro de ubicación
            if filters.get('ubicacion'):
                domain.append(('ubicacion', 'ilike', filters['ubicacion']))
            
            # Determinar orden
            order_map = {
                'precio_asc': 'precio ASC',
                'precio_desc': 'precio DESC',
                'fecha_desc': 'fecha_publicacion DESC',
                'fecha_asc': 'fecha_publicacion ASC',
            }
            order = order_map.get(filters.get('orden', 'fecha_desc'), 'fecha_publicacion DESC')
            
            # Buscar
            Producto = request.env['renaix.producto'].sudo()
            productos = Producto.search(domain, order=order, limit=settings.MAX_SEARCH_RESULTS)
            
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
                message=f'Se encontraron {total} productos'
            )
            
        except Exception as e:
            _logger.error(f'Error en búsqueda: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>/imagenes', type='http', auth='public', 
                methods=['POST'], csrf=False, cors='*')
    def agregar_imagen(self, producto_id, **params):
        """
        Agregar imagen a un producto.
        
        Body JSON:
        {
            "url_imagen": "https://example.com/image.jpg",
            "es_principal": false,  # opcional
            "descripcion": "Vista frontal"  # opcional
        }
        
        Returns:
            JSON: {imagen}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Verificar que sea el propietario
            if producto.propietario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')
            
            # Obtener datos
            data = json.loads(request.httprequest.data.decode('utf-8'))
            
            if not data.get('url_imagen'):
                return response_helpers.validation_error_response('URL de imagen requerida')
            
            # Verificar límite de imágenes
            if len(producto.imagen_ids) >= settings.MAX_IMAGES_PER_PRODUCT:
                return response_helpers.validation_error_response(f'Máximo {settings.MAX_IMAGES_PER_PRODUCT} imágenes por producto')
            
            # Crear imagen
            imagen_vals = {
                'producto_id': producto.id,
                'url_imagen': data['url_imagen'],
                'es_principal': data.get('es_principal', False),
                'descripcion': data.get('descripcion', ''),
            }
            
            imagen = request.env['renaix.producto.imagen'].sudo().create(imagen_vals)
            
            _logger.info(f'Imagen añadida al producto {producto.id}')
            
            return response_helpers.success_response(
                data=serializers.serialize_producto_imagen(imagen),
                message='Imagen añadida exitosamente',
                status=201
            )
            
        except json.JSONDecodeError:
            return response_helpers.validation_error_response('JSON inválido')
        
        except Exception as e:
            _logger.error(f'Error al agregar imagen: {str(e)}')
            return response_helpers.server_error_response(str(e))
    
    
    @http.route('/api/v1/productos/<int:producto_id>/imagenes/<int:imagen_id>', 
                type='http', auth='public', methods=['DELETE'], csrf=False, cors='*')
    def eliminar_imagen(self, producto_id, imagen_id, **params):
        """
        Eliminar imagen de un producto.
        
        Returns:
            JSON: {message}
        """
        try:
            # Verificar token
            partner = jwt_utils.verify_token(request)
            
            # Buscar producto
            producto = request.env['renaix.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return response_helpers.not_found_response('Producto no encontrado')
            
            # Verificar que sea el propietario
            if producto.propietario_id.id != partner.id:
                return response_helpers.forbidden_response('No tienes permiso')
            
            # Buscar imagen
            imagen = request.env['renaix.producto.imagen'].sudo().browse(imagen_id)
            
            if not imagen.exists() or imagen.producto_id.id != producto.id:
                return response_helpers.not_found_response('Imagen no encontrada')
            
            # Eliminar
            imagen.sudo().unlink()
            
            _logger.info(f'Imagen eliminada del producto {producto.id}')
            
            return response_helpers.success_response(
                message='Imagen eliminada exitosamente'
            )
            
        except Exception as e:
            _logger.error(f'Error al eliminar imagen: {str(e)}')
            return response_helpers.server_error_response(str(e))
