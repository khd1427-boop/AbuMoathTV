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
public final class TitleDao_Impl implements TitleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TitleEntity> __insertionAdapterOfTitleEntity;

  private final SharedSQLiteStatement __preparedStmtOfAddView;

  public TitleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTitleEntity = new EntityInsertionAdapter<TitleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `titles` (`id`,`ext`,`type`,`title`,`titleNorm`,`year`,`overview`,`language`,`runtimeMin`,`genres`,`directors`,`license`,`popularity`,`views`,`addedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TitleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getExt());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getTitle());
        statement.bindString(5, entity.getTitleNorm());
        if (entity.getYear() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getYear());
        }
        if (entity.getOverview() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getOverview());
        }
        if (entity.getLanguage() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getLanguage());
        }
        if (entity.getRuntimeMin() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getRuntimeMin());
        }
        statement.bindString(10, entity.getGenres());
        if (entity.getDirectors() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDirectors());
        }
        if (entity.getLicense() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getLicense());
        }
        statement.bindLong(13, entity.getPopularity());
        statement.bindLong(14, entity.getViews());
        statement.bindLong(15, entity.getAddedAt());
      }
    };
    this.__preparedStmtOfAddView = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE titles SET views = views + 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final TitleEntity t, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTitleEntity.insertAndReturnId(t);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
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
    final String _sql = "SELECT id, ext FROM titles";
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
  public Object get(final long id, final Continuation<? super TitleEntity> $completion) {
    final String _sql = "SELECT * FROM titles WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TitleEntity>() {
      @Override
      @Nullable
      public TitleEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTitleNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "titleNorm");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfRuntimeMin = CursorUtil.getColumnIndexOrThrow(_cursor, "runtimeMin");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfDirectors = CursorUtil.getColumnIndexOrThrow(_cursor, "directors");
          final int _cursorIndexOfLicense = CursorUtil.getColumnIndexOrThrow(_cursor, "license");
          final int _cursorIndexOfPopularity = CursorUtil.getColumnIndexOrThrow(_cursor, "popularity");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final TitleEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpTitleNorm;
            _tmpTitleNorm = _cursor.getString(_cursorIndexOfTitleNorm);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpOverview;
            if (_cursor.isNull(_cursorIndexOfOverview)) {
              _tmpOverview = null;
            } else {
              _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final Integer _tmpRuntimeMin;
            if (_cursor.isNull(_cursorIndexOfRuntimeMin)) {
              _tmpRuntimeMin = null;
            } else {
              _tmpRuntimeMin = _cursor.getInt(_cursorIndexOfRuntimeMin);
            }
            final String _tmpGenres;
            _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            final String _tmpDirectors;
            if (_cursor.isNull(_cursorIndexOfDirectors)) {
              _tmpDirectors = null;
            } else {
              _tmpDirectors = _cursor.getString(_cursorIndexOfDirectors);
            }
            final String _tmpLicense;
            if (_cursor.isNull(_cursorIndexOfLicense)) {
              _tmpLicense = null;
            } else {
              _tmpLicense = _cursor.getString(_cursorIndexOfLicense);
            }
            final int _tmpPopularity;
            _tmpPopularity = _cursor.getInt(_cursorIndexOfPopularity);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _result = new TitleEntity(_tmpId,_tmpExt,_tmpType,_tmpTitle,_tmpTitleNorm,_tmpYear,_tmpOverview,_tmpLanguage,_tmpRuntimeMin,_tmpGenres,_tmpDirectors,_tmpLicense,_tmpPopularity,_tmpViews,_tmpAddedAt);
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
      final Continuation<? super List<TitleEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM titles WHERE id IN (");
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
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TitleEntity>>() {
      @Override
      @NonNull
      public List<TitleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTitleNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "titleNorm");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfRuntimeMin = CursorUtil.getColumnIndexOrThrow(_cursor, "runtimeMin");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfDirectors = CursorUtil.getColumnIndexOrThrow(_cursor, "directors");
          final int _cursorIndexOfLicense = CursorUtil.getColumnIndexOrThrow(_cursor, "license");
          final int _cursorIndexOfPopularity = CursorUtil.getColumnIndexOrThrow(_cursor, "popularity");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<TitleEntity> _result = new ArrayList<TitleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TitleEntity _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpTitleNorm;
            _tmpTitleNorm = _cursor.getString(_cursorIndexOfTitleNorm);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpOverview;
            if (_cursor.isNull(_cursorIndexOfOverview)) {
              _tmpOverview = null;
            } else {
              _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final Integer _tmpRuntimeMin;
            if (_cursor.isNull(_cursorIndexOfRuntimeMin)) {
              _tmpRuntimeMin = null;
            } else {
              _tmpRuntimeMin = _cursor.getInt(_cursorIndexOfRuntimeMin);
            }
            final String _tmpGenres;
            _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            final String _tmpDirectors;
            if (_cursor.isNull(_cursorIndexOfDirectors)) {
              _tmpDirectors = null;
            } else {
              _tmpDirectors = _cursor.getString(_cursorIndexOfDirectors);
            }
            final String _tmpLicense;
            if (_cursor.isNull(_cursorIndexOfLicense)) {
              _tmpLicense = null;
            } else {
              _tmpLicense = _cursor.getString(_cursorIndexOfLicense);
            }
            final int _tmpPopularity;
            _tmpPopularity = _cursor.getInt(_cursorIndexOfPopularity);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item_1 = new TitleEntity(_tmpId,_tmpExt,_tmpType,_tmpTitle,_tmpTitleNorm,_tmpYear,_tmpOverview,_tmpLanguage,_tmpRuntimeMin,_tmpGenres,_tmpDirectors,_tmpLicense,_tmpPopularity,_tmpViews,_tmpAddedAt);
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
  public Object all(final Continuation<? super List<TitleEntity>> $completion) {
    final String _sql = "SELECT * FROM titles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TitleEntity>>() {
      @Override
      @NonNull
      public List<TitleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExt = CursorUtil.getColumnIndexOrThrow(_cursor, "ext");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTitleNorm = CursorUtil.getColumnIndexOrThrow(_cursor, "titleNorm");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfOverview = CursorUtil.getColumnIndexOrThrow(_cursor, "overview");
          final int _cursorIndexOfLanguage = CursorUtil.getColumnIndexOrThrow(_cursor, "language");
          final int _cursorIndexOfRuntimeMin = CursorUtil.getColumnIndexOrThrow(_cursor, "runtimeMin");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfDirectors = CursorUtil.getColumnIndexOrThrow(_cursor, "directors");
          final int _cursorIndexOfLicense = CursorUtil.getColumnIndexOrThrow(_cursor, "license");
          final int _cursorIndexOfPopularity = CursorUtil.getColumnIndexOrThrow(_cursor, "popularity");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final List<TitleEntity> _result = new ArrayList<TitleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TitleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpExt;
            _tmpExt = _cursor.getString(_cursorIndexOfExt);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpTitleNorm;
            _tmpTitleNorm = _cursor.getString(_cursorIndexOfTitleNorm);
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpOverview;
            if (_cursor.isNull(_cursorIndexOfOverview)) {
              _tmpOverview = null;
            } else {
              _tmpOverview = _cursor.getString(_cursorIndexOfOverview);
            }
            final String _tmpLanguage;
            if (_cursor.isNull(_cursorIndexOfLanguage)) {
              _tmpLanguage = null;
            } else {
              _tmpLanguage = _cursor.getString(_cursorIndexOfLanguage);
            }
            final Integer _tmpRuntimeMin;
            if (_cursor.isNull(_cursorIndexOfRuntimeMin)) {
              _tmpRuntimeMin = null;
            } else {
              _tmpRuntimeMin = _cursor.getInt(_cursorIndexOfRuntimeMin);
            }
            final String _tmpGenres;
            _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            final String _tmpDirectors;
            if (_cursor.isNull(_cursorIndexOfDirectors)) {
              _tmpDirectors = null;
            } else {
              _tmpDirectors = _cursor.getString(_cursorIndexOfDirectors);
            }
            final String _tmpLicense;
            if (_cursor.isNull(_cursorIndexOfLicense)) {
              _tmpLicense = null;
            } else {
              _tmpLicense = _cursor.getString(_cursorIndexOfLicense);
            }
            final int _tmpPopularity;
            _tmpPopularity = _cursor.getInt(_cursorIndexOfPopularity);
            final int _tmpViews;
            _tmpViews = _cursor.getInt(_cursorIndexOfViews);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            _item = new TitleEntity(_tmpId,_tmpExt,_tmpType,_tmpTitle,_tmpTitleNorm,_tmpYear,_tmpOverview,_tmpLanguage,_tmpRuntimeMin,_tmpGenres,_tmpDirectors,_tmpLicense,_tmpPopularity,_tmpViews,_tmpAddedAt);
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
    final String _sql = "SELECT COUNT(*) FROM titles";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
