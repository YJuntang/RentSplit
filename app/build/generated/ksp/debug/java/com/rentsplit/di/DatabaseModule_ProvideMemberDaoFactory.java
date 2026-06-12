package com.rentsplit.di;

import com.rentsplit.data.local.RentSplitDatabase;
import com.rentsplit.data.local.dao.MemberDao;
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
public final class DatabaseModule_ProvideMemberDaoFactory implements Factory<MemberDao> {
  private final Provider<RentSplitDatabase> databaseProvider;

  public DatabaseModule_ProvideMemberDaoFactory(Provider<RentSplitDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MemberDao get() {
    return provideMemberDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideMemberDaoFactory create(
      Provider<RentSplitDatabase> databaseProvider) {
    return new DatabaseModule_ProvideMemberDaoFactory(databaseProvider);
  }

  public static MemberDao provideMemberDao(RentSplitDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMemberDao(database));
  }
}
