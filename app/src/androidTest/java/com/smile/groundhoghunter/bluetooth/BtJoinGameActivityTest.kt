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
import com.smile.groundhoghunter.view.bluetooth.BtJoinGameActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [BtJoinGameActivity] — the "client" (Join Game) side of the
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
class BtJoinGameActivityTest {

    // ── Permissions ───────────────────────────────────────────────────────────

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Launches [BtJoinGameActivity] with [playerName] passed as an Intent extra. */
    private fun launch(playerName: String): ActivityScenario<BtJoinGameActivity> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            BtJoinGameActivity::class.java
        ).putExtra(Constants.PLAYER_NAME, playerName)
        return ActivityScenario.launch(intent)
    }

    // ── Launch ────────────────────────────────────────────────────────────────

    @Test
    fun activityLaunchesSuccessfully() {
        launch("ClientPlayer").use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Activity should not be null", activity)
            }
        }
    }

    // ── Title ─────────────────────────────────────────────────────────────────

    @Test
    fun titleTextView_showsJoinBluetoothGame() {
        launch("ClientPlayer").use {
            onView(withId(R.id.joinGameTitleTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.joinBluetoothGameString)))
        }
    }

    // ── Player name ───────────────────────────────────────────────────────────

    @Test
    fun playerNameTextView_showsNameFromIntent() {
        val name = "ClientPlayer"
        launch(name).use {
            onView(withId(R.id.playerNameTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(name)))
        }
    }

    @Test
    fun playerNameStringLabel_isDisplayed() {
        launch("ClientPlayer").use {
            onView(withId(R.id.playerNameStringTextView))
                .check(matches(isDisplayed()))
        }
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    @Test
    fun oppositePlayerRecyclerView_isDisplayed() {
        launch("ClientPlayer").use {
            onView(withId(R.id.oppositePlayerNameListView))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun oppositePlayerRecyclerView_initiallyEmpty() {
        launch("ClientPlayer").use { scenario ->
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
    fun refreshJoinGameButton_isDisplayed() {
        launch("ClientPlayer").use {
            onView(withId(R.id.refreshJoinGameButton))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancelJoinGameButton_isDisplayed() {
        launch("ClientPlayer").use {
            onView(withId(R.id.cancelJoinGameButton))
                .check(matches(isDisplayed()))
        }
    }

    // ── Cancel behaviour ──────────────────────────────────────────────────────

    @Test
    fun cancelButton_click_finishesActivity() {
        launch("ClientPlayer").use { scenario ->
            onView(withId(R.id.cancelJoinGameButton)).perform(click())
            assertEquals(
                "Activity should be destroyed after cancel",
                Lifecycle.State.DESTROYED,
                scenario.state
            )
        }
    }

    // ── Discovering message shown on launch ───────────────────────────────────

    /**
     * When the activity starts it immediately calls [startDiscovery] which shows
     * the "Finding Players" message in the toast TextView.
     */
    @Test
    fun onLaunch_discoveringMessageIsShown() {
        launch("ClientPlayer").use {
            onView(withId(R.id.toastMessageTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.discoverPlayerString)))
        }
    }

    // ── Refresh button ────────────────────────────────────────────────────────

    /**
     * Tapping Refresh restarts discovery; the "Finding Players" message should
     * (re)appear in the toast TextView.
     */
    @Test
    fun refreshButton_click_showsDiscoveringMessage() {
        launch("ClientPlayer").use {
            onView(withId(R.id.refreshJoinGameButton)).perform(click())
            onView(withId(R.id.toastMessageTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(R.string.discoverPlayerString)))
        }
    }

    // ── Toast message view ────────────────────────────────────────────────────

    @Test
    fun toastMessageTextView_existsInLayout() {
        launch("ClientPlayer").use {
            onView(withId(R.id.toastMessageTextView))
                .check(matches(withId(R.id.toastMessageTextView)))
        }
    }

    // ── Empty player name ─────────────────────────────────────────────────────

    @Test
    fun activityLaunches_withEmptyPlayerName() {
        launch("").use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
            // Player name TextView should show an empty string
            onView(withId(R.id.playerNameTextView))
                .check(matches(withText("")))
        }
    }
}

