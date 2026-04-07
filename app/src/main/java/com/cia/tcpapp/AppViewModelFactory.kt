package com.cia.tcpapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Permite crear el ViewModel con acceso al Application context.
class AppViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TcpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TcpViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
