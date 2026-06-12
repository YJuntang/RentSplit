package com.rentsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rentsplit.data.preferences.UserPreferencesRepository
import com.rentsplit.data.repository.RentSplitRepository
import com.rentsplit.ui.main.MainScreen
import com.rentsplit.ui.theme.RentSplitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var repository: RentSplitRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            val preferences by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = null)
            val households by repository.getAllHouseholds().collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()

            // Self-healing check: if preferences say onboarding is complete but Room has no households, reset onboarding
            LaunchedEffect(preferences, households) {
                if (preferences != null && preferences!!.hasCompletedOnboarding && households != null && households!!.isEmpty()) {
                    preferencesRepository.setHasCompletedOnboarding(false)
                }
            }

            val darkTheme = when (preferences?.appTheme) {
                "DARK"  -> true
                "LIGHT" -> false
                else    -> systemDark  // "SYSTEM" or null (initial load) follows device
            }

            RentSplitTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (preferences != null) {
                        if (preferences!!.hasCompletedOnboarding) {
                            MainScreen()
                        } else {
                            com.rentsplit.ui.onboarding.OnboardingScreen(
                                onComplete = { /* Recomposed automatically when preferences update */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
