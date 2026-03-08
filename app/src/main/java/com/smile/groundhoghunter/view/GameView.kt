package com.smile.groundhoghunter.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.lifecycleScope
import com.smile.groundhoghunter.GHogHunterApp
import com.smile.groundhoghunter.R
import com.smile.groundhoghunter.abstract_threads.IoFunctionThread
import com.smile.groundhoghunter.constants.Constants
import com.smile.groundhoghunter.models.Groundhog
import com.smile.groundhoghunter.threads.GameTimerThread
import com.smile.groundhoghunter.threads.GameViewDrawThread
import com.smile.groundhoghunter.threads.GroundhogRandomThread
import com.smile.smilelibraries.utilities.SoundPoolUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("ViewConstructor")
class GameView(
    private val mActivity: GroundhogActivity,
    private val mGType: Int,
    private val mRowNum: Int,
    private val mColNum: Int,
    private val mGViewWidth: Int,
    private val mGViewHeight: Int,
    private val mIoFuncThread: IoFunctionThread?
) : SurfaceView(mActivity), SurfaceHolder.Callback {

    companion object {
        private const val TAG = "GameView"
        const val BT_MEDIA_TYPE: Int = 0
        const val WIFI_MEDIA_TYPE: Int = 1
        const val NONE_MEDIA_TYPE: Int = -1
        const val TIMER_INTERVAL: Int = 60 // 60 seconds
        const val DRAWING_INTERVAL: Int = 80
        const val NUM_G_HOG_TYPES: Int = 4 // including hiding
        const val TIMER_INTERVAL_SHOWN: Int = 300 // 300 milliseconds
        @JvmField
        var gViewPause: Boolean = false // for synchronizing
        @JvmField
        val gViewLocker = Object()  // for synchronizing
        @JvmField
        val numTimeIntervalShown = intArrayOf(
            4, // has to be even (4 frames for animation, total time is 300 * 4 milliseconds
            6, // has to be even (6 frames for animation, total time is 300 * 6 milliseconds
            8, // has to be even (8 frames for animation, total time is 300 * 8 milliseconds
            10 // has to be even (10 frames for animation, total time is 300 * 10 milliseconds
        )
        @JvmField
        val groundhogBitmaps: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_0),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_1),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_2),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_3)
        )
        @JvmField
        val groundhogHitBitmaps: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_0),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_1),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_2),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.groundhog_3)
        )
        @JvmField
        val hitScores = intArrayOf(40, 30, 20, 10)
        @JvmField
        val score_board: Array<Bitmap> = arrayOf(
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.red_score_board),
            BitmapFactory.decodeResource(GHogHunterApp.appResources, R.drawable.yellow_score_board)
        )
    }

    private val mSurfaceHolder: SurfaceHolder
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
    private var mHasSound: Boolean
    private val mSNDPoolUtil: SoundPoolUtil
    private lateinit var mGHogArray: Array<Groundhog>

    init {
        Log.d(TAG, "GameView.init")
        gViewPause = false // for synchronizing
        setWillNotDraw(true) // added on 2017-11-07 for just in case, the default is true
        mSurfaceHolder = holder
        mSurfaceHolder.addCallback(this) // register the interface
        setZOrderOnTop(true)
        // surfaceHolder.setFormat(PixelFormat.TRANSPARENT);    // same effect as the following
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT)
        mHighestScore = mActivity.getHighestScore()
        mCurrentScore = 0
        mNumOfHits = 0
        isOpposPlayerLeft = false
        isReceivedScoreFromOppos = false
        isSFViewCreated = false // surfaceView has not been created yet
        mRunningStatus = 0 // game is not running
        mTimeRemaining = TIMER_INTERVAL
        mHasSound = true // default is having sound
        // create sound pool
        mSNDPoolUtil = mActivity.getSoundPoolUtil()
        // Creating groundhogs' object
        // start to initialize groundhogArray array
        createGroundhogs()
    }

    fun getRunningStatus(): Int {
        return mRunningStatus
    }

    fun hasSound(): Boolean {
        return mHasSound
    }

    fun setHasSound(hasSound: Boolean) {
        mHasSound = hasSound
    }

    fun setHighestScore(hScore: Int) {
        mHighestScore = hScore
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

    fun getIoFuncThread(): IoFunctionThread? {
        return mIoFuncThread
    }

    fun getOpposCurrentScore(): Int {
        return mOpposCurrentScore
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
                        if (hitStatus == Constants.NO_HIT_STATUS) {
                            // not hit
                            val newHitStatus: Int
                            if (groundhog.drawArea.contains(x.toFloat(), y.toFloat())) {
                                // hit
                                if (mHasSound) {
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
                                    mIoFuncThread?.write(
                                        Constants.TWO_PLAY_GAME_G_HOG_HIT,
                                        writeString
                                    )
                                }
                                groundhog.hitStatus = newHitStatus
                                Log.d(TAG, "onTouchEvent.hitStatus = " + groundhog.hitStatus)
                                ++mNumOfHits
                                mCurrentScore += hitScores[groundhog.status]
                                Log.d(TAG, "onTouchEvent.mCurrentScore = $mCurrentScore")
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
            val mGd = mGDrawTh ?: return
            val mGH = mGHogRandomTh ?: return
            val mGT = mGTimerTh ?: return
            mGH.start()
            mGd.start()
            mGT.start()
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
        val mGT = mGTimerTh ?: return
        mTimeRemaining = mGT.timeRemaining
        startDrawingScreen()
        if ((mTimeRemaining <= 0) && (mRunningStatus == 1)) {
            // if game is running and timer is finished, then it is game over
            gameOver()
        }
    }

    fun releaseSynchronizations() {
        Log.d(TAG, "releaseSynchronizations")
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
        Log.d(TAG, "stopThreads")
        var retry: Boolean
        val mGH = mGHogRandomTh ?: return
        val mGD = mGDrawTh ?: return
        val mGT = mGTimerTh ?: return
        mGH.setKeepRunning(false)
        retry = true
        while (retry) {
            try {
                mGH.join()
                Log.d(TAG, "stopThreads.groundhogRandomThread.Join()")
                retry = false
            } catch (ex: InterruptedException) {
                Log.e(TAG, "stopThreads.Exception: ", ex)
            } // continue processing until the thread ends
        }

        mGD.setKeepRunning(false)
        retry = true
        while (retry) {
            try {
                mGD.join()
                Log.d(TAG, "stopThreads.gameViewDrawThread.Join()")
                retry = false
            } catch (ex: InterruptedException) {
                Log.e(TAG, "stopThreads.Exception: ", ex)
            }
        }

        mGT.setKeepRunning(false) // stop the gameTimerThread
        retry = true
        while (retry) {
            try {
                Log.d(TAG, "gameTimerThread.Join()")
                mGT.join()
                retry = false
            } catch (ex: InterruptedException) {
                Log.e(TAG, "stopThreads.Exception: ", ex)
            }
        }
    }

    fun releaseResources() {
        Log.d(TAG, "releaseResources")
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
        Log.d(TAG, "startDrawingScreen")
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
        Log.d(TAG, "doDraw.mHighestScore = $mHighestScore")
        val hScore = mHighestScore.toString()
        Log.d(TAG, "doDraw.hScore = $hScore")
        Log.d(TAG, "doDraw.mCurrentScore = $mCurrentScore")
        val cScore = mCurrentScore.toString()
        Log.d(TAG, "doDraw.cScore = $cScore")
        Log.d(TAG, "doDraw.mTimeRemaining = $mTimeRemaining")
        val tRemaining = mTimeRemaining.toString()
        Log.d(TAG, "doDraw.tRemaining = $tRemaining")
        Log.d(TAG, "doDraw.mNumOfHits = $mNumOfHits")
        val nHits = mNumOfHits.toString()
        Log.d(TAG, "doDraw.nHits = $nHits")
        mActivity.setTextForHighScoreTextView(hScore)
        mActivity.setTextForScoreTextView(cScore)
        mActivity.setTextForTimerTextView(tRemaining)
        mActivity.setTextForHitNumTextView(nHits)
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
        Log.d(TAG, "gameOver")
        val mGd = mGDrawTh ?: return
        val mGH = mGHogRandomTh ?: return
        val mGT = mGTimerTh ?: return
        // game over
        // set threads to stop running loop
        // but do not use Thread.join() to stop stop thread
        mGd.setKeepRunning(false)
        mGH.setKeepRunning(false)
        mGT.setKeepRunning(false)
        mRunningStatus = 2
        if (mGType == Constants.GAME_BY_SINGLE_PLAY) {
            // single player then record the score
            mActivity.recordScore(mCurrentScore)
        } else {
            // display the competition result
            val mIO = mIoFuncThread ?: return
            var scoreString = String.format(Locale.ENGLISH, "%04d", mCurrentScore)
            scoreString += String.format(Locale.ENGLISH, "%04d", mNumOfHits)
            mIO.write(Constants.TWO_PLAY_GAME_SCORE_RECEIVED, scoreString)
            mActivity.lifecycleScope.launch(Dispatchers.Main) {
                // mActivity.disableAllButtons()
                if (isOpposPlayerLeft) {
                    // opposite player has left game then show result
                    mOpposCurrentScore = 0
                    mOpposNumOfHits = 0
                } else {
                    // waiting until received the scores from opposite player (only wait 6 seconds)
                    withContext(Dispatchers.IO) {
                        val maxLoop = 30
                        var i = 0
                        while ((!isReceivedScoreFromOppos) && (i < maxLoop)) {
                            Log.d(TAG, "doInBackground.Number of loop (i) = $i")
                            delay(200)
                            i++
                        }
                    }
                    if (mGType == Constants.TWO_PLAY_GAME_BY_HOST) {
                        mActivity.displayTwoPlayerResult(
                            mCurrentScore,
                            mNumOfHits,
                            mOpposCurrentScore,
                            mOpposNumOfHits
                        )
                    } else {
                        // TwoPlayerGameByClient
                        mActivity.displayTwoPlayerResult(
                            mOpposCurrentScore,
                            mOpposNumOfHits,
                            mCurrentScore,
                            mNumOfHits
                        )
                    }
                    mActivity.disableAllButtons()
                }
            }
        }
    }
}
