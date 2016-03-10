#属性,条件和条件组
DBFlow，通过Java注释处理，生成一个`_Table`表示你的 `Model`。生成的每个领域都是子类的`IProperty`。每个的`IProperty`代表在相应表中的列，并提供了一个类型安全的条件操作把它变成SQLCondition或变异成另一种属性。

`SQLCondition`是一个代表SQL语句中的条件语句的接口。这是一个接口，这样其他类型的条件可以使用，并且允许最大的灵活性，以满足您的需求。

例如，写在原始的SQLite：

```sql

`name` = 'Test'

`name` = `SomeTable`.`Test`

`name` LIKE '%Test%'

`name` != 'Test'

`salary` BETWEEN 15000 AND 40000

`name` IN('Test','Test2','TestN')

((`name`='Test' AND `rank`=6) OR (`name`='Bob' AND `rank`=8))
```

## 如何使用条件
建议我们的查询从`Model`的`属性`中创造`条件`。

我们有一个简单的表：

```java
@Table(database = TestDatabase.class)
public class TestModel3 {

    @PrimaryKey
    String name;

    @Column
    String type;
}
```

有了这个定义, DBFlow将帮我们生产一个TestModel3_Table类：

```java

public final class TestModel3_Table {
  public static final PropertyConverter PROPERTY_CONVERTER = new PropertyConverter(){
  public IProperty fromName(String columnName) {
  return com.raizlabs.android.dbflow.test.sql.TestModel3_Table.getProperty(columnName);
  }
  };

  public static final Property<String> type = new Property<String>(TestModel3.class, "type");

  public static final Property<String> name = new Property<String>(TestModel3.class, "name");

  public static final IProperty[] getAllColumnProperties() {
    return new IProperty[]{type,name};
  }

  public static BaseProperty getProperty(String columnName) {
    columnName = QueryBuilder.quoteIfNeeded(columnName);
    switch (columnName)  {
      case "`type`":  {
        return type;
      }
      case "`name`":  {
        return name;
      }
      default:  {
        throw new IllegalArgumentException("Invalid column name passed. Ensure you are calling the correct table's column");
      }
    }
  }
}
```

从生成的类文件中使用的字段，我们现在可以在我们的查询中使用`属性`生成`SQLCondition`：

```java

TestModel3_Table.name.is("Test"); // `name` = 'Test'
TestModel3_Table.name.withTable().is("Test"); // `TestModel3`.`name` = 'Test'
TestModel3_Table.name.like("%Test%");

// `name`=`AnotherTable`.`name`
TestModel3_Table.name.eq(AnotherTable_Table.name);
```

A whole set of `SQLCondition` operations are supported for `Property` generated for a Table including:
1. `is()`, `eq()` -> =
2. `isNot()`, `notEq()` -> !=
3. `isNull()` -> IS NULL / `isNotNull()`IS NOT NULL
4. `like()`, `glob()`
5. `greaterThan()`, `greaterThanOrEqual()`, `lessThan()`, `lessThanOrEqual()`
6. `between()` -> BETWEEN
7. `in()`, `notIn()`

此外，我们可以做加法和减法：

```java

SomeTable_Table.latitude.plus(SomeTable_Table.longitude).lessThan(45.0); // `latitude` + `longitude` < 45.0

SomeTable_Table.latitude.minus(SomeTable_Table.longitude).greaterThan(45.0); // `latitude` - `longitude` > 45.0

```

## 条件组
该`ConditionGroup`是`ConditionQueryBuilder`的继任者。这是有缺陷的，就是它符合的`QueryBuilder`，还包含 `Condition`，以及所需要的类型的参数都需要属于它自己的表。

`ConditionGroup`是任意集合 `SQLCondition`，可以组合成一个单一的条件，SELECT projection，或者被用作`SQLCondition`在另一个`ConditionGroup`内。

这用于包装查询语句，支持其他各种查询和类。

```java

SQLite.select()
  .from(MyTable.class)
  .where(MyTable_Table.someColumn.is("SomeValue"))
  .and(MyTable_Table.anotherColumn.is("ThisValue"));

  // SELECT * FROM `MyTable` WHERE `someColumn`='OneValue' OR (`someColumn`='SomeValue' AND `anotherColumn`='ThisValue')
  SQLite.select()
    .from(MyTable.class)
    .where(MyTable.someColumn.is("OneValue"))
    .or(ConditionGroup.clause()
      .and(MyTable_Table.someColumn.is("SomeValue")
      .AND(MyTable_Table.anotherColumn.is("ThisValue"));


```
