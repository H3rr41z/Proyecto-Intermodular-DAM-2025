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
        * Informes PDF imprimibles
        
        Módulos del sistema:
        -------------------
        * ERP (Odoo): Gestión y moderación por empleados
        * API REST: Servicios para app móvil
        * App Móvil: Interfaz para usuarios finales
        
        Sprint 1 - Completado:
        ---------------------
        ✅ Modelo de datos completo (11 modelos)
        ✅ Backend administrativo con vistas List/Form/Search
        ✅ Sistema de permisos (4 niveles)
        ✅ Estadísticas con gráficos (4 Graph views)
        ✅ Listados avanzados (2 listados)
        ✅ Dashboard organizado jerárquicamente
        ✅ Informe QWeb profesional (PDF)
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
        # ================================
        # SECURITY (debe ir primero)
        # ================================
        'security/security.xml',
        'security/ir.model.access.csv',
        
        # ================================
        # DATA (datos iniciales)
        # ================================
        'data/sequences.xml',
        'data/categorias_data.xml',
        
        # ================================
        # VIEWS (vistas y acciones)
        # ================================
        
        # Configuración
        'views/categoria_views.xml',
        'views/etiqueta_views.xml',
        
        # Usuarios
        'views/res_partner_views.xml',
        
        # Productos
        'views/producto_views.xml',
        'views/producto_imagen_views.xml',
        
        # Transacciones
        'views/compra_views.xml',
        'views/valoracion_views.xml',
        
        # Comunicación
        'views/comentario_views.xml',
        'views/mensaje_views.xml',
        
        # Moderación
        'views/denuncia_views.xml',
        
        # Estadísticas (gráficos + listados)
        'views/estadisticas_views.xml',
        
        # ================================
        # REPORTS (informes PDF)
        # ================================
        'reports/report_partner_activity.xml',
        
        # ================================
        # MENU (AL FINAL - después de todo)
        # ================================
        'views/menu.xml',
    ],
    'demo': [],
    'images': ['static/description/icon.png'],
    'installable': True,
    'application': True,
    'auto_install': False,
    
    # Configuración adicional
    'sequence': 10,
    'price': 0.00,
    'currency': 'EUR',
}
