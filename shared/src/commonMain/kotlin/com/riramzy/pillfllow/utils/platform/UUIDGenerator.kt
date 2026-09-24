package com.riramzy.pillfllow.utils.platform

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun randomUUID(): String = Uuid.random().toString()