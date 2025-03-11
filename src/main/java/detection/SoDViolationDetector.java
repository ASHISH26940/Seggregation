package detection;

import models.EmployeeGraph;
import java.util.*;

public class SoDViolationDetector {
    private final EmployeeGraph employeeGraph;

    public SoDViolationDetector(EmployeeGraph graph) {
        this.employeeGraph = graph;
    }

    public List<String> detectConflicts() {
        List<String> violations = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (String employee : employeeGraph.getAllEmployees()) {
            List<String> roles = employeeGraph.getRoles(employee);
            
            // 🔹 Ensure unique roles
            Set<String> uniqueRoles = new HashSet<>(roles);

            // 🔹 Conflict: Employee has multiple *different* roles
            if (uniqueRoles.size() > 1) { 
                violations.add(employee + ", Conflict Detected: " + String.join(" <-> ", uniqueRoles));
            }

            // 🔹 Cycle Detection (DFS)
            if (detectCycles(employee, new HashSet<>(), visited)) {
                violations.add(employee + ", Cycle detected in role hierarchy");
            }
        }
        return violations;
    }

    private boolean detectCycles(String employee, Set<String> currentPath, Set<String> visited) {
        if (currentPath.contains(employee)) {
            return true; // 🔹 Cycle detected
        }

        if (visited.contains(employee)) {
            return false; // 🔹 Already checked, no cycle
        }

        visited.add(employee);
        currentPath.add(employee);

        for (String role : employeeGraph.getRoles(employee)) {
            if (detectCycles(role, currentPath, visited)) {
                return true;
            }
        }

        currentPath.remove(employee);
        return false;
    }
}
