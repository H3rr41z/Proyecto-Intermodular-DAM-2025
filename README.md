# Proyecto-Intermodular-2025-26
Autores: Javier Herraiz Calatayud & Alejandro Sánchez Serrano

## Descripción general

Este repositorio recoge todo el trabajo realizado en el proyecto de compraventa de productos de segunda mano, desarrollado por nuestro equipo durante el curso 2025-26. El proyecto consta de dos aplicaciones principales:
- **ERP (Odoo, Docker, PostgreSQL):** plataforma para gestión y supervisión por parte de los empleados.
- **App móvil (Kotlin):** aplicación destinada a los usuarios para publicar, buscar y comprar productos.

El objetivo es facilitar la integración entre ambos sistemas y ofrecer una experiencia optimizada tanto al usuario final como al empleado administrador.

---

## Estructura del repositorio
```text 
projecte-dam-25-26-javier-alejandro/
├── erp/                    # Código, configuración y documentación del ERP (Odoo + Docker)
│   ├── docker/
│   ├── odoo-modules/
│   └── documentación/
├── app-movil/              # Código fuente y documentación de la app móvil en Kotlin
│   ├── src/
│   └── documentación/
├── documentación-general/  # Documentación transversal, diagramas ER/UML, arquitectura, requisitos
├── README.md               # Archivo de ayuda rápida
└── .gitignore
```

---

## Contenidos esenciales

- **erp/**  
  Todo lo necesario para desplegar el ERP, incluyendo el entorno dockerizado y los módulos personalizados.

- **app-movil/**  
  Código y recursos para la app móvil en Kotlin, además de manuales de instalación y los diagramas técnicos.

- **documentación-general/**  
  Documentos clave del proyecto, como requisitos funcionales y no funcionales, análisis y diseño de la base de datos, diagrama entidad-relación, arquitectura general del sistema (endpoints API REST), diagramas de clases y casos de uso.

---

## Organización del trabajo

Las tareas se han planificado y distribuido usando Trello.  
Cada componente del proyecto ha sido desarrollado por los integrantes según sus áreas de especialización:
- **ERP:** configuración, despliegue y administración de Odoo y la base de datos.
- **App móvil:** desarrollo en Kotlin y diseño de la experiencia de usuario.
- **Documentación:** creación y actualización constante de los documentos técnicos y diagramas.

Colaboramos mediante ramas de desarrollo y revisiones frecuentes en GitHub para asegurar la coherencia del trabajo.

---

## Cómo navegar el repositorio

- Si eres docente o revisor, parte de la carpeta **documentación-general/** para encontrar todos los documentos explicativos, diagramas y requisitos.
- Para revisar el ERP, visita **erp/** donde hallarás tanto la configuración Docker como los módulos.
- Para la app móvil, examina **app-movil/** y sus subcarpetas.

---

## Contribuyentes

- Alejandro-WOU
- H3rr41z

---

## Contacto

Para cualquier consulta técnica o sugerencia, utilizar los issues o contactar vía correo del repositorio.

---

