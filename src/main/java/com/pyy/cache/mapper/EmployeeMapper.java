package com.pyy.cache.mapper;

import com.pyy.cache.bean.Employee;
import org.apache.ibatis.annotations.*;

/**
 * Created by Administrator on 2018/7/3 0003.
 */
@Mapper
public interface EmployeeMapper {

    @Select("select * from employee where id = #{id}")
    public Employee getEmpById(Integer id);

    @Update("update employee set lastName=#{lastName}, email=#{email}, gender=#{gender}, d_id=#{dId} where id=#{id}")
    public void updateEmp(Employee emp);

    @Delete("delete from employee where id = #{id}")
    public void deleteEmp(Integer id);

    @Insert("insert into employee(lastName, email, gender, d_id) values(#{lastName},#{email}, #{gender}, #{dId})")
    public void insertEmp(Employee employee);
}
