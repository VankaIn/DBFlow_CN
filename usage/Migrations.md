# DB迁移变得容易！
每当你修改数据库架构，你需要在对应的`@Database`类内部通过增加数据库版本。还需要添加一个迁移到配置或通过定义迁移 `/assets/migrations/{DatabaseName}/{versionName.sql}`。

在首次创建数据库，您可以使用版本0指定一个迁移运行时！

**注意**：任何提供的子类，如`AlterTableMigration`，`UpdateTableMigration`和`IndexMigration`应该只覆盖`onPreMigrate（）`和**调用super.onPreMigrate（）**所以它的正确实例化。

注意：所有`迁移`必须只有一个`public`默认构造函数。

## 迁移类
基类，`BaseMigration`是一个非常简单的类来执行迁移：

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(DatabaseWrapper database) {
      List<SomeClass> list = SQLite.select()
          .from(SomeClass.class)
          .queryList(database); // must pass in wrapper in order to prevent recursive calls to DB.
    }
}
```

## 添加列
此处是添加到数据库的列的一个例子：

假设我们有原来的示例类：

```java

@Table
public class TestModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    int randomNumber;
}
```

现在，我们要**添加**一列到这个表。我们有两种方式：
- 利用SQL语句

  `ALTER TABLE TestModel ADD COLUMN timestamp INTEGER;` in a {dbVersion.sql} file in the assets directory. If we need to add any other column, we have to add more lines.

- 通过`Migration`:

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends AlterTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      // Simple ALTER TABLE migration wraps the statements into a nice builder notation
      addColumn(Long.class, "timestamp");
    }
}
```

## 更新列

```java

@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends UpdateTableMigration<TestModel> {

    @Override
    public void onPreMigrate() {
      // UPDATE TestModel SET deviceType = "phablet" WHERE screenSize > 5.7 AND screenSize < 7;
      set(TestModel_Table.deviceType.is("phablet"))
        .where(TestModel_Table.screenSize.greaterThan(5.7), TestModel_Table.screenSize.lessThan(7));
    }
}
```
