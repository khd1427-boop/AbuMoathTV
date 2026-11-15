package com.abumoath.tv.data.repo;

import com.abumoath.tv.data.local.LocalDb;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class LibraryRepository_Factory implements Factory<LibraryRepository> {
  private final Provider<LocalDb> dbProvider;

  private final Provider<CatalogRepository> catalogProvider;

  public LibraryRepository_Factory(Provider<LocalDb> dbProvider,
      Provider<CatalogRepository> catalogProvider) {
    this.dbProvider = dbProvider;
    this.catalogProvider = catalogProvider;
  }

  @Override
  public LibraryRepository get() {
    return newInstance(dbProvider.get(), catalogProvider.get());
  }

  public static LibraryRepository_Factory create(javax.inject.Provider<LocalDb> dbProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider) {
    return new LibraryRepository_Factory(Providers.asDaggerProvider(dbProvider), Providers.asDaggerProvider(catalogProvider));
  }

  public static LibraryRepository_Factory create(Provider<LocalDb> dbProvider,
      Provider<CatalogRepository> catalogProvider) {
    return new LibraryRepository_Factory(dbProvider, catalogProvider);
  }

  public static LibraryRepository newInstance(LocalDb db, CatalogRepository catalog) {
    return new LibraryRepository(db, catalog);
  }
}
