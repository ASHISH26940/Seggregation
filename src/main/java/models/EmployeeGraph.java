package models;
import java.util.*;

public class EmployeeGraph {
    private final Map<String, Set<String>> employeeRoles;  // Employee -> Roles
    private final Map<String, Set<String>> roleHierarchy;  // Parent Role -> Child Roles

    public EmployeeGraph() {
        employeeRoles = new HashMap<>();
        roleHierarchy = new HashMap<>();
    }

    // Add Employee-Role Mapping
    public void addRole(String employee, String role) {
        employee = employee.trim();
        role = role.trim();

        if (employee.isEmpty() || role.isEmpty()) {
            System.err.println("⚠ Skipping invalid entry (empty employee or role).");
            return;
        }

        employeeRoles.putIfAbsent(employee, new HashSet<>());
        employeeRoles.get(employee).add(role);
    }

    // Add Role Hierarchy Relationship
    public void addHierarchy(String childRole, String parentRole) {
        childRole = childRole.trim();
        parentRole = parentRole.trim();

        if (childRole.isEmpty() || parentRole.isEmpty()) {
            System.err.println("⚠ Skipping invalid hierarchy entry.");
            return;
        }

        roleHierarchy.putIfAbsent(parentRole, new HashSet<>());
        roleHierarchy.get(parentRole).add(childRole);
    }

    // Get all roles assigned to an employee
    public List<String> getRoles(String employee) {
        return new ArrayList<>(employeeRoles.getOrDefault(employee, new HashSet<>()));
    }

    // Get all employees
    public Set<String> getAllEmployees() {
        return employeeRoles.keySet();
    }

    // Get all child roles of a parent role (hierarchical lookup)
    public List<String> getChildRoles(String parentRole) {
        return new ArrayList<>(roleHierarchy.getOrDefault(parentRole, new HashSet<>()));
    }
}
