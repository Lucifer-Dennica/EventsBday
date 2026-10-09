package ru.yourname.events.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.yourname.events.data.contacts.ContactsImporter
import ru.yourname.events.data.db.AppDatabase
import ru.yourname.events.data.model.Event
import ru.yourname.events.data.repository.EventRepository
import java.time.LocalDate

class EventViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = EventRepository(AppDatabase.get(app).eventDao())

    val events: StateFlow<List<Event>> = repo.events.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    fun addOrUpdate(event: Event) = viewModelScope.launch { repo.upsert(event) }
    fun delete(event: Event) = viewModelScope.launch { repo.delete(event) }

    fun importFromContacts(onDone: (Int) -> Unit) = viewModelScope.launch {
        val imported = ContactsImporter.import(getApplication<Application>().contentResolver)
        val existing = events.value.filter { it.isFromContacts }.mapNotNull { it.contactId }.toSet()
        val filtered = imported.filter { it.contactId !in existing }
        repo.upsertAll(filtered)
        onDone(filtered.size)
    }
}
