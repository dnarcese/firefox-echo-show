/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

public class ViewUtils {
    /**
     * Flag of imeOptions: used to request that the IME does not update any personalized data such
     * as typing history and personalized language model based on what the user typed on this text
     * editing object.
     */
    public static final int IME_FLAG_NO_PERSONALIZED_LEARNING = 0x01000000;

    /**
     * Runnable to show the keyboard for a specific view.
     */
    private static class ShowKeyboard implements Runnable {
        private static final int INTERVAL_MS = 100;

        private final WeakReference<View> viewReferemce;
        private final Handler handler;

        private int tries;

        private ShowKeyboard(View view) {
            this.viewReferemce = new WeakReference<>(view);
            this.handler = new Handler(Looper.getMainLooper());
            this.tries = 10;
        }

        @Override
        public void run() {
            if (tries <= 0) {
                return;
            }

            final View view = viewReferemce.get();
            if (view == null) {
                // The view is gone. No need to continue.
                return;
            }

            if (!view.isFocusable() || !view.isFocusableInTouchMode()) {
                // The view is not focusable - we can't show the keyboard for it.
                return;
            }

            if (!view.requestFocus()) {
                // Focus this view first.
                post();
                return;
            }

            final Activity activity = (Activity) view.getContext();
            if (activity == null) {
                return;
            }

            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }

            if (!imm.isActive(view)) {
                // This view is not the currently active view for the input method yet.
                post();
                return;
            }

            if (!imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)) {
                // Showing they keyboard failed. Try again later.
                post();
            }
        }

        private void post() {
            tries--;
            handler.postDelayed(this, INTERVAL_MS);
        }
    }

    public static void showKeyboard(View view) {
        final ShowKeyboard showKeyboard = new ShowKeyboard(view);
        showKeyboard.post();
    }

    public static boolean hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }

        return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isRTL(View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }
}
