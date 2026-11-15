package com.abumoath.tv.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ChannelDao_Impl implements ChannelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChannelEntity> __insertionAdapterOfChannelEntity;

  private final EntityInsertionAdapter<ChannelStreamEntity> __insertionAdapterOfChannelStreamEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteStreams;

  private final SharedSQLiteStatement __preparedStmtOfMarkFailed;

  private final SharedSQLiteStatement __preparedStmtOfMarkOk;

  private final SharedSQLiteStatement __preparedStmtOfAddView;

  public ChannelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChannelEntity = new EntityInsertionAdapter<ChannelEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `channels` (`id`,`ext`,`name`,`nameNorm`,`logo`,`country`,`categories`,`bestHeight`,`views`,`addedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChannelEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getExt());
        statement.bindString(3, entity.getName());
        statement.bindString(4, entity.getNameNorm());
        if (entity.getLogo() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLogo());
        }
        if (entity.getCountry() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCountry());
        }
        statement.bindString(7, entity.getCategories());
        statement.bindLong(8, entity.getBestHeight());
        statement.bindLong(9, entity.getViews());
        statement.bindLong(10, entity.getAddedAt());
      }
    };
    this.__insertionAdapterOfChannelStreamEntity = new EntityInsertionAdapter<ChannelStreamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `channel_streams` (`id`,`channelId`,`url`,`height`,`referrer`,`userAgent`,`failCount`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChannelStreamEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getChannelId());
        statement.bindString(3, entity.getUrl());
        statement.bindLong(4, entity.getHeight());
        if (entity.getReferrer() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getReferrer());
        }
        if (entity.getUserAgent() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getUserAgent());
        }
        statement.bindLong(7, entity.getFailCount());
      }
    };
    this.__preparedStmtOfDeleteStreams = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM channel_streams WHERE channelId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkFailed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE channel_streams SET failCount = failCount + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkOk = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE channel_streams SET failCount = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfAddView = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE channels SET views = views + 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final ChannelEntity c, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfChannelEntity.insertAndReturnId(c);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertStreams(final List<ChannelStreamEntity> s,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChannelStreamEntity.insert(s);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStreams(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteStreams.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteStreams.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markFailed(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkFailed.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfMarkFailed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markOk(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkOk.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfMarkOk.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object addView(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAddView.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfAddView.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object idMap(final Continuation<? super List<IdExt>> $completion) {
    final String _sql = "SELECT id, ext FROM channels";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<IdExt>>() {
      @Override
      @NonNull
      public List<IdExt> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfExt = 1;
          final List<IdExt> _result = new ArrayList<IdExt>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IdExt _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            _item = new IdExt(_tmpId,_tmpExt);
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

  @Override
  public Object all(final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
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

  @Override
  public Object list(final String country, final String cat, final int limit, final int offset,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE (? IS NULL OR country = ?) AND (? IS NULL OR categories LIKE '%,' || ? || ',%')\n"
            + "        ORDER BY views DESC, CASE WHEN country IN ('SA','AE','KW','QA','BH','OM','EG','IQ','JO','PS','SY','LB','MA','DZ','TN','SD','YE') THEN 0 ELSE 1 END, name LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 6);
    int _argIndex = 1;
    if (country == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, country);
    }
    _argIndex = 2;
    if (country == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, country);
    }
    _argIndex = 3;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    _argIndex = 4;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    _argIndex = 5;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 6;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
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

  @Override
  public Object count(final String country, final String cat,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM channels WHERE (? IS NULL OR country = ?) AND (? IS NULL OR categories LIKE '%,' || ? || ',%')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    if (country == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, country);
    }
    _argIndex = 2;
    if (country == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, country);
    }
    _argIndex = 3;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    _argIndex = 4;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object inCountries(final List<String> countries, final String cat, final int limit,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM channels WHERE country IN (");
    final int _inputSize = countries.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND (");
    _stringBuilder.append("?");
    _stringBuilder.append(" IS NULL OR categories LIKE '%,' || ");
    _stringBuilder.append("?");
    _stringBuilder.append(" || ',%') ORDER BY views DESC, name LIMIT ");
    _stringBuilder.append("?");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 3 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : countries) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    _argIndex = 2 + _inputSize;
    if (cat == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cat);
    }
    _argIndex = 3 + _inputSize;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item_1 = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
            _result.add(_item_1);
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
  public Object get(final long id, final Continuation<? super ChannelEntity> $completion) {
    final String _sql = "SELECT * FROM channels WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChannelEntity>() {
      @Override
      @Nullable
      public ChannelEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final ChannelEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _result = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
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
  public Object getMany(final List<Long> ids,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM channels WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : ids) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item_1 = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
            _result.add(_item_1);
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
  public Object streams(final long id,
      final Continuation<? super List<ChannelStreamEntity>> $completion) {
    final String _sql = "SELECT * FROM channel_streams WHERE channelId = ? ORDER BY failCount ASC, height DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelStreamEntity>>() {
      @Override
      @NonNull
      public List<ChannelStreamEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChannelId = CursorUtil.getColumnIndexOrThrow(_cursor, "channelId");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfReferrer = CursorUtil.getColumnIndexOrThrow(_cursor, "referrer");
          final int _cursorIndexOfUserAgent = CursorUtil.getColumnIndexOrThrow(_cursor, "userAgent");
          final int _cursorIndexOfFailCount = CursorUtil.getColumnIndexOrThrow(_cursor, "failCount");
          final List<ChannelStreamEntity> _result = new ArrayList<ChannelStreamEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelStreamEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpChannelId;
            _tmpChannelId = _cursor.getLong(_cursorIndexOfChannelId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final String _tmpReferrer;
            if (_cursor.isNull(_cursorIndexOfReferrer)) {
              _tmpReferrer = null;
            } else {
              _tmpReferrer = _cursor.getString(_cursorIndexOfReferrer);
            }
            final String _tmpUserAgent;
            if (_cursor.isNull(_cursorIndexOfUserAgent)) {
              _tmpUserAgent = null;
            } else {
              _tmpUserAgent = _cursor.getString(_cursorIndexOfUserAgent);
            }
            final int _tmpFailCount;
            _tmpFailCount = _cursor.getInt(_cursorIndexOfFailCount);
            _item = new ChannelStreamEntity(_tmpId,_tmpChannelId,_tmpUrl,_tmpHeight,_tmpReferrer,_tmpUserAgent,_tmpFailCount);
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

  @Override
  public Object mostWatched(final int limit,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE views > 0 ORDER BY views DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNameNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "nameNorm");
          final int _cursorIndexOfLogo = CursorUtil.getColumnIndexOrThrow(_cursor, "logo");
          final int _cursorIndexOfCountry = CursorUtil.getColumnIndexOrThrow(_cursor, "country");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final int _cursorIndexOfBestHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "bestHeight");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new ChannelEntity(_tmpId,_tmpExt,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight,_tmpViews,_tmpAddedAt);
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

  @Override
  public Object countryFacets(final Continuation<? super List<FacetRow>> $completion) {
    final String _sql = "SELECT country AS k, COUNT(*) AS n FROM channels WHERE country IS NOT NULL GROUP BY country";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FacetRow>>() {
      @Override
      @NonNull
      public List<FacetRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfK = 0;
          final int _cursorIndexOfN = 1;
          final List<FacetRow> _result = new ArrayList<FacetRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FacetRow _item;
            final String _tmpK;
            if (_cursor.isNull(_cursorIndexOfK)) {
              _tmpK = null;
            } else {
              _tmpK = _cursor.getString(_cursorIndexOfK);
            }
            final int _tmpN;
            _tmpN = _cursor.getInt(_cursorIndexOfN);
            _item = new FacetRow(_tmpK,_tmpN);
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

  @Override
  public Object allCategories(final Continuation<? super List<FacetRow>> $completion) {
    final String _sql = "SELECT categories AS k, 1 AS n FROM channels";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FacetRow>>() {
      @Override
      @NonNull
      public List<FacetRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfK = 0;
          final int _cursorIndexOfN = 1;
          final List<FacetRow> _result = new ArrayList<FacetRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FacetRow _item;
            final String _tmpK;
            if (_cursor.isNull(_cursorIndexOfK)) {
              _tmpK = null;
            } else {
              _tmpK = _cursor.getString(_cursorIndexOfK);
            }
            final int _tmpN;
            _tmpN = _cursor.getInt(_cursorIndexOfN);
            _item = new FacetRow(_tmpK,_tmpN);
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

  @Override
  public Object searchIndex(final Continuation<? super List<ChannelIndexRow>> $completion) {
    final String _sql = "SELECT id, name, nameNorm, logo, country, categories, bestHeight FROM channels";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelIndexRow>>() {
      @Override
      @NonNull
      public List<ChannelIndexRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfName = 1;
          final int _cursorIndexOfNameNorm = 2;
          final int _cursorIndexOfLogo = 3;
          final int _cursorIndexOfCountry = 4;
          final int _cursorIndexOfCategories = 5;
          final int _cursorIndexOfBestHeight = 6;
          final List<ChannelIndexRow> _result = new ArrayList<ChannelIndexRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelIndexRow _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpNameNorm;
            _tmpNameNorm = _cursor.getString(_cursorIndexOfNameNorm);
            final String _tmpLogo;
            if (_cursor.isNull(_cursorIndexOfLogo)) {
              _tmpLogo = null;
            } else {
              _tmpLogo = _cursor.getString(_cursorIndexOfLogo);
            }
            final String _tmpCountry;
            if (_cursor.isNull(_cursorIndexOfCountry)) {
              _tmpCountry = null;
            } else {
              _tmpCountry = _cursor.getString(_cursorIndexOfCountry);
            }
            final String _tmpCategories;
            _tmpCategories = _cursor.getString(_cursorIndexOfCategories);
            final int _tmpBestHeight;
            _tmpBestHeight = _cursor.getInt(_cursorIndexOfBestHeight);
            _item = new ChannelIndexRow(_tmpId,_tmpName,_tmpNameNorm,_tmpLogo,_tmpCountry,_tmpCategories,_tmpBestHeight);
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

  @Override
  public Object total(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM channels";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object deleteChannels(final List<Long> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM channels WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteStreamsOf(final List<Long> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM channel_streams WHERE channelId IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
