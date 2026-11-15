package com.abumoath.tv.data.source;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ArchiveResolver_Factory implements Factory<ArchiveResolver> {
  private final Provider<Context> contextProvider;

  private final Provider<OkHttpClient> clientProvider;

  public ArchiveResolver_Factory(Provider<Context> contextProvider,
      Provider<OkHttpClient> clientProvider) {
    this.contextProvider = contextProvider;
    this.clientProvider = clientProvider;
  }

  @Override
  public ArchiveResolver get() {
    return newInstance(contextProvider.get(), clientProvider.get());
  }

  public static ArchiveResolver_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<OkHttpClient> clientProvider) {
    return new ArchiveResolver_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(clientProvider));
  }

  public static ArchiveResolver_Factory create(Provider<Context> contextProvider,
      Provider<OkHttpClient> clientProvider) {
    return new ArchiveResolver_Factory(contextProvider, clientProvider);
  }

  public static ArchiveResolver newInstance(Context context, OkHttpClient client) {
    return new ArchiveResolver(context, client);
  }
}
