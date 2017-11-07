package com.paxos.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

/**
 * Created by zhenzuo.zzz on 2017/11/2.
 *
 * @author zhenzuo.zzz
 * @date 2017/11/02
 */
@Endpoint(id = "person")
public class PersonEndpoint {

    private final Map<String, Person> people = new HashMap<>();

    PersonEndpoint() {
        this.people.put("mike", new Person("Michael Redlich"));
        this.people.put("rowena", new Person("Rowena Redlich"));
        this.people.put("barry", new Person("Barry Burd"));
    }

    @ReadOperation
    public List<Person> index() {
        return new ArrayList<>(this.people.values());
    }

    @ReadOperation
    public Person getPerson(@Selector String person) {
        return this.people.get(person);
    }

    @WriteOperation
    public void updatePerson(@Selector String name, String person) {
        this.people.put(name, new Person(person));
    }

    public static class Person {
        private String name;

        Person(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}