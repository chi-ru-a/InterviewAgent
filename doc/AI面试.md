## 知识点

### 注解

* @ConfigurationProperties(prefix = "interview.graph")：**springboot 、类、@Bean方法**；把 application.yml 或 application.properties 中，以 interview.knowledge 开头的配置项，自动映射到当前类的字段上

* @MappedTypes(List.class)：**mybatis、自定义的TypeHandler类**；声明这个 TypeHandler 负责处理哪个（或哪些）Java 类型

* @MappedJdbcTypes(JdbcType.OTHER)：**mybatis、自定义的TypeHandler类**；声明这个 TypeHandler 负责处理哪种 JDBC 类型

  * TypeHandler接口、BaseTypeHandler抽象类，对TypeHandler做了一层封装，自定义类继承BaseTypeHandler即可

  * 自定义 TypeHandler 永远是双向的（既能写，也能读）

  * BaseTypeHandler<泛型> 里填的什么，就是什么 Java 类型；@MappedJdbcTypes 里填的什么，就是什么数据库类型。

  * 两者通过 setNonNullParameter (写) 和 getNullableResult (读) 互相转换。

  * ```java
    /**
     * PostgreSQL jsonb 类型处理器
     * 使用 PGobject（运行时可用）确保 JDBC 参数类型正确设置为 jsonb，
     * 避免 PostgreSQL 拒绝 varchar → jsonb 的隐式转换。
     * 
     * 自定义 TypeHandler 永远是双向的（既能写，也能读）
     * BaseTypeHandler<泛型> 里填的什么，就是什么 Java 类型；@MappedJdbcTypes 里填的什么，就是什么数据库类型。
     * 两者通过 setNonNullParameter (写) 和 getNullableResult (读) 互相转换。
     */
    @MappedJdbcTypes(JdbcType.OTHER)
    @MappedTypes(List.class)
    public class JsonbTypeHandler extends BaseTypeHandler<List<String>> {
    
        private static final ObjectMapper MAPPER = new ObjectMapper();
    
        // 只处理非空情况
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
            try {
                String json = MAPPER.writeValueAsString(parameter);
                // 反射创建 PGobject，因为 postgresql 依赖 scope=runtime 编译期不可见
                try {
                    Object pgObject = Class.forName("org.postgresql.util.PGobject")
                            .getDeclaredConstructor().newInstance();
                    pgObject.getClass().getMethod("setType", String.class).invoke(pgObject, "jsonb");
                    pgObject.getClass().getMethod("setValue", String.class).invoke(pgObject, json);
                    ps.setObject(i, pgObject);
                } catch (ReflectiveOperationException e) {
                    // Fallback: 用 Types.OTHER 让 PG 驱动自动推断
                    ps.setObject(i, json, Types.OTHER);
                }
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to serialize list to JSON", e);
            }
        }
    
        @Override
        public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return parseJson(rs.getString(columnName));
        }
    
        @Override
        public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return parseJson(rs.getString(columnIndex));
        }
    
        @Override
        public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return parseJson(cs.getString(columnIndex));
        }
    
        private List<String> parseJson(String json) {
            if (json == null || json.isEmpty()) {
                return null;
            }
            try {
                return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize JSON to List<String>", e);
            }
        }
    }
    ```

* @EnableAsync：**spring 、类或配置类**；让 Spring 能够识别并处理 @Async 注解【**spring、方法，也可以标在类上**，该类所有方法都是异步，但一定要搭配@EnableAsync使用】，使被标注的方法在独立线程中运行，而不是阻塞调用者

* @EnableWebSocket：**spring 、配置类**；激活 Spring 的 WebSocket 基础设施，让你能够注册并处理 WebSocket 请求

  * 标在 @Configuration 类上后，通常需要让该类实现 **WebSocketConfigurer** 接口，并重写 **registerWebSocketHandlers** 方法来**注册具体的 WebSocket 处理器和路径（如 /echo）**

  * ```java
    @Configuration
    @EnableWebSocket
    @RequiredArgsConstructor
    public class WebSocketConfig implements WebSocketConfigurer {
    
        private final InterviewWebSocketHandler interviewWebSocketHandler;
    
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(interviewWebSocketHandler, "/ws/interview")
                    .setAllowedOrigins("*");
        }
    }
    ```

* 





**mybatis 的类型处理器 TypeHandler**：在 Java 类型和 JDBC 类型（数据库类型）之间进行双向转换

![image-20261106184323624](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106184323624.png)

