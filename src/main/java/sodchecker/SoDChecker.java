package sodchecker;

import detection.SoDViolationDetector;
import models.EmployeeGraph;
import utils.ExcelReader;
import utils.OutputGenerator;
import java.util.*;
import java.util.concurrent.*;

public class SoDChecker {
    public static void main(String[] args) {
        // Initialize the employee graph
        long startTime = System.currentTimeMillis();
        EmployeeGraph graph = new EmployeeGraph();

        // Define file paths
        // String userDetailsPath = "src/data/userDetails.xlsx";
        // String userRoleMappingPath = "src/data/userRoleMapping.xlsx";
        // String roleMasterDetailsPath = "src/data/roleMasterDetails.xlsx";
        // String roleToRolePath = "src/data/roleToRole.xlsx";
        // String privilegeMasterPath = "src/data/pvlgsMaster.xlsx";

        if (args.length < 5) {
            System.err.println("Usage: java -jar app.jar <userDetailsPath> <userRoleMappingPath> <roleMasterDetailsPath> <roleToRolePath> <privilegeMasterPath> <outputPath>");
            System.exit(1);
        }

        String userDetailsPath = args[0];
        String userRoleMappingPath = args[1];
        String roleMasterDetailsPath = args[2];
        String roleToRolePath = args[3];
        String privilegeMasterPath = args[4];
        String outputPath = args[5];

        // Create an executor service with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<List<String[]>>> futures = new ArrayList<>();

        // Read data from Excel files using ExcelReader in parallel
        System.out.println("Reading Excel files in parallel...");
        futures.add(executor.submit(() -> ExcelReader.readExcelFile(userDetailsPath)));
        futures.add(executor.submit(() -> ExcelReader.readExcelFile(userRoleMappingPath)));
        futures.add(executor.submit(() -> ExcelReader.readExcelFile(roleMasterDetailsPath)));
        futures.add(executor.submit(() -> ExcelReader.readExcelFile(roleToRolePath)));
        futures.add(executor.submit(() -> ExcelReader.readExcelFile(privilegeMasterPath)));

        try {
            // Get the results from all futures
            List<String[]> userDetails = futures.get(0).get();
            List<String[]> userRoleMapping = futures.get(1).get();
            List<String[]> roleMasterDetails = futures.get(2).get();
            List<String[]> roleToRole = futures.get(3).get();
            List<String[]> privilegeMaster = futures.get(4).get();
            
            // Log data counts
            System.out.println("Data loaded - Users: " + userDetails.size() + 
                               ", User-Role mappings: " + userRoleMapping.size() + 
                               ", Roles: " + roleMasterDetails.size() + 
                               ", Role hierarchies: " + roleToRole.size() + 
                               ", Privileges: " + privilegeMaster.size());

            // Process the data in parallel using CountDownLatch to wait for all tasks to complete
            CountDownLatch latch = new CountDownLatch(3);
            
            // Build the employee-role graph in a thread
            executor.submit(() -> {
                try {
                    buildEmployeeRoleGraph(graph, userDetails, userRoleMapping, roleMasterDetails);
                } finally {
                    latch.countDown();
                }
            });
            
            // Build the role hierarchy in a thread
            executor.submit(() -> {
                try {
                    buildRoleHierarchy(graph, roleToRole, roleMasterDetails);
                } finally {
                    latch.countDown();
                }
            });
            
            // Build the role-privilege relationships in a thread
            executor.submit(() -> {
                try {
                    buildRolePrivilegeRelationships(graph, privilegeMaster, roleMasterDetails);
                } finally {
                    latch.countDown();
                }
            });
            
            // Wait for all three processing tasks to complete
            latch.await();
            
            // Initialize the SoD violation detector as a separate component
            SoDViolationDetector detector = new SoDViolationDetector(graph);
            
            // Run the violation detection
            System.out.println("Detecting SoD violations...");
            List<String[]> violations = detector.detectConflicts();

            // Output the results
            System.out.println("Found " + violations.size() + " potential SoD violations");
            OutputGenerator.generateExcel(violations, outputPath);
            
            //OutputGenerator.generateExcel(violations, "output.xlsx");
            System.out.println("Results saved to output.csv");
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error in parallel processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Shutdown the executor service
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            long endTime = System.currentTimeMillis(); // End time tracking
            System.out.println("Total time taken: " + (endTime - startTime) + " ms");
        }
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
        
        // Create a thread-safe map to store user names and role names for parallel lookup
        ConcurrentMap<String, String> userIdToNameMap = new ConcurrentHashMap<>();
        ConcurrentMap<String, String> roleIdToNameMap = new ConcurrentHashMap<>();
        
        // Precompute maps for faster lookups
        for (String[] user : userDetailsData) {
            if (user.length > 5) {
                userIdToNameMap.put(user[5], user[2]); // Map USER_ID to USER_DISPLAY_NAME
            }
        }
        
        for (String[] role : roleMasterData) {
            if (role.length > 1) {
                roleIdToNameMap.put(role[0], role[1]); // Map ROLE_ID to ROLE_NAME
            }
        }
        
        // Process user-role mappings in parallel chunks
        int chunkSize = Math.max(1, userRoleMappingData.size() / 4);
        List<List<String[]>> chunks = new ArrayList<>();
        
        for (int i = 0; i < userRoleMappingData.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, userRoleMappingData.size());
            chunks.add(userRoleMappingData.subList(i, end));
        }
        
