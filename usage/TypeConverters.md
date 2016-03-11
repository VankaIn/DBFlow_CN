# 类型转换器

类型转换使`Model`内字段不一定是数据库类型的。它们把该字段的值转换成在数据库可以定义的类型。它们还定义了当一个模型被加载的方式，我们使用转换器重新创建字段。__注意__：`TypeConverter`只能转化成常规列，由于数据中的非确定性映射，`PrimaryKey` 或`ForeignKey`是不能转化的。

这些转换器在所有数据库共享。

如果我们指定model值作为`Model` 类的话，可能有一些字段非常意外的行为被定义为Column.FOREIGN_KEY

下面是实现LocationConverter，把位置转化成字符串：

```java

  // First type param is the type that goes into the database
  // Second type param is the type that the model contains for that field.
  @com.raizlabs.android.dbflow.annotation.TypeConverter
  public class LocationConverter extends TypeConverter<String,Location> {

    @Override
    public String getDBValue(Location model) {
        return model == null ? null : String.valueOf(model.getLatitude()) + "," + model.getLongitude();
    }

    @Override
    public Location getModelValue(String data) {
        String[] values = data.split(",");
        if(values.length < 2) {
            return null;
        } else {
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(values[0]));
            location.setLongitude(Double.parseDouble(values[1]));
            return location;
        }
    }
  }
```

要使用`LocationConverter`，使用类型转换器，我们只需添加类作为我们的表中的字段：

```java

@Table(...)
public class SomeTable extends BaseModel {


  @Column
  Location location;

}
```

## 列具体的类型转换器
作为3.0的TypeConverter可以在column-by-column基础上使用。

```java

@Table(...)
public class SomeTable extends BaseModel {


  @Column(typeConverter = SomeTypeConverter.class)
  Location location;

}
```

_注_: `enum` 类举希望从默认枚举转换（从枚举到字符串），你必须定义一个自定义类型转换为列。
