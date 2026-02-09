package com.renaix.domain.usecase.category

import com.renaix.domain.model.Category
import com.renaix.domain.repository.CategoryRepository

/**
 * Caso de uso para obtener categorías
 */
class GetCategoriesUseCase(private val categoryRepository: CategoryRepository) {

    suspend operator fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
