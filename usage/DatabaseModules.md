# Database Modules
# 数据库模块
DBFlow它的底层将产生一个GeneratedDatabaseHolder类，它包含了所有的数据库，表，一切应用程序将需要与库交互时的引用。

然而，在有些情况下的应用程序有一个library或子项目也使用DBFlow来管理其数据库时候。这是一个重要的方案，因为它可以让你在用多个应用程序中重复使的数据库。此前，DBFlow不支持这种用例，并试图这样做的时候会失败。

为了解决这个问题，你必须确保数据库的module被加载。幸运的是，这是一个非常简单的过程。

将数据库添加到模块，首先更新你的 `build.gradle` 库的自定义一个 `apt`参数，这将放置 `GeneratedDatabaseHolder`类---好像定义一个不同的类（在相同的包中），因此这些类没有重复，在你加入"apply plugin: 'com.neenbedankt.android-apt'"之后，而不是在你差生依赖前增加这点：

```groovy
apt {
    arguments {
        targetModuleName 'Test'
    }
}
```

for KAPT:
```groovy

kapt {
    generateStubs = true
    arguments {
        arg("targetModuleName", "Test")
    }
}

```

通过传递`targetModuleName`，再把它添加到`GeneratedDatabaseHolder`创建`TestGeneratedDatabaseHolder` 模块。

在你的库（和应用），你应该使用标准方法初始化DBFlow。

```java
public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FlowManager.init(this);
    }
}
```

最后，指示DBFlow加载包含数据库模块（您可能需要构建应用程序生成的类文件才能够引用它）。

```java
FlowManager.initModule(TestGeneratedDatabaseHolder.class);
```

这个方法可以被调用多次，而不会对应用程序的状态产生任何影响, 因为它使已经加载的那些的映射
