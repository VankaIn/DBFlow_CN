# 事务管理器
`TransactionManager`是管理一批DB操作的主要的类。它是用于检索，更新，保存和删除items。它是由原生SQL封装的，而且**事务**操作很简单。此类还提供了一种在同一线程上异步执行数据库操作，使数据业务做不阻止用户界面线程。

该`事务处理`利用了`DBTransactionQueue`。这个队列是基于Volley的`VolleyRequestQueue`通过使用`PriorityBlockingQueue`。此队列将使用下列顺序按优先执行我们的数据库事务（最高到最低）：
1. **UI**：保留将显示在用户界面的数据的操作。
2  **HIGH**：保留，这将影响用户任务互动的操作。
3. 一定时间内（不一定是马上）在UI在某一时刻的数据显示。
4  **NORMAL**：`Transaction`的默认优先级，增加事务的时候，应用程序并不需要访问。
5. **LOW**：低优先级，对于非基本任务保留。

`DBTransactionInfo`：持有在`DBTransactionQueue`上如何处理`BaseTransaction`的信息。它包含一个名称和优先级。这个名字纯粹是为了在运行时和执行时识别它的时候调试。优先级优先于上一段提及的优先级。

这些优先级只是`int`，你可以指定你自己更高或者不同的优先级。

对于先进的用法，`TableTransactionManager` 或通过扩展`TransactionManager`，您可以创建并指定自己的`DBTransactionQueue`。你就可以用这个类来管理你的事务了。


## 批量处理
处理保存一组数据，而又不阻塞ui线程，以前我们是这样做的：

```java

  new Thread() {
    @Override
    public void run(){
        database.beginTransaction();
        try {
            for(ModelClass model: models){
              // save model here
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
  }.start();
```

我们使用以下几行代码代替而之，使得方法实用，而且又在线程运行：

```java

TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(models)));
```

## 批量处理`Model`
`ProcessModelInfo`：描述了如何用一个事务来保存信息，例如`DBTransactionInfo`，table，一组models，和一个TransactionListener为事务。**注意***：该类的建立是为了大幅度简化方法数的TransactionManager上。

对于跨上百个`Model`的大行动中，首选的方法是在`DBTransactionQueue`运行它。这将在同一线程上进行数据库操作，以减轻同步锁定和UI线程阻塞。

对于大规模的`save()`操作，首选的方法是通过 `DBBatchSaveQueue`。这将运行一个批处理`DBTransaction` 一旦队列满（默认为50 models，并且可以修改）上的所有模型在同一时间。如果要保存少量的items，或者需要他们准确低保存，最好的选择是使用常规的保存事务。


### 例

```java

  ProcessModelInfo<SomeModel> processModelInfo = ProcessModelInfo<SomeModel>.withModels(models)
                                                                            .result(resultReceiver)
                                                                            .info(myInfo);

  TransactionManager.getInstance().saveOnSaveQueue(models);

  // or directly to the queue
  TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(processModelInfo));

  // Updating only updates on the ``DBTransactionQueue``
  TransactionManager.getInstance().addTransaction(new UpdateModelListTransaction(processModelInfo));

  TransactionManager.getInstance().addTransaction(new DeleteModelListTransaction(processModelInfo));
```

有很多各种各样的事务管理器方法在`DBTransactionQueue`中执行，用于用于读取，保存，updateing和删除。一探究竟！

## 检索模型
该`SelectListTransaction`和`SelectSingleModelTransaction`在`DBTransactionQueue`中进行select，它完成时`TransactionListener`将在UI线程调用。一个正常的SQLite.select（）将当前线程上完成。虽然这是简单的数据库操作，这要好得多，在`DBTransactionQueue`执行这些操作使其他操作不会导致主线程的“锁”。

```java

  // Just get all items from the table
  // You can even use Select and Where statements instead
  TransactionManager.getInstance().addTransaction(new SelectListTransaction<>(new TransactionListenerAdapter<TestModel.class>() {
     @Override
    public void onResultReceived(List<TestModel> testModels) {
        // on the UI thread, do something here
    }
  }, TestModel.class, condition1, condition2,..);
```

## 自定义事务处理
这个库可以很容易添加自定义的事务。将它们添加到事务管理方式：
This library makes it very easy to perform custom transactions. Add them to the `TransactionManager` by:

```java

TransactionManager.getInstance().addTransaction(myTransaction);
```


有几个方法可以创建你一个你想要的特定事务：
- 扩展 `BaseTransaction`将要求你在 `onExecute()`进行一些操作.

  ```java

  BaseTransaction<TestModel1> testModel1BaseTransaction = new BaseTransaction<TestModel1>() {
   @Override
   public TestModel1 onExecute() {
   // do something and return an object
     return testModel;
   }
  };
  ```

- `BaseResultTransaction` 增加了一个简单的 `TransactionListener` ，确保您可以监听事务更新。

  ```java

  BaseResultTransaction<TestModel1> baseResultTransaction = new BaseResultTransaction<TestModel1>(dbTransactionInfo, transactionListener) {
  @Override
  public TestModel1 onExecute() {
   return testmodel;
  }
  };
  ```

- `ProcessModelTransaction`发在在一个model内，使您能够定义如何在`DBTransactionQueue`处理在每一款model。

  ```java

  public class CustomProcessModelTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

  public CustomProcessModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
     super(modelInfo);
  }

  @Override
  public void processModel(ModelClass model) {
     // process model class here!
  }
  }
  ```

- `QueryTransaction`有你使用一个可查询的检索游标无你用什么方式。

```java

// any Where, From, Insert, Set, and StringQuery all are valid parameters
TransactionManager.getInstance().addTransaction(new QueryTransaction(DBTransactionInfo.create(),
  SQLite.delete().from(MyTable.class).where(MyTable_Table.name.is("Deleters"))));
```
