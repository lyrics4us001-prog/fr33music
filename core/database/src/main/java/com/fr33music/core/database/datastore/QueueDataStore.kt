package com.fr33music.core.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fr33music.core.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fr33music_queue_prefs")

data class PersistedQueueState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val positionMs: Long = 0L,
    val isShuffle: Boolean = false
)

@Singleton
class QueueDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_QUEUE = stringPreferencesKey("queue_json")
        private val KEY_INDEX = intPreferencesKey("current_index")
        private val KEY_POSITION = longPreferencesKey("position_ms")
        private val KEY_SHUFFLE = booleanPreferencesKey("is_shuffle")
    }

    val queueState: Flow<PersistedQueueState> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val queueJson = prefs[KEY_QUEUE]
            val queue = if (!queueJson.isNullOrBlank()) {
                try {
                    json.decodeFromString<List<Track>>(queueJson)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
            val index = prefs[KEY_INDEX] ?: 0
            val position = prefs[KEY_POSITION] ?: 0L
            val shuffle = prefs[KEY_SHUFFLE] ?: false

            PersistedQueueState(
                queue = queue,
                currentIndex = index.coerceIn(0, (queue.size - 1).coerceAtLeast(0)),
                positionMs = position,
                isShuffle = shuffle
            )
        }

    suspend fun getQueueStateSync(): PersistedQueueState {
        return queueState.first()
    }

    suspend fun saveQueue(queue: List<Track>, currentIndex: Int, isShuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUEUE] = json.encodeToString(queue)
            prefs[KEY_INDEX] = currentIndex
            prefs[KEY_SHUFFLE] = isShuffle
        }
    }

    suspend fun savePlaybackPosition(positionMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POSITION] = positionMs
        }
    }

    suspend fun saveCurrentIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INDEX] = index
        }
    }

    suspend fun saveShuffle(isShuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHUFFLE] = isShuffle
        }
    }

    suspend fun clearQueue() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_QUEUE)
            prefs.remove(KEY_INDEX)
            prefs.remove(KEY_POSITION)
        }
    }
}
