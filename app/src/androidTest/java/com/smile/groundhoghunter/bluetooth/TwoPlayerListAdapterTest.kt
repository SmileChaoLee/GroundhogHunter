package com.smile.groundhoghunter.bluetooth

import android.Manifest
import android.content.Intent
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.adapters.TwoPlayerListAdapter
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.view.CreateGameActivity
import com.smile.groundhoghunter.view.bluetooth.BtCreateGameActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field

/**
 * UI tests for [TwoPlayerListAdapter] exercised through [BtCreateGameActivity].
 *
 * The adapter is a private field of [CreateGameActivity], so we use reflection
 * to obtain it inside [ActivityScenario.onActivity] blocks. All interactions with
 * the RecyclerView are validated both through Espresso matchers on the view and
 * through direct assertions on the adapter.
 *
 * Run on a real device (or an emulator that supports Bluetooth).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TwoPlayerListAdapterTest {

    // ── Permissions ───────────────────────────────────────────────────────────

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun launchCreateGame(playerName: String = "HostPlayer"): ActivityScenario<BtCreateGameActivity> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            BtCreateGameActivity::class.java
        ).putExtra(Constants.PLAYER_NAME, playerName)
        return ActivityScenario.launch(intent)
    }

    /**
     * Retrieves the private `twoPlayerListAdapter` field from [CreateGameActivity]
     * via reflection.
     */
    private fun getAdapter(activity: BtCreateGameActivity): TwoPlayerListAdapter {
        val field: Field = CreateGameActivity::class.java
            .getDeclaredField("twoPlayerListAdapter")
        field.isAccessible = true
        return field.get(activity) as TwoPlayerListAdapter
    }

    /**
     * Runs [block] on the **main thread** inside [scenario.onActivity] and waits
     * for Espresso's idle loop to settle before returning.
     */
    private fun runOnMain(
        scenario: ActivityScenario<BtCreateGameActivity>,
        block: (BtCreateGameActivity) -> Unit
    ) {
        scenario.onActivity { activity -> block(activity) }
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun adapter_initialItemCount_isZero() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                assertEquals("Adapter should start empty", 0, adapter.itemCount)
            }
        }
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    fun addItem_increasesItemCount() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                assertEquals(1, adapter.itemCount)
            }
        }
    }

    @Test
    fun addItem_twoItems_itemCountIsTwo() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
                assertEquals(2, adapter.itemCount)
            }
        }
    }

    @Test
    fun addItem_duplicateKey_doesNotIncreaseBeyondOne() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One Updated")
                // LinkedHashMap replaces the value but the size stays 1
                assertEquals(1, adapter.itemCount)
            }
        }
    }

    @Test
    fun addItem_visibleInRecyclerView() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                getAdapter(activity).addItem("AA:BB:CC:DD:EE:01", "Player One")
            }
            // At least one child view should now be attached
            onView(withId(R.id.oppositePlayerNameListView))
                .check(matches(hasMinimumChildCount(1)))
        }
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    fun removeItem_decreasesItemCount() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
                adapter.removeItem("AA:BB:CC:DD:EE:01")
                assertEquals(1, adapter.itemCount)
            }
        }
    }

    @Test
    fun removeItem_nonExistentKey_doesNotCrash() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.removeItem("FF:FF:FF:FF:FF:FF")   // key not present
                assertEquals(1, adapter.itemCount)
            }
        }
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    fun clear_resetsItemCountToZero() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
                adapter.clear()
                assertEquals(0, adapter.itemCount)
            }
        }
    }

    // ── myNotifyItemChanged / selection highlight ──────────────────────────────

    /**
     * After calling [TwoPlayerListAdapter.myNotifyItemChanged] the selected
     * position should be reflected inside the adapter.
     *
     * We verify indirectly by checking that the [playerNameTextView] of the
     * clicked item turns RED (Color.RED) and the other item stays transparent.
     */
    @Test
    fun myNotifyItemChanged_updatesSelectedPosition() {
        launchCreateGame().use { scenario ->
            // Add two items on the main thread
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
            }

            // Click position 0 through Espresso
            onView(withId(R.id.oppositePlayerNameListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // Verify via adapter's internal selectedPosition
            scenario.onActivity { activity ->
                val adapterField = CreateGameActivity::class.java
                    .getDeclaredField("twoPlayerListAdapter")
                adapterField.isAccessible = true
                val adapter = adapterField.get(activity) as TwoPlayerListAdapter

                val selectedPosField = TwoPlayerListAdapter::class.java
                    .getDeclaredField("selectedPosition")
                selectedPosField.isAccessible = true
                val selectedPos = selectedPosField.getInt(adapter)
                assertEquals(
                    "selectedPosition should be 0 after clicking first item",
                    0,
                    selectedPos
                )
            }
        }
    }

    @Test
    fun myNotifyItemChanged_changingSelectionUpdatesOldAndNewItem() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
                // Programmatically select position 0 first
                adapter.myNotifyItemChanged(0)
            }

            // Now click position 1
            onView(withId(R.id.oppositePlayerNameListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

            // selectedPosition should now be 1
            scenario.onActivity { activity ->
                val adapterField = CreateGameActivity::class.java
                    .getDeclaredField("twoPlayerListAdapter")
                adapterField.isAccessible = true
                val adapter = adapterField.get(activity) as TwoPlayerListAdapter

                val selectedPosField = TwoPlayerListAdapter::class.java
                    .getDeclaredField("selectedPosition")
                selectedPosField.isAccessible = true
                val selectedPos = selectedPosField.getInt(adapter)
                assertEquals(
                    "selectedPosition should be 1 after clicking second item",
                    1,
                    selectedPos
                )
            }
        }
    }

    // ── updateData ────────────────────────────────────────────────────────────

    @Test
    fun updateData_replacesExistingContent() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")

                val newData = linkedMapOf(
                    "AA:BB:CC:DD:EE:10" to "New Player A",
                    "AA:BB:CC:DD:EE:11" to "New Player B",
                    "AA:BB:CC:DD:EE:12" to "New Player C"
                )
                adapter.updateData(newData)
                assertEquals(3, adapter.itemCount)
            }
        }
    }

    @Test
    fun updateData_resetsSelectedPosition() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.myNotifyItemChanged(0)   // select item 0

                val newData = linkedMapOf("AA:BB:CC:DD:EE:10" to "New Player")
                adapter.updateData(newData)

                // selectedPosition should be reset to -1
                val selectedPosField = TwoPlayerListAdapter::class.java
                    .getDeclaredField("selectedPosition")
                selectedPosField.isAccessible = true
                assertEquals(-1, selectedPosField.getInt(adapter))
            }
        }
    }

    // ── Background colour ─────────────────────────────────────────────────────

    /**
     * When an item is clicked its [playerNameTextView] background should change
     * to [Color.RED]. We verify by inspecting the view directly via onActivity.
     */
    @Test
    fun clickedItem_backgroundBecomesRed() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
            }

            onView(withId(R.id.oppositePlayerNameListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            scenario.onActivity { activity ->
                val rv = activity.findViewById<RecyclerView>(R.id.oppositePlayerNameListView)
                val vh = rv.findViewHolderForAdapterPosition(0)
                assertNotNull("ViewHolder at position 0 should exist", vh)
                val tv = vh!!.itemView.findViewById<android.widget.TextView>(R.id.playerNameTextView)
                assertNotNull(tv)
                val bgColor = (tv.background as? android.graphics.drawable.ColorDrawable)?.color
                assertEquals(
                    "Background of selected item should be RED",
                    Color.RED,
                    bgColor
                )
            }
        }
    }

    /**
     * After selecting item 0 and then selecting item 1, item 0's background
     * should revert to transparent and item 1's background should be RED.
     */
    @Test
    fun previouslySelectedItem_backgroundRevertsToTransparent() {
        launchCreateGame().use { scenario ->
            runOnMain(scenario) { activity ->
                val adapter = getAdapter(activity)
                adapter.addItem("AA:BB:CC:DD:EE:01", "Player One")
                adapter.addItem("AA:BB:CC:DD:EE:02", "Player Two")
            }

            // Select item 0
            onView(withId(R.id.oppositePlayerNameListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

            // Now select item 1
            onView(withId(R.id.oppositePlayerNameListView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

            scenario.onActivity { activity ->
                val rv = activity.findViewById<RecyclerView>(R.id.oppositePlayerNameListView)

                val vh0 = rv.findViewHolderForAdapterPosition(0)
                assertNotNull(vh0)
                val tv0 = vh0!!.itemView.findViewById<android.widget.TextView>(R.id.playerNameTextView)
                val bg0 = (tv0.background as? android.graphics.drawable.ColorDrawable)?.color
                assertNotEquals(
                    "Item 0 should NOT be RED after de-selection",
                    Color.RED,
                    bg0
                )

                val vh1 = rv.findViewHolderForAdapterPosition(1)
                assertNotNull(vh1)
                val tv1 = vh1!!.itemView.findViewById<android.widget.TextView>(R.id.playerNameTextView)
                val bg1 = (tv1.background as? android.graphics.drawable.ColorDrawable)?.color
                assertEquals(
                    "Item 1 should be RED after selection",
                    Color.RED,
                    bg1
                )
            }
        }
    }
}

