package com.hatopigeon.cubictimer.items;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores a solve. Solves can be converted to parcels, allowing their state to be saved and
 * restored in the context of managing the user-interface elements.
 */
public class Solve implements Parcelable {
    long   id;
    long   time;
    String puzzle;
    String subtype;
    long   date;
    String scramble;

    // penalty
    //  MMM_PP
    //   MMM : the number of penalty for MBLD
    //   PP  : penalty flag (0:no penalty, 1:+2, 2:DNF, 10:HIDETIME (for special purpose)
    int    penalty;
    String comment;
    boolean history;

    private static final int PLACE_MBLD_PENALTY_NUM = 100;

    public Solve(long time, String puzzle, String subtype, long date, String scramble, int penalty,
                 String comment, boolean history) {
        this.time = time;
        this.puzzle = puzzle;
        this.subtype = subtype;
        this.date = date;
        this.scramble = scramble;
        this.penalty = penalty;
        this.comment = comment;
        this.history = history;
    }

    public Solve(long time, String puzzle, String subtype, long date, String scramble, int penalty,
                 int mbldPenaltyNum, String comment, boolean history) {
        this.time = time;
        this.puzzle = puzzle;
        this.subtype = subtype;
        this.date = date;
        this.scramble = scramble;
        this.penalty = penalty;
        setMbldPenaltyNum(mbldPenaltyNum);
        this.comment = comment;
        this.history = history;
    }

    public Solve(long id, long time, String puzzle, String subtype, long date, String scramble,
                 int penalty, String comment, boolean history) {
        this.id = id;
        this.time = time;
        this.puzzle = puzzle;
        this.subtype = subtype;
        this.date = date;
        this.scramble = scramble;
        this.penalty = penalty;
        this.comment = comment;
        this.history = history;
    }

    /**
     * Creates a solve from a {@code Parcel}.
     *
     * @param in The parcel from which to read the details of the solve.
     */
    protected Solve(Parcel in) {
        id = in.readLong();
        time = in.readLong();
        puzzle = in.readString();
        subtype = in.readString();
        date = in.readLong();
        scramble = in.readString();
        penalty = in.readInt();
        comment = in.readString();
        history = in.readByte() != 0;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isHistory() {
        return history;
    }

    public void setHistory(boolean history) {
        this.history = history;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(String puzzle) {
        this.puzzle = puzzle;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getScramble() {
        return scramble;
    }

    public void setScramble(String scramble) {
        this.scramble = scramble;
    }

    public int getPenalty() {
        return penalty % PLACE_MBLD_PENALTY_NUM;
    }

    public void setPenalty(int penalty) {
        this.penalty = getMbldPenaltyNum() * PLACE_MBLD_PENALTY_NUM + penalty;
    }

    public int getMbldPenaltyNum() {
        return penalty / PLACE_MBLD_PENALTY_NUM;
    }

    public void setMbldPenaltyNum(int penaltyNum) {
        penalty = penaltyNum * PLACE_MBLD_PENALTY_NUM + this.penalty % PLACE_MBLD_PENALTY_NUM;
    }

    public int getRawPenalty() {
        return penalty;
    }

    public static int getPenalty(int rawPenalty) {
        return rawPenalty % PLACE_MBLD_PENALTY_NUM;
    }

    public static int getMbldPenaltyNum(int rawPenalty) {
        return rawPenalty / PLACE_MBLD_PENALTY_NUM;
    }
    /**
     * Writes this solve to a parcel.
     *
     * @param dest  The parcel to which to write the solve data.
     * @param flags Ignored. Use zero.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(time);
        dest.writeString(puzzle);
        dest.writeString(subtype);
        dest.writeLong(date);
        dest.writeString(scramble);
        dest.writeInt(penalty);
        dest.writeString(comment);
        dest.writeByte((byte) (history ? 1 : 0));
    }

    /**
     * Describes any special contents of this parcel. There are none.
     *
     * @return Zero, as there are no special contents.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Standard creator for parcels containing a {@link Solve}.
     */
    public static final Creator<Solve> CREATOR = new Creator<Solve>() {
        @Override
        public Solve createFromParcel(Parcel in) {
            return new Solve(in);
        }

        @Override
        public Solve[] newArray(int size) {
            return new Solve[size];
        }
    };
}
