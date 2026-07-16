package com.marcroldan.rimemba.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marcroldan.rimemba.data.repository.ItemRepository
import com.marcroldan.rimemba.domain.model.Item
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemListViewModel(private val repository: ItemRepository) : ViewModel() {

    val items: StateFlow<List<Item>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCompletado(item: Item, completado: Boolean) {
        viewModelScope.launch { repository.setCompletado(item.id, completado) }
    }

    fun delete(item: Item) {
        viewModelScope.launch { repository.delete(item) }
    }

    class Factory(private val repository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ItemListViewModel(repository) as T
    }
}
