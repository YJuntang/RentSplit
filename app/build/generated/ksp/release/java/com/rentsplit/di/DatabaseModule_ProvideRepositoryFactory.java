package com.rentsplit.di;

import com.rentsplit.data.local.RentSplitDatabase;
import com.rentsplit.data.local.dao.CategoryDao;
import com.rentsplit.data.local.dao.ExpenseDao;
import com.rentsplit.data.local.dao.HouseholdDao;
import com.rentsplit.data.local.dao.MemberDao;
import com.rentsplit.data.local.dao.RentConfigDao;
import com.rentsplit.data.local.dao.SplitDao;
import com.rentsplit.data.repository.RentSplitRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideRepositoryFactory implements Factory<RentSplitRepository> {
  private final Provider<RentSplitDatabase> databaseProvider;

  private final Provider<HouseholdDao> householdDaoProvider;

  private final Provider<MemberDao> memberDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<ExpenseDao> expenseDaoProvider;

  private final Provider<SplitDao> splitDaoProvider;

  private final Provider<RentConfigDao> rentConfigDaoProvider;

  public DatabaseModule_ProvideRepositoryFactory(Provider<RentSplitDatabase> databaseProvider,
      Provider<HouseholdDao> householdDaoProvider, Provider<MemberDao> memberDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ExpenseDao> expenseDaoProvider,
      Provider<SplitDao> splitDaoProvider, Provider<RentConfigDao> rentConfigDaoProvider) {
    this.databaseProvider = databaseProvider;
    this.householdDaoProvider = householdDaoProvider;
    this.memberDaoProvider = memberDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.expenseDaoProvider = expenseDaoProvider;
    this.splitDaoProvider = splitDaoProvider;
    this.rentConfigDaoProvider = rentConfigDaoProvider;
  }

  @Override
  public RentSplitRepository get() {
    return provideRepository(databaseProvider.get(), householdDaoProvider.get(), memberDaoProvider.get(), categoryDaoProvider.get(), expenseDaoProvider.get(), splitDaoProvider.get(), rentConfigDaoProvider.get());
  }

  public static DatabaseModule_ProvideRepositoryFactory create(
      Provider<RentSplitDatabase> databaseProvider, Provider<HouseholdDao> householdDaoProvider,
      Provider<MemberDao> memberDaoProvider, Provider<CategoryDao> categoryDaoProvider,
      Provider<ExpenseDao> expenseDaoProvider, Provider<SplitDao> splitDaoProvider,
      Provider<RentConfigDao> rentConfigDaoProvider) {
    return new DatabaseModule_ProvideRepositoryFactory(databaseProvider, householdDaoProvider, memberDaoProvider, categoryDaoProvider, expenseDaoProvider, splitDaoProvider, rentConfigDaoProvider);
  }

  public static RentSplitRepository provideRepository(RentSplitDatabase database,
      HouseholdDao householdDao, MemberDao memberDao, CategoryDao categoryDao,
      ExpenseDao expenseDao, SplitDao splitDao, RentConfigDao rentConfigDao) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideRepository(database, householdDao, memberDao, categoryDao, expenseDao, splitDao, rentConfigDao));
  }
}
