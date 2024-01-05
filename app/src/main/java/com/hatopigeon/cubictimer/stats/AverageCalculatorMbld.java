package com.hatopigeon.cubictimer.stats;

import android.util.Log;

import com.hatopigeon.cubictimer.items.AverageComponent;
import com.hatopigeon.cubictimer.utils.PuzzleUtils;
import com.hatopigeon.cubictimer.utils.PuzzleUtils.MbldRecord;
import com.hatopigeon.cubictimer.utils.StatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AverageCalculator for MBLD
 */
public final class AverageCalculatorMbld extends AverageCalculatorSuper {
    AverageCalculatorMbld(int n, int trimPercent) {
        super(n, trimPercent);
    }

    /**
     * Adds a solve time to be included in the calculation of the average. Solve times should be
     * added in chronological order (i.e., by solve time-stamp, not solve time).
     *
     * @param time
     *     The solve time in milliseconds. The time must be greater than zero. Use {@link #DNF} to
     *     represent a DNF solve.
     *
     * @throws IllegalArgumentException
     *     If the added time is not greater than zero and is not {@code DNF}.
     */
    @Override
    public void addTime(long time) {
        if (time <= 0L && time != DNF) {
            // FIXME: throwing an IllegalArgumentException here is too harsh for the user. If the app
            // incorrectly imports an illegal solve, the app will keep crashing and the only way for
            // the user to fix this is to clear the app data. I'm commenting this off for the time
            // being until the import algorithm gets sorted out.
            // TODO: Should the app automatically remove illegal solves?

            Log.e("AverageCalculatorMbld", "Time must be > 0 or be 'DNF': " + time);

            // throw new IllegalArgumentException("Time must be > 0 or be 'DNF': " + time);
        } else {

            mNumSolves++;

            final long ejectedTime;

            // If the array has just been filled, store a sorted version of it
            // If the array is full, "mNext" points to the oldest result that needs to be ejected first.
            //      We also need to remove the oldest solve from the sorted array and insert the new solve
            // If the array is not full, then "mNext" points to an empty entry, so no special handling
            // is needed.
            if (mNumSolves >= mN) {
                if (mNext == mN) {
                    // Need to wrap around to the start (index zero).
                    mNext = 0;
                }
                ejectedTime = mTimes[mNext]; // May be DNF.

                // Create the sorted list as soon as numSolves reaches N
                // We only need to sort it once. Subsequent added solves will use
                // an algorithm to insert the new solves in the correct (sorted) position
                if (mNumSolves == mN) {
                    mTimes[mNext] = time;

                    // Sort mTimes
                    // it can be sorted as same as others but order is reversed
                    // reverse order of time means normal oder of point
                    List<Long> sortedTimes = new ArrayList<>(StatUtils.asList(mTimes));
                    Collections.sort(sortedTimes, Collections.reverseOrder());

                    // Distribute the sorted times into the trims
                    int count = 0;
                    for (long solve : sortedTimes) {
                        long point = new MbldRecord(solve).getPoint();
                        if (point == DNF) point = -1;
                        if (count < mLowerTrimBound)
                            mLowerTrim.put(point);
                        else if (count >= mUpperTrimBound)
                            mUpperTrim.put(point);
                        else
                            mMiddleTrim.put(point);
                        count++;
                    }
                }

            } else {
                // "mNext" must be less than "mN" if "mNumSolves" is less than "mN".
                ejectedTime = UNKNOWN; // Nothing ejected.
            }

            mTimes[mNext] = time;
            mNext++;

            //Log.d("AverageCalculatorMbld", "N: " + mN + " | Set: " + mSortedTimes);

            // Order is important here, as these methods change fields and some methods depend on the
            // fields being updated by other methods before they are called. All depend on the new
            // time being stored already (see above) and any ejected time being known (also above).
            final long point = new MbldRecord(time).getPoint();
            final long ejectedPoint = new MbldRecord(ejectedTime).getPoint();
            updateDNFCounts(time, ejectedTime);
            updateCurrentBestAndWorstTimes(time, ejectedTime);
            updateSums(point, ejectedPoint);
            updateCurrentTrims(point, ejectedPoint);
            updateVariance(point);
            updateCurrentAverage();

            updateAllTimeBestAndWorstTimes();
            updateAllTimeBestAverage();
        }
    }

