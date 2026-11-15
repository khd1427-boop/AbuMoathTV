package com.abumoath.tv.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_BaseClientFactory implements Factory<OkHttpClient> {
  @Override
  public OkHttpClient get() {
    return baseClient();
  }

  public static AppModule_BaseClientFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OkHttpClient baseClient() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.baseClient());
  }

  private static final class InstanceHolder {
    static final AppModule_BaseClientFactory INSTANCE = new AppModule_BaseClientFactory();
  }
}