![image-20261106184545717](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106184545717.png)







### 数据模型

各种各样的数据模型，总之全是 Java 类，用于封装数据，持久层、业务层、业务层和控制层交互、客户端和控制层交互、Webscoket通信、视图（展示给前端的数据，和 resopne 有些区别）、接收复杂查询条件（和 request 有些区别，request 增删改查全接收，qo 只接收查询）

![image-20261106174855075](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106174855075.png)

![image-20261106175034963](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106175034963.png)

![image-20261106175307889](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106175307889.png)

![image-20261106175610441](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106175610441.png)

![image-20261106175831770](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106175831770.png)

![image-20261106180228824](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106180228824.png)

![image-20261106180300764](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106180300764.png)

### 枚举

![image-20261106191917767](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106191917767.png)

```java
package com.zunff.interview.constant;

import lombok.Getter;

/**
 * 问题类型枚举
 */
@Getter
public enum QuestionType {

    /**
     * 枚举值、值当中的字段写为 private final 属性、全参构造、getter 方法
     * 可以有别的方法，比如通过自定义方法，根据某个字段查找对应的枚举值【通过 values() 遍历查找】
     */

    // ========== 技术轮问题类型 ==========
    TECHNICAL_BASIC(1, "technical_basic", "技术基础", "technical"),
    PROJECT_EXPERIENCE(2, "project_experience", "项目经验", "project"),
    TECHNICAL_CHALLENGE(3, "technical_challenge", "技术难点", "technical"),
    SYSTEM_DESIGN(4, "system_design", "系统设计", "technical"),

    // ========== 业务轮问题类型 ==========
    BUSINESS_UNDERSTANDING(5, "business_understanding", "业务理解", "business"),
    COMMUNICATION(6, "communication", "沟通协作", "soft"),
    PROBLEM_SOLVING(7, "problem_solving", "场景分析", "business"),
    PROFESSIONALISM(8, "professionalism", "职业素养", "soft"),

    // ========== 追问类型 ==========
    FOLLOW_UP(9, "follow_up", "追问", "followup-basic"),
    CHALLENGE_QUESTION(10, "challenge_question", "挑战题", "followup-challenge"),
    DEEP_DIVE(11, "deep_dive", "深入追问", "followup-deep-dive"),
    ;

    private final int code;
    private final String apiName;
    private final String displayName;
    private final String evaluationPrompt;

    QuestionType(int code, String apiName, String displayName, String evaluationPrompt) {
        this.code = code;
        this.apiName = apiName;
        this.displayName = displayName;
        this.evaluationPrompt = evaluationPrompt;
    }




    /**
     * 根据显示名称获取问题类型
     */
    public static QuestionType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return TECHNICAL_BASIC;
        }
        String normalized = displayName.trim().toLowerCase().replace('-', '_');
        for (QuestionType type : values()) {
            if (type.displayName.equals(displayName)
                    || type.apiName.equalsIgnoreCase(normalized)
                    || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return TECHNICAL_BASIC;
    }

    public static QuestionType fromCode(Integer code) {
        if (code == null) {
            return TECHNICAL_BASIC;
        }
        // values() 方法返回枚举类的所有枚举值，遍历这些值，找到与给定 code 匹配的枚举类型
        // values() 是在编译时，Java 为每一个枚举类自动生成的一个静态方法
        for (QuestionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return TECHNICAL_BASIC;
    }

    /**
     * 判断是否为技术轮问题类型
     */
    public boolean isTechnicalType() {
        return this == TECHNICAL_BASIC
                || this == PROJECT_EXPERIENCE
                || this == TECHNICAL_CHALLENGE
                || this == SYSTEM_DESIGN;
    }

    /**
     * 判断是否为业务轮问题类型
     */
    public boolean isBusinessType() {
        return this == BUSINESS_UNDERSTANDING
                || this == COMMUNICATION
                || this == PROBLEM_SOLVING
                || this == PROFESSIONALISM;
    }

    /**
     * 判断是否为追问类型
     */
    public boolean isFollowUpType() {
        return this == FOLLOW_UP
                || this == CHALLENGE_QUESTION
                || this == DEEP_DIVE;
    }
}
```

### common与项目结构

![image-20261106192640725](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106192640725.png)





![image-20261106192114198](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106192114198.png)

![image-20261106192211172](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106192211172.png)

![image-20261106192312410](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106192312410.png)

### Stream流

