package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ScrimsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ScrimsNepal", appName)
    }

    @Test
    fun `scrims repository loads active tournaments`() {
        val repo = ScrimsRepository()
        val scrims = repo.scrims.value
        assertTrue("Scrims list should not be empty", scrims.isNotEmpty())
        assertNotNull("First scrim should exist", scrims.firstOrNull())
    }
}
