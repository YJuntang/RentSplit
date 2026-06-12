package com.rentsplit.ui.settings;

import android.content.Context;
import com.rentsplit.data.preferences.UserPreferencesRepository;
import com.rentsplit.data.repository.RentSplitRepository;
import com.rentsplit.util.BackupManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
    "KotlinInternalInJava"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  private final Provider<RentSplitRepository> repositoryProvider;

  private final Provider<BackupManager> backupManagerProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<RentSplitRepository> repositoryProvider,
      Provider<BackupManager> backupManagerProvider, Provider<Context> contextProvider) {
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
    this.repositoryProvider = repositoryProvider;
    this.backupManagerProvider = backupManagerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(preferencesRepositoryProvider.get(), repositoryProvider.get(), backupManagerProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserPreferencesRepository> preferencesRepositoryProvider,
      Provider<RentSplitRepository> repositoryProvider,
      Provider<BackupManager> backupManagerProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(preferencesRepositoryProvider, repositoryProvider, backupManagerProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(UserPreferencesRepository preferencesRepository,
      RentSplitRepository repository, BackupManager backupManager, Context context) {
    return new SettingsViewModel(preferencesRepository, repository, backupManager, context);
  }
}
