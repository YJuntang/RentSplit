package com.rentsplit.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.rentsplit.data.local.dao.CategoryDao;
import com.rentsplit.data.local.dao.CategoryDao_Impl;
import com.rentsplit.data.local.dao.ExpenseDao;
import com.rentsplit.data.local.dao.ExpenseDao_Impl;
import com.rentsplit.data.local.dao.HouseholdDao;
import com.rentsplit.data.local.dao.HouseholdDao_Impl;
import com.rentsplit.data.local.dao.MemberDao;
import com.rentsplit.data.local.dao.MemberDao_Impl;
import com.rentsplit.data.local.dao.RentConfigDao;
import com.rentsplit.data.local.dao.RentConfigDao_Impl;
import com.rentsplit.data.local.dao.SplitDao;
import com.rentsplit.data.local.dao.SplitDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RentSplitDatabase_Impl extends RentSplitDatabase {
  private volatile HouseholdDao _householdDao;

  private volatile MemberDao _memberDao;

  private volatile CategoryDao _categoryDao;

  private volatile ExpenseDao _expenseDao;

  private volatile SplitDao _splitDao;

  private volatile RentConfigDao _rentConfigDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `households` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `householdId` INTEGER NOT NULL, `name` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `isHouseLeader` INTEGER NOT NULL, FOREIGN KEY(`householdId`) REFERENCES `households`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_members_householdId` ON `members` (`householdId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `householdId` INTEGER NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `budgetLimit` REAL, `sortOrder` INTEGER NOT NULL, FOREIGN KEY(`householdId`) REFERENCES `households`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_householdId` ON `categories` (`householdId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `categoryId` INTEGER, `date` INTEGER NOT NULL, `paidByMemberId` INTEGER, `month` INTEGER NOT NULL, `year` INTEGER NOT NULL, `splitType` TEXT NOT NULL, FOREIGN KEY(`paidByMemberId`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paidByMemberId` ON `expenses` (`paidByMemberId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `splits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `expenseId` INTEGER NOT NULL, `memberId` INTEGER NOT NULL, `amountOwed` REAL NOT NULL, `amountPaid` REAL NOT NULL, `isPaid` INTEGER NOT NULL, FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`memberId`) REFERENCES `members`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_splits_expenseId` ON `splits` (`expenseId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_splits_memberId` ON `splits` (`memberId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `rent_config` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `householdId` INTEGER NOT NULL, `amount` REAL NOT NULL, `dueDayOfMonth` INTEGER NOT NULL, `lastGeneratedMonth` INTEGER NOT NULL, `lastGeneratedYear` INTEGER NOT NULL, FOREIGN KEY(`householdId`) REFERENCES `households`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rent_config_householdId` ON `rent_config` (`householdId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '35d8afe927a3540a57b0330d825fa424')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `households`");
        db.execSQL("DROP TABLE IF EXISTS `members`");
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `expenses`");
        db.execSQL("DROP TABLE IF EXISTS `splits`");
        db.execSQL("DROP TABLE IF EXISTS `rent_config`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsHouseholds = new HashMap<String, TableInfo.Column>(2);
        _columnsHouseholds.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHouseholds.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHouseholds = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHouseholds = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHouseholds = new TableInfo("households", _columnsHouseholds, _foreignKeysHouseholds, _indicesHouseholds);
        final TableInfo _existingHouseholds = TableInfo.read(db, "households");
        if (!_infoHouseholds.equals(_existingHouseholds)) {
          return new RoomOpenHelper.ValidationResult(false, "households(com.rentsplit.data.model.Household).\n"
                  + " Expected:\n" + _infoHouseholds + "\n"
                  + " Found:\n" + _existingHouseholds);
        }
        final HashMap<String, TableInfo.Column> _columnsMembers = new HashMap<String, TableInfo.Column>(5);
        _columnsMembers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("householdId", new TableInfo.Column("householdId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("isHouseLeader", new TableInfo.Column("isHouseLeader", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMembers = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMembers.add(new TableInfo.ForeignKey("households", "CASCADE", "NO ACTION", Arrays.asList("householdId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMembers = new HashSet<TableInfo.Index>(1);
        _indicesMembers.add(new TableInfo.Index("index_members_householdId", false, Arrays.asList("householdId"), Arrays.asList("ASC")));
        final TableInfo _infoMembers = new TableInfo("members", _columnsMembers, _foreignKeysMembers, _indicesMembers);
        final TableInfo _existingMembers = TableInfo.read(db, "members");
        if (!_infoMembers.equals(_existingMembers)) {
          return new RoomOpenHelper.ValidationResult(false, "members(com.rentsplit.data.model.Member).\n"
                  + " Expected:\n" + _infoMembers + "\n"
                  + " Found:\n" + _existingMembers);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(7);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("householdId", new TableInfo.Column("householdId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("iconName", new TableInfo.Column("iconName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("colorHex", new TableInfo.Column("colorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("budgetLimit", new TableInfo.Column("budgetLimit", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysCategories.add(new TableInfo.ForeignKey("households", "CASCADE", "NO ACTION", Arrays.asList("householdId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(1);
        _indicesCategories.add(new TableInfo.Index("index_categories_householdId", false, Arrays.asList("householdId"), Arrays.asList("ASC")));
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.rentsplit.data.model.Category).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsExpenses = new HashMap<String, TableInfo.Column>(9);
        _columnsExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("paidByMemberId", new TableInfo.Column("paidByMemberId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("month", new TableInfo.Column("month", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("year", new TableInfo.Column("year", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExpenses.put("splitType", new TableInfo.Column("splitType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExpenses = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysExpenses.add(new TableInfo.ForeignKey("members", "SET NULL", "NO ACTION", Arrays.asList("paidByMemberId"), Arrays.asList("id")));
        _foreignKeysExpenses.add(new TableInfo.ForeignKey("categories", "SET NULL", "NO ACTION", Arrays.asList("categoryId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesExpenses = new HashSet<TableInfo.Index>(2);
        _indicesExpenses.add(new TableInfo.Index("index_expenses_paidByMemberId", false, Arrays.asList("paidByMemberId"), Arrays.asList("ASC")));
        _indicesExpenses.add(new TableInfo.Index("index_expenses_categoryId", false, Arrays.asList("categoryId"), Arrays.asList("ASC")));
        final TableInfo _infoExpenses = new TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses);
        final TableInfo _existingExpenses = TableInfo.read(db, "expenses");
        if (!_infoExpenses.equals(_existingExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "expenses(com.rentsplit.data.model.Expense).\n"
                  + " Expected:\n" + _infoExpenses + "\n"
                  + " Found:\n" + _existingExpenses);
        }
        final HashMap<String, TableInfo.Column> _columnsSplits = new HashMap<String, TableInfo.Column>(6);
        _columnsSplits.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("expenseId", new TableInfo.Column("expenseId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("memberId", new TableInfo.Column("memberId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("amountOwed", new TableInfo.Column("amountOwed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("amountPaid", new TableInfo.Column("amountPaid", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSplits.put("isPaid", new TableInfo.Column("isPaid", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSplits = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysSplits.add(new TableInfo.ForeignKey("expenses", "CASCADE", "NO ACTION", Arrays.asList("expenseId"), Arrays.asList("id")));
        _foreignKeysSplits.add(new TableInfo.ForeignKey("members", "CASCADE", "NO ACTION", Arrays.asList("memberId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSplits = new HashSet<TableInfo.Index>(2);
        _indicesSplits.add(new TableInfo.Index("index_splits_expenseId", false, Arrays.asList("expenseId"), Arrays.asList("ASC")));
        _indicesSplits.add(new TableInfo.Index("index_splits_memberId", false, Arrays.asList("memberId"), Arrays.asList("ASC")));
        final TableInfo _infoSplits = new TableInfo("splits", _columnsSplits, _foreignKeysSplits, _indicesSplits);
        final TableInfo _existingSplits = TableInfo.read(db, "splits");
        if (!_infoSplits.equals(_existingSplits)) {
          return new RoomOpenHelper.ValidationResult(false, "splits(com.rentsplit.data.model.Split).\n"
                  + " Expected:\n" + _infoSplits + "\n"
                  + " Found:\n" + _existingSplits);
        }
        final HashMap<String, TableInfo.Column> _columnsRentConfig = new HashMap<String, TableInfo.Column>(6);
        _columnsRentConfig.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRentConfig.put("householdId", new TableInfo.Column("householdId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRentConfig.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRentConfig.put("dueDayOfMonth", new TableInfo.Column("dueDayOfMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRentConfig.put("lastGeneratedMonth", new TableInfo.Column("lastGeneratedMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRentConfig.put("lastGeneratedYear", new TableInfo.Column("lastGeneratedYear", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRentConfig = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysRentConfig.add(new TableInfo.ForeignKey("households", "CASCADE", "NO ACTION", Arrays.asList("householdId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesRentConfig = new HashSet<TableInfo.Index>(1);
        _indicesRentConfig.add(new TableInfo.Index("index_rent_config_householdId", false, Arrays.asList("householdId"), Arrays.asList("ASC")));
        final TableInfo _infoRentConfig = new TableInfo("rent_config", _columnsRentConfig, _foreignKeysRentConfig, _indicesRentConfig);
        final TableInfo _existingRentConfig = TableInfo.read(db, "rent_config");
        if (!_infoRentConfig.equals(_existingRentConfig)) {
          return new RoomOpenHelper.ValidationResult(false, "rent_config(com.rentsplit.data.model.RentConfig).\n"
                  + " Expected:\n" + _infoRentConfig + "\n"
                  + " Found:\n" + _existingRentConfig);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "35d8afe927a3540a57b0330d825fa424", "e8ea919043dda2e0d69704afd47aaffa");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "households","members","categories","expenses","splits","rent_config");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `households`");
      _db.execSQL("DELETE FROM `members`");
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `expenses`");
      _db.execSQL("DELETE FROM `splits`");
      _db.execSQL("DELETE FROM `rent_config`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(HouseholdDao.class, HouseholdDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MemberDao.class, MemberDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExpenseDao.class, ExpenseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SplitDao.class, SplitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RentConfigDao.class, RentConfigDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public HouseholdDao householdDao() {
    if (_householdDao != null) {
      return _householdDao;
    } else {
      synchronized(this) {
        if(_householdDao == null) {
          _householdDao = new HouseholdDao_Impl(this);
        }
        return _householdDao;
      }
    }
  }

  @Override
  public MemberDao memberDao() {
    if (_memberDao != null) {
      return _memberDao;
    } else {
      synchronized(this) {
        if(_memberDao == null) {
          _memberDao = new MemberDao_Impl(this);
        }
        return _memberDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public ExpenseDao expenseDao() {
    if (_expenseDao != null) {
      return _expenseDao;
    } else {
      synchronized(this) {
        if(_expenseDao == null) {
          _expenseDao = new ExpenseDao_Impl(this);
        }
        return _expenseDao;
      }
    }
  }

  @Override
  public SplitDao splitDao() {
    if (_splitDao != null) {
      return _splitDao;
    } else {
      synchronized(this) {
        if(_splitDao == null) {
          _splitDao = new SplitDao_Impl(this);
        }
        return _splitDao;
      }
    }
  }

  @Override
  public RentConfigDao rentConfigDao() {
    if (_rentConfigDao != null) {
      return _rentConfigDao;
    } else {
      synchronized(this) {
        if(_rentConfigDao == null) {
          _rentConfigDao = new RentConfigDao_Impl(this);
        }
        return _rentConfigDao;
      }
    }
  }
}
