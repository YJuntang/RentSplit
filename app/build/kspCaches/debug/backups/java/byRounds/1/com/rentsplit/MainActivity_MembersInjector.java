package com.rentsplit;

import com.rentsplit.data.preferences.UserPreferencesRepository;
import com.rentsplit.data.repository.RentSplitRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "KotlinInternalInJava"
})
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<RentSplitRepository> repositoryProvider;

  public MainActivity_MembersInjector(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<RentSplitRepository> repositoryProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<RentSplitRepository> repositoryProvider) {
    return new MainActivity_MembersInjector(preferencesRepositoryProvider, repositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectPreferencesRepository(instance, preferencesRepositoryProvider.get());
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.rentsplit.MainActivity.preferencesRepository")
  public static void injectPreferencesRepository(MainActivity instance,
      UserPreferencesRepository preferencesRepository) {
    instance.preferencesRepository = preferencesRepository;
  }

  @InjectedFieldSignature("com.rentsplit.MainActivity.repository")
  public static void injectRepository(MainActivity instance, RentSplitRepository repository) {
    instance.repository = repository;
  }
}
