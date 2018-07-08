# Redis简介
Redis 是一个开源（BSD许可）的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。
#搭建Redis环境
**1. 安装redis：使用Docker （使用docker中国加速**
```shell
[root@localhost ~]# docker pull registry.docker-cn.com/library/redis
Using default tag: latest
latest: Pulling from library/redis
683abbb4ea60: Already exists 
259238e792d8: Pull complete 
78399601c709: Pull complete 
f397da474601: Pull complete 
c57de4edc390: Pull complete 
b2ea05c9d9a1: Pull complete 
Digest: sha256:5534b92530acc653f0721ebfa14f31bc718f68bf9070cbba25bb00bc7aacfabb
Status: Downloaded newer image for registry.docker-cn.com/library/redis:latest
```
使用docker images查询
```
[root@localhost ~]# docker images
REPOSITORY                             TAG                 IMAGE ID            CREATED             SIZE
registry.docker-cn.com/library/redis   latest              71a81cb279e3        7 days ago          83.4MB
mysql                                  5.7                 66bc0f66b7af        7 days ago          372MB
```
使用docker启动redis
```shell
[root@localhost ~]# docker run -d -p 6379:6379 --name myredis registry.docker-cn.com/library/redis
ba65a5f3fc5c996ea23582a0cfeb0275be759c1a19fc920dd6f513127b5c9738
```
使用docker ps -a 查询运行情况
```shell
[root@localhost ~]# docker ps -a
CONTAINER ID        IMAGE                                  COMMAND                  CREATED              STATUS                       PORTS                    NAMES
ba65a5f3fc5c        registry.docker-cn.com/library/redis   "docker-entrypoint..."   About a minute ago   Up About a minute            0.0.0.0:6379->6379/tcp   myredis
```
**2. 引入redis依赖包**
```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>
```
**3. 配置redis**
```
# redis配置项
# Redis数据库索引（默认为0）
spring.redis.database=0  
# Redis服务器地址
spring.redis.host=192.168.43.53
# Redis服务器连接端口
spring.redis.port=6379  
# Redis服务器连接密码（默认为空）
spring.redis.password=  
```
引入redis依赖包后系统就会自动引入RedisAutoConfiguration完成redis相关配置。其中就包括自动配置好了，RedisTemplate 、StringRedisTemplage。我们就可以在项目中直接使用。
**4. 编写redis测试方法**

Redis 常见的五大数据类型：
String(字符串）、List(列表）、Set(集合)、Hash(散列)/ ZSet(有序集合)

stringRedisTemplate.opsForValue()：【String（字符串）】
stringRedisTemplate.opsForList()：【List（列表）】
stringRedisTemplate.opsForSet()：【Set（集合）】
stringRedisTemplate.opsForHash()：【Hash（散列）】
stringRedisTemplate.opsForZSet():【有序集合】
```java
/**
	 * Redis 常见的五大数据类型
	 * String(字符串）、List(列表）、Set(集合)、Hash(散列)/ ZSet(有序集合)
	 */
    @Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedisTemplate redisTemplate;

	@Autowired
	RedisTemplate empRedisTemplate; // 自定义RedisTemplate

	@Test
	public void test01() {
		// 给redis存字符串数据
		stringRedisTemplate.opsForValue().append("msg", "hello");
		stringRedisTemplate.opsForList().leftPush("list", "v1");
		stringRedisTemplate.opsForList().leftPush("list", "v2");
		stringRedisTemplate.opsForList().leftPush("list", "v3");
		stringRedisTemplate.opsForList().leftPush("list", "v4");

		String msg = stringRedisTemplate.opsForValue().get("msg");
		System.out.println(msg);

		// 给redis存对象数据
		Employee employee = employeeMapper.getEmpById(1);
		// 这里使用默认的JDK序列化器：JdkSerializationRedisSerializer 将序列化后的数据保存到redis中
		// 我们也可以自定义序列化器完成序列化存储
		// 1. 自动手动将对象序列化为JSON字符串
		// 2. 指定redisTemplate默认的序列化器
		redisTemplate.opsForValue().set("emp", employee);

		empRedisTemplate.opsForValue().set("emp_json", employee);
	}
```
自定义序列化器：
```java
/**
 * Created by Administrator on 2018/7/4 0004.
 */
@Configuration
public class MyRedisConfig {

    @Bean
    public RedisTemplate<Object, Employee> empRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Employee> template = new RedisTemplate<Object, Employee>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Employee> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Employee.class);
        template.setDefaultSerializer(jackson2JsonRedisSerializer);
        return template;
    }
}
```
5. 使用Redis测试SpringBoot缓存
  原理：系统使用CacheManager（ConcurrentMapCacheManager默认）来创建Cache组件，来完成缓存的CRUD操作。
- 默认情况下系统使用SimpleCacheConfiguration来引入ConcurrentMapCacheManager缓存管理器--》ConcurrentMapCache作为缓存组件
- 引入了redis的starter后容器中保存的是RedisCacheManager--》RedisCache作为缓存组件（通过操作redis缓存数据）， 默认保存数据 k-v 都是Object 默认利用jdk序列化保存

所以我们需要自定义CacheManager（使用我们自己的序列化器）：
重写RedisCacheConfiguration类中的方法：
```
 @Bean
    public RedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setUsePrefix(true);
        List cacheNames = this.cacheProperties.getCacheNames();
        if(!cacheNames.isEmpty()) {
            cacheManager.setCacheNames(cacheNames);
        }

        return (RedisCacheManager)this.customizerInvoker.customize(cacheManager);
    }
```
```
@Configuration
public class MyRedisConfig {

    @Bean
    public RedisTemplate<Object, Employee> empRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Employee> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Employee> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Employee.class);
        template.setDefaultSerializer(jackson2JsonRedisSerializer);
        return template;
    }

    @Bean
    public RedisTemplate<Object, Department> deptRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Department> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Department> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Department.class);
        template.setDefaultSerializer(jackson2JsonRedisSerializer);
        return template;
    }


    @Bean
    public RedisCacheManager empCacheManager(RedisTemplate<Object, Employee> empRedisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(empRedisTemplate);
        // 使用前綴，默认使用cacheNames作为前缀
        cacheManager.setUsePrefix(true);

        return cacheManager;
    }

    @Bean
    public RedisCacheManager deptCacheManager(RedisTemplate<Object, Department> deptRedisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(deptRedisTemplate);
        // 使用前綴，默认使用cacheNames作为前缀
        cacheManager.setUsePrefix(true);

        return cacheManager;
    }

    @Primary
    @Bean
    public RedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setUsePrefix(true);

        return cacheManager;
    }
}
```
在使用RedisCache时使用注解指定CacheManager:
```
@Cacheable(cacheNames = {"emp"}, cacheManager = "empCacheManager")
@Service
public class EmployeeService {
```
手动编码操作缓存：
```
   @Autowired
    @Qualifier("empCacheManager")
    CacheManager empCacheManager;


    public Employee getEmp(Integer id){
        Employee emp = employeeMapper.getEmpById(id);

        // 获取某个缓存组件
        Cache empCache = empCacheManager.getCache("emp");
        empCache.put("emp:1", emp);

        return emp;
    }
```


Github：https://github.com/pyygithub/springboot-01-cache