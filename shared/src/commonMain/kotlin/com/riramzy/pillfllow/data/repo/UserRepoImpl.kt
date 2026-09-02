package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.data.remote.dto.UserDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.currentTimeMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserRepoImpl(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
): UserRepo {
    override fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    override suspend fun getAllUsersOnce(): List<UserEntity> = userDao.getAllUsersOnce()

    override fun getUserById(id: String): Flow<UserEntity?> {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                firestore
                    .collection("users")
                    .document(id)
                    .snapshots.collect { snapshot ->
                        if (snapshot.exists) {
                            val remoteUser = snapshot.data<UserDto>().toEntity()
                            userDao.insertUser(remoteUser)
                        }
                    }
            }
        }
        return userDao.getUserById(id)
    }

    override suspend fun getUserByIdOnce(id: String): UserEntity? {
        val remoteUser = runCatching {
            val doc = firestore
                .collection("users")
                .document(id)
                .get()

            if (doc.exists) doc.data<UserDto>().toEntity() else null
        }.getOrNull()

        remoteUser?.let { userDao.insertUser(it) }
        return remoteUser ?: userDao.getUserByIdOnce(id)
    }

    override suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)

        runCatching {
            val dto = user.toDto(updatedAt = currentTimeMillis())
            firestore
                .collection("users")
                .document(user.id)
                .set(dto)
        }
    }

    override suspend fun updateUser(user: UserEntity): Int {
        val count = userDao.updateUser(user)

        runCatching {
            val dto = user.toDto(updatedAt = currentTimeMillis())
            firestore
                .collection("users")
                .document(user.id)
                .set(dto, merge = true)
        }
        return count
    }
}