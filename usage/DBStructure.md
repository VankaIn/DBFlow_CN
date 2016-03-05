# 表格和数据库属性
## 创建数据库
用DBFlow创建数据库是超级简单的。只要简单地定义一个占位符 `@Database` 类：

```java

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

  public static final String NAME = "AppDatabase";

  public static final int VERSION = 1;
}
```

_P.S._ 你可以定义为许多`@Database` 只要你喜欢，但是名称要唯一的。

### 预包装数据库
如果想为你得app包含预先准备好的数据库，直接把 ".db" 文件复制到`src/main/assets/{databaseName}.db`目录中。在创建数据库时，我们复制该文件给应用使用。由于这是APK内预先包装，在复制完后，我们无法将其删除，导致APK变大（取决于数据库文件大小）。


### 配置属性
**全局冲突处理**：在这里通过指定`insertConflict（）`和 `updateConflict（）`，任何 `@Table`没有明确定上面2个属性的任意一个，他将会使用最适合的一个关联`@Database`。

以前，你需要定义一个`generatedClassSeparator()`  才能运行
Previously you needed to define a  `generatedClassSeparator()` that works for it.

如果要更改默认`_` ，只需添加一些字符串：

```java

@Database(generatedClassSeparator = "$$")
```

**开放数据库的完整性检查**:每当打开数据，`consistencyChecksEnabled()` 将运行一个`PRAGMA quick_check(1)`，如果失败，它将尝试复制预先打包的数据库。

**轻松备份数据库**：`backupEnabled()` 调用，可以备份数据库

```
FlowManager.getDatabaseForTable(table).backupDB()
```

请注意：当数据库备份失败时，这将创建一个临时的第三方数据库。
Please Note: This creates a temporary _third_ database in case of a failed backup.

**打开外键Constrants**：通过设置`foreignKeysSupported()=true` ，让数据库强制执行外键。默认情况下此处于关闭状态。们仍然可以定义@ForeignKey，但他们的关系不执行。

**OpenHelper的定义实现**：他们必须为FlowSQLiteOpenHelper配置构造参数：

```java
public FlowSQLiteOpenHelper(BaseDatabaseDefinition flowManager, DatabaseHelperListener listener)
```

要 `public`，并在 `@Database` 注解中指出了 `sqlHelperClass()` 自定义类。

## 模型与创造
所有标准表必须使用`@Table` 注解和实现`Model`接口。为方便起见，BaseModel提供了一个默认的实现。

**`Model`的支持**：
  1. Fields: 支持任何默认Java类型.
  2. `类型转换器`: 您可以定义非标准类的列，如`Date`, `Calendar`等，这些可以在列逐列的基础上进行配置。
  3. 复合 `@PrimaryKey`
  4. 复合 `@ForeignKey`. 嵌套 `Model`, `ModelContainer`, `ForeignKeyContainer` or standard `@Column`.
  5. 结合 `@PrimaryKey` 和 `@ForeignKey`, 以及那些可以有复杂的主键。
  6. 内部类
  
**Models的规则和技巧**：
  1.`Model`必须有一个可访问的默认构造函数。这可以是public或package private.。
  2. 子类是完全支持。DBFlow将收集并结合每个子类“的注释并将它们组合为当前类。
  3. 字段可以是public，package private（我们生成package helpers访问），或private（要有getter和setter）。Private fields需要有一个getter `get{Name}`和setter `set{Name}`。这些也可以被配置。
  4. 我们可以继承非`Model`的类，这样类可以通过扩展`inheritedColumns()` （或`inheritedPrimaryKeys()`）。这些都必须通过带有对应的getter和setter的 package-private, public, or private 
  5. 结合 `@PrimaryKey` 和 `@ForeignKey`, 以及那些可以有复杂的主键。
  6. 要启用缓存，设置 `cachingEnabled = true`，这将加快在大多数情况下检索。
  

### 简单例子
这是一个带有一个主键(一个`Model`最少有一个)和其他列的 `Model`。

```java
@Table(database = AppDatabase.class)
public class TestModel extends BaseModel {

    // All tables must have a least one primary key
    @PrimaryKey
    String name;

    // By default the column name is the field name
    @Column
    int randomNumber;

}
```

## 高级表功能
### 为特定的列自定义类型转换器
在3.0，现在您可以为特定`@Column`指定一个`TypeConverter`，为转换器指定对应的`Column` ：


```java

@Column(typeConverter = SomeTypeConverter.class)
SomeObject someObject;
```

它将取代通常的转换/访问方法（除如果该字段是私有的，它保留了基于私有访问方法）。

### 所有字段作为列
因为其他库也这样做，你可以设置 `@Table(allFields = true)` 打开使用所有的public/package private，non-final,，以及non-static 字段作为 `@Column`。你仍然需要提供至少一个 `@PrimaryKey` 字段。

如果您需要忽略一个字段，使用 `@ColumnIgnore` 注释。

### 私人列
如果你想使用私有字段，只需指定一个getter和setter，格式：`name` -> `getName()` + `setName(columnFieldType)`
If you wish to use private fields, simply specify a getter and setter that follow the format of: `name` -> `getName()` + `setName(columnFieldType)`

```java

@Table(database = TestDatabase.class)
public class PrivateModelTest extends BaseModel {

    @PrimaryKey
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

`boolean` fields can use "is"

```java

@Table(database = TestDatabase.class, useIsForPrivateBooleans = true)
public class PrivateModelTest extends BaseModel {

    @PrimaryKey
    private String name;

    @Column
    private boolean selected;

    public boolean isSelected() {
      return selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    //... etc
}
```

### 默认值
当字段的值丢失或遗漏，希望提供在数据库中默认值。SQLite使用`DEFAULT`来实现。


然而，在DBFlow也不是那么容易，因为我们依靠预编译实现这个`INSERT`声明。因此，作为一种妥协，这些值插入这样：


```java
@Table(database = TestDatabase.class)
public class DefaultModel extends TestModel1 {

    @Column(defaultValue = "55")
    Integer count;

}
```

In the `_Adapter`:

```java
@Override
  public final void bindToInsertValues(ContentValues values, DefaultModel model) {
    if (model.count != null) {
      values.put("`count`", model.count);
    } else {
      values.put("`count`", 55);
    }
    //...
  }
```

我们在运行时插入它的值。这有一定的局限性：
  1. 常量, 纯字符串值
  2. No `Model`, `ModelContainer`, or primitive can use this.
  3. 必须是同一类型的
  4. `String` 类型需要通过进行转义: `"\"something\""`
