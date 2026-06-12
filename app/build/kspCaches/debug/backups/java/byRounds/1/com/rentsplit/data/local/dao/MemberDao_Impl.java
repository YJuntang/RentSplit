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
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rentsplit.data.model.Member;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class MemberDao_Impl implements MemberDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Member> __insertionAdapterOfMember;

  private final EntityDeletionOrUpdateAdapter<Member> __deletionAdapterOfMember;

  private final EntityDeletionOrUpdateAdapter<Member> __updateAdapterOfMember;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllMembers;

  public MemberDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMember = new EntityInsertionAdapter<Member>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `members` (`id`,`householdId`,`name`,`colorHex`,`isHouseLeader`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Member entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHouseholdId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getColorHex());
        final int _tmp = entity.isHouseLeader() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__deletionAdapterOfMember = new EntityDeletionOrUpdateAdapter<Member>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `members` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Member entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMember = new EntityDeletionOrUpdateAdapter<Member>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `members` SET `id` = ?,`householdId` = ?,`name` = ?,`colorHex` = ?,`isHouseLeader` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Member entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHouseholdId());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getColorHex());
        final int _tmp = entity.isHouseLeader() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllMembers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM members";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Member member, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMember.insertAndReturnId(member);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Member member, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMember.handle(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Member member, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMember.handle(member);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllMembers(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllMembers.acquire();
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
          __preparedStmtOfDeleteAllMembers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Member>> getMembersByHousehold(final long householdId) {
    final String _sql = "SELECT * FROM members WHERE householdId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, householdId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"members"}, new Callable<List<Member>>() {
      @Override
      @NonNull
      public List<Member> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHouseholdId = CursorUtil.getColumnIndexOrThrow(_cursor, "householdId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfIsHouseLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isHouseLeader");
          final List<Member> _result = new ArrayList<Member>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Member _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHouseholdId;
            _tmpHouseholdId = _cursor.getLong(_cursorIndexOfHouseholdId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final boolean _tmpIsHouseLeader;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsHouseLeader);
            _tmpIsHouseLeader = _tmp != 0;
            _item = new Member(_tmpId,_tmpHouseholdId,_tmpName,_tmpColorHex,_tmpIsHouseLeader);
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
  public Object getMemberById(final long id, final Continuation<? super Member> $completion) {
    final String _sql = "SELECT * FROM members WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Member>() {
      @Override
      @Nullable
      public Member call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHouseholdId = CursorUtil.getColumnIndexOrThrow(_cursor, "householdId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfIsHouseLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isHouseLeader");
          final Member _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHouseholdId;
            _tmpHouseholdId = _cursor.getLong(_cursorIndexOfHouseholdId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final boolean _tmpIsHouseLeader;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsHouseLeader);
            _tmpIsHouseLeader = _tmp != 0;
            _result = new Member(_tmpId,_tmpHouseholdId,_tmpName,_tmpColorHex,_tmpIsHouseLeader);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllMembersSync(final Continuation<? super List<Member>> $completion) {
    final String _sql = "SELECT * FROM members";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Member>>() {
      @Override
      @NonNull
      public List<Member> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHouseholdId = CursorUtil.getColumnIndexOrThrow(_cursor, "householdId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfIsHouseLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isHouseLeader");
          final List<Member> _result = new ArrayList<Member>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Member _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpHouseholdId;
            _tmpHouseholdId = _cursor.getLong(_cursorIndexOfHouseholdId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpColorHex;
            _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            final boolean _tmpIsHouseLeader;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsHouseLeader);
            _tmpIsHouseLeader = _tmp != 0;
            _item = new Member(_tmpId,_tmpHouseholdId,_tmpName,_tmpColorHex,_tmpIsHouseLeader);
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
