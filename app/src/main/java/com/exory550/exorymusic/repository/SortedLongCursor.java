package com.exory550.exorymusic.repository;

import android.database.AbstractCursor;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SortedLongCursor extends AbstractCursor {
  private final Cursor mCursor;
  private ArrayList<Integer> mOrderedPositions;
  private final ArrayList<Long> mMissingIds;
  private HashMap<Long, Integer> mMapCursorPositions;

  public SortedLongCursor(final Cursor cursor, final long[] order, final String columnName) {

    mCursor = cursor;
    mMissingIds = buildCursorPositionMapping(order, columnName);
  }

  @NonNull
  private ArrayList<Long> buildCursorPositionMapping(
      @Nullable final long[] order, final String columnName) {
    ArrayList<Long> missingIds = new ArrayList<>();

    mOrderedPositions = new ArrayList<>(mCursor.getCount());

    mMapCursorPositions = new HashMap<>(mCursor.getCount());
    final int idPosition = mCursor.getColumnIndex(columnName);

    if (mCursor.moveToFirst()) {
      do {
        mMapCursorPositions.put(mCursor.getLong(idPosition), mCursor.getPosition());
      } while (mCursor.moveToNext());

      for (int i = 0; order != null && i < order.length; i++) {
        final long id = order[i];
        if (mMapCursorPositions.containsKey(id)) {
          mOrderedPositions.add(mMapCursorPositions.get(id));
          mMapCursorPositions.remove(id);
        } else {
          missingIds.add(id);
        }
      }

      mCursor.moveToFirst();
    }

    return missingIds;
  }

  public ArrayList<Long> getMissingIds() {
    return mMissingIds;
  }

  @NonNull
  public Collection<Long> getExtraIds() {
    return mMapCursorPositions.keySet();
  }

  @Override
  public void close() {
    mCursor.close();
    super.close();
  }

  @Override
  public int getCount() {
    return mOrderedPositions.size();
  }

  @Override
  public String[] getColumnNames() {
    return mCursor.getColumnNames();
  }

  @Override
  public String getString(int column) {
    return mCursor.getString(column);
  }

  @Override
  public short getShort(int column) {
    return mCursor.getShort(column);
  }

  @Override
  public int getInt(int column) {
    return mCursor.getInt(column);
  }

  @Override
  public long getLong(int column) {
    return mCursor.getLong(column);
  }

  @Override
  public float getFloat(int column) {
    return mCursor.getFloat(column);
  }

  @Override
  public double getDouble(int column) {
    return mCursor.getDouble(column);
  }

  @Override
  public boolean isNull(int column) {
    return mCursor.isNull(column);
  }

  @Override
  public boolean onMove(int oldPosition, int newPosition) {
    if (newPosition >= 0 && newPosition < getCount()) {
      mCursor.moveToPosition(mOrderedPositions.get(newPosition));
      return true;
    }

    return false;
  }
}
