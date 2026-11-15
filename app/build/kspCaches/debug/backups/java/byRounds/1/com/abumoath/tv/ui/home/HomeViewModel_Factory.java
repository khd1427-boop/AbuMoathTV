package com.abumoath.tv.ui.home;

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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CatalogRepository> catalogProvider;

  public HomeViewModel_Factory(Provider<CatalogRepository> catalogProvider) {
    this.catalogProvider = catalogProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(catalogProvider.get());
  }

  public static HomeViewModel_Factory create(
      javax.inject.Provider<CatalogRepository> catalogProvider) {
    return new HomeViewModel_Factory(Providers.asDaggerProvider(catalogProvider));
  }

  public static HomeViewModel_Factory create(Provider<CatalogRepository> catalogProvider) {
    return new HomeViewModel_Factory(catalogProvider);
  }

  public static HomeViewModel newInstance(CatalogRepository catalog) {
    return new HomeViewModel(catalog);
  }
}
