package com.riramzy.pillfllow.domain.repo

import com.riramzy.pillfllow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepo {
    fun getAllUsers(): Flow<List<UserEntity>>
    suspend fun getAllUsersOnce(): List<UserEntity>
    fun getUserById(id: String): Flow<UserEntity?>
    suspend fun getUserByIdOnce(id: String): UserEntity?
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity): Int
}