package com.abumoath.tv.ui.portal;

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
public final class PortalViewModel_Factory implements Factory<PortalViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<CatalogRepository> catalogProvider;

  public PortalViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    this.handleProvider = handleProvider;
    this.catalogProvider = catalogProvider;
  }

  @Override
  public PortalViewModel get() {
    return newInstance(handleProvider.get(), catalogProvider.get());
  }

  public static PortalViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider) {
    return new PortalViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(catalogProvider));
  }

  public static PortalViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider) {
    return new PortalViewModel_Factory(handleProvider, catalogProvider);
  }

  public static PortalViewModel newInstance(SavedStateHandle handle, CatalogRepository catalog) {
    return new PortalViewModel(handle, catalog);
  }
}
