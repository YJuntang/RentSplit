package com.rentsplit.ui.balances;

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
public final class BalancesViewModel_Factory implements Factory<BalancesViewModel> {
  private final Provider<RentSplitRepository> repositoryProvider;

  public BalancesViewModel_Factory(Provider<RentSplitRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public BalancesViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static BalancesViewModel_Factory create(Provider<RentSplitRepository> repositoryProvider) {
    return new BalancesViewModel_Factory(repositoryProvider);
  }

  public static BalancesViewModel newInstance(RentSplitRepository repository) {
    return new BalancesViewModel(repository);
  }
}
