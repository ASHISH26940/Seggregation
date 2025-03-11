package sodchecker;

import detection.SoDViolationDetector;
import models.EmployeeGraph;
import utils.ExcelReader;
import utils.OutputGenerator;
import java.util.*;

public class SoDChecker {
    public static void main(String[] args) {
        // Initialize the employee graph
        EmployeeGraph graph = new EmployeeGraph();

        // Define file paths
        String userDetailsPath = "src/data/userDetails.xlsx";
        String userRoleMappingPath = "src/data/userRoleMapping.xlsx";
        String roleMasterDetailsPath = "src/data/roleMasterDetails.xlsx";
        String roleToRolePath = "src/data/roleToRole.xlsx";
        String privilegeMasterPath = "src/data/pvlgsMaster.xlsx";

        // Read data from Excel files using ExcelReader
        System.out.println("Reading Excel files...");
        List<String[]> userDetails = ExcelReader.readExcelFile(userDetailsPath);
        List<String[]> userRoleMapping = ExcelReader.readExcelFile(userRoleMappingPath);
        List<String[]> roleMasterDetails = ExcelReader.readExcelFile(roleMasterDetailsPath);
        List<String[]> roleToRole = ExcelReader.readExcelFile(roleToRolePath);
        List<String[]> privilegeMaster = ExcelReader.readExcelFile(privilegeMasterPath);
        
        // Log data counts
        System.out.println("Data loaded - Users: " + userDetails.size() + 
                           ", User-Role mappings: " + userRoleMapping.size() + 
                           ", Roles: " + roleMasterDetails.size() + 
                           ", Role hierarchies: " + roleToRole.size() + 
                           ", Privileges: " + privilegeMaster.size());

        // Build the employee-role graph
        buildEmployeeRoleGraph(graph, userDetails, userRoleMapping, roleMasterDetails);
        
        // Build the role hierarchy
        buildRoleHierarchy(graph, roleToRole, roleMasterDetails);
        
        // Build the role-privilege relationships
        buildRolePrivilegeRelationships(graph, privilegeMaster, roleMasterDetails);

        // Initialize the SoD violation detector as a separate component
        SoDViolationDetector detector = new SoDViolationDetector(graph);
        
        // Run the violation detection
        System.out.println("Detecting SoD violations...");
        List<String> violations = detector.detectConflicts();

        // Output the results
        System.out.println("Found " + violations.size() + " potential SoD violations");
        OutputGenerator.generateCSV(violations, "output.csv");
        System.out.println("Results saved to output.csv");
    }
    
    private static void buildEmployeeRoleGraph(EmployeeGraph graph, 
                                             List<String[]> userDetails,
                                             List<String[]> userRoleMapping,
                                             List<String[]> roleMasterDetails) {
        System.out.println("Building employee-role graph...");
        
        // Skip header rows
        List<String[]> userDetailsData = new ArrayList<>(userDetails);
        List<String[]> userRoleMappingData = new ArrayList<>(userRoleMapping);
        List<String[]> roleMasterData = new ArrayList<>(roleMasterDetails);
        
        if (!userDetailsData.isEmpty()) userDetailsData.remove(0);
        if (!userRoleMappingData.isEmpty()) userRoleMappingData.remove(0);
        if (!roleMasterData.isEmpty()) roleMasterData.remove(0);
        
        for (String[] entry : userRoleMappingData) {
            if (entry.length < 3) {
                System.out.println("⚠ Skipping incomplete user-role mapping record");
                continue;
            }
            
            String roleID = entry[0]; // ROLE_ID
            String userID = entry[2]; // USER_ID

            String employeeName = getUserNameByID(userDetailsData, userID);
            String roleName = getRoleNameByID(roleMasterData, roleID);

            if (employeeName != null && roleName != null) {
                graph.addRole(employeeName, roleName);
                System.out.println("Added role mapping: " + employeeName + " -> " + roleName);
            }
        }
    }
    
    private static void buildRoleHierarchy(EmployeeGraph graph,
                                         List<String[]> roleToRole,
                                         List<String[]> roleMasterDetails) {
        System.out.println("Building role hierarchy...");
        
        // Skip header rows
        List<String[]> roleToRoleData = new ArrayList<>(roleToRole);
        List<String[]> roleMasterData = new ArrayList<>(roleMasterDetails);
        
        if (!roleToRoleData.isEmpty()) roleToRoleData.remove(0);
        if (!roleMasterData.isEmpty()) roleMasterData.remove(0);
        
        for (String[] entry : roleToRoleData) {
            if (entry.length < 3) {
                System.out.println("⚠ Skipping incomplete role hierarchy record");
                continue;
            }
            
            String childRoleID = entry[1]; // CHILD_ROLE_ID
            String parentRoleID = entry[2]; // PARENT_ROLE_ID

            String childRole = getRoleNameByID(roleMasterData, childRoleID);
            String parentRole = getRoleNameByID(roleMasterData, parentRoleID);

            if (childRole != null && parentRole != null) {
                graph.addHierarchy(childRole, "Parent_" + parentRole);
                System.out.println("Added hierarchy: " + childRole + " -> Parent_" + parentRole);
            }
        }
    }
    
    private static void buildRolePrivilegeRelationships(EmployeeGraph graph,
                                                     List<String[]> privilegeMaster,
                                                     List<String[]> roleMasterDetails) {
        System.out.println("Building role-privilege relationships...");
        
        // Skip header rows
        List<String[]> privilegeData = new ArrayList<>(privilegeMaster);
        List<String[]> roleMasterData = new ArrayList<>(roleMasterDetails);
        
        if (!privilegeData.isEmpty()) privilegeData.remove(0);
        if (!roleMasterData.isEmpty()) roleMasterData.remove(0);
        
        for (String[] entry : privilegeData) {
            if (entry.length < 2) {
                System.out.println("⚠ Skipping incomplete privilege record");
                continue;
            }
            
            String privilegeName = entry[1]; // NAME field
            
            // For simplicity, we'll just link privileges to roles based on name matching
            // In a real implementation, you'd use the proper relationship tables
            for (String[] role : roleMasterData) {
                if (role.length < 2) continue;
                
                String roleName = role[1]; // ROLE_NAME
                if (roleName != null && 
                    (roleName.contains(privilegeName) || privilegeName.contains(roleName))) {
                    graph.addRole(roleName, "Privilege_" + privilegeName);
                    System.out.println("Added privilege mapping: " + roleName + " -> Privilege_" + privilegeName);
                }
            }
        }
    }

    private static String getUserNameByID(List<String[]> users, String userID) {
        for (String[] user : users) {
            if (user.length > 5 && user[5].equals(userID)) { // USER_ID is at index 5
                return user[2]; // USER_DISPLAY_NAME is at index 2
            }
        }
        System.out.println("⚠ UserID not found: " + userID);
        return null;
    }
    
    private static String getRoleNameByID(List<String[]> roles, String roleID) {
        for (String[] role : roles) {
            if (role.length > 1 && role[0].equals(roleID)) { // ROLE_ID is at index 0
                return role[1]; // ROLE_NAME is at index 1
            }
        }
        System.out.println("⚠ RoleID not found: " + roleID);
        return null;
    }
}