package com.abumoath.tv.data.repo;

import com.abumoath.tv.data.local.LocalDb;
import com.abumoath.tv.data.prefs.AppSettings;
import com.abumoath.tv.data.source.ArchiveResolver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CatalogRepository_Factory implements Factory<CatalogRepository> {
  private final Provider<LocalDb> dbProvider;

  private final Provider<AppSettings> settingsProvider;

  private final Provider<ArchiveResolver> resolverProvider;

  private final Provider<OkHttpClient> clientProvider;

  public CatalogRepository_Factory(Provider<LocalDb> dbProvider,
      Provider<AppSettings> settingsProvider, Provider<ArchiveResolver> resolverProvider,
      Provider<OkHttpClient> clientProvider) {
    this.dbProvider = dbProvider;
    this.settingsProvider = settingsProvider;
    this.resolverProvider = resolverProvider;
    this.clientProvider = clientProvider;
  }

  @Override
  public CatalogRepository get() {
    return newInstance(dbProvider.get(), settingsProvider.get(), resolverProvider.get(), clientProvider.get());
  }

  public static CatalogRepository_Factory create(javax.inject.Provider<LocalDb> dbProvider,
      javax.inject.Provider<AppSettings> settingsProvider,
      javax.inject.Provider<ArchiveResolver> resolverProvider,
      javax.inject.Provider<OkHttpClient> clientProvider) {
    return new CatalogRepository_Factory(Providers.asDaggerProvider(dbProvider), Providers.asDaggerProvider(settingsProvider), Providers.asDaggerProvider(resolverProvider), Providers.asDaggerProvider(clientProvider));
  }

  public static CatalogRepository_Factory create(Provider<LocalDb> dbProvider,
      Provider<AppSettings> settingsProvider, Provider<ArchiveResolver> resolverProvider,
      Provider<OkHttpClient> clientProvider) {
    return new CatalogRepository_Factory(dbProvider, settingsProvider, resolverProvider, clientProvider);
  }

  public static CatalogRepository newInstance(LocalDb db, AppSettings settings,
      ArchiveResolver resolver, OkHttpClient client) {
    return new CatalogRepository(db, settings, resolver, client);
  }
}
