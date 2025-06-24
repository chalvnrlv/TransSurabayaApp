package com.example.transsurabayaapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TicketEntity(
    @PrimaryKey val id: String,
    val userId: Int,
    val routeCode: String,
    val fromStop: String,
    val toStop: String,
    val price: Int,
    val purchaseTime: Long,
    val isUsed: Boolean = false,
    val isFree: Boolean = false
)