```java
import java.util.Scanner;
import java.util.Arrays;
import java.lang.Math;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        /**
        转为数组，Arrays 可以排序，进而求极差
        Arrays 主要提供排序、查找、比较、填充、复制等操作，没有求和操作
        可以用 Stream 流的 sum 方法
        Stream 流是用来简化数组、集合的操作的；创建、中间【过滤】、终结【求和、统计】

        Stream 流的创建
            List、Set：collection.stream()、collection.parallelStream()
                顺序流：单线程、串行
                并行流：多线程、大任务拆成小任务，最后再汇总结果；用于数据量极大且处理逻辑耗时的场景
            Arrays.stream()：接收对象数组和基本类型数组
            一组具体的值：Stream.of(T... values)

            无限流：Stream.iterate(0, n -> n + 2)  无限生成偶数，种子 0 就是 n 的初始值
                    Stream.generate(supplier)  基于 supplier 这个接口的实现来生成
            从文件/IO生成
                Files.lines(Path)：把文本文件的每一行当作一个字符串元素，返回一个 Stream<String>
                BufferedReader.lines()：把读取器中的每一行当作一个字符串元素，返回一个 Stream<String>


            中间操作：
                filter、distinct()、limit()【截断，通常配合无限流使用】、skip()【跳过前N个】等
                map：stream.map(String::length) 把字符串流变成整数流
                flatMap(Function)：扁平化映射，比如把 Stream<List<Integer>> 变成 Stream<Integer>
                sorted()、sorted(Comparator)：排序
            终结操作：
                forEach(Consumer)、anyMatch(Predicate)、allMatch(Predicate)、noneMatch(Predicate)【一个元素、所有元素、没有元素满足条件】
                findFirst()：查找第一个元素
                findAny()：返回任意一个元素
                reduce(BinaryOperator)：比如stream.reduce((a, b) -> a + b) 求和
                count()：计数
                max(Comparator) / min(Comparator：返回最大、最小值
                collect(Collector)：收集，将流转换成其他数据结构
                    stream.collect(Collectors.toList()) 转为 List
                    stream.collect(Collectors.toSet()) 转为 Set
                    stream.collect(Collectors.joining(",")) 拼接成字符串


            Java 有 4 种 Stream 流
                Stream<T>：对象流，里面装的是对象
                IntStream：int 专用流
                LongStream：long 专用流
                DoubleStream：double 专用流
                之所以有这些流，是为了性能和避免装箱拆箱，如果你用 Stream<Integer> 处理 100 万个数字，Java 需要不停地把 int 包装成 Integer 对象，再拆箱，这非常消耗内存和时间；用 IntStream 直接处理基本类型 int，效率极高；
                这些非标准 Stream 流，提供了一些特有方法 .sum()、.average()、range()【创建指定范围的数字流  range(start, end)（左闭右开）和 rangeClosed(start, end)（左闭右闭）】、summaryStatistics()【一次性获取数量、和、最小值、最大值、平均值】
         */
        int n = in.nextInt();
        int jiCha = 0;
        double fangCha = 0.0;
        for (int i = 1; i <= n; i++) {
            int m = in.nextInt();
            in.nextLine();
            String[] strArr = in.nextLine().split(" ");
            int[] intArr = new int[strArr.length];
            for (int j = 0; j < strArr.length; j++) {
                // Integer.parseInt() 返回的是 int，不是 Integer
                intArr[j] = Integer.parseInt(strArr[j]);
            }
            // Arrays.sort() 是直接对这个数组本身排序，不会返回新数组，如果要想保留原来的数组，要先复制保留一份
            Arrays.sort(intArr);
            jiCha = intArr[intArr.length - 1] - intArr[0];
            // 计算方差
            // Arrays.stream() 对象数组和基本类型数组都可以接收，但是 sum() 只有基本类型数组才有【int、long、double】
            int sum = Arrays.stream(intArr).sum();
            double avg = sum / (double) intArr.length;
            for (int k = 0; k < intArr.length; k++) {
                fangCha += Math.pow((intArr[k] - avg), 2);
            }
            fangCha /= intArr.length;
            // 打印  .f 是给小数用的，double、float
            System.out.printf("%d %.3f\n", jiCha, fangCha);
            fangCha = 0.0;
        }
    }
}
```

`Collectors` 是 Java 8 提供的一个**工具类**（位于 `java.util.stream` 包下），专门配合 Stream 的 **`collect()`** 终结操作来使用。

![image-20261106221919221](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106221919221.png)

![image-20261106221930850](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106221930850.png)

