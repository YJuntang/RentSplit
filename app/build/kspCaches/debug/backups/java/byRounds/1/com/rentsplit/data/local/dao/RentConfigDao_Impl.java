package com.rentsplit.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rentsplit.data.model.RentConfig;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RentConfigDao_Impl implements RentConfigDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RentConfig> __insertionAdapterOfRentConfig;

  public RentConfigDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRentConfig = new EntityInsertionAdapter<RentConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rent_config` (`id`,`householdId`,`amount`,`dueDayOfMonth`,`lastGeneratedMonth`,`lastGeneratedYear`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RentConfig entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHouseholdId());
        statement.bindDouble(3, entity.getAmount());
        statement.bindLong(4, entity.getDueDayOfMonth());
        statement.bindLong(5, entity.getLastGeneratedMonth());
        statement.bindLong(6, entity.getLastGeneratedYear());
      }
    };
  }

  @Override
  public Object insertOrUpdate(final RentConfig rentConfig,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRentConfig.insert(rentConfig);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<RentConfig> getRentConfig(final long householdId) {
    final String _sql = "SELECT * FROM rent_config WHERE householdId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, householdId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rent_config"}, new Callable<RentConfig>() {
      @Override
      @Nullable
      public RentConfig call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHouseholdId = CursorUtil.getColumnIndexOrThrow(_cursor, "householdId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDueDayOfMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDayOfMonth");
          final int _cursorIndexOfLastGeneratedMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "lastGeneratedMonth");
          final int _cursorIndexOfLastGeneratedYear = CursorUtil.getColumnIndexOrThrow(_cursor, "lastGeneratedYear");
          final RentConfig _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHouseholdId;
            _tmpHouseholdId = _cursor.getLong(_cursorIndexOfHouseholdId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final int _tmpDueDayOfMonth;
            _tmpDueDayOfMonth = _cursor.getInt(_cursorIndexOfDueDayOfMonth);
            final int _tmpLastGeneratedMonth;
            _tmpLastGeneratedMonth = _cursor.getInt(_cursorIndexOfLastGeneratedMonth);
            final int _tmpLastGeneratedYear;
            _tmpLastGeneratedYear = _cursor.getInt(_cursorIndexOfLastGeneratedYear);
            _result = new RentConfig(_tmpId,_tmpHouseholdId,_tmpAmount,_tmpDueDayOfMonth,_tmpLastGeneratedMonth,_tmpLastGeneratedYear);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
