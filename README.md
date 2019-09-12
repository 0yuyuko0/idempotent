## 使用
### spring
将@Idempotent注解标注在bean的方法上，id为该幂等操作的唯一标识，使用spring-el语法 <br>
maxExecutionTime为该方法执行最大时间，超过该时间则id在redis中自动过期，duration为方法执行成功后 <br>
幂等持续时间

- TestPOJO.java，测试pojo类
```java
public class TestPOJO {
    private String username;

    public TestPOJO(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```
- TestBean.java，测试bean类
```java
@Component
 public class TestBean {
    //使用spring-el来获取方法参数中的值
     @Idempotent(id = "#testPOJO.getUsername()", rollbackFor = RuntimeException.class)
     public void hello(TestPOJO testPOJO) {
         System.out.println(String.format("hello %s\n", testPOJO.getUsername()));
     }
 }
```
- 测试
```java
    @Test
    public void testSpring() {
        testBean.hello(new TestPOJO("yuyuko"));
        testBean.hello(new TestPOJO("yuyuko"));
    }
```
- 输出
```java
hello yuyuko
2019-09-12 22:59:56.729  INFO 11428 --- [           main] c.y.i.i.dubbo.IdempotentFilter           : 拒绝执行方法[test],幂等操作id[idem:yuyuko]
```
### dubbo
将@Idempotent注解标注在dubbo的接口api上（注意是接口！）,方法重复调用时默认返回null <br>
所以请不要将注解标注在带返回值的具有副作用方法上.