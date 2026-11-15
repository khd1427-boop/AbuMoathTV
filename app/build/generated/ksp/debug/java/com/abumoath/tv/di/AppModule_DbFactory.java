package com.abumoath.tv.di;

import android.content.Context;
import com.abumoath.tv.data.local.LocalDb;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_DbFactory implements Factory<LocalDb> {
  private final Provider<Context> contextProvider;

  public AppModule_DbFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocalDb get() {
    return db(contextProvider.get());
  }

  public static AppModule_DbFactory create(javax.inject.Provider<Context> contextProvider) {
    return new AppModule_DbFactory(Providers.asDaggerProvider(contextProvider));
  }

  public static AppModule_DbFactory create(Provider<Context> contextProvider) {
    return new AppModule_DbFactory(contextProvider);
  }

  public static LocalDb db(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.db(context));
  }
}
