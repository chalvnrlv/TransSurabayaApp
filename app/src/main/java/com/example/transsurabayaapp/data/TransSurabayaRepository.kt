package com.example.transsurabayaapp.data

import com.example.transsurabayaapp.data.local.AppDatabase
import com.example.transsurabayaapp.data.local.TicketDao
import com.example.transsurabayaapp.data.local.TicketEntity
import com.example.transsurabayaapp.data.local.UserDao
import com.example.transsurabayaapp.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransSurabayaRepository(database: AppDatabase) {
    private val userDao: UserDao = database.userDao()
    private val ticketDao: TicketDao = database.ticketDao()

    suspend fun registerUser(name: String, email: String, password: String): UserEntity {
        val newUser = UserEntity(name = name, email = email, password = password)
        val id = userDao.insert(newUser).toInt()
        return newUser.copy(id = id)
    }

    suspend fun login(email: String, password: String): UserEntity? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.password == password) {
            user
        } else {
            null
        }
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    suspend fun purchaseTicket(ticket: TicketEntity) {
        ticketDao.insert(ticket)
    }

    fun getTicketsForUser(userId: Int): Flow<List<Ticket>> {
        return ticketDao.getTicketsByUser(userId).map { entities ->
            entities.map { it.toTicket() }
        }
    }

    private fun TicketEntity.toTicket() = Ticket(
        id, routeCode, fromStop, toStop, price, purchaseTime, isUsed, isFree
    )

    fun UserEntity.toUserProfile() = UserProfile(
        id = this.id,
        name = this.name,
        email = this.email,
        password = this.password,
        totalTrips = this.totalTrips,
        freeRideCount = this.freeRideCount
    )
}