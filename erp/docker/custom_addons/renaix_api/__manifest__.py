# -*- coding: utf-8 -*-
{
    'name': 'Renaix API REST',
    'version': '1.0.0',
    'category': 'API',
    'summary': 'API REST para aplicación móvil Renaix',
    'description': """
        API REST segura con autenticación JWT para la aplicación móvil 
        de compra-venta de productos de segunda mano Renaix.
        
        Funcionalidades:
        - Autenticación JWT con refresh tokens
        - CRUD de productos
        - Sistema de compra-venta
        - Comentarios y valoraciones
        - Mensajería entre usuarios
        - Sistema de denuncias
        - Gestión de imágenes
    """,
    'author': 'Javier Herraiz & Alejandro Sánchez',
    'website': 'https://github.com/tuusuario/renaix',
    'license': 'LGPL-3',
    
    # Dependencias
    'depends': [
        'base',
        'mail',
        'renaix',  # Módulo core
    ],
    
    # Archivos del módulo
    'data': [],  # No necesitamos vistas ni datos, solo API
    
    # Configuración
    'installable': True,
    'application': False,
    'auto_install': False,
    
    # External dependencies
    # PyJWT está instalado manualmente en el contenedor
    'external_dependencies': {
        'python': [],
    },
}
