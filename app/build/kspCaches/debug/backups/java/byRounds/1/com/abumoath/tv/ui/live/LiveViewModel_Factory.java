package com.abumoath.tv.ui.live;

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
public final class LiveViewModel_Factory implements Factory<LiveViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<CatalogRepository> catalogProvider;

  public LiveViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    this.handleProvider = handleProvider;
    this.catalogProvider = catalogProvider;
  }

  @Override
  public LiveViewModel get() {
    return newInstance(handleProvider.get(), catalogProvider.get());
  }

  public static LiveViewModel_Factory create(javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider) {
    return new LiveViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(catalogProvider));
  }

  public static LiveViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    return new LiveViewModel_Factory(handleProvider, catalogProvider);
  }

  public static LiveViewModel newInstance(SavedStateHandle handle, CatalogRepository catalog) {
    return new LiveViewModel(handle, catalog);
  }
}
