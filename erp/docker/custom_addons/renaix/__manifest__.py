# -*- coding: utf-8 -*-
{
    'name': 'Renaix - Marketplace Segunda Mano',
    'version': '1.0.0',
    'category': 'Sales',
    'summary': 'Plataforma de compraventa de productos de segunda mano',
    'description': """
        Renaix - Marketplace de Segunda Mano
        =====================================
        
        Aplicación completa para gestionar una plataforma de compraventa 
        de productos de segunda mano entre particulares.
        
        Características principales:
        ---------------------------
        * Gestión de usuarios (compradores y vendedores)
        * Publicación de productos con imágenes y categorías
        * Sistema de valoraciones y comentarios
        * Mensajería entre usuarios
        * Sistema de denuncias y moderación
        * Panel de control para empleados
        * Estadísticas y reportes
        
        Módulos del sistema:
        -------------------
        * ERP (Odoo): Gestión y moderación por empleados
        * API REST: Servicios para app móvil
        * App Móvil: Interfaz para usuarios finales
    """,
    'author': 'Javier Herraiz & Alejandro Sánchez',
    'website': 'https://github.com/Alejandro-WOU/projecte-dam-25-26-javier-alejandro',
    'license': 'LGPL-3',
    'depends': [
        'base',
        'mail',           # Para Chatter y notificaciones
        'contacts',       # Para res.partner
    ],
    'data': [
        # Security
        'security/security.xml',
        'security/ir.model.access.csv',
        
        # Data
        'data/sequences.xml',
        'data/categorias_data.xml',
        
        # Views
        'views/menu.xml',
        'views/categoria_views.xml',
        'views/etiqueta_views.xml',
        'views/res_partner_views.xml',
        'views/producto_views.xml',
        'views/producto_imagen_views.xml',
        'views/compra_views.xml',
    ],
    'demo': [],
    'installable': True,
    'application': True,
    'auto_install': False,
}