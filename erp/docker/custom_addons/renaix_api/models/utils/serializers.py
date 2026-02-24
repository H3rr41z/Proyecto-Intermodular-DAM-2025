# -*- coding: utf-8 -*-
"""
Serializers: Conversión de modelos Odoo a JSON
"""


def serialize_partner(partner, full=False):
    """
    Serializa un res.partner a JSON.
    
    Args:
        partner: Recordset de res.partner
        full: Si True, incluye información completa
    
    Returns:
        dict: Partner serializado
    """
    if not partner:
        return None
    
    data = {
        'id': partner.id,
        'name': partner.name,
        'email': partner.email,
    }
    
    if full:
        data.update({
            'phone': partner.phone or '',
            'mobile': partner.mobile or '',
            'partner_gid': partner.partner_gid,
            'valoracion_promedio': round(partner.valoracion_promedio, 2),
            'productos_en_venta': partner.productos_en_venta,
            'productos_vendidos': partner.productos_vendidos,
            'productos_comprados': partner.productos_comprados,
            'total_comentarios': partner.total_comentarios,
            'fecha_registro_app': partner.fecha_registro_app.isoformat() if partner.fecha_registro_app else None,
            'image_url': f'/api/v1/usuarios/{partner.id}/imagen' if partner.image_1920 else None,
        })
    
    return data


def serialize_categoria(categoria):
    """
    Serializa una categoría a JSON.
    
    Args:
        categoria: Recordset de renaix.categoria
    
    Returns:
        dict: Categoría serializada
    """
    if not categoria:
        return None
    
    return {
        'id': categoria.id,
        'nombre': categoria.name,
        'descripcion': categoria.descripcion or '',
        'producto_count': categoria.producto_count,
        # URL de la imagen
        'imagen_url': f'/web/image/renaix.categoria/{categoria.id}/image' if categoria.image else None,
    }


def serialize_etiqueta(etiqueta):
    """
    Serializa una etiqueta a JSON.
    
    Args:
        etiqueta: Recordset de renaix.etiqueta
    
    Returns:
        dict: Etiqueta serializada
    """
    if not etiqueta:
        return None
    
    return {
        'id': etiqueta.id,
        'nombre': etiqueta.name,
        'producto_count': etiqueta.producto_count,
        'color': etiqueta.color,
    }


def serialize_producto_imagen(imagen):
    """
    Serializa una imagen de producto a JSON.
    
    Args:
        imagen: Recordset de renaix.producto.imagen
    
    Returns:
        dict: Imagen serializada
    """
    if not imagen:
        return None
    
    return {
        'id': imagen.id,
        'url_imagen': f'/api/v1/imagenes/{imagen.id}' if imagen.id else '',
        'es_principal': imagen.es_principal,
        'descripcion': imagen.descripcion or '',
        'secuencia': imagen.secuencia,
    }


def serialize_producto(producto, include_images=True, include_comentarios=False, include_propietario_full=False):
    """
    Serializa un producto a JSON.
    
    Args:
        producto: Recordset de renaix.producto
        include_images: Si True, incluye las imágenes
        include_comentarios: Si True, incluye los comentarios
        include_propietario_full: Si True, incluye info completa del propietario
    
    Returns:
        dict: Producto serializado
    """
    if not producto:
        return None
    
    data = {
        'id': producto.id,
        'nombre': producto.name,
        'descripcion': producto.descripcion or '',
        'precio': producto.precio,
        'estado_producto': producto.estado_producto,
        'estado_venta': producto.estado_venta,
        'antiguedad': producto.antiguedad or '',
        'ubicacion': producto.ubicacion or '',
        'fecha_publicacion': producto.fecha_publicacion.isoformat() if producto.fecha_publicacion else None,
        'fecha_actualizacion': producto.fecha_actualizacion.isoformat() if producto.fecha_actualizacion else None,
        'dias_publicado': producto.dias_publicado,
        'total_comentarios': producto.total_comentarios,
        'total_denuncias': producto.total_denuncias,
        'propietario': serialize_partner(producto.propietario_id, full=include_propietario_full),
        'categoria': serialize_categoria(producto.categoria_id),
        'etiquetas': [serialize_etiqueta(e) for e in producto.etiqueta_ids],
    }
    
    if include_images:
        data['imagenes'] = [serialize_producto_imagen(img) for img in producto.imagen_ids.sorted('secuencia')]
    
    if include_comentarios:
        data['comentarios'] = [serialize_comentario(c) for c in producto.comentario_ids.filtered(lambda x: x.active)]
    
    return data


def serialize_comentario(comentario):
    """
    Serializa un comentario a JSON.
    
    Args:
        comentario: Recordset de renaix.comentario
    
    Returns:
        dict: Comentario serializado
    """
    if not comentario:
        return None
    
    return {
        'id': comentario.id,
        'texto': comentario.texto,
        'fecha': comentario.fecha.isoformat() if comentario.fecha else None,
        'usuario': serialize_partner(comentario.usuario_id, full=False),
        'producto_id': comentario.producto_id.id,
        'producto_nombre': comentario.producto_nombre,
    }


def serialize_valoracion(valoracion):
    """
    Serializa una valoración a JSON.
    
    Args:
        valoracion: Recordset de renaix.valoracion
    
    Returns:
        dict: Valoración serializada
    """
    if not valoracion:
        return None
    
    return {
        'id': valoracion.id,
        'puntuacion': valoracion.puntuacion,
        'comentario': valoracion.comentario or '',
        'fecha': valoracion.fecha.isoformat() if valoracion.fecha else None,
        'tipo_valoracion': valoracion.tipo_valoracion,
        'usuario_valorador': serialize_partner(valoracion.usuario_valorador_id, full=False),
        'usuario_valorado': serialize_partner(valoracion.usuario_valorado_id, full=False),
        'compra_id': valoracion.compra_id.id,
    }