        // Process each chunk in parallel
        CountDownLatch chunkLatch = new CountDownLatch(chunks.size());
        for (List<String[]> chunk : chunks) {
            Thread t = new Thread(() -> {
                try {
                    for (String[] entry : chunk) {
                        if (entry.length < 3) {
                            System.out.println("⚠ Skipping incomplete user-role mapping record");
                            continue;
                        }
                        
                        String roleID = entry[0]; // ROLE_ID
                        String userID = entry[2]; // USER_ID
                        
                        String employeeName = userIdToNameMap.get(userID);
                        String roleName = roleIdToNameMap.get(roleID);
                        
                        if (employeeName != null && roleName != null) {
                            synchronized (graph) {
                                graph.addRole(employeeName, roleName);
                            }
                            System.out.println("Added role mapping: " + employeeName + " -> " + roleName);
                        } else {
                            if (employeeName == null) {
                                System.out.println("⚠ UserID not found: " + userID);
                            }
                            if (roleName == null) {
                                System.out.println("⚠ RoleID not found: " + roleID);
                            }
                        }
                    }
                } finally {
                    chunkLatch.countDown();
                }
            });
            t.start();
        }
        
        try {
            chunkLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while building employee-role graph: " + e.getMessage());
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
        
        // Create a thread-safe map for role name lookups
        Map<String, String> roleIdToNameMap = new ConcurrentHashMap<>();
        for (String[] role : roleMasterData) {
            if (role.length > 1) {
                roleIdToNameMap.put(role[0], role[1]); // Map ROLE_ID to ROLE_NAME
            }
        }
        
        // Process role hierarchies in parallel chunks
        int chunkSize = Math.max(1, roleToRoleData.size() / 4);
        List<List<String[]>> chunks = new ArrayList<>();
        
        for (int i = 0; i < roleToRoleData.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, roleToRoleData.size());
            chunks.add(roleToRoleData.subList(i, end));
        }
        
        // Process each chunk in parallel
        CountDownLatch chunkLatch = new CountDownLatch(chunks.size());
        for (List<String[]> chunk : chunks) {
            Thread t = new Thread(() -> {
                try {
                    for (String[] entry : chunk) {
                        if (entry.length < 3) {
                            System.out.println("⚠ Skipping incomplete role hierarchy record");
                            continue;
                        }
                        
                        String childRoleID = entry[1]; // CHILD_ROLE_ID
                        String parentRoleID = entry[2]; // PARENT_ROLE_ID
                        
                        String childRole = roleIdToNameMap.get(childRoleID);
                        String parentRole = roleIdToNameMap.get(parentRoleID);
                        
                        if (childRole != null && parentRole != null) {
                            synchronized (graph) {
                                graph.addHierarchy(childRole, parentRole);
                            }
                            System.out.println("Added hierarchy: " + childRole + " -> Parent_" + parentRole);
                        } else {
                            if (childRole == null) {
                                System.out.println("⚠ Child RoleID not found: " + childRoleID);
                            }
                            if (parentRole == null) {
                                System.out.println("⚠ Parent RoleID not found: " + parentRoleID);
                            }
                        }
                    }
                } finally {
                    chunkLatch.countDown();
                }
            });
            t.start();
        }
        
        try {
            chunkLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while building role hierarchy: " + e.getMessage());
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
        
        // Process privileges in parallel chunks
        int chunkSize = Math.max(1, privilegeData.size() / 4);
        List<List<String[]>> chunks = new ArrayList<>();
        
        for (int i = 0; i < privilegeData.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, privilegeData.size());
            chunks.add(privilegeData.subList(i, end));
        }
        
        // Create a thread-safe copy of role data
        final List<String[]> roleMasterDataFinal = new ArrayList<>(roleMasterData);
        
        // Process each chunk in parallel
        CountDownLatch chunkLatch = new CountDownLatch(chunks.size());
        for (List<String[]> chunk : chunks) {
            Thread t = new Thread(() -> {
                try {
                    for (String[] entry : chunk) {
                        if (entry.length < 2) {
                            System.out.println("⚠ Skipping incomplete privilege record");
                            continue;
                        }
                        
                        String privilegeName = entry[1]; // NAME field
                        
                        // For simplicity, we'll just link privileges to roles based on name matching
                        // In a real implementation, you'd use the proper relationship tables
                        for (String[] role : roleMasterDataFinal) {
                            if (role.length < 2) continue;
                            
                            String roleName = role[1]; // ROLE_NAME
                            if (roleName != null && 
                                (roleName.contains(privilegeName) || privilegeName.contains(roleName))) {
                                synchronized (graph) {
                                    graph.addRolePrivilege(roleName, privilegeName);
                                }
                                System.out.println("Added privilege mapping: " + roleName + " -> Privilege_" + privilegeName);
                            }
                        }
                    }
                } finally {
                    chunkLatch.countDown();
                }
            });
            t.start();
        }
        
        try {
            chunkLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted while building role-privilege relationships: " + e.getMessage());
        }
    }
}