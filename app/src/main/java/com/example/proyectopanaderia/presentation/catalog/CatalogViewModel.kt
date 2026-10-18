package com.example.proyectopanaderia.presentation.catalog
import androidx.lifecycle.SavedStateHandle
import com.example.proyectopanaderia.domain.repository.*
import com.example.proyectopanaderia.domain.usecase.FilterCatalogUseCase
import com.example.proyectopanaderia.presentation.common.*
import kotlinx.coroutines.flow.combine

@dagger.hilt.android.lifecycle.HiltViewModel(assistedFactory = CatalogViewModel.Factory::class)
class CatalogViewModel @dagger.assisted.AssistedInject constructor(@dagger.assisted.Assisted private val email: String, private val customers: CustomerRepository,
    private val catalog: CatalogRepository, private val shopping: ShoppingRepository,
    private val savedState: SavedStateHandle,
    private val filterCatalog: FilterCatalogUseCase = FilterCatalogUseCase()) : ScreenViewModel<CatalogState>() {
    @dagger.assisted.AssistedFactory
    interface Factory { fun create(email: String): CatalogViewModel }
    private var customerId = 0L
    init { load() }
    fun load() = observe {
        customerId = customers.ensureLocalCustomer(email)
        combine(catalog.products(), catalog.categories(), shopping.favorites(customerId),
            savedState.getStateFlow("query", ""), savedState.getStateFlow<Long?>("categoryId", null)
        ) { products, categories, favorites, query, selected ->
            val filtered = filterCatalog(products, categories, query, selected)
            CatalogState(filtered.products, categories, favorites.map { it.id }.toSet(), query,
                filtered.categoryId, products.isEmpty())
        }
    }
    fun setQuery(value: String) { savedState["query"] = value }
    fun setCategory(value: Long?) { savedState["categoryId"] = value }
    fun add(id: Long) = change {
        shopping.changeQuantity(customerId, id, 1)
        UiEvent.Message("Producto añadido al carrito.")
    }
    fun favorite(id: Long, enabled: Boolean) = change { shopping.setFavorite(customerId, id, enabled); null }
    fun examples() = change { catalog.loadExampleCatalog(); UiEvent.Message("Catálogo de ejemplo preparado.") }
}