![image-20261106221942815](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106221942815.png)











### React

![image-20261110091952003](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110091952003.png)

![image-20261110092020251](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110092020251.png)

![image-20261110092034379](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110092034379.png)

![image-20261110092059069](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110092059069.png)

![image-20261110092133909](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110092133909.png)

![image-20261110092202089](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110092202089.png)









## AI面试系统项目

### 需要掌握的技术

![image-20261110091841153](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110091841153.png)

![image-20261110091824720](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110091824720.png)



### 问题

#### 1.工作流暂停

**1.工作流暂停和恢复用在了哪些地方、怎么实现的【按照整个主图来说，哪些节点会暂停和恢复工作流、具体是怎么实现的】**

主图的人物画像分析节点前、技术轮和业务轮面试子图，提问节点执行之后，会暂停工作流；

等待用户输入；用户自我介绍和用户对问题的回答；

拿到用户的数据，再恢复工作流的执行；

暂停工作流是为了拿到用户的数据，基于用户的数据，再走下面的流程，可以实现HITL；



![image-20261109193239466](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193239466.png)

```text
START
  ↓
INIT
  ├── JOB_ANALYSIS
  └── SELF_INTRO
        ↓
    等待两个分支汇合
        ↓
    PROFILE_ANALYSIS（暂停在执行之前）
```

![image-20261109193404135](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193404135.png)

![image-20261109193454758](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193454758.png)

![image-20261109193612336](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193612336.png)

![image-20261109193642172](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193642172.png)

![image-20261109193731885](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109193731885.png)



#### 2.生题子图的规划和聚合节点

**2.生题子图的题目规划节点是做什么的、具体是怎么做的，生题子图最后为什么要有一个聚合节点，它是怎么做的？**

题目规划节点负责划分考察范围，题目生成节点根据划分结果生成具体问题。

![image-20261109194718962](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109194718962.png)

![image-20261109195702617](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109195702617.png)

题目规划节点抛异常失败，规划为 null，四个生题节点依然可以生题，只是没有了规划节点提供的**主题分配和防重复约束**

四个生题节点的代码都是这样，从共享状态中读取生题规划，如果规划的改类型题目为0，则返回空列表，不为零调用生题服务生题，题目放到对应的列表中，生题时发生异常返回空列表；

```java
/**
 * 技术基础题生成节点
 * 独立节点，负责生成技术基础题目
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TechBasicGenNode {

    private final QuestionGenerationService questionGenerationService;

    /**
     * 执行技术基础题生成
     */
    public CompletableFuture<Map<String, Object>> execute(BatchQuestionGenState state) {
        try {
            int count = state.getTechnicalBasicCount();
            if (count <= 0) {
                log.info("技术基础题数量为 0，跳过生成");
                return CompletableFuture.completedFuture(Map.of(
                        BatchQuestionGenState.TECHNICAL_BASIC_QUESTIONS, new ArrayList<GeneratedQuestion>()
                ));
            }

            log.info("开始生成技术基础题，数量: {}", count);

            // 调用题目生成服务
            return questionGenerationService.execute(state, count, QuestionType.TECHNICAL_BASIC, BatchQuestionGenState.TECHNICAL_BASIC_QUESTIONS)
                    .thenApply(result -> {
                        @SuppressWarnings("unchecked")
                        List<GeneratedQuestion> questions = (List<GeneratedQuestion>) result.get(BatchQuestionGenState.TECHNICAL_BASIC_QUESTIONS);
                        log.info("技术基础题生成完成，实际生成: {}", questions != null ? questions.size() : 0);
                        return Map.of(BatchQuestionGenState.TECHNICAL_BASIC_QUESTIONS, questions != null ? questions : new ArrayList<GeneratedQuestion>());
                    });

        } catch (Exception e) {
            log.error("技术基础题生成失败", e);
            return CompletableFuture.completedFuture(Map.of(
                    BatchQuestionGenState.TECHNICAL_BASIC_QUESTIONS, new ArrayList<GeneratedQuestion>()
            ));
        }
    }
}
```



聚合节点

1.合并题目：技术、项目，合并到技术队列；业务、软技能合并到业务队列

2.重新编号

3.处理降级，如果题目都生成失败，都是空列表，填充预设的题目，技术轮、业务轮各预设了1个；【技术、项目生题节点都返回空，技术轮才使用预设的题目；业务、软技能生题节点都返回空，业务轮轮才使用预设的题目；】



#### 3.主图各节点作用

