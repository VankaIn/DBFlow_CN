# 入门

在本节中，我们介绍如何构建一个简单的数据库，表，还有如何建立Model之间的关系。

**蚁后**：我们想知道蚁群数据是怎么保存的。我们要跟踪和标记所有蚂蚁的特定群体以及每个蚁后。

我们有这样的关系：

```

Colony (1..1) -> Queen (1...many)-> Ants//1对1，1对多
```


## 设置DBFlow

要初始化DBFlow，放置在这段代码( FlowManager.init(this);)在你自定义的 `Application` 类中（推荐）：

```java

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }

}
```
别担心，这只是初始化一次，它会守住只有应用程序，即使使用其他`Context`初始化。

最后，定义添加到清单（对应您的自定义应用程序的名称）：

```xml

<application
  android:name="{packageName}.ExampleApplication"
  ...>
</application>
```


## 定义我们的数据库

在DBFlow，一个 `@Database` 是一个占位符，这个占位符可以生产子类对象 `BaseDatabaseDefinition`， `BaseDatabaseDefinition`连接所有表，ModelAdapter，Views，Queries还有更多其下的对象。所有连接都在预编译的时候完成，所以没有搜索，反射，和任何其他能减慢您的应用程序的运行时间的影响。

在这个例子中，我们需要定义我们要把蚁群保存到哪里（定义数据库）：

```java

@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
public class ColonyDatabase {

  public static final String NAME = "Colonies";

  public static final int VERSION = 1;
}
```

对于最佳实践，我们声明的常量`NAME`和 `VERSION`为public，以便于我们以后可以使用它。

_Note:_ 如果你想使用[SQLCipher（数据库加密）](https://www.zetetic.net/sqlcipher/) 请阅读 [setup here](usage/SQLCipherSupport.md)


## 创建我们的表和建立关系

现在，我们有了保存蚁群数据的地方了(ColonyDatabase)，我们需要明确定义`Model` 来保存数据和展示数据


### 蚁后表（The Queen Table）

我们将自上而下的理解关系。每个蚁群只有一个皇后。我们定义数据库对象使用ORM（对象关系映射）模型。我们需要做的是在我们的`Model`定义我们每个需要保存到数据库的字段。

在DBFlow，任何要使用ORM实现数据库交互的都必须实现接口`Model`（也就是说如果你的数据库表一定要实现`Model`接口）。这样做的原因是统一接口balabala。。。。。为了方便起见，我们可以extends `BaseModel`（`BaseModel` 已经实现了 `Model`接口）

要正确定义一个表，我们必须：

1. @Table注释标记类

2. 将表到正确的数据库，例如`ColonyDatabase`

3. 至少定义一个主键

4. 类及其所有数据库中的列（model中的变量）必须用`private`或`public`,`private`的必须有（getter和setter方法）。这样从DBFlow生成的类可以访问它们。

我们可以这样定义一个基础的 `Queen` 表:

```java

@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String name;

}
```

因此，我们有一个蚁后的定义后，现在我们需要为蚁后定义一个蚁群。


### The Colony(蚁群)

```java
@ModelContainer // more on this later.
@Table(database = ColonyDatabase.class)
public class Colony extends BaseModel {

  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String name;

}
```

现在，我们有一个`Queen`和`Colony`，我们要建立一个1对1的关系。我们希望，当数据被删除，例如，如果发生火灾，破坏蚁群 `Colony`。当蚁群被破坏，我们假设女王`Queen`不再存在，所以我们要为 `Colony`“杀”了`Queen`，使其不再存在。

### 1-1 关系
为了建立他们的关系，我们将会定义一个外键作为Child：

```java

@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {

  //...previous code here

  @Column
  @ForeignKey(saveForeignKeyModel = false)
  Colony colony;

}
```
为`Model`定义为外键的时候，查询数据库时候，该外键的值会自动加载（查询`Queen`的时候,对应的`Colony`会自动被加载）。出于性能方面的原因，我们默认`saveForeignKeyModel=false` ，目的是保存 `Queen`不会自动保存 `Colony` 。

如果你想保持这种配对完好，设置`saveForeignKeyModel=true`。

在3.0，我们不再需要明确地定义`@ForeignKeyReference` 每个引用列。DBFlow会自动将它们添加到表定义中，基于引用表的`@PrimaryKey`。它们将出现在格式`{foreignKeyFieldName}_{referencedColumnName}`。（如上外键`colony`的格式是：`colony_id`）


### 蚂蚁表 + 一对多
现在，我们有一个蚁群`Colony` 与蚁后 `Queen` 属于它，我们需要一些蚂蚁服侍她！


```java

@Table(database = ColonyDatabase.class)
public class Ant extends BaseModel {

  @PrimaryKey(autoincrement = true)
  long id;

  @Column
  String type;

  @Column
  boolean isMale;

  @ForeignKey(saveForeignKeyModel = false)
  ForeignKeyContainer<Queen> queenForeignKeyContainer;

  /**
  * Example of setting the model for the queen.
  */
  public void associateQueen(Queen queen) {
    queenForeignKeyContainer = FlowManager.getContainerAdapter(Queen.class).toForeignKeyContainer(queen);
  }
}
```

我们有 `type`，它可以是 "worker", "mater", 或 "other"。此外，如果蚂蚁还有男女之分。

在这种情况下，我们使用 `ForeignKeyContainer`，因为我们可以有成千上万的蚂蚁。出于性能的考虑，`Queen`将会被“延迟加载的”，只有我们调用`toModel（）`,才会查询数据库找出对应的 `Queen`。与此说，为了在`ForeignKeyContainer`上设置适当的值，你应该通过调用其生成的方法(`FlowManager.getContainerAdapter(Queen.class).toForeignKeyContainer(queen)`)为自己转换成 `ForeignKeyContainer` 。

由于`ModelContainer`默认情况下不会使用，所以我们必须添加 `@ModelContainer`注释到`Queen` 累中才能使用 `ForeignKeyContainer`。

最后，使用`@ForeignKeyContainer`可以防止循环引用。如果这 `Queen` 和 `Colony`互相引用，我们会碰上的StackOverflowError，因为他们都将尝试从数据库加载对方中。

接下来，我们通过延迟加载蚂蚁建立“一对多”的关系，因为我们可能有成千上万，甚至是，数以百万计的储存：

```java

@ModelContainer
@Table(database = ColonyDatabase.class)
public class Queen extends BaseModel {
  //...

  // needs to be accessible for DELETE
  List<Ant> ants;

  @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "ants")
  public List<Ant> getMyAnts() {
    if (ants == null || ants.isEmpty()) {
            ants = SQLite.select()
                    .from(Ant.class)
                    .where(Ant_Table.queenForeignKeyContainer_id.eq(id))
                    .queryList();
    }
    return ants;
  }
}
```

``注意``：如果你发现`Ant_Table`报错的时候，跑一下，让库自动生成模板就不会再报错了

如果你想给自己懒加载，指定`OneToMany.Method.DELETE`和`SAVE`，代替`ALL`。如果每当女王的数据变化时候，您不希望保存，那么只指定 `DELETE` 和 `LOAD`。
