package com.abumoath.tv.ui.browse;

import androidx.lifecycle.SavedStateHandle;
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
public final class BrowseViewModel_Factory implements Factory<BrowseViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<CatalogRepository> catalogProvider;

  public BrowseViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    this.handleProvider = handleProvider;
    this.catalogProvider = catalogProvider;
  }

  @Override
  public BrowseViewModel get() {
    return newInstance(handleProvider.get(), catalogProvider.get());
  }

  public static BrowseViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider) {
    return new BrowseViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(catalogProvider));
  }

  public static BrowseViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    return new BrowseViewModel_Factory(handleProvider, catalogProvider);
  }

  public static BrowseViewModel newInstance(SavedStateHandle handle, CatalogRepository catalog) {
    return new BrowseViewModel(handle, catalog);
  }
}
