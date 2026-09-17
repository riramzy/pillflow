package com.riramzy.pillfllow.ui.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riramzy.pillfllow.domain.usecase.auth.ObserveCurrentUserUseCase
import com.riramzy.pillfllow.ui.state.splash.SplashAction
import com.riramzy.pillfllow.ui.state.splash.SplashNavEvent
import com.riramzy.pillfllow.ui.state.splash.SplashState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()
    private val _navEvent = MutableStateFlow<SplashNavEvent?>(null)
    val navEvent: StateFlow<SplashNavEvent?> = _navEvent.asStateFlow()


    init {
        onAction(SplashAction.CheckSession)
    }

    fun onAction(action: SplashAction) {
        when (action) {
            SplashAction.CheckSession -> checkSession()
        }
    }

    private fun checkSession() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000.milliseconds)

            val user = observeCurrentUserUseCase.once()

            if (user != null) {
                _state.update { it.copy(isLoading = false) }
                _navEvent.value = SplashNavEvent.NavigateToHome
            } else {
                _state.update { it.copy(isLoading = false) }
                _navEvent.value = SplashNavEvent.NavigateToRoleSelection
            }
        }
    }
}