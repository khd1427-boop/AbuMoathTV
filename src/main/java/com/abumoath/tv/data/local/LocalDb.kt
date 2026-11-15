package com.abumoath.tv.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** A live TV channel (from the iptv-org public index). `ext` = upstream id, e.g. "AlJazeera.qa". */
@Entity(tableName = "channels", indices = [Index(value = ["ext"], unique = true), Index("country")])
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ext: String,
    val name: String,
    val nameNorm: String,
    val logo: String?,
    val country: String?,
    val categories: String,      // ",news,sports,"
    val bestHeight: Int,
    val views: Int = 0,
    val addedAt: Long
)

@Entity(tableName = "channel_streams", indices = [Index("channelId")])
data class ChannelStreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long,
    val url: String,
    val height: Int,
    val referrer: String?,
    val userAgent: String?,
    val failCount: Int = 0
)

/** A public-domain / Creative-Commons film from the Internet Archive. `ext` = archive identifier. */
@Entity(tableName = "titles", indices = [Index(value = ["ext"], unique = true)])
data class TitleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ext: String,
    val type: String = "movie",
    val title: String,
    val titleNorm: String,
    val year: Int?,
    val overview: String?,
    val language: String?,
    val runtimeMin: Int?,
    val genres: String,          // ",drama,comedy,"
    val directors: String?,
    val license: String?,
    val popularity: Int,
    val views: Int = 0,
    val addedAt: Long
)

@Entity(tableName = "progress", primaryKeys = ["itemType", "itemId"])
data class ProgressEntity(
    val itemType: String,        // title
    val itemId: Long,
    val titleId: Long,
    val positionMs: Long,
    val durationMs: Long,
    val completed: Boolean,
    val updatedAt: Long
)

@Entity(tableName = "library", primaryKeys = ["list", "itemType", "itemId"])
data class LibraryEntity(val list: String, val itemType: String, val itemId: Long, val addedAt: Long)

// ================== بداية إضافة الاشتراكات ==================

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,       // اسم الاشتراك
    val type: String,       // "M3U" أو "XTREAM"
    val url: String?,       // رابط M3U
    val server: String?,    // خادم Xtream
    val username: String?,
    val password: String?,
    val lastUpdated: Long
)

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun getAll(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)
}

// ================== نهاية إضافة الاشتراكات ==================

data class FacetRow(val k: String?, val n: Int)
data class IdExt(val id: Long, val ext: String)

