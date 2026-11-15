package com.abumoath.tv.ui.search;

import com.abumoath.tv.data.prefs.AppSettings;
import com.abumoath.tv.data.repo.CatalogRepository;
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<CatalogRepository> catalogProvider;

  private final Provider<AppSettings> settingsProvider;

  public SearchViewModel_Factory(Provider<CatalogRepository> catalogProvider,
      Provider<AppSettings> settingsProvider) {
    this.catalogProvider = catalogProvider;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(catalogProvider.get(), settingsProvider.get());
  }

  public static SearchViewModel_Factory create(
      javax.inject.Provider<CatalogRepository> catalogProvider,
      javax.inject.Provider<AppSettings> settingsProvider) {
    return new SearchViewModel_Factory(Providers.asDaggerProvider(catalogProvider), Providers.asDaggerProvider(settingsProvider));
  }

  public static SearchViewModel_Factory create(Provider<CatalogRepository> catalogProvider,
      Provider<AppSettings> settingsProvider) {
    return new SearchViewModel_Factory(catalogProvider, settingsProvider);
  }

  public static SearchViewModel newInstance(CatalogRepository catalog, AppSettings settings) {
    return new SearchViewModel(catalog, settings);
  }
}
