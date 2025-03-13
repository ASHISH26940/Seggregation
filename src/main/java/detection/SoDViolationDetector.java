package detection;

import models.EmployeeGraph;
import java.util.*;

public class SoDViolationDetector {
    private final EmployeeGraph employeeGraph;

    public SoDViolationDetector(EmployeeGraph graph) {
        this.employeeGraph = graph;
    }

    // In SoDViolationDetector.java
    public List<String[]> detectConflicts() {
        List<String[]> violations = new ArrayList<>();
        Set<String> visitedRoles = new HashSet<>();

        System.out.println("Starting conflict detection...");
        System.out.println("Total employees: " + employeeGraph.getAllEmployees().size());
        
        // Check for cycles in the entire graph
        checkForCyclesInGraph();
        
        for (String employee : employeeGraph.getAllEmployees()) {
            List<String> roles = employeeGraph.getRoles(employee);
            System.out.println("Checking employee: " + employee + " with roles: " + roles);

            if (roles.size() < 2) {
                System.out.println("Employee " + employee + " has less than 2 roles, skipping conflict check");
                continue;  // No conflict possible with less than 2 roles
            }

            // 🔹 Ensure unique roles
            Set<String> uniqueRoles = new HashSet<>(roles);
            System.out.println("Unique roles: " + uniqueRoles);

            // 🔹 Conflict: Employee has multiple different roles with overlapping privileges
            boolean hasConflict = hasPrivilegeConflict(uniqueRoles);
            System.out.println("Has privilege conflict: " + hasConflict);
            
            if (hasConflict) {
                // Add as a String[] instead of a String
                violations.add(new String[]{
                    employee, 
                    "Privilege Conflict", 
                    String.join(",", uniqueRoles)
                });
            }

            // 🔹 Cycle Detection in Role Hierarchy (DFS on roles)
            for (String role : uniqueRoles) {
                if (detectRoleCycles(role, new HashSet<>(), visitedRoles)) {
                    // Add as a String[] instead of a String
                    violations.add(new String[]{
                        employee,
                        "Cycle Detected",
                        role
                    });
                }
            }
        }
    
        System.out.println("Detected " + violations.size() + " violations");
        return violations;
    }
    // private void printGraphState() {
    //     System.out.println("\n--- Graph State ---");
    //     System.out.println("Employees: " + employeeGraph.getAllEmployees().size());
        
    //     for (String employee : employeeGraph.getAllEmployees()) {
    //         System.out.println("Employee: " + employee);
    //         System.out.println("  Roles: " + employeeGraph.getRoles(employee));
            
    //         for (String role : employeeGraph.getRoles(employee)) {
    //             System.out.println("    Role: " + role);
    //             System.out.println("      Privileges: " + employeeGraph.getRolePrivileges(role));
    //             System.out.println("      Child Roles: " + employeeGraph.getChildRoles(role));
    //         }
    //     }
    //     System.out.println("--- End Graph State ---\n");
    // }

    private boolean detectRoleCycles(String role, Set<String> currentPath, Set<String> visitedRoles) {
        // If we've already visited this role and didn't find a cycle, no need to check again
        if (visitedRoles.contains(role) && !currentPath.contains(role)) {
            return false;
        }
        
        // If we encounter the role again in our current path, we've found a cycle
        if (currentPath.contains(role)) {
            return true;
        }
        
        // Add the role to our current path and visited set
        currentPath.add(role);
        visitedRoles.add(role);
        
        // Check all child roles
        List<String> childRoles = employeeGraph.getChildRoles(role);
        for (String childRole : childRoles) {
            if (detectRoleCycles(childRole, currentPath, visitedRoles)) {
                return true;
            }
        }
        
        // Remove the role from our current path as we backtrack
        currentPath.remove(role);
        return false;
    }

    private boolean hasPrivilegeConflict(Set<String> roles) {
        // Create a map to track which roles have which privileges
        Map<String, Set<String>> privilegeToRoles = new HashMap<>();
        
        // For each role, collect its privileges
        for (String role : roles) {
            // Get all privileges for this role (direct and inherited)
            Set<String> privileges = employeeGraph.getRolePrivileges(role);
            System.out.println("Role " + role + " has privileges: " + privileges);
            
            // Record which roles have which privileges
            for (String privilege : privileges) {
                privilegeToRoles.putIfAbsent(privilege, new HashSet<>());
                privilegeToRoles.get(privilege).add(role);
            }
        }
        
        // Check if any privilege is assigned via multiple roles
        for (Map.Entry<String, Set<String>> entry : privilegeToRoles.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println("Conflict detected: Privilege " + entry.getKey() + 
                                  " is assigned via multiple roles: " + entry.getValue());
                return true;
            }
        }
        
        return false;
    }
    private void checkForCyclesInGraph() {
        System.out.println("Checking for cycles in the entire role hierarchy...");
        Set<String> allRoles = new HashSet<>();
        
        // Collect all roles from the graph
        for (String employee : employeeGraph.getAllEmployees()) {
            allRoles.addAll(employeeGraph.getRoles(employee));
        }
        
        // Check each role for cycles
        Set<String> visitedRoles = new HashSet<>();
        for (String role : allRoles) {
            if (detectRoleCycles(role, new HashSet<>(), visitedRoles)) {
                System.out.println("WARNING: Cycle detected starting from role: " + role);
            }
        }
    }
}
