package com.pyy.cache.service;

import com.pyy.cache.bean.Employee;
import com.pyy.cache.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2018/7/3 0003.
 */
@Cacheable(cacheNames = {"emp"}, cacheManager = "empCacheManager")
@Service
public class EmployeeService {
    @Autowired
    EmployeeMapper employeeMapper;

    /**
     * 将方法的结果存入缓存，以后查询相同的数据就会去缓存中获取，不会再去数据库查询
     *
     *  CacheManager管理多个Cache组件，对换的真正CRUD操作Cache组件中，每个缓存组件有自己唯一的名称
     *  Cacheable中的几个属性：
     *      cacheNames/value:执行缓存的名字
     *      key: 缓存数据使用的key，默认使用方法参数的值
     *          SpEL表达式： #id 参数id的值 #a0 #p0 #root.args[0]
     *      keyGenerator: key的生成器； key自己指定key的生成器组件id
     *      key/keyGenerator二选一使用
     *
     *      key是按照某种策略生产的默认使用keyGenerator生产的，默认使用SimpleKeyGenerator生成key
     *          SimpleKeyGenerator生成默认策略：
     *              如果没有参数:key=new SimpleKey();
     *              如果有一个参数：key=参数的值
     *              如果多个参数： key= new SimpleKey(params)
     *
     *      cacheManager：指定使用的缓存管理器
     *      或者cacheResolver指定获取解析器
     *
     *      condition：指定符合条件的情况下才缓存
     *              condition = "#id > 0" 当id大于0时缓存
     *      unless: 否定缓存；当unless指定的条件为true就不会被缓存；可以获取到结果进行判断
     *              unless = "#result == null"： 如果查询结果为空就不缓存
     *
     *      sync: 是否使用异步模式
     *
     * 原理：
     *  1、自动配置类：CacheAutoConfiguration 导入了11个缓存配置组件
     *  2、缓存的配置类
     *  org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.GuavaCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration
     *
     * 3、哪个配置类默认生效： SimpleCacheConfiguration
     *      在application.properties中开启debug=true
     *      配置好debug参数后，运行SpringBoot程序,可以看到控制台下打印出了自动配置的报
     * 4、默认给容器中注册了CacheManager：ConcurrentMapCacheManager
     * 5、这个组件可以获取和创建ConcurrentMapCache类型的缓存组件：将数据保存在ConcurrentMap中；
     *
     * @Cacheable 标注的方法执行之前先来检查缓存中有没有这个数据，默认按照参数的值为key去查询缓存，如果没有就运行方法，并将结果放入缓存
     *
     * 核心：
     *  1）、使用CacheManager【ConcurrentMapCacheManager】按照名字得到Cache【ConcurrentMapCache】组件
     *  2）、key使用keyGenerator生成的，默认是SimpleKeyGenerator
     *
     * @param id
     * @return
     */
    @Cacheable(cacheNames = {"emp"}, key="#id") // keyGenerator = "myKeyGenerator", condition = "#id > 0", unless = "#result == null")
    public Employee getEmp(Integer id){
        Employee emp = employeeMapper.getEmpById(id);

        // 获取某个缓存组件
        Cache empCache = empCacheManager.getCache("emp");
        empCache.put("emp:1", emp);

        return emp;
    }

    @Autowired
    @Qualifier("empCacheManager")
    CacheManager empCacheManager;

    /**
     *
     * @param employee
     * @return
     */
    @CachePut(value = {"emp"}, condition = "#result.id")
    public Employee updateEmp(Integer id, Employee employee) {
        employee.setdId(id);
        employeeMapper.updateEmp(employee);
        return employee;
    }
}











