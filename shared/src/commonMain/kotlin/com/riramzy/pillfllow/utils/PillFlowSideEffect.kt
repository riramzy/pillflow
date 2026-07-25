package com.riramzy.pillfllow.utils

sealed interface PillFlowSideEffect {
    data class ShowToast(val message: String) : PillFlowSideEffect
    data object TriggerHapticDispensed : PillFlowSideEffect
}