package com.rentsplit.ui.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<RentSplitRepository> repositoryProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public OnboardingViewModel_Factory(Provider<RentSplitRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(repositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static OnboardingViewModel_Factory create(Provider<RentSplitRepository> repositoryProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new OnboardingViewModel_Factory(repositoryProvider, preferencesRepositoryProvider);
  }

  public static OnboardingViewModel newInstance(RentSplitRepository repository,
      UserPreferencesRepository preferencesRepository) {
    return new OnboardingViewModel(repository, preferencesRepository);
  }
}
