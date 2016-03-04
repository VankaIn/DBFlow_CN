# DBFlow_CN
翻译DBFlow文档


![Image](https://github.com/agrosner/DBFlow/blob/develop/dbflow_banner.png?raw=true)

[![JitPack.io](https://img.shields.io/badge/JitPack.io-3.0.0beta4-red.svg?style=flat)](https://jitpack.io/#Raizlabs/DBFlow) [![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23129-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-129) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-DBFlow-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1134)

DBFlow是一个功能强大，简单易用的ORM安卓数据库库，他使用了**注释处理**.

这个库速度快，性能高，而且非常易用。它不但消除了大部分繁琐的公式化的数据库操作代码，而且还提交了一套功能强大，简单易用的API。

DBFlow使sql代码就跟流式调用一样简洁，因此您可以集中精力去编写优秀的应用。

#为什么要使用DBFlow
DBFlow目的是把其他ORM的数据库最好的优点集合在一起，而且将它们进一步优化。DBFlow不只是让你知道如何解决你的功能上的问题，而且它使你容易处理Android上的数据库。让我们好好利用DBFlow，使我们尽可能的把程序写的最好。

- **可扩展性**：`Model` 是一个接口，无需子类，但为了方便起见，我们建议使用 `BaseModel`。你可以不继承任何`Model`类在不同的包中的类，并把它们作为你的数据库表。你也可以继承其他`Model`然后同时加入`@Column`，他们又可以在不同的packages中。此外，在该库的子类对象，能满足您的需求。（翻译不好）
- **速度**:这个库内置Java的注释处理代码生成，有几乎为零的运行时性能（反射是主要的，生成的数据库模块的构造方法）。该库通过生成的代码，你可以节省样板代码和维护时间。凭借强大的模式高速缓存（多主键`Model` 也行），你可以通过重复使用，在这里可能超过SQLite的速度。我们支持延迟加载，如支持@ForeignKey或@OneToMany，使查询发生的速度超快。
- **SQLite流式查询**:此库中的查询尽可能坚持SQLite的原生查询， `select(name, screenSize).from(Android.class).where(name.is("Nexus 5x")).and(version.is(6.0)).querySingle()`
- **开源**:该库是完全开源，不仅欢迎贡献，而且鼓励。
- **强大**: 我们支持触发器，模型视图，索引，迁移，在同一个线程中，内置的数据库请求队列执行操作，还有更多的功能。。。
- **多个数据库，多个模块**:我们无缝支持多个数据库文件，数据库模块，在同一时间。
- **基于SQLite**:SQLite是世界上最广泛使用的数据库引擎。。。。。

# 使用文档
想了解更多详细的使用，看看这些部分：

[入门](usage/GettingStarted.md)

[表格和数据库属性](usage/DBStructure.md)

[关于DBFlow的多个实例/多个数据库模块](usage/DatabaseModules.md)

[使用包装类对sql语句声明](usage/SQLQuery.md)

[属性和条件](usage/Conditions.md)

[事物处理](usage/Transactions.md)

[类型转换器](usage/TypeConverters.md)

[强大的模型缓存](usage/ModelCaching.md)

[多模型查询](usage/QueryModels.md)

[Content Provider Generation](usage/ContentProviderGenerators.md)

[数据库迁移](usage/Migrations.md)

[模型容器](usage/ModelContainers.md)

[观察模型](usage/ObservableModels.md)

[查询列表](usage/TableList.md)

[触发器，索引，以及更多](usage/TriggersIndexesAndMore.md)

[SQLCipher支持]不翻译

[Kotlin 扩展]不翻译

# 导入到你的项目中
如果你使用KAPT (Kotlin's APT)，跳过这第一步。

我们需要包括 [apt plugin](https://bitbucket.org/hvisser/android-apt)在我们的classpath中，使它来支持**注释处理**:

```groovy

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

allProjects {
  repositories {
    // required to find the project's artifacts
    maven { url "https://jitpack.io" }
  }
}
```

Add the library to the project-level build.gradle, using the apt plugin to enable Annotation Processing:
该库添加到项目级的build.gradle，使apt插件支持**注释处理**:

```groovy

  apply plugin: 'com.neenbedankt.android-apt'

  def dbflow_version = "3.0.0-beta4"
  // or dbflow_version = "develop-SNAPSHOT" for grabbing latest dependency in your project on the develop branch
  // or 10-digit short-hash of a specific commit. (Useful for bugs fixed in develop, but not in a release yet)

  dependencies {
    apt "com.github.Raizlabs.DBFlow:dbflow-processor:${dbflow_version}"
    // kapt for kotlin apt
    compile "com.github.Raizlabs.DBFlow:dbflow-core:${dbflow_version}"
    compile "com.github.Raizlabs.DBFlow:dbflow:${dbflow_version}"

    // sql-cipher database encyrption (optional)
    compile "com.github.Raizlabs.DBFlow:dbflow-sqlcipher:${dbflow_version}"

    // kotlin extensions
    compile "com.github.Raizlabs.DBFlow:dbflow-kotlinextensions:${dbflow_version}"
  }

```


# Translate By
[VankaIn](https://github.com/VankaIn) ([email](Vancouver031@gmail))
