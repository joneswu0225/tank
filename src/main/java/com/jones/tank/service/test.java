package com.jones.tank.service;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class test {
    public static void main(String[] args) {
        HashMap<Integer, List<Student>> studentsMap = new HashMap<>();
        int row = 5;
        int column = 8;
        for (int i = 0; i < row; i++) {
            LinkedList<Student> list = new LinkedList<>();
            for (int j = 0; j < column; j++) {
                list.add(new Student(i * 10 + j, (i * 10 + j) + ""));
            }
            studentsMap.put(i, list);
        }

        // 现在把 map 解析出来组成一个链表
        List<Student> allStudents = studentsMap.entrySet().stream()
                .flatMap(map -> map.getValue().stream()
                        .map(e -> new Student(e.getId(), e.getName()))).collect(Collectors.toList());
        allStudents.forEach(System.out::println);

        // 此时并没有达到解包的目的，依然是二维链表
        List<Stream<Student>> streamList = studentsMap.entrySet().stream().map(e -> e.getValue().stream()
                        .map(v -> new Student(v.getId(), v.getName())))
                .collect(Collectors.toList());

        // 此时得到的是二维链表
        List<List<Student>> lists = studentsMap.values().stream().map(students -> students.stream()
                .map(v -> new Student(v.getId(), v.getName())).collect(Collectors.toList())).collect(Collectors.toList());

        // 等价于上面的内容
        List<List<Student>> lists2 = studentsMap.entrySet().stream().map(e -> e.getValue().stream()
                .map(v -> new Student(v.getId(), v.getName())).collect(Collectors.toList())).collect(Collectors.toList());

    }

    @Data
    @AllArgsConstructor
    public static class Student {
        private Integer id;
        private String name;
    }

}
