package com.abumoath.tv.ui.library;

import androidx.lifecycle.SavedStateHandle;
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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<LibraryRepository> repoProvider;

  public LibraryViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<LibraryRepository> repoProvider) {
    this.handleProvider = handleProvider;
    this.repoProvider = repoProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(handleProvider.get(), repoProvider.get());
  }

  public static LibraryViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<LibraryRepository> repoProvider) {
    return new LibraryViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(repoProvider));
  }

  public static LibraryViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<LibraryRepository> repoProvider) {
    return new LibraryViewModel_Factory(handleProvider, repoProvider);
  }

  public static LibraryViewModel newInstance(SavedStateHandle handle, LibraryRepository repo) {
    return new LibraryViewModel(handle, repo);
  }
}
