package com.abumoath.tv;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Named;
import okhttp3.OkHttpClient;

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
public final class AbuMoathApp_MembersInjector implements MembersInjector<AbuMoathApp> {
  private final Provider<OkHttpClient> imageClientProvider;

  public AbuMoathApp_MembersInjector(Provider<OkHttpClient> imageClientProvider) {
    this.imageClientProvider = imageClientProvider;
  }

  public static MembersInjector<AbuMoathApp> create(Provider<OkHttpClient> imageClientProvider) {
    return new AbuMoathApp_MembersInjector(imageClientProvider);
  }

  public static MembersInjector<AbuMoathApp> create(
      javax.inject.Provider<OkHttpClient> imageClientProvider) {
    return new AbuMoathApp_MembersInjector(Providers.asDaggerProvider(imageClientProvider));
  }

  @Override
  public void injectMembers(AbuMoathApp instance) {
    injectImageClient(instance, imageClientProvider.get());
  }

  @InjectedFieldSignature("com.abumoath.tv.AbuMoathApp.imageClient")
  @Named("images")
  public static void injectImageClient(AbuMoathApp instance, OkHttpClient imageClient) {
    instance.imageClient = imageClient;
  }
}
