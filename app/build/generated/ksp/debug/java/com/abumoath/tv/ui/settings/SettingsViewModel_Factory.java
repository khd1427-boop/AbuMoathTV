package com.abumoath.tv.ui.settings;

import com.abumoath.tv.data.prefs.AppSettings;
import com.abumoath.tv.data.repo.CatalogRepository;
import com.abumoath.tv.data.repo.LibraryRepository;
import com.abumoath.tv.data.source.ContentSync;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AppSettings> settingsProvider;

  private final Provider<CatalogRepository> catalogProvider;

  private final Provider<LibraryRepository> libraryProvider;

  private final Provider<ContentSync> syncProvider;

  public SettingsViewModel_Factory(Provider<AppSettings> settingsProvider,
      Provider<CatalogRepository> catalogProvider, Provider<LibraryRepository> libraryProvider,
      Provider<ContentSync> syncProvider) {
    this.settingsProvider = settingsProvider;
    this.catalogProvider = catalogProvider;
    this.libraryProvider = libraryProvider;
    this.syncProvider = syncProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsProvider.get(), catalogProvider.get(), libraryProvider.get(), syncProvider.get());
  }

  public static SettingsViewModel_Factory create(
      javax.inject.Provider<AppSettings> settingsProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider,
      javax.inject.Provider<LibraryRepository> libraryProvider,
      javax.inject.Provider<ContentSync> syncProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(settingsProvider), Providers.asDaggerProvider(catalogProvider), Providers.asDaggerProvider(libraryProvider), Providers.asDaggerProvider(syncProvider));
  }

  public static SettingsViewModel_Factory create(Provider<AppSettings> settingsProvider,
      Provider<CatalogRepository> catalogProvider, Provider<LibraryRepository> libraryProvider,
      Provider<ContentSync> syncProvider) {
    return new SettingsViewModel_Factory(settingsProvider, catalogProvider, libraryProvider, syncProvider);
  }

  public static SettingsViewModel newInstance(AppSettings settings, CatalogRepository catalog,
      LibraryRepository library, ContentSync sync) {
    return new SettingsViewModel(settings, catalog, library, sync);
  }
}
