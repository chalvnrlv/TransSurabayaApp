package com.example.transsurabayaapp

import android.app.Application
import com.example.transsurabayaapp.data.TransSurabayaRepository
import com.example.transsurabayaapp.data.local.AppDatabase

class TransSurabayaApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransSurabayaRepository(database) }
}