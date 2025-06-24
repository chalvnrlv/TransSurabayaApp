package com.example.transsurabayaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.transsurabayaapp.data.TransSurabayaRepository

class TransSurabayaViewModelFactory(
    private val repository: TransSurabayaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransSurabayaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransSurabayaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}