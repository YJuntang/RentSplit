package com.rentsplit.ui.categories;

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
public final class CategoriesViewModel_Factory implements Factory<CategoriesViewModel> {
  private final Provider<RentSplitRepository> repositoryProvider;

  public CategoriesViewModel_Factory(Provider<RentSplitRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CategoriesViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CategoriesViewModel_Factory create(
      Provider<RentSplitRepository> repositoryProvider) {
    return new CategoriesViewModel_Factory(repositoryProvider);
  }

  public static CategoriesViewModel newInstance(RentSplitRepository repository) {
    return new CategoriesViewModel(repository);
  }
}
