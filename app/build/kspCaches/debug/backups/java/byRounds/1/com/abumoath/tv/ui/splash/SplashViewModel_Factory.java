package com.abumoath.tv.ui.splash;

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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<ContentSync> syncProvider;

  public SplashViewModel_Factory(Provider<ContentSync> syncProvider) {
    this.syncProvider = syncProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(syncProvider.get());
  }

  public static SplashViewModel_Factory create(javax.inject.Provider<ContentSync> syncProvider) {
    return new SplashViewModel_Factory(Providers.asDaggerProvider(syncProvider));
  }

  public static SplashViewModel_Factory create(Provider<ContentSync> syncProvider) {
    return new SplashViewModel_Factory(syncProvider);
  }

  public static SplashViewModel newInstance(ContentSync sync) {
    return new SplashViewModel(sync);
  }
}