3.主图当中，初始化节点、自我介绍节点、岗位分析节点、人物画像节点都是做什么的、具体是怎么做的？

**初始化节点**

![image-20261109203508919](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109203508919.png)

**岗位分析节点**

解决：当前招聘的是什么类型的岗位，面试应该如何分配技术题和业务题

输入岗位JD，使用预设的岗位分析提示词，调用大模型，解析模型结果为预设的Dto模型

![image-20261109201918284](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109201918284.png)

![image-20261109202025802](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109202025802.png)

**自我介绍节点**

![image-20261109202730367](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109202730367.png)

**画像分析节点**

![image-20261109202856501](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109202856501.png)

![image-20261109203157431](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109203157431.png)

![image-20261109203356254](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109203356254.png)



#### 4.报告节点生成报告与答题子图的评估

4.主图的生成报告节点是怎么做的？这里的生成报告和答题子图，用户回答完一题之后，调用多模态模型进行评估在持久化，和这当中的评估有什么区别和联系？

![image-20261109203645315](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109203645315.png)

生题子图主题评估

```text
用户回答
   ↓
ComprehensiveEvaluationNode【生题子图的多模态评估节点】
   ↓
调用 Omni 多模态模型【使用多模态评估提示词并调用多模态模型】
   ↓
得到当前问题的 EvaluationBO【解析模型响应为预设的Bo模型】
   ↓
PersistenceNode
   ↓
保存 answer_record
保存 evaluation_record
   ↓
FollowUpDecisionNode
```

![image-20261109204006141](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109204006141.png)

**主图生成报告节点**

![image-20261109204205943](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109204205943.png)

![image-20261109204258389](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109204258389.png)

![image-20261109204426893](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109204426893.png)

![image-20261109204537569](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109204537569.png)



#### 5.四类题目和两类队列

5.答题子图，也就是技术和业务轮子图，从队列获取问题节点是怎么做的？生题子图不是生成了4类问题吗？技术、项目、业务、软技能，然后把4类问题放到两个队列（技术和业务），哪类问题放到了技术队列中，哪类问题放到了业务队列中？

* 技术、项目放到技术队列，技术轮
* 业务、软技能放到业务队列，业务轮

**从队列取问题流程**

1.判断当前轮次【技术还是业务】，通过共享状态中的isTechnicalRound()字段

```java
if (state.isTechnicalRound()) {
    queueKey = TECHNICAL_QUESTIONS_QUEUE;
} else {
    queueKey = BUSINESS_QUESTIONS_QUEUE;
}
```

2.复制当前轮次的问题队列

```java
List<GeneratedQuestion> queue =
    new ArrayList<>(当前轮次的题目队列);
```

3.操作这个复制的队列，取出队头题目

```java
GeneratedQuestion nextQuestion =
    queue.removeFirst();
```

4.题目消费成功之后，更新状态图中的队列为当前这个复制队列





#### 6.答题子图，追问策略节点

6.答题子图中，追问决策节点是怎么做的？追问节点后面有3种追问策略和一个下一题节点，各自的条件是什么？也就是什么情况下会走到什么节点？除了追问策略，有没有别的限制，比如追问上限什么的？技术轮和业务轮是怎么切换的？



**追问决策怎么做 / 追问决策节点怎么做**

![image-20261109205848613](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109205848613.png)

![image-20261109210025973](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109210025973.png)

**其他限制**

* 技术轮、业务轮设置的最大追问次数
* 追问后，分数提升不足 5 分，停止继续追问，进入下一题

**追问策略**

![image-20261109210341046](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109210341046.png)

**怎么追问**

![image-20261109210315616](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109210315616.png)

**轮次的切换**

技术轮队列为空进入业务轮，也就是技术轮生成的题目问完，才会进入业务轮；

生成题目的数量是有限制的；



#### 7.视频帧和ASR转录文本为什么加时间戳

7.视频帧和转录文本为什么要携带时间戳？

为了让模型知道用户在什么时间说了什么，以及当时的面部和肢体表现是什么

![image-20261109211036885](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109211036885.png)

![image-20261109211428544](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109211428544.png)



#### 8.降级汇总

8.你提到的降级策略中，岗位分析失败，使用默认岗位分析结果，岗位那么多，怎么预设的默认岗位分析结果？人物画像分析失败，使用默认级别匹配结果，人物画像是啥？分析它有啥用？题目规划失败，不使用规划结果，继续生成题目，题目规划到底在规划什么？规划失败还能生成4类题目吗？题目生成失败使用默认题目，岗位那么多，怎么预设默认题目？

