package com.smile.groundhoghunter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.Groundhog
import com.smile.groundhoghunter.threads.GameTimerThread
import com.smile.groundhoghunter.threads.GameViewDrawThread
import com.smile.groundhoghunter.threads.GroundhogRandomThread
import com.smile.smilelibraries.player_record_rest.httpUrl.PlayerRecordRest
import com.smile.smilelibraries.utilities.ScreenUtil
import com.smile.smilelibraries.utilities.SoundPoolUtil
import okhttp3.internal.notifyAll
import org.json.JSONObject
import java.util.Locale

@SuppressLint("ViewConstructor")
class GameView(
    context: Context, gameType: Int, gWidth: Int, gHeight: Int,
    ioFunctionThread: IoFunctionThread?
) : SurfaceView(context), SurfaceHolder.Callback {


    companion object {
        private const val TAG = "GameView"
        @JvmField
        var gViewPause: Boolean = false // for synchronizing
        @JvmField
        val gViewLocker = Object()  // for synchronizing
        const val BT_MEDIA_TYPE: Int = 0
        const val WIFI_MEDIA_TYPE: Int = 1
        const val NONE_MEDIA_TYPE: Int = -1
        const val TIMER_INTERVAL: Int = 60 // 60 seconds
        const val DRAWING_INTERVAL: Int = 80
        const val NUM_G_HOG_TYPES: Int = 4 // including hiding
        const val TIMER_INTERVAL_SHOWN: Int = 300 // 300 milli seconds
        @JvmField
        val numTimeIntervalShown = intArrayOf(
            4, // has to be even (4 frames for animation, total time is 300 * 4 milliseconds
            6, // has to be even (6 frames for animation, total time is 300 * 6 milliseconds
            8, // has to be even (8 frames for animation, total time is 300 * 8 milliseconds
            10 // has to be even (10 frames for animation, total time is 300 * 10 milliseconds
        )
        @JvmField
        val groundhogBitmaps: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_0
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_1
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_2
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_3
            )
        )

        @JvmField
        val groundhogHitBitmaps: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_0
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_1
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_2
            ),
            BitmapFactory.decodeResource(
                GroundhogHunterApp.AppResources,
                R.drawable.groundhog_3
            )
        )

        @JvmField
        val hitScores = intArrayOf(40, 30, 20, 10)

        @JvmField
        val score_board: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.red_score_board),
            BitmapFactory.decodeResource(GroundhogHunterApp.AppResources, R.drawable.yellow_score_board)
        )
    }

    private var mGType: Int
    private val mTextFontSize: Float
    private val mSurfaceHolder: SurfaceHolder
    private val mGroundhogAct: GroundhogActivity
    private val mRowNum: Int
    private val mColNum: Int
    private val mGViewWidth: Int
    private val mGViewHeight: Int
    private var mRectWidthForOneHog = 0f
    private var mRectHeightForOneHog = 0f
    private var mHighestScore: Int
    private var mCurrentScore: Int
    private var mNumOfHits: Int
    private var mOpposCurrentScore = 0
    private var mOpposNumOfHits = 0
    private var isOpposPlayerLeft: Boolean
    private var isReceivedScoreFromOppos: Boolean
    private var mTimeRemaining: Int
    private var mGDrawTh: GameViewDrawThread? = null
    private var mGHogRandomTh: GroundhogRandomThread? = null
    private var mGTimerTh: GameTimerThread? = null
    private var isSFViewCreated: Boolean
    private var mRunningStatus: Int
    private var hasSound: Boolean
    private var mSelectedIoFuncTh: IoFunctionThread? = null
    private val mSNDPoolUtil: SoundPoolUtil
    private lateinit var mGHogArray: Array<Groundhog>

    init {
        Log.d(TAG, "GameView.init")
        mSelectedIoFuncTh = ioFunctionThread
        mGroundhogAct = context as GroundhogActivity
        mGType = gameType
        mTextFontSize = ScreenUtil.getPxTextFontSizeNeeded(mGroundhogAct)
        mRowNum = mGroundhogAct.rowNum
        mColNum = mGroundhogAct.colNum
        mGViewWidth = gWidth
        mGViewHeight = gHeight
        gViewPause = false // for synchronizing
        setWillNotDraw(true) // added on 2017-11-07 for just in case, the default is true
        mSurfaceHolder = holder
        mSurfaceHolder.addCallback(this) // register the interface
        setZOrderOnTop(true)
        // surfaceHolder.setFormat(PixelFormat.TRANSPARENT);    // same effect as the following
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT)
        mHighestScore = mGroundhogAct.highestScore
        mCurrentScore = 0
        mNumOfHits = 0
        isOpposPlayerLeft = false
        isReceivedScoreFromOppos = false
        isSFViewCreated = false // surfaceView has not been created yet
        mRunningStatus = 0 // game is not running
        mTimeRemaining = TIMER_INTERVAL
        hasSound = true // default is having sound
        // create sound pool
        mSNDPoolUtil = SoundPoolUtil(context, R.raw.ouh)
        // Creating groundhogs' object
        // start to initialize groundhogArray array
        createGroundhogs()
    }

    fun getRunningStatus(): Int {
        return mRunningStatus
    }

    fun hasSound(): Boolean {
        return hasSound()
    }

    fun setHasSound(hasSound: Boolean) {
        this.hasSound = hasSound
    }

    fun isOpposPlayerLeft(): Boolean {
        return isOpposPlayerLeft
    }

    fun setOpposPlayerLeft(isOpposPlayerLeft: Boolean) {
        this.isOpposPlayerLeft = isOpposPlayerLeft
    }

    fun getGTimerTh(): GameTimerThread? {
        return mGTimerTh
    }

    fun getGHogArray(): Array<Groundhog> {
        return mGHogArray
    }

    fun getGType(): Int {
        return mGType
    }

    fun getSelectedIoFuncTh(): IoFunctionThread? {
        return mSelectedIoFuncTh
    }

    fun setOpposCurrentScore(opposCurrentScore: Int) {
        mOpposCurrentScore = opposCurrentScore
    }

    fun setOpposNumOfHits(opposNumOfHits: Int) {
        mOpposNumOfHits = opposNumOfHits
    }

    fun setReceivedScoreFromOppos(isReceivedFromOpposite: Boolean) {
        isReceivedScoreFromOppos = isReceivedFromOpposite
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.i(TAG, "onDraw() is called")
        // doDraw(canvas);
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated() is called")
        isSFViewCreated = true // surfaceView has been created
        startDrawingScreen() // draw screen
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        Log.i(TAG, "surfaceChanged() is called")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // the if statement is added on 2018-08-31
        if ((mRunningStatus == 1) && (!gViewPause)) {
            // game is running
            val x = event.x.toInt()
            val y = event.y.toInt()
            val action = event.action
            val groundhog: Groundhog
            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_BUTTON_PRESS, MotionEvent.ACTION_DOWN -> {
                    val i = (y / mRectHeightForOneHog).toInt() // row
                    val j = (x / mRectWidthForOneHog).toInt() // col
                    val index = mColNum * i + j
                    groundhog = mGHogArray[index]
                    if (!groundhog.isHiding) {
                        // showing but not hiding
                        val hitStatus = groundhog.hitStatus
                        if (hitStatus == 0) {
                            // not hit
                            val newHitStatus: Int
                            if (groundhog.drawArea.contains(x.toFloat(), y.toFloat())) {
                                // hit
                                if (hasSound) {
                                    // needs to play sound for hitting
                                    // SoundUtil.playSound(groundhogActivity, R.raw.ouh);
                                    mSNDPoolUtil.playSound()
                                }

                                if (mGType == Constants.GAME_BY_SINGLE_PLAY) {
                                    newHitStatus = Constants.SINGLE_PLAY_HIT_STATUS
                                } else {
                                    // not single player
                                    newHitStatus = if (mGType == Constants.TWO_PLAY_GAME_BY_HOST) {
                                        // host
                                        Constants.HOST_PLAY_HIT_STATUS
                                    } else {
                                        // client
                                        Constants.CLIENT_PLAY_HIT_STATUS
                                    }
                                    var writeString = ""
                                    writeString += String.format(Locale.ENGLISH, "%02d", index)
                                    val status = groundhog.status
                                    writeString += String.format(Locale.ENGLISH, "%01d", status)
                                    val isHiding = groundhog.isHiding
                                    writeString += if (isHiding) {
                                        "1"
                                    } else {
                                        "0"
                                    }
                                    val numOfTimeIntervalShown =
                                        groundhog.numOfTimeIntervalShown
                                    writeString += String.format(
                                        Locale.ENGLISH,
                                        "%02d",
                                        numOfTimeIntervalShown
                                    )
                                    writeString += newHitStatus // hit status
                                    mSelectedIoFuncTh?.write(
                                        Constants.TWO_PLAY_GAME_G_HOG_HIT,
                                        writeString
                                    )
                                }
                                groundhog.hitStatus = newHitStatus
                                Log.d(TAG, "onTouchEvent.hitStatus = " + groundhog.hitStatus)
                                ++mNumOfHits
                                mCurrentScore += hitScores[groundhog.status]
                                Log.d(TAG, "onTouchEvent.startDrawingScreen()")
                                startDrawingScreen() // added on 2018-10-29 for testing
                            }
                        }
                    }
                }

                else -> {}
            }
        }

        return super.onTouchEvent(event)
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed() is called")
    }

    fun startGame() {
        if ((isSFViewCreated) && (mRunningStatus == 0)) {
            mRunningStatus = 1 // game is set to be running
            mGTimerTh = GameTimerThread(this)
            mGHogRandomTh = GroundhogRandomThread(this)
            mGDrawTh = GameViewDrawThread(this)
            mGHogRandomTh!!.start()
            mGDrawTh!!.start()
            mGTimerTh!!.start()
        }
    }

    fun pauseGame() {
        if ((isSFViewCreated) && (mRunningStatus == 1) && (!gViewPause)) {
            // when game is running
            synchronized(gViewLocker) {
                gViewPause = true
            }
        }
    }

    fun resumeGame() {
        if ((isSFViewCreated) && (mRunningStatus == 1) && (gViewPause)) {
            // when game is running
            synchronized(gViewLocker) {
                gViewPause = false
                gViewLocker.notifyAll()
            }
        }
    }

    fun newGame() {
        if (mRunningStatus != 0) {
            // game is running or game over
            mCurrentScore = 0
            mNumOfHits = 0
            mRunningStatus = 0 // game is not running
            releaseSynchronizations()
            stopThreads()
            mTimeRemaining = TIMER_INTERVAL
            for (groundhog in mGHogArray) {
                groundhog.setIsHiding(true)
            }
            startDrawingScreen()
        }
    }

    fun drawGameScreen() {
        // Canvas canvas = null;
        mTimeRemaining = mGTimerTh!!.timeRemaining
        startDrawingScreen()
        if ((mTimeRemaining <= 0) && (mRunningStatus == 1)) {
            // if game is running and timer is finished, then it is game over
            gameOver()
        }
    }

    fun releaseSynchronizations() {
        if (GroundhogActivity.GamePause) {
            // in pause status
            synchronized(GroundhogActivity.activityLocker) {
                GroundhogActivity.GamePause = false
                GroundhogActivity.activityLocker.notifyAll()
            }
        }

        if (gViewPause) {
            // GameView in pause status
            synchronized(gViewLocker) {
                gViewPause = false
                gViewLocker.notifyAll()
            }
        }
    }

    fun stopThreads() {
        var retry: Boolean
        if (mGHogRandomTh != null) {
            mGHogRandomTh!!.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    mGHogRandomTh!!.join()
                    Log.d(TAG, "stopThreads.groundhogRandomThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    Log.e(TAG, "stopThreads.Exception: ", ex)
                } // continue processing until the thread ends
            }
        }

        if (mGDrawTh != null) {
            mGDrawTh!!.setKeepRunning(false)
            retry = true
            while (retry) {
                try {
                    mGDrawTh!!.join()
                    Log.d(TAG, "stopThreads.gameViewDrawThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    Log.e(TAG, "stopThreads.Exception: ", ex)
                } // continue processing until the thread ends
            }
        }

        if (mGTimerTh != null) {
            mGTimerTh!!.setKeepRunning(false) // stop the gameTimerThread
            retry = true
            while (retry) {
                try {
                    mGTimerTh!!.join()
                    Log.d(TAG, "gameTimerThread.Join()")
                    retry = false
                } catch (ex: InterruptedException) {
                    Log.e(TAG, "stopThreads.Exception: ", ex)
                } // continue processing until the thread ends
            }
        }
    }

    fun releaseResources() {
        Log.d(TAG, "releaseResources() is called.")
        mSNDPoolUtil.release() // release SoundPool
    }

    fun setGroundhogByMsgString(msgString: String) {
        val index = msgString.substring(0, 2).toInt()
        val groundhog = mGHogArray[index]
        val status = msgString.substring(2, 3).toInt()
        groundhog.setStatus(status)
        val hideByte = msgString.substring(3, 4).toInt()
        groundhog.setIsHiding(hideByte == 1)
        val numOfTimeIntervalShown: Int = msgString.substring(4, 6).toInt()
        groundhog.numOfTimeIntervalShown = numOfTimeIntervalShown
        val hitByte = msgString.substring(6, 7).toInt()
        groundhog.hitStatus = hitByte
    }

    // private methods
    /*
    private fun createGroundhogs() {
        mGHogArray = Array<Groundhog>(mRowNum * mColNum)
        var x: Float
        var y = 0f
        mRectWidthForOneHog = mGViewWidth / mColNum.toFloat()
        mRectHeightForOneHog = mGViewHeight / mRowNum.toFloat()
        var bottomY: Float
        var index: Int
        var groundhog: Groundhog?
        val temp = RectF()
        for (i in 0..<mRowNum) {
            x = 0f
            bottomY = y + mRectHeightForOneHog
            for (j in 0..<mColNum) {
                index = mColNum * i + j
                temp.left = x
                x += mRectWidthForOneHog
                temp.right = x
                temp.top = y
                temp.bottom = bottomY
                groundhog = Groundhog(temp)
                mGHogArray!![index] = groundhog
            }
            y = bottomY
        }
    }
    */

    private fun createGroundhogs() {
        Log.d(TAG, "createGroundhogs")
        val totalHogs = mRowNum * mColNum
        mRectWidthForOneHog = mGViewWidth / mColNum.toFloat()
        mRectHeightForOneHog = mGViewHeight / mRowNum.toFloat()
        // Initialize and fill the array in one block
        mGHogArray = Array(totalHogs) { index ->
            // Calculate row and column from the flat index
            val row = index / mColNum
            val col = index % mColNum
            val left = col * mRectWidthForOneHog
            val top = row * mRectHeightForOneHog
            val rect = RectF(left, top, left + mRectWidthForOneHog, top + mRectHeightForOneHog)
            Groundhog(rect) // This becomes the element at [index]
        }
    }

    private fun startDrawingScreen() {
        var canvas: Canvas? = null
        try {
            canvas = mSurfaceHolder.lockCanvas(null)
            Log.d(TAG, "startDrawingScreen.canvas = $canvas")
            synchronized(mSurfaceHolder) {
                Log.d(TAG, "startDrawingScreen.doDraw(canvas)")
                doDraw(canvas)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "startDrawingScreen.Exception: ", ex)
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private fun doDraw(canvas: Canvas?) {
        mGroundhogAct.runOnUiThread {
            mGroundhogAct.setTextForHighScoreTextView(mHighestScore.toString())
            mGroundhogAct.setTextForScoreTextView(mCurrentScore.toString())
            mGroundhogAct.setTextForTimerTextView(mTimeRemaining.toString())
            mGroundhogAct.setTextForHitNumTextView(mNumOfHits.toString())
        }
        Log.d(TAG, "doDraw.canvas = $canvas")
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            // Game View part
            for (groundhog in mGHogArray) {
                Log.d(TAG, "doDraw.groundhog.draw(canvas)")
                groundhog.draw(canvas)
            }
        }
    }

    private fun gameOver() {
        // game over
        // set threads to stop running loop
        // but do not use Thread.join() to stop stop thread
        mGDrawTh!!.setKeepRunning(false)
        mGHogRandomTh!!.setKeepRunning(false)
        mGTimerTh!!.setKeepRunning(false)
        mRunningStatus = 2
        if (mGType == Constants.GAME_BY_SINGLE_PLAY) {
            // single player then record the score
            val isInTop10 = GroundhogHunterApp.ScoreSQLiteDB.isInTop10(mCurrentScore)
            if (isInTop10) {
                // record the current score
                recordScore(mCurrentScore)
            }
        } else {
            // display the competition result
            var scoreString = String.format(Locale.ENGLISH, "%04d", mCurrentScore)
            scoreString += String.format(Locale.ENGLISH, "%04d", mNumOfHits)
            mSelectedIoFuncTh?.write(Constants.TWO_PLAY_GAME_SCORE_RECEIVED, scoreString)
            mGroundhogAct.runOnUiThread {
                val displayResultAsyncTask: AsyncTask<Void?, Void?, Void?> =
                    DisplayResultAsyncTask()
                displayResultAsyncTask.execute()
            }
        }
    }

    private fun recordScore(score: Int) {
        //    record currentScore as a score in database
        val res = resources
        mGroundhogAct.runOnUiThread {
            val et = EditText(mGroundhogAct)
            ScreenUtil.resizeTextSize(et, mTextFontSize)
            et.setTextColor(Color.BLUE)
            et.setHint(res.getString(R.string.nameString))
            et.setGravity(Gravity.CENTER)
            val alertD = AlertDialog.Builder(mGroundhogAct).create()
            alertD.setTitle(null)
            alertD.requestWindowFeature(Window.FEATURE_NO_TITLE)
            alertD.setCancelable(false)
            alertD.setView(et)
            alertD.setButton(
                DialogInterface.BUTTON_NEGATIVE, res.getString(R.string.cancelString)
            ) { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() }
            alertD.setButton(
                DialogInterface.BUTTON_POSITIVE, res.getString(R.string.submitString)
            ) { dialog: DialogInterface?, which: Int ->
                dialog!!.dismiss()
                // use thread to add a record to database (remote database on AWS-EC2)
                val restThread: Thread = object : Thread() {
                    override fun run() {
                        try {
                            val jsonObject = JSONObject()
                            jsonObject.put(Constants.PLAYER_NAME, et.getText().toString())
                            jsonObject.put(Constants.SCORE, score)
                            jsonObject.put(Constants.GAME_ID, Constants.GROUNDHOG_GAME_ID)
                            PlayerRecordRest.addOneRecord(jsonObject)
                        } catch (ex: Exception) {
                            Log.e(TAG, "recordScore.Exception", ex)
                        }
                    }
                }
                restThread.start()
                GroundhogHunterApp.ScoreSQLiteDB.addScore(et.getText().toString(), score)
                GroundhogHunterApp.ScoreSQLiteDB.deleteAllAfterTop10() // only keep the top 10
                if (mCurrentScore > mHighestScore) {
                    mHighestScore = mCurrentScore
                    mGroundhogAct.highestScore = mHighestScore
                    mGroundhogAct.setTextForHighScoreTextView(mHighestScore.toString())
                }
            }
            alertD.setOnShowListener { dialog: DialogInterface? ->
                this.setDialogStyle(
                    dialog
                )
            }
            alertD.show()
        }
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
        ScreenUtil.resizeTextSize(nBtn, mTextFontSize)
        nBtn.setTypeface(Typeface.DEFAULT_BOLD)
        nBtn.setTextColor(Color.RED)
        val layoutParams = nBtn.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        nBtn.setLayoutParams(layoutParams)
        val pBtn = dlg.getButton(DialogInterface.BUTTON_POSITIVE)
        ScreenUtil.resizeTextSize(pBtn, mTextFontSize)
        pBtn.setTypeface(Typeface.DEFAULT_BOLD)
        pBtn.setTextColor(Color.rgb(0x00, 0x64, 0x00))
        pBtn.setLayoutParams(layoutParams)
    }

    private inner class DisplayResultAsyncTask : AsyncTask<Void?, Void?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mGroundhogAct.disableAllButtons()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            if (isOpposPlayerLeft) {
                // opposite player has left game then show result
                mOpposCurrentScore = 0
                mOpposNumOfHits = 0
            } else {
                // waiting until received the scores from opposite player (only wait 6 seconds)
                val maxLoop = 30
                var i = 0
                while ((!isReceivedScoreFromOppos) && (i < maxLoop)) {
                    try {
                        Thread.sleep(200)
                    } catch (ex: InterruptedException) {
                        Log.e(TAG, "doInBackground.Exception: ", ex)
                    }
                    i++
                }
                Log.d(TAG, "doInBackground.Number of loop (i) = $i")
            }
            return null
        }

        override fun onPostExecute(o: Void?) {
            super.onPostExecute(o)
            if (mGType == Constants.TWO_PLAY_GAME_BY_HOST) {
                mGroundhogAct.displayTwoPlayerResult(
                    mCurrentScore,
                    mNumOfHits,
                    mOpposCurrentScore,
                    mOpposNumOfHits
                )
            } else {
                // TwoPlayerGameByClient
                mGroundhogAct.displayTwoPlayerResult(
                    mOpposCurrentScore,
                    mOpposNumOfHits,
                    mCurrentScore,
                    mNumOfHits
                )
            }
            mGroundhogAct.disableAllButtons()
        }
    }
}
