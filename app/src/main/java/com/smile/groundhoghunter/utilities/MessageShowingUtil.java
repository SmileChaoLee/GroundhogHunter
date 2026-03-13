package com.smile.groundhoghunter.utilities;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

public class MessageShowingUtil {

    private final Activity mActivity;
    private final Handler mHandler;
    private final TextView mTextView;
    private final Queue<Runnable> runnableQueue;
    private final Runnable setTextViewInvisible;
    private boolean isRunnableRunning;

    public MessageShowingUtil(final Activity activity, final TextView textView) {
        mActivity = activity;
        mTextView = textView;
        mHandler = new Handler(Looper.getMainLooper());

        mActivity.runOnUiThread(() -> mTextView.setVisibility(View.INVISIBLE));

        runnableQueue = new LinkedList<>();
        isRunnableRunning = false;

        setTextViewInvisible = new Runnable() {
            @Override
            public void run() {
                mHandler.removeCallbacks(this);
                mTextView.setVisibility(View.INVISIBLE);
                /*
                if (!runnableQueue.isEmpty()) {
                    Runnable mRunnable = runnableQueue.poll();
                    mActivity.runOnUiThread(mRunnable);
                } else {
                    // no more runnable object in queue
                    isRunnableRunning = false;
                }
                */
            }
        };

    }

    public void showMessageInTextView(final String message, final int duration) {
        final Runnable setMessageToTextView = () -> {
            isRunnableRunning = true;
            mTextView.setText(message);
            mTextView.setVisibility(View.VISIBLE);
            mHandler.postDelayed(setTextViewInvisible, duration);
        };
        mActivity.runOnUiThread(setMessageToTextView);

        /*
        runnableQueue.add(setMessageToTextView);
        if (!isRunnableRunning) {
            isRunnableRunning = true;
            mActivity.runOnUiThread(setMessageToTextView);
        } else {
            runnableQueue.add(setMessageToTextView);
        }
        */
    }
}
