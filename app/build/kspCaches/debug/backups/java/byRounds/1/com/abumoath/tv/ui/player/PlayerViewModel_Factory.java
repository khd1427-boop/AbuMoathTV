package com.abumoath.tv.ui.player;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.abumoath.tv.data.prefs.AppSettings;
import com.abumoath.tv.data.repo.CatalogRepository;
import com.abumoath.tv.data.repo.LibraryRepository;
import com.abumoath.tv.data.source.ArchiveResolver;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "javax.inject.Named"
})
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<SavedStateHandle> handleProvider;

  private final Provider<Context> contextProvider;

  private final Provider<CatalogRepository> catalogProvider;

  private final Provider<LibraryRepository> libraryProvider;

  private final Provider<ArchiveResolver> resolverProvider;

  private final Provider<AppSettings> settingsProvider;

  private final Provider<OkHttpClient> videoClientProvider;

  public PlayerViewModel_Factory(Provider<SavedStateHandle> handleProvider,
      Provider<Context> contextProvider, Provider<CatalogRepository> catalogProvider,
      Provider<LibraryRepository> libraryProvider, Provider<ArchiveResolver> resolverProvider,
      Provider<AppSettings> settingsProvider, Provider<OkHttpClient> videoClientProvider) {
    this.handleProvider = handleProvider;
    this.contextProvider = contextProvider;
    this.catalogProvider = catalogProvider;
    this.libraryProvider = libraryProvider;
    this.resolverProvider = resolverProvider;
    this.settingsProvider = settingsProvider;
    this.videoClientProvider = videoClientProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(handleProvider.get(), contextProvider.get(), catalogProvider.get(), libraryProvider.get(), resolverProvider.get(), settingsProvider.get(), videoClientProvider.get());
  }

  public static PlayerViewModel_Factory create(
      javax.inject.Provider<SavedStateHandle> handleProvider,
      javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<CatalogRepository> catalogProvider,
      javax.inject.Provider<LibraryRepository> libraryProvider,
      javax.inject.Provider<ArchiveResolver> resolverProvider,
      javax.inject.Provider<AppSettings> settingsProvider,
      javax.inject.Provider<OkHttpClient> videoClientProvider) {
    return new PlayerViewModel_Factory(Providers.asDaggerProvider(handleProvider), Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(catalogProvider), Providers.asDaggerProvider(libraryProvider), Providers.asDaggerProvider(resolverProvider), Providers.asDaggerProvider(settingsProvider), Providers.asDaggerProvider(videoClientProvider));
  }

  public static PlayerViewModel_Factory create(Provider<SavedStateHandle> handleProvider,
      Provider<Context> contextProvider, Provider<CatalogRepository> catalogProvider,
      Provider<LibraryRepository> libraryProvider, Provider<ArchiveResolver> resolverProvider,
      Provider<AppSettings> settingsProvider, Provider<OkHttpClient> videoClientProvider) {
    return new PlayerViewModel_Factory(handleProvider, contextProvider, catalogProvider, libraryProvider, resolverProvider, settingsProvider, videoClientProvider);
  }

  public static PlayerViewModel newInstance(SavedStateHandle handle, Context context,
      CatalogRepository catalog, LibraryRepository library, ArchiveResolver resolver,
      AppSettings settings, OkHttpClient videoClient) {
    return new PlayerViewModel(handle, context, catalog, library, resolver, settings, videoClient);
  }
}
