package com.example.transsurabayaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: TicketEntity)

    @Update
    suspend fun update(ticket: TicketEntity)

    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY purchaseTime DESC")
    fun getTicketsByUser(userId: Int): Flow<List<TicketEntity>>

    @Query("DELETE FROM tickets WHERE id = :ticketId")
    suspend fun deleteTicket(ticketId: String)
}