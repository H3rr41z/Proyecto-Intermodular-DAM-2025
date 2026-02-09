package com.renaix.domain.usecase.product

import com.renaix.domain.repository.ProductRepository
import com.renaix.util.Constants

/**
 * Caso de uso para crear un producto
 */
class CreateProductUseCase(private val productRepository: ProductRepository) {

    suspend operator fun invoke(
        nombre: String,
        descripcion: String,
        precio: Double,
        categoriaId: Int,
        estadoProducto: String,
        antiguedad: String? = null,
        ubicacion: String? = null,
        etiquetaIds: List<Int>? = null,
        etiquetaNombres: List<String>? = null
    ): Result<Int> {
        // Validaciones
        if (nombre.isBlank()) {
            return Result.failure(Exception("El nombre es requerido"))
        }
        if (nombre.length < Constants.MIN_NAME_LENGTH) {
            return Result.failure(Exception("El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres"))
        }
        if (descripcion.isBlank()) {
            return Result.failure(Exception("La descripción es requerida"))
        }
        if (descripcion.length < Constants.MIN_DESCRIPTION_LENGTH) {
            return Result.failure(Exception("La descripción debe tener al menos ${Constants.MIN_DESCRIPTION_LENGTH} caracteres"))
        }
        if (precio <= 0) {
            return Result.failure(Exception("El precio debe ser mayor a 0"))
        }
        if (precio > 1_000_000) {
            return Result.failure(Exception("El precio no puede superar 1.000.000€"))
        }
        if (categoriaId <= 0) {
            return Result.failure(Exception("Debes seleccionar una categoría"))
        }

        return productRepository.createProduct(
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            precio = precio,
            categoriaId = categoriaId,
            estadoProducto = estadoProducto,
            antiguedad = antiguedad,
            ubicacion = ubicacion?.trim(),
            etiquetaIds = etiquetaIds,
            etiquetaNombres = etiquetaNombres?.map { it.trim() }
        )
    }
}
