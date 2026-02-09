package com.renaix.domain.usecase.user

import com.renaix.domain.model.Product
import com.renaix.domain.repository.UserRepository

/**
 * Caso de uso para obtener los productos de un usuario
 *
 * Nota: Actualmente usa getMyProducts del repositorio.
 * Si se necesita obtener productos de otro usuario,
 * se debería usar ProductRepository con filtro por vendedor.
 */
class GetUserProductsUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Obtiene los productos del usuario actual
     *
     * TODO: Modificar para aceptar userId y obtener productos de cualquier usuario
     *
     * @param page Página a obtener
     * @param limit Límite de productos por página
     * @return Lista de productos del usuario
     */
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20
    ): Result<List<Product>> {
        return userRepository.getMyProducts(page, limit)
    }
}
