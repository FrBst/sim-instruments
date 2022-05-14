package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Person {
    int hid;
    int pid;
    List<Activity> activities = new ArrayList<>();
}
