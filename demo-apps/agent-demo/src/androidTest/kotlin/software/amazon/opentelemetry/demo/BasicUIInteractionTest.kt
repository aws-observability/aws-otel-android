package software.amazon.opentelemetry.demo

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import software.amazon.opentelemetry.android.demo.agent.MainActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class BasicUIInteractionTest {
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setup() {
        // Launch the activity before each test
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun basicUIFlow() {
        Thread.sleep(1000)
        onView(withText("Go to Second Screen")).perform(click())
        Thread.sleep(1000)
        onView(withText("Return to Main")).perform(click())
        Thread.sleep(40000) // NOTE: Exporters do not flush when app closes!! This is bad!
    }

    @Test
    fun testHttpCall() {
        onView(withText("Go to Second Screen")).perform(click())
        Thread.sleep(5000)
        onView(withText("Http Call")).perform(click())
        Thread.sleep(60000)
    }

}
