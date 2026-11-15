package com.abumoath.tv.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class AppModule_VideoClientFactory implements Factory<OkHttpClient> {
  private final Provider<OkHttpClient> baseProvider;

  public AppModule_VideoClientFactory(Provider<OkHttpClient> baseProvider) {
    this.baseProvider = baseProvider;
  }

  @Override
  public OkHttpClient get() {
    return videoClient(baseProvider.get());
  }

  public static AppModule_VideoClientFactory create(
      javax.inject.Provider<OkHttpClient> baseProvider) {
    return new AppModule_VideoClientFactory(Providers.asDaggerProvider(baseProvider));
  }

  public static AppModule_VideoClientFactory create(Provider<OkHttpClient> baseProvider) {
    return new AppModule_VideoClientFactory(baseProvider);
  }

  public static OkHttpClient videoClient(OkHttpClient base) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.videoClient(base));
  }
}
