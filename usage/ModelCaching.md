# 强大的模型缓存
在这个库模型缓存是很简单的，是非常可扩展的，可获取和使用。

`ModelCache`是一个用sqlite查询实现的缓存接口，`FlowQueryList`， `FlowCursorList`，或者其他你想使用它的任何地方。

## 在表中启用高速缓存
只要增加 `cachingEnabled = true`在你得`@Table`注解中就可以启用表的高速缓存。要启用类缓存多列`@PrimaryKey`，你_必须_定义一个`@MultiCacheField`对象（下文解释）。

当查询在数据库运行时，它将在缓存中存储模型的实例，并且缓存是一个有效的内存管理单独负责的。

有几种缓存方式：
There are a few kinds of caches supported out the box:
  1. `ModelLruCache`  -> 使用LruCache, Lru规则自动对内存进行管理。
  2.  `SimpleMapCache` -> 简单存储models在预定容量的`Map`中（默认为HashMap中）。
  3. `SparseArrayBasedCache` -> 一个基于`SparseArray`下的int->object key/value 类型。它适用于任何_数量_的后代或基本对应（Integer, Double, Long等），或`MulitCacheConverter`返回相同的密钥类型。

**注意** 如果你运行一个带有字段的 `SELECT` ，你也可以缓存整个`Model` 。强烈建议只加载在这种情况下全款，因为缓存机制将弥补最效率的问题。

默认的缓存是 `SimpleMapCache`。你可以在 `@Table`指定 `cacheSize()` 来设置默认缓存的内容的大小。一旦指定自定义缓存，此参数无效。

要使用自定义缓存， 在`Model`类中指定缓存：

```java
@ModelCacheField
public static ModelCache<CacheableModel3, ?> modelCache = new SimpleMapCache<>();
```

该 `@ModelCacheField` 必须是公共静态。

作为3.0，现在DBFlow从缓存加载时巧妙地重新加载`@ForeignKey` 的关系。

### 缓存到底是如何工作
  1.每个`@Table`/`Model`类都有其自己的缓存，它们表与表和父类与之类之间不共享。
  2. 它“拦截”查询运行并引用缓存（下面解释）。
  3. 当表中使用了任何包含 `Insert`, `Update`, 或者 `Delete` 方法，将强烈建议不要进行缓存，当运行这些方法后，模型作为缓存将不会更新（因为效率和性能方面的原因）。如果您需要运行这些操作，一个简单的`FlowManager.getModelAdapter(MyTable.class).getModelCache().clear()` 运行后，查询的缓存将失效，它将会继续更新其信息。
  4. 从缓存中修改对象遵循Java引用的规则：在一个线程中更改字段值可能导致您的应用程序数据数据不一致，直到`save()`, `insert()` or `update()`方法运行，才会重新到数据库加载正确的数据。更改任何字段直接从缓存中修改对象（当从它直接加载），所以多加留意。

当通过包装语言运行查询，DBFlow将：
  1. 运行查询，从而产生一个`游标`
  2. 通过`游标`检索主键列值
  3. 如果组合键是在高速缓存中，我们：
    1. 刷新关系，如@ForeignKey（如果存在的话）。_提示：_通过表缓存使此更快地实现这一目。
    2. 然后返回缓存的对象。
  4. 如果对象不存在，我们从DB加载完整的对象，和随后的查询到相同的对象将从缓存返回对象（直到它驱逐或从高速缓存清除。）

### 多主键缓存
在3.0，DBFlow支持的高速缓存的多个主密钥。它_需要_那些有一个以上的主键的模型定义一个`@MultiCacheField`：

```java
@Table(database = TestDatabase.class, cachingEnabled = true)
public class MultipleCacheableModel extends BaseModel {

    @MultiCacheField
    public static IMultiKeyCacheConverter<String> multiKeyCacheModel = new IMultiKeyCacheConverter<String>() {

        @Override
        @NonNull
        public String getCachingKey(@NonNull Object[] values) { // in order of the primary keys defined
            return "(" + values[0] + "," + values[1] + ")";
        }
    };

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;

    @ForeignKey(references = {@ForeignKeyReference(columnName = "associatedModel",
            columnType = String.class, foreignKeyColumnName = "name", referencedFieldIsPackagePrivate = true)})
    TestModel1 associatedModel;

}
```

返回类型可以是任何东西，要`ModelCache`定义的类支持返回类型。

### FlowCursorList + FlowQueryList
在`@Table`/`Model`相关缓存中，`FlowCursorList` 和 `FlowQueryList` 利用单独 的`ModelCache`，该覆盖默认的缓存机制：

```java

@Override
protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new MyCustomCache<>();
}
```

### 自定义缓存
只要你想的话，你可以创建自己的缓存并使用它。

一个从支持库复制的`LruCache`缓存使用例子：
An example cache is using a copied `LruCache` from the support library:

```java

public class ModelLruCache<ModelClass extends Model> extends ModelCache<ModelClass, LruCache<Long, ModelClass>>{

    public ModelLruCache(int size) {
        super(new LruCache<Long, ModelClass>(size));
    }

    @Override
    public void addModel(Object id, ModelClass model) {
        if(id instanceof Number) {
            synchronized (getCache()) {
                Number number = ((Number) id);
                getCache().put(number.longValue(), model);
            }
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }

    @Override
    public ModelClass removeModel(Object id) {
        ModelClass model;
        if(id instanceof Number) {
            synchronized (getCache()) {
                model = getCache().remove(((Number) id).longValue());
            }
        }  else {
            throw new IllegalArgumentException("A ModelLruCache uses an id that can cast to" +
                                               "a Number to convert it into a long");
        }
        return model;
    }

    @Override
    public void clear() {
        synchronized (getCache()) {
            getCache().evictAll();
        }
    }

    @Override
    public void setCacheSize(int size) {
        getCache().resize(size);
    }

    @Override
    public ModelClass get(Object id) {
        if(id instanceof Number) {
            return getCache().get(((Number) id).longValue());
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }
}
```
