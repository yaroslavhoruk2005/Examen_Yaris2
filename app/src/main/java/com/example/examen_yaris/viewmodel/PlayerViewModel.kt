package com.example.examen_yaris.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.examen_yaris.data.Player
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PlayerViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val jugadoresCollection = firestore.collection("Jugadores")

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPlayers()
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            try {
                jugadoresCollection.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val playersList = snapshot?.documents?.mapNotNull { doc ->
                        Player(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            numero = doc.getLong("numero")?.toInt() ?: 0,
                            nacionalidad = doc.getString("nacionalidad") ?: "",
                            posicion = doc.getString("posicion") ?: "",
                            imagen = doc.getString("imagen") ?: ""
                        )
                    } ?: emptyList()

                    _players.value = playersList.sortedBy { it.numero }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addPlayer(player: Player, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val playerData = hashMapOf(
                    "nombre" to player.nombre,
                    "numero" to player.numero,
                    "nacionalidad" to player.nacionalidad,
                    "posicion" to player.posicion,
                    "imagen" to player.imagen
                )
                jugadoresCollection.add(playerData).await()
                _isLoading.value = false
                onResult(true, null)
            } catch (e: Exception) {
                _isLoading.value = false
                onResult(false, e.message)
            }
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            try {
                jugadoresCollection.document(player.id).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
