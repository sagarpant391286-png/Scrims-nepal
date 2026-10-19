package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ScrimsRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("1:25539132067:android:3303d3e295d8fbd73ddd16")
                    .setProjectId("gen-lang-client-0410459307")
                    .setApiKey("AIzaSyA1D9EhspZO3EWzET0_pCYUXpi3OI0fEO8")
                    .build()
            )
        }
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ScrimsNepal", appName)
    }

    @Test
    fun `user initial wallet balance invariant is zero`() {
        val repo = ScrimsRepository()
        val user = repo.currentUser.value
        assertEquals("Initial user wallet balance must be 0 NPR", 0, user.walletBalanceNpr)
        assertEquals("Unauthenticated user should not be signed in by default", false, repo.isAuthenticated.value)
    }
}
