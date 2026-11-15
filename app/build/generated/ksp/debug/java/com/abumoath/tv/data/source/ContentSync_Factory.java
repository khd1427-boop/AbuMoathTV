package com.abumoath.tv.data.source;

import android.content.Context;
import com.abumoath.tv.data.local.LocalDb;
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
public final class ContentSync_Factory implements Factory<ContentSync> {
  private final Provider<Context> contextProvider;

  private final Provider<LocalDb> dbProvider;

  private final Provider<OkHttpClient> clientProvider;

  public ContentSync_Factory(Provider<Context> contextProvider, Provider<LocalDb> dbProvider,
      Provider<OkHttpClient> clientProvider) {
    this.contextProvider = contextProvider;
    this.dbProvider = dbProvider;
    this.clientProvider = clientProvider;
  }

  @Override
  public ContentSync get() {
    return newInstance(contextProvider.get(), dbProvider.get(), clientProvider.get());
  }

  public static ContentSync_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<LocalDb> dbProvider,
      javax.inject.Provider<OkHttpClient> clientProvider) {
    return new ContentSync_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(dbProvider), Providers.asDaggerProvider(clientProvider));
  }

  public static ContentSync_Factory create(Provider<Context> contextProvider,
      Provider<LocalDb> dbProvider, Provider<OkHttpClient> clientProvider) {
    return new ContentSync_Factory(contextProvider, dbProvider, clientProvider);
  }

  public static ContentSync newInstance(Context context, LocalDb db, OkHttpClient client) {
    return new ContentSync(context, db, client);
  }
}