![image-20261109212715726](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109212715726.png)

岗位类型设置了三种：技术型、业务型、技术业务均衡型

**岗位分析失败，统一采用**

![image-20261109211627679](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109211627679.png)

**候选人画像节点失败，默认使用这些**

![image-20261109211808992](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109211808992.png)

**题目规划节点失败**

![image-20261109212021022](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109212021022.png)

**题目生成失败**

![image-20261109212342160](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109212342160.png)

![image-20261109212504691](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109212504691.png)



#### 9.会话创建为什么不放在初始化节点中

**会话的创建**

![image-20261109215337022](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215337022.png)

**流程**

![image-20261109215356535](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215356535.png)



![image-20261109215425287](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215425287.png)

![image-20261109215455348](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215455348.png)

**为什么不把会话的创建放到初始化节点中**

![image-20261109215551032](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215551032.png)

![image-20261109215640251](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215640251.png)

![image-20261109215726116](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109215726116.png)



怎么保证模型输出可以解析为预设的数据模型：提示词约束、框架的结构化输出、输出后的字段校验、失败重试、最大重试次数兜底







### 模型/工具/数据库

问答模型：qwen3.5-flash

嵌入模型：DashScope text-embedding-v3（1024维）

多模态模型：qwen3.5-omni-plus

ASR：阿里的 fun-asr-realtime

TTS：qwen3-tts-instruct-flash-realtime



前端：React

后端：Spring Boot、LangGraph4j

数据库：PostgreSQL 存业务数据，PGVector存向量数据



工具：终止工具、tavily联网搜索工具







### 项目介绍

这个项目是一个基于 Spring Boot 和 LangGraph4j 开发的 AI 视频面试系统，前端使用 Next.js，后端使用 Java，数据库使用 PostgreSQL。

整个系统主要分为前端交互层、后端业务层、AI 工作流层和数据存储层。

前后端交互方面，主要采用 HTTP 和 WebSocket 两种方式。HTTP 负责查询面试历史和最终报告，WebSocket 负责面试启动、问题推送以及实时音视频数据传输。

AI 工作流方面，我使用 LangGraph4j 编排整个面试流程。整体分为一个主图和多个子图。主图负责控制面试生命周期，包括岗位分析、候选人画像分析、题目生成、技术面试、业务面试和最终报告生成。

其中，题目生成采用独立子图，先进行题目规划，再并行生成技术基础题、项目经验题、业务场景题和软技能题。技术面试和业务面试复用同一套轮次子图，内部包含提问、综合评估、结果持久化和追问决策等节点，根据用户回答动态决定是否进行普通追问、深入追问或者挑战式追问。

音视频方面，前端通过浏览器获取摄像头和麦克风数据，音频经过降采样和 PCM 编码后，通过 WebSocket 发送给后端，后端调用实时 ASR 获取语音转录。视频则通过关键帧筛选机制上传，并保留时间戳。用户回答完成后，将转录文本、原始音频和视频帧一起交给 Omni 多模态模型进行综合评估。

数据存储方面，使用 PostgreSQL 保存面试会话、回答记录和评估结果，同时将 LangGraph4j 的 Checkpoint 持久化到数据库，实现面试中断后的状态恢复。

工程稳定性方面，主要做了异步执行、失败降级、连续失败熔断、Checkpoint 断点恢复、音视频缓存限制以及资源清理等设计，尽量保证外部模型或音视频服务出现异常时，面试流程仍然能够继续运行。





**为什么使用 websocket，前后端是怎么实现 websocket 的？**



**工作流编排**





**模型/工具/数据库**

为什么这样选型？

问答模型：qwen3.5-flash

嵌入模型：DashScope text-embedding-v3（1024维）

多模态模型：qwen3.5-omni-plus

ASR：阿里的 fun-asr-realtime

TTS：qwen3-tts-instruct-flash-realtime



前端：React

后端：Spring Boot、LangGraph4j

数据库：PostgreSQL 存业务数据，PGVector存向量数据



工具：终止工具、tavily联网搜索工具



**整体流程**

