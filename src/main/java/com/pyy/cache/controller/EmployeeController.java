package com.pyy.cache.controller;

import com.pyy.cache.bean.Employee;
import com.pyy.cache.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Administrator on 2018/7/3 0003.
 */
@RestController
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @GetMapping("/emp/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Integer id) {
        Employee emp = employeeService.getEmp(id);
        return new ResponseEntity<>(emp, HttpStatus.OK);
    }

    @PutMapping("/emp/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Integer id, @RequestBody Employee employee) {
        Employee emp = employeeService.updateEmp(id, employee);
        return new ResponseEntity<>(emp, HttpStatus.OK);
    }

}
