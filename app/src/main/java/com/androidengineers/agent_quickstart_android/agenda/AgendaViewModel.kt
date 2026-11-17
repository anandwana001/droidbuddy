package com.androidengineers.agent_quickstart_android.agenda

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AgendaState(
    val loading: Boolean = true,
    val agenda: ConferenceAgenda? = null,
    val saved: Set<String> = emptySet(),
    val error: String? = null,
)

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("droidbuddy.agenda", 0)
    private val _state =
        MutableStateFlow(
            AgendaState(saved = preferences.getStringSet("saved", emptySet()).orEmpty().toSet())
        )
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                    withContext(Dispatchers.IO) {
                        getApplication<Application>()
                            .assets
                            .open("agenda.json")
                            .bufferedReader()
                            .use { reader -> Gson().fromJson(reader, ConferenceAgenda::class.java) }
                    }
                }
                .onSuccess { agenda -> _state.update { it.copy(loading = false, agenda = agenda) } }
                .onFailure {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = "Couldn’t load the bundled agenda. Try again.",
                        )
                    }
                }
        }
    }

    fun toggleSaved(id: String) {
        _state.update { state ->
            val saved = if (id in state.saved) state.saved - id else state.saved + id
            preferences.edit().putStringSet("saved", saved).apply()
            state.copy(saved = saved)
        }
    }
}
