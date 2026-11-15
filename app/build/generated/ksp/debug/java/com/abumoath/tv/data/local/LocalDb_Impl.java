package com.abumoath.tv.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LocalDb_Impl extends LocalDb {
  private volatile ChannelDao _channelDao;

  private volatile TitleDao _titleDao;

  private volatile ProgressDao _progressDao;

  private volatile LibraryDao _libraryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `channels` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ext` TEXT NOT NULL, `name` TEXT NOT NULL, `nameNorm` TEXT NOT NULL, `logo` TEXT, `country` TEXT, `categories` TEXT NOT NULL, `bestHeight` INTEGER NOT NULL, `views` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_channels_ext` ON `channels` (`ext`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_channels_country` ON `channels` (`country`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `channel_streams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `channelId` INTEGER NOT NULL, `url` TEXT NOT NULL, `height` INTEGER NOT NULL, `referrer` TEXT, `userAgent` TEXT, `failCount` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_channel_streams_channelId` ON `channel_streams` (`channelId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `titles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ext` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `titleNorm` TEXT NOT NULL, `year` INTEGER, `overview` TEXT, `language` TEXT, `runtimeMin` INTEGER, `genres` TEXT NOT NULL, `directors` TEXT, `license` TEXT, `popularity` INTEGER NOT NULL, `views` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_titles_ext` ON `titles` (`ext`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `progress` (`itemType` TEXT NOT NULL, `itemId` INTEGER NOT NULL, `titleId` INTEGER NOT NULL, `positionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`itemType`, `itemId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `library` (`list` TEXT NOT NULL, `itemType` TEXT NOT NULL, `itemId` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`list`, `itemType`, `itemId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3f4ef0823a235dec306fddc316cf47db')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `channels`");
        db.execSQL("DROP TABLE IF EXISTS `channel_streams`");
        db.execSQL("DROP TABLE IF EXISTS `titles`");
        db.execSQL("DROP TABLE IF EXISTS `progress`");
        db.execSQL("DROP TABLE IF EXISTS `library`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsChannels = new HashMap<String, TableInfo.Column>(10);
        _columnsChannels.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("ext", new TableInfo.Column("ext", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("nameNorm", new TableInfo.Column("nameNorm", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("logo", new TableInfo.Column("logo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("country", new TableInfo.Column("country", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("categories", new TableInfo.Column("categories", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("bestHeight", new TableInfo.Column("bestHeight", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("views", new TableInfo.Column("views", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChannels = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChannels = new HashSet<TableInfo.Index>(2);
        _indicesChannels.add(new TableInfo.Index("index_channels_ext", true, Arrays.asList("ext"), Arrays.asList("ASC")));
        _indicesChannels.add(new TableInfo.Index("index_channels_country", false, Arrays.asList("country"), Arrays.asList("ASC")));
        final TableInfo _infoChannels = new TableInfo("channels", _columnsChannels, _foreignKeysChannels, _indicesChannels);
        final TableInfo _existingChannels = TableInfo.read(db, "channels");
        if (!_infoChannels.equals(_existingChannels)) {
          return new RoomOpenHelper.ValidationResult(false, "channels(com.abumoath.tv.data.local.ChannelEntity).\n"
                  + " Expected:\n" + _infoChannels + "\n"
                  + " Found:\n" + _existingChannels);
        }
        final HashMap<String, TableInfo.Column> _columnsChannelStreams = new HashMap<String, TableInfo.Column>(7);
        _columnsChannelStreams.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("channelId", new TableInfo.Column("channelId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("url", new TableInfo.Column("url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("height", new TableInfo.Column("height", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("referrer", new TableInfo.Column("referrer", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("userAgent", new TableInfo.Column("userAgent", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannelStreams.put("failCount", new TableInfo.Column("failCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChannelStreams = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChannelStreams = new HashSet<TableInfo.Index>(1);
        _indicesChannelStreams.add(new TableInfo.Index("index_channel_streams_channelId", false, Arrays.asList("channelId"), Arrays.asList("ASC")));
        final TableInfo _infoChannelStreams = new TableInfo("channel_streams", _columnsChannelStreams, _foreignKeysChannelStreams, _indicesChannelStreams);
        final TableInfo _existingChannelStreams = TableInfo.read(db, "channel_streams");
        if (!_infoChannelStreams.equals(_existingChannelStreams)) {
          return new RoomOpenHelper.ValidationResult(false, "channel_streams(com.abumoath.tv.data.local.ChannelStreamEntity).\n"
                  + " Expected:\n" + _infoChannelStreams + "\n"
                  + " Found:\n" + _existingChannelStreams);
        }
        final HashMap<String, TableInfo.Column> _columnsTitles = new HashMap<String, TableInfo.Column>(15);
        _columnsTitles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("ext", new TableInfo.Column("ext", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("titleNorm", new TableInfo.Column("titleNorm", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("year", new TableInfo.Column("year", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("overview", new TableInfo.Column("overview", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("language", new TableInfo.Column("language", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("runtimeMin", new TableInfo.Column("runtimeMin", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("genres", new TableInfo.Column("genres", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("directors", new TableInfo.Column("directors", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("license", new TableInfo.Column("license", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("popularity", new TableInfo.Column("popularity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("views", new TableInfo.Column("views", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTitles.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTitles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTitles = new HashSet<TableInfo.Index>(1);
        _indicesTitles.add(new TableInfo.Index("index_titles_ext", true, Arrays.asList("ext"), Arrays.asList("ASC")));
        final TableInfo _infoTitles = new TableInfo("titles", _columnsTitles, _foreignKeysTitles, _indicesTitles);
        final TableInfo _existingTitles = TableInfo.read(db, "titles");
        if (!_infoTitles.equals(_existingTitles)) {
          return new RoomOpenHelper.ValidationResult(false, "titles(com.abumoath.tv.data.local.TitleEntity).\n"
                  + " Expected:\n" + _infoTitles + "\n"
                  + " Found:\n" + _existingTitles);
        }
        final HashMap<String, TableInfo.Column> _columnsProgress = new HashMap<String, TableInfo.Column>(7);
        _columnsProgress.put("itemType", new TableInfo.Column("itemType", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("itemId", new TableInfo.Column("itemId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("titleId", new TableInfo.Column("titleId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("positionMs", new TableInfo.Column("positionMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("completed", new TableInfo.Column("completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProgress.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProgress = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProgress = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProgress = new TableInfo("progress", _columnsProgress, _foreignKeysProgress, _indicesProgress);
        final TableInfo _existingProgress = TableInfo.read(db, "progress");
        if (!_infoProgress.equals(_existingProgress)) {
          return new RoomOpenHelper.ValidationResult(false, "progress(com.abumoath.tv.data.local.ProgressEntity).\n"
                  + " Expected:\n" + _infoProgress + "\n"
                  + " Found:\n" + _existingProgress);
        }
        final HashMap<String, TableInfo.Column> _columnsLibrary = new HashMap<String, TableInfo.Column>(4);
        _columnsLibrary.put("list", new TableInfo.Column("list", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrary.put("itemType", new TableInfo.Column("itemType", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrary.put("itemId", new TableInfo.Column("itemId", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrary.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLibrary = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLibrary = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLibrary = new TableInfo("library", _columnsLibrary, _foreignKeysLibrary, _indicesLibrary);
        final TableInfo _existingLibrary = TableInfo.read(db, "library");
        if (!_infoLibrary.equals(_existingLibrary)) {
          return new RoomOpenHelper.ValidationResult(false, "library(com.abumoath.tv.data.local.LibraryEntity).\n"
                  + " Expected:\n" + _infoLibrary + "\n"
                  + " Found:\n" + _existingLibrary);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "3f4ef0823a235dec306fddc316cf47db", "aace03bb8926f2347e047050bb74cb87");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "channels","channel_streams","titles","progress","library");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `channels`");
      _db.execSQL("DELETE FROM `channel_streams`");
      _db.execSQL("DELETE FROM `titles`");
      _db.execSQL("DELETE FROM `progress`");
      _db.execSQL("DELETE FROM `library`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ChannelDao.class, ChannelDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TitleDao.class, TitleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProgressDao.class, ProgressDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LibraryDao.class, LibraryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ChannelDao channels() {
    if (_channelDao != null) {
      return _channelDao;
    } else {
      synchronized(this) {
        if(_channelDao == null) {
          _channelDao = new ChannelDao_Impl(this);
        }
        return _channelDao;
      }
    }
  }

  @Override
  public TitleDao titles() {
    if (_titleDao != null) {
      return _titleDao;
    } else {
      synchronized(this) {
        if(_titleDao == null) {
          _titleDao = new TitleDao_Impl(this);
        }
        return _titleDao;
      }
    }
  }

  @Override
  public ProgressDao progress() {
    if (_progressDao != null) {
      return _progressDao;
    } else {
      synchronized(this) {
        if(_progressDao == null) {
          _progressDao = new ProgressDao_Impl(this);
        }
        return _progressDao;
      }
    }
  }

  @Override
  public LibraryDao library() {
    if (_libraryDao != null) {
      return _libraryDao;
    } else {
      synchronized(this) {
        if(_libraryDao == null) {
          _libraryDao = new LibraryDao_Impl(this);
        }
        return _libraryDao;
      }
    }
  }
}
