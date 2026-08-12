package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import kotlinx.coroutines.flow.Flow

class UserRepoImpl(
    private val userDao: UserDao
): UserRepo {
    override fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    override suspend fun getAllUsersOnce(): List<UserEntity> = userDao.getAllUsersOnce()
    override fun getUserById(id: String): Flow<UserEntity?> = userDao.getUserById(id)
    override suspend fun getUserByIdOnce(id: String): UserEntity? = userDao.getUserByIdOnce(id)
    override suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    override suspend fun updateUser(user: UserEntity): Int = userDao.updateUser(user)
}