@Dao
interface ChannelDao {
    @Query("SELECT id, ext FROM channels") suspend fun idMap(): List<IdExt>
    @Query("SELECT * FROM channels") suspend fun all(): List<ChannelEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: ChannelEntity): Long
    @Query("DELETE FROM channel_streams WHERE channelId = :id") suspend fun deleteStreams(id: Long)
    @Insert suspend fun insertStreams(s: List<ChannelStreamEntity>)
    @Query("DELETE FROM channels WHERE id IN (:ids)") suspend fun deleteChannels(ids: List<Long>)
    @Query("DELETE FROM channel_streams WHERE channelId IN (:ids)") suspend fun deleteStreamsOf(ids: List<Long>)

    @Query("""SELECT * FROM channels WHERE (:country IS NULL OR country = :country) AND (:cat IS NULL OR categories LIKE '%,' || :cat || ',%')
        ORDER BY views DESC, CASE WHEN country IN ('SA','AE','KW','QA','BH','OM','EG','IQ','JO','PS','SY','LB','MA','DZ','TN','SD','YE') THEN 0 ELSE 1 END, name LIMIT :limit OFFSET :offset""")
    suspend fun list(country: String?, cat: String?, limit: Int, offset: Int): List<ChannelEntity>
    @Query("SELECT COUNT(*) FROM channels WHERE (:country IS NULL OR country = :country) AND (:cat IS NULL OR categories LIKE '%,' || :cat || ',%')")
    suspend fun count(country: String?, cat: String?): Int
    @Query("SELECT * FROM channels WHERE country IN (:countries) AND (:cat IS NULL OR categories LIKE '%,' || :cat || ',%') ORDER BY views DESC, name LIMIT :limit")
    suspend fun inCountries(countries: List<String>, cat: String?, limit: Int): List<ChannelEntity>
    @Query("SELECT * FROM channels WHERE id = :id") suspend fun get(id: Long): ChannelEntity?
    @Query("SELECT * FROM channels WHERE id IN (:ids)") suspend fun getMany(ids: List<Long>): List<ChannelEntity>
    @Query("SELECT * FROM channel_streams WHERE channelId = :id ORDER BY failCount ASC, height DESC") suspend fun streams(id: Long): List<ChannelStreamEntity>
    @Query("UPDATE channel_streams SET failCount = failCount + 1 WHERE id = :id") suspend fun markFailed(id: Long)
    @Query("UPDATE channel_streams SET failCount = 0 WHERE id = :id") suspend fun markOk(id: Long)
    @Query("UPDATE channels SET views = views + 1 WHERE id = :id") suspend fun addView(id: Long)
    @Query("SELECT country AS k, COUNT(*) AS n FROM channels WHERE country IS NOT NULL GROUP BY country") suspend fun countryFacets(): List<FacetRow>
    @Query("SELECT categories AS k, 1 AS n FROM channels") suspend fun allCategories(): List<FacetRow>
    @Query("SELECT id, name, nameNorm, logo, country, categories, bestHeight FROM channels") suspend fun searchIndex(): List<ChannelIndexRow>
    @Query("SELECT COUNT(*) FROM channels") suspend fun total(): Int
}

data class ChannelIndexRow(val id: Long, val name: String, val nameNorm: String, val logo: String?, val country: String?, val categories: String, val bestHeight: Int)

@Dao
interface TitleDao {
    @Query("SELECT id, ext FROM titles") suspend fun idMap(): List<IdExt>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(t: TitleEntity): Long
    @Query("SELECT * FROM titles WHERE id = :id") suspend fun get(id: Long): TitleEntity?
    @Query("SELECT * FROM titles WHERE id IN (:ids)") suspend fun getMany(ids: List<Long>): List<TitleEntity>
    @Query("UPDATE titles SET views = views + 1 WHERE id = :id") suspend fun addView(id: Long)
    @Query("SELECT * FROM titles") suspend fun all(): List<TitleEntity>
    @Query("SELECT COUNT(*) FROM titles") suspend fun total(): Int
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: ProgressEntity)
    @Query("SELECT * FROM progress WHERE itemType = :type AND itemId = :id") suspend fun get(type: String, id: Long): ProgressEntity?
    @Query("SELECT * FROM progress ORDER BY updatedAt DESC LIMIT 200") suspend fun recent(): List<ProgressEntity>
    @Query("DELETE FROM progress WHERE titleId = :titleId") suspend fun deleteForTitle(titleId: Long)
    @Query("DELETE FROM progress") suspend fun clear()
}

@Dao
interface LibraryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun add(e: LibraryEntity)
    @Query("DELETE FROM library WHERE list = :list AND itemType = :type AND itemId = :id") suspend fun remove(list: String, type: String, id: Long)
    @Query("SELECT * FROM library WHERE list = :list ORDER BY addedAt DESC") suspend fun list(list: String): List<LibraryEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM library WHERE list = :list AND itemType = :type AND itemId = :id)") fun observeContains(list: String, type: String, id: Long): Flow<Boolean>
}

@Database(
    entities = [ChannelEntity::class, ChannelStreamEntity::class, TitleEntity::class, ProgressEntity::class, LibraryEntity::class, SubscriptionEntity::class],
    version = 3, exportSchema = false
)
abstract class LocalDb : RoomDatabase() {
    abstract fun channels(): ChannelDao
    abstract fun titles(): TitleDao
    abstract fun progress(): ProgressDao
    abstract fun library(): LibraryDao
    abstract fun subscriptionDao(): SubscriptionDao // <--- تمت إضافة هذا السطر
}