```text
AI 面试系统运行时序
1. 用户填写简历和岗位信息
   
   前端通过 WebSocket 发送 start_interview。
2. 后端创建面试会话
   
   保存到 PostgreSQL，生成 sessionId，返回 session_created。
3. 执行 LangGraph4j 主图
   
   初始化、岗位分析和自我介绍阶段并行执行。
4. 用户进行自我介绍
   
   前端采集音频，后端通过 ASR 转录，完成后恢复工作流。
5. 分析候选人画像并生成题目
   
   执行批量题目生成子图，生成技术题和业务题。
6. 进入技术面试
   
   推送问题，TTS 朗读，用户通过摄像头和麦克风回答。
7. 评估和追问
   
   ASR、视频帧、音频数据交给 Omni 模型综合分析，保存评估结果，决定是否追问。
8. 进入业务面试
   
   复用同一套轮次子图，完成业务问题及追问。
9. 生成最终报告
   
   汇总面试数据和评估结果，生成报告，保存到面试会话表。
10. 前端展示报告
    
    通过报告查询接口获取报告，展示评分、优缺点及改进建议。
```





![image-20261106172234120](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106172234120.png)



为什么使用 websocket 来进行通信

![image-20261106172400130](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261106172400130.png)



前端负责采集用户信息和音视频数据，后端负责管理面试会话，LangGraph4j 负责组织 AI 面试流程，数据库负责保存面试数据和工作流状态。



**面试启动流程**

* 用户上传简历，输入岗位JD，点击开始面试；
* 前端将简历和岗位信息发送给后端；
* 后端生成sessionId、创建会话、将会话持久化，`InterviewSessionServiceImpl` 生成 sessionId，将会话信息保存到 `interview_session` 表；
* 然后将 sessionId 推送给前端，前端跳转到视频面试页面；
* 后端使用虚拟线程异步执行面试工作流 `executeInterviewGraph()`，不阻塞 WebSocket 消息处理线程；
* 向前端推送面试题，前端更新页面，用户进行答题；



**面试交互流程**

![image-20261109160531152](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109160531152.png)

**AI面试的五张表**：最终的报告没有保存

保存每一场面试的基本信息

![image-20261109152959210](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109152959210.png)

保存用户每道题的回答

![image-20261109153046507](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109153046507.png)

保存 AI 对用户每道回答的评估结果

![image-20261109153413041](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109153413041.png)

保存 LangGraph 工作流检查点，支持断点恢复

需要注意，thread_id 被设置为了唯一索引（截图中没有截取建索引语句），同一个 thread_id 只能存在一条记录，在LangGraph状态图中，thread_id 其实就是 session_id，因此，同一个会话保存的是最新的检查点而不是检查点历史列表

![image-20261109153545090](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109153545090.png)

面试知识库表 (RAG)：用于知识库检索辅助出题

* 项目使用 Spring AI 的 `PgVectorStore` 自动配置，表名是 `vector_store`
* 表结构包含：id(UUID), content(TEXT), metadata(JSONB), embedding(vector(1024))【id、原始文本、元信息、文本向量】
* 使用阿里云 DashScope text-embedding-v3 模型（1024维，与配置匹配）
* 数据库：PostgreSQL + pgvector
* 相似度：余弦距离
* 向量索引：HNSW

### RAG

tavily联网搜索不是用来出题的，是用于简历分析项目的；要编一下

知识库没有文档、没有离线建库的过程，要编一下



加载、切分、索引、元数据增强、向量化、落库

重写、扩展、元数据过滤、相似度检索、重排序、检索增强





编造：文档中是搜集的很多公司的题目，把这些题目弄为统一的格式（比如：公司、岗位、题目类型、题目信息），切分时按照题目切分，查询时将公司、岗位、题目类型都作为元数据，以题目类型做元数据过滤，以公司、岗位做重排，讲检索到的题目放到模型上下文，给模型的提示词 “检索到的参考题目只能作为宽泛的灵感，不允许直接复制或轻微改写，必须结合当前候选人和岗位生成原创问题。”

#### 离线建库

**搜集+清洗+统一格式**

从公开的技术博客、面试经验分享这些文章中收集题目，人工整理 + 清洗

然后将题目统一为：公司、岗位、题目类型、题目 这种格式，并对 公司、岗位、题目类型 进行归一化；【岗位：技术型、业务型、技术业务均衡型；题目类型：技术、项目、业务、软技能；公司：与腾讯有关的都改为腾讯、与小米有关的都改为小米，不要那些有限公司、股份有限公司这些后缀】，整理到一个 txt 文件中



这一块直接说让AI搜集的怎么样；让AI编造的模拟数据

把公司、岗位、题目类型归一化要求说清楚，再提供一些目标公司，然后让AI整理成txt文件，题目之间以空行隔开，再人工核查；

