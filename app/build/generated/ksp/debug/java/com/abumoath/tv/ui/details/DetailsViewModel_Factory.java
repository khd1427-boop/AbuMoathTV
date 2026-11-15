package com.abumoath.tv.ui.details;

import androidx.lifecycle.SavedStateHandle;
import com.abumoath.tv.data.repo.CatalogRepository;
import com.abumoath.tv.data.repo.LibraryRepository;
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
public final class DetailsViewModel_Factory implements Factory<DetailsViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<CatalogRepository> catalogProvider;

  private final Provider<LibraryRepository> libraryProvider;

  public DetailsViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider, Provider<LibraryRepository> libraryProvider) {
    this.handleProvider = handleProvider;
    this.catalogProvider = catalogProvider;
    this.libraryProvider = libraryProvider;
  }

  @Override
  public DetailsViewModel get() {
    return newInstance(handleProvider.get(), catalogProvider.get(), libraryProvider.get());
  }

  public static DetailsViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider,
      javax.inject.Provider<LibraryRepository> libraryProvider) {
    return new DetailsViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(catalogProvider), Providers.asDaggerProvider(libraryProvider));
  }

  public static DetailsViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<CatalogRepository> catalogProvider, Provider<LibraryRepository> libraryProvider) {
    return new DetailsViewModel_Factory(handleProvider, catalogProvider, libraryProvider);
  }

  public static DetailsViewModel newInstance(SavedStateHandle handle, CatalogRepository catalog,
      LibraryRepository library) {
    return new DetailsViewModel(handle, catalog, library);
  }
}
