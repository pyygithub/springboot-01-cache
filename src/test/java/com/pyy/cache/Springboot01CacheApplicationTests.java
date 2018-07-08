package com.pyy.cache;

import com.pyy.cache.bean.Employee;
import com.pyy.cache.mapper.EmployeeMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Springboot01CacheApplicationTests {

	@Autowired
	EmployeeMapper employeeMapper;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedisTemplate redisTemplate;

	@Autowired
	RedisTemplate empRedisTemplate;

	/**
	 * Redis 常见的五大数据类型
	 * String(字符串）、List(列表）、Set(集合)、Hash(散列)/ ZSet(有序集合)
	 */
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









	@Test
	public void contextLoads() {
		Employee employee = employeeMapper.getEmpById(1);
		System.out.println(employee);
	}

}
