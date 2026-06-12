package com.rentsplit.ui.home;

import com.rentsplit.data.preferences.UserPreferencesRepository;
import com.rentsplit.data.repository.RentSplitRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "KotlinInternalInJava"
})
public final class AddExpenseViewModel_Factory implements Factory<AddExpenseViewModel> {
  private final Provider<RentSplitRepository> repositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public AddExpenseViewModel_Factory(Provider<RentSplitRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public AddExpenseViewModel get() {
    return newInstance(repositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static AddExpenseViewModel_Factory create(Provider<RentSplitRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new AddExpenseViewModel_Factory(repositoryProvider, preferencesRepositoryProvider);
  }

  public static AddExpenseViewModel newInstance(RentSplitRepository repository,
      UserPreferencesRepository preferencesRepository) {
    return new AddExpenseViewModel(repository, preferencesRepository);
  }
}