def serialize_compra(compra, include_valoraciones=False):
    """
    Serializa una compra a JSON.
    
    Args:
        compra: Recordset de renaix.compra
        include_valoraciones: Si True, incluye las valoraciones
    
    Returns:
        dict: Compra serializada
    """
    if not compra:
        return None
    
    data = {
        'id': compra.id,
        'codigo': compra.codigo,
        'fecha_compra': compra.fecha_compra.isoformat() if compra.fecha_compra else None,
        'precio_final': compra.precio_final,
        'estado': compra.estado,
        'notas': compra.notas or '',
        'producto': serialize_producto(compra.producto_id, include_images=True, include_comentarios=False),
        'comprador': serialize_partner(compra.comprador_id, full=True),
        'vendedor': serialize_partner(compra.vendedor_id, full=True),
        'comprador_valoro': compra.comprador_valoro,
        'vendedor_valoro': compra.vendedor_valoro,
    }
    
    if include_valoraciones:
        data['valoraciones'] = {
            'comprador_a_vendedor': [serialize_valoracion(v) for v in compra.valoracion_comprador_ids],
            'vendedor_a_comprador': [serialize_valoracion(v) for v in compra.valoracion_vendedor_ids],
        }
    
    return data


def serialize_mensaje(mensaje):
    """
    Serializa un mensaje a JSON.

    Args:
        mensaje: Recordset de renaix.mensaje

    Returns:
        dict: Mensaje serializado
    """
    if not mensaje:
        return None

    data = {
        'id': mensaje.id,
        'texto': mensaje.texto,
        'fecha': mensaje.fecha.isoformat() if mensaje.fecha else None,
        'leido': mensaje.leido,
        'fecha_lectura': mensaje.fecha_lectura.isoformat() if mensaje.fecha_lectura else None,
        'emisor': serialize_partner(mensaje.emisor_id, full=False),
        'receptor': serialize_partner(mensaje.receptor_id, full=False),
        'producto_id': mensaje.producto_id.id if mensaje.producto_id else None,
        'producto_nombre': mensaje.producto_nombre or '',
        'hilo_id': mensaje.hilo_id,
        'message_type': mensaje.tipo_mensaje or 'text',
    }

    # Incluir datos de oferta si es un mensaje de oferta
    if mensaje.tipo_mensaje in ('offer', 'offer_accepted', 'offer_rejected', 'counter_offer'):
        data['offer_data'] = {
            'product_id': mensaje.producto_id.id if mensaje.producto_id else None,
            'product_name': mensaje.producto_nombre or '',
            'original_price': mensaje.precio_original or 0.0,
            'offered_price': mensaje.precio_ofertado or 0.0,
        }

    return data


def serialize_denuncia(denuncia):
    """
    Serializa una denuncia a JSON.
    
    Args:
        denuncia: Recordset de renaix.denuncia
    
    Returns:
        dict: Denuncia serializada
    """
    if not denuncia:
        return None
    
    return {
        'id': denuncia.id,
        'tipo': denuncia.tipo,
        'motivo': denuncia.motivo,
        'categoria': denuncia.categoria,
        'estado': denuncia.estado,
        'fecha_denuncia': denuncia.fecha_denuncia.isoformat() if denuncia.fecha_denuncia else None,
        'fecha_resolucion': denuncia.fecha_resolucion.isoformat() if denuncia.fecha_resolucion else None,
        'resolucion': denuncia.resolucion or '',
        'denunciado_nombre': denuncia.denunciado_nombre,
        'usuario_reportante': serialize_partner(denuncia.usuario_reportante_id, full=False),
        'producto_id': denuncia.producto_id.id if denuncia.producto_id else None,
        'comentario_id': denuncia.comentario_id.id if denuncia.comentario_id else None,
        'usuario_reportado_id': denuncia.usuario_reportado_id.id if denuncia.usuario_reportado_id else None,
    }


def serialize_conversacion(mensajes):
    """
    Serializa una conversación (lista de mensajes) agrupada.
    
    Args:
        mensajes: Recordset de renaix.mensaje
    
    Returns:
        dict: Conversación serializada con metadatos
    """
    if not mensajes:
        return None
    
    # Asumir que todos los mensajes son del mismo hilo
    primer_mensaje = mensajes[0]
    ultimo_mensaje = mensajes[-1]
    
    # Determinar el "otro" usuario (el que no soy yo)
    # Esto se debe calcular en el controlador según el usuario autenticado
    
    mensajes_no_leidos = [m for m in mensajes if not m.leido]

    # Recopilar participantes únicos del hilo (emisor y receptor)
    seen_ids = set()
    participantes = []
    for m in mensajes:
        for partner in [m.emisor_id, m.receptor_id]:
            if partner and partner.id and partner.id not in seen_ids:
                seen_ids.add(partner.id)
                participantes.append(serialize_partner(partner, full=False))

    return {
        'hilo_id': primer_mensaje.hilo_id,
        'participantes': participantes,
        'producto': serialize_producto(primer_mensaje.producto_id, include_images=False) if primer_mensaje.producto_id else None,
        'ultimo_mensaje': serialize_mensaje(ultimo_mensaje),
        'total_mensajes': len(mensajes),
        'mensajes_no_leidos': len(mensajes_no_leidos),
        'mensajes': [serialize_mensaje(m) for m in mensajes],
    }
