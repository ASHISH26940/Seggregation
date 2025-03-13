package models;
import java.util.*;

public class EmployeeGraph {
    private final Map<String, Set<String>> employeeRoles;     // Employee -> Roles
    private final Map<String, Set<String>> roleHierarchy;     // Parent Role -> Child Roles
    private final Map<String, Set<String>> rolePrivileges;    // Role -> Privileges
    private final Map<String, Set<String>> privilegeEntitlements; // Privilege -> Entitlements

    public EmployeeGraph() {
        employeeRoles = new HashMap<>();
        roleHierarchy = new HashMap<>();
        rolePrivileges = new HashMap<>();
        privilegeEntitlements = new HashMap<>();
    }

    // ✅ Add Employee-Role Mapping
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

    // ✅ Add Role Hierarchy Relationship
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

    // ✅ Add Role-Privilege Mapping
    public void addRolePrivilege(String role, String privilege) {
        role = role.trim();
        privilege = privilege.trim();

        if (role.isEmpty() || privilege.isEmpty()) {
            System.err.println("⚠ Skipping invalid role-privilege entry.");
            return;
        }

        rolePrivileges.putIfAbsent(role, new HashSet<>());
        rolePrivileges.get(role).add(privilege);
    }

    // ✅ Add Privilege-Entitlement Mapping
    public void addPrivilegeEntitlement(String privilege, String entitlement) {
        privilege = privilege.trim();
        entitlement = entitlement.trim();

        if (privilege.isEmpty() || entitlement.isEmpty()) {
            System.err.println("⚠ Skipping invalid privilege-entitlement entry.");
            return;
        }

        privilegeEntitlements.putIfAbsent(privilege, new HashSet<>());
        privilegeEntitlements.get(privilege).add(entitlement);
    }

    // ✅ Get all roles assigned to an employee
    public List<String> getRoles(String employee) {
        return new ArrayList<>(employeeRoles.getOrDefault(employee, new HashSet<>()));
    }

    // ✅ Get all employees
    public Set<String> getAllEmployees() {
        return employeeRoles.keySet();
    }

    // ✅ Get all child roles of a parent role (hierarchical lookup)
    public List<String> getChildRoles(String parentRole) {
        return new ArrayList<>(roleHierarchy.getOrDefault(parentRole, new HashSet<>()));
    }

    // ✅ Get all privileges of a given role (direct and inherited)
    public Set<String> getRolePrivileges(String role) {
        // Use a set to track visited roles to prevent infinite recursion
        return getRolePrivilegesHelper(role, new HashSet<>());
    }
    private Set<String> getRolePrivilegesHelper(String role, Set<String> visitedRoles) {
        // If we've already processed this role, skip it to prevent cycles
        if (visitedRoles.contains(role)) {
            return new HashSet<>();
        }
        
        // Mark this role as visited
        visitedRoles.add(role);
        
        // Get direct privileges for this role
        Set<String> privileges = new HashSet<>(rolePrivileges.getOrDefault(role, new HashSet<>()));
    
        // Traverse child roles and collect their privileges
        for (String childRole : getChildRoles(role)) {
            privileges.addAll(getRolePrivilegesHelper(childRole, visitedRoles));
        }
        return privileges;
    }

    // ✅ Get all entitlements from a given privilege
    public Set<String> getPrivilegeEntitlements(String privilege) {
        return privilegeEntitlements.getOrDefault(privilege, new HashSet<>());
    }

    // ✅ Get all entitlements of an employee (via roles → privileges → entitlements)
    public Set<String> getEmployeeEntitlements(String employee) {
        Set<String> entitlements = new HashSet<>();

        for (String role : getRoles(employee)) {
            for (String privilege : getRolePrivileges(role)) {
                entitlements.addAll(getPrivilegeEntitlements(privilege));
            }
        }
        return entitlements;
    }
}
