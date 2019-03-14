package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class MessageShowingUtil {

    private final Context mContext;
    private final Handler mHandler;
    private final TextView mTextView;
    private Queue<Runnable> runnableQueue;
    private boolean isRunnableRunning;

    public MessageShowingUtil(Context context, TextView textView) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mTextView = textView;
        runnableQueue = new LinkedList<>();
        isRunnableRunning = false;
    }

    public void showMessageInTextView(final String message, final int duration) {
        final Runnable setTextViewInvisible = new Runnable() {
            @Override
            public void run() {
                mTextView.setVisibility(View.INVISIBLE);
                mHandler.removeCallbacks(this);
                if (!runnableQueue.isEmpty()) {
                    Runnable runnable = runnableQueue.poll();
                    runnable.run();
                } else {
                    // no more runnable object in queue
                    isRunnableRunning = false;
                }
            }
        };

        final Runnable setMessageToTextView = new Runnable() {
            @Override
            public void run() {
                mTextView.setText(message);
                mTextView.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(this);
                isRunnableRunning = true;
                mHandler.postDelayed(setTextViewInvisible, duration);
            }
        };

        runnableQueue.add(setMessageToTextView);
        if (!isRunnableRunning) {
            isRunnableRunning = true;
            setMessageToTextView.run();
        } else {
            runnableQueue.add(setMessageToTextView);
        }
    }
}