**加载+切分**

![image-20261110104258160](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110104258160.png)

![image-20261110104328002](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110104328002.png)

![image-20261110104427564](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110104427564.png)

**向量化+落库**

阿里云 DashScope text-embedding-v3 模型（1024维）

![image-20261110104526508](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110104526508.png)



**检索增强**

根据岗位分析结果构造RAG查询文本

用题目类型进行元数据过滤

余弦相似度检索

用向量相似度+公司名称相似度+岗位相似度进行重排

取topk的题目信息放到上下文，让模型作为参考，不允许直接复制或轻微改写，必须结合当前候选人和岗位生成原创问题



#### 查询增强

1.根据岗位分析结果来构造的RAG查询文本（因为用户输入的只有简历和岗位JD）

![image-20261110100721644](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110100721644.png)

2.元数据过滤 + 向量检索 + 重排

![image-20261110100914993](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110100914993.png)

![image-20261110101103158](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110101103158.png)

![image-20261110101146311](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110101146311.png)

如果目标公司或岗位为空，或者候选记录缺少相应元数据，该维度就不参与加权，剩余权重会重新归一化。因此，0.6、0.2、0.2 是基础权重，不代表每条记录始终严格使用这三个比例

3.结果放入上下文

![image-20261110102110144](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261110102110144.png)









生成的题目持久化了吗？持久化到了那张表的哪个字段？

* 持久化了，在回答记录表和评估记录表中存的都有  question字段
* 此外，完整的当前题目对象 `currentGeneratedQuestion` 也会随图状态保存到图状态表中的state字段

用户的作答记录持久化了吗？原始音频、视频、语音识别到的文字，都持久化了？持久化到了那张表的哪个字段？

* 持久化了文本：回答记录表的answer_text、评估记录表的answer
* 音频、视频帧和转录条目在 LangGraph 状态中存在，没有持久化到数据库，无法查看历史回放，但这个项目也没有说实现了这个功能，只是说具备工作流断点恢复，保存了检查点，当然具备断点恢复的能力；



音频

* WAV文件：WAV头 + 音频PCM数据，是二进制数据；体积小、内容完整、可直接播放
* 经过Base64编码，得到 WAV Base64，是文本字符串；大、编码整个WAV文件完整，只编码PCM不完整、内容完整+解码可以播放
* 编码是为了适配JSON和state状态字段



### 工具

特殊的终止工具 FinishChatTool：当思考完成时调用此工具，传递思考总结和关键信息，用于生成最终回答

联网搜索工具 WebSearchTool：获取与关键词相关的网页信息。用于验证技术栈、查询公司背景、了解行业趋势等

* 使用 tavily api 的轻量搜索
* 使用 JSONUtil 组装请求体、使用 RestClient 向 TAVILY API 发送 POST 请求、再把结果解析成干净的纯文本
* 因为大模型非常擅长阅读干净的 Markdown 格式文本，从而基于最新的联网信息来生成问题



### 后端接口

HTTP REST 和 WebSocket 两类接口

REST 负责面试结果的查询，WebSocket 负责整个面试过程中的实时交互

![image-20261109162900896](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109162900896.png)

![image-20261109162918851](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109162918851.png)

![image-20261109162929986](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109162929986.png)





### 前端

![image-20261109161022584](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109161022584.png)



### 音视频交互

![image-20261109172548650](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109172548650.png)

![image-20261109172652125](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109172652125.png)

![image-20261109172751228](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109172751228.png)

![image-20261109172905488](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109172905488.png)

![image-20261109173004794](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173004794.png)

![image-20261109173032629](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173032629.png)



### 工作流

#### 主图

![image-20261109173135807](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173135807.png)

![image-20261109173144767](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173144767.png)

![image-20261109173225376](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173225376.png)

![image-20261109173236216](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173236216.png)

#### 生题子图

![image-20261109173309924](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173309924.png)

![image-20261109173331059](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173331059.png)



#### 面试子图

![image-20261109173356333](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173356333.png)

![image-20261109173433477](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173433477.png)

![image-20261109173502347](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173502347.png)

![image-20261109173531041](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109173531041.png)

#### 工作流恢复

![image-20261109174252724](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109174252724.png)

![image-20261109174730402](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109174730402.png)

### 工程稳定性

![image-20261109175016482](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109175016482.png)

![image-20261109175241003](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109175241003.png)

![image-20261109175622267](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109175622267.png)

![image-20261109175653556](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\image-20261109175653556.png)

