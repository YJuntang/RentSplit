package com.rentsplit.di;

import com.rentsplit.data.local.RentSplitDatabase;
import com.rentsplit.data.local.dao.RentConfigDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideRentConfigDaoFactory implements Factory<RentConfigDao> {
  private final Provider<RentSplitDatabase> databaseProvider;

  public DatabaseModule_ProvideRentConfigDaoFactory(Provider<RentSplitDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public RentConfigDao get() {
    return provideRentConfigDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideRentConfigDaoFactory create(
      Provider<RentSplitDatabase> databaseProvider) {
    return new DatabaseModule_ProvideRentConfigDaoFactory(databaseProvider);
  }

  public static RentConfigDao provideRentConfigDao(RentSplitDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRentConfigDao(database));
  }
}