    @Override
    protected void updateCurrentTrims(long addedTime, long ejectedTime) {
        if (addedTime == DNF) addedTime = -1;
        if (ejectedTime == DNF) ejectedTime = -1;

        if (mNumSolves > mN && mLowerTrimBound > 0) {
            // Ejected time belongs to lower trim
            if (ejectedTime <= mLowerTrim.getGreatest()) {
                // Remove the ejected time
                mLowerTrim.remove(ejectedTime);

                if (addedTime <= mMiddleTrim.getLeast()) {
                    // Added time belongs to lower trim
                    mLowerTrim.put(addedTime);
                } else if (addedTime >= mUpperTrim.getLeast()) {
                    // Added time belongs to upper trim
                    // Move least elements to the left
                    mLowerTrim.put(mMiddleTrim.getLeast());
                    mMiddleTrim.remove(mMiddleTrim.getLeast());
                    mMiddleTrim.put(mUpperTrim.getLeast());
                    mUpperTrim.remove(mUpperTrim.getLeast());
                    mUpperTrim.put(addedTime);
                } else {
                    // Added time belongs to middle trim
                    // Move least elements to the left
                    mLowerTrim.put(mMiddleTrim.getLeast());
                    mMiddleTrim.remove(mMiddleTrim.getLeast());
                    mMiddleTrim.put(addedTime);
                }
            }

            // Ejected time belongs to upper trim
            else if (ejectedTime >= mUpperTrim.getLeast()) {
                // Remove the ejected time
                mUpperTrim.remove(ejectedTime);

                if (addedTime >= mMiddleTrim.getGreatest()) {
                    // Added time belongs to upper trim
                    mUpperTrim.put(addedTime);
                } else if (addedTime <= mLowerTrim.getGreatest()) {
                    // Added time belongs to lower trim
                    // Move greatest elements to right
                    mUpperTrim.put(mMiddleTrim.getGreatest());
                    mMiddleTrim.remove(mMiddleTrim.getGreatest());
                    mMiddleTrim.put(mLowerTrim.getGreatest());
                    mLowerTrim.remove(mLowerTrim.getGreatest());
                    mLowerTrim.put(addedTime);
                } else {
                    // Added time belongs to middle trim
                    // Move greatest elements to right
                    mUpperTrim.put(mMiddleTrim.getGreatest());
                    mMiddleTrim.remove(mMiddleTrim.getGreatest());
                    mMiddleTrim.put(addedTime);
                }
            }

            // Ejected time belongs to middle trim
            else {
                // Remove the ejected time
                mMiddleTrim.remove(ejectedTime);

                if (addedTime >= mUpperTrim.getLeast()) {
                    // Added time belongs to upper trim
                    // Move least elements to left
                    mMiddleTrim.put(mUpperTrim.getLeast());
                    mUpperTrim.remove(mUpperTrim.getLeast());
                    mUpperTrim.put(addedTime);
                } else if (addedTime <= mLowerTrim.getGreatest()) {
                    // Added time belongs to lower trim
                    // Move greatest elements to right
                    mMiddleTrim.put(mLowerTrim.getGreatest());
                    mLowerTrim.remove(mLowerTrim.getGreatest());
                    mLowerTrim.put(addedTime);
                } else {
                    // Added time belongs to middle trim
                    mMiddleTrim.put(addedTime);
                }
            }
        } else if (mNumSolves > mN) {
            // If the bound is 0, mLowerTrim and mUpperTrim will be null
            // All operations will be done on mMiddleTrim
            mMiddleTrim.remove(ejectedTime);
            mMiddleTrim.put(addedTime);
        }
    }

    /**
     * Updates the all-time best average after a new time is added. The current average must be
     * updated by {@link #updateCurrentAverage()} before calling this method.
     */
    @Override
    protected void updateAllTimeBestAverage() {
        if (mAllTimeBestAverage == UNKNOWN || mAllTimeBestAverage == DNF) {
            // "mCurrentAverage" may still be UNKNOWN or DNF, but cannot change back to UNKNOWN once
            // set to a different value, as UNKNOWN is cleared once "mN" solves have been added.
            // Therefore, we never set "mAllTimeBestAverage" to a value worse than it already has.
            mAllTimeBestAverage = mCurrentAverage;
        } else if (mCurrentAverage != DNF) {
            if (mCurrentAverage > mAllTimeBestAverage) {
                mIsAllTimeBestAverageUpdated = true;
            } else {
                mIsAllTimeBestAverageUpdated = false;
            }
            mAllTimeBestAverage = Math.max(mAllTimeBestAverage, mCurrentAverage);
        }
    }
}
