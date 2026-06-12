package com.rentsplit.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rentsplit.data.model.Split;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SplitDao_Impl implements SplitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Split> __insertionAdapterOfSplit;

  private final EntityDeletionOrUpdateAdapter<Split> __deletionAdapterOfSplit;

  private final EntityDeletionOrUpdateAdapter<Split> __updateAdapterOfSplit;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSplits;

  public SplitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSplit = new EntityInsertionAdapter<Split>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `splits` (`id`,`expenseId`,`memberId`,`amountOwed`,`amountPaid`,`isPaid`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Split entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getExpenseId());
        statement.bindLong(3, entity.getMemberId());
        statement.bindDouble(4, entity.getAmountOwed());
        statement.bindDouble(5, entity.getAmountPaid());
        final int _tmp = entity.isPaid() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfSplit = new EntityDeletionOrUpdateAdapter<Split>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `splits` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Split entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSplit = new EntityDeletionOrUpdateAdapter<Split>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `splits` SET `id` = ?,`expenseId` = ?,`memberId` = ?,`amountOwed` = ?,`amountPaid` = ?,`isPaid` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Split entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getExpenseId());
        statement.bindLong(3, entity.getMemberId());
        statement.bindDouble(4, entity.getAmountOwed());
        statement.bindDouble(5, entity.getAmountPaid());
        final int _tmp = entity.isPaid() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllSplits = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM splits";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Split split, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSplit.insertAndReturnId(split);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Split split, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSplit.handle(split);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Split split, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSplit.handle(split);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSplits(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSplits.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllSplits.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Split>> getSplitsByExpense(final long expenseId) {
    final String _sql = "SELECT * FROM splits WHERE expenseId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, expenseId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"splits"}, new Callable<List<Split>>() {
      @Override
      @NonNull
      public List<Split> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExpenseId = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseId");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfAmountOwed = CursorUtil.getColumnIndexOrThrow(_cursor, "amountOwed");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final List<Split> _result = new ArrayList<Split>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Split _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final long _tmpMemberId;
            _tmpMemberId = _cursor.getLong(_cursorIndexOfMemberId);
            final double _tmpAmountOwed;
            _tmpAmountOwed = _cursor.getDouble(_cursorIndexOfAmountOwed);
            final double _tmpAmountPaid;
            _tmpAmountPaid = _cursor.getDouble(_cursorIndexOfAmountPaid);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            _item = new Split(_tmpId,_tmpExpenseId,_tmpMemberId,_tmpAmountOwed,_tmpAmountPaid,_tmpIsPaid);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Split>> getSplitsByMember(final long memberId) {
    final String _sql = "SELECT * FROM splits WHERE memberId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, memberId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"splits"}, new Callable<List<Split>>() {
      @Override
      @NonNull
      public List<Split> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExpenseId = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseId");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfAmountOwed = CursorUtil.getColumnIndexOrThrow(_cursor, "amountOwed");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final List<Split> _result = new ArrayList<Split>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Split _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final long _tmpMemberId;
            _tmpMemberId = _cursor.getLong(_cursorIndexOfMemberId);
            final double _tmpAmountOwed;
            _tmpAmountOwed = _cursor.getDouble(_cursorIndexOfAmountOwed);
            final double _tmpAmountPaid;
            _tmpAmountPaid = _cursor.getDouble(_cursorIndexOfAmountPaid);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            _item = new Split(_tmpId,_tmpExpenseId,_tmpMemberId,_tmpAmountOwed,_tmpAmountPaid,_tmpIsPaid);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Split>> getSplitsForExpenses(final List<Long> expenseIds) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM splits WHERE expenseId IN (");
    final int _inputSize = expenseIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : expenseIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"splits"}, new Callable<List<Split>>() {
      @Override
      @NonNull
      public List<Split> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExpenseId = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseId");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfAmountOwed = CursorUtil.getColumnIndexOrThrow(_cursor, "amountOwed");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final List<Split> _result = new ArrayList<Split>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Split _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final long _tmpMemberId;
            _tmpMemberId = _cursor.getLong(_cursorIndexOfMemberId);
            final double _tmpAmountOwed;
            _tmpAmountOwed = _cursor.getDouble(_cursorIndexOfAmountOwed);
            final double _tmpAmountPaid;
            _tmpAmountPaid = _cursor.getDouble(_cursorIndexOfAmountPaid);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            _item_1 = new Split(_tmpId,_tmpExpenseId,_tmpMemberId,_tmpAmountOwed,_tmpAmountPaid,_tmpIsPaid);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Double> getTotalOwedByMember(final long memberId) {
    final String _sql = "SELECT SUM(amountOwed - amountPaid) FROM splits WHERE memberId = ? AND isPaid = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, memberId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"splits"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllSplitsSync(final Continuation<? super List<Split>> $completion) {
    final String _sql = "SELECT * FROM splits";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Split>>() {
      @Override
      @NonNull
      public List<Split> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExpenseId = CursorUtil.getColumnIndexOrThrow(_cursor, "expenseId");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfAmountOwed = CursorUtil.getColumnIndexOrThrow(_cursor, "amountOwed");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfIsPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "isPaid");
          final List<Split> _result = new ArrayList<Split>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Split _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpExpenseId;
            _tmpExpenseId = _cursor.getLong(_cursorIndexOfExpenseId);
            final long _tmpMemberId;
            _tmpMemberId = _cursor.getLong(_cursorIndexOfMemberId);
            final double _tmpAmountOwed;
            _tmpAmountOwed = _cursor.getDouble(_cursorIndexOfAmountOwed);
            final double _tmpAmountPaid;
            _tmpAmountPaid = _cursor.getDouble(_cursorIndexOfAmountPaid);
            final boolean _tmpIsPaid;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPaid);
            _tmpIsPaid = _tmp != 0;
            _item = new Split(_tmpId,_tmpExpenseId,_tmpMemberId,_tmpAmountOwed,_tmpAmountPaid,_tmpIsPaid);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
