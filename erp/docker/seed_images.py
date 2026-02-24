#!/usr/bin/env python3
"""
Seeder de imágenes para Renaix.

Descarga imágenes de categoría apropiada (loremflickr.com) y las sube
directamente a los productos demo via XML-RPC de Odoo.

Requisitos:
    pip install requests

Uso:
    python seed_images.py

Asegúrate de que Odoo esté corriendo en localhost:8069 antes de ejecutar.
"""

import xmlrpc.client
import requests
import base64
import sys
import time
import getpass
import argparse

# ==================== CONFIGURACIÓN ====================

ODOO_URL  = "http://localhost:8069"
ODOO_DB   = "Renaix_db"

def parse_args():
    parser = argparse.ArgumentParser(description="Seeder de imágenes para Renaix")
    parser.add_argument("--user",     default=None, help="Email/login del admin de Odoo")
    parser.add_argument("--password", default=None, help="Contraseña del admin de Odoo")
    return parser.parse_args()

args = parse_args()
ADMIN_USER     = args.user     or input("Email/login del admin de Odoo: ").strip()
ADMIN_PASSWORD = args.password or getpass.getpass(f"Contraseña para '{ADMIN_USER}': ")

# ==================== IMÁGENES POR PRODUCTO ====================
# Usa loremflickr.com con keywords + lock para resultados consistentes.
# Formato: "fragment del nombre del producto": [(keyword, lock), ...]

PRODUCT_IMAGE_MAP = {
    "iphone":       [("iphone", 1), ("smartphone", 2)],
    "playstation":  [("playstation,gaming", 1), ("gamepad,controller", 2)],
    "macbook":      [("macbook,laptop", 1), ("apple,computer", 2)],
    "airpods":      [("airpods,earphones", 1)],
    "zapatillas":   [("sneakers,nike", 1), ("shoes,sport", 2)],
    "chaqueta":     [("leather,jacket", 1), ("fashion,jacket", 2)],
    "robot aspirador": [("roomba,vacuum", 1), ("robot,vacuum", 2)],
    "mesa":         [("dining,table,wood", 1), ("furniture,table", 2)],
    "bicicleta":    [("mountain,bike", 1), ("cycling,bicycle", 2)],
    "tabla de surf":    [("surfboard,surfing", 1), ("surf,beach", 2)],
    "harry potter": [("harry,potter,books", 1), ("fantasy,books", 2)],
    "vinilo":       [("vinyl,record", 1), ("beatles,music", 2)],
    "lego":         [("lego,starwars", 1), ("lego,toys", 2)],
    "patinete":     [("electric,scooter", 1), ("scooter,city", 2)],
    "nintendo":     [("nintendo,switch", 1), ("gaming,nintendo", 2)],
    "cámara":       [("canon,camera", 1), ("photography,camera", 2)],
    "samsung":      [("samsung,phone", 1), ("android,smartphone", 2)],
}

FALLBACK_KEYWORDS = [("marketplace,product", 1), ("second,hand", 2)]


def get_keywords_for_product(name: str) -> list:
    """Devuelve la lista de (keyword, lock) para un nombre de producto."""
    name_lower = name.lower()
    for fragment, kw_list in PRODUCT_IMAGE_MAP.items():
        if fragment in name_lower:
            return kw_list
    return FALLBACK_KEYWORDS


def download_image_b64(keyword: str, lock: int, size: int = 600) -> str | None:
    """
    Descarga una imagen de loremflickr.com y la devuelve como base64.
    Sigue redirecciones automáticamente.
    """
    url = f"https://loremflickr.com/{size}/{size}/{keyword}?lock={lock}"
    try:
        resp = requests.get(url, timeout=20, allow_redirects=True)
        resp.raise_for_status()
        if "image" not in resp.headers.get("Content-Type", ""):
            print(f"    ⚠️  La respuesta no es una imagen ({url})")
            return None
        return base64.b64encode(resp.content).decode("utf-8")
    except Exception as e:
        print(f"    ⚠️  Error descargando {url}: {e}")
        return None


def connect_odoo():
    """Autentica contra Odoo y devuelve (uid, models_proxy)."""
    print(f"Conectando a {ODOO_URL} (BD: {ODOO_DB})...")
    try:
        common = xmlrpc.client.ServerProxy(f"{ODOO_URL}/xmlrpc/2/common")
        uid = common.authenticate(ODOO_DB, ADMIN_USER, ADMIN_PASSWORD, {})
        if not uid:
            print("❌  Autenticación fallida. Verifica ODOO_DB / ADMIN_USER / ADMIN_PASSWORD.")
            sys.exit(1)
        print(f"✅  Autenticado como UID {uid}\n")
        models = xmlrpc.client.ServerProxy(f"{ODOO_URL}/xmlrpc/2/object")
        return uid, models
    except Exception as e:
        print(f"❌  No se puede conectar: {e}")
        sys.exit(1)


def main():
    uid, models = connect_odoo()

    # Obtener todos los productos con sus imágenes actuales
    products = models.execute_kw(
        ODOO_DB, uid, ADMIN_PASSWORD,
        "renaix.producto", "search_read", [[]],
        {"fields": ["id", "name", "imagen_ids"], "order": "id asc"},
    )
    print(f"Productos encontrados: {len(products)}\n")
    print("-" * 60)

    created = 0
    skipped = 0

    for product in products:
        pid   = product["id"]
        name  = product["name"]
        existing = product.get("imagen_ids", [])

        if existing:
            print(f"⏭️   [{pid:>3}] {name} — ya tiene {len(existing)} imagen(es), omitiendo")
            skipped += 1
            continue

        print(f"📸  [{pid:>3}] {name}")
        kw_list = get_keywords_for_product(name)

        for i, (keyword, lock) in enumerate(kw_list):
            print(f"       Descargando imagen {i+1}/{len(kw_list)} ({keyword}, lock={lock})...")
            b64 = download_image_b64(keyword, lock)
            if not b64:
                continue

            vals = {
                "producto_id": pid,
                "imagen":      b64,
                "es_principal": i == 0,
                "descripcion": f"Imagen de {name}",
                "secuencia":   (i + 1) * 10,
            }
            try:
                img_id = models.execute_kw(
                    ODOO_DB, uid, ADMIN_PASSWORD,
                    "renaix.producto.imagen", "create", [vals],
                )
                print(f"       ✅ Imagen creada (ID {img_id})")
                created += 1
            except Exception as e:
                print(f"       ❌ Error al crear imagen: {e}")

            time.sleep(0.5)   # Pausa cortés entre peticiones

        print()

    print("-" * 60)
    print(f"Resumen: {created} imágenes creadas · {skipped} productos omitidos")
    print("✅  ¡Listo!")


if __name__ == "__main__":
    main()
