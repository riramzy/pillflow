package com.riramzy.pillfllow.data.repo

import com.riramzy.pillfllow.data.local.dao.UserDao
import com.riramzy.pillfllow.data.local.entity.UserEntity
import com.riramzy.pillfllow.data.remote.dto.UserDto
import com.riramzy.pillfllow.data.remote.dto.toDto
import com.riramzy.pillfllow.data.remote.dto.toEntity
import com.riramzy.pillfllow.domain.repo.UserRepo
import com.riramzy.pillfllow.utils.currentTimeMillis
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class UserRepoImpl(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
): UserRepo {
    override fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers().distinctUntilChanged()

    override suspend fun getAllUsersOnce(): List<UserEntity> = userDao.getAllUsersOnce()

    override fun getUserById(id: String): Flow<UserEntity?> = channelFlow {
        launch(Dispatchers.IO) {
            runCatching {
                firestore
                    .collection("users")
                    .document(id)
                    .snapshots
                    .collect { snapshot ->
                        if (snapshot.exists) {
                            val remoteUser = snapshot.data<UserDto>().toEntity()
                            userDao.insertUser(remoteUser)
                        }
                    }
            }
        }
        userDao.getUserById(id).distinctUntilChanged().collect { send(it) }
    }

    override suspend fun getUserByIdOnce(id: String): UserEntity? {
        val localUser = userDao.getUserByIdOnce(id)
        if (localUser != null) return localUser
        val remoteUser = runCatching {
            val doc = firestore
                .collection("users")
                .document(id)
                .get()

            if (doc.exists) doc.data<UserDto>().toEntity() else null
        }.getOrNull()
        remoteUser?.let { userDao.insertUser(it) }
        return remoteUser
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

    override suspend fun linkCaregiverAndPatient(caregiverId: String, patientId: String) {
        val caregiver = getUserByIdOnce(caregiverId)
        if (caregiver != null) {
            val updatedIds = (caregiver.pairedPatientIds + patientId).distinct()
            userDao.insertUser(caregiver.copy(pairedPatientIdsString = updatedIds.joinToString(",")))
            runCatching {
                firestore
                    .collection("users")
                    .document(caregiverId)
                    .update(
                        "pairedPatientIds" to updatedIds,
                        "updatedAt" to currentTimeMillis()
                    )
            }
        }

        val patient = getUserByIdOnce(patientId)
        if (patient != null) {
            userDao.insertUser(patient.copy(pairedCaregiverId = caregiverId))
        }
    }

    override suspend fun unlinkCaregiverAndPatient(caregiverId: String, patientId: String) {
        val caregiver = getUserByIdOnce(caregiverId)
        if (caregiver != null) {
            val updatedIds = caregiver.pairedPatientIds.filter { it != patientId }
            userDao.insertUser(caregiver.copy(pairedPatientIdsString = updatedIds.joinToString(",")))
            runCatching {
                firestore
                    .collection("users")
                    .document(caregiverId)
                    .update(
                        "pairedPatientIds" to updatedIds,
                        "updatedAt" to currentTimeMillis()
                    )
            }
        }
        val patient = getUserByIdOnce(patientId)
        if (patient != null) {
            userDao.insertUser(patient.copy(pairedCaregiverId = null))
        }
    }
}