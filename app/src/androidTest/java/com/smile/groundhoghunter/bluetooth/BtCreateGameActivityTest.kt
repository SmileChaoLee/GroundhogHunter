package com.smile.groundhoghunter.bluetooth

import android.Manifest
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.view.bluetooth.BtCreateGameActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [BtCreateGameActivity] — the "host" (Create Game) side of the
 * Bluetooth two-player flow.
 *
 * Tests verify the initial UI state and basic button interactions. They do NOT
 * test actual Bluetooth connectivity because that requires hardware pairing, which
 * belongs to integration/end-to-end tests.
 *
 * Run on a real device (or an emulator that supports Bluetooth).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BtCreateGameActivityTest {

    // ── Permissions ───────────────────────────────────────────────────────────

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Launches [BtCreateGameActivity] with [playerName] passed as an Intent extra. */
    private fun launch(playerName: String): ActivityScenario<BtCreateGameActivity> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            BtCreateGameActivity::class.java
        ).putExtra(Constants.PLAYER_NAME, playerName)
        return ActivityScenario.launch(intent)
    }

    // ── Launch ────────────────────────────────────────────────────────────────

    @Test
    fun activityLaunchesSuccessfully() {
        launch("HostPlayer").use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity should not be null", activity)
            }
        }
    }

    // ── Title ─────────────────────────────────────────────────────────────────

    @Test
    fun titleTextView_showsCreateBluetoothGame() {
        launch("HostPlayer").use {
            onView(withId(R.id.createGameTitleTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.createBluetoothGameString)))
        }
    }

    // ── Player name ───────────────────────────────────────────────────────────

    @Test
    fun playerNameTextView_showsNameFromIntent() {
        val name = "HostPlayer"
        launch(name).use {
            onView(withId(R.id.playerNameTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(name)))
        }
    }

    @Test
    fun playerNameStringLabel_isDisplayed() {
        launch("HostPlayer").use {
            onView(withId(R.id.playerNameStringTextView))
                .check(matches(isDisplayed()))
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    @Test
    fun oppositePlayerRecyclerView_isDisplayed() {
        launch("HostPlayer").use {
            onView(withId(R.id.oppositePlayerNameListView))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun oppositePlayerRecyclerView_initiallyEmpty() {
        launch("HostPlayer").use { scenario ->
            scenario.onActivity { activity ->
                val rv = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(
                    R.id.oppositePlayerNameListView
                )
                assertNotNull(rv)
                assertEquals(
                    "RecyclerView should be empty on launch",
                    0,
                    rv.adapter?.itemCount ?: 0
                )
            }
        }
    }

    // ── Buttons ───────────────────────────────────────────────────────────────

    @Test
    fun startCreateGameButton_isDisplayed() {
        launch("HostPlayer").use {
            onView(withId(R.id.startCreateGameButton))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun refreshCreateGameButton_isDisplayed() {
        launch("HostPlayer").use {
            onView(withId(R.id.refreshCreateGameButton))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancelCreateGameButton_isDisplayed() {
        launch("HostPlayer").use {
            onView(withId(R.id.cancelCreateGameButton))
                .check(matches(isDisplayed()))
        }
    }

    // ── Cancel behaviour ──────────────────────────────────────────────────────

    @Test
    fun cancelButton_click_finishesActivity() {
        launch("HostPlayer").use { scenario ->
            onView(withId(R.id.cancelCreateGameButton)).perform(click())
            assertEquals(
                "Activity should be destroyed after cancel",
                Lifecycle.State.DESTROYED,
                scenario.state
            )
        }
    }

    // ── Start button – validation ─────────────────────────────────────────────

    /**
     * Clicking Start when the player name is empty should show
     * "Player name cannot be empty." in the toast TextView.
     */
    @Test
    fun startButton_withEmptyPlayerName_showsValidationMessage() {
        launch("").use {
            onView(withId(R.id.startCreateGameButton)).perform(click())
            onView(withId(R.id.toastMessageTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.playerNameCannotBeEmptyString)))
        }
    }

    /**
     * Clicking Start when there is no opposite player selected should show
     * "No opposite player" in the toast TextView.
     */
    @Test
    fun startButton_withNoOpponentSelected_showsValidationMessage() {
        launch("HostPlayer").use {
            onView(withId(R.id.startCreateGameButton)).perform(click())
            onView(withId(R.id.toastMessageTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.noOppositePlayerString)))
        }
    }

    // ── Toast message view ────────────────────────────────────────────────────

    @Test
    fun toastMessageTextView_existsInLayout() {
        launch("HostPlayer").use {
            onView(withId(R.id.toastMessageTextView))
                .check(matches(withId(R.id.toastMessageTextView)))
        }
    }
}

