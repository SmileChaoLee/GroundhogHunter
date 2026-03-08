package com.smile.groundhoghunter.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smile.groundhoghunter.GHogHunterApp
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.services.GlobalTop10Service
import com.smile.groundhoghunter.services.LocalTop10Service
import com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment
import com.smile.smilelibraries.customized_button.SmileImageButton
import com.smile.smilelibraries.google_ads_util.AdMobInterstitial
import com.smile.smilelibraries.models.ExitAppTimer
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.scoresqlite.ScoreSQLite
import com.smile.smilelibraries.show_banner_ads.SetBannerAdView
import com.smile.smilelibraries.show_interstitial_ads.ShowInterstitial
import com.smile.smilelibraries.utilities.FontAndBitmapUtil
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

open class GroundhogActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GroundhogAct"
        private const val AD_PROVIDER = 0
        private const val LOAD_DIALOG_TAG = "LoadingDialogTag"
        @JvmField
        var GamePause: Boolean = false
        @JvmField
        val activityLocker: Object = Object()
    }

    private var rowNum = 0
    private var colNum = 0
    private var highestScore = 0
    private var isShowingLoadingMessage = false
    private var gameType = 0
    protected var textFontSize: Float = 0f
    protected var fontScale: Float = 0f
    protected var toastTextSize: Float = 0f
    private lateinit var loadingString: String
    private lateinit var soundOnOffImageView: ImageView
    private lateinit var highScoreTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var hitNumTextView: TextView
    private lateinit var settingButton: SmileImageButton
    private lateinit var top10Button: SmileImageButton
    private lateinit var globalTop10Button: SmileImageButton
    private var loadingDialog: AlertDialogFragment? = null
    private lateinit var bReceiver: BroadcastReceiver
    protected lateinit var startGameButton: SmileImageButton
    protected lateinit var pauseGameButton: SmileImageButton
    protected lateinit var resumeGameButton: SmileImageButton
    protected lateinit var newGameButton: SmileImageButton
    protected lateinit var quitGameButton: SmileImageButton
    @JvmField
    protected var gameView: GameView? = null
    @JvmField
    protected var selectedIoFuncThread: IoFunctionThread? = null
    private lateinit var settingLauncher: ActivityResultLauncher<Intent>
    private lateinit var otherLauncher: ActivityResultLauncher<Intent>
    private lateinit var scoreDB: ScoreSQLite
    private lateinit var interstitialAd: ShowInterstitial

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate(")

        scoreDB = ScoreSQLite(this@GroundhogActivity, Constants.DATABASE_NAME)

        val adMobInterstitialID = "ca-app-pub-8354869049759576/6595392508"
        val adMobInterstitial = AdMobInterstitial(this@GroundhogActivity, adMobInterstitialID)
        adMobInterstitial.loadAd() // load first ad
        val adHandler = Handler(Looper.getMainLooper())
        val adRunnable = Runnable {
            adHandler.removeCallbacksAndMessages(null)
            adMobInterstitial.loadAd() // load first google ad
        }
        adHandler.postDelayed(adRunnable, 1000)
        interstitialAd = ShowInterstitial(this@GroundhogActivity, null, adMobInterstitial)

        Log.d(TAG, "onCreate.savedInstanceState = $savedInstanceState")

        selectedIoFuncThread = GHogHunterApp.selectedIoFuncThread
        if (selectedIoFuncThread == null) {
            Log.d(TAG, "selectedIoFunctionThread is null.")
        }

        highestScore = scoreDB.readHighestScore()
        loadingString = getString(R.string.loadingString)
        textFontSize = ScreenUtil.getPxTextFontSizeNeeded(this@GroundhogActivity)
        fontScale = ScreenUtil.getPxFontScale(this@GroundhogActivity)
        toastTextSize = textFontSize * 0.8f
        isShowingLoadingMessage = false
        val callingIntent = intent
        gameType = callingIntent.getIntExtra(Constants.GAME_TYPE,
            Constants.GAME_BY_SINGLE_PLAY)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_groundhog)

        GamePause = false

        val darkRed = ContextCompat.getColor(this@GroundhogActivity, R.color.darkRed)
        val settingString = getString(R.string.settingString)
        settingButton = findViewById(R.id.settingButton)
        val settingBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.setting_button,
            settingString,
            Color.BLUE
        )
        settingButton.setImageBitmap(settingBitmap)
        settingButton.setOnClickListener { view: View? ->
            gameView?.let { gv ->
                if ((gv.getRunningStatus() != 1) || (GameView.gViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons()
                    val intent = Intent(this@GroundhogActivity,
                        SettingActivity::class.java)
                    val extras = Bundle()
                    extras.putBoolean("HasSound", gameView!!.hasSound())
                    intent.putExtras(extras)
                    settingLauncher.launch(intent)
                }
            }
        }

        // for top 10 button
        val localTop10String = getString(R.string.localTop10String)
        top10Button = findViewById(R.id.top10Button)
        val top10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.top10_button,
            localTop10String,
            darkRed
        )
        top10Button.setImageBitmap(top10Bitmap)
        top10Button.setOnClickListener { view: View? ->
            gameView?.let { gv ->
                if ((gv.getRunningStatus() != 1) || (GameView.gViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons()
                    getLocalTop10ScoreList() // removed for testing on 2019-05-07
                }
            }
        }

        // for top 10 button
        val globalTop10String = getString(R.string.globalTop10String)
        globalTop10Button = findViewById(R.id.globalTop10Button)
        val globalTop10Bitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.global_top10_button,
            globalTop10String,
            darkRed
        )
        globalTop10Button.setImageBitmap(globalTop10Bitmap)
        globalTop10Button.setOnClickListener { view: View? ->
            gameView?.let { gv ->
                if ((gv.getRunningStatus() != 1) || (GameView.gViewPause)) {
                    // client is not playing game or not pause status
                    disableAllButtons()
                    getGlobalTop10ScoreList()
                }
            }
        }

        // score layout
        val gameStatusTitleTextView = findViewById<TextView>(R.id.gameStatusTitle)
        ScreenUtil.resizeTextSize(gameStatusTitleTextView, textFontSize)
        soundOnOffImageView = findViewById(R.id.soundOnOffImageView)

        val highScoreTitleTextView = findViewById<TextView>(R.id.highestScoreTitle)
        ScreenUtil.resizeTextSize(highScoreTitleTextView, textFontSize)
        highScoreTextView = findViewById(R.id.highestScoreText)
        ScreenUtil.resizeTextSize(highScoreTextView, textFontSize)
        highScoreTextView.text = highestScore.toString()

        val timerTitleTextView = findViewById<TextView>(R.id.timerTitle)
        ScreenUtil.resizeTextSize(timerTitleTextView, textFontSize)
        timerTextView = findViewById(R.id.timerText)
        ScreenUtil.resizeTextSize(timerTextView, textFontSize)
        timerTextView.text = GameView.TIMER_INTERVAL.toString()

        val scoreTitleTextView = findViewById<TextView>(R.id.scoreTitle)
        ScreenUtil.resizeTextSize(scoreTitleTextView, textFontSize)
        scoreTextView = findViewById(R.id.scoreText)
        ScreenUtil.resizeTextSize(scoreTextView, textFontSize)
        // scoreTextView.text = "0000"

        val hitNumTitleTextView = findViewById<TextView>(R.id.num_hit_Title)
        ScreenUtil.resizeTextSize(hitNumTitleTextView, textFontSize)
        hitNumTextView = findViewById(R.id.num_hit_Text)
        ScreenUtil.resizeTextSize(hitNumTextView, textFontSize)
        // hitNumTextView.text = "0000"

        val gameLinearLayout = findViewById<LinearLayout>(R.id.gameViewAreaLinearLayout)
        val gameFrameLayout = findViewById<FrameLayout>(R.id.gameViewAreaFrameLayout)
        // game view area
        val gameGrid = findViewById<GridLayout>(R.id.gameAreaGridLayout)
        rowNum = gameGrid.rowCount
        colNum = gameGrid.columnCount
        for (i in 0..<rowNum) {
            val rowSpec = GridLayout.spec(i, 1, 1f)
            for (j in 0..<colNum) {
                val colSpec = GridLayout.spec(j, 1, 1f)
                val glP = GridLayout.LayoutParams()
                glP.width = 0
                glP.height = 0
                glP.rowSpec = rowSpec
                glP.columnSpec = colSpec

                val index = rowNum * i + j
                val imageView = ImageView(this@GroundhogActivity)
                imageView.setId(index)
                imageView.isClickable = true
                imageView.setBackgroundResource(R.drawable.groundhog_hole)
                gameGrid.addView(imageView, index, glP)
            }
        }

        val bannerLinearLayout = findViewById<LinearLayout>(R.id.linearlayout_for_ads_in_myActivity)
        if (Constants.ADMOB_BANNER_ID.isNotEmpty()) {
            val myBannerAdView = SetBannerAdView(this@GroundhogActivity, null,
                bannerLinearLayout,
                Constants.ADMOB_BANNER_ID, "")
            myBannerAdView.showBannerAdView(AD_PROVIDER)  // AdMob first
        } else {
            var lp = bannerLinearLayout.layoutParams as ConstraintLayout.LayoutParams
            val tempPercent = lp.matchConstraintPercentHeight
            lp.matchConstraintPercentHeight = 0.0f
            lp = gameLinearLayout.layoutParams as ConstraintLayout.LayoutParams
            lp.matchConstraintPercentHeight += tempPercent
        }

        gameFrameLayout.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // hove to use removeGlobalOnLayoutListener() method after API 16 or is API 16
                    gameFrameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    val frameWidth = gameFrameLayout.width
                    val frameHeight = gameFrameLayout.height
                    gameView = GameView(
                        this@GroundhogActivity,
                        gameType,
                        rowNum,
                        colNum,
                        frameWidth,
                        frameHeight,
                        selectedIoFuncThread
                    )
                    Log.i(TAG, "gameView created.")
                    gameFrameLayout.addView(gameView)
                    soundOnOffImageView.setImageResource(R.drawable.sound_on_image)
                }
            })

        // buttons for start game, new game, quit game
        val startString = getString(R.string.startString)
        val pauseString = getString(R.string.pauseString)
        val resumeString = getString(R.string.resumeString)

        startGameButton = findViewById(R.id.startGameButton)
        val startGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.start_game_button,
            startString,
            Color.BLUE
        )
        startGameButton.apply {
            setImageBitmap(startGameBitmap)
            isClickable = true
            isEnabled = true
            visibility = View.VISIBLE
            setOnClickListener { view: View? -> startGame() }
        }

        pauseGameButton = findViewById(R.id.pauseGameButton)
        val pauseGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.pause_game_button,
            pauseString,
            Color.BLUE
        )
        pauseGameButton.apply {
            setImageBitmap(pauseGameBitmap)
            isClickable = false
            isEnabled = false
            visibility = View.INVISIBLE
            setOnClickListener { view: View? -> pauseGame() }
        }

        resumeGameButton = findViewById(R.id.resumeGameButton)
        val resumeGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.resume_game_button,
            resumeString,
            Color.BLUE
        )
        resumeGameButton.apply {
            setImageBitmap(resumeGameBitmap)
            isClickable = false
            isEnabled = false
            visibility = View.INVISIBLE
            setOnClickListener { view: View? -> resumeGame() }
        }

        val newGameString = getString(R.string.newString)
        newGameButton = findViewById(R.id.newGameButton)
        val newGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.new_game_button,
            newGameString,
            Color.BLUE
        )
        newGameButton.apply {
            setImageBitmap(newGameBitmap)
            setOnClickListener { view: View? -> newGame() }
        }

        val quitGameString = getString(R.string.quitString)
        quitGameButton = findViewById(R.id.quitGameButton)
        val quitGameBitmap = FontAndBitmapUtil.getBitmapFromResourceWithText(
            this@GroundhogActivity,
            R.drawable.quit_game_button,
            quitGameString, Color.YELLOW
        )
        quitGameButton.apply {
            setImageBitmap(quitGameBitmap)
            setOnClickListener { view: View? -> quitGame() }
        }

        bReceiver = GhHunterBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(LocalTop10Service.Action_Name)
        intentFilter.addAction(GlobalTop10Service.Action_Name)
        val localBroadcastManager = LocalBroadcastManager.getInstance(this@GroundhogActivity)
        localBroadcastManager.registerReceiver(bReceiver, intentFilter)

        onBackPressedDispatcher.addCallback(
            this@GroundhogActivity,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    exitApp()
                }
            })

        settingLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            enableAllButtons()
            val resultCode = result.resultCode
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "SettingActivity returned ok.")
                val data = result.data
                if (data == null) return@registerForActivityResult
                val extras = data.extras
                if (extras == null) return@registerForActivityResult
                gameView?.setHasSound(extras.getBoolean("HasSound"))
            } else {
                Log.d(TAG, "SettingActivity returned cancel.")
            }
            // update Main UI for sound
            gameView?.let { gv ->
                if (gv.hasSound()) {
                    soundOnOffImageView.setImageResource(R.drawable.sound_on_image)
                } else {
                    soundOnOffImageView.setImageResource(R.drawable.sound_off_image)
                }
            }
        }
        otherLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult -> enableAllButtons() }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        synchronized(activityLocker) {
            GamePause = false
            activityLocker.notifyAll()
        }
    }

    public override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() is called.")
        synchronized(activityLocker) {
            GamePause = true
        }
    }

    override fun onNewIntent(intent: Intent) {
        Log.d(TAG, "onNewIntent() is called.")
        super.onNewIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() is called.")
    }

    public override fun onDestroy() {
        Log.d(TAG, "onDestroy() is called.")
        super.onDestroy()
        // release and destroy threads and resources before destroy activity
        if (isFinishing) {
            scoreDB.close()
        }
        val localBroadcastManager = LocalBroadcastManager.getInstance(this@GroundhogActivity)
        localBroadcastManager.unregisterReceiver(bReceiver)
        finishApplication()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("IsShowingLoadingMessage", isShowingLoadingMessage)
        super.onSaveInstanceState(outState)
    }

    private fun exitApp() {
        // capture the event of back button when it is pressed
        // change back button behavior
        val exitAppTimer = ExitAppTimer.getInstance(1000) // singleton class
        if (exitAppTimer.canExit()) {
            quitGame()
        } else {
            exitAppTimer.start()
            ScreenUtil.showToast(
                this@GroundhogActivity, getString(R.string.backKeyToExitApp),
                toastTextSize, Toast.LENGTH_SHORT
            )
        }
    }

    // private methods
    private fun finishApplication() {
        Log.d(TAG, "finishApplication");
        // release resources and threads
        gameView?.apply {
            releaseSynchronizations()
            stopThreads()
            releaseResources()
        }
    }

    protected open fun startGame() {
        val gv = gameView ?: return
        gv.startGame()
        startGameButton.apply {
            startGameButton.isEnabled = false
            startGameButton.visibility = View.INVISIBLE
        }
        pauseGameButton.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        resumeGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
    }

    protected open fun pauseGame() {
        val gv = gameView ?: return
        gv.pauseGame()
        startGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
        pauseGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
        resumeGameButton.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
    }

    protected open fun resumeGame() {
        val gv = gameView ?: return
        gv.resumeGame()
        startGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
        pauseGameButton.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        resumeGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
    }

    protected open fun newGame() {
        val gv = gameView ?: return
        gv.newGame()
        startGameButton.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        pauseGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
        resumeGameButton.apply {
            isEnabled = false
            visibility = View.INVISIBLE
        }
    }

    protected open fun quitGame() {
        val gv = gameView ?: return
        // close the socket (BluetoothSocket, Wifi socket, or internet socket)
        gv.newGame() // set to new game (refresh the UI and stop threads) before quiting game
        // int entryPoint = 0; //  no used
        /*
        val showInterstitialAdThread =
            interstitialAd.ShowAdThread(
                object : DismissFunction {
                    override fun backgroundWork() {
                    }
                    override fun executeDismiss() {
                        returnToPrevious()
                    }
                    override fun afterFinished(isAdShown: Boolean) {
                        if (!isAdShown) returnToPrevious()
                    }
                })
        showInterstitialAdThread.startShowAd(AD_PROVIDER)
        */
        interstitialAd.ShowAdThread().startShowAd(AD_PROVIDER)
        returnToPrevious()
    }

    private fun returnToPrevious() {
        val returnIntent = Intent() // used to bundle data
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun getLocalTop10ScoreList() {
        showLoadingMessage()
        val serviceIntent = Intent(this@GroundhogActivity,
                LocalTop10Service::class.java)
        startService(serviceIntent)
    }

    private fun getGlobalTop10ScoreList() {
        // showing loading message
        showLoadingMessage()
        val serviceIntent = Intent(this@GroundhogActivity,
            GlobalTop10Service::class.java)
        startService(serviceIntent)
    }

    fun enableAllButtons() {
        Log.d(TAG, "enableAllButtons");
        startGameButton.isEnabled = true
        pauseGameButton.isEnabled = true
        resumeGameButton.isEnabled = true
        newGameButton.isEnabled = true
        quitGameButton.isEnabled = true
        settingButton.isEnabled = true
        top10Button.isEnabled = true
        globalTop10Button.isEnabled = true
    }

    fun showLoadingMessage() {
        isShowingLoadingMessage = true
        loadingDialog = AlertDialogFragment.newInstance(
            loadingString,
            ScreenUtil.FontSize_Pixel_Type, textFontSize,
            Color.RED, 0, 0, true
        )
        loadingDialog?.show(supportFragmentManager, LOAD_DIALOG_TAG)
    }

    fun dismissShowingLoadingMessage() {
        isShowingLoadingMessage = false
        val lDialog = loadingDialog ?: return
        if (lDialog.isStateSaved()) {
            lDialog.dismissAllowingStateLoss()
        } else {
            lDialog.dismiss()
        }
    }

    // public methods for others to use (GameView)
    fun getHighestScore(): Int {
        return highestScore
    }

    fun getSoundPoolUtil():SoundPoolUtil {
        return SoundPoolUtil(this@GroundhogActivity, R.raw.ouh)
    }

    fun setTextForHighScoreTextView(text: String) {
        highScoreTextView.text = text
    }

    fun setTextForTimerTextView(text: String) {
        timerTextView.text = text
    }

    fun setTextForScoreTextView(text: String) {
        scoreTextView.text = text
    }

    fun setTextForHitNumTextView(text: String) {
        hitNumTextView.text = text
    }

    fun recordScore(score: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isInTop10 = scoreDB.isInTop10(score)
            if (isInTop10) {
                // record the current score
                withContext(Dispatchers.Main) {
                    val et = EditText(this@GroundhogActivity)
                    ScreenUtil.resizeTextSize(et, textFontSize)
                    et.setTextColor(Color.BLUE)
                    et.setHint(getString(R.string.nameString))
                    et.setGravity(Gravity.CENTER)
                    val alertD = AlertDialog.Builder(this@GroundhogActivity).create()
                    alertD.setTitle(null)
                    alertD.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    alertD.setCancelable(false)
                    alertD.setView(et)
                    alertD.setButton(
                        DialogInterface.BUTTON_NEGATIVE,
                        getString(R.string.cancelString)
                    ) { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() }
                    alertD.setButton(
                        DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.submitString)
                    ) { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                        // use thread to add a record to database (remote database on cloud)
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val jsonObject = JSONObject()
                                jsonObject.put(Constants.PLAYER_NAME, et.getText().toString())
                                jsonObject.put(Constants.SCORE, score)
                                jsonObject.put(Constants.GAME_ID, Constants.GROUNDHOG_GAME_ID)
                                PlayerRecordRest.addOneRecord(jsonObject)
                            } catch (ex: Exception) {
                                Log.e(TAG, "recordScore.Exception", ex)
                            }
                            delay(200)
                            scoreDB.addScore(et.getText().toString(), score)
                            scoreDB.deleteAllAfterTop10() // only keep the top 10
                            withContext(Dispatchers.Main) {
                                if (score > highestScore) {
                                    highestScore = score
                                    gameView?.setHighestScore(highestScore)
                                    setTextForHighScoreTextView(highestScore.toString())
                                }
                            }
                        }
                    }
                    alertD.setOnShowListener { dialog: DialogInterface ->
                        setDialogStyle(
                            dialog
                        )
                    }
                    alertD.show()
                }
            }
        }
    }

    fun disableAllButtons() {
        Log.d(TAG, "disableAllButtons");
        startGameButton.isEnabled = false
        pauseGameButton.isEnabled = false
        resumeGameButton.isEnabled = false
        newGameButton.isEnabled = false
        quitGameButton.isEnabled = false
        settingButton.isEnabled = false
        top10Button.isEnabled = false
        globalTop10Button.isEnabled = false
    }

    fun displayTwoPlayerResult(
        hostScore: Int,
        hostHitNum: Int,
        clientScore: Int,
        clientHitNum: Int
    ) {
        val resultIntent = Intent(this@GroundhogActivity, TwoPlayerResultActivity::class.java)
        resultIntent.putExtra(Constants.HOST_SCORE, hostScore)
        resultIntent.putExtra(Constants.HOST_HIT_NUM, hostHitNum)
        resultIntent.putExtra(Constants.CLIENT_SCORE, clientScore)
        resultIntent.putExtra(Constants.CLIENT_HIT_NUM, clientHitNum)
        otherLauncher.launch(resultIntent)
    }

    private fun setDialogStyle(dialog: DialogInterface?) {
        val dlg = dialog as AlertDialog
        val win = dlg.window
        if (win == null) return
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        win.setDimAmount(0.0f) // no dim for background screen
        win.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        win.setBackgroundDrawableResource(R.drawable.dialog_background_image)
        val nBtn = dlg.getButton(DialogInterface.BUTTON_NEGATIVE)
        ScreenUtil.resizeTextSize(nBtn, textFontSize)
        nBtn.setTypeface(Typeface.DEFAULT_BOLD)
        nBtn.setTextColor(Color.RED)
        val layoutParams = nBtn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        nBtn.setLayoutParams(layoutParams)
        val pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        ScreenUtil.resizeTextSize(pBtn, textFontSize)
        pBtn.setTypeface(Typeface.DEFAULT_BOLD)
        pBtn.setTextColor(Color.rgb(0x00, 0x64, 0x00))
        pBtn.setLayoutParams(layoutParams)
    }

    // private class (Nested class)
    private inner class GhHunterBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val extras = intent.extras
            val actionName = intent.action
            if (actionName == null) return
            when (actionName) {
                LocalTop10Service.Action_Name -> {
                    // dismiss showing message
                    dismissShowingLoadingMessage()
                    val localTop10Intent =
                        Intent(this@GroundhogActivity, Top10ScoreActivity::class.java)
                    val localTop10Extras = Bundle()
                    localTop10Extras.putString(
                        "Top10TitleName",
                        getString(R.string.localTop10ScoreTitleString)
                    )
                    if (extras == null) {
                        localTop10Extras.putStringArrayList("Top10Players", ArrayList<String?>())
                        localTop10Extras.putIntegerArrayList("Top10Scores", ArrayList<Int?>())
                    } else {
                        localTop10Extras.putStringArrayList(
                            "Top10Players",
                            extras.getStringArrayList("PlayerNames")
                        )
                        localTop10Extras.putIntegerArrayList(
                            "Top10Scores",
                            extras.getIntegerArrayList("PlayerScores")
                        )
                    }
                    localTop10Intent.putExtras(localTop10Extras)
                    otherLauncher.launch(localTop10Intent)
                }

                GlobalTop10Service.Action_Name -> {
                    // dismiss showing message
                    dismissShowingLoadingMessage()
                    val globalTop10Intent =
                        Intent(this@GroundhogActivity, Top10ScoreActivity::class.java)
                    val globalTop10Extras = Bundle()
                    globalTop10Extras.putString(
                        "Top10TitleName",
                        getString(R.string.globalTop10ScoreTitleString)
                    )
                    if (extras == null) {
                        globalTop10Extras.putStringArrayList("Top10Players", ArrayList<String?>())
                        globalTop10Extras.putIntegerArrayList("Top10Scores", ArrayList<Int?>())
                    } else {
                        globalTop10Extras.putStringArrayList(
                            "Top10Players",
                            extras.getStringArrayList("PlayerNames")
                        )
                        globalTop10Extras.putIntegerArrayList(
                            "Top10Scores",
                            extras.getIntegerArrayList("PlayerScores")
                        )
                    }
                    globalTop10Intent.putExtras(globalTop10Extras)
                    otherLauncher.launch(globalTop10Intent)
                }
            }
        }
    }
}