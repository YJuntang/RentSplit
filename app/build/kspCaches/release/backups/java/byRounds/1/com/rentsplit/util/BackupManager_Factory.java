package com.rentsplit.util;

import com.rentsplit.data.repository.RentSplitRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BackupManager_Factory implements Factory<BackupManager> {
  private final Provider<RentSplitRepository> repositoryProvider;

  public BackupManager_Factory(Provider<RentSplitRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BackupManager get() {
    return newInstance(repositoryProvider.get());
  }

  public static BackupManager_Factory create(Provider<RentSplitRepository> repositoryProvider) {
    return new BackupManager_Factory(repositoryProvider);
  }

  public static BackupManager newInstance(RentSplitRepository repository) {
    return new BackupManager(repository);
  }
}
