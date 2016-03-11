# 查询模型

Query Models 或`@QueryModel`是简单用于映射一个指定的，非标准查询的对象，返回一些不属于 `@Table`的列。类似 `@ModelView` 的定义，这些不能包含`@PrimaryKey`，但_必须_还扩展BaseQueryModel。

要创建一个:

```java

@QueryModel(database = TestDatabase.class)
public class TestQueryModel extends BaseQueryModel {

    @Column
    String newName;

    @Column
    long average_salary;

    @Column
    String department;
}

```

规则同样适用于表和视图，该字段必须是包私有，公共或私人用，带有getter和setter方法。如果你想不冗长定义与或注释的所有字段，那就设置`@QueryModel（allFields = TRUE）` 。
Same rules apply to Tables and Views, the fields must be package private, public, or private with getters and setters. If you wish to not verbosely define all fields with an annotation, set `@QueryModel(allFields=true)